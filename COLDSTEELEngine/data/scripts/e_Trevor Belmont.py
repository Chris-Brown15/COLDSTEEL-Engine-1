if not initialized:

	from Core import UIScript
	from Game.Items import ItemComponents

	#position state constants
	GROUNDED = 1	
	AIRBORN = 2
	DUCKING = 3

	#action state constants
	IDLE = 1
	ATTACKING = 2
	SUB_WEAPON = 3
	HURT = 4

	HUD = UIScript("ui_TrevorHUD.py")
	HUD.set("maxLife" , maxLife())
	HUD.set("life" , currentLife())
	HUD.set("hearts" , 0)
	HUD.toggle()
	gettingHurt = FALSE

	positionState = GROUNDED
	previousPositionState = GROUNDED
	actionState = IDLE

	attackCooldown = 743999 + E.getID()
	subWeaponCooldown = 352454545 + E.getID()

	whip = inventory.equipSlot(0)
	subWeapon = inventory.equipSlot(1)

	def itemAcquireCallback(item):
		if item.has(ItemComponents.FLAGS):
			if item.componentData().hasFlag("SUBWEAPON"):
				if not inventory.has(item):
					global subWeapon
					addSubWeaponFunction = HUD.get("addSubWeapon")
					addSubWeaponFunction(item)
					subWeapon = item

	inventory.onAcquire(itemAcquireCallback)

	def endHurt():
		setControl(TRUE)
		global actionState
		actionState = IDLE

	def onHurtImpl(hurtData):
		global actionState
		actionState = HURT
		
		hurtDir = horizontally(hurtData)
		initialX = 5 if hurtDir == Direction.LEFT else -5
		Kinematics.impulse(ForceType.LINEAR_DECAY , 2500  , 0.0 , 5.5 , 0.0 , 0.1 , E)
		Kinematics.impulse(ForceType.LINEAR_DECAY , 2500  , initialX , 0.0 , 0.1 , 0.0 , E)
		TemporalExecutor.onTicks(55 , lambda: endHurt())
		setControl(FALSE)

	def setActionState(state):
		global actionStated
		actionState = state
		if actionState != HURT:
			setControl(TRUE)

	def getPositionState():
		global positionState
		positionState = AIRBORN if distanceToFloor() > 5 else DUCKING if glfw.isSPressed() else GROUNDED

	def getActionState():
		global actionState
		if actionState == HURT:
			actionState = HURT

		elif glfw.isLMousePressed():
			actionState = ATTACKING

		elif glfw.isEPressed():
			actionState = SUB_WEAPON
		else:
			actionState = IDLE

	def attack(attackAnim):
		activateAnim(attackAnim)
		if not TemporalExecutor.coolingDown(attackCooldown):
			startHangup(1)
			TemporalExecutor.coolDown(isHungup , attackCooldown)
			TemporalExecutor.whileTrue(isHungup , lambda: whip.use())
			if positionState == GROUNDED:
				setHorizontalControl(FALSE)
				setAutoOrient(FALSE)
				TemporalExecutor.onTrue(lambda: not isHungup() , lambda: setControl(TRUE))

	def useSubWeapon(itemUseAnim):
		if subWeapon == None or inventory.numberOfItem("Heart").get() <= 0:
			return

		if not TemporalExecutor.coolingDown(subWeaponCooldown):
			inventory.remove("Heart")
			activateAnim(itemUseAnim)
			startHangup(1)
			if positionState == GROUNDED:
				setHorizontalControl(FALSE)
				TemporalExecutor.onTrue(lambda: not isHungup() , lambda: setHorizontalControl(TRUE))
			TemporalExecutor.coolDown(isHungup , subWeaponCooldown)
			TemporalExecutor.whileTrue(isHungup , lambda: subWeapon.use())

	def getAnimation():
		if actionState == HURT:
			activateAnim(e_TrevorHurt)
		elif positionState == AIRBORN:
			if actionState == ATTACKING:
				attack(e_TrevorJumpingAttack)				
								
			elif actionState == SUB_WEAPON:
				useSubWeapon(e_TrevorJumpingItem)
				
			elif horizDisplacement() != 0:
				activateAnim(e_TrevorJumpMoving)
				if animations[e_TrevorJumpMoving].lastFrameOfLastSprite():
					animations[e_TrevorJumpMoving].freeze = TRUE
			else:
				activateAnim(e_TrevorJumpStraight)

		elif positionState == GROUNDED:
			if actionState == ATTACKING:
				attack(e_TrevorAttack)

			elif actionState == SUB_WEAPON:
				useSubWeapon(e_TrevorItemUse)
			
				
			
			elif horizDisplacement() != 0:
				activateAnim(e_TrevorWalk)

			else:
				activateAnim(e_TrevorIdle) 

		elif positionState == DUCKING:
			setHorizontalControl(FALSE)
			if previousPositionState != positionState:
				TemporalExecutor.onTrue(lambda: not glfw.isSPressed() , lambda: setHorizontalControl(TRUE))
			
			if actionState == ATTACKING:
				attack(e_TrevorDuckingAttack)				
				
			else:
				activateAnim(e_TrevorDuck)
				if animations[e_TrevorDuck].lastFrameOfLastSprite():
					animations[e_TrevorDuck].freeze = TRUE

	def updateUI():
		HUD.set("life" , currentLife())
		HUD.set("hearts" , inventory.numberOfItem("Heart"))

	onHurt(onHurtImpl)
	initialized = TRUE

getPositionState()
getActionState()
findItemsByFlag(["SUBWEAPON" , "AMMO"])
getAnimation()
updateUI()

global previousPositionState
previousPositionState = positionState