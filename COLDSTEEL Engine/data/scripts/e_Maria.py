if not initialized:
	
	from Physics import MExpression
	from Renderer import ParticleEmitter
	from Core import UIScript

	GROUNDED = 0
	AIRBORN = 1
	DUCKING = 2

	actionState = 0
	positionState = 0

	hurtBloodExpr = MExpression("srand ( 125 / x )" , TRUE)
	hurtBloodEmitter = createParticleEmitter(160 , 2400 , hurtBloodExpr , hurtBloodExpr , 0.90 , 0.0 , 0.0 , 1.5 , 1.5 , TRUE) 
	hurtBloodEmitter.setEmissionRate(1)
	
	syncControls([Controls.UP , Controls.DOWN , Controls.LEFT , Controls.RIGHT , Controls.JUMP , Controls.ATTACK1 , Controls.ATTACK2 , Controls.POWER1])

	initialized = TRUE
	'''
	0 -> idle
	1 -> running grounded
	2 -> jumping
	3 -> falling
	'''
	state = 0

	def setPositionState():
		global positionState


	def getState():
		global state
		vertVel = vertDisplacement()

		if struck(Controls.POWER1):
			state = -1
			activateAnim(e_MariaSlide)
			startHangup(1)
			onHangupEnd(lambda: setState(0))

		if state == -1:
			return

		elif vertVel > 0:
			state = 2

		elif vertVel < 0:
			state = 3

		elif anyPressed([Controls.LEFT , Controls.RIGHT]):
			state = 1

		else:
			state = 0

	def setState(val):
		global state
		state = val

	def hurt(damageType , horizDirection , vertDirection):

		life = lib.subtractLife(E , damageType)

		if life < 0:
			pass

		else:
			global state
			state = 4
			initialX = -4.5 if hurt_horizDir == Direction.RIGHT else 4.5         
			impulse(ForceType.LINEAR_DECAY , 2500  , 0.0 , 5.5 , 0.0 , 0.08)
			impulse(ForceType.LINEAR_DECAY , 2500  , initialX , 0.0 , 0.08 , 0.0)
			onFinish(setState , 0)
			face(horizDirection.opposite())
			setControl(FALSE)  
			hurtBloodEmitter.start()  
			forMillis(1000 , lambda: hurtBloodEmitter.setPosition(xMid() , yMid()) , None)


getState()

if state == 0:
	activateAnim(e_MariaIdle2)
elif state == 1:
	activateAnim(e_MariaWalk)
elif state == 2:
	activateAnim(e_MariaJump)
elif state == 3:
	activateAnim(e_MariaFall) 
elif state == 4:
	activateAnim(e_MariaHurt)
	if animations[e_MariaHurt].lastFrameOfLastSprite():
		animations[e_MariaHurt].freeze = TRUE