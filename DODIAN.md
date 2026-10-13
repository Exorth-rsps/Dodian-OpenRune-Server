# Dodian-gamemodus

Deze fork van [OpenRune-Server](https://github.com/OpenRune/OpenRune-Server) heeft een extra
gamemodus **`dodian`**. In `normal` (standaard) gedraagt de server zich precies zoals upstream.

## Modus aan/uit zetten

1. Zet in `game.yml`:

   ```yaml
   gamemode: "dodian"   # of "normal"
   ```

2. Bouw de cache opnieuw — de NPC-, item- en area-spawns zitten ín de cache:

   ```
   gradlew buildCache
   ```

   Eenmalig een andere modus bouwen zonder `game.yml` te wijzigen kan met
   `gradlew buildCache -Pgamemode=dodian`.

3. Start de server: `gradlew run`.

**Van `dodian` terug naar `normal`:** in Dodian-modus worden de originele OSRS-kaarten echt uit de
cache verwijderd. Draai dan `gradlew freshCache` in plaats van `buildCache`; dat haalt de cache
opnieuw op en bouwt hem. Vergeet je dat, dan stopt `buildCache` met een melding.

De cache-build schrijft de gebouwde modus naar `.data/cache/gamemode.txt`. Klopt die niet met
`game.yml`, dan logt de server bij het opstarten een waarschuwing
(`... the cache was built for '...'`). Vergeten `buildCache` te draaien merk je dus meteen.

## Wat de modus doet

| | `normal` | `dodian` |
|---|---|---|
| Kaarten (terrein + objecten) | alle OSRS-kaarten | **alleen** `.data/raw-cache/map/dodian/*.pack` |
| NPC-spawns | `.data/raw-cache/map/npcs/` | `.data/dodian/map/npcs/` |
| Item-spawns (grond) | `.data/raw-cache/map/objs/` | `.data/dodian/map/objs/` |
| Areas (multi, wildy, …) | `.data/raw-cache/map/area/` | `.data/dodian/map/area/` |
| Content-scripts | alles behalve `content/dodian` | alles behalve de packages in `.data/dodian/disabled-content.toml` |

Uitgeschakeld in Dodian-modus (`.data/dodian/disabled-content.toml`):

- `org.rsmod.content.areas` — `content/areas`
- `org.rsmod.content.bosses` — `content/bosses`
- `org.rsmod.content.quest` — `content/quest`
- `org.rsmod.content.skills.shootingstars` — `content/events/shooting-stars` (de code staat in
  een `skills`-package, niet `events`)

Een package schakelt ook alles daaronder uit. Wil je meer uitzetten, voeg dan het package toe aan
die lijst (geen rebuild van de cache nodig, alleen herstarten).

**Hoe het werkt:** de code blijft gewoon gecompileerd (skills hebben bijv. quest-helpers nodig om te
compileren), maar de plugin-loader start de scripts uit die packages niet. Guice-modules
(`*Module.kt`) uit die mappen worden wél nog geladen; die bevatten alleen bindings, geen gedrag in
de wereld.

## Waar staat wat

- **Kaarten:** `.data/raw-cache/map/dodian/` — RSPSi `.pack`-bestanden. Het getal aan het eind van
  de naam is het basis-regio-id (`mainlands_9520.pack`). In Dodian-modus verwijdert de build alle
  OSRS-kaartvakken die niet in deze bestanden zitten (`DodianMapClear`) en pakt daarna deze
  bestanden in (`PackMaps`). Nieuwe `.pack` erbij → `gradlew buildCache`.
- **Startlocatie (tijdelijk):** `DodianHomeScript.kt`, `START = (2670, 3231, 0)`. Wie inlogt op een
  plek waar geen kaart is (bijv. de standaard-spawn in Lumbridge), wordt daarheen verplaatst.

- **Spawns:** `.data/dodian/map/npcs/`, `objs/`, `area/` — elk `.toml`-bestand in die map wordt
  ingelezen. `home.toml` is het voorbeeld (Yanille bank).
- **Content-uitsluitingen:** `.data/dodian/disabled-content.toml`.
- **Eigen content:** `content/dodian/` (package `org.rsmod.content.dodian`). Scripts daar starten
  **alleen** in Dodian-modus. Voorbeeld: `DodianHomeScript.kt`.

## Zelf toevoegen

### Een NPC of item spawnen

Maak een bestand in `.data/dodian/map/npcs/` (of `objs/`), bijv. `training.toml`:

```toml
[[spawn]]
npc = "npc.cow"
coords = "0_40_48_44_22"
```

`coords` = `level_vakX_vakZ_lokaalX_lokaalZ`. Reken een tegel `(x, z)` om met
`vak = x / 64` en `lokaal = x % 64`. Voorbeeld: tegel `(2615, 3092)` → `0_40_48_55_20`.
Voor items gebruik je `obj = "obj.coins"`. Namen (`npc.*`, `obj.*`) moeten bestaande gamevals zijn;
kijk bij twijfel in `.data/raw-cache/map/` hoe upstream ze noemt.

Daarna: `gradlew buildCache` en server herstarten.

### Een objectfunctie (klik op een object)

Voeg een `PluginScript` toe in `content/dodian/src/main/kotlin/org/rsmod/content/dodian/`.
Registreren is niet nodig, de server vindt hem zelf:

```kotlin
class MijnObject @Inject constructor() : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1("loc.naam_van_object") {
            mes("Je klikt op het object.")
            telejump(CoordGrid(2612, 3092, 0), TeleportType.Exempt)
        }
    }
}
```

Let op: elk object/elke NPC mag maar door één script per optie afgehandeld worden. Kies je een
object dat al in `content/generic` of `content/skills` wordt gebruikt, dan crasht de server bij het
opstarten met `Event with id already registered`. Objecten uit `content/areas` zijn in Dodian-modus
vrij, omdat die scripts dan niet starten.

## Aangepaste upstream-bestanden

Alle wijzigingen zijn gemarkeerd met `DODIAN:` (zoek daarop bij merge-conflicten).

| Bestand | Wijziging | Waarom |
|---|---|---|
| `api/server-config/.../ServerConfig.kt` | 1 regel: veld `gamemode` | modus inlezen uit `game.yml` |
| `game.example.yml` | sleutel `gamemode: "normal"` + uitleg | standaardwaarde en documentatie |
| `or-cache/.../map/packing/MapNpcPacker.kt` | 1 regel: `resourceDir` via `DodianMapPaths` | spawnmap per modus |
| `or-cache/.../map/packing/MapObjPacker.kt` | idem | idem |
| `or-cache/.../map/packing/MapAreaPacker.kt` | 1 regel: `areaDir` via `DodianMapPaths` | idem |
| `or-cache/.../pack/PluginPacks.kt` | 1 regel in `buildPackTasks`: `DodianMapPaths.mapTasks()` | kaarten per modus |
| `or-cache/build.gradle.kts` | 1 regel in `buildCache`: `-Pgamemode` doorgeven | modus eenmalig overschrijven |
| `server/shared/.../loader/PluginScriptLoader.kt` | 2 regels: `DodianScriptFilter` | content aan/uit per modus |
| `.data/.gitignore` | `!dodian/` | `.data/dodian/` in git houden |

Nieuwe (Dodian-eigen) bestanden — geen conflicten te verwachten:
`api/server-config/.../GameMode.kt`, `or-cache/.../dodian/` (`DodianMapPaths`, `DodianMapClear`,
`DodianMapGuard`),
`server/shared/.../loader/DodianScriptFilter.kt`, `content/dodian/`, `.data/dodian/`, `DODIAN.md`.

## Na een upstream-merge controleren

1. Zoek op `DODIAN:` — staan alle regels uit de tabel hierboven er nog?
2. Zijn de packers verplaatst of hernoemd (`or-cache/.../map/packing/`)? Dan het pad-regeltje
   opnieuw toepassen.
3. Heeft upstream een nieuwe content-map toegevoegd die in Dodian uit moet? Kijk naar nieuwe mappen
   onder `content/` en hun `package`-regel, en voeg die toe aan `disabled-content.toml`.
4. Bouw en test beide modi:
   - `gamemode: "normal"` → `gradlew buildCache` → `gradlew run` → server start, aantal scripts
     in de log (`Loaded N plugin script(s)`) is gelijk aan upstream.
   - `gamemode: "dodian"` → `gradlew buildCache` → `gradlew run` → log toont
     `Dodian gamemode: skipping scripts in [...]`, en in-game werkt `::dodian`.
5. Faalt het opstarten in Dodian-modus met `Event with id already registered`, dan bindt een nieuw
   upstream-script hetzelfde object/NPC als een Dodian-script.

## Bekende beperkingen

- **Startlocatie:** de server zet nieuwe spelers op de realm-spawn uit de central-database
  (Lumbridge). In Dodian-modus verplaatst `DodianHomeScript` iedereen die buiten de kaarten inlogt
  naar `START`. Respawn na doodgaan gaat nog naar de realm-respawn; `::dodian home` brengt je terug.
- **Wereldkaart:** de wereldkaart in het spel toont nog de OSRS-kaart.
- **Cache-tool slikt fouten in:** een fout in een cache-taak geeft vaak toch `BUILD SUCCESSFUL`.
  Kijk bij twijfel in de uitvoer naar `Exception`. (Upstream-gedrag.)
- **Areas:** in Dodian-modus bestaan alleen de areas uit `.data/dodian/map/area/`. Multi-combat,
  wildernis e.d. werken pas als je ze daar zelf definieert.
- **Upstream `content/areas`** bevat ook kleine dingen zoals deuren, trappen en NPC-dialogen van
  steden. Die werken in Dodian-modus niet; bouw wat je nodig hebt na in `content/dodian`.
