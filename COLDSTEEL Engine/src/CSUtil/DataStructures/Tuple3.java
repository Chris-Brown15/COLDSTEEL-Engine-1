package CSUtil.DataStructures;

public class Tuple3 <F , S , T>{

	F first;
	S second;
	T third;
	
	public F getFirst() {
		
		return first;
		
	}
	
	public void setFirst(F first) {
		
		this.first = first;
		
	}
	
	public S getSecond() {
		
		return second;
		
	}
	
	public void setSecond(S second) {
		
		this.second = second;
		
	}
	
	public T getThird() {
		
		return third;
		
	}
	
	public void setThird(T third) {
		
		this.third = third;
		
	}

	public Tuple3(F first , S second , T third) {
		
		this.first = first;
		this.second = second;
		this.third = third;
		
	}

	public Tuple2<F , S> toTuple2(){
		
		return new Tuple2<F , S>(first , second);
		
	}
	
	public Tuple3() {}
}
