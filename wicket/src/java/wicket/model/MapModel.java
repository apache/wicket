/*
 * $Id$ $Revision$
 * $Date$
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
package wicket.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A Model subclass that holds a Map, wrapping it in a HashMap (if necessary) to
 * ensure that it is Serializable.
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class MapModel extends Model
{
	/** Serial Version ID. */
	static final long serialVersionUID = 898435312991208901L;

	/**
	 * Static factory method for creating a MapModel for a given Map.
	 * 
	 * @param map
	 *            The map to construct a MapModel for
	 * @return The MapModel
	 */
	public static MapModel valueOf(final Map map)
	{
		if (map instanceof Serializable)
		{
			return new MapModel((Serializable)map);
		}
		else
		{
			return new MapModel(new HashMap(map));
		}
	}

	/**
	 * Private constructor forces use of static factory method.
	 * 
	 * @param object
	 *            The model object
	 */
	private MapModel(Serializable object)
	{
		super(object);
	}
}
