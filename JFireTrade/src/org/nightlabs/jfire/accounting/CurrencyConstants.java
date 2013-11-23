package org.nightlabs.jfire.accounting;

import org.nightlabs.jfire.accounting.id.CurrencyID;

/**
 * @deprecated Adding this class was a bad idea. We should get rid of all hard-coded
 * references to EUR and CHF rather than adding a constants-class for it. Btw. there's
 * now the
 */
@Deprecated
public class CurrencyConstants {
	public static final CurrencyID EUR = CurrencyID.create("EUR");
	public static final CurrencyID CHF = CurrencyID.create("CHF");
}
