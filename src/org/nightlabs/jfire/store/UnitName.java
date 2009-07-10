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
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.store;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Column;
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
import org.nightlabs.jfire.store.id.UnitNameID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
@PersistenceCapable(
	objectIdClass=UnitNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_UnitName")
@FetchGroups(
	@FetchGroup(
		name="Unit.name",
		members={@Persistent(name="names")}
//		members={@Persistent(name="unit"), @Persistent(name="names")}
	)
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class UnitName
extends I18nText
{
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=50)
	private String unitID;

//	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
//	private Unit unit;

	/**
	 * key: String languageID<br/>
	 * value: String name
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_UnitName_names",
		persistenceModifier=PersistenceModifier.PERSISTENT
	)
	private Map<String, String> names = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected UnitName() { }

	public UnitName(Unit unit)
	{
//		this.unit = unit;
		this.organisationID = unit.getOrganisationID();
		this.unitID = unit.getUnitID();
	}

	@Override
	protected Map<String, String> getI18nMap()
	{
		return names;
	}

	@Override
	protected String getFallBackValue(String languageID)
	{
		return String.valueOf(unitID);
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}

	public String getUnitID()
	{
		return unitID;
	}

//	public Unit getUnit()
//	{
//		return unit;
//	}
}
