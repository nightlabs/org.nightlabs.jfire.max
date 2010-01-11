package org.nightlabs.jfire.trade.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.config.ConfigModule;

/**
 * @author Chairat Kongarayawetchakun <!-- chairat [AT] nightlabs [DOT] de -->
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireTrade_SummedPriceFragmentTypeConfigModule")
	@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
	@FetchGroups({
		@FetchGroup(
			name=SummedPriceFragmentTypeConfigModule.FETCH_GROUP_SUMMED_PRICE_FRAGMENT_TYPE_LIST,
			members=@Persistent(name="summedPriceFragmentTypeList")
		),
	})
public class SummedPriceFragmentTypeConfigModule 
extends ConfigModule
{
	private static final long serialVersionUID = 1L;
	
	public static final String FETCH_GROUP_SUMMED_PRICE_FRAGMENT_TYPE_LIST = "SummedPriceFragmentTypeConfigModule.summedPriceFragmentTypeList"; //$NON-NLS-1$
	
	/**
	 * The list of PriceFragments that should be summed.
	 */
	@Join
	@Persistent(table="JFireTrade_SummedPriceFragmentTypeConfigModule_summedPriceFragmentTypeList")
	private List<PriceFragmentType> summedPriceFragmentTypeList;
	
	@Override
	public void init() {
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("JDOHelper.getPersistenceManager(this) returned null!");

		summedPriceFragmentTypeList = new ArrayList<PriceFragmentType>();
	}

	public List<PriceFragmentType> getSummedPriceFragmentTypeList() {
		return summedPriceFragmentTypeList;
	}
	
	public void addPriceFragmentType(PriceFragmentType priceFragmentType) {
		summedPriceFragmentTypeList.add(priceFragmentType);
	}
	
	public void addPriceFragmentTypes(Collection<PriceFragmentType> priceFragmentTypes) {
		summedPriceFragmentTypeList.addAll(priceFragmentTypes);
	}
	
	public void removePriceFragmentType(PriceFragmentType priceFragmentType) {
		summedPriceFragmentTypeList.remove(priceFragmentType);
	}
	
	public void removePriceFragmentTypes(Collection<PriceFragmentType> priceFragmentTypes) {
		summedPriceFragmentTypeList.removeAll(priceFragmentTypes);
	}
}