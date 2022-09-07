if not initialized:

	healthBarOptions = NK_WINDOW_MOVABLE|NK_WINDOW_NO_SCROLLBAR|NK_WINDOW_BORDER
	imageData = getImage(assets + "/Fonts_UI/MariaFace.png")

	rect = rect = newRect(5 , 5 , 400 , 300)

	initialized = TRUE

if nk_begin(context , "Maria Health Bar" , rect , healthBarOptions):

	nk_layout_row_begin(context , NK_STATIC , 290 , 2)
	nk_layout_row_push(context , imageData.width() / 2)
	nk_image(context , imageData.image())
	nk_layout_row_push(context , 100)
	nk_prog(context , 100 , 100 , FALSE)
	nk_layout_row_end(context)

nk_end(context)
