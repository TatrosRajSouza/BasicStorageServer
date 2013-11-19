package app_kvServer;

import java.util.concurrent.ConcurrentHashMap;



public class KVData {
private ConcurrentHashMap<String, String> dataStore = new ConcurrentHashMap<String, String>();
public KVData()
{
	
}

public String put(String key, String value) throws Exception {
	// TODO Auto-generated method stub
	String returnValue = null;
	if(value!= null)
	{
	returnValue = dataStore.putIfAbsent(key, value);
	if(returnValue == null)
	{
		returnValue = dataStore.replace(key, value);
		if(returnValue != null)
		returnValue = dataStore.get(key);
	}
	}
	else
	{
		returnValue = dataStore.remove(key);
	}
	return returnValue;
	
}


public String get(String key) throws Exception {
	// TODO Auto-generated method stub
	return dataStore.get(key);
}
}
