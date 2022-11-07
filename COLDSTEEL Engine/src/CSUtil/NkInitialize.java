package CSUtil;

import static org.lwjgl.nuklear.Nuklear.NK_UTF_INVALID;
import static org.lwjgl.nuklear.Nuklear.nk_buffer_init;
import static org.lwjgl.nuklear.Nuklear.nk_free;
import static org.lwjgl.nuklear.Nuklear.nk_init;
import static org.lwjgl.nuklear.Nuklear.nk_style_set_font;
import static org.lwjgl.nuklear.Nuklear.nnk_utf_decode;
import static org.lwjgl.opengl.GL11C.GL_LINEAR;
import static org.lwjgl.opengl.GL11C.GL_RGBA;
import static org.lwjgl.opengl.GL11C.GL_RGBA8;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL11C.glDeleteTextures;
import static org.lwjgl.opengl.GL11C.glGenTextures;
import static org.lwjgl.opengl.GL11C.glTexImage2D;
import static org.lwjgl.opengl.GL11C.glTexParameteri;
import static org.lwjgl.opengl.GL12C.GL_UNSIGNED_INT_8_8_8_8_REV;
import static org.lwjgl.stb.STBTruetype.stbtt_GetCodepointHMetrics;
import static org.lwjgl.stb.STBTruetype.stbtt_GetFontVMetrics;
import static org.lwjgl.stb.STBTruetype.stbtt_GetPackedQuad;
import static org.lwjgl.stb.STBTruetype.stbtt_InitFont;
import static org.lwjgl.stb.STBTruetype.stbtt_PackBegin;
import static org.lwjgl.stb.STBTruetype.stbtt_PackEnd;
import static org.lwjgl.stb.STBTruetype.stbtt_PackFontRange;
import static org.lwjgl.stb.STBTruetype.stbtt_PackSetOversampling;
import static org.lwjgl.stb.STBTruetype.stbtt_ScaleForPixelHeight;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.nmemAlloc;
import static org.lwjgl.system.MemoryUtil.nmemFree;
import static CSUtil.BigMixin.loadTTF;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import org.lwjgl.nuklear.NkAllocator;
import org.lwjgl.nuklear.NkBuffer;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkPluginAlloc;
import org.lwjgl.nuklear.NkPluginAllocI;
import org.lwjgl.nuklear.NkPluginFree;
import org.lwjgl.nuklear.NkPluginFreeI;
import org.lwjgl.nuklear.NkUserFont;
import org.lwjgl.nuklear.NkUserFontGlyph;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryStack;

public class NkInitialize {

	private static final NkAllocator ALLOCATOR;
	private static final NkPluginFree allocatorFree;
	private static final NkPluginAlloc allocatorAlloc;
	static {

		NkPluginAllocI allocCallback;
		allocCallback = (handle , oldData , size) -> nmemAlloc(size);
		allocatorAlloc = NkPluginAlloc.create(allocCallback);
		
		NkPluginFreeI freeCallback;
		freeCallback = (handle , free) -> nmemFree(free);
		allocatorFree = NkPluginFree.create(freeCallback);
		
		ALLOCATOR = NkAllocator.create().alloc(allocatorAlloc).mfree(allocatorFree);

	}

    private static final int BUFFER_INITIAL_SIZE = 4 * 1024;

    private NkContext ctx = NkContext.create();
    private NkBuffer cmds = NkBuffer.create();
	private ByteBuffer fontBytes;
	private NkUserFont NkFont = NkUserFont.create();
		
	public NkInitialize(){
		
        try {

            this.fontBytes = loadTTF(CS.COLDSTEEL.assets + "ui/FiraSans-Bold.ttf");
            
        } catch (IOException e) {

            throw new RuntimeException(e);

        }

	}
	
