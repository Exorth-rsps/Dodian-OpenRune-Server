# Opdracht: Dodian-gamemodus opzetten in OpenRune-Server

## Context

Dit is mijn fork van https://github.com/OpenRune/OpenRune-Server (OSRS-compatibele server, revision 241, Kotlin, fork van RSMod). Ik wil een eigen gamemodus **"dodian"** bouwen. Als die modus actief is:

- worden **alle originele NPC-spawns, item-spawns en areas** niet geladen, maar mijn eigen Dodian-spawns;
- wordt de **originele gebiedscontent** (areas, bosses, quests, events) niet geladen;
- blijft de **basis** werken (interfaces zoals bank/equipment/settings, skills, generic, combat, drops-systeem);
- draait mijn **eigen Dodian-content** (home, winkels, trainingsgebieden, dungeons, objectfuncties).

Als de modus `"normal"` is (standaard), moet de server zich **exact** gedragen zoals upstream OpenRune.

## Belangrijkste eis: updates van OpenRune moeten makkelijk binnen te halen blijven

Ik merge regelmatig `upstream/main` (OpenRune) in mijn fork via GitHub Desktop. Daarom:

1. **Raak zo min mogelijk upstream-bestanden aan.** Alles wat Dodian-specifiek is, komt in nieuwe mappen/bestanden.
2. Waar een upstream-bestand toch aangepast moet worden: **kleinst mogelijke wijziging** (bij voorkeur één regel of één hook), en markeer die met een commentaar `// DODIAN:` zodat ik ze bij merge-conflicten terugvind.
3. Houd een bestand **`DODIAN.md`** in de root bij met:
   - een lijst van elk aangepast upstream-bestand, wat er veranderd is en waarom;
   - hoe de modus aan/uit gaat;
   - waar Dodian-spawns en -content staan;
   - wat ik moet controleren na een upstream-merge.
4. Verander **nooit** de originele spawnbestanden in `.data/raw-cache/map/` — alleen lezen.

## Wat ik al weet over de codebase (controleer dit eerst zelf)

- `AGENTS.md` en `openwiki/` bevatten de architectuurdocumentatie. **Lees die eerst.**
- Serverconfig: `game.example.yml` / `game.yml`, ingelezen in `api/server-config/.../ServerConfig.kt`. Er bestaat nog geen `gamemode`-sleutel.
- NPC-spawns: TOML-bestanden in `.data/raw-cache/map/npcs/`, ingelezen door `or-cache/.../map/packing/MapNpcPacker.kt` (hard pad `../.data/raw-cache/map/npcs`). Idem `MapObjPacker.kt` (`map/objs`) en `MapAreaPacker.kt` (`map/area`). Ze worden bij `buildCache` in de cache gepakt.
- Content: alle mappen onder `content/` met een `build.gradle.kts` worden automatisch als module ingeladen (`settings.gradle.kts`, `includeProjects`). Scripts zijn `PluginScript`-subklassen, gevonden via een classpath-scan.
- Externe plugins: `plugins/`-map, `example-plugin` als voorbeeld.
- De server faalt bewust bij boot als er naar een niet-bestaande gameval verwezen wordt.

## Gewenst ontwerp (stel bij als de code iets anders vraagt — leg dan uit waarom)

1. **Gamemode-instelling**
   - Voeg `gamemode: "normal"` toe aan `game.example.yml` en een veld aan `ServerConfig` (enum `normal` / `dodian`, default `normal`).
   - Zorg dat de cache-build (`buildCache`) óók weet welke modus actief is (bijv. door `game.yml` te lezen of via een Gradle-property). Leg uit welke keuze je maakt.

2. **Spawns per modus**
   - Nieuwe mappen: `.data/dodian/map/npcs/`, `.data/dodian/map/objs/`, `.data/dodian/map/area/`.
   - Laat de packers het pad kiezen op basis van de modus (minimale wijziging in de packer-klassen, of één gedeelde helper).
   - Zet in elke map één klein voorbeeldbestand (bv. een paar NPC's op een home-locatie) zodat ik het formaat zie.

3. **Content aan/uit per modus**
   - Bedenk een **centrale** manier om hele content-modules uit te zetten in Dodian-modus (bijv. in de plugin-loader een filter op package of module, gestuurd door een lijst in een configbestand zoals `.data/dodian/disabled-content.toml`). **Niet** in honderden scripts losse `if`-checks toevoegen.
   - Begin met uitzetten van: `content/areas`, `content/bosses`, `content/quest`, `content/events`. Laat de rest aan.
   - Controleer of de server daarna nog boot; kijk welke afhankelijkheden (bijv. scripts die naar elkaar verwijzen) problemen geven en rapporteer die.

4. **Eigen Dodian-content**
   - Nieuwe module `content/dodian/` (met eigen `build.gradle.kts`), waarvan de scripts **alleen** draaien in Dodian-modus.
   - Voeg één eenvoudig voorbeeldscript toe: bijv. een object op de home-locatie dat met `onOpLoc1` een bericht toont of teleporteert, en een `::dodian`-cheatcommand dat laat zien dat de modus actief is.

## Werkwijze

- **Stap 0:** Verken de repo (AGENTS.md, openwiki, de genoemde bestanden). Geef me een kort plan met de exacte bestanden die je wilt aanpassen en wacht op mijn akkoord voordat je iets wijzigt.
- Werk op een nieuwe git-branch `dodian` (niet op `main`).
- Werk in kleine stappen, één commit per stap, met duidelijke commitberichten (`dodian: ...`).
- Na elke stap: bouwen en testen (`buildCache` en de server laten booten) in **beide** modi. Meld wat er werkt en wat niet.
- Leg dingen uit in eenvoudige taal; ik ben geen ervaren Kotlin-ontwikkelaar.
- Antwoord in het Nederlands.

## Na afloop wil ik

- Werkende `gamemode`-schakelaar in `game.yml`.
- Lege Dodian-wereld (alleen mijn voorbeeldspawns) die boot en waar ik in kan inloggen.
- Normale modus die nog precies werkt zoals upstream.
- `DODIAN.md` met uitleg en de lijst van aangepaste upstream-bestanden.
- Korte uitleg hoe ik zelf nieuwe spawns en objectfuncties toevoeg.
