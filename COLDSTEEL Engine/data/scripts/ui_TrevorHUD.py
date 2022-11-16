if not initialized:

	from CSUtil.DataStructures import CSLinked

	options = NK_WINDOW_BORDER|NK_WINDOW_NO_SCROLLBAR
	subWeapons = CSLinked()
	subWeaponIcons = CSLinked()
	equippedItemIcon = None

	def addSubWeapon(subweapon):
		subWeapons.add(subweapon)
		subWeaponIcons.add(itemIconAsImageSubRegion(subweapon))

	def updateItemIcon():
		global equippedItemIcon
		equippedItemIcon = itemIconAsImageSubRegion(equippedItem)
		
	inventoryUI = ScriptedUserInterface("Trevor's Inventory" , 5 , 465 , 400 , 400 , options , options)

	def toggleImpl():
		inventoryUI.toggle()

	setToggleImpl(toggleImpl)

	def inventoryBody(stackFrame):

		nk_layout_row_dynamic(context , 20 , 1)
		nk_text(context , "Life: " + str(life) + " / " + str(maxLife) , NK_TEXT_ALIGN_MIDDLE|NK_TEXT_ALIGN_CENTERED)
		nk_layout_row_dynamic(context , 70 , 1)
		nk_prog(context , long(life) , long(maxLife) , FALSE)

		nk_layout_row_dynamic(context , 10 , 1)
		nk_layout_row_dynamic(context , 50 , 5)
		nk_text(context , "Hearts: " + str(hearts) , NK_TEXT_ALIGN_CENTERED|NK_TEXT_ALIGN_MIDDLE)
		
		node = subWeaponIcons.get(0)
		for i in range(subWeaponIcons.size()):
			nk_image(context , node.val.getFirst())
			node = node.next

	inventoryUI.setLayout(inventoryBody)
	initialized = TRUE