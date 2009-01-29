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

package org.nightlabs.jfire.store;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.transfer.Anchor;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.RepositoryNameID"
 *		detachable="true"
 *		table="JFireTrade_RepositoryName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, anchorTypeID, anchorID"
 *
 * @jdo.fetch-group name="Repository.name" fields="repository, names"
 */
public class RepositoryName extends I18nText
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String anchorTypeID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String anchorID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Repository repository;


	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected RepositoryName() { }

	public RepositoryName(Repository repository) {
		this.organisationID = repository.getOrganisationID();
		this.anchorTypeID = repository.getAnchorTypeID();
		this.anchorID = repository.getAnchorID();
		this.repository = repository;
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
	 *		table="JFireTrade_RepositoryName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	protected Map<String, String> names;

	@Override
	protected Map<String, String> getI18nMap() {
		return names;
	}

	@Override
	protected String getFallBackValue(String languageID) {
		return Anchor.getPrimaryKey(organisationID, anchorTypeID, anchorID);
//		return anchorID;
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public String getAnchorTypeID()
	{
		return anchorTypeID;
	}

	public String getAnchorID() {
		return anchorID;
	}

	public Repository getRepository()
	{
		return repository;
	}

}
