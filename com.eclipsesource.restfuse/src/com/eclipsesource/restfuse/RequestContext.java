/*******************************************************************************
 * Copyright (c) 2011 EclipseSource and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 *
 * Contributors: drejc (https://github.com/drejc)
 *               Holger Staudacher - ongoing development
 *               Dennis Crissman - added dynamic path segments
 *               Guus Gerrits - added ProcessRequest and PathSegment Processing using reflection
 *
 ******************************************************************************/

package com.eclipsesource.restfuse;

import com.eclipsesource.restfuse.annotation.Authentication;
import com.eclipsesource.restfuse.annotation.Header;
import com.eclipsesource.restfuse.annotation.HttpTest;
import com.eclipsesource.restfuse.internal.AuthenticationInfo;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>RequestContext holds additional data to be added to the request before execution like headers (cookies) or dynamic
 * path segments. The context is used to configure requests dynamically.
 * </p>
 */
public class RequestContext implements TestRule
{
    private static final String PATH_SEPARATOR = "/";

    protected Object testObject = null;

    protected HttpTest httpTestAnnotation = null;
    protected String testName = null;
    protected Class<?> testClass = null;

    /**
     * <p>Name value collection of HTTP-Headers.</p>
     */
    protected Map<String, List<String>> headers = new HashMap<String, List<String>>();

    /**
     * <p>Name value collection of PathSegments.</p>
     */
    protected Map<String, String> pathSegments = new HashMap<String, String>();

    /**
     * <p>Name value collection of RequestObjects.</p>
     */
    protected Map<String, Object> requestObjects = new HashMap<String, Object>();

    /**
     * <p>List of AuthenticationInfo.</p>
     */
    protected List<AuthenticationInfo> authentications = new ArrayList<AuthenticationInfo>();

    protected String baseUrl = null;
    protected String pathUrl = null;
    protected String mediaType = null;
    protected String contentStr = null;
    protected String contentFile = null;
    protected String processRequestClassName = null;
    protected InputStream body = null;
    protected Method requestMethod = null;

    /**
     * Constructs a newly allocated RequestContext object.
     */
    public RequestContext()
    {
        // Default
    }

    /**
     * Constructs a newly allocated RequestContext object.
     *
     * @param baseUrl
     */
    public RequestContext(Object testObject, String baseUrl)
    {
        this.testObject = testObject;
        this.baseUrl = baseUrl;
    }

    /**
     * Returns the requestMethod attribute of the RequestContext.
     *
     * @return The requestMethod
     */
    public Method getRequestMethod()
    {
        return requestMethod;
    }

    /**
     * Sets the requestMethod attribute of the RequestContext.
     *
     * @param requestMethod The requestMethod to set
     */
    public void setRequestMethod(Method requestMethod)
    {
        this.requestMethod = requestMethod;
    }

    /**
     * Set RequestMethod from HttpTest-Annotation "method".
     * @param call HttpTest-Annotation
     */
    public void setRequestMethodFromAnnotation(HttpTest call)
    {
        this.requestMethod = call.method();
    }


    /**
     * Returns the authentications attribute of the RequestContext.
     *
     * @return The authentications
     */
    public List<AuthenticationInfo> getAuthentications()
    {
        return authentications;
    }

    /**
     * Sets the authentications attribute of the RequestContext.
     *
     * @param authentications The authentications to set
     */
    public void setAuthentications(List<AuthenticationInfo> authentications)
    {
        this.authentications = authentications;
    }

    /**
     *
     */
    public void clearAuthenticationInfos()
    {
        authentications.clear();
    }

    /**
     *
     * @param call
     */
    public void addAuthenticationFromAnnotation(HttpTest call)
    {
        Authentication[] authentications = call.authentications();
        if (authentications != null)
        {
            for (Authentication authentication : authentications)
            {
                AuthenticationType type = authentication.type();
                String user = authentication.user();
                String password = authentication.password();
                this.authentications.add(new AuthenticationInfo(type, user, password));
            }
        }
    }

    /**
     *
     * @param authentication
     */
    public void addAuthenticationInfo(AuthenticationInfo authentication)
    {
        authentications.add(authentication);
    }


