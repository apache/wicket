/*
 * $Id: MergedMarkup.java 5771 2006-05-19 12:04:06 +0000 (Fri, 19 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-19 12:04:06 +0000 (Fri, 19 May
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
package wicket.markup;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A Markup class which represents merged markup, as it is required for markup
 * inheritance.
 * <p>
 * The Markups are merged at load time. Deep markup hierarchies are supported.
 * Multiple inheritance is not.
 * <p>
 * The markup resource file, which is associated with the markup, will be the
 * resource of the requested markup file. The base markup resources are not.
 * <p>
 * Base Markup must have a &lt;wicket:hild/&gt; tag which the position where the
 * derived markup is inserted. From the derived markup all tags in between
 * &lt;wicket:extend&gt; and &lt;/wicket:extend&gt; will be inserted.
 * <p>
 * In addition, all &lt;wicket:head> regions are copied as well as the body
 * onLoad attribute. This allows to develop completely self-contained plug &
 * play components including javascript etc.
 * 
 * @author Juergen Donnerstag
 */
public final class MergedMarkup extends Markup
{
	private final static Log log = LogFactory.getLog(MergedMarkup.class);

	/** The list of all dependent markup resources except the top one */
	private final List<MarkupResourceStream> baseMarkupResources = new ArrayList<MarkupResourceStream>();

	/**
	 * Merge inherited and base markup.
	 * 
	 * @param markup
	 *            The inherited markup
	 * @param baseMarkup
	 *            The base markup
	 */
	public MergedMarkup(final IMarkup markup, final IMarkup baseMarkup)
	{
		// Copy settings from derived markup
		setResource(markup.getResource());
		setXmlDeclaration(markup.getXmlDeclaration());
		setEncoding(markup.getEncoding());
		setWicketNamespace(markup.getWicketNamespace());

		if (baseMarkup instanceof MergedMarkup)
		{
			this.baseMarkupResources.addAll(((MergedMarkup)baseMarkup)
					.getBaseMarkupResourceStreams());
		}
	}

	/**
	 * 
	 * @return The list of base markups
	 */
	List<MarkupResourceStream> getBaseMarkupResourceStreams()
	{
		return this.baseMarkupResources;
	}
}
