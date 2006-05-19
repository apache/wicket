/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.util.convert;

import wicket.ICoverterLocator;

/**
 * Implementation of {@link wicket.util.convert.IConverterFactory}which creates
 * an instance of CoverterLocator in order to fulfill the IConverter contract.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public final class CoverterLocatorFactory implements ICoverterLocatorFactory
{
	/**
	 * @see wicket.util.convert.ICoverterLocatorFactory#newConverterSupplier()
	 */
	public ICoverterLocator newConverterSupplier()
	{
		return new CoverterLocator();
	}
}