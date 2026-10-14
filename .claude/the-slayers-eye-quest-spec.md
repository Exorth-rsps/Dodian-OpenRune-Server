# Quest specificatie: The Slayer's Eye

> **Instructie voor Claude Code:** implementeer deze quest in de bestaande codebase van deze OSRS private server.
> 1. Zoek eerst hoe bestaande quests zijn opgebouwd (questregistratie, quest journal, stage/varp-opslag, dialogue-systeem, NPC-interactiehandlers, drop tables, rewards) en volg exact dezelfde patronen en mappenstructuur.
> 2. Alle ID's hieronder staan als `TODO`. Zoek ze op in de bestaande item- en NPC-definities. Als een NPC of item nog niet bestaat, vraag het aan de ontwikkelaar in plaats van zelf een ID te verzinnen. NPC-posities en spawns worden door de ontwikkelaar geregeld.
> 3. Neem de dialogen **letterlijk** over.
> 4. Werk de checklist onderaan af en meld wat je niet kon implementeren.

---

## 1. Overzicht

| Veld | Waarde |
|---|---|
| Naam | **The Slayer's Eye** |
| Doel | Speler maakt de Enchanted gem en speelt daarmee de Slayer skill vrij |
| Startpunt | Aya, Heroes' Guild |
| Quest points | 1 |
| Vereisten | Geen |
| Beloning | 1 QP, 10.000 Slayer XP, 20.000 coins (Enchanted gem heeft de speler al) |

**Gevolg van voltooien:** pas na deze quest geven slayermasters taken. Zie sectie 7.

---

## 2. NPC's

| NPC | Locatie | NPC ID |
|---|---|---|
| Aya | Heroes' Guild | `TODO` |
| Tureal | Yanille prison (bewaker) | `TODO` |
| Gem Trader | Gem stall, Yanille | `TODO` |
| Aubury | Wizards' Guild (magic guild), Yanille | `TODO` |

Let op: als Aubury of Tureal al bestaande dialogen hebben (bijv. shop of slayermaster), voeg dan de questdialoog toe als extra optie of laat die voorgaan zolang de quest bezig is. Verwijder bestaande functionaliteit niet.

---

## 3. Items

| Item | Item ID | Bron |
|---|---|---|
| Uncut sapphire | `TODO` | Bestaand item, speler regelt zelf |
| Sapphire | `TODO` | Zelf craften (chisel) of door Gem Trader laten cutten |
| Bone shard | `TODO` | Questdrop: Skeletons |
| Eye of newt | `TODO` | Questdrop: Moss giants |
| Enchanted gem | `TODO` | Gemaakt door Aubury |
| Coins | `TODO` | Beloning |

---

## 4. Stages (varp/varbit)

| Stage | Betekenis | Wordt gezet door |
|---|---|---|
| 0 | Niet gestart | – |
| 1 | Gestart, moet naar Tureal | Aya (optie "Yes, I'll help you.") |
| 2 | Tureal gesproken, moet naar Gem Trader | Tureal eerste gesprek |
| 3 | Gem Trader gesproken, moet naar Aubury | Gem Trader eerste gesprek |
| 4 | Aubury gesproken, verzamelt items | Aubury eerste gesprek |
| 5 | Enchanted gem ontvangen, terug naar Aya | Aubury (items ingeleverd) |
| 6 | Quest voltooid | Aya afronding |

---

## 5. Drops

- **Bone shard** van alle Skeletons en **Eye of newt** van alle Moss giants.
- Alleen droppen als quest stage **2 t/m 5** is én de speler het item nog niet in inventory of bank heeft. Zo kan een speler die vooruitwerkt het al verzamelen, maar ontstaan er geen stapels.
- Voorgestelde drop rate: **1/5**. Maak dit een makkelijk aanpasbare constante.
- Drop alleen zichtbaar voor de speler die de kill maakte (als de server dat ondersteunt).

---

## 6. Dialogen

Notatie:
- `[Conditie]` geeft aan wanneer een dialoog getoond wordt.
- `>` zijn keuzeopties voor de speler.
- `{ACTIE}` is een code-actie op dat punt.

