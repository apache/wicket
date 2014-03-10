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
package org.apache.wicket.pageStore;

/**
 * An application scoped cache that holds the last N used pages in the application.
 * Acts as a second level cache between the Http Session (first level) and the
 * disk (third level cache).
 *
 * @param <S>
 *          The type of the session identifier
 * @param <PI>
 *          The type of the page identifier
 * @param <P>
 *          The type of the stored page
 */
public interface SecondLevelPageCache<S, PI, P>
{
	P removePage(S session, PI pageId);

	void removePages(S session);

	P getPage(S session, PI pageId);

	void storePage(S session, PI pageId, P page);
}
