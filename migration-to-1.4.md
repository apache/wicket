<!--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->
# Migration to Wicket 1.4

## Migrating to Wicket 1.4

#### Environment

- Wicket 1.4 requires at least Java 5

#### Model changes

##### Component.getModel() and friends renamed to getDefaultModel() and friends

In Wicket 1.3 each component had by default a model: a Label had a model, a Link and even BookmarkablePageLink had a model property (all because they extend Component which has a model property). When we generified IModel, this had averse effects on Component: suddenly **all** components had to be generified and had to take the type parameter of the model that was associated with it. But that poses problems for components that use two different model types: which one should be in the lead? To illustrate the removal of the default model, take a look at the following link residing within a ListView of people:

```java
ListView peopleListView = new ListView("people", people) {
        protected void populateItem(ListItem item) {
            item.add(new Link("editPerson", item.getModel()){
                public void onClick() {
                    Person p = (Person)getModelObject();
                    setResponsePage(new EditPersonPage(p));
                }
            });
        }
    };
```

You can see the type cast when we retrieve the model object from the link, just before we pass the person on to the EditPersonPage. When we migrate this code to Wicket 1.4, the example looks like this:

```java
ListView<Person> peopleListView = new ListView<Person>("people", people) {
        protected void populateItem(ListItem<Person> item) {
            item.add(new Link<Person>("editPerson", item.getModel()){
                public void onClick() {
                    Person p = getModelObject();
                    setResponsePage(new EditPersonPage(p));
                }
            });
        }
    };
```

In this second example, the generics make the intent of the code much clearer: the anonymous class extending Link inside the populateItem method is now typed to take a Person as its model object, which is provided by the ListItem—conveniently specified to deliver Person objects as well. There's a price to pay: the code is more verbose and we had to specify the Person type four times to eliminate just one cast.

The Link class provides its own getModelObject implementation, which has the signature T getModelObject. In our example, it will be typed as Person getModelObject(). However, Component doens't feature a getModelObject method anymore—enabling ways for components to forgo the problems of providing type safety.

In order to make development of components easy Component provides setDefaultModel(IModel\<?\> model) and setDefaultModelObject(Object o) with their getter analogs. This allows Component to provide model services such as model detachment and model wrapping, while allowing users an easy way to create generified versions of model accessors by delegating to these default implementations:

```java
class FormComponent<T> extends Component {

    public final void setModel(IModel<T> model)
    {
        setDefaultModel(model);
    }

    @SuppressWarnings("unchecked")
    public final IModel<T> getModel()
    {
        return (IModel<T>)getDefaultModel();
    }
}
```

#### FileUploadField - now requires model

The FileUploadField now REQUIRES a model. This means that if you previously used the ID-only constructor (i.e. new FileUploadField("myFileField")), you will need to change it to include a model (i.e. new FileUploadField("myFileField", new Model\<FileUpload\>())). Of course, if you don't do this, FileUploadField will look for a CompoundPropertyModel up the tree as other components do.

#### Registering a custom string resource loader

The API to register a custom string resource loader (`getResourceSettings()#addStringResourceLoader(IStringResourceLoader)`) no longer removes the default string resource loaders upon the first add. To make sure that your custom string resource loader is the first to be called use the method `getResourceSettings()#addStringResourceLoader(int,IStringResourceLoader)`.

#### Spring Support

##### Spring Modules

wicket-spring and wicket-spring-annot modules have been merged. If your project depends on wicket-spring-annot simply replace it with a dependency on wicket-spring.

If you are using the SpringBean annotation, you need to add wicket-ioc, starting from version 1.4-m2.

##### SpringWebApplication

SpringWebApplication has been deprecated in favor of SpringBean annotation. See SpringWebApplication javadoc for how to setup SpringBean based injection.

#### Validators

NumberValidator has been deprecated and broken into three more generic validators: RangeValidator, MinimumValidator, MaximumValidator. Likewise, the corresponding resource keys have also been changed (eg NumberValidator.maximum resource key has become MaximumValidator). NumberValidator.POSITIVE and NEGATIVE have been deprecated without a replacement, they were too confusing to the users with regard to whether or not they included zero - the same result can be easily reproduced by either Minimum or MaximumValidator.

#### Configuration type variable name change

Wicket 1.4 uses wicket.configuration instead of configuration context variable in web.xml to declare configuration type. This is done to avoid collissions with other frameworks. configuration variable is still supported, but may be removed in a future release.

#### Generics for most Objects

Most users - having wicket 1.3 code and migrating to 1.4 - will get many "compile warnings" due to generics.\
The reason is the new typesafety. The class Component got "generified" and because all Wicket components extend Component, they require a generic too.\
But for most Components that doesn't make too much sense. Therefore you should use Void as the generic.\
Some Examples:

*BookmarkablePageLink in wicket 1.3*

```java

add(new BookmarkablePageLink("signin", LoginPage.class));
```

*generified BookmarkablePageLink in wicket 1.4*

```java

add(new BookmarkablePageLink<Void>("signin", LoginPage.class));
```

#### Enclosures

Prior to 1.4.2, if an enclosure was not to be rendered, it was not mandatory to add any other children to it apart from the 'child' that determines its visibility. This is no longer true as of 1.4.2: you will have to add all children, even if the enclosure is not to be rendered.

<https://issues.apache.org/jira/browse/WICKET-2605>

The error message will be 'org.apache.wicket.WicketRuntimeException: Could not find child with id: foo in the wicket:enclosure', even though it is not the element with id 'foo' that cannot be found. The incorrect component id display has been fixed as of 1.4.5 via <https://issues.apache.org/jira/browse/WICKET-2606>
