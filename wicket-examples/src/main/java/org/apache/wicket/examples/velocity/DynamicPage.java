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
package org.apache.wicket.examples.velocity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.util.MapModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.core.util.resource.PackageResourceStream;
import org.apache.wicket.velocity.markup.html.VelocityPanel;

/**
 * Template example page.
 * 
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class DynamicPage extends WicketExamplePage
{
	/**
	 * Constructor
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public DynamicPage(final PageParameters parameters)
	{
		final IResourceStream template = new PackageResourceStream(DynamicPage.class,
			"fields.vm");

		Map<String, List<Field>> map = new HashMap<String, List<Field>>();
		List<Field> fields = VelocityTemplateApplication.getFields();
		map.put("fields", fields);

		VelocityPanel panel;
		add(panel = new VelocityPanel("templatePanel", new MapModel<String, List<Field>>(map))
		{
			@Override
			protected IResourceStream getTemplateResource()
			{
				return template;
			}

			@Override
			protected boolean parseGeneratedMarkup()
			{
				return true;
			}
		});
		for (Field field : fields)
		{
			panel.add(new TextField<Object>(field.getFieldName()));
		}
	}
}