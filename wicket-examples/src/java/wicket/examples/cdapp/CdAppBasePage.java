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

import net.sf.hibernate.Session;
import wicket.examples.WicketExamplePage;
import wicket.examples.cdapp.model.CdDao;
import wicket.model.IModel;

/**
 * Base page for the cd app example.
 *
 * @author Eelco Hillenius
 */
public abstract class CdAppBasePage extends WicketExamplePage
{
	/**
	 * Construct.
	 */
	public CdAppBasePage()
	{
		super();
	}

	/**
	 * Construct.
	 * @param model
	 */
	public CdAppBasePage(IModel model)
	{
		super(model);
	}

	/**
	 * Gets an instance of cd dao.
	 * @return an instance of cd dao
	 */
	protected final CdDao getCdDao()
	{
		Session hibernateSession = ((CdAppRequestCycle)getRequestCycle()).getHibernateSession();
		return new CdDao(hibernateSession);
	}
}