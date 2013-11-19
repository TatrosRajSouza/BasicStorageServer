package common.messages;

public class InvalidMessage extends Exception {

	private static final long serialVersionUID = 3208812901373211818L;

	public InvalidMessage() {
		super();
	}
	
	public InvalidMessage(String message) {
		super(message);
	}
}
