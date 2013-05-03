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
package org.apache.wicket.markup.head;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.request.Response;

/**
 * {@link HeaderItem} that has priority over other header items. {@code PriorityHeaderItem}s
 * rendered parent-first at the beginning of the header. Dependencies of a
 * {@code PriorityHeaderItem} also have priority.
 * 
 * @author papegaaij
 */
public class PriorityHeaderItem extends HeaderItem implements IWrappedHeaderItem
{
	private HeaderItem wrapped;

	/**
	 * Construct.
	 * 
	 * @param wrapped
	 *            the actual {@link HeaderItem} that should have priority
	 */
	public PriorityHeaderItem(HeaderItem wrapped)
	{
		this.wrapped = wrapped;
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
		List<PriorityHeaderItem> ret = new ArrayList<>();
		for (HeaderItem curDependency : getWrapped().getDependencies())
		{
			ret.add(new PriorityHeaderItem(curDependency));
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
		if (obj instanceof PriorityHeaderItem)
		{
			return ((PriorityHeaderItem)obj).getWrapped().equals(getWrapped());
		}
		return false;
	}

	@Override
	public String toString()
	{
		return "PriorityHeaderItem(" + getWrapped() + ")";
	}
}
