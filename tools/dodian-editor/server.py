"""Dodian editor: draw music regions and place npc spawns on the Dodian maps.

Run from anywhere:  python tools/dodian-editor/server.py
Needs `gradlew buildCache` in dodian mode first (it exports the maps and track list).
"""

import json
import re
import struct
import sys
import tomllib
import webbrowser
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

PORT = 7791
ROOT = Path(__file__).resolve().parents[2]
HERE = Path(__file__).resolve().parent
EXPORT_DIR = ROOT / ".data/cache/dodian-editor"
MUSIC_FILE = ROOT / ".data/dodian/music.toml"
AREA_DIR = ROOT / ".data/dodian/map/area"
EDITOR_AREA_FILE = AREA_DIR / "music_regions.toml"
NPC_DIR = ROOT / ".data/dodian/map/npcs"
NPC_FILE_PATTERN = re.compile(r"^[a-z0-9_-]+\.toml$")
DIRECTIONS = {"north", "northeast", "east", "southeast", "south", "southwest", "west", "northwest"}
DODIAN_GAMEVALS = ROOT / "content/dodian/src/main/resources/gamevals.toml"
AREA_RSCM = ROOT / ".data/gamevals/area.rscm"
BINARY_GAMEVALS = ROOT / ".data/gamevals-binary/gamevals.dat"
FIRST_CUSTOM_AREA_ID = 30100
NAME_PATTERN = re.compile(r"^[a-z0-9_]+$")

MUSIC_HEADER = """# Music per region in dodian mode. Run `gradlew buildCache` after changing this file.
# Edited by tools/dodian-editor (python tools/dodian-editor/server.py); hand edits are fine too.
#
# Every [[region]]:
#   area   = an area gameval; its shape is an [[area]] in .data/dodian/map/area/ and new names are
#            registered in content/dodian/src/main/resources/gamevals.toml under [gamevals.area]
#   tracks = OSRS music tracks (dbrow.music_*); with more than one they play shuffled
#
# Entering the area unlocks and plays its tracks, leaving it stops the music.
"""

NPC_HEADER = """# NPC spawns in dodian mode. Edited by tools/dodian-editor; hand edits are fine too, but the
# editor rewrites the whole file (comments included) when you save it there.
# coords = "level_mapsquareX_mapsquareZ_localX_localZ"; run `gradlew buildCache` after changes.
# Optional: face = "north" | "northeast" | ... (spawn direction), wander = tiles (0 = stands still);
# left out = the npc type's default. These two only need a server restart, no buildCache.
"""

AREA_HEADER = """# Music regions drawn with tools/dodian-editor. This file is rewritten on every save;
# put hand-made areas in another file in this folder.
"""


def read_binary_gamevals(table):
    data = BINARY_GAMEVALS.read_bytes()
    pos = 0
    (count,) = struct.unpack_from(">I", data, pos)
    pos += 4
    for _ in range(count):
        (length,) = struct.unpack_from(">H", data, pos)
        pos += 2
        name = data[pos:pos + length].decode()
        pos += length
        (entries,) = struct.unpack_from(">I", data, pos)
        pos += 4
        values = {}
        for _ in range(entries):
            (length,) = struct.unpack_from(">H", data, pos)
            pos += 2
            key, _, value = data[pos:pos + length].decode().partition("=")
            pos += length
            values[key] = int(value)
        if name == table:
            return values
    return {}


def load_toml(path):
    with path.open("rb") as file:
        return tomllib.load(file)


def known_areas():
    """Every area gameval name -> id from area.rscm and all gamevals.toml files."""
    areas = {}
    if AREA_RSCM.exists():
        for line in AREA_RSCM.read_text(encoding="utf-8").splitlines():
            key, sep, value = line.partition("=")
            if sep and value.strip().isdigit():
                areas[key.strip()] = int(value)
    for base in (ROOT / "content", ROOT / "api"):
        for path in base.rglob("gamevals.toml"):
            if "build" in path.relative_to(ROOT).parts:
                continue
            section = load_toml(path).get("gamevals", {}).get("area", {})
            areas.update({k: int(v) for k, v in section.items()})
    return areas


def read_area_files():
    """area name (without 'area.') -> {polygons, levels, file}"""
    shapes = {}
    for path in sorted(AREA_DIR.glob("*.toml")):
        for area in load_toml(path).get("area", []):
            name = str(area.get("area_id", "")).removeprefix("area.")
            entry = shapes.setdefault(name, {"polygons": [], "levels": area.get("levels", [0]), "file": path.name})
            for polygon in area.get("polygons", []):
                entry["polygons"].append([[int(x), int(y)] for x, y in polygon.get("vertices", [])])
    return shapes


