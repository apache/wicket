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

import org.apache.wicket.core.util.string.CssUtils;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.util.lang.Args;

/**
 * A component that represents HTML &lt;label&gt; tag. This component will automatically make the
 * form component output an <em>id</em> attribute and link its <em>for</em> attribute with that
 * value.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class FormComponentLabel extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	public static final String REQUIRED_CSS_CLASS_KEY = CssUtils.key(FormComponentLabel.class,
			"required");

	public static final String INVALID_CSS_CLASS_KEY = CssUtils.key(FormComponentLabel.class,
			"invalid");

	public static final String DISABLED_CSS_CLASS_KEY = CssUtils.key(FormComponentLabel.class,
			"disabled");

	private final LabeledWebMarkupContainer component;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param component
	 *            component that this label is linked to
	 */
	public FormComponentLabel(String id, LabeledWebMarkupContainer component)
	{
		super(id);

		this.component = Args.notNull(component, "component");
		component.setOutputMarkupId(true);
	}

	/**
	 * 
	 * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);

		checkComponentTag(tag, "label");

		LabeledWebMarkupContainer formComponent = getFormComponent();

		tag.put("for", formComponent.getMarkupId());

		if (formComponent instanceof FormComponent<?>)
		{
			FormComponent<?> fc = (FormComponent<?>) formComponent;

			if (fc.isRequired())
			{
				tag.append("class", getString(REQUIRED_CSS_CLASS_KEY), " ");
			}
			if (fc.isValid() == false)
			{
				tag.append("class", getString(INVALID_CSS_CLASS_KEY), " ");
			}
		}

		if (formComponent.isEnabledInHierarchy() == false)
		{
			tag.append("class", getString(DISABLED_CSS_CLASS_KEY), " ");
		}

		// always transform the tag to <span></span> so even labels defined as <span/> render
		tag.setType(TagType.OPEN);
	}

	/**
	 * Returns LabeledWebMarkupContainer bound to this label. This will be a FormComponent, a Radio
	 * or a Check.
	 * 
	 * @return form component
	 */
	public LabeledWebMarkupContainer getFormComponent()
	{
		return component;
	}
}
