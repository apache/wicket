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
package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.UrlValidator;

/**
 * A {@link TextField} for HTML5 &lt;input&gt; with type <em>url</em>.
 * 
 * <p>
 * Automatically validates the input that it is a valid Url.
 */
public class UrlTextField extends TextField<String>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 * @param url
	 *            the url input value
	 */
	public UrlTextField(String id, final String url)
	{
		this(id, new Model<String>(url));
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            the component id
	 * @param model
	 *            the input value
	 */
	public UrlTextField(String id, IModel<String> model)
	{
		this(id, model, new UrlValidator());
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            the component id
	 * @param model
	 *            the input value
	 * @param urlValidator
	 *            the validator that will check the correctness of the input value
	 */
	public UrlTextField(String id, IModel<String> model, UrlValidator urlValidator)
	{
		super(id, model, String.class);

		add(urlValidator);
	}

	@Override
	protected String[] getInputTypes()
	{
		return new String[] {"url"};
	}
}
