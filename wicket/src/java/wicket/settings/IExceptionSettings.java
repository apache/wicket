package wicket.settings;



/**
 * Settings interface for configuring exception handling related settings.
 * <p>
 * <i>unexpectedExceptionDisplay </i> (defaults to SHOW_EXCEPTION_PAGE) -
 * Determines how exceptions are displayed to the developer or user
 * <p>
 * <i>throwExceptionOnMissingResource </i> (defaults to true) - Set to true to
 * throw a runtime exception if a required string resource is not found. Set to
 * false to return the requested resource key surrounded by pairs of question
 * mark characters (e.g. "??missingKey??")
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public interface IExceptionSettings
{
	/**
	 * Enumerated type for different ways of displaying unexpected exceptions.
	 */
	public static enum UnexpectedExceptionDisplay
	{
		/**
		 * Indicates that an exception page appropriate to development should be
		 * shown when an unexpected exception is thrown.
		 */
		SHOW_EXCEPTION_PAGE,
		/**
		 * Indicates a generic internal error page should be shown when an
		 * unexpected exception is thrown.
		 */
		SHOW_INTERNAL_ERROR_PAGE,
		/**
		 * Indicates that no exception page should be shown when an unexpected
		 * exception is thrown.
		 */
		SHOW_NO_EXCEPTION_PAGE
	}

	/**
	 * @return Whether to throw an exception when a missing resource is
	 *         requested
	 */
	boolean getThrowExceptionOnMissingResource();

	/**
	 * @return Returns the unexpectedExceptionDisplay.
	 */
	UnexpectedExceptionDisplay getUnexpectedExceptionDisplay();

	/**
	 * @param throwExceptionOnMissingResource
	 *            Whether to throw an exception when a missing resource is
	 *            requested
	 */
	void setThrowExceptionOnMissingResource(final boolean throwExceptionOnMissingResource);

	/**
	 * The exception display type determines how the framework displays
	 * exceptions to you as a developer or user.
	 * <p>
	 * The default value for exception display type is SHOW_EXCEPTION_PAGE. When
	 * this value is set and an unhandled runtime exception is thrown by a page,
	 * a redirect to a helpful exception display page will occur.
	 * <p>
	 * This is a developer feature, however, and you may want to instead show an
	 * internal error page without developer details that allows a user to start
	 * over at the application's home page. This can be accomplished by setting
	 * the exception display type to SHOW_INTERNAL_ERROR_PAGE.
	 * <p>
	 * Finally, if you are having trouble with the exception display pages
	 * themselves, you can disable exception displaying entirely with the value
	 * SHOW_NO_EXCEPTION_PAGE. This will cause the framework to re-throw any
	 * unhandled runtime exceptions after wrapping them in a ServletException
	 * wrapper.
	 * 
	 * @param unexpectedExceptionDisplay
	 *            The unexpectedExceptionDisplay to set.
	 */
	void setUnexpectedExceptionDisplay(UnexpectedExceptionDisplay unexpectedExceptionDisplay);
}
