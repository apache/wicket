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
package org.apache.wicket.examples.tree.content;

import org.apache.wicket.Component;
import org.apache.wicket.examples.tree.Foo;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

/**
 * Tree content factory for the {@link org.apache.wicket.examples.tree.TreePage}.
 * 
 * Note: This indirection is used for demonstration purposes only! Don't jump through similar hoops
 * if you're just using one type of content for your application's trees.
 * 
 * @author Sven Meier
 */
public abstract class Content implements IDetachable
{

	/**
	 * Create new content.
	 */
	public abstract Component newContentComponent(String id, AbstractTree<Foo> tree,
		IModel<Foo> model);

	public void detach()
	{
	}
}