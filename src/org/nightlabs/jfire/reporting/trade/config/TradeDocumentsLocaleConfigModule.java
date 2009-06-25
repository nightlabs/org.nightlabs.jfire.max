/**
 * 
 */
package org.nightlabs.jfire.reporting.trade.config;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.config.ConfigModule;

/**
 * ConfigModule to store the locale to use when rendering trade documents (reports).
 * The choices are the values of the enum {@link LocaleOption}.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireReportingTrade_TradeDocumentsReportLocalConfigModule")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class TradeDocumentsLocaleConfigModule extends ConfigModule {

	private static final long serialVersionUID = 20090624L;

	/**
	 * Options available for this config module.
	 */
	public enum LocaleOption {
		/**
		 * Use the locale of the customer.
		 */
		customerLocale,
		/**
		 * Use the locale of the user.
		 */
		userLocale,
		/**
		 * If locale of user and customer differ, ask the user which locale to use. 
		 */
		askIfCustomerAndUserLocaleDiffer,
		
		/**
		 * Use the locale of the client-application that initiated the rendering.
		 */
		clientLocale
	}
	
	private LocaleOption localeOption;
	
	/**
	 * 
	 */
	public TradeDocumentsLocaleConfigModule() {
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigModule#init()
	 */
	@Override
	public void init() {
		localeOption = LocaleOption.customerLocale;
	}

	/**
	 * @return The {@link LocaleOption}.
	 */
	public LocaleOption getLocaleOption() {
		return localeOption;
	}
	
	/**
	 * Set the locale option.
	 * @param localeOption The option to set.
	 */
	public void setLocaleOption(LocaleOption localeOption) {
		this.localeOption = localeOption;
	}

}
