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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;

/**
 * This is a simple Container component which can be used to build Border like components.
 * <p>
 * Example:
 * 
 * <pre>
 * <u>Panel Markup:</u>
 * ..
 * &lt;div wicket:id="myPanel"&gt;
 *   My Panels body
 * &lt;/div&gt;
 * ..
 * 
 * <u>Panel associated Markup:</u>
 * &lt;wicket:panel&gt;
 *   ..
 *   &lt;div wicket:id="myBody"/&gt;
 *   ..
 * &lt;/wicket:panel&gt;
 * 
 * <u>Panel Java code:</u>
 * class MyPanel extends Panel
 * {
 *   ..
 *   public MyPanel(String id)
 *   {
 *      add(new Body("myBody", this);
 *   }
 *   ..
 * }
 * </pre>
 * 
 * There can be any number of containers between the Panel and Body. You must only remember to
 * provide the correct markup provider to the Body.
 * 
 * @author Juergen Donnerstag
 */
public class Body extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	private final MarkupContainer markupProvider;

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 * @param markupProvider
	 *            Usually a Panel
	 */
	public Body(final String id, final IModel<?> model, final MarkupContainer markupProvider)
	{
		super(id, model);

		Args.notNull(markupProvider, "markupProvider");
		this.markupProvider = markupProvider;
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param markupProvider
	 */
	public Body(String id, final MarkupContainer markupProvider)
	{
		this(id, null, markupProvider);
	}

	@Override
	public IMarkupFragment getMarkup()
	{
		// Panel.getMarkup() returns the "calling" markup. Which is what we want. We do not want the
		// <wicket:panel> markup.
		return markupProvider.getMarkup();
	}
}
