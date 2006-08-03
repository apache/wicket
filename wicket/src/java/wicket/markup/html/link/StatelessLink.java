/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) eelco12 $
 * $Revision: 5004 $
 * $Date: 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) $
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
package wicket.markup.html.link;

import wicket.MarkupContainer;

/**
 * This link is stateless that means that the url to this link could generate
 * a new page before the link onClick is called. Because of this you can't depend
 * on model data in the onClick method.
 * 
 * This Link component is the same as a normal link with the statelesshint to true.
 * 
 * @author jcompagner
 */
public abstract class StatelessLink extends Link
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * @param parent
	 * @param id
	 */
	public StatelessLink(MarkupContainer parent, String id)
	{
		super(parent, id);
	}
	
	

	@Override
	protected boolean getStatelessHint()
	{
		return true;
	}
}
