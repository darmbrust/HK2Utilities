/*
 * Copyright 2018 VetsEZ Inc, Sagebits LLC
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
 * 
 * Contributions from 2015-2017 where performed either by US government
 * employees, or under US Veterans Health Administration contracts.
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government
 * employees are USGovWork (17USC §105). Not subject to copyright.
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 */
package net.sagebits.HK2Utilities;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.Populator;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.hk2.annotations.Service;
import eu.infomas.annotation.AnnotationDetector;

/**
 * HK2RuntimeInitializer
 * 
 * HK2 currently doesn't provide any mechanism to configure itself via annotations at runtime - instead,
 * it is done via a tool that runs at build time which creates 'inhabitant' files. But having those files
 * around is problematic at best, and makes debugging in Eclipse or other environments difficult, since it
 * relies on having the maven-tool generated file.
 * 
 * This utility alleviates those issues by allowing HK2 to be configured at runtime.
 *
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class HK2RuntimeInitializer
{
	static Logger log = LogManager.getLogger(HK2RuntimeInitializer.class);

	/**
	 * calls {@link #init(String, ServiceLocator, boolean, String...)} with the (ServiceLocator parentServiceLocator) set to null.
	 * 
	 * @param serviceLocatorName
	 * @param readInhabitantFiles
	 * @param packageNames
	 * @return the Service Locator
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static ServiceLocator init(String serviceLocatorName, boolean readInhabitantFiles, String... packageNames) throws IOException, ClassNotFoundException
	{
		return init(serviceLocatorName, null, readInhabitantFiles, packageNames);
	}

	/**
	 * Scan the requested packages on the classpath for HK2 'Service' and 'Contract' annotated classes.
	 * Load the metadata for those classes into the HK2 Service Locator.
	 * 
	 * @see org.glassfish.hk2.api.ServiceLocatorFactory#create(String)
	 * @see ServiceLocatorUtilities#createAndPopulateServiceLocator(String)
	 * 
	 * @param serviceLocatorName - The name of the ServiceLocator to find (or create if it doesn't yet exist)
	 * @param parentServiceLocator - The parent service locator - may be null.
	 * @param readInhabitantFiles - Read and process inhabitant files before doing the classpath scan. Annotated items
	 *            found during the scan will override items found in the inhabitant files, if they collide.
	 * @param packageNames -- The set of package names to scan recursively - for example - new String[]{"org.foo", "com.bar"}
	 *            If not provided, the entire classpath is scanned
	 * @return - The created ServiceLocator (but in practice, you can lookup this ServiceLocator by doing:
	 * 
	 *         <pre>
	 * {@code
	 * ServiceLocatorFactory.getInstance().create("SomeName");
	 * }
	 *         </pre>
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static ServiceLocator init(String serviceLocatorName, ServiceLocator parentServiceLocator, boolean readInhabitantFiles, String... packageNames)
			throws IOException, ClassNotFoundException
	{
		AnnotatedClasses ac = new AnnotatedClasses();

		@SuppressWarnings("unchecked")
		AnnotationDetector cf = new AnnotationDetector(new AnnotationReporter(ac, new Class[] { Service.class }));
		if (packageNames == null || packageNames.length == 0)
		{
			cf.detect();
		}
		else
		{
			cf.detect(packageNames);
		}

		ServiceLocator locator = ServiceLocatorFactory.getInstance().create(serviceLocatorName, parentServiceLocator);

		if (readInhabitantFiles)
		{
			// code borrowed from ServiceLocatorUtilities.createAndPopulateServiceLocator
			DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
			Populator populator = dcs.getPopulator();

			try
			{
				populator.populate();
			}
			catch (IOException e)
			{
				throw new MultiException(e);
			}
		}

		for (ActiveDescriptor<?> ad : ServiceLocatorUtilities.addClasses(locator, ac.getAnnotatedClasses()))
		{
			log.debug("Added " + ad.toString());
		}

		return locator;
	}
}
