package Core;

import org.python.core.PyObject;
import org.python.core.adapter.ClassicPyObjectAdapter;

import CS.UserInterface;

public class ScriptedUserInterface extends UserInterface {

	/** No need to continually allocate this, just keep it around. */
	private static final ClassicPyObjectAdapter frameAdapter = new ClassicPyObjectAdapter();
	
	public ScriptedUserInterface(String title , float x , float y , float w , float h , int normalOptions , int unopenedOptions) {

		super(title, x, y, w, h, normalOptions, unopenedOptions);

	}
	
	public void setLayout(PyObject layout) {
	
		layoutBody((frame) -> layout.__call__(frameAdapter.adapt(frame)));
		
	}

	public void show() {
		
		show = true;
		
	}

	public void hide() {
		
		show = true;
		
	}
	
	public void toggle() {
		
		if(show) show = false;
		else show = true;
		
	}
	
}