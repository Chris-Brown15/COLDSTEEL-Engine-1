
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

from org.lwjgl.glfw.GLFW import GLFW_MOUSE_BUTTON_LEFT
from org.lwjgl.glfw.GLFW import GLFW_MOUSE_BUTTON_RIGHT
from org.lwjgl.glfw.GLFW import GLFW_MOUSE_BUTTON_MIDDLE
from org.lwjgl.glfw.GLFW import GLFW_MOUSE_BUTTON_4
from org.lwjgl.glfw.GLFW import GLFW_MOUSE_BUTTON_5

from org.lwjgl.glfw.GLFW import GLFW_KEY_Q
from org.lwjgl.glfw.GLFW import GLFW_KEY_W
from org.lwjgl.glfw.GLFW import GLFW_KEY_E
from org.lwjgl.glfw.GLFW import GLFW_KEY_R
from org.lwjgl.glfw.GLFW import GLFW_KEY_T
from org.lwjgl.glfw.GLFW import GLFW_KEY_Y
from org.lwjgl.glfw.GLFW import GLFW_KEY_U
from org.lwjgl.glfw.GLFW import GLFW_KEY_I
from org.lwjgl.glfw.GLFW import GLFW_KEY_O
from org.lwjgl.glfw.GLFW import GLFW_KEY_P

from org.lwjgl.glfw.GLFW import GLFW_KEY_A
from org.lwjgl.glfw.GLFW import GLFW_KEY_S
from org.lwjgl.glfw.GLFW import GLFW_KEY_D
from org.lwjgl.glfw.GLFW import GLFW_KEY_F
from org.lwjgl.glfw.GLFW import GLFW_KEY_G
from org.lwjgl.glfw.GLFW import GLFW_KEY_H
from org.lwjgl.glfw.GLFW import GLFW_KEY_J
from org.lwjgl.glfw.GLFW import GLFW_KEY_K
from org.lwjgl.glfw.GLFW import GLFW_KEY_L

from org.lwjgl.glfw.GLFW import GLFW_KEY_Z
from org.lwjgl.glfw.GLFW import GLFW_KEY_X
from org.lwjgl.glfw.GLFW import GLFW_KEY_C
from org.lwjgl.glfw.GLFW import GLFW_KEY_V
from org.lwjgl.glfw.GLFW import GLFW_KEY_B
from org.lwjgl.glfw.GLFW import GLFW_KEY_N
from org.lwjgl.glfw.GLFW import GLFW_KEY_M

from org.lwjgl.glfw.GLFW import GLFW_KEY_KP_0
from org.lwjgl.glfw.GLFW import GLFW_KEY_KP_1
from org.lwjgl.glfw.GLFW import GLFW_KEY_KP_2
from org.lwjgl.glfw.GLFW import GLFW_KEY_KP_3
from org.lwjgl.glfw.GLFW import GLFW_KEY_KP_4
from org.lwjgl.glfw.GLFW import GLFW_KEY_KP_5
from org.lwjgl.glfw.GLFW import GLFW_KEY_KP_6
from org.lwjgl.glfw.GLFW import GLFW_KEY_KP_7
from org.lwjgl.glfw.GLFW import GLFW_KEY_KP_8
from org.lwjgl.glfw.GLFW import GLFW_KEY_KP_9

from org.lwjgl.glfw.GLFW import GLFW_KEY_0
from org.lwjgl.glfw.GLFW import GLFW_KEY_1
from org.lwjgl.glfw.GLFW import GLFW_KEY_2
from org.lwjgl.glfw.GLFW import GLFW_KEY_3
from org.lwjgl.glfw.GLFW import GLFW_KEY_4
from org.lwjgl.glfw.GLFW import GLFW_KEY_5
from org.lwjgl.glfw.GLFW import GLFW_KEY_6
from org.lwjgl.glfw.GLFW import GLFW_KEY_7
from org.lwjgl.glfw.GLFW import GLFW_KEY_8
from org.lwjgl.glfw.GLFW import GLFW_KEY_9

from org.lwjgl.glfw.GLFW import GLFW_KEY_TAB
from org.lwjgl.glfw.GLFW import GLFW_KEY_CAPS_LOCK
from org.lwjgl.glfw.GLFW import GLFW_KEY_LEFT_SHIFT
from org.lwjgl.glfw.GLFW import GLFW_KEY_LEFT_CONTROL
from org.lwjgl.glfw.GLFW import GLFW_KEY_LEFT_ALT
from org.lwjgl.glfw.GLFW import GLFW_KEY_SPACE
from org.lwjgl.glfw.GLFW import GLFW_KEY_BACKSPACE
from org.lwjgl.glfw.GLFW import GLFW_KEY_ENTER
from org.lwjgl.glfw.GLFW import GLFW_KEY_RIGHT_SHIFT
from org.lwjgl.glfw.GLFW import GLFW_KEY_RIGHT_CONTROL
from org.lwjgl.glfw.GLFW import GLFW_KEY_RIGHT_ALT

from org.lwjgl.glfw.GLFW import GLFW_PRESS
from org.lwjgl.glfw.GLFW import GLFW_RELEASE
from org.lwjgl.glfw.GLFW import GLFW_REPEAT

console = lib.getConsole() if lib.getState() == RuntimeState.EDITOR else None

TRUE = 1
FALSE = 0

def getAllVariables():    
    from CSUtil.DataStructures import CSLinked
    from CSUtil.DataStructures import Tuple2

    globalVariables = globals()
    global variableList    
    variableList = CSLinked()

    for x , i in list(globalVariables.items()):
        variableList.add(Tuple2(x , i))

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

def getKey(key):
    return lib.getKey(key)

def kbPressed(key):
    return lib.getKey(key) == GLFW_PRESS

def kbReleased(key):
    return lib.getKey(key) == GLFW_RELEASE    

def mPressed(key):
    return lib.getMouseKey(key) == GLFW_PRESS

def mReleased(key):
    return lib.getMouseKey(key) == GLFW_RELEASE

def kbStruck(key):
    return lib.keyboardStruck(key)

def mStruck(key):
    return lib.mouseStruck(key)

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