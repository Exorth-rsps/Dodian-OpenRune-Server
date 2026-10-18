# Quest specificatie: The Frozen Passage

> **Instructie voor Claude Code:** implementeer deze quest in de bestaande codebase van deze OSRS private server.
> 1. Deze quest is nieuw. Hij lijkt op **Doric's Quest** (ertsen brengen naar Doric), maar met smithing en een eigen verhaal. Bestaat Doric's Quest al in de codebase, laat die dan ongemoeid, tenzij de ontwikkelaar anders zegt.
> 2. De quest **blokkeert de tunnel onder de Ice Mountain** tot hij voltooid is.
> 3. Volg de bestaande patronen voor questregistratie, quest journal, stage/varp-opslag, dialogen, NPC-handlers, object-interacties (deur/ingang van de tunnel) en rewards.
> 4. Alle ID's staan als `TODO`. Zoek ze op in de bestaande definities. Bestaat een NPC, item of object nog niet, vraag het dan aan de ontwikkelaar. NPC-posities, spawns en objecten regelt de ontwikkelaar.
> 5. **Alle items bestaan in OSRS. Er worden geen nieuwe items aangemaakt.**
> 6. Neem de dialogen **letterlijk** over en werk de checklist onderaan af.

---

## 1. Overzicht

| Veld | Waarde |
|---|---|
| Naam | **The Frozen Passage** |
| Doel | De bevroren stutten in de tunnel onder de Ice Mountain herstellen |
| Startpunt | Doric, bij de ingang van de tunnel |
| Quest points | 1 |
| Vereisten | Geen om te starten. Om de items te maken: **Smithing 4** (bronze nails) |
| Beloning | 1 QP, 1.000 Mining XP, 1.000 Smithing XP, toegang tot de tunnel |

### Het verhaal in het kort
Doric de dwerg bewaakt de ingang van de tunnel onder de Ice Mountain. Deze winter is de vorst zo diep in de berg gekropen dat de houten stutten zijn gebarsten en het ijs tussen de rotsen drukt. Doric laat niemand door voordat de tunnel veilig is.
Hij vraagt de speler om **clay** te minen voor mortel, en om **bronze bars** en **bronze nails** te smeden om nieuwe beugels te maken. Als alles klaar is, wil hij dat de speler als eerste de tunnel in gaat om de diepste stut te controleren. Dan komt het uit: Doric is zelf doodsbang voor de tunnel, omdat hij er als jonge dwerg een nacht in verdwaald is. Als de speler veilig terugkomt, overwint Doric zijn angst en gaat de tunnel open.

---

## 2. NPC's en objecten

| NPC / object | Locatie | ID |
|---|---|---|
| Doric | Bij de ingang van de tunnel onder de Ice Mountain | `TODO` (bestaande OSRS-NPC) |
| Ingang van de tunnel (deur/grot) | Ice Mountain | `TODO` (bestaand object) |
| Deepest support (stut) | Diep in de tunnel | `TODO` (bestaand object, bijv. een houten stut of balk) |

**Voor de ontwikkelaar:** zorg dat rond de Ice Mountain te vinden zijn: copper-, tin- en clay-rotsen, een furnace en een anvil, en een Hammer-spawn of shop. Dat mag ook in de buurt van Catherby zijn.

---

## 3. Items

| Item | Item ID | Bron |
|---|---|---|
| Clay | `TODO` | Clay-rotsen minen (Mining 1) |
| Copper ore | `TODO` | Copper-rotsen minen (Mining 1) |
| Tin ore | `TODO` | Tin-rotsen minen (Mining 1) |
| Bronze bar | `TODO` | Copper ore + Tin ore in een furnace (Smithing 1) |
| Bronze nails | `TODO` | Bronze bar op een anvil, levert 15 nails (Smithing 4) |
| Hammer | `TODO` | Nodig voor smithing |

### Wat Doric nodig heeft
| Item | Aantal |
|---|---|
| Clay | 4 |
| Bronze bar | 2 |
| Bronze nails | 15 |

Dat zijn in totaal 3 bronze bars (2 voor Doric, 1 voor de nails), dus 3 copper ore en 3 tin ore, plus 4 clay.

---

## 4. Stages

