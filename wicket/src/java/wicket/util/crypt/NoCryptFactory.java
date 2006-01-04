package wicket.util.crypt;

import wicket.settings.ISecuritySettings;

/**
 * Crypt factory implementation for noop {@link NoCrypt} class
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class NoCryptFactory extends CryptFactoryCachingDecorator
{

	/**
	 * Construct.
	 */
	public NoCryptFactory()
	{
		super(new ClassCryptFactory(NoCrypt.class, ISecuritySettings.DEFAULT_ENCRYPTION_KEY));
	}

}