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
package org.apache.wicket.extensions.ajax;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.AjaxCallListener;

/**
 * An {@link AjaxCallListener} to disable the associated component while the AJAX request is running.
 * Please note that under the hood this class uses DOM attribute 'disabled' to disable a component,
 * hence it can be used only with those HTML components that support this attribute.
 * 
 * @author Andrea Del Bene
 *
 */
public class AjaxDisableComponentListener extends AjaxCallListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3772784701483881109L;
	private static final String DISABLE_ENABLE_JS = ";document.getElementById('%s').disabled = %s;";

	@Override
	public CharSequence getBeforeHandler(Component component)
	{
		return generateHandlerJs(component, true);
	}

	@Override
	public CharSequence getCompleteHandler(Component component)
	{
		return generateHandlerJs(component, false);
	}

	@Override
	public CharSequence getFailureHandler(Component component)
	{
		return generateHandlerJs(component, false);
	}

	protected String generateHandlerJ(Component component, boolean disabled)
	{
		return String.format(DISABLE_ENABLE_JS, component.getMarkupId(), disabled);
	}
}