def load_maps():
    if not (EXPORT_DIR / "maps.json").exists():
        raise ValueError(
            "Geen kaartdata gevonden. Zet gamemode op \"dodian\" in game.yml en draai `gradlew buildCache`."
        )
    return json.loads((EXPORT_DIR / "maps.json").read_text(encoding="utf-8"))


def npc_state():
    maps = load_maps()
    npcs_file = EXPORT_DIR / "npcs.json"
    if not npcs_file.exists():
        raise ValueError("Geen NPC-lijst gevonden. Draai `gradlew buildCache` in dodian-modus.")
    names = {v: k for k, v in read_binary_gamevals("npc").items()}
    npcs = []
    for npc in json.loads(npcs_file.read_text(encoding="utf-8"), strict=False):
        gameval = names.get(npc["id"])
        if gameval:
            npcs.append({
                "gameval": f"npc.{gameval}",
                "name": npc["name"],
                "combat": npc["combat"],
                "size": npc["size"],
            })
    npcs.sort(key=lambda n: (n["name"].lower(), n["combat"]))

    files = {}
    for path in sorted(NPC_DIR.glob("*.toml")):
        spawns = []
        for spawn in load_toml(path).get("spawn", []):
            level, mx, mz, lx, lz = (int(v) for v in str(spawn["coords"]).split("_"))
            spawns.append({
                "npc": spawn["npc"],
                "x": mx * 64 + lx,
                "y": mz * 64 + lz,
                "level": level,
                "face": str(spawn["face"]).replace("_", "").lower() if "face" in spawn else None,
                "wander": int(spawn["wander"]) if "wander" in spawn else None,
            })
        files[path.name] = spawns
    return {"maps": maps, "npcs": npcs, "files": files}


def npc_save(payload):
    known = {f"npc.{name}" for name in read_binary_gamevals("npc")}
    files = payload.get("files", {})
    for name, spawns in files.items():
        if not NPC_FILE_PATTERN.match(name):
            raise ValueError(f"Ongeldige bestandsnaam '{name}': alleen kleine letters, cijfers, - en _ (eindigt op .toml).")
        for spawn in spawns:
            if spawn.get("npc") not in known:
                raise ValueError(f"Onbekende NPC '{spawn.get('npc')}' in {name}.")
            if spawn.get("face") not in (None, "") and spawn["face"] not in DIRECTIONS:
                raise ValueError(f"Onbekende richting '{spawn['face']}' in {name}.")
            wander = spawn.get("wander")
            if wander not in (None, "") and not (isinstance(wander, int) and 0 <= wander <= 64):
                raise ValueError(f"Wander moet een getal van 0 t/m 64 zijn (in {name}).")
    for name in payload.get("deleted", []):
        path = NPC_DIR / name
        if NPC_FILE_PATTERN.match(name) and path.is_file():
            path.unlink()
    for name, spawns in files.items():
        lines = [NPC_HEADER]
        for spawn in spawns:
            x, y, level = int(spawn["x"]), int(spawn["y"]), int(spawn.get("level", 0))
            lines.append("[[spawn]]")
            lines.append(f'npc = "{spawn["npc"]}"')
            lines.append(f'coords = "{level}_{x >> 6}_{y >> 6}_{x & 63}_{y & 63}"')
            if spawn.get("face"):
                lines.append(f'face = "{spawn["face"]}"')
            if spawn.get("wander") not in (None, ""):
                lines.append(f"wander = {int(spawn['wander'])}")
            lines.append("")
        (NPC_DIR / name).write_text("\n".join(lines), encoding="utf-8", newline="\n")
    return {"ok": True}


def state():
    maps = load_maps()
    names = {v: k for k, v in read_binary_gamevals("dbrow").items()}
    tracks = []
    for track in json.loads((EXPORT_DIR / "tracks.json").read_text(encoding="utf-8"), strict=False):
        gameval = names.get(track["row"])
        if gameval:
            tracks.append({"gameval": f"dbrow.{gameval}", "name": track["name"], "hint": track["hint"]})
    tracks.sort(key=lambda t: t["name"].lower())

    shapes = read_area_files()
    regions = []
    music = load_toml(MUSIC_FILE) if MUSIC_FILE.exists() else {}
    used = set()
    for region in music.get("region", []):
        name = str(region.get("area", "")).removeprefix("area.")
        shape = shapes.get(name, {"polygons": [], "levels": [0, 1, 2, 3], "file": None})
        used.add(name)
        regions.append({
            "name": name,
            "tracks": list(region.get("tracks", [])),
            "levels": shape["levels"],
            "polygons": shape["polygons"],
            "editable": shape["file"] in (None, EDITOR_AREA_FILE.name),
            "file": shape["file"],
        })
    others = [
        {"name": name, "polygons": shape["polygons"], "file": shape["file"]}
        for name, shape in shapes.items()
        if name not in used
    ]
    return {"maps": maps, "tracks": tracks, "regions": regions, "otherAreas": others}


