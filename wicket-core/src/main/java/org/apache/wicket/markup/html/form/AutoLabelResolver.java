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
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.RawMarkup;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.parser.XmlPullParser;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolver that implements the {@code wicket:for} attribute functionality. The attribute makes it
 * easy to set up {@code <label>} tags for form components by providing the following features
 * without having to add any additional components in code:
 * <ul>
 * <li>Outputs the {@code for} attribute with the value equivalent to the markup id of the
 * referenced form component</li>
 * <li>Appends {@code required} css class to the {@code <label>} tag if the referenced form
 * component is required</li>
 * <li>Appends {@code error} css class to the {@code <label>} tag if the referenced form component
 * has failed validation</li>
 * <li>If the {@code <label>} tag contains {@code <span class='text'></span>} markup and the form
 * component has a label configured either via the label model or a property files, the body of the
 * {code <span>} will be replaced with the label</li>
 * <li>If the {@code <label>} tag contains {@code <span class='text'>body</span>} markup and the
 * form component does not have a label configured either via the label model or a properties file,
 * the label of the form component will be set to the body of the {@code <span>} tag - in this
 * example {@code body}</li>
 * </ul>
 * 
 * <p>
 * The value of the {@code wicket:for} attribute can either contain an id of the form component or a
 * path a path to it using the standard {@code :} path separator. Note that {@code ..} can be used
 * as part of the path to construct a reference to the parent container, eg {@code ..:..:foo:bar}.
 * First the value of the attribute will be treated as a path and the {@code <label>} tag's closest
 * parent container will be queried for the form component. If the form component cannot be resolved
 * the value of the {@code wicket:for} attribute will be treated as an id and all containers will be
 * searched from the closest parent to the page.
 * </p>
 * 
 * <p>
 * Given markup like this:
 * 
 * <code>
 * [label wicket:for="name"][span class="text"]Name[/span]:[/label][input wicket:id="name" type="text"/]
 * </code>
 * 
 * If the {@code name} component has its label set to 'First Name' the resulting output will be:
 * <code>
 * [label for="name5"][span class="text"]First Name[/span]:[/label][input name="name" type="text" id="name5"/]
 * </code>
 * 
 * However, if the {@code name} component does not have a label set then it will be set to
 * {@code Name} based on the markup.
 * </p>
 * 
 * @author igor
 */