### 6.1 Aya

**[stage 0]**
> **Aya:** Ah, an adventurer. You have the look of someone who's faced a few monsters in their time.
> **Player:** I've had my share. Is something wrong?
> **Aya:** Something's been lost. A stone... we call it the Enchanted Gem. It's like an eye. Through it, a slayer master can see the tasks they've given, and the slayer can see how many creatures remain.
> **Aya:** Without it, no slayer master can hand out tasks. The whole craft is grinding to a halt.
> **Player:** Can't you just make a new one?
> **Aya:** If only it were that simple. I never learned how they were made. But my father... he knows far more about the Eye than I ever did.
> **Aya:** Perhaps he even knows how to make one. Will you help me?
>
> **Opties:**
> - `Yes, I'll help you.`
> - `Sorry, I'm busy.`
>
> *Yes, I'll help you.*
> **Player:** Yes, I'll help you. Where can I find your father?
> **Aya:** His name is Tureal. He works as a guard at the prison in Yanille. Tell him I sent you. He can be a bit gruff, but he means well.
> **Aya:** Thank you, adventurer. The slayer masters will owe you a great deal.
> `{ACTIE: start quest, stage = 1}`
>
> *Sorry, I'm busy.*
> **Player:** Sorry, I'm busy right now.
> **Aya:** I understand. If you change your mind, you know where to find me.

**[stage 1 t/m 4]**
> **Aya:** Have you spoken to my father yet? He's a guard at the prison in Yanille.
> **Aya:** If anyone knows how to make a new Eye, it's him.

**[stage 5, speler heeft geen Enchanted gem bij zich]**
> **Aya:** Aubury made the Eye for you? Wonderful! But where is it? Bring it here so I can see it with my own eyes.

**[stage 5, speler heeft Enchanted gem in inventory]**
> **Player:** Aya, I have it. A new Enchanted Gem!
> **Aya:** Let me see... By the gods, it's real. You've actually done it!
> **Aya:** My father, the gem trader, Aubury... and you, bringing it all together.
> **Aya:** You've saved the work of the slayer masters. Without you, the craft would have died out completely.
> **Aya:** And there's something else. Anyone who carries an Eye is a slayer. From today, that includes you.
> **Aya:** Visit any slayer master and they'll give you a task. Use the gem to check how many creatures you still have to slay.
> **Aya:** Welcome to the hunt, slayer.
> `{ACTIE: stage = 6, quest complete, rewards geven, quest complete-scherm tonen}`
> *(De speler houdt de Enchanted gem.)*

**[stage 6]**
> **Aya:** Hello again, slayer. Have you been to a slayer master yet? They'll be glad to have someone like you.

### 6.2 Tureal

**[stage 0]** *(alleen als Tureal geen andere standaarddialoog heeft)*
> **Tureal:** Move along. This is a prison, not a tourist attraction.

**[stage 1]**
> **Player:** Are you Tureal? Your daughter Aya sent me.
> **Tureal:** Aya? Hah, she still remembers her old man. What does she want?
> **Player:** She says the Enchanted Gem has been lost. The slayer masters can't give out tasks without it.
> **Tureal:** The Eye... I haven't heard anyone speak of it in years.
> **Tureal:** Yes, I know what it is. Every slayer used to carry one. Slayer master hands out a task, and the Eye keeps track of it for you. Clever little things.
> **Player:** Do you know how to make one?
> **Tureal:** Not myself, no. But I know what goes into it. You'll need an uncut sapphire, a bone shard and an eye of newt.
> **Tureal:** The sapphire forms the body of the Eye. The bone shard gives it the memory of the creatures it hunts. And the eye of newt... well, it's an eye, isn't it? It lets the stone see.
> **Player:** And who puts it all together?
> **Tureal:** I had a friend who could make them. He runs the gem stall here in Yanille. Go and talk to him, tell him Tureal sent you.
> `{ACTIE: stage = 2}`

**[stage 2 t/m 5]**
> **Tureal:** Still here? You'll need an uncut sapphire, a bone shard and an eye of newt.
> **Tureal:** Talk to the gem trader in Yanille. He made the Eyes back in the day.

