package Networking;

public enum ClientState {

	DORMANT(0),
	AWAITING_PRE_CONNECTION(1),
	AWAITING_CONNECTION(2),
	READY(2),
	;
	
	public final int rating ;
	
	ClientState(final int rating) {
		
		this.rating = rating;
		
	}

	public boolean greaterThanEqualTo(final ClientState other) {
		
		return rating >= other.rating;
		
	}

	public boolean greaterThan(final ClientState other) {
		
		return rating > other.rating;
		
	}
	
}
