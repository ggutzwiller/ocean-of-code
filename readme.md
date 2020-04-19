# Codingame - Ocean of code - Submarine bot

## The game
#### Goal of the game
This is a Codingame bot competition. The aim is to write a bot to 
compete with other players' bots.  

This game for this competition is based on the board game Captain Sonar.
You pilot a submarine and you know that an enemy is present near you 
because you are listening to its radio frequency communication. 
You don't know exactly where it is but you can hear all its orders. 
You and your opponent have 6 hit points. When a player's hit 
points reach 0, the player loses.

#### Rules of the game
##### Definitions

* Submarines move on a map constituted of water and islands. 
They can only move on cells with water. They can share the 
same cell without colliding.
* The map is 15 cells in width and 15 cells in height. 
Coordinates start at (0,0) which is the top left cell of the map.
* The map is split in 9 sectors, which contain 25 cells each
 (5x5 blocks of cells). The top left sector is 1. The bottom
  right sector is 9.

##### Beginning of the game
At the beginning of the game, you'll receive a map (15x15 cells) that 
indicates the position of islands. Islands are obstacles. You cannot move 
or fire through islands. Then, you will decide where you want to 
place your submarine by indicating a coordinate (x,y).

##### Each turn
This is a turn based game which means that each player plays a turn one 
after the other. The player with the id 0 begins. During your turn, 
thankfully to your radio frequency analysis, you will receive an 
indication of what your opponent has done. For example, you can receive 
that it moved to the north. It's up to you to use this valuable 
information to detect where it is. Then, you must perform at least one action.

##### Actions

Each turn you must perform at least one action. You can do several 
actions by chaining them using the pipe |. But you can use each 
type of action only once per turn (you can move one time per turn, no more). 
If you fail to output a valid action, you will SURFACE in that turn.

###### Move

A move action moves your submarine 1 cell in a given direction 
(north, east, south, west) and charges a power of your choice. 
When you move, you must respect the following rules:

* You cannot move through islands
* You cannot move on a cell you already visited before
* You can decide, what to charge. Different devices require a different amount of charges to be ready.

###### Surface

By using surface you will reset your path of visited cells so that 
you can freely move to a cell that you have previously visited. 
But surfacing has a major impact: your opponent will know in which sector 
you are surfacing and you lose 1 hit point.

###### Torpedo

A torpedo requires 3 charge actions to be ready. When fully charged, 
the torpedo can be fired at an arbitrary water position within a range 
of 4 cells. This allows the torpedo's path to contain corners and 
go around islands, but not through them. The damage of the explosion 
is 2 on the cell itself and 1 on all neighbors (including diagonal ones). 
You can also damage yourself with a torpedo.

###### Sonar

Sonar requires 4 charge actions to load. It allows you to check, 
if the opponent's submarine is in a given sector. You will get 
the response in your next turn. This is in respect to the time 
of issuing the command, not after the opponent moved.

###### Silence

Silence requires 6 charge actions to load. This allows you to 
move 0 to 4 cells in a given direction (not visiting already 
visited cells or islands). Your opponent will not know in which 
direction or how far you've moved.

###### Mine

Mine requires 3 charge actions to load. It can be placed on any cell 
next to you (north, east, south, west). You can't place two own mines 
on the same cell. However it's possible to place your own mine on an 
opponent mine or the opponent's submarine itself. Mines will only 
detonate when using the trigger command, not when moving onto them.

###### Trigger

Triggering a mine will cause an explosion. You can only trigger your 
own mines. Like for a torpedo, the explosion has a damage of 2 on the 
location of the mine and 1 damage to nearby cells (including diagonally). 
You can't trigger multiple mines in the same turn. You can't place 
and trigger a mine in the same turn. You can also damage yourself with a mine.

#### Victory conditions

Have more lifes than your enemy at the end of the game. 
Each player has 300 turns including the initial placement.

## The bot

The bot is built in Java 8, it is quite simple and went to Silver league.