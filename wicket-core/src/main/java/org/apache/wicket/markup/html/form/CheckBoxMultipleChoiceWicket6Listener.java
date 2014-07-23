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
package org.apache.wicket.markup.html.form;

import org.apache.wicket.Component;
import org.apache.wicket.application.IComponentInstantiationListener;

/**
 * Restores the rendering behavior of CheckBoxMultipleChoice until Wicket 6.x.
 * That means:
 * <ul>
 *     <li>Uses <xmp><br/></xmp> as a suffix</li>
 *     <li>renders the label after the checkbox</li>
 * </ul>
 *
 * <p>
 *  Usage (in MyApplication#init()):
 *      getComponentInstantionListeners().add(new CheckBoxMultipleChoiceWicket6Listener());
 * </p>
 *
 * @deprecated Will be removed for Wicket 8.0.0. Use CSS for styling
 */
@Deprecated
public class CheckBoxMultipleChoiceWicket6Listener implements IComponentInstantiationListener
{
	@Override
	public void onInstantiation(Component component)
	{
		if (component instanceof CheckBoxMultipleChoice<?>)
		{
			CheckBoxMultipleChoice<?> checkBoxMultipleChoice = (CheckBoxMultipleChoice<?>) component;
			checkBoxMultipleChoice.setSuffix("<br/>\n");
			checkBoxMultipleChoice.setLabelPosition(AbstractChoice.LabelPosition.AFTER);
		}
	}
}
