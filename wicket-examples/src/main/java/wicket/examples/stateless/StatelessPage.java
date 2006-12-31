/*
 * $Id: HelloWorld.java 5394 2006-04-16 13:36:52 +0000 (Sun, 16 Apr 2006)
 * jdonnerstag $ $Revision: 5394 $ $Date: 2006-04-16 13:36:52 +0000 (Sun, 16 Apr
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
package wicket.examples.stateless;

import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.StatelessForm;
import wicket.markup.html.form.TextField;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.model.Model;

/**
 * Another page of the stateless example.
 * 
 * @author Eelco Hillenius
 */
public class StatelessPage extends WebPage
{
	private TextField<String> tf;

	/**
	 * Constructor
	 */
	public StatelessPage()
	{
		setStatelessHint(true);
		new Label(this, "message", new SessionModel());
		new BookmarkablePageLink(this, "indexLink", Index.class);
		
		StatelessForm<Object> statelessForm = new StatelessForm<Object>(this,"statelessform"){
			/**
			 * @see wicket.markup.html.form.Form#onSubmit()
			 */
			@Override
			protected void onSubmit()
			{
				info("Onsubmit of stateless page pressed, textfield updated: " + tf.getModelObject());
				tf.setModelObject(tf.getModelObject()+ "_" +tf.getModelObject());
			}
		};
		tf = new TextField<String>(statelessForm,"textfield", new Model<String>());
	}
}