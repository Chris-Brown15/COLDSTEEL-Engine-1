if not initialized:

	from Game.Items import ItemComponents
	from CSUtil.DataStructures import cdNode
	options = NK_WINDOW_BORDER|NK_WINDOW_MOVABLE|NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_TITLE

	inventoryUI = ScriptedUserInterface("Alucard's Inventory" , 5 , 465 , 400 , 400 , options , options)
	
	def toggleImpl():
		inventoryUI.toggle()

	setToggleImpl(toggleImpl)

	def inventoryBody(stackFrame):

		#nk_layout_row_static(context , 200 , 295 , 2)
		#nk_image(context , imageData.image())

		nk_layout_row_dynamic(context , 30 , 1)
		nk_text(context , "Equipped Items" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED)

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
	
		nk_layout_row_dynamic(context , 5 , 1)

		nk_layout_row_dynamic(context , 30 , 1)
		nk_text(context , "Inventory" , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED)

		#items in inventory
		node = items.get(0)
		for i in range(inventory.inventorySize()):

			currentItem = node.val.getFirst()
			if i % inventory.xDimension() == 0:
				nk_layout_row_dynamic(context , 30 , inventory.xDimension())

			if nk_selectable_text(context , currentItem.name() + "(" + node.val.getSecond().toString() + ")", NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED , stackFrame.bytes(1)):
				if currentItem.has(ItemComponents.EQUIPPABLE):
					inventory.equip(currentItem)
				elif currentItem.has(ItemComponents.USABLE):
					currentItem.use()
				break

			node = node.next

		
	inventoryUI.setLayout(inventoryBody)
	LSMOptions = NK_WINDOW_MOVABLE|NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_BORDER
	LSMView = ScriptedUserInterface("AlucardLSMView" , 5 , 5 , 450 , 150 , LSMOptions , LSMOptions)

	maxLife = 0
	maxMana = 0
	life = 0
	mana = 0

	def LSMBody(stackFrame):

		#nk_layout_row_dynamic(context , 20 , 1)
		#nk_text(context , "Life: " + toStr(life) + " / " + toStr(maxLife) , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED)

		nk_layout_row_dynamic(context , 20 , 1)
		nk_text(context , "Life" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_CENTERED)

		nk_layout_row_dynamic(context , 30 , 1)
		nk_prog(context , long(life) , long(maxLife) , FALSE)

		#nk_layout_row_dynamic(context , 20 , 1)
		#nk_text(context , "Mana: " + toStr(mana) + " / " + toStr(maxMana) , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED)

		nk_layout_row_dynamic(context , 20 , 1)
		nk_text(context , "Mana" , NK_TEXT_ALIGN_LEFT|NK_TEXT_ALIGN_CENTERED)

		nk_layout_row_dynamic(context , 30 , 1)
		nk_prog(context , long(mana) , long(maxMana) , FALSE)

	LSMView.setLayout(LSMBody)

	LSMView.show()
	inventoryUI.show()

	initialized = TRUE