package Game.Projectiles;

import static CS.Engine.INTERNAL_ENGINE_PYTHON;

import org.python.core.PyCode;
import org.python.core.PyObject;
import org.python.core.adapter.ClassicPyObjectAdapter;

import Core.Scene;
import Core.Entities.EntityLists;
import Core.Entities.EntityScanResult;
import Physics.Kinematics;

/**
 * Singleton style class that projectile scripts get a reference to which allow projectile scripts to work as desired.
 * 
 * @author Chris Brown
 *
 */
public class ProjectileScriptingInterface {
	
	public static final PyCode PROJECTILE_SCRIPTING_FACADE = INTERNAL_ENGINE_PYTHON().compile("CS_ProjectileScriptingFunctions.py");;
	
	Scene scene;
	
	public ProjectileScriptingInterface(Scene scene) {
		
		this.scene = scene;
		
	}

	/**
	 * Scans P's hitbox against that of all other entities in the given radius, calling callback on each of them. Callback will provide a 
	 * EntityScanResult that the Python Code will use.
	 * 
	 * @param P
	 * @param radius
	 * @param callback
	 */
	public void scanForEntities(Projectiles P , float radius , PyObject code) {
		
		EntityScanResult ent = scene.entities().nearestEntity(P, radius);
		if(ent.hasEntity()) { 
			
			var hitboxscan = scene.entities().checkHitBoxes(P, ent);
			if(hitboxscan.isCallerHot() && hitboxscan.isTargetCold())
				code.__call__(new ClassicPyObjectAdapter().adapt(EntityLists.getEnclosedEntity(ent)));
			
		}
		
	}
	
	public Kinematics kinematics() {
		
		return scene.kinematics();
		
	}
	
}
