package org.nightlabs.jfire.voucher.store;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductLocal;
import org.nightlabs.jfire.store.Repository;
import org.nightlabs.jfire.trade.Article;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.ProductLocal"
 *		detachable="true"
 *		table="JFireVoucher_VoucherLocal"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="VoucherLocal.restValue" fields="restValue"
 */
public class VoucherLocal
extends ProductLocal
implements DetachCallback
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUPS_REST_VALUE = "VoucherLocal.restValue";
	public static final String FETCH_GROUPS_NOMINAL_VALUE = "VoucherLocal.nominalValue";

	/**
	 * @deprecated Only for JDO!
	 */
	protected VoucherLocal() { }

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private boolean nominalValue_detached = false;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private Price nominalValue;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	private Price restValue;

	public VoucherLocal(User user, Product _product, Repository initialRepository)
	{
		super(user, _product, initialRepository);
	}

	/**
	 * @return the nominal value as assigned to the {@link Article} when the {@link Voucher} was sold. This method will return <code>null</code>,
	 *		if the Voucher is currently NOT assigned to any customer.
	 */
	public Price getNominalValue()
	{
		if (nominalValue_detached)
			return nominalValue;

		Article article = getArticle();
		if (article == null)
			return null;

		return article.getPrice();
	}
	/**
	 * @return the value that's still available - i.e. the nominal amount minus the redeemed amount. This will be <code>null</code>,
	 *		if the Voucher is currently NOT assigned to any customer. 
	 */
	public Price getRestValue()
	{
		return restValue;
	}

//	public void setRestValue(Price restValue)
//	{
//		this.restValue = restValue;
//	}

	public long decrementRestValue(long amount)
	{
		return incrementRestValue(-amount);
	}

	public long incrementRestValue(long amount)
	{
		if (restValue == null)
			throw new IllegalStateException("This Voucher is currently not allocated within an Article - there is no restValue existing.");

		long newAmount = restValue.getAmount() + amount;
		if (newAmount < 0)
			throw new IllegalStateException("restValue.amount would become negative!");

		restValue.setAmount(newAmount);
		return newAmount;
	}

	@Implement
	public void jdoPostDetach(Object _attached)
	{
		VoucherLocal attached = (VoucherLocal) _attached;
		VoucherLocal detached = this;

		PersistenceManager pm = JDOHelper.getPersistenceManager(attached);
		if (pm.getFetchPlan().getGroups().contains(FETCH_GROUPS_NOMINAL_VALUE)) {
			detached.nominalValue = attached.getNominalValue();
			detached.nominalValue_detached = true;
		}
	}

	@Implement
	public void jdoPreDetach()
	{
		// nothing
	}

	protected void onAssemble(User user)
	{
		Price origPrice = getArticle().getPrice();
		long priceID = PriceConfig.createPriceID(origPrice.getOrganisationID(), origPrice.getPriceConfigID());
		restValue = new Price(origPrice.getOrganisationID(), origPrice.getPriceConfigID(), priceID, origPrice.getCurrency()); 
	}

	protected void onDisassemble(User user, boolean onRelease)
	{
		restValue = null;
	}
}
