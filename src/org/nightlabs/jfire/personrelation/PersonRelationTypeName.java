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

package org.nightlabs.jfire.personrelation;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Column;
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
import org.nightlabs.jfire.personrelation.id.PersonRelationTypeNameID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author marco schulze - marco at nightlabs dot de
 */
@PersistenceCapable(
	objectIdClass=PersonRelationTypeNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFirePersonRelation_PersonRelationTypeName"
)
//@FetchGroups(
//	@FetchGroup(
//		name="PersonRelationType.name",
//		members={@Persistent(name="accountType"), @Persistent(name="names")})
//)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class PersonRelationTypeName extends I18nText
{
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String personRelationTypeID;

//	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
//	private PersonRelationType personRelationType;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected PersonRelationTypeName() {
	}

	public PersonRelationTypeName(PersonRelationType personRelationType) {
//		this.personRelationType = personRelationType;
		this.organisationID = personRelationType.getOrganisationID();
		this.personRelationTypeID = personRelationType.getPersonRelationTypeID();
		this.names = new HashMap<String, String>();
	}

	/**
	 * key: String languageID<br/>
	 * value: String name
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFirePersonRelation_PersonRelationTypeName_names",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT
	)
	protected Map<String, String> names;

	@Override
	protected Map<String, String> getI18nMap() {
		return names;
	}

	@Override
	protected String getFallBackValue(String languageID) {
		return personRelationTypeID;
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public String getPersonRelationTypeID() {
		return personRelationTypeID;
	}

//	public PersonRelationType getPersonRelationType() {
//		return personRelationType;
//	}
}
