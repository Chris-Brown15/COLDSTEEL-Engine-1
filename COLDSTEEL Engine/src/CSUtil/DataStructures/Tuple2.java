package CSUtil.DataStructures;

public class Tuple2<U , V> {

	private U first;
	private V second;

	public Tuple2() {}
	
	public Tuple2(U first , V second){
		
		this.first = first;
		this.second = second;
		
	}
	
	public Tuple2(Tuple2<U , V> source) {
		
		this.first = source.first;
		this.second = source.second;
		
	}
	
	public void setFirst(U u){

		this.first = u;

	}

	public void setSecond(V v){

		this.second = v;

	}

	public U getFirst(){

		return first;

	}

	public V getSecond(){

		return second;

	}

	public boolean anyNull() {
		
		return first == null || second == null;
		
	}
	
}