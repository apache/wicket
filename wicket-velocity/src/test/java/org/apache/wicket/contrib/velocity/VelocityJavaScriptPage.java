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
package org.apache.wicket.contrib.velocity;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.collections.MiniMap;
import org.apache.wicket.util.lang.Packages;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.velocity.VelocityJavaScriptContributor;

/**
 * Test page.
 */
public class VelocityJavaScriptPage extends WebPage
{
	private static final long serialVersionUID = 1L;
	static final String MSG1 = "Stoopid test 1";

	/**
	 * Construct.
	 */
	public VelocityJavaScriptPage()
	{
		String templateName = Packages.absolutePath(getClass(), "testTemplate.vm");

		String id = "000001";
		String javascript = "msg1: Stoopid test 1\nmsg2: Stooopid test 2";
		JavaScriptUtils.writeJavaScript(getResponse(), javascript, id);

		IModel<MiniMap<String, String>> model = new Model<MiniMap<String, String>>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public MiniMap<String, String> getObject()
			{
				MiniMap<String, String> map = new MiniMap<String, String>(2);
				map.put("msg1", MSG1);
				map.put("msg2", "Stooopid test 2");
				return map;
			}

		};

		add(new VelocityJavaScriptContributor(templateName, model, id));
	}
}
