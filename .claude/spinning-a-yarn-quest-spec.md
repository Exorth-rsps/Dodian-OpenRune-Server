# Quest specificatie: Spinning a Yarn

> **Instructie voor Claude Code:** implementeer deze quest in de bestaande codebase van deze OSRS private server.
> 1. Deze quest is een **variant op Sheep Shearer**: zelfde soort opdracht (wol scheren, spinnen en inleveren), nieuw verhaal en nieuwe NPC.
>    - Bestaat Sheep Shearer al in de codebase, kijk dan hoe het gedeeltelijk inleveren en tellen daar werkt en volg dat patroon. Laat Sheep Shearer zelf ongemoeid, tenzij de ontwikkelaar anders zegt.
> 2. Volg de bestaande patronen voor questregistratie, quest journal, stage/varp-opslag, dialogen, NPC-handlers, shops en rewards.
> 3. Alle ID's staan als `TODO`. Zoek ze op in de bestaande definities. Bestaat een NPC of item nog niet, vraag het dan aan de ontwikkelaar. NPC-posities, spawns, ground items en objecten (spinning wheel) regelt de ontwikkelaar.
> 4. **Alle items bestaan in OSRS. Er worden geen nieuwe items aangemaakt.**
> 5. Neem de dialogen **letterlijk** over en werk de checklist onderaan af.

---

## 1. Overzicht

| Veld | Waarde |
|---|---|
| Naam | **Spinning a Yarn** |
| Doel | De Silk merchant helpen aan 20 balls of wool |
| Startpunt | Silk merchant, bij zijn kraam |
| Quest points | 1 |
| Vereisten | Geen |
| Beloning | 1 QP, 150 Crafting XP, 500 coins, Silk merchant verkoopt weer balls of wool |

### Het verhaal in het kort
De Silk merchant is trots op zijn zijde en kijkt neer op "boerenwol". Toch verkoopt hij stiekem ook balls of wool, want daar verdient hij het meest aan. Nu de nachten kouder worden, heeft de **wacht van Yanille** een grote bestelling geplaatst: 20 balls of wool om warme sokken te breien voor de nachtwacht op de stadsmuren. Maar zijn vaste leverancier, de boer, ligt in bed met een verrekte rug, en zijn voorraad is op.
De koopman wil voor geen goud zelf een schaap aanraken. Hij vraagt de speler om op de boerderij schapen te scheren en de wol te spinnen. Onderweg vertelt hij steeds grotere sterke verhalen over zijn reizen. De titel verwijst daarnaar: *spinning a yarn* betekent ook "een sterk verhaal vertellen".

---

## 2. NPC's

| NPC | Locatie | NPC ID |
|---|---|---|
| Silk merchant | Zijn kraam (positie: ontwikkelaar) | `TODO` (bestaande OSRS-NPC "Silk merchant") |

De Silk merchant heeft een shop en een silk stall. Voeg de questdialoog toe als optie of laat die voorgaan zolang de quest loopt. Breek de shop en stall niet.

---

## 3. Items en objecten

| Item / object | ID | Bron (rond de boerderij) |
|---|---|---|
| Shears | `TODO` | Ground spawn op de boerderij |
| Wool | `TODO` | Shears gebruiken op een **Sheep** op de boerderij |
| Ball of wool | `TODO` | Wool spinnen op een **Spinning wheel** |
| Spinning wheel (object) | `TODO` | Op of bij de boerderij |
| Coins | `TODO` | Beloning |

**Voor de ontwikkelaar:** zorg dat rond de boerderij te vinden zijn: schapen (scheerbaar), een Shears-spawn en een Spinning wheel. Het spinnen van wol tot ball of wool volgt de normale OSRS Crafting-regels (level 1).

---

## 4. Stages

| Stage | Betekenis | Wordt gezet door |
|---|---|---|
| 0 | Niet gestart | – |
| 1 | Gestart, verzamelt balls of wool | Silk merchant |
| 2 | Quest voltooid | Silk merchant (20 ingeleverd) |