    /**
     * Returns the mediaType attribute of the RequestContext.
     *
     * @return The mediaType
     */
    public String getContentType()
    {
        return mediaType;
    }

    /**
     * Sets the mediaType attribute of the RequestContext.
     *
     * @param mediaType The mediaType to set
     */
    public void setContentType(String mediaType)
    {
        this.mediaType = mediaType;
    }

    /**
     * Set ContentType from HttpTest-Annotation "type".
     * Remark: In case the ContentType already has been set before the apply() method
     * (which calls this setContentTypeFromAnnotation method) is called the
     * HttpTest-Annotation "type" value is ignored!
     * @param call HttpTest-Annotation
     */
    public void setContentTypeFromAnnotation(HttpTest call)
    {
        MediaType contentType = call.type();
        if (contentType != null && this.mediaType == null)
        {
            this.mediaType = contentType.getMimeType();
        }
    }

    /**
     *
     * @return
     */
    public String getBaseUrl()
    {
        return baseUrl;
    }

    /**
     *
     * @param baseUrl
     */
    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    /**
     * Returns the pathUrl attribute of the RequestContext.
     *
     * @return The pathUrl
     */
    public String getPathUrl()
    {
        return pathUrl;
    }

    /**
     * Sets the pathUrl attribute of the RequestContext.
     *
     * @param pathUrl The pathUrl to set
     */
    public void setPathUrl(String pathUrl)
    {
        this.pathUrl = pathUrl;
    }

    /**
     * Set PathUrl from HttpTest-Annotation "path".
     * Remark: In case the PathUrl already has been set before the apply() method
     * (which calls this setPathUrlFromAnnotation method) is called the
     * HttpTest-Annotation "path" value is ignored!
     * @param call HttpTest-Annotation
     */
    public void setPathUrlFromAnnotation(HttpTest call)
    {
        String path = call.path();
        if (path != null && !path.equals("") && this.pathUrl == null)
        {
            this.pathUrl = path;
        }
    }

    /**
     * Returns the pathSegments attribute of the RequestContext.
     *
     * @return The pathSegments
     */
    public Map<String, String> getPathSegments()
    {
        return pathSegments;
    }

    /**
     * Sets the pathSegments attribute of the RequestContext.
     *
     * @param pathSegments The pathSegments to set
     */
    public void setPathSegments(Map<String, String> pathSegments)
    {
        this.pathSegments = pathSegments;
    }

    /**
     * <p>
     * Dynamic path segments.Example with Given: http://localhost/{version}/{id}/<br>
     * <p/>
     * <pre>
     * Destination destination = new Destination(&quot;http://localhost/{version}/&quot;);
     * RequestContext context = destination.getRequestContext();
     * context.addPathSegment(&quot;id&quot;, &quot;12345&quot;).addPathSegment(&quot;version&quot;, &quot;1.1&quot;);
     * </pre>
     * <p/>
     * Produces: http://localhost/1.1/12345/
     * </p>
     *
     * @since 1.2
     */
    public RequestContext addPathSegment(String segment, String replacement)
    {
        pathSegments.put(segment, replacement);
        return this;
    }

