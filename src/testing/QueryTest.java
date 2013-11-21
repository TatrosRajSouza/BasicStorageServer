package testing;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import common.messages.InvalidMessageException;
import common.messages.KVQuery;
import common.messages.KVMessage.StatusType;
import junit.framework.TestCase;

public class QueryTest extends TestCase{
	
	@BeforeClass
	public void testSetEncoding() {
		System.setProperty("file.encoding", "US-ASCII");
	}
	
	@Test
	public void testCreateNoArgumentQueryFromByteArraySuccess() {
		KVQuery kvQuery = null;
		byte[] bytes;
		String message = "CN\n\r";
		InvalidMessageException ex = null;
		
		bytes = message.getBytes();
		try {
			kvQuery = new KVQuery(bytes);
		} catch (InvalidMessageException e) {
			ex = e;
		}
		
		assertNull(ex);
		assertEquals(kvQuery.getCommand(), StatusType.CONNECT);
	}
	
	@Test
	public void testCreateOneArgumentQueryFromByteArraySuccess() {
		KVQuery kvQuery = null;
		byte[] bytes;
		String message = "GT\nfoo\n\r";
		InvalidMessageException ex = null;
		
		bytes = message.getBytes();
		try {
			kvQuery = new KVQuery(bytes);
		} catch (InvalidMessageException e) {
			ex = e;
		}
		
		assertNull(ex);
		assertEquals(kvQuery.getCommand(), StatusType.GET);
		try {
			assertEquals(kvQuery.getKey(), "foo");
		} catch (InvalidMessageException e) {
			ex = e;
		}
		assertNull(ex);
	}
	
	@Test
	public void testCreateTwoArgumentQueryFromByteArraySuccess() {
		KVQuery kvQuery = null;
		byte[] bytes;
		String message = "PT\nfoo\nbar\n\r";
		Exception ex = null;
		
		bytes = message.getBytes();
		try {
			kvQuery = new KVQuery(bytes);
		} catch (InvalidMessageException e) {
			ex = e;
		}
		
		assertNull(ex);
		assertEquals(kvQuery.getCommand(), StatusType.PUT);
		try {
			assertEquals(kvQuery.getKey(), "foo");
		} catch (InvalidMessageException e) {
			ex = e;
		}
		assertNull(ex);
	}
	
	@Test
	public void testCreateQueryUnknownCommandFromByteArray() {
		byte[] bytes;
		String message = "as\nfoo\nbar\n\r";
		Exception ex = null;
		
		bytes = message.getBytes();
		try {
			new KVQuery(bytes);
		} catch (InvalidMessageException e) {
			ex = e;
		}
		
		assertTrue(ex instanceof InvalidMessageException);
	}
	
	@Test
	public void testCreateQueryIncorrectSizeCommandFromByteArray() {
		byte[] bytes;
		String message = "as\nfoo\nbar\nasuh\n\r";
		Exception ex = null;
		
		bytes = message.getBytes();
		try {
			new KVQuery(bytes);
		} catch (InvalidMessageException e) {
			ex = e;
		}
		
		assertTrue(ex instanceof InvalidMessageException);
	}
	
	@Test
	public void testCreateNoArgumentQueryFromArgumentsSuccess() {
		KVQuery kvQuery = null;
		Exception ex = null;
		
		try {
			kvQuery = new KVQuery(StatusType.CONNECT);
		} catch (InvalidMessageException e) {
			ex = e;
		}
		
		assertNull(ex);
		assertEquals(kvQuery.getCommand(), StatusType.CONNECT);
	}
	
	@Test
	public void testCreateKeyArgumentQueryFromArgumentsSuccess() {
		KVQuery kvQuery = null;
		Exception ex = null;
		String key = null;
		
		try {
			kvQuery = new KVQuery(StatusType.GET, "foo");
			key = kvQuery.getKey();
		} catch (InvalidMessageException e) {
			ex = e;
		}
		
		assertNull(ex);
		assertEquals(kvQuery.getCommand(), StatusType.GET);
		assertEquals(key, "foo");
	}
	
	@Test
	public void testCreateMessageQueryFromArgumentsSuccess() {
		KVQuery kvQuery = null;
		Exception ex = null;
		String message = null;
		
		try {
			kvQuery = new KVQuery(StatusType.CONNECT_SUCCESS, "conexion established");
			message = kvQuery.getTextMessage();
		} catch (InvalidMessageException e) {
			ex = e;
		}
		
		assertNull(ex);
		assertEquals(kvQuery.getCommand(), StatusType.CONNECT_SUCCESS);
		assertEquals(message, "conexion established");
	}
	
	@Test
	public void testCreateKeyValueArgumentQueryFromArgumentsSuccess() {
		KVQuery kvQuery = null;
		Exception ex = null;
		String key = null;
		String value = null;
		
		try {
			kvQuery = new KVQuery(StatusType.PUT, "foo", "bar");
			key = kvQuery.getKey();
			value = kvQuery.getValue();
		} catch (InvalidMessageException e) {
			ex = e;
		}
		
		assertNull(ex);
		assertEquals(kvQuery.getCommand(), StatusType.PUT);
		assertEquals(key, "foo");
		assertEquals(value, "bar");
	}
	
	@Test
	public void testCreateNoArgumentQueryFromArgumentsFail() {
		Exception ex = null;
		
		try {
			new KVQuery(StatusType.GET_ERROR);
		} catch (InvalidMessageException e) {
			ex = e;
		}
		
		assertTrue(ex instanceof InvalidMessageException);
	}

	@Test
	public void testCreateKeyArgumentQueryFromArgumentsFail() {
		Exception ex = null;
		
		try {
			new KVQuery(StatusType.GET_SUCCESS, "foo");
		} catch (InvalidMessageException e) {
			ex = e;
		}
		
		assertTrue(ex instanceof InvalidMessageException);
	}
	
	@Test
	public void testCreateMessageQueryFromArgumentsFail() {
		Exception ex = null;
		
		try {
			new KVQuery(StatusType.PUT_SUCCESS, "conexion established");
		} catch (InvalidMessageException e) {
			ex = e;
		}
		
		assertTrue(ex instanceof InvalidMessageException);
	}
	
	@Test
	public void testCreateKeyValueArgumentQueryFromArgumentsFail() {
		Exception ex = null;
		
		try {
			new KVQuery(StatusType.CONNECT_ERROR, "foo", "bar");
		} catch (InvalidMessageException e) {
			ex = e;
		}
		
		assertTrue(ex instanceof InvalidMessageException);
	}
	
	@Test
	public void testToBytesNoArgumentSucces() {
		KVQuery kvQuery = null;
		byte[] bytes = null;
		Exception ex = null;

		try {
			kvQuery = new KVQuery(StatusType.CONNECT);
		} catch (InvalidMessageException e) {
			ex = e;
		}
		bytes = kvQuery.toBytes();
		
		assertNull(ex);
		assertTrue(Arrays.equals(bytes, "CN\n\r".getBytes()));
	}
}