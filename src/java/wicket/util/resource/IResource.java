/*
 * $Id$ $Revision:
 * 1.4 $ $Date$
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
package wicket.util.resource;

import java.io.Serializable;

import wicket.util.watch.IModifiable;

/**
 * An IResource is some storage unit somewhere such as a File (FileResource) or
 * URL (UrlResource) that has a stream (IResourceStream) and content type
 * (determined by the file or URL's extension). In some cases, the resource will
 * also have a last modification date (IModifiable). Thus an IResource is some
 * kind of typed stream that can change over time.
 * <p>
 * The resource data itself can be read from the stream returned by calling
 * IResourceStream.getInputStream(). Subsequently, the resource can be closed
 * with a call to IResourceStream.close(). The type of content in the stream can
 * be determined by calling getContentType(), which returns a mime type such as
 * "image/jpeg" or "text/html".
 * <p>
 * IResources can be files or they can be resources found in Jar files which
 * will be loaded by a ClassLoader. When loaded from a file, they can be watched
 * for changes using the IModifiable interface. When loaded with a ClassLoader,
 * they have a stream nature and cannot be watched for changes since
 * IModifiable.lastModifiedTime() will return null.
 * 
 * @see wicket.util.watch.IModifiable
 * @see wicket.util.resource.IResourceStream
 * @see wicket.util.resource.IResourceStream#getInputStream()
 * @see wicket.util.resource.IResourceStream#close()
 * @author Jonathan Locke
 */
public interface IResource extends IResourceStream, IModifiable, Serializable
{
	/**
	 * Gets the mime type of this resource
	 * 
	 * @return The mime type of this resource, such as "image/jpeg" or
	 *         "text/html"
	 */
	public String getContentType();
}
