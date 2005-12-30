/*
 * Created on Mar 16, 2005
 */
package org.nightlabs.ipanema.accounting.tariffpriceconfig;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;

import org.nightlabs.ModuleException;
import org.nightlabs.ipanema.accounting.AccountingManagerBean;
import org.nightlabs.ipanema.accounting.Price;
import org.nightlabs.ipanema.accounting.Tariff;
import org.nightlabs.ipanema.accounting.id.CurrencyID;
import org.nightlabs.ipanema.accounting.id.TariffID;
import org.nightlabs.ipanema.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.ipanema.base.BaseSessionBeanImpl;
import org.nightlabs.ipanema.trade.CustomerGroup;
import org.nightlabs.ipanema.trade.id.CustomerGroupID;
import org.nightlabs.jdo.NLJDOHelper;

/**
 * @ejb.bean name="ipanema/ejb/JFireTrade/TariffPriceConfigManager"	
 *           jndi-name="ipanema/ejb/JFireTrade/TariffPriceConfigManager"
 *           type="Stateless" 
 *           transaction-type="Container"
 *
 * @ejb.util generate = "physical"
 */
public abstract class TariffPriceConfigManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	public static final Logger LOGGER = Logger.getLogger(AccountingManagerBean.class);

	/**
	 * @see org.nightlabs.ipanema.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext sessionContext)
			throws EJBException, RemoteException
	{
		LOGGER.debug(this.getClass().getName() + ".setSessionContext("+sessionContext+")");
		super.setSessionContext(sessionContext);
	}
	/**
	 * @see org.nightlabs.ipanema.base.BaseSessionBeanImpl#unsetSessionContext()
	 */
	public void unsetSessionContext() {
		super.unsetSessionContext();
	}
	/**
	 * @ejb.create-method  
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate()
	throws CreateException
	{
		LOGGER.debug(this.getClass().getName() + ".ejbCreate()");
	}
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 * 
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException
	{
		LOGGER.debug(this.getClass().getName() + ".ejbRemove()");
	}
	
	/**
	 * @see javax.ejb.SessionBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException, RemoteException
	{
		LOGGER.debug(this.getClass().getName() + ".ejbActivate()");
	}
	/**
	 * @see javax.ejb.SessionBean#ejbPassivate()
	 */
	public void ejbPassivate() throws EJBException, RemoteException
	{
		LOGGER.debug(this.getClass().getName() + ".ejbPassivate()");
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public TariffPriceConfig storePriceConfig(TariffPriceConfig priceConfig, boolean get, String[] fetchGroups)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return (TariffPriceConfig) NLJDOHelper.storeJDO(pm, priceConfig, get, fetchGroups);
		} finally {
			pm.close();
		}
	}

	protected static Pattern tariffPKSplitPattern = null;

	/**
	 * @return a <tt>Collection</tt> of {@link TariffPricePair}
	 * 
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection getTariffPricePairs(
			PriceConfigID priceConfigID, CustomerGroupID customerGroupID, CurrencyID currencyID,
			String[] tariffFetchGroups, String[] priceFetchGroups)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (tariffPKSplitPattern == null)
				tariffPKSplitPattern = Pattern.compile("/");

			// TODO use setResult and put all this logic into the JDO query!
			StablePriceConfig priceConfig = (StablePriceConfig) pm.getObjectById(priceConfigID);
			Collection priceCells = priceConfig.getPriceCells(
					CustomerGroup.getPrimaryKey(customerGroupID.organisationID, customerGroupID.customerGroupID),
					currencyID.currencyID);

			Collection res = new ArrayList();

			for (Iterator it = priceCells.iterator(); it.hasNext(); ) {
				PriceCell priceCell = (PriceCell) it.next();
				String tariffPK = priceCell.getPriceCoordinate().getTariffPK();
				String[] tariffPKParts = tariffPKSplitPattern.split(tariffPK);
				if (tariffPKParts.length != 2)
					throw new IllegalStateException("How the hell can it happen that the tariffPK does not consist out of two parts?");

				String tariffOrganisationID = tariffPKParts[0];
				long tariffID = Long.parseLong(tariffPKParts[1]);

				if (tariffFetchGroups != null)
					pm.getFetchPlan().setGroups(tariffFetchGroups);

				Tariff tariff = (Tariff) pm.getObjectById(TariffID.create(tariffOrganisationID, tariffID));
				tariff = (Tariff) pm.detachCopy(tariff);

				if (priceFetchGroups != null)
					pm.getFetchPlan().setGroups(priceFetchGroups);

				Price price = (Price) pm.detachCopy(priceCell.getPrice());

				res.add(new TariffPricePair(tariff, price));
			}

			return res;
		} finally {
			pm.close();
		}
	}
}
