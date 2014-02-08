/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright 
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.oia.HK2Utilities.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import gov.va.oia.HK2Utilities.HK2RuntimeInitializer;
import gov.va.oia.HK2Utilities.HK2RuntimeInitializerCustom;
import gov.va.oia.HK2Utilities.test.services.TestInterface;
import gov.va.oia.HK2Utilities.test.services.TestService;
import gov.va.oia.HK2Utilities.test.services.TestService2;
import gov.va.oia.HK2Utilities.test.services.TestService3;
import java.io.IOException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test - Just some basic tests for the HK2RuntimeInitializer, to ensure that 
 * it is finding the annotations it claims to support, and getting them loaded
 * into HK2 properly.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

@RunWith(JUnit4.class)
public class TestHK2Utils
{
	@BeforeClass
	public static void initialize()
	{
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
	}

	@Test
	public void testPackageList() throws ClassNotFoundException, IOException 
	{
		ServiceLocator foo = HK2RuntimeInitializer.init("SomeName", false, "gov.va.oia", "org.ihtsdo");
		ServiceLocator locator = ServiceLocatorFactory.getInstance().create("SomeName");
		assertSame(foo, locator);
		verify(locator);
	}
	
	@Test
	public void testFullSearch() throws ClassNotFoundException, IOException 
	{
		ServiceLocator foo = HK2RuntimeInitializer.init("OtherName", false);
		ServiceLocator locator = ServiceLocatorFactory.getInstance().create("OtherName");
		assertSame(foo, locator);
		verify(locator);
	}
	
	@Test
	public void testPackageListCustom() throws ClassNotFoundException, IOException 
	{
		ServiceLocator foo = HK2RuntimeInitializerCustom.init("foo", false, "gov.va.oia", "org.ihtsdo");
		ServiceLocator locator = ServiceLocatorFactory.getInstance().create("foo");
		assertSame(foo, locator);
		verify(locator);
	}
	
	@Test
	public void testFullSearchCustom() throws ClassNotFoundException, IOException 
	{
		ServiceLocator foo = HK2RuntimeInitializerCustom.init("foo2", false);
		ServiceLocator locator = ServiceLocatorFactory.getInstance().create("foo2");
		assertSame(foo, locator);
		verify(locator);
	}
	
	private void verify(ServiceLocator locator)
	{
		assertNotNull(locator.getService(TestService.class));
		assertNotNull(locator.getService(TestService2.class));
		assertNotNull(locator.getService(TestService3.class));
		assertNotNull(locator.getService(TestInterface.class));
		assertNotNull(locator.getService(TestInterface.class, "fred"));
	
		TestInterface ti = locator.getService(TestInterface.class);
		assertTrue(ti.getInstanceId() > 0);  //asking for the interface... we should get something - not sure which.
		
		ti = locator.getService(TestService.class);
		ti.reset();
		assertEquals(ti.getInstanceId(), 1);  //TestService should default to singleton - both ids should increment per call
		assertEquals(ti.getStaticId(), 1); 
		ti = locator.getService(TestService.class);
		assertEquals(ti.getInstanceId(), 2);  
		assertEquals(ti.getStaticId(), 2); 

		
		ti = locator.getService(TestService2.class);
		ti.reset();
		assertEquals(ti.getInstanceId(), 2);  //TestService2 is singleton - both ids should increment per call
		assertEquals(ti.getStaticId(), 2); 
		ti = locator.getService(TestService2.class);
		assertEquals(ti.getInstanceId(), 3);
		assertEquals(ti.getStaticId(), 3); 
		
		//by name call should give me 2
		ti = locator.getService(TestInterface.class, "fred");
		ti.reset();
		assertEquals(ti.getInstanceId(), 2);
		
		
		TestService3 ts3 = locator.getService(TestService3.class);
		ts3.reset();
		assertEquals(ts3.getInstanceId(), 3);  //TestService3 is per call- only 1 id should increment per call
		assertEquals(ts3.getStaticId(), 3); 
		ts3 = locator.getService(TestService3.class);
		assertEquals(ts3.getInstanceId(), 3);
		assertEquals(ts3.getStaticId(), 4);
		
		assertEquals(ts3.getTwoId(), 3);  //this verifies the injection worked - the id from service 2 should be sitting at 3, now.
	}
}
