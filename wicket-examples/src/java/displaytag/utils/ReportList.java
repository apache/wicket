package displaytag.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Just a utility class for testing out the table and column tags. This List fills itself with objects and sorts them
 * as though it where pulling data from a report. This list is used to show the various report oriented examples (such
 * as grouping, callbacks, and data exports).
 * @author epesh
 * @version $Revision $ ($Author $)
 */
public class ReportList extends ArrayList implements Serializable
{
    /**
     * Creats a TestList that is filled with 20 ReportableListObject suitable for testing.
     */
    public ReportList()
    {
        super();

        for (int j = 0; j < 20; j++)
        {
            add(new ReportableListObject());
        }

        Collections.sort(this);
    }

    /**
     * Creates a TestList that is filled with [size] ReportableListObject suitable for testing.
     * @param size int
     */
    public ReportList(int size)
    {
        super();

        for (int j = 0; j < size; j++)
        {
            add(new ReportableListObject());
        }

        Collections.sort(this);
    }
}
