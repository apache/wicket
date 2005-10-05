package wicket.feedback;

import wicket.Component;

/**
 * Filter for accepting feedback messages for a particular component.
 * 
 * @author Jonathan Locke
 */
public class ComponentFeedbackMessageFilter implements IFeedbackMessageFilter
{
	/** The component to accept feedback messages for */
	private final Component component;

	/**
	 * Constructor
	 * 
	 * @param component
	 *            The component to filter on
	 */
	public ComponentFeedbackMessageFilter(Component component)
	{
		this.component = component;
	}

	/**
	 * @see wicket.feedback.IFeedbackMessageFilter#accept(wicket.feedback.FeedbackMessage)
	 */
	public boolean accept(FeedbackMessage message)
	{
		return component == message.getReporter();
	}
}
