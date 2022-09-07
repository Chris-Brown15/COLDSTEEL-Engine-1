package CSUtil.DataStructures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Tuple2<U , V> implements Iterable<Object> {

	private U first;
	private V second;

	public Tuple2() {}
	
	public Tuple2(U first , V second){
		
		this.first = first;
		this.second = second;
		
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
	
	@Override public Iterator<Object> iterator() {

		List<Object> TupleAsList = new ArrayList<Object>(2);
		TupleAsList.add(first);
		TupleAsList.add(second);

		return TupleAsList.iterator();
		
	}
		
}