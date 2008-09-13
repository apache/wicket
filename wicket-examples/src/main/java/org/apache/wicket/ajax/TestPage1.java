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
package org.apache.wicket.ajax;

import org.apache._wicket.ajax.AjaxRequestTarget.ComponentEntry;
import org.apache._wicket.ajax.AjaxRequestTarget;
import org.apache._wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;

/**
 * @author matej
 */
public class TestPage1 extends WebPage
{

	/**
	 * Construct.
	 */
	public TestPage1()
	{
		final WebMarkupContainer c1 = new WebMarkupContainer("c1");
		c1.add(new AjaxEventBehavior("click")
		{
			
			/**
			 * @see org.apache.wicket.ajaxng.AjaxBehavior#respond(org.apache.wicket.ajaxng.AjaxRequestTarget)
			 */
			@Override
			public void onEvent(AjaxRequestTarget target)
			{
				ComponentEntry e = new ComponentEntry(c1);
				//e.setAfterReplaceJavascript("console.info(insertedElements); notify()");
				target.addComponent(e);
				//target.prependJavascript("console.info(requestQueueItem); alert('x');", false);
			}
		
		});
		
		add(c1);
		
//		c1.add(new org.apache.wicket.ajax.AjaxEventBehavior("onclick")
//		{
//			/**
//			 * @see org.apache.wicket.ajax.AjaxEventBehavior#onEvent(org.apache.wicket.ajax.AjaxRequestTarget)
//			 */
//			@Override
//			protected void onEvent(org.apache.wicket.ajax.AjaxRequestTarget target)
//			{
//			}
//		});
		
	}		

}
