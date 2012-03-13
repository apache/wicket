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
package org.apache.wicket.markup.html;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.util.io.IClusterable;

/**
 * An interface to be implemented by {@link org.apache.wicket.behavior.Behavior}s,
 * {@link org.apache.wicket.ajax.attributes.IAjaxCallListener}s.
 * 
 * Example:
 * 
 * <pre>
 * class MyAjaxCallDecorator implements IAjaxCallListener, IComponentAwareHeaderContributor
 * {
 *
 *  // IAjaxCallListener methods omitted for brevity
 *
 * 	public void renderHead(Component component, IHeaderResponse response)
 * 	{
 * 		response.render(new OnLoadJavaScriptHeaderItem(&quot;alert('page loaded!');&quot;));
 * 	}
 * }
 * </pre>
 */
public interface IComponentAwareHeaderContributor extends IClusterable
{
	/**
	 * Render to the web response whatever the component-aware wants to contribute to the head
	 * section.
	 * 
	 * @param component
	 *            component which is contributing to the response. This parameter is here to give
	 *            the component as the context for component-awares implementing this interface
	 * 
	 * @param response
	 *            Response object
	 */
	void renderHead(Component component, IHeaderResponse response);
}
