package dev.openrune.dodian

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.toml.TomlMapper
import dev.openrune.cache.CONFIGS
import dev.openrune.cache.DBROW
import dev.openrune.cache.DBTABLE
import dev.openrune.cache.DBTABLEINDEX
import dev.openrune.cache.tools.TaskPriority
import dev.openrune.cache.tools.tasks.CacheTask
import dev.openrune.definition.codec.DBRowCodec
import dev.openrune.definition.codec.DBTableCodec
import dev.openrune.definition.codec.DBTableIndexCodec
import dev.openrune.definition.constants.ConstantProvider
import dev.openrune.definition.type.DBColumnType
import dev.openrune.definition.type.DBRowType
import dev.openrune.definition.type.DBTableIndexColumn
import dev.openrune.definition.type.DBTableIndexKey
import dev.openrune.definition.type.DBTableIndexType
import dev.openrune.definition.util.BaseVarType
import dev.openrune.filesystem.Cache
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import java.io.File

/**
 * Adds the quests from `.data/dodian/quests.toml` to the cache's `quest` table in dodian mode.
 *
 * The rows are appended rather than packed as a table: the client draws the quest tab by looking up
 * quest ids 1..`db_listall(quest)` with `db_find(quest:id)`, so each Dodian quest gets the next free quest
 * id and is added to the table's master index and column indexes next to the OSRS rows (which the
 * quest manager hides). Every row starts as a copy of its `template` quest, keeping only the list
 * columns (release type, type, location, release date) before the toml values are applied.
 */
public class DodianQuests : CacheTask() {
    override val priority: TaskPriority = TaskPriority.NORMAL

    private val questFile = File("../.data/dodian/quests.toml")
    private val rowCodec = DBRowCodec()
    private val indexCodec = DBTableIndexCodec()

    override fun init(cache: Cache) {
        if (!DodianMapPaths.isDodian || !questFile.exists()) {
            return
        }
        val quests = TomlMapper().readTree(questFile).path("quest").toList()
        if (quests.isEmpty()) {
            return
        }
        val table = DBTableCodec().loadData(QUEST_TABLE, cache.data(CONFIGS, DBTABLE, QUEST_TABLE))
        val ownRows = quests.map { mapping(it, "row") }.toSet()
        val master = readIndex(cache, 0)
        val osrsRows = master.columns.single().valueToRowIds.values.flatten().toSet() - ownRows
        val nextId = osrsRows.size + 1
        val highestOsrsId = readIndex(cache, COL_ID + 1).columns.firstOrNull()?.valueToRowIds
            ?.filterValues { ids -> ids.any { it in osrsRows } }?.keys
            ?.maxOfOrNull { (it as DBTableIndexKey.IntKey).value }
        check(highestOsrsId == null || highestOsrsId < nextId) {
            "OSRS quest ids go up to $highestOsrsId but there are only ${osrsRows.size} OSRS rows; " +
                "Dodian quest id $nextId would clash."
        }

        val rows = quests.mapIndexed { index, quest -> buildRow(cache, quest, nextId + index, table.columns) }
        for (row in rows) {
            val writer = Unpooled.buffer(256)
            with(rowCodec) { writer.encode(row) }
            cache.write(CONFIGS, DBROW, row.id, writer.toByteArray())
        }
        writeIndexes(cache, rows, osrsRows + rows.map { it.id })
        println("Dodian quests: ${rows.size} quest(s) from quest id $nextId")
    }

    private fun buildRow(
        cache: Cache,
        quest: JsonNode,
        questId: Int,
        columns: Map<Int, DBColumnType>,
    ): DBRowType {
        val rowId = mapping(quest, "row")
        val templateId = mapping(quest, "template")
        val template = rowCodec.loadData(templateId, cache.data(CONFIGS, DBROW, templateId))
        val values = mutableMapOf<Int, Array<Any>>()
        val templateColumns = template.definedColumns()
        for (column in KEPT_TEMPLATE_COLUMNS) {
            templateColumns[column]?.values?.let { values[column] = it }
        }

        values[COL_ID] = arrayOf(questId)
        values[COL_SORTNAME] = arrayOf(quest.text("sortname"))
        values[COL_DISPLAYNAME] = arrayOf(quest.text("name"))
        values[COL_QUESTPOINTS] = arrayOf(quest.int("questpoints"))
        values[COL_UNSTARTEDSTATE] = arrayOf(0)
        values[COL_ENDSTATE] = arrayOf(quest.int("endstate"))
        if (quest.has("releasedate")) {
            values[COL_RELEASEDATE] = releaseDate(quest.text("releasedate"))
        }
        quest.optionalInt("difficulty")?.let { values[COL_DIFFICULTY] = arrayOf(it) }
        quest.optionalInt("length")?.let { values[COL_LENGTH] = arrayOf(it) }
        if (quest.has("members")) {
            val templateValue = values[COL_MEMBERS]?.firstOrNull()
            val members = quest.path("members").asBoolean()
            values[COL_MEMBERS] = arrayOf(if (templateValue is Boolean) members else if (members) 1 else 0)
        }
        if (quest.has("startnpc")) {
            values[COL_STARTNPC] = arrayOf(mapping(quest, "startnpc"))
        }
        if (quest.has("startcoord")) {
            values[COL_STARTCOORD] = arrayOf(packedCoord(quest.text("startcoord")))
        }

        val row = DBRowType(rowId)
        row.tableId = QUEST_TABLE
        row.ensureColumnStorage(columns.keys.max() + 1)
        for ((column, cells) in values) {
            val types = checkNotNull(columns[column]) { "quest table has no column $column" }.types
            row.field5306!![column] = IntArray(types.size) { types[it].id }
            row.columnTypes!![column] = Array(cells.size) { cells[it] }
        }
        return row
    }

