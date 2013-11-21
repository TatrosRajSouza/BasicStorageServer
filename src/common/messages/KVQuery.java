package common.messages;

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
	private int numArgs;
	
	private String[] arguments;
	
	private static final int BUFFER_SIZE = 1024;
	private static final int DROP_SIZE = 128 * BUFFER_SIZE;
	
	private static Logger logger = Logger.getRootLogger();

	/**
	 * Construct a query from a message received in the form of an array of bytes
	 * @param bytes
	 * @throws InvalidMessageException
	 */
	public KVQuery(byte[] bytes) throws InvalidMessageException {
		String message;
		//TODO put this in Client and Server main
		//System.setProperty("file.encoding", "US-ASCII");

		/* Note: Converting the Bytes back to strings didn't work,
		 * since it would always fail at reading the carriage return (\r).
		 * Somehow the carriage return gets lost somewhere during the conversion process, 
		 * I'm not 100% sure where it happens.
		 * 
		 * As the carriage return is missing it would always drop to the else case 
		 * and throw an invalid message exception. This should have been obvious while testing the code.
		 * 
		 * I removed the carriage return convention, as I don't see its use anyway.
		 * The format is now simply <Command>\n [<Argument 1>\n...<Argument n>]
		 */
		
		message = new String(bytes);
		arguments = message.split("\n");

		/* Note: What about messages of length 2? e.g. Command + Message such as CONNECT_SUCCESS <MSG>. Not handled.
		 * Changed the argument checking.
		 */
		
		if (arguments.length >= 1 && arguments.length <= 3 && bytes.length <= DROP_SIZE) {
			setType(arguments[0]);

			if (arguments.length == 1) {
				key = "";
				value = "";
			} else if (arguments.length == 2) {
				key = arguments[1];
				value = "";
			} else if (arguments.length == 3) {
				key = arguments[1];
				value = arguments[2];
			}
		} else {
			throw new InvalidMessageException("Incorrect number of arguments or size of message exceeded.");
		}
	}
	
	/**
	 * Constructs an query that consists only of a command.
	 * @param command the type of the query
	 * @throws InvalidMessageException thrown when a command that is not associated with exactly one argument is entered
	 */
	public KVQuery(StatusType command) throws InvalidMessageException {
		if (command != StatusType.CONNECT && command != StatusType.CONNECT_ERROR
				&& command != StatusType.DISCONNECT)
			throw new InvalidMessageException("Incorrect number of arguments or unknown command.");
		
		/* Note: I don't understand why there is no Constructor for KVQueries that have no arguments, only the command.
		 * Since this type of message is clearly specified in your protocol (e.g. CONNECT and DISCONNECT).
		 * So I added this.
		 */
		
		this.command = command;
		this.numArgs = 1;
	}
	
	/**
	 * Constructs an query with only one argument.
	 * @param command the type of the query
	 * @param argument  may contain the key (key-value) of the query or the message from a connection. Depends on the command. 
	 * @throws InvalidMessageException thrown when a command that is not associated with exactly one argument is entered
	 */
	public KVQuery(StatusType command, String argument) throws InvalidMessageException {
		if (command != StatusType.GET && command != StatusType.GET_ERROR && command != StatusType.GET_SUCCESS
				&& command != StatusType.FAILED && command != StatusType.CONNECT_SUCCESS)
			throw new InvalidMessageException("Incorrect number of arguments for the command");
		
		/* Note: This was missing CONNECT_SUCCESS and GET_SUCCESS message since it is 
		 * specified in protocol as 2 argument Type Command.
		 * Furthermore the CONNECT message is a Command without arguments so I removed it. 
		 */
		
		this.command = command;
		this.key = argument;
		this.numArgs = 2;
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
		this.numArgs = 3;
	}
	
	/**
	 * Transform the query to an array of bytes to be sent to a client or server.
	 * Marshalling method.
	 * 
	 * @return an array of bytes with the query ready to be sent. Returns null if an error occurs with the buffer.
	 */
	public byte[] toBytes() {
		/* Note: This was completely broken. Would throw NullReferenceExceptions every single time, since arguments[] 
		 * array was not even initialized at this point as it isn't even used for the creation of KVQuery Objects.
		 * Only command, key and value are used.  
		 */
		
		/* logger.info("Command is: " + command.toString()
				+ "\nKey is: " + key
				+ "\nValue is: " + value);
		*/
		
		byte[] bytes;
		if (numArgs == 1) {
			String message = command.toString() + "\r";
			// logger.debug("converting to bytes: " + command.toString());
			bytes = message.getBytes();
		} else if (numArgs == 2) {
			String message = command.toString() + "\n" + key + "\r";
			// logger.debug("converting to bytes: " + command.toString() + "\n" + key);
			bytes = message.getBytes();
		} else if (numArgs == 3) {
			String message = command.toString() + "\n" + key + "\n" + value + "\r";
			// logger.debug("converting to bytes: " + command.toString() + "\n" + key + "\n" + value);
			bytes = message.getBytes();
		} else {
			logger.error("Cannot convert KVQuery to bytes, since it has an incorrect number of arguments. (" + numArgs + ")");
			return null;
		}
		
		if (bytes.length > DROP_SIZE) {
			logger.error("Cannot convert KVQuery to bytes, since the payload would be too large.\n"
					+ "  Payload: " + bytes.length / 1024 + " kb"
					+ "  Maxmium allowed: " + DROP_SIZE / 1024 + " kb");
			return null;
		}
		
		return bytes;
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
		return this.key;
	}

	/**
	 * Get a text message from connection established or failed message sent from server
	 * @return text message from connection established or failed message
	 * @throws InvalidMessageException if the query is not of the types CONNECT or FAILED 
	 */
	public String getTextMessage() throws InvalidMessageException {
		/* Note: Again you confused CONNECT with CONNECT_SUCCESS. Changed it. */
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
	
		/* Note: What? What's up with the random case constants. Where are you even setting GT, GE.... and so on?
		 * These constants are never part of any messages or appear anywhere else in the code.
		 * I'm always dropping to the default case ("This code does not represent a command") because of that. 
		 * I changed it to the actual commands. At least it works that way. 
		 */
		
		switch (command) {
		case "GET":
			this.command = StatusType.GET;
			break;
		case "GET_ERROR":
			this.command = StatusType.GET_ERROR;
			break;
		case "GET_SUCCESS":
			this.command = StatusType.GET_SUCCESS;
			break;
		case "PUT":
			this.command = StatusType.PUT;
			break;
		case "PUT_SUCCESS":
			this.command = StatusType.PUT_SUCCESS;
			break;
		case "PUT_UPDATE":
			this.command = StatusType.PUT_UPDATE;
			break;
		case "PUT_ERROR":
			this.command = StatusType.PUT_ERROR;
			break;
		case "DELETE_SUCCESS":
			this.command = StatusType.DELETE_SUCCESS;
			break;
		case "DELETE_ERROR":
			this.command = StatusType.DELETE_ERROR;
			break;
		case "CONNECT":
			this.command = StatusType.CONNECT;
			break;
		case "CONNECT_SUCCESS":
			this.command = StatusType.CONNECT_SUCCESS;
			break;
		case "CONNECT_ERROR":
			this.command = StatusType.CONNECT_ERROR;
			break;
		case "DISCONNECT":
			this.command = StatusType.DISCONNECT;
			break;
		case "DISCONNECT_SUCCES":
			this.command = StatusType.DISCONNECT_SUCCES;
			break;
		case "FAILED":
			this.command = StatusType.FAILED;
			break;
		default:
			throw new InvalidMessageException("This code does not represent a command.");	
		}
	}
}