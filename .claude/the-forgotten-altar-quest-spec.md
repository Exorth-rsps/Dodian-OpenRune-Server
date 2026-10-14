# Quest specificatie: The Forgotten Altar

> **Instructie voor Claude Code:** implementeer deze quest in de bestaande codebase van deze OSRS private server.
> 1. Deze quest is een **vervanging van Rune Mysteries**: zelfde doel (Runecrafting vrijspelen), zelfde items, nieuw verhaal en nieuwe NPC's.
> 2. Zoek eerst of Rune Mysteries al in de codebase zit.
>    - **Zo ja:** hergebruik de varp/varbit van Rune Mysteries voor de stages, en laat alle bestaande checks op "Rune Mysteries voltooid" (altars, essence mine, talisman-gebruik, enz.) voortaan naar deze quest wijzen. Schakel de oude Rune Mysteries-dialogen uit (Duke Horacio, Sedridor, Aubury) zodat de spelers maar één route hebben. Verwijder geen andere functionaliteit van die NPC's.
>    - **Zo nee:** maak een nieuwe varp/varbit aan volgens het bestaande quest-patroon.
> 3. Volg de bestaande patronen voor questregistratie, quest journal, dialogen, NPC-handlers en rewards.
> 4. Alle ID's staan als `TODO`. Zoek ze op in de bestaande definities. Bestaat een NPC of item nog niet, vraag het dan aan de ontwikkelaar. NPC-posities en spawns regelt de ontwikkelaar.
> 5. Neem de dialogen **letterlijk** over en werk de checklist onderaan af.

---

## 1. Overzicht

| Veld | Waarde |
|---|---|
| Naam | **The Forgotten Altar** |
| Doel | Speler ontdekt het geheim van de talismans en speelt Runecrafting vrij |
| Startpunt | Wizard Sinina, bovenste verdieping Wizards' Guild (magic guild), Yanille |
| Quest points | 1 |
| Vereisten | Geen |
| Beloning | 1 QP, Air talisman, toegang tot Runecrafting |

### Het verhaal in het kort
Bij het opruimen van de oude kluis van de Wizards' Guild vindt Wizard Sinina een vreemde talisman die zachtjes zoemt. Ze stuurt de speler naar Wizard Persten, een kenner van oude magie, die in de toren bij de Crafting Guild woont. Persten vermoedt dat de talisman een sleutel is tot een vergeten altaar, maar heeft bewijs nodig. Hij stuurt zijn onderzoek naar Wizard Traiborn, een verstrooide tovenaar die al jaren naast de air ruins woont en de energie daar bestudeert. Traiborn vult het onderzoek aan met zijn eigen (rommelige) aantekeningen. Samen ontrafelen ze het geheim: met een talisman kun je de verborgen altaars betreden en van essence runes maken. De oude kunst van Runecrafting is herontdekt.

---

## 2. NPC's

| NPC | Locatie | NPC ID |
|---|---|---|
| Wizard Sinina | Wizards' Guild, Yanille (bovenste verdieping) | `TODO` |
| Wizard Persten | Toren bij de Crafting Guild (bovenste verdieping) | `TODO` |
| Wizard Traiborn | Huis bij de air ruins | `TODO` |

Als deze NPC's al andere dialogen of functies hebben, voeg de questdialoog dan toe als optie of laat die voorgaan zolang de quest loopt.

---

## 3. Items

Hergebruik de items van Rune Mysteries.

| Item | Item ID | Rol |
|---|---|---|
| Air talisman | `TODO` (zelfde als Rune Mysteries) | Gekregen van Sinina, beloning aan het einde |
| Research package | `TODO` (zelfde als Rune Mysteries) | Van Persten naar Traiborn |
| Notes | `TODO` (zelfde als Rune Mysteries) | Van Traiborn naar Persten |

Research package en Notes zijn questitems: niet verhandelbaar, en ze verdwijnen als de quest klaar is (volg het bestaande patroon).

---

## 4. Stages

