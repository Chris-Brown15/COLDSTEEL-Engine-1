package CSUtil.DataStructures;

public class tNode <T , H> {

	public CSLinked<tNode<T , H>> attached = new CSLinked<tNode<T , H>>();
	public T val;
	public H ID;
		
	tNode(T val , H identifier){
		
		this.val = val;
		this.ID = identifier;
		
	}
		
	tNode(){}
	
	public tNode<T , H> add(T val , H id) {
		
		tNode<T , H> newNode = new tNode<>(val , id);
		attached.add(newNode);
		return newNode;
		
	}

	public tNode<T , H> add() {
		
		tNode<T , H> newNode = new tNode<>();
		attached.add(newNode);
		return newNode;
		
	}
	
	public tNode<T , H> get(H ID){
		
		return attached.getIfExists(tnode -> tnode.ID.equals(ID)).val;
		
	}
	
	public String toString() {
		
		return val.toString() + " " + ID.toString();
		
	}

	public boolean isAttachedByID(H ID) {
		
		cdNode<tNode<T , H>> iter = attached.get(0);
		for(int i = 0 ; i < attached.size() ; i ++ , iter = iter.next) if(iter.val.ID.equals(ID)) return true;
		return false;
		
	}
	
}
