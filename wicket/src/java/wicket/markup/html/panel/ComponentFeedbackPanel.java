package wicket.markup.html.panel;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.feedback.ComponentFeedbackMessageFilter;

/**
 * Convenience feedback panel that filters the feedback messages based on the
 * component given in the constructor.
 * 
 * @author Martijn Dashorst
 * @author Igor Vaynberg
 */
public class ComponentFeedbackPanel extends FeedbackPanel
{
	/** For serialization. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            the component id.
	 * @param filter
	 *            the component for which the messages need to be filtered.
	 */
	public ComponentFeedbackPanel(MarkupContainer parent, String id, Component filter)
	{
		super(parent, id, new ComponentFeedbackMessageFilter(filter));
	}
}
