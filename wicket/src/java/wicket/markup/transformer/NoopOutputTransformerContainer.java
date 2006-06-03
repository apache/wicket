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
import wicket.MarkupContainer;
import wicket.model.IModel;

/**
 * An implementation of an output transformer which does nothing. It does not
 * modify the markup at all.
 * 
 * @author Juergen Donnerstag
 * @param <T> 
 */
public class NoopOutputTransformerContainer<T> extends AbstractOutputTransformerContainer<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct
	 * 
	 * @see wicket.Component#Component(MarkupContainer,String)
	 */
	public NoopOutputTransformerContainer(MarkupContainer parent, final String id)
	{
		super(parent, id);
	}

	/**
	 * Construct
	 * 
	 * @see wicket.Component#Component(MarkupContainer,String, IModel)
	 */
	public NoopOutputTransformerContainer(MarkupContainer parent, final String id,
			final IModel<T> model)
	{
		super(parent, id, model);
	}

	/**
	 * 
	 * @see wicket.markup.transformer.ITransformer#transform(wicket.Component,
	 *      java.lang.String)
	 */
	@Override
	public CharSequence transform(final Component component, final String output)
	{
		return output;
	}
}
