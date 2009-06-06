/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.reporting.layout;

import java.util.Iterator;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.reporting.Birt;
import org.nightlabs.jfire.reporting.Birt.OutputFormat;
import org.nightlabs.jfire.reporting.classloader.ReportingClassLoader;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryID;
import org.nightlabs.jfire.reporting.layout.render.ReportLayoutRenderer;

/**
 * Singleton to hold the id of next new {@link org.nightlabs.jfire.reporting.layout.ReportRegistryItem}
 * and registrations of {@link ReportLayoutRenderer} to {@link OutputFormat}.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class = "org.nightlabs.jfire.reporting.layout.id.ReportRegistryID"
 *		detachable="true"
 *		table="JFireReporting_ReportRegistry"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.inheritance strategy="new-table"
 */
@PersistenceCapable(
	objectIdClass=ReportRegistryID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireReporting_ReportRegistry")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ReportRegistry {

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private int reportRegistryID = 0;

	// TODO: Change to use IDGenerator.
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long newReportItemID = 0;

	/**
	 * key: String format (see {@link Birt.OutputFormat})<br/>
	 * value: String reportRendererClassName (fully qualified name of a class extending {@link ReportLayoutRenderer})
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireReporting_ReportRegistry_format2ReportRendererClassName"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireReporting_ReportRegistry_format2ReportRendererClassName",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Map<String, String> format2ReportRendererClassName;

	/**
	 * @deprecated Only for JDO
	 */
	@Deprecated
	protected ReportRegistry() {
		super();
	}

	public ReportRegistry(int reportRegistryID) {
		this.reportRegistryID = reportRegistryID;
	}

	public long getNewReportItemID() {
		return newReportItemID;
	}

	public int getReportRegistryID() {
		return reportRegistryID;
	}

	/**
	 * Register the given ReportRender class to a Birt OutputFormat.
	 * The ReportLayoutRenderer will be instantiated and asked for the format.
	 *
	 * @param clazz The class to register.
	 * @throws InstantiationException When instantiating fails.
	 * @throws IllegalAccessException When instantiating fails.
	 */
	public void registerReportRenderer(String format, Class<? extends ReportLayoutRenderer> clazz)
	throws InstantiationException, IllegalAccessException
	{
		if (!ReportLayoutRenderer.class.isAssignableFrom(clazz))
			throw new ClassCastException("Class " + clazz.getName() + " does not implement " + ReportLayoutRenderer.class.getName());

		ReportLayoutRenderer renderer = clazz.newInstance();
		if (renderer != null) {
			unbindFormat(format);
			format2ReportRendererClassName.put(format.toString(), clazz.getName());
		}
	}

	/**
	 * Unbinds the registration of a class-name to the given Birt OutputFormat.
	 *
	 * @param format The format to unbind.
	 */
	public void unbindFormat(String format) {
		format2ReportRendererClassName.remove(format.toString());
	}


	/**
	 * Returns the class registered to the given Birt OutputFormat.
	 *
	 * @param format The format to search the class for.
	 * @param throwExceptionIfNotFound If true and no format can be found an {@link IllegalArgumentException} will be thrown
	 * @return The class registered to the given Birt OutputFormat.
	 * @throws ClassNotFoundException When the registered class can not be found in the classpath.
	 * @throws IllegalArgumentException When no registration could be found and throwExceptionIfNotFound is true
	 */
	public Class<? extends ReportLayoutRenderer> getReportRendererClass(OutputFormat format, boolean throwExceptionIfNotFound)
	throws ClassNotFoundException, IllegalArgumentException
	{
		String className = format2ReportRendererClassName.get(format.toString());
		if (className == null) {
			if (throwExceptionIfNotFound)
				throw new IllegalArgumentException("The format \"" + format + "\" is unknown: No ReportLayoutRenderer class bound!");

			return null;
		}

//		return (Class<? extends ReportLayoutRenderer>) Class.forName(className);
		return (Class<? extends ReportLayoutRenderer>) ReportingClassLoader.sharedInstance().loadClass(className);
	}

	/**
	 * Creates a new instance of the {@link ReportLayoutRenderer} implementation registered
	 * to the given format and returns it.
	 *
	 * @param format The format the renderer should be searched for.
	 * @return A new instance of the registered {@link ReportLayoutRenderer}
	 * @throws IllegalArgumentException When no registration could be found.
	 * @throws ClassNotFoundException When the registered class could not be found.
	 * @throws InstantiationException When the renderer could not be instantiated.
	 * @throws IllegalAccessException When the renderer could not be instantiated.
	 */
	public ReportLayoutRenderer createReportRenderer(OutputFormat format)
	throws IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		return getReportRendererClass(format, true).newInstance();
	}

	/**
	 * Returns the current newReportCategoryID
	 * before incrementing it by one. Equivalent to
	 * <code>newReportCategoryID++</code>
	 */
	public long createNewReportItemID() {
		long result = getNewReportItemID();
		newReportItemID = result + 1;
		return result;
	}




	public static final int SINGLETON_REGISTRY_ID = 0;
	public static final ReportRegistryID SINGLETON_ID = ReportRegistryID.create(SINGLETON_REGISTRY_ID);

	public static ReportRegistry getReportRegistry(PersistenceManager pm) {
		Iterator it = pm.getExtent(ReportRegistry.class).iterator();
		ReportRegistry registry;
		if (it.hasNext()) {
			registry = (ReportRegistry)it.next();
		}
		else {
			registry = new ReportRegistry(SINGLETON_REGISTRY_ID);
			registry = pm.makePersistent(registry);
		}
		return registry;
	}

}
