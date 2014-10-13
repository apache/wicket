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
package org.apache.wicket.markup.resolver;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.settings.PageSettings;
import org.apache.wicket.util.io.IClusterable;

/**
 * IComponentResolvers are responsible for mapping component names to Wicket components. Resolvers
 * are first looked up in a component's hierarchy before falling back to a list of
 * IComponentResolvers maintained in {@link PageSettings}.
 * 
 * NOTE: implementations for this interface must be thread-safe!
 * 
 * @see ComponentResolvers
 * 
 * @author Juergen Donnerstag
 */
public interface IComponentResolver extends IClusterable
{
	/**
	 * Try to resolve a component.
	 * 
	 * @param container
	 *            The container parsing its markup
	 * @param markupStream
	 *            The current markupStream
	 * @param tag
	 *            The current component tag while parsing the markup
	 * @return component or {@code null} if not found
	 */
	public Component resolve(final MarkupContainer container, final MarkupStream markupStream,
		final ComponentTag tag);
}
