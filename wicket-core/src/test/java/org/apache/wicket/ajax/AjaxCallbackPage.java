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

import java.util.Locale;

import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.html.WebPage;

public class AjaxCallbackPage extends WebPage
{
	private AbstractDefaultAjaxBehavior behavior1;
	private AbstractDefaultAjaxBehavior behavior2;

	public AjaxCallbackPage()
	{
		add(behavior1 = new AbstractDefaultAjaxBehavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void respond(AjaxRequestTarget target)
			{
			}
		});
		add(behavior2 = new AbstractDefaultAjaxBehavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getExtraParameters().put("param1", 123);
				attributes.getExtraParameters().put("param2", Locale.SIMPLIFIED_CHINESE);
			}

			@Override
			protected void respond(AjaxRequestTarget target)
			{
			}
		});
	}

	public AbstractDefaultAjaxBehavior getBehavior1()
	{
		return behavior1;
	}

	public AbstractDefaultAjaxBehavior getBehavior2()
	{
		return behavior2;
	}
}
