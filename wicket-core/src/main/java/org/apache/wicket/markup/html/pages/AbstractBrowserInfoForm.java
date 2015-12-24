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
package org.apache.wicket.markup.html.pages;

import java.util.Locale;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * @author Rakesh.A
 */
public abstract class AbstractBrowserInfoForm<M extends ClientProperties> extends GenericPanel<M>
{
	public static final ResourceReference BROWSER_INFO_JS = new JavaScriptResourceReference(AbstractBrowserInfoForm.class, "wicket-browser-info.js");

	/**
	 * The special form that submits the client/browser info
	 */
	private final Form<M> form;

	/**
	 * @param id
	 */
	public AbstractBrowserInfoForm(String id, IModel<M> properties)
	{
		super(id, properties);

		this.form = createForm("postback", properties);
		form.setOutputMarkupId(true);
		add(form);
	}

	/**
	 * @return The markup id of the form that submits the client info
	 */
	public final String getFormMarkupId()
	{
		return form.getMarkupId();
	}

	/**
	 * @return Form<M>
	 */
	protected final Form<M> getForm()
	{
		return this.form;
	}

	/**
	 * @param clientInfo
	 * @param propertiesBean
	 */
	protected void afterSubmit(M propertiesBean)
	{
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(BROWSER_INFO_JS));
	}

	/**
	 * Creates the form
	 *
	 * @param componentId
	 *            the id for the Form component
	 * @return the Form that will submit the data
	 */
	protected Form<M> createForm(String componentId, IModel<M> properties)
	{
		Form<M> form = new Form<M>(componentId, new CompoundPropertyModel<M>(properties))
		{
			@Override
			protected void onSubmit()
			{
				afterSubmit(getModelObject());
			}
		};

		form.add(new ReadOnlyTextField<String>("navigatorAppName"));
		form.add(new ReadOnlyTextField<String>("navigatorAppVersion"));
		form.add(new ReadOnlyTextField<String>("navigatorAppCodeName"));
		form.add(new ReadOnlyTextField<Boolean>("navigatorCookieEnabled"));
		form.add(new ReadOnlyTextField<Boolean>("navigatorJavaEnabled"));
		form.add(new ReadOnlyTextField<String>("navigatorLanguage"));
		form.add(new ReadOnlyTextField<String>("navigatorPlatform"));
		form.add(new ReadOnlyTextField<String>("navigatorUserAgent"));
		form.add(new ReadOnlyTextField<String>("screenWidth"));
		form.add(new ReadOnlyTextField<String>("screenHeight"));
		form.add(new ReadOnlyTextField<String>("screenColorDepth"));
		form.add(new ReadOnlyTextField<String>("utcOffset"));
		form.add(new ReadOnlyTextField<String>("utcDSTOffset"));
		form.add(new ReadOnlyTextField<String>("browserWidth"));
		form.add(new ReadOnlyTextField<String>("browserHeight"));
		form.add(new ReadOnlyTextField<String>("hostname"));

		return form;
	}

	private static final class ReadOnlyTextField<T> extends TextField<T> {

		public ReadOnlyTextField(String id)
		{
			super(id);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getModelValue()
		{
			return "";
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Locale getLocale()
		{
			return Locale.ENGLISH;
		}
	}
}
