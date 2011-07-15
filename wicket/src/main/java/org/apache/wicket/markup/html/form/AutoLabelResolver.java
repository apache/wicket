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
import org.apache.wicket.Component.IVisitor;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.resolver.IComponentResolver;
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
 * <li>Appends {@code disabled} css class to the {@code <label>} tag if the referenced form
 * component has is not enabled in hierarchy</li>
 * <p>
 * The value of the {@code wicket:for} atribute can either contain an id of the form component or a
 * path to it using the standard {@code :} path separator. Note that {@code ..} can be used as part
 * of the path to construct a reference to the parent container, eg {@code ..:..:foo:bar}. First the
 * value of the attribute will be treated as a path and the {@code <label>} tag's closest parent
 * container will be queried for the form component. If the form component cannot be resolved the
 * value of the {@code wicket:for} attribute will be treated as an id and all containers will be
 * searched from the closest parent to the page.
 * </p>
 * 
<<<<<<< .mine
=======
 * <p>
 * Given markup like this:
 * 
 * <code>
 * [label wicket:for="name"][span class="label-text"]Name[/span]:[/label][input wicket:id="name" type="text"/]
 * </code>
 * 
 * If the {@code name} component has its label set to 'First Name' the resulting output will be:
 * <code>
 * [label for="name5"][span class="label-text"]First Name[/span]:[/label][input name="name" type="text" id="name5"/]
 * </code>
 * 
 * However, if the {@code name} component does not have a label set then it will be set to
 * {@code Name} based on the markup.
 * </p>
 * 
>>>>>>> .r1146946
 * @author igor
 * @author Carl-Eric Menzel <cmenzel@wicketbuch.de>
 */
public class AutoLabelResolver implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(AutoLabelResolver.class);

	public boolean resolve(MarkupContainer container, MarkupStream markupStream, ComponentTag tag)
	{
		if (!AutoLabelTagHandler.class.getName().equals(tag.getId()))
		{
			return false;
		}

		final String id = tag.getAttribute("wicket:for").trim();

		Component component = findRelatedComponent(container, id);

		if (component == null)
		{
			throw new WicketRuntimeException("Could not find form component with id: " + id +
				" while trying to resolve wicket:for attribute");
		}
		if (!(component instanceof ILabelProvider))
		{
			throw new WicketRuntimeException("Component pointed to by wicket:for attribute: " + id +
				" does not implement " + ILabelProvider.class.getName());
		}

		if (!component.getOutputMarkupId())
		{
			component.setOutputMarkupId(true);
			if (!component.hasBeenRendered())
			{
				logger.warn(
					"Component: {} is reference via a wicket:for attribute but does not have its outputMarkupId property set to true",
					component.toString(false));
			}
		}

		container.autoAdd(new AutoLabel("label" + container.getPage().getAutoIndex2(), component),
			markupStream);

		return true;
	}

	static Component findRelatedComponent(MarkupContainer container, final String id)
	{
		// try the quick and easy route first

		Component component = container.get(id);
		if (component != null && (component instanceof ILabelProvider))
		{
			return component;
		}

		// try the long way, search the hierarchy from the closest container up to the page

		final Component[] searched = new Component[] { null };
		while (container != null)
		{
			component = (Component)container.visitChildren(Component.class,
				new IVisitor<Component>()
				{
					public Object component(Component child)
					{
						if (child == searched[0])
						{
							// this container was already searched
							return CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
						}
						if (id.equals(child.getId()))
						{
							return child;
						}
						return CONTINUE_TRAVERSAL;
					}
				});

			if (component != null)
			{
				return component;
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

		private final Component component;

		public AutoLabel(String id, Component fc)
		{
			super(id);
			component = fc;
		}

		@Override
		protected void onComponentTag(ComponentTag tag)
		{
			super.onComponentTag(tag);
			tag.put("for", component.getMarkupId());

			if (component instanceof FormComponent)
			{
				FormComponent<?> fc = (FormComponent<?>)component;
				if (fc.isRequired())
				{
					tag.append("class", "required", " ");
				}
				if (!fc.isValid())
				{
					tag.append("class", "error", " ");
				}
				if (!fc.isEnabledInHierarchy())
				{
					tag.append("class", "disabled", " ");
				}
			}
		}

		@Override
		public boolean isTransparentResolver()
		{
			return true;
		}

		/**
		 * @return the component this label points to, if any.
		 */
		public Component getRelatedComponent()
		{
			return component;
		}
	}
}
