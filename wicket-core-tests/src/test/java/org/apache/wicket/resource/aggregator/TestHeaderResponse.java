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
package org.apache.wicket.resource.aggregator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.NoHeaderItem;
import org.apache.wicket.request.Response;
import org.apache.wicket.response.StringResponse;

/**
 * IHeaderResponse stub that only records the items rendered
 * 
 * @author papegaaij
 */
class TestHeaderResponse implements IHeaderResponse
{
	private StringResponse response = new StringResponse();
	private Set<Object> rendered = new HashSet<Object>();
	private List<HeaderItem> items = new ArrayList<HeaderItem>();
	private boolean closed = false;

	/**
	 * @return the recorded items
	 */
	public List<HeaderItem> getItems()
	{
		return items;
	}

	@Override
	public void render(HeaderItem item)
	{
		if (item != NoHeaderItem.get())
			items.add(item);
	}

	@Override
	public void markRendered(Object object)
	{
		rendered.add(object);
	}

	@Override
	public boolean wasRendered(Object object)
	{
		return rendered.contains(object);
	}

	@Override
	public Response getResponse()
	{
		return response;
	}

	@Override
	public void close()
	{
		response.close();
		closed = true;
	}

	@Override
	public boolean isClosed()
	{
		return closed;
	}
}