/*
 * $Id$
 * $Revision$ $Date$
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
package wicket;


/**
 * This is just a marker interface which may only be used in combination
 * with IComponentResolver. Usually AutoLinkResolver will getParent() of
 * all IComponentResolver's to find the proper component. The existence
 * of IComponentResolverMarker will interrupt that loop and return the
 * component implementing IComponentResolverMarker. This e.g. is the case
 * for Border. 
 * 
 * @author Juergen Donnerstag
 */
public interface IComponentResolverMarker
{
}
