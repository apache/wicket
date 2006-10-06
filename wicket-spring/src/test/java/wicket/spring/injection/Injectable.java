/*
 * $Id: Injectable.java 3481 2005-12-23 23:01:34 +0000 (Fri, 23 Dec 2005) ivaynberg $
 * $Revision: 3481 $
 * $Date: 2005-12-23 23:01:34 +0000 (Fri, 23 Dec 2005) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.spring.injection;

import wicket.spring.Bean2;

/**
 * Mock for an object with some SpringBean annotations
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class Injectable
{
	private Bean nobean;

	@SpringBean
	private Bean beanByClass;

	@SpringBean(id = "somebean")
	private Bean2 beanByName;

	/**
	 * @return bean of specified class
	 */
	public Bean getBeanByClass()
	{
		return beanByClass;
	}

	/**
	 * @return bean of specified name
	 */
	public Bean2 getBeanByName()
	{
		return beanByName;
	}

	/**
	 * @return no bean
	 */
	public Bean getNobean()
	{
		return nobean;
	}

}
