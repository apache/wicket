package wicket.examples.repeater;

import java.io.Serializable;

/**
 * domain object for demonstrations.
 * 
 * @author igor
 * 
 */
public class Contact implements Serializable
{
	private long id;

	private String firstName;

	private String lastName;

	private String homePhone;

	private String cellPhone;

	/**
	 * Constructor
	 */
	public Contact()
	{

	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[Contact id=" + id + " firstName=" + firstName + " lastName=" + lastName
				+ " homePhone=" + homePhone + " cellPhone=" + cellPhone + "]";
	}


	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof Contact)
		{
			Contact other = (Contact)obj;
			return other.getFirstName().equals(getFirstName())
					&& other.getLastName().equals(getLastName())
					&& other.getHomePhone().equals(getHomePhone())
					&& other.getCellPhone().equals(getCellPhone());

		}
		else
		{
			return false;
		}
	}

	/**
	 * @param id
	 */
	public void setId(long id)
	{
		this.id = id;
	}

	/**
	 * @return id
	 */
	public long getId()
	{
		return id;
	}

	/**
	 * Constructor
	 * 
	 * @param firstName
	 * @param lastName
	 */
	public Contact(String firstName, String lastName)
	{
		this.firstName = firstName;
		this.lastName = lastName;
	}

	/**
	 * @return cellPhone
	 */
	public String getCellPhone()
	{
		return cellPhone;
	}

	/**
	 * @param cellPhone
	 */
	public void setCellPhone(String cellPhone)
	{
		this.cellPhone = cellPhone;
	}

	/**
	 * @return firstName
	 */
	public String getFirstName()
	{
		return firstName;
	}

	/**
	 * @param firstName
	 */
	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	/**
	 * @return homePhone
	 */
	public String getHomePhone()
	{
		return homePhone;
	}

	/**
	 * @param homePhone
	 */
	public void setHomePhone(String homePhone)
	{
		this.homePhone = homePhone;
	}

	/**
	 * @return lastName
	 */
	public String getLastName()
	{
		return lastName;
	}

	/**
	 * @param lastName
	 */
	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

}