	private void setupFont(){

		int BITMAP_W = 1024;
	    int BITMAP_H = 1024;

	    int FONT_HEIGHT = 16;
	    int fontTexID = glGenTextures();

	    STBTTFontinfo fontInfo = STBTTFontinfo.create();
	    STBTTPackedchar.Buffer cdata = STBTTPackedchar.create(95);

	    float scale;
	    float descent;

	    stbtt_InitFont(fontInfo, fontBytes);
	    scale = stbtt_ScaleForPixelHeight(fontInfo, FONT_HEIGHT);
	        
	    try (MemoryStack stack = stackPush()) {

	    	IntBuffer typefaceDecentBelowScanLine = stack.mallocInt(1);
	        stbtt_GetFontVMetrics(fontInfo, null, typefaceDecentBelowScanLine, null);
	        descent = typefaceDecentBelowScanLine.get(0) * scale;

	        ByteBuffer bitmap = memAlloc(BITMAP_W * BITMAP_H);

	        STBTTPackContext pc = STBTTPackContext.malloc(stack);
	        stbtt_PackBegin(pc, bitmap, BITMAP_W, BITMAP_H, 0, 1, NULL);
	        stbtt_PackSetOversampling(pc, 1 , 1);
	        stbtt_PackFontRange(pc, fontBytes, 0, FONT_HEIGHT, 32, cdata);
	        stbtt_PackEnd(pc);

	        // Convert R8 to RGBA8
	        ByteBuffer texture = memAlloc(BITMAP_W * BITMAP_H * 4);

	        for (int i = 0; i < bitmap.capacity(); i++) texture.putInt((bitmap.get(i) << 24) | 0x00FFFFFF);

	        texture.flip();

	        glBindTexture(GL_TEXTURE_2D, fontTexID);
	        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, BITMAP_W, BITMAP_H, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, texture);
	        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

	        memFree(texture);
	        memFree(bitmap);

	    }

	    NkFont.width((handle, h, text, len) -> {

	        float text_width = 0;

	        try (MemoryStack stack = stackPush()) {

	            IntBuffer unicode = stack.mallocInt(1);
	            int glyph_len = nnk_utf_decode(text, memAddress(unicode), len);
	            int text_len = glyph_len;

	            if (glyph_len == 0) return 0;

	            IntBuffer advance = stack.mallocInt(1);

	            while (text_len <= len && glyph_len != 0) {

	            	if (unicode.get(0) == NK_UTF_INVALID) break;

	                 /* query currently drawn glyph information */
	                 stbtt_GetCodepointHMetrics(fontInfo, unicode.get(0), advance, null);
	                 text_width += advance.get(0) * scale;

	                 /* offset next glyph */
	                 glyph_len = nnk_utf_decode(text + text_len, memAddress(unicode), len - text_len);
	                 text_len += glyph_len;

	            }

	        }

	    return text_width;

	    }).height(FONT_HEIGHT).query((handle, font_height, glyph, codepoint, next_codepoint) -> {

		    try (MemoryStack stack = stackPush()) {
	
		        FloatBuffer x = stack.floats(0.0f);
		        FloatBuffer y = stack.floats(0.0f);
		        STBTTAlignedQuad q       = STBTTAlignedQuad.malloc(stack);
		        IntBuffer        advance = stack.mallocInt(1);
	
		        stbtt_GetPackedQuad(cdata, BITMAP_W, BITMAP_H, codepoint - 32, x, y, q, false);
		        stbtt_GetCodepointHMetrics(fontInfo, codepoint, advance, null);
	
		        NkUserFontGlyph ufg = NkUserFontGlyph.create(glyph);
	
		        ufg.width(q.x1() - q.x0());
		        ufg.height(q.y1() - q.y0());
		        ufg.offset().set(q.x0(), q.y0() + (FONT_HEIGHT + descent));
		        ufg.xadvance(advance.get(0) * scale);
		        ufg.uv(0).set(q.s0(), q.t0());
		        ufg.uv(1).set(q.s1(), q.t1());
	
		    }

	    }).texture(it -> it.id(fontTexID));
	     
	}

	public NkContext initNKGUI(){

		setupFont();
    	nk_init(ctx, ALLOCATOR, null);
    	nk_buffer_init(cmds, ALLOCATOR, BUFFER_INITIAL_SIZE);
		nk_style_set_font(ctx, NkFont);

		return ctx;

	}

    public void shutDown() {

    	System.out.println("Shutting down Nuklear Initializer...");

        glDeleteTextures(NkFont.texture().id());
        nk_free(ctx);
        
        Objects.requireNonNull(NkFont.query()).free();
        Objects.requireNonNull(NkFont.width()).free();

        System.out.println("Nuklear Initializer shut down.");

    }

    public void shutDownAllocator(){

    	System.out.println("Shutting down Nuklear Allocator...");

    	Objects.requireNonNull(ALLOCATOR.alloc()).free();
        Objects.requireNonNull(ALLOCATOR.mfree()).free();

        System.out.println("Nuklear Allocator shut down.");

    }

	public NkBuffer getCommands() {

		return cmds;

	}

	public NkContext getContext() {

		return ctx;

	}

}
