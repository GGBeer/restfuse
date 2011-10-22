package com.eclipsesource.restfuse.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.eclipsesource.restfuse.Assert_Test;
import com.eclipsesource.restfuse.CallbackResource_Test;
import com.eclipsesource.restfuse.DefaultCallbackResource_Test;
import com.eclipsesource.restfuse.Destination_Test;
import com.eclipsesource.restfuse.HttpJUnitRunner_Test;
import com.eclipsesource.restfuse.internal.AuthenticationInfo_Test;
import com.eclipsesource.restfuse.internal.CallbackServer_Test;
import com.eclipsesource.restfuse.internal.CallbackServlet_Test;
import com.eclipsesource.restfuse.internal.HttpTestStatement_Test;
import com.eclipsesource.restfuse.internal.InternalRequest_Test;
import com.eclipsesource.restfuse.internal.RequestConfiguration_Test;
import com.eclipsesource.restfuse.internal.RequestImpl_Test;
import com.eclipsesource.restfuse.internal.ResponseImpl_Test;


@RunWith( Suite.class ) 
@SuiteClasses( {
  Assert_Test.class,
  CallbackResource_Test.class,
  DefaultCallbackResource_Test.class,
  Destination_Test.class,
  HttpJUnitRunner_Test.class,
  AuthenticationInfo_Test.class,
  CallbackServer_Test.class,
  CallbackServlet_Test.class,
  HttpTestStatement_Test.class,
  InternalRequest_Test.class,
  RequestConfiguration_Test.class,
  RequestImpl_Test.class,
  ResponseImpl_Test.class
} )

public class AllRestfustTestSuite {
}
