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
import org.apache.wicket.core.request.handler.ComponentNotFoundException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.AutoLabelResolver.AutoLabel;
import org.apache.wicket.markup.html.internal.ResponseBufferZone;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
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
 * <p>
 * The label taken from a model or from a resource bundle is text, so it is escaped before it is
 * written. A tag can ask for it to be written as is instead:
 * 
 * <pre>
 * {@literal
 * <wicket:label escape="false"/>
 * }
 * </pre>
 * 
 * The application then takes responsibility for the content. The attribute accepts
 * <code>true</code>/<code>false</code>, <code>on</code>/<code>off</code>,
 * <code>yes</code>/<code>no</code>, <code>y</code>/<code>n</code> and <code>1</code>/<code>0</code>;
 * any other value raises a
 * {@link org.apache.wicket.util.string.StringValueConversionException}. Leaving the attribute out,
 * or leaving it empty, keeps the escaping.
 * <p>
 * The attribute says nothing about the tag body. That body is markup this label has just rendered
 * itself and is always written as is. Note also that <code>{@literal <wicket:message>}</code>
 * spells the same attribute the other way round: a message is written as markup by default and
 * <code>escape="true"</code> asks for it to be escaped.
 * 
 * @author Carl-Eric Menzel
 * @author igor
 */
public class AutoLabelTextResolver implements IComponentResolver
{
	public static final String LABEL = "label";

	public static final String ESCAPE_ATTRIBUTE = "escape";

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

			// try and find some form of label content...
			IModel<String> labelModel = findLabelModel(openTag);

			if (labelModel != null)
			{
				// the label is text, coming from a model or from a resource bundle, so it has to
				// be escaped before it goes into the markup. Escaping is read from this component
				// and not from the labeled one: TextField and Button clear the flag in their
				// constructor so that their value attribute is not encoded twice, which would
				// leave the label unescaped for exactly the components <wicket:label> is used with
				String text = labelModel.getObject();
				replaceComponentTagBody(markupStream, openTag,
					getEscapeModelStrings() ? Strings.escapeMarkup(text) : text);
			}
			else
			{
				// as a last resort use the tag body. That body is markup this label has just
				// rendered itself, nested components and <wicket:message> included, so it is
				// written as is. It is also the way to put markup in a label deliberately
				CharSequence body = new ResponseBufferZone(RequestCycle.get(), markupStream)
				{
					@Override
					protected void executeInsideBufferedZone()
					{
						TextLabel.super.onComponentTagBody(markupStream, openTag);
					}
				}.execute();

				replaceComponentTagBody(markupStream, openTag, body);

				if (!Strings.isEmpty(body))
				{
					labelModel = Model.of(body.toString());
				}
			}

			// store the label text in FormComponent's label model so its available to errors
			if (labelModel != null)
			{
				if (labeled instanceof FormComponent)
				{
					FormComponent<?> fc = (FormComponent<?>)labeled;
					fc.setLabel(labelModel);
				}
				else
				{
					// if we can't hand off the labelmodel to a component, we have to detach it
					labelModel.detach();
				}
			}
		}

		/**
		 * Finds the label as text, from the labeled component or from a resource bundle. Returns
		 * null when there is none, in which case the tag body is used instead.
		 */
		private IModel<String> findLabelModel(final ComponentTag tag)
		{
			if (labeled instanceof ILabelProvider)
			{
				// check if the labeled component is a label provider
				ILabelProvider<String> provider = (ILabelProvider<String>)labeled;
				if (provider.getLabel() != null)
				{
					if (!Strings.isEmpty(provider.getLabel().getObject()))

					{
						return provider.getLabel();
					}
				}
			}

			// check if the labeled component is a form component
			if (labeled instanceof FormComponent)
			{
				final FormComponent<?> formComponent = (FormComponent<?>)labeled;
				String text = formComponent.getDefaultLabel("wicket:unknown");
				if (!"wicket:unknown".equals(text) && !Strings.isEmpty(text))
				{
					return new LoadableDetachableModel<String>()
					{
						@Override
						protected String load()
						{
							return formComponent.getDefaultLabel("wicket:unknown");
						}
					};
				}
			}

			// check if wicket:label tag has a message key
			{
				String resourceKey = tag.getAttribute("key");
				if (resourceKey != null)
				{
					String text = labeled.getString(resourceKey);
					if (!Strings.isEmpty(text))
					{
						return new StringResourceModel(resourceKey, labeled);
					}
				}
			}

			return null;
		}
	}

	@Override
	public Component resolve(MarkupContainer container, MarkupStream markupStream, ComponentTag tag)
	{
		if (tag instanceof WicketTag && "label".equals(tag.getName()))
		{
			// We need to find a FormComponent...
			Component related = null;
			// ...which could be explicitly specified...
			String forAttributeValue = tag.getAttribute("for");
			if (forAttributeValue != null)
			{
				Component component = AutoLabelResolver.findRelatedComponent(container, forAttributeValue);
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
				String forAttr = forAttributeValue != null ? " for=\"" + forAttributeValue + "\"" : "";
				throw new ComponentNotFoundException("no related component found for <wicket:label"+forAttr+">");
			}
			else
			{
				// ...found the form component, so we can return our label.
				TextLabel label = new TextLabel(tag.getId(), related);
				String escape = tag.getAttribute(ESCAPE_ATTRIBUTE);
				label.setEscapeModelStrings(Strings.isEmpty(escape) || Strings.isTrue(escape));
				return label;
			}
		}
		return null;
	}

}
