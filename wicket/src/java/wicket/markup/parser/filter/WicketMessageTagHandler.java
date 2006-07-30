/*
 * $Id: WicketMessageTagHandler.java 5771 2006-05-19 12:04:06 +0000 (Fri, 19 May
 * 2006) joco01 $ $Revision$ $Date: 2006-05-19 12:04:06 +0000 (Fri, 19
 * May 2006) $
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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import wicket.Application;
import wicket.markup.ComponentTag;
import wicket.markup.ContainerInfo;
import wicket.markup.MarkupElement;
import wicket.markup.parser.AbstractMarkupFilter;
import wicket.settings.IResourceSettings;

/**
 * THIS IS EXPERIMENTAL ONLY AND DISABLED BY DEFAULT
 * <p>
 * This is a markup inline filter. It identifies wicket:message attributes and
 * replaces the attributes referenced. E.g. wicket:message="value=key" would
 * replace or add the attribute "value" with the message associated with "key".
 * 
 * @author Juergen Donnerstag
 */
public final class WicketMessageTagHandler extends AbstractMarkupFilter
{
	/** Logging */
	// private final static Log log =
	// LogFactory.getLog(WicketMessageTagHandler.class);
	/** TODO Post 1.2: General: Namespace should not be a constant */
	private final static String WICKET_MESSAGE_ATTRIBUTE_NAME = "wicket:message";

	/**
	 * globally enable wicket:message; If accepted by user, we should use an
	 * apps setting
	 */
	public static boolean enable = false;

	/**
	 * The MarkupContainer requesting the information incl. class, locale and
	 * style
	 */
	private final ContainerInfo containerInfo;

	/** temporary storage unomdified while the object instance exists */
	private final List<Class> searchStack;

	/**
	 * The application settings required. Note: you can rely on
	 * Application.get().getResourceSettings() as reading the markup happens in
	 * another thread due to ModificationWatcher.
	 */
	private IResourceSettings settings;

	/**
	 * Construct.
	 * 
	 * @param containerInfo
	 *            The container requesting the current markup incl class, style
	 *            and locale
	 */
	public WicketMessageTagHandler(final ContainerInfo containerInfo)
	{
		this.containerInfo = containerInfo;
		this.settings = Application.get().getResourceSettings();

		this.searchStack = new ArrayList<Class>();
		searchStack.add(containerInfo.getContainerClass());
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
		final ComponentTag tag = nextComponentTag();
		if (tag == null)
		{
			return tag;
		}

		final String wicketMessageAttribute = tag.getAttributes().getString(
				WICKET_MESSAGE_ATTRIBUTE_NAME);
		if ((wicketMessageAttribute != null) && (wicketMessageAttribute.trim().length() > 0))
		{
			if (this.containerInfo == null)
			{
				throw new ParseException(
						"Found "
								+ WICKET_MESSAGE_ATTRIBUTE_NAME
								+ " but the message can not be resolved, because the associated Page is not known."
								+ " This might be caused by using the wrong MarkupParser constructor",
						tag.getPos());
			}

			final StringTokenizer attrTokenizer = new StringTokenizer(wicketMessageAttribute, ",");
			while (attrTokenizer.hasMoreTokens())
			{
				String text = attrTokenizer.nextToken().trim();
				if (text == null)
				{
					text = wicketMessageAttribute;
				}

				final StringTokenizer valueTokenizer = new StringTokenizer(text, "=");
				if (valueTokenizer.countTokens() != 2)
				{
					throw new ParseException("Wrong format of wicket:message attribute value. "
							+ text + "; Must be: key=value[, key=value]", tag.getPos());
				}

				final String attrName = valueTokenizer.nextToken();
				final String messageKey = valueTokenizer.nextToken();
				if ((attrName == null) || (attrName.trim().length() == 0) || (messageKey == null)
						|| (messageKey.trim().length() == 0))
				{
					throw new ParseException("Wrong format of wicket:message attribute value. "
							+ text + "; Must be: key=value[, key=value]", tag.getPos());
				}

				final String value = settings.getLocalizer().getString(messageKey, null, searchStack,
						containerInfo.getLocale(), containerInfo.getStyle());

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
