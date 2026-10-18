# Quest specificatie: A Piece of Cake

> **Instructie voor Claude Code:** implementeer deze quest in de bestaande codebase van deze OSRS private server.
> 1. Deze quest is een **variant op Cook's Assistant**: zelfde soort opdracht (drie ingrediënten verzamelen en inleveren), nieuw verhaal en nieuwe NPC.
>    - Bestaat Cook's Assistant al in de codebase, kijk dan hoe het gedeeltelijk inleveren daar werkt en volg dat patroon. Laat Cook's Assistant zelf ongemoeid, tenzij de ontwikkelaar anders zegt.
> 2. Volg de bestaande patronen voor questregistratie, quest journal, stage/varp-opslag, dialogen, NPC-handlers en rewards.
> 3. Alle ID's staan als `TODO`. Zoek ze op in de bestaande definities. Bestaat een NPC of item nog niet, vraag het dan aan de ontwikkelaar. NPC-posities, spawns en ground items regelt de ontwikkelaar.
> 4. **Alle items bestaan in OSRS. Er worden geen nieuwe items aangemaakt.**
> 5. Neem de dialogen **letterlijk** over en werk de checklist onderaan af.

---

## 1. Overzicht

| Veld | Waarde |
|---|---|
| Naam | **A Piece of Cake** |
| Doel | De bakker in Yanille helpen aan ingrediënten voor een cake |
| Startpunt | Baker, bij zijn kraampje op het plein in Yanille |
| Quest points | 1 |
| Vereisten | Geen |
| Beloning | 1 QP, 300 Cooking XP, 2 Cakes |

### Het verhaal in het kort
Vannacht zijn er ratten in de voorraadkamer van de bakker gekropen. Ze hebben al zijn eieren, melk en meel opgegeten. Uitgerekend vandaag heeft de Wizards' Guild een grote cake besteld voor de middagthee, en tovenaars wachten niet graag. De bakker kan zijn kraam niet onbeheerd laten, dus vraagt hij de speler om snel een ei, een emmer melk en een pot meel te halen bij de boerderij buiten de stad. Als de cake op tijd klaar is, krijgt de speler een stuk van de beloning.

---

## 2. NPC's

| NPC | Locatie | NPC ID |
|---|---|---|
| Baker | Bakkerskraam, plein in Yanille | `TODO` (bestaande OSRS-NPC "Baker", bijv. die van de Ardougne-markt) |

Heeft de Baker al een shop of steal-functie (bakery stall), voeg de questdialoog dan toe als optie of laat die voorgaan zolang de quest loopt. Breek de stall en shop niet.

---

## 3. Items

| Item | Item ID | Bron (rond de boerderij) |
|---|---|---|
| Egg | `TODO` | Kippenhok op de boerderij |
| Bucket | `TODO` | Ground spawn op of bij de boerderij |
| Bucket of milk | `TODO` | Bucket gebruiken op een **Dairy cow** |
| Pot | `TODO` | Ground spawn op of bij de boerderij |
| Grain | `TODO` | Tarweveld bij de boerderij (Wheat plukken) |
| Pot of flour | `TODO` | Grain in de hopper van de molen → Pot gebruiken bij de flour bin |
| Cake | `TODO` | Beloning |
| Coins | – | Niet gebruikt |

**Voor de ontwikkelaar:** zorg dat rond de boerderij te vinden zijn: kippen/eieren, een Dairy cow, een Bucket- en Pot-spawn, een tarweveld en een molen met hopper en flour bin.
Is er geen molen bij de boerderij? Laat dat dan weten aan Claude Code. Alternatief: een **Pot of flour** als ground spawn in de boerderij (bijv. in de keuken van de boerderij).

---

## 4. Stages

| Stage | Betekenis | Wordt gezet door |
|---|---|---|
| 0 | Niet gestart | – |
| 1 | Gestart, verzamelt ingrediënten | Baker |
| 2 | Quest voltooid | Baker (alle drie ingeleverd) |

### Gedeeltelijk inleveren
De speler mag de ingrediënten **één voor één** inleveren, net als bij Cook's Assistant. Sla per ingrediënt op of het is ingeleverd (bijv. drie varbits of bitflags op de quest-varp, volg het bestaande patroon):

| Flag | Betekenis |
|---|---|
| `EGG_GIVEN` | Egg ingeleverd |
| `MILK_GIVEN` | Bucket of milk ingeleverd |
| `FLOUR_GIVEN` | Pot of flour ingeleverd |

Bij het inleveren van een Bucket of milk krijgt de speler een lege **Bucket** terug, en bij een Pot of flour een lege **Pot** (zoals in OSRS).

---

## 5. Dialogen

Notatie: `[Conditie]`, opties onder **Opties**, `{ACTIE}` voor code-acties.

### 5.1 Baker

