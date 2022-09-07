package CSUtil;

/**
 * 
 * Reference to an integer. We can propogate changes of indices to holders of indices with this class. For example, an entity has a sound index
 * but once we change the order of the elements in the sound list, the int index will no longer point to the correct sound. But if we change the 
 * value wrapped in an instance of this class, the owner of the index doesn't have to be notified or interupted because the index wrapped in this 
 * class will be modified when a list is modified so it still points to the same element, at its new position.
 * 
 * @author Chris Brown
 *
 */
public class RefInt {

	private int val;
	
	public RefInt(int val) {
		
		this.val = val;
		
	}
	
	public int get() {
		
		return val;
		
	}
	
	public void set(int val) {
		
		this.val = val;
		
	}
	
	public void dec() {
		
		val--;
		
	}
	
	public int decNA() {
		
		return val - 1;
		
	}
	
	public void add() {
		
		val++;
		
	}
	
	public int addNA() {
		
		return val + 1;
		
	}
	
	public void add(int addend) {
		
		val += addend;
		
	}
	
	public int addNA(int addend) {
		
		return val + addend;
		
	}
	
	public void sub(int subtrahend) {
		
		val -= subtrahend;
		
	}
	
	public int subNA(int subtrahend) {
		
		return val - subtrahend;
		
	}
	
	public void div(int dividend) {
		
		val /= dividend;
		
	}
	
	public int divNA(int dividend) {
		
		return val / dividend;
		
	}
	
	public void mul(int mul) {
		
		val *= mul;
		
	}
	
	public int mulNA(int mul) {
		
		return val * mul;
		
	}
	
	public boolean greaterThan(int operand) {
	
		return val > operand;
		
	}

	public boolean lessThan(int operand) {
	
		return val < operand;
		
	}

	public boolean greaterThanEqualTo(int operand) {
	
		return val >= operand;
		
	}

	public boolean lessThanEqualTo(int operand) {
	
		return val <= operand;
		
	}
	
	public boolean equals(int other) {
		
		return val == other;
		
	}
	
	public String toString() {
		
		return "" + val;
		
	}
		
	public int hashCode() {
		
		return val;
	}
	
}
