/*
 * Created 	on Apr 21, 2005
 * 					by alex
 *
 */
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