    /**
     * Add PathSegment items contained in the URL by calling getter get<KeyName>() in
     * TestClass using reflection. All getters must return String. Items which are already
     * present in the pathSegments Map (e.g. by using addPathSegment() in the Rule init)
     * are skipped here.
     * Remark: First character of KeyName is uppercased on building the getter method name.
     */
    public void addPathSegmentItemsFromUrl()
    {
        // Prefill the PathSegment HashMap.
        // Use .getPathSegmentKeys() to retrieve the Set of Items required
        Pattern pattern = Pattern.compile(".*?\\{(.*?)\\}.*?");
        Matcher matcher = pattern.matcher(this.getUrl());
        while (matcher.find())
        {
            String key = matcher.group(1);
            if (!pathSegments.containsKey(key))
            {
                // Call getter() get<Keyname>() in TestClass using reflection
                String value = null;
                try
                {
                    String getterName = "get" + key.substring(0, 1).toUpperCase() + key.substring(1);
                    //noinspection NullArgumentToVariableArgMethod
                    java.lang.reflect.Method method = this.testClass.getMethod(getterName, (Class[]) null);
                    if (method.getReturnType().equals(String.class))
                    {
                        //noinspection NullArgumentToVariableArgMethod
                        value = (String) method.invoke(testObject, new Object[]{});
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored)
                {
                    // Ignore
                }
                this.addPathSegment(key, value);
            }
        }

    }

    /**
     *
     * @return
     */
    public Set<String> getPathSegmentKeys()
    {
        return pathSegments.keySet();
    }

    /**
     * Returns the url attribute of the RequestContext.
     *
     * @return The url
     */
    public String getUrl()
    {
        return combineBaseAndPathUrl();
    }

    /**
     * Sets the url attribute of the RequestContext.
     *
     * @param url The url to set
     */
    public void setUrl(String url)
    {
        baseUrl = url;
        pathUrl = null;
    }

    /**
     *
     * @return
     */
    public Map<String, List<String>> getHeaders()
    {
        return new HashMap<String, List<String>>(headers);
    }

    /**
     * Sets the headers attribute of the RequestContext.
     *
     * @param headers The headers to set
     */
    public void setHeaders(Map<String, List<String>> headers)
    {
        this.headers = headers;
    }

    /**
     * <p/>
     * Adds a header attribute to a request.
     * <p/>
     *
     * @see Header
     * @since 1.2
     */
    public void addHeader(String name, String value)
    {
        List<String> param = headers.get(name);
        if (param == null)
        {
            List<String> params = new ArrayList<String>();
            params.add(value);
            headers.put(name, params);
        } else
        {
            param.add(value);
        }
    }

    /**
     * <p/>
     * Adds a header attribute to a request.
     * <p/>
     *
     * @see Header
     * @since 1.2
     */
    public void addHeader(String name, List<String> values)
    {
        List<String> param = headers.get(name);
        if (param == null)
        {
            List<String> params = new ArrayList<String>();
            params.addAll(values);
            headers.put(name, params);
        } else
        {
            param.addAll(values);
        }
    }

    /**
     *
     * @param call
     */
    public void addHeadersFromAnnotation(HttpTest call)
    {
        Header[] header = call.headers();
        if (header != null)
        {
            for (Header parameter : header)
            {
                this.addHeader(parameter.name(), parameter.value());
            }
        }
    }

    /**
     *
     * @return
     */
    public String getContentStr()
    {
        return contentStr;
    }

    /**
     *
     * @param contentStr
     */
    public void setContentStr(String contentStr)
    {
        this.contentStr = contentStr;
    }

    /**
     *
     * @return
     */
    public String getContentFile()
    {
        return contentFile;
    }

    /**
     *
     * @param contentFile
     */
    public void setContentFile(String contentFile)
    {
        this.contentFile = contentFile;
    }


    /**
     * Set ContentStr from HttpTest-Annotation "content".
     * Remark: In case the ContentStr already has been set before the apply() method
     * (which calls this setContentStrFromAnnotation method) is called the
     * HttpTest-Annotation "content" value is ignored!
     * @param call HttpTest-Annotation
     */
    public void setContentStrFromAnnotation(HttpTest call)
    {
        String content = call.content();
        if (content != null && !content.equals("") && this.contentStr == null)
        {
            this.contentStr = content;
        }
    }

    /**
     * Set ContentStr from HttpTest-Annotation "file".
     * Remark: In case the ContentFile already has been set before the apply() method
     * (which calls this setContentFileFromAnnotation method) is called the
     * HttpTest-Annotation "file" value is ignored!
     * @param call HttpTest-Annotation
     */
    public void setContentFileFromAnnotation(HttpTest call)
    {
        String file = call.file();
        if (file != null && !file.equals("") && this.contentFile == null)
        {
            this.contentFile = file;
        }
    }

    /**
     * @return ProcessRequest ClassName
     */
    public String getProcessRequestClassName()
    {
        return processRequestClassName;
    }

    /**
     * Set ProcessRequest from HttpTest-Annotation "processrequest".
     * @param call HttpTest-Annotation
     */
    public void setProcessRequestFromAnnotation(HttpTest call)
    {
        String processrequest = call.processrequest();
        if (processrequest != null && !processrequest.equals(""))
        {
            this.processRequestClassName = processrequest;
        }
    }

    /**
     * Returns the requestObjects attribute of the RequestContext.
     *
     * @return The requestObjects
     */
    public Map<String, Object> getRequestObjects()
    {
        return requestObjects;
    }

    /**
     * Sets the requestObjects attribute of the RequestContext.
     *
     * @param requestObjects The requestObjects to set
     */
    public void setRequestObjects(Map<String, Object> requestObjects)
    {
        this.requestObjects = requestObjects;
    }

    /**
     *
     * @return
     */
    public Set<String> getRequestObjectKeys()
    {
        return requestObjects.keySet();
    }

    /**
     * <p>
     * Dynamic RequestObjects.Example<br>
     * <p/>
     * <pre>
     *   Destination destination = new Destination( "http://localhost/{version}/" );
     *   RequestContext context = destination.getRequestContext();
     *   context.addRequestObject("myPreConditionObjectTag", myPreConditions).("myDefaultUserObjectTag", myDefaultUser).;
     * </pre>
     * <p/>
     * The current RequestContext will be used as input to the test's processrequest constructor. Which will
     * process/transform the RequestContext using the contained RequestObjects.
     * </p>
     */
    public RequestContext addRequestObject(String tag, Object requestObject)
    {
        requestObjects.put(tag, requestObject);
        return this;
    }

    /**
     * Add RequestObject items listed in the HttpTest Annotation by calling getter
     * get<KeyName>() in TestClass using reflection. Getters may return any object type
     * except void. Items which are already present in the requestObjects Map (e.g. by
     * using addRequestObject() in the Rule init) are skipped here.
     * Remark: First character of KeyName is uppercased on building the getter method name.
     * @param call
     */
    public void addRequestObjectItemsFromAnnotation(HttpTest call)
    {
        String[] requestObjectKeys = call.requestObjectKeys();
        if (requestObjectKeys != null)
        {
            for (String key : requestObjectKeys)
            {
                if (!requestObjects.containsKey(key))
                {
                    // Call getter() get<KeyName>() in TestClass using reflection
                    Object value = null;
                    try
                    {
                        String getterName = "get" + key.substring(0, 1).toUpperCase() + key.substring(1);
                        //noinspection NullArgumentToVariableArgMethod
                        java.lang.reflect.Method method = this.testClass.getMethod(getterName, (Class[]) null);
                        //noinspection NullArgumentToVariableArgMethod
                        value = method.invoke(testObject, new Object[]{});
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored)
                    {
                        // Ignore
                    }

                    this.addRequestObject(key, value);
                }
            }
        }
    }


    /**
     * Returns the body attribute of the RequestContext.
     *
     * @return The body
     */
    public InputStream getBody()
    {
        return body;
    }

    /**
     *
     * @param contentStr
     */
    public void setBody(String contentStr)
    {
        try
        {
            this.setBody(new ByteArrayInputStream(contentStr.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException shouldNotHappen)
        {
            throw new IllegalStateException(shouldNotHappen);
        }
    }

    /**
     * Sets the body attribute of the RequestContext.
     *
     * @param body The body to set
     */
    public void setBody(InputStream body)
    {
        this.body = body;
    }

    /**
     * @deprecated Use <ReqCtx>.getContentStr() instead.
     */
    @Deprecated
    public String getDynamicBody()
    {
        return contentStr;
    }

    /**
     * @deprecated Use <ReqCtx>.setContentStr(String contentStr) instead.
     * Enhancement for manipulating the body sent with the request in a basic dynamic way
     *
     * @param dynamicBody
     * @author mihm
     */
    @Deprecated
    public void setDynamicBody(String dynamicBody)
    {
        this.contentStr = dynamicBody;
    }

    /**
     * @return the HttpTest Annotation
     */
    public HttpTest getHttpTestAnnotation()
    {
        return httpTestAnnotation;
    }

    /**
     * @param httpTestAnnotation
     */
    public void setHttpTestAnnotation(HttpTest httpTestAnnotation)
    {
        this.httpTestAnnotation = httpTestAnnotation;
    }

    /**
     *
     * @return
     */
    public String getTestName()
    {
        return testName;
    }

    /**
     *
     * @return
     */
    public Class<?> getTestClass()
    {
        return testClass;
    }


    @Override
    /**
     * <p><b>Not meant for public use</b>!
     *  This method will be invoked by the JUnit framework after all @Rules are initialized
     *  i.e. only after the Rule inits in the Test-Class the JU Statement and Description
     *  values (containing the Annotations etc.) are available</p>
     */
    public Statement apply(Statement base, Description description)
    {
        this.testName = description.getDisplayName();
        this.testClass = description.getTestClass();

        if (hasHttpTestAnnotation(description))
        {
            // Process HttpTest-Annotations and store infos into RequestInfo
            this.httpTestAnnotation = description.getAnnotation(HttpTest.class);

            this.setRequestMethodFromAnnotation(this.httpTestAnnotation);
            this.setPathUrlFromAnnotation(this.httpTestAnnotation);
            this.setContentTypeFromAnnotation(this.httpTestAnnotation);
            this.addHeadersFromAnnotation(this.httpTestAnnotation);
            this.addAuthenticationFromAnnotation(this.httpTestAnnotation);
            this.setContentFileFromAnnotation(this.httpTestAnnotation);
            this.setContentStrFromAnnotation(this.httpTestAnnotation);
            this.setProcessRequestFromAnnotation(this.httpTestAnnotation);
            this.addRequestObjectItemsFromAnnotation(this.httpTestAnnotation); // Use .getRequestObjectKeys() to retrieve the Set of Items required
            // HttpTest Order
        }

        // Prefill the PathSegment HashMap.
        this.addPathSegmentItemsFromUrl();

        // Just return the base Statement and use the updated RequestContext in a 2nd Rule in the Destination
        // Constructor.
        return base;
    }


    /**
     *
     * @return
     */
    private String combineBaseAndPathUrl()
    {
        String url = null;
        if (baseUrl != null)
        {
            url = baseUrl;
            if (pathUrl != null)
            {
                if (baseUrl.endsWith(PATH_SEPARATOR) && pathUrl.startsWith(PATH_SEPARATOR))
                {
                    url = baseUrl + pathUrl.substring(1);
                } else if ((!baseUrl.endsWith(PATH_SEPARATOR) && pathUrl.startsWith(PATH_SEPARATOR))
                        || (baseUrl.endsWith(PATH_SEPARATOR) && !pathUrl.startsWith(PATH_SEPARATOR)))
                {
                    url = baseUrl + pathUrl;
                } else if (!baseUrl.endsWith(PATH_SEPARATOR) && !pathUrl.startsWith(PATH_SEPARATOR))
                {
                    url = baseUrl + PATH_SEPARATOR + pathUrl;
                } else
                {
                    throw new IllegalStateException("Invalid URL format with base url " + baseUrl + " and path url" + pathUrl);
                }
            }
        }

        return url;
    }

    /**
     *
     * @return
     */
    public String substituePathSegments()
    {
        String substitutedPath = getUrl();
        Pattern pattern = Pattern.compile(".*?\\{(.*?)\\}.*?");
        Matcher matcher = pattern.matcher(getUrl());
        while (matcher.find())
        {
            String segment = matcher.group(1);
            checkSubstitutionExists(segment);
            substitutedPath = substitutedPath.replace("{" + segment + "}", this.pathSegments.get(segment));
        }
        return substitutedPath;
    }

    /**
     *
     * @param segment
     */
    private void checkSubstitutionExists(String segment)
    {
        if (!pathSegments.containsKey(segment))
        {
            throw new IllegalStateException("Misconfigured RequestContext. Could not replace {" + segment + "} in URL.");
        }
    }

    /**
     *
     * @param description
     * @return
     */
    private boolean hasHttpTestAnnotation(Description description)
    {
        return description.getAnnotation(HttpTest.class) != null;
    }


}

