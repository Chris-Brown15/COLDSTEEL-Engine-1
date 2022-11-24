package Renderer;

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
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
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
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL11.glScissor;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.opengl.GL11C.glViewport;
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

import static CSUtil.BigMixin.async;
import static CSUtil.BigMixin.TRY;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

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

import CSUtil.Timer;
import CSUtil.DataStructures.CSQueue;
import Core.Executor;
import CS.Engine;
import CS.GLFWWindow;
import CS.UserInterface;
import CSUtil.DataStructures.CSLinked;
import CSUtil.DataStructures.CSStack;
import CSUtil.DataStructures.Tuple2;
import Core.CSType;
import Core.Direction;
import Core.ECS;
import Core.Quads;
import Core.Scene;
import Core.Entities.Entities;
import Core.Entities.EntityHitBoxes;
import Core.Statics.Statics;
import Game.Items.Inventories;
import Game.Items.ItemComponents;
import Game.Levels.Levels;
import Game.Levels.MacroLevels;

public final class Renderer {

	private static volatile CSLinked<Textures> LOADED_TEXTURES = new CSLinked<Textures>();
	private static volatile ReentrantLock REQUEST_LOCK = new ReentrantLock();
	private static volatile CSStack<Tuple2<Textures , String>> TEXTURE_LOAD_REQUESTS = new CSStack<>();

    private static int drawCalls = 0;
    private static double renderTime;

    private static Thread theRenderThread;
    private static AtomicBoolean persisting = new AtomicBoolean(false);
    private static volatile boolean shutDown = false;

    static final int[] elementArray = {
        2 , 1 , 0 ,
        0 , 1 , 3
    };

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
	
	private static void checkedGL(Executor code) {
		
		code.execute();
		if(checkErrors()) throw new IllegalStateException();
		
	}
	
	public static void loadTexture(Textures texture , String filepath) {

		if(filepath == null || filepath.equals("null")) return;
				
		if(!Thread.currentThread().equals(theRenderThread)) {
			
			REQUEST_LOCK.lock();
			TEXTURE_LOAD_REQUESTS.push(new Tuple2<>(texture , filepath));
			REQUEST_LOCK.unlock();		
			
		} else {
			
			REQUEST_LOCK.lock();
			texture.initialize(filepath);
			LOADED_TEXTURES.add(texture);
			if(checkErrors()) {
				
				try {
					
					throw new IllegalStateException("GL Error thrown on call to loadTexture. Parameters: " + filepath);
					
				} finally {
					
					REQUEST_LOCK.unlock();
					
				}
				
			} else REQUEST_LOCK.unlock();			
			
		}
		
	}

	public static void loadTexture(Textures texture) {

		if(!Thread.currentThread().equals(theRenderThread)) { 
			
			REQUEST_LOCK.lock();
			TEXTURE_LOAD_REQUESTS.push(new Tuple2<>(texture , null));
			REQUEST_LOCK.unlock();		
			
		} else {
			
			REQUEST_LOCK.lock();
			texture.initialize();
			LOADED_TEXTURES.add(texture);
			if(checkErrors()) {
				
				try {
					
					throw new IllegalStateException("GL Error thrown on call to loadTexture. no Parameters.");
					
				} finally {
					
					REQUEST_LOCK.unlock();
					
				}
				
			} else REQUEST_LOCK.unlock();
			
		}
		
	}
	
	private static void handleTextureLoadRequests() {

		assert Thread.currentThread().equals(theRenderThread) : "Invalid call to handleTextureLoadRequests, must be called in the main thread";
		
		REQUEST_LOCK.lock();
		
		Tuple2<Textures , String> request;
		while(!TEXTURE_LOAD_REQUESTS.empty()) {
			
			request = TEXTURE_LOAD_REQUESTS.pop();			
			if(request.getSecond() != null) loadTexture(request.getFirst() , request.getSecond());
			else if (request.getSecond() == null) loadTexture(request.getFirst());
			
		}
		
		REQUEST_LOCK.unlock();
		
	}
	
