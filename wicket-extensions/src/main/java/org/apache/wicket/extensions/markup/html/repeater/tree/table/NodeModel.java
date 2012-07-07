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

import java.util.Arrays;

import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.model.IModel;

/**
 * A model wrapping the actual node model, carrying additional information about the parental
 * branches.
 * 
 * @see #getBranches()
 * @see ITreeProvider#model(Object)
 * 
 * @author svenmeier
 */
public class NodeModel<T> implements IModel<T>
{

	private static final long serialVersionUID = 1L;

	private IModel<T> model;

	private boolean[] branches;

	public NodeModel(IModel<T> model, boolean[] branches)
	{
		this.model = model;
		this.branches = branches;
	}

	public IModel<T> getWrappedModel()
	{
		return model;
	}

	@Override
	public T getObject()
	{
		return model.getObject();
	}

	@Override
	public void setObject(T object)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void detach()
	{
		model.detach();
	}

	public int getDepth()
	{
		return branches.length;
	}

	public boolean[] getBranches()
	{
		return branches;
	}

	@Override
	public int hashCode()
	{
		return model.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof NodeModel<?>)
		{
			NodeModel<?> nodeModel = (NodeModel<?>)obj;

			return Arrays.equals(this.branches, nodeModel.branches) &&
				this.model.equals((nodeModel).model);
		}
		return false;
	}
}