package CSUtil.DataStructures;

public class Tuple5<A , B , C , D, E> {

	private A first;
	private B second;
	private C third;
	private D fourth;
	private E fifth;

	boolean valuesStored = false;

	public Tuple5(A first , B second , C third , D fourth , E fifth){

		this.first = first;
		this.second = second;
		this.third = third;
		this.fourth = fourth;
		this.fifth = fifth;

		valuesStored = true;

	}

	public Tuple5(){



	}

	public Object[] getValues() {

		Object[] values = {first , second , third , fourth , fifth};
		return values;

	}

	public A getFirst() {

		return first;

	}

	public void setFirst(A first) {

		this.first = first;

	}

	public B getSecond() {

		return second;

	}

	public void setSecond(B second) {

		this.second = second;

	}

	public C getThird() {

		return third;

	}

	public void setThird(C third) {

		this.third = third;

	}

	public D getFourth() {

		return fourth;

	}

	public void setFourth(D fourth) {

		this.fourth = fourth;

	}

	public E getFifth() {

		return fifth;
	}

	public void setFifth(E fifth) {

		this.fifth = fifth;

	}

}
