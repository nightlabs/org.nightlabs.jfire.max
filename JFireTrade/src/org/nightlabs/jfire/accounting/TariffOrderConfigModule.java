package org.nightlabs.jfire.accounting;

//import java.util.Comparator;
import java.text.Collator;
import java.util.Comparator;
import java.util.Map;

import org.nightlabs.jfire.config.ConfigModule;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.config.ConfigModule"
 *		detachable="true"
 *		table="JFireTrade_TariffOrderConfigModule"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.fetch-group name="TariffOrderConfigModule.this" fetch-groups="default" fields="tariffOrderMap"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_TariffOrderConfigModule")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name=TariffOrderConfigModule.FETCH_GROUP_TARIFF_ORDER_CONFIG_MODULE,
		members=@Persistent(name="tariffOrderMap"))
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class TariffOrderConfigModule extends ConfigModule {
	
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		table="JFireTrade_TariffOrderConfigModule_tariffOrderMap"
	 * 
	 * @jdo.join
	 */
	@Join
	@Persistent(
		table="JFireTrade_TariffOrderConfigModule_tariffOrderMap",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Map<Tariff, Integer> tariffOrderMap = null;
	
	public static final String FETCH_GROUP_TARIFF_ORDER_CONFIG_MODULE = "TariffOrderConfigModule.this"; //$NON-NLS-1$

	@Override
	public void init() {
	}

	/**
	 * This method returns a {@link Comparator} for sorting {@link Tariff}s. If the {@link #tariffOrderMap} of this config module has been set before,
	 * the information stored there is used to sort. If not, the <code>tariffIndex</code> of the {@link Tariff}s is used for sorting. If the tariffIndices
	 * of the two tariffs in questions are equal, the tariffs are ordered lexicographically.
	 * @return A comparator for sorting {@link Tariff}s.
	 */
	public Comparator<Tariff> getTariffComparator() {
		if (tariffOrderMap == null || tariffOrderMap.isEmpty()) {
			return new Comparator<Tariff>() {
				public int compare(Tariff o1, Tariff o2) {
					Integer index1 = o1.getTariffIndex(), index2 = o2.getTariffIndex();
					
					if (index1.equals(index2))
						return Collator.getInstance().compare(o1.getName().getText(), o2.getName().getText());
					else
						return new Integer(o1.getTariffIndex()).compareTo(o2.getTariffIndex());
				}
			};
		} else {
			return new Comparator<Tariff>() {
				public int compare(Tariff o1, Tariff o2) {
					Integer index1 = Integer.MAX_VALUE, index2 = Integer.MAX_VALUE;
					if (tariffOrderMap.containsKey(o1))
						index1 = tariffOrderMap.get(o1);
					if (tariffOrderMap.containsKey(o2))
						index2 = tariffOrderMap.get(o2);
					
					if (index1.equals(index2))
						return Collator.getInstance().compare(o1.getName().getText(), o2.getName().getText());
					else
						return index1.compareTo(index2);
				}
			};
		}
	}
	
	public void setTariffOrderMap(Map<Tariff, Integer> tariffOrderMap) {
		this.tariffOrderMap = tariffOrderMap;
	}
	
	public Map<Tariff, Integer> getTariffOrderMap() {
		return tariffOrderMap;
	}
}
