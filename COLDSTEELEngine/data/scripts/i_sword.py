'''
sword swinging script
handles moving sword graphic to the user's joint 0 
and setting the sword swing animation frame to match the user's sword swing animation
'''

if not initialized:

	from Core import SpriteSets
	from Game.Core import DamageType

	swordDamage = DamageType.PHYSICAL
	swordDamage.setValue(10)
	render()
	swordSwingAnim = SpriteSets(data + "spritesets/i_SwordSwing.CStf")

	def stopUse():
		I.shouldRender(FALSE)

	initialized = TRUE

I.shouldRender(TRUE)
userActiveAnimSpriteIndex = E_animList.active().getCurrentSprite()
swordSwingAnim.setCurrentSprite(userActiveAnimSpriteIndex)

if userActiveAnimSpriteIndex <= 1:
	animate(swordSwingAnim)
	moveToOwnersJoint(0 , 0 , swordSwingAnim.getActiveSprite())
	lib.activateHitboxByAnimation(I , swordSwingAnim)
	entityScan = findEntity(100)
	if entityScan.hasEntity():
		hitboxScan = checkHitBoxes(entityScan)
		if hitboxScan.collided() and isUserHot(hitboxScan) and isTargetCold(hitboxScan):
			hurt(entityScan , swordDamage)

else:
	I.shouldRender(FALSE)
