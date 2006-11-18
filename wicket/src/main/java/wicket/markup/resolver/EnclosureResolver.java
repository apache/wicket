/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.markup.resolver;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.internal.Enclosure;
import wicket.markup.parser.filter.EnclosureHandler;

/**
 * This is a tag resolver which automatically adds a Enclosure container for
 * each &lt;wicket:enclosure&gt; tag. As this is no default resolver, it must
 * be added manually:
 * 
 * <pre>
 * MyApplication.java
 * { 
 *   ...
 *   public void init()
 *   {
 *       super.init();
 *       getPageSettings().addComponentResolver(new EnclosureResolver());
 *   }
 *   </pre>
 *
 * @see EnclosureHandler
 * @see Enclosure
 * 
 * @author Juergen Donnerstag
 */
public class EnclosureResolver implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @see wicket.markup.resolver.IComponentResolver#resolve(wicket.MarkupContainer,
	 *      wicket.markup.MarkupStream, wicket.markup.ComponentTag)
	 */
	public boolean resolve(final MarkupContainer container, final MarkupStream markupStream,
			final ComponentTag tag)
	{
		if (tag.isEnclosureTag())
		{
			String id = Component.AUTO_COMPONENT_PREFIX + "enclosure-"
					+ container.getPage().getAutoIndex();
			final Enclosure enclosure = new Enclosure(container, id, tag
					.getString(EnclosureHandler.CHILD_ATTRIBUTE));
			enclosure.render(markupStream);

			// Yes, we handled the tag
			return true;
		}

		// We were not able to handle the tag
		return false;
	}
}