package wicket.feedback;

/**
 * Filter for accepting feedback messages of a certain error level.
 * 
 * @author Jonathan Locke
 */
public class ErrorLevelFeedbackMessageFilter implements IFeedbackMessageFilter
{
	/** The minimum error level */
	private final int minimumErrorLevel;

	/**
	 * Constructor
	 * 
	 * @param minimumErrorLevel
	 *            The component to filter on
	 */
	public ErrorLevelFeedbackMessageFilter(int minimumErrorLevel)
	{
		this.minimumErrorLevel = minimumErrorLevel;
	}

	/**
	 * @see wicket.feedback.IFeedbackMessageFilter#accept(wicket.feedback.FeedbackMessage)
	 */
	public boolean accept(FeedbackMessage message)
	{
		return message.isLevel(minimumErrorLevel);
	}
}
