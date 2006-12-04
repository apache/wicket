/*
 * $Id$ $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.wizard.framework;

import wicket.util.lang.EnumeratedType;

/**
 * Type for transition labels.
 * 
 * @author Eelco Hillenius
 */
public class TransitionLabel extends EnumeratedType
{
	/** result for the next step. */
	public static final TransitionLabel NEXT = new TransitionLabel("next");

	/** result for the previous step. */
	public static final TransitionLabel PREVIOUS = new TransitionLabel("previous");

	/** result for the finishing step. */
	public static final TransitionLabel FINISH = new TransitionLabel("finish");

	/** result for staying at the current step. */
	public static final TransitionLabel CURRENT = null;

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public TransitionLabel(String name)
	{
		super(name);
	}
}
