package app_kvClient;

import java.io.IOException;
import java.net.UnknownHostException;

import logger.LogSetup;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import common.messages.InvalidMessageException;

import client.KVCommunication;
import client.KVStore;

public class KVClient {
	private static Logger logger = Logger.getRootLogger();
	private KVStore kvStore = null;
	KVCommunication connection = null;
	
    /**
     * Main entry point for the KVClient application. 
     */
    public static void main(String[] args) {
    	try {
    		System.setProperty("file.encoding", "US-ASCII");
			new LogSetup("logs/client.log", Level.ALL);
		} catch (IOException e) {
			System.out.println("Error! Unable to initialize logger!");
			e.printStackTrace();
			System.exit(1);
		} catch (SecurityException ex) {
			System.out.println("Error! Unable to set enconding to ASCII.");
			ex.printStackTrace();
			System.exit(1);
		}
    	
    	try {
    		Shell shell = new Shell(new KVClient());
    		shell.display();
    	} catch (Exception ex)	{
    		ex.printStackTrace();
    		System.exit(1);
    	}
    }
    
    public static Logger getLogger()
    {
    	return logger;
    }
    
	public void connect(String address, int port) throws IOException, UnknownHostException {
		this.kvStore = new KVStore(address, port);
		try {
			kvStore.connect();
		} catch (InvalidMessageException ex) {
			System.out.println("Unable to connect to server. Received an invalid message: \n" + ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	public void disconnect() {
		kvStore.disconnect();
	}
	
	public void put(String key, String value) {
		kvStore.put(key, value);
	}
	
	public void get(String key) {
		kvStore.get(key);
	}
}