/*******************************************************************************
 * Copyright (c) 2011 EclipseSource and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: Holger Staudacher - initial API and implementation
 * 
 ******************************************************************************/
package com.eclipsesource.restfuse.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.runner.Description;

import com.eclipsesource.restfuse.RequestContext;

public class RequestConfiguration {

  private final String baseUrl;
  private final Description description;
  private final Object target;

  public RequestConfiguration( String baseUrl, Description description, Object target ) {
    this.baseUrl = baseUrl;
    this.description = description;
    this.target = target;
  }

  public InternalRequest createRequest( RequestContext context ) {
    InternalRequest request = new InternalRequest( context );
    request.updateBody();
    try {
      processRequest(request);
    } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e)
    {
      throw new IllegalStateException( "ProcessRequest exception: " + e.getMessage(), e );
    }

    return request;
  }

  /**
   * Process/transform Content by calling the ProcessRequest-Annotation field as a class
   * using the current request (context) as constructor input.
   * @param request
   */
  private void processRequest( RequestContext request ) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    if(request.getProcessRequestClassName()!=null && !request.getProcessRequestClassName().equals( "" ) )
    {
      // get the Class object for the implementation class
      Class clazz;
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      if (loader == null)
      {
        clazz = Class.forName(request.getProcessRequestClassName());
      }
      else
      {
        clazz = loader.loadClass(request.getProcessRequestClassName());
      }
      // fetch the (InternalRequest, RequestContext) constructor
      Constructor cons = clazz.getConstructor(new Class[] {RequestContext.class});
      // invoke constructor and return the ProcessRequest object
      Object obj = cons.newInstance(new Object[] {request});
// TODO Access ProcessRequest object/methods here?
//      ProcessRequest pReq = (ProcessRequest) obj;
//      pReq.getBody();
    }
  }

}
