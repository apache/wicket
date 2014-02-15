package org.apache.wicket;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.util.collections.ArrayListStack;

public class DequeueContext
{
	private final IMarkupFragment markup;
	private int index;
	private ComponentTag next;
	private ArrayListStack<ComponentTag> tags = new ArrayListStack<>();
	private ArrayListStack<MarkupContainer> containers = new ArrayListStack<>();

	public static class Bookmark
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
			parser.tags = new ArrayListStack<ComponentTag>(tags);
			parser.containers = new ArrayListStack<MarkupContainer>(containers);
		}
	}
	
	public DequeueContext(IMarkupFragment markup, MarkupContainer root)
	{
		this.markup = markup;
		containers.push(root);
		next=nextTag();
	}
	
	public Bookmark save()
	{
		return new Bookmark(this);
	}

	public void restore(Bookmark bookmark)
	{
		bookmark.restore(this);
	}

	public ComponentTag peekTag()
	{
		return next;
	}
	
	public ComponentTag popTag()
	{
		ComponentTag taken=next;
		tags.push(taken);
		next=nextTag();
		return taken;
	}
	
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
				if (canDequeue(open))
				{
					index++;
					return tag;
				}
			}
		}
		return null;
	}
	
	private boolean canDequeue(ComponentTag open)
	{
		if (containers.size() < 1)
		{
			// TODO queueing message: called too early
			throw new IllegalStateException();
		}
		for (int i = containers.size() - 1; i >= 0; i--)
		{
			if (containers.get(i).supportsDequeueingFrom((open)))
			{
				return true;
			}
		}
		return false;
	}

	public boolean isAtOpenOrOpenCloseTag()
	{
		return peekTag() != null && (peekTag().isOpen() || peekTag().isOpenClose());
	}

	public MarkupContainer peekContainer()
	{
		return containers.peek();
	}

	public void pushContainer(MarkupContainer container)
	{
		containers.push(container);
	}

	public MarkupContainer popContainer()
	{
		return containers.pop();
	}

	public Component dequeue(ComponentTag tag)
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
