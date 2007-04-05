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
package wicket.protocol.http;

import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxLink;
import wicket.markup.html.WebPage;

/**
 * Tests that an error page is displayed on runtime errors during ajax requests.
 */
public class TestErrorPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	private boolean clicked = false;

	/**
	 * Construct.
	 */
	public TestErrorPage()
	{

		add(new AjaxLink("link")
		{
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target)
			{
				clicked = true;
				target.addComponent(this);
			}

			protected void onAfterRender()
			{
				if (clicked)
					throw new IllegalStateException("Intentional error");
			}
		});
	}

}
