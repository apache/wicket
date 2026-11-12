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
package org.apache.wicket.csp;

import static org.apache.wicket.csp.CSPDirective.IMG_SRC;
import static org.apache.wicket.csp.CSPDirectiveSrcValue.SELF;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.WicketAjaxResourceReference;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

/**
 * Tests for the opt-in Trusted Types support: the CSP headers and the flag for Wicket's JavaScript.
 */
class CSPTrustedTypesTest extends WicketTestCase
{
	private static final String FLAG = "<meta name=\"wicket-trusted-types\" content=\"wicket\" />";

	private static final String TRUSTED_TYPES = "require-trusted-types-for 'script'; "
		+ "trusted-types wicket";

	private static final String STRICT = "default-src 'none'; "
		+ "script-src 'strict-dynamic' 'nonce-NONCE'; "
		+ "style-src 'nonce-NONCE'; img-src 'self'; connect-src 'self'; font-src 'self'; "
		+ "manifest-src 'self'; child-src 'self'; base-uri 'self'; frame-src 'self'";

	private ContentSecurityPolicySettings settings()
	{
		return tester.getApplication().getCspSettings().setNonceCreator(() -> "NONCE");
	}

	@Test
	void flagIsRenderedWithBlockingHeader()
	{
		settings().blocking().requireTrustedTypes();

		tester.startPage(AjaxPage.class);

		assertEquals(TRUSTED_TYPES, tester.getLastResponse().getHeader("Content-Security-Policy"));
		assertNull(tester.getLastResponse().getHeader("Content-Security-Policy-Report-Only"));
		assertFlagBeforeAjaxScript("wicket-ajax-jquery.js");
	}

	@Test
	void flagIsRenderedWithReportingHeader()
	{
		settings().reporting().requireTrustedTypes();

		tester.startPage(AjaxPage.class);

		assertEquals(TRUSTED_TYPES,
			tester.getLastResponse().getHeader("Content-Security-Policy-Report-Only"));
		assertNull(tester.getLastResponse().getHeader("Content-Security-Policy"));
		assertFlagBeforeAjaxScript("wicket-ajax-jquery.js");
	}

	@Test
	void flagIsRenderedForJQueryFreeAjax()
	{
		tester.getApplication()
			.getJavaScriptLibrarySettings()
			.setWicketAjaxReference(WicketAjaxResourceReference.get());
		settings().blocking().requireTrustedTypes();

		tester.startPage(AjaxPage.class);

		assertFlagBeforeAjaxScript("wicket-ajax.js");
	}

	@Test
	void flagIsNotRenderedWithoutTrustedTypes()
	{
		settings().blocking().add(IMG_SRC, SELF);
		settings().reporting().add(IMG_SRC, SELF);

		tester.startPage(AjaxPage.class);
		String header = tester.getLastResponse().getHeader("Content-Security-Policy");
		String document = tester.getLastResponseAsString();

		assertEquals("img-src 'self'", header);
		assertThat(document).doesNotContain("wicket-trusted-types")
			.doesNotContain("Wicket.TrustedTypes")
			.isEqualTo(renderWithoutCsp());
	}

	private String renderWithoutCsp()
	{
		WicketTester withoutCsp = new WicketTester(new MockApplication());
		try
		{
			withoutCsp.startPage(AjaxPage.class);
			assertFalse(withoutCsp.getLastResponse().containsHeader("Content-Security-Policy"));
			return withoutCsp.getLastResponseAsString();
		}
		finally
		{
			withoutCsp.destroy();
		}
	}

	@Test
	void flagIsNotRenderedForOtherPolicyNames()
	{
		settings().blocking().add(CSPDirective.TRUSTED_TYPES, "myPolicy");

		tester.startPage(AjaxPage.class);

		assertEquals("trusted-types myPolicy",
			tester.getLastResponse().getHeader("Content-Security-Policy"));
		assertThat(tester.getLastResponseAsString()).doesNotContain("wicket-trusted-types");
	}

	@Test
	void flagIsNotRenderedOnUnprotectedPage()
	{
		settings().setProtectedFilter(page -> !(page instanceof AjaxPage))
			.blocking()
			.requireTrustedTypes();

		tester.startPage(AjaxPage.class);

		assertFalse(tester.getLastResponse().containsHeader("Content-Security-Policy"));
		assertThat(tester.getLastResponseAsString()).doesNotContain("wicket-trusted-types");
	}

	@Test
	void requireTrustedTypesTwice()
	{
		settings().blocking().requireTrustedTypes().requireTrustedTypes();

		tester.startPage(AjaxPage.class);

		assertEquals(TRUSTED_TYPES, tester.getLastResponse().getHeader("Content-Security-Policy"));
	}

