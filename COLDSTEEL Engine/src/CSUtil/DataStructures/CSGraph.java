package CSUtil.DataStructures;

public class CSGraph <T , K> {

	public gNode<T , K> addNode(T data , K key , @SuppressWarnings("unchecked") gNode<T , K>...links) {
	
		gNode<T , K> newNode = new gNode<>(data);
		for(int i = 0 ; i < links.length ; i ++) gNode.link(links[i] , newNode);
		return newNode;
		
	}
		
}