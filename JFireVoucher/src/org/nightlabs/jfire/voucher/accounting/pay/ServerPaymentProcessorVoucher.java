package org.nightlabs.jfire.voucher.accounting.pay;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.pay.PaymentException;
import org.nightlabs.jfire.accounting.pay.PaymentResult;
import org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor;
import org.nightlabs.jfire.accounting.pay.id.ServerPaymentProcessorID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.voucher.JFireVoucherEAR;
import org.nightlabs.jfire.voucher.accounting.VoucherMoneyTransfer;
import org.nightlabs.jfire.voucher.accounting.VoucherRedemption;
import org.nightlabs.jfire.voucher.store.VoucherKey;

import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class ServerPaymentProcessorVoucher
		extends ServerPaymentProcessor
{
	private static final long serialVersionUID = 1L;

	public static ServerPaymentProcessorVoucher getServerPaymentProcessorVoucher(PersistenceManager pm)
	{
		ServerPaymentProcessorVoucher serverPaymentProcessorVoucher;
		try {
			pm.getExtent(ServerPaymentProcessorVoucher.class);
			serverPaymentProcessorVoucher = (ServerPaymentProcessorVoucher) pm.getObjectById(
					ServerPaymentProcessorID.create(Organisation.DEV_ORGANISATION_ID, ServerPaymentProcessorVoucher.class.getName()));
		} catch (JDOObjectNotFoundException e) {
			serverPaymentProcessorVoucher = new ServerPaymentProcessorVoucher(Organisation.DEV_ORGANISATION_ID, ServerPaymentProcessorVoucher.class.getName());
			serverPaymentProcessorVoucher = pm.makePersistent(serverPaymentProcessorVoucher);
		}

		return serverPaymentProcessorVoucher;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ServerPaymentProcessorVoucher() { }

	public ServerPaymentProcessorVoucher(String organisationID, String serverPaymentProcessorID)
	{
		super(organisationID, serverPaymentProcessorID);
	}

	@Override
	protected PaymentResult externalPayBegin(PayParams payParams)
			throws PaymentException
	{
		return null;
	}

	@Override
	protected PaymentResult externalPayCommit(PayParams payParams)
			throws PaymentException
	{
		PersistenceManager pm = getPersistenceManager();

		PaymentDataVoucher paymentData = (PaymentDataVoucher) payParams.paymentData;
		VoucherKey voucherKey = VoucherKey.getVoucherKey(pm, paymentData.getVoucherKey());

		voucherKey.addRedemption(
				new VoucherRedemption(IDGenerator.getOrganisationID(), IDGenerator.nextID(VoucherRedemption.class), voucherKey, paymentData.getPayment()));

		return null;
	}

	@Override
	protected PaymentResult externalPayDoWork(PayParams payParams)
			throws PaymentException
	{
		return null;
	}

	@Override
	protected PaymentResult externalPayRollback(PayParams payParams)
			throws PaymentException
	{
		return null;
	}

	@Override
	public Anchor getAnchorOutside(PayParams payParams)
	{
		PersistenceManager pm = getPersistenceManager();

		PaymentDataVoucher paymentData = (PaymentDataVoucher) payParams.paymentData;
		VoucherKey voucherKey = VoucherKey.getVoucherKey(pm, paymentData.getVoucherKey());

		if (voucherKey == null)
			throw new IllegalStateException("Voucher not found! VoucherKey=" + paymentData.getVoucherKey()); // TODO this should be another exception - or we check for it already in the ClientDeliveryProcessor.

		if (VoucherKey.VALIDITY_VALID != voucherKey.getValidity())
			throw new IllegalStateException("Voucher not valid! VoucherKey=" + paymentData.getVoucherKey()); // TODO this should be another exception - or we check for it already in the ClientDeliveryProcessor.

		Article article = voucherKey.getVoucher().getProductLocal().getSaleArticle();
		if (article == null)
			throw new IllegalStateException("No Article! VoucherKey=" + paymentData.getVoucherKey()); // TODO this should be another exception - or we check for it already in the ClientDeliveryProcessor.

		Invoice invoice = article.getInvoice();
		if (invoice == null)
			throw new IllegalStateException("No invoice! VoucherKey=" + paymentData.getVoucherKey()); // TODO this should be another exception - or we check for it already in the ClientDeliveryProcessor.

		if (!invoice.getInvoiceLocal().isBooked())
			throw new IllegalStateException("Invoice not yet booked! VoucherKey=" + paymentData.getVoucherKey()); // TODO this should be another exception - or we check for it already in the ClientDeliveryProcessor.

		VoucherMoneyTransfer voucherMoneyTransfer = VoucherMoneyTransfer.getVoucherMoneyTransfer(pm, article);
		if (voucherMoneyTransfer == null)
			throw new IllegalStateException("No VoucherMoneyTransfer! VoucherKey=" + paymentData.getVoucherKey());

		if (!voucherKey.getRestValue().getCurrency().equals(paymentData.getPayment().getCurrency()))
			throw new IllegalStateException("Currency mismatch! VoucherKey's currency ("+voucherKey.getRestValue().getCurrency().getCurrencyID()+") is not the same as payment's currency ("+paymentData.getPayment().getCurrency().getCurrencyID()+")! VoucherKey=" + paymentData.getVoucherKey());

		if (voucherKey.getRestValue().getAmount() < paymentData.getPayment().getAmount())
			throw new IllegalStateException("Insufficient value ("+voucherKey.getRestValue().getAmount()+") for this payment ("+paymentData.getPayment().getAmount()+")! VoucherKey=" + paymentData.getVoucherKey());

//		if (VoucherLocalAccountantDelegate.ACCOUNT_ANCHOR_TYPE_ID_VOUCHER.equals(voucherMoneyTransfer.getFrom().getAnchorTypeID()))
//			return voucherMoneyTransfer.getFrom();
//
//		if (VoucherLocalAccountantDelegate.ACCOUNT_ANCHOR_TYPE_ID_VOUCHER.equals(voucherMoneyTransfer.getTo().getAnchorTypeID()))
//			return voucherMoneyTransfer.getTo();

		if (voucherMoneyTransfer.getFrom() instanceof Account) {
			Account fromAccount = (Account) voucherMoneyTransfer.getFrom();
			if (JFireVoucherEAR.ACCOUNT_TYPE_ID_VOUCHER.equals(JDOHelper.getObjectId(fromAccount.getAccountType())))
				return voucherMoneyTransfer.getFrom();
		}

		if (voucherMoneyTransfer.getTo() instanceof Account) {
			Account toAccount = (Account) voucherMoneyTransfer.getTo();
			if (JFireVoucherEAR.ACCOUNT_TYPE_ID_VOUCHER.equals(JDOHelper.getObjectId(toAccount.getAccountType())))
				return voucherMoneyTransfer.getTo();
		}

		throw new IllegalStateException("Neither VoucherMoneyTransfer.from nor VoucherMoneyTransfer.to has AccountTypeID==JFireVoucherEAR.ACCOUNT_TYPE_ID_VOUCHER! VoucherKey=" + paymentData.getVoucherKey());
	}
}
