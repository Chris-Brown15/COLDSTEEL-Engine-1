package CSUtil.DataStructures;

public class Tuple6<First , Second , Third , Fourth , Fifth , Sixth> {

	private First first;
	private Second second;
	private Third third;
	private Fourth fourth;
	private Fifth fifth;
	private Sixth sixth;

	public Tuple6<First , Second , Third , Fourth , Fifth , Sixth> getValues(){

		return Tuple6.this;

	}

	public First getFirst() {
		return first;
	}

	public void setFirst(First first) {
		this.first = first;
	}

	public Second getSecond() {
		return second;
	}

	public void setSecond(Second second) {
		this.second = second;
	}

	public Third getThird() {
		return third;
	}

	public void setThird(Third third) {
		this.third = third;
	}

	public Fourth getFourth() {
		return fourth;
	}

	public void setFourth(Fourth fourth) {
		this.fourth = fourth;
	}

	public Fifth getFifth() {
		return fifth;
	}

	public void setFifth(Fifth fifth) {
		this.fifth = fifth;
	}

	public Sixth getSixth() {
		return sixth;
	}

	public void setSixth(Sixth sixth) {
		this.sixth = sixth;
	}

}
