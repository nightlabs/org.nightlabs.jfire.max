// REV: Alex: These are the scripts that will be used initially 
// when a LDAP-Server (LDAPScriptSet) is created, so maybe there 
// should be a comment on top of every script telling the administrator 
// what this script is for, when it is executed and which 
// variables are published into it when it is executed.
// Additionally the comment should tell, whether the script is 
// supposed to return a value and for what this value is used

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

// REV: Alex. I think you can use the getPersistentDataFieldByIndex()-way exclusively. 
// And maybe use a small function for better readability
if (personData != null){
	
	$personID$ = personData.getPropertySetID();
	$personOrganisationID$ = personData.getOrganisationID();
	$personLocaleLanguage$ = personData.getLocale().getLanguage();
	if (personData.getLocale().getCountry() != null && personData.getLocale().getCountry() != ''){
		$personLocaleLanguage$ = $personLocaleLanguage$ + '_' + personData.getLocale().getCountry();
	}
	$personDisplayName$ = personData.getDisplayName();
	
	if (person.isInflated()){
		
		// personal data
		$personCompany$ = personData.getDataField(PersonStruct.PERSONALDATA_COMPANY).getData();
		$personName$ = personData.getDataField(PersonStruct.PERSONALDATA_NAME).getData();
		$personFirstName$ = personData.getDataField(PersonStruct.PERSONALDATA_FIRSTNAME).getData();
		$personSalutation$ = personData.getDataField(PersonStruct.PERSONALDATA_SALUTATION).getData();
		$personTitle$ = personData.getDataField(PersonStruct.PERSONALDATA_TITLE).getData();
		$personDateOfBirth$ = personData.getDataField(PersonStruct.PERSONALDATA_DATEOFBIRTH).getData();
		$personPhoto$ = personData.getDataField(PersonStruct.PERSONALDATA_PHOTO).getData();
		
		// postadress
		$personAddress$ = personData.getDataField(PersonStruct.POSTADDRESS_ADDRESS).getData();
		$personPostCode$ = personData.getDataField(PersonStruct.POSTADDRESS_POSTCODE).getData();
		$personCity$ = personData.getDataField(PersonStruct.POSTADDRESS_CITY).getData();
		$personRegion$ = personData.getDataField(PersonStruct.POSTADDRESS_REGION).getData();
		$personCountry$ = personData.getDataField(PersonStruct.POSTADDRESS_COUNTRY).getData();
	
		// internet
		$personEMail$ = personData.getDataField(PersonStruct.INTERNET_EMAIL).getData();
		$personHomepage$ = personData.getDataField(PersonStruct.INTERNET_HOMEPAGE).getData();
		
		// phone
		$personPhonePrimary$ = personData.getDataField(PersonStruct.PHONE_PRIMARY).getData();
		if ($personPhonePrimary$ != null){
			$personPhonePrimary$ = $personPhonePrimary$.toString();
		}
		$personFax$ = personData.getDataField(PersonStruct.FAX).getData();
		if ($personFax$ != null){
			$personFax$ = $personFax$.toString();
		}
		
		// bankdata
		$personAccountHolder$ = personData.getDataField(PersonStruct.BANKDATA_ACCOUNTHOLDER).getData();
		$personBankCode$ = personData.getDataField(PersonStruct.BANKDATA_BANKCODE).getData();
		$personBankName$ = personData.getDataField(PersonStruct.BANKDATA_BANKNAME).getData();
		$personAccountNumber$ = personData.getDataField(PersonStruct.BANKDATA_ACCOUNTNUMBER).getData();
		$personIBAN$ = personData.getDataField(PersonStruct.BANKDATA_IBAN).getData();
		$personBIC$ = personData.getDataField(PersonStruct.BANKDATA_BIC).getData();
		
		// creditcard
		$personCreditCardHolder$ = personData.getDataField(PersonStruct.CREDITCARD_CREDITCARDHOLDER).getData();
		$personCreditCardNumber$ = personData.getDataField(PersonStruct.CREDITCARD_NUMBER).getData();
		$personCreditCardExpiryYear$ = personData.getDataField(PersonStruct.CREDITCARD_EXPIRYYEAR).getData();
		$personCreditCardExpiryMonth$ = personData.getDataField(PersonStruct.CREDITCARD_EXPIRYMONTH).getData();
		
		// govermentaldata
		$personVATIN$ = personData.getDataField(PersonStruct.GOVERNMENTALDATA_VATIN).getData();
		$personNationalTaxNumber$ = personData.getDataField(PersonStruct.GOVERNMENTALDATA_NATIONALTAXNUMBER).getData();
		$personTradeRegisterName$ = personData.getDataField(PersonStruct.GOVERNMENTALDATA_TRADEREGISTERNAME).getData();
		$personTradeRegisterNumber$ = personData.getDataField(PersonStruct.GOVERNMENTALDATA_TRADEREGISTERNUMBER).getData();
		
		// comment
		$personComment$ = personData.getDataField(PersonStruct.COMMENT_COMMENT).getData();

	}else{	// takes by zero index
		
		// personal data
		$personCompany$ = personData.getPersistentDataFieldByIndex(PersonStruct.PERSONALDATA_COMPANY, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.PERSONALDATA_COMPANY, 0).getData():null;
		$personName$ = personData.getPersistentDataFieldByIndex(PersonStruct.PERSONALDATA_NAME, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.PERSONALDATA_NAME, 0).getData():null;
		$personFirstName$ = personData.getPersistentDataFieldByIndex(PersonStruct.PERSONALDATA_FIRSTNAME, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.PERSONALDATA_FIRSTNAME, 0).getData():null;
		$personSalutation$ = personData.getPersistentDataFieldByIndex(PersonStruct.PERSONALDATA_SALUTATION, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.PERSONALDATA_SALUTATION, 0).getData():null;
		$personTitle$ = personData.getPersistentDataFieldByIndex(PersonStruct.PERSONALDATA_TITLE, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.PERSONALDATA_TITLE, 0).getData():null;
		$personDateOfBirth$ = personData.getPersistentDataFieldByIndex(PersonStruct.PERSONALDATA_DATEOFBIRTH, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.PERSONALDATA_DATEOFBIRTH, 0).getData():null;
		$personPhoto$ = personData.getPersistentDataFieldByIndex(PersonStruct.PERSONALDATA_PHOTO, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.PERSONALDATA_PHOTO, 0).getData():null;
		
		// postadress
		$personAddress$ = personData.getPersistentDataFieldByIndex(PersonStruct.POSTADDRESS_ADDRESS, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.POSTADDRESS_ADDRESS, 0).getData():null;
		$personPostCode$ = personData.getPersistentDataFieldByIndex(PersonStruct.POSTADDRESS_POSTCODE, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.POSTADDRESS_POSTCODE, 0).getData():null;
		$personCity$ = personData.getPersistentDataFieldByIndex(PersonStruct.POSTADDRESS_CITY, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.POSTADDRESS_CITY, 0).getData():null;
		$personRegion$ = personData.getPersistentDataFieldByIndex(PersonStruct.POSTADDRESS_REGION, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.POSTADDRESS_REGION, 0).getData():null;
		$personCountry$ = personData.getPersistentDataFieldByIndex(PersonStruct.POSTADDRESS_COUNTRY, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.POSTADDRESS_COUNTRY, 0).getData():null;
	
		// internet
		$personEMail$ = personData.getPersistentDataFieldByIndex(PersonStruct.INTERNET_EMAIL, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.INTERNET_EMAIL, 0).getData():null;
		$personHomepage$ = personData.getPersistentDataFieldByIndex(PersonStruct.INTERNET_HOMEPAGE, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.INTERNET_HOMEPAGE, 0).getData():null;
		
		// phone
		$personPhonePrimary$ = personData.getPersistentDataFieldByIndex(PersonStruct.PHONE_PRIMARY, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.PHONE_PRIMARY, 0).getData():null;
		if ($personPhonePrimary$ != null){
			$personPhonePrimary$ = $personPhonePrimary$.toString();
		}
		$personFax$ = personData.getPersistentDataFieldByIndex(PersonStruct.FAX, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.FAX, 0).getData():null;
		if ($personFax$ != null){
			$personFax$ = $personFax$.toString();
		}
		
		// bankdata
		$personAccountHolder$ = personData.getPersistentDataFieldByIndex(PersonStruct.BANKDATA_ACCOUNTHOLDER, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.BANKDATA_ACCOUNTHOLDER, 0).getData():null;
		$personBankCode$ = personData.getPersistentDataFieldByIndex(PersonStruct.BANKDATA_BANKCODE, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.BANKDATA_BANKCODE, 0).getData():null;
		$personBankName$ = personData.getPersistentDataFieldByIndex(PersonStruct.BANKDATA_BANKNAME, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.BANKDATA_BANKNAME, 0).getData():null;
		$personAccountNumber$ = personData.getPersistentDataFieldByIndex(PersonStruct.BANKDATA_ACCOUNTNUMBER, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.BANKDATA_ACCOUNTNUMBER, 0).getData():null;
		$personIBAN$ = personData.getPersistentDataFieldByIndex(PersonStruct.BANKDATA_IBAN, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.BANKDATA_IBAN, 0).getData():null;
		$personBIC$ = personData.getPersistentDataFieldByIndex(PersonStruct.BANKDATA_BIC, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.BANKDATA_BIC, 0).getData():null;
		
		// creditcard
		$personCreditCardHolder$ = personData.getPersistentDataFieldByIndex(PersonStruct.CREDITCARD_CREDITCARDHOLDER, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.CREDITCARD_CREDITCARDHOLDER, 0).getData():null;
		$personCreditCardNumber$ = personData.getPersistentDataFieldByIndex(PersonStruct.CREDITCARD_NUMBER, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.CREDITCARD_NUMBER, 0).getData():null;
		$personCreditCardExpiryYear$ = personData.getPersistentDataFieldByIndex(PersonStruct.CREDITCARD_EXPIRYYEAR, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.CREDITCARD_EXPIRYYEAR, 0).getData():null;
		$personCreditCardExpiryMonth$ = personData.getPersistentDataFieldByIndex(PersonStruct.CREDITCARD_EXPIRYMONTH, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.CREDITCARD_EXPIRYMONTH, 0).getData():null;
		
		// govermentaldata
		$personVATIN$ = personData.getPersistentDataFieldByIndex(PersonStruct.GOVERNMENTALDATA_VATIN, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.GOVERNMENTALDATA_VATIN, 0).getData():null;
		$personNationalTaxNumber$ = personData.getPersistentDataFieldByIndex(PersonStruct.GOVERNMENTALDATA_NATIONALTAXNUMBER, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.GOVERNMENTALDATA_NATIONALTAXNUMBER, 0).getData():null;
		$personTradeRegisterName$ = personData.getPersistentDataFieldByIndex(PersonStruct.GOVERNMENTALDATA_TRADEREGISTERNAME, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.GOVERNMENTALDATA_TRADEREGISTERNAME, 0).getData():null;
		$personTradeRegisterNumber$ = personData.getPersistentDataFieldByIndex(PersonStruct.GOVERNMENTALDATA_TRADEREGISTERNUMBER, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.GOVERNMENTALDATA_TRADEREGISTERNUMBER, 0).getData():null;
		
		// comment
		$personComment$ = personData.getPersistentDataFieldByIndex(PersonStruct.COMMENT_COMMENT, 0)!=null?personData.getPersistentDataFieldByIndex(PersonStruct.COMMENT_COMMENT, 0).getData():null;
	}
}