### Gedeeltelijk inleveren
De speler mag in delen inleveren, zoals bij Sheep Shearer. Sla het aantal ingeleverde balls of wool op (0–20) in een varbit of in de quest-varp, volg het bestaande patroon.

- Bij elk gesprek in stage 1 neemt de koopman **alle** balls of wool die de speler bij zich heeft, tot het totaal 20 is. Nooit meer dan nodig.
- **Gewone Wool** (ongesponnen) wordt **niet** geaccepteerd (zie dialoog).

### Shop tijdens de quest
Zijn voorraad is op, dat hoort bij het verhaal. Spelers die de quest **niet** hebben voltooid, kunnen **geen Ball of wool** kopen in zijn shop. Bij een poging:
> *(chatbox)* The Silk merchant has no balls of wool in stock.

Na het voltooien werkt de shop voor die speler weer normaal. Als per-speler blokkeren in de shop niet kan, laat dit dan weten aan de ontwikkelaar.

---

## 5. Dialogen

Notatie: `[Conditie]`, opties onder **Opties**, `{ACTIE}` voor code-acties.

### 5.1 Silk merchant

**[stage 0]**
> **Silk merchant:** Silk! The finest silk in all the land! Smooth as a summer breeze, light as a feather!
> **Player:** I see you also sell balls of wool.
> **Silk merchant:** Shh! Not so loud. Wool is... a side business. Silk is my passion. Wool merely pays the bills.
> **Silk merchant:** And right now, it isn't even doing that. I'm completely sold out.
> **Player:** What happened?
> **Silk merchant:** The city guard has ordered twenty balls of wool. The nights on the walls are getting cold, and they want to knit warm socks for the night watch.
> **Silk merchant:** Twenty! My biggest order of the year. And my supplier, the farmer, has done his back in. He can't even bend down to tie his boots, let alone shear a sheep.
> **Player:** Can't you shear them yourself?
> **Silk merchant:** Me? Touch a sheep? Do you know what they smell like? I once sailed through a storm off Karamja with nothing but a silk sail and a ship's biscuit, but a sheep... no. I have my limits.
> **Silk merchant:** But you look like someone who isn't afraid of a little lanolin. Would you bring me twenty balls of wool?
>
> **Opties:**
> - `Alright, I'll get your wool.`
> - `I'd rather not smell like a sheep.`
>
> *Alright, I'll get your wool.*
> **Player:** Alright, I'll get your wool.
> **Silk merchant:** Splendid! You'll find the sheep at the farm outside town. There should be a pair of shears lying around there somewhere.
> **Silk merchant:** Shear the sheep, then spin the wool into balls on the spinning wheel. Raw wool is no use to me. The guard wants to knit, not stuff pillows.
> **Silk merchant:** You can bring them in a few at a time, I'll keep count. I'm a merchant after all. Counting is the one thing I never get wrong.
> `{ACTIE: start quest, stage = 1, aantal = 0}`
>
> *I'd rather not smell like a sheep.*
> **Player:** I'd rather not smell like a sheep.
> **Silk merchant:** I understand completely. Truly, I do. But the guard won't. If you change your mind, I'll be here.

**[stage 1] — controleer in deze volgorde:**

1. **Speler heeft een of meer Balls of wool bij zich**
> **Silk merchant:** Is that wool I see? Hand it over, quickly, before anyone notices I'm dealing in the stuff.
> `{ACTIE: neem min(aantal in inventory, 20 − ingeleverd) Balls of wool, verhoog de teller}`
>
> → **Teller is nu 20:** ga direct door naar **"Alle wol binnen"**.
>
> → **Teller nog onder 20:**
> **Silk merchant:** That's {INGELEVERD} so far. I still need {RESTEREND} more.
> → Toon daarna één willekeurig **sterk verhaal** (zie hieronder).

2. **Speler heeft alleen Wool (ongesponnen) bij zich**
> **Silk merchant:** That's raw wool! Did you think the guard would knit socks out of fluff? Spin it into balls on the spinning wheel first.

