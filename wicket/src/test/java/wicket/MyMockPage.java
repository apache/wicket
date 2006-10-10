/*
 * $Id: MyMockPage.java 5875 2006-05-25 22:52:19 +0000 (Thu, 25 May 2006)
 * eelco12 $ $Revision$ $Date: 2006-05-25 22:52:19 +0000 (Thu, 25 May
 * 2006) $
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
package wicket;

import java.util.Arrays;

import wicket.markup.html.WebPage;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.Form;

/**
 * 
 */
public class MyMockPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	DropDownChoice<String> drop1;
	DropDownChoice<String> drop2;

	/**
	 * Construct.
	 */
	public MyMockPage()
	{
		final Form form = new Form(this, "form");

		String[] choices = { "choice1", "choice2" };
		drop1 = new DropDownChoice<String>(form, "drop1", Arrays.asList(choices));
		drop2 = new DropDownChoice<String>(form, "drop2", Arrays.asList(choices));

		drop1.setNullValid(true);
		drop2.setNullValid(true);

	}
}