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
package org.apache.wicket.markup.html.internal;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;


/**
 * Mock page for testing.
 * 
 * @author Juergen Donnerstag
 */
public class EnclosurePage_1 extends WebPage
{
	private static final long serialVersionUID = 1L;
	
	public int pendingAfterRenderCount = 0; 

	/**
	 * Construct.
	 */
	public EnclosurePage_1()
	{
		add(new AfterRenderCountingLabel("label1", "Test Label 1"));
		add(new AfterRenderCountingLabel("label2", "Test Label 2"));
		add(new AfterRenderCountingLabel("label3", "Test Label 3").setVisible(false));
		add(new AfterRenderCountingLabel("label4", "Test Label 2"));
		add(new AfterRenderCountingLabel("label5", "Test Label 2"));
		add(new AfterRenderCountingLabel("label6", "Test Label 2"));
		add(new AfterRenderCountingLabel("label7", "Test Label 2"));
		
		WebMarkupContainer container = new WebMarkupContainer("container");
		add(container);
		
		container.add(new AfterRenderCountingLabel("label8", "Test Label 2"));
		
		add(new AfterRenderCountingLabel("label9", "Test Label 2"));
		add(new AfterRenderCountingLabel("label10", "Test Label 3"));
	}
	
	class AfterRenderCountingLabel extends Label {

		public AfterRenderCountingLabel(String id, String model)
		{
			super(id, model);
		}
		
		@Override
		protected void onBeforeRender()
		{
			super.onBeforeRender();
			
			
			pendingAfterRenderCount++;
		}
		
		@Override
		protected void onAfterRender()
		{
			super.onAfterRender();
			
			pendingAfterRenderCount--;
		}
	}
}
