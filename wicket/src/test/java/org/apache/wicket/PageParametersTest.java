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
package org.apache.wicket;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Unit test for the PageParameters, introduced for bug [ 1213889 ] PageParameter keyValuePair
 * disallows negatives.
 * 
 * @author Martijn Dashorst
 */
public class PageParametersTest extends TestCase
{
	/**
	 * 
	 */
	public void test_1()
	{
		PageParameters parameters = new PageParameters("0=test");
		assertEquals("test", parameters.get("0"));
	}

	/**
	 * 
	 */
	public void test_2()
	{
		PageParameters parameters = new PageParameters("test");
		assertNull(parameters.get("test"));
	}

	/**
	 * 
	 */
	public void test_3()
	{
		PageParameters parameters = new PageParameters("test=");
		assertEquals("", parameters.get("test"));
	}

	/**
	 * 
	 */
	public void test_4()
	{
		try
		{
			new PageParameters("=test");
			fail("Expected an exception: invalid URL parameter");
		}
		catch (IllegalArgumentException ex)
		{
			// ok; expected
		}
	}

	/**
	 * Test creation of an array on multiple calls to add.
	 */
	public void testArray1()
	{
		PageParameters parameters = new PageParameters();
		parameters.add("a", "1");
		assertEquals("1", parameters.get("a"));
		parameters.add("a", "2");
		Object o = parameters.get("a");
		assertTrue(o instanceof String[]);
		String[] a = (String[])o;
		assertEquals(2, a.length);
		assertEquals("1", a[0]);
		assertEquals("2", a[1]);
		parameters.add("a", "3");
		o = parameters.get("a");
		assertTrue(o instanceof String[]);
		a = (String[])o;
		assertEquals(3, a.length);
		assertEquals("1", a[0]);
		assertEquals("2", a[1]);
		assertEquals("3", a[2]);
	}

	public void testArray2()
	{
		PageParameters parameters = new PageParameters("a=1,a=2,a=3");
		String[] a = parameters.getStringArray("a");
		assertEquals(3, a.length);
		assertEquals("1", a[0]);
		assertEquals("2", a[1]);
		assertEquals("3", a[2]);
	}

	/**
	 * Parsing of negative numbers on the right side of the assignment didn't work, as the minus
	 * character was not part of the word pattern.
	 */
	public void testNegativeNumberParameter()
	{
		PageParameters parameters = new PageParameters("a=-1");
		assertEquals("-1", parameters.get("a"));
	}

	/**
	 * 
	 */
	public void testAddInteger()
	{
		PageParameters params = new PageParameters();
		params.put("myint", 12345);
		assertEquals(params.getAsInteger("myint").intValue(), 12345);
	}

	/**
	 * 
	 */
	public void testAsObject()
	{
		System.out.println("testObjectDereferencing");
		PageParameters params = new PageParameters();
		Face face = new Face();
		IFace fromParams = params.asObject(IFace.class);
		assertNotNull(fromParams);
		face.copyInto(fromParams);

		IFace another = params.asObject(IFace.class);
		Face copy = new Face(another);
		assertEquals(face, copy);

		System.err.println("Params:\n" + params);

		// Do the equivalent of passing this as a URL and reconstructing it

		Map<String, String[]> x = params.toRequestParameters();
		PageParameters nue = new PageParameters(x);

		another = nue.asObject(IFace.class);
		copy = new Face(another);
		assertEquals(face, copy);
		assertNotSame(face, copy);

		PageParameters pp = new PageParameters();
		IFace i = pp.asObject(IFace.class);
		Face one = new Face(new SerializableThing("whee"), 72, false, 1.23, 3.75F, (short)450,
			(byte)-5, new byte[] { 1, 2, 3 }, 'q', 342L, "testAllThis");
		one.copyInto(i);

		Face two = new Face(i);
		assertFalse(copy.equals(two));
		assertEquals(one, two);

		assertEquals(72, i.getIntVal());
		assertNotNull(pp.get("intVal"));

		assertEquals(72, (int)pp.getAsInteger("intVal"));

		i.setIntVal(325);
		assertEquals(325, (int)pp.getAsInteger("intVal"));

	}


