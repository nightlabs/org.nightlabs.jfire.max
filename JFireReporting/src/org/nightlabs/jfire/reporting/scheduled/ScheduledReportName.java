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

package org.nightlabs.jfire.reporting.scheduled;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.reporting.scheduled.id.ScheduledReportNameID;

/**
 * I18n-Name for {@link ScheduledReport}s.
 *   
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
@PersistenceCapable(
	objectIdClass=ScheduledReportNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireReporting_ScheduledReportName")
@FetchGroups(
	@FetchGroup(
		name="ScheduledReport.name",
		members={@Persistent(name="scheduledReport"), @Persistent(name="names")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ScheduledReportName extends I18nText {
	
	private static final long serialVersionUID = 20100106L;
	
	/** pk-part of {@link ScheduledReport} */
	@PrimaryKey
	private String organisationID;
	
	/** pk-part of {@link ScheduledReport} */
	@PrimaryKey
	private long scheduledReportID;
	
	/** The {@link ScheduledReport} this is linked to */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private ScheduledReport scheduledReport;
	
	
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ScheduledReportName() {
	}

	public ScheduledReportName(ScheduledReport scheduledReport) {
		this.organisationID = scheduledReport.getOrganisationID();
		this.scheduledReportID = scheduledReport.getScheduledReportID();
		this.scheduledReport = scheduledReport;
		this.names = new HashMap<String, String>();
	}

	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireReporting_ScheduledReportName_names",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, String> names;
	
	/**
	 * @see com.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map<String, String> getI18nMap() {
		return names;
	}

	/**
	 * @see com.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID) {
		return ObjectIDUtil.longObjectIDFieldToString(scheduledReport.getScheduledReportID());
	}

	/**
	 * @return The pk-part of this {@link ScheduledReportName} (corresponds to
	 *         the pk-part of the linked {@link ScheduledReport})
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return The pk-part of this {@link ScheduledReportName} (corresponds to
	 *         the pk-part of the linked {@link ScheduledReport})
	 */
	public long getScheduledReportID() {
		return scheduledReportID;
	}
	
	/**
	 * @return The linked {@link ScheduledReport})
	 */
	public ScheduledReport getScheduledReport() {
		return scheduledReport;
	}
}
