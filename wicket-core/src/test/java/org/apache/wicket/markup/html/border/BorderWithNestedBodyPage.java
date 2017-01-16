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
package org.apache.wicket.markup.html.border;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;

public class BorderWithNestedBodyPage extends WebPage 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8364550846663714451L;
	
	//https://issues.apache.org/jira/browse/WICKET-6303
	private boolean behaviorRendered = false;
	
	@Override
	protected void onInitialize() 
	{
		super.onInitialize();
		BorderWithNestedBody border = new BorderWithNestedBody("outerBorder");
		border.add(new AjaxLink<Void>("ajaxClick") 
		{            
            @Override
            public void internalRenderHead(HtmlHeaderContainer container)
            {
            	super.internalRenderHead(container);
            	behaviorRendered = true;
            }
            
			@Override
			public void onClick(AjaxRequestTarget target) 
			{}
		});
		
		add(border);
	}

	public boolean isBehaviorRendered() 
	{
		return behaviorRendered;
	}
}