	public static interface IFace extends Serializable
	{
		byte[] getByteArrVal();

		byte getByteVal();

		char getCharVal();

		double getDoubleVal();

		float getFloatVal();

		int getIntVal();

		long getLongVal();

		short getShortVal();

		String getStringVal();

		SerializableThing getThing();

		boolean isBoolVal();

		void setBoolVal(boolean boolVal);

		void setByteArrVal(byte[] byteArrVal);

		void setByteVal(byte byteVal);

		void setCharVal(char charVal);

		void setDoubleVal(double doubleVal);

		void setFloatVal(float floatVal);

		void setIntVal(int intVal);

		void setLongVal(long longVal);

		void setShortVal(short shortVal);

		void setStringVal(String stringVal);

		void setThing(SerializableThing thing);
	}

	/**
	 * 
	 */
	public static final class Face implements IFace
	{
		private static final long serialVersionUID = 1L;

		private SerializableThing thing = new SerializableThing("Foo");
		private int intVal = 23;
		private boolean boolVal = true;
		private double doubleVal = 0.135D;
		private float floatVal = 12.230F;
		private short shortVal = 32766;
		private byte byteVal = -123;
		private byte[] byteArrVal = new byte[] { -124, -3, 0, 14, 22 };
		private char charVal = 'c';
		private long longVal = 1294380151L;
		private String stringVal = "Hello World";

		/**
		 * Construct.
		 * 
		 * @param thing
		 * @param intVal
		 * @param boolVal
		 * @param doubleVal
		 * @param floatVal
		 * @param shortVal
		 * @param byteVal
		 * @param byteArrVal
		 * @param charVal
		 * @param longVal
		 * @param stringVal
		 */
		public Face(SerializableThing thing, int intVal, boolean boolVal, double doubleVal,
			float floatVal, short shortVal, byte byteVal, byte[] byteArrVal, char charVal,
			long longVal, String stringVal)
		{
			this.thing = thing;
			this.intVal = intVal;
			this.boolVal = boolVal;
			this.doubleVal = doubleVal;
			this.floatVal = floatVal;
			this.shortVal = shortVal;
			this.byteVal = byteVal;
			this.byteArrVal = byteArrVal;
			this.charVal = charVal;
			this.longVal = longVal;
			this.stringVal = stringVal;
		}

		/**
		 * Construct.
		 */
		public Face()
		{

		}

		/**
		 * Construct.
		 * 
		 * @param o
		 */
		public Face(IFace o)
		{
			thing = o.getThing();
			intVal = o.getIntVal();
			boolVal = o.isBoolVal();
			doubleVal = o.getDoubleVal();
			floatVal = o.getFloatVal();
			shortVal = o.getShortVal();
			byteVal = o.getByteVal();
			byteArrVal = o.getByteArrVal();
			charVal = o.getCharVal();
			longVal = o.getLongVal();
			stringVal = o.getStringVal();
		}

		/**
		 * 
		 * @param o
		 */
		public void copyInto(IFace o)
		{
			o.setThing(thing);
			o.setIntVal(intVal);
			o.setBoolVal(boolVal);
			o.setDoubleVal(doubleVal);
			o.setFloatVal(floatVal);
			o.setShortVal(shortVal);
			o.setByteVal(byteVal);
			o.setByteArrVal(byteArrVal);
			o.setCharVal(charVal);
			o.setLongVal(longVal);
			o.setStringVal(stringVal);
		}

		public boolean isBoolVal()
		{
			return boolVal;
		}

		public void setBoolVal(boolean boolVal)
		{
			this.boolVal = boolVal;
		}

		public byte[] getByteArrVal()
		{
			return byteArrVal;
		}

