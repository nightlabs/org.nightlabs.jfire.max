package org.nightlabs.jfire.voucher.scripting.javaclass;

import org.nightlabs.jfire.scripting.AbstractScriptExecutorJavaClassDelegate;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.voucher.scripting.VoucherScriptingConstants;
import org.nightlabs.jfire.voucher.store.Voucher;
import org.nightlabs.jfire.voucher.store.VoucherType;
import org.nightlabs.jfire.voucher.store.id.VoucherKeyID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class AbstractVoucherScript 
extends AbstractScriptExecutorJavaClassDelegate 
{

	public AbstractVoucherScript() {
		super();
	}

	public void doPrepare() throws ScriptException {
		// default implementation is empty
	}

//	public ProductID getVoucherID() {
//		return (ProductID) getParameterValue(VoucherScriptingConstants.PARAMETER_ID_VOUCHER_ID);
//	}	
	
	public VoucherKeyID getVoucherKeyID() {
		return (VoucherKeyID) getParameterValue(VoucherScriptingConstants.PARAMETER_ID_VOUCHER_KEY_ID);		
	}
	
	public org.nightlabs.jfire.voucher.store.VoucherKey getVoucherKey() {
		return (org.nightlabs.jfire.voucher.store.VoucherKey) getPersistenceManager().getObjectById(getVoucherKeyID());
	}
	
//	public Voucher getVoucher() {
//		return (Voucher) getPersistenceManager().getObjectById(getVoucherID());	
//	}
	public Voucher getVoucher() {
		return (Voucher) getVoucherKey().getVoucher();	
	}
	
	public VoucherType getVoucherType() {
		return (VoucherType) getVoucher().getProductType();
	}
}