**[stage 0]**
> **Baker:** Oh no, oh no, oh no... what am I going to do?
> **Player:** What's wrong?
> **Baker:** Rats! A whole family of them got into my storeroom last night. They ate everything. My eggs, my milk, every last bit of flour!
> **Baker:** And today of all days, the Wizards' Guild has ordered a big cake for their afternoon tea.
> **Baker:** Have you ever seen an impatient wizard? I have. The last baker who was late with an order spent a week as a frog.
> **Player:** That sounds serious.
> **Baker:** It is! I can't leave my stall, or the whole market will help themselves to my bread. Could you fetch me some ingredients?
>
> **Opties:**
> - `Of course, what do you need?`
> - `Sorry, I'm in a hurry.`
>
> *Of course, what do you need?*
> **Player:** Of course, what do you need?
> **Baker:** Oh, thank you! I need an egg, a bucket of milk and a pot of flour.
> **Baker:** You'll find everything at the farm outside town. There are chickens for the eggs and a dairy cow for the milk.
> **Baker:** For the flour, pick some wheat in the field, put the grain in the hopper at the mill and work the controls. Then collect the flour with a pot.
> **Baker:** You'll find a bucket and a pot lying around the farm. Farmers never tidy anything up.
> **Baker:** Please hurry! I can already feel my nose turning green.
> `{ACTIE: start quest, stage = 1}`
>
> *Sorry, I'm in a hurry.*
> **Player:** Sorry, I'm in a hurry.
> **Baker:** So am I! So am I... If you change your mind, I'll be here. Hopefully not as a frog.

**[stage 1] — controleer in deze volgorde:**

1. **Speler heeft een of meer ontbrekende ingrediënten bij zich**
> **Baker:** You're back! Did you bring anything?
> `{ACTIE: lever per ontbrekend ingrediënt dat de speler bij zich heeft één exemplaar in, zet de bijbehorende flag, geef lege Bucket/Pot terug. Toon per ingeleverd item de bijbehorende regel hieronder.}`
>
> *Egg ingeleverd:*
> **Baker:** An egg! Lovely and fresh, too.
>
> *Bucket of milk ingeleverd:*
> **Baker:** Milk! Here's your bucket back.
>
> *Pot of flour ingeleverd:*
> **Baker:** Flour, wonderful! Here's your pot back.
>
> → Zijn nu **alle drie** ingeleverd: ga direct door naar **"Alle ingrediënten binnen"**.
> → Zo niet: ga door naar **"Wat ontbreekt nog"**.

2. **Speler heeft niets van de ontbrekende ingrediënten bij zich** → ga naar **"Wat ontbreekt nog"**.

**Wat ontbreekt nog** *(toon alleen de regels voor ingrediënten die nog niet zijn ingeleverd)*
> **Baker:** I still need:
> **Baker:** An egg. The chickens at the farm should have laid a few by now.
> **Baker:** A bucket of milk. Try the dairy cow at the farm.
> **Baker:** A pot of flour. Grain in the hopper at the mill, then collect it with a pot.
> **Baker:** Please hurry, the wizards will be here soon!

**Alle ingrediënten binnen**
> **Baker:** That's everything! Egg, milk, flour... Now stand back and watch a master at work.
> *(chatbox)* The baker mixes the ingredients at lightning speed and slides the tin into his oven.
> **Baker:** ...
> **Baker:** ...
> **Baker:** There! A perfect cake, and with time to spare. The wizards will never know how close it was.
> **Baker:** You've saved my stall, and very likely my nose too. Here, take these. Freshly baked, of course.
> **Baker:** And if you ever want to learn to bake one yourself, you know who to ask.
> `{ACTIE: stage = 2, quest complete, rewards, quest complete-scherm}`

**[stage 2]**
> **Baker:** Ah, my saviour! The wizards loved the cake. One of them even smiled. I didn't know they could do that.

---

## 6. Beloningen (bij stage 2)

- 1 Quest point
- 300 Cooking XP
- 2 × Cake (inventory; bij volle inventory: bank of op de grond, volg de bestaande conventie)
- Quest complete-scherm met:
  - 1 Quest Point
  - 300 Cooking XP
  - 2 Cakes
- Item op het questcompletescherm: Cake

XP en aantal cakes zijn constanten zodat de ontwikkelaar ze makkelijk kan aanpassen.

---

## 7. Quest journal

| Stage | Tekst |
|---|---|
| 0 | I can start this quest by speaking to the **Baker** at his stall on the square in **Yanille**. |
| 1 | Rats ate all of the Baker's ingredients, and the **Wizards' Guild** has ordered a cake. I need to bring him: |
| 1 | an **egg** *(doorgestreept als ingeleverd)* |
| 1 | a **bucket of milk** *(doorgestreept als ingeleverd)* |
| 1 | a **pot of flour** *(doorgestreept als ingeleverd)* |
| 1 | Everything can be found at the **farm** outside town. |
| 2 | **QUEST COMPLETE!** The cake was finished just in time. |

---

## 8. Checklist

- [ ] Quest geregistreerd als "A Piece of Cake", 1 QP
- [ ] Bestaande Cook's Assistant blijft werken (indien aanwezig)
- [ ] Bestaande bakery stall/shop van de Baker blijft werken
- [ ] Dialogen voor de Baker per stage
- [ ] Gedeeltelijk inleveren met flags per ingrediënt
- [ ] Lege Bucket en Pot terug bij inleveren
- [ ] Alleen ontbrekende ingrediënten worden gevraagd/ingeleverd (geen dubbele inname)
- [ ] Rewards en quest complete-scherm
- [ ] Quest journal met doorgestreepte ingrediënten
- [ ] Getest: alles in één keer inleveren
- [ ] Getest: ingrediënten één voor één inleveren
- [ ] Getest: volle inventory bij reward
