package common.messages;

import java.nio.BufferOverflowException;

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

	private static final int BUFFER_SIZE = 1024;
	private static final int DROP_SIZE = 128 * BUFFER_SIZE;

	private static final String LINE_FEED = "\n";
	private static final String RETURN = "\r";

	private static Logger logger = Logger.getRootLogger();

	/**
	 * Construct a query from a message received in the form of an array of bytes
	 * @param bytes
	 * @throws InvalidMessageException
	 */
	public KVQuery(byte[] bytes) throws InvalidMessageException {
		String message;
		String[] arguments;
		int index = 0;
		//TODO put this in Client and Server main
		//System.setProperty("file.encoding", "US-ASCII");

		message = new String(bytes);
		arguments = message.split("\n");

		if (arguments.length >= 2 && arguments.length <= 4
				&& arguments[arguments.length - 1].equals(RETURN)
				&& bytes.length <= DROP_SIZE) {
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
			throw new InvalidMessageException("Incorrect number of arguments or size of message too big "
					+ "or message not finishing with \"\\r\\n\"");
		}
	}

	/**
	 * Constructs an query with no argument.
	 * @param command the type of the query
	 * @throws InvalidMessageException thrown when a command that is not associated with exactly one argument is entered
	 */
	public KVQuery(StatusType command) throws InvalidMessageException {
		if (command != StatusType.CONNECT && command != StatusType.CONNECT_ERROR
				&& command != StatusType.DISCONNECT && command != StatusType.DISCONNECT_SUCCESS)
			throw new InvalidMessageException("Incorrect number of arguments for the command");
		this.command = command;
	}

	/**
	 * Constructs an query with only one argument.
	 * @param command the type of the query
	 * @param argument  may contain the key (key-value) of the query or the message from a connection. Depends on the command. 
	 * @throws InvalidMessageException thrown when a command that is not associated with exactly one argument is entered
	 */
	public KVQuery(StatusType command, String argument) throws InvalidMessageException {
		if (command != StatusType.GET && command != StatusType.GET_ERROR
				&& command != StatusType.CONNECT_SUCCESS && command != StatusType.FAILED)
			throw new InvalidMessageException("Incorrect number of arguments for the command");
		this.command = command;
		this.key = argument;
	}

	/**
	 * Constructs an query with a key and value.
	 * @param command the type of the query
	 * @param key the key (key-value) of the query
	 * @param value the value (key-value) of the query
	 * @throws InvalidMessageException thrown when a command associated with less than two arguments is entered
	 */
	public KVQuery(StatusType command, String key, String value) throws InvalidMessageException {
		if (command != StatusType.GET_SUCCESS
				&& command != StatusType.PUT			&& command != StatusType.PUT_SUCCESS
				&& command != StatusType.PUT_UPDATE		&& command != StatusType.PUT_ERROR
				&& command != StatusType.DELETE_SUCCESS && command != StatusType.DELETE_ERROR) {
			throw new InvalidMessageException("Incorrect number of arguments for the command.");
		}
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
		String message;
		int length = getCorrectLength(command);

		try {
			message = command.toString() + LINE_FEED;
			if (length >= 3) {
				message += key + LINE_FEED;
				if (length == 4) {
					message += value + LINE_FEED;
				}
			}
			message += RETURN;
		} catch (BufferOverflowException ex) {
			logger.error("Error! Unable to marshal the message. Buffer full. \n", ex);
			return null;
		}

		return message.getBytes();
	}

	/**
	 * Get the type of command the message is
	 * @return the command of the message
	 */
	public StatusType getCommand() {
		return this.command;
	}

	/**
	 * Get the key of a key-value query
	 * @return the key of a key-value query
	 * @throws InvalidMessageException if the query does not has a key
	 */
	public String getKey() throws InvalidMessageException {
		if (command.equals(StatusType.CONNECT) || command.equals(StatusType.CONNECT_ERROR)
				|| command.equals(StatusType.DISCONNECT) || command.equals(StatusType.DISCONNECT_SUCCESS)) {
			throw new InvalidMessageException("This command doesn't have a key. " + command.toString());
		}
		return this.key;
	}

	/**
	 * Get a text message from connection established or failed message sent from server
	 * @return text message from connection established or failed message
	 * @throws InvalidMessageException if the query is not of the types CONNECT or FAILED 
	 */
	public String getTextMessage() throws InvalidMessageException {
		if (!(command.equals(StatusType.CONNECT_SUCCESS) || command.equals(StatusType.FAILED))) {
			throw new InvalidMessageException("This command doesn't have a text message. " + command.toString());
		}
		return this.key;
	}

	/**
	 * Get the value of a key-value query
	 * @return the value of a key-value query
	 * @throws InvalidMessageException if the message does not has a value
	 */
	public String getValue() throws InvalidMessageException {
		if (this.value == null) {
			throw new InvalidMessageException("This command doesn't have a value. " + command.toString());
		}
		return this.value;
	}

	private void setType(String command) throws InvalidMessageException {
		try {
			this.command = StatusType.valueOf(command);
		} catch (Exception ex) {
			throw new InvalidMessageException("This code does not represent a command.");	
		}
	}

	private int getCorrectLength(StatusType command) {
		if (command.equals(StatusType.CONNECT) || command.equals(StatusType.CONNECT_ERROR)
				|| command.equals(StatusType.DISCONNECT) || command.equals(StatusType.DISCONNECT_SUCCESS)) {
			return 2;
		} else if (command.equals(StatusType.GET) || command.equals(StatusType.GET_ERROR)
				|| command.equals(StatusType.CONNECT_SUCCESS) || command.equals(StatusType.FAILED)) {
			return 3;
		}
		return 4;
	}
}