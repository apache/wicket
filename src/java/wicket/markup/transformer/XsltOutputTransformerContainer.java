/*
 * $Id: OutputTransformerContainer.java,v 1.1 2005/12/31 10:09:31 jdonnerstag
 * Exp $ $Revision$ $Date$
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
package wicket.markup.transformer;

import wicket.Component;
import wicket.model.IModel;

/**
 * A container which output markup will be processes by a XSLT processor prior
 * to writing the output into the web response. The *.xsl resource must be
 * located in the same path as the nearest parent with an associated markup and
 * must have a filename equal to the component's id.
 * <p>
 * Similar to this container, a <code>IBehavior</code> is available which
 * does the same, but does not require an additional Container.
 * 
 * @see wicket.markup.transformer.XsltTransfomerBehaviour
 * 
 * @author Juergen Donnerstag
 */
public class XsltOutputTransformerContainer extends AbstractOutputTransformerContainer
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct
	 * 
	 * @see wicket.Component#Component(String)
	 */
	public XsltOutputTransformerContainer(final String id)
	{
		super(id);
	}

	/**
	 * Construct
	 * 
	 * @see wicket.Component#Component(String, IModel)
	 */
	public XsltOutputTransformerContainer(final String id, final IModel model)
	{
		super(id, model);
	}

	/**
	 * 
	 * @see wicket.MarkupContainer#getMarkupType()
	 */
	public String getMarkupType()
	{
		return "xsl";
	}

	/**
	 * 
	 * @see wicket.markup.transformer.ITransformer#transform(wicket.Component,
	 *      java.lang.String)
	 */
	public CharSequence transform(final Component component, final String output) throws Exception
	{
		return new XsltTransformer().transform(component, output);
	}
}
