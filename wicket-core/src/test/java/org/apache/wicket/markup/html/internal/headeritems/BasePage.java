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
package org.apache.wicket.markup.html.internal.headeritems;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * A base page that contributes a StringHeaderItem
 * and adds a Panel with its own contributions
 */
public class BasePage extends WebPage {
	private static final long serialVersionUID = 1L;

	public BasePage(final PageParameters parameters) {
		super(parameters);

		add(new PanelA("panel"));
    }

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		response.render(StringHeaderItem.forString("<meta name='fromBasePage' content='1'/>"));
	}
}
