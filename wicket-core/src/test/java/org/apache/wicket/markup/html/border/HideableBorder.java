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
package org.apache.wicket.markup.html.border;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;

/**
 * 
 */
public class HideableBorder extends Border
{
	private static final long serialVersionUID = 1L;

	private boolean hidden = false;

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public HideableBorder(String name)
	{
		super(name);
		final WebMarkupContainer containerWrapper = new WebMarkupContainer("wrapper")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible()
			{
				return !hidden;
			}
		};
		addToBorder(containerWrapper);
		containerWrapper.add(getBodyContainer());
		addToBorder(new AjaxLink<Void>("hideLink")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				hidden = !hidden;
				target.add(containerWrapper);
			}
		});
		containerWrapper.setOutputMarkupPlaceholderTag(true);
	}

	/**
	 * @return true if body of this border is hidden
	 */
	public boolean isHidden()
	{
		return hidden;
	}

	/**
	 * @param hidden
	 */
	public void setHidden(boolean hidden)
	{
		this.hidden = hidden;
	}


}
