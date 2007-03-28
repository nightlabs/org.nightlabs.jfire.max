package org.nightlabs.jfire.voucher.scripting.javaclass;

import org.nightlabs.jfire.scripting.ScriptException;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class VoucherName 
extends AbstractVoucherScript 
{

	public Object doExecute() throws ScriptException {
		return getVoucherType().getName().getText();
	}

}
