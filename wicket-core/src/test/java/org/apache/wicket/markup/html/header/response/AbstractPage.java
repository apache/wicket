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
package org.apache.wicket.markup.html.header.response;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.head.PriorityHeaderItem;

/**
 * Page with a head and header contribution
 */
public class AbstractPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	@Override
	public void renderHead(IHeaderResponse response)
	{
		response.render(StringHeaderItem.forString("<title>AbstractPage-HeaderItem</title>\n"));
		response.render(new PriorityHeaderItem(
			StringHeaderItem.forString("<title>AbstractPage-PriorityHeaderItem</title>\n")));
	}
}
