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
package org.apache.wicket.util.io;

import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.Locale;

import junit.framework.Assert;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.io.IObjectStreamFactory.DefaultObjectStreamFactory;
import org.apache.wicket.util.lang.Objects;

/**
 * @author jcompagner
 */
public class WicketOutputStreamTest extends WicketTestCase
{
	ByteArrayOutputStream baos;
	WicketObjectOutputStream woos;

	/**
	 * Tests serialization of a big int.
	 * 
	 * @throws Exception
	 */
	public void testBigInteger() throws Exception
	{
		BigInteger bi = new BigInteger("102312302132130123230021301023");
		woos.writeObject(bi);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		WicketObjectInputStream wois = new WicketObjectInputStream(bais);
		BigInteger bi2 = (BigInteger)wois.readObject();

		Assert.assertEquals(bi, bi2);

	}

	/**
	 * @throws Exception
	 */
	public void testGregorianCalendar() throws Exception
	{
		GregorianCalendar gc = new GregorianCalendar(2005, 10, 10);

		woos.writeObject(gc);
		woos.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		WicketObjectInputStream wois = new WicketObjectInputStream(bais);
		GregorianCalendar gc2 = (GregorianCalendar)wois.readObject();

		Assert.assertEquals(gc, gc2);

	}


	 public void testNotSerializeable() throws Exception
	 {
		 WebApplication app = new WebApplication()
		 {
			 public Class getHomePage()
			 {
				 return null;
			 }
		 };
				
		 try
		 {
			 woos.writeObject(app);
			 assertFalse("webapplication is not serializeable",false);
		 } 
		 catch(Exception e){ }
	 }
	 
	 public void testLocale() throws Exception
	 {
		 Locale locale = new Locale("nl","NL");
		 woos.writeObject(locale);

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		WicketObjectInputStream wois = new WicketObjectInputStream(bais);
		Locale locale2 = (Locale)wois.readObject();

		Assert.assertEquals(locale, locale2);

	 }

	public void testPageReference() throws Exception
	{
		PageB b = new PageB("test");
		PageA a = new PageA(b);
		b.setA(a);
		
		woos.writeObject(a);

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		WicketObjectInputStream wois = new WicketObjectInputStream(bais);
		PageA a2 = (PageA)wois.readObject();

		Assert.assertEquals(a, a2);
		
		Assert.assertSame(a2, a2.getB().getA());

		Objects.setObjectStreamFactory(new DefaultObjectStreamFactory());
		
		byte[] array = Objects.objectToByteArray(a);
		PageA aa = (PageA) Objects.byteArrayToObject(array);
		
		Assert.assertEquals(a, aa);
		
		Assert.assertSame(aa, aa.getB().getA());
	}
	 

	// public void testStringsEqualsAfterSerialization() throws Exception
	// {
	// String[] strings = new String[2];
	// strings[0] = new String("wicket");
	// strings[1] = "wicket";
	//		
	// assertEquals(false, strings[0] == strings[1]);
	//		
	// woos.writeObject(strings);
	// woos.close();
	//		
	// ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	//		
	// WicketObjectInputStream wois = new WicketObjectInputStream(bais);
	// String[] strings2 = (String[])wois.readObject();
	//		
	// Assert.assertEquals(strings[0], strings[1]);
	//		
	// Assert.assertSame(strings[0], strings[1]);
	//
	//		
	// }

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
		baos = new ByteArrayOutputStream();
		woos = new WicketObjectOutputStream(baos);
	}
}
