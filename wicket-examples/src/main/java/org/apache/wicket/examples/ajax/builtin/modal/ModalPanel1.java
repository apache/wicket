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
package org.apache.wicket.examples.ajax.builtin.modal;

import java.util.Map;

import org.apache.wicket.extensions.markup.html.form.datetime.DateTimeField;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author Matej Knopp
 */
public class ModalPanel1 extends Panel
{

	/**
	 * @param id
	 */
	public ModalPanel1(String id)
	{
		super(id);

		add(new DateTimeField("dateTimeField")
		{
			/**
			 * @see DateTimeField#configure(java.util.Map)
			 */
			@Override
			protected void configure(Map<String, Object> widgetProperties)
			{
				super.configure(widgetProperties);
				// IE 6 breaks layout with iframe - is that a YUI bug?
				widgetProperties.put("iframe", false);
			}
		});
	}

}
