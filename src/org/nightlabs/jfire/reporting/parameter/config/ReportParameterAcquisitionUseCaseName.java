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

package org.nightlabs.jfire.reporting.parameter.config;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * Name i18n text for {@link ReportParameterAcquisitionUseCase}s.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.reporting.parameter.config.id.ReportParameterAcquisitionUseCaseNameID"
 *		detachable="true"
 *		table="JFireReporting_ReportParameterAcquisitionUseCaseName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, reportParameterAcquisitionSetupID, reportParameterAcquisitionUseCaseID"
 *
 * @jdo.fetch-group name="ReportParameterAcquisitionUseCase.name" fields="useCase, names"
 */
public class ReportParameterAcquisitionUseCaseName extends I18nText implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private long reportParameterAcquisitionSetupID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String reportParameterAcquisitionUseCaseID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ReportParameterAcquisitionUseCase useCase;
	
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ReportParameterAcquisitionUseCaseName() {
	}

	public ReportParameterAcquisitionUseCaseName(ReportParameterAcquisitionUseCase useCase) {
		this.organisationID = useCase.getOrganisationID();
		this.reportParameterAcquisitionSetupID = useCase.getReportParameterAcquisitionSetupID();
		this.reportParameterAcquisitionUseCaseID = useCase.getReportParameterAcquisitionUseCaseID();
		this.useCase = useCase;
		this.names = new HashMap<String, String>();
	}

	/**
	 * key: String languageID<br/>
	 * value: String name
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		default-fetch-group="true"
	 *		table="JFireReporting_ReportParameterAcquisitionUseCaseName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
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
		return organisationID + "/" + String.valueOf(reportParameterAcquisitionSetupID) + "/" + String.valueOf(reportParameterAcquisitionUseCaseID);
	}

	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return the reportParameterAcquisitionSetupID
	 */
	public long getReportParameterAcquisitionSetupID() {
		return reportParameterAcquisitionSetupID;
	}

	/**
	 * @return the reportParameterAcquisitionUseCaseID
	 */
	public String getReportParameterAcquisitionUseCaseID() {
		return reportParameterAcquisitionUseCaseID;
	}

	/**
	 * @return the useCase
	 */
	public ReportParameterAcquisitionUseCase getUseCase() {
		return useCase;
	}
	
}
