# Quest specificatie: The Darkened Shield

> **Instructie voor Claude Code:** implementeer deze quest in de bestaande codebase van deze OSRS private server.
> 1. Deze quest vervangt **Legends' Quest** als toegang tot de Legends' Guild. Zoek of Legends' Quest al in de codebase zit.
>    - **Zo ja:** laat alle bestaande checks op "Legends' Quest voltooid" (guild-poort, Cape of Legends, enz.) voortaan naar deze quest wijzen en schakel de oude questroute uit. Verwijder geen andere functionaliteit van de betrokken NPC's.
>    - **Zo nee:** maak een nieuwe varp/varbit aan volgens het bestaande quest-patroon.
> 2. Volg de bestaande patronen voor questregistratie, requirement-checks, quest journal, dialogen, NPC-handlers, combat/spawns en rewards.
> 3. Alle ID's staan als `TODO`. Zoek ze op in de bestaande definities. Bestaat een NPC, item of object nog niet, vraag het dan aan de ontwikkelaar. NPC-posities en spawns regelt de ontwikkelaar, behalve Nezikchened (wordt via code opgeroepen).
> 4. Neem de dialogen **letterlijk** over en werk de checklist onderaan af.

---

## 1. Overzicht

| Veld | Waarde |
|---|---|
| Naam | **The Darkened Shield** |
| Doel | Het schild van de Watchtower herstellen en toegang krijgen tot de Legends' Guild |
| Startpunt | Radimus Erkle, buiten de poort van de Legends' Guild |
| Quest points | 4 |
| Beloning | 4 QP, toegang tot de Legends' Guild, 2 XP-lampen |

### Requirements

| Requirement | Waarde |
|---|---|
| Quest points | 10 |
| Attack | 50 |
| Strength | 50 |
| Defence | 50 |
| Prayer | 42 |

Controleer de **base levels** (niet geboost).

### Het verhaal in het kort
Vier Powering crystals in de Watchtower houden een magisch schild boven Yanille in stand. Een van de crystals is gestolen door Viyeldi, een dark wizard die het als krachtbron gebruikt om demonen op te roepen op de heuvels ten oosten van de Fishing Guild. De speler confronteert hem en verslaat de demon Nezikchened. Viyeldi vlucht en laat het crystal achter, maar dat is grijs en leeg geworden. Na het reinigen bij een altaar plaatst de Watchtower Wizard het crystal terug en staat het schild weer. Radimus Erkle erkent de speler als Legend.

---

## 2. NPC's

| NPC | Locatie | NPC ID |
|---|---|---|
| Radimus Erkle | Buiten de poort van de Legends' Guild | `TODO` |
| Watchtower Wizard | Watchtower, noord van Yanille | `TODO` |
| Viyeldi | Bruine heuvels ten oosten van de Fishing Guild | `TODO` |
| Nezikchened | Wordt opgeroepen naast Viyeldi | `TODO` |

De Watchtower Wizard heeft mogelijk al dialogen vanuit de Watchtower-quest. Voeg de questdialoog toe als optie of laat die voorgaan zolang deze quest actief is. Breek de bestaande Watchtower-quest niet.

---

## 3. Items

| Item | Item ID | Rol |
|---|---|---|
| Powering crystal (grijs) | `TODO` (bestaand OSRS-item uit de Watchtower-quest, de **grijze** variant) | Het vervloekte, uitgedoofde crystal dat de speler na het gevecht krijgt |
| Powering crystal (cyaan) | `TODO` (bestaand OSRS-item uit de Watchtower-quest, de **cyaan** variant) | Het gereinigde, opgeladen crystal |
| Antique lamp | `TODO` (bestaand OSRS-item, het standaard XP-lamp-item van de server) | Beloning |

**Alle items bestaan in OSRS. Er worden geen nieuwe items aangemaakt.**

- **Grijs** = niet opgeladen / vervloekt door Viyeldi (stage 4)
- **Cyaan** = opgeladen / gereinigd bij het altaar (stage 5)

Beide crystals zijn in deze quest niet verhandelbaar. Als de Watchtower-quest dezelfde items gebruikt, zorg dan dat ontvangen/inleveren in deze quest geen stages van de Watchtower-quest beïnvloedt.

---

## 4. Stages