| Stage | Betekenis | Wordt gezet door |
|---|---|---|
| 0 | Niet gestart | – |
| 1 | Air talisman ontvangen, moet naar Persten | Sinina |
| 2 | Talisman bij Persten, Research package ontvangen | Persten |
| 3 | Package bij Traiborn, Notes ontvangen | Traiborn |
| 4 | Notes bij Persten, talisman terug, moet naar Sinina | Persten |
| 5 | Quest voltooid | Sinina |

Als je de Rune Mysteries-varp hergebruikt en die andere waarden voor "voltooid" gebruikt, map deze stages dan op die waarden, zodat bestaande checks blijven werken. Documenteer de mapping in de code.

---

## 5. Dialogen

Notatie: `[Conditie]`, opties onder **Opties**, `{ACTIE}` voor code-acties.

### 5.1 Wizard Sinina (start en einde)

**[stage 0]**
> **Wizard Sinina:** Oh! Careful where you step, adventurer. I've been clearing out the old guild vault and there's dust everywhere.
> **Player:** Find anything interesting?
> **Wizard Sinina:** Mostly broken wands and mouldy spellbooks. But this... this is different. Here, listen.
> **Wizard Sinina:** Hold it close. Can you hear it? It hums. Softly, like the wind through a keyhole.
> **Player:** It's... vibrating. What is it?
> **Wizard Sinina:** That's just it, I don't know. None of the books in the vault mention it. But I know someone who might.
> **Wizard Sinina:** Wizard Persten. She spends her days studying magic older than this guild itself. Would you take it to her?
>
> **Opties:**
> - `Of course, I'll take it to her.`
> - `Not right now.`
>
> *Of course, I'll take it to her.*
> **Player:** Of course, I'll take it to her. Where can I find her?
> **Wizard Sinina:** She lives in the tower near the Crafting Guild. You'll find her at the very top. She likes to be close to the sky.
> **Wizard Sinina:** Be careful with it. Something tells me it's more important than it looks.
> `{ACTIE: start quest, geef Air talisman, stage = 1}`
> *(Bij volle inventory: "You need a free inventory space." en de quest start niet.)*
>
> *Not right now.*
> **Player:** Not right now, sorry.
> **Wizard Sinina:** Very well. The talisman isn't going anywhere. It's been in that vault for centuries, after all.

**[stage 1, speler heeft Air talisman]**
> **Wizard Sinina:** Have you brought the talisman to Wizard Persten yet? Her tower is near the Crafting Guild, at the very top.

**[stage 1, speler heeft geen Air talisman in inventory of bank]**
> **Wizard Sinina:** You lost it? Oh dear... Luckily the vault had more than one of those strange stones. Here, take this one, and please be more careful.
> `{ACTIE: geef Air talisman}`

**[stage 2 t/m 3]**
> **Wizard Sinina:** Persten and Traiborn working together? I'd pay good money to see that. Let me know what they find.

**[stage 4]**
> **Player:** Sinina! We found out what the talisman is.
> **Wizard Sinina:** Tell me everything!
> **Player:** It's a key. Ancient altars are hidden inside ruins all over the land. With a talisman, you can enter them.
> **Player:** And at those altars you can turn rune essence into runes.
> **Wizard Sinina:** Make our own runes? That's... that's a craft we lost so long ago, nobody even remembered it existed.
> **Wizard Sinina:** And it was lying in our own vault, collecting dust. I feel rather foolish.
> **Wizard Sinina:** You've brought something back that the whole world forgot. The craft is yours now: Runecrafting.
> **Wizard Sinina:** Keep that talisman. It will lead you to the air altar near Traiborn's house. Find more talismans and you'll find more altars.
> `{ACTIE: stage = 5, quest complete, rewards, quest complete-scherm}`

**[stage 5]**
> **Wizard Sinina:** Hello again, runecrafter! I've started cataloguing the rest of the vault. Who knows what else is hiding in there.

### 5.2 Wizard Persten

**[stage 0]**
> **Wizard Persten:** Shh! Can't you see I'm concentrating? Come back when you have something worth my time.

