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
package org.apache.wicket.examples.template.border;

import org.apache.wicket.examples.template.Banner;
import org.apache.wicket.examples.template.Banner1;
import org.apache.wicket.examples.template.Banner2;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;

/**
 * Border that holds layout elements that can be reused by pages.
 * 
 * @author Eelco Hillenius
 */
public class TemplateBorder extends Border
{
	/** the current banner. */
	private Banner currentBanner;

	/**
	 * Constructor
	 * 
	 * @param id
	 */
	public TemplateBorder(String id)
	{
		super(id);
		addToBorder(currentBanner = new Banner1("ad"));
		addToBorder(new Link("changeAdLink")
		{
			/**
			 * @see org.apache.wicket.markup.html.link.Link#onClick()
			 */
			@Override
			public void onClick()
			{
				if (currentBanner.getClass() == Banner1.class)
				{
					TemplateBorder.this.replaceInBorder(currentBanner = new Banner2("ad"));
				}
				else
				{
					TemplateBorder.this.replaceInBorder(currentBanner = new Banner1("ad"));
				}
			}
		});
		addToBorder(new BookmarkablePageLink<>("page1Link", Page1.class));
		addToBorder(new BookmarkablePageLink<>("page2Link", Page2.class));
	}
}