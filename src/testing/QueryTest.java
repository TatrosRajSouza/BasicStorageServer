package testing;

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
	public void testCreateQueryUnknownCommandFromByte() {
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
	public void testCreateQueryIncorrectSizeCommandFromByte() {
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
}
