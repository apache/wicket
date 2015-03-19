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
package org.apache.wicket.protocol.https;

/**
 * Url scheme
 * 
 * @author igor
 */
public enum Scheme {
	/** https */
	HTTPS {
		@Override
		public boolean usesStandardPort(HttpsConfig config)
		{
			return getPort(config) == 443;
		}

		@Override
		public int getPort(HttpsConfig config)
		{
			return config.getHttpsPort();
		}
	},
	/** http */
	HTTP {
		@Override
		public boolean usesStandardPort(HttpsConfig config)
		{
			return getPort(config) == 80;
		}

		@Override
		public int getPort(HttpsConfig config)
		{
			return config.getHttpPort();
		}
	},
	/** any, aka preserve current */
	ANY {
		@Override
		public String urlName()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isCompatibleWith(Scheme other)
		{
			return true;
		}

		@Override
		public boolean usesStandardPort(HttpsConfig config)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public int getPort(HttpsConfig config)
		{
			throw new UnsupportedOperationException();
		}
	};


	/**
	 * @return scheme's url name
	 */
	public String urlName()
	{
		return name().toLowerCase();
	}

	/**
	 * Checks if two schemes are compatible. Compatible schemes do not require a redirect from the
	 * current scheme to the {@code other}.
	 * 
	 * @param other
	 * @return {@code true} iff the schemes are compatible.
	 */
	public boolean isCompatibleWith(Scheme other)
	{
		return this == other;
	}

	public abstract boolean usesStandardPort(HttpsConfig config);

	public abstract int getPort(HttpsConfig config);
}
