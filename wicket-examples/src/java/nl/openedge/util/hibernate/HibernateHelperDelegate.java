/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.openedge.util.hibernate;

import java.net.URL;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;

/**
 * Interface for implementing the behaviour of HibernateHelper.
 * @author Eelco Hillenius
 */
public interface HibernateHelperDelegate
{

	/**
	 * initialise.
	 * @throws ConfigException when an exception occurs during initialization
	 */
	void init() throws ConfigException;

	/**
	 * Get session for this Thread.
	 * 
	 * @return an appropriate Session object
	 * @throws HibernateException when an unexpected Hibernate exception occurs
	 */
	Session getSession() throws HibernateException;

	/**
	 * close session for this Thread.
	 * @throws HibernateException when an unexpected Hibernate exception occurs
	 */
	void closeSession() throws HibernateException;

	/**
	 * disconnect session and remove from threadlocal for this Thread.
	 * @throws HibernateException when an unexpected Hibernate exception occurs
	 */
	void disconnectSession() throws HibernateException;

	/**
	 * set current session.
	 * 
	 * @param session
	 *            hibernate session
	 * @param actionForCurrentSession
	 *            one of the constants HibernateHelperThreadLocaleImpl.ACTION_CLOSE close current
	 *            session HibernateHelperThreadLocaleImpl.ACTION_DISCONNECT disconnect current
	 *            session
	 */
	void setSession(Session session, int actionForCurrentSession);

	/**
	 * @return the hibernate session factory
	 */
	SessionFactory getSessionFactory();

	/**
	 * Set the session factory.
	 * @param factory the session factory
	 */
	void setSessionFactory(SessionFactory factory);

	/**
	 * Get the configuration URL.
	 * @return URL the configuration url
	 */
	URL getConfigURL();

	/**
	 * Set the configuration URL.
	 * @param url the configuration URL
	 */
	void setConfigURL(URL url);

	/**
	 * @return Configuration
	 */
	Configuration getConfiguration();

	/**
	 * get factory level interceptor class name.
	 * 
	 * @return String factory level interceptor class name
	 */
	String getInterceptorClass();

	/**
	 * set factory level interceptor class name.
	 * 
	 * @param className
	 *            factory level interceptor class name
	 */
	void setInterceptorClass(String className);

	/**
	 * If true, only one instance will be created of the interceptor for all sessions, if false, a
	 * new - and thus thread safe - instance will be created for session.
	 * 
	 * @return boolean
	 */
	boolean isSingleInterceptor();

	/**
	 * If true, only one instance will be created of the interceptor for all sessions,
	 * if false, a new - and thus thread safe - instance will be created for session.
	 * 
	 * @param b If true, only one instance will be created of the interceptor
	 * for all sessions, if false, a new - and thus thread safe - instance will
	 * be created for session
	 */
	void setSingleInterceptor(boolean b);

}