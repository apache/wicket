///////////////////////////////////////////////////////////////////////////////////
//
// Created Jun 12, 2004
//
// Copyright 2004, Jonathan W. Locke
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package guestbook2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model that holds guest book comments.
 * @author Jonathan Locke
 */
public class CommentList
{
    /**
     * @param comment Comment to add to this comment list
     */
    public void add(final Comment comment)
    {
        comments.add(0, comment);
    }
    
    /**
     * @return Returns the comments.
     */
    public List getComments()
    {
        return comments;
    }

    /**
     * @param comments The comments to set.
     */
    public void setComments(final List comments)
    {
        this.comments = comments;
    }
    
    // Synchronized list of comments
    private List comments = Collections.synchronizedList(new ArrayList());
}

///////////////////////////////// End of File /////////////////////////////////
