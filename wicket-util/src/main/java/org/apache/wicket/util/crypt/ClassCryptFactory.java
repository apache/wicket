/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.util.crypt;

import java.lang.ref.WeakReference;

import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Crypt factory that creates the {@link ICrypt} object by instantiating a provided class. The class
 * must implement {@link ICrypt}.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class ClassCryptFactory implements ICryptFactory
{
	private static final Logger log = LoggerFactory.getLogger(ClassCryptFactory.class);

	private final WeakReference<Class<?>> cryptClass;
	private final String encryptionKey;

	/**
	 * Construct.
	 * 
	 * @param cryptClass
	 *            class that will be instantiated to represent the ICrypt object
	 * @param encryptionKey
	 *            encryption key
	 */
	public ClassCryptFactory(final Class<?> cryptClass, final String encryptionKey)
	{
		Args.notNull(cryptClass, "cryptClass");

		if (!ICrypt.class.isAssignableFrom(cryptClass))
		{
			throw new IllegalArgumentException("cryptClass must implement ICrypt interface");
		}

		this.cryptClass = new WeakReference<Class<?>>(cryptClass);
		this.encryptionKey = encryptionKey;
	}

	/**
	 * @see org.apache.wicket.util.crypt.ICryptFactory#newCrypt()
	 */
	@Override
	public ICrypt newCrypt()
	{
		try
		{
			ICrypt crypt = (ICrypt)(cryptClass.get()).newInstance();
			log.info("using encryption/decryption object " + crypt);
			crypt.setKey(encryptionKey);
			return crypt;
		}
		catch (Exception e)
		{
			log.warn("************************** WARNING **************************");
			log.warn("As the instantion of encryption/decryption class:");
			log.warn("\t" + cryptClass);
			log.warn("failed, Wicket will fallback on a dummy implementation");
			log.warn("\t(" + NoCrypt.class.getName() + ")");
			log.warn("This is not recommended for production systems.");
			log.warn("Please override method org.apache.wicket.Application.newCrypt()");
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
