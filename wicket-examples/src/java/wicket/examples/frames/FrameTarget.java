/*
 * $Id$ $Revision:
 * 1.5 $ $Date$
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

/**
 * Simple struct for holding the class name of the right frame.
 * 
 * @author Eelco Hillenius
 */
public final class FrameTarget
{
	private String frameClass;

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
	public FrameTarget(String frameClass)
	{
		this.frameClass = frameClass;
	}

	/**
	 * Gets frame class.
	 * 
	 * @return lefFrameClass
	 */
	public String getFrameClass()
	{
		return frameClass;
	}

	/**
	 * Sets frame class.
	 * 
	 * @param frameClass
	 *            lefFrameClass
	 */
	public void setFrameClass(String frameClass)
	{
		this.frameClass = frameClass;
	}
}