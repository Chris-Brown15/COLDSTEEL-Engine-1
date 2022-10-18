if not initialized:
	'''
	position states, these describe his location in space
	'''
	POS_ON_GROUND = 0
	POS_IN_AIR = 1
	POS_DUCKING = 2

	'''
	action states, these describe the actions he's performing. 
		These are bit masks. Multiple can be true at once.
	'''

	A_IDLE = 0
	A_MOVING = 1
	# need two bits for this, unset both if neither is happening
	A_SWORD_ATTACK = 2
	A_ITEM_USE = 4
	A_HURT = 8

	#non constants here
	actionState = A_IDLE
	positionState = POS_ON_GROUND

	actionFrozen = FALSE

	def actionState():

		if actionFrozen:
			return

		global actionState

		if horizDisplacement() != 0:
			actionState |= A_MOVING
		else:
			actionState |= A_IDLE

		if struck(Controls.ATTACKI):
			actionState |= A_SWORD_ATTACK
		elif struck(Controls.ATTACKII):
			actionState |= A_ITEM_USE
		else:
			actionState ^= (A_SWORD_ATTACK|A_ITEM_USE)

	def getAnimation():		
		if positionState == POS_ON_GROUND:

			if (actionState & A_SWORD_ATTACK) != 0:
				activateAnim(e_AlucardStandingSword)
				startHangup(1)

			elif (actionState & A_SWORD_ATTACK) != 0:
				pass

			elif (actionState & A_IDLE) != 0:
				activateAnim(e_AlucardIdle)

			elif (actionState & A_MOVING):
				activateAnim(e_AlucardRun)
				if(animations[e_AlucardRun].lastFrameOfLastSprite()):
					animations[e_AlucardRun].setCurrentSprite(15)

		elif positionState == POS_IN_AIR:
			pass
		elif positionState == POS_DUCKING:
			pass

	initialized = TRUE

actionState()
getAnimation()
