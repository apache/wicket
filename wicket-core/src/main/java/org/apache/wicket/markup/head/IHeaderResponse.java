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

import java.io.Closeable;

import org.apache.wicket.request.Response;

/**
 * Interface that is used to render header elements (usually javascript and CSS references).
 * 
 * Implementation of this interface is responsible for filtering duplicate contributions (so that
 * for example the same javascript is not loaded twice) during the same request.
 * 
 * @author Matej Knopp
 */
public interface IHeaderResponse extends Closeable
{
	/**
	 * Renders the given {@link HeaderItem} to the response if none of its
	 * {@linkplain HeaderItem#getRenderTokens() tokens} has been rendered before.
	 * <p>
	 * Automatically marks all item's tokens as rendered.
	 * 
	 * @param item
	 *            The item to render.
	 * @see #markRendered(Object)
	 */
	void render(HeaderItem item);

	/**
	 * Marks the given object as rendered. The object can be anything (string, resource reference,
	 * etc...). The purpose of this function is to allow user to manually keep track of rendered
	 * items. This can be useful for items that are expensive to generate (like interpolated text).
	 * 
	 * @param object
	 *            object to be marked as rendered.
	 */
	void markRendered(Object object);

	/**
	 * Returns whether the given object has been marked as rendered.
	 * 
	 * @param object
	 *            Object that is queried to be rendered
	 * @return Whether the object has been marked as rendered during the request
	 * 
	 * @see #markRendered(Object)
	 */
	boolean wasRendered(Object object);

	/**
	 * Returns the response that can be used to write arbitrary text to the head section.
	 * <p>
	 * Note: This method is kind of dangerous as users are able to write to the output whatever they
	 * like.
	 * 
	 * @return Response
	 */
	Response getResponse();

	/**
	 * Mark Header rendering is completed and subsequent usage will be ignored. If some kind of
	 * buffering is used internally, this action will mark that the contents has to be flushed out.
	 */
	@Override
	void close();

	/**
	 * @return if header rendering is completed and subsequent usage will be ignored
	 */
	boolean isClosed();
}
