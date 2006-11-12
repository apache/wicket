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
package wicket.markup.html.basic;

import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebComponent;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.util.string.Strings;

/**
 * A MultiLineLabel component replaces its body with the model object.
 * <p>
 * Unlike {@link Label}, {@link MultiLineLabel} shows text that spans multiple
 * lines by inserting line breaks (<code>BR</code> tags) for newlines and
 * paragraph markers (<code>P</code> tags) for sequences of more than one
 * newline.
 * 
 * @author Jonathan Locke
 */
public class MultiLineLabel extends WebComponent
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor. Same as Label(String).
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            See Component
	 * @see Label#Label(MarkupContainer, String)
	 */
	public MultiLineLabel(MarkupContainer parent, final String id)
	{
		super(parent, id);
	}

	/**
	 * Convenience constructor. Same as MultiLineLabel(String, new
	 * Model(String))
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            See Component
	 * @param label
	 *            The label text
	 * 
	 * @see wicket.Component#Component(MarkupContainer, String, IModel)
	 */
	public MultiLineLabel(MarkupContainer parent, final String id, String label)
	{
		this(parent, id, new Model<String>(label));
	}

	/**
	 * @see wicket.Component#Component(MarkupContainer, String, IModel)
	 */
	@SuppressWarnings("unchecked")
	public MultiLineLabel(MarkupContainer parent, final String id, IModel model)
	{
		super(parent, id, model);
	}

	/**
	 * @see wicket.Component#onComponentTagBody(MarkupStream, ComponentTag)
	 */
	@Override
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		final CharSequence body = Strings.toMultilineMarkup(getModelObjectAsString());
		replaceComponentTagBody(markupStream, openTag, body);
	}
}
