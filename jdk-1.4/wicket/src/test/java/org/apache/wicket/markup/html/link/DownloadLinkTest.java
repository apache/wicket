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
package org.apache.wicket.markup.html.link;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.protocol.http.MockServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Tests DownloadLink
 * 
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class DownloadLinkTest extends WicketTestCase
{
	private static final String APPLICATION_X_CUSTOM = "application/x-custom";
	private static final Logger log = LoggerFactory.getLogger(DownloadLinkTest.class);

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public DownloadLinkTest(String name)
	{
		super(name);
	}

	/**
	 * Tests custom type download.
	 */
	public void testCustomTypeDownloadLink()
	{
		tester.startPage(DownloadPage.class);
		((MockServletContext)tester.getApplication().getServletContext()).addMimeType("custom",
				APPLICATION_X_CUSTOM);
		tester.clickLink(DownloadPage.CUSTOM_DOWNLOAD_LINK);
		log.debug("Content-Type: " + getContentType());
		assertTrue(getContentType().startsWith(APPLICATION_X_CUSTOM));
	}

	/**
	 * Tests pdf download.
	 */
	public void testPdfDownloadLink()
	{
		tester.startPage(DownloadPage.class);
		tester.clickLink(DownloadPage.PDF_DOWNLOAD_LINK);
		assertTrue(getContentType().startsWith("application/pdf"));
		assertEquals(DownloadPage.HELLO_WORLD.length(), getContentLength());
	}

	/**
	 * Tests text download.
	 */
	public void testTextDownloadLink()
	{
		tester.startPage(DownloadPage.class);
		tester.clickLink(DownloadPage.TEXT_DOWNLOAD_LINK);
		assertTrue(getContentType().startsWith("text/plain"));
		assertTrue(getContentDisposition().startsWith("attachment; filename="));
		assertEquals(0, getContentLength());
	}
}
