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
package wicket.markup.html.form;

/**
 * This class is currently basically (excluding name, value and path) a copy of
 * Cookie. It provides default values for the persister.
 * 
 * @author Juergen Donnerstag
 */
public class FormComponentPersistenceDefaults
{ // TODO finalize javadoc
    /** max age that the component will be persisted. */
    private int maxAge = 3600 * 24 * 30; // 30 days

    /** cookie comment. */
    private String comment;

    /** cookie domain. */
    private String domain;

    /** whether the cookie is secure. */
    private boolean secure;

    /** cookie version. */
    private int version;

    /**
     * Gets the max age.
     * @return the max age
     */
    public int getMaxAge()
    {
        return maxAge;
    }

    /**
     * Sets the max age.
     * @param maxAge the max age
     */
    public void setMaxAge(int maxAge)
    {
        this.maxAge = maxAge;
    }

    /**
     * Gets the cookie comment.
     * @return the cookie comment
     */
    public String getComment()
    {
        return comment;
    }

    /**
     * Sets the cookie comment.
     * @param comment the cookie comment
     */
    public void setComment(String comment)
    {
        this.comment = comment;
    }

    /**
     * Gets the cookie domain.
     * @return the cookie domain
     */
    public String getDomain()
    {
        return domain;
    }

    /**
     * Sets the cookie domain.
     * @param domain the cookie domain
     */
    public void setDomain(String domain)
    {
        this.domain = domain;
    }

    /**
     * Gets whether this cookie is secure.
     * @return whether this cookie is secure
     */
    public boolean isSecure()
    {
        return secure;
    }

    /**
     * Sets whether this cookie is secure.
     * @param secure whether this cookie is secure
     */
    public void setSecure(boolean secure)
    {
        this.secure = secure;
    }

    /**
     * Gets the cookie version.
     * @return the cookie version
     */
    public int getVersion()
    {
        return version;
    }

    /**
     * Sets the cookie version.
     * @param version the cookie version
     */
    public void setVersion(int version)
    {
        this.version = version;
    }
}