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
package org.apache.wicket.ajax.markup.html.componentMap;

import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import java.time.Duration;

/**
 * 
 */
public class SimpleTestPanel extends SimpleTestPanelBase
{
	private static final long serialVersionUID = 1L;

	private int count = 0;

	private final AbstractAjaxBehavior timer;

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public SimpleTestPanel(String name)
	{
		super(name);

		Label ajaxLabel = new Label("linja1", new PropertyModel<Integer>(this, "count"));
		timer = new AjaxSelfUpdatingTimerBehavior(Duration.ofSeconds(2));

		ajaxLabel.add(timer);
		baseSpan.add(ajaxLabel);
	}

	/**
	 * 
	 * @return timer behvior
	 */
	public AbstractAjaxBehavior getTimeBehavior()
	{
		return timer;
	}

	/**
	 * @return Count
	 */
	public int getCount()
	{
		return count++;
	}
}
