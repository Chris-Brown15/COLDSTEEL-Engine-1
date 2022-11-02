
'''

This is a script containing python functions available to all entities
This script will be called by the interpreter for this entity in startup, making all this available
After this call, which will take place in the EntityScriptInterpreter constructor, the PythonInterpreter
will have access to functions needed for scripting.

'''
from Core import Direction
from CS import RuntimeState
from CSUtil import Timer
from Physics import Kinematics
from Physics import ForceType
from Core import TemporalExecutor
from Core import UIScript
from CS import Controls

console = lib.getConsole() if lib.getState() == RuntimeState.EDITOR else None
controlBinds = lib.clientControls()

TRUE = 1
FALSE = 0

def moveH(xDist):
    return lib.moveHzntl(E , xDist)

def moveV(yDist):
    return lib.moveVrtcl(E , yDist)

def animate(dir , index):
    lib.animate(E , dir , index)

def toggleHorizontalControl():
    lib.toggleHorizontalControl(E)
    
def toggleVerticalControl():
    lib.toggleVerticalControl(E)

def activeAnimation():
    return lib.activeAnimation(E)
    
def activeAnimIndex():
    return lib.activeAnimationIndex(E)

def activateAnim(index):
    lib.activateAnim(E , index)

def setAutoOrient(state):
    lib.setAutoOrient(E , state)

def activateHitBox(index):
    lib.activateHitBox(E , index)

def activeHitBoxIndex():
    return lib.activeHitBoxIndex(E)

def distanceToFloor():
    return lib.distanceToFloor(E)

def setHorizontalControl(state):
    lib.setHorizontalControl(E , state)
    
def setVerticalControl(state):
    lib.setVerticalControl(E , state)
    
def findEntityAnd(radius , callback):
    return lib.findEntityAnd(E , radius , callback)
    
def say(sayThis):
    console.say(E.name() + " says: " + str(sayThis))
    
def face(target):
    return lib.face(E , target)

def hurtTarget(entityScan , damageType):
    lib.hurtTarget(E , entityScan , damageType)

def findEntity(radius):
    return lib.findEntity(E , radius)
    
def findEntityWithAny(radius , comps):
    return lib.findEntityWithAny(E , radius , comps)

def findEntityWithAll(radius , comps):
    return lib.findEntityWithAll(E , radius , comps)

def hitboxScan(scanResult):
    return lib.scanHitBoxes(E , scanResult)
    
def hotbox(hitboxScan):
    return hitboxScan.collided() and hitboxScan.callerHot() >= 0 and hitboxScan.targetCold() >= 0

def horizDirection():
    return components[DOFF]

def vertDirection():
    return components[DOFF + 1]

def horizDisplacement():
    return components[HDOFF + 1]

def vertDisplacement():
    return components[VDOFF + 1]

def toggle(boolOrByte):
    boolOrByte = 0 if boolOrByte else 1
    return boolOrByte

def remove():
    lib.remove(E)

def subtractLife(damageType):
    return lib.subtractLife(E , damageType)

def currentLife():
    return rpgStats.currentLife()

def currentMana():
    return rpgStats.currentMana()

def currentStamina():
    return rpgStats.currentStamina()

def maxMana():
    return rpgStats.maxMana()

def maxLife():
    return rpgStats.maxLife()   

def maxStamina():
    return rpgStats.maxStamina()   

def horizontally(entityScan):
    return lib.horizontally(E , entityScan)

def randomInt(range):
    return lib.randomInt(range)

def startHangup(iters):
    animList.startHangup(iters)

def endHangup():
    animList.endHangup()

def onHangupEnd(callback):
    lib.onHangupEnd(animList , callback)

def isHungup():
    return animList.isHungup()

def currentHorizSpeed():
    return lib.currentSpeedX(E)

def colliding():
    return components[CDOFF + 2]

def createParticleEmitter(numberParticles , lifetimeMillis , xFunction , yFunction , red , green , blue , xSize , ySize , foreground):
    return lib.createParticleEmitter(numberParticles , lifetimeMillis , xFunction , yFunction , red , green , blue , xSize , ySize , foreground)

def createAnimatedParticleEmitter(numberParticles, lifetimeMillis , xFunction , yFunction , textureAbsPath , animAbsPath , foreground):
    return lib.createParticleEmitter(numberParticles , lifetimeMillis , xFunction , yFunction , textureAbsPath , animAbsPath , foreground)

def createAnimatedParticleEmitterGivenAnim(numberParticles , lifetimeMillis , xFunction , yFunction , textureAbsPath , animation , foreground):
    return lib.createParticleEmitter(numberParticles , lifetimeMillis ,  xFunction , yFunction , textureAbsPath , animation , foreground)

def xMid():
    return E.getMidpoint()[0]

def yMid():
    return E.getMidpoint()[1]

def onElapseOf(timeMillis , code , codeInput):
    if codeInput != None:
        lib.onElapseOf(timeMillis , code , codeInput)
    else:
        lib.onElapseOf(timeMillis , code)
    lib.onTicks(ticks , code)
 
def setControl(state):
    setHorizontalControl(state)
    setVerticalControl(state) 
    setAutoOrient(state)

def facing(scanRes):
    return lib.facing(E , scanRes)

def findItems():
    return lib.findItems(E)

def findItemsByFlag(flags):
    lib.findItemsByFlag(E , flags)

def getInventory():
    return components[IOFF]

def freeze():
    E.freeze(TRUE)

def unFreeze():
    E.freeze(FALSE)

def isFrozen():
    return E.isFrozen()

def onHurt(function):
    rpgStats.onHurt(function)

def onKill(function):
    rpgStats.onKill(function)

def stopOnInitialization(state):
    stopOnInitialization = state
    if state == TRUE:
        components[SOFF + 1] = FALSE

def newLootTable():
    return lib.newLootTable()

def pressed(control):
    return lib.pressed(E , control)

def struck(control):
    return lib.struck(E , control)

def allPressed(controls):
    return lib.allPressed(E , controls)

def allStruck(controls):
    return lib.allStruck(E , controls)

def anyPressed(controls):
    return lib.anyPressed(E , controls)

def anyStruck(controls):
    return lib.anyStruck(E , controls)

def syncControls(controls):
    lib.setNetworkedControls(controls)

def onServer():
    return lib.onServer()
