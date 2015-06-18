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

import java.io.File;
import java.io.IOException;

import org.apache.wicket.Component;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IComponentAssignedModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.IWrapModel;
import org.apache.wicket.protocol.http.mock.MockServletContext;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;
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
	 * Tests custom type download.
	 */
	@Test
	public void customTypeDownloadLink()
	{
		tester.startPage(DownloadPage.class);
		((MockServletContext)tester.getApplication().getServletContext()).addMimeType("custom",
			APPLICATION_X_CUSTOM);
		tester.clickLink(DownloadPage.CUSTOM_DOWNLOAD_LINK);
		log.debug("Content-Type: " + tester.getContentTypeFromResponseHeader());
		assertTrue(tester.getContentTypeFromResponseHeader().startsWith(APPLICATION_X_CUSTOM));
	}

	/**
	 * Tests pdf download.
	 */
	@Test
	public void pdfDownloadLink()
	{
		tester.startPage(DownloadPage.class);
		tester.clickLink(DownloadPage.PDF_DOWNLOAD_LINK);
		assertTrue(tester.getContentTypeFromResponseHeader().startsWith("application/pdf"));
		assertEquals(DownloadPage.HELLO_WORLD.length(), tester.getContentLengthFromResponseHeader());
	}

	/**
	 * Tests text download.
	 */
	@Test
	public void textDownloadLink()
	{
		tester.startPage(DownloadPage.class);
		tester.clickLink(DownloadPage.TEXT_DOWNLOAD_LINK);
		assertTrue(tester.getContentTypeFromResponseHeader().startsWith("text/plain"));
		assertTrue(tester.getContentDispositionFromResponseHeader().startsWith(
			"attachment; filename="));
		assertEquals(0, tester.getContentLengthFromResponseHeader());
	}

	/**
	 * Tests file removal after download
	 */
	@Test
	public void deleteAfterLink()
	{
		DownloadPage page;

		try
		{
			page = new DownloadPage();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		tester.startPage(page);
		File temporary = page.getTemporaryFile();
		tester.clickLink(DownloadPage.DELETE_DOWNLOAD_LINK);
		assertFalse(temporary.exists());
	}

	/**
	 * WICKET-4738 wrapOnAssigment and detach on fileName
	 */
	@Test
	public void fileNameModel()
	{

		FileNameModel fileNameModel = new FileNameModel();

		DownloadLink link = new DownloadLink("test", new AbstractReadOnlyModel<File>()
		{
			@Override
			public File getObject()
			{
				return null;
			}
		}, fileNameModel);

		assertTrue(fileNameModel.wrapOnAssignmentCalled);

		link.detach();

		assertTrue(fileNameModel.detachCalled);
	}

	private class FileNameModel extends AbstractReadOnlyModel<String>
		implements
			IComponentAssignedModel<String>,
			IWrapModel<String>
	{
		private static final long serialVersionUID = 1L;

		boolean detachCalled = false;
		boolean wrapOnAssignmentCalled;

		@Override
		public String getObject()
		{
			return null;
		}

		@Override
		public IWrapModel<String> wrapOnAssignment(Component component)
		{
			wrapOnAssignmentCalled = true;

			return this;
		}

		@Override
		public void detach()
		{
			detachCalled = true;
		}

		@Override
		public IModel<?> getWrappedModel()
		{
			return null;
		}
	}
}
