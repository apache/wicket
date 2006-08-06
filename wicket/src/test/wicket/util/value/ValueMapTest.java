/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) eelco12 $
 * $Revision: 5004 $
 * $Date: 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.util.value;

import junit.framework.TestCase;

/**
 * @author jcompagner
 */
public class ValueMapTest extends TestCase
{
	/**
	 * @throws Exception
	 */
	public void testStringParseConstructor() throws Exception
	{
		ValueMap vm = new ValueMap("param=value");
		assertEquals(1, vm.size());
		assertEquals("value", vm.get("param"));
		

		vm = new ValueMap("param1=value1,param2=value2");
		assertEquals(2, vm.size());
		assertEquals("value1", vm.get("param1"));
		assertEquals("value2", vm.get("param2"));

		vm = new ValueMap("param1=value1;param2=value2", ";");
		assertEquals(2, vm.size());
		assertEquals("value1", vm.get("param1"));
		assertEquals("value2", vm.get("param2"));

		vm = new ValueMap("param1=val>ue1;param2=value2", ";");
		assertEquals(2, vm.size());
		assertEquals("val>ue1", vm.get("param1"));
		assertEquals("value2", vm.get("param2"));

		vm = new ValueMap("param1=val:ue1;param2=value2", ";");
		assertEquals(2, vm.size());
		assertEquals("val:ue1", vm.get("param1"));
		assertEquals("value2", vm.get("param2"));

		vm = new ValueMap("param1=val?ue1;param2=value2", ";");
		assertEquals(2, vm.size());
		assertEquals("val?ue1", vm.get("param1"));
		assertEquals("value2", vm.get("param2"));

		vm = new ValueMap("param1=val=ue1;param2=value2", ";");
		assertEquals(2, vm.size());
		assertEquals("val=ue1", vm.get("param1"));
		assertEquals("value2", vm.get("param2"));
		
	}
}
