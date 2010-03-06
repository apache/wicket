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
package org.apache.wicket;

import org.apache.wicket.ng.request.mapper.BookmarkableMapper;
import org.apache.wicket.ng.request.mapper.BufferedResponseMapper;
import org.apache.wicket.ng.request.mapper.CompoundRequestMapper;
import org.apache.wicket.ng.request.mapper.HomePageMapper;
import org.apache.wicket.ng.request.mapper.PageInstanceMapper;
import org.apache.wicket.ng.request.mapper.ResourceReferenceMapper;
import org.apache.wicket.ng.request.mapper.parameters.SimplePageParametersEncoder;
import org.apache.wicket.util.IProvider;


/**
 * Mapper that encapsulates mappers that are necessary for Wicket to function.
 * 
 * @author igor.vaynberg
 * 
 */
public class SystemMapper extends CompoundRequestMapper
{
	/**
	 * Constructor
	 */
	public SystemMapper(Application application)
	{
		add(RestartResponseAtInterceptPageException.MAPPER);
		add(new HomePageMapper());
		add(new PageInstanceMapper());
		add(new BookmarkableMapper());
		add(new ResourceReferenceMapper(new SimplePageParametersEncoder(),
			new ParentFolderPlaceholderProvider(application)));
		add(new BufferedResponseMapper());
	}

	private static class ParentFolderPlaceholderProvider implements IProvider<String>
	{
		private final Application application;

		public ParentFolderPlaceholderProvider(Application application)
		{
			this.application = application;
		}

		public String get()
		{
			return application.getResourceSettings().getParentFolderPlaceholder();
		}


	}
}
