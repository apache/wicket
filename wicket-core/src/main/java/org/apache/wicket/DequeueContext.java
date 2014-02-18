package org.apache.wicket;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.util.collections.ArrayListStack;

/**
 * Context for component dequeueing. Keeps track of markup position and container stack.
 * 
 * @author igor
 *
 */
public final class DequeueContext
{
	private final IMarkupFragment markup;
	private int index;
	private ComponentTag next;
	private ArrayListStack<ComponentTag> tags = new ArrayListStack<>();

	private ArrayListStack<MarkupContainer> containers = new ArrayListStack<>();

	/** A bookmark for the DequeueContext stack */
	public static final class Bookmark
	{
		private final int index;
		private final ComponentTag next;
		private final ArrayListStack<ComponentTag> tags;
		private final ArrayListStack<MarkupContainer> containers;

		private Bookmark(DequeueContext parser)
		{
			this.index = parser.index;
			this.next = parser.next;
			this.tags = new ArrayListStack<>(parser.tags);
			this.containers = new ArrayListStack<>(parser.containers);
		}

		private void restore(DequeueContext parser)
		{
			parser.index = index;
			parser.next = next;
			parser.tags = new ArrayListStack<>(tags);
			parser.containers = new ArrayListStack<>(containers);
		}
	}
	
	public DequeueContext(IMarkupFragment markup, MarkupContainer root)
	{
		this.markup = markup;
		containers.push(root);
		next=nextTag();
	}
	
	/**
	 * Saves the state of the context into a bookmark which can later be used to restore it.
	 */
	public Bookmark save()
	{
		return new Bookmark(this);
	}

	/**
	 * Restores the state of the context from the bookmark
	 * 
	 * @param bookmark
	 */
	public void restore(Bookmark bookmark)
	{
		bookmark.restore(this);
	}

	/**
	 * Peeks markup tag that would be retrieved by call to {@link #popTag()}
	 * 
	 * @return
	 */
	public ComponentTag peekTag()
	{
		return next;
	}
	
	/**
	 * Retrieves the next markup tag
	 * 
	 * @return
	 */
	public ComponentTag popTag()
	{
		ComponentTag taken = next;
		tags.push(taken);
		next = nextTag();
		return taken;
	}
	
	/**
	 * Skips to the closing tag of the tag retrieved from last call to {@link #popTag()}
	 */
	public void skipToCloseTag()
	{
		if (tags.peek().isOpen())
		{
			while (!next.closes(tags.peek()))
			{
				next = nextTag();
			}
			tags.pop();
		}
	}
	
	private ComponentTag nextTag()
	{
		for (; index < markup.size(); index++)
		{
			MarkupElement element = markup.get(index);
			if (element instanceof ComponentTag)
			{
				ComponentTag tag = (ComponentTag)element;
				ComponentTag open = tag.isClose() ? tag.getOpenTag() : tag;
				if (canDequeueTag(open))
				{
					index++;
					return tag;
				}
			}
		}
		return null;
	}
	
	private boolean canDequeueTag(ComponentTag open)
	{
		if (containers.size() < 1)
		{
			// TODO queueing message: called too early
			throw new IllegalStateException();
		}
		for (int i = containers.size() - 1; i >= 0; i--)
		{
			if (containers.get(i).canDequeueTag((open)))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the tag returned by {@link #peekTag()} is either open or open-close.
	 * 
	 * @return
	 */
	public boolean isAtOpenOrOpenCloseTag()
	{
		return peekTag() != null && (peekTag().isOpen() || peekTag().isOpenClose());
	}

	/**
	 * Retrieves the container on the top of the containers stack
	 * 
	 * @return
	 */
	public MarkupContainer peekContainer()
	{
		return containers.peek();
	}

	/**
	 * Pushes a container onto the container stack
	 * 
	 * @param container
	 */
	public void pushContainer(MarkupContainer container)
	{
		containers.push(container);
	}

	/**
	 * Pops a container from the container stack
	 * 
	 * @return
	 */
	public MarkupContainer popContainer()
	{
		return containers.pop();
	}

	/**
	 * Searches the container stack for a component that can be dequeud
	 * 
	 * @param tag
	 * @return
	 */
	public Component findComponentToDequeue(ComponentTag tag)
	{
		for (int j = containers.size() - 1; j >= 0; j--)
		{
			MarkupContainer container = containers.get(j);
			Component child = container.findComponentToDequeue(tag);
			if (child != null)
			{
				return child;
			}
		}
		return null;
	}

}
