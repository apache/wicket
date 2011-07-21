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

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.AutoLabelResolver.AutoLabel;
import org.apache.wicket.markup.html.internal.ResponseBufferZone;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.markup.parser.filter.WicketTagIdentifier;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.Strings;

/**
 * Resolver that provides the <code>{@literal <wicket:label>}</code> tag, which will output a
 * FormComponent's {@link FormComponent#getLabel() label} without requiring a manual extra component
 * such as {@link Label} or {@link FormComponentLabel}.
 * 
 * <code>{@literal <wicket:label>}</code> can be used
 * <ul>
 * <li>together with <code>{@literal <label wicket:for="...">}</code>:
 * 
 * <pre>
 * {@literal
 * <label wicket:for="myFormComponent">some other markup, optionally<wicket:label/></label>
 * }
 * </pre>
 * 
 * </li>
 * <li>
 * standalone, with a <code>for</code> attribute:
 * 
 * <pre>
 * {@literal
 * <wicket:label for="myFormComponent"/>
 * }
 * </pre>
 * 
 * </li>
 * </ul>
 * <p>
 * It also supports both input and output:
 * <ul>
 * <li>If the FormComponent has a label model, the <code>{@literal <wicket:label>}</code> tag will
 * be replaced by the contents of that label.</li>
 * <li>If the FormComponent's label model is null, it can be picked up from
 * <code>{@literal <wicket:label>}</code>:
 * <ul>
 * <li><code>{@literal <wicket:label>}</code> can contain some raw markup, like this:
 * 
 * <pre>
 * {@literal
 * <wicket:label>I will become the component's label!</wicket:label>
 * }
 * </pre>
 * 
 * </li>
 * <li>Or it can be a message pulled from resources, similar to
 * <code>{@literal <wicket:message/>}</code>:
 * 
 * <pre>
 * {@literal
 * <wicket:label key="messagekey"/>
 * }
 * </pre>
 * 
 * </li>
 * </ul>
 * </li>
 * </ul>
 * 
 * 
 * @author Carl-Eric Menzel <cmenzel@wicketbuch.de>
 * @author igor
 */
public class AutoLabelTextResolver implements IComponentResolver
{
	static
	{
		WicketTagIdentifier.registerWellKnownTagName("label");
	}

	/**
	 * This is inserted by the resolver to render the label.
	 */
	private static class TextLabel extends WebMarkupContainer
	{

		private final Component labeled;

		public TextLabel(String id, Component labeled)

		{
			super(id);
			this.labeled = labeled;
			setRenderBodyOnly(true);
		}

		@Override
		protected void onComponentTag(final ComponentTag tag)
		{
			if (tag.isOpenClose())
			{
				tag.setType(XmlTag.TagType.OPEN);
			}
			super.onComponentTag(tag);
		}

		@Override
		public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
		{
			boolean storeLabelText = false;

			// try and find some form of label content...
			String labelText = null;

			// check if the labeled component is a label provider

			if (labeled instanceof ILabelProvider)
			{
				ILabelProvider<String> provider = (ILabelProvider<String>)labeled;
				if (provider.getLabel() != null)
				{
					String text = provider.getLabel().getObject();
					if (!Strings.isEmpty(text))
					{
						labelText = text;
					}
				}
			}

			// check if the labeled component is a form component

			if (labelText == null && labeled instanceof FormComponent)
			{
				String text = ((FormComponent<?>)labeled).getDefaultLabel("wicket:unknown");
				if (!"wicket:unknown".equals(text) && !Strings.isEmpty(text))
				{
					labelText = text;
				}
			}

			// check if wicket:label tag has a message key
			if (labelText == null && openTag.getAttribute("key") != null)
			{
				String text = labeled.getString(openTag.getAttribute("key"));
				if (!Strings.isEmpty(text))
				{
					labelText = text;
					storeLabelText = true;
				}
			}

			// as last resort use the tag body
			if (labelText == null)
			{
				String text = new ResponseBufferZone(RequestCycle.get(), markupStream)
				{
					@Override
					protected void executeInsideBufferedZone()
					{
						TextLabel.super.onComponentTagBody(markupStream, openTag);
					}
				}.execute().toString();

				if (!Strings.isEmpty(text))
				{
					labelText = text;
					storeLabelText = true;
				}
			}

			// print the label text
			replaceComponentTagBody(markupStream, openTag, labelText);

			// store the label text in FormComponent's label model so its available to errors
			if (labeled instanceof FormComponent)
			{
				FormComponent<?> fc = (FormComponent<?>)labeled;
				fc.setLabel(Model.of(labelText));
			}
		}
	}

	public Component resolve(MarkupContainer container, MarkupStream markupStream, ComponentTag tag)
	{
		if (tag instanceof WicketTag && "label".equals(((WicketTag)tag).getName()))
		{
			// We need to find a FormComponent...
			Component related = null;
			// ...which could be explicitly specified...
			if (tag.getAttribute("for") != null)
			{
				Component component = AutoLabelResolver.findRelatedComponent(container,
					tag.getAttribute("for"));
				related = component;
			}
			if (related == null)
			{
				// ...or available through an AutoLabel, either directly above us...
				if (container instanceof AutoLabel)
				{
					related = ((AutoLabel)container).getRelatedComponent();
				}
				if (related == null)
				{
					// ...or perhaps further up...
					AutoLabel autoLabel = container.findParent(AutoLabel.class);
					if (autoLabel != null)
					{
						related = autoLabel.getRelatedComponent();
					}
				}
			}
			if (related == null)
			{
				// ...or it might just not be available.
				throw new IllegalStateException("no related component found for <wicket:label>");
			}
			else
			{
				// ...found the form component, so we can return our label.
				return new TextLabel("label" + container.getPage().getAutoIndex(), related);
			}
		}
		return null;
	}

}
