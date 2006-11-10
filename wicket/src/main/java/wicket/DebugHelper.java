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
package wicket;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component.IVisitor;
import wicket.markup.MarkupException;
import wicket.settings.IDebugSettings;
import wicket.util.value.Count;

/**
 * Class that consolidates and helps the debugging process for applications in
 * development mode.
 * 
 * @author ivaynberg
 */
public class DebugHelper
{
	private static Log log = LogFactory.getLog(DebugHelper.class);

	private Set<Component> renderedComponents;

	/**
	 * Resets the set of rendered components kept for component-use check.
	 * 
	 * @see IDebugSettings#setComponentUseCheck(boolean)
	 * 
	 */
	public void resetRenderedComponentsSet()
	{
		this.renderedComponents = null;
	}

	/**
	 * Listener method invoked by the framework when a component render is began
	 * 
	 * @param component
	 *            component being rendered
	 */
	public void onBeginComponentRender(Component component)
	{
		if (renderedComponents == null)
		{
			renderedComponents = new HashSet<Component>();
		}
		if (renderedComponents.add(component) == false)
		{
			throw new MarkupException(
					"The component "
							+ component
							+ " has the same wicket:id as another component already rendered at the same level");
		}
		if (log.isDebugEnabled())
		{
			log.debug("Rendered " + component);
		}
	}

	/**
	 * Listener method invoked by the framework when a component render is
	 * finished
	 * 
	 * @param component
	 *            component being rendered
	 */
	public void onEndComponentRender(Component component)
	{
		if (component instanceof MarkupContainer)
		{
			checkRendering((MarkupContainer)component);
		}
		else
		{
			renderedComponents = null;
		}
	}

	/**
	 * Throw an exception if not all components rendered.
	 * 
	 * @param container
	 *            The page itself if it was a full page render or the container
	 *            that was rendered standalone
	 */
	private void checkRendering(MarkupContainer container)
	{
		// If the application wants component uses checked and
		// the response is not a redirect
		final IDebugSettings debugSettings = Application.get().getDebugSettings();
		if (debugSettings.getComponentUseCheck() && !RequestCycle.get().getResponse().isRedirect())
		{
			final Count unrenderedComponents = new Count();
			final List<Component> unrenderedAutoComponents = new ArrayList<Component>();
			final StringBuffer buffer = new StringBuffer();
			container.visitChildren(new IVisitor()
			{
				public Object component(final Component component)
				{
					// If component never rendered
					if (renderedComponents == null || !renderedComponents.contains(component))
					{
						// If auto component ...
						if (component.isAuto())
						{
							// Add to list of unrendered auto components to
							// delete below
							unrenderedAutoComponents.add(component);
						}
						else if (component.isVisibleInHierarchy())
						{
							// Increase number of unrendered components
							unrenderedComponents.increment();

							// Add to explanatory string to buffer
							buffer.append(Integer.toString(unrenderedComponents.getCount()) + ". "
									+ component + "\n");
						}
						else
						{
							// if the component is not visible in hierarchy we
							// should not visit its children since they are also
							// not visible
							return CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
						}
					}
					return CONTINUE_TRAVERSAL;
				}
			});

			// Remove any unrendered auto components since versioning couldn't
			// do it. We can't remove the component in the above visitChildren
			// callback because we're traversing the list at that time.
			for (int i = 0; i < unrenderedAutoComponents.size(); i++)
			{
				unrenderedAutoComponents.get(i).remove();
			}

			// Throw exception if any errors were found
			if (unrenderedComponents.getCount() > 0)
			{
				// Get rid of set
				renderedComponents = null;

				// Throw exception
				throw new WicketRuntimeException(
						"The component(s) below failed to render. A common problem is that you have added a component in code but forgot to reference it in the markup (thus the component will never be rendered).\n\n"
								+ buffer.toString());
			}
		}

		// Get rid of set
		renderedComponents = null;

	}

}
