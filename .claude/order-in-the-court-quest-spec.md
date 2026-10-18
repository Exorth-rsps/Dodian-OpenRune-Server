# Quest specificatie: Order in the Court

> **Instructie voor Claude Code:** implementeer deze quest in de bestaande codebase van deze OSRS private server.
> 1. Deze quest is nieuw: een detective-quest zonder gevecht, in de stijl van Murder Mystery. Hij speelt zich af in en rond het **courthouse in Seers' Village**.
> 2. Volg de bestaande patronen voor questregistratie, quest journal, stage/varp-opslag, dialogen, NPC-handlers, object-interacties en rewards.
> 3. Alle ID's staan als `TODO`. Zoek ze op in de bestaande definities. Bestaat een NPC, item of object nog niet, vraag het dan aan de ontwikkelaar. NPC-posities, spawns en objecten regelt de ontwikkelaar.
> 4. **Alle items bestaan in OSRS. Er worden geen nieuwe items aangemaakt.**
> 5. Neem de dialogen **letterlijk** over en werk de checklist onderaan af.

---

## 1. Overzicht

| Veld | Waarde |
|---|---|
| Naam | **Order in the Court** |
| Doel | Bewijs verzamelen en de echte dader aanwijzen in de rechtszaal van Seers |
| Startpunt | King Arthur, in het courthouse van Seers' Village |
| Quest points | 2 |
| Vereisten | Geen |
| Beloning | 2 QP, 5.000 coins, 1 Antique lamp (2.500 XP) |

### Het verhaal in het kort
Gisteravond werd het bier in de **Forester's Arms** vergiftigd. De helft van Seers' Village lag de hele nacht ziek op bed. Iedereen wijst naar de **Poison Salesman**: wie anders verkoopt er gif in het dorp? Hij zit nu vast in afwachting van zijn proces.
King Arthur leidt de zaak in het courthouse en twijfelt. Hij vraagt de speler om het bewijs te onderzoeken voor het proces begint. De speler ondervraagt de Bartender, de Poison Salesman, de Flax keeper en een Seer, en vindt twee bewijsstukken. De echte dader blijkt de **Flax keeper**: dronken bezoekers van de kroeg lopen elke nacht door zijn vlasveld en vertrappen zijn oogst. Hij wilde de kroeg laten sluiten en een ander de schuld geven.
In de rechtszaal moet de speler de juiste verdachte én het juiste bewijs kiezen. Een verkeerde beschuldiging betekent: opnieuw beginnen.

---

## 2. NPC's en objecten

| NPC / object | Rol | Locatie | ID |
|---|---|---|---|
| King Arthur | Rechter | Courthouse, Seers' Village | `TODO` (bestaande OSRS-NPC) |
| Bartender | Slachtoffer, eigenaar van de Forester's Arms | Forester's Arms, Seers | `TODO` (bestaande OSRS-NPC) |
| Poison Salesman | Beschuldigde (onschuldig) | Seers (positie: ontwikkelaar) | `TODO` (bestaande OSRS-NPC) |
| Flax keeper | Echte dader | Bij het vlasveld van Seers | `TODO` (bestaande OSRS-NPC) |
| Seer | Getuige (komisch, onbetrouwbaar) | Seers' Village | `TODO` (bestaande OSRS-NPC) |
| Bales of flax / haystack (object) | Verstopplek van het bewijs | Naast het huis of veld van de Flax keeper | `TODO` (bestaand object) |

Bestaande functies van deze NPC's (shops, Flax keeper-ruil, enz.) blijven werken. Laat de questdialoog voorgaan zolang de quest loopt.

---

## 3. Items

| Item | Item ID | Bron |
|---|---|---|
| Flax | `TODO` | Krijgt de speler van de Bartender (gevonden bij het vergiftigde vat) |
| Weapon poison | `TODO` | Gevonden in de bales of flax bij de Flax keeper (half leeg) |
| Coins | `TODO` | Beloning |
| Antique lamp | `TODO` | Beloning |

Flax en Weapon poison zijn in deze quest bewijsstukken. Zorg dat de speler het **bewijs-Flax** niet verwart met gewone flax: controleer bij de rechtszaak de flag `FOUND_FLAX` en of de speler minstens 1 Flax bij zich heeft. Dat geldt ook voor de Weapon poison (`FOUND_POISON`).

---

## 4. Stages en flags

| Stage | Betekenis | Wordt gezet door |
|---|---|---|
| 0 | Niet gestart | – |
| 1 | Onderzoek bezig | King Arthur |
| 2 | Quest voltooid | King Arthur (juiste uitspraak) |

