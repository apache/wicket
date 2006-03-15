/*
 * $Id: FormPage.java 4916 2006-03-13 23:15:39 -0800 (Mon, 13 Mar 2006) ivaynberg $
 * $Revision: 4916 $
 * $Date: 2006-03-13 23:15:39 -0800 (Mon, 13 Mar 2006) $
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
package wicket.examples.ajax.builtin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import wicket.extensions.ajax.markup.html.autocomplete.capxous.SimpleAutoAssistBehavior;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.model.Model;

/**
 * Page to demonstrate instant ajax validaion feedback. Validation is trigger in
 * onblur javascript event handler in every form input.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class AutoCompletePage extends BasePage
{
	/**
	 * Constructor
	 */
	public AutoCompletePage()
	{
		Form form=new Form("form");
		add(form);
		
		TextField tf1=new TextField("tf1", new Model());
		form.add(tf1);
		
		tf1.add(new SimpleAutoAssistBehavior() {

			protected Iterator getCompletionsForPrefix(String prefix)
			{
				List completions=new ArrayList();
				completions.add(prefix+"1");
				completions.add(prefix+"2");
				completions.add(prefix+"3");
				return completions.iterator();
			}
			
		});
		
		
	}
	

}