# Beyond - 3D Java Multiplayer Game
This project was for my Computer Game Architecture class where we learned the structure of game engines and how to modify them to fit our needs. Instead of using professional game engines like Unity, our professor had created a very minimal game engine in java which forced us to get our hands dirty and build upon it to add features we wanted. I took this course alongside an Advanced Computer Graphics course so I was also able to incorporate what I have learned in that class to enhance the visuals by modifying the shader pipeline. This project taught me about different camera controllers, scenegraphs, and how to build game worlds with textures and skyboxes among other things. In my game I largely focused on the physics aspect as I found it very interesting but also enjoyed building the NPC's and networking allowing for multiplayer.

## What I learned
- Overview of Game engines and Matrix transformations for a 3D Game Space
- Input handling from keyboards and different types of controllers
- 3D model files and how objects are projected and manipulated 
- Lighting concepts light the ADS model and Blinn-Phong reflections
- The concept of a synthetic camera and how to manipulate it.
- Different game camera implementation like First/Third person or chase cameras.
- Utilizing viewports
- Hierarchical scenegraphs and node controllers for interesting object behavior
- How Networked multiplayer works in games with ghost avatars and synchronization
- Game World aspects like textures, height/normal maps, skyboxes, fog, etc
- 3D modeling with rigging and animation (Programmer art level)
- How physics engines work beyond surface level knowledge. 
- NPC behavior with State machines and behavior trees (Not implemented in project due to time constraints)
- Implementing 3D spacial sounds.
- So much more but I’d be here all day 

## What Im Proud of 
- Physics and player movement:
	I spent a lot of time building and tweaking the players movement to get it to where it is now. I was constantly thinking about the force and momentum vectors and how they react with each other to create a smooth zero gravity feel while also not being too realistic to where it would be impossible to play. 

- Player HUD:
The tiny game engine we were using did not have a very extensive HUD implementation so I decided to make the entire HUD in the game,s 3D space. Necessary player info was displayed with scaling 3d objects that were part of the HUD and as an added bonus this allowed for me to add a FOV tweak for when the player accelerates. 

- Multiplayer:
This was my first real dive into a networked multiplayer game so even just seeing the other player move and ROTATE was super satisfying. Throughout the project I was able to add so many more things like damaging other players, displaying ghost avatar projectiles and playing accurate spacial sound based off of them as well.  

- Game Description:
	The game was made to be a team vs team spaceship combat game where the two teams compete to collect and bring resources back to their teams mothership. To collect resources you can either mine them from the surrounding asteroids or destroy enemy ships. Depositing too many resources at once causes the enemy team to spawn drones that will chase after you and attack when they get close. First team to deposit 10,000 points wins. 

# Controls
Controller:
Left Stick: Move Ship (Forward and Backward, Left and Right)
Click in Left Stick: Speed Boost;
Right Stick: Look around
Click in Right Stick: Descend
Bumpers: Roll Left and Right Respectively
A: Ascend
Right Trigger: Laser Beam
Left Trigger: Space Break
B: Launch Missle
X; Respawn
D-Pad Down: Emote
	KeyBoard(Doesn’t work on some machines for some reason):
		WASD: Move Ship
		Shift: Speed Boost
		Mouse: Look around
		Left Click: Shoot Laser
		Right Click: Shoot Missle
		Space: Ascend
		CTRL: Descend
		F: Emote
		X: Respawn:
		Q or E: Roll Left or Right Respectively

# How to run
  Coming Soon! (Want to make it as easy as possible)
