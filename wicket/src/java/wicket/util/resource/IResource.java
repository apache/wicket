/*
 * $Id$ $Revision:
 * 1.4 $ $Date$
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
package wicket.util.resource;

/**
 * Interface to a streamed resource of a given type. The resource data itself
 * can be read from the stream returned by getInputStream(). Subsequently, the
 * resource can be closed with a call to close(). The type of content in the
 * stream can be determined by calling getExtension(), which returns an
 * extension String such as "jpeg" or "html".
 * 
 * @see wicket.util.resource.IResourceStream#getInputStream()
 * @see wicket.util.resource.IResourceStream#close()
 * @author Jonathan Locke
 */
public interface IResource extends IResourceStream
{
	/**
	 * Gets the extension of this resource
	 * 
	 * @return The extension of this resource, such as "jpeg" or "html"
	 */
	public String getExtension();
}
