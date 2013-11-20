package app_kvClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import common.messages.InvalidMessage;
import common.messages.KVMessage;
import common.messages.KVQuery;
import app_kvServer.KVData;
/**
 * Represents a connection end point for a particular client that is 
 * connected to the server. This class is responsible for message reception 
 * and sending. 
 * The class also implements the get,put functionality. 
 */
public class KVClient implements Runnable {

	private static Logger logger = Logger.getRootLogger();

	private boolean isOpen;
	private static final int BUFFER_SIZE = 1024;
	private static final int DROP_SIZE = 128 * BUFFER_SIZE;
	private static KVData kvdata = new KVData();
	private Socket clientSocket;
	private InputStream input;
	private OutputStream output;

	/**
	 * Constructs a new CientConnection object for a given TCP socket.
	 * @param clientSocket the Socket object for the client connection.
	 */
	public KVClient(Socket clientSocket) {
		this.clientSocket = clientSocket;
		this.isOpen = true;
	}

	/**
	 * Initializes and starts the client connection. 
	 * Loops until the connection is closed or aborted by the client.
	 */
	public void run() {
		try {
			output = clientSocket.getOutputStream();
			input = clientSocket.getInputStream();
			String connectSuccess = "Connection to MSRG Echo server established: " 
					+ clientSocket.getLocalAddress() + " / "
					+ clientSocket.getLocalPort();
			//need to be edited
			KVQuery kvQueryConnect;
			try {
				kvQueryConnect = new KVQuery(KVMessage.StatusType.CONNECT_SUCCESS,connectSuccess );
				sendMessage(kvQueryConnect.toBytes());
			} catch (InvalidMessage e) {
				// TODO Auto-generated catch block
				logger.error("Invalid connect message");
			}
			while(isOpen) {
				try {
					byte[] latestMsg = receiveMessage();
					KVQuery kvQueryCommand;
					try {
						kvQueryCommand = new KVQuery(latestMsg);
						String key=null,value=null,returnValue;
						if(kvQueryCommand.getCommand().toString().equals("GET"))
						{

							key = kvQueryCommand.getKey();
							try {
								returnValue = kvdata.get(key);
								KVQuery kvQueryGet = new KVQuery(KVMessage.StatusType.GET_SUCCESS,returnValue);
								sendMessage(kvQueryGet.toBytes());
							} catch (Exception e) {
								String errorMsg = "Error in get operation for the key" + key ;
								logger.error(errorMsg);
								sendError(KVMessage.StatusType.GET_ERROR,errorMsg);
								
							}
						}
						else if(kvQueryCommand.getCommand().toString().equals("PUT"))
						{

							key = kvQueryCommand.getKey();
							value = kvQueryCommand.getValue();
							try {
								returnValue = kvdata.put(key,value );
								KVQuery kvQueryPut = new KVQuery(KVMessage.StatusType.PUT_SUCCESS,returnValue);
								sendMessage(kvQueryPut.toBytes());
							} catch (Exception e) {
								String errorMsg = "Error in put operation for Key:"+key + "and value:" + value ;
								logger.error(errorMsg);
								sendError(KVMessage.StatusType.PUT_ERROR,errorMsg);
							}
						}
						else
						{
							
							sendError(KVMessage.StatusType.ERROR,"Invalid command");
						}
					} catch (InvalidMessage e) {
						logger.error("Invalid message received from client");	
						sendError(KVMessage.StatusType.ERROR, "Invalid command");
					}
	
				} catch (IOException ioe) {
					logger.error("Error! Connection lost!");
					isOpen = false;
				}

			}

		} catch (IOException ioe) {
			logger.error("Error! Connection could not be established!", ioe);

		} finally {

			try {
				if (clientSocket != null) {
					input.close();
					output.close();
					clientSocket.close();
				}
			} catch (IOException ioe) {
				logger.error("Error! Unable to tear down connection!", ioe);
			}
		}
	}

	private void sendError(KVMessage.StatusType statusType, String errorMsg) throws UnsupportedEncodingException, IOException {
		// TODO Auto-generated method stub
		KVQuery kvQueryError;
		try {
			kvQueryError = new KVQuery(statusType,errorMsg);
			sendMessage(kvQueryError.toBytes());
		} catch (InvalidMessage e) {
			logger.error("Error in ErrorMessage format");
		}
		
	}

	/**
	 * Method sends a TextMessage using this socket.
	 * @param msg the message that is to be sent.
	 * @throws IOException some I/O error regarding the output stream 
	 */
	public void sendMessage(byte[] msgBytes) throws IOException {
		output.write(msgBytes, 0, msgBytes.length);
		output.flush();
		logger.info("SEND \t<" 
				+ clientSocket.getInetAddress().getHostAddress() + ":" 
				+ clientSocket.getPort() + ">: '" 
				+ new String(msgBytes) +"'");
	}


	private byte[] receiveMessage() throws IOException {

		int index = 0;
		byte[] msgBytes = null, tmp = null;
		byte[] bufferBytes = new byte[BUFFER_SIZE];

		/* read first char from stream */
		byte read = (byte) input.read();	
		boolean reading = true;

		while(read != 13 && reading) {/* carriage return */
			/* if buffer filled, copy to msg array */
			if(index == BUFFER_SIZE) {
				if(msgBytes == null){
					tmp = new byte[BUFFER_SIZE];
					System.arraycopy(bufferBytes, 0, tmp, 0, BUFFER_SIZE);
				} else {
					tmp = new byte[msgBytes.length + BUFFER_SIZE];
					System.arraycopy(msgBytes, 0, tmp, 0, msgBytes.length);
					System.arraycopy(bufferBytes, 0, tmp, msgBytes.length,
							BUFFER_SIZE);
				}

				msgBytes = tmp;
				bufferBytes = new byte[BUFFER_SIZE];
				index = 0;
			} 

			/* only read valid characters, i.e. letters and constants */
			bufferBytes[index] = read;
			index++;

			/* stop reading is DROP_SIZE is reached */
			if(msgBytes != null && msgBytes.length + index >= DROP_SIZE) {
				reading = false;
			}

			/* read next char from stream */
			read = (byte) input.read();
		}

		if(msgBytes == null){
			tmp = new byte[index];
			System.arraycopy(bufferBytes, 0, tmp, 0, index);
		} else {
			tmp = new byte[msgBytes.length + index];
			System.arraycopy(msgBytes, 0, tmp, 0, msgBytes.length);
			System.arraycopy(bufferBytes, 0, tmp, msgBytes.length, index);
		}

		msgBytes = tmp;

		/* build final String */

		logger.info("RECEIVE \t<" 
				+ clientSocket.getInetAddress().getHostAddress() + ":" 
				+ clientSocket.getPort() + ">: '" 
				+ new String(msgBytes) + "'");
		return msgBytes;
	}



}