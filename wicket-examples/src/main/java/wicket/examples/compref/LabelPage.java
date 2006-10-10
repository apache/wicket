/*
 * $Id$ $Revision$ $Date$
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
package wicket.examples.compref;

import java.util.Date;

import wicket.examples.WicketExamplePage;
import wicket.markup.html.basic.Label;
import wicket.model.Model;
import wicket.model.StringResourceModel;

/**
 * Page with examples on {@link wicket.markup.html.basic.Label}.
 * 
 * @author Eelco Hillenius
 */
public class LabelPage extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public LabelPage()
	{
		// add a static label
		new Label(this, "staticLabel", "static text");

		// add a dynamic label. For this example, we create an annonymous
		// subclass
		// of Model (just because it is less work then directly implementing
		// IModel)
		// that returns a new java.util.Date on each invocation
		new Label(this, "dynamicLabel", new Model()
		{
			@Override
			public Object getObject()
			{
				return new Date();
			}
		});

		// add a label with a model that gets its display text from a resource
		// bundle
		// (which is in this case LabelPage.properties)
		// We use key 'label.current.locale' and provide a the current locale
		// for
		// parameter substitution.
		StringResourceModel stringResourceModel = new StringResourceModel("label.current.locale",
				this, null, new Object[] { getLocale() });
		new Label(this, "resourceLabel", stringResourceModel);

		// and here we add a label that contains markup. Normally, this markup
		// would be converted
		// to HTML escape characters so that e.g. a & really dislays as that
		// literal char wihout
		// our browser trying to resolve it to an HTML entity. But it this case
		// we actually want
		// our browser to interpret it as real markup, so we set the
		// escapeModelString property
		// to false
		Label markupLabel = new Label(this, "markupLabel",
				"now <i>that</i> is a pretty <b>bold</b> statement!");
		markupLabel.setEscapeModelStrings(false);
	}

	/**
	 * Override base method to provide an explanation
	 */
	@Override
	protected void explain()
	{
		String html = "<span wicket:id=\"markupLabel\" class=\"mark\">this text will be replaced</span>";
		String code = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Label markupLabel = new Label(\"markupLabel\", \"now &lt;i&gt;that&lt;/i&gt; is a pretty &lt;b&gt;bold&lt;/b&gt; statement!\");\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;markupLabel.setEscapeModelStrings(false);\n"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;add(markupLabel);";
		new ExplainPanel(this, html, code);

	}
}