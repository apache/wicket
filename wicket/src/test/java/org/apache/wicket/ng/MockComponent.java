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
package org.apache.wicket.ng;

import java.util.Collections;
import java.util.List;

import org.apache.wicket.ng.behavior.IBehavior;
import org.apache.wicket.ng.request.component.RequestableComponent;
import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.ng.util.string.Strings;

/**
 * Simple {@link RequestableComponent} implementation for testing purposes
 * 
 * @author Matej Knopp
 */
public class MockComponent implements RequestableComponent
{

	private static final long serialVersionUID = 1L;

	private String markupId;
	private String id;
	private RequestablePage page;
	private String path;
	
	/**
	 * Construct.
	 */
	public MockComponent()
	{
	}
	
	public RequestableComponent get(String path)
	{
		MockComponent c = new MockComponent();
		if (Strings.isEmpty(getPath()))
		{
			c.setPath(path);	
		}
		else
		{
			c.setPath(this.getPath() + ":" + path);
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

	public RequestablePage getPage()
	{
		return page;
	}
	
	/**
	 * Sets the page instance
	 * 
	 * @param page
	 * @return <code>this</code>
	 */
	public MockComponent setPage(RequestablePage page)
	{
		this.page = page;
		return this;
	}

	public String getPath()
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
}
