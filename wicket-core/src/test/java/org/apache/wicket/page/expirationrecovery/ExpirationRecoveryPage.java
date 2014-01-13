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

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Test page for PageExpirationRecoveryTest
 */
public class ExpirationRecoveryPage extends WebPage
{
	final IModel<String> textModel = Model.of("texttt");
	final AtomicBoolean formSubmitted = new AtomicBoolean(false);
	final AtomicBoolean submitLinkSubmitted = new AtomicBoolean(false);
	// Execution of Ajax callbacks doesn't record the newly created page in
	// org.apache.wicket.util.tester.BaseWicketTester.LastPageRecordingPageRendererProvider
	// so we need to use static fields
	static final AtomicBoolean ajaxSubmitLinkSubmitted = new AtomicBoolean(false);
	static final AtomicBoolean ajaxLinkClicked = new AtomicBoolean(false);
	final AtomicBoolean linkClicked = new AtomicBoolean(false);

	public ExpirationRecoveryPage(final PageParameters parameters)
	{
		super(parameters);

		Form f;
		add(f = createForm("f"), createLink("link"), createAjaxLink("alink"));

		f.add(new TextField<>("text", textModel), createSubmitLink("sl"), createAjaxSubmitLink("asl"));
    }

	private Form createForm(String id)
	{
		return new Form(id)
		{
			@Override
			protected void onSubmit()
			{
				super.onSubmit();

				formSubmitted.set(true);
			}
		};
	}

	private AjaxSubmitLink createAjaxSubmitLink(String id)
	{
		return new AjaxSubmitLink(id)
		{
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				super.onSubmit(target, form);
				ajaxSubmitLinkSubmitted.set(true);
			}

		};
	}

	private SubmitLink createSubmitLink(String id)
	{
		return new SubmitLink(id)
		{
			@Override
			public void onSubmit()
			{
				super.onSubmit();
				submitLinkSubmitted.set(true);
			}

		};
	}

	private Component createAjaxLink(String id)
	{
		return new AjaxLink<Void>(id)
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				ajaxLinkClicked.set(true);
			}
		};
	}

	private Link<Void> createLink(String id)
	{
		return new Link<Void>(id)
		{
			@Override
			public void onClick()
			{
				linkClicked.set(true);
			}
		};
	}
}
