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

package org.nightlabs.jfire.accounting.book;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;
/**
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.book.id.LocalAccountantDelegateNameID"
 *		detachable="true"
 *		table="JFireTrade_LocalAccountantDelegateName"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.create-objectid-class
 *		field-order="organisationID, localAccountantDelegateID"
 */
public class LocalAccountantDelegateName extends I18nText {

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String localAccountantDelegateID;
	
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private LocalAccountantDelegate localAccountantDelegate;
	
	/**
	 * @deprecated Only for JDO!
	 */
	protected LocalAccountantDelegateName() {
	}
	
	public LocalAccountantDelegateName(LocalAccountantDelegate localAccountantDelegate) {
		this.localAccountantDelegate = localAccountantDelegate;
		this.organisationID = localAccountantDelegate.getOrganisationID();
		this.localAccountantDelegateID = localAccountantDelegate.getLocalAccountantDelegateID();
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
	 *		dependent="true"
	 *		default-fetch-group="true"
	 *		table="JFireTrade_LocalAccountantDelegateName_names"
	 *
	 * @jdo.join
	 */
	protected Map names = new HashMap();
	
	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	protected Map getI18nMap() {
		return names;
	}

	/**
	 * This variable contains the name in a certain language after localization.
	 *
	 * @see #localize(String)
	 * @see #detachCopyLocalized(String, javax.jdo.PersistenceManager)
	 *
	 * @jdo.field persistence-modifier="transactional" default-fetch-group="false"
	 */
	protected String name;

	/**
	 * @see org.nightlabs.i18n.I18nText#setText(java.lang.String)
	 */
	protected void setText(String localizedValue) {
		name = localizedValue;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getText()
	 */
	public String getText() {
		return name;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	protected String getFallBackValue(String languageID) {
		return localAccountantDelegateID;
	}

	public String getOrganisationID() {
		return organisationID;
	}
	
	public String getLocalAccountantDelegateID() {
		return localAccountantDelegateID;
	}
	
	public LocalAccountantDelegate getLocalAccountantDelegate() {
		return localAccountantDelegate;
	}

}