    private fun writeIndexes(cache: Cache, rows: List<DBRowType>, allRows: Set<Int>) {
        val master =
            DBTableIndexType(
                columns =
                    listOf(
                        DBTableIndexColumn(
                            BaseVarType.INTEGER,
                            mapOf(DBTableIndexKey.IntKey(0) to allRows.sorted()),
                        )
                    )
            )
        writeIndex(cache, 0, master)

        val ownIds = rows.map { it.id }.toSet()
        for (file in cache.files(DBTABLEINDEX, QUEST_TABLE).filter { it > 0 }) {
            val column = file - 1
            val index = readIndex(cache, file)
            if (index.columns.isEmpty()) {
                continue
            }
            val buckets =
                index.columns.map { indexColumn ->
                    indexColumn.valueToRowIds
                        .mapValues { (_, ids) -> ids.filterNot { it in ownIds }.toMutableList() }
                        .toMutableMap()
                }
            for (row in rows) {
                val cells = row.columnTypes?.getOrNull(column) ?: continue
                for ((position, cell) in cells.withIndex()) {
                    val tuple = position % buckets.size
                    val key = cell?.let { indexKey(it, index.columns[tuple].valueType) } ?: continue
                    buckets[tuple].getOrPut(key) { mutableListOf() }.add(row.id)
                }
            }
            val updated =
                index.columns.mapIndexed { tuple, indexColumn ->
                    DBTableIndexColumn(
                        indexColumn.valueType,
                        buckets[tuple].filterValues { it.isNotEmpty() }.mapValues { it.value.sorted() },
                    )
                }
            writeIndex(cache, file, DBTableIndexType(columns = updated))
        }
    }

    private fun readIndex(cache: Cache, file: Int): DBTableIndexType =
        indexCodec.loadData(file, cache.data(DBTABLEINDEX, QUEST_TABLE, file))

    private fun writeIndex(cache: Cache, file: Int, index: DBTableIndexType) {
        val writer = Unpooled.buffer(1024)
        with(indexCodec) { writer.encode(index) }
        cache.write(DBTABLEINDEX, QUEST_TABLE, file, writer.toByteArray())
    }

    private fun indexKey(value: Any, type: BaseVarType): DBTableIndexKey? =
        when (type) {
            BaseVarType.INTEGER ->
                when (value) {
                    is Boolean -> DBTableIndexKey.IntKey(if (value) 1 else 0)
                    is Number -> DBTableIndexKey.IntKey(value.toInt())
                    else -> null
                }
            BaseVarType.LONG -> (value as? Number)?.let { DBTableIndexKey.LongKey(it.toLong()) }
            BaseVarType.STRING -> (value as? String)?.let { DBTableIndexKey.StringKey(it) }
            BaseVarType.ARRAY -> null
        }

    /** `day-month-year`, stored as the quest table's (day, month, year) tuple. */
    private fun releaseDate(text: String): Array<Any> {
        val parts = text.split('-').map(String::toInt)
        require(parts.size == 3) { "$questFile: releasedate must be day-month-year, got '$text'" }
        return arrayOf(parts[0], parts[1], parts[2])
    }

    /** Same `level_mapsquareX_mapsquareZ_localX_localZ` format as the spawn files. */
    private fun packedCoord(text: String): Int {
        val (level, squareX, squareZ, localX, localZ) = text.split('_').map(String::toInt)
        val x = squareX * 64 + localX
        val z = squareZ * 64 + localZ
        return (level shl 28) or (x shl 14) or z
    }

    private fun mapping(quest: JsonNode, field: String): Int = ConstantProvider.getMapping(quest.text(field))

    private fun JsonNode.text(field: String): String {
        val value = path(field).asText()
        require(value.isNotEmpty()) { "$questFile: every [[quest]] needs '$field'" }
        return value
    }

    private fun JsonNode.int(field: String): Int =
        checkNotNull(optionalInt(field)) { "$questFile: every [[quest]] needs '$field'" }

    private fun JsonNode.optionalInt(field: String): Int? = if (has(field)) path(field).asInt() else null

    private fun ByteBuf.toByteArray(): ByteArray =
        ByteArray(writerIndex()).also { getBytes(0, it) }

    private companion object {
        const val QUEST_TABLE = 0
        const val COL_ID = 0
        const val COL_SORTNAME = 1
        const val COL_DISPLAYNAME = 2
        const val COL_MEMBERS = 5
        const val COL_DIFFICULTY = 6
        const val COL_LENGTH = 7
        const val COL_RELEASEDATE = 9
        const val COL_STARTCOORD = 13
        const val COL_STARTNPC = 14
        const val COL_QUESTPOINTS = 17
        const val COL_UNSTARTEDSTATE = 18
        const val COL_ENDSTATE = 19
        val KEPT_TEMPLATE_COLUMNS = listOf(3, 4, COL_MEMBERS, COL_DIFFICULTY, COL_LENGTH, 8, 9)
    }
}
