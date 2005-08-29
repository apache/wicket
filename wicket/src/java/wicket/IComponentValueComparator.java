/**
 * 
 */
package wicket;


/**
 * Implementation of this interface can be used in the Component.getComparator() for testing the 
 * current value of the components model data with the new value that is given.
 * 
 * @author jcompagner
 *
 */
public interface IComponentValueComparator
{
	/**
	 * @param component The component for which the compare must take place. 
	 * @param newObject The object to compare the current value to.
	 * @return true if the current component model value is the same as the newObject.
	 */
	boolean compareValue(Component component, Object newObject);
}