| Flag | Betekenis | Gezet door |
|---|---|---|
| `TALKED_BARTENDER` | Bartender gesproken, Flax ontvangen | Bartender |
| `FOUND_FLAX` | Bewijs: Flax bij het vat | Bartender |
| `TALKED_SALESMAN` | Poison Salesman gesproken: hij verkocht gisteren één vial, aan een klant "die naar vlas rook" | Poison Salesman |
| `TALKED_FLAXKEEPER` | Flax keeper gesproken (hij liegt) | Flax keeper |
| `TALKED_SEER` | Seer gesproken (optioneel) | Seer |
| `FOUND_POISON` | Bewijs: Weapon poison gevonden in de flax bales | Object |

- Het object met de Weapon poison werkt pas als `TALKED_SALESMAN` gezet is.
- De rechtszaak kan altijd worden gestart, maar alleen met de juiste verdachte en het juiste bewijs win je.

---

## 5. Dialogen

Notatie: `[Conditie]`, opties onder **Opties**, `{ACTIE}` voor code-acties.

### 5.1 King Arthur (rechter)

**[stage 0]**
> **King Arthur:** Ah, an adventurer. You've arrived at a troubled time. Order in the court, if you please.
> **Player:** What's happened?
> **King Arthur:** Last night, someone poisoned the ale at the Forester's Arms. Half the village spent the night groaning in bed. Myself included, I'm ashamed to say.
> **King Arthur:** The village has already decided who did it. The Poison Salesman. Who else sells poison in Seers?
> **King Arthur:** But a king cannot judge on rumours alone. The man swears he's innocent, and something about this case doesn't sit right with me.
> **King Arthur:** Will you investigate before the trial begins? Speak to the Bartender, the accused, anyone who might know something. Then come back and present your findings to this court.
>
> **Opties:**
> - `I'll find out the truth.`
> - `Sounds like the Poison Salesman did it.`
>
> *I'll find out the truth.*
> **Player:** I'll find out the truth.
> **King Arthur:** Excellent. Start at the Forester's Arms. The Bartender found something by the barrel this morning.
> `{ACTIE: start quest, stage = 1}`
>
> *Sounds like the Poison Salesman did it.*
> **Player:** Sounds like the Poison Salesman did it.
> **King Arthur:** That's what everyone says. That's exactly what worries me. Come back if you'd like to help.

**[stage 1, speler wil nog niet naar de rechtszaak]**
> **King Arthur:** How goes the investigation? Are you ready to present your case to the court?
>
> **Opties:**
> - `I'm ready to present my case.` → ga naar **De rechtszaak**
> - `Not yet.`
>
> *Not yet.*
> **King Arthur:** Take your time. Better a late verdict than a wrong one.

### De rechtszaak

**Stap 1: de verdachte**
> **King Arthur:** Order in the court! The court will now hear the case of the poisoned ale.
> **King Arthur:** Adventurer, who do you accuse?
>
> **Opties:**
> - `The Poison Salesman.`
> - `The Bartender.`
> - `The Flax keeper.`

*The Poison Salesman. / The Bartender.* → ga naar **Verkeerde uitspraak**.

*The Flax keeper.*
> **Player:** I accuse the Flax keeper.
> *(chatbox)* A murmur runs through the courtroom.
> **King Arthur:** The Flax keeper? A serious accusation. What evidence do you bring?

**Stap 2: het bewijs**
> **Opties:**
> - `The flax found by the poisoned barrel.`
> - `The Seer's vision.`
> - `I have no evidence, it's just a feeling.`

*The Seer's vision.*
> **Player:** The Seer foresaw the culprit in a vision.
> **King Arthur:** This court does not accept prophecies. If it did, every case would be decided before the crime was committed.
> → ga naar **Verkeerde uitspraak**.

*I have no evidence, it's just a feeling.* → ga naar **Verkeerde uitspraak**.

*The flax found by the poisoned barrel.*
- **Speler heeft geen Flax bij zich of `FOUND_FLAX` niet gezet** → ga naar **Geen bewijs**.
- **Anders:**
> **Player:** The Bartender found this flax right next to the poisoned barrel.
> **King Arthur:** Flax... in a tavern cellar. Curious. But anyone could have walked through a flax field. Do you have more?

**Stap 3: het doorslaggevende bewijs**
> **Opties:**
> - `This half-empty vial of weapon poison.`
> - `The Flax keeper looked nervous.`

*The Flax keeper looked nervous.*
> **King Arthur:** Everyone looks nervous in a courtroom. I look nervous in a courtroom, and I'm the judge.
> → ga naar **Verkeerde uitspraak**.

