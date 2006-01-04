package wicket.settings;

/**
 * Settings interface for various debug settings
 * <p>
 * <i>componentUseCheck </i> (defaults to true) - Causes the framework to do a
 * check after rendering each page to ensure that each component was used in
 * rendering the markup. If components are found that are not referenced in the
 * markup, an appropriate error will be displayed
 * <p>
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public interface IDebugSettings
{

	/**
	 * Sets componentUseCheck debug settings
	 * 
	 * @param check
	 */
	void setComponentUseCheck(boolean check);

	/**
	 * @return true if componentUseCheck is enabled
	 */
	boolean getComponentUseCheck();
}
