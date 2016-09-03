# Description

## Tournament mode:

The players have to collect coins, but they can't do it without the crown.

Players fight for the crown, the player who collects the most gold at the end of the timer wins the round. A player needs to win N rouns to win the tournament and be the king of the world: "the king and his vassals".

Note that the tournament winner (i.e., the king) is not necessary the player who collected the most money. When that happens, this person will be rewarded as "the goldsmith", "richer than the king". The person who will the most often the crown carrier will be "the king slayer".

The person who carried the longest the crown (without winning) will be the "fool".

The person with the most kills is the "assassin". The less rich is the "beggar", The player still have a collective wallet, more the collective wallet is full and more you get item distribution in the next round,

## Story mode:

_4 adolescents prabbits exécutent leur rite depassage à l'adulte._

_Shaman prabbit introduit les règles aux quatre adolescents, ce qui donne lieur à un tutorial._ _The shaman may incarnate the bad guy who serves as boss and summon monster on the map._

Here, you can pass to the next level only if the collective wallet reaches a certain amount of gold. This should favors cooperation. To prevent people to get bored, ennemies pop on the map if the Crown Carrier is not attacked during 5 seconds. The Crown carrier can't protect himself. Except with dodge, slaps and items. (that means no body try to kill him, as people want to cooperate a minimum). However, to keep some competition, the player who collect the most momey is the king of the level.

- the king of losers when level not passed,
- the king of winners, and his knight, for the guy who protected the crown the most,
- you also have the title, "king slayer" which provide you a bonus.

Règle de partie perdue (recommence leniveau) :

1. Si la couronne n'est portée parpersonne à la fin du timer
2. Si la couronne est détruite
3. Si tous les joueurs meurent enmême temps (temps de respawn overlappe)
4. Si les conditions de succès nesont pas réunis (seuil fixé)

--------------------------------------------------------------------------------

# TO-DO LIST:

## Bugs:

- [ ] check all the list of bugs

- [ ] **[A]** fireball can stop and/or slow down...? probably a problem with collision

- [ ] **[A]** bug: sometimes the player revive with automatic damage per second.

  > - In an attempt to debug, we replace flush() with the deepFlush() function in the isDead() function.
  > - We can't test if it is effective.
  > - I think that it is actually better to flush in the respawn function!!

- [ ] **[A]** controller don't work after fullscreen

- [ ] **[B]** when moving platform dissappear (are invisible) their position keeps growing, this might lead to an error when the PC reach the limit of float.

- [ ] **[TR]** Atanaska succeeded to replicate the bug of the flying prabbit: jump works as a jetpack. We thought we handle that problem. Not well enough apparently!

- [ ] **[TR]** Automatically die after respawn, after spike death,

- [ ] **[TR]** Apparently we can pick up moving platform ... which is not good,

- [ ] **[TR]** bug Lance: why bouncing on wall sometimes? (it is a bullet, no solution found),
- [ ] **[TR]** Lance can kill fireball,

  > should we keep and improve that possibility?

- [x] **[TR]** bug: the moving platform trajectory solver are still doing shit. The platforms dont always start at the same place.

## General Improvements:

- [ ] **[A]** deactivate lances when the platform supporting them disappears.

- [ ] **[A]** the lance should deactivate the launcher

- [ ] **[A]** Animations:

  - [x] Lances could have another appearance when fully loaded

  - [x] Fireball explosion

  - [x] Picking up / Projecting character:

    1. [x] the lifted prabbit should be panicked
    2. [x] the lifting prabbit should have his arm up?
    3. [x] a GUI appears to tell the players to press A as many time as they can

      > we can use the progress bar in the GUI folder for the accoutn of how many time the button A/B have been pressed.

    4. [x] Projection animation

  - [x] Golden Prabbit animations:

    > - if a character passes the threshold at which he will win even without the crown, he get a golden glare.
    > - note that the actual aura was made on the blue prabbit and it is quite pretty on the normal prabbits. The aura, when tried on the golden prabbit was quite creepy. So maybe we could use both aura:

    >   - When winning: simple aura, keeping the color of the prabbit
    >   - When winning and special conditions: Golden Prabbits.

    - [ ] the yellow skin should be only used for the Golden Prabbit
    - [ ] add a layer in character class to add effects (here it will be a golden aura)
    - [ ] animate the golden aura

  - [ ] Animate the flowers?

  - [ ] Animate the coins going to your wallet

  - [ ] Animate the crown:

    - [ ] Write the amount of money going to the crown on the crown
    - [ ] Make the crown bouncing/scaling each time it gets money

