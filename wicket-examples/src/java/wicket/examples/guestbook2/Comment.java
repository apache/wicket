/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples.guestbook2;

import java.io.Serializable;

import java.util.Date;

/**
 * Simple "POJO" bean that holds a wicket.examples.wicket.examples.guestbook text.
 * @hibernate.class
 * @author Jonathan Locke
 */
public class Comment implements Serializable
{
    private int id;
    private String text;
    private Date date = new Date();

    /**
     * Constructor
     */
    public Comment()
    {
    }

    /**`
     * Copy constructor
     * @param comment The comment to copy
     */
    public Comment(final Comment comment)
    {
        this.text = comment.text;
        this.date = comment.date;
    }

    /**
     * @hibernate.property
     * @return Returns the text.
     */
    public String getText()
    {
        return text;
    }

    /**
     * @param text The text to set.
     */
    public void setText(String text)
    {
        this.text = text;
    }

    /**
     * @hibernate.property
     * @return Returns the date.
     */
    public Date getDate()
    {
        return date;
    }

    /**
     * @param date The date to set.
     */
    public void setDate(Date date)
    {
        this.date = date;
    }

    /**
     * @hibernate.id generator-class = "native"
     * @return Returns the id.
     */
    public int getId()
    {
        return id;
    }

    /**
     * @param id The id to set.
     */
    public void setId(int id)
    {
        this.id = id;
    }
}

///////////////////////////////// End of File /////////////////////////////////
