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
package wicket.extensions.markup.html.repeater.data;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import wicket.model.IModel;
import wicket.model.Model;

/**
 * Allows the use of lists with dataview. The only requirement is that either
 * list items must be serializable or model(Object) needs to be overridden to
 * provide the proper model implementation.
 * 
 * @author Igor Vaynberg ( ivaynberg )
 * 
 */
public class ListDataProvider implements IDataProvider
{
	private static final long serialVersionUID = 1L;

	/** reference to the list used as dataprovider for the dataview */
	private List list;

	/**
	 * 
	 * @param list
	 *            the list used as dataprovider for the dataview
	 */
	public ListDataProvider(List list)
	{
		if (list == null)
		{
			throw new IllegalArgumentException("argument [list] cannot be null");
		}

		this.list = list;
	}

	/**
	 * @see IDataProvider#iterator(int, int)
	 */
	public Iterator iterator(final int first, final int count)
	{
		return list.listIterator(first);
	}

	/**
	 * @see IDataProvider#size()
	 */
	public int size()
	{
		return list.size();
	}

	/**
	 * @see IDataProvider#model(Object)
	 */
	public IModel model(Object object)
	{
		return new Model((Serializable)object);
	}

}
