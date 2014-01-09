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
package org.apache.wicket.examples.base.markdown;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

public class MarkdownLabel extends Label
{
	private static final long serialVersionUID = 1L;

	public MarkdownLabel(String id)
	{
		super(id);
	}

	public MarkdownLabel(String id, IModel<?> model)
	{
		super(id, model);
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		setEscapeModelStrings(false);
	}

	@Override
	public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
		String markdown = getModelObjectAsUnescapedString();
		String html = Markdown.markdownToHtml(markdown);
		replaceComponentTagBody(markupStream, openTag, html);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String getModelObjectAsUnescapedString()
	{
		Object modelObject = getDefaultModelObject();
		if (modelObject != null)
		{
			// Get converter
			final Class<?> objectClass = modelObject.getClass();

			final IConverter converter = getConverter(objectClass);

			// Model string from property
			final String modelString = converter.convertToString(modelObject, getLocale());

			return modelString;
		}
		return null;
	}
}
