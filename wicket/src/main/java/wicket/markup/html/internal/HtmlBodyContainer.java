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

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.html.WebMarkupContainer;

/**
 * This is basically a transparent WebMarkupContainer with the ability to get
 * the markup stream positioned correctly where the component begins from the
 * parent (page) container.
 * <p>
 * It is transparent in the sense that all requests to access a child component
 * are forwarded to the parent (page) container.
 * <p>
 * Because this container is usually automatically created, it can accessed by
 * WebPage.getBodyContainer().
 * <p>
 * Though it is automatically created it may be replaced by adding you own
 * Component with id == BodyOnLoadHandler.BODY_ID to the Page.
 * <p>
 * Components and Behaviors which wish to modify e.g. the body onLoad attribute
 * may attach an AttributeModifier by means of <code>addOnLoadModifier()</code>
 * or <code>add(new AttributeModifier())</code>.
 * 
 * @author Juergen Donnerstag
 */
public class HtmlBodyContainer extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct
	 * 
	 * @see Component#Component(MarkupContainer,String)
	 */
	public HtmlBodyContainer(MarkupContainer parent, final String id)
	{
		super(parent, id);
	}

	/**
	 * @see wicket.MarkupContainer#isTransparentResolver()
	 */
	@Override
	public boolean isTransparentResolver()
	{
		return true;
	}
}
