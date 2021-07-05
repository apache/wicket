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
package org.apache.wicket.core.util.io;

import org.apache.wicket.markup.html.WebPage;

/**
 * @author jcompagner
 */
public class PageB extends WebPage
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private PageA a;
	private final String test;

	/**
	 * Construct.
	 * 
	 * @param test
	 */
	public PageB(String test)
	{
		this.test = test;

	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof PageB)
		{
			return getPageId() == ((PageB)obj).getPageId() && test.equals(((PageB)obj).test);
		}
		return false;
	}

	/**
	 * @param a
	 */
	public void setA(PageA a)
	{
		this.a = a;
	}

	/**
	 * @return PageA
	 */
	public PageA getA()
	{
		return a;
	}

}
