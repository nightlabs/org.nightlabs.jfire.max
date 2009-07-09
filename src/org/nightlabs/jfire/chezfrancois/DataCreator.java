/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.chezfrancois;

import java.util.Date;
import java.util.Locale;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.AccountType;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.PriceFragmentTypeHelper;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.book.mappingbased.MoneyFlowMapping;
import org.nightlabs.jfire.accounting.book.mappingbased.PFMoneyFlowMapping;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.datafield.PhoneNumberDataField;
import org.nightlabs.jfire.prop.datafield.SelectionDataField;
import org.nightlabs.jfire.prop.exception.DataBlockGroupNotFoundException;
import org.nightlabs.jfire.prop.exception.DataBlockNotFoundException;
import org.nightlabs.jfire.prop.exception.DataFieldNotFoundException;
import org.nightlabs.jfire.prop.exception.StructBlockNotFoundException;
import org.nightlabs.jfire.prop.exception.StructFieldNotFoundException;
import org.nightlabs.jfire.prop.exception.StructFieldValueNotFoundException;
import org.nightlabs.jfire.prop.structfield.SelectionStructField;
import org.nightlabs.jfire.prop.structfield.StructFieldValue;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.trade.SegmentType;
import org.nightlabs.jfire.trade.Trader;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.jfire.transfer.id.AnchorID;

public class DataCreator
{
	public static final String languageID = Locale.ENGLISH.getLanguage();
	public static final String[] languages = new String[] {
		Locale.ENGLISH.getLanguage(),
		Locale.GERMAN.getLanguage(),
		Locale.FRENCH.getLanguage()
	};

	protected PersistenceManager pm;
	protected String organisationID;
	protected User user;
	protected Trader trader;
	protected Store store;
	protected Accounting accounting;
	protected String rootOrganisationID;