    /**
     * To free a macro level, we free all textures pushed onto the macro level's loaded texture stack. This stack represents
     * the indices of the loaded textures so we will first populate a linked list with the textures, then free them all while 
     * removing then from the static list of textures.
     * 
     * @param freeThis � the macro level to free
     */
    public static synchronized void freeMacroLevel(MacroLevels freeThis) {
    	
    	while(!freeThis.loadedTextures().empty()) LOADED_TEXTURES.removeVal(freeThis.loadedTextures().pop()).val.shutDown();
    	
    }
    
    public static synchronized void removeTexture(int index) {
    	
    	LOADED_TEXTURES.removeVal(index).shutDown();
    	
    }
    
	public static int getNumberDrawCalls(){

		return drawCalls + numberNuklearDrawCalls;

	}
	
	public static int sceneDrawCalls() { 
		
		return drawCalls;
		
	}
	
	public boolean isPersisting() {
		
		return persisting.get();
		
	}
	
	public boolean isShutDown() {
		
		return shutDown;
		
	}
	
	private Textures previousTexture = null;
	
	private volatile Camera camera = new Camera(new Vector2f(0 , 0));
	private Shader shader = new Shader();
	
    private int VAOID;
    private int VBOID;
    private int EBOID;
    
    private FloatBuffer vertexBuffer;
	private IntBuffer elementBuffer;

	private boolean renderOthers = true;

	private volatile CSQueue<Executor> rendererCallbacks = new CSQueue<Executor>();

	private GLFWWindow window;

    public volatile Quads screenQuad = new Quads(-1);
    private volatile Scene renderScene;    
    private volatile Levels debugRenderLevel;
    private volatile boolean renderDebug = false;

    private Timer renderTimer = new Timer();
    public int framesLastSecond = 0;
    private int currentFrame = 0;
    private int totalSeconds = 0;
    private Thread renderThread;
    
    public Renderer(ReentrantLock shutDownLock , Scene renderScene , GLFWWindow window) {
            	
    	renderThread = new Thread(() -> {

    		initialize(renderScene , window);    		
    		while(persisting.get()) { 
    		
    		 	Future<?> uiDrawFuture = async(UserInterface.NUKLEAR_RUNNABLE);
    			checkedGL(() -> handleScheduledEvents());
    			clear();
    			run(uiDrawFuture);
    			
      	    	window.swapBuffers();
      	    	
    		}
    		
    		shutDown(shutDownLock);
    		
    	});
    	
    	theRenderThread = renderThread;    	
    	renderThread.setName("OpenGL Thread");
    	renderThread.start();    	
    	
    }
    
    public void setNuklearEnv(NkContext context , NkBuffer commands) {
    	
    	this.context = context;
    	this.commands = commands;
    	
    }
    
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

    public void renderScene(Scene renderScene) {    	
    
    	this.renderScene = renderScene;
    	
    }
    
	public void initialize(Scene renderScene , GLFWWindow window){

		this.window = window;
		this.window.makeCurrent();
		
		System.out.println("Beginning Renderer initialization...");
		shader.initializeShader();
		shader.activate();
		initializeVAO();
		initializeVBO();
		initializeEBO();
		initializeTextures();
		initializeVertexAttribs();
		setOpenGLState();
		if(checkErrors()) throw new IllegalStateException("GL Error thrown on call to initialize."); 
		screenQuad.moveTo(camera.cameraPosition.x , camera.cameraPosition.y);		
		screenQuad.setDimensions(window.getWindowDimensions());
		screenQuad.makeTranslucent(0.0f);		
    	int[] winDims = window.getFramebufferDimensions();    	
        glViewport(0, 0, winDims[0], winDims[1]);
		renderScene(renderScene);
		System.out.println("Renderer initialization complete.");

		persisting.getAndSet(true);
		
	}
	
	public void schedule(Executor callback) {
		
		synchronized(rendererCallbacks) {
			
			rendererCallbacks.enqueue(callback);
			
		}
		
	}
	
