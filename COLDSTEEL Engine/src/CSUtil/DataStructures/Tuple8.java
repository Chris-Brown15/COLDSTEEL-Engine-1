package CSUtil.DataStructures;

public class Tuple8 <Q , W , E , R , T , Y , U , I>{

	private Q first; 
	private W second; 
	private E third;	
	private R fourth; 
	private T fifth; 
	private Y sixth; 
	private U seventh; 
	private I eighth;
	
	public Tuple8() {}
	public Tuple8(Q first,  W second, E third, R fourth,  T fifth,  Y sixth,  U seventh,  I eighth) {
		
		this.first = first;
		this.second = second;
		this.third = third;
		this.fourth = fourth;
		this.fifth = fifth;
		this.sixth = sixth;
		this.seventh = seventh;
		this.eighth = eighth;
		
	}
	
	public void set(Q first,  W second, E third, R fourth,  T fifth,  Y sixth,  U seventh,  I eighth) {
		this.first = first;
		this.second = second;
		this.third = third;
		this.fourth = fourth;
		this.fifth = fifth;
		this.sixth = sixth;
		this.seventh = seventh;
		this.eighth = eighth;
	}
	
	public Q getFirst() {
		return first;
	}
	public void setFirst(Q first) {
		this.first = first;
	}
	public W getSecond() {
		return second;
	}
	public void setSecond(W second) {
		this.second = second;
	}
	public E getThird() {
		return third;
	}
	public void setThird(E third) {
		this.third = third;
	}
	public R getFourth() {
		return fourth;
	}
	public void setFourth(R fourth) {
		this.fourth = fourth;
	}
	public T getFifth() {
		return fifth;
	}
	public void setFifth(T fifth) {
		this.fifth = fifth;
	}
	public Y getSixth() {
		return sixth;
	}
	public void setSixth(Y sixth) {
		this.sixth = sixth;
	}
	public U getSeventh() {
		return seventh;
	}
	public void setSeventh(U seventh) {
		this.seventh = seventh;
	}
	public I getEighth() {
		return eighth;
	}
	public void setEighth(I eighth) {
		this.eighth = eighth;
	}
	

	
}