	@Test
	void requireTrustedTypesKeepsExistingValues()
	{
		settings().blocking()
			.add(CSPDirective.TRUSTED_TYPES, "myPolicy",
				CSPHeaderConfiguration.TRUSTED_TYPES_POLICY)
			.requireTrustedTypes()
			.requireTrustedTypes();

		tester.startPage(AjaxPage.class);

		assertEquals("require-trusted-types-for 'script'; trusted-types myPolicy wicket",
			tester.getLastResponse().getHeader("Content-Security-Policy"));
	}

	@Test
	void requireTrustedTypesWithStrict()
	{
		settings().blocking().strict().requireTrustedTypes().requireTrustedTypes();

		tester.startPage(AjaxPage.class);

		assertEquals(STRICT + "; " + TRUSTED_TYPES,
			tester.getLastResponse().getHeader("Content-Security-Policy"));
		assertFlagBeforeAjaxScript("wicket-ajax-jquery.js");
	}

	@Test
	void requireTrustedTypesWithReporting()
	{
		settings().blocking().strict();
		settings().reporting().strict().requireTrustedTypes().requireTrustedTypes();

		tester.startPage(AjaxPage.class);

		assertEquals(STRICT, tester.getLastResponse().getHeader("Content-Security-Policy"));
		assertEquals(STRICT + "; " + TRUSTED_TYPES,
			tester.getLastResponse().getHeader("Content-Security-Policy-Report-Only"));
		assertFlagBeforeAjaxScript("wicket-ajax-jquery.js");
	}

	@Test
	void formScriptsUseThePolicy()
	{
		settings().blocking().requireTrustedTypes();

		tester.startPage(AjaxPage.class);
		AjaxPage page = (AjaxPage)tester.getLastRenderedPage();

		assertEquals("var f = document.getElementById('form2');"
			+ "var h = document.getElementById('form2_hf_0');"
			+ "h.innerHTML = Wicket.TrustedTypes.html("
			+ "h.innerHTML + '<input type=\"hidden\" name=\"send\" value=\"x\" />');"
			+ "Wicket.Event.requestSubmit(f);",
			page.form.getJsForSubmitter(page.submit, true).toString());
		assertEquals("document.getElementById('form2_hf_0').innerHTML = Wicket.TrustedTypes.html("
			+ "'<input type=\"hidden\" name=\"a\" value=\"b\" />');"
			+ "var f = document.getElementById('form2');f.action='path';"
			+ "if (Wicket.Event.triggerSubmit(f)) { HTMLFormElement.prototype.submit.call(f); }",
			page.form.getJsForListenerUrl("path?a=b").toString());
	}

	@Test
	void formScriptsAreUnchangedWithoutTrustedTypes()
	{
		settings().blocking().add(IMG_SRC, SELF);

		tester.startPage(AjaxPage.class);
		AjaxPage page = (AjaxPage)tester.getLastRenderedPage();

		assertEquals("var f = document.getElementById('form2');"
			+ "document.getElementById('form2_hf_0').innerHTML += "
			+ "'<input type=\"hidden\" name=\"send\" value=\"x\" />';"
			+ "Wicket.Event.requestSubmit(f);",
			page.form.getJsForSubmitter(page.submit, true).toString());
		assertEquals("document.getElementById('form2_hf_0').innerHTML = "
			+ "'<input type=\"hidden\" name=\"a\" value=\"b\" />';"
			+ "var f = document.getElementById('form2');f.action='path';"
			+ "if (Wicket.Event.triggerSubmit(f)) { HTMLFormElement.prototype.submit.call(f); }",
			page.form.getJsForListenerUrl("path?a=b").toString());
	}

	private void assertFlagBeforeAjaxScript(String script)
	{
		String document = tester.getLastResponseAsString();

		assertThat(document).containsOnlyOnce(FLAG);
		assertThat(document.indexOf(FLAG)).isLessThan(document.indexOf(script));
	}

	public static class AjaxPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		final Form<Void> form;

		final SubmitLink submit;

		public AjaxPage()
		{
			add(new AjaxLink<Void>("link")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target)
				{
					target.add(this);
				}
			});
			form = new Form<>("form")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected String getMethod()
				{
					return METHOD_GET;
				}
			};
			add(form);
			submit = new SubmitLink("send");
			form.add(submit);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><head></head><body>"
				+ "<a wicket:id=\"link\">link</a>"
				+ "<form wicket:id=\"form\"><a wicket:id=\"send\">send</a></form></body></html>");
		}
	}
}
