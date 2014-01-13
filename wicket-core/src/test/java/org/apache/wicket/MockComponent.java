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
package org.apache.wicket;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.util.string.Strings;

/**
 * Simple {@link IRequestableComponent} implementation for testing purposes
 * 
 * @author Matej Knopp
 */
public class MockComponent implements IRequestableComponent
{
	private String markupId;
	private String id;
	private IRequestablePage page;
	private String path;

	/**
	 * Construct.
	 */
	public MockComponent()
	{
	}

	@Override
	public IRequestableComponent get(String path)
	{
		MockComponent c = new MockComponent();
		if (Strings.isEmpty(getPageRelativePath()))
		{
			c.setPath(path);
		}
		else
		{
			c.setPath(getPageRelativePath() + ":" + path);
		}
		c.setPage(getPage());
		c.setId(Strings.lastPathComponent(path, ':'));
		return c;
	}

	@Override
	public String getId()
	{
		return id;
	}

	/**
	 * Sets the component id
	 * 
	 * @param id
	 * @return <code>this</code>
	 */
	public MockComponent setId(String id)
	{
		this.id = id;
		return this;
	}

	/**
	 * Sets the markup Id
	 * 
	 * @param markupId
	 * @return <code>this</code>
	 */
	public MockComponent setMarkupId(String markupId)
	{
		this.markupId = markupId;
		return this;
	}

	/**
	 * @param createIfDoesNotExist
	 * @return markupId
	 */
	public String getMarkupId(boolean createIfDoesNotExist)
	{
		return markupId;
	}

	@Override
	public IRequestablePage getPage()
	{
		return page;
	}

	/**
	 * Sets the page instance
	 * 
	 * @param page
	 * @return <code>this</code>
	 */
	public MockComponent setPage(IRequestablePage page)
	{
		this.page = page;
		return this;
	}

	@Override
	public String getPageRelativePath()
	{
		return path;
	}

	/**
	 * Sets the component path
	 * 
	 * @param path
	 * @return <code>this</code>
	 */
	public MockComponent setPath(String path)
	{
		this.path = path;
		return this;
	}

	@Override
	public void detach()
	{
	}

	@Override
	public boolean canCallListenerInterfaceAfterExpiry()
	{
		return false;
	}

	public boolean canCallListenerInterface()
	{
		return true;
	}

	/**
	 * @return false
	 */
	public boolean isEnabledInHierarchy()
	{
		return false;
	}

	/**
	 * @return false
	 */
	public boolean isVisibleInHierarchy()
	{
		return false;
	}

	@Override
	public int getBehaviorId(Behavior behavior)
	{
		throw new IllegalArgumentException();
	}

	@Override
	public Behavior getBehaviorById(int id)
	{
		return null;
	}
}
