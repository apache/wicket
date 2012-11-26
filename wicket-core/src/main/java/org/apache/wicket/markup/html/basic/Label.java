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
package org.apache.wicket.markup.html.basic;

import java.io.Serializable;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * A Label component replaces its body with the String version of its model object returned by
 * getModelObjectAsString().
 * <p>
 * Exactly what is displayed as the body, depends on the model. The simplest case is a Label with a
 * static String model, which can be constructed like this:
 * 
 * <pre>
 * add(new Label(&quot;myLabel&quot;, &quot;the string to display&quot;))
 * </pre>
 * 
 * A Label with a dynamic model can be created like this:
 * 
 * <pre>
 * 
 *       add(new Label(&quot;myLabel&quot;, new PropertyModel(person, &quot;name&quot;));
 * 
 * </pre>
 * 
 * In this case, the Label component will replace the body of the tag it is attached to with the
 * 'name' property of the given Person object, where Person might look like:
 * 
 * <pre>
 * public class Person
 * {
 * 	private String name;
 * 
 * 	public String getName()
 * 	{
 * 		return name;
 * 	}
 * 
 * 	public void setName(String name)
 * 	{
 * 		this.name = name;
 * 	}
 * }
 * </pre>
 * 
 * @author Jonathan Locke
 */
public class Label extends WebComponent
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            See Component
	 */
	public Label(final String id)
	{
		super(id);
	}

	/**
	 * Convenience constructor. Same as Label(String, new Model&lt;String&gt;(String))
	 * 
	 * @param id
	 *            See Component
	 * @param label
	 *            The label text
	 * 
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	// TODO Wicket 7 remove this constructor. The one with Serializable is the replacement
	public Label(final String id, String label)
	{
		this(id, new Model<String>(label));
	}

	/**
	 * Convenience constructor. Same as Label(String, Model.of(Serializable))
	 * 
	 * @param id
	 *            See Component
	 * @param label
	 *            The label text or object, converted to a string via the {@link org.apache.wicket.util.convert.IConverter}.
	 * 
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public Label(final String id, Serializable label)
	{
		this(id, Model.of(label));
	}

	/**
	 * @param id
	 * @param model
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public Label(final String id, IModel<?> model)
	{
		super(id, model);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		replaceComponentTagBody(markupStream, openTag, getDefaultModelObjectAsString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);

		if (tag.isOpenClose())
		{
			// always transform the tag to <span></span> so even labels defined as <span/> render
			tag.setType(TagType.OPEN);
		}
	}
}
