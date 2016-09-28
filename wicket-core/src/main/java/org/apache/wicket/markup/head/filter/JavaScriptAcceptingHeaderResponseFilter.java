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
package org.apache.wicket.markup.head.filter;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnEventHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;

/**
 * This filter accepts all {@link JavaScriptHeaderItem}s.
 * 
 * Note: this filter used to accept everything that is not css, it no longer does. For example,
 * meta-tags are neither CSS nor JS.
 * 
 * @author Jeremy Thomerson
 * @author Emond Papegaaij
 */
public class JavaScriptAcceptingHeaderResponseFilter extends AbstractHeaderResponseFilter
{

	/**
	 * Construct.
	 * 
	 * @param name
	 *            name of the filter (used by the container that renders these resources)
	 */
	public JavaScriptAcceptingHeaderResponseFilter(String name)
	{
		super(name);
	}

	@Override
	protected boolean acceptsWrapped(HeaderItem item)
	{
		return (item instanceof JavaScriptHeaderItem ||
				item instanceof OnDomReadyHeaderItem ||
				item instanceof OnLoadHeaderItem     ||
				item instanceof OnEventHeaderItem
		);
	}
}
