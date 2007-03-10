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
package wicket.markup.html.link;

import wicket.WicketTestCase;
import wicket.protocol.http.MockHttpServletResponse;
import wicket.protocol.http.MockServletContext;

/**
 * Tests DownloadLink
 * 
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class DownloadLinkTest extends WicketTestCase
{
	private static final String APPLICATION_X_CUSTOM = "application/x-custom";

	public DownloadLinkTest(String name)
	{
		super(name);
	}

	public void testTextDownloadLink()
	{
		tester.startPage(DownloadPage.class);
		tester.clickLink(DownloadPage.TEXT_DOWNLOAD_LINK);
		assertTrue(getContentType().startsWith("text/plain"));
		assertTrue(getContentDisposition().startsWith("attachment; filename="));
		assertEquals(0, getContentLength());
	}

	public void testPdfDownloadLink()
	{
		tester.startPage(DownloadPage.class);
		tester.clickLink(DownloadPage.PDF_DOWNLOAD_LINK);
		assertTrue(getContentType().startsWith("application/pdf"));
		assertEquals(DownloadPage.HELLO_WORLD.length(), getContentLength());
	}

	public void testCustomTypeDownloadLink()
	{
		tester.startPage(DownloadPage.class);
		((MockServletContext)tester.getApplication().getServletContext()).addMimeType("custom",
				APPLICATION_X_CUSTOM);
		tester.clickLink(DownloadPage.CUSTOM_DOWNLOAD_LINK);
		assertTrue(getContentType().startsWith(APPLICATION_X_CUSTOM));
	}

	private String getContentType()
	{
		return ((MockHttpServletResponse)tester.getWicketResponse().getHttpServletResponse())
				.getHeader("Content-Type");
	}

	private int getContentLength()
	{
		return Integer.parseInt(((MockHttpServletResponse)tester.getWicketResponse()
				.getHttpServletResponse()).getHeader("Content-Length"));
	}

	private String getContentDisposition()
	{
		return ((MockHttpServletResponse)tester.getWicketResponse().getHttpServletResponse())
				.getHeader("Content-Disposition");
	}
}