		public void setByteArrVal(byte[] byteArrVal)
		{
			this.byteArrVal = byteArrVal;
		}

		public byte getByteVal()
		{
			return byteVal;
		}

		public void setByteVal(byte byteVal)
		{
			this.byteVal = byteVal;
		}

		public char getCharVal()
		{
			return charVal;
		}

		public void setCharVal(char charVal)
		{
			this.charVal = charVal;
		}

		public double getDoubleVal()
		{
			return doubleVal;
		}

		public void setDoubleVal(double doubleVal)
		{
			this.doubleVal = doubleVal;
		}

		public float getFloatVal()
		{
			return floatVal;
		}

		public void setFloatVal(float floatVal)
		{
			this.floatVal = floatVal;
		}

		public int getIntVal()
		{
			return intVal;
		}

		public void setIntVal(int intVal)
		{
			this.intVal = intVal;
		}

		public long getLongVal()
		{
			return longVal;
		}

		public void setLongVal(long longVal)
		{
			this.longVal = longVal;
		}

		public short getShortVal()
		{
			return shortVal;
		}

		public void setShortVal(short shortVal)
		{
			this.shortVal = shortVal;
		}

		public String getStringVal()
		{
			return stringVal;
		}

		public void setStringVal(String stringVal)
		{
			this.stringVal = stringVal;
		}

		public SerializableThing getThing()
		{
			return thing;
		}

		public void setThing(SerializableThing thing)
		{
			this.thing = thing;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == null)
			{
				return false;
			}
			if (getClass() != obj.getClass())
			{
				return false;
			}
			final Face other = (Face)obj;
			if (thing != other.thing && (thing == null || !thing.equals(other.thing)))
			{
				return false;
			}
			if (intVal != other.intVal)
			{
				return false;
			}
			if (boolVal != other.boolVal)
			{
				return false;
			}
			if (doubleVal != other.doubleVal)
			{
				return false;
			}
			if (floatVal != other.floatVal)
			{
				return false;
			}
			if (shortVal != other.shortVal)
			{
				return false;
			}
			if (byteVal != other.byteVal)
			{
				return false;
			}
			if (!Arrays.equals(byteArrVal, other.byteArrVal))
			{
				return false;
			}
			if (charVal != other.charVal)
			{
				return false;
			}
			if (longVal != other.longVal)
			{
				return false;
			}
			if ((stringVal == null) ? (other.stringVal != null)
				: !stringVal.equals(other.stringVal))
			{
				return false;
			}
			return true;
		}

		@Override
		public int hashCode()
		{
			int hash = 3;
			hash = 23 * hash + (thing != null ? thing.hashCode() : 0);
			hash = 23 * hash + intVal;
			hash = 23 * hash + (boolVal ? 1 : 0);
			hash = 23 *
				hash +
				(int)(Double.doubleToLongBits(doubleVal) ^ (Double.doubleToLongBits(doubleVal) >>> 32));
			hash = 23 * hash + Float.floatToIntBits(floatVal);
			hash = 23 * hash + shortVal;
			hash = 23 * hash + byteVal;
			hash = 23 * hash + Arrays.hashCode(byteArrVal);
			hash = 23 * hash + charVal;
			hash = 23 * hash + (int)(longVal ^ (longVal >>> 32));
			hash = 23 * hash + (stringVal != null ? stringVal.hashCode() : 0);
			return hash;
		}


	}

	public static final class SerializableThing implements Serializable
	{
		public final String word;

		public SerializableThing(String word)
		{
			this.word = word;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == null)
			{
				return false;
			}
			if (getClass() != obj.getClass())
			{
				return false;
			}
			final SerializableThing other = (SerializableThing)obj;
			if ((word == null) ? (other.word != null) : !word.equals(other.word))
			{
				return false;
			}
			return true;
		}

		@Override
		public int hashCode()
		{
			int hash = 7;
			hash = 23 * hash + (word != null ? word.hashCode() : 0);
			return hash;
		}
	}
}
