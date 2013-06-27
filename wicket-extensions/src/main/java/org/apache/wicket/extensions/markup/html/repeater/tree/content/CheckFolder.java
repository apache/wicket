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
package org.apache.wicket.extensions.markup.html.repeater.tree.content;

import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.model.IModel;

/**
 * This class adds a {@link Check} to a {@link Folder}. Remeber to wrap your tree in a
 * {@link CheckGroup} for all checks to work correctly.
 * 
 * @author svenmeier
 */
public class CheckFolder<T> extends Folder<T>
{
	private static final long serialVersionUID = 1L;

	public CheckFolder(String id, AbstractTree<T> tree, IModel<T> model)
	{
		super(id, tree, model);

		add(new Check<>("check", model));
	}
}