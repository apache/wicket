package org.apache.wicket;

import java.lang.reflect.Field;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkup;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupStream;

/**
 *
 */
class ComponentTreeBuilder
{
	void rebuild(final MarkupContainer container)
	{
		IMarkupFragment markup = getMarkup(container);

		if (markup != null && markup.size() > 1)
		{
			MarkupStream stream = new MarkupStream(markup);

			// Skip the first component tag which already belongs to 'this' container
			if (stream.skipUntil(ComponentTag.class))
			{
				stream.next();
			}

			while (stream.skipUntil(ComponentTag.class))
			{
				ComponentTag tag = stream.getTag();
				if (!tag.isAutoComponentTag() && (tag.isOpen() || tag.isOpenClose()))
				{
					String componentId = tag.getId();
					Component component = container.get(componentId);
					if (component == null)
					{
						try
						{
							component = findAutoAnnotatedComponent(container, componentId);

							if (component != null)
							{
								container.add(component);
							}

						} catch (Exception e)
						{
							throw new WicketRuntimeException(e);
						}
					}

					print(tag);
				}

				if (tag.isOpen())
				{
					stream.skipToMatchingCloseTag(tag);
				}

				stream.next();
			}

		}
	}

	/**
	 * Find the markup of the container.
	 * If there is associated markup (Panel, Border) then it is preferred.
	 *
	 * @param container
	 *              The container which markup should be get
	 * @return the container's markup
	 */
	private IMarkupFragment getMarkup(MarkupContainer container)
	{
		IMarkupFragment markup = container.getAssociatedMarkup();
		if (markup == null)
		{
			markup = container.getMarkup();
		}
		return markup;
	}

	/**
	 * Searches for a member field that is a Component with the expected component id
	 *
	 * @param cursor
	 * @param componentId
	 * @return
	 * @throws IllegalAccessException
	 */
	private Component findAutoAnnotatedComponent(MarkupContainer cursor, String componentId) throws IllegalAccessException
	{
		if (cursor == null)
		{
			return null;
		}

		Class<?> cursorClass = cursor.getClass();

		while (cursorClass != null)
		{
			for (Field field : cursorClass.getDeclaredFields())
			{
				Auto annotation = field.getAnnotation(Auto.class);
				if (annotation != null)
				{
					String annId = annotation.id();
					if (componentId.equals(annId) || componentId.equals(field.getName()))
					{
						boolean accessible = field.isAccessible();
						try
						{
							field.setAccessible(true);
							return (Component) field.get(cursor);
						}
						finally
						{
							field.setAccessible(accessible);
						}

					}
				}
			}

			cursorClass = cursorClass.getSuperclass();
		}

		return findAutoAnnotatedComponent(cursor.getParent(), componentId);
	}

	private void print(ComponentTag tag)
	{
		System.err.println("tag: id=" + tag.getId() +
				"\n\t\topen=" + tag.isOpen() +
				"\n\t\tclose=" + tag.isClose() +
				"\n\t\topenclose=" + tag.isOpenClose() +
				"\n\t\tautoLinkEnabled=" + tag.isAutolinkEnabled() +
				"\n\t\tauto=" + tag.isAutoComponentTag());

	}
}
