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
package wicket.markup.html.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupException;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.parser.filter.EnclosureHandler;
import wicket.markup.resolver.EnclosureResolver;

/**
 * An Enclosure are automatically created by Wicket. Do not create it yourself.
 * An Enclosure container is created if &lt;wicket:enclosure&gt; is found in the
 * markup. It is meant to solve the following situation. Instead of
 * 
 * <pre>
 *    &lt;table wicket:id=&quot;label-container&quot; class=&quot;notify&quot;&gt;&lt;tr&gt;&lt;td&gt;&lt;span wicket:id=&quot;label&quot;&gt;[[notification]]&lt;/span&gt;&lt;/td&gt;&lt;/tr&gt;&lt;/table&gt; 
 *  
 *    WebMarkupContainer container=new WebMarkupContainer(&quot;label-container&quot;) 
 *    {
 *       public boolean isVisible() 
 *       {
 *           return hasNotification();
 *       }
 *    };
 *    add(container);
 *     container.add(new Label(&quot;label&quot;, notificationModel)); 
 * </pre>
 * 
 * with Enclosure you are able to do the following:
 * 
 * <pre>
 *    &lt;wicket:enclosure&gt; 
 *      &lt;table class=&quot;notify&quot;&gt;&lt;tr&gt;&lt;td&gt;&lt;span wicket:id=&quot;label&quot;&gt;[[notification]]&lt;/span&gt;&lt;/td&gt;&lt;/tr&gt;&lt;/table&gt;
 *    &lt;/wicket:enclosure&gt;
 * 
 *    add(new Label(&quot;label&quot;, notificationModel)) 
 *    {
 *       public boolean isVisible() 
 *       {
 *           return hasNotification();
 *       }
 *    }
 * </pre>
 * 
 * @see EnclosureResolver
 * @see EnclosureHandler
 * 
 * @author Juergen Donnerstag
 */
public class Enclosure extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(Enclosure.class);

	/** The child component to delegate the isVisible() call to */
	private final Component childComponent;

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 * @param childId
	 */
	public Enclosure(final MarkupContainer parent, final String id, final CharSequence childId)
	{
		super(parent, id);

		this.childComponent = getChildComponent(childId);
	}

	/**
	 * 
	 * @see wicket.MarkupContainer#isTransparentResolver()
	 */
	@Override
	public boolean isTransparentResolver()
	{
		return true;
	}

	/**
	 * 
	 * @param childId
	 * @return Child Component
	 */
	private Component getChildComponent(final CharSequence childId)
	{
		MarkupContainer parent = getParent();
		while ((parent != null) && parent.isTransparentResolver())
		{
			parent = parent.getParent();
		}

		if (parent == null)
		{
			throw new WicketRuntimeException(
					"Unable to find parent component which is not a transparent resolver");
		}

		if (childId == null)
		{
			throw new MarkupException(
					"You most likely forgot to register the EnclosureHandler with the MarkupParserFactory");
		}

		final Component child = parent.get(childId.toString());
		if (child == null)
		{
			throw new MarkupException("Didn't find child component of <wicket:enclosure> with id='"
					+ childId + "'. Component: " + this.toString());
		}

		return child;
	}

	/**
	 * 
	 * @see wicket.MarkupContainer#onComponentTagBody(wicket.markup.MarkupStream,
	 *      wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
		if (this.childComponent == this)
		{
			throw new WicketRuntimeException(
					"Programming error: childComponent == enclose component; endless loop");
		}
		else if (this.childComponent != null)
		{
			// Delegate to child component
			setVisible(this.childComponent.isVisible());
		}

		if (isVisible() == true)
		{
			super.onComponentTagBody(markupStream, openTag);
		}
		else
		{
			markupStream.skipUntil(openTag.getName());
		}
	}
}
