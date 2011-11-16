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

import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Simple {@link IRequestablePage} implementation for testing purposes
 * 
 * @author Matej Knopp
 */
public class MockPage extends MockComponent implements IRequestablePage
{
	private static final long serialVersionUID = -4776374984666617518L;

	private int pageId;

	/**
	 * Construct.
	 */
	public MockPage()
	{
		setPath("");
	}


	/**
	 * Construct.
	 * 
	 * @param pageId
	 */
	public MockPage(int pageId)
	{
		setPageId(pageId);
	}

	/**
	 * Sets the page id
	 * 
	 * @param pageId
	 * @return <code>this</code>
	 */
	public MockPage setPageId(int pageId)
	{
		this.pageId = pageId;
		return this;
	}

	@Override
	public IRequestablePage getPage()
	{
		return this;
	}

	@Override
	public int getPageId()
	{
		return pageId;
	}

	private final PageParameters pageParameters = new PageParameters();

	@Override
	public PageParameters getPageParameters()
	{
		return pageParameters;
	}

	private boolean bookmarkable;

	@Override
	public boolean isBookmarkable()
	{
		return bookmarkable;
	}

	/**
	 * Sets the bookmarkable flags
	 * 
	 * @param bookmarkable
	 * @return <code>this</code>
	 */
	public MockPage setBookmarkable(boolean bookmarkable)
	{
		this.bookmarkable = bookmarkable;
		return this;
	}

	private boolean stateless = false;

	/**
	 * Sets the stateless flag
	 * 
	 * @param stateless
	 * @return <code>this</code>
	 */
	public MockPage setPageStateless(boolean stateless)
	{
		this.stateless = stateless;
		return this;
	}

	@Override
	public boolean isPageStateless()
	{
		return stateless;
	}

	@Override
	public void renderPage()
	{
	}

	private boolean createBookmarkable;

	@Override
	public boolean wasCreatedBookmarkable()
	{
		return createBookmarkable;
	}

	/**
	 * Sets the createdBookmarkable flag.
	 * 
	 * @see IRequestablePage#wasCreatedBookmarkable()
	 * 
	 * @param createdBookmarkable
	 * @return <code>this</code>
	 */
	public MockPage setCreatedBookmarkable(boolean createdBookmarkable)
	{
		createBookmarkable = createdBookmarkable;
		return this;
	}

	private int renderCount;

	@Override
	public int getRenderCount()
	{
		return renderCount;
	}

	/**
	 * Sets the render count
	 * 
	 * @param renderCount
	 */
	public void setRenderCount(int renderCount)
	{
		this.renderCount = renderCount;
	}


	@Override
	public boolean setFreezePageId(boolean freeze)
	{
		return false;
	}
}
