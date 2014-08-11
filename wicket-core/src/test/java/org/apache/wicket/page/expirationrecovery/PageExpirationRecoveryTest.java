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
package org.apache.wicket.page.expirationrecovery;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.mapper.parameter.INamedParameters;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.settings.PageSettings;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for
 * WICKET-5068 PageParameters missing from re-created Page
 * WICKET-5070 Optionally execute Callback Behavior on Re-construction after Expiry
 * WICKET-5001 Recovery of bookmarkable Page after Session Expiry
 */
public class PageExpirationRecoveryTest extends WicketTestCase
{

	private final PageParameters parameters = new PageParameters()
			.set("a", "b", INamedParameters.Type.MANUAL)
			.set("c", "d", INamedParameters.Type.MANUAL);

	@Before
	public void before()
	{
		tester.getApplication().mountPage("under/test", ExpirationRecoveryPage.class);

		// Execution of Ajax callbacks doesn't record the newly created page in
		// org.apache.wicket.util.tester.BaseWicketTester.LastPageRecordingPageRendererProvider
		// so we need to use static fields
		ExpirationRecoveryPage.ajaxLinkClicked.set(false);
		ExpirationRecoveryPage.ajaxSubmitLinkSubmitted.set(false);
	}

	@Test(expected = PageExpiredException.class)
	public void cannotRecreatePageShouldThrowPEE()
	{
		PageSettings pageSettings = tester.getApplication().getPageSettings();
		pageSettings.setRecreateMountedPagesAfterExpiry(false); // CANNOT recreate

		ExpirationRecoveryPage page = tester.startPage(ExpirationRecoveryPage.class, parameters);
		assertEquals(parameters, page.getPageParameters());

		tester.getSession().invalidateNow();

		assertFalse(page.linkClicked.get());
		tester.clickLink("link", false); // leads to PageExpiredException
	}

	@Test
	public void cannotExecuteListenerInterface()
	{
		PageSettings pageSettings = tester.getApplication().getPageSettings();
		pageSettings.setRecreateMountedPagesAfterExpiry(true); // CAN recreate
		pageSettings.setCallListenerInterfaceAfterExpiry(false); // CANNOT execute listener interfaces

		ExpirationRecoveryPage page = tester.startPage(ExpirationRecoveryPage.class, parameters);
		assertEquals(parameters, page.getPageParameters());


		tester.getSession().invalidateNow();
		assertFalse(page.linkClicked.get());
		tester.clickLink("link", false);
		page = (ExpirationRecoveryPage) tester.getLastRenderedPage();
		// the page is properly recreated
		assertEquals("PageParameters should be preserved", parameters, page.getPageParameters());
		// but the listener interface is not executed
		assertFalse("Link should not be clicked!", page.linkClicked.get());


		tester.getSession().invalidateNow();
		assertFalse(page.ajaxLinkClicked.get());
		tester.clickLink("alink", true);
		page = (ExpirationRecoveryPage) tester.getLastRenderedPage();
		assertFalse("AjaxLink should not be clicked!", page.ajaxLinkClicked.get());
		assertEquals("PageParameters should be preserved", parameters, page.getPageParameters());


		tester.getSession().invalidateNow();
		assertFalse(page.submitLinkSubmitted.get());
		tester.clickLink("f:sl", false);
		page = (ExpirationRecoveryPage) tester.getLastRenderedPage();
		assertFalse("SubmitLink should not be submitted!", page.submitLinkSubmitted.get());
		assertEquals("PageParameters should be preserved", parameters, page.getPageParameters());


		tester.getSession().invalidateNow();
		assertFalse(page.ajaxSubmitLinkSubmitted.get());
		tester.clickLink("f:asl", true);
		page = (ExpirationRecoveryPage) tester.getLastRenderedPage();
		assertFalse("AjaxSubmitLink should not be submitted", page.ajaxSubmitLinkSubmitted.get());
		assertEquals("PageParameters should be preserved", parameters, page.getPageParameters());


		tester.getSession().invalidateNow();
		assertFalse(page.formSubmitted.get());
		String textOldValue = page.textModel.getObject();
		FormTester formTester = tester.newFormTester("f");
		formTester.setValue("text", "newValue");
		formTester.submit();
		page = (ExpirationRecoveryPage) tester.getLastRenderedPage();
		assertFalse("Form should not be submitted", page.formSubmitted.get());
		assertEquals("TextField's value should not be modified", textOldValue, page.textModel.getObject());
		assertEquals("PageParameters should be preserved", parameters, page.getPageParameters());
	}

	@Test
	public void canExecuteListenerInterface()
	{
		PageSettings pageSettings = tester.getApplication().getPageSettings();
		pageSettings.setCallListenerInterfaceAfterExpiry(true);
		pageSettings.setRecreateMountedPagesAfterExpiry(true);

		ExpirationRecoveryPage page = tester.startPage(ExpirationRecoveryPage.class, parameters);
		assertEquals(parameters, page.getPageParameters());


		tester.getSession().invalidateNow();
		assertFalse(page.linkClicked.get());
		tester.clickLink("link", false);
		page = (ExpirationRecoveryPage) tester.getLastRenderedPage();
		assertTrue("Link should be clicked!", page.linkClicked.get());
		assertEquals("PageParameters should be preserved", parameters, page.getPageParameters());


		tester.getSession().invalidateNow();
		assertFalse(page.ajaxLinkClicked.get());
		tester.clickLink("alink", true);
		page = (ExpirationRecoveryPage) tester.getLastRenderedPage();
		assertTrue("AjaxLink should be clicked!", page.ajaxLinkClicked.get());
		assertEquals("PageParameters should be preserved", parameters, page.getPageParameters());


		tester.getSession().invalidateNow();
		assertFalse(page.formSubmitted.get());
		FormTester formTester = tester.newFormTester("f");
		String newValue = "newValue";
		formTester.setValue("text", newValue);
		formTester.submit();
		page = (ExpirationRecoveryPage) tester.getLastRenderedPage();
		assertTrue("Form should be submitted", page.formSubmitted.get());
		assertEquals("TextField's value should be modified", newValue, page.textModel.getObject());
		assertEquals("PageParameters should be preserved", parameters, page.getPageParameters());


		tester.getSession().invalidateNow();
		assertFalse(page.submitLinkSubmitted.get());
		tester.clickLink("f:sl", false);
		page = (ExpirationRecoveryPage) tester.getLastRenderedPage();
		assertTrue("SubmitLink should be submitted!", page.submitLinkSubmitted.get());
		assertEquals("PageParameters should be preserved", parameters, page.getPageParameters());


		tester.getSession().invalidateNow();
		assertFalse(page.ajaxSubmitLinkSubmitted.get());
		tester.clickLink("f:asl", true);
		page = (ExpirationRecoveryPage) tester.getLastRenderedPage();
		assertTrue("AjaxSubmitLink should be submitted", page.ajaxSubmitLinkSubmitted.get());
		assertEquals("PageParameters should be preserved", parameters, page.getPageParameters());
	}
}
