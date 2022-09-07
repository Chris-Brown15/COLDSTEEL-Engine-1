from Core import TemporalExecutor

console = lib.getConsole()
glfw = lib.getGLFW()

def say(line):
	console.say(str(line))

def ownerHangup():
	EanimList.startHangup()

def moveH(amount):
	I.translate(amount , 0)

def moveV(amount):
	I.translate(0 , amount)

'''
This function moves the calling item to it's owners joint provided by jointIndex. 
itemJointIndex is the joint in the list of joints present in the float array itemSprite which will be used for moving this item to its owner
itemSprite is a float array representation of a sprite whose joint at itemJointIndex will be used for translation

'''
def moveToOwnersJoint(ownerJointIndex , itemJointIndex , itemSprite):

	#joint positions are relative to the top left position of the quad they belong to
	#so get their world coordinate by adding the joint positions to the top left x and y
	ownerActiveSprite = E_animList.active().getActiveSprite()

	if len(itemSprite) <= 7 or len(ownerActiveSprite) <= 7:
		return

	ownerData = E.getData()
	itemData = I.getData()	
		
	userDirection = E_components[DOFF]
	defaultDir = E_animList.active().defaultDirection()	

	#joint offsets are the local space coordinates of the joints with respect to the top left coordinate
	ownerJointOffsetX = ownerActiveSprite[6 + (3 * ownerJointIndex) + 1]
	ownerJointOffsetY = ownerActiveSprite[6 + (3 * ownerJointIndex) + 2]

	itemJointOffsetX = itemSprite[6 + (3 * itemJointIndex) + 1]
	itemJointOffsetY = itemSprite[6 + (3 * itemJointIndex) + 2]

	if userDirection == defaultDir:
		ownerJointX = ownerData[9] + ownerJointOffsetX
		ownerJointY = ownerData[10] + ownerJointOffsetY

		itemJointX = itemData[9] + itemJointOffsetX
		itemJointY = itemData[10] + itemJointOffsetY

		deltaX = ownerJointX - itemJointX
		deltaY = ownerJointY - itemJointY

		I.translate(deltaX , deltaY)

	else:
		#when reflected
		ownerJointX = ownerData[18] - ownerJointOffsetX
		ownerJointY = ownerData[10] + ownerJointOffsetY

		itemJointX = itemData[18] - itemJointOffsetX
		itemJointY = itemData[10] + itemJointOffsetY

		deltaX = ownerJointX - itemJointX
		deltaY = ownerJointY - itemJointY

		I.translate(deltaX , deltaY)
	

def getOwnersJointPosition(ownerJointIndex):

	ownerData = E.getData()
	ownerActiveSprite = E_animList.active().getActiveSprite()	

	if len(ownerActiveSprite) <= 7 :
		return [0 , 0]

	ownerJointOffsetX = ownerActiveSprite[6 + (3 * ownerJointIndex) + 1]
	ownerJointOffsetY = ownerActiveSprite[6 + (3 * ownerJointIndex) + 2]

	if E_components[DOFF] == E_animList.active().defaultDirection(): #if the entity is facing the sprite set's default direction
		return [ownerData[9] + ownerJointOffsetX , ownerData[10] + ownerJointOffsetY]
	else:
		return [ownerData[18] - ownerJointOffsetX , ownerData[10] + ownerJointOffsetY]

def render():
	lib.getRenderer().addToOthers(I)

def animate(spriteSet):
	if spriteSet.defaultDirection() != E_components[DOFF]:
		I.swapAndFlipSprite(spriteSet.swapSprite())
	else:
		I.swapSprite(spriteSet.swapSprite())

def renderHitboxes():
	lib.renderItemHitboxes(I)

def stopRenderHitboxes():
	lib.stopRenderItemHitboxes(I)

def findEntity(scanRadius):
	return lib.findEntity(I , scanRadius)

def checkHitBoxes(entityOptional):
	return lib.checkHitBoxes(I , entityOptional)

def isUserHot(hitboxScan):
	return hitboxScan.callerHot() >= 0

def isTargetCold(hitboxScan):
	return hitboxScan.targetCold() >= 0
	
def hurt(entityScan , damageType):
    lib.hurt(E , entityScan , damageType)

def launchProjectile(projectile):
	lib.launchProjectile(projectile)

def usersAnimation():
	return E_animList.active()