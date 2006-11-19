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
package wicket.markup.resolver;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.Page;
import wicket.markup.ComponentTag;
import wicket.markup.IScopedComponent;
import wicket.markup.MarkupStream;
import wicket.markup.html.panel.Panel;

/**
 * Implement a component resolver which walks up the component tree until a Page
 * or Panel and tries to find a component with a matching wicket id, effectivly
 * providing something like scoping for wicket id resolution.
 * <p>
 * Note: This resolver is not activated by default. It has to be added by means of
 * <code>Application.getComponentResolvers().add(new InheritComponentResolver())
 * to be activated.</code>.
 * <p>
 * Example:
 * <pre>
 * MyPage()
 * { 
 *    add(new Label("hidden-by-cont1","hidden")); 
 *    add(new Label("global","can be everywhere")); //the intresting case
 * 
 *    WebMarkupContainer cont1 = new WebMarkupContainer("cont1"); 
 *    add(cont1);
 * 
 *     cont1.add(new Label("hidden-by-cont1","cont1 hides")); 
 *     cont1.add(new Label("same-id","cont1 same-id"));
 * 
 *     WebMarkupContainer cont2 = new WebMarkupContainer("cont2"); 
 *     add(cont2);
 * 
 *     cont2.add(new Label("same-id","cont2 same-id")); 
 * }
 * </pre>
 * <pre>
 * HTML:
 * <html> 
 * <body> 
 *   <span wicket:id="hidden-by-cont1">Prints: hidden</span> 
 *   <div wicket:id="cont1"> 
 *     <span wicket:id="hidden-by-cont1">Prints: cont1 hides</span>
 *     <span wicket:id="same-id">Prints: cont1 same-id</span> 
 *   </div>
 * 
 *   <div wicket:id="cont2"> 
 *     <span wicket:id="global">Prints: can be everywhere</span>
 *     <span wicket:id="same-id">Prints: cont2 same-id</span> 
 *   </div>
 * </pre>
 * 
 * So you can use the same ids in the same page. If the containing containers
 * are not in the same hierarchy-line nothing changes. A comp with the same id
 * hides the one of the parent-container with the same id.
 * 
 * @see wicket.MarkupContainer#isTransparentResolver()
 * @see wicket.markup.resolver.ParentResolver
 * 
 * @author Christian Essl
 * @author Juergen Donnerstag
 */
public class ScopedComponentResolver implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public ScopedComponentResolver()
	{
		super();
	}

	/**
	 * 
	 * @see wicket.markup.resolver.IComponentResolver#resolve(wicket.MarkupContainer,
	 *      wicket.markup.MarkupStream, wicket.markup.ComponentTag)
	 */
	public boolean resolve(final MarkupContainer container, final MarkupStream markupStream,
			final ComponentTag tag)
	{
		// Try to find the component with the parent component.
		final String id = tag.getId();
		MarkupContainer parent = container;

		while (!(parent instanceof Page) && !(parent instanceof Panel) && (parent != null))
		{
			parent = parent.getParent();
			if (parent == null)
			{
				return false;
			}

			final Component component = parent.get(id);
			if ((component != null) && (component instanceof IScopedComponent))
			{
				IScopedComponent sc = (IScopedComponent)component;
				if (sc.isRenderableInSubContainers())
				{
					component.render(markupStream);
					return true;
				}
			}
		}

		return false;
	}
}
