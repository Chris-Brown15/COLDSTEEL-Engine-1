package Renderer;

import java.util.function.Consumer;

import org.joml.Vector3f;
import org.python.core.PyObject;
import org.python.core.adapter.ClassicPyObjectAdapter;

import Core.Executor;
import Core.Scene;
import Core.SpriteSets;
import Core.TemporalExecutor;
import Physics.MExpression;

/**
 * WIP particle emitter system. This uses quads to represent particles. 
 * Particle is a class however and it extends {@code Quads} while also managing an animation and lifetime. 
 * <br><br> 
 * ParticleEmitter behavior is controlled by three methods, {@code start}, {@code update}, and {@code finish}.
 * <br><br> 
 * {@code finish} should only be called once before calling {@code update} and after {@code finish} has been called.
 * <br><br> 
 * {@code update} should be called each frame, but mainly only if there is an animation that needs to be updated. 
 * If particles do not play animations, it may not be necessary to call {@code update} each frame.
 * <br><br> 
 * Finally there is {@code finish} which removes the particles from the scene. Particles are moved by two {@code MExpression} objects which are passed into
 * {@code Kinematics}, from which forces are generated which manipulate the particles.    
 * <br><br>
 * 
 * @author Chris Brown
 *
 */
public class ParticleEmitter {

	private Scene owner;
	Textures texture = new Textures();
	private final Particle[] particles;
	private final SpriteSets particleAnimation;
	float[] position = new float[2]; 
	private boolean started = false;
	private boolean textured = false;
	private final double particleLife;
	
	//IMPORTANT: these functions are runnable, but they are NOT to be used with threads. We are merely using Runnable as our SAM functions.
	private Executor emitterStartFunction;
	private Executor emitterUpdateFunction;	
	private Executor emitterFinishFunction;
	private Consumer<Particle> particleStartFunction;
	private Consumer<Particle> particleUpdateFunction;
	private Consumer<Particle> particleFinishFunction;
	
	private MExpression xMoveFunction;
	private MExpression yMoveFunction;
	//emissionRate refers to the number of particles emitted per frame
	private int emissionRate = -1;
	private boolean finishAtEnd = false;
	
	private final Executor shutDown;
	
	
	public ParticleEmitter(Scene owner , int numberParticles , double particleLifetime , MExpression xMoveFunction , MExpression yMoveFunction , String textureAbsPath , String animNamePath , boolean foreground) {
		
		this.owner = owner;
		particles = new Particle[numberParticles];
		Renderer.loadTexture(texture, textureAbsPath);
		particleAnimation = new SpriteSets(animNamePath);
		textured = true;
		this.particleLife = particleLifetime;
		for(int i = 0 ; i < particles.length ; i ++) {
			
			particles[i] = new Particle(i , particleLifetime , texture , particleAnimation.copy());
			if (foreground) owner.quads2().add(particles[i]);
			else owner.quads1().add(particles[i]);		 
			particles[i].shouldRender(false);
			
		}
		
		shutDown = () -> {
			
			if(foreground) for(int i = 0 ; i < particles.length ; i ++) owner.quads2().delete(particles[i]);
			else for(int i = 0 ; i < particles.length ; i ++) owner.quads2().delete(particles[i]);
						
		};
		
		this.xMoveFunction = xMoveFunction;
		this.yMoveFunction = yMoveFunction;
				
	}
	
