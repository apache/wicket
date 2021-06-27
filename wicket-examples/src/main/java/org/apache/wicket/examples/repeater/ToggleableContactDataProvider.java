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
package org.apache.wicket.examples.repeater;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.toggle.IToggleableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;


/**
 * implementation of IToggleableDataProvider for contacts that keeps track of sort information
 *
 * @author Roland Kurucz
 */
public class ToggleableContactDataProvider extends SortableDataProvider<Contact, String> implements IToggleableDataProvider<Contact> {
    private final ContactFilter contactFilter = new ContactFilter();

    /**
     * constructor
     */
    public ToggleableContactDataProvider() {
        // set default sort
        setSort("firstName", SortOrder.ASCENDING);
    }

    protected ContactsDatabase getContactsDB() {
        return DatabaseLocator.getDatabase();
    }

    @Override
    public Iterator<Contact> iterator(long first, long count) {
        List<Contact> contactsFound = getContactsDB().getIndex(getSort());

        return filterContacts(contactsFound).
                subList((int) first, (int) (first + count)).
                iterator();
    }

    private List<Contact> filterContacts(List<Contact> contactsFound) {
        ArrayList<Contact> result = new ArrayList<>();
        Date dateFrom = contactFilter.getDateFrom();
        Date dateTo = contactFilter.getDateTo();

        for (Contact contact : contactsFound) {
            Date bornDate = contact.getBornDate();

            if (dateFrom != null && bornDate.before(dateFrom)) {
                continue;
            }

            if (dateTo != null && bornDate.after(dateTo)) {
                continue;
            }

            result.add(contact);
        }

        return result;
    }

    @Override
    public long size() {
        return filterContacts(getContactsDB().getIndex(getSort())).size();
    }

    @Override
    public IModel<Contact> model(Contact object) {
        return new DetachableContactModel(object);
    }

    @Override
    public boolean isToggleable(Contact parent) {
        return Optional.ofNullable(parent)
                .map(Contact::getId)
                .filter(id -> id % 2 == 0)
                .isPresent();
    }
}