**[stage 1, speler heeft Air talisman]**
> **Player:** Wizard Persten? Wizard Sinina sent me. She found this in the vault of the Wizards' Guild.
> **Wizard Persten:** Sinina? What has she dug up this time... Wait. Let me see that.
> **Wizard Persten:** By Saradomin's beard... it's humming. Do you have any idea what this is?
> **Player:** I was hoping you would.
> **Wizard Persten:** I've read about objects like this. Fragments of very old texts. They speak of altars that can't be seen, only entered.
> **Wizard Persten:** If I'm right, this talisman is a key. But I need proof. Theories are nothing without proof.
> **Wizard Persten:** I'll keep the talisman here to study. Meanwhile, take this package with my research to Wizard Traiborn.
> **Player:** Who is he?
> **Wizard Persten:** An old colleague. Scatterbrained, terribly forgetful, but he's lived next to the air ruins for years and has measured every gust of magic that comes out of them.
> **Wizard Persten:** If anyone can confirm my theory, it's him. You'll find his house right next to the ruins.
> `{ACTIE: verwijder Air talisman, geef Research package, stage = 2}`

**[stage 1, speler heeft geen Air talisman]**
> **Wizard Persten:** Sinina sent you? Then where is this mysterious object of hers? Go and get it back.

**[stage 2, speler heeft Research package]**
> **Wizard Persten:** Have you delivered my research to Traiborn yet? His house is next to the air ruins.
> **Wizard Persten:** And make sure he doesn't put it somewhere and forget about it.

**[stage 2, speler heeft geen Research package in inventory of bank]**
> **Wizard Persten:** You lost my research? Years of work! ...Fortunately I always keep a copy. Here. Don't lose this one.
> `{ACTIE: geef Research package}`

**[stage 3, speler heeft geen Notes]**
> **Wizard Persten:** Did Traiborn give you anything? I need his notes to confirm my theory.

**[stage 3, speler heeft Notes]**
> **Player:** Traiborn gave me his notes.
> **Wizard Persten:** Finally! Let me see... Oh, his handwriting is dreadful. And is that a soup stain?
> **Wizard Persten:** But this... yes... yes! Look here. He measured the energy around the ruins every day for years. And whenever someone stood near the ruins with a talisman, the energy spiked.
> **Wizard Persten:** I was right. The talisman is a key. The ruins are doorways to hidden altars, each bound to its own element.
> **Wizard Persten:** And at those altars, rune essence can be bound into runes. Air, water, earth, fire... perhaps even more.
> **Player:** So anyone with a talisman and some essence can make runes?
> **Wizard Persten:** Anyone who knows how. And now you know how. Here's your talisman back. You've earned it.
> **Wizard Persten:** Go and tell Sinina. She'll want to hear that her dusty vault hid the greatest discovery of the age.
> `{ACTIE: verwijder Notes, geef Air talisman, stage = 4}`
> *(Bij volle inventory: talisman toch geven volgens het bestaande "inventory vol"-patroon, of de speler vragen ruimte te maken.)*

**[stage 4]**
> **Wizard Persten:** What are you still doing here? Go and tell Sinina what we found!

**[stage 5]**
> **Wizard Persten:** Ah, the runecrafter. I'm writing a book about our discovery. Don't worry, you'll be in the acknowledgements.

### 5.3 Wizard Traiborn

**[stage 0 t/m 1]**
> **Wizard Traiborn:** Hmm? Oh, hello. Did I invite you? I don't remember inviting anyone. I don't remember much, to be honest.

**[stage 2, speler heeft Research package]**
> **Player:** Wizard Traiborn? Wizard Persten sent me with this package.
> **Wizard Traiborn:** Persten? Persten... ah yes, the one with the tower! Very serious woman. Never laughs at my jokes.
> **Wizard Traiborn:** A package for me? How lovely. Is it my birthday?
> **Player:** It's her research. About a talisman that hums. She thinks it's the key to an altar in the ruins.
> **Wizard Traiborn:** The ruins? Oh, those ruins! I've been studying those for... well, a long time. Years, I think. Possibly decades.
> **Wizard Traiborn:** Let me read... mmh... mhm... OH! But that explains everything!
> **Wizard Traiborn:** Every now and then the energy from the ruins jumps right off my scales. I always thought it was the weather. Or my cat.
> **Wizard Traiborn:** I've written it all down, somewhere. Now where did I put those notes...
> **Wizard Traiborn:** Not under the bed... not in the teapot... aha! In my hat. Of course. Where else would I keep them?
> **Wizard Traiborn:** Here, take them to Persten. And tell her the soup stain was there when I found it.
> `{ACTIE: verwijder Research package, geef Notes, stage = 3}`

