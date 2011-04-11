/**
 * This is initial script which is used when new LDAPServer and corresponding LDAPScriptSet are created. 
 * All changes in this particular file WILL NOT be reflected in existing LDAPServers but only in new ones.
 * 
 * This script is used for binding script variables to values taken from JFire objects (e.g. User and Person).
 * These variables are used afterwards in other scripts (i.e. InetOrgPersonGetAttributesForLDAPScript.js, SambaGetDNScript.js etc.)
 * so this script SHOULD be evaluated BEFORE other scripts are evaluated or joined at first place together with them (see {@link LDAPScriptSet}).
 * 
 * It makes use of <code>user</code> and <code>person</code> java objects passed to evaluating ScriptContext.
 * 
 * NOT supposed to return any values.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 * 
 */
importClass(org.nightlabs.jfire.person.PersonStruct);

var $userID$ = null;
var $userName$ = null;
var $userDescription$ = null;
var $userType$ = null;
var $userOrganisationID$ = null;
var $userChangeDT$ = null;
var $userCompleteUserID$ = null;
var userData = null;
try{
	userData = user;
}catch(e){
	// do nothing
}
if (userData != null){
	$userID$ = userData.getUserID();
	$userName$ = userData.getName();
	$userDescription$ = userData.getDescription();
	$userType$ = userData.getUserType();
	$userOrganisationID$ = userData.getOrganisationID();
	$userChangeDT$ = userData.getChangeDT();
	$userCompleteUserID$ = userData.getCompleteUserID();
}

var $personID$ = null;
var $personOrganisationID$ = null;
var $personLocaleLanguage$ = null;

// internal
var $personDisplayName$ = null;

// personal data
var $personCompany$ = null;
var $personName$ = null;
var $personFirstName$ = null;
var $personSalutation$ = null;
var $personTitle$ = null;
var $personDateOfBirth$ = null;
var $personPhoto$ = null;

// postadress
var $personAddress$ = null;
var $personPostCode$ = null;
var $personCity$ = null;
var $personRegion$ = null;
var $personCountry$ = null;

// internet
var $personEMail$ = null;
var $personHomepage$ = null;

// phone
var $personPhonePrimary$ = null;
var $personFax$ = null;

// bankdata
var $personAccountHolder$ = null;
var $personBankCode$ = null;
var $personBankName$ = null;
var $personAccountNumber$ = null;
var $personIBAN$ = null;
var $personBIC$ = null;

// creditcard
var $personCreditCardHolder$ = null;
var $personCreditCardNumber$ = null;
var $personCreditCardExpiryYear$ = null;
var $personCreditCardExpiryMonth$ = null;

// govermentaldata
var $personVATIN$ = null;
var $personNationalTaxNumber$ = null;
var $personTradeRegisterName$ = null;
var $personTradeRegisterNumber$ = null;

// comment
var $personComment$ = null;

var personData = null;
try{
	personData = person;
}catch(e){
	// do nothing
}

function getPersonDataFieldValue(person, fieldID){
	return person.getPersistentDataFieldByIndex(fieldID, 0)!=null?person.getPersistentDataFieldByIndex(fieldID, 0).getData():null;	
}
	
