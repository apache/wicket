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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.model.IModel;

/**
 * Multi-row text editing component.
 * 
 * @author Jonathan Locke
 * 
 * @param <T>
 *            The model object type
 */
public class TextArea<T> extends AbstractTextComponent<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public TextArea(final String id)
	{
		super(id);
	}

	/**
	 * @param id
	 * @param model
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public TextArea(final String id, final IModel<T> model)
	{
		super(id, model);
	}

	/**
	 * Handle the container's body.
	 * 
	 * @param markupStream
	 *            The markup stream
	 * @param openTag
	 *            The open tag for the body
	 * @see org.apache.wicket.Component#onComponentTagBody(MarkupStream, ComponentTag)
	 */
	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		checkComponentTag(openTag, "textarea");

		String value = getValue();
		if (value != null)
		{
			if (value.startsWith("\n"))
			{
				value = "\n" + value;
			}
			else if (value.startsWith("\r\n"))
			{
				value = "\r\n" + value;
			}
			else if (value.startsWith("\r"))
			{
				value = "\r" + value;
			}
		}
		replaceComponentTagBody(markupStream, openTag, value);
	}
}