- [ ] **[A]** Sounds:

  - [ ] fireball death sound and animation
  - [ ] sound for switcher
  - [ ] sound for end of jumping
  - [ ] **[wip]** Switcher
  - [ ] Fireball launcher
  - [ ] Spikes?

- [ ] **[B]** level menu

- [ ] **[B]** option menu

- [ ] **[B]** the physical size of platform should be independent from the texture size (the character does not touch the wood platforms)

- [ ] **[C]** make background for the level,

- [ ] **[C]** keyboard control: pick-up and slap with right-click, respectively with and without the SHIFT pressed

- [ ] **[C]** try to do one way (single ticket) platforms,

- [ ] **[B]** could we add blood?

- [ ] **[C]** Small music:

  - [ ] Winning small music,
  - [ ] No winner small music,

- [ ] **[C]** try make it work on HTML5

## Gameplay Ideas

- **[A]** player with crown can not attack (except slaps)
- **[A]** crown carrier can't attack,
- **[A]** we could have a switch to respawn the gold coin, instead of an automatic respawn.
- **[A]** double jump?
- **[A]** slow down character who carry something,

  > should we slow down the crown carrier?

- **[A]** dodging system?

  > every 3 seconds

- **[A]** lost of money when die/ revive? 15% of money to revive,

- **[A]** collect a ruby give you 1 second of invicibility?

- **[B]** boxes/blocks system: character could move blocks on the map to activate switch (basic puzzle system)
- **[B]** Usable/throwable object system (use the LanceManager, make it an ItemManager)
- **[B]** Modifier Object system (transient/constant?)
- **[B]** lance locking system ?

  > useful for puzzles where we need to keep our lance on a switch

- **[C]** lance are locked when touched the other player,

  > why? what does that add to the gameplay?

- **[C]** do bridges with the Lances??

- **[C]** marking exchange system idea: when slap you get rid of one mark and give one to the opponent,

  > does not seem a good idea, but we do need something to get rid of the fatigue marks.

  > maybe an object could give temporarily the above effect to a player.

# Further Ideas

## Moves Ideas:

- Combination down+B to take/drop an object (make things more difficult to project someone)
- Combination down+B to pick downward if lance is the current object?
- we could lock our lances (not come back to the magic pool)
- can lock the lastly used Lance, (can lock more than one Lance)
- can hang to a wall with a Lance,
- can pick the Lance on the ground and stand on it (in balance)
- sprint button
- dash/dodge

## Level design idea:

- **Dark Room** -- you can switch off the main light of the level (the switch can be not under the players control). When in the dark the player can switch on the individual light of their character, or switch it off to become invisible, even for themselve :)

- ropes could hold platform, to cut the rope would make the platform fall,

- explosion could break specific blocks.

- **Scrollingvertical** -- lave qui pousse au cul, pour debloquerl'étage supérieur chaque joueur doit activer une clé correspondant à sa couleur avec sa lance.Présence de pièges, à chaque étage franchi, plus on franchi d'étages plus lamanière de les éviter devient restrictive, à la fin, une seul manière de leséviter à la fin.

  Au dernier étage, le pattern de move de chaque joueur est précis et ordonné en fonction des autres joueurs (comme une danse)

  > **Idée étage intermédiaire :** l'ordre d'activation des clés est ordonné.

- **Tableaufixe** -- Roue centrale à 4 platformes, chaque joueur commence sur une platforme differente. La roue se met à tourner de plusen plus vite. 4 clés à activer pour desactiver le piège. Le piege fonctionne comme suit : si une des clés est désactivé pendant plus de 3 sec, la roue augmente sa vitesse d'une unité. Si deux clés sont desactivés plus de 3sec, la roue augmente sa vitesse de deux unités. Si trois clés désactivés

## Enemies ideas:

- **crickets:** walk and jumps on you

- **flies:** fly on you

- **frogs:** jump off the screen, then you see its shadows coming and it falls where the shadows appeared, with range damage.

## Bonus/Object Idea:

- **Divine Resurection** -- after you died a column appear on the screen to control where you want to respawn, you will be send off the sky to the first platform above, making a huge and destrutive impact while ressurecting.

- lance with rope

- autopilot lance (remote and blocked)

- explosive lance

- mine lance

- fire/ice lance

- bonus which decrease your respawn time or payment to revive,

- increase/decrease death time, but how?

- life

- speed

- **Baseball bat** -- beat people to knock them out.

- **Baseball ball** -- bounce on all the screen if beaten with baseball bat

- **Invincible Star** -- anyone you touch die, you are immortal until you get the crown.

- double jump ?

- spring lance?

- **Grapple** -- swap the wallet (score), the current position, and the crown owning from the targeted character.

  - **Mind Swapper Lance** -- you exchange the control of your character for a while. The curse stops when one of the character dies.

