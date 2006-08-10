/*
 * $Id: IHeaderContributor.java 5039 2006-03-20 10:11:34 +0000 (Mon, 20 Mar
 * 2006) joco01 $ $Revision$ $Date: 2006-03-20 10:11:34 +0000 (Mon, 20
 * Mar 2006) $
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
package wicket.markup.html;

import java.io.Serializable;

/**
 * An interface to be implemented by components which are able to render the
 * header section associated with the markup. 
 * 
 * @author Juergen Donnerstag
 */
public interface IHeaderContributor extends Serializable
{
	/**
	 * Render to the web response whatever the component wants to contribute to
	 * the head section.
	 * 
	 * @param response
	 *            Response object
	 */
	void renderHead(final IHeaderResponse response);
}
