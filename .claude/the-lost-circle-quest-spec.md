# Quest specificatie: The Lost Circle

> **Instructie voor Claude Code:** implementeer deze quest in de bestaande codebase van deze OSRS private server.
> 1. Deze quest is een **vervanging van Druidic Ritual**: zelfde doel (Herblore vrijspelen), zelfde items, nieuw verhaal. De stone circle bestaat op deze server niet meer.
>    - Zoek of Druidic Ritual al in de codebase zit. **Zo ja:** hergebruik de varp/varbit en laat alle bestaande checks op "Druidic Ritual voltooid" (herbs cleanen, potions maken, enz.) naar deze quest verwijzen. Schakel de oude questroute uit, zonder andere functies van Kaqemeex en Sanfew te breken.
>    - **Zo nee:** maak een nieuwe varp/varbit aan volgens het bestaande quest-patroon.
> 2. Volg de bestaande patronen voor questregistratie, quest journal, dialogen, NPC-handlers, item-op-object-interacties en rewards.
> 3. Alle ID's staan als `TODO`. Zoek ze op in de bestaande definities. Bestaat een NPC, item of object nog niet, vraag het dan aan de ontwikkelaar. NPC-posities, spawns en objecten regelt de ontwikkelaar.
> 4. **Alle items bestaan in OSRS. Er worden geen nieuwe items aangemaakt.**
> 5. Neem de dialogen **letterlijk** over en werk de checklist onderaan af.

---

## 1. Overzicht

| Veld | Waarde |
|---|---|
| Naam | **The Lost Circle** |
| Doel | De druïden van Taverley helpen een nieuw heilig ritueel uit te voeren en Herblore leren |
| Startpunt | Kaqemeex, Taverley |
| Quest points | 4 |
| Vereisten | Geen |
| Beloning | 4 QP, 250 Herblore XP, toegang tot Herblore |

### Het verhaal in het kort
Eeuwenlang voerden de druïden van Taverley hun rituelen uit in een grote cirkel van stenen. Die cirkel is er niet meer: de stenen zijn één voor één gebarsten en tot stof vergaan, en niemand weet waarom. Zonder de cirkel voelen de druïden de kracht van Guthix niet meer, en hun kruiden verliezen hun werking.
Kaqemeex is wanhopig, maar Sanfew kent een ouder ritueel uit de tijd vóór de stenen: een offer van vier soorten vlees, gedoopt in de **Cauldron of Thunder**. Het ritueel heeft geen stenen nodig, alleen een zuiver offer. Als het lukt, keert de kracht terug en leert Kaqemeex de speler de geheimen van de kruiden.

---

## 2. NPC's en objecten

| NPC / object | Locatie | ID |
|---|---|---|
| Kaqemeex | Taverley, op de plek waar vroeger de stone circle stond (of ergens in het dorp) | `TODO` (bestaande OSRS-NPC) |
| Sanfew | Taverley, bovenverdieping van een huis | `TODO` (bestaande OSRS-NPC) |
| Cauldron of Thunder (object) | Keuze ontwikkelaar. Origineel: Taverley Dungeon. Suggestie als die er niet is: een rustige plek net buiten Taverley | `TODO` (bestaand OSRS-object) |

---

## 3. Items

