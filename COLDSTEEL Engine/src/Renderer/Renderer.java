package Renderer;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.nuklear.Nuklear.NK_ANTI_ALIASING_ON;
import static org.lwjgl.nuklear.Nuklear.NK_FORMAT_COUNT;
import static org.lwjgl.nuklear.Nuklear.NK_FORMAT_FLOAT;
import static org.lwjgl.nuklear.Nuklear.NK_FORMAT_R8G8B8A8;
import static org.lwjgl.nuklear.Nuklear.NK_VERTEX_ATTRIBUTE_COUNT;
import static org.lwjgl.nuklear.Nuklear.NK_VERTEX_COLOR;
import static org.lwjgl.nuklear.Nuklear.NK_VERTEX_POSITION;
import static org.lwjgl.nuklear.Nuklear.NK_VERTEX_TEXCOORD;
import static org.lwjgl.nuklear.Nuklear.nk__draw_begin;
import static org.lwjgl.nuklear.Nuklear.nk__draw_next;
import static org.lwjgl.nuklear.Nuklear.nk_buffer_free;
import static org.lwjgl.nuklear.Nuklear.nk_buffer_init_fixed;
import static org.lwjgl.nuklear.Nuklear.nk_clear;
import static org.lwjgl.nuklear.Nuklear.nk_convert;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_INVALID_ENUM;
import static org.lwjgl.opengl.GL11.GL_INVALID_OPERATION;
import static org.lwjgl.opengl.GL11.GL_INVALID_VALUE;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_OUT_OF_MEMORY;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_STACK_OVERFLOW;
import static org.lwjgl.opengl.GL11.GL_STACK_UNDERFLOW;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_SHORT;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL11.glScissor;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL14.GL_FUNC_ADD;
import static org.lwjgl.opengl.GL14.glBlendEquation;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glMapBuffer;
import static org.lwjgl.opengl.GL15.glUnmapBuffer;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL31.GL_TEXTURE_BUFFER;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Objects;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.nuklear.NkBuffer;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkConvertConfig;
import org.lwjgl.nuklear.NkDrawCommand;
import org.lwjgl.nuklear.NkDrawNullTexture;
import org.lwjgl.nuklear.NkDrawVertexLayoutElement;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import CS.Engine;
import CS.GLFWWindow;
import CS.RuntimeState;
import CSUtil.Timer;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.CSStack;
import Core.CSType;
import Core.Quads;
import Core.Statics.Statics;
import Game.Levels.MacroLevels;
import Physics.ColliderLists;

public class Renderer {

	private final Thread nuklearRenderThread = new Thread(new Runnable() {
		
		@Override public void run(){
		
			renderUI();
			
		}
		
	});
	
	private static final CSLinked<Textures> LOADED_TEXTURES = new CSLinked<Textures>();
	private Textures previousTexture = null;
	
	private Camera camera = new Camera(new Vector2f(0 , 0));
	private Shader shader = new Shader();
	
    private int VAOID;
    private int VBOID;
    private int EBOID;
    
    private FloatBuffer vertexBuffer;
	private IntBuffer elementBuffer;
	private int drawCalls = 0;

	private boolean renderOthers = true;
	private ArrayList<Particle> backgroundParticles = new ArrayList<Particle>();
	private ArrayList<Particle> foregroundParticles = new ArrayList<Particle>();
	private ArrayList<Quads> others = new ArrayList<Quads>();
	private ArrayList<float[]> raw = new ArrayList<float[]>();
	private ArrayList<Quads> finals = new ArrayList<Quads>();

    private double renderTime;
    private double renderMilliCounter = 0;
    private Timer renderTimer = new Timer();

    public final Quads screenQuad = new Quads(-1);
    
    private static final CSStack<Quads> BACKGROUND_QUAD_DRAW_COMMANDS = new CSStack<Quads>();
    private static final CSStack<float[]> BACKGROUND_ARRAY_DRAW_COMMANDS = new CSStack<float[]>();
    private static final CSStack<Quads> FOREGROUND_QUAD_DRAW_COMMANDS = new CSStack<Quads>();
    private static final CSStack<float[]> FOREGROUND_ARRAY_DRAW_COMMANDS = new CSStack<float[]>();

