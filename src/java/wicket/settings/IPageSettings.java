package wicket.settings;


/**
 * Interface for page related settings. *
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public interface IPageSettings
{

	/**
	 * @return Returns the maxPageVersions.
	 */
	int getMaxPageVersions();

	/**
	 * @return Returns the pagesVersionedByDefault.
	 */
	boolean getVersionPagesByDefault();

	/**
	 * @param maxPageVersions
	 *            The maxPageVersion to set.
	 */
	void setMaxPageVersions(int maxPageVersions);

	/**
	 * @param pagesVersionedByDefault
	 *            The pagesVersionedByDefault to set.
	 */
	void setVersionPagesByDefault(boolean pagesVersionedByDefault);



}