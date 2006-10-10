/*
 * $Id$ $Revision$ $Date$
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
package wicket.extensions.injection;

/**
 * Abstract injector that allows subclasses to provide IFieldValueFactory
 * pragmatically by implementing getFieldValueFactory(). Allows for injectors
 * that can be used with inject(Object obj) call instead of inject(Object obj,
 * IFieldValueFactory factory), thereby allowing for default factories.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class ConfigurableInjector extends Injector
{
	/**
	 * Injects proxies using IFieldValueFactory obtained by calling
	 * getFieldValueFactory() method
	 * 
	 * @param object
	 *            object to be injected
	 * @return Object that was injected - used for chainig
	 */
	public Object inject(Object object)
	{
		return inject(object, getFieldValueFactory());
	}

	/**
	 * Return the field value factory that will be used to inject objects
	 * 
	 * @return field value locator factory that will be used to inject objects
	 */
	abstract protected IFieldValueFactory getFieldValueFactory();
}