def save(payload):
    regions = payload.get("regions", [])
    names = set()
    for region in regions:
        name = region.get("name", "")
        if not NAME_PATTERN.match(name):
            raise ValueError(f"Ongeldige naam '{name}': alleen kleine letters, cijfers en _.")
        if name in names:
            raise ValueError(f"De naam '{name}' komt twee keer voor.")
        names.add(name)
        if not region.get("tracks"):
            raise ValueError(f"Regio '{name}' heeft nog geen nummers.")
        if region.get("editable", True):
            polygons = [p for p in region.get("polygons", []) if len(p) >= 3]
            if not polygons:
                raise ValueError(f"Regio '{name}' heeft nog geen vorm (minstens 3 punten).")
            if not region.get("levels"):
                raise ValueError(f"Regio '{name}' heeft geen enkele verdieping aangevinkt.")

    added = register_area_gamevals(names)

    lines = [MUSIC_HEADER]
    for region in regions:
        lines.append("[[region]]")
        lines.append(f'area = "area.{region["name"]}"')
        lines.append("tracks = [")
        lines.extend(f'    "{track}",' for track in region["tracks"])
        lines.append("]\n")
    MUSIC_FILE.write_text("\n".join(lines), encoding="utf-8", newline="\n")

    lines = [AREA_HEADER]
    for region in regions:
        if not region.get("editable", True):
            continue
        levels = ", ".join(str(int(level)) for level in sorted(region["levels"]))
        lines.append("[[area]]")
        lines.append(f'name = "Music: {region["name"]}"')
        lines.append(f'area_id = "area.{region["name"]}"')
        lines.append(f"levels = [{levels}]\n")
        for polygon in region["polygons"]:
            if len(polygon) < 3:
                continue
            lines.append("[[area.polygons]]")
            lines.append("vertices = [")
            lines.extend(f"    [{int(x)}, {int(y)}]," for x, y in polygon)
            lines.append("]\n")
    EDITOR_AREA_FILE.write_text("\n".join(lines), encoding="utf-8", newline="\n")
    return {"ok": True, "addedGamevals": added}


def register_area_gamevals(names):
    known = known_areas()
    missing = sorted(name for name in names if name not in known)
    if not missing:
        return []
    data = load_toml(DODIAN_GAMEVALS) if DODIAN_GAMEVALS.exists() else {}
    tables = data.get("gamevals", {})
    area_table = dict(tables.get("area", {}))
    used_ids = set(known.values())
    next_id = FIRST_CUSTOM_AREA_ID
    added = []
    for name in missing:
        while next_id in used_ids:
            next_id += 1
        area_table[name] = next_id
        used_ids.add(next_id)
        added.append(f"area.{name} = {next_id}")
    tables["area"] = area_table
    lines = []
    for table, values in tables.items():
        lines.append(f"[gamevals.{table}]")
        lines.extend(f"{key} = {int(value)}" for key, value in values.items())
        lines.append("")
    DODIAN_GAMEVALS.write_text("\n".join(lines), encoding="utf-8", newline="\n")
    return added


class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path in ("/", "/index.html"):
            self.send_file(HERE / "index.html", "text/html; charset=utf-8")
        elif self.path == "/api/state":
            self.send_json(state)
        elif self.path == "/api/npc-state":
            self.send_json(npc_state)
        elif self.path.startswith("/maps/"):
            file = EXPORT_DIR / Path(self.path.removeprefix("/maps/")).name
            self.send_file(file, "image/png")
        else:
            self.send_error(404)

    def do_POST(self):
        handlers = {"/api/save": save, "/api/npc-save": npc_save}
        handler = handlers.get(self.path)
        if handler is None:
            self.send_error(404)
            return
        length = int(self.headers.get("Content-Length", 0))
        payload = json.loads(self.rfile.read(length) or b"{}")
        self.send_json(lambda: handler(payload))

    def send_json(self, producer):
        try:
            body, status = json.dumps(producer()), 200
        except ValueError as error:
            body, status = json.dumps({"ok": False, "error": str(error)}), 400
        data = body.encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def send_file(self, path, content_type):
        if not path.is_file():
            self.send_error(404)
            return
        data = path.read_bytes()
        self.send_response(200)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(data)))
        self.send_header("Cache-Control", "no-store")
        self.end_headers()
        self.wfile.write(data)

    def log_message(self, *args):
        pass


def main():
    server = ThreadingHTTPServer(("127.0.0.1", PORT), Handler)
    url = f"http://127.0.0.1:{PORT}/"
    print(f"Dodian editor: {url}  (Ctrl+C to stop)")
    if "--no-browser" not in sys.argv:
        webbrowser.open(url)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass


if __name__ == "__main__":
    main()
