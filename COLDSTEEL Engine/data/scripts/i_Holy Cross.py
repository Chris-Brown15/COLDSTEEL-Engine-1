if not initialized:

	from Game.Projectiles import Projectiles
	from Core import SpriteSets
	from Core import HitBoxSets
	holyCrossPrototype = createProjectile(I.getTexture() , SpriteSets("p_Holy Cross.CStf") , "p_Holy Cross.py")
	holyCrossPrototype.toggleDirection().toggleCollisions()
	holyCrossPrototype.hitbox(HitBoxSets("Box 10 x 10.CStf"))
	holyCrossPrototype.removeColor(0.32941177 , 0.42745098 , 0.5568628)
	initialized = TRUE
	requirement = 2

	def onUse():
		pass

	def stopUse():
		pass

'''
move the prototype projectile to the user's joint and then launch it
'''

activeSet = usersAnimation()
if activeSet.getCurrentSprite() == 3:
	holyCrossPrototype.moveTo(getOwnersJointPosition(0))
	holyCrossPrototype.horizDirection(E_components[DOFF])	
	launchProjectile(holyCrossPrototype.copy())
