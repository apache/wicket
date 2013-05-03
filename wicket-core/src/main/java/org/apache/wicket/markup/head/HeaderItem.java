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
package org.apache.wicket.markup.head;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.request.Response;

/**
 * {@code HeaderItem} represents anything that can be rendered into the header. This can, for
 * example, be a {@linkplain JavaScriptHeaderItem script} or a {@linkplain CssHeaderItem stylesheet}
 * , but also {@linkplain OnEventHeaderItem event triggers} or {@linkplain StringHeaderItem free
 * form text}. {@code HeaderItem}s are used by {@link org.apache.wicket.markup.head.ResourceAggregator} to be able to collect all
 * header sections in a uniform way. All {@code HeaderItem}s are expected to have decent
 * {@code equals}, {@code hashCode} and {@code toString} (for debugging).
 * 
 * @author papegaaij
 */
public abstract class HeaderItem
{
	/**
	 * @return The dependencies this {@code HeaderItem} has. Dependencies will always be rendered
	 *         before the item itself.
	 */
	public List<HeaderItem> getDependencies()
	{
		return new ArrayList<>();
	}

	/**
	 * @return The resources this {@code HeaderItem} provides. As these resources are provided by
	 *         this item, they will no longer be rendered.
	 */
	public Iterable<? extends HeaderItem> getProvidedResources()
	{
		return Collections.emptyList();
	}

	/**
	 * @return The tokens this {@code HeaderItem} can be identified by. If any of the tokens has
	 *         already been rendered, this {@code HeaderItem} will not be rendered.
	 */
	public abstract Iterable<?> getRenderTokens();

	/**
	 * Renders the {@code HeaderItem} to the response.
	 * 
	 * @param response
	 */
	public abstract void render(Response response);
}
