/*
 * $Id: WicketTagComponentResolver.java,v 1.4 2005/01/18 08:04:29 jonathanlocke
 * Exp $ $Revision$ $Date: 2006-09-25 09:36:44 +0200 (Mo, 25 Sep 2006) $
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
package wicket.markup.parser.onLoadListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupException;
import wicket.markup.MarkupFragment;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.parser.XmlTag;
import wicket.markup.parser.filter.WicketMessageTagHandler;
import wicket.markup.parser.filter.WicketTagIdentifier;

/**
 * This is a tag resolver which handles &lt;wicket:message
 * attr="myKey"&gt;Default Text&lt;/wicket:message&gt;. The resolver will
 * replace the whole tag with the message found in the properties file
 * associated with the Page. If no message is found, the default body text will
 * remain.
 * 
 * @author Juergen Donnerstag
 */
public class WicketMessageLoadListener extends AbstractMarkupLoadListener
{
	private static final Log log = LogFactory.getLog(WicketMessageLoadListener.class);

	private static final long serialVersionUID = 1L;

	static
	{
		// register "wicket:message"
		WicketTagIdentifier.registerWellKnownTagName("message");
	}

	/**
	 * 
	 * @see wicket.markup.parser.onLoadListener.AbstractMarkupLoadListener#visit(wicket.MarkupContainer,
	 *      wicket.markup.MarkupFragment)
	 */
	@Override
	protected Object visit(final MarkupContainer container, final MarkupFragment fragment)
	{
		final ComponentTag tag = fragment.getTag();
		if (tag.isMessageTag())
		{
			// this is a <wicket:message> tag
			String messageKey = tag.getAttributes().getString("key");
			if ((messageKey == null) || (messageKey.trim().length() == 0))
			{
				throw new MarkupException(
						"Wrong format of <wicket:message key='xxx'>: attribute 'key' is missing");
			}

			final String value = container.getApplication().getResourceSettings().getLocalizer()
					.getString(messageKey, container, "");

			Component component = null;
			if ((value != null) && (value.trim().length() > 0))
			{
				component = new MyLabel(container, tag.getId(), value);
			}
			else
			{
				log.info("No value found for message key: " + messageKey);
				component = new WebMarkupContainer(container, tag.getId());
			}

			component.setRenderBodyOnly(container.getApplication().getMarkupSettings()
					.getStripWicketTags());
		}
		else if (tag.getId().startsWith(WicketMessageTagHandler.WICKET_MESSAGE_CONTAINER_ID))
		{
			// this is a raw tag with wicket:message attribute, we need to
			// create a transparent auto container to stand in.
			MarkupContainer messageContainer = new WebMarkupContainer(container, tag.getId())
			{
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isTransparentResolver()
				{
					return true;
				}
			};

			// Attach an attribute modifier to localize the attribute
			messageContainer.add(WicketMessageTagHandler.ATTRIBUTE_LOCALIZER);
		}

		return MarkupFragment.IVisitor.CONTINUE_TRAVERSAL;
	}

	/**
	 * A Label with expands open-close tags to open-body-close if required
	 */
	public static class MyLabel extends Label
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param parent
		 * @param id
		 * @param value
		 */
		public MyLabel(MarkupContainer parent, final String id, final String value)
		{
			super(parent, id, value);
		}

		/**
		 * 
		 * @see wicket.Component#onComponentTag(wicket.markup.ComponentTag)
		 */
		@Override
		protected void onComponentTag(ComponentTag tag)
		{
			// Convert <wicket:message /> into
			// <wicket:message>...</wicket:message>
			if (tag.isOpenClose())
			{
				tag.setType(XmlTag.Type.OPEN);
			}
			super.onComponentTag(tag);
		}
	}
}