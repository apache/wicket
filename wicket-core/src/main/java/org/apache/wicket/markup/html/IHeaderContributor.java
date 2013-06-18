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
package org.apache.wicket.markup.html;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.util.io.IClusterable;

/**
 * An interface to be implemented by application level listeners or other entities that wish to
 * contribute to the header section of the page. Class {@link org.apache.wicket.Component} already
 * implements it. <br/>
 * Example: <br/>
 * <br/>
 * 
 * <pre>
 * class MyPanel extends Panel
 * {
 * 	public MyPanel(String id)
 * 	{
 * 		super(id);
 * 	}
 * 
 * 	public void renderHead(IHeaderResponse response)
 * 	{
 * 		response.render(JavaScriptHeaderItem.forScript(&quot;alert('page loaded!');&quot;));
 * 	}
 * }
 * </pre>
 * 
 * @see IHeaderResponse
 * @see HeaderItem
 * @see Application#getHeaderContributorListeners()
 * 
 * @author Juergen Donnerstag
 * @author Matej Knopp
 */
public interface IHeaderContributor extends IClusterable
{
	/**
	 * Render to the web response whatever the component wants to contribute to the head section.
	 * 
	 * @param response
	 *            Response object
	 */
	void renderHead(IHeaderResponse response);
}
