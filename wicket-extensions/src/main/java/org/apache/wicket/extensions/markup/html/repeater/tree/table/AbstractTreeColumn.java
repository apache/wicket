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
package org.apache.wicket.extensions.markup.html.repeater.tree.table;

import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.tree.TableTree;
import org.apache.wicket.model.IModel;

/**
 * @author svenmeier
 */
public abstract class AbstractTreeColumn<T> extends AbstractColumn<T> implements ITreeColumn<T>
{

	private TableTree<T> tree;

	public AbstractTreeColumn(IModel<String> displayModel)
	{
		super(displayModel);
	}

	public AbstractTreeColumn(IModel<String> displayModel, String sortProperty)
	{
		super(displayModel, sortProperty);
	}

	public void setTree(TableTree<T> tree)
	{
		this.tree = tree;
	}

	public TableTree<T> getTree()
	{
		return tree;
	}
}