	public void schedule(RenderCommands command) {

		synchronized(rendererCallbacks) {
			
			switch(command) {
			
				case INITIATE_SHUT_DOWN -> rendererCallbacks.enqueue(() -> persisting.getAndSet(false));
				case INITIALIZE_NUKLEAR -> rendererCallbacks.enqueue(() -> initializeNuklear());
				
			}
				
		}
		
	}
	
	private void handleScheduledEvents() {
		
		handleTextureLoadRequests();
		
		synchronized(rendererCallbacks) {
			
			while(!rendererCallbacks.empty()) rendererCallbacks.dequeue().execute();
			
		}
		
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
    		
    		if(texture.filledOut()) {
    			
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
    
    private void drawQuadParallax(Quads drawThis) {
    	
    	shader.uniformMatrix4("uView", new Matrix4f().identity());
    	drawQuad(drawThis);
    	
    }
    
    public static double getRenderTime(){

    	return renderTime;

    }
    
    private void drawByType(Quads quad , CSType type) {
    	
    	switch(type) {
    		
			case STATIC -> drawStatic((Statics)quad);
			case ENTITY -> {
				
				if(((Entities)quad).has(ECS.CAMERA_TRACK)) {

			    	shader.uniformMatrix4("uView", camera.getViewMatrix());
			    	drawQuad(quad);
			    	
				}
				
			}
			
			default -> drawQuad(quad);
    	
    	}
    	
    }
    
    public int framesLastSecond() {
    	
    	return framesLastSecond;
    	
    }
    
    private void clear() {

		glClearColor(window.r() , window.g() , window.b() , 1);
		glClear(GL_COLOR_BUFFER_BIT);

    }
    
    private void run(Future<?> uiDrawFuture) {

		if(renderTimer.getElapsedTimeSecs() >= 1) {
			
			renderTimer.start();
			framesLastSecond = currentFrame;
			currentFrame = 0;
			if(Engine.printFPS) System.out.println("Render Frames in Second " + totalSeconds + ": " + framesLastSecond);
			totalSeconds++;
			
		}
		
    	drawCalls = 0;
    		
    	previousTexture = null;    	
    	
    	setVertexAttribPointersScene();
    	shader.uniformMatrix4("uProjection", camera.getProjectionMatrix());
    	shader.uniformMatrix4("uView", camera.getViewMatrix());
    	
    	REQUEST_LOCK.lock();
    	
    	if(renderScene != null) { 
    		
    		renderScene.forDefault(list -> list.forEach(instanceOfQuads -> drawByType(instanceOfQuads , list.TYPE)));    		
    		renderScene.finalArrays().forEachVal(array -> drawData(array));
    		
    	}    	
    	        
        REQUEST_LOCK.unlock();
        
		if(renderDebug) renderDebug();
    	
    	if(initializedNuklear) {
    		
    		TRY(() -> uiDrawFuture.get());
    		renderUI();
    		
    	}

    	setVertexAttribPointersScene();
    	shader.uniformMatrix4("uProjection", camera.getProjectionMatrix());
    	shader.uniformMatrix4("uView", camera.getViewMatrix());
    	
    	drawQuadParallax(screenQuad);
    	
    	currentFrame++;

    }

	private void renderDebug() {

		renderScene.colliders().forEach(quad -> drawQuad(quad));
		renderScene.entities().forEach(entity -> {
			
			Object[] comps = entity.components();
			if(entity.has(ECS.COLLISION_DETECTION) && comps[Entities.CDOFF] != null) drawData((float[]) comps[Entities.CDOFF]);
			if(entity.has(ECS.HITBOXES)) {
				
				EntityHitBoxes entityHitBoxes = (EntityHitBoxes) comps[Entities.HOFF];
				float[][] boxes = entityHitBoxes.getActiveHitBoxes(entity, (Direction)comps[Entities.DOFF]);
				if(boxes != null) for(float[] x : boxes) drawData(x);
				
			}
			
			if(entity.has(ECS.INVENTORY)) {
				
				((Inventories)comps[Entities.IOFF]).getEquipped().forEach(item -> {
					
					if(item.has(ItemComponents.HITBOXABLE)) {
						
						float[][] boxes = item.componentData().getActiveHitBoxes();
						if(boxes != null) for(float[] x : boxes) drawData(x);
						
					}
					
				});
				
			}
			
			if(debugRenderLevel != null){
				
				debugRenderLevel.forEachLoadDoor(loadDoor -> drawQuad(loadDoor.getConditionArea()));
				debugRenderLevel.forEachTrigger(trigger -> {
					
					trigger.forEachConditionArea(conditionArea -> drawQuad(conditionArea));
					trigger.forEachEffectArea(effectArea -> drawQuad(effectArea));
					
				});
				
			}
			
		});
		
	}

	public void toggleRenderDebug(Levels level) {
		
		renderDebug = renderDebug ? false:true;
		debugRenderLevel = level;
		
	}

	public boolean isRenderingDebug() {
		
		return renderDebug; 
		
	}
	
    public int getNumberNuklearDrawCalls() {

		return numberNuklearDrawCalls;

	}

	public Camera getCamera(){

		return camera;

	}

	public void setViewport(int widthPx , int heightPx) {
		
		glViewport(0 , 0 , widthPx , heightPx);
		
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

    private volatile NkContext context;
    private volatile NkBuffer commands;
    
    private static final NkDrawVertexLayoutElement.Buffer VERTEX_LAYOUT;
    static {

        VERTEX_LAYOUT = NkDrawVertexLayoutElement.create(4)
            .position(0).attribute(NK_VERTEX_POSITION).format(NK_FORMAT_FLOAT).offset(0)
            .position(2).attribute(NK_VERTEX_COLOR).format(NK_FORMAT_R8G8B8A8).offset(positionSize * Float.BYTES)
            .position(1).attribute(NK_VERTEX_TEXCOORD).format(NK_FORMAT_FLOAT).offset((positionSize + colorSize) * Float.BYTES)
            .position(3).attribute(NK_VERTEX_ATTRIBUTE_COUNT).format(NK_FORMAT_COUNT).offset(0)
            .flip();
     
    }

    private static int numberNuklearDrawCalls = 0;
       
    private FloatBuffer viewBuffer = BufferUtils.createFloatBuffer(16);
    private FloatBuffer projectionBuffer = BufferUtils.createFloatBuffer(16);
    private NkDrawNullTexture null_texture = NkDrawNullTexture.create();

    private int width , height;
    private int display_width , display_height;
    private boolean initializedNuklear = false;
    
    private void initializeNuklear(){
        
    	// null texture setup
        int nullTexID = glGenTextures();

        null_texture.texture().id(nullTexID);
        null_texture.uv().set(0.5f, 0.5f);
        
        int[] dims = window.getWindowDimensions();
        width = dims[0]; height = dims[1];
        int[] frameBuffer = window.getFramebufferDimensions();
        display_width = frameBuffer[0]; display_height = frameBuffer[1];
        
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
        
        initializedNuklear = true;
        
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

    private void renderUI() {
    	
    	try (MemoryStack stack = stackPush()) {

            int[] dims = window.getWindowDimensions();
            width = dims[0]; height = dims[1];
            int[] frameBuffer = window.getFramebufferDimensions();
            display_width = frameBuffer[0]; display_height = frameBuffer[1];
            
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
                .line_AA(NK_ANTI_ALIASING_ON)
            ;

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

	private void shutDownRenderer(){

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
    
    public void shutDown(ReentrantLock shutDownLock) {

    	shutDownLock.lock();
    	    	
		System.out.println("Shutting down Renderer...");
		
		LOADED_TEXTURES.forEachVal(Textures::shutDown);
		
        shader.disableShader();
        glDeleteTextures(null_texture.texture().id());
        shutDownRenderer();
        
        nk_buffer_free(commands);
        GL.setCapabilities(null);

        System.out.println("Renderer shut down.");
        
        shutDownLock.unlock();
        shutDown = true;
        
    }

    public void join() {
    	
    	try {
    		
			renderThread.join();
			
		} catch (InterruptedException e) {
			
			e.printStackTrace();
			
		}
    	
    }
    
}