**[stage 6]**
> **Tureal:** So you're the one who made the new Eye. Aya told me all about it. Good work, slayer.

### 6.3 Gem Trader

Questdialoog is een extra optie naast de bestaande shopfunctie, beschikbaar vanaf stage 2.

**[stage 2]**
> **Player:** Tureal sent me. He says you used to make the Enchanted Gems.
> **Gem Trader:** Tureal? Ha! Haven't seen that old dog in years. The Enchanted Gems, you say...
> **Gem Trader:** I'm afraid he's given me too much credit. I never did anything special. I only cut the uncut sapphires into proper sapphires.
> **Gem Trader:** Any decent crafter can do that. You could do it yourself, or I can do it for you.
> **Player:** Then who made them into Enchanted Gems?
> **Gem Trader:** That was the work of a wizard. Aubury, he lives at the Wizards' Guild here in Yanille. With the right ingredients, he can enchant the sapphire.
> `{ACTIE: stage = 3}`
> → ga door naar **Cut-opties** hieronder.

**[stage 3 t/m 5] (questoptie gekozen)**
> **Gem Trader:** Back again? Remember, Aubury at the Wizards' Guild can enchant the sapphire.
> → ga door naar **Cut-opties**.

**Cut-opties:**
> **Opties:**
> - `Can you cut my uncut sapphire for me?`
> - `I'll cut it myself.`
> - `Thanks, I'll go see Aubury.`
>
> *Can you cut my uncut sapphire for me?* — **speler heeft uncut sapphire**
> **Player:** Can you cut my uncut sapphire for me?
> **Gem Trader:** For a friend of Tureal? Of course. Hand it over...
> `{ACTIE: verwijder 1 Uncut sapphire, geef 1 Sapphire. Gratis, geen Crafting-level nodig, geen XP}`
> **Gem Trader:** There you go. Clean cut, if I may say so myself. Take it to Aubury at the Wizards' Guild.
>
> *Can you cut my uncut sapphire for me?* — **speler heeft geen uncut sapphire**
> **Gem Trader:** You don't have an uncut sapphire with you. Bring me one and I'll cut it for you.
>
> *I'll cut it myself.*
> **Player:** I'll cut it myself.
> **Gem Trader:** Good for you. Just bring the cut sapphire to Aubury at the Wizards' Guild.
>
> *Thanks, I'll go see Aubury.*
> **Player:** Thanks, I'll go see Aubury.
> **Gem Trader:** Don't forget, he'll need the sapphire cut. Come back if you want me to do it.

### 6.4 Aubury (Wizards' Guild, Yanille)

Questdialoog is een extra optie naast bestaande functionaliteit, beschikbaar vanaf stage 3.

**[stage 3]**
> **Player:** The gem trader sent me. He says you can make an Enchanted Gem.
> **Aubury:** An Enchanted Gem? My, my... that takes me back. Yes, I used to enchant those for the slayer masters.
> **Aubury:** I'll need three things: a cut sapphire, a bone shard and an eye of newt.
> **Player:** Where can I find a bone shard and an eye of newt?
> **Aubury:** Skeletons sometimes carry bone shards. Old, brittle things, they practically fall apart when you hit them.
> **Aubury:** As for the eye of newt, try the moss giants. Don't ask me why they have them. Giants eat all sorts of things.
> **Aubury:** Bring me all three and I'll enchant the gem for you.
> `{ACTIE: stage = 4}`
> *(Als de speler op dit moment al alle drie items heeft: ga direct door naar "Alle items aanwezig".)*

**[stage 4] — controleer in deze volgorde:**

1. **Alle items aanwezig** (Sapphire + Bone shard + Eye of newt in inventory)
> **Player:** I have everything you need.
> **Aubury:** Excellent! Let me see... a sapphire, a bone shard and an eye of newt. Perfect.
> **Aubury:** Stand back a little. This needs concentration.
> `{ACTIE: verwijder 1 Sapphire, 1 Bone shard, 1 Eye of newt. Geef 1 Enchanted gem. Optioneel: animatie/graphic. stage = 5}`
> **Aubury:** There we are. Look closely, it's watching you.
> **Aubury:** Take it back to whoever sent you. I'm sure they'll be very pleased.

