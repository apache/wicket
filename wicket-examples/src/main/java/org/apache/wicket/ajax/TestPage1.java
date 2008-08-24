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

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.ajaxng.AjaxEventBehavior;
import org.apache.wicket.ajaxng.AjaxRequestAttributes;
import org.apache.wicket.ajaxng.AjaxRequestAttributesImpl;
import org.apache.wicket.ajaxng.FunctionList;
import org.apache.wicket.ajaxng.request.AjaxRequestTarget;
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
			 * @see org.apache.wicket.ajaxng.AjaxBehavior#getAttributes()
			 */
			@Override
			public AjaxRequestAttributes getAttributes()
			{
				if (true) {
					return super.getAttributes();					
				}
				return new AjaxRequestAttributesImpl(super.getAttributes())
				{
					/**
					 * @see org.apache.wicket.ajaxng.AjaxRequestAttributesImpl#getUrlArguments()
					 */
					@Override
					public Map<String, Object> getUrlArguments()
					{
						Map<String, Object> args = new HashMap<String, Object>();
						
						args.put("key 1", "value\n\t1");
						
						return args;
					}
					/**
					 * @see org.apache.wicket.ajaxng.AjaxRequestAttributesImpl#getBeforeHandlers()
					 */
					@Override
					public FunctionList getBeforeHandlers()
					{
						return super.getBeforeHandlers().add("function(i) { W.Log.debug('before!'); }").add(0, "function(i) { W.Log.debug('b!'); }");
					}
					/**
					 * @see org.apache.wicket.ajaxng.AjaxRequestAttributesImpl#getSuccessHandlers()
					 */
					@Override
					public FunctionList getSuccessHandlers()
					{
						return super.getSuccessHandlers().add("function(i) { W.Log.debug('after!'); }");
					}
					/**
					 * @see org.apache.wicket.ajaxng.AjaxRequestAttributesImpl#getPreconditions()
					 */
					@Override
					public FunctionList getPreconditions()
					{
						return super.getPreconditions().add("function(i) { return true;}");
					}
					/**
					 * @see org.apache.wicket.ajaxng.AjaxRequestAttributesImpl#getUrlArgumentMethods()
					 */
					@Override
					public FunctionList getUrlArgumentMethods()
					{
						return super.getUrlArgumentMethods().add("function(i) { return { x:i.event.clientX,y:i.event.clientY }; }");
					}
				};
			}
			
			/**
			 * @see org.apache.wicket.ajaxng.AjaxBehavior#respond(org.apache.wicket.ajaxng.request.AjaxRequestTarget)
			 */
			@Override
			public void respond(AjaxRequestTarget target)
			{
				target.addComponent(c1);
			}
		
		});
		
		add(c1);
		
//		c1.add(new org.apache.wicket.ajax.AjaxEventBehavior("onclick")
//		{
//			/**
//			 * @see org.apache.wicket.ajax.AjaxEventBehavior#onEvent(org.apache.wicket.ajax.AjaxRequestTarget)
//			 */
//			@Override
//			protected void onEvent(AjaxRequestTarget target)
//			{
//			}
//		});
//		
	}		

}
