package org.nightlabs.jfire.voucher.scripting.javaclass;

import org.nightlabs.jfire.scripting.ScriptException;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class VoucherKey 
extends AbstractVoucherScript 
{
	
	public Object doExecute() throws ScriptException {
		return getVoucher().getVoucherKey().getVoucherKey();
	}

}
