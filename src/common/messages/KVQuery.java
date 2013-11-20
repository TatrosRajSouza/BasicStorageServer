package common.messages;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import common.messages.KVMessage.StatusType;

/**
 * Create a message from or to client/server.
 * Implements the protocol established for this application
 * @author Claynon de Souza
 *
 */
public class KVQuery {
	private StatusType command;
	private String key;
	private String value;
	private String[] arguments;
	private int index;
	private static final int BUFFER_SIZE = 1024;
	private static final int DROP_SIZE = 128 * BUFFER_SIZE;
	private static final String LINE_FEED = "\n";
	private static final String RETURN = "\r";
	private static Logger logger = Logger.getRootLogger();

	//TODO implement 0 parameters (and change name of 'key' and 'value'). Connect and disconnect related messages
	public KVQuery(byte[] bytes) throws InvalidMessage {
		String message;

		//TODO put this in a better place. In the main() 
		System.setProperty("file.encoding", "US-ASCII");

		index = 0;

		message = new String(bytes);
		arguments = message.split("\n");

		if (arguments.length >= 2 && arguments.length <= 4
				&& arguments[arguments.length - 1].equals(RETURN)) {
			setType(arguments[index++]);

			if (arguments.length == 4) {
				key = arguments[index++];
				value = arguments[index++];
			} else if (arguments.length == 3) {
				key = arguments[index++];
				value = null;
			} else {
				key = null;
				value = null;
			}
		} else {
			command = StatusType.ERROR;
		}
	}
	
	/**
	 * Constructs an query with only one argument.
	 * @param command the type of the query
	 * @param argument  may contain the key (key-value) of the query or the message from a connection. Depends on the command. 
	 * @throws InvalidMessage thrown when a command that is not associated with exactly one argument is entered
	 */
	public KVQuery(StatusType command, String argument) throws InvalidMessage {
		if (command != StatusType.GET && command != StatusType.CONNECT)
		this.command = command;
		this.key = argument;
	}
	
	/**
	 * Constructs an query with a key and value.
	 * @param command the type of the query
	 * @param key the key (key-value) of the query
	 * @param value the value (key-value) of the query
	 * @throws InvalidMessage thrown when a command associated with less than two arguments is entered
	 */
	public KVQuery(StatusType command, String key, String value) throws InvalidMessage {
		if (command != StatusType.GET_ERROR				&& command != StatusType.GET_SUCCESS
				&& command != StatusType.PUT			&& command != StatusType.PUT_SUCCESS
				&& command != StatusType.PUT_UPDATE		&& command != StatusType.PUT_ERROR
				&& command != StatusType.DELETE_SUCCESS && command != StatusType.DELETE_ERROR)
		this.command = command;
		this.key = key;
		this.value = value;
	}
	
	/**
	 * Transform the query to an array of bytes to be sent to a client or server.
	 * Marshalling method.
	 * 
	 * @return an array of bytes with the query ready to be sent. Returns null if an error occurs with the buffer.
	 */
	public byte[] toBytes() {
		ByteBuffer byteBuffer = null;
		try {
			byteBuffer = ByteBuffer.allocate(DROP_SIZE);

			byteBuffer.put(command.toString().getBytes());
			byteBuffer.put(LINE_FEED.getBytes());
	
			if (arguments.length >= 3)
			byteBuffer.put(key.getBytes());
			byteBuffer.put(LINE_FEED.getBytes());
			
			if (arguments.length == 4) {
				byteBuffer.put(value.getBytes());
				byteBuffer.put(LINE_FEED.getBytes());
			}
			
			byteBuffer.put(RETURN.getBytes());
		} catch (BufferOverflowException ex) {
			logger.error("Error! Unable to marshal the message. Buffer full. \n", ex);
		}
		
		return byteBuffer.array();
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
		case "CN":
			this.command = StatusType.CONNECT;
			break;
		case "CS":
			this.command = StatusType.CONNECT_SUCCESS;
			break;
		case "CE":
			this.command = StatusType.CONNECT_ERROR;
			break;
		case "DN":
			this.command = StatusType.DISCONNECT;
			break;
		case "DC":
			this.command = StatusType.DISCONNECT_SUCCES;
			break;
		case "EE":
			this.command = StatusType.ERROR;
			break;
		}
	}

}