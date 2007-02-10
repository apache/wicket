/*
 * $Id$ $Revision$ $Date:
 * 2006-03-04 13:35:30 +0100 (Sa, 04 Mrz 2006) $
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
package wicket.examples.niceurl.mounted;

import java.util.Random;

import wicket.PageParameters;
import wicket.examples.WicketExamplePage;
import wicket.examples.niceurl.Home;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.BookmarkablePageLink;

/**
 * Simple bookmarkable page that displays page parameters.
 * 
 * @author Igor Vaynberg
 */
public class Page5 extends WicketExamplePage
{
	private Random random = new Random();

	/**
	 * Constructor
	 * 
	 * @param parameters
	 */
	public Page5(PageParameters parameters)
	{
		String p1 = "CANNOT RESOLVE FROM URL";
		if (parameters.containsKey("param1"))
		{
			p1 = "";
			String[] array = parameters.getStringArray("param1");
			for (int i = 0; i < array.length; i++)
			{
				p1 += array[i];
				if (array.length - 1 != i)
				{
					p1 += ", ";
				}
			}
		}
		String p2 = "CANNOT RESOLVE FROM URL";
		if (parameters.containsKey("param2"))
		{
			p2 = "";
			String[] array = parameters.getStringArray("param2");
			for (int i = 0; i < array.length; i++)
			{
				p2 += array[i];
				if (array.length - 1 != i)
				{
					p2 += ", ";
				}
			}
		}

		add(new Label("p1", p1));
		add(new Label("p2", p2));

		String newP1 = String.valueOf(random.nextInt());
		String newP2 = String.valueOf(random.nextInt());

		PageParameters params = new PageParameters();
		params.put("param1", newP1);
		params.put("param2", newP2);

		BookmarkablePageLink link = new BookmarkablePageLink("refreshLink", Page5.class, params);
		add(link);
		add(new BookmarkablePageLink("homeLink", Home.class));
	}
}
