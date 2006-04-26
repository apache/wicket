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
import java.util.HashMap;
import java.util.Map;

/**
 * Transitions between steps.
 * 
 * @author Eelco Hillenius
 */
public class Transitions implements Serializable
{
	/** transitions. */
	private Map/* <Step,Map<TransitionResult,Transition>> */transitions = new HashMap();

	/** the first node of the wizard. */
	private final Node first;

	/**
	 * Construct.
	 * 
	 * @param first
	 *            the first step of the wizard
	 */
	public Transitions(Node first)
	{
		this.first = first;
	}

	/**
	 * Adds a transition.
	 * 
	 * @param node
	 *            the node
	 * @param transition
	 *            the transition to add
	 */
	public void put(Node node, Transition transition)
	{
		Map/* <TransitionLabel,Transition> */m = (Map)transitions.get(node);
		if (m == null)
		{
			m = new HashMap();
			transitions.put(node, m);
		}
		m.put(transition.getLabel(), transition);
	}

	/**
	 * Gets a transition.
	 * 
	 * @param node
	 *            the node
	 * @param label
	 *            for the label
	 * @return the found transition or null if not found
	 */
	public Transition get(Node node, TransitionLabel label)
	{
		Map/* <TransitionLabel,Transition> */m = (Map)transitions.get(node);
		if (m != null)
		{
			return (Transition)m.get(label);
		}

		return null;
	}

	/**
	 * Returns whether any transition exists for the given node and transition
	 * label.
	 * 
	 * @param node
	 *            the node
	 * @param label
	 *            the transition label
	 * @return true if it does exist, false otherwise
	 */
	public boolean exists(Node node, TransitionLabel label)
	{
		return (get(node, label) != null);
	}

	/**
	 * Gets the first node.
	 * 
	 * @return the first node
	 */
	public Node getFirst()
	{
		return first;
	}
}
