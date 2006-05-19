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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Hotel Preferences POJO.
 * 
 * @author Eelco Hillenius
 */
public class HotelPreferences implements Serializable
{
	private String firstName;
	private String lastName;
	private Date dateOfBirth;
	private String passportNumber;

	private String roomClass = Room.ROOM_CLASS_ECONOMY; // defaults to economy
	private boolean wantsBalcony = false;
	private Room room;

	/**
	 * List of rooms. For the example this is super-simple, but in the realworld
	 * you would need to track which rooms have been reserved etc.
	 */
	private static Room[] availableRooms = new Room[] {
			new Room("1", Room.ROOM_CLASS_ECONOMY, false),
			new Room("2", Room.ROOM_CLASS_ECONOMY, false),
			new Room("3", Room.ROOM_CLASS_ECONOMY, true),
			new Room("4", Room.ROOM_CLASS_ECONOMY, true),
			new Room("5", Room.ROOM_CLASS_EXECUTIVE, false),
			new Room("6", Room.ROOM_CLASS_EXECUTIVE, false),
			new Room("7", Room.ROOM_CLASS_EXECUTIVE, true),
			new Room("8", Room.ROOM_CLASS_EXECUTIVE, true) };

	private boolean wantsWakeUpCall = false;
	private Integer wakeUpCallHours;
	private Integer wakeUpCallMinutes;
	private boolean wantsBreakFast = true;

	/**
	 * Construct.
	 */
	public HotelPreferences()
	{
	}

	/**
	 * Gets the available room classes.
	 * 
	 * @return the available room classes
	 */
	public final List roomClasses()
	{
		return Arrays.asList(new String[] { Room.ROOM_CLASS_ECONOMY, Room.ROOM_CLASS_EXECUTIVE });
	}

	/**
	 * Gets the available rooms for the given parameters.
	 * 
	 * @param roomClass
	 *            class of the room
	 * @param withBalcony
	 *            whether the room should have a balcony
	 * @return the available rooms for the given parameters
	 */
	public final List/* <Room> */getAvailableRooms(String roomClass, boolean withBalcony)
	{
		List l = new ArrayList();
		int len = availableRooms.length;
		for (int i = 0; i < len; i++)
		{
			if (availableRooms[i].getRoomClass().equals(roomClass)
					&& availableRooms[i].getHasBalcony() == withBalcony)
			{
				l.add(availableRooms[i]);
			}
		}
		return l;
	}

	/**
	 * Gets the dateOfBirth.
	 * 
	 * @return dateOfBirth
	 */
	public Date getDateOfBirth()
	{
		return dateOfBirth;
	}

	/**
	 * Sets the dateOfBirth.
	 * 
	 * @param dateOfBirth
	 *            dateOfBirth
	 */
	public void setDateOfBirth(Date dateOfBirth)
	{
		this.dateOfBirth = dateOfBirth;
	}

	/**
	 * Gets the firstName.
	 * 
	 * @return firstName
	 */
	public String getFirstName()
	{
		return firstName;
	}

	/**
	 * Sets the firstName.
	 * 
	 * @param firstName
	 *            firstName
	 */
	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	/**
	 * Gets the lastName.
	 * 
	 * @return lastName
	 */
	public String getLastName()
	{
		return lastName;
	}

	/**
	 * Sets the lastName.
	 * 
	 * @param lastName
	 *            lastName
	 */
	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	/**
	 * Gets the passportNumber.
	 * 
	 * @return passportNumber
	 */
	public String getPassportNumber()
	{
		return passportNumber;
	}

	/**
	 * Sets the passportNumber.
	 * 
	 * @param passportNumber
	 *            passportNumber
	 */
	public void setPassportNumber(String passportNumber)
	{
		this.passportNumber = passportNumber;
	}

	/**
	 * Gets the room.
	 * 
	 * @return room
	 */
	public Room getRoom()
	{
		return room;
	}

	/**
	 * Sets the room.
	 * 
	 * @param room
	 *            room
	 */
	public void setRoom(Room room)
	{
		this.room = room;
	}

	/**
	 * Gets the wantsWakeUpCall.
	 * 
	 * @return wantsWakeUpCall
	 */
	public boolean getWantsWakeUpCall()
	{
		return wantsWakeUpCall;
	}

	/**
	 * Sets the wantsWakeUpCall.
	 * 
	 * @param wantsWakeUpCall
	 *            wantsWakeUpCall
	 */
	public void setWantsWakeUpCall(boolean wantsWakeUpCall)
	{
		this.wantsWakeUpCall = wantsWakeUpCall;
	}

	/**
	 * Gets the wakeUpCallHours.
	 * 
	 * @return wakeUpCallHours
	 */
	public Integer getWakeUpCallHours()
	{
		return wakeUpCallHours;
	}

	/**
	 * Sets the wakeUpCallHours.
	 * 
	 * @param wakeUpCallHours
	 *            wakeUpCallHours
	 */
	public void setWakeUpCallHours(Integer wakeUpCallHours)
	{
		this.wakeUpCallHours = wakeUpCallHours;
	}

	/**
	 * Gets the wakeUpCallMinutes.
	 * 
	 * @return wakeUpCallMinutes
	 */
	public Integer getWakeUpCallMinutes()
	{
		return wakeUpCallMinutes;
	}

	/**
	 * Sets the wakeUpCallMinutes.
	 * 
	 * @param wakeUpCallMinutes
	 *            wakeUpCallMinutes
	 */
	public void setWakeUpCallMinutes(Integer wakeUpCallMinutes)
	{
		this.wakeUpCallMinutes = wakeUpCallMinutes;
	}

	/**
	 * Gets the wantsBreakFast.
	 * 
	 * @return wantsBreakFast
	 */
	public boolean getWantsBreakFast()
	{
		return wantsBreakFast;
	}

	/**
	 * Sets the wantsBreakFast.
	 * 
	 * @param wantsBreakFast
	 *            wantsBreakFast
	 */
	public void setWantsBreakFast(boolean wantsBreakFast)
	{
		this.wantsBreakFast = wantsBreakFast;
	}
}
