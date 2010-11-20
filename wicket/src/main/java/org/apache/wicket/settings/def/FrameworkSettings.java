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
package org.apache.wicket.settings.def;

import org.apache.wicket.IDetachListener;
import org.apache.wicket.settings.IFrameworkSettings;
import org.apache.wicket.util.string.Strings;

/**
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 * @author Martijn Dashorst
 * @author James Carman
 */
public class FrameworkSettings implements IFrameworkSettings
{
	private IDetachListener detachListener;

	/**
	 * @see org.apache.wicket.settings.IFrameworkSettings#getVersion()
	 */
	public String getVersion()
	{
		String implVersion = null;
		Package pkg = getClass().getPackage();
		if (pkg != null)
		{
			implVersion = pkg.getImplementationVersion();
		}
		return Strings.isEmpty(implVersion) ? "n/a" : implVersion;
	}

	/**
	 * @see org.apache.wicket.settings.IFrameworkSettings#getDetachListener()
	 */
	public IDetachListener getDetachListener()
	{
		return detachListener;
	}

	/**
	 * @see org.apache.wicket.settings.IFrameworkSettings#setDetachListener(org.apache.wicket.IDetachListener)
	 */
	public void setDetachListener(IDetachListener detachListener)
	{
		this.detachListener = detachListener;
	}
}
