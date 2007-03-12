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
package wicket.markup.html;

import wicket.Component;
import wicket.markup.html.basic.Label;
import wicket.model.AbstractReadOnlyModel;

/**
 * 
 * @author Juergen Donnerstag
 */
public class ScopedPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	private int _clicked = 0;

	/**
	 * Construct.
	 */
	public ScopedPage()
	{
		super();
		add(new Label("unscoped", "unscoped"));

		add(new ScopedLabel("clicked", new AbstractReadOnlyModel()
		{
			private static final long serialVersionUID = 1L;
			
			public Object getObject(Component component)
			{
				return "Clicked: " + _clicked;
			}
		}));

		add(new ScopedLabel("global", "Global"));

		add(new ScopedLink("globalLink")
		{
			private static final long serialVersionUID = 1L;

			public void onClick()
			{
				_clicked++;
			}
		});

		WebMarkupContainer cont1 = new WebMarkupContainer("cont1");
		add(cont1);
		cont1.add(new ScopedLabel("localscoped", "Local Scoped"));
		cont1.add(new Label("local", "Local"));

		WebMarkupContainer cont11 = new WebMarkupContainer("cont11");
		cont1.add(cont11);
		cont11.add(new Label("global", " hide global"));

		WebMarkupContainer cont2 = new WebMarkupContainer("cont2");
		add(cont2);
		cont2.add(new Label("local", "Local2"));
		cont2.add(new ScopedLabel("localscoped", "Local Scoped"));
	}
}