	private static boolean checkErrors() {
		
		int errorCode;
		boolean error = false;
		while((errorCode = glGetError()) != GL_NO_ERROR) switch(errorCode) {
		
			case GL_INVALID_ENUM -> { 
				
				System.err.println("GL error thrown; Invalid Enum:");
				error = true;
				
			}
			
			case GL_INVALID_VALUE -> { 
				
				System.err.println("GL error thrown; Invalid Value:");
				error = true;
				
			}
			
			case GL_INVALID_OPERATION -> { 
				
				System.err.println("GL error thrown; Invalid Operation:");
				error = true;
				
			}
			
			case GL_STACK_OVERFLOW -> { 
				
				System.err.println( "GL error thrown; Stack Overflow:");
				error = true;
				
			}
			
			case GL_STACK_UNDERFLOW -> { 
				
				System.err.println("GL error thrown; Stack Underflow:");
				error = true;
				
			}
			
			case GL_OUT_OF_MEMORY -> { 
				
				System.err.println("GL error thrown; Out Of Memory:");
				error = true;
				
			}
	
		}	
		
		return error;
		
	}
	
	public static final Textures loadTexture(String filepath) {
		
		if(filepath == null || filepath.equals("null")) return null;
		
		Textures newTexture = new Textures(filepath);
		newTexture.initialize();
		LOADED_TEXTURES.add(newTexture);
		if(checkErrors()) throw new IllegalStateException("GL Error thrown on call to loadTexture. Parameters: " + filepath);
		return newTexture;
		
	}

	public static final void addTexture(Textures newTexture) {
		
		LOADED_TEXTURES.add(newTexture);
		if(checkErrors()) throw new IllegalStateException("GL Error thrown on call to addTexture. Parameters: " + newTexture.toString());
		
	}
		
    public static void draw_background(Quads drawThis) {
    	
    	Objects.requireNonNull(drawThis);
    	BACKGROUND_QUAD_DRAW_COMMANDS.push(drawThis);
    	
    }
    
    public static void draw_foreground(Quads drawThis) {
    	
    	Objects.requireNonNull(drawThis);
    	FOREGROUND_QUAD_DRAW_COMMANDS.push(drawThis);
    	
    }
	
    public static void draw_background(float[] drawThis) {
    	
    	Objects.requireNonNull(drawThis);
    	BACKGROUND_ARRAY_DRAW_COMMANDS.push(drawThis);
    	
    }
    
    public static void draw_foreground(float[] drawThis) {
    	
    	Objects.requireNonNull(drawThis);
    	FOREGROUND_ARRAY_DRAW_COMMANDS.push(drawThis);
    	
    }
    
    /**
     * To free a macro level, we free all textures pushed onto the macro level's loaded texture stack. This stack represents
     * the indices of the loaded textures so we will first populate a linked list with the textures, then free them all while 
     * removing then from the static list of textures.
     * 
     * @param freeThis � the macro level to free
     */
    public static void freeMacroLevel(MacroLevels freeThis) {
    	
    	while(!freeThis.loadedTextures().empty()) LOADED_TEXTURES.removeVal(freeThis.loadedTextures().pop()).val.shutDown();
    	
    }
    
    public static void removeTexture(int index) {
    	
    	LOADED_TEXTURES.removeVal(index).shutDown();
    	
    }
    
	public int getNumberDrawCalls(){

		return drawCalls + numberNuklearDrawCalls;

	}

    static final int[] elementArray = {

            2 , 1 , 0 ,
            0 , 1 , 3
    };
    
	private FloatBuffer initializeVAO() {

		// allocate VBO
		VAOID = glGenVertexArrays();
		vertexBuffer = memAllocFloat(36);

		//activate VBO
		glBindVertexArray(VAOID);
		
		return vertexBuffer;

	}

