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

import java.io.Serializable;

import org.apache.wicket.islands.IPagelessAuthorization;

/**
 * The board a page-less call is about. The board island's page issues it as a handle, so the
 * browser cannot ask about another board by changing an id; the endpoints still check that the
 * session may access the board, as a handle is no permission.
 *
 * @param boardId
 *            the id of the board
 */
public record BoardRef(String boardId) implements Serializable
{
	/**
	 * Allows a call when the session may access the board of its handle.
	 */
	static final IPagelessAuthorization MEMBERS_ONLY = call -> TaskBoardApplication
		.mayAccess(call.getSession(), call.getHandle(BoardRef.class).boardId());
}
