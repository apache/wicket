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
package org.apache.wicket.coop;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.WebApplication;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Specifies the configuration for Cross-Origin Opener Policy to be used by
 * {@link CrossOriginOpenerPolicyRequestCycleListener} when adding COOP headers. Users can specify the paths that
 * should be exempt from COOP and one of 4 modes
 * (<code>UNSAFE_NONE, SAME_ORIGIN, SAME_ORIGIN_ALLOW_POPUPS, DISABLED</code>) for the policy. The
 * config object lives in {@link org.apache.wicket.settings.SecuritySettings}, users can specify
 * their COOP preferences with the following lines in their application's
 * {@link WebApplication#init()} method:
 * 
 * <pre>
 * &#064;Override
 * protected void init()
 * {
 * 	// ...
 * 	getSecuritySettings().setCrossOriginOpenerPolicyConfiguration(CoopMode.SAME_ORIGIN,
 * 		"EXEMPTED PATHS");
 * 	// ...
 * }
 * </pre>
 * 
 * The config value will be read once at stratup in {@link Application#initApplication()}, changing
 * the configuration at runtime will have no effect of the COOP headers set.
 *
 * @author Santiago Diaz - saldiaz@google.com
 * @author Ecenaz Jen Ozmen - ecenazo@google.com
 *
 * @see CrossOriginOpenerPolicyRequestCycleListener
 * @see org.apache.wicket.settings.SecuritySettings
 */
public class CrossOriginOpenerPolicyConfiguration
{
	public enum CoopMode
	{
		UNSAFE_NONE("unsafe-none"), SAME_ORIGIN("same-origin"), SAME_ORIGIN_ALLOW_POPUPS(
			"same-origin-allow-popups"), DISABLED("");

		final String keyword;

		CoopMode(String keyword)
		{
			this.keyword = keyword;
		}
	}


	private final Set<String> exemptions = new HashSet<>();
	private final CoopMode mode;

	public CrossOriginOpenerPolicyConfiguration(CoopMode mode, String... exemptions)
	{
		this.exemptions.addAll(Arrays.asList(exemptions));
		this.mode = mode;
	}

	public CrossOriginOpenerPolicyConfiguration(CoopMode mode)
	{
		this.mode = mode;
	}

	public CrossOriginOpenerPolicyConfiguration addExemptedPath(String path)
	{
		exemptions.add(path);
		return this;
	}

	public Set<String> getExemptions()
	{
		return exemptions;
	}

	public CoopMode getMode()
	{
		return mode;
	}

	public String getHeaderValue()
	{
		return mode.keyword;
	}

	public boolean isEnabled()
	{
		return mode != CoopMode.DISABLED;
	}
}
