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
package wicket.settings;

import wicket.authorization.IAuthorizationStrategy;
import wicket.authorization.IUnauthorizedComponentInstantiationListener;
import wicket.markup.html.form.persistence.CookieValuePersisterSettings;
import wicket.util.crypt.ICryptFactory;

/**
 * Interface for security related settings
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public interface ISecuritySettings
{
	/**
	 * encryption key used by default crypt factory
	 */
	public static final String DEFAULT_ENCRYPTION_KEY = "WiCkEt-FRAMEwork";

	/**
	 * Gets the authorization strategy.
	 * 
	 * @return Returns the authorizationStrategy.
	 */
	IAuthorizationStrategy getAuthorizationStrategy();

	/**
	 * Get the defaults to be used by persistence manager
	 * 
	 * @return CookieValuePersisterSettings
	 */
	CookieValuePersisterSettings getCookieValuePersisterSettings();

	/**
	 * @return crypt factory used to generate crypt objects
	 */
	ICryptFactory getCryptFactory();

	/**
	 * Gets whether mounts should be enforced. If true, requests for mounted
	 * targets have to done through the mounted paths. If, for instance, a
	 * bookmarkable page is mounted to a path, a request to that same page via
	 * the bookmarkablePage parameter will be denied.
	 * 
	 * @return Whether mounts should be enforced
	 */
	boolean getEnforceMounts();

	/**
	 * @return The listener
	 * @see IUnauthorizedComponentInstantiationListener
	 */
	IUnauthorizedComponentInstantiationListener getUnauthorizedComponentInstantiationListener();

	/**
	 * Sets the authorization strategy.
	 * 
	 * @param strategy
	 *            new authorization strategy
	 * 
	 */
	void setAuthorizationStrategy(IAuthorizationStrategy strategy);

	/**
	 * @param cookieValuePersisterSettings
	 *            The cookieValuePersisterSettings to set.
	 */
	void setCookieValuePersisterSettings(CookieValuePersisterSettings cookieValuePersisterSettings);

	/**
	 * Sets the factory that will be used to create crypt objects. The crypt
	 * object returned from the first call is cached.
	 * 
	 * @param cryptFactory
	 */
	void setCryptFactory(ICryptFactory cryptFactory);

	/**
	 * Sets whether mounts should be enforced. If true, requests for mounted
	 * targets have to done through the mounted paths. If, for instance, a
	 * bookmarkable page is mounted to a path, a request to that same page via
	 * the bookmarkablePage parameter will be denied.
	 * 
	 * @param enforce
	 *            Whether mounts should be enforced
	 */
	void setEnforceMounts(boolean enforce);

	/**
	 * @param unauthorizedComponentInstantiationListener
	 *            The listener to set
	 * @see IUnauthorizedComponentInstantiationListener
	 */
	void setUnauthorizedComponentInstantiationListener(
			IUnauthorizedComponentInstantiationListener unauthorizedComponentInstantiationListener);
}
