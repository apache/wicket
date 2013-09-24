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
package org.apache.wicket.markup.head.filter;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IWrappedHeaderItem;
import org.apache.wicket.request.Response;

/**
 * {@link HeaderItem} that specifies the filter it belongs to. Dependencies of a
 * {@code FilteredHeaderItem} belong to the same filter. If used in conjunction with
 * {@link JavaScriptFilteredIntoFooterHeaderResponse}, use
 * {@link FilteringHeaderResponse#DEFAULT_HEADER_FILTER_NAME DEFAULT_HEADER_FILTER_NAME} to render
 * items in the header.
 * 
 * @author papegaaij
 */
public class FilteredHeaderItem extends HeaderItem implements IWrappedHeaderItem
{
	private HeaderItem wrapped;

	private String filterName;

	/**
	 * Construct.
	 * 
	 * @param wrapped
	 *            the actual {@link HeaderItem}
	 * @param filterName
	 *            the name of the filter this item belongs to
	 */
	public FilteredHeaderItem(HeaderItem wrapped, String filterName)
	{
		this.wrapped = wrapped;
		this.filterName = filterName;
	}

	/**
	 * @return the actual {@link HeaderItem}
	 */
	@Override
	public HeaderItem getWrapped()
	{
		return wrapped;
	}

	@Override
	public FilteredHeaderItem wrap(HeaderItem item)
	{
		return new FilteredHeaderItem(item, getFilterName());
	}

	/**
	 * @return the name of the filter this item belongs to
	 */
	public String getFilterName()
	{
		return filterName;
	}

	@Override
	public Iterable<?> getRenderTokens()
	{
		return getWrapped().getRenderTokens();
	}

	@Override
	public void render(Response response)
	{
		getWrapped().render(response);
	}

	@Override
	public List<HeaderItem> getDependencies()
	{
		List<FilteredHeaderItem> ret = new ArrayList<>();
		for (HeaderItem curDependency : getWrapped().getDependencies())
		{
			ret.add(wrap(curDependency));
		}
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.addAll(ret);
		return dependencies;
	}

	@Override
	public Iterable<? extends HeaderItem> getProvidedResources()
	{
		return getWrapped().getProvidedResources();
	}

	@Override
	public int hashCode()
	{
		return getWrapped().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof FilteredHeaderItem)
		{
			return ((FilteredHeaderItem)obj).getWrapped().equals(getWrapped());
		}
		return false;
	}

	@Override
	public String toString()
	{
		return "FilteredHeaderItem(" + getWrapped() + ", " + getFilterName() + ")";
	}
}
