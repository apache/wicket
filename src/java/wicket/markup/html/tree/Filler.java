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
package wicket.markup.html.tree;

import wicket.markup.html.HtmlContainer;

/**
 * Container for tree elements that fill spaces before the junction and node are rendered.
 * The level is the number of positions this filler is from the beginning of the line
 * (root).
 * @author Eelco Hillenius
 */
public final class Filler extends HtmlContainer
{
	/** Serial Version ID */
	private static final long serialVersionUID = -6804722658941134756L;
	
	/** number of nodes from root. */
    private final int level;

    /**
     * Constructor.
     * @param level level
     */
    public Filler(int level)
    {
        super(Integer.toString(level));
        this.level = level;
    }
}
