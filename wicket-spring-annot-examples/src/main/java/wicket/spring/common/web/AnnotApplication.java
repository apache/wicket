/*
 * $Id$
 * $Revision$
 * $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.spring.common.web;

import wicket.spring.injection.annot.AnnotSpringWebApplication;
import wicket.util.time.Duration;

public class AnnotApplication extends AnnotSpringWebApplication {

	/**
	 * Custom initialisation. This method is called right after this application
	 * class is constructed, and the wicket servlet is set.
	 */
	protected void init() {
		super.init();

		getPages().setHomePage(HomePage.class);

		getSettings().addResourceFolder("src/main/java")
				.setResourcePollFrequency(Duration.seconds(1));

	}

}
