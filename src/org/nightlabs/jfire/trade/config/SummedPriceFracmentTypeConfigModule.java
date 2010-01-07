package org.nightlabs.jfire.trade.config;

import java.util.ArrayList;
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
		table="JFireTrade_SummedPriceFracmentTypeConfigModule")
	@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
	@FetchGroups({
		@FetchGroup(
			name=SummedPriceFracmentTypeConfigModule.FETCH_GROUP_SUMMED_PRICE_FRACMENT_TYPE_LIST,
			members=@Persistent(name="summedPriceFracmentTypeList")
		),
	})
public class SummedPriceFracmentTypeConfigModule 
extends ConfigModule
{
	private static final long serialVersionUID = 1L;
	
	public static final String FETCH_GROUP_SUMMED_PRICE_FRACMENT_TYPE_LIST = "SummedPriceFracmentTypeConfigModule.summedPriceFracmentTypeList"; //$NON-NLS-1$
	
	/**
	 * The list of PriceFragments that should be summed.
	 */
	@Join
	@Persistent(table="JFireTrade_SummedPriceFracmentTypeConfigModule_summedPriceFracmentTypeList")
	private List<PriceFragmentType> summedPriceFracmentTypeList;
	
	@Override
	public void init() {
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("JDOHelper.getPersistenceManager(this) returned null!");

		summedPriceFracmentTypeList = new ArrayList<PriceFragmentType>();
	}

	public List<PriceFragmentType> getSummedPriceFracmentTypeList() {
		return Collections.unmodifiableList(summedPriceFracmentTypeList);
	}
	
	public void addPriceFracmentType(PriceFragmentType priceFracmentType) {
		summedPriceFracmentTypeList.add(priceFracmentType);
	}
	
	public void removePriceFracmentType(PriceFragmentType priceFracmentType) {
		summedPriceFracmentTypeList.remove(priceFracmentType);
	}
}