*This half-empty vial of weapon poison.*
- **Speler heeft geen Weapon poison bij zich of `FOUND_POISON` niet gezet** → ga naar **Geen bewijs**.
- **Anders:** ga naar **Juiste uitspraak**.

**Juiste uitspraak**
> **Player:** I found this vial of weapon poison hidden in the bales of flax beside the Flax keeper's house.
> **Player:** And the Poison Salesman told me he sold exactly one vial yesterday. To a customer who smelled of flax.
> **King Arthur:** ...Flax keeper. Step forward. What do you have to say?
> **Flax keeper:** ...Fine! It was me! Every night those drunks stumble out of that tavern and straight through my field. Trampling, singing, falling over. My whole harvest is ruined!
> **Flax keeper:** I only wanted the place closed. Nobody was supposed to get properly ill. Just... a little ill.
> **King Arthur:** You poisoned half the village and let an innocent man take the blame. The court finds you guilty.
> **King Arthur:** Your sentence: you will work in the Forester's Arms, without pay, until the Bartender is satisfied. And the Bartender will put up a fence around your field.
> **King Arthur:** Poison Salesman, you are free to go. With the court's apologies.
> **King Arthur:** As for you, adventurer... the court thanks you. Seers' Village sleeps safely tonight because of you.
> `{ACTIE: stage = 2, quest complete, rewards, quest complete-scherm}`

**Geen bewijs**
> **King Arthur:** You speak of evidence you don't have with you. The court cannot judge on words alone.
> **King Arthur:** Bring the evidence and try again.
> `{ACTIE: rechtszaak eindigt, stage blijft 1, flags blijven staan}`

**Verkeerde uitspraak**
> **King Arthur:** That isn't nearly enough to convict anyone. The court is adjourned.
> **King Arthur:** Investigate further, adventurer. And next time, bring me facts.
> `{ACTIE: rechtszaak eindigt, stage blijft 1, flags blijven staan}`

**[stage 2]**
> **King Arthur:** Order in the court! ...Ah, it's you. The court remains in your debt, adventurer.

### 5.2 Bartender

**[stage 1, `TALKED_BARTENDER` niet gezet]**
> **Bartender:** Don't order the ale. Seriously. Don't.
> **Player:** I'm investigating the poisoning for King Arthur.
> **Bartender:** Finally, someone with sense. Everyone says it was the Poison Salesman, but he never even comes in here. Says the ale's too weak.
> **Bartender:** This morning I found something odd by the barrel in the cellar. Strands of flax. Lots of them. Nobody brings flax into a tavern.
> **Bartender:** Here, take them. Maybe they'll mean something to you.
> `{ACTIE: geef 1 Flax, TALKED_BARTENDER = true, FOUND_FLAX = true}`

**[stage 1, `TALKED_BARTENDER` gezet, speler heeft geen Flax in inventory of bank]**
> **Bartender:** Lost it? There's more where that came from. The cellar's still full of the stuff. Here.
> `{ACTIE: geef 1 Flax}`

**[stage 1, `TALKED_BARTENDER` gezet]**
> **Bartender:** Found out who did it yet? I'd like to know who I'm banning for life.

**[stage 2]**
> **Bartender:** The Flax keeper is mopping my floors as we speak. Best employee I've ever had. Doesn't get paid, never complains.

### 5.3 Poison Salesman

**[stage 1, `TALKED_SALESMAN` niet gezet]**
> **Poison Salesman:** I didn't do it! I sell poison to adventurers, for their weapons! I don't go around pouring it into people's ale!
> **Player:** I believe you. Did anyone buy poison from you recently?
> **Poison Salesman:** Business has been terrible. I sold just one vial of weapon poison all week. Yesterday afternoon.
> **Player:** Who bought it?
> **Poison Salesman:** I didn't see his face, he kept his hood up. Said it was for rats. But I remember the smell. He smelled like a field. Like... flax.
> `{ACTIE: TALKED_SALESMAN = true}`

**[stage 1, `TALKED_SALESMAN` gezet]**
> **Poison Salesman:** One vial, yesterday afternoon, to a man who smelled of flax. That's all I know. Please, get me out of this.

**[stage 2]**
> **Poison Salesman:** You saved my business! And my neck. If you ever need poison... for your weapons, of course... come to me.

### 5.4 Flax keeper

