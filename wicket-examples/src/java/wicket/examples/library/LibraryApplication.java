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
package wicket.examples.library;

import wicket.ApplicationSettings;
import wicket.ISessionFactory;
import wicket.Session;
import wicket.examples.WicketExampleApplication;

/**
 * WicketServlet class for example.
 * @author Jonathan Locke
 */
public final class LibraryApplication extends WicketExampleApplication
{
    /**
     * Constructor.
     */
    public LibraryApplication()
    {
        getPages().setHomePage(Home.class);
        getSettings().setThrowExceptionOnMissingResource(false);
		getSettings().setRenderStrategy(ApplicationSettings.REDIRECT_TO_RENDER);
    }
    
    /**
	 * @see wicket.protocol.http.WebApplication#getSessionFactory()
	 */
	public ISessionFactory getSessionFactory()
	{
        return new ISessionFactory()
        {
            public Session newSession()
            {
                return new LibrarySession(LibraryApplication.this);
            }
        };
	}
}
