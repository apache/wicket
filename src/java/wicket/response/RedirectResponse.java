/**
 * 
 */
package wicket.response;

/**
 * @author jcompagner
 *
 */
public class RedirectResponse extends StringResponse
{
	private String redirectUrl;
	
	private String mimeType;

	/**
	 * @param redirectUrl
	 */
	public RedirectResponse(String redirectUrl)
	{
		this.redirectUrl = redirectUrl;
	}
	
	/**
	 * @return The redirect url that is used for this response
	 */
	public String getRedirectUrl()
	{
		return redirectUrl;
	}
	
	/**
	 * @return The content length of this redirect response
	 */
	public int getContentLength()
	{
		return this.out.getBuffer().length();
	}

	/**
	 * @return The content type of this redirect response
	 * 
	 */
	public String getContentType()
	{
		return this.mimeType;
	}
	

	/**
	 * @see wicket.Response#setContentType(java.lang.String)
	 */
	public void setContentType(String mimeType)
	{
		this.mimeType = mimeType;
	}
}
