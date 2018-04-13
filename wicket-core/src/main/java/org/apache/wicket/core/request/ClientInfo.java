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
package org.apache.wicket.core.request;

import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.settings.RequestCycleSettings;
import org.apache.wicket.util.io.IClusterable;

/**
 * Encapsulates information about the request cycle agents' capabilities.
 *
 * @author Eelco Hillenius
 */
public abstract class ClientInfo implements IClusterable
{
	private static final long serialVersionUID = 1L;

	/**
	 * Initializes the {@link ClientInfo} user agent detection. This method is only called from
	 * {@link WebSession} if
	 * {@link #org.apache.wicket.settings.RequestCycleSettings.setGatherExtendedBrowserInfo(boolean)}
	 * of {@link RequestCycleSettings} is set to true
	 */
	public abstract void initialize();
}
