package wicket.util.crypt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * Crypt factory that creates the {@link ICrypt} object by instantiating a
 * provided class. The class must implement {@link ICrypt}.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class ClassCryptFactory implements ICryptFactory
{
	private static final Log log = LogFactory.getLog(ClassCryptFactory.class);
	private Class cryptClass;
	private String encryptionKey;

	/**
	 * Construct.
	 * 
	 * @param cryptClass
	 *            class that will be instantiated to represent the ICrypt object
	 * @param encryptionKey
	 *            encryption key
	 */
	public ClassCryptFactory(Class cryptClass, String encryptionKey)
	{
		if (cryptClass == null)
		{
			throw new IllegalArgumentException("cryptClass cannot be null");
		}

		if (!ICrypt.class.isAssignableFrom(cryptClass))
		{
			throw new IllegalArgumentException("cryptClass must implement ICrypt interface");
		}

		this.cryptClass = cryptClass;
		this.encryptionKey = encryptionKey;
	}

	/**
	 * @see wicket.util.crypt.ICryptFactory#newCrypt()
	 */
	public ICrypt newCrypt()
	{
		try
		{
			ICrypt crypt = (ICrypt)cryptClass.newInstance();
			log.info("using encryption/decryption object " + crypt);
			crypt.setKey(encryptionKey);
			return crypt;
		}
		catch (Throwable e)
		{
			log.warn("************************** WARNING **************************");
			log.warn("As the instantion of encryption/decryption class:");
			log.warn("\t" + cryptClass);
			log.warn("failed, Wicket will fallback on a dummy implementation");
			log.warn("\t(" + NoCrypt.class.getName() + ")");
			log.warn("This is not recommended for production systems.");
			log.warn("Please override method wicket.Application.newCrypt()");
			log.warn("to provide a custom encryption/decryption implementation");
			log.warn("The cause of the instantion failure: ");
			log.warn("\t" + e.getMessage());
			if (log.isDebugEnabled())
			{
				log.debug("exception: ", e);
			}
			else
			{
				log.warn("set log level to DEBUG to display the stack trace.");
			}
			log.warn("*************************************************************");

			// assign the dummy crypt implementation
			return new NoCrypt();
		}
	}

}
