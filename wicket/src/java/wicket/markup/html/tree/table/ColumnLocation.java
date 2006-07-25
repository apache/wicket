package wicket.markup.html.tree.table;

import java.io.Serializable;

/**
 * This class represents location of a column in tree table.
 * <p>
 * First attribute of location is <b>alignment</b>. Alignment specifies,
 * whether the column is located on the left side of the table, on the right
 * side, or in the middle. Columns in the middle of the table take all space
 * between columns on the left and columns on the right.
 * <p>
 * Next two attributes are <b>size</b> and <b>unit</b>:
 * <ul>
 * <li> For columns aligned to the left and to the right, the <b>size</b>
 * represents the actual width of the column, according to chosen unit. Possible
 * units for left and right aligned columns are <em>PX</em>, <em>EM</em>
 * and <em>PERCENT</em>. </li>
 * <li> For columns in the middle, the only valid unit is <em>PROPORTIONAL</em>.
 * These columns take all available space between columns on the left and
 * columns on the right. How this space is divided between middle columns is
 * determined by the <b>size</b>. In this case the size can be understand as
 * weight. Columns with bigger size take more space than columns with smaller
 * size. For example, if there are three columns and their sizes are 2, 1, 1,
 * the first column thakes 50% of the space and the second two columns take 25%
 * each. </li>
 * </ul>
 * 
 * @author Matej Knopp
 */
public class ColumnLocation implements Serializable
{
	/**
	 * Alignment of the column.
	 */
	public enum Alignment {
		/** Align left. */
		LEFT,
		/** Align middle. */
		MIDDLE,
		/** Align right. */
		RIGHT
	}

	/**
	 * Units.
	 */
	public enum Unit {
		/** Size of letter M in the current font. */
		EM,
		/** Percentage. */
		PERCENT,
		/** Proportional. */
		PROPORTIONAL,
		/** Pixels. */
		PX
	};

	private static final long serialVersionUID = 1L;;

	private Alignment alignment;
	private int size;
	private Unit unit;

	/**
	 * Constructs the ColumnLocation object.
	 * 
	 * @param alignment
	 *            The column alignment
	 * @param size
	 *            The column size in expressed in the provided unit
	 * @param unit
	 *            The unit that the size argument is expressed in
	 * @throws IllegalArgumentException
	 *             if the unit does not matche the alignment
	 */
	public ColumnLocation(Alignment alignment, int size, Unit unit)
	{
		this.alignment = alignment;
		this.size = size;
		this.unit = unit;

		if (alignment == Alignment.MIDDLE && unit != Unit.PROPORTIONAL)
		{
			throw new IllegalArgumentException(
					"For alignment MIDDLE the specified unit must be PROPORTIONAL.");
		}
		else if (alignment != Alignment.MIDDLE && unit == Unit.PROPORTIONAL)
		{
			throw new IllegalArgumentException(
					"Unit PROPORTIONAL can be specified only for columns with alignment MIDDLE.");
		}
	}

	/**
	 * Returns the allignment of this column.
	 * 
	 * @return The alignment of this column
	 */
	public Alignment getAlignment()
	{
		return alignment;
	}

	/**
	 * Returns the size of this column.
	 * 
	 * @return The size of this column
	 */
	public int getSize()
	{
		return size;
	}

	/**
	 * Returns the unit of a column.
	 * 
	 * @return The unit of this column
	 */
	public Unit getUnit()
	{
		return unit;
	}
}
