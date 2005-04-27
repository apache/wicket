/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ================================================================================
 * Copyright (c)
 * All rechten voorbehouden.
 */
package objectedit;


/**
 * Model that knows about 'modes of operation'.
 * Comes default with MODE_READ_ONLY and MODE_EDIT, but user types can be defined.
 */
public class EditMode
{
	/** Mode that indicates the panel is in read only mode. */
	public static final int MODE_READ_ONLY = 0;

	/** Mode that indicates the panel is in edit mode. */
	public static final int MODE_EDIT = 1;

	/** the current mode; MODE_READ_ONLY, MODE_EDIT or a custom mode. */
	private int mode = MODE_EDIT;

	/**
	 * Construct.
	 */
	public EditMode()
	{
	}

	/**
	 * Construct.
	 * @param mode
	 */
	public EditMode(int mode)
	{
		this.mode = mode;
	}

	/**
	 * Gets the current mode.
	 * @return mode
	 */
	public int getMode()
	{
		return mode;
	}

	/**
	 * Sets the current mode.
	 * @param mode the mode
	 */
	public void setMode(int mode)
	{
		this.mode = mode;
	}
}
