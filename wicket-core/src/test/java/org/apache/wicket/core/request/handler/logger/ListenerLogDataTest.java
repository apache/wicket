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
package org.apache.wicket.core.request.handler.logger;

import org.apache.wicket.core.request.handler.IPageAndComponentProvider;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.Test;

public class ListenerLogDataTest {

	/**
	 * Test for WICKET-6908.
	 */
	@Test
	public void neverFails() {
		IPageAndComponentProvider provider = new IPageAndComponentProvider() {
			
			@Override
			public boolean isNewPageInstance() {
				throw new IllegalStateException();
			}
			
			@Override
			public boolean wasExpired() {
				throw new IllegalStateException();
			}
			
			@Override
			public boolean hasPageInstance() {
				throw new IllegalStateException();
			}
			
			@Override
			public Integer getRenderCount() {
				throw new IllegalStateException();
			}
			
			@Override
			public PageParameters getPageParameters() throws PageExpiredException {
				throw new IllegalStateException();
			}
			
			@Override
			public IRequestablePage getPageInstance() throws PageExpiredException {
				throw new IllegalStateException();
			}
			
			@Override
			public Integer getPageId() {
				throw new IllegalStateException();
			}
			
			@Override
			public Class<? extends IRequestablePage> getPageClass() throws PageExpiredException {
				throw new IllegalStateException();
			}
			
			@Override
			public boolean doesProvideNewPage() {
				throw new IllegalStateException();
			}
			
			@Override
			public void detach() {
			}
			
			@Override
			public String getComponentPath() {
				throw new IllegalStateException();
			}
			
			@Override
			public IRequestableComponent getComponent() {
				throw new IllegalStateException();
			}
		};
		
		new ListenerLogData(provider, 1);
	}
}
