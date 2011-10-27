package org.nightlabs.jfire.voucher.store;

import java.util.Collection;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.numorgid.NumericOrganisationIdentifier;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.DeliveryNoteActionHandler;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.deliver.Delivery;
import org.nightlabs.jfire.store.deliver.DeliveryData;
import org.nightlabs.jfire.store.deliver.DeliveryException;
import org.nightlabs.jfire.trade.Article;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.DeliveryNoteActionHandler"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class VoucherDeliveryNoteActionHandler
extends DeliveryNoteActionHandler
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(VoucherDeliveryNoteActionHandler.class);

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected VoucherDeliveryNoteActionHandler() { }

	public VoucherDeliveryNoteActionHandler(String organisationID, String deliveryNoteActionHandlerID)
	{
		super(organisationID, deliveryNoteActionHandlerID);
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient VoucherStore voucherStore = null;

	public VoucherStore getVoucherStore()
	{
		if (voucherStore == null)
			voucherStore = VoucherStore.getVoucherStore(getPersistenceManager());

		return voucherStore;
	}

	@Override
	public void onDeliverBegin(User user, DeliveryData deliveryData, DeliveryNote deliveryNote)
			throws Exception, DeliveryException
	{
//		super.onDeliverBegin(user, deliveryData, deliveryNote); // super method is right now a noop. Marco.

		if (logger.isDebugEnabled())
			logger.debug("onDeliverBegin: entered for " + JDOHelper.getObjectId(deliveryData) + " and " + JDOHelper.getObjectId(deliveryNote));

		Delivery delivery = deliveryData.getDelivery();

		// If the delivery is postponed, we do not create voucher keys.
		if (delivery.isPostponed())
			return;

		PersistenceManager pm = getPersistenceManager();

//		if (Delivery.DELIVERY_DIRECTION_OUTGOING.equals(delivery.getDeliveryDirection())) {
			for (Article article : delivery.getArticles()) {
				Product product = article.getProduct();
				if (product instanceof Voucher) {
					Voucher voucher = (Voucher) product;

					if (article.isReversing()) // we handle these completely in onDeliverEnd
						continue;

					Collection<? extends VoucherKey> voucherKeys = VoucherKey.getVoucherKeys(pm, voucher, VoucherKey.VALIDITY_VALID);
					if (!voucherKeys.isEmpty())
						throw new IllegalStateException("There should be no valid VoucherKey for this Voucher, but there is/are "+voucherKeys.size()+"!!! VoucherPK=" + voucher.getPrimaryKey());

//					VoucherKey voucherKey = new VoucherKey(
//							getVoucherStore().getVoucherOrganisationID(), IDGenerator.nextID(VoucherKey.class), article, user);
					VoucherKey voucherKey = new VoucherKey(NumericOrganisationIdentifier.getNumericOrganisationID(pm), IDGenerator.nextID(VoucherKey.class), article, user);
					voucherKey = pm.makePersistent(voucherKey);
					voucher.setVoucherKey(voucherKey); // if we would allow the trade of vouchers between organisations, this should probably be in the VoucherLocal
				}
			}
//		}
	}

	@Override
	public void onDeliverEnd(User user, DeliveryData deliveryData, DeliveryNote deliveryNote)
	{
//		super.onDeliverEnd(user, deliveryData, deliveryNote); // super method is right now a noop. Marco.

		if (logger.isDebugEnabled())
			logger.debug("onDeliverEnd: entered for " + JDOHelper.getObjectId(deliveryData) + " and " + JDOHelper.getObjectId(deliveryNote));

		Delivery delivery = deliveryData.getDelivery();

		// If the delivery is postponed, we do not create voucher keys.
		if (delivery.isPostponed())
			return;

		for (Article article : delivery.getArticles()) {
			Product product = article.getProduct();
			if (product instanceof Voucher) {
				Voucher voucher = (Voucher) product;

				if (article.isReversing())
					voucher.getVoucherKey().setValidity(VoucherKey.VALIDITY_REVERSED, user);
				else
					voucher.getVoucherKey().setValidity(VoucherKey.VALIDITY_VALID, user);
			}
		}
	}
}
