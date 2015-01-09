package com.eclipsesource.restfuse.example;

import com.eclipsesource.restfuse.*;
import com.eclipsesource.restfuse.internal.AuthenticationInfo;
import com.eclipsesource.restfuse.internal.InternalRequest;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyRequestProcessing implements ProcessRequest
{
    public MyRequestProcessing(RequestContext requestContext)
    {
        // Rewrite the URL
        String url = requestContext.getUrl();
        requestContext.setUrl(url+"/?myid=12345");

        // Add my own headers
        Map<String, List<String>> headers = requestContext.getHeaders();
        requestContext.addHeader("X-RF-Dummy", "some test junk");

        // Replace AuthenticationInfo
        List<AuthenticationInfo> authentications = requestContext.getAuthentications(); // Already part of headers?
        requestContext.clearAuthenticationInfos();
        requestContext.addAuthenticationInfo(new AuthenticationInfo(AuthenticationType.BASIC, "emt", "emt"));

        // Transform/Replace Content
        String ctype = requestContext.getContentType();
        InputStream contentIS = requestContext.getBody();
        String bodyStr = "";
        try
        {
            bodyStr = IOUtils.toString(contentIS, "UTF-8");  // TODO should respect CHARSET Param from HEADER_CONTENT_TYPE,
        } catch (IOException ignored)
        {
            // Ignored
        }

        // Get current items from the internalRequest (requestContext)
        HashMap<String, Object> requestObjects = (HashMap<String, Object>) requestContext.getRequestObjects();

        String keyA = "reqParam_StrA";
        String valueA = null;

        String keyB = "reqParam_ByteB";
        byte[] valueB = null;

        if(requestContext.getRequestObjectKeys().contains(keyA))
            valueA = (String) requestObjects.get(keyA);

        if(requestContext.getRequestObjectKeys().contains(keyB))
            valueB = (byte[]) requestObjects.get(keyB);

        if(valueA!=null)
            bodyStr = bodyStr.replace("{" + keyA+ "}", valueA);

        if(valueB!=null)
            bodyStr = bodyStr.replace("{" + keyB+ "}", bytesToHex(valueB));

        requestContext.setBody("MyRequestProcessing extended: " + bodyStr);


        // Change RequestMethod
        requestContext.setRequestMethod(Method.GET);
    }

    /**
     * Converts bytes to hex digits.
     *
     * @param bytes
     * @return hex digits
     */
    public static String bytesToHex(byte[] bytes)
    {
        if (bytes == null)
        {
            return "(null)";
        }
        return bytesToHex(bytes, 0, bytes.length);
    }

    /**
     * Converts bytes to hex digits.
     *
     * @param bytes
     * @param offset
     * @param length
     * @return hex digits
     */
    private static String bytesToHex(byte[] bytes, int offset, int length)
    {
        // Constants
        // Radix for hex values.
        char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

        char[] hex = new char[2 * length];
        int ci = 0;
        for (int i = 0; i < length; i++)
        {
            int b = bytes[offset + i] & 0xFF;
            hex[ci++] = HEX_DIGITS[b >> 4];
            hex[ci++] = HEX_DIGITS[b & 0xF];
        }
        return new String(hex).toLowerCase();
    }
}
