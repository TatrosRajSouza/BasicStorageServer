package testing;

import static org.junit.Assert.*;

import org.junit.Test;

import app_kvServer.KVData;

public class KVDataTest {

	KVData kvData = new KVData();

	@Test
	public void testPut() {
		String returnValue1 = kvData.put("name1","jona");
		assertTrue( returnValue1 == null);
		String returnValue2 = kvData.put("name2","gull");
		assertTrue(returnValue2 == null);
	}

	@Test
	public void testGet() {
		String returnValue1 = kvData.put("name1","jona");
		String returnValue2 = kvData.put("name2","gull");
		assertTrue(kvData.get("name1").equals("jona"));
		assertTrue(kvData.get("name2").equals("gull"));
	}
	@Test
	public void testUpdate() {
		String returnValue1 = kvData.put("name1","jona");
		String returnValue2 = kvData.put("name2","gull");
		String returnValue = kvData.put("name2","seagull");
		assertTrue( returnValue.equals("seagull"));
		assertTrue(kvData.get("name2").equals("seagull"));
	}
	@Test
	public void testDelete() {
		String returnValue1 = kvData.put("name1","jona");
		String returnValue2 = kvData.put("name2","gull");
		String returnValu3 = kvData.put("name2","seagull");
		String returnValue = kvData.put("name2",null);
		assertTrue( returnValue.equals("seagull"));
		assertTrue(kvData.get("name2") == null);
	}

}
