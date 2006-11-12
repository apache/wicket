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
package wicket.ajax.markup.html.componentMap;

import wicket.MarkupContainer;
import wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import wicket.behavior.AbstractAjaxBehavior;
import wicket.markup.html.basic.Label;
import wicket.model.PropertyModel;
import wicket.util.time.Duration;

/**
 * 
 */
public class SimpleTestPanel extends SimpleTestPanelBase
{
	private static final long serialVersionUID = 1L;

	private int count = 0;

	private AbstractAjaxBehavior timer;

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param name
	 */
	public SimpleTestPanel(MarkupContainer parent, String name)
	{
		super(parent, name);

		Label ajaxLabel = new Label(baseSpan, "linja1", new PropertyModel(this, "count"));
		this.timer = new AjaxSelfUpdatingTimerBehavior(Duration.seconds(2));

		ajaxLabel.add(timer);
	}

	/**
	 * 
	 * @return timer behvior
	 */
	public AbstractAjaxBehavior getTimeBehavior()
	{
		return this.timer;
	}

	/**
	 * @return Count
	 */
	public int getCount()
	{
		return count++;
	}
}
