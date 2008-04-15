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

package org.nightlabs.jfire.simpletrade.store;

import java.util.Map;
import java.util.Set;

import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.store.ProductTypeSearchFilter;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class SimpleProductTypeSearchFilter
	extends ProductTypeSearchFilter<SimpleProductType>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param conjunction
	 */
	public SimpleProductTypeSearchFilter(int conjunction) {
		super(conjunction);
	}

	@Override
	protected void prepareQuery(Set<Class<?>> imports, StringBuffer vars,
			StringBuffer filter, StringBuffer params, Map<String, Object> paramMap,
			StringBuffer result) {
		filter.append("this.published && this.saleable && this.organisationID == myOrganisationID");
		params.append("java.lang.String myOrganisationID");
		paramMap.put("myOrganisationID", LocalOrganisation.getLocalOrganisation(getPersistenceManager()).getOrganisationID());
	}

	@Override
	protected Class<SimpleProductType> initCandidateClass()
	{
		return SimpleProductType.class;
	}

}
