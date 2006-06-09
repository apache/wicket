package wicket.extensions.proxy.util;

/**
 * @see IObjectMethodTester
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class ObjectMethodTester implements IObjectMethodTester
{
	private boolean valid = true;

	/**
	 * Constructor
	 */
	public ObjectMethodTester()
	{
		valid = true;
	}

	/**
	 * @see wicket.extensions.proxy.util.IObjectMethodTester#isValid()
	 */
	public boolean isValid()
	{
		return valid;
	}

	/**
	 * @see wicket.extensions.proxy.util.IObjectMethodTester#reset()
	 */
	public void reset()
	{
		valid = true;
	}

	/**
	 * @see wicket.extensions.proxy.util.IObjectMethodTester#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		valid = false;
		return super.equals(obj);
	}

	/**
	 * @see wicket.extensions.proxy.util.IObjectMethodTester#hashCode()
	 */
	@Override
	public int hashCode()
	{
		valid = false;
		return super.hashCode();
	}

	/**
	 * @see wicket.extensions.proxy.util.IObjectMethodTester#toString()
	 */
	@Override
	public String toString()
	{
		valid = false;
		return super.toString();
	}

}