	public ParticleEmitter(Scene owner , int numberParticles , double particleLifetime , MExpression xMoveFunction , MExpression yMoveFunction , float R , float G , float B  , float xDims , float yDims , boolean foreground) {
	
		this.owner = owner;
		particles = new Particle[numberParticles];
		texture = null;
		particleAnimation = null;
		this.particleLife = particleLifetime;
		for(int i = 0 ; i < particles.length ; i ++) {
			
			particles[i] = new Particle(i , particleLifetime);
			particles[i].color(R , G , B);
			particles[i].setWidth(xDims);
			particles[i].setHeight(yDims);
			if (foreground) owner.quads2().add(particles[i]);
			else owner.quads1().add(particles[i]);	
			particles[i].shouldRender(false);			
			
		}

		shutDown = () -> {
			
			if(foreground) for(int i = 0 ; i < particles.length ; i ++) owner.quads2().delete(particles[i]);
			else for(int i = 0 ; i < particles.length ; i ++) owner.quads2().delete(particles[i]);
						
		};
		
		this.xMoveFunction = xMoveFunction;
		this.yMoveFunction = yMoveFunction;
		
	}

	public ParticleEmitter(Scene owner , int numberParticles , double particleLifetime , MExpression xMoveFunction , MExpression yMoveFunction , String textureAbsPath , SpriteSets anim , boolean foreground) {
		
		this.owner = owner;
		particles = new Particle[numberParticles];
		Renderer.loadTexture(texture, textureAbsPath);
		particleAnimation = anim;
		textured = true;
		this.particleLife = particleLifetime;
		for(int i = 0 ; i < particles.length ; i ++) {
			
			particles[i] = new Particle(i , particleLifetime , texture , particleAnimation);
			if (foreground) owner.quads2().add(particles[i]);
			else owner.quads1().add(particles[i]);
			particles[i].shouldRender(false);
			
		}

		shutDown = () -> {
			
			if(foreground) for(int i = 0 ; i < particles.length ; i ++) owner.quads2().delete(particles[i]);
			else for(int i = 0 ; i < particles.length ; i ++) owner.quads2().delete(particles[i]);
						
		};
		
		this.xMoveFunction = xMoveFunction;
		this.yMoveFunction = yMoveFunction;
				
	}
		
	public void setTexture(Textures texture) {
		
		this.texture = texture;
		for(Particle x : particles) x.setTexture(texture);
		
	}
	
	public boolean isTextureNull() {
		
		return texture == null;
		
	}
	
	public void setPosition(float x , float y) {
		
		position[0] = x;		
		position[1] = y;
		
	}
	
	public void finishAtEnd(boolean finish) {
		
		this.finishAtEnd = finish;
		
	}
	
	public boolean finishAtEnd() {
		
		return finishAtEnd;
		
	}
	
	public void setXFunction(MExpression xFunction) {
		
		this.xMoveFunction = xFunction;
		
	}

	public void setYFunction(MExpression yFunction) {
		
		this.xMoveFunction = yFunction;
		
	}
	
	public void setEmissionRate(int particlesPerFrame) {
		
		assert particlesPerFrame >= 0 : 
			"Invalid number of particles to emit, should be unsigned int greater than 0, or 0 if all particles should be emitted at once.";
		
		emissionRate = particlesPerFrame;
		
	}
	
	public int emissionRate() {
		
		return emissionRate;
		
	}
	
	public void removeColor(Vector3f removeThis) {
		
		for(int i = 0 ; i < particles.length ; i ++) particles[i].setRemovedColor(removeThis);
		
	}

	public void removeColor(float r , float g , float b) {
		
		for(int i = 0 ; i < particles.length ; i ++) particles[i].removeColor(r , g , b);
		
	}
	
	public void onEmitterStart(Executor startFunction) {
		
		this.emitterStartFunction = startFunction;
		
	}
	
	public void onEmitterStart(PyObject startFunction) {
		
		this.emitterStartFunction = () -> startFunction.__call__();
		
	}
	
	public void onEmitterUpdate(Executor updateFunction) {
		
		this.emitterUpdateFunction = updateFunction;
		
	}

	public void onEmitterUpdate(PyObject updateFunction) {
		
		this.emitterUpdateFunction = () -> updateFunction.__call__();
		
	}
	
	public void onEmitterFinish(Executor finishFunction) {
		
		this.emitterFinishFunction = finishFunction;
		
	}

