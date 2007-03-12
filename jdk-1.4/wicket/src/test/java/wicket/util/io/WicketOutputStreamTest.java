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
package wicket.util.io;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.util.GregorianCalendar;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author jcompagner
 */
public class WicketOutputStreamTest extends TestCase
{
	ByteArrayOutputStream baos;
	WicketObjectOutputStream woos;

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		baos = new ByteArrayOutputStream();
		woos = new WicketObjectOutputStream(baos);
	}
	/**
	 * @throws Exception
	 */
	public void testGregorianCalendar() throws Exception
	{
		GregorianCalendar gc = new GregorianCalendar(2005,10,10);
		
		woos.writeObject(gc);
		woos.close();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		
		WicketObjectInputStream wois = new WicketObjectInputStream(bais);
		GregorianCalendar gc2 = (GregorianCalendar)wois.readObject();
		
		Assert.assertEquals(gc, gc2);
		
	}

	
//	public void testStringsEqualsAfterSerialization() throws Exception
//	{
//		String[] strings = new String[2];
//		strings[0] = new String("wicket");
//		strings[1] = "wicket";
//		
//		assertEquals(false, strings[0] == strings[1]);
//		
//		woos.writeObject(strings);
//		woos.close();
//		
//		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
//		
//		WicketObjectInputStream wois = new WicketObjectInputStream(bais);
//		String[] strings2 = (String[])wois.readObject();
//		
//		Assert.assertEquals(strings[0], strings[1]);
//		
//		Assert.assertSame(strings[0], strings[1]);
//
//		
//	}
	
	
	public void testBigInteger() throws Exception
	{
		BigInteger bi = new BigInteger("102312302132130123230021301023");
		woos.writeObject(bi);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		
		WicketObjectInputStream wois = new WicketObjectInputStream(bais);
		BigInteger bi2 = (BigInteger)wois.readObject();
		
		Assert.assertEquals(bi, bi2);
		
	}
}