| Stage | Betekenis | Wordt gezet door |
|---|---|---|
| 0 | Niet gestart | – |
| 1 | Gestart, verzamelt materialen | Doric |
| 2 | Materialen ingeleverd, moet de diepste stut controleren | Doric |
| 3 | Stut gecontroleerd, terug naar Doric | Stut-object |
| 4 | Quest voltooid | Doric |

### Gedeeltelijk inleveren (stage 1)
De speler mag de drie soorten materialen los inleveren. Sla per soort op of die compleet is:

| Flag | Betekenis |
|---|---|
| `CLAY_GIVEN` | 4 Clay ingeleverd |
| `BARS_GIVEN` | 2 Bronze bars ingeleverd |
| `NAILS_GIVEN` | 15 Bronze nails ingeleverd |

Een soort wordt alleen ingenomen als de speler het **volledige aantal** bij zich heeft (dus geen 3 van de 4 clay).

---

## 5. Tunneltoegang

- De ingang van de tunnel is **dicht** tot stage 2.
  > **Doric:** Oi! Nobody goes in there until those supports are fixed. Talk to me first.
- In **stage 2 en 3** mag de speler naar binnen om de stut te controleren.
- Na **stage 4** is de tunnel voor de speler altijd open.

---

## 6. Dialogen

Notatie: `[Conditie]`, opties onder **Opties**, `{ACTIE}` voor code-acties.

### 6.1 Doric

**[stage 0]**
> **Doric:** Hold it right there! Where do you think you're going?
> **Player:** Into the tunnel?
> **Doric:** Not today you're not. Not any day, until those supports are fixed.
> **Doric:** This winter the frost crept so deep into the mountain that the old wooden supports cracked. The ice is pushing the rocks apart. One wrong step and the whole ceiling comes down.
> **Player:** Can it be repaired?
> **Doric:** Of course it can. I'm a dwarf. But I'm a dwarf with a bad knee and no materials.
> **Doric:** Bring me what I need, and I'll fix it myself.
>
> **Opties:**
> - `What do you need?`
> - `I'll find another way around.`
>
> *What do you need?*
> **Player:** What do you need?
> **Doric:** Four lumps of clay for the mortar. That'll seal the cracks so the ice can't get back in.
> **Doric:** Two bronze bars for new brackets, and fifteen bronze nails to fix them to the beams.
> **Doric:** You'll find copper, tin and clay rocks around here. Smelt the ore in a furnace, then hammer one bar into nails on an anvil. Don't forget a hammer.
> **Doric:** Bring it all in at once or bit by bit, I don't mind. Just bring the full amount of each.
> `{ACTIE: start quest, stage = 1}`
>
> *I'll find another way around.*
> **Player:** I'll find another way around.
> **Doric:** Good luck with that. It's a mountain. There isn't another way around.

**[stage 1] — controleer in deze volgorde:**

1. **Speler heeft het volledige aantal van een of meer ontbrekende materialen bij zich**
> **Doric:** Ah, materials! Let's see what you've got.
> `{ACTIE: lever per ontbrekende soort waarvan de speler genoeg heeft het volledige aantal in, zet de bijbehorende flag. Toon per ingeleverde soort de regel hieronder.}`
>
> *Clay ingeleverd:*
> **Doric:** Four lumps of clay. Nice and sticky.
>
> *Bronze bars ingeleverd:*
> **Doric:** Two bronze bars. Good, solid work.
>
> *Bronze nails ingeleverd:*
> **Doric:** Fifteen nails, and not a bent one among them. You've got a smith's hands.
>
> → Zijn nu **alle drie** ingeleverd: ga door naar **"Alle materialen binnen"**.
> → Zo niet: ga door naar **"Wat ontbreekt nog"**.

2. **Speler heeft een deel van een materiaal, maar niet genoeg** (bijv. 2 clay)
> **Doric:** That's not enough. I need the full amount, or the repair won't hold.
> → ga door naar **"Wat ontbreekt nog"**.

3. **Overige gevallen** → **"Wat ontbreekt nog"**.

**Wat ontbreekt nog** *(toon alleen de regels voor soorten die nog niet zijn ingeleverd)*
> **Doric:** I still need:
> **Doric:** Four lumps of clay.
> **Doric:** Two bronze bars.
> **Doric:** Fifteen bronze nails. Hammer a bronze bar on an anvil.

