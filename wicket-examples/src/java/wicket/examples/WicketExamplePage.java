/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples;

import wicket.markup.html.WebPage;
import wicket.model.IModel;
import wicket.protocol.http.request.urlcompressing.ICompressingUrlPage;
import wicket.protocol.http.request.urlcompressing.UrlCompressor;
import wicket.protocol.http.request.urlcompressing.WebURLCompressingCodingStrategy;
import wicket.protocol.http.request.urlcompressing.WebURLCompressingTargetResolverStrategy;
import wicket.util.string.Strings;

/**
 * Base class for all example pages.
 * 
 * @author Jonathan Locke
 */
public class WicketExamplePage extends WebPage implements ICompressingUrlPage
{
	private UrlCompressor compressor;
	
    /**
     * Constructor
     */
    public WicketExamplePage()
    {
		this(null);
	}

	/**
	 * Construct.
	 * @param model
	 */
	public WicketExamplePage(IModel model)
	{
		super(model);
        final String packageName = getClass().getPackage().getName();
        add(new WicketExampleHeader("mainNavigation", Strings.afterLast(packageName, '.'), this));
		explain();
	}

	/**
	 * Override base method to provide an explanation
	 */
	protected void explain() {}

	/**
	 * Special overwrite to have url compressing for examples that uses the url compressing 
	 * coding strategy and resolver.
	 * 
	 * @see WebURLCompressingCodingStrategy
	 * @see WebURLCompressingTargetResolverStrategy
	 * @see UrlCompressor
	 * @see wicket.protocol.http.request.urlcompressing.ICompressingUrlPage#getUrlCompressor()
	 */
	public UrlCompressor getUrlCompressor()
	{
		if(compressor == null) compressor = new UrlCompressor();
		return compressor;
	}
}