3. **Overige gevallen**
> **Silk merchant:** I still need {RESTEREND} balls of wool. The sheep are at the farm outside town, and don't forget to spin the wool first.

**Sterke verhalen** *(kies er één willekeurig na een gedeeltelijke inlevering)*
> **Silk merchant:** Did I ever tell you about the time I traded silk with a dragon? It wanted a scarf. Very fashionable creature. Terrible haggler, though.

> **Silk merchant:** When I was younger, I crossed the desert on a camel made entirely of silk. Well, its saddle was silk. The camel was mostly camel.

> **Silk merchant:** I once sold a silk robe to a wizard who turned me into a teapot when I wouldn't lower my price. I still got my asking price. Teapots are very persuasive.

> **Silk merchant:** The silk I sell comes from spiders the size of houses. Or so I tell the customers. Don't repeat that.

**Alle wol binnen**
> **Silk merchant:** Twenty! Twenty balls of wool! Exactly twenty, I counted them myself, three times.
> **Silk merchant:** The guard will have warm feet all winter, and I'll have a very full purse. Well, slightly fuller.
> **Silk merchant:** Here's your share. And between you and me...
> **Silk merchant:** ...wool isn't all that bad. Don't tell anyone I said that. I have a reputation to protect.
> **Silk merchant:** From now on, if you ever need balls of wool, you know where to come. And if you ever want real quality, there's always silk.
> `{ACTIE: stage = 2, quest complete, rewards, quest complete-scherm, shop: Ball of wool weer te koop voor deze speler}`

**[stage 2]**
> **Silk merchant:** Ah, my favourite shepherd! The guards say their socks are the warmest in the land. I told them they'd be warmer in silk. They didn't listen.
> → daarna gewone shop/dialoogopties

---

## 6. Beloningen (bij stage 2)

- 1 Quest point
- 150 Crafting XP
- 500 coins (inventory; bij volle inventory: bank of op de grond, volg de bestaande conventie)
- Ball of wool weer te koop bij de Silk merchant
- Quest complete-scherm met:
  - 1 Quest Point
  - 150 Crafting XP
  - 500 coins
  - Balls of wool for sale at the Silk merchant
- Item op het questcompletescherm: Ball of wool

XP en coins zijn constanten zodat de ontwikkelaar ze makkelijk kan aanpassen.

---

## 7. Quest journal

| Stage | Tekst |
|---|---|
| 0 | I can start this quest by speaking to the **Silk merchant** at his stall. |
| 1 | The city guard has ordered **20 balls of wool** from the Silk merchant, but his supplier has hurt his back. |
| 1 | I can shear the **sheep** at the **farm** outside town and spin the wool on a **spinning wheel**. |
| 1 | I have delivered **{INGELEVERD}/20** balls of wool. |
| 2 | **QUEST COMPLETE!** The night watch of Yanille will have warm socks all winter. |

---

## 8. Checklist

- [ ] Quest geregistreerd als "Spinning a Yarn", 1 QP
- [ ] Bestaande Sheep Shearer blijft werken (indien aanwezig)
- [ ] Bestaande shop en silk stall van de Silk merchant blijven werken
- [ ] Dialogen voor de Silk merchant per stage
- [ ] Teller voor ingeleverde balls of wool (0–20), nooit meer dan nodig innemen
- [ ] Ongesponnen Wool wordt geweigerd met eigen dialoog
- [ ] Willekeurig sterk verhaal na gedeeltelijke inlevering
- [ ] Ball of wool niet te koop tot de quest voltooid is (per speler)
- [ ] Rewards en quest complete-scherm
- [ ] Quest journal met teller
- [ ] Getest: 20 in één keer inleveren
- [ ] Getest: in delen inleveren (bijv. 7 + 7 + 6)
- [ ] Getest: meer dan 20 bij zich, alleen 20 (of het restant) wordt ingenomen
- [ ] Getest: shop vóór en na de quest
