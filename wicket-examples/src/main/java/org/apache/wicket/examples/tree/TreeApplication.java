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
package org.apache.wicket.examples.tree;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.response.filter.ServerAndClientTimeFilter;

/**
 * Application class for tree examples.
 * 
 * @author svenmeier
 */
public class TreeApplication extends WebApplication
{
	/**
	 * All root {@link Foo}s.
	 */
	public List<Foo> foos = new ArrayList<>();

	/**
	 * Constructor.
	 */
	public TreeApplication()
	{
		Foo fooA = new Foo("A");
		{
			Foo fooAA = new Foo(fooA, "AA");
			{
				new Foo(fooAA, "AAA");
				new Foo(fooAA, "AAB");
			}
			Foo fooAB = new Foo(fooA, "AB");
			{
				new Foo(fooAB, "ABA");
				Foo fooABB = new Foo(fooAB, "ABB");
				{
					new Foo(fooABB, "ABBA");
					Foo fooABBB = new Foo(fooABB, "ABBB");
					{
						new Foo(fooABBB, "ABBBA");
					}
				}
				new Foo(fooAB, "ABC");
				new Foo(fooAB, "ABD");
			}
			Foo fooAC = new Foo(fooA, "AC");
			{
				new Foo(fooAC, "ACA");
				new Foo(fooAC, "ACB");
			}
		}
		foos.add(fooA);

		Foo fooB = new Foo("B");
		{
			new Foo(fooB, "BA");
			new Foo(fooB, "BB");
		}
		foos.add(fooB);

		Foo fooC = new Foo("C");
		foos.add(fooC);
	}

	/**
	 * @see org.apache.wicket.protocol.http.WebApplication#init()
	 */
	@Override
	protected void init()
	{
		getDebugSettings().setDevelopmentUtilitiesEnabled(true);

		getRequestCycleSettings().addResponseFilter(new ServerAndClientTimeFilter());
	}

	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends Page> getHomePage()
	{
		return BeginnersTreePage.class;
	}

	/**
	 * Get a {@link Foo} by its id.
	 */
	public Foo getFoo(String id)
	{
		return findFoo(foos, id);
	}

	private static Foo findFoo(List<Foo> foos, String id)
	{
		for (Foo foo : foos)
		{
			if (foo.getId().equals(id))
			{
				return foo;
			}

			Foo temp = findFoo(foo.getFoos(), id);
			if (temp != null)
			{
				return temp;
			}
		}

		return null;
	}

	public static TreeApplication get()
	{
		return (TreeApplication)WebApplication.get();
	}
}
