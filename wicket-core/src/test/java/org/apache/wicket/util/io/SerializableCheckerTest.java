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

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serializable;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.core.util.io.SerializableChecker;
import org.apache.wicket.core.util.objects.checker.ObjectChecker;
import org.apache.wicket.util.Log4jEventHistory;
import org.apache.wicket.util.value.ValueMap;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pedro Santos
 */
public class SerializableCheckerTest extends Assert
{

	/**
	 * Test {@link ValueMap} serializability.
	 * 
	 * @throws IOException
	 */
	@Test
	public void valueMap() throws IOException
	{
		SerializableChecker checker = new SerializableChecker(new NotSerializableException());
		checker.writeObject(new ValueMap());
	}

	/**
	 * Asserting an meaningful message get logged on console when serializable checker is testing
	 * problematic {@link Object#equals(Object)} method implementations.
	 * 
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3354">WICKET-3354</a>
	 * @throws IOException
	 */
	@Test
	public void runtimeExceptionTolerance() throws IOException
	{
		Logger logger = LogManager.getLogger(ObjectChecker.class);
		logger.setLevel(Level.WARN);
		Log4jEventHistory logHistory = new Log4jEventHistory();
		logger.addAppender(logHistory);
		SerializableChecker serializableChecker = new SerializableChecker(
			new NotSerializableException());
		try
		{
			serializableChecker.writeObject(new TestType1());
			String expectedMessage = "Wasn't possible to check the object 'class org.apache.wicket.util.io.SerializableCheckerTest$ProblematicType' possible due an problematic implementation of equals method";
			assertTrue(logHistory.contains(Level.WARN, expectedMessage));
		}
		catch (TestException notMeaningfulException)
		{
			fail("Should have just logged on console, the checker is after another problem");
		}
	}

	private static class TestType1 implements Serializable
	{
		private static final long serialVersionUID = 1L;
		ProblematicType problematicType = new ProblematicType();
	}

	private static class TestType2 implements Serializable
	{
		private static final long serialVersionUID = 1L;
		ProblematicType problematicType = new ProblematicType();
		SerializableType serializableType = new SerializableType();
		NonSerializableType nonSerializable = new NonSerializableType();
	}

	private static class NonSerializableType
	{
	}

	private static class SerializableType implements Serializable
	{
		private static final long serialVersionUID = 1L;
	}
	private static class TestException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

	}
	private static class ProblematicType implements Serializable
	{
		private static final long serialVersionUID = 1L;

		@Override
		public boolean equals(Object obj)
		{
			throw new TestException();
		}
	}
}
