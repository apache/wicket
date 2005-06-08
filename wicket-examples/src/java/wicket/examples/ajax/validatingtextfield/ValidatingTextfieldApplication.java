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
package wicket.examples.ajax.validatingtextfield;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.ApplicationSettings;
import wicket.protocol.http.WebApplication;
import wicket.util.time.Duration;

/**
 * Wicket test application.
 * @author Eelco Hillenius
 */
public class ValidatingTextfieldApplication extends WebApplication
{
	/** Logger. */
	private static Log log = LogFactory.getLog(ValidatingTextfieldApplication.class);

	/**
	 * Constructor
	 */
	public ValidatingTextfieldApplication()
	{
		ApplicationSettings settings = getSettings();
		settings.setThrowExceptionOnMissingResource(false);
		getPages().setHomePage(ValidatingTextFieldPage.class);
		if (!Boolean.getBoolean("cache-templates"))
		{
			Duration pollFreq = Duration.ONE_SECOND;
			settings.setResourcePollFrequency(pollFreq);
			log.info("template caching is INACTIVE");
		}
		else
		{
			log.info("template caching is ACTIVE");
		}
	}

}
