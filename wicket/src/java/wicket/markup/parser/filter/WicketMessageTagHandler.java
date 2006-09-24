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

import wicket.Component;
import wicket.WicketRuntimeException;
import wicket.behavior.AbstractBehavior;
import wicket.behavior.IBehavior;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupElement;
import wicket.markup.parser.AbstractMarkupFilter;
import wicket.util.string.Strings;

/**
 * This is a markup inline filter. It identifies wicket:message attributes and
 * adds an attribute modifier to the component tag that can localize
 * wicket:message="attr-name:i18n-key,attr-name-2:i18n-key-2,..." expressions,
 * replacing values of attributes specified by attr-name with a localizer lookup
 * with key i18n-key. If an attribute being localized has a set value that value
 * will be used as the default value for the localization lookup.
 * 
 * @author Juergen Donnerstag
 * @author Igor Vaynberg
 */
public final class WicketMessageTagHandler extends AbstractMarkupFilter
{
	/** TODO Post 1.2: General: Namespace should not be a constant */
	private final static String WICKET_MESSAGE_ATTRIBUTE_NAME = "wicket:message";

	/**
	 * Construct.
	 */
	public WicketMessageTagHandler()
	{
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
			tag.addBehavior(ATTRIBUTE_LOCALIZER);
		}

		return tag;
	}

	/** singleton instance of {@link AttributeLocalizer} */
	private final IBehavior ATTRIBUTE_LOCALIZER = new AttributeLocalizer();

	/**
	 * Attribute localizing behavior. See the javadoc of
	 * {@link WicketMessageTagHandler} for details.
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 */
	private static class AttributeLocalizer extends AbstractBehavior
	{

		private static final long serialVersionUID = 1L;

		/**
		 * @see wicket.behavior.AbstractBehavior#onComponentTag(wicket.Component,
		 *      wicket.markup.ComponentTag)
		 */
		@Override
		public void onComponentTag(Component component, ComponentTag tag)
		{
			String expr = tag.getAttributes().getString(WICKET_MESSAGE_ATTRIBUTE_NAME);
			if (!Strings.isEmpty(expr))
			{
				expr = expr.trim();

				String[] attrsAndKeys = expr.split(",");

				for (String attrAndKey : attrsAndKeys)
				{
					int colon = attrAndKey.indexOf(":");
					// make sure the attribute-key pair is valid
					if (attrAndKey.length() < 3 || colon < 1 || colon > attrAndKey.length() - 2)
					{
						throw new WicketRuntimeException(
								"wicket:message attribute contains an invalid value [[" + expr
										+ "]], must be of form (attr:key)+");
					}

					String attr = attrAndKey.substring(0, colon);
					String key = attrAndKey.substring(colon + 1);

					String value;
					// we need to call the proper getString() method based on
					// whether or not we have a default value
					if (tag.getAttributes().containsKey(attr))
					{
						value = component.getString(key, null, tag.getAttributes().getKey(attr));
					}
					else
					{
						value = component.getString(key);
					}
					tag.put(attr, value);
				}
			}


		}
	}
}
