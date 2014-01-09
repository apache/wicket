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
import java.util.Collections;
import java.util.List;

/**
 * @author Sven Meier
 */
public class Foo
{

	private static final long serialVersionUID = 1L;

	private String id;

	private String bar;

	private String baz;

	private boolean quux;

	private boolean loaded;

	private Foo parent;

	private List<Foo> foos = new ArrayList<>();

	public Foo(String id)
	{
		this.id = id;
		bar = id.toLowerCase() + "Bar";
		baz = id.toLowerCase() + "Baz";
	}

	public Foo(Foo parent, String name)
	{
		this(name);

		this.parent = parent;
		this.parent.foos.add(this);
	}

	public Foo getParent()
	{
		return parent;
	}

	public String getId()
	{
		return id;
	}

	public String getBar()
	{
		return bar;
	}

	public String getBaz()
	{
		return baz;
	}

	public void setBar(String bar)
	{
		this.bar = bar;
	}

	public void setBaz(String baz)
	{
		this.baz = baz;
	}

	public void setQuux(boolean quux)
	{
		this.quux = quux;

		if (quux)
		{
			// set quux on all descendants
			for (Foo foo : foos)
			{
				foo.setQuux(true);
			}
		}
		else
		{
			// clear quux on all ancestors
			if (parent != null)
			{
				parent.setQuux(false);
			}
		}
	}

	public boolean getQuux()
	{
		return quux;
	}

	public List<Foo> getFoos()
	{
		return Collections.unmodifiableList(foos);
	}

	@Override
	public String toString()
	{
		return id;
	}

	public boolean isLoaded()
	{
		return loaded;
	}

	public void setLoaded(boolean loaded)
	{
		this.loaded = loaded;
	}
}
