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

import java.io.Serializable;

/**
 * Transition command.
 * 
 * @author Eelco Hillenius
 */
public abstract class Transition implements Serializable
{
	/** for label. */
	private final TransitionLabel label;

	/**
	 * Construct.
	 * 
	 * @param label
	 */
	public Transition(TransitionLabel label)
	{
		this.label = label;
	}

	/**
	 * Gets the transition label.
	 * 
	 * @return transition label
	 */
	public TransitionLabel getLabel()
	{
		return label;
	}

	/**
	 * Gets the next node.
	 * 
	 * @param current
	 *            the current node
	 * @return nextStep
	 */
	public abstract Node next(Node current);
}
