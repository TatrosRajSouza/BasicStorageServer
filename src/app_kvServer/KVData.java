package app_kvServer;

import java.util.concurrent.ConcurrentHashMap;



public class KVData {
	public ConcurrentHashMap<String, String> dataStore = new ConcurrentHashMap<String, String>();
	public KVData()
	{

	}

	public String put(String key, String value) {
		String returnValue = null;
		if(value!= null)
		{
			  returnValue  = "putfail";
			if(dataStore.putIfAbsent(key, value) == null)
				returnValue = "put";
			else
			{
				returnValue= "replacefail";
				if(dataStore.replace(key, value)!=null)
					returnValue = dataStore.get(key);
			}
		}
		else
		{
			returnValue = "deletefail";
			dataStore.remove(key);
			returnValue = "deleted";
			
		}
		return returnValue;

	}


	public String get(String key)  {
		// TODO Auto-generated method stub
		return dataStore.get(key);
	}
}