**[stage 1, `TALKED_FLAXKEEPER` niet gezet]**
> **Flax keeper:** Hmm? The poisoning? Terrible business. Just terrible. I was home all evening. Alone. With my flax.
> **Player:** Did you go near the Forester's Arms last night?
> **Flax keeper:** The tavern? Never. I hate that place. Well, I don't hate it. I just... never go there. Why would I? Ha. Ha.
> **Flax keeper:** Anyway, I have a lot of flax to keep. Goodbye.
> `{ACTIE: TALKED_FLAXKEEPER = true}`

**[stage 1, `TALKED_FLAXKEEPER` gezet]**
> **Flax keeper:** I told you, I was home all evening. Please stop looking at my flax bales like that.

**[stage 2]**
> **Flax keeper:** Don't talk to me. I have to go mop a tavern.
> → daarna bestaande dialoog/ruil van de Flax keeper

### 5.5 Seer (optioneel)

**[stage 1]**
> **Seer:** I saw it coming, you know. In a vision.
> **Player:** You saw who poisoned the ale?
> **Seer:** I saw a man. Or possibly a large bird. Surrounded by... something yellow. Or brown. Long and stringy.
> **Seer:** I also saw myself drinking the ale anyway. Visions aren't always useful.
> `{ACTIE: TALKED_SEER = true}`

**[stage 2]**
> **Seer:** I knew you'd solve it. I saw it in a vision. After you solved it.

### 5.6 Bales of flax (object)

**[stage 1, `TALKED_SALESMAN` gezet, `FOUND_POISON` niet gezet] — Search**
> *(chatbox)* You search through the bales of flax...
> *(chatbox)* Hidden deep inside, you find a half-empty vial of weapon poison!
> `{ACTIE: geef 1 Weapon poison, FOUND_POISON = true}`

**[stage 1, `FOUND_POISON` gezet, speler heeft geen Weapon poison in inventory of bank] — Search**
> *(chatbox)* You search the bales again and find another vial of weapon poison. The Flax keeper must have bought more than one.
> `{ACTIE: geef 1 Weapon poison}`

**[andere gevallen] — Search**
> *(chatbox)* Just bales of flax. Lots and lots of flax.

---

## 6. Beloningen (bij stage 2)

- 2 Quest points
- 5.000 coins
- 1 Antique lamp (2.500 XP in een skill naar keuze, via het bestaande lamp-systeem)
- Quest complete-scherm met:
  - 2 Quest Points
  - 5,000 coins
  - 2,500 XP lamp
- Item op het questcompletescherm: Flax

Coins en XP zijn constanten zodat de ontwikkelaar ze makkelijk kan aanpassen.

---

## 7. Quest journal

| Stage | Tekst |
|---|---|
| 0 | I can start this quest by speaking to **King Arthur** in the **courthouse** in **Seers' Village**. |
| 1 | Someone poisoned the ale at the **Forester's Arms**. Everyone blames the **Poison Salesman**, but King Arthur has his doubts. |
| 1 | The Bartender found **flax** by the poisoned barrel. *(alleen tonen als `FOUND_FLAX`)* |
| 1 | The Poison Salesman sold one vial of weapon poison yesterday, to a man who **smelled of flax**. *(alleen tonen als `TALKED_SALESMAN`)* |
| 1 | The Flax keeper says he was home all evening. He seemed nervous. *(alleen tonen als `TALKED_FLAXKEEPER`)* |
| 1 | I found a half-empty vial of **weapon poison** in the **bales of flax**. *(alleen tonen als `FOUND_POISON`)* |
| 1 | When I'm ready, I can present my case to **King Arthur**. |
| 2 | **QUEST COMPLETE!** The Flax keeper confessed, and the Poison Salesman is free. |

---

## 8. Checklist

- [ ] Quest geregistreerd als "Order in the Court", 2 QP
- [ ] Dialogen voor King Arthur, Bartender, Poison Salesman, Flax keeper en Seer per stage en flag
- [ ] Bestaande functies van deze NPC's blijven werken
- [ ] Flags voor elk onderzoeksonderdeel
- [ ] Bales of flax geven Weapon poison pas na `TALKED_SALESMAN`
- [ ] Rechtszaak: drie keuzerondes, alleen Flax keeper + Flax + Weapon poison wint
- [ ] Verkeerde uitspraak en geen bewijs: rechtszaak eindigt, flags blijven staan
- [ ] Vervangende bewijsstukken bij verlies
- [ ] Rewards en quest complete-scherm
- [ ] Quest journal met regels per flag
- [ ] Getest: volledige doorloop van stage 0 tot 2
- [ ] Getest: verkeerde verdachte beschuldigen en opnieuw proberen
- [ ] Getest: rechtszaak zonder bewijs bij zich
- [ ] Getest: bales of flax doorzoeken vóór en na het gesprek met de Poison Salesman