**[stage 2, speler heeft geen Research package]**
> **Wizard Traiborn:** Persten sent you? With a package? I don't see any package. Unless I've forgotten it already. That happens.

**[stage 3, speler heeft Notes]**
> **Wizard Traiborn:** Shouldn't you be bringing those notes to Persten? Or was I supposed to do that? No, I'm fairly sure it was you.

**[stage 3, speler heeft geen Notes in inventory of bank]**
> **Wizard Traiborn:** Lost my notes? Oh, don't worry. I wrote everything down twice. Or was it three times? Here, take this copy.
> `{ACTIE: geef Notes}`

**[stage 4 t/m 5]**
> **Wizard Traiborn:** So the ruins are a door, and the talisman is the key! And to think I've lived next to it all these years and only ever used it to dry my socks.

---

## 6. Runecrafting-unlock

- Runecrafting-altars (via ruins of talisman/tiara) en de rune essence mine zijn pas toegankelijk na deze quest (stage 5).
- Als er al checks zijn op Rune Mysteries: laat die naar deze quest verwijzen in plaats van dubbele checks toe te voegen.
- Zonder voltooide quest, bij het gebruik van een talisman op ruins:
> *You feel a strange power, but you don't understand how to use it.*
- Als de server een NPC heeft die naar de rune essence mine teleporteert: blokkeer die teleport tot de quest voltooid is. Optioneel (overleg met de ontwikkelaar): voeg de teleportoptie toe aan Wizard Sinina na de quest.

---

## 7. Beloningen (bij stage 5)

- 1 Quest point
- Air talisman (de speler heeft hem al, die wordt niet opnieuw gegeven)
- Toegang tot Runecrafting
- Quest complete-scherm met:
  - 1 Quest Point
  - Air talisman
  - Access to Runecrafting
- Item op het questcompletescherm: Air talisman

---

## 8. Quest journal

| Stage | Tekst |
|---|---|
| 0 | I can start this quest by speaking to **Wizard Sinina** at the top of the **Wizards' Guild** in **Yanille**. |
| 1 | Sinina found a strange humming **talisman** in the guild vault. I should take it to **Wizard Persten** at the top of the tower near the **Crafting Guild**. |
| 2 | Persten thinks the talisman is a key. She gave me her **research package** to deliver to **Wizard Traiborn**, who lives next to the **air ruins**. |
| 3 | Traiborn gave me his **notes** about the ruins. I should bring them to **Wizard Persten**. |
| 4 | The talisman is a key to hidden altars where runes can be crafted! I should tell **Wizard Sinina**. |
| 5 | **QUEST COMPLETE!** I have rediscovered the art of **Runecrafting**. |

---

## 9. Checklist

- [ ] Gecontroleerd of Rune Mysteries al bestaat; varp hergebruikt of nieuwe aangemaakt
- [ ] Oude Rune Mysteries-route uitgeschakeld (indien aanwezig), zonder andere functies van die NPC's te breken
- [ ] Quest geregistreerd als "The Forgotten Altar", 1 QP
- [ ] Dialogen voor Sinina, Persten en Traiborn per stage
- [ ] Item-ruil per stap (talisman → package → notes → talisman)
- [ ] Vervangende items bij verlies (talisman stage 1, package stage 2, notes stage 3)
- [ ] Runecrafting-checks wijzen naar deze quest
- [ ] Rewards en quest complete-scherm
- [ ] Quest journal per stage
- [ ] Getest: volledige doorloop van stage 0 tot 5
- [ ] Getest: items verliezen en opnieuw krijgen
- [ ] Getest: talisman op ruins vóór en na de quest
