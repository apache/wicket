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
package wicket.examples.cdapp;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import wicket.WicketRuntimeException;
import wicket.contrib.data.util.hibernate.HibernateHelper;
import wicket.examples.cdapp.model.CD;

/**
 * Simple DAO for cd's.
 *
 * @author Eelco Hillenius
 */
public final class CDDao
{
	/**
	 * Construct.
	 */
	public CDDao()
	{
	}

	/**
	 * Load a cd with the given id
	 * @param id the cd's id
	 * @return the cd
	 */
	public CD load(final Long id)
	{
		if (id == null)
		{
			throw new NullPointerException("id must be not null");
		}
		try
		{
			return (CD)HibernateHelper.getSession().load(CD.class, id);
		}
		catch (HibernateException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	/**
	 * Saves a cd.
	 * @param cd to save
	 */
	public void save(CD cd)
	{
		if (cd == null)
		{
			throw new NullPointerException("cd must be not null");
		}
		Session session = null;
		Transaction tx = null;
		try
		{
			session = HibernateHelper.getSession();
			tx = session.beginTransaction();
			session.saveOrUpdate(cd);
			tx.commit();
		}
		catch (HibernateException e)
		{
			try
			{
				tx.rollback();
			}
			catch (HibernateException ex)
			{
				ex.printStackTrace();
			}
			throw new WicketRuntimeException(e);
		}
	}

	/**
	 * Deletes a cd.
	 * @param cd to delete
	 */
	public void delete(CD cd)
	{
		if (cd == null)
		{
			throw new NullPointerException("cd must be not null");
		}
		Session session = null;
		Transaction tx = null;
		try
		{
			session = HibernateHelper.getSession();
			tx = session.beginTransaction();
			session.delete(cd);
			tx.commit();
		}
		catch (HibernateException e)
		{
			try
			{
				tx.rollback();
			}
			catch (HibernateException ex)
			{
				ex.printStackTrace();
			}
			throw new WicketRuntimeException(e);
		}
	}

	/**
	 * Deletes a cd.
	 * @param id id of cd to delete
	 */
	public void delete(Long id)
	{
		if (id == null)
		{
			throw new NullPointerException("id must be not null");
		}
		Session session = null;
		Transaction tx = null;
		try
		{
			session = HibernateHelper.getSession();
			tx = session.beginTransaction();
			session.delete(load(id));
			tx.commit();
		}
		catch (HibernateException e)
		{
			try
			{
				tx.rollback();
			}
			catch (HibernateException ex)
			{
				ex.printStackTrace();
			}
			throw new WicketRuntimeException(e);
		}
	}
}