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
import java.util.Date;

/**
 * Represents a room reservation.
 * 
 * @author Eelco Hillenius
 */
public final class RoomReservation implements Serializable
{
	private Date from;
	private Date until;
	private Room room;

	/**
	 * Construct.
	 * 
	 * @param room
	 *            room
	 * @param from
	 *            from date
	 * @param until
	 *            until date
	 */
	public RoomReservation(Room room, Date from, Date until)
	{
		this.room = room;
		this.from = from;
		this.until = until;
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
	 * Gets the from.
	 * 
	 * @return from
	 */
	public Date getFrom()
	{
		return from;
	}

	/**
	 * Sets the from.
	 * 
	 * @param from
	 *            from
	 */
	public void setFrom(Date from)
	{
		this.from = from;
	}

	/**
	 * Gets the until.
	 * 
	 * @return until
	 */
	public Date getUntil()
	{
		return until;
	}

	/**
	 * Sets the until.
	 * 
	 * @param until
	 *            until
	 */
	public void setUntil(Date until)
	{
		this.until = until;
	}
}
