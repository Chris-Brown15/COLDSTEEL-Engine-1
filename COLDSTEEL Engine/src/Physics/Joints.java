package Physics;

import Core.CSType;
import Core.Quads;

/**
 * 
 * 
 * @author Chris Brown
 *
 */
public class Joints extends Quads{

	{
		setWidth(1);
		setHeight(1);
	}
	
	public Joints() {
		
		super(CSUtil.BigMixin.getJointFloatArray() , -1 , CSType.JOINT);	
		
	}
	
	int select(float xCoord , float yCoord) {
		
		return selectQuad(xCoord , yCoord);
		
	}
		
	public void setID(int id) {
		
		this.ID = id;
		
	}
	
}