- **Boomerang** -- throw it and gets the money which passes through. Get items or steal the crown otherwise.

- **Chicken** -- unleach a chicken which will collect money for you. The chicken will try to collect all coins connected together. If the chicken or the player get beaten during the calling time (summon time), the chicken stop.

  > If you don't have the crown, the chicken just eat the coins (make them not available for the current Crown owner)

- **Clock time** -- when pick up, pause the game, display a clock, you can either increase the remaining time or decrease it, or do nothing.

- **Banquier** -- an item which transfers the Crown wallet to your wallet permanently

- **Money eater bomb** -- absorb all the money in the action radius, then you need to collect the capsule to get the money.

- **Permanent money collector** -- collect money in a certain radius, make it unavailable for the others.

- platform/lava/water creation from an object throw in between two lances?

--------------------------------------------------------------------------------

# Changelog

## Up to 07/08/2016

- [x] Menu - restart/resume/quit/preference,

- [x] **[A]** some switcher can activate the onset of a group of coins

  ```
  > I think that it was already implemented
  ```

- [x] Controller/Keyboard distribution,

- [x] **[A]** to have been knock out/throwed would increase the death time?

- [x] preference for Keyboard/Controllers distribution.

- [x] no lance collision when lance on the players,

- [x] Lance angles -- no collision/one way collision

  ```
  > longer than what I thought to resolve.
  > Finally create a new collision shape for the player.
  ```

- [x] bug: when jumping through an almost vertical lance, the character can get stuck in the FALLING state

  ```
  > Solution: add a bigger array for the Feet ContactData.
  ```

- [x] First trap, fire launcher, add trap fireball (extend the throwable system to a non player object)

- [x] switch improvements? switcher (Trigerrable) specific to character / switcher specific to weapons

- [x] Color the characters

- [x] GUI

- [x] bug: now the projectiles have a good strat point and a good rotation,

- [x] bug: moving platform, started at a random distance when triggered bu a switch (the timer was counting when not activated)

- [x] bug: the one-way collicsion of Lances was not working anymore.

  ```
  >the problem came from the mainBodyBox addition in GameObject.
  >All object has a ContactSensor now. The ConstactListener was considering that only the lance would have a ContactSensor.
  ```

- [x] check the code line 138-150 in Lance Manager, I got the impression, it is wrong. (powerLoad == defaultLoad) ??

- [x] bug: slapping when close to the oponent was not working anymore

  ```
  > the problem came from the mainBodyBox addition in GameObject which was not well used (ignored) in children objects, especially in the LanceManager.
  ```

- [x] system of crown score / player score (money go to the crown account or to the player account)

- [x] add moving platforms

  ```
   > Note that for now, it is the character adds by itself to itself the speed of the last grounded object.
   > Indeed, no suitable solution has been found with using the friction and the physics engine. Indeed the player class is using the physics in a tricky way to make the character fully controllable.
  ```

- [x] bug: threshold/switcher (trap were initially activated while the threshold was not reached)

- [x] Separate Player from Character.

- [x] bug: Aiming and moving does not works after this change

- [x] add compatibility with keyboard / allow custom button mapping

- [x] the friction switch has been added back, with some modification. Low Friction of 0.01 when moving prevent to stick of hard corner. Also prevent to stick on top edge when jumping? even thought we also cut down the top of the hit box of the character to prevent it.

- [x] [not applicable] in MapBodyBuilder, we could try to play with friction and restitution to hav block which glides and block which stick, and block which bounces. (not possible)

- [x] to remove the friction switch (0.2 when grounded, 0.0 when in air), remove the casual bugs of staying blocked on a downer corner of a brick when jumping.

  ```
  However, this switch is needed in order to not slide on moving platform :/
  ```

- [x] the bug of the omnipresent slap occur after carrying the victim player.

  > RESOLVED: a loop while in the function "public boolean removeTouchedFixtures(Fixture f)" in ContactData has been added, to make sure we remove all the instances of the fixture in the list.

- [x] the bug of free flying is not yet resolved...

  ```
  > was happening, for instance, when a Lance disappeared from the Player's feet. It stayed in the feet sensor.

  > RESOLVED: coming from the ContactData gestion, we add a loop which find any fixture touched by the removed Lance and tell it to remove the Lance from its ContactData list.
  > However, the Lance itself didn't have enough space in its ContactData list to keep track to every body, we increase its size (from 1 to 8).
  > Finally, the last issue was that the ContactListener was not adding the Contact to ContactData if the fixture was not a Sensor... So the Lance even didn't know it was touching the feet.

  > note also we modified the function pushTouchedFixtures(Fixture f) so that it add a fixture only if not already present in the list of ContactData.
  > That change would probably resolved the bug of the slap also.
  ```

