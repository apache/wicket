/*
 * $Id: WicketRemoveTagHandler.java,v 1.4 2005/01/23 17:45:03 jdonnerstag
 * Exp $ $Revision$ $Date$
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
package wicket.markup.parser.filter;

import java.text.ParseException;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupElement;
import wicket.markup.parser.AbstractMarkupFilter;
import wicket.markup.parser.IMarkupFilter;

/**
 * This is a markup inline filter. It identifies wicket:i18n attributes
 * and replaces the attributes referenced. E.g. wicket:message="value=key"
 * would replace or add the attribute "value" with the message associated
 * with "key". The 
 * 
 * @author Juergen Donnerstag
 */
public final class WicketMessageTagHandler extends AbstractMarkupFilter
{
	/** Logging */
	private final static Log log = LogFactory.getLog(WicketMessageTagHandler.class);
	
	/** TODO Namespace should not be a constant */
	private final static String WICKET_MESSAGE_ATTR_NAME = "wicket:message";

	/** globally enable wicket:message; If accepted by user, we should use an apps setting */
	public static boolean enable = true;
	
	/** The MarkupContainer requesting the information */
	private final MarkupContainer container;
	
	/**
	 * Construct.
	 * 
	 * @param container
	 *            The container requesting the current markup
	 * @param parent
	 *            The next MarkupFilter in the processing chain
	 */
	public WicketMessageTagHandler(final MarkupContainer container, final IMarkupFilter parent)
	{
		super(parent);
		this.container = container;
	}

	/**
	 * 
	 * @see wicket.markup.parser.IMarkupFilter#nextTag()
	 * @return The next tag to be processed. Null, if not more tags are
	 *         available
	 */
	public final MarkupElement nextTag() throws ParseException
	{
		// Get the next tag from the next MarkupFilter in the chain
		// If null, no more tags are available
		final ComponentTag tag = (ComponentTag)getParent().nextTag();
		if (tag == null)
		{
			return tag;
		}

		final String wicketMessageAttribute = tag.getAttributes().getString(WICKET_MESSAGE_ATTR_NAME);
		if ((wicketMessageAttribute != null) && (wicketMessageAttribute.trim().length() > 0))
		{
			if (this.container == null)
			{
				throw new ParseException("Found "
						+ WICKET_MESSAGE_ATTR_NAME 
						+ " but the message can not be resolved, because the associated Page is not known."
						+ " This might be caused by using the wrong MarkupParser constructor"
						, tag.getPos());
			}

			StringTokenizer attrTokenizer = new StringTokenizer(wicketMessageAttribute, ",");
			while (attrTokenizer.hasMoreTokens())
			{
				String text = attrTokenizer.nextToken().trim();
				if (text == null)
				{
					text = wicketMessageAttribute;
				}

				StringTokenizer valueTokenizer = new StringTokenizer(text, "=");
				if (valueTokenizer.countTokens() != 2)
				{
					throw new ParseException(
							"Wrong format of wicket:message attribute value. " + text + "; Must be: key=value[, key=value]", 
							tag.getPos());
				}
				
				String attrName = valueTokenizer.nextToken();
				String messageKey = valueTokenizer.nextToken();
				if ((attrName == null) || (attrName.trim().length() == 0) || (messageKey == null) || (messageKey.trim().length() == 0))
				{
					throw new ParseException(
							"Wrong format of wicket:message attribute value. " + text + "; Must be: key=value[, key=value]", 
							tag.getPos());
				}

				String value = container.getApplication().getLocalizer().getString(
						messageKey, container, "");

				if (value.length() > 0)
				{
					tag.getAttributes().put(attrName, value);
					tag.setModified(true);
				}
				else if (tag.getAttributes().get(attrName) == null)
				{
					tag.getAttributes().put(attrName, value);
					tag.setModified(true);
				}
				else
				{
					// Do not modify the existing value
				}
			}
		}
		
		return tag;
	}
}
