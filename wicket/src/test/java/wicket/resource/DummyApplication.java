/*
 * $Id: DummyApplication.java 5443 2006-04-17 20:02:21 +0000 (Mon, 17 Apr 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-04-17 20:02:21 +0000 (Mon, 17 Apr
 * 2006) $
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
package wicket.resource;

import wicket.Page;
import wicket.protocol.http.WebApplication;

/**
 * Dummy tester used for resource loader testing.
 * 
 * @author Chris Turner
 */
public class DummyApplication extends WebApplication
{
	/**
	 * Create the dummy tester.
	 */
	public DummyApplication()
	{
	}

	/**
	 * 
	 * @see wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends Page> getHomePage()
	{
		return DummyPage.class;
	}
}
