# Quest specificatie: The Silver Blade

> **Instructie voor Claude Code:** implementeer deze quest in de bestaande codebase van deze OSRS private server.
> 1. Deze quest is de **Dragon Slayer-variant** van deze server: hij speelt de **Rune platebody** en de **Green d'hide body** vrij. Het verhaal en de mechaniek komen uit **Demon Slayer** (Silverlight, drie sleutels, Delrith).
>    - Zoek of Dragon Slayer en/of Demon Slayer al in de codebase zitten. **Zo ja:** laat alle bestaande checks op "Dragon Slayer voltooid" (rune platebody dragen/kopen, green d'hide body dragen) naar deze quest verwijzen en schakel de oude questroutes uit, zonder andere functies van de betrokken NPC's te breken.
>    - **Zo nee:** maak een nieuwe varp/varbit aan volgens het bestaande quest-patroon.
> 2. Volg de bestaande patronen voor questregistratie, requirement-checks, quest journal, dialogen, NPC-handlers, combat/spawns, equipment-requirements, shops en rewards.
> 3. Alle ID's staan als `TODO`. Zoek ze op in de bestaande definities. Bestaat een NPC of item nog niet, vraag het dan aan de ontwikkelaar. NPC-posities en spawns regelt de ontwikkelaar, behalve Delrith (wordt via code opgeroepen).
> 4. **Alle items bestaan in OSRS. Er worden geen nieuwe items aangemaakt.**
> 5. Neem de dialogen **letterlijk** over en werk de checklist onderaan af.

---

## 1. Overzicht

| Veld | Waarde |
|---|---|
| Naam | **The Silver Blade** |
| Doel | De demon Delrith verslaan met Silverlight en het vertrouwen van Oziach winnen |
| Startpunt | Oziach |
| Quest points | 2 |
| Beloning | 2 QP, Rune platebody en Green d'hide body dragen, Oziach verkoopt de Rune platebody, Silverlight houden, 10.000 Strength XP, 10.000 Defence XP |

### Requirements

| Requirement | Waarde |
|---|---|
| Quest | **The Darkened Shield** voltooid |

Dit is een vervolg op The Darkened Shield. Via die quest heeft de speler al 10 QP en 50 Attack, Strength en Defence.

### Het verhaal in het kort
In The Darkened Shield vluchtte de dark wizard Viyeldi met de woorden *"There will be other crystals... and other doors."* Hij is teruggekeerd naar dezelfde heuvels ten oosten van de Fishing Guild en heeft een nieuwe poort geopend. Daardoor is de demon **Delrith** naar deze wereld gekomen.
Oziach, een oude en nurkse held, verkoopt zijn rune platebody alleen aan iemand die een echt monster heeft verslagen. Delrith is precies dat monster. Een gewoon wapen doet hem niets: alleen **Silverlight**, het zwaard waarmee Sir Prysins voorvader Delrith ooit verbande, kan hem verwonden. Sir Prysin bewaart het zwaard in een kist met drie sloten. Hij heeft zelf één sleutel. De andere twee heeft hij jaren geleden aan twee vrienden gegeven: **Wizard Traiborn** en **Captain Rovin**.

---

## 2. NPC's

| NPC | Locatie | NPC ID |
|---|---|---|
| Oziach | Keuze ontwikkelaar | `TODO` (bestaande OSRS-NPC) |
| Sir Prysin | Keuze ontwikkelaar | `TODO` (bestaande OSRS-NPC) |
| Wizard Traiborn | Huis bij de air ruins (zelfde NPC als in The Forgotten Altar) | `TODO` |
| Captain Rovin | Keuze ontwikkelaar | `TODO` (bestaande OSRS-NPC) |
| Viyeldi | Bruine heuvels ten oosten van de Fishing Guild (zelfde plek als in The Darkened Shield) | `TODO` |
| Delrith | Wordt opgeroepen naast Viyeldi | `TODO` (bestaande OSRS-NPC) |

**Let op Viyeldi:** in The Darkened Shield wordt Viyeldi na stage 4 verborgen (als dat kon). In deze quest moet hij weer zichtbaar zijn voor spelers in stage **1 t/m 4** van The Silver Blade, en weer verborgen in stage 5. Zorg dat de zichtbaarheidsregels van beide quests samenwerken.

**Let op Traiborn:** hij heeft al dialogen uit The Forgotten Altar. Laat de questdialoog van deze quest voorgaan zolang de quest loopt. Breek The Forgotten Altar niet.

---

## 3. Items

| Item | Item ID | Bron |
|---|---|---|
| Silverlight key (Sir Prysin) | `TODO` | Sir Prysin |
| Silverlight key (Wizard Traiborn) | `TODO` | Wizard Traiborn, voor 25 Bones |
| Silverlight key (Captain Rovin) | `TODO` | Captain Rovin |
| Bones | `TODO` | Monsters (normale drop) |
| Silverlight | `TODO` | Sir Prysin, als alle drie sleutels terug zijn |
| Rune platebody | `TODO` | Beloning: dragen en kopen |
| Green d'hide body | `TODO` | Beloning: dragen |

De drie Silverlight keys zijn questitems: niet verhandelbaar, en ze verdwijnen als Sir Prysin de kist opent.

---

## 4. Stages

| Stage | Betekenis | Wordt gezet door |
|---|---|---|
| 0 | Niet gestart | – |
| 1 | Oziach gesproken, moet naar Sir Prysin | Oziach |
| 2 | Sir Prysin gesproken, verzamelt sleutels | Sir Prysin |
| 3 | Silverlight ontvangen, moet naar Delrith | Sir Prysin |
| 4 | Delrith verbannen | Delrith (incantatie) |
| 5 | Quest voltooid | Oziach |

### Extra opslag (stage 2)
| Flag / teller | Betekenis |
|---|---|
| `TRAIBORN_BONES` | Aantal ingeleverde bones bij Traiborn (0–25) |
| `TRAIBORN_KEY_GIVEN` | Traiborn heeft zijn sleutel gegeven |
| `ROVIN_KEY_GIVEN` | Rovin heeft zijn sleutel gegeven |

Sir Prysin geeft zijn eigen sleutel niet af: hij houdt die tot de speler de andere twee brengt.

---

## 5. Dialogen

Notatie: `[Conditie]`, opties onder **Opties**, `{ACTIE}` voor code-acties.

### 5.1 Oziach

**[stage 0, The Darkened Shield niet voltooid]**
> **Oziach:** Aye, I've got a rune platebody. Finest armour money can buy. And no, it's not for sale. Not to the likes of you.
> **Oziach:** Come back when you've done something worth telling.

**[stage 0, The Darkened Shield voltooid]**
> **Oziach:** You again? I've heard the stories. A demon slain at the Watchtower. Not bad.
> **Player:** I was hoping to buy your rune platebody.
> **Oziach:** Hah! One demon and you think you're a hero. I've slain more beasts than you've had hot dinners.
> **Oziach:** Still... there is something you could do. That dark wizard you chased off, Viyeldi. He's back. Same hills, east of the Fishing Guild.
> **Oziach:** And this time he hasn't summoned some lesser demon. He's brought back Delrith. An ancient thing. It took a legendary sword to banish it the last time.
> **Oziach:** Banish Delrith, and I'll sell you the armour. I might even let you wear it in my presence.
>
> **Opties:**
> - `I'll deal with Delrith.`
> - `That sounds like a job for someone else.`
>
> *I'll deal with Delrith.*
> **Player:** I'll deal with Delrith. Where do I find that legendary sword?
> **Oziach:** Silverlight. Sir Prysin keeps it. His ancestor Wally used it to banish Delrith centuries ago.
> **Oziach:** Don't even think about going after that demon with an ordinary weapon. It'll laugh at you. Then it'll eat you.
> `{ACTIE: start quest, stage = 1}`
>
> *That sounds like a job for someone else.*
> **Player:** That sounds like a job for someone else.
> **Oziach:** That's what they all say. That's why I still have the armour.

**[stage 1 t/m 3]**
> **Oziach:** Is Delrith banished yet? No? Then stop wasting my time. Sir Prysin has the sword, and the demon is on the hills east of the Fishing Guild.

**[stage 4]**
> **Player:** Delrith is banished. Viyeldi ran off again.
> **Oziach:** ...You're not lying, are you? I can see it in your face. You've looked into the eyes of something old.
> **Oziach:** Fine. A deal's a deal. From now on, the rune platebody is yours to buy, and to wear.
> **Oziach:** And you can wear green dragonhide too, while you're at it. Anyone who can face Delrith can handle a bit of leather.
> **Oziach:** Keep that sword close. Wizards like Viyeldi never stay gone for long.
> `{ACTIE: stage = 5, quest complete, rewards, quest complete-scherm}`

**[stage 5]**
> **Oziach:** Back for more armour? Good. Heroes get through it faster than anyone.
> → daarna shop

### 5.2 Sir Prysin

**[stage 0]**
> **Sir Prysin:** Good day. Forgive me, I'm rather busy.

**[stage 1]**
> **Player:** Oziach sent me. Delrith has returned, and I need Silverlight.
> **Sir Prysin:** Delrith? Back? By Saradomin... my ancestor Wally gave his life's work to banish that creature.
> **Sir Prysin:** Silverlight is the only weapon that can harm it. I keep it locked in a chest with three locks, so that no single person can ever take it.
> **Player:** Then open it, please.
> **Sir Prysin:** If only it were that easy. I have one key. The other two I gave to two old friends, for safekeeping.
> **Sir Prysin:** Wizard Traiborn has the second. He lives next to the air ruins. Brilliant man, though... a little forgetful.
> **Sir Prysin:** Captain Rovin has the third. He's as stubborn as a mule, so mention my name and tell him exactly why you need it.
> **Sir Prysin:** Bring me both keys and I'll open the chest.
> **Sir Prysin:** And remember Wally's words. When Delrith falls, you must speak the incantation, or it will simply rise again: Carlem... Aber... Camerinthum... Purchai... Gabindo.
> **Player:** Could you say that one more time?
> **Sir Prysin:** Carlem, Aber, Camerinthum, Purchai, Gabindo. In that order. Get it wrong and you'll be fighting it all night.
> `{ACTIE: stage = 2}`

**[stage 2] — controleer in deze volgorde:**

1. **Speler heeft beide sleutels (Traiborn en Rovin)**
> **Player:** I have both keys.
> **Sir Prysin:** Wonderful! Let me add mine...
> *(chatbox)* Sir Prysin turns all three keys at once. The chest creaks open.
> **Sir Prysin:** Silverlight. It hasn't seen daylight in years. It will glow when you face evil.
> **Sir Prysin:** Go. And don't forget the words: Carlem, Aber, Camerinthum, Purchai, Gabindo.
> `{ACTIE: verwijder Silverlight key (Wizard Traiborn) en Silverlight key (Captain Rovin), geef Silverlight, stage = 3}`

2. **Overige gevallen** *(toon alleen de regels voor ontbrekende sleutels)*
> **Sir Prysin:** I still need the key from Wizard Traiborn, next to the air ruins.
> **Sir Prysin:** I still need the key from Captain Rovin. Mention my name.
> **Sir Prysin:** The incantation, in case you've forgotten: Carlem, Aber, Camerinthum, Purchai, Gabindo.

**[stage 3, speler heeft geen Silverlight in inventory, equipment of bank]**
> **Sir Prysin:** You lost Silverlight?! ...Thankfully, the sword always finds its way back to my family. I found it in the chest this morning. Here.
> `{ACTIE: geef Silverlight}`

**[stage 3, speler heeft Silverlight]**
> **Sir Prysin:** Go, before Delrith grows stronger. Carlem, Aber, Camerinthum, Purchai, Gabindo.

**[stage 4 t/m 5]**
> **Sir Prysin:** Wally would be proud. Keep Silverlight. It has chosen you now.

### 5.3 Wizard Traiborn

**[stage 2, sleutel nog niet gegeven, bones = 0]**
> **Player:** Sir Prysin says you have one of the Silverlight keys.
> **Wizard Traiborn:** A key? Did I? ...Oh yes! Prysin's key! I put it somewhere very safe.
> **Wizard Traiborn:** So safe that I can't remember where. Typical.
> **Wizard Traiborn:** No matter, I can summon it back. I'll just need some bones for the spell. Twenty-five should do it.
> **Player:** Twenty-five bones?
> **Wizard Traiborn:** It's a very old spell. Old spells are hungry. You can bring them in a few at a time, I'll keep count. I'll write it down this time. Probably.
> `{ACTIE: als de speler Bones bij zich heeft: neem min(aantal, 25 − ingeleverd), verhoog TRAIBORN_BONES}`

**[stage 2, sleutel nog niet gegeven, bones 1–24]**
> **Wizard Traiborn:** Bones! Lovely. Hand them over.
> `{ACTIE: neem min(aantal, 25 − ingeleverd) Bones, verhoog TRAIBORN_BONES}`
> **Wizard Traiborn:** That's {INGELEVERD} so far. Only {RESTEREND} to go. Or was it more? No, I wrote it down. On my hand.
> *(Heeft de speler geen bones bij zich, alleen de tweede zin.)*

**[stage 2, teller wordt 25]**
> **Wizard Traiborn:** Twenty-five! Now stand back... Bones of old, bring back what's lost...
> *(chatbox)* There is a puff of smoke. A small silver key falls out of Traiborn's hat.
> **Wizard Traiborn:** My hat! Of course. It's always in my hat. Here you go.
> `{ACTIE: geef Silverlight key (Wizard Traiborn), TRAIBORN_KEY_GIVEN = true}`

**[stage 2, sleutel gegeven, speler heeft hem niet meer in inventory of bank]**
> **Wizard Traiborn:** Lost it already? Don't worry, I made a spare. I keep it... in my hat.
> `{ACTIE: geef Silverlight key (Wizard Traiborn)}`

**[stage 2, sleutel gegeven, speler heeft hem]**
> **Wizard Traiborn:** Take the key to Prysin. And say hello to him from me. Or did I already do that?

**[stage 3 t/m 5]**
> **Wizard Traiborn:** Silverlight! I remember that sword. Or I think I do. Shiny thing, isn't it?

### 5.4 Captain Rovin

**[stage 0 t/m 1]**
> **Captain Rovin:** Move along, citizen. I have important matters to attend to.

**[stage 2, sleutel nog niet gegeven]**
> **Player:** I need the Silverlight key Sir Prysin gave you.
> **Captain Rovin:** The key? Absolutely not. Do you know how many people have tried to talk me out of that key? Dozens. Liars, every one of them.
> **Captain Rovin:** Tell me why you need it. And it had better be good.
>
> **Opties:**
> - `Delrith has returned, and Sir Prysin sent me to banish it.`
> - `I want to sell the sword.`
> - `I'm a collector of shiny keys.`
>
> *Delrith has returned, and Sir Prysin sent me to banish it.*
> **Player:** Delrith has returned, and Sir Prysin sent me to banish it.
> **Captain Rovin:** Delrith? ...Prysin only ever told one person what that key was for. Me. And now you know too, so he must have told you.
> **Captain Rovin:** Very well. Take it. And if you get yourself killed, don't come crying to me.
> `{ACTIE: geef Silverlight key (Captain Rovin), ROVIN_KEY_GIVEN = true}`
>
> *I want to sell the sword.*
> **Player:** I want to sell the sword.
> **Captain Rovin:** At least you're honest. Honestly stupid. Get out.
>
> *I'm a collector of shiny keys.*
> **Player:** I'm a collector of shiny keys.
> **Captain Rovin:** And I'm the Queen of Karamja. No.

**[stage 2, sleutel gegeven, speler heeft hem niet meer in inventory of bank]**
> **Captain Rovin:** You lost it? Prysin's key? ...Thankfully, I had a copy made. Don't tell him. Here.
> `{ACTIE: geef Silverlight key (Captain Rovin)}`

**[stage 2, sleutel gegeven, speler heeft hem]**
> **Captain Rovin:** You have the key. What are you still doing here?

**[stage 3 t/m 5]**
> **Captain Rovin:** So you're the one who faced Delrith. Hmph. Not bad, citizen. Not bad at all.

### 5.5 Viyeldi

**[stage 1 t/m 2]**
> **Viyeldi:** You again! Come to steal another crystal? There's nothing for you here. Not yet.
> **Viyeldi:** Come back with a real weapon. If you can find one.

**[stage 3, speler heeft Silverlight niet bij zich of uitgerust]**
> **Viyeldi:** No sword? No chance. Delrith would eat you before breakfast.

**[stage 3, speler heeft Silverlight bij zich of uitgerust]**
> **Viyeldi:** Silverlight?! Where did you... no matter. Not even that old blade will save you.
> **Viyeldi:** DELRITH! Rise from the dark and tear this fool apart!
> *(chatbox)* Viyeldi vanishes in a cloud of black smoke, laughing.
> `{ACTIE: spawn Delrith (zie sectie 6). Viyeldi blijft weg zolang Delrith leeft.}`

**[stage 3, Delrith verdwenen zonder te zijn verbannen]** *(speler gestorven of weggelopen)*
> **Viyeldi:** Back for more? DELRITH! Again!
> `{ACTIE: spawn Delrith}`

**[stage 4]** *(Viyeldi verbergen als dat kan; zo niet:)*
> **Viyeldi:** You've won again... but every door you close, I'll open two more.

---

## 6. Delrith (gevecht en incantatie)

- Spawnt naast Viyeldi in stage 3. **Alleen aanvalbaar door de speler voor wie hij is opgeroepen**, één tegelijk per speler.
- **Stats:** het origineel is level 27, te zwak voor een afsluitende quest. Doel: ongeveer **level 120–140** (iets sterker dan Nezikchened in The Darkened Shield). Maak de stats aanpasbaar via constanten.
- **Alleen Silverlight doet schade.** Met een ander wapen of met magic/ranged:
> *(chatbox)* Your attack doesn't seem to hurt Delrith at all.
- **Aanvallen:** voornamelijk **melee**. Spelers hebben na The Darkened Shield minstens 42 Prayer; de ontwikkelaar beslist of Protect from Melee (43) nodig moet zijn.
- **Despawn:** als de speler sterft, uitlogt of meer dan ~15 tiles wegloopt, of na 5 minuten zonder gevecht.
- **Geen normale drops.**

### Incantatie
Wanneer Delrith **0 HP** bereikt, sterft hij niet, maar wordt hij **verzwakt**:
> *(chatbox)* Delrith is weakened. Now is the time to speak the incantation!

- Delrith valt in deze staat niet aan en kan niet bewegen.
- De speler krijgt een dialoog met **vijf keuzes na elkaar**. Bij elke keuze zijn deze vijf woorden de opties (in willekeurige volgorde): `Carlem`, `Aber`, `Camerinthum`, `Purchai`, `Gabindo`.
- De juiste volgorde is: **Carlem → Aber → Camerinthum → Purchai → Gabindo**.

**Juiste volgorde**
> **Player:** Carlem... Aber... Camerinthum... Purchai... Gabindo!
> *(chatbox)* Delrith screams. A white light pours out of Silverlight, and the demon is pulled back into the void.
> `{ACTIE: despawn Delrith, stage = 4}`

**Verkeerde volgorde** (bij het eerste verkeerde woord stopt de dialoog)
> **Player:** {de gekozen woorden}...
> *(chatbox)* Delrith roars and rises again!
> `{ACTIE: Delrith krijgt 25% HP terug en valt weer aan}`

**Gesprek afgebroken of weggelopen tijdens de verzwakte staat:** na 30 seconden herstelt Delrith zich met 25% HP.

---

## 7. Equipment en shop

- **Rune platebody** kan alleen worden **gedragen** na het voltooien van deze quest (stage 5).
- **Green d'hide body** kan alleen worden **gedragen** na het voltooien van deze quest. Maak dit een aan/uit-constante, zodat de ontwikkelaar het kan uitzetten.
- Bij een poging zonder voltooide quest:
> *(chatbox)* You need to complete The Silver Blade to wear this.
- **Oziach** verkoopt de Rune platebody alleen aan spelers die de quest voltooid hebben. Anders opent zijn shop niet, of hij zegt de stage 0-dialoog.

---

## 8. Beloningen (bij stage 5)

- 2 Quest points
- Rune platebody en Green d'hide body dragen
- Oziach verkoopt de Rune platebody
- Silverlight houden
- 10.000 Strength XP
- 10.000 Defence XP
- Quest complete-scherm met:
  - 2 Quest Points
  - Ability to wear rune platebodies
  - Ability to wear green d'hide bodies
  - 10,000 Strength XP
  - 10,000 Defence XP
- Item op het questcompletescherm: Silverlight

XP is een constante zodat de ontwikkelaar het makkelijk kan aanpassen.

---

## 9. Quest journal

| Stage | Tekst |
|---|---|
| 0 | I can start this quest by speaking to **Oziach**. I must have completed **The Darkened Shield**. |
| 1 | Viyeldi has summoned the demon **Delrith**. Only **Silverlight** can harm it. I should speak to **Sir Prysin**. |
| 2 | Sir Prysin keeps Silverlight in a chest with three locks. I need the keys from **Wizard Traiborn** *(doorgestreept als ontvangen)* and **Captain Rovin** *(doorgestreept als ontvangen)*. |
| 2 | Traiborn needs **25 bones** to summon his key. I have given him **{INGELEVERD}/25**. *(alleen tonen tot de sleutel gegeven is)* |
| 2 | The incantation is: **Carlem, Aber, Camerinthum, Purchai, Gabindo**. |
| 3 | I have **Silverlight**. Delrith is on the **hills east of the Fishing Guild**. When it falls, I must speak the incantation: **Carlem, Aber, Camerinthum, Purchai, Gabindo**. |
| 4 | Delrith has been banished. I should return to **Oziach**. |
| 5 | **QUEST COMPLETE!** Oziach trusts me now. I can wear **rune platebodies** and **green d'hide bodies**. |

---

## 10. Checklist

- [ ] Gecontroleerd of Dragon Slayer en/of Demon Slayer bestaan; checks laten verwijzen naar deze quest en oude routes uitgeschakeld
- [ ] Quest geregistreerd als "The Silver Blade", 2 QP
- [ ] Requirement: The Darkened Shield voltooid
- [ ] Dialogen voor Oziach, Sir Prysin, Wizard Traiborn, Captain Rovin en Viyeldi per stage
- [ ] Viyeldi zichtbaar in stage 1–4 van deze quest, ook na The Darkened Shield
- [ ] The Forgotten Altar-dialogen van Traiborn blijven werken
- [ ] Bones-teller bij Traiborn (0–25), in delen inleveren
- [ ] Rovin: alleen de juiste dialoogoptie levert de sleutel op
- [ ] Vervangende sleutels en Silverlight bij verlies
- [ ] Delrith: spawn per speler, aangepaste stats, alleen Silverlight doet schade, despawn-regels
- [ ] Incantatie bij 0 HP, juiste volgorde verbant, fout geeft 25% HP terug
- [ ] Rune platebody en Green d'hide body alleen dragen na de quest (green d'hide via constante)
- [ ] Oziach verkoopt Rune platebody alleen na de quest
- [ ] Rewards en quest complete-scherm
- [ ] Quest journal per stage
- [ ] Getest: volledige doorloop van stage 0 tot 5
- [ ] Getest: Delrith aanvallen zonder Silverlight
- [ ] Getest: verkeerde incantatie en opnieuw proberen
- [ ] Getest: sterven tijdens het gevecht
- [ ] Getest: rune platebody aantrekken vóór en na de quest
