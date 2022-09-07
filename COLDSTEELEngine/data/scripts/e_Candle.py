if not initialized:

	from Game.Items import LootTables
	from Core import Direction
	
	destroyParticle = createAnimatedParticleEmitter(1 , 120 , None , None , assets + "entities/Skeleton.png" , "m_SmallFireParticle.CStf" , bool(TRUE))
	destroyParticle.removeColor(1.0 , 1.0 , 1.0)
	destroyParticle.finishAtEnd(TRUE)

	animList.activate(e_Candle1)
	hitboxList.activate(0)

	loot = newLootTable()	
	loot.addItem("Heart" , 100 , 1)

	def shutDown():
		pass

	def onKillImpl(hurtData):
		mid = E.getMidpoint()
		loot.moveTo(mid[0] , mid[1])
		loot.computeLootTable()			
		destroyParticle.start()
		TemporalExecutor.forMillis(120 , destroyParticle.update)
		remove()

	components[SOFF + 1] = bool(FALSE)
	onKill(onKillImpl)
	initialized = TRUE