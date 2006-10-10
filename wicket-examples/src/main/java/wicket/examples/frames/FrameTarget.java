/*
 * $Id: FrameTarget.java 4252 2006-02-09 18:19:15 +0000 (Thu, 09 Feb 2006)
 * eelco12 $ $Revision$ $Date: 2006-02-09 18:19:15 +0000 (Thu, 09 Feb
 * 2006) $
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
package wicket.examples.frames;

import java.io.Serializable;

import wicket.Page;

/**
 * Simple struct for holding the class of the right frame.
 * 
 * @author Eelco Hillenius
 */
public final class FrameTarget implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** the class of the bookmarkable page. */
	private Class<? extends Page> frameClass;

	/**
	 * Construct.
	 */
	public FrameTarget()
	{
	}

	/**
	 * Construct.
	 * 
	 * @param frameClass
	 */
	public FrameTarget(Class<? extends Page> frameClass)
	{
		this.frameClass = frameClass;
	}

	/**
	 * Gets frame class.
	 * 
	 * @return lefFrameClass
	 */
	public Class<? extends Page> getFrameClass()
	{
		return frameClass;
	}

	/**
	 * Sets frame class.
	 * 
	 * @param frameClass
	 *            lefFrameClass
	 */
	public void setFrameClass(Class<? extends Page> frameClass)
	{
		this.frameClass = frameClass;
	}
}