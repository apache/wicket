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
package org.apache.wicket.examples.breadcrumb;

import org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel;
import org.apache.wicket.extensions.breadcrumb.panel.BreadCrumbPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Test bread crumb enabled panel.
 * 
 * @author Eelco Hillenius
 */
public class ThirdPanel extends BreadCrumbPanel
{
	/**
	 * Construct.
	 * 
	 * @param id
	 * @param breadCrumbModel
	 */
	public ThirdPanel(final String id, final IBreadCrumbModel breadCrumbModel)
	{
		super(id, breadCrumbModel);
	}

	/**
	 * @see org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant#getTitle()
	 */
	@Override
	public IModel<String> getTitle()
	{
		return Model.of("third");
	}
}
