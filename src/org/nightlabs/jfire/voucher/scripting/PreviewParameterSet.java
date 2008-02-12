package org.nightlabs.jfire.voucher.scripting;

import java.io.Serializable;

import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.store.id.ProductTypeID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class PreviewParameterSet 
implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public PreviewParameterSet(ProductTypeID voucherTypeID) {
		this(voucherTypeID, null);
	}
	
	public PreviewParameterSet(ProductTypeID voucherTypeID, CurrencyID currencyID) {
		super();
		this.voucherTypeID = voucherTypeID;
		this.currencyID = currencyID;
	}
	
	private CurrencyID currencyID;
	public CurrencyID getCurrencyID() {
		return currencyID;
	}
	public void setCurrencyID(CurrencyID currencyID) {
		this.currencyID = currencyID;
	}	
	
	private ProductTypeID voucherTypeID;
	public ProductTypeID getVoucherTypeID() {
		return voucherTypeID;
	}
	public void setVoucherTypeID(ProductTypeID voucherTypeID) {
		this.voucherTypeID = voucherTypeID;
	}

}
