package wicket.request;

import wicket.PageParameters;
import wicket.Request;

/**
 * Encoder responsible for encoding and decoding <code>PageParameters</code>
 * object to and from a url fragment
 * 
 * Url fragment is usually the part of url after the request path.
 * 
 * @see PageParameters
 * @see Request#getPath()
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public interface IPageParamsEncoder
{
	/**
	 * Encodes PageParameters into a url fragment
	 * 
	 * @param parameters
	 *            PageParameters object to be encoded
	 * @return url fragment that represents the parameters
	 */
	String encode(PageParameters parameters);

	/**
	 * Decodes PageParameters object from the provided url fragment
	 * 
	 * @param urlFragment
	 * @return PageParameters object created from the url fragment
	 */
	PageParameters decode(String urlFragment);
}
