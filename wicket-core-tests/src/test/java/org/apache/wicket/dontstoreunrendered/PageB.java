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
package org.apache.wicket.dontstoreunrendered;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.RestartResponseException;

/**
 *
 */
public class PageB extends BasePage {

	static final AtomicBoolean PAGE_B_INITIALIZED = new AtomicBoolean(false);
	static final AtomicBoolean PAGE_B_RENDERED    = new AtomicBoolean(false);

	public PageB() {
		super();

		throw new RestartResponseException(PageC.class);
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		PAGE_B_INITIALIZED.set(true);
	}

	@Override
	public void renderPage()
	{
		super.renderPage();
		PAGE_B_RENDERED.set(true);
	}
}
