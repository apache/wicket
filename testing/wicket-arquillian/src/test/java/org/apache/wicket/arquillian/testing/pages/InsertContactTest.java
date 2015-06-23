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
package org.apache.wicket.arquillian.testing.pages;

import static org.junit.Assert.*;

import java.util.List;

import javax.inject.Inject;

import org.apache.wicket.arquillian.testing.dao.ContactDao;
import org.apache.wicket.arquillian.testing.deployment.AbstractDeploymentTest;
import org.apache.wicket.arquillian.testing.model.Contact;
import org.apache.wicket.util.tester.FormTester;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <b>WARNING: If this error occurs - org.jboss.arquillian.container.spi.client.container.LifecycleException: The server is already running! Managed containers do not support connecting to running server instances due to the possible harmful effect of connecting to the wrong server. Please stop server before running or change to another type of container.
 *	To disable this check and allow Arquillian to connect to a running server, set allowConnectingToRunningServer to true in the container configuration.</b>
 *	
 *	<b>SOLUTION: Search and kill wildfly or jboss proccess instance that are using port 8080.</b>
 * 
 * <b> If you can't run inside eclipse, add as source the folder src/test/resources and try again. </b>
 * 
 * Just a class test to show that everything is working.
 * 
 * @author felipecalmeida
 * @since 06/21/2015
 *
 */
@RunWith(Arquillian.class)
public class InsertContactTest extends AbstractDeploymentTest {
	
	private static final String EMAIL_IS_REQUIRED = "'email' is required.";

	private static final String NAME_IS_REQUIRED = "'name' is required.";

	private static final String INSERT_FORM = "insertForm";

	private static final String EMAIL = "email";

	private static final String NAME = "name";

	private static final String WICKET_ARQUILLIAN_TEST_APACHE_ORG = "wicket-arquillian-test@apache.org";

	private static final String WICKET_ARQUILLIAN_TEST = "Wicket Arquillian Test";

	private static final Logger log = LoggerFactory.getLogger(InsertContactTest.class);

	@Inject
    private ContactDao contactDao;
	
	@Test
	public void testErrorMessagesInsertContact() {
		Class<InsertContact> pageClass = InsertContact.class;
		getWicketTester().startPage(pageClass);
		getWicketTester().assertRenderedPage(pageClass);
		
		FormTester formTester = getWicketTester().newFormTester(INSERT_FORM);
		formTester.submit();
		getWicketTester().assertErrorMessages(NAME_IS_REQUIRED, EMAIL_IS_REQUIRED);
		log.info("Required Messages: " + NAME_IS_REQUIRED + " and " + EMAIL_IS_REQUIRED);
		
		getWicketTester().assertRenderedPage(pageClass);
	}
	
	@Test
	public void testInsertContact() {
		Class<InsertContact> pageClass = InsertContact.class;
		getWicketTester().startPage(pageClass);
		getWicketTester().assertRenderedPage(pageClass);
		
		FormTester formTester = getWicketTester().newFormTester(INSERT_FORM);
		formTester.setValue(NAME, WICKET_ARQUILLIAN_TEST);
		formTester.setValue(EMAIL, WICKET_ARQUILLIAN_TEST_APACHE_ORG);
		formTester.submit();
		
		getWicketTester().assertNoErrorMessage();
		getWicketTester().assertRenderedPage(ListContacts.class);
		
		log.info("Retrieving contacts to assert:");
		List<Contact> contacts = contactDao.getContacts();
		int contactsSize = contacts.size();
		assertEquals(1, contactsSize);
		
		Contact contact = contacts.get(0);
		assertNotNull(contact.getId());
		assertEquals(WICKET_ARQUILLIAN_TEST,contact.getName());
		assertEquals(WICKET_ARQUILLIAN_TEST_APACHE_ORG,contact.getEmail());
		
		log.info("Contacts size: " + contactsSize);
		for (Contact infoContact : contacts) {
			log.info("Contacts info: " + infoContact);
		}
	}

}
