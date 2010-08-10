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

package org.nightlabs.jfire.reporting.layout;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jdo.search.SearchFilterItem;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ReportCategorySearchFilterItem extends SearchFilterItem {

	private static final long serialVersionUID = 1L;
	
	public static final int INTERNAL_FILTER_BOTH = 0;
	public static final int INTERNAL_FILTER_INTERNAL = 1;
	public static final int INTERNAL_FILTER__NOT_INTERNAL = 2;
	
	private int internalFilter = INTERNAL_FILTER_BOTH;
	
	private String nameFilter = null;
	
	/**
	 * @param matchType
	 * @param needle
	 */
	public ReportCategorySearchFilterItem(MatchType matchType) {
		super(matchType);
	}

//	/* (non-Javadoc)
//	 * @see com.nightlabs.jdo.search.SearchFilterItem#getSearchField()
//	 */
//	@Override
//	public Object getSearchField() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
	/* (non-Javadoc)
	 * @see com.nightlabs.jdo.search.SearchFilterItem#isConstraint()
	 */
	@Override
	public boolean isConstraint() {
		return (nameFilter != null) || (internalFilter != INTERNAL_FILTER_BOTH);
	}
//
//	/* (non-Javadoc)
//	 * @see com.nightlabs.jdo.search.SearchFilterItem#appendSubQuery(int, int, java.util.Set, java.lang.StringBuffer, java.lang.StringBuffer, java.lang.StringBuffer, java.util.Map)
//	 */
//	@Override
//	public void appendSubQuery(int itemIndex, Set imports, StringBuffer vars,
//			StringBuffer filter, StringBuffer params, Map paramMap) {
//
//	}
	
	public int getInternalFilter() {
		return internalFilter;
	}
	
	public String getNameFilter() {
		return nameFilter;
	}
	
	public void setInternalFilter(int internalFilter) {
		this.internalFilter = internalFilter;
	}
	
	public void setNameFilter(String nameFilter) {
		this.nameFilter = nameFilter;
	}

	@Override
	public void appendSubQuery(int itemIndex, Set<Class<?>> imports, StringBuffer vars, StringBuffer filter, StringBuffer params,
			Map<String, Object> paramMap) {
		filter.append("(");
		if (nameFilter != null)
			filter.append("this.name.names.contains()");
		// TODO: Howto search for name in map ...
		
		filter.append(")");
	}
	
	public static final EnumSet<MatchType> SUPPORTED_MATCH_TYPES = EnumSet.of(MatchType.CONTAINS);

	@Override
	public EnumSet<MatchType> getSupportedMatchTypes() {
		return SUPPORTED_MATCH_TYPES;
	}

}
