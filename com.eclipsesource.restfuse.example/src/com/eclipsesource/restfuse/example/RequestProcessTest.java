/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package com.eclipsesource.restfuse.example;

import com.eclipsesource.restfuse.*;
import com.eclipsesource.restfuse.annotation.Authentication;
import com.eclipsesource.restfuse.annotation.Context;
import com.eclipsesource.restfuse.annotation.HttpTest;
import org.junit.Rule;
import org.junit.runner.RunWith;

import static com.eclipsesource.restfuse.AuthenticationType.BASIC;

@RunWith(HttpJUnitRunner.class)
public class RequestProcessTest
{
    @Rule
    public RequestContext requestContext = new RequestContext(this, "http://localhost:8080/restfuse");

	@Rule
	public Destination restfuse = getDestination();

	private Destination getDestination() {
		Destination destination = new Destination(this, requestContext);
		return destination;
	}

	@Context
	private Response response;

	// static variable to store the requestParamA and requestParamB to be used in further Tests
	private static String reqParam_StrA;
	private static byte[] reqParam_ByteB;

    public static String getReqParam_StrA()
    {
        return reqParam_StrA;
    }

    public static byte[] getReqParam_ByteB()
    {
        return reqParam_ByteB;
    }

    @HttpTest(method = Method.GET, path = "/tenants/gws", order = 1, authentications = {@Authentication(type = BASIC, user = "gwadmusr", password = "gwadmpwd")},
			type = MediaType.APPLICATION_JSON
	)
	public void a_getItems() {
		// assertOk( response );
		String jsonResponse = response.getBody();

		// prepare next test method
		reqParam_StrA = "{\"name\":\"testitem\", \"description\":\"a new item\"}";
		reqParam_ByteB = "Somebody Was Here".getBytes();
	}

	@HttpTest(method = Method.POST, path = "/tenants/smgws", order = 2,
			content = "AAA:{reqParam_StrA} or BBB:{reqParam_ByteB}",
			processrequest="com.eclipsesource.restfuse.example.MyRequestProcessing",
            requestObjectKeys = {"reqParam_StrA", "reqParam_ByteB"},
			authentications = {@Authentication(type = BASIC, user = "gwa", password = "gwa")},
			type = MediaType.APPLICATION_JSON)
	public void b_addItem() {
		// assertOk( response );
		String jsonResponse = response.getBody();
	}

}
