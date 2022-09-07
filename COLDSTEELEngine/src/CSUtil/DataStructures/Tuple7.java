package CSUtil.DataStructures;

public class Tuple7<Q , W , E , R , T , Y , U> {

	private Q first;
	private W second;
	private E third;
	private R fourth;
	private T fifth;
	private Y sixth;
	private U seventh;

	public Tuple7(){}

	public Tuple7<Q , W , E , R , T , Y , U> getData(){

		return Tuple7.this;

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
	
	public void set(Q f , W s , E t, R fo , T fi , Y si , U sv) {
		
		first = f;
		second = s;
		third = t;
		fourth = fo;
		fifth = fi;
		sixth = si;
		seventh = sv;
		
	}

}
