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
package org.apache.wicket.core.util.lang;

public class PropertyExpression
{
	JavaProperty javaProperty;
	BeanProperty beanProperty;
	CharSequence index;
	PropertyExpression next;

	static class BeanProperty
	{
		CharSequence propertyName;
		CharSequence index;

		public BeanProperty()
		{
		}

		public BeanProperty(String name, String index)
		{
			this.propertyName = name;
			this.index = index;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((index == null) ? 0 : index.hashCode());
			result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BeanProperty other = (BeanProperty)obj;
			if (index == null)
			{
				if (other.index != null)
					return false;
			}
			else if (!index.equals(other.index))
				return false;
			if (propertyName == null)
			{
				if (other.propertyName != null)
					return false;
			}
			else if (!propertyName.equals(other.propertyName))
				return false;
			return true;
		}

	}

	static class JavaProperty
	{
		CharSequence javaIdentifier;
		CharSequence index;
		public boolean hasMethodSign;

		public JavaProperty()
		{
		}

		public JavaProperty(String javaIdentifier, String index, boolean hasMethodSign)
		{
			this.javaIdentifier = javaIdentifier;
			this.index = index;
			this.hasMethodSign = hasMethodSign;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + (hasMethodSign ? 1231 : 1237);
			result = prime * result + ((index == null) ? 0 : index.hashCode());
			result = prime * result + ((javaIdentifier == null) ? 0 : javaIdentifier.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			JavaProperty other = (JavaProperty)obj;
			if (hasMethodSign != other.hasMethodSign)
				return false;
			if (index == null)
			{
				if (other.index != null)
					return false;
			}
			else if (!index.equals(other.index))
				return false;
			if (javaIdentifier == null)
			{
				if (other.javaIdentifier != null)
					return false;
			}
			else if (!javaIdentifier.equals(other.javaIdentifier))
				return false;
			return true;
		}

	}

}