/**
 * *****************************************************************************
 * Copyright (c) 2013 Meding Software Technik
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Uwe B. Meding <uwe@uwemeding.com>
 * ****************************************************************************
 */
package com.eclipsesource.restfuse.example;

import com.eclipsesource.restfuse.Assert;
import com.eclipsesource.restfuse.Destination;
import com.eclipsesource.restfuse.HttpJUnitRunner;
import com.eclipsesource.restfuse.Method;
import com.eclipsesource.restfuse.annotation.Context;
import com.eclipsesource.restfuse.annotation.HttpTest;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.RunWith;

/**
 * Run a test to show how we can manage cookies
 *
 * @author uwe
 */
@RunWith(HttpJUnitRunner.class)
public class CookieManagerTest {

	@Rule
	public Destination dest = new Destination(this, "http://www.google.com");

	@Context
	private com.eclipsesource.restfuse.Response response;

    // Cookie manager must be static.
	private static CookieManager cookieManager;

	/**
	 * Setup the cookie manager for this test
	 */
	@BeforeClass
	public static void setupCookieManager() {
		cookieManager = new CookieManager();
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(cookieManager);
	}

	@HttpTest(method = Method.GET, path = "/")
	public void getSessionId() {
		Assert.assertOk(response);
		dumpCookies();
	}

	private void dumpCookies() {
		System.out.println("Cookies:");
		CookieStore cookieJar = cookieManager.getCookieStore();
		for (URI uri : cookieJar.getURIs()) {
			System.out.println("uri: " + uri.getHost());
		}
		for (HttpCookie cookie : cookieJar.getCookies()) {
			System.out.println("cookie: " + cookie);
		}
	}
}