2. **Uncut sapphire in plaats van Sapphire** (geen Sapphire, wel Uncut sapphire)
> **Aubury:** That sapphire is still uncut. I can't enchant it like that. Cut it yourself, or have the gem trader do it for you.

3. **Overige gevallen**
> **Aubury:** Do you have everything? I need a cut sapphire, a bone shard and an eye of newt.
> **Aubury:** Remember, skeletons drop the bone shards and moss giants carry the eyes of newt.

**[stage 5, speler heeft geen Enchanted gem meer in inventory of bank]**
> **Aubury:** Lost it already? Bring me another sapphire, bone shard and eye of newt and I'll make you a new one.
> *(Gebruik dezelfde itemcheck als stage 4; bij succes geen stage-wijziging.)*

**[stage 5, speler heeft de Enchanted gem nog]**
> **Aubury:** Go on, take the gem to whoever sent you.

---

## 7. Slayer-unlock

- Slayermasters geven alleen taken als `The Slayer's Eye` voltooid is (stage 6).
- Zonder voltooide quest zegt elke slayermaster bij het vragen om een taak:
> **Slayer Master:** You don't even have an Enchanted Gem. Speak to Aya in the Heroes' Guild if you want to become a slayer.
- Na de quest werkt de Enchanted gem zoals normaal (taakvoortgang checken, enz.). Het kopen van een vervangende gem bij slayermasters mag pas na de quest.

---

## 8. Beloningen (bij stage 6)

- 1 Quest point
- 10.000 Slayer XP
- 20.000 coins → inventory; als die vol is: bank (of op de grond, volg de bestaande conventie van de server)
- Quest complete-scherm met:
  - 1 Quest Point
  - 10,000 Slayer XP
  - 20,000 coins
  - Access to Slayer masters
- Het item op het questcompletescherm: Enchanted gem

---

## 9. Quest journal

| Stage | Tekst |
|---|---|
| 0 | I can start this quest by speaking to **Aya** in the **Heroes' Guild**. |
| 1 | Aya told me the Enchanted Gem has been lost. Her father, **Tureal**, might know more. He's a guard at the **Yanille prison**. |
| 2 | Tureal said I need an **uncut sapphire**, a **bone shard** and an **eye of newt**. He sent me to the **gem trader** in **Yanille**. |
| 3 | The gem trader only cut the sapphires. **Aubury** at the **Wizards' Guild** in **Yanille** can enchant the gem. |
| 4 | Aubury needs a **cut sapphire**, a **bone shard** (dropped by skeletons) and an **eye of newt** (dropped by moss giants). |
| 5 | Aubury made the **Enchanted gem**. I should show it to **Aya** in the **Heroes' Guild**. |
| 6 | **QUEST COMPLETE!** I have become a slayer and can now get tasks from any slayer master. |

Doorgestreepte (voltooide) regels volgen de bestaande stijl van de server.

---

## 10. Checklist

- [ ] Quest geregistreerd met naam "The Slayer's Eye", 1 QP, zichtbaar in de questlijst
- [ ] Stage-opslag via varp/varbit volgens het bestaande quest-patroon
- [ ] Dialogen voor Aya, Tureal, Gem Trader en Aubury per stage
- [ ] Bestaande dialogen/shops van deze NPC's blijven werken
- [ ] Gem Trader cut sapphire gratis zonder Crafting-check
- [ ] Questdrops Bone shard (Skeletons) en Eye of newt (Moss giants) met stagecheck en duplicaatcheck
- [ ] Aubury itemcheck, itemruil en Enchanted gem
- [ ] Vervangende gem bij Aubury in stage 5
- [ ] Rewards en quest complete-scherm
- [ ] Quest journal per stage
- [ ] Slayermasters blokkeren taken tot de quest voltooid is
- [ ] Getest: volledige doorloop van stage 0 tot 6
- [ ] Getest: items in "verkeerde" volgorde verzamelen (drops vóór Aubury gesproken)
- [ ] Getest: volle inventory bij reward