	private void initializeVBO() {

		// Create VBO upload the vertex buffer
	    VBOID = glGenBuffers();
	    glBindBuffer(GL_ARRAY_BUFFER, VBOID);
	    glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_DYNAMIC_DRAW);
	    
	}

	private IntBuffer initializeEBO() {

		EBOID = glGenBuffers();
	    // allocate EBO
	    elementBuffer = memAllocInt(6);

	    //allocate buffer
	    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBOID);
	    glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_DYNAMIC_DRAW);
	    return elementBuffer;

	}

	private void initializeVertexAttribs(){

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
	    //texture coordinate attribute
	    glVertexAttribPointer(2 , UVSize, GL_FLOAT, false, vertexSizeBytes, (positionSize + colorSize) * Float.BYTES);

	}

	private void initializeTextures() {

		stbi_set_flip_vertically_on_load(true);

    	//Four below lines set up options for textures to be adhered to
    	//texture wrap parameters
    	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
    	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
    	//texture fill parameters
    	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

	}

	private void setOpenGLState(){

		glEnable(GL_BLEND);
		glEnable(GL_SCISSOR_TEST);
		glBlendEquation(GL_FUNC_ADD);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

	}

    private void setVertexAttribPointersScene(){

	    glVertexAttribPointer(0, positionSize, GL_FLOAT, false, vertexSizeBytes, 0);
	    glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes , positionSize * Float.BYTES);

    }

	public void initialize(GLFWWindow window){

		System.out.println("Beginning Renderer initialization...");
		this.glfw = window.getGlfwWindow();
		shader.initializeShader();
		shader.activate();
		initializeVAO();
		initializeVBO();
		initializeEBO();
		initializeTextures();
		initializeVertexAttribs();
		setOpenGLState();
		initializeNuklear();
		if(checkErrors()) throw new IllegalStateException("GL Error thrown on call to initialize. Parameters: " + window.toString()); 
		
		
		int[] dims = window.getWindowDimensions();		
		screenQuad.moveTo(camera.cameraPosition);		
		screenQuad.setWidth(dims[0]);
		screenQuad.setHeight(dims[1]);
		screenQuad.makeTranslucent(0.0f);
		
		System.out.println("Renderer initialization complete.");

	}
	
    private FloatBuffer updateVAO(float [] target) {

    	vertexBuffer.clear();
		vertexBuffer.put(target).flip();

		return vertexBuffer;

	}

	private void updateVBO() {

		 glBufferSubData(GL_ARRAY_BUFFER , 0 , vertexBuffer);

	}

	private void updateEBO() {

	    glBufferSubData(GL_ELEMENT_ARRAY_BUFFER , 0 , elementArray);

	}

	public void addToOthers(Quads addThis){

		others.add(addThis);
		
	}

	public void removeFromOthers(Quads object){

		others.remove(object);
		
	}
	
	public void addToForegroundParticles(Particle p) {
		
		foregroundParticles.add(p);
		
	}

	public void addToBackgroundParticles(Particle p) {
		
		backgroundParticles.add(p);
		
	}

	public void removeFromForegroundParticles(Particle p) {
		
		foregroundParticles.remove(p);
		
	}

	public void removeFromBackgroundParticles(Particle p) {
		
		backgroundParticles.remove(p);
		
	}
	
	public void addToFinals(Quads addThis) {
		
		finals.add(addThis);
		
	}
	
	public void addToFinalsSafe(Quads addThis) {
		
		if(!finals.contains(addThis)) finals.add(addThis);
		
	}

	public void removeFromFinals(Quads removeThis) {
		
		finals.remove(removeThis);
		
	}
	
	public void addToRawData(float[] addThis) {
		
		
		raw.add(addThis);
				
	}
	
	public void removeFromRawData(float[] removeThis) {
		
		raw.remove(removeThis);
		
	}
	
	public boolean rawHas(float[] array) {
		
		return raw.contains(array);
		
	}
	
	public void rawReplaceWith(int replaceIndex , float[] with) {
		
		raw.add(replaceIndex, with);
		raw.remove(replaceIndex + 1);
		
	}
	
	public void toggelRenderOthers(){

		renderOthers = renderOthers ? false:true;

	}

	private void reRender(Quads target) {

		updateVAO(target.getData());
		updateVBO();
		updateEBO();

	}

	private void reRender(float[] target) {

		updateVAO(target);
		updateVBO();
		updateEBO();

	}
		
    public void drawQuad(Quads target) {

    	if(!target.getShouldRender()) return;
    	if(target.isTextured()) {
    		
    		Textures texture = target.getTexture();
    		
    		if(texture != null) {
    			
    			shader.uniformInt("mode", 1);
    			
    			if(previousTexture != texture) {

    				texture.activate(0);
    				previousTexture = texture;
    				
    			}
    			
    		}
    		
    	} else shader.uniformInt("mode" , 0);
    			
    	shader.uniformMatrix4("translation", target.translation());
    	shader.uniformMatrix4("rotation", target.rotation());
		shader.uniformVector3("removed" , target.getRemovedColor());
		shader.uniformVector3("RGBAdd" , target.getFilter());
    	reRender(target);
    	glDrawElements(GL_TRIANGLES, 6 , GL_UNSIGNED_INT , 0);
    	drawCalls++;

    }

    private void drawStatic(Statics target) {

    	if(!target.getShouldRender()) return;    	
    	if(target.hasParallax()) shader.uniformMatrix4("uView", camera.getViewMatrix(target.getViewOffsetX(), target.getViewOffsetY()));
    	else shader.uniformMatrix4("uView", camera.getViewMatrix());
    	
    	if(target.isTextured()) {
    		
    		Textures texture = target.getTexture(); 
    		if(texture != null) {
    			
    			shader.uniformInt("mode", 1);    	
    			
    			if(previousTexture != texture) {
    				
    				texture.activate(0);
    				previousTexture = texture;
    				
    			}
    			
    		}
    		
    	} else shader.uniformInt("mode" , 0);
    			    		    	    	
		shader.uniformVector3("removed" , target.getRemovedColor());
		shader.uniformVector3("RGBAdd" , target.getFilter());
    	reRender(target);
    	glDrawElements(GL_TRIANGLES, 6 , GL_UNSIGNED_INT , 0);
    	drawCalls++;

    }
    
    private Vector3f noColors = new Vector3f(2.0f , 2.0f , 2.0f);
    
    private void drawData(float[] data) {
    	
    	shader.uniformMatrix4("uView", camera.getViewMatrix());
		shader.uniformInt("mode", 0);
		shader.uniformVector3("colorFilter" , noColors);		
    	reRender(data);
    	glDrawElements(GL_TRIANGLES, 6 , GL_UNSIGNED_INT , 0);
    	drawCalls++;
    			
    }
    
    private void drawFadeToBlack() {
    	
    	shader.uniformMatrix4("uView", new Matrix4f().identity());
    	drawQuad(screenQuad);
    	
    }
    
    public double getRenderTime(){

    	return renderTime;

    }

    private void drawByType(Quads quad , CSType type) {
    	
    	switch(type) {
    		
			case STATIC -> drawStatic((Statics)quad);			
			default -> drawQuad(quad);
    	
    	}
    	
    }
    
    public void run() {

    	drawCalls = 0;
    	
    	renderTimer.start();
    	previousTexture = null;    	
    	
    	if(Engine.secondPassed()) {
    		
    		renderTime = renderMilliCounter/ Engine.framesLastSecond(); //renders time per frame average
    		renderMilliCounter = 0;
    		
    	}

    	setVertexAttribPointersScene();
    	shader.uniformMatrix4("uProjection", camera.getProjectionMatrix());
    	shader.uniformMatrix4("uView", camera.getViewMatrix());
    	
    	while(!BACKGROUND_QUAD_DRAW_COMMANDS.empty()) drawQuad(BACKGROUND_QUAD_DRAW_COMMANDS.pop());
    	while(!BACKGROUND_ARRAY_DRAW_COMMANDS.empty()) drawData(BACKGROUND_ARRAY_DRAW_COMMANDS.pop());
    	
    	backgroundParticles.forEach(particle -> drawQuad(particle));
    	
    	Engine.forEachObjectList(list -> list.forEach(instanceOfQuads -> drawByType(instanceOfQuads , list.TYPE)));
    	
    	if(renderOthers) {
    		
    		if(Engine.STATE == RuntimeState.EDITOR && ColliderLists.shouldRender()) ColliderLists.forAll(x -> drawQuad(x));
    		
			for(Quads x : others) drawQuad(x);			
    		for(float[] x : raw) drawData(x);	
			
    	}
    	
    	foregroundParticles.forEach(particle -> drawQuad(particle));
    	
    	while(!FOREGROUND_QUAD_DRAW_COMMANDS.empty()) drawQuad(FOREGROUND_QUAD_DRAW_COMMANDS.pop());
    	while(!FOREGROUND_ARRAY_DRAW_COMMANDS.empty()) drawData(FOREGROUND_ARRAY_DRAW_COMMANDS.pop());
    	
    	nuklearRenderThread.run();
    	
    	setVertexAttribPointersScene();
    	shader.uniformMatrix4("uProjection", camera.getProjectionMatrix());
    	shader.uniformMatrix4("uView", camera.getViewMatrix());
   	
    	drawFadeToBlack();
    	
    	for(Quads x : finals) drawQuad(x);
    	
    	renderMilliCounter += renderTimer.getElapsedTimeMillis();		
    	
    }
    
    public int getNumberNuklearDrawCalls() {

		return numberNuklearDrawCalls;

	}

	public Camera getCamera(){

		return camera;

	}

    /*
     * ______________________________________________________
     * |													|
     * |													|
     * |					Nuklear phase					|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */

	private int uniform_tex;
    private int uniform_view;
    private int uniform_proj;
    private int uiMode;

    private Matrix4f identityMatrix = new Matrix4f().identity();   
    private long glfw;
    
    /*
     * ______________________________________________________
     * |													|
     * |													|
     * |				Initialization phase				|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */
    
    static int positionSize = 3;
    static int colorSize = 4;
    static int UVSize = 2;
    static int vertexSizeBytes = (positionSize + colorSize + UVSize) * Float.BYTES;
    private static final int MAX_VERTEX_BUFFER  = 512 * 1024;
    private static final int MAX_ELEMENT_BUFFER = 128 * 1024;

    private static NkContext context;
    private static NkBuffer commands;
    
    private static final NkDrawVertexLayoutElement.Buffer VERTEX_LAYOUT;
    static {

        VERTEX_LAYOUT = NkDrawVertexLayoutElement.create(4)
            .position(0).attribute(NK_VERTEX_POSITION).format(NK_FORMAT_FLOAT).offset(0)
            .position(2).attribute(NK_VERTEX_COLOR).format(NK_FORMAT_R8G8B8A8).offset(positionSize * Float.BYTES)
            .position(1).attribute(NK_VERTEX_TEXCOORD).format(NK_FORMAT_FLOAT).offset((positionSize + colorSize) * Float.BYTES)
            .position(3).attribute(NK_VERTEX_ATTRIBUTE_COUNT).format(NK_FORMAT_COUNT).offset(0)
            .flip();
     
    }

    int numberNuklearDrawCalls = 0;
       
    private FloatBuffer viewBuffer = BufferUtils.createFloatBuffer(16);
    private FloatBuffer projectionBuffer = BufferUtils.createFloatBuffer(16);
    private NkDrawNullTexture null_texture = NkDrawNullTexture.create();

    private int width , height;
    private int display_width , display_height;

    private void initializeNuklear(){

        context = Engine.NuklearContext();
        commands = Engine.NuklearDrawCommands();
        
    	// null texture setup
        int nullTexID = glGenTextures();

        null_texture.texture().id(nullTexID);
        null_texture.uv().set(0.5f, 0.5f);
        setNuklearWidthHeight();
        
        glBindTexture(GL_TEXTURE_2D, nullTexID);

        try (MemoryStack stack = stackPush()) {

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 1, 1, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, stack.ints(0xFFFFFFFF));
            
        }

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
     
        uniform_tex = glGetUniformLocation(shader.shaderProgramID, "TEX_SAMPLER");
        uniform_view = glGetUniformLocation(shader.shaderProgramID , "uView");
        uniform_proj = glGetUniformLocation(shader.shaderProgramID, "uProjection");
        uiMode = glGetUniformLocation(shader.shaderProgramID , "mode");
        identityMatrix.get(viewBuffer);        
      
        glUniform1i(uniform_tex, 0);
              
        projectionBuffer.put(new float[] {
        		
                2.0f / width  ,  0.0f 		   ,  0.0f , 0.0f,
                0.0f		  , -2.0f / height ,  0.0f , 0.0f,
                0.0f		  ,  0.0f		   , -1.0f , 0.0f,
               -1.0f		  ,  1.0f		   ,  0.0f , 1.0f
       
        		
   		});
        
    }

    /*
     * ______________________________________________________
     * |													|
     * |													|
     * |				Rendering phase						|
     * |													|
     * |													|
     * �뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗뗗�
     *
     */

	private void setNuklearWidthHeight() {

    	try (MemoryStack stack = stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);

            glfwGetWindowSize(glfw, w, h);
            width = w.get(0);
            height = h.get(0);

            glfwGetFramebufferSize(glfw, w, h);
            display_width = w.get(0);
            display_height = h.get(0);

        }

    }

    private void renderUI() {
    	
    	try (MemoryStack stack = stackPush()) {
        
    		IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);

            glfwGetWindowSize(glfw, w, h);
            width = w.get(0);
            height = h.get(0);

            glfwGetFramebufferSize(glfw, w, h);
            display_width = w.get(0);
            display_height = h.get(0);

            glUniformMatrix4fv(uniform_view , false , viewBuffer);
    		glUniform1i(uiMode , 2);

            glUniform1i(uniform_tex , 0);
            glUniformMatrix4fv(uniform_proj, false, stack.floats(
            		
                   2.0f / width  ,  0.0f 		  ,  0.0f , 0.0f,
                   0.0f		  	 , -2.0f / height ,  0.0f , 0.0f,
                   0.0f		  	 ,  0.0f		  , -1.0f , 0.0f,
                  -1.0f		  	 ,  1.0f		  ,  0.0f , 1.0f
                       		
       		));
            
        }
    	
    	//reset Vertex attribs for Nuklear
    	glVertexAttribPointer(0, 2, GL_FLOAT, false, vertexSizeBytes, 0);
        glVertexAttribPointer(1, 4, GL_UNSIGNED_BYTE, true, vertexSizeBytes, positionSize * Float.BYTES);

        //setup and prepare for drawing
        // convert from command queue into draw list and draw to screen
        glBufferData(GL_ARRAY_BUFFER, MAX_VERTEX_BUFFER, GL_STREAM_DRAW);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, MAX_ELEMENT_BUFFER, GL_STREAM_DRAW);

        // load draw vertices & elements directly into vertex + element buffer
        ByteBuffer vertices = Objects.requireNonNull(glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY, MAX_VERTEX_BUFFER, null));
        ByteBuffer elements = Objects.requireNonNull(glMapBuffer(GL_ELEMENT_ARRAY_BUFFER, GL_WRITE_ONLY, MAX_ELEMENT_BUFFER, null));

        try (MemoryStack stack = stackPush()) {
            // fill convert configuration
            NkConvertConfig config = NkConvertConfig.calloc(stack)
                .vertex_layout(VERTEX_LAYOUT)
                .vertex_size(36)
                .vertex_alignment(4)
                .null_texture(null_texture)
                .circle_segment_count(22)
                .curve_segment_count(22)
                .arc_segment_count(22)
                .global_alpha(1.0f)
                .shape_AA(NK_ANTI_ALIASING_ON)
                .line_AA(NK_ANTI_ALIASING_ON);

            // setup buffers to load vertices and elements
            NkBuffer vbuf = NkBuffer.malloc(stack);
            NkBuffer ebuf = NkBuffer.malloc(stack);

            nk_buffer_init_fixed(vbuf, vertices/*, max_vertex_buffer*/);
            nk_buffer_init_fixed(ebuf, elements/*, max_element_buffer*/);
            nk_convert(context , commands , vbuf , ebuf , config);
            
        }

        glUnmapBuffer(GL_ELEMENT_ARRAY_BUFFER);
        glUnmapBuffer(GL_ARRAY_BUFFER);
        
        shader.uniformMatrix4("rotation", identityMatrix);
        shader.uniformMatrix4("translation", identityMatrix);
        
    	numberNuklearDrawCalls = 0;
    	 // iterate over and execute each draw command
        float fb_scale_x = (float)display_width / (float)width;
        float fb_scale_y = (float)display_height / (float)height;
        long offset = NULL;

        for (NkDrawCommand cmd = nk__draw_begin(context , commands); cmd != null; cmd = nk__draw_next(cmd, commands , context)) {

            if (cmd.elem_count() == 0) continue;
            
            glBindTexture(GL_TEXTURE_2D, cmd.texture().id());
            glScissor((int)(cmd.clip_rect().x() * fb_scale_x) ,
            		  (int)((height - (int)(cmd.clip_rect().y() + cmd.clip_rect().h())) * fb_scale_y) ,
            		  (int)(cmd.clip_rect().w() * fb_scale_x) ,
            		  (int)(cmd.clip_rect().h() * fb_scale_y));
            glDrawElements(GL_TRIANGLES, cmd.elem_count(), GL_UNSIGNED_SHORT, offset);
            numberNuklearDrawCalls++;
            offset += cmd.elem_count() * 2;

        }

        nk_clear(context);
        
    }

	public void shutDownRenderer(){

		glBindBuffer(GL_ARRAY_BUFFER , 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER , 0);
		glBindBuffer(GL_TEXTURE_BUFFER , 0);
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(2);

		memFree(vertexBuffer);
		memFree(elementBuffer);
		GL.destroy();

	}
    
    public void shutDown() {

		System.out.println("Shutting down Renderer...");
		
		LOADED_TEXTURES.forEachVal(Textures::shutDown);
		
        shader.disableShader();
        glDeleteTextures(null_texture.texture().id());
        shutDownRenderer();
        nk_buffer_free(commands);
        GL.setCapabilities(null);

        System.out.println("Renderer shut down.");

    }


}