if (personData != null){
	
	$personID$ = personData.getPropertySetID();
	$personOrganisationID$ = personData.getOrganisationID();
	$personLocaleLanguage$ = personData.getLocale().getLanguage();
	if (personData.getLocale().getCountry() != null && personData.getLocale().getCountry() != ''){
		$personLocaleLanguage$ = $personLocaleLanguage$ + '_' + personData.getLocale().getCountry();
	}
	$personDisplayName$ = personData.getDisplayName();
	
	// personal data
	$personCompany$ = getPersonDataFieldValue(personData, PersonStruct.PERSONALDATA_COMPANY);
	$personName$ = getPersonDataFieldValue(personData, PersonStruct.PERSONALDATA_NAME);
	$personFirstName$ = getPersonDataFieldValue(personData, PersonStruct.PERSONALDATA_FIRSTNAME);
	$personSalutation$ = getPersonDataFieldValue(personData, PersonStruct.PERSONALDATA_SALUTATION);
	$personTitle$ = getPersonDataFieldValue(personData, PersonStruct.PERSONALDATA_TITLE);
	$personDateOfBirth$ = getPersonDataFieldValue(personData, PersonStruct.PERSONALDATA_DATEOFBIRTH);
	$personPhoto$ = getPersonDataFieldValue(personData, PersonStruct.PERSONALDATA_PHOTO);
	
	// postadress
	$personAddress$ = getPersonDataFieldValue(personData, PersonStruct.POSTADDRESS_ADDRESS);
	$personPostCode$ = getPersonDataFieldValue(personData, PersonStruct.POSTADDRESS_POSTCODE);
	$personCity$ = getPersonDataFieldValue(personData, PersonStruct.POSTADDRESS_CITY);
	$personRegion$ = getPersonDataFieldValue(personData, PersonStruct.POSTADDRESS_REGION);
	$personCountry$ = getPersonDataFieldValue(personData, PersonStruct.POSTADDRESS_COUNTRY);

	// internet
	$personEMail$ = getPersonDataFieldValue(personData, PersonStruct.INTERNET_EMAIL);
	$personHomepage$ = getPersonDataFieldValue(personData, PersonStruct.INTERNET_HOMEPAGE);
	
	// phone
	$personPhonePrimary$ = getPersonDataFieldValue(personData, PersonStruct.PHONE_PRIMARY);
	if ($personPhonePrimary$ != null){
		$personPhonePrimary$ = $personPhonePrimary$.toString();
	}
	$personFax$ = getPersonDataFieldValue(personData, PersonStruct.FAX);
	if ($personFax$ != null){
		$personFax$ = $personFax$.toString();
	}
	
	// bankdata
	$personAccountHolder$ = getPersonDataFieldValue(personData, PersonStruct.BANKDATA_ACCOUNTHOLDER);
	$personBankCode$ = getPersonDataFieldValue(personData, PersonStruct.BANKDATA_BANKCODE);
	$personBankName$ = getPersonDataFieldValue(personData, PersonStruct.BANKDATA_BANKNAME);
	$personAccountNumber$ = getPersonDataFieldValue(personData, PersonStruct.BANKDATA_ACCOUNTNUMBER);
	$personIBAN$ = getPersonDataFieldValue(personData, PersonStruct.BANKDATA_IBAN);
	$personBIC$ = getPersonDataFieldValue(personData, PersonStruct.BANKDATA_BIC);
	
	// creditcard
	$personCreditCardHolder$ = getPersonDataFieldValue(personData, PersonStruct.CREDITCARD_CREDITCARDHOLDER);
	$personCreditCardNumber$ = getPersonDataFieldValue(personData, PersonStruct.CREDITCARD_NUMBER);
	$personCreditCardExpiryYear$ = getPersonDataFieldValue(personData, PersonStruct.CREDITCARD_EXPIRYYEAR);
	$personCreditCardExpiryMonth$ = getPersonDataFieldValue(personData, PersonStruct.CREDITCARD_EXPIRYMONTH);
	
	// govermentaldata
	$personVATIN$ = getPersonDataFieldValue(personData, PersonStruct.GOVERNMENTALDATA_VATIN);
	$personNationalTaxNumber$ = getPersonDataFieldValue(personData, PersonStruct.GOVERNMENTALDATA_NATIONALTAXNUMBER);
	$personTradeRegisterName$ = getPersonDataFieldValue(personData, PersonStruct.GOVERNMENTALDATA_TRADEREGISTERNAME);
	$personTradeRegisterNumber$ = getPersonDataFieldValue(personData, PersonStruct.GOVERNMENTALDATA_TRADEREGISTERNUMBER);
	
	// comment
	$personComment$ = getPersonDataFieldValue(personData, PersonStruct.COMMENT_COMMENT);
}