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
package wicket.extensions.markup.html.form.select;

import java.util.Collection;

import wicket.MarkupContainer;
import wicket.model.IModel;

/**
 * Component that represents a multiple selection <code>&lt;select&gt;</code> box. Elements are
 * provided by one or more <code>SelectChoice</code> or
 * <code>SelectOptions</code> components in the hierarchy below the
 * <code>Select</code> component.
 * 
 * Advantages to the standard choice components is that the user has a lot more
 * control over the markup between the &lt;select&gt; tag and its children
 * &lt;option&gt; tags: allowing for such things as &lt;optgroup&gt; tags. 
 * 
 * TODO Post 1.2: General: Example
 * 
 * @see SelectOption
 * @see SelectOptions
 * 
 * @author Igor Vaynberg (ivaynberg@users.sf.net)
 * @author Matej Knopp
 *
 * @param <T> Type of selection items' model
 */
public class SelectMultiple<T> extends AbstractSelect<Collection<T>>
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * @see wicket.Component#Component(MarkupContainer,String)
	 */
	public SelectMultiple(MarkupContainer parent, String id)
	{
		super(parent, id);
	}

	/**
	 * @see wicket.Component#Component(MarkupContainer, String, IModel)
	 */
	public SelectMultiple(MarkupContainer parent, String id, IModel<Collection<T>> model)
	{
		super(parent, id, model);
	}

	/**
	 * @see AbstractSelect#clearModel()
	 */
	@Override
	protected void clearModel()
	{
		modelChanging();
		getModelObject().clear();
	}
	
	/**
	 * @see AbstractSelect#assignValue(Object)
	 */	
	@Override
	@SuppressWarnings("unchecked")
	protected void assignValue(Object value)
	{
		getModelObject().add((T)value);
	}
	
	/**
	 * @see AbstractSelect#checkSelectedOptionsCount(int)
	 */	
	@Override
	protected void checkSelectedOptionsCount(int count)
	{		
		// any count is all right for this component
	}
	
	/**
	 * @see AbstractSelect#finishModelUpdate()
	 */	
	@Override
	protected void finishModelUpdate()
	{
		modelChanged();
	}

}