**Alle materialen binnen**
> **Doric:** That's everything! Right, give me a moment...
> *(chatbox)* Doric disappears just inside the tunnel entrance. You hear hammering, grumbling and a lot of dwarvish swearing.
> **Doric:** There. The front supports are as good as new.
> **Doric:** Now, there's one more support, right at the deepest part of the tunnel. Somebody needs to check it.
> **Player:** Why don't you check it?
> **Doric:** Me? Er... my knee. Yes. My knee is bad. Very bad. You go. Just inspect the deepest support and tell me if it's holding.
> `{ACTIE: stage = 2, tunnel open voor deze speler}`

**[stage 2]**
> **Doric:** Have you checked the deepest support yet? It's right at the far end of the tunnel. My knee and I will wait here.

**[stage 3]**
> **Player:** The deepest support is holding. The whole tunnel is safe.
> **Doric:** It is? ...Really safe? No cracks, no ice?
> **Player:** Not a single crack. Is something wrong?
> **Doric:** ...Fine. I'll tell you the truth. There's nothing wrong with my knee.
> **Doric:** When I was a young dwarf, I got lost in that tunnel. A whole night, alone in the dark, listening to the ice creak. I haven't been past the entrance since.
> **Doric:** But if you went all the way to the end and came back in one piece... then maybe it's time I stopped being afraid of it.
> **Doric:** Thank you, friend. The tunnel is open to you. And maybe, one day, to me too.
> `{ACTIE: stage = 4, quest complete, rewards, quest complete-scherm}`

**[stage 4]**
> **Doric:** I went in yesterday. Ten whole steps! Tomorrow I'm going for twenty.

### 6.2 Deepest support (object)

**[stage 2] — Inspect**
> *(chatbox)* You inspect the support. The wood is old but sturdy, and the ice around it has been sealed with fresh clay.
> *(chatbox)* It's holding. You should tell Doric.
> `{ACTIE: stage = 3}`

**[andere stages] — Inspect**
> *(chatbox)* A sturdy wooden support. It looks like it'll hold for another hundred years.

---

## 7. Beloningen (bij stage 4)

- 1 Quest point
- 1.000 Mining XP
- 1.000 Smithing XP
- Toegang tot de tunnel onder de Ice Mountain
- Quest complete-scherm met:
  - 1 Quest Point
  - 1,000 Mining XP
  - 1,000 Smithing XP
  - Access to the Ice Mountain tunnel
- Item op het questcompletescherm: Bronze bar

XP is een constante zodat de ontwikkelaar het makkelijk kan aanpassen.

---

## 8. Quest journal

| Stage | Tekst |
|---|---|
| 0 | I can start this quest by speaking to **Doric** at the tunnel entrance under the **Ice Mountain**. |
| 1 | The frost has cracked the supports in the tunnel. Doric needs: |
| 1 | **4 clay** *(doorgestreept als ingeleverd)* |
| 1 | **2 bronze bars** *(doorgestreept als ingeleverd)* |
| 1 | **15 bronze nails** *(doorgestreept als ingeleverd)* |
| 2 | Doric fixed the front supports. I should **inspect the deepest support** at the far end of the tunnel. |
| 3 | The deepest support is holding. I should tell **Doric**. |
| 4 | **QUEST COMPLETE!** The tunnel under the Ice Mountain is open, and Doric is a little less afraid of the dark. |

---

## 9. Checklist

- [ ] Quest geregistreerd als "The Frozen Passage", 1 QP
- [ ] Bestaande Doric's Quest blijft werken (indien aanwezig)
- [ ] Tunnelingang dicht tot stage 2, daarna open
- [ ] Dialogen voor Doric per stage
- [ ] Gedeeltelijk inleveren per soort, alleen bij het volledige aantal
- [ ] Inspect-actie op de diepste stut zet stage 3
- [ ] Rewards en quest complete-scherm
- [ ] Quest journal met doorgestreepte materialen
- [ ] Getest: volledige doorloop van stage 0 tot 4
- [ ] Getest: materialen in delen inleveren
- [ ] Getest: te weinig van één materiaal bij zich
- [ ] Getest: tunnel vóór, tijdens en na de quest
