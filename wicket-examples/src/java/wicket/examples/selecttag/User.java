package wicket.examples.selecttag;

/**
 * @author jcompagner
 * @version $Id$
 */
public class User
{
	private Long id;
	private String name;

	/**
	 * Constructor
	 * 
	 * @param id The user's id 
	 * @param name The user's name
	 */
	public User(Long id, String name)
	{
		this.id = id;
		this.name = name;
	}

	/**
	 * @return Returns the id.
	 */
	public Long getId()
	{
		return id;
	}

	/**
	 * @param id
	 *            The id to set.
	 */
	public void setId(Long id)
	{
		this.id = id;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name)
	{
		this.name = name;
	}
}
