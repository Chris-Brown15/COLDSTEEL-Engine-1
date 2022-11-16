package CSUtil.DataStructures;

public class gNode <T , K> {

	public T data;
	public K Key;
	public CSLinked<gNode<T , K>> links;
	
	public gNode(T node) {
		
		data = node;
		
	}
	
	public static final <T  , K> void link(gNode<T , K> n1 , gNode<T , K> n2) {

		n1.links.add(n2);
		n2.links.add(n1);
		
	}
	
}
