/*
 * $Id: HelloWorldApplication.java 5394 2006-04-16 15:36:52 +0200 (Sun, 16 Apr 2006) jdonnerstag $
 * $Revision: 5394 $ $Date: 2006-04-16 15:36:52 +0200 (Sun, 16 Apr 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.threadtest.apps.app2;

import wicket.Application;
import wicket.markup.html.image.resource.DefaultButtonImageResource;
import wicket.protocol.http.WebApplication;

public class TestApp2 extends WebApplication {

	public static TestApp2 get() {
		return (TestApp2) Application.get();
	}

	public TestApp2() {
	}

	@Override
	public Class getHomePage() {
		return Home.class;
	}

	@Override
	protected void init() {
		getSharedResources().add("cancelButton", new DefaultButtonImageResource("Cancel"));
	}
}
