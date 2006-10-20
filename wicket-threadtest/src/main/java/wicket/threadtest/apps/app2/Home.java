/*
 * $Id: HelloWorld.java 5394 2006-04-16 15:36:52 +0200 (Sun, 16 Apr 2006)
 * jdonnerstag $ $Revision: 5394 $ $Date: 2006-04-16 15:36:52 +0200 (Sun, 16 Apr
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
package wicket.threadtest.apps.app2;

import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.IModel;

public class Home extends WebPage {

	public Home() {

		IModel model = new AbstractReadOnlyModel() {

			@Override
			public Object getObject() {
				return Pool.getConnection().getData();
			}
		};
		new Label(this, "label", model);
	}
}