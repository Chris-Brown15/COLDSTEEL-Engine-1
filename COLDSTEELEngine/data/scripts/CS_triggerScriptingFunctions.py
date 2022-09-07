from Core import Direction

TRUE = 1
FALSE = 0

def getEntity(LID):
	return lib.getEntity(LID)

def numberEntities():
	return lib.numberEntities()

def insideConditionAreas(E):
	return lib.insideConditionAreas(E , T)

def insideEffectAreas(E):
	return lib.insideEffectAreas(E , T)

def killEntity(entity , horizontal , vertical):
	lib.killEntity(entity , horizontal , vertical)

def entityCurrentHorizontalDirection(entity):
	return lib.entityCurrentHorizontalDirection(entity)

def entityCurrentVerticalDirection(entity):
	return lib.entityCurrentVerticalDirection(entity)

def isEntityKilled(E):
	return lib.isEntityKilled(E)

def stopTrigger():
	lib.removeTrigger(T)

def hurtEntity(E , data):
	lib.hurtEntity(E , data)

def getConditionArea(index):
	return lib.getConditionArea(T , index)