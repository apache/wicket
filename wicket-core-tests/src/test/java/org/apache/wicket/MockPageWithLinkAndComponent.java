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

import org.apache.wicket.markup.html.WebPage;

/**
 * Use this mock when testing you wan't to test a link and a component on a page.
 * <p>
 * Example:
 * 
 * <pre>
 * Page page = new MockPageWithLinkAndComponent();
 * page.add(new Link(MockPageWithLinkAndComponent.LINK_ID)
 * {
 * 	public void onClick()
 * 	{
 * 		// ...
 * 	}
 * });
 * 
 * page.add(new WebComponent(MockPageWithLinkAndComponent.COMPONENT_ID));
 * </pre>
 * 
 * @author Frank Bille
 */
public class MockPageWithLinkAndComponent extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Use this constant for links you add to the page.
	 */
	public static final String LINK_ID = "link";

	/**
	 * Use this constant for components you add to the page.
	 */
	public static final String COMPONENT_ID = "component";
}
