/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
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
package wicket;

import java.io.Serializable;

import wicket.util.convert.ConverterLocator;
import wicket.util.convert.IConverter;

/**
 * A Class who implement this interface must return the right converter for the
 * given class type.
 * 
 * @author jcompagner
 * 
 * @see Component
 * @see Session
 * @see ConverterLocator
 */
public interface IConverterLocator extends Serializable
{

	/**
	 * Returns the Conveter for the class that is given.
	 * 
	 * @param type
	 * @return The converter for the given type.
	 */
	public IConverter getConverter(Class type);
}
