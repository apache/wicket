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
package org.apache.wicket.examples.niceurl;

import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Simple bookmarkable page that displays page parameters which is mounted with parameter
 * placeholders.
 * 
 * @see MountedMapper
 * @author Eelco Hillenius
 */
public class Page2PP extends Page2
{
	/**
	 * Constructor
	 * 
	 * @param parameters
	 */
	public Page2PP(PageParameters parameters)
	{
		super(parameters);
	}
}
