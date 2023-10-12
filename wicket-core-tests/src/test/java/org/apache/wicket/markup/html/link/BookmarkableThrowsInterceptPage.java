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
package org.apache.wicket.markup.html.link;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.html.WebPage;

/**
 * @author jcompagner
 */
public class BookmarkableThrowsInterceptPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public static final MetaDataKey<Boolean> SECURITY = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;

	};

	/**
	 * Construct.
	 */
	public BookmarkableThrowsInterceptPage()
	{
		if (Application.get().getMetaData(SECURITY) == null)
		{
			throw new RestartResponseAtInterceptPageException(BookmarkableSetSecurityPage.class);
		}
	}

}