public class AutoLabelResolver implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(AutoLabelResolver.class);

	static final String WICKET_FOR = "wicket:for";

	public Component resolve(final MarkupContainer container, final MarkupStream markupStream,
		final ComponentTag tag)
	{
		if (!AutoLabelTagHandler.class.getName().equals(tag.getId()))
		{
			return null;
		}

		final String id = tag.getAttribute(WICKET_FOR).trim();

		FormComponent<?> component = findRelatedComponent(container, id);
		if (component == null)
		{
			throw new WicketRuntimeException("Could not find form component with id: " + id +
				" while trying to resolve wicket:for attribute");
		}

		if (!(component instanceof FormComponent<?>))
		{
			throw new WicketRuntimeException("Component pointed to by wicket:for attribute: " + id +
				" is not a form component");
		}

		if (!component.getOutputMarkupId())
		{
			component.setOutputMarkupId(true);
			if (!component.hasBeenRendered())
			{
				logger.warn(
					"Form component: {} is reference via a wicket:for attribute but does not have its outputMarkupId property set to true",
					component.toString(false));
			}
		}

		FormComponent<?> fc = component;
		return new AutoLabel("label" + container.getPage().getAutoIndex(), fc);
	}

	/**
	 * 
	 * @param container
	 * @param id
	 * @return FormComponent
	 */
	protected FormComponent<?> findRelatedComponent(MarkupContainer container, final String id)
	{
		// try the quick and easy route first

		Component component = container.get(id);
		if (component != null && (component instanceof FormComponent<?>))
		{
			return (FormComponent<?>)component;
		}

		// try the long way, search the hierarchy from the closest container up to the page

		final Component[] searched = new Component[] { null };
		while (container != null)
		{
			component = container.visitChildren(Component.class,
				new IVisitor<Component, Component>()
				{
					public void component(Component child, IVisit<Component> visit)
					{
						if (child == searched[0])
						{
							// this container was already searched
							visit.dontGoDeeper();
							return;
						}
						if (id.equals(child.getId()) && (child instanceof FormComponent))
						{
							visit.stop(child);
							return;
						}
					}
				});

			if (component != null && (component instanceof FormComponent))
			{
				return (FormComponent<?>)component;
			}

			// remember the container so we dont search it again, and search the parent
			searched[0] = container;
			container = container.getParent();
		}

		return null;
	}

	/**
	 * Component that is attached to the {@code <label>} tag and takes care of writing out the label
	 * text as well as setting classes on the {@code <label>} tag
	 * 
	 * @author igor
	 */
	protected static class AutoLabel extends WebMarkupContainer
	{
		private static final long serialVersionUID = 1L;

		private static final String WICKET_UNKNOWN = "wicket:unknown";
		private static final String CLASS = "class";

		private final FormComponent<?> fc;

		public AutoLabel(final String id, final FormComponent<?> fc)
		{
			super(id);
			this.fc = fc;
		}

		@Override
		protected void onComponentTag(final ComponentTag tag)
		{
			super.onComponentTag(tag);

			tag.put("for", fc.getMarkupId());
			if (fc.isRequired())
			{
				tag.append(CLASS, "required", " ");
			}

			if (!fc.isValid())
			{
				tag.append(CLASS, "error", " ");
			}
		}

		@Override
		public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
		{
			if (!(markupStream.get() instanceof RawMarkup))
			{
				// no raw markup found inside the label, do not modify the contents
				return;
			}

			// read all raw markup in the body and find the range of the label text inside it. the
			// range is specified as the body of the <span class='text'></span> tag.

			AppendingStringBuffer markup = readBodyMarkup(markupStream);
			int[] range = findLabelTextRange(markup);
			final int start = range[0];
			final int end = range[1];

			if (start < 0)
			{
				// if we could not find the range of the label text in the markup we have nothing
				// further to do

				super.onComponentTagBody(markupStream, openTag);
				return;
			}

			// based on whether or not the form component has a label set read or write it into the
			// markup

			String label = getFormComponentLabelText(fc);
			if (label != null)
			{
				// if label is set write it into the markup

				markup = markup.replace(start, end, label);
				replaceComponentTagBody(markupStream, openTag, markup);
			}
			else
			{
				// if label is not set, read it from the markup into the form component

				String markupLabel = markup.substring(start, end);
				fc.setLabel(Model.of(markupLabel));
				super.onComponentTagBody(markupStream, openTag);
			}
		}

		/**
		 * Finds start and end index of text in the label. This range is represented by the body of
		 * the {@code <span class='text'></span>} tag
		 * 
		 * @param markup
		 * @return Start and end index of text in the label
		 */
		protected int[] findLabelTextRange(final AppendingStringBuffer markup)
		{
			int[] range = new int[] { -1, -1 };

			XmlPullParser parser = new XmlPullParser();
			XmlTag opening = null; // opening label text span tag
			XmlTag closing = null; // close label text span tag

			try
			{
				parser.parse(markup);

				XmlTag tag = null; // current tag

				int depth = 0; // depth of span tags
				int openDepth = -1; // depth of the label text open span tag

				while (((tag = parser.nextTag()) != null))
				{
					if (!"span".equalsIgnoreCase(tag.getName()) || tag.getNamespace() != null)
					{
						// skip non-span tags
						continue;
					}

					if (opening != null && tag.isClose() && depth == openDepth)
					{
						// found the closing tag we need, we are done
						closing = tag;
						break;
					}

					depth += tag.isOpen() ? 1 : -1;

					if (opening == null && isTextSpan(tag))
					{
						// found the opening tag, keep looking for the closing one
						opening = tag;
						openDepth = depth;
						continue;
					}
				}
			}
			catch (Exception e)
			{
				throw new WicketRuntimeException(
					"Could not parse markup while processing an auto label for component: " +
						fc.toString(false), e);
			}

			if (opening != null)
			{
				// calculate the range of the tag's body, this is where the label text is/will be
				range[0] = opening.getPos() + opening.getLength();
				range[1] = closing.getPos();
			}

			return range;
		}

		/**
		 * 
		 * @param markupStream
		 * @return buffer
		 */
		protected AppendingStringBuffer readBodyMarkup(final MarkupStream markupStream)
		{
			int streamIndex = markupStream.getCurrentIndex();

			AppendingStringBuffer markup = new AppendingStringBuffer();
			do
			{
				markup.append(((RawMarkup)markupStream.get()).toString());
				markupStream.next();
			}
			while ((markupStream.get() instanceof RawMarkup));

			markupStream.setCurrentIndex(streamIndex);

			return markup;
		}

		/**
		 * 
		 * @param fc
		 * @return ??
		 */
		protected String getFormComponentLabelText(final FormComponent<?> fc)
		{
			String label = fc.getLabel() != null ? fc.getLabel().getObject() : null;
			if (label == null)
			{
				label = fc.getDefaultLabel(WICKET_UNKNOWN);
				if (WICKET_UNKNOWN.equals(label))
				{
					label = null;
				}
			}
			return label;
		}

		/**
		 * 
		 * @param tag
		 * @return true, if ???
		 */
		protected final boolean isTextSpan(final XmlTag tag)
		{
			if (!tag.isOpen())
			{
				return false;
			}

			if (!"span".equalsIgnoreCase(tag.getName()) || tag.getNamespace() != null)
			{
				return false;
			}

			String classNames = tag.getAttributes().getString(CLASS);
			if (Strings.isEmpty(classNames))
			{
				return false;
			}

			boolean textClassFound = false;
			for (String className : classNames.split(" "))
			{
				if ("text".equals(className))
				{
					textClassFound = true;
					break;
				}
			}

			if (!textClassFound)
			{
				return false;
			}

			return true;
		}
	}
}
