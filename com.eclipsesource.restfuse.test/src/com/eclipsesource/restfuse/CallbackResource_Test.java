package com.eclipsesource.restfuse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;


public class CallbackResource_Test {
  
  @Test
  public void testCreateResponse() {
    Map<String, List<String>> headers = new HashMap<String, List<String>>();
    
    Response response 
      = CallbackResource.createResponse( Status.OK, MediaType.WILDCARD, "body", headers );
    
    assertEquals( Status.OK.getStatusCode(), response.getStatus() );
    assertEquals( MediaType.WILDCARD, response.getType() );
    assertEquals( "body", response.getBody( String.class ) );
    assertEquals( headers, response.getHeaders() );
  }
  
  @Test
  public void testCreateResponseCreatesHeaderSaveCopy() {
    Map<String, List<String>> headers = new HashMap<String, List<String>>();
    
    Response response 
      = CallbackResource.createResponse( Status.OK, MediaType.WILDCARD, "body", headers );
    
    assertNotSame( headers, response.getHeaders() );
  }
  
  @Test
  public void testCreateResponseHasBody() {
    Map<String, List<String>> headers = new HashMap<String, List<String>>();
    
    Response response 
      = CallbackResource.createResponse( Status.OK, MediaType.WILDCARD, "body", headers );
    
    assertTrue( response.hasBody() );
  }
  
  @Test
  public void testCreateResponseHasNoBody() {
    Response response 
    = CallbackResource.createResponse( Status.OK, MediaType.WILDCARD, null, null );
    
    assertFalse( response.hasBody() );
  }
  
  @Test
  public void testCreateResponseHasNoHeaders() {
    Response response 
    = CallbackResource.createResponse( Status.OK, MediaType.WILDCARD, null, null );
    
    assertNull( response.getHeaders() );
  }
  
  @Test
  public void testCreateResponseCreatesNewInstance() {
    Map<String, List<String>> headers = new HashMap<String, List<String>>();
    
    Response response 
      = CallbackResource.createResponse( Status.OK, MediaType.WILDCARD, "body", headers );
    Response response2 
      = CallbackResource.createResponse( Status.OK, MediaType.WILDCARD, "body", headers );
    
    assertNotSame( response, response2 );
  }
  
  
}
