/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/**
 * 
 */
package wicket.examples.guestbook;

import java.util.ArrayList;
import java.util.List;

import wicket.protocol.http.WebApplication;
import wicket.protocol.http.WebSession;

/**
 * 
 * @author Juergen Donnerstag
 */
public class GuestBookSession extends WebSession
{
	/**
	 * A global list of all comments from all users across all sessions.
	 * <p>
	 * Note that only for the purpose of the example the list is bound to a
	 * session. It might as well be a global list or stored in a database.
	 * However our online life examples experienced some spammers automatically
	 * adding comments to the guestbook. As google might search and list the
	 * guestbook as well, we decided, for the purpose of this example, to store
	 * the guestbook data in the session.
	 */
	private final List<Comment> commentList = new ArrayList<Comment>();

	/**
	 * Constructor
	 * 
	 * @param application
	 */
	protected GuestBookSession(final WebApplication application)
	{
		super(application);
	}

	/**
	 * 
	 * @return comment list
	 */
	public List<Comment> getCommentList()
	{
		return commentList;
	}
}
