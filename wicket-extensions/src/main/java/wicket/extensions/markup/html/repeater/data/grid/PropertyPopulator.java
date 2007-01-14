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
package wicket.extensions.markup.html.repeater.data.grid;

import wicket.markup.html.basic.Label;
import wicket.markup.repeater.Item;
import wicket.model.IModel;
import wicket.model.PropertyModel;

/**
 * A convinience implementation of {@link ICellPopulator} that adds a label that
 * will display the value of the specified property. Non-string properties will
 * be converted to a string before display.
 * <p>
 * Example
 * 
 * <pre>
 * ICellPopulator cityPopulator = new PropertyPopulator(&quot;address.city&quot;);
 * </pre>
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class PropertyPopulator implements ICellPopulator
{
	private static final long serialVersionUID = 1L;
	private String property;

	/**
	 * Constructor
	 * 
	 * @param property
	 *            property whose value will be displayed in the cell. uses
	 *            wicket's {@link PropertyModel} notation.
	 */
	public PropertyPopulator(String property)
	{
		if (property == null)
		{
			throw new IllegalArgumentException("argument [property] cannot be null");
		}
		this.property = property;
	}

	/**
	 * @see wicket.extensions.markup.html.repeater.data.grid.ICellPopulator#populateItem(wicket.markup.repeater.Item,
	 *      java.lang.String, wicket.model.IModel)
	 */
	public void populateItem(Item cellItem, String componentId, IModel rowModel)
	{
		cellItem.add(new Label(componentId, new PropertyModel(rowModel, property)));
	}

}
