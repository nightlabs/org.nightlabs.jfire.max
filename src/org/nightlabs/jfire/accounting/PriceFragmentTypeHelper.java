package org.nightlabs.jfire.accounting;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.nightlabs.jfire.accounting.id.PriceFragmentTypeID;
import org.nightlabs.jfire.organisation.Organisation;

public class PriceFragmentTypeHelper
{
	private static String getPriceFragmentTypeOrganisationID()
	{
		try {
			InitialContext initialContext = new InitialContext();
			try {
				return Organisation.getRootOrganisationID(initialContext);
			} finally {
				initialContext.close();
			}
		} catch (NamingException x) {
			throw new RuntimeException(x); // should never happen - our internal JNDI should always be accessible
		}
	}

	public static class DE {
		public final PriceFragmentTypeID VAT_DE_19_NET;
		public final PriceFragmentTypeID VAT_DE_19_VAL;
		public final PriceFragmentTypeID VAT_DE_7_NET;
		public final PriceFragmentTypeID VAT_DE_7_VAL;

		{
			VAT_DE_19_NET = PriceFragmentTypeID.create(getPriceFragmentTypeOrganisationID(), "vat-de-19-net");
			VAT_DE_19_VAL = PriceFragmentTypeID.create(getPriceFragmentTypeOrganisationID(), "vat-de-19-val");
			VAT_DE_7_NET = PriceFragmentTypeID.create(getPriceFragmentTypeOrganisationID(), "vat-de-7-net");
			VAT_DE_7_VAL = PriceFragmentTypeID.create(getPriceFragmentTypeOrganisationID(), "vat-de-7-val");
		}
	}

	public static class CH {
		public final PriceFragmentTypeID VAT_CH_7_6_NET;
		public final PriceFragmentTypeID VAT_CH_7_6_VAL;

		{
			VAT_CH_7_6_NET = PriceFragmentTypeID.create(getPriceFragmentTypeOrganisationID(), "vat-ch-7_6-net");
			VAT_CH_7_6_VAL = PriceFragmentTypeID.create(getPriceFragmentTypeOrganisationID(), "vat-ch-7_6-val");
		}
	}

	private static DE de;
	public static DE getDE()
	{
		if (de == null)
			de = new DE();
		
		return de;
	}

	private static CH ch;
	public static CH getCH()
	{
		if (ch == null)
			ch = new CH();

		return ch;
	}
}
