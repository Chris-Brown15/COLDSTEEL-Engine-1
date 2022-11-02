from Core import ScriptedUserInterface
from org.lwjgl.nuklear.Nuklear import NK_STATIC
from org.lwjgl.nuklear.Nuklear import NK_TEXT_ALIGN_CENTERED
from org.lwjgl.nuklear.Nuklear import NK_TEXT_ALIGN_MIDDLE
from org.lwjgl.nuklear.Nuklear import NK_TEXT_ALIGN_LEFT
from org.lwjgl.nuklear.Nuklear import NK_TEXT_ALIGN_RIGHT
from org.lwjgl.nuklear.Nuklear import NK_TEXT_ALIGN_TOP
from org.lwjgl.nuklear.Nuklear import NK_WINDOW_BORDER
from org.lwjgl.nuklear.Nuklear import NK_WINDOW_MINIMIZABLE
from org.lwjgl.nuklear.Nuklear import NK_WINDOW_MINIMIZED
from org.lwjgl.nuklear.Nuklear import NK_WINDOW_MOVABLE
from org.lwjgl.nuklear.Nuklear import NK_WINDOW_NO_SCROLLBAR
from org.lwjgl.nuklear.Nuklear import NK_WINDOW_SCALABLE
from org.lwjgl.nuklear.Nuklear import NK_CHART_LINES
from org.lwjgl.nuklear.Nuklear import NK_WINDOW_TITLE
from org.lwjgl.nuklear.Nuklear import nk_begin
from org.lwjgl.nuklear.Nuklear import nk_button_label
from org.lwjgl.nuklear.Nuklear import nk_color_pick
from org.lwjgl.nuklear.Nuklear import nk_end
from org.lwjgl.nuklear.Nuklear import nk_group_begin
from org.lwjgl.nuklear.Nuklear import nk_group_end
from org.lwjgl.nuklear.Nuklear import nk_label
from org.lwjgl.nuklear.Nuklear import nk_layout_row_begin
from org.lwjgl.nuklear.Nuklear import nk_layout_row_dynamic
from org.lwjgl.nuklear.Nuklear import nk_layout_row_end
from org.lwjgl.nuklear.Nuklear import nk_layout_row_push
from org.lwjgl.nuklear.Nuklear import nk_prog
from org.lwjgl.nuklear.Nuklear import nk_property_float
from org.lwjgl.nuklear.Nuklear import nk_property_int
from org.lwjgl.nuklear.Nuklear import nk_radio_label
from org.lwjgl.nuklear.Nuklear import nk_radio_text
from org.lwjgl.nuklear.Nuklear import nk_text
from org.lwjgl.nuklear.Nuklear import nk_checkbox_label
from org.lwjgl.nuklear.Nuklear import nk_text_wrap
from org.lwjgl.nuklear.Nuklear import nk_plot
from org.lwjgl.nuklear.Nuklear import nk_widget_is_hovered
from org.lwjgl.nuklear.Nuklear import nk_selectable_text
from org.lwjgl.nuklear.Nuklear import nk_image
from org.lwjgl.nuklear.Nuklear import nk_layout_row_static
from org.lwjgl.nuklear.Nuklear import NK_TEXT_ALIGN_TOP
from org.lwjgl.nuklear.Nuklear import nk_popup_begin
from org.lwjgl.nuklear.Nuklear import nk_popup_close
from org.lwjgl.nuklear.Nuklear import nk_popup_end
from org.lwjgl.nuklear.Nuklear import NK_POPUP_STATIC
from org.lwjgl.nuklear.Nuklear import nk_button_image

from CS import Engine

context = lib.getContext()
console = lib.getConsole()

'''
returns a Tuple2 containing an NkImage and NkRect which can be used for its dimensions
'''
def itemIconAsImageSubRegion(item):
    return lib.itemIconAsImageSubRegion(item)

def setToggleImpl(toggleCode):
    UI.setToggleImpl(toggleCode)

def isServer():
    return lib.isServer()