	public void onEmitterFinish(PyObject finishFunction) {
		
		this.emitterFinishFunction = () -> finishFunction.__call__();
		
	}
			
	public void onParticleStart(Consumer<Particle> startFunction) {
		
		this.particleStartFunction = startFunction;
		
	}
	
	public void onParticleStart(PyObject code) {
		
		this.particleStartFunction = (x) -> code.__call__(new ClassicPyObjectAdapter().adapt(x));
		
	}
	
	public void onParticleUpdate(Consumer<Particle> updateFunction) {
		
		this.particleUpdateFunction = updateFunction;
		
	}

	public void onParticleUpdate(PyObject code) {
		
		this.particleUpdateFunction = (x) -> code.__call__(new ClassicPyObjectAdapter().adapt(x));
		
	}
		
	public void onParticleFinish(Consumer<Particle> finishFunction) {
		
		this.particleFinishFunction = finishFunction;
		
	}
	
	public void onParticleFinish(PyObject code) {
		
		this.particleFinishFunction = (x) -> code.__call__(new ClassicPyObjectAdapter().adapt(x));
		
	}

	private void startParticle(Particle startThis) {
		
		startThis.moveToEmitter(this);		
		if(particleStartFunction != null) particleStartFunction.accept(startThis);
		startThis.particleTimer.start();
		startThis.started = true;
		owner.kinematics().impulse(particleLife , xMoveFunction , yMoveFunction , startThis);		
		//will individually call finish after each particle has run out of time
		//otherwise, it will be reset 
		if(finishAtEnd) TemporalExecutor.onElapseOf(((startThis.getID() * emissionRate) + particleLife) - 50, () -> finishParticle(startThis));
		//will finish all particles at once at the end of the last particle's time
		else if(startThis.getID() == particles.length -1) owner.kinematics().onFinish(this::reset);

		//work around to ensure when a particle is updated it will not look wrong
		startThis.animate();
		startThis.animate();
		startThis.animate();
		startThis.shouldRender(true);
		
	}
	
	public void start() {
		
		if(emitterStartFunction != null) emitterStartFunction.execute();
		
		//offset the startup of the particles by a number of milliseconds 
		if(emissionRate > 0) for(int i = 0 ; i < particles.length ; i++) {
			
			Particle query = particles[i];
			TemporalExecutor.onElapseOf(i * emissionRate, () -> startParticle(query));
			
		}
		
		//start all the particles at once
		else for(int i = 0 ; i < particles.length ; i ++) startParticle(particles[i]);
		
		started = true;
		
	}
	
	private void updateParticles() {
		
		for(int i = 0 ; i < particles.length ; i ++) if(particles[i].started) {
						
			if(particleUpdateFunction != null) particleUpdateFunction.accept(particles[i]);
			if(textured) particles[i].animate();
			if(particles[i].finished()) reset(particles[i]);
			
		}
			 		
	}
		
	public void update() {
		
		if(started) {
			
			if(emitterUpdateFunction != null) emitterUpdateFunction.execute();			
			updateParticles();
						
		}
		
	}
		
	void finishParticle(Particle finishThis) {

		if(particleFinishFunction != null) particleFinishFunction.accept(finishThis);
		finishThis.shouldRender(false);
		finishThis.started = false;
		
	}
	
	public void finish() {
		
		if(emitterFinishFunction != null) emitterFinishFunction.execute();
		for(Particle x : particles) finishParticle(x);
		
		started = false;
		
	}
	
	public void reset() {
		
		for(int i = 0 ; i < particles.length ; i ++) reset(particles[i]);
		
	}
	
	void reset(Particle particle) {
		
		particle.particleTimer.start();
		particle.moveToEmitter(this);
		owner.kinematics().impulse(particleLife , xMoveFunction , yMoveFunction , particle);
		
	}
	
	public void shutDown() {
		
		shutDown.execute();
		
	}
	
	public boolean started() {
		
		return started;
		
	}
	
}