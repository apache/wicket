package wicket.markup.html.panel;

import wicket.Component;
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
	 * @param id the component id.
	 * @param filter the component for which the messages need to be filtered.
	 */
	public ComponentFeedbackPanel(String id, Component filter)
	{
		super(id, new ComponentFeedbackMessageFilter(filter));
	}
}
