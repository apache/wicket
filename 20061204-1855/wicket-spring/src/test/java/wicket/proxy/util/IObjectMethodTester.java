package wicket.proxy.util;

/**
 * Tester object that is valid as long as equals/hashCode/toString have not been
 * called on it.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public interface IObjectMethodTester
{

	/**
	 * @return true if object is valid, false otherwise
	 */
	boolean isValid();

	/**
	 * Resets state of object back to valid
	 */
	void reset();

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	boolean equals(Object obj);

	/**
	 * @see java.lang.Object#hashCode()
	 */
	int hashCode();

	/**
	 * @see java.lang.Object#toString()
	 */
	String toString();

}