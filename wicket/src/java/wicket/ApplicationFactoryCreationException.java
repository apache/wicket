package wicket;

/**
 * Thrown when the {@link wicket.protocol.http.IWebApplicationFactory} cannot
 * be created for some reason.
 * 
 * @author Seth Ladd
 */
public class ApplicationFactoryCreationException extends WicketRuntimeException
{

	/**
	 * Constructor.
	 * @param appFactoryClassName name of the application factory
	 * @param e the cause for the creation problem
	 */
	public ApplicationFactoryCreationException(String appFactoryClassName, Exception e)
	{
		super("Unable to create application factory of class "
				+ appFactoryClassName, e);
	}

}
