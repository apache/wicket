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
package org.apache.wicket.util.io;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;

/**
 * @author jcompagner
 */
public class PageA extends WebPage
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private PageB page;
	
	public PageA(PageB page)
	{
		this.page = page;
		add(new AttributeModifier("test",new Model(page)));
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (obj instanceof PageA)
		{
			if (getNumericId() == ((PageA)obj).getNumericId())
			{
				if (page != null)
				{
					return page.equals(((PageA)obj).page);
				}
				return ((PageA)obj).page == null;
			}
		}
		return false;
	}

	/**
	 * @return
	 */
	public PageB getB()
	{
		return page;
	}
}
