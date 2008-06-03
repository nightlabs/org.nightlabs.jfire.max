/**
 * 
 */
package org.nightlabs.jfire.store.search;

import javax.jdo.Query;

import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeGroup;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public abstract class AbstractProductTypeGroupQuery 
extends VendorDependentQuery
{
	private boolean saleable = true;
	private boolean published = true;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jdo.query.AbstractJDOQuery#prepareQuery(javax.jdo.Query)
	 */
	@Override
	protected void prepareQuery(Query q) {
		super.prepareQuery(q);
		StringBuffer filter = getFilter();
		StringBuffer vars = getVars();
		StringBuffer imports = getImports();
		
//		filter.append("productType.productTypeGroups.containsValue(this)");
//		FIXME Workaround for JPOX - begin
		filter.append("productType.productTypeGroups.containsValue(workaroundGroup) &&");
		filter.append("workaroundGroup == this");
		vars.append(ProductTypeGroup.class.getName()+" workaroundGroup;");
//		imports.add(EventGroup.class);
		addImport(ProductTypeGroup.class.getName());
//		FIXME Workaround for JPOX - end

		filter.append(" && ");
		filter.append("productType.organisationID == :myOrganisationID");
//		paramMap.put("myOrganisationID", LocalOrganisation.getLocalOrganisation(getPersistenceManager()).getOrganisationID());

		filter.append(" && ");
		filter.append("productType.published == "+Boolean.toString(isPublished()));
		filter.append(" && ");
		filter.append("productType.saleable == "+Boolean.toString(isSaleable()));
//		int dateCount = 0;
//		if (isUsePerformaceTimeFilter()) {
//			if ((getPerformanceTimeFilters() != null) && (getPerformanceTimeFilters().size() > 0) ) {
//				filter.append(" && (");
//				for (Iterator iter = getPerformanceTimeFilters().iterator(); iter.hasNext();) {
//					PerformanceTimeFilterItem filterItem = (PerformanceTimeFilterItem) iter.next();
//					filterItem.appendSubQuery(dateCount, 0, imports, vars, filter, params, paramMap);
//					if (iter.hasNext()) {
//						switch (getPerformanceTimeFilterConjunction()) {
//							case SearchFilter.CONJUNCTION_AND:
//								filter.append(" && ");
//								break;
//							case SearchFilter.CONJUNCTION_OR:
//								filter.append(" || ");
//								break;
//						}
//					}
//				}
//				filter.append(")");
//			}
//		}

		vars.append(ProductType.class.getName()+" productType");
//		imports.add(Event.class);
		addImport(ProductType.class.getName());

//		result.append("distinct this");		 
	}

	/**
	 * Returns whether only published Events should be searched for. 
	 * @return whether only published Events should be searched for.
	 */
	public boolean isPublished() {
		return published;
	}

	/**
	 * Returns whether only saleable Events should be searched for.
	 * @return whether only saleable Events should be searched for.
	 */
	public boolean isSaleable() {
		return saleable;
	}

	/**
	 * Sets the saleable.
	 * @param saleable the saleable to set
	 */
	public void setSaleable(boolean saleable) {
		this.saleable = saleable;
	}

	/**
	 * Sets the published.
	 * @param published the published to set
	 */
	public void setPublished(boolean published) {
		this.published = published;
	}	
}
