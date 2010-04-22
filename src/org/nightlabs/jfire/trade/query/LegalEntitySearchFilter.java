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

package org.nightlabs.jfire.trade.query;

import java.util.Collection;
import java.util.LinkedList;

import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jfire.prop.search.PropSearchFilter;
import org.nightlabs.jfire.trade.LegalEntity;

/**
 * A {@link PropSearchFilter} that can be used to search {@link LegalEntity}s by the properties of
 * its linked Person.
 * <p>
 * The result of a search using this query is a Collection of <b>
 * {@link LegalEntityPersonMappingBean}</b>s.
 * </p>
 * <p>
 * Note, that although this is an {@link AbstractJDOQuery} and therefore compatible with the JFire
 * query framework this filter can't be used as a query constraining the candidates of a subsequent
 * query of the candidate-class {@link LegalEntity}. It can only be the last query in the chain as
 * its result type is not the candidate-class.
 * </p>
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 */
public class LegalEntitySearchFilter
	extends PropSearchFilter
{
	
	private static final long serialVersionUID = 20100408L;

	/**
	 * Create a new {@link LegalEntitySearchFilter} with the default conjunction.
	 */
	public LegalEntitySearchFilter() {
		super();
	}

	/**
	 * Create a new {@link LegalEntitySearchFilter} using the given conjunction.
	 * 
	 * @param _conjunction The conjunction to use.
	 */
	public LegalEntitySearchFilter(int _conjunction) {
		super(_conjunction);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Points to the LegalEntity's person.
	 * </p>
	 */
	@Override
	public void setPropVariableCondition(StringBuffer filter) {
		filter.append(PROPERTY_VARNAME+" == this.person");
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * {@link LegalEntity}.class
	 * </p>
	 */
	@Override
	protected Class<LegalEntity> initCandidateClass()
	{
		return LegalEntity.class;
	}

//	All of this does not work using DataNucleus in JFire 1.0 and trunk (2010-04-06)
//	@Override
//	protected void prepareQuery(Query query) {
//		super.prepareQuery(query);
////		query.setResult("new " + LegalEntityPersonMappingBean.class.getName() + "(JDOHelper.getObjectId(this), JDOHelper.getObjectId(this.person))");
////		query.setResult("JDOHelper.getObjectId(this) as legalEntityID, JDOHelper.getObjectId(this.person) as personID");
//		query.setResult("this as legalEntity");
//		query.setResultClass(LegalEntityPersonMappingBean.class);
//	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Currently this filter performs a post-processing of the query-result and converts it to
	 * instances of {@link LegalEntityPersonMappingBean}.
	 * </p>
	 */
	@Override
	protected Collection<?> executeQuery() {
		Collection<LegalEntityPersonMappingBean> result = new LinkedList<LegalEntityPersonMappingBean>();
		Collection<?> queryResult = super.executeQuery();
		for (Object legalEntity : queryResult) {
			result.add(new LegalEntityPersonMappingBean((LegalEntity) legalEntity));
		}
		return result;
	}
}
