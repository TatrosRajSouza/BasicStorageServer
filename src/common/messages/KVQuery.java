package common.messages;

import common.messages.KVMessage.StatusType;

public class KVQuery {
	private StatusType command;
	private int length1;
	private int length2;
	private String key;
	private String value;
	private boolean twoCommands;

	//TODO implement 0 parameters (and change name of 'key' and 'value'). Connect and disconnect related messages
	public KVQuery(byte[] bytes) throws InvalidMessage {
		int index = 0;
		String command = getString(bytes, index, 2);
		String length1 = getString(bytes, index, 512);
		String length2;
		
		this.twoCommands = false;
		//TODO handle exception
		this.length1 = new Integer(length1);
		
		setType(command);
		if (this.command.equals(StatusType.PUT) ||
				this.command.equals(StatusType.PUT_SUCCESS) ||
				this.command.equals(StatusType.PUT_UPDATE)  ||
				this.command.equals(StatusType.PUT_ERROR)) {
			this.twoCommands = true;
		}
		
		if (this.twoCommands) {
			length2 = getString(bytes, index, 512);
			//TODO handle exception
			this.length2 = new Integer(length2);
			this.key = getString(bytes, index, this.length1);
			this.value = getString(bytes, index, this.length2);
		} else {
			this.key = getString(bytes, index, this.length1);
			this.value = null;
		}
	}
	
	public StatusType getCommand() {
		return this.command;
	}
	
	public String getKey() {
		return this.key;
	}
	
	public String getValue() throws InvalidMessage {
		if (this.value == null) {
			throw new InvalidMessage();
		}
		return this.value;
	}

	private String getString(byte[] bytes, int index, int length) {
		//TODO handle exception or throw and handle in the constructor
		String tmp = new String(bytes, index, length);
		index += length;
		return tmp;
	}

	private void setType(String command) {
		switch (command) {
		case "GT":
			this.command = StatusType.GET;
			break;
		case "GE":
			this.command = StatusType.GET_ERROR;
			break;
		case "GS":
			this.command = StatusType.GET_SUCCESS;
			break;
		case "PT":
			this.command = StatusType.PUT;
			break;
		case "PS":
			this.command = StatusType.PUT_SUCCESS;
			break;
		case "PU":
			this.command = StatusType.PUT_UPDATE;
			break;
		case "PE":
			this.command = StatusType.PUT_ERROR;
			break;
		case "DS":
			this.command = StatusType.DELETE_SUCCESS;
			break;
		case "DE":
			this.command = StatusType.DELETE_ERROR;
			break;
		case "EE":
			this.command = StatusType.ERROR;
			break;
		}
	}

}