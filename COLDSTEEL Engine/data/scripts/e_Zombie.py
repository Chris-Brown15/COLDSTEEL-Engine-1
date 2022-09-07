if not initialized:

	from Core import components 
	from Game.Core import DamageType    
	from java.util import Random

	searchFor = [components.HORIZONTAL_PLAYER_CONTROLLER , components.VERTICAL_PLAYER_CONTROLLER]
	targetScan = findEntityWithAny(99999 , searchFor)
	rand = Random()
	isPurp = rand.nextBoolean()
	zombieHand = damageType.PHYSICAL
	zombieHand.value(7)

	if targetScan.hasEntity():
		global walkSpeed
		walkSpeed = -1 if face(targetScan) == direction.RIGHT else 1
		resDirection = horizontally(targetScan)
	else:
		remove()

	if isPurp:
		activateAnim(e_PurpleZombieSpawn)
	else:
		activateAnim(e_OrangeZombieSpawn)

	started = FALSE

	def start():
		global started
		started = TRUE
		if isPurp:
			activateAnim(e_PurpleZombieWalk)
		else:
			activateAnim(e_OrangeZombieWalk)

	animList.startHangup()
	onHangupEnd(start , None)
	initialized = TRUE	

	def kill():
		if isPurp:
			animations[e_PurpleZombieSpawn].reverse(TRUE)
			activateAnim(e_PurpleZombieSpawn)
			if animations[e_PurpleZombieSpawn].lastFrameOfLastSprite():
				remove()
		else:
			animations[e_OrangeZombieSpawn].reverse(TRUE)
			activateAnim(e_OrangeZombieSpawn)
			if animations[e_OrangeZombieSpawn].lastFrameOfLastSprite():
				remove()

if started:	

	console.put(0 , activeHitBoxIndex())

	#if the zombie collides with anything, kill it
	if moveH(walkSpeed):
		kill()

	hitScan = hitboxScan(targetScan)
	if hotbox(hitScan):
		attack(targetScan , zombieHand)