| Stage | Betekenis | Wordt gezet door |
|---|---|---|
| 0 | Niet gestart | – |
| 1 | Gestart, moet naar de Watchtower Wizard | Radimus |
| 2 | Wizard gesproken, moet naar Viyeldi | Watchtower Wizard |
| 3 | Viyeldi gesproken, Nezikchened opgeroepen | Viyeldi |
| 4 | Nezikchened verslagen, grijs Powering crystal ontvangen | Kill van Nezikchened |
| 5 | Crystal gereinigd bij altaar | Altaar |
| 6 | Crystal ingeleverd, schild hersteld | Watchtower Wizard |
| 7 | Quest voltooid | Radimus |

---

## 5. Dialogen

Notatie: `[Conditie]`, opties onder **Opties**, `{ACTIE}` voor code-acties.

### 5.1 Radimus Erkle

**[stage 0, requirements niet gehaald]**
> **Radimus Erkle:** Halt. Beyond this gate lies the Legends' Guild, and only true legends may enter.
> **Radimus Erkle:** You are not ready, adventurer. Come back when you have proven yourself.
> `{ACTIE: toon in de chatbox welke requirements ontbreken, bijv. "You need 10 Quest Points, 50 Attack, 50 Strength, 50 Defence and 42 Prayer to start this quest."}`

**[stage 0, requirements gehaald]**
> **Radimus Erkle:** Halt. Beyond this gate lies the Legends' Guild, and only true legends may enter.
> **Player:** How do I become a legend?
> **Radimus Erkle:** Not with words. Legends are made by deeds, the kind that people still tell stories about long after you're gone.
> **Radimus Erkle:** As it happens, I've received troubling news. The magical shield above Yanille has been flickering. The Watchtower that sustains it is in trouble.
> **Radimus Erkle:** If that shield falls, the ogres will overrun the city. Save Yanille, and the gate is yours.
>
> **Opties:**
> - `I'll go to the Watchtower.`
> - `Maybe another time.`
>
> *I'll go to the Watchtower.*
> **Player:** I'll go to the Watchtower.
> **Radimus Erkle:** Good. The tower stands just north of Yanille. Speak to the wizard there.
> **Radimus Erkle:** And adventurer... come back alive. Legends are no use to anyone dead.
> `{ACTIE: start quest, stage = 1}`
>
> *Maybe another time.*
> **Player:** Maybe another time.
> **Radimus Erkle:** Then the gate stays closed. I'll be here.

**[stage 1 t/m 5]**
> **Radimus Erkle:** Have you helped the Watchtower yet? The tower stands north of Yanille. The longer you wait, the weaker that shield becomes.

**[stage 6]**
> **Radimus Erkle:** Word travels fast. A demon slain, a dark wizard driven off, and the shield above Yanille shines as bright as ever.
> **Radimus Erkle:** You didn't come here asking to be called a legend. You went out and became one.
> **Radimus Erkle:** The gate of the Legends' Guild is open to you. Welcome, Legend.
> `{ACTIE: stage = 7, quest complete, rewards, quest complete-scherm}`

**[stage 7]**
> **Radimus Erkle:** Welcome back, Legend. The guild is yours to enter.

### 5.2 Watchtower Wizard

**[stage 1]**
> **Player:** Radimus Erkle sent me. He says the shield above Yanille is in trouble.
> **Watchtower Wizard:** Trouble? It's a disaster! One of the four powering crystals is gone. Stolen, right from under my nose!
> **Watchtower Wizard:** Without all four crystals, the shield fades. A few more days, and it will collapse completely. And then the ogres...
> **Player:** Do you know who took it?
> **Watchtower Wizard:** For the last few nights, I've seen a dark glow from the top of this tower. Far to the northeast, on the hills east of the Fishing Guild.
> **Watchtower Wizard:** Whoever is up there is using powerful magic. My crystal's magic, I'd bet my beard on it.
> **Watchtower Wizard:** Please, find whoever it is and bring the crystal back. But be careful. Anyone who steals from this tower is no ordinary thief.
> `{ACTIE: stage = 2}`

**[stage 2 t/m 3]**
> **Watchtower Wizard:** Have you found my crystal? Look on the hills east of the Fishing Guild. That's where the dark glow comes from.

**[stage 4, speler heeft grijs Powering crystal]**
> **Player:** I found your crystal. The thief was a dark wizard named Viyeldi.
> **Watchtower Wizard:** Viyeldi! I thought that name had been forgotten. Show me the crystal...
> **Watchtower Wizard:** Oh no. Oh no, no, no. It's grey and lifeless. His dark magic has drained every bit of power from it.
> **Watchtower Wizard:** I can't put it back like this. It would poison the whole shield.
> **Player:** Can you clean it?
> **Watchtower Wizard:** Not with magic. Dark magic this deep can only be driven out by faith. Take it to an altar and pray over it.
> **Watchtower Wizard:** It will take a strong spirit. But you've already faced a demon, so I think you'll manage.

