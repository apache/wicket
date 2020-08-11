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
package org.apache.wicket.coep;

import org.apache.wicket.request.http.WebResponse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Specifies the configuration for Cross-Origin Embedder Policy to be used for
 * {@link CoepRequestCycleListener}. Users can specify the paths that should be exempt from COEP and
 * one of 2 modes (<code>REPORTING, ENFORCING</code>) for the policy.
 *
 * You can enable COEP headers by adding it to the request cycle listeners in your
 * {@link org.apache.wicket.protocol.http.WebApplication#init() application's init method}:
 *
 * <pre>
 * &#064;Override
 * protected void init()
 * {
 * 	// ...
 * 	enableCoep(new CoepConfiguration.Builder().withMode(CoepMode.ENFORCING)
 * 		.withExemptions("EXEMPTED PATHS").build());
 * 	// ...
 * }
 * </pre>
 *
 * @author Santiago Diaz - saldiaz@google.com
 * @author Ecenaz Jen Ozmen - ecenazo@google.com
 *
 * @see CoepRequestCycleListener
 */
public class CoepConfiguration
{
	public enum CoepMode
	{
		ENFORCING("Cross-Origin-Embedder-Policy"),
		REPORTING("Cross-Origin-Embedder-Policy-Report-Only");

		final String header;

		CoepMode(String header)
		{
			this.header = header;
		}
	}

	static final String REQUIRE_CORP = "require-corp";

	private final Set<String> exemptions;
	private final CoepMode mode;

	private CoepConfiguration(Set<String> exemptions, CoepMode mode)
	{
		this.exemptions = exemptions;
		this.mode = mode;
	}

	public static class Builder
	{
		// default values - to avoid NullPointerExceptions when a build method isn't used
		private Set<String> exemptions = new HashSet<>();
		private CoepMode mode = CoepMode.REPORTING;

		public Builder withExemptions(String... exemptions)
		{
			this.exemptions.addAll(Arrays.asList(exemptions));
			return this;
		}

		public Builder withMode(CoepMode mode)
		{
			this.mode = mode;
			return this;
		}

		public CoepConfiguration build()
		{
			return new CoepConfiguration(exemptions, mode);
		}
	}

	public boolean isExempted(String path)
	{
		return exemptions.contains(path);
	}

	public void addCoepHeader(WebResponse resp)
	{
		resp.setHeader(mode.header, REQUIRE_CORP);
	}
}
