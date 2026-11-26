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
package org.apache.wicket;

/**
 * Generates markup ids for components
 */
@FunctionalInterface
public interface IMarkupIdGenerator
{
	/**
	 * Generates markup id for the given component
	 * <p>
	 * Wicket writes the markup id into the JavaScript it generates, without escaping it. A
	 * generated markup id may therefore only contain letters, digits, {@code '_'}, {@code '-'} and
	 * {@code '.'}. An id the component already has, from {@link Component#setMarkupId(String)} or
	 * from the id attribute in its markup, has been checked already. The component id may also
	 * contain {@code '('}, {@code ')'}, {@code '['} and {@code ']'}, so a generator that builds the
	 * markup id from it must replace these.
	 *
	 * @param component
	 *            The component for which to generate a markup id
	 * @param createIfDoesNotExist
	 *            When there is no existing markup id, determines whether it should be generated or
	 *            whether <code>null</code> should be returned.
	 * @return The generated markup id
	 */
	String generateMarkupId(Component component, boolean createIfDoesNotExist);
}
