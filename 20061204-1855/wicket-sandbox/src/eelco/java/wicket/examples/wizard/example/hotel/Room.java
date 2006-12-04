/*
 * $Id$ $Revision$ $Date$
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
package wicket.examples.wizard.example.hotel;

import java.io.Serializable;

/**
 * Represents a room.
 * 
 * @author Eelco Hillenius
 */
public final class Room implements Serializable
{
	/** class economy. */
	public static final String ROOM_CLASS_ECONOMY = "economy";

	/** class executive. */
	public static final String ROOM_CLASS_EXECUTIVE = "executive";

	private final String roomClass; // defaults to economy
	private final boolean hasBalcony;
	private final String roomNumber;

	/**
	 * Construct.
	 * 
	 * @param roomNumber
	 * @param roomClass
	 * @param hasBalcony
	 */
	public Room(String roomNumber, String roomClass, boolean hasBalcony)
	{
		this.roomNumber = roomNumber;
		this.roomClass = roomClass;
		this.hasBalcony = hasBalcony;
	}

	/**
	 * Gets the hasBalcony.
	 * 
	 * @return hasBalcony
	 */
	public boolean getHasBalcony()
	{
		return hasBalcony;
	}

	/**
	 * Gets the roomClass.
	 * 
	 * @return roomClass
	 */
	public String getRoomClass()
	{
		return roomClass;
	}

	/**
	 * Gets the roomNumber.
	 * 
	 * @return roomNumber
	 */
	public String getRoomNumber()
	{
		return roomNumber;
	}

}
