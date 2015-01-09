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

package com.eclipsesource.restfuse.internal;

import java.io.InputStream;
import java.net.URL;
import java.util.*;

import com.eclipsesource.restfuse.AuthenticationType;
import com.eclipsesource.restfuse.Method;
import com.eclipsesource.restfuse.RequestContext;
import com.eclipsesource.restfuse.Response;
import com.eclipsesource.restfuse.annotation.HttpTest;
import com.github.kevinsawicki.http.HttpRequest;

public class InternalRequest extends RequestContext
{
    @Deprecated
    public InternalRequest(String url)
    {
        this.baseUrl = url;
        this.headers = new HashMap<String, List<String>>();
        this.pathSegments = new HashMap<String, String>();
        this.requestObjects = new HashMap<String, Object>();
        this.authentications = new ArrayList<AuthenticationInfo>();
    }

    public InternalRequest(RequestContext requestContext)
    {
        this.headers = requestContext.getHeaders();
        this.pathSegments = requestContext.getPathSegments();
        this.requestObjects = requestContext.getRequestObjects();
        this.authentications = requestContext.getAuthentications();
        this.baseUrl = requestContext.getUrl();
        this.mediaType = requestContext.getContentType();
        this.contentFile = requestContext.getContentFile();
        this.contentStr = requestContext.getContentStr();
        this.processRequestClassName = requestContext.getProcessRequestClassName();
        this.body = requestContext.getBody();
        this.requestMethod = requestContext.getRequestMethod();

        this.httpTestAnnotation = requestContext.getHttpTestAnnotation();
        this.testName = requestContext.getTestName();
        this.testClass = requestContext.getTestClass();

        this.baseUrl = this.substituePathSegments();
    }

    public Response get()
    {
        HttpRequest request = HttpRequest.get(this.baseUrl);
        addHttpContentType(request);
        addHttpHeaders(request);
        addHttpAuthentications(request);
        sendHttpRequest(request);
        return new ResponseImpl(request);
    }

    public Response post()
    {
        HttpRequest request = HttpRequest.post(this.baseUrl);
        addHttpContentType(request);
        addHttpHeaders(request);
        addHttpAuthentications(request);
        request.send(body);
        sendHttpRequest(request);
        return new ResponseImpl(request);
    }

    public Response delete()
    {
        HttpRequest request = HttpRequest.delete(this.baseUrl);
        addHttpContentType(request);
        addHttpHeaders(request);
        addHttpAuthentications(request);
        sendHttpRequest(request);
        return new ResponseImpl(request);
    }

    public Response put()
    {
        HttpRequest request = HttpRequest.put(this.baseUrl);
        addHttpContentType(request);
        addHttpHeaders(request);
        addHttpAuthentications(request);
        request.send(body);
        sendHttpRequest(request);
        return new ResponseImpl(request);
    }

    public Response head()
    {
        HttpRequest request = HttpRequest.head(this.baseUrl);
        addHttpContentType(request);
        addHttpHeaders(request);
        addHttpAuthentications(request);
        sendHttpRequest(request);
        return new ResponseImpl(request);
    }

    public Response options()
    {
        HttpRequest request = HttpRequest.options(this.baseUrl);
        addHttpContentType(request);
        addHttpHeaders(request);
        addHttpAuthentications(request);
        sendHttpRequest(request);
        return new ResponseImpl(request);
    }

    private void addHttpContentType(HttpRequest request)
    {
        String type = mediaType != null ? mediaType : "*/*";
        request.contentType(type);
    }

    private void addHttpHeaders(HttpRequest request)
    {
        Set<String> keySet = headers.keySet();
        for (String key : keySet)
        {
            List<String> values = headers.get(key);
            StringBuilder builder = new StringBuilder();
            for (String value : values)
            {
                builder.append(value + ",");
            }
            request.header(key, builder.substring(0, builder.length() - 1));
        }
        request.trustAllCerts();
        request.trustAllHosts();
    }

    private void addHttpAuthentications(HttpRequest request)
    {
        for (AuthenticationInfo authentication : authentications)
        {
            if (authentication.getType().equals(AuthenticationType.BASIC))
            {
                request.basic(authentication.getUser(), authentication.getPassword());
            }
            else if (authentication.getType().equals(AuthenticationType.DIGEST))
            {
                // TODO: implement digest auth
            }
        }
    }

    /**
     *
     * @param request
     */
    private void sendHttpRequest(HttpRequest request)
    {
        request.code();
    }

    /**
     *
     */
    public void updateBody()
    {
        if (this.contentFile != null && !this.contentFile.isEmpty())
        {
            this.setBody(getFileStream(this.contentFile));
        } else if (this.contentStr != null && !this.contentStr.isEmpty())
        {
            this.setBody(this.contentStr);
        }
    }

    private InputStream getFileStream( String file ) {
        URL resource = this.getTestClass().getResource( file );
        try {
            return resource.openStream();
        } catch( Exception ioe ) {
            throw new IllegalStateException( "Could not open file "
                    + file + ". Maybe it's not on the classpath?" );
        }
    }


}
