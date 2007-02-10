/*
 * $Id$
 * $Revision$
 * $Date$
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
package wicket.spring.injection.cattr;

/**
 * Commons Attribute used to tag a field as a placeholder for a spring bean.
 * 
 * @author Karthik Gurumurthy
 */
public class SpringBean {

	/** the name of the bean. */
	private String name;

	/**
	 * Construct.
	 * 
	 * @param name
	 *            the name of the bean
	 */
	public SpringBean(String name) {
		this.name = name;
	}

	/**
	 * Gets the name of the bean.
	 * 
	 * @return the name of the bean
	 */
	public String getName() {
		return name;
	}

}
