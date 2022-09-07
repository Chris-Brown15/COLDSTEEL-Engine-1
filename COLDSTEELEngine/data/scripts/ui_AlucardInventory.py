if not initialized:
    
    from Game.Items import ItemComponents
    from CSUtil.DataStructures import cdNode
    
    options = NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_TITLE    
    imageData = getImage(assets + "/ui/Alucard.png")    
    
    rect = newRect(5 , 465 , 600 , 400)
    name = "Inventory"
    
    imageRect = newRect(5 , 5 , 300 , 200)
    equipedRect = newRect(305 , 5 , 285 , 200)
    
    healthBarRect = newRect(5 , 5 , 450 , 200)
    healthBarOptions = NK_WINDOW_MOVABLE|NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_BORDER

    initialized = 1

if nk_begin(context , name , rect , options):
            
    nk_layout_row_static(context , 200 , 295 , 2)    
    nk_image(context , imageData.image())
    
    nk_group_begin(context , "Equipped Items" , NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_TITLE)

    for i in range(16):
                
        if i % 4 == 0:
            nk_layout_row_dynamic(context , 35 , 4)

        if equipped.get(i) != None:
            if nk_button_label(context , equipped.get(i).name()):
                inventory.unequip(i)
                break
        else:
            if nk_button_label(context , ""):
                pass
    
    nk_group_end(context)

    nk_layout_row_dynamic(context , 5 , 1)
        
    #items in inventory
    with UIMemoryStack() as stack:
    
        node = items.get(0)
        for i in range(inventory.inventorySize()):
                
            currentItem = node.val.getFirst()
            if i % inventory.xDimension() == 0:
                nk_layout_row_dynamic(context , 30 , inventory.xDimension())

            if nk_selectable_text(context , currentItem.name() + "(" + node.val.getSecond().toString() + ")", NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED , stack.bytes(1)):
                if currentItem.has(ItemComponents.EQUIPPABLE):
                    inventory.equip(currentItem)
                elif currentItem.has(ItemComponents.USABLE):
                    currentItem.use()
                break
            
            node = node.next

nk_end(context)

if nk_begin(context , "" , healthBarRect , healthBarOptions):

    nk_layout_row_dynamic(context , 20 , 1)
    nk_text(context , "Life: " + toStr(life) + " / " + toStr(maxLife) , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED)
    
    nk_layout_row_dynamic(context , 50 , 1)
    nk_prog(context , long(life) , long(maxLife) , FALSE)
    
    nk_layout_row_dynamic(context , 20 , 1)
    nk_text(context , "Mana: " + toStr(mana) + " / " + toStr(maxMana) , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED)
    
    nk_layout_row_dynamic(context , 50 , 1)
    nk_prog(context , long(mana) , long(maxMana) , FALSE)
    
nk_end(context)