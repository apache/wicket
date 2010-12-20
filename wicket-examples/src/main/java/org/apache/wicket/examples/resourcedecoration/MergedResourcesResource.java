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
package org.apache.wicket.examples.resourcedecoration;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.util.lang.WicketObjects;
import org.apache.wicket.util.resource.PackageResourceStream;

/**
 * A shared resource that receives as query parameters a list of the names of grouped resources and
 * their type, then it loads each resource and merges it with the others. At the end returns the
 * merged resource.
 * 
 * @author jthomerson
 */
public class MergedResourcesResource extends AbstractResource
{
	private static final long serialVersionUID = 1L;

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes)
	{
		PageParameters pageParameters = attributes.getParameters();
		String refsVal = pageParameters.get("refs").toOptionalString();
		String type = pageParameters.get("type").toOptionalString();
		boolean isCss = "css".equals(type);

		ResourceResponse resourceResponse = new ResourceResponse();

		if (resourceResponse.dataNeedsToBeWritten(attributes))
		{
			if (isCss)
			{
				resourceResponse.setContentType("text/css");
			}
			else
			{
				resourceResponse.setContentType("text/javascript");
			}

			final StringBuilder combined = new StringBuilder();
			String[] refs = refsVal.split("\\|");
			for (String ref : refs)
			{
				String[] parts = ref.split(":");
				String className = parts[0];
				String name = parts[1];

				PackageResourceStream resourceStream = new PackageResourceStream(
					WicketObjects.resolveClass(className), name);
				try
				{
					BufferedReader br = new BufferedReader(new InputStreamReader(
						resourceStream.getInputStream()));
					for (String line = br.readLine(); line != null; line = br.readLine())
					{
						combined.append(line).append("\n");
					}
				}
				catch (Exception e)
				{
					combined.append("/* ERROR: ").append(e.getMessage()).append(" */\n");
				}
			}

			resourceResponse.setWriteCallback(new WriteCallback()
			{
				@Override
				public void writeData(Attributes attributes)
				{
					attributes.getResponse().write(combined);
				}
			});
		}

		return resourceResponse;
	}
}
