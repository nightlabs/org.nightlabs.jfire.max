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

package org.nightlabs.jfire.accounting;

import java.util.Map;
import java.util.Set;

import org.nightlabs.jfire.person.util.PersonSearchFilter;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.jdo.search.SearchFilter;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class AccountSearchFilter extends SearchFilter {
	
	private AnchorID owner;
	private String anchorTypeID;
	private String currencyID;
	private String nameFilter;
	private PersonSearchFilter personFilter;

	/**
	 * @param _conjunction
	 */
	public AccountSearchFilter() {
		super(SearchFilter.CONJUNCTION_DEFAULT);
	}

	protected Class getExtentClass() {
		return Account.class;
	}

	protected void prepareQuery(Set imports, StringBuffer vars, StringBuffer filter, StringBuffer params, Map paramMap, StringBuffer result) {
		params.append("java.lang.String ownerOrganisationID, ");
		params.append("java.lang.String ownerAnchorTypeID, ");
		params.append("java.lang.String ownerAnchorID, ");
		params.append("java.lang.String accountAnchorTypeID, ");
		params.append("java.lang.String accountCurrencyID, ");
		params.append("java.lang.String paramNameFilter");
	
//		vars.append("java.lang.String nameFilterVar");
		
		if (owner != null) {
			paramMap.put("ownerOrganisationID", owner.organisationID);
			paramMap.put("ownerAnchorTypeID", owner.anchorTypeID);
			paramMap.put("ownerAnchorID", owner.anchorID);
			
			filter.append("( this.owner.organisationID == ownerOrganisationID && ");
			filter.append("  this.owner.anchorTypeID == ownerAnchorTypeID && ");
			filter.append("  this.owner.anchorID == ownerAnchorID )");
		}
		else {
			paramMap.put("ownerOrganisationID", null); 		
			paramMap.put("ownerAnchorTypeID", null);
			paramMap.put("ownerAnchorID", null);
		}
		
		
		paramMap.put("accountAnchorTypeID", anchorTypeID);
		if (anchorTypeID != null) {
			if (filter.length() > 0)
				filter.append(" && ");
			filter.append("(this.anchorTypeID == accountAnchorTypeID)");
		}

		paramMap.put("accountCurrencyID", currencyID);
		if (currencyID != null) {
			if (filter.length() > 0)
				filter.append(" && ");
			filter.append("(this.currency.currencyID == accountCurrencyID)");
		}
		
		paramMap.put("paramNameFilter", nameFilter);
		if (nameFilter != null) {
			vars.append("java.lang.String nameFilterVar");
			if (filter.length() > 0)
				filter.append(" && ");
			filter.append("(this.name.names.containsValue(nameFilterVar) && nameFilterVar.toLowerCase().indexOf(paramNameFilter.toLowerCase())>=0)");
		}
		
		if (personFilter != null) {
			filter.append(" && (");
			personFilter.setPersonVariableCondition(filter);
			filter.append(" && ");
			personFilter.addPersonFilterItems(imports, vars, filter, params, paramMap);
			filter.append(" && )");
		}
	}

	/**
	 * @return Returns the anchorTypeID.
	 */
	public String getAnchorTypeID() {
		return anchorTypeID;
	}

	/**
	 * @param anchorTypeID The anchorTypeID to set.
	 */
	public void setAnchorTypeID(String anchorTypeID) {
		this.anchorTypeID = anchorTypeID;
	}

	/**
	 * @return Returns the currencyID.
	 */
	public String getCurrencyID() {
		return currencyID;
	}

	/**
	 * @param currencyID The currencyID to set.
	 */
	public void setCurrencyID(String currencyID) {
		this.currencyID = currencyID;
	}

	/**
	 * @return Returns the nameFilter.
	 */
	public String getNameFilter() {
		return nameFilter;
	}

	/**
	 * @param nameFilter The nameFilter to set.
	 */
	public void setNameFilter(String nameFilter) {
		this.nameFilter = nameFilter;
	}

	/**
	 * @return Returns the owner.
	 */
	public AnchorID getOwner() {
		return owner;
	}

	/**
	 * @param owner The owner to set.
	 */
	public void setOwner(AnchorID owner) {
		this.owner = owner;
	}

	/**
	 * @return Returns the personFilter.
	 */
	public PersonSearchFilter getPersonFilter() {
		return personFilter;
	}

	/**
	 * @param personFilter The personFilter to set.
	 */
	public void setPersonFilter(PersonSearchFilter personFilter) {
		this.personFilter = personFilter;
	}
	
	


}
