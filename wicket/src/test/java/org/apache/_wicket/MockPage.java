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
package org.apache._wicket;

/**
 * Simple {@link IPage} implementation for testing purposes
 * 
 * @author Matej Knopp
 */
public class MockPage extends MockComponent implements IPage
{
	
	private static final long serialVersionUID = 1L;
	
	private int pageId;
	
	/**
	 * Construct.
	 * 
	 * @param pageId
	 */
	public MockPage()
	{
		setPath("");
	}

	
	/**
	 * Construct.
	 * @param pageId
	 * @param pageVersion
	 * @param pageMapName
	 */
	public MockPage(int pageId, int pageVersion, String pageMapName)
	{
		setPageId(pageId);
		setPageVersionNumber(pageVersion);
		setPageMapName(pageMapName);
	}
	
	/**
	 * Sets the page id
	 * @param pageId
	 * @return <code>this</code>
	 */
	public MockPage setPageId(int pageId)
	{
		this.pageId = pageId;
		return this;
	}
	
	@Override
	public IPage getPage()
	{
		return this;
	}

	public int getPageId()
	{
		return pageId;
	}	

	private String pageMapName;
	
	public String getPageMapName()
	{
		return pageMapName;
	}
	
	/**
	 * Sets the pagemap name
	 * 
	 * @param pageMapName
	 * @return <code>this</code>
	 */
	public MockPage setPageMapName(String pageMapName)
	{
		this.pageMapName = pageMapName;
		return this;
	}
	
	private PageParameters pageParameters = new PageParameters();

	public PageParameters getPageParameters()
	{
		return pageParameters;
	}	

	private int pageVersionNumber;
	
	public int getPageVersionNumber()
	{
		return pageVersionNumber;
	}
	
	/**
	 * Sets the page version number
	 * 
	 * @param pageVersionNumber
	 * @return <code>this</code>
	 */
	public MockPage setPageVersionNumber(int pageVersionNumber)
	{
		this.pageVersionNumber = pageVersionNumber;
		return this;
	}

	private boolean bookmarkable;
	
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

	public boolean isPageStateless()
	{
		return false;
	}

	public void renderPage()
	{
	}
	
	private boolean createBookmarkable;

	public boolean wasCreatedBookmarkable()
	{
		return createBookmarkable;
	}
	
	/**
	 * Sets the createdBookmarkable flag.
	 * 
	 * @see IPage#wasCreatedBookmarkable()
	 * 
	 * @param createdBookmarkable
	 * @return <code>this</code>
	 */
	public MockPage setCreatedBookmarkable(boolean createdBookmarkable)
	{
		this.createBookmarkable = createdBookmarkable;
		return this;
	}

}
