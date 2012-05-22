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
package org.apache.wicket.markup.html.border;

import org.apache.wicket.markup.html.panel.IMarkupSourcingStrategy;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.panel.PanelMarkupSourcingStrategy;
import org.apache.wicket.model.IModel;

/**
 * Whereas a Panel replaces the body markup with the associated markup file, a BorderPanel assumes a
 * that Body component renders the body markup including any number of Wicket Components.
 * <p>
 * Example:
 * 
 * <pre>
 * <u>MyPage.html</u>
 * ...
 * &lt;div wicket:id="myPanel"&gt;
 *   ...
 *   &lt;div wicket:id="componentInBody"/&gt;
 *   ...
 * &lt;/div&gt;
 * 
 * <u>MyPage.java</u>
 * ...
 * public MyPage extends WebPage {
 *   ...
 *   public MyPage() { 
 *     ...
 *     MyPanel border = new MyPanel("myPanel");
 *     add(border);
 *     border.getBodyContainer().add(new MyComponent("componentInBody"));
 *     ...
 *   }
 *   ...
 * }
 * 
 * <u>MyPanel.java</u>
 * ...
 * public MyPanel extends BorderPanel {
 *   ...
 *   public MyPanel(final String id) {
 *     super(id);
 *     ...
 *     add(newBodyContainer("body"));
 *     ...
 *   }
 * }
 * </pre>
 * 
 * @see BorderBehavior A behavior which adds (raw) markup before and after the component
 * 
 * @author Juergen Donnerstag
 */
public abstract class BorderPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	private Body body;

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public BorderPanel(final String id)
	{
		this(id, null);
	}

	/**
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public BorderPanel(final String id, final IModel<?> model)
	{
		super(id, model);
	}

	@Override
	protected IMarkupSourcingStrategy newMarkupSourcingStrategy()
	{
		return new PanelMarkupSourcingStrategy(true);
	}

	/**
	 * Sets the body container
	 * 
	 * @param body
	 * @return The body component
	 */
	public final Body setBodyContainer(final Body body)
	{
		this.body = body;
		return body;
	}

	/**
	 * Provide easy access to the Body component.
	 * 
	 * @return The body container
	 */
	public final Body getBodyContainer()
	{
		return body;
	}

	/**
	 * Create a new body container identified by id in the panel's markup
	 * 
	 * @param id
	 * @return Body component
	 */
	public final Body newBodyContainer(final String id)
	{
		body = new Body(id, this);
		return body;
	}
}
