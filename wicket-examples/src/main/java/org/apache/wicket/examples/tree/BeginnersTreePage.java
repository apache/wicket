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
package org.apache.wicket.examples.tree;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.tree.DefaultNestedTree;
import org.apache.wicket.model.IModel;

/**
 * A very simple page containing a {@link DefaultNestedTree} only.
 * 
 * @author Sven Meier
 */
public class BeginnersTreePage extends AbstractTreePage
{

	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public BeginnersTreePage()
	{
		add(new DefaultNestedTree<Foo>("tree", new FooProvider())
		{

			/**
			 * To use a custom component for the representation of a node's content we would
			 * override this method.
			 */
			@Override
			protected Component newContentComponent(String id, IModel<Foo> node)
			{
				return super.newContentComponent(id, node);
			}
		});
	}
}
