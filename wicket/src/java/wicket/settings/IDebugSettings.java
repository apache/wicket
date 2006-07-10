package wicket.settings;

/**
 * Settings interface for various debug settings
 * <p>
 * <i>componentUseCheck </i> (defaults to true) - Causes the framework to do a
 * check after rendering each page to ensure that each component was used in
 * rendering the markup. If components are found that are not referenced in the
 * markup, an appropriate error will be displayed <i>serializeSessionAttributes</i>
 * (defaults to true in devel mode) - Causes the framework to serialize any
 * attribute put into session - this helps find Not Serializable errors early
 * <p>
 * 
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public interface IDebugSettings
{
	/**
	 * @return true if componentUseCheck is enabled
	 */
	boolean getComponentUseCheck();

	/**
	 * Sets componentUseCheck debug settings
	 * 
	 * @param check
	 */
	void setComponentUseCheck(boolean check);

	/**
	 * Enables or disables ajax debug mode. See {@link IAjaxSettings} for
	 * details
	 * 
	 * @param enable
	 * 
	 */
	void setAjaxDebugModeEnabled(boolean enable);

	/**
	 * Returns status of ajax debug mode. See {@link IAjaxSettings} for details
	 * 
	 * @return true if ajax debug mode is enabled, false otherwise
	 * 
	 */
	boolean isAjaxDebugModeEnabled();

	/**
	 * Sets the seriaalize session attributes setting
	 * 
	 * @param serialize
	 */
	void setSerializeSessionAttributes(boolean serialize);

	/**
	 * @return true if serialize session attributes is enabled, false otherwise
	 */
	boolean getSerializeSessionAttributes();

}
