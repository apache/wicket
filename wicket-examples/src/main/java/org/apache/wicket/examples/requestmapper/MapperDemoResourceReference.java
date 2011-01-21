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
package org.apache.wicket.examples.requestmapper;

import java.util.Locale;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.StringValue;

/**
 * A resource reference which shows how to serve a custom resource and use request parameters
 */
public class MapperDemoResourceReference extends ResourceReference
{

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public MapperDemoResourceReference()
	{
		super(new Key(MapperDemoResourceReference.class.getName(), "demoResource", Locale.ENGLISH,
			null, null));
	}

	@Override
	public IResource getResource()
	{
		return new AbstractResource()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected ResourceResponse newResourceResponse(Attributes attributes)
			{
				ResourceResponse resourceResponse = new ResourceResponse();

				PageParameters parameters = attributes.getParameters();
				StringValue sheetParam = parameters.get("sheet");
				StringValue formatParam = parameters.get("format");
				final String responseText = String.format(
					"You just printed sheet '%s' in format '%s'.\n\n\nPress browser's back button to go to the examples.",
					sheetParam, formatParam);

				resourceResponse.setContentType("text/plain");
				resourceResponse.setContentLength(responseText.length());
				resourceResponse.setWriteCallback(new WriteCallback()
				{

					@Override
					public void writeData(Attributes attributes)
					{
						attributes.getResponse().write(responseText);
					}
				});

				return resourceResponse;
			}

		};
	}

}
