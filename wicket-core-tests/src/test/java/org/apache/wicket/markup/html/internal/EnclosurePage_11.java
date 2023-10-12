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
package org.apache.wicket.markup.html.internal;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.resolver.IComponentResolver;


/**
 * Mock page for testing (see WICKET-2882).
 * 
 * @author Jeremy Thomerson
 */
public class EnclosurePage_11 extends WebPage implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public EnclosurePage_11()
	{
	}

	@Override
	public Component resolve(MarkupContainer container, MarkupStream markupStream, ComponentTag tag)
	{
		if ((tag instanceof WicketTag) || !"autoCreatedPanel".equals(tag.getId()))
		{
			// this resolver does not handle wicket tags
			return null;
		}

		return new SimplePanel(tag.getId());
	}
}
