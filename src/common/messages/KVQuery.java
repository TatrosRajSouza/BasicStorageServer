package common.messages;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import common.messages.KVMessage.StatusType;

public class KVQuery {
	private StatusType command;
	private String key;
	private String value;
	private String[] arguments;
	private int index;
	private boolean twoCommands;
	private static final int BUFFER_SIZE = 1024;
	private static final int DROP_SIZE = 128 * BUFFER_SIZE;
	private static final String LINE_FEED = "\n";
	private static final String RETURN = "\r";

	//TODO implement 0 parameters (and change name of 'key' and 'value'). Connect and disconnect related messages
	public KVQuery(byte[] bytes) throws InvalidMessage {
		String arguments;

		//TODO put this in a better place. In the main() 
		System.setProperty("file.encoding", "US-ASCII");

		index = 0;
		this.twoCommands = false;

		arguments = new String(bytes);
		this.arguments = arguments.split("\n");

		if (this.arguments.length > 0 && this.arguments[this.arguments.length - 1].equals(RETURN)) {
			setType(this.arguments[index++]);

			if (this.command.equals(StatusType.PUT) ||
					this.command.equals(StatusType.PUT_SUCCESS) ||
					this.command.equals(StatusType.PUT_UPDATE)  ||
					this.command.equals(StatusType.PUT_ERROR)) {
				this.twoCommands = true;
			}

			if (twoCommands) {
				if (this.arguments.length != 4) {
					this.command = StatusType.ERROR;
				}
				this.key = this.arguments[index++];
				this.value = this.arguments[index++];
			} else {
				if (this.arguments.length != 3) {
					this.command = StatusType.ERROR;
				}
				this.key = this.arguments[index++];
				this.value = null;
			}
		} else {
			this.command = StatusType.ERROR;
		}
	}
	
	public KVQuery(StatusType command, String argument) {
		this.command = command;
		this.key = argument;
		this.twoCommands = false;
	}
	
	public KVQuery(StatusType command, String argument1, String argument2) {
		this.command = command;
		this.key = argument1;
		this.value = argument2;
		this.twoCommands = false;
	}

	//TODO handle the Exception, instead of throwing it
	public byte[] toBytes() throws UnsupportedEncodingException {
		ByteBuffer byteBuffer = ByteBuffer.allocate(DROP_SIZE);

		byteBuffer.put(command.toString().getBytes());
		byteBuffer.put(LINE_FEED.getBytes());
		//bytes.putInt(length1);
		byteBuffer.put(key.getBytes());
		byteBuffer.put(LINE_FEED.getBytes());
		if (twoCommands) {
			//bytes.putInt(length2);
			byteBuffer.put(LINE_FEED.getBytes());
			byteBuffer.put(value.getBytes());
		}
		byteBuffer.put(LINE_FEED.getBytes());
		byteBuffer.put(RETURN.getBytes());

		return byteBuffer.array();
	}
	
	public KVQuery() {
		// TODO Auto-generated constructor stub
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