**[stage 4, speler heeft geen grijs Powering crystal in inventory of bank]**
> **Watchtower Wizard:** You lost the crystal? Don't worry. The crystals are bound to this tower. Let me call it back...
> **Watchtower Wizard:** There. Still grey and lifeless, I'm afraid. Take it to an altar and pray over it.
> `{ACTIE: geef grijs Powering crystal}`

**[stage 5, speler heeft cyaan Powering crystal]**
> **Player:** I've cleansed the crystal.
> **Watchtower Wizard:** Let me see... It's shining again! Pure as the day I first placed it.
> **Watchtower Wizard:** Now, back where it belongs...
> `{ACTIE: verwijder cyaan Powering crystal. Optioneel: graphic/animatie. stage = 6}`
> **Watchtower Wizard:** Listen... can you hear the hum? The shield is whole again. Yanille is safe.
> **Watchtower Wizard:** I don't know how to thank you. But I know someone who will want to hear about this. Go back to Radimus Erkle.

**[stage 5, speler heeft geen cyaan Powering crystal in inventory of bank]**
> **Watchtower Wizard:** You cleansed it, and then lost it? Hm. Luckily it's still bound to this tower. Here it is.
> `{ACTIE: geef cyaan Powering crystal}`

**[stage 6]**
> **Watchtower Wizard:** The shield is holding perfectly. Go and see Radimus Erkle, you've earned it.

**[stage 7]**
> **Watchtower Wizard:** Ah, our hero! The ogres haven't dared come near Yanille since the shield was restored.

### 5.3 Viyeldi

**[stage 0 t/m 1]**
> **Viyeldi:** Leave. You have no business here.

**[stage 2]**
> **Player:** You're the one who stole the crystal from the Watchtower.
> **Viyeldi:** Stole? I *borrowed* it. That old fool in his tower wastes its power holding back a few ogres.
> **Viyeldi:** In my hands, it opens doors. Doors to places you couldn't imagine. And things come through those doors...
> **Player:** Give the crystal back. Yanille needs it.
> **Viyeldi:** Yanille? Hah! Let me show you what I need it for.
> **Viyeldi:** NEZIKCHENED! I call upon you! Rise, and destroy this fool!
> `{ACTIE: stage = 3, spawn Nezikchened (zie sectie 6)}`

**[stage 3, Nezikchened niet aanwezig]** *(speler is gestorven of weggelopen)*
> **Viyeldi:** Back again? You didn't learn your lesson the first time.
> **Viyeldi:** NEZIKCHENED! Finish what you started!
> `{ACTIE: spawn Nezikchened}`

**[stage 3, Nezikchened aanwezig]**
> **Viyeldi:** Ha! You'd better worry about my pet, not me.

**Bij de kill van Nezikchened (stage 3):**
> *(chatbox / overhead)* **Viyeldi:** No! Impossible! This isn't over, adventurer!
> *(chatbox)* Viyeldi vanishes in a cloud of black smoke. He leaves the crystal behind.
> `{ACTIE: stage = 4, geef grijs Powering crystal (bij volle inventory: op de grond), Viyeldi verbergen voor deze speler als de server varp-gebaseerde zichtbaarheid ondersteunt}`

**[stage 4 en hoger]** *(alleen als Viyeldi niet verborgen kan worden)*
> **Viyeldi:** Enjoy your little victory. There will be other crystals... and other doors.

---

## 6. Nezikchened (gevecht)

- Spawnt naast Viyeldi wanneer hij dat zegt in de dialoog (stage 3).
- **Alleen aanvalbaar door de speler voor wie hij is opgeroepen**, en alleen één tegelijk per speler.
- **Stats:** het origineel is level 187, te zwaar voor 50 melee. Doel: ongeveer **level 100–120**. Maak de stats aanpasbaar via constanten.
- **Aanvallen:** voornamelijk **magic**. Protect from Melee vraagt 43 Prayer en de requirement is 42, dus de speler moet Protect from Magic (37) kunnen gebruiken. Een af en toe zwakke melee-aanval is prima.
- **Despawn:** als de speler sterft, uitlogt of meer dan ~15 tiles van de ritueelplek wegloopt, of na 5 minuten zonder gevecht. De speler kan dan opnieuw met Viyeldi praten (stage 3).
- **Geen normale drops.** Alleen de quest-actie bij de kill (zie 5.3).

