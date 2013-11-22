package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;
import app_kvClient.KVClient;
import app_kvClient.SocketStatus;

public class KVCommunication {
	private Logger logger;
	private Socket clientSocket;
	private SocketStatus socketStatus;
	private OutputStream output;
 	private InputStream input;
 	
 	private static final int TIMEOUT_MS = 3000;
	private static final int BUFFER_SIZE = 1024;
	private static final int DROP_SIZE = 1024 * BUFFER_SIZE;
	
	public KVCommunication(String address, int port) throws UnknownHostException, IOException {
		logger = KVClient.getLogger();
		connect(address, port);
	}
	
	private void connect(String address, int port) throws UnknownHostException, IOException, SocketTimeoutException
	{
		clientSocket = new Socket(address, port);
		clientSocket.setSoTimeout(TIMEOUT_MS);
		setSocketStatus(SocketStatus.CONNECTED);
		logger.info("Connection established");
	}
	
	public void closeConnection() {
		logger.info("try to close connection ...");
		
		try {
			tearDownConnection();
			setSocketStatus(SocketStatus.DISCONNECTED);
		} catch (IOException ioe) {
			logger.error("Unable to close connection!");
		}
	}
	
	private void tearDownConnection() throws IOException {
		logger.info("tearing down the connection ...");
		if (clientSocket != null) {
			if (input != null)
				input.close();
			if (output != null)
				output.close();
			
			clientSocket.close();
			clientSocket = null;
			logger.info("Connection closed by communication module!");
		}
	}
	
	/**
	 * Method sends a Message using this socket.
	 * @param msg the message that is to be sent.
	 * @throws IOException some I/O error regarding the output stream 
	 */
	public void sendMessage(byte[] msgBytes) throws IOException, SocketTimeoutException {
		if (msgBytes != null) {
		output = clientSocket.getOutputStream();
		output.write(msgBytes, 0, msgBytes.length);
		output.flush();
		logger.info("SEND \t<" 
				+ clientSocket.getInetAddress().getHostAddress() + ":" 
				+ clientSocket.getPort() + ">: '" 
				+ new String(msgBytes) +"'");
		} else {
			throw new IOException("Unable to transmit message, the message was null.");
		}
	}
	
	public byte[] receiveMessage() throws IOException, SocketTimeoutException {
		input = clientSocket.getInputStream();
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

	public SocketStatus getSocketStatus() {
		return socketStatus;
	}

	private void setSocketStatus(SocketStatus socketStatus) {
		this.socketStatus = socketStatus;
	}
}
