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

import java.util.Collections;
import java.util.List;

import org.apache.wicket.behavior.IBehavior;
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

	private static final long serialVersionUID = 1L;

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

	public String getMarkupId(boolean createIfDoesNotExist)
	{
		return markupId;
	}

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

	public void detach()
	{
	}

	public boolean canCallListenerInterface()
	{
		return true;
	}

	public List<IBehavior> getBehaviors()
	{
		return Collections.emptyList();
	}

	public boolean isEnabledInHierarchy()
	{
		return false;
	}

	public boolean isVisibleInHierarchy()
	{
		return false;
	}
}
