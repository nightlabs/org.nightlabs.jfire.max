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

package org.nightlabs.jfire.trade.config;

import java.util.ArrayList;
import java.util.List;

import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.person.PersonStruct;

/**
 * 
 * ConfigModule for the LegalEntity view. Wich Fields to display in
 * wich order etc.
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 * 		persistence-capable-superclass="org.nightlabs.jfire.config.ConfigModule"
 *		detachable="true"
 *		table="JFireTrade_LegalEntityViewConfigModule"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class LegalEntityViewConfigModule extends ConfigModule {

	/**
	 * 
	 */
	protected LegalEntityViewConfigModule() {
		super();
	}

	public LegalEntityViewConfigModule(String organisationID, Config config, String cfModID) {
		super(organisationID, config, cfModID);
	}
	
	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="java.lang.String"
	 *		table="JFireTrade_LegalEntityViewConfigModule_personStructFields"
	 *
	 * @jdo.join
	 */
	private List personStructFields = new ArrayList();
	
	public List getStructFields() {
		return personStructFields;
	}

	public void init() {
		personStructFields.add(PersonStruct.PERSONALDATA_COMPANY.toString());
		personStructFields.add(PersonStruct.PERSONALDATA_NAME.toString());
		personStructFields.add(PersonStruct.PERSONALDATA_FIRSTNAME.toString());
		
		personStructFields.add(PersonStruct.POSTADDRESS_ADDRESS.toString());
		personStructFields.add(PersonStruct.POSTADDRESS_POSTCODE.toString());
		personStructFields.add(PersonStruct.POSTADDRESS_CITY.toString());
		personStructFields.add(PersonStruct.INTERNET_EMAIL.toString());
	}
	
}
