package wicket.feedback;

import wicket.MarkupContainer;

/**
 * Filter for child-of relationship
 * 
 * @author Jonathan Locke
 */
public class ContainedByFeedbackMessageFilter implements IFeedbackMessageFilter
{
	private final MarkupContainer container;

	/**
	 * Constructor
	 * 
	 * @param container
	 *            The container that message reporters must be a child of
	 */
	public ContainedByFeedbackMessageFilter(MarkupContainer container)
	{
		this.container = container;
	}

	/**
	 * @see wicket.feedback.IFeedbackMessageFilter#accept(wicket.feedback.FeedbackMessage)
	 */
	public boolean accept(FeedbackMessage message)
	{
		return container.isAncestorOf(message.getReporter());
	}
}
