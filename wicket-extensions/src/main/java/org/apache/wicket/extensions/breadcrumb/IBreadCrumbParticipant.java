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
package org.apache.wicket.extensions.breadcrumb;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.breadcrumb.panel.BreadCrumbPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.io.IClusterable;


/**
 * Bread crumb participants function as proxies for components that are part of a bread crumb
 * hierarchy. An example of a bread crumb is:
 * 
 * <pre>
 *     Home &gt; Products &amp; Solutions &gt; Hardware &gt; Desktop Systems
 * </pre>
 * 
 * In a {@link BreadCrumbPanel panel based implementation}, <tt>Home</tt>,
 * <tt>Products &amp; Solutions</tt> etc would be separate panels that all are bread crumb
 * participants: for instance the <tt>Home</tt> participant's {@link #getTitle() title} would return
 * 'Home', and {@link #getComponent() the component} would be the corresponding panel.
 * 
 * @author Eelco Hillenius
 */
public interface IBreadCrumbParticipant extends IClusterable
{
	/**
	 * Gets the participating component. Typically, this is a panel.
	 * 
	 * @return The participating component, must return a non-null value
	 */
	Component getComponent();

	/**
	 * Gets the title of the bread crumb, which will be used for displaying it.
	 * 
	 * @return The title of the bread crumb
	 */
	IModel<String> getTitle();

	/**
	 * Called when the corresponding bread crumb is activated.
	 * 
	 * @param previous
	 *            The previously active bread crumb participant, possibly null
	 */
	void onActivate(IBreadCrumbParticipant previous);
}
