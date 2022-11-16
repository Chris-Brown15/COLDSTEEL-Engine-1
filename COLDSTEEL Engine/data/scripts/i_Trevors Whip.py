if not initialized:

	from Core import SpriteSets
	from Game.Core import DamageType

	whipAnim = SpriteSets("i_Trevor Whip 1.CStf")

	whipHolyDamage = DamageType.HOLY
	whipHolyDamage.setValue(45)
	whipPhysDamage = DamageType.PHYSICAL
	whipPhysDamage.setValue(45)

	def stopUse():
		I.shouldRender(FALSE)

	initialized = TRUE

render()
I.shouldRender(TRUE)
userActiveAnimSpriteIndex = E_animList.active().getCurrentSprite()
whipAnim.setCurrentSprite(userActiveAnimSpriteIndex)
animate(whipAnim)
moveToOwnersJoint(0 , 0 , whipAnim.getActiveSprite())
lib.activateHitboxByAnimation(I , whipAnim)
lib.checkHitBoxesAndHurt(I , [whipHolyDamage , whipPhysDamage])