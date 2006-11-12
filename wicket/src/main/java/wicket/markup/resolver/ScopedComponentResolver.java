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
 * Note: This resolver is not activated by default. It has to be added by means
 * of
 * <code>Application.getComponentResolvers().add(new InheritComponentResolver())
 * to be activated.</code>.
 * <p>
 * Example:
 * 
 * <pre>
 * MyPage()
 * {
 * 	add(new Label(&quot;hidden-by-cont1&quot;, &quot;hidden&quot;));
 * 	add(new Label(&quot;global&quot;, &quot;can be everywhere&quot;)); //the intresting case
 * 
 * 	WebMarkupContainer cont1 = new WebMarkupContainer(&quot;cont1&quot;);
 * 	add(cont1);
 * 
 * 	cont1.add(new Label(&quot;hidden-by-cont1&quot;, &quot;cont1 hides&quot;));
 * 	cont1.add(new Label(&quot;same-id&quot;, &quot;cont1 same-id&quot;));
 * 
 * 	WebMarkupContainer cont2 = new WebMarkupContainer(&quot;cont2&quot;);
 * 	add(cont2);
 * 
 * 	cont2.add(new Label(&quot;same-id&quot;, &quot;cont2 same-id&quot;));
 * }
 * </pre>
 * <pre>
 *    HTML:
 *    &lt;html&gt; 
 *    &lt;body&gt; 
 *      &lt;span wicket:id=&quot;hidden-by-cont1&quot;&gt;Prints: hidden&lt;/span&gt; 
 *      &lt;div wicket:id=&quot;cont1&quot;&gt; 
 *        &lt;span wicket:id=&quot;hidden-by-cont1&quot;&gt;Prints: cont1 hides&lt;/span&gt;
 *        &lt;span wicket:id=&quot;same-id&quot;&gt;Prints: cont1 same-id&lt;/span&gt; 
 *      &lt;/div&gt;
 *    
 *      &lt;div wicket:id=&quot;cont2&quot;&gt; 
 *        &lt;span wicket:id=&quot;global&quot;&gt;Prints: can be everywhere&lt;/span&gt;
 *        &lt;span wicket:id=&quot;same-id&quot;&gt;Prints: cont2 same-id&lt;/span&gt; 
 *      &lt;/div&gt;
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
