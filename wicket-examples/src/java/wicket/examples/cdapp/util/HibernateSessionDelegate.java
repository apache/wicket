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
package wicket.examples.cdapp.util;

import net.sf.hibernate.Session;
import wicket.RequestCycle;
import wicket.contrib.data.model.hibernate.IHibernateSessionDelegate;
import wicket.examples.cdapp.CdAppRequestCycle;

/**
 * Implementation of {@link wicket.contrib.data.model.hibernate.IHibernateSessionDelegate}
 * for the cd app example.
 *
 * @author Eelco Hillenius
 */
public final class HibernateSessionDelegate implements IHibernateSessionDelegate
{
	/**
	 * @see wicket.contrib.data.model.hibernate.IHibernateSessionDelegate#getSession()
	 */
	public Session getSession()
	{
		return ((CdAppRequestCycle)RequestCycle.get()).getHibernateSession();
	}
}