'''

entities have an RPGStats component which has two Executors called onHurt and onKill
These will be called automatically when an entity is hit or killed by another entity.
The implementation of these two functions is given by an entity's script component.

'''
# init
if not initialized:
    
    #1 if on the ground, 2 if crouching, 3 if in the air , 4 if back dashing, 5 if taking damage
    actionState = 1
    
    from Core import UIScript
    from Physics import MExpression
    from Renderer import ParticleEmitter
    from Game.Items import ItemComponents
    
    print("Syncing controls in alucard script")
    syncControls([Controls.UP , Controls.DOWN , Controls.LEFT , Controls.RIGHT , Controls.JUMP , Controls.ATTACK1 , Controls.ATTACK2 , Controls.POWER1])

    isBackDashing = FALSE

    hurtBloodExpr = MExpression("srand ( 125 / x )" , TRUE)
    hurtBloodEmitter = createParticleEmitter(160 , 2400 , hurtBloodExpr , hurtBloodExpr , 0.90 , 0.0 , 0.0 , 1.5 , 1.5 , TRUE) 
    hurtBloodEmitter.setEmissionRate(1)

    if not onServer():
        HUD = UIScript("ui_AlucardUI.py")
        HUD.set("inventory" , inventory)
        HUD.set("equipped" , inventory.getEquipped())
        HUD.set("items" , inventory.getItems())
        HUD.run()

    '''
    Alucard can do a few moves:
    1) Run left and right (preparing run and run)
    2) crouch
    3) jump up and left and right (ascending and falling)
    4) back dash
    5) super jump
    6) various attacks (punch, kick, sword, thrust sword)
    7) magic attacks
    8) sub weapon attacks
    '''
    def setState(val):
        global actionState
        actionState = val

    # shuts down the script. This gets called from java when the entity is being removed. This is mainly for
    # closing UI elements this entity had running. 
    def shutDown():
        hurtBloodEmitter.shutDown()        

    def getHurtSound(): #randomly picks a sound from his hurt sounds
        rng = randomInt(2)
        if rng == 0:
            return e_AlucardHurt1
        else:
            return e_AlucardHurt2

    hurtHorizontalDirection = Direction.RIGHT

    # life is already subtracted.
    def onHurtImpl(entityHurtData):
        global actionState
        actionState = 5
        activateAnim(e_AlucardSpinning)
        hurtBloodEmitterSetup()
        setControl(FALSE)

        getHurtSound().play()
        global hurtHorizontalDirection
        hurtHorizontalDirection = horizontally(entityHurtData)
        face(hurtHorizontalDirection)
        initialX = 4.5 if hurtHorizontalDirection == Direction.LEFT else -4.5
        Kinematics.impulse(ForceType.LINEAR_DECAY , 2500  , 0.0 , 5.5 , 0.0 , 0.08 , E)
        Kinematics.impulse(ForceType.LINEAR_DECAY , 2500  , initialX , 0.0 , 0.08 , 0.0 , E)
        Kinematics.onFinish(lambda: setState(6))
        moveV(5)
        moveH(initialX)

    def onKillImpl(entityHurtData):
        activateAnim(e_AlucardSpinning)
        startHangup(3)
        onHangupEnd(finishKill)
        e_AlucardDeath.play()
        global actionState
        actionState = 7
        setControl(FALSE)
        hurtBloodEmitterSetup()
        hurtBloodEmitter.start()
        attackerDirection = horizontally(entityHurtData)
        face(attackerDirection)
        initialX = -4.5 if attackerDirection == Direction.LEFT else 4.5
        Kinematics.impulse(ForceType.LINEAR_DECAY , 5000  , 0.0 , 5.5 , 0.0 , 0.08 , E)
        Kinematics.impulse(ForceType.LINEAR_DECAY , 5000  , initialX , 0.0 , 0.08 , 0.0 , E)
        
    def grounded():
        
        setControl(TRUE)
        if isRunning:
            activateAnim(e_AlucardRun)
            endHangup()
            if(animations[e_AlucardRun].lastFrameOfLastSprite()):
                animations[e_AlucardRun].setCurrentSprite(15)
        else:
            activateAnim(e_AlucardIdle)
    
    def airborn():

        setControl(TRUE)
        global isBackDashing
        isBackDashing = FALSE
        
        if isJumping:

            setAutoOrient(TRUE)                       
            if anyPressed([Controls.RIGHT , Controls.LEFT]):
                activateAnim(e_AlucardJumpingHoriz)
            else:
                activateAnim(e_AlucardJumpStraight)
            if animations[e_AlucardJumpStraight].lastFrameOfLastSprite():
                animations[e_AlucardJumpStraight].freeze = TRUE
            if animations[e_AlucardJumpingHoriz].lastFrameOfLastSprite():
                animations[e_AlucardJumpingHoriz].freeze = TRUE
                
        else:
            activateAnim(e_AlucardFalling)
            setAutoOrient(TRUE)            
            if(animations[e_AlucardFalling].lastFrameOfLastSprite()):
                animations[e_AlucardFalling].freeze = TRUE    
            
    def crouch():
        
        global isBackDashing
        activateAnim(e_AlucardDucking)
        if animations[e_AlucardDucking].getCurrentSprite() == 11:
            animations[e_AlucardDucking].freeze = TRUE
        setVerticalControl(FALSE)
        setHorizontalControl(FALSE)
        isBackDashing = FALSE

    def backDash():
        global actionState
        setControl(FALSE)
        activateAnim(e_AlucardBackDash)

        if horizDirection() == Direction.RIGHT:
            moveH(-3)
        else:
            moveH(3)

        if animations[e_AlucardBackDash].lastFrameOfLastSprite():
            actionState = 1

    def unarmed():
        if struck(Controls.ATTACK1):
            global actionState            
            if isHungup():
                endHangup()

            if actionState == 1:#punching standing up
                
                activateAnim(e_AlucardStandingPunch)
                setHorizontalControl(FALSE)
                startHangup(2)

            elif actionState == 2:#punching while crouching
                activateAnim(e_AlucardDuckingPunch)
                startHangup(1)
                if animations[e_AlucardDuckingPunch].lastSprite:
                    animations[e_AlucardDucking].setCurrentSprite(11)

            elif actionState == 3: #punching while in the air

                if pressed(Controls.DOWN):
                    activateAnim(e_AlucardJumpingPunchDown)
                else:
                    activateAnim(e_AlucardJumpingPunchStraigt)

                animList.startHangup(2)

    def finishKill():
        shutDown()
        remove()

    def knockbackStandUp():         
        hurtBloodEmitter.finish()
        setControl(FALSE)
        activateAnim(e_AlucardStandUp)        
        if animations[e_AlucardStandUp].lastFrameOfLastSprite():
            global actionState
            actionState = 1
        else:
            moveH(1 if hurtHorizontalDirection == Direction.LEFT else -1)   

    def updateVariables():
        global isJumping
        global isRunning
        global nearFloor
        isJumping = components[VCOFF + 3]
        isRunning = pressed(Controls.RIGHT) or pressed(Controls.LEFT)
        nearFloor = distanceToFloor() < 5
        if not onServer():
            HUD.set("maxMana" , maxMana())
            HUD.set("maxLife" , maxLife())
            HUD.set("life" , currentLife())
            HUD.set("mana" , currentMana())

    def getActionState():
        global actionState

        if actionState == 7:
            actionState = 7

        elif actionState == 5:
            actionState = 5

        elif actionState == 6:
            actionState = 6

        elif not nearFloor or pressed(Controls.JUMP) : #in the air
            actionState = 3

        elif nearFloor and pressed(Controls.DOWN) :#crouching
            actionState = 2

        elif nearFloor and (pressed(Controls.POWER1) or actionState == 4):#back dashing
            actionState = 4

        elif nearFloor:
            actionState = 1 #walking

    def hurtBloodEmitterSetup():
        hurtBloodEmitter.start()  
        TemporalExecutor.forMillis(1000 , lambda: hurtBloodEmitter.setPosition(xMid() , yMid()))  

    def useItem(slot):
        equipped = inventory.getEquippedItem(slot)
        if equipped != None and equipped.has(ItemComponents.USABLE):
            equipped.use()

    def useSword():
        #play correct sword swing based on current state
        if actionState <= 2:
            activateAnim(e_AlucardStandingSword)
            startHangup(1)

        elif actionState == 3:
            activateAnim(e_AlucardJumpingSwordForward)
            startHangup(1)

    onHurt(onHurtImpl)
    onKill(onKillImpl)
    initialized = TRUE

updateVariables()
findItems()

#first set the action state, then call the correct bahavior
getActionState()

#should be impossible on server anyway
if struck(Controls.INVENTORY):
    HUD.toggle()

if actionState <= 3 and pressed(Controls.ATTACK1):
    useSword()
    useItem(0)

else:
    if actionState == 6:
        knockbackStandUp()
    elif actionState == 1:
        grounded()
    elif actionState == 2:
        crouch()
    elif actionState == 3:
        airborn() 
    elif(actionState == 4):
        backDash()

#handles unarmed attacks and accounts for state on its own
unarmed()