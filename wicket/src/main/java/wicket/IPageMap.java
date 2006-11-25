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
package wicket;

import java.io.Serializable;

import wicket.session.pagemap.IPageMapEntry;

/**
 * @author eelcohillenius
 * @author Johan Compagner
 */
public interface IPageMap extends Serializable
{
	/**
	 * @param id
	 *            The page id to create an attribute for
	 * @return The session attribute for the given page (for replication of
	 *         state)
	 */
	String attributeForId(final int id);

	/**
	 * Removes all pages from this map
	 */
	void clear();

	/**
	 * Redirects to any intercept page previously specified by a call to
	 * redirectToInterceptPage.
	 * 
	 * @return True if an original destination was redirected to
	 * @see Component#redirectToInterceptPage(Page)
	 */
	boolean continueToOriginalDestination();

	/**
	 * Retrieves page with given id.
	 * 
	 * @param id
	 *            The page identifier
	 * @param versionNumber
	 *            The version to get
	 * @return Any page having the given id
	 */
	Page get(final int id, int versionNumber);

	/**
	 * Retrieves entry with given id.
	 * 
	 * @param id
	 *            The page identifier
	 * @return Any entry having the given id
	 */
	IPageMapEntry getEntry(final int id);

	/**
	 * @return Returns the name.
	 */
	String getName();

	/**
	 * @return The session that this PageMap is in.
	 */
	Session getSession();

	/**
	 * @return Size of this page map in bytes, including a sum of the sizes of
	 *         all the pages it contains.
	 */
	long getSizeInBytes();

	/**
	 * @return True if this is the default page map
	 */
	boolean isDefault();

	/**
	 * @return The next id for this pagemap
	 */
	int nextId();

	/**
	 * @param page
	 *            The page to put into this map
	 */
	void put(final Page page);

	/**
	 * Redirects browser to an intermediate page such as a sign-in page. The
	 * current request's URL is saved exactly as it was requested for future use
	 * by continueToOriginalDestination(); Only use this method when you plan to
	 * continue to the current URL at some later time; otherwise just use
	 * setResponsePage or, when you are in a constructor, redirectTo.
	 * 
	 * @param pageClazz
	 *            The page clazz to temporarily redirect to
	 */
	void redirectToInterceptPage(final Class pageClazz);

	/**
	 * Redirects browser to an intermediate page such as a sign-in page. The
	 * current request's url is saved for future use by method
	 * continueToOriginalDestination(); Only use this method when you plan to
	 * continue to the current url at some later time; otherwise just use
	 * setResponsePage or - when you are in a constructor or checkAccessMethod,
	 * call redirectTo.
	 * 
	 * @param page
	 *            The sign in page
	 * 
	 * @see Component#continueToOriginalDestination()
	 */
	void redirectToInterceptPage(final Page page);

	/**
	 * Removes this PageMap from the Session.
	 */
	void remove();

	/**
	 * Removes the page from the pagemap
	 * 
	 * @param page
	 *            page to be removed from the pagemap
	 */
	void remove(final Page page);

	/**
	 * @param entry
	 *            The entry to remove
	 */
	void removeEntry(final IPageMapEntry entry);

	/**
	 * @param session
	 *            Session to set
	 */
	void setSession(final Session session);

}