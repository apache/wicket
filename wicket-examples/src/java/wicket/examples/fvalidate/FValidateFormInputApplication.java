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
package wicket.examples.fvalidate;

import wicket.ApplicationSettings;
import wicket.protocol.http.WebApplication;
import wicket.util.time.Duration;

/**
 * Application class for form input example.
 *
 * @author Eelco Hillenius
 */
public class FValidateFormInputApplication extends WebApplication
{
    /**
     * Constructor.
     */
    public FValidateFormInputApplication()
    {
        ApplicationSettings settings = getSettings();
        getPages().setHomePage(FValidateFormInput.class);
        settings.setResourcePollFrequency(Duration.ONE_SECOND);

        // show ?? markers when a message resource is not found
        settings.setThrowExceptionOnMissingResource(false);
    }
}
