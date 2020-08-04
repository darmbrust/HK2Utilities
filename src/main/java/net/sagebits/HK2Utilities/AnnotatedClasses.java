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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.utilities.DescriptorImpl;

/**
 * AnnotatedClasses
 *
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class AnnotatedClasses
{
	Logger log = LogManager.getLogger(this.getClass());

	private Hashtable<String, ClassInfo> annotations = new Hashtable<>();

	protected void addAnnotation(Class<? extends Annotation> annotation, String className)
	{
		ClassInfo ci = annotations.get(className);
		if (ci == null)
		{
			ci = new ClassInfo(className);
			annotations.put(className, ci);
		}
		ci.addAnnotation(annotation.getName());
	}

	protected boolean isContract(String className)
	{
		ClassInfo ci = annotations.get(className);
		if (ci != null)
		{
			return ci.isContract();
		}
		return false;
	}

	protected boolean isService(String className)
	{
		ClassInfo ci = annotations.get(className);
		if (ci != null)
		{
			return ci.isService();
		}
		return false;
	}

	public Class<?>[] getAnnotatedClasses() throws ClassNotFoundException
	{
		Class<?>[] result = new Class<?>[annotations.size()];

		int i = 0;
		for (String className : annotations.keySet())
		{
			result[i++] = Class.forName(className);
		}
		return result;
	}

	public List<DescriptorImpl> createDescriptors() throws ClassNotFoundException
	{
		ArrayList<DescriptorImpl> results = new ArrayList<>();

		for (ClassInfo ci : annotations.values())
		{
			if (ci.isService())
			{
				Class<?> c = Class.forName(ci.getName());

				DescriptorImpl di = new DescriptorImpl();
				di.setImplementation(ci.getName());

				String name = null;
				if (ci.hasAnnotation(Named.class.getName()))
				{
					name = ((Named) c.getAnnotation(Named.class)).value();
				}
				if (name == null || name.length() == 0)
				{
					name = ci.getName().substring(ci.getName().lastIndexOf('.') + 1);
				}

				di.setName(name);
				di.addAdvertisedContract(ci.getName());

				for (String contract : getParentContracts(c.getInterfaces()))
				{
					di.addAdvertisedContract(contract);
				}

				for (String contract : getParentContracts(Class.forName(ci.getName()).getSuperclass()))
				{
					di.addAdvertisedContract(contract);
				}

				String scope = ci.getScope();
				if (scope != null)
				{
					di.setScope(ci.getScope());
				}
				else
				{
					di.setScope(Singleton.class.getName());
				}

				if (ci.isProxyable())
				{
					di.setProxiable(true);
				}
				else if (ci.isUnproxyable())
				{
					di.setProxiable(false);
				}

				results.add(di);
				log.debug("Created descriptor {}", di.toString());
			}
		}
		return results;
	}

	/**
	 * for parent classes
	 */
	private ArrayList<String> getParentContracts(Class<?> parentClass)
	{
		ArrayList<String> result = new ArrayList<>();
		if (parentClass == null)
		{
			return result;
		}
		if (isContract(parentClass.getName()) || isService(parentClass.getName()))
		{
			result.add(parentClass.getName());
		}

		for (String contract : getParentContracts(parentClass.getInterfaces()))
		{
			result.add(contract);
		}
		result.addAll(getParentContracts(parentClass.getSuperclass()));
		return result;
	}

	/**
	 * For interfaces
	 */
	private ArrayList<String> getParentContracts(Class<?>[] parentInterfaces)
	{
		ArrayList<String> result = new ArrayList<>();
		if (parentInterfaces == null)
		{
			return result;
		}

		for (Class<?> c : parentInterfaces)
		{
			if (isContract(c.getName()) || isService(c.getName()))
			{
				result.add(c.getName());
			}
			result.addAll(getParentContracts(c.getInterfaces()));
		}
		return result;
	}
}
