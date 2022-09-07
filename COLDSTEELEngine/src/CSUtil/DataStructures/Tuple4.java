package CSUtil.DataStructures;

public class Tuple4<F , S , T , Fr> {

	F first;
	S second;
	T third;
	Fr fourth;
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
	public Fr getFourth() {
		return fourth;
	}
	public void setFourth(Fr fourth) {
		this.fourth = fourth;
	}
	
	public Tuple4(){}
	public Tuple4(F first , S second , T third , Fr fourth){
		
		this.first = first;
		this.second = second;
		this.third = third;
		this.fourth = fourth;
		
	}

}