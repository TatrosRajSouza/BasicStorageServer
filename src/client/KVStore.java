package client;


import java.io.IOException;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;
import app_kvClient.KVClient;
import common.messages.InvalidMessageException;
import common.messages.KVMessage;
import common.messages.KVQuery;
import common.messages.KVMessage.StatusType;

public class KVStore implements KVCommInterface {

	private KVCommunication kvComm;
	private Logger logger;
	private String address;
	private int port;
	
	
	/**
	 * Initialize KVStore with address and port of KVServer
	 * @param address the address of the KVServer
	 * @param port the port of the KVServer
	 */
	public KVStore(String address, int port) {
		this.address = address;
		this.port = port;
		this.logger = KVClient.getLogger();
	}
	
	@Override
	public void connect() throws UnknownHostException, IOException, InvalidMessageException {
		kvComm = new KVCommunication(address, port);
		byte[] connectResponse = kvComm.receiveMessage();
		KVQuery kvQueryMessage = new KVQuery(connectResponse);
		
		if (kvQueryMessage.getCommand() == StatusType.CONNECT_SUCCESS) {
			logger.info("Connected to KVServer");
			logger.info("Server Message: " + kvQueryMessage.getTextMessage());
		}
		
		else if (kvQueryMessage.getCommand() == StatusType.CONNECT_ERROR) {
			logger.error("Unable to connect to KVServer.");
		}
		
		else {
			logger.error("Unknown Message received from KVServer. Type: " + kvQueryMessage.getCommand().toString());
		}
	}

	@Override
	public void disconnect() {
		try {
		kvComm.sendMessage(new KVQuery(StatusType.DISCONNECT).toBytes());
		} catch (IOException ex) {
			logger.error("Unable to send disconnect message, an IO Error occured:\n" + ex.getMessage());
		} catch (InvalidMessageException ex) {
			logger.error("Unable to generate disconnect message, the message type was invalid.");
		}
		
		// TODO: WAIT FOR DISCONNECT_SUCCESS MESSAGE THEN DISCONNECT
		// As this is never sent by the server I simply close the connection for now.
		kvComm.closeConnection();
	}

	@Override
	public KVMessage put(String key, String value) {
		try {
			kvComm.sendMessage(new KVQuery(StatusType.PUT, key, value).toBytes());
			logger.info("Sent PUT Request for <key, value>: <" + key + ", " + value + ">");
		} catch (IOException ex) {
			logger.error("Unable to send put command, an IO Error occured during transmission:\n" + ex.getMessage());
		} catch (InvalidMessageException ex) {
			logger.error("Unable to generate put command, the message type is invalid for the given arguments.");
		}
		
		// Wait for answer
		logger.info("Waiting for PUT response from server..");
		try {
			byte[] putResponse = kvComm.receiveMessage();
			KVQuery kvQueryMessage = new KVQuery(putResponse);
			KVResult kvResult = new KVResult(kvQueryMessage.getCommand(), kvQueryMessage.getKey(), kvQueryMessage.getValue());
			return kvResult;
		} catch (InvalidMessageException ex) {
			logger.error("Unable to generate KVQueryMessage from Server response:\n" + ex.getMessage());
			ex.printStackTrace();
		} catch (IOException ex) {
			logger.error("An IO Exception occured while waiting for PUT response from the server:\n" + ex.getMessage());
			ex.printStackTrace();
		}
		return null;
	}

	@Override
	public KVMessage get(String key) {
		try {
			kvComm.sendMessage(new KVQuery(StatusType.GET, key).toBytes());
			logger.info("Sent GET Request for <key>: <" + key + ">");
		} catch (IOException ex) {
			logger.error("Unable to send get command, an IO Error occured during transmission:\n" + ex.getMessage());
		} catch (InvalidMessageException ex) {
			logger.error("Unable to generate get command, the message type is invalid for the given arguments.");
		}
		
		// Wait for answer
		logger.info("Waiting for GET response from server..");
		try {
			byte[] getResponse = kvComm.receiveMessage();
			KVQuery kvQueryMessage = new KVQuery(getResponse);
			KVResult kvResult = new KVResult(kvQueryMessage.getCommand(), kvQueryMessage.getKey(), kvQueryMessage.getValue());
			return kvResult;
		} catch (InvalidMessageException ex) {
			logger.error("Unable to generate KVQueryMessage from Server response:\n" + ex.getMessage());
			ex.printStackTrace();
		} catch (IOException ex) {
			logger.error("An IO Exception occured while waiting for GET response from the server:\n" + ex.getMessage());
			ex.printStackTrace();
		}
		return null;
	}

	
}
