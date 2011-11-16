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
package org.apache.wicket.markup.loader;

import java.io.IOException;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;

/**
 * This is Wickets default markup loader. It uses the {@link InheritedMarkupMarkupLoader} and
 * {@link SimpleMarkupLoader} to load the markup associated with a {@link MarkupContainer}.
 * 
 * @author Juergen Donnerstag
 */
public class DefaultMarkupLoader implements IMarkupLoader
{
	/**
	 * Constructor.
	 */
	public DefaultMarkupLoader()
	{
	}

	/**
	 * Uses {@link SimpleMarkupLoader} to load the resource(s), read it and check if markup
	 * inheritance applies. If yes, load the required other markup and merge them using
	 * {@link InheritedMarkupMarkupLoader}.
	 */
	@Override
	public final Markup loadMarkup(final MarkupContainer container,
		final MarkupResourceStream markupResourceStream, final IMarkupLoader baseLoader,
		final boolean enforceReload) throws IOException, ResourceStreamNotFoundException
	{
		IMarkupLoader loader = new InheritedMarkupMarkupLoader();
		return loader.loadMarkup(container, markupResourceStream, new SimpleMarkupLoader(),
			enforceReload);
	}
}