- [x] add LT for launching a Lance and aim

- [x] use texture like "--->" instead of the actual Lance when aiming

- [x] bug Lance: stop display when was aiming and hit an object box:

  ```
  > changed to "short attack not aiming and is colliding something"
  > if you collide something, you can still aim if you were previously aiming.
  ```

- [x] bug Lance: the lance has to hit and hang on the Player body, not on the sensors,

- [x] bug Lance: remove them when character die (on him, and his own?)

- [x] respawn the coins

- [x] the crown can't die (and why there is a crash when it did?)

- [x] crown stealing bugs

- [x] remove bug: when throw a lance, the lance spawn always at an angle of 0 degree.

- [x] add trap pick

- [x] add timer and screen end

- [x] decrease force of projection

- [x] decrease time of KO when projected

- [x] selection of list of Levels in a folder,

- [x] new Level,

- [x] bug: the Pause window was not showing anymore,

- [x] bug: disable the Pause button in ScoreTable, it was doing weird stuff,

- [x] Menu: Score Table: Next Round / Start a new Tournament button,

- [x] Menu: Score Table: Back to Menu (Stop Tournament) button,

- [x] better rotation initialization from Tiled,

- [x] simplify rotations: all in radians and transform in degrees for rendering,

- [x] bug: when the reward Window comes the game didn't pause,

  ```
  - [x] bug: reward Window, the top columns is too high!

        > (was actually caused by the fact the game didn't pause)
  ```

- [x] moving platform can stop at different check points define by vertex in Tiledmap,

- [x] make switches triggerable, such that a switch can enable a new switch,

  ```
  **[?]** this, for now causes a bug because parented switches are not desactivated with their parent,
  ```

- [x] balance the crown rate to 0.333

- [x] spiking moving platform

  ```
  >  add also parent system, switch will also be able to follow a platform (attachable interface),

  - [x] create the Parenting System (managed from Tiled),
  ```

- [x] bugs: parent-children rotations,

  ```
  > the bug comes from the desactivation of the spikes. We may want to try to change the fixture filter t0 0x000 instead of deactivating the body...
  ```

- [x] replace flush with deepFlush when it seemed needed, I flagged the change, normally...

- [x] debug text system,

- [x] jump system, add this limitation: can't automatically re-jump if RISING

  ```
  > we tried to do it, with saving previous state, we get a bug when on moving ascending platform...

  > the bug comes from a y_vel > 0.1 detected before the ground contact has been detected.

  > the solution to avoid complex solution was to forbid FALLING --> RISING (which will allow forbid double-jump).
  ```

- [x] marking system, to slap give a mark to the slapped character, a top of 5 marks, 1 mark add 3 seconds to next death (an usual death is 5 seconds),

- [x] separate short and long attack, for more control,

- [x] add texture for platform, |--o--|

- [x] slapping and attack has been improve!

  ```
    > - slapping box is wider/inside player (of course, it ignores the slapper player)
    > - can't attack during animation (consequently, that decreases the shooting rate)
    > - increase impulse distance when slapped.
    >   - 3 lances to kill,
    >   - 12 slaps to kill,
  ```

- [x] Tournament Mode,

- [x] Diamond and Ruby addition,

- [x] different coins distribution which can be activated by a triggerable,

- [x] aiming arrow change: always displayed when aiming + charge more visible,

- [x] switcher assets have been integrated,

- [x] display background on the background,

- [x] bug: switcher lance, make switchers for lances working

- [x] debug: lance angle when connect/weldJoint on a rotated/rotating object was wrong,

- [x] debug: fireball are doing weird thing when colliding an other fireball, (it seems that body.setActive(False) call endContact() of the contact listener, and then remove the fixture of the contactdata of the other fireball)

- [x] improve Fixture ContactData --> reference to their fixture and deepFlush (clean Flush)

- [x] bug: moving platform are pickable by the player!!

- [x] bug: revive in the arm of a player if picked up...

- [x] bug: respawn is cleaner now / less buggy

  ```
  > sometimes the slap was giving a strong impulse.
  ```

- [x] bug: we were able to attack when carrying someone

- [x] bug: can't import group of sounds with the cookbook improved AssetManager:

  > Note: that the crown doesnt use the mainBoxContact, I dont know why

> Note: I remove the flush after throwing (useCarriedBody and setCarriedBody(null)), this is better to allow throwing someone just after having throwed someone. However, it happened twice that a character reappear in my hands while taking the crown. Didn't succed to replicate.
