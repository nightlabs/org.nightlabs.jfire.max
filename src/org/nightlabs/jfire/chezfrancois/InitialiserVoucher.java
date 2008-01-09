package org.nightlabs.jfire.chezfrancois;

import java.util.Locale;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.AccountType;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.CannotConfirmProductTypeException;
import org.nightlabs.jfire.store.CannotMakeProductTypeSaleableException;
import org.nightlabs.jfire.store.CannotPublishProductTypeException;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.voucher.JFireVoucherEAR;
import org.nightlabs.jfire.voucher.accounting.VoucherLocalAccountantDelegate;
import org.nightlabs.jfire.voucher.accounting.VoucherPriceConfig;
import org.nightlabs.jfire.voucher.store.VoucherType;

public class InitialiserVoucher
extends Initialiser
{
	private static final Logger logger = Logger.getLogger(InitialiserVoucher.class);

	public InitialiserVoucher(PersistenceManager pm, JFirePrincipal principal)
	{
		super(pm, principal);
	}

	public void createDemoData()
	throws ModuleException, CannotPublishProductTypeException, CannotConfirmProductTypeException, CannotMakeProductTypeSaleableException 
	{
		String organisationID = getOrganisationID();

		pm.getExtent(VoucherType.class);
		ProductTypeID voucherTypeNormalID = ProductTypeID.create(organisationID, "voucherType.normal");
		try {
			pm.getObjectById(voucherTypeNormalID);
			return; // it exists => do nothing
		} catch (JDOObjectNotFoundException x) {
			// ignore this exception and create demo date
		}

		DataCreatorVoucher dataCreator = new DataCreatorVoucher(pm, User.getUser(pm, getPrincipal()));

		VoucherPriceConfig priceConfig10 = new VoucherPriceConfig(organisationID, PriceConfig.createPriceConfigID());
		priceConfig10.getName().setText(Locale.ENGLISH.getLanguage(), "Voucher 10");
		priceConfig10.addCurrency(dataCreator.getCurrencyEUR());
		priceConfig10.setPrice(dataCreator.getCurrencyEUR(), new Long(1000));

		VoucherPriceConfig priceConfig20 = new VoucherPriceConfig(organisationID, PriceConfig.createPriceConfigID());
		priceConfig20.getName().setText(Locale.ENGLISH.getLanguage(), "Voucher 20");
		priceConfig20.addCurrency(dataCreator.getCurrencyEUR());
		priceConfig20.setPrice(dataCreator.getCurrencyEUR(), new Long(2000));

		VoucherPriceConfig priceConfig50 = new VoucherPriceConfig(organisationID, PriceConfig.createPriceConfigID());
		priceConfig50.getName().setText(Locale.ENGLISH.getLanguage(), "Voucher 50");
		priceConfig50.addCurrency(dataCreator.getCurrencyEUR());
		priceConfig50.setPrice(dataCreator.getCurrencyEUR(), new Long(5000));

		AccountType accountType = (AccountType) pm.getObjectById(JFireVoucherEAR.ACCOUNT_TYPE_ID_VOUCHER);
		Account accountNormalEur = new Account(organisationID, "voucherType.normal.eur", accountType, dataCreator.getOrganisationLegalEntity(), dataCreator.getCurrencyEUR());
		Account accountXmasEur = new Account(organisationID, "voucherType.xmas.eur", accountType, dataCreator.getOrganisationLegalEntity(), dataCreator.getCurrencyEUR());

		VoucherLocalAccountantDelegate localAccountantDelegateNormal = new VoucherLocalAccountantDelegate(organisationID, "voucherType.normal");
		localAccountantDelegateNormal.setAccount(dataCreator.getCurrencyEUR().getCurrencyID(), accountNormalEur);

		VoucherLocalAccountantDelegate localAccountantDelegateXmas = new VoucherLocalAccountantDelegate(organisationID, "voucherType.xmas");
		localAccountantDelegateXmas.setAccount(dataCreator.getCurrencyEUR().getCurrencyID(), accountXmasEur);

		VoucherType normal = dataCreator.createCategory(null, voucherTypeNormalID.productTypeID, localAccountantDelegateNormal, null, "Normal");
		dataCreator.createLeaf(normal, "voucherType.normal.10", priceConfig10, null, "Voucher 10", "Gutschein 10");
		dataCreator.createLeaf(normal, "voucherType.normal.20", priceConfig20, null, "Voucher 20", "Gutschein 20");
		dataCreator.createLeaf(normal, "voucherType.normal.50", priceConfig50, null, "Voucher 50", "Gutschein 50");

		VoucherType xmas = dataCreator.createCategory(null, "voucherType.xmas", localAccountantDelegateXmas, null, "Christmas", "Weihnachten");
		dataCreator.createLeaf(xmas, "voucherType.xmas.10", priceConfig10, null, "Voucher Christmas Special 10", "Weihnachts-Spezial-Gutschein 10");
		dataCreator.createLeaf(xmas, "voucherType.xmas.20", priceConfig20, null, "Voucher Christmas Special 20", "Weihnachts-Spezial-Gutschein 20");
		dataCreator.createLeaf(xmas, "voucherType.xmas.50", priceConfig50, null, "Voucher Christmas Special 50", "Weihnachts-Spezial-Gutschein 50");

		dataCreator.getRootVoucherType().applyInheritance();
		
		dataCreator.makeAllLeafsSaleable();
	}
}
