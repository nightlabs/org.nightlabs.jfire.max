package org.nightlabs.jfire.voucher.scripting.javaclass;

import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.voucher.store.Voucher;
import org.nightlabs.l10n.NumberFormatter;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class VoucherPrice 
extends AbstractVoucherScript 
{

	public Object doExecute() throws ScriptException 
	{
		Voucher voucher = getVoucher();
		long amount = voucher.getProductLocal().getSaleArticle().getPrice().getAmount();
		Currency currency = voucher.getProductLocal().getSaleArticle().getPrice().getCurrency();
		return NumberFormatter.formatCurrency(amount, currency);
	}

}
