package wicket.proxy.util;

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
	 * @see wicket.proxy.util.IObjectMethodTester#isValid()
	 */
	public boolean isValid()
	{
		return valid;
	}

	/**
	 * @see wicket.proxy.util.IObjectMethodTester#reset()
	 */
	public void reset()
	{
		valid = true;
	}

	/**
	 * @see wicket.proxy.util.IObjectMethodTester#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		valid = false;
		return super.equals(obj);
	}

	/**
	 * @see wicket.proxy.util.IObjectMethodTester#hashCode()
	 */
	public int hashCode()
	{
		valid = false;
		return super.hashCode();
	}

	/**
	 * @see wicket.proxy.util.IObjectMethodTester#toString()
	 */
	public String toString()
	{
		valid = false;
		return super.toString();
	}

}
