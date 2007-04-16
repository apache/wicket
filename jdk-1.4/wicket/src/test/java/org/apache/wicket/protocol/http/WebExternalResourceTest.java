/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.protocol.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.protocol.http.request.WebExternalResourceRequestTarget;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.resource.WebExternalResourceStream;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.util.tester.WicketTester.DummyWebApplication;

/**
 * Test WebExternalResourceRequestTarget and WebExternalResourceStream
 * 
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class WebExternalResourceTest extends WicketTestCase
{
	protected void setUp() throws Exception
	{
		File tempDir = new File("target/webapp");
		tempDir.mkdir();
		File tempFile = new File(tempDir, "index.html");
		FileOutputStream out = new FileOutputStream(tempFile);
		InputStream in = WebExternalResourceTest.class.getResourceAsStream("index.html");
		Streams.copy(in, out);
		in.close();
		out.close();
		tester = new WicketTester(new DummyWebApplication(), tempDir.getPath());
		tester.setupRequestAndResponse();
		// We fake the browser URL, otherwise Wicket doesn't know the requested URL and cannot guess the Content-Type
		tester.getServletRequest().setPath("/index.html");
	}

	public void testWebExternalResourceRequestTarget() throws Exception
	{
		WebExternalResourceRequestTarget rt = new WebExternalResourceRequestTarget("/index.html");
		WebRequestCycle cycle = tester.createRequestCycle();
		cycle.setRequestTarget(rt);
		tester.processRequestCycle(cycle);
		assertTrue(getContentType().startsWith("text/html"));
		// WebExternalResourceRequestTarget does not set Content-Length
		// assertEquals(23, getContentLength());
		tester.assertResultPage(WebExternalResourceTest.class, "index.html");
	}

	// FIXME WebExternalResourceStream does not implement length()
	public void testWebExternalResource() throws Exception
	{
		WebExternalResourceStream resource = new WebExternalResourceStream("/index.html");
		ResourceStreamRequestTarget rt = new ResourceStreamRequestTarget(resource);
		WebRequestCycle cycle = tester.createRequestCycle();
		cycle.setRequestTarget(rt);
		tester.processRequestCycle(cycle);
		assertTrue(getContentType().startsWith("text/html"));
		// WebExternalResourceStream does not set Content-Length
		// assertEquals(23, getContentLength());
		tester.assertResultPage(WebExternalResourceTest.class, "index.html");
	}
}