| Item | Item ID | Bron |
|---|---|---|
| Raw beef | `TODO` | Koeien (bijv. bij Moo's Farm) |
| Raw rat meat | `TODO` | Ratten |
| Raw bear meat | `TODO` | Beren |
| Raw chicken | `TODO` | Kippen (bijv. bij Moo's Farm) |
| Enchanted beef | `TODO` | Raw beef gedoopt in de Cauldron of Thunder |
| Enchanted rat | `TODO` | Raw rat meat gedoopt in de Cauldron of Thunder |
| Enchanted bear | `TODO` | Raw bear meat gedoopt in de Cauldron of Thunder |
| Enchanted chicken | `TODO` | Raw chicken gedoopt in de Cauldron of Thunder |

**Voor de ontwikkelaar:** zorg dat koeien, ratten, beren en kippen op de server te vinden zijn en de juiste rauwe vleesdrops hebben.

---

## 4. Stages

| Stage | Betekenis | Wordt gezet door |
|---|---|---|
| 0 | Niet gestart | – |
| 1 | Kaqemeex gesproken, moet naar Sanfew | Kaqemeex |
| 2 | Sanfew gesproken, verzamelt en doopt vlees | Sanfew |
| 3 | Vlees ingeleverd, ritueel uitgevoerd, terug naar Kaqemeex | Sanfew |
| 4 | Quest voltooid | Kaqemeex |

---

## 5. Cauldron of Thunder (vlees dopen)

- Trigger: **Use [rauw vlees] on Cauldron of Thunder**.
- Werkt alleen in **stage 2**.

| Gebruikt item | Resultaat |
|---|---|
| Raw beef | Enchanted beef |
| Raw rat meat | Enchanted rat |
| Raw bear meat | Enchanted bear |
| Raw chicken | Enchanted chicken |

**[stage 2, geldig rauw vlees]**
> *(chatbox)* You dip the {vlees} into the cauldron. It crackles with a faint green light.
> `{ACTIE: vervang het rauwe vlees door de enchanted versie}`

**[stage 2, ander item]**
> *(chatbox)* Nothing interesting happens.

**[andere stages]**
> *(chatbox)* The cauldron bubbles quietly. You don't think you should put anything in it.

---

## 6. Dialogen

Notatie: `[Conditie]`, opties onder **Opties**, `{ACTIE}` voor code-acties.

### 6.1 Kaqemeex

**[stage 0]**
> **Kaqemeex:** Greetings, traveller. You stand where the great circle once stood.
> **Player:** Circle? I don't see anything here.
> **Kaqemeex:** No. And that is the tragedy. For centuries, the stones of Taverley stood here. We druids gathered among them to feel the power of Guthix flowing through the earth.
> **Kaqemeex:** Then, one by one, the stones cracked. They crumbled to dust, and the wind carried them away. Nobody knows why.
> **Kaqemeex:** Without the circle, the balance is broken. Our herbs have lost their strength. Our potions are little more than coloured water.
> **Player:** Is there anything that can be done?
> **Kaqemeex:** Perhaps. My friend Sanfew has been searching through the oldest scrolls of our order. He speaks of a ritual from the time before the stones.
> **Kaqemeex:** Will you help him? I am too old to run errands across the land, and the young druids have lost hope.
>
> **Opties:**
> - `I'll help the druids.`
> - `Not right now.`
>
> *I'll help the druids.*
> **Player:** I'll help the druids.
> **Kaqemeex:** Guthix smiles on you. Sanfew lives here in Taverley, on the upper floor of his house. Tell him I sent you.
> `{ACTIE: start quest, stage = 1}`
>
> *Not right now.*
> **Player:** Not right now.
> **Kaqemeex:** Balance cannot be rushed. But do not wait too long. Every day without the circle, the herbs grow weaker.

**[stage 1 t/m 2]**
> **Kaqemeex:** Have you spoken to Sanfew? He lives here in Taverley, on the upper floor of his house. Only he knows the old ritual.

**[stage 3]**
> **Kaqemeex:** I felt it. Just now, beneath my feet... the earth is warm again.
> **Player:** Sanfew performed the ritual. It worked.
> **Kaqemeex:** It did more than work. We always believed the power lived in the stones. But the stones were only a reminder. The power was in the earth all along.
> **Kaqemeex:** You have restored the balance, and that deserves a reward that cannot be bought. I will teach you the art of Herblore.
> **Kaqemeex:** Herbs that grow in the wild are covered in dirt and must be cleaned before use. Place a clean herb in a vial of water to make an unfinished potion.
> **Kaqemeex:** Then add a secondary ingredient. An eye of newt, for example, with a guam leaf makes an attack potion.
> **Kaqemeex:** The more you learn, the more powerful the potions you'll be able to brew. Go now, herbalist. The earth is listening again.
> `{ACTIE: stage = 4, quest complete, rewards, quest complete-scherm}`

**[stage 4]**
> **Kaqemeex:** Welcome back, herbalist. The herbs of Taverley are stronger than they have been in years.
> → daarna bestaande dialoogopties (bijv. shop), indien aanwezig

### 6.2 Sanfew

**[stage 0]**
> **Sanfew:** Hmm? Oh, I'm sorry, I'm terribly busy with these scrolls. Perhaps another time.

**[stage 1]**
> **Player:** Kaqemeex sent me. He says you know a ritual that could help the druids.
> **Sanfew:** He sent you? Then he trusts you, and that is good enough for me.
> **Sanfew:** Look at these scrolls. They are older than the stone circle itself. Before there were stones, our ancestors made offerings directly to the earth.
> **Sanfew:** The ritual needs four kinds of meat, offered as one: beef, rat, bear and chicken. The strong, the small, the wild and the tame. Together, they stand for balance.
> **Player:** And then I just bring you some raw meat?
> **Sanfew:** Raw meat? Goodness, no. Each piece must first be dipped in the Cauldron of Thunder. Only then is the offering pure.
> **Sanfew:** Bring me enchanted beef, enchanted rat, enchanted bear and enchanted chicken, and I will perform the ritual.
> `{ACTIE: stage = 2}`

**[stage 2] — controleer in deze volgorde:**

1. **Speler heeft alle vier enchanted meats**
> **Player:** I have all four enchanted meats.
> **Sanfew:** Let me see... beef, rat, bear and chicken. Yes, I can feel the cauldron's power in them.
> **Sanfew:** Stand back. It has been a very long time since anyone performed this ritual.
> `{ACTIE: verwijder Enchanted beef, Enchanted rat, Enchanted bear, Enchanted chicken. Optioneel: animatie/graphic op Sanfew. stage = 3}`
> *(chatbox)* Sanfew raises the offering and speaks words in a language you've never heard. The floor trembles briefly.
> **Sanfew:** ...It is done. Did you feel that? The earth answered.
> **Sanfew:** Go to Kaqemeex. He'll want to thank you himself.

2. **Speler heeft rauw vlees (een of meer van de vier) maar niet alle vier enchanted**
> **Sanfew:** That meat hasn't been dipped in the Cauldron of Thunder yet. Raw meat is no offering, it's just dinner.

3. **Overige gevallen**
> **Sanfew:** I need enchanted beef, enchanted rat, enchanted bear and enchanted chicken.
> **Sanfew:** Dip the raw meat in the Cauldron of Thunder, and bring all four to me at once.

**[stage 3]**
> **Sanfew:** The ritual is complete. Go and see Kaqemeex, he's waiting for you.

**[stage 4]**
> **Sanfew:** The young druids have started sitting where the circle once stood again. No stones, just people. Perhaps that is how it was always meant to be.

---

## 7. Herblore-unlock

- Herbs cleanen, unfinished potions maken en potions mengen kan pas na deze quest (stage 4).
- Als er al checks op Druidic Ritual zijn, laat die naar deze quest verwijzen in plaats van dubbele checks toe te voegen.
- Zonder voltooide quest, bij het cleanen van een herb:
> *(chatbox)* You need to complete The Lost Circle to use the Herblore skill.

---

## 8. Beloningen (bij stage 4)

- 4 Quest points
- 250 Herblore XP
- Toegang tot Herblore
- Quest complete-scherm met:
  - 4 Quest Points
  - 250 Herblore XP
  - Access to Herblore
- Item op het questcompletescherm: Guam leaf (of Enchanted beef, volg wat de server toont)

---

## 9. Quest journal

| Stage | Tekst |
|---|---|
| 0 | I can start this quest by speaking to **Kaqemeex** in **Taverley**. |
| 1 | The stone circle of Taverley has crumbled to dust and the druids have lost their power. I should speak to **Sanfew** on the upper floor of his house in **Taverley**. |
| 2 | Sanfew needs **enchanted beef**, **enchanted rat**, **enchanted bear** and **enchanted chicken**. I can make them by dipping raw meat in the **Cauldron of Thunder**. |
| 3 | Sanfew performed the ritual and the earth answered. I should return to **Kaqemeex**. |
| 4 | **QUEST COMPLETE!** Kaqemeex has taught me the art of **Herblore**. |

---

## 10. Checklist

- [ ] Gecontroleerd of Druidic Ritual bestaat; varp hergebruikt of nieuwe aangemaakt
- [ ] Oude Druidic Ritual-route uitgeschakeld (indien aanwezig), zonder andere functies van Kaqemeex en Sanfew te breken
- [ ] Quest geregistreerd als "The Lost Circle", 4 QP
- [ ] Dialogen voor Kaqemeex en Sanfew per stage
- [ ] Cauldron of Thunder: vier soorten rauw vlees → enchanted versie, alleen in stage 2
- [ ] Sanfew accepteert alleen alle vier enchanted meats tegelijk
- [ ] Herblore-checks wijzen naar deze quest
- [ ] Rewards en quest complete-scherm
- [ ] Quest journal per stage
- [ ] Getest: volledige doorloop van stage 0 tot 4
- [ ] Getest: rauw vlees bij Sanfew inleveren
- [ ] Getest: cauldron gebruiken vóór en na stage 2
- [ ] Getest: herbs cleanen vóór en na de quest
