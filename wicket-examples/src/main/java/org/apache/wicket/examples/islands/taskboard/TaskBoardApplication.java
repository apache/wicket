/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.examples.islands.taskboard;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.Application;
import org.apache.wicket.ISessionListener;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.csp.CSPDirective;
import org.apache.wicket.examples.WicketExampleApplication;
import org.apache.wicket.islands.IslandSettings;

/**
 * The task board example: a board of cards rendered by React, a summary, a dialog and a form in
 * plain Wicket, all on one page.
 * <p>
 * Every session gets a board of its own, so two tabs of one browser share it and can get in each
 * other's way, as two users would.
 * <p>
 * To work on the React components with hot reload, start the Vite dev server in
 * <code>src/main/frontend</code> (<code>npm run dev</code>) and run the examples in development
 * mode with <code>-Dwicket.islands.devServer=http://localhost:5173</code>.
 */
public class TaskBoardApplication extends WicketExampleApplication
{
	/**
	 * The system property naming the URL of a running Vite dev server.
	 */
	public static final String DEV_SERVER_PROPERTY = "wicket.islands.devServer";

	private static final MetaDataKey<String> BOARD = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	private final BoardRepository repository = new BoardRepository();

	private final Map<String, String> boardsBySession = new ConcurrentHashMap<>();

	public static TaskBoardApplication get()
	{
		return (TaskBoardApplication)Application.get();
	}

	public BoardRepository getRepository()
	{
		return repository;
	}

	/**
	 * @return the id of the current session's board, which is created on first use
	 */
	public String getBoardId()
	{
		Session session = Session.get();
		synchronized (session)
		{
			String boardId = session.getMetaData(BOARD);
			if (boardId == null)
			{
				session.bind();
				boardId = repository.createDemoBoard();
				session.setMetaData(BOARD, boardId);
				boardsBySession.put(session.getId(), boardId);
			}
			return boardId;
		}
	}

	/**
	 * @param session
	 *            a session
	 * @param boardId
	 *            the id of a board
	 * @return whether the session may access the board
	 */
	public static boolean mayAccess(Session session, String boardId)
	{
		return boardId != null && boardId.equals(session.getMetaData(BOARD));
	}

	@Override
	public Class<? extends Page> getHomePage()
	{
		return TaskBoardPage.class;
	}

	@Override
	protected void init()
	{
		super.init();

		getSessionListeners().add(new ISessionListener()
		{
			@Override
			public void onUnbound(String sessionId)
			{
				String boardId = boardsBySession.remove(sessionId);
				if (boardId != null)
				{
					repository.remove(boardId);
				}
			}
		});

		IslandSettings islands = IslandSettings.install(this);
		islands.getPagelessEndpoints()
			.register("members", MembersEndpoint.MemberQuery.class, new MembersEndpoint(repository),
				BoardRef.MEMBERS_ONLY)
			.register("throughput", ThroughputEndpoint.ThroughputQuery.class,
				new ThroughputEndpoint(repository), BoardRef.MEMBERS_ONLY);

		String devServer = System.getProperty(DEV_SERVER_PROPERTY);
		if (devServer != null && usesDevelopmentConfig())
		{
			islands.setDevServerUrl(devServer);
			getCspSettings().blocking()
				.add(CSPDirective.CONNECT_SRC, devServer, devServer.replaceFirst("^http", "ws"));
		}
	}
}
