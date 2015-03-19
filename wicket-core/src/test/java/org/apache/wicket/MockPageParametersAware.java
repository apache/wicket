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
package org.apache.wicket;

import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;


/**
 * Mock Page to maintain the request parameters after the request cycle end
 */
public class MockPageParametersAware extends WebPage
	implements
		IMarkupResourceStreamProvider,
		IRequestableComponent
{
	private static final long serialVersionUID = 1L;

	private IRequestParameters lastQueryParameters;
	private IRequestParameters lastPostParameters;
	private TextField<String> textField;

	/** */
	public MockPageParametersAware()
	{
		Form<Void> form = newForm("form");
		textField = new TextField<String>("textfield", Model.of(""));
		form.add(textField);
		add(form);
	}

	protected Form<Void> newForm(String id)
	{
		return new Form<Void>(id);
	}

	@Override
	protected void onDetach()
	{
		super.onDetach();
		lastQueryParameters = getRequest().getQueryParameters();
		lastPostParameters = getRequest().getPostParameters();
	}

	/**
	 * @return IRequestParameters query parameters used on last request
	 */
	public IRequestParameters getLastQueryParameters()
	{
		return lastQueryParameters;
	}


	/**
	 * @return IRequestParameters POST parameters used on last request
	 */
	public IRequestParameters getLastPostParameters()
	{
		return lastPostParameters;
	}

	/**
	 * just an utility
	 */
	public void printParameters()
	{
		for (String n : lastPostParameters.getParameterNames())
		{
			System.out.println("post: " + n + " : " + lastPostParameters.getParameterValues(n));
		}
		for (String n : lastQueryParameters.getParameterNames())
		{
			System.out.println("query: " + n + " : " + lastQueryParameters.getParameterValues(n));
		}
	}


	/**
	 * @return textField
	 */
	public TextField<String> getTextField()
	{
		return textField;
	}

	@Override
	public IResourceStream getMarkupResourceStream(MarkupContainer container,
		Class<?> containerClass)
	{
		return new StringResourceStream(
			"<html><body><form wicket:id=\"form\"><input wicket:id=\"textfield\"/></form></body></html>");
	}
}
