package org.nightlabs.jfire.chezfrancois;

import java.util.Locale;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.AccountType;
import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.prop.exception.DataBlockGroupNotFoundException;
import org.nightlabs.jfire.prop.exception.DataBlockNotFoundException;
import org.nightlabs.jfire.prop.exception.DataFieldNotFoundException;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.CannotConfirmProductTypeException;
import org.nightlabs.jfire.store.CannotMakeProductTypeSaleableException;
import org.nightlabs.jfire.store.CannotPublishProductTypeException;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.voucher.JFireVoucherEAR;
import org.nightlabs.jfire.voucher.accounting.VoucherLocalAccountantDelegate;
import org.nightlabs.jfire.voucher.accounting.VoucherPriceConfig;
import org.nightlabs.jfire.voucher.scripting.VoucherLayout;
import org.nightlabs.jfire.voucher.store.VoucherType;

public class InitialiserVoucher
extends Initialiser
{
	public InitialiserVoucher(final PersistenceManager pm, final JFirePrincipal principal)
	{
		super(pm, principal);
	}

	public void createDemoData()
	throws CannotPublishProductTypeException, CannotConfirmProductTypeException, CannotMakeProductTypeSaleableException, DataBlockNotFoundException, DataBlockGroupNotFoundException, DataFieldNotFoundException
	{
		final String organisationID = getOrganisationID();

		pm.getExtent(VoucherType.class);
		final ProductTypeID voucherTypeNormalID = ProductTypeID.create(organisationID, "voucherType.normal");
		try {
			pm.getObjectById(voucherTypeNormalID);
			return; // it exists => do nothing
		} catch (final JDOObjectNotFoundException x) {
			// ignore this exception and create demo date
		}

		final DataCreatorVoucher dataCreator = new DataCreatorVoucher(pm, User.getUser(pm, getPrincipal()));

		final VoucherPriceConfig priceConfig10 = new VoucherPriceConfig(null);
		priceConfig10.getName().setText(Locale.ENGLISH.getLanguage(), "Voucher 10");
		priceConfig10.addCurrency(dataCreator.getCurrencyEUR());
		priceConfig10.setPrice(dataCreator.getCurrencyEUR(), new Long(1000));

		final VoucherPriceConfig priceConfig20 = new VoucherPriceConfig(null);
		priceConfig20.getName().setText(Locale.ENGLISH.getLanguage(), "Voucher 20");
		priceConfig20.addCurrency(dataCreator.getCurrencyEUR());
		priceConfig20.setPrice(dataCreator.getCurrencyEUR(), new Long(2000));

		final VoucherPriceConfig priceConfig50 = new VoucherPriceConfig(null);
		priceConfig50.getName().setText(Locale.ENGLISH.getLanguage(), "Voucher 50");
		priceConfig50.addCurrency(dataCreator.getCurrencyEUR());
		priceConfig50.setPrice(dataCreator.getCurrencyEUR(), new Long(5000));


		final VoucherPriceConfig priceConfig40 = new VoucherPriceConfig(null);
		priceConfig40.getName().setText(Locale.ENGLISH.getLanguage(), "Voucher 40");
		priceConfig40.addCurrency(dataCreator.getCurrencyEUR());
		priceConfig40.setPrice(dataCreator.getCurrencyEUR(), new Long(4000));

		final AccountType accountType = (AccountType) pm.getObjectById(JFireVoucherEAR.ACCOUNT_TYPE_ID_VOUCHER);
		final Account accountNormalEur = new Account(organisationID, "voucherType.normal.eur", accountType, dataCreator.getOrganisationLegalEntity(), dataCreator.getCurrencyEUR());
		final Account accountXmasEur = new Account(organisationID, "voucherType.xmas.eur", accountType, dataCreator.getOrganisationLegalEntity(), dataCreator.getCurrencyEUR());

		final VoucherLocalAccountantDelegate localAccountantDelegateNormal = new VoucherLocalAccountantDelegate(organisationID, "voucherType.normal");
		localAccountantDelegateNormal.setAccount(dataCreator.getCurrencyEUR().getCurrencyID(), accountNormalEur);

		final VoucherLocalAccountantDelegate localAccountantDelegateXmas = new VoucherLocalAccountantDelegate(organisationID, "voucherType.xmas");
		localAccountantDelegateXmas.setAccount(dataCreator.getCurrencyEUR().getCurrencyID(), accountXmasEur);

		final LegalEntity vendor = dataCreator.createVendor1();

		final VoucherType normal = dataCreator.createCategory(null, voucherTypeNormalID.productTypeID, localAccountantDelegateNormal, null, "Normal");
		final VoucherType normal10 = dataCreator.createLeaf(normal, "voucherType.normal.10", priceConfig10, null, null, "Voucher 10", "Gutschein 10");
		final VoucherType normal20 = dataCreator.createLeaf(normal, "voucherType.normal.20", priceConfig20, null, null, "Voucher 20", "Gutschein 20");
		final VoucherType normal40 = dataCreator.createLeaf(normal, "voucherType.normal.40", priceConfig40, null, vendor, "Voucher 40", "Gutschein 40");
		final VoucherType normal50 = dataCreator.createLeaf(normal, "voucherType.normal.50", priceConfig50, null, null, "Voucher 50", "Gutschein 50");

		final VoucherType xmas = dataCreator.createCategory(null, "voucherType.xmas", localAccountantDelegateXmas, null, "Christmas", "Weihnachten");
		final VoucherType xmas10 = dataCreator.createLeaf(xmas, "voucherType.xmas.10", priceConfig10, null, null, "Voucher Christmas Special 10", "Weihnachts-Spezial-Gutschein 10");
		final VoucherType xmas20 = dataCreator.createLeaf(xmas, "voucherType.xmas.20", priceConfig20, null, null, "Voucher Christmas Special 20", "Weihnachts-Spezial-Gutschein 20");
		final VoucherType xmas40 = dataCreator.createLeaf(xmas, "voucherType.xmas.40", priceConfig50, null, vendor, "Voucher Christmas Special 40", "Weihnachts-Spezial-Gutschein 40");
		final VoucherType xmas50 = dataCreator.createLeaf(xmas, "voucherType.xmas.50", priceConfig50, null, null, "Voucher Christmas Special 50", "Weihnachts-Spezial-Gutschein 50");

		// Could nto be done here because once a ProductType has been confirmed the vendor can not be set anymore
//		normal40.setVendor(legalEntityVendor);
//		xmas40.setVendor(legalEntityVendor);

		final VoucherLayout voucherLayout = dataCreator.createVoucherLayout();
		if (voucherLayout != null) {
			normal10.setVoucherLayout(voucherLayout);
			normal20.setVoucherLayout(voucherLayout);
			normal40.setVoucherLayout(voucherLayout);
			normal50.setVoucherLayout(voucherLayout);

			xmas10.setVoucherLayout(voucherLayout);
			xmas20.setVoucherLayout(voucherLayout);
			xmas40.setVoucherLayout(voucherLayout);
			xmas50.setVoucherLayout(voucherLayout);
		}

		dataCreator.getRootVoucherType().applyInheritance();

		dataCreator.makeAllLeafsSaleable();
	}
}
