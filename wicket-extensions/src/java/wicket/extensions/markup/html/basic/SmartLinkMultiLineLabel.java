/*
 * $Id: SmartLinkMultiLineLabel.java 5860 2006-05-25 20:29:28 +0000 (Thu, 25 May
 * 2006) eelco12 $ $Revision$ $Date: 2006-05-25 20:29:28 +0000 (Thu, 25
 * May 2006) $
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
package wicket.extensions.markup.html.basic;

import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.basic.MultiLineLabel;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.util.string.Strings;

/**
 * If you have email addresses or web URLs in the data that you are displaying,
 * then you can automatically display those pieces of data as hyperlinks, you
 * will not have to take any action to convert that data.
 * <p>
 * Email addresses will be wrapped with a &lt;a
 * href="mailto:xxx"&gt;xxx&lt;/a&gt; tag, where "xxx" is the email address that
 * was detected.
 * <p>
 * Web URLs will be wrapped with a &lt;a href="xxx"&gt;xxx&lt;/a&gt; tag, where
 * "xxx" is the URL that was detected (it can be any valid URL type, http://,
 * https://, ftp://, etc...)
 * 
 * @author Juergen Donnerstag
 */
public final class SmartLinkMultiLineLabel extends MultiLineLabel
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see MultiLineLabel#MultiLineLabel(MarkupContainer,String, String)
	 */
	public SmartLinkMultiLineLabel(MarkupContainer parent, String name, String label)
	{
		this(parent, name, new Model<String>(label));
	}

	/**
	 * @see MultiLineLabel#MultiLineLabel(MarkupContainer,String, IModel)
	 */
	@SuppressWarnings("unchecked")
	public SmartLinkMultiLineLabel(MarkupContainer parent, String name, IModel model)
	{
		super(parent, name, model);
	}

	/**
	 * @see wicket.Component#onComponentTagBody(wicket.markup.MarkupStream,
	 *      wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		final CharSequence body = Strings.toMultilineMarkup(getModelObjectAsString());
		replaceComponentTagBody(markupStream, openTag, SmartLinkLabel.smartLink(body));
	}
}
