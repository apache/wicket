/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.voicetribe.wicket;

import java.io.Serializable;
import java.util.Calendar;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.protocol.http.HttpRequestCycle;
import com.voicetribe.wicket.protocol.http.MockHttpApplication;
import com.voicetribe.wicket.protocol.http.MockPage;
import com.voicetribe.wicket.resource.BundleStringResourceLoader;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Test cases for the <code>StringResourceModel</code> class.
 *
 * @author Chris Turner
 */
public class StringResourceModelTest extends TestCase {

    private MockHttpApplication application;
    private HtmlPage page;
    private WeatherStation ws;
    private Model wsModel;

    /**
     * Create the test case.
     *
     * @param name The test name
     */
    public StringResourceModelTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        application = new MockHttpApplication(null);
        application.getSettings().addStringResourceLoader(
            new BundleStringResourceLoader("com.voicetribe.wicket.StringResourceModelTest"));
        page = new MockPage(null);
        ws = new WeatherStation();
        wsModel = new Model(ws);
    }

    public void testGetSimpleResource() {
        StringResourceModel model = new StringResourceModel("simple.text", page, null);
        Assert.assertEquals("Text should be as expected", "Simple text", model.getString());
        Assert.assertEquals("Text should be as expected", "Simple text", model.getObject());
        Assert.assertEquals("Text should be as expected", "Simple text", model.toString());
    }

    public void testNullResourceKey() {
        try {
            new StringResourceModel(null, page, null);
            Assert.fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected result
        }
    }

    public void testGetSimpleResourceWithKeySubstitution() {
        StringResourceModel model = new StringResourceModel("weather.${currentStatus}", page, wsModel);
        Assert.assertEquals("Text should be as expected",
                            "It's sunny, wear sunscreen",
                            model.getString());
        ws.setCurrentStatus("raining");
        Assert.assertEquals("Text should be as expected",
                            "It's raining, take an umberella",
                            model.getString());
    }

    public void testGetOGNLResource() {
        StringResourceModel model = new StringResourceModel("weather.message", page, wsModel);
        Assert.assertEquals("Text should be as expected",
                            "Weather station reports that the temperature is 25.7 \u00B0C",
                            model.getString());
        ws.setCurrentTemperature(11.5);
        Assert.assertEquals("Text should be as expected",
                            "Weather station reports that the temperature is 11.5 \u00B0C",
                            model.getString());
    }

    public void testSubstitutionParametersResource() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.OCTOBER, 15, 13, 21);
        StringResourceModel model = new StringResourceModel(
                          "weather.detail", page, wsModel,
                          new Object[] {
                              cal.getTime(),
                              "${currentStatus}",
                              new PropertyModel(wsModel, "currentTemperature"),
                              new PropertyModel(wsModel, "units")});
        Assert.assertEquals("Text should be as expected",
                            "The report for 15-Oct-2004, shows the temparature as 25.7 \u00B0C and the weather to be sunny",
                            model.getString());
        ws.setCurrentStatus("raining");
        ws.setCurrentTemperature(11.568);
        Assert.assertEquals("Text should be as expected",
                            "The report for 15-Oct-2004, shows the temparature as 11.57 \u00B0C and the weather to be raining",
                            model.getString());
    }

    public void testUninitialisedLocalizer() {
        StringResourceModel model = new StringResourceModel("simple.text", null, null);
        try {
            model.getString();
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            // Expected result
        }
    }

    public void testSetObject() {
        StringResourceModel model = new StringResourceModel("simple.text", page, null);
        model.setObject("Some value");
        Assert.assertEquals("Text should be as expected", "Simple text", model.getString());
    }

    public void testDetachAttachNormalModel() throws Exception {
        StringResourceModel model = new StringResourceModel("simple.text", page, wsModel);
        application.setupRequestAndResponse();
        RequestCycle cycle = new HttpRequestCycle(application,
                                                  application.getWicketSession(),
                                                  application.getWicketRequest(),
                                                  application.getWicketResponse());
        model.attach(cycle);
        Assert.assertNotNull(model.getLocalizer());
        model.detach(cycle);
        Assert.assertNull(model.getLocalizer());
    }

    public void testDetachAttachDetachableModel() throws Exception {
        IModel wsDetachModel = new DetachableModel(wsModel) {
            protected void doAttach(RequestCycle cycle) {
                setObject(new WeatherStation());
            }

            protected void doDetach(RequestCycle cycle) {
                setObject(null);
            }
        };
        StringResourceModel model = new StringResourceModel("simple.text", page, wsDetachModel);
        application.setupRequestAndResponse();
        RequestCycle cycle = new HttpRequestCycle(application,
                                                  application.getWicketSession(),
                                                  application.getWicketRequest(),
                                                  application.getWicketResponse());
        model.attach(cycle);
        Assert.assertNotNull(model.getModel().getObject());
        Assert.assertNotNull(model.getLocalizer());
        model.detach(cycle);
        Assert.assertNull(model.getModel().getObject());
        Assert.assertNull(model.getLocalizer());
    }

    /**
     * Inner class used for testing.
     */
    class WeatherStation implements Serializable {

        private String currentStatus = "sunny";
        private double currentTemperature = 25.7;

        public String getCurrentStatus() {
            return currentStatus;
        }

        public void setCurrentStatus(String currentStatus) {
            this.currentStatus = currentStatus;
        }

        public double getCurrentTemperature() {
            return currentTemperature;
        }

        public void setCurrentTemperature(double currentTemperature) {
            this.currentTemperature = currentTemperature;
        }

        public String getUnits() {
            return "\u00B0C";
        }
    }

}
