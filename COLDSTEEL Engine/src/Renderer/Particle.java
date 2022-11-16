package Renderer;

import CSUtil.Timer;
import Core.CSType;
import Core.Quads;
import Core.SpriteSets;

/**
 * Class for a particle
 * 
 * @author Chris Brown
 *
 */

public class Particle  extends Quads {

	SpriteSets anim;
	public double lifetime;
	Timer particleTimer = new Timer();
	boolean started = false;		
	
	public Particle(int ID , double lifetimeMillis , Textures texture , SpriteSets anim) {
	
		super(CSUtil.BigMixin.getFloatArray(), ID , CSType.PARTICLE);			
		setTexture(texture);
		this.lifetime = lifetimeMillis;
		this.anim = anim;
		snapToPixels = false;
		
	}
	
	public Particle(int ID , double lifetimeMillis) {
		
		super(CSUtil.BigMixin.getFloatArray(), ID , CSType.PARTICLE);
		this.lifetime = lifetimeMillis;
		anim = null;
		snapToPixels = false;
				
	}
	
	public boolean finished() {
		
		return particleTimer.getElapsedTimeMillis() > lifetime;
		
	}
	
	//sets the color of this particle to passed color
	void color(float r , float g , float b) {
		
		vertexData[30] = r;
		vertexData[31] = g;
		vertexData[32] = b;
		vertexData[3] = r;
		vertexData[4] = g;
		vertexData[5] = b;
		vertexData[12] = r;
		vertexData[13] = g;
		vertexData[14] = b;
		vertexData[21] = r;
		vertexData[22] = g;
		vertexData[23] = b;
	
		
	}

	public void moveToEmitter(ParticleEmitter emitter) {
		
		moveTo(emitter.position[0] , emitter.position[1]);
		
	}
	
	public void animate() {
		
		if(anim != null) swapSprite(anim.swapSprite());
		
	}
	
}