---

## 7. Altaar (crystal reinigen)

- Werkt op elk gebedsaltaar in de game (objecten waar je Prayer kunt herladen). Als dat te lastig is: één specifiek altaar, overleg met de ontwikkelaar.
- Trigger: **Use grijs Powering crystal on altar** (en optioneel: "Pray" bij het altaar terwijl de speler het grijze crystal heeft).

**[stage 4, base Prayer ≥ 42]**
> *(chatbox)* You kneel at the altar and pray over the dark crystal...
> *(chatbox)* The darkness slowly fades. The crystal glows a bright, pure cyan.
> `{ACTIE: optioneel prayer-animatie, verwijder grijs Powering crystal, geef cyaan Powering crystal, stage = 5}`

**[stage 4, base Prayer < 42]**
> *(chatbox)* You pray over the crystal, but the darkness is too strong. You need a Prayer level of 42.

**[andere stages]** Een crystal op een altaar doet niets bijzonders (standaard "Nothing interesting happens.").

---

## 8. Legends' Guild-poort

- De poort van de Legends' Guild opent alleen als de quest voltooid is (stage 7).
- Anders:
> **Radimus Erkle:** Only legends may pass through this gate.
- Als er bestaande checks zijn op Legends' Quest (poort, Cape of Legends, guild-faciliteiten), laat die naar deze quest verwijzen.

---

## 9. Beloningen (bij stage 7)

- 4 Quest points
- Toegang tot de Legends' Guild
- 2 XP-lampen, elk **7.500 XP** in een skill naar keuze (gebruik het bestaande lamp-systeem; als dat skills uitsluit, volg die regels)
- Quest complete-scherm met:
  - 4 Quest Points
  - Access to the Legends' Guild
  - 2 × 7,500 XP lamps
- Item op het questcompletescherm: cyaan Powering crystal

Hoeveelheden en XP zijn constanten zodat de ontwikkelaar ze makkelijk kan aanpassen.

---

## 10. Quest journal

| Stage | Tekst |
|---|---|
| 0 | I can start this quest by speaking to **Radimus Erkle** outside the **Legends' Guild**. Requirements: 10 Quest Points, 50 Attack, 50 Strength, 50 Defence, 42 Prayer. |
| 1 | Radimus told me the shield above Yanille is failing. I should speak to the **Watchtower Wizard** in the **Watchtower** north of **Yanille**. |
| 2 | One of the **powering crystals** has been stolen. The wizard saw a dark glow on the **hills east of the Fishing Guild**. |
| 3 | The dark wizard **Viyeldi** stole the crystal and summoned the demon **Nezikchened**. I must defeat it. |
| 4 | I defeated Nezikchened and Viyeldi fled. The crystal has turned grey and lifeless. I must **pray over it at an altar**. |
| 5 | The crystal is cleansed. I should bring it back to the **Watchtower Wizard**. |
| 6 | The shield above Yanille is restored. I should return to **Radimus Erkle**. |
| 7 | **QUEST COMPLETE!** I have become a Legend and may enter the **Legends' Guild**. |

---

## 11. Checklist

- [ ] Gecontroleerd of Legends' Quest bestaat; checks laten verwijzen naar deze quest en oude route uitgeschakeld
- [ ] Quest geregistreerd als "The Darkened Shield", 4 QP
- [ ] Requirement-check bij Radimus (10 QP, 50 Att/Str/Def, 42 Prayer, base levels)
- [ ] Dialogen voor Radimus, Watchtower Wizard en Viyeldi per stage
- [ ] Bestaande Watchtower-quest blijft werken
- [ ] Nezikchened: spawn per speler, aangepaste stats, magic-aanvallen, despawn-regels, opnieuw oproepen
- [ ] Kill van Nezikchened → grijs Powering crystal, stage 4, Viyeldi verbergen (indien mogelijk)
- [ ] Altaar-interactie met Prayer-check (grijs → cyaan)
- [ ] Vervangende crystals bij verlies (stage 4 en 5)
- [ ] Legends' Guild-poort geblokkeerd tot stage 7
- [ ] Rewards, XP-lampen en quest complete-scherm
- [ ] Quest journal per stage
- [ ] Getest: volledige doorloop van stage 0 tot 7
- [ ] Getest: sterven tijdens het gevecht en opnieuw proberen
- [ ] Getest: altaar met te laag Prayer-level
- [ ] Getest: poort vóór en na de quest
