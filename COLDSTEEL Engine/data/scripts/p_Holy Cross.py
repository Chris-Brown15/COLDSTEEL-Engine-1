if not initialized:

	from Physics import ForceType
	from Core import HitBoxSets
	from Core import SpriteSets
	from Physics import MExpression
	from Renderer import ParticleEmitter
	from Game.Core import DamageType
	from Game.Core import EntityHurtData
	from Core import ECS

	exists = TRUE

	holyCrossDamage = DamageType.HOLY
	holyCrossDamage.setValue(25)

	def onCollide():
		global exists	
		exists = FALSE
		remove()

	def onHit(entity):
		if entity != None and entity.has(ECS.RPG_STATS):
			entity.components()[RPGOFF].hurt(EntityHurtData(holyCrossDamage , xMid() , yMid()))

	lib.kinematics().impulse(ForceType.LINEAR_DECAY , 2000 , 5.0 if horizDirection() == Direction.RIGHT else -5.0 , 0.0 , 0.07 , 0.0 , P)
	lib.kinematics().then(ForceType.LINEAR_DECAY , 2000 , -5.0 if horizDirection() == Direction.RIGHT else 5.0 , 0.0 , 0.07 , 0.0)
	lib.kinematics().onFinish(onCollide)
	lib.kinematics().killIf(lambda: exists == FALSE)

	initialized = TRUE

P.rotate(-(horizDisplacement() * 2))
projectileScan(150)