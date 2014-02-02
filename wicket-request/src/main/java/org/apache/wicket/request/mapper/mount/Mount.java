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
package org.apache.wicket.request.mapper.mount;

import org.apache.wicket.request.Url;

/**
 * @deprecated Will be removed in Wicket 8.0
 */
@Deprecated
public class Mount
{
	/**
	 * The {@link org.apache.wicket.request.Url} to mount on
	 */
	private final Url url;

	/**
	 * A map of placeholder key/value pairs for the {@link #url}'s segments
	 */
	private MountParameters mountParameters = new MountParameters();

	/**
	 * Construct.
	 * 
	 * @param url
	 */
	public Mount(final Url url)
	{
		this.url = url;
	}

	/**
	 * 
	 * @param mountParameters
	 */
	public void setMountParameters(final MountParameters mountParameters)
	{
		this.mountParameters = mountParameters;
	}

	/**
	 * 
	 * @return mount parameters
	 */
	public MountParameters getMountParameters()
	{
		return mountParameters;
	}

	/**
	 * 
	 * @return Url
	 */
	public Url getUrl()
	{
		return url;
	}
}
