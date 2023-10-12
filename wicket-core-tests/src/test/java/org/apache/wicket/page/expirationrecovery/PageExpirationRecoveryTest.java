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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.settings.PageSettings;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for
 * WICKET-5068 PageParameters missing from re-created Page
 * WICKET-5070 Optionally execute Callback Behavior on Re-construction after Expiry
 * WICKET-5001 Recovery of bookmarkable Page after Session Expiry
 */
class PageExpirationRecoveryTest extends WicketTestCase
{

	private final PageParameters parameters = new PageParameters()
			.set("a", "b")
			.set("c", "d");

	@BeforeEach
	void before()
	{
		tester.getApplication().mountPage("under/test", ExpirationRecoveryPage.class);

		// Execution of Ajax callbacks doesn't record the newly created page in
		// org.apache.wicket.util.tester.BaseWicketTester.LastPageRecordingPageRendererProvider
		// so we need to use static fields
		ExpirationRecoveryPage.ajaxLinkClicked.set(false);
		ExpirationRecoveryPage.ajaxSubmitLinkSubmitted.set(false);
	}

	@Test
	void cannotRecreatePageShouldThrowPEE()
	{
		PageSettings pageSettings = tester.getApplication().getPageSettings();
		pageSettings.setRecreateBookmarkablePagesAfterExpiry(false); // CANNOT recreate

		ExpirationRecoveryPage page = tester.startPage(ExpirationRecoveryPage.class, parameters);
		assertEquals(parameters, page.getPageParameters());

		tester.getSession().invalidateNow();

		assertFalse(page.linkClicked.get());

		assertThrows(PageExpiredException.class, () -> {
			tester.clickLink("link", false);
		});
	}

	@Test
	void cannotExecuteListener()
	{
		PageSettings pageSettings = tester.getApplication().getPageSettings();
		pageSettings.setRecreateBookmarkablePagesAfterExpiry(true); // CAN recreate
		pageSettings.setCallListenerAfterExpiry(false); // CANNOT execute listener interfaces

		ExpirationRecoveryPage page = tester.startPage(ExpirationRecoveryPage.class, parameters);
		assertEquals(parameters, page.getPageParameters());


		tester.getSession().invalidateNow();
		assertFalse(page.linkClicked.get());
		tester.clickLink("link", false);
		page = (ExpirationRecoveryPage) tester.getLastRenderedPage();
		// the page is properly recreated
		assertEquals(parameters, page.getPageParameters(), "PageParameters should be preserved");
		// but the listener interface is not executed
		assertFalse(page.linkClicked.get(), "Link should not be clicked!");


		tester.getSession().invalidateNow();
		assertFalse(page.ajaxLinkClicked.get());
		tester.clickLink("alink", true);
		page = (ExpirationRecoveryPage) tester.getLastRenderedPage();
		assertFalse(page.ajaxLinkClicked.get(), "AjaxLink should not be clicked!");
		assertEquals(parameters, page.getPageParameters(), "PageParameters should be preserved");


		tester.getSession().invalidateNow();
		assertFalse(page.submitLinkSubmitted.get());
		tester.clickLink("f:sl", false);
		page = (ExpirationRecoveryPage) tester.getLastRenderedPage();
		assertFalse(page.submitLinkSubmitted.get(), "SubmitLink should not be submitted!");
		assertEquals(parameters, page.getPageParameters(), "PageParameters should be preserved");


		tester.getSession().invalidateNow();
		assertFalse(page.ajaxSubmitLinkSubmitted.get());
		tester.clickLink("f:asl", true);
		page = (ExpirationRecoveryPage) tester.getLastRenderedPage();
		assertFalse(page.ajaxSubmitLinkSubmitted.get(), "AjaxSubmitLink should not be submitted");
		assertEquals(parameters, page.getPageParameters(), "PageParameters should be preserved");


		tester.getSession().invalidateNow();
		assertFalse(page.formSubmitted.get());
		String textOldValue = page.textModel.getObject();
		FormTester formTester = tester.newFormTester("f");
		formTester.setValue("text", "newValue");
		formTester.submit();
		page = (ExpirationRecoveryPage) tester.getLastRenderedPage();
		assertFalse(page.formSubmitted.get(), "Form should not be submitted");
		assertEquals(textOldValue, page.textModel.getObject(), "TextField's value should not be modified");
		assertEquals(parameters, page.getPageParameters(), "PageParameters should be preserved");
	}

	@Test
	void canExecuteListener()
	{
		PageSettings pageSettings = tester.getApplication().getPageSettings();
		pageSettings.setCallListenerAfterExpiry(true);
		pageSettings.setRecreateBookmarkablePagesAfterExpiry(true);

		ExpirationRecoveryPage page = tester.startPage(ExpirationRecoveryPage.class, parameters);
		assertEquals(page.getPageParameters(), parameters);


		tester.getSession().invalidateNow();
		assertFalse(page.linkClicked.get());
		tester.clickLink("link", false);
		page = (ExpirationRecoveryPage) tester.getLastRenderedPage();
		assertTrue(page.linkClicked.get(), "Link should be clicked!");
		assertEquals(parameters, page.getPageParameters(), "PageParameters should be preserved");


		tester.getSession().invalidateNow();
		assertFalse(page.ajaxLinkClicked.get());
		tester.clickLink("alink", true);
		page = (ExpirationRecoveryPage) tester.getLastRenderedPage();
		assertTrue(page.ajaxLinkClicked.get(), "AjaxLink should be clicked!");
		assertEquals(parameters, page.getPageParameters(), "PageParameters should be preserved");


		tester.getSession().invalidateNow();
		assertFalse(page.formSubmitted.get());
		FormTester formTester = tester.newFormTester("f");
		String newValue = "newValue";
		formTester.setValue("text", newValue);
		formTester.submit();
		page = (ExpirationRecoveryPage) tester.getLastRenderedPage();
		assertTrue(page.formSubmitted.get(), "Form should be submitted");
		assertEquals(newValue, page.textModel.getObject(), "TextField's value should be modified");
		assertEquals(parameters, page.getPageParameters(), "PageParameters should be preserved");


		tester.getSession().invalidateNow();
		assertFalse(page.submitLinkSubmitted.get());
		tester.clickLink("f:sl", false);
		page = (ExpirationRecoveryPage) tester.getLastRenderedPage();
		assertTrue(page.submitLinkSubmitted.get(), "SubmitLink should be submitted!");
		assertEquals(parameters, page.getPageParameters(), "PageParameters should be preserved");


		tester.getSession().invalidateNow();
		assertFalse(page.ajaxSubmitLinkSubmitted.get());
		tester.clickLink("f:asl", true);
		page = (ExpirationRecoveryPage) tester.getLastRenderedPage();
		assertTrue(page.ajaxSubmitLinkSubmitted.get(), "AjaxSubmitLink should be submitted");
		assertEquals(parameters, page.getPageParameters(), "PageParameters should be preserved");
	}
}
