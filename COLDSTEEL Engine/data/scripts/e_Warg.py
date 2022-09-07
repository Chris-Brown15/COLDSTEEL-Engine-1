'''
Warg is a wolf monster that has a few behaviors. 
1) If it cannot find an enemy, it will idle.
2) if it finds an entity and it is not facing them, it will face them
3) if it is facing an enemy it will growl at them
4) if the enemy gets close enough, the warg will lunge at them
5) it can be killed like anything else

'''

if not initialized:
    
    from Game.Core import DamageType    
    from Core import ECS 
    from Core import SpriteSets
    from Physics import MExpression
    from CSUtil import Timer
    from Core import TemporalExecutor

    hasScan = FALSE

    biteAttack = DamageType.PHYSICAL
    biteAttack.setValue(15)
    
    hurtBloodExpr = MExpression("srand ( 125 / x )" , TRUE)
    hurtBloodEmitter = createParticleEmitter(160 , 2400 , hurtBloodExpr , hurtBloodExpr , 0.90 , 0.0 , 0.0 , 1.5 , 1.5 , TRUE)  
    hurtBloodEmitter.setEmissionRate(1)

    fireParticleOffseterX = MExpression("srand x")
    fireParticleOffseterY = MExpression("rand x")
    deathFireEmitter = createAnimatedParticleEmitter(10 , 2000 , None , None , assets + "entities/firePuff.png" , "m_firePuff.CStf" , TRUE)
    deathFireEmitter.setEmissionRate(200)
    deathFireEmitter.finishAtEnd(TRUE)
    deathFireEmitter.removeColor(0.5019608 , 0 , 0)
    
    def deathFireParticleInit(particle):
        particle.translate(fireParticleOffseterX.at(75) , fireParticleOffseterY.at(50) + 15)

    def deathFireParticleUpdate(particle):
        particle.translate(0 , 1)

    deathFireEmitter.onParticleStart(lambda particle: deathFireParticleInit(particle))

    hurtColorExpr = MExpression("rand x")
    actionState = 1
    searchFor = [ECS.HORIZONTAL_PLAYER_CONTROLLER , ECS.VERTICAL_PLAYER_CONTROLLER]
    CHARGE_COOLDOWN = 240838029458902 + E.LID()
    setAutoOrient(FALSE)
    hitTarget = FALSE

    def grounded():
        activateAnim(e_WargIdle)

    def aggrod():
        activateAnim(e_WargAggro)
        animations[e_WargAggro].setCurrentSprite(1)
     
    def scanForHit(scan):
        if scan.hasEntity():
            hitboxCollision = hitboxScan(scan)            
            if hotbox(hitboxCollision):
                return TRUE

        return FALSE

    #attack has to return a damage type for use in the library function
    def bite(scanRes):
        global actionState
        global hitTarget
        activateAnim(e_WargBite)
        if not TemporalExecutor.coolingDown(CHARGE_COOLDOWN):
                        
            TemporalExecutor.coolDown(2400.0 , CHARGE_COOLDOWN)
            resDirection = horizontally(scanRes)
            Kinematics.impulse(ForceType.LINEAR_DECAY , 700 , 3.5 if resDirection == Direction.RIGHT else -3.5 , 0 , 0.05 , 0 , E)
            hitTarget = FALSE

        #hit the entity
        if scanForHit(scanRes):
            hurtTarget(scanRes , biteAttack)
            hitTarget = TRUE

    def endTurnAround(direction):
        global actionState
        components[DOFF] = direction
        actionState = 3

    def turnAround(direction):
        global actionState
        actionState = 2
        activateAnim(e_WargTurnAround)
        startHangup(1)
        onHangupEnd(lambda: endTurnAround(direction))

    def hurtElapseOf():
        global actionState
        actionState = 3
        animList.active().freeze = FALSE
        hurtBloodEmitter.finish()
        E.setFilter(0 , 0 , 0)

    def takingDamage():        
        animList.active().freeze = TRUE        
        redColor = hurtColorExpr.at(0.25) + 0.45
        E.setFilter(redColor , 0.0 , 0.0)

    def death():
        if animList.active().lastFrameOfLastSprite():
            animList.active().freeze = TRUE

        deathFireEmitter.update()
        animList.endHangup()
        currentFilter = E.getFilter()
        E.setFilter(currentFilter.x() - 0.03 , currentFilter.y() - 0.03 , currentFilter.z() - 0.03)        
        E.makeTranslucent(E.getTranslucency() - 0.001)

    def deathAnimation():
        global actionState
        actionState = 6
        activateAnim(e_WargKill)
        if animations[e_WargKill].lastFrameOfLastSprite():
            animations[e_WargKill].freeze = TRUE
        currentFilter = E.getFilter()
        E.setFilter(currentFilter.x() - 0.06 , currentFilter.y() - 0.06 , currentFilter.z() - 0.06)        
        E.makeTranslucent(E.getTranslucency() - 0.0025)

    def onKillImpl(hurtData):
        global actionState
        actionState = 6
        deathFireEmitter.setPosition(xMid() , yMid())
        activateAnim(e_WargKill)
        TemporalExecutor.forMillis(6000 , deathAnimation)
        TemporalExecutor.onElapseOf(6000 , remove)
        #we need to update the fire emitter for longer than the entity will exist
        TemporalExecutor.forMillis(7000 , lambda: death())
        deathFireEmitter.start()


    #TODO: implement the warg slide based on the direction it was hit from
    def onHurtImpl(hurtData):
        #getting hit
        global actionState
        actionState = 5
        animList.active().freeze = TRUE
        hurtBloodEmitter.start()
        hurtBloodEmitter.setPosition(xMid() , yMid())
        Kinematics.impulse(ForceType.LINEAR , 250 , 1 if horizontally(hurtData) == Direction.LEFT else -1 , 0 , 0 , 0 , E)
        TemporalExecutor.onElapseOf(250 , hurtElapseOf)

    def setState(scan):

        global actionState

        #if not currently doing something post attack
        if actionState < 5:
            #idle
            if not scan.hasEntity():
                actionState = 1

            #found an entity
            else:
                isFacing = facing(scan)
                targetDirection = horizontally(scan)
                if actionState != 2 and not isFacing:
                    turnAround(targetDirection)

                # found an entity and facing it
                elif actionState > 2:
                    #start attacking
                    if abs(scan.xDistance) < 140 and abs(scan.yDistance) <= 50:
                        actionState = 4

                    else:
                        actionState = 3

    onHurt(onHurtImpl)
    onKill(onKillImpl)
    initialized = TRUE

if not hasScan:
    scan = findEntityWithAny(300 , searchFor)

if actionState < 5:
    setState(scan)

if actionState == 1:
    grounded()

elif actionState == 2:
    pass

elif actionState == 3:
    aggrod()

elif actionState == 4:
    bite(scan)

elif actionState == 5:
    takingDamage()

elif actionState == 6:
    pass