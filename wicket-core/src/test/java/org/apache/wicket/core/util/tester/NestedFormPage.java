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
package org.apache.wicket.core.util.tester;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;

public class NestedFormPage extends WebPage
{
	public String url = "";
	public boolean outerSubmitted;
	public boolean innerSubmitted;

	public NestedFormPage()
	{
		Form<?> outer = new Form("outer")
		{
			@Override
			protected void onSubmit()
			{
				super.onSubmit();
				outerSubmitted = true;
			}
		};
		add(outer);
		Form<?> inner = new Form("inner")
		{
			@Override
			protected void onSubmit()
			{
				url = getRequest().getUrl().toString();
				innerSubmitted = true;
			}
		};
		outer.add(inner);
		inner.add(new Button("submit"));
	}
}
