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
 * Holds a reference to the injector that will be used to automatically
 * initialize components that are used. Usually the application class should set
 * the injector in the holder when it initializes.
 * <p>
 * This class can be used for unit-testing to switch the standard injector with
 * an injector that will lookup dependencies from a mock application context.
 * <p>
 * 
 * <pre>
 * class MockSpringInjector extends SpringInjector
 * {
 * 	protected ISpringContextLocator getContextLocator()
 * 	{
 * 		return new MockContextLocator();
 * 	}
 * }
 * 
 * InjectorHolder.setInjector(new MockSpringInjector());
 * 
 * //from this point on InjectableWebPage and InjectablePanel
 * //will be injected using the MockSpringInjector 
 * </pre>
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 * TODO shouldn't we move this class to wicket.extensions.injection ?
 */
public class InjectorHolder
{
	private static ConfigurableInjector injector = null;

	/**
	 * Gets an injector
	 * 
	 * NOTICE this method is not thread safe if setInjector() is used
	 * 
	 * @return injector
	 */
	public static ConfigurableInjector getInjector()
	{
		if (injector == null)
		{
			throw new IllegalStateException(
					"InjectorHolder has not been assigned an injector. "
							+ "Use InjectorHolder.setInjector() to assign an injector. "
							+ "In most cases this should be done once inside "
							+ "SpringWebApplication subclass's init() method.");
		}
		return injector;
	}

	/**
	 * Sets an injector
	 * 
	 * NOTICE this method is not thread safe.
	 * 
	 * @param newInjector
	 *            new injector
	 */
	public static void setInjector(ConfigurableInjector newInjector)
	{
		injector = newInjector;
	}

}
