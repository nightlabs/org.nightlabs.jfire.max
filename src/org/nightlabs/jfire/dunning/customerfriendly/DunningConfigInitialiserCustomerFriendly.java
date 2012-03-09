package org.nightlabs.jfire.dunning.customerfriendly;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.dunning.DunningAutoMode;
import org.nightlabs.jfire.dunning.DunningConfig;
import org.nightlabs.jfire.dunning.DunningFeeType;
import org.nightlabs.jfire.dunning.DunningStep;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.l10n.Currency;

/**
 *  TODO: write javadoc
 * @author Marius Heinzmann <!-- Marius[at]NightLabs[dot]de -->
 */
public class DunningConfigInitialiserCustomerFriendly
{

	public static DunningConfig createCustomerFriendlyDunningConfig(String organisationID, Currency currency, PriceFragmentType totalPrice)
	{
		String baseName = "org.nightlabs.jfire.dunning.resource.messages";
		ClassLoader loader = DunningConfigInitialiserCustomerFriendly.class.getClassLoader();

		DunningFeeAdderCustomerFriendly dunningFeeAdder = new DunningFeeAdderCustomerFriendly(organisationID);
		DunningInterestCalculatorCustomerFriendly dunningInterestCalculator = new DunningInterestCalculatorCustomerFriendly(organisationID);
		
		DunningConfig customerFriendlyDunningConfig = new DunningConfig(
				organisationID, DunningConfig.DUNNINGCONFIG_DEFAULT_DUNNINGCONFIGID, DunningAutoMode.createAndFinalize
		);
		customerFriendlyDunningConfig.getName().readFromProperties(baseName, loader, "org.nightlabs.jfire.dunning.DunningConfig.default.name");
		customerFriendlyDunningConfig.getDescription().readFromProperties(baseName, loader, "org.nightlabs.jfire.dunning.DunningConfig.default.description");
		
		long defaultCoolDownPeriod = TimeUnit.DAYS.toMillis(14); // TODO: What should be default coolDownPeriod? (Marius)
		//Step0
		DunningStep dunningStep0 = new DunningStep(customerFriendlyDunningConfig);
		dunningStep0.setCoolDownPeriod(defaultCoolDownPeriod);
		dunningStep0.setPeriodOfGraceMSec(TimeUnit.DAYS.toMillis(31));
		dunningStep0.setInterestPercentage(null);

		//Step1
		DunningStep dunningStep1 = new DunningStep(customerFriendlyDunningConfig);
		dunningStep1.setCoolDownPeriod(defaultCoolDownPeriod);
		dunningStep1.setPeriodOfGraceMSec(TimeUnit.DAYS.toMillis(31));
		dunningStep1.setInterestPercentage(BigDecimal.valueOf(4l, 2));
		dunningStep1.addFeeType(createDefaultDunningFeeType(organisationID, currency, totalPrice, 250l));

		//Step2
		DunningStep dunningStep2 = new DunningStep(customerFriendlyDunningConfig);
		dunningStep2.setCoolDownPeriod(defaultCoolDownPeriod);
		dunningStep2.setPeriodOfGraceMSec(TimeUnit.DAYS.toMillis(31));
		dunningStep2.setInterestPercentage(BigDecimal.valueOf(4l, 2));
		dunningStep2.addFeeType(createDefaultDunningFeeType(organisationID, currency, totalPrice, 250l));

		customerFriendlyDunningConfig.addDunningStep(dunningStep0);
		customerFriendlyDunningConfig.addDunningStep(dunningStep1);
		customerFriendlyDunningConfig.addDunningStep(dunningStep2);

		customerFriendlyDunningConfig.setInterestCalculator(dunningInterestCalculator);
		customerFriendlyDunningConfig.setFeeAdder(dunningFeeAdder);

		// FIXME: we need a default layout!! (Marius)
		// and how about the moneyflow config?
		
		return customerFriendlyDunningConfig;
	}

	private static DunningFeeType createDefaultDunningFeeType(
			String organisationID, Currency currency, PriceFragmentType totalPrice, long fee)
	{
		DunningFeeType defaultDunningFeeType = new DunningFeeType(organisationID, IDGenerator.nextID(DunningFeeType.class));
		String baseName = "org.nightlabs.jfire.dunning.resource.messages";
		ClassLoader loader = DunningConfigInitialiserCustomerFriendly.class.getClassLoader();
		defaultDunningFeeType.getName().readFromProperties(baseName, loader, "org.nightlabs.jfire.dunning.DunningFeeType.default.name");
		defaultDunningFeeType.getDescription().readFromProperties(baseName, loader, "org.nightlabs.jfire.dunning.DunningFeeType.default.description");
		// TODO: Why is Price not using the interface? DataNucleus can handle them already?!? (Marius)
		Price price = new Price(organisationID, IDGenerator.nextID(Price.class), (org.nightlabs.jfire.accounting.Currency) currency);
		price.setAmount(totalPrice, fee);
		defaultDunningFeeType.setPrice(price);
		return defaultDunningFeeType;
	}
	
}
