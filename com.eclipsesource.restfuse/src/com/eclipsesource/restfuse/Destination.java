/*******************************************************************************
 * Copyright (c) 2011 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Holger Staudacher - initial API and implementation
 ******************************************************************************/ 
package com.eclipsesource.restfuse;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.eclipsesource.restfuse.annotation.Callback;
import com.eclipsesource.restfuse.annotation.HttpTest;
import com.eclipsesource.restfuse.internal.HttpTestStatement;

/**
 * <p>A <code>Destination</code> marks a requirement for http tests. Before you can use the 
 * <code>{@link HttpTest}</code> annotation you need to instantiate a <code>Destination</code> in 
 * your TestCase. This rule does manage the <code>{@link HttpTest}</code> and the 
 * <code>{@link Callback}</code> annotations of your test methods.</p>
 * 
 * <p>A <code>Destination</code> needs an url during instantiation. This url will be used within a
 * TestCase to send requests to. If the url is not valid an <code>IllegalArgumentException</code> 
 * will be thrown.</p>
 *
 * <p>A simple test looks like this:
 * <pre>
 * <b>&#064;RunWith( HttpJUnitRunner.class )</b>
 * public class Example {
 * 
 *   <b>&#064;Rule</b>
 *   public Destination destination = new Destination( "http://localhost" );
 *    
 *   <b>&#064;Context</b>
 *   private Response response;
 * 
 *   <b>&#064;HttpTest( method = Method.GET, path = "/test" )</b> 
 *   public void testMethod() {
 *     com.eclipsesource.restfuse.Assert.assertAccepted( response );
 *   }
 * }
 * </pre>
 * </p>
 * 
 * @see HttpTest
 * @see Callback
 */
public class Destination implements TestRule {

  private HttpTestStatement requestStatement;
  private String proxyHost;
  private int proxyPort;
  private RequestContext context;
  private Object testObject;

  /**
   * <p>Constructs a new <code>Destination</code> object. An url is needed as parameter which will
   * be used in the whole test to send requests to.</p>
   * 
   * @param baseUrl The url to send requests to.
   * 
   * @throws IllegalArgumentException Will be thrown when the <code>baseUrl</code> is null or 
   * not a valid url.
   */
  public Destination( Object testObject, String baseUrl) {
    checkBaseUrl(baseUrl);
    checkTestObject( testObject );
    this.testObject = testObject;
    this.context = new RequestContext(testObject, baseUrl);
  }
  
  /**
   * <p>Constructs a new <code>Destination</code> object and sets some proxy properties which will 
   * be used when sending the HTTP request. An url is needed as parameter which will
   * be used in the whole test to send requests to. The passed proxy properties has to conform with
   * the proxy properties described here 
   * http://docs.oracle.com/javase/1.5.0/docs/guide/net/properties.html</p>
   * 
   * @param baseUrl The url to send requests to.
   * @param proxyHost The value of the http.proxyHost property.
   * @param proxyPort The value of the http.proxyPort property.
   * 
   * @throws IllegalArgumentException Will be thrown when the <code>baseUrl</code> is null or 
   * not a valid url.
   */
  public Destination( Object testObject, String baseUrl, String proxyHost, int proxyPort ) {
    this( testObject, baseUrl );
    this.proxyHost = proxyHost;
    this.proxyPort = proxyPort;
  }

  
  /**
   * <p>Constructs a new <code>Destination</code> object. An requestContext is needed as parameter which will
   * be used in the whole test to create requests.</p>
   * 
   * @param requestContext The requestContext containing further Destination parameters.
   * 
   * @throws IllegalArgumentException Will be thrown when the <code>requestContext</code> is null.
   */
  public Destination( Object testObject, RequestContext requestContext)
  {
    checkTestObject( testObject );
    checkRequestContext(requestContext);
    checkBaseUrl(requestContext.getBaseUrl());

    this.testObject = testObject;
    this.context = requestContext;
  }

  /**
   * Access to context to define additional request properties at runtime
   * @return context to be manipulated
   */
  public RequestContext getRequestContext() {
    return context;
  }
  
  private void checkBaseUrl( String baseUrl ) {
    if( baseUrl == null ) {
      throw new IllegalArgumentException( "baseUrl must not be null" );
    }
    try {
      new URL( baseUrl );
    } catch( MalformedURLException mue ) {
      throw new IllegalArgumentException( "baseUrl has to be an URL" );
    }
  }

  private void checkTestObject( Object testObject ) {
    if( testObject == null ) {
      throw new IllegalArgumentException( "testObject must not be null." );
    }
  }

  private void checkRequestContext( RequestContext requestContext ) {
      if( requestContext == null ) {
          throw new IllegalArgumentException( "requestContext must not be null." );
      }
  }

    @Override
  /**
   * <p><b>Not meant for public use. This method will be invoked by the JUnit framework.</b></p>
   */
  public Statement apply( Statement base, Description description ) {
    Statement result;
    if( hasHttpTestAnnotation(description) )
    {
      // In case the context.TestName has not been set, context was not processed as a JUnit-Rule and its internal fields are not populated correctly i.e. just call apply() here instead.
      if(context.getTestName()== null)
           context.apply(base, description);
      requestStatement = new HttpTestStatement( base, description, testObject, context.getBaseUrl(), proxyHost, proxyPort, context );
      result = requestStatement;
    } else {
      result = base;
    }
    return result;
  }

  private boolean hasHttpTestAnnotation(Description description) {
    return description.getAnnotation( HttpTest.class ) != null;
  }


}