	public DataCreator(PersistenceManager pm, User user)
	{
		this.pm = pm;
		this.user = user;
		this.organisationID = user.getOrganisationID();

		trader = Trader.getTrader(pm);
		store = trader.getStore();
		accounting = trader.getAccounting();

		try {
			InitialContext ctx = new InitialContext();
			try {
				rootOrganisationID = Organisation.getRootOrganisationID(ctx);
			} finally {
				ctx.close();
			}
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Set names for different languages. The order for language
	 * entries is defined by {@link #languages}.
	 * @param names Names in different languages.
	 * @param name The i18n text object to set.
	 */
	protected void setNames(I18nText name, String[] names)
	{
		String prefix = "";
		if (ChezFrancoisServerInitialiser.ORGANISATION_ID_RESELLER.equals(organisationID)) {
			prefix = "R ";
		}

		int langIdx = 0;
		for (String string : names) {
			if(langIdx >= languages.length)
				break;
			name.setText(languages[langIdx], prefix + string);
			langIdx++;
		}
	}


	private Tariff tariffNormalPrice = null;
	private Tariff tariffGoldCard = null;

	public Tariff getTariffNormalPrice()
	{
		if (tariffNormalPrice == null) {
			pm.getExtent(Tariff.class);
			TariffID tariffID = TariffID.create(organisationID, "_normal_price_");
			try {
				tariffNormalPrice = (Tariff) pm.getObjectById(tariffID);
			} catch (JDOObjectNotFoundException x) {
				tariffNormalPrice = pm.makePersistent(new Tariff(tariffID.organisationID, tariffID.tariffID));
				tariffNormalPrice.setTariffIndex(0);
				tariffNormalPrice.getName().setText(Locale.ENGLISH.getLanguage(), "Normal Price");
				tariffNormalPrice.getName().setText(Locale.GERMAN.getLanguage(), "Normaler Preis");
				tariffNormalPrice.getName().setText(Locale.FRENCH.getLanguage(), "Prix normal");
			}
		}
		return tariffNormalPrice;
	}

	public Tariff getTariffGoldCard()
	{
		if (tariffGoldCard == null) {
			pm.getExtent(Tariff.class);
			TariffID tariffID = TariffID.create(organisationID, "_gold_card_");
			try {
				tariffGoldCard = (Tariff) pm.getObjectById(tariffID);
			} catch (JDOObjectNotFoundException x) {
				tariffGoldCard = pm.makePersistent(new Tariff(tariffID.organisationID, tariffID.tariffID));
				tariffGoldCard.setTariffIndex(1);
				tariffGoldCard.getName().setText(Locale.ENGLISH.getLanguage(), "Gold Card");
				tariffGoldCard.getName().setText(Locale.GERMAN.getLanguage(), "Goldene Kundenkarte");
				tariffGoldCard.getName().setText(Locale.FRENCH.getLanguage(), "Carte d'or");
			}
		}
		return tariffGoldCard;
	}


	private Currency euro = null;
	public Currency getCurrencyEUR()
	{
		if (euro == null) {
			pm.getExtent(Currency.class);
			euro = (Currency) pm.getObjectById(CurrencyID.create("EUR"));
		}

		return euro;
	}

	private PriceFragmentType priceFragmentTypeTotal = null;
	public PriceFragmentType getPriceFragmentTypeTotal()
	{
		if (priceFragmentTypeTotal == null)
			priceFragmentTypeTotal = PriceFragmentType.getTotalPriceFragmentType(pm);

		return priceFragmentTypeTotal;
	}

	private PriceFragmentType priceFragmentTypeVatNet = null;
	public PriceFragmentType getPriceFragmentTypeVatNet() {
		if (priceFragmentTypeVatNet == null)
			priceFragmentTypeVatNet = (PriceFragmentType) pm.getObjectById(new PriceFragmentTypeHelper().getDE().VAT_DE_19_NET);

		return priceFragmentTypeVatNet;
	}

	private PriceFragmentType priceFragmentTypeVatVal = null;
	public PriceFragmentType getPriceFragmentTypeVatVal() {
		if (priceFragmentTypeVatVal == null)
			priceFragmentTypeVatVal = (PriceFragmentType) pm.getObjectById(new PriceFragmentTypeHelper().getDE().VAT_DE_19_VAL);

		return priceFragmentTypeVatVal;
	}


	private OrganisationLegalEntity organisationLegalEntity = null;
	protected OrganisationLegalEntity getOrganisationLegalEntity()
	{
		if (organisationLegalEntity == null)
			organisationLegalEntity = OrganisationLegalEntity.getOrganisationLegalEntity(
					pm, organisationID);

		return organisationLegalEntity;
	}

	private CustomerGroup customerGroupDefault = null;
	public CustomerGroup getCustomerGroupDefault()
	{
		if (customerGroupDefault == null)
			customerGroupDefault = (CustomerGroup) pm.getObjectById(CustomerGroupID.create(organisationID, CustomerGroup.CUSTOMER_GROUP_ID_DEFAULT));

		return customerGroupDefault;
	}

	private CustomerGroup customerGroupAnonymous = null;
	public CustomerGroup getCustomerGroupAnonymous()
	{
		if (customerGroupAnonymous == null)
			customerGroupAnonymous = (CustomerGroup) pm.getObjectById(CustomerGroupID.create(organisationID, CustomerGroup.CUSTOMER_GROUP_ID_ANONYMOUS));

		return customerGroupAnonymous;
	}

	public Account createLocalRevenueAccount(String anchorIDSuffix, String name)
	{
		Currency euro = getCurrencyEUR();

		AccountType accountType = (AccountType) pm.getObjectById(AccountType.ACCOUNT_TYPE_ID_LOCAL_REVENUE);
		Account account = new Account(
				organisationID, "revenue#" + anchorIDSuffix, accountType, getOrganisationLegalEntity(), euro);
		account.getName().setText(languageID, name);

		account = pm.makePersistent(account);

		return account;
	}
	public Account createLocalExpenseAccount(String anchorIDSuffix, String name)
	{
		Currency euro = getCurrencyEUR();

		AccountType accountType = (AccountType) pm.getObjectById(AccountType.ACCOUNT_TYPE_ID_LOCAL_EXPENSE);
		Account account = new Account(
				organisationID, "expense#" + anchorIDSuffix, accountType, getOrganisationLegalEntity(), euro);
		account.getName().setText(languageID, name);

		account = pm.makePersistent(account);

		return account;
	}

	public PFMoneyFlowMapping createPFMoneyFlowMapping(
			ProductType productType, PriceFragmentType priceFragmentType, Account revenueAccount, Account expenseAccount)
	{
		Currency euro = getCurrencyEUR();
		PFMoneyFlowMapping mapping = new PFMoneyFlowMapping(
				IDGenerator.getOrganisationID(),
				IDGenerator.nextID(MoneyFlowMapping.class),
				productType,
				MoneyFlowMapping.PACKAGE_TYPE_PACKAGE,
				priceFragmentType,
				euro
		);
		mapping.setOwner(null);
		mapping.setSourceOrganisationID(null);
		mapping.setRevenueAccount(revenueAccount);
		mapping.setExpenseAccount(expenseAccount);

		return mapping;
	}

	public User createUser(
			String userID, String password,
			String personCompany, String personName, String personFirstName, String personEMail)
	throws
	SecurityException, DataBlockNotFoundException, DataBlockGroupNotFoundException, DataFieldNotFoundException
	{
		User user = null;
		pm.getExtent(User.class);
		try {
			user = (User) pm.getObjectById(UserID.create(organisationID, userID));
			// it already exists => return, but before check for existing person
			if (user.getPerson() != null)
				return user;
		} catch (JDOObjectNotFoundException x) {
			// fine, it doesn't exist yet => create it
			user = new User(organisationID, userID);
			UserLocal userLocal = new UserLocal(user);
			userLocal.setPasswordPlain(password);
		}

		Person person = createPerson(personCompany, personName, personFirstName, personEMail);
		user.setPerson(person);
		user = pm.makePersistent(user);
		return user;
	}

	public User createUser(String userID, String password, Person person)
//	throws SecurityException, DataBlockNotFoundException, DataBlockGroupNotFoundException, DataFieldNotFoundException
	{
		pm.getExtent(User.class);
		try {
			User user = (User) pm.getObjectById(UserID.create(organisationID, userID));
			// it already exists => return
			return user;
		} catch (JDOObjectNotFoundException x) {
			// fine, it doesn't exist yet
		}

		User user = new User(organisationID, userID);
		UserLocal userLocal = new UserLocal(user);
		userLocal.setPasswordPlain(password);
		user.setPerson(person);
		user = pm.makePersistent(user);
		return user;
	}

	public Person createPerson(String company, String name, String firstName, String eMail,
			Date dateOfBirth, String salutation, String title, String postAdress, String postCode,
			String postCity, String postRegion, String postCountry, String phoneCountryCode,
			String phoneAreaCode, String phoneNumber, String faxCountryCode,
			String faxAreaCode, String faxNumber, String bankAccountHolder, String bankAccountNumber,
			String bankCode, String bankName, String creditCardHolder, String creditCardNumber,
			int creditCardExpiryMonth, int creditCardExpiryYear, String comment)
	throws DataBlockNotFoundException, DataBlockGroupNotFoundException, DataFieldNotFoundException, StructFieldValueNotFoundException, StructFieldNotFoundException, StructBlockNotFoundException
	{
		IStruct personStruct = getPersonStruct();

		Person person = new Person(IDGenerator.getOrganisationID(), IDGenerator.nextID(PropertySet.class));
		person.inflate(personStruct);
		person.getDataField(PersonStruct.PERSONALDATA_COMPANY).setData(company);
		person.getDataField(PersonStruct.PERSONALDATA_NAME).setData(name);
		person.getDataField(PersonStruct.PERSONALDATA_FIRSTNAME).setData(firstName);
		person.getDataField(PersonStruct.INTERNET_EMAIL).setData(eMail);
		person.getDataField(PersonStruct.PERSONALDATA_DATEOFBIRTH).setData(dateOfBirth);

		SelectionStructField salutationSelectionStructField = (SelectionStructField) personStruct.getStructField(
				PersonStruct.PERSONALDATA, PersonStruct.PERSONALDATA_SALUTATION);
		StructFieldValue sfv = salutationSelectionStructField.getStructFieldValue(PersonStruct.PERSONALDATA_SALUTATION_MR);
		person.getDataField(PersonStruct.PERSONALDATA_SALUTATION, SelectionDataField.class).setSelection(sfv);

		person.getDataField(PersonStruct.PERSONALDATA_TITLE).setData(title);
		person.getDataField(PersonStruct.POSTADDRESS_ADDRESS).setData(postAdress);
		person.getDataField(PersonStruct.POSTADDRESS_POSTCODE).setData(postCode);
		person.getDataField(PersonStruct.POSTADDRESS_CITY).setData(postCity);
		person.getDataField(PersonStruct.POSTADDRESS_REGION).setData(postRegion);
		person.getDataField(PersonStruct.POSTADDRESS_COUNTRY).setData(postCountry);

		PhoneNumberDataField phoneNumberDF = person.getDataField(PersonStruct.PHONE_PRIMARY, PhoneNumberDataField.class);
		phoneNumberDF.setCountryCode(phoneCountryCode);
		phoneNumberDF.setAreaCode(phoneAreaCode);
		phoneNumberDF.setLocalNumber(phoneNumber);

		PhoneNumberDataField faxDF = person.getDataField(PersonStruct.FAX, PhoneNumberDataField.class);
		faxDF.setCountryCode(faxCountryCode);
		faxDF.setAreaCode(faxAreaCode);
		faxDF.setLocalNumber(faxNumber);

		person.getDataField(PersonStruct.BANKDATA_ACCOUNTHOLDER).setData(bankAccountHolder);
		person.getDataField(PersonStruct.BANKDATA_ACCOUNTNUMBER).setData(bankAccountNumber);
		person.getDataField(PersonStruct.BANKDATA_BANKCODE).setData(bankCode);
		person.getDataField(PersonStruct.BANKDATA_BANKNAME).setData(bankName);

		person.getDataField(PersonStruct.CREDITCARD_CREDITCARDHOLDER).setData(creditCardHolder);
		person.getDataField(PersonStruct.CREDITCARD_NUMBER).setData(creditCardNumber);

//		((NumberDataField)person.getDataField(PersonStruct.CREDITCARD_EXPIRYMONTH)).setValue(creditCardExpiryMonth);

		SelectionStructField expiryMonthStructField = (SelectionStructField) personStruct.getStructField(
				PersonStruct.CREDITCARD, PersonStruct.CREDITCARD_EXPIRYMONTH);
		if (creditCardExpiryMonth < 1 || creditCardExpiryMonth > 12)
			sfv = null;
		else
			sfv = expiryMonthStructField.getStructFieldValue(PersonStruct.CREDITCARD_EXPIRYMONTHS[creditCardExpiryMonth - 1]);
		person.getDataField(PersonStruct.CREDITCARD_EXPIRYMONTH, SelectionDataField.class).setSelection(sfv);

		person.getDataField(PersonStruct.CREDITCARD_EXPIRYYEAR).setData(creditCardExpiryYear);

		person.getDataField(PersonStruct.COMMENT_COMMENT).setData(comment);

		person.setAutoGenerateDisplayName(true);
		person.setDisplayName(null, personStruct);
		person.deflate();
		pm.makePersistent(person);
		return person;
	}

	private IStruct personStruct = null;
	protected IStruct getPersonStruct()
	{
		if (personStruct == null) {
			// We have to work with the StructLocal here...
			// personStruct = Struct.getStruct(getOrganisationLegalEntity().getOrganisationID(), Person.class, pm);
//			personStruct = StructLocal.getStructLocal(pm, getOrganisationLegalEntity().getOrganisationID(), Person.class.getName(), Person.STRUCT_SCOPE, Person.STRUCT_LOCAL_SCOPE);
			personStruct = PersonStruct.getPersonStructLocal(pm);
		}

		return personStruct;
	}

	public Person createPerson(
			String company, String name, String firstName, String eMail)
	throws DataBlockNotFoundException, DataBlockGroupNotFoundException, DataFieldNotFoundException
	{
		IStruct personStruct = getPersonStruct();
		Person person = new Person(IDGenerator.getOrganisationID(), IDGenerator.nextID(PropertySet.class));
		person.inflate(personStruct);
		person.getDataField(PersonStruct.PERSONALDATA_COMPANY).setData(company);
		person.getDataField(PersonStruct.PERSONALDATA_NAME).setData(name);
		person.getDataField(PersonStruct.PERSONALDATA_FIRSTNAME).setData(firstName);
		person.getDataField(PersonStruct.INTERNET_EMAIL).setData(eMail);
		person.setAutoGenerateDisplayName(true);
		person.setDisplayName(null, personStruct);
		person.deflate();
		person = pm.makePersistent(person);
		return person;
	}

	public LegalEntity createLegalEntity(Person person)
	{
		if (person == null)
			throw new IllegalArgumentException("person must not be null!");

		Trader trader = Trader.getTrader(pm);
		return trader.setPersonToLegalEntity(person, true);
	}

	/**
	 * This method creates a vendor. If this vendor already exists, it is returned without any write action.
	 */
	public LegalEntity createVendor1()
	throws DataBlockNotFoundException, DataBlockGroupNotFoundException, DataFieldNotFoundException
	{
		return createVendor("alexandra.kessler", "Weinexport GmbH", "Kessler", "Alexandra", "Alexandra@Weinexport.co.th");
	}

	/**
	 * This method creates a vendor with the specified properties. If a vendor with the ID specified by <code>_vendorID</code>
	 * already exists, this method returns it without changing it. In this case, the parameters <code>company</code>,
	 * <code>name</code> etc. are ignored.
	 *
	 * @param _vendorID the local part of the primary key (within the namespace of the current <code>organisationID</code>).
	 * @param company the name of the company.
	 * @param name the name of the contact person.
	 * @param firstName the first name of the contact person.
	 * @param eMail the e-mail address of the company/contact person.
	 * @return the new or previously existing vendor.
	 */
	public LegalEntity createVendor(String _vendorID, String company, String name, String firstName, String eMail)
	throws DataBlockNotFoundException, DataBlockGroupNotFoundException, DataFieldNotFoundException
	{
		pm.getExtent(LegalEntity.class); // ensure meta-data is loaded

		AnchorID vendorID = AnchorID.create(organisationID, LegalEntity.ANCHOR_TYPE_ID_LEGAL_ENTITY, _vendorID);
		try {
			return (LegalEntity) pm.getObjectById(vendorID);
		} catch (JDOObjectNotFoundException x) {
			// vendor does not exist => create it below
		}

		Person vendorPerson = createPerson(company, name, firstName, eMail);
		LegalEntity vendor = new LegalEntity(vendorID.organisationID, vendorID.anchorID);
		vendor = pm.makePersistent(vendor);
		vendor.setPerson(vendorPerson);
		vendor.setDefaultCustomerGroup(Trader.getTrader(pm).getDefaultCustomerGroupForKnownCustomer());
		return vendor;
	}

	public Order createOrderForEndcustomer(LegalEntity customer)
	{
		Trader trader = Trader.getTrader(pm);
		Order order = trader.createOrder(trader.getMandator(), customer, null, getCurrencyEUR());
		trader.createSegment(order, SegmentType.getDefaultSegmentType(pm));
		return order;
	}

}
