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
import java.util.Stack;

import wicket.Application;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupElement;
import wicket.markup.html.internal.Enclosure;
import wicket.markup.parser.AbstractMarkupFilter;
import wicket.markup.resolver.EnclosureResolver;

/**
 * THIS IS EXPERIMENTAL ONLY AND DISABLED BY DEFAULT
 * <p>
 * This is a markup inline filter. It identifies &lt;wicket:enclosure&gt; tags.
 * If the 'child' attribute is empty it determines the wicket:id of the child
 * component automatically by analysing the only wicket component allowed in
 * between the open and close tag. If the enclosure tag has a 'child' attribute
 * like <code>&lt;wicket:enclosure child="xxx"&gt;</code> than more than just
 * one wicket component inside the enclosure tags is allowed and the child
 * component which determines the visibility of the enclosure is identified by
 * the 'child' attribute value which must be equal to the child id.
 * <p>
 * This handler is no Wicket default handler and must be added manually such as
 * <pre>
 * 		this.application.getMarkupSettings().setMarkupParserFactory(new MarkupParserFactory()
 *		{
 *			@Override
 *			public MarkupParser newMarkupParser(MarkupResourceStream resource)
 *			{
 *				MarkupParser parser = super.newMarkupParser(resource);
 *				// register the additional EnclosureHandler
 *				parser.registerMarkupFilter(new EnclosureHandler(application));
 *				return parser;
 *			}
 *		});
 * </pre>
 * 
 * @see EnclosureResolver
 * @see Enclosure
 * 
 * @author Juergen Donnerstag
 */
public final class EnclosureHandler extends AbstractMarkupFilter
{
	/** The child attribute */
	public static final String CHILD_ATTRIBUTE = "child";

	static
	{
		// register "wicket:enclosure"
		WicketTagIdentifier.registerWellKnownTagName("enclosure");
	}

	/** Stack of <wicket:enclosure> tags */
	private Stack<ComponentTag> stack;

	/** The id of the first wicket tag inside the enclosure */
	private String childId;

	/**
	 * Construct.
	 * 
	 * @param application
	 *            The Wicket application object
	 */
	public EnclosureHandler(final Application application)
	{
		application.getPageSettings().addComponentResolver(new EnclosureResolver());
	}

	/**
	 * @see wicket.markup.parser.IMarkupFilter#nextTag()
	 */
	public final MarkupElement nextTag() throws ParseException
	{
		// Get the next tag from the next MarkupFilter in the chain.
		// If null, no more tags are available
		final ComponentTag tag = nextComponentTag();
		if (tag == null)
		{
			return tag;
		}

		// If wicket:enclosure
		if (tag.isEnclosureTag())
		{
			// If open tag, than put the tag onto the stack
			if (tag.isOpen())
			{
				if (this.stack == null)
				{
					this.stack = new Stack<ComponentTag>();
				}
				this.stack.push(tag);
			}
			// If close tag, than remove the tag from the stack and update
			// the child attribute of the open tag if required
			else if (tag.isClose())
			{
				if (this.stack == null)
				{
					throw new ParseException("Missing open tag for Enclosure: " + tag.toString(),
							tag.getPos());
				}

				// Remove the open tag from the stack
				ComponentTag lastEnclosure = this.stack.pop();

				// If the child attribute has not been given by the user,
				// than ...
				if (this.childId != null)
				{
					lastEnclosure.put(CHILD_ATTRIBUTE, this.childId);
					lastEnclosure.setModified(true);
					this.childId = null;
				}

				if (this.stack.size() == 0)
				{
					this.stack = null;
				}
			}
			else
			{
				throw new ParseException("Open-close tag not allowed for Enclosure: "
						+ tag.toString(), tag.getPos());
			}
		}
		// Are we inside a wicket:enclosure tag?
		else if ((tag.getId() != null) && (tag.isWicketTag() == false) && (stack != null))
		{
			ComponentTag lastEnclosure = this.stack.lastElement();

			// If the enclosure tag has NO child attribute, than ...
			if (lastEnclosure.getString(CHILD_ATTRIBUTE) == null)
			{
				// We encountered more than one child component inside
				// the enclosure and are not able to automatically
				// determine the child component to delegate the
				// isVisible() to => Exception
				if (this.childId != null)
				{
					throw new ParseException(
							"Use <wicket:enclosure child='xxx'> to name the child component", tag
									.getPos());
				}
				// Remember the child id. The open tag will be updated
				// once the close tag is found. See above.
				this.childId = tag.getId();
			}
		}

		return tag;
	}
}
