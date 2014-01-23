package org.apache.wicket;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupElement;

/**
 *
 */
class ComponentTreeBuilder
{
	private static final String USER_DATA_CURSOR_CHANGE = "cursorChanged";

	void rebuild(Page page)
	{
		MarkupContainer cursor = page;

		Deque<MarkupContainer> cursors = new ArrayDeque<>();

		IMarkupFragment markup = page.getMarkup();
		Iterator<MarkupElement> markupElementIterator = markup.iterator();
		while (markupElementIterator.hasNext())
		{
			MarkupElement element = markupElementIterator.next();

			if (element instanceof ComponentTag)
			{
				ComponentTag tag = (ComponentTag) element;

				if (!tag.isAutoComponentTag() && (tag.isOpen() || tag.isOpenClose()))
				{
					String componentId = tag.getId();
					Component component = cursor.get(componentId);
					if (component == null)
					{
						try
						{
							component = findAutoAnnotatedComponent(cursor, componentId);

							if (component != null)
							{
								cursor.add(component);

								if (component instanceof MarkupContainer)
								{
									tag.setUserData(USER_DATA_CURSOR_CHANGE, Boolean.TRUE);
									cursors.add(cursor);
									cursor = (MarkupContainer) component;
								}
							}

						} catch (Exception e)
						{
							throw new WicketRuntimeException(e);
						}
					}

					print(tag);
				}
				else if (tag.isClose())
				{
					ComponentTag openTag = tag.getOpenTag();
					Object cursorChanged = openTag.getUserData(USER_DATA_CURSOR_CHANGE);
					if (cursorChanged != null)
					{
						cursor = cursors.pop();
					}
				}
			}

		}
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
