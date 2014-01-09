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
package org.apache.wicket.examples.niceurl;

import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.examples.niceurl.mounted.Page3;
import org.apache.wicket.examples.niceurl.mounted.Page4;
import org.apache.wicket.examples.niceurl.mounted.Page5;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;


/**
 * Has links to bookmarkable pages with 'nice' uls.
 * 
 * @author Eelco Hillenius
 */
public class Home extends WicketExamplePage
{
	/**
	 * Construct.
	 */
	public Home()
	{
		// references to single mounts
		add(new BookmarkablePageLink<>("page1Link", Page1.class));
		add(new BookmarkablePageLink<>("page2Link", Page2.class));
		add(new BookmarkablePageLink<>("page2LinkPp", Page2PP.class));
		add(new BookmarkablePageLink<>("page2LinkUp", Page2UP.class));

		// references to package mounts
		add(new BookmarkablePageLink<>("page3Link", Page3.class));
		add(new BookmarkablePageLink<>("page4Link", Page4.class));
		add(new BookmarkablePageLink<>("page5Link", Page5.class));
	}

	/**
	 * @see org.apache.wicket.Component#isVersioned()
	 */
	@Override
	public boolean isVersioned()
	{
		return false;
	}
}
