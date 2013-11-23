package org.nightlabs.jfire.pbx;

import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.prop.datafield.PhoneNumberDataField;
import org.nightlabs.jfire.prop.id.DataFieldID;

public class PhoneNumberDataFieldCall implements Call {
	private static final long serialVersionUID = 1L;

	private DataFieldID phoneNumberDataFieldID;

	public PhoneNumberDataFieldCall(DataFieldID phoneNumberDataFieldID) {
		this.phoneNumberDataFieldID = phoneNumberDataFieldID;
	}

	public DataFieldID getPhoneNumberDataFieldID() {
		return phoneNumberDataFieldID;
	}
	
	@Override
	public String getDialPhoneNumber(PersistenceManager pm, PhoneSystem phoneSystem) throws UnsupportedCallException {
		if (getPhoneNumberDataFieldID() == null)
			throw new UnsupportedCallException(this, "The call must contain a phoneNumberDataFieldID!"); //$NON-NLS-1$

		Object o = pm.getObjectById(getPhoneNumberDataFieldID());
		if (!(o instanceof PhoneNumberDataField))
			throw new IllegalArgumentException("call.phoneNumberDataFieldID does not reference an instance of PhoneNumberDataField! Instead, it is: " + o.getClass().getName()); //$NON-NLS-1$

		PhoneNumberDataField phoneNumberDataField = (PhoneNumberDataField) o;

		// Pattern to clean all spaces, minuses etc. in order to make the number
		// an ordinary-phone-callable-number (i.e. purely digits from 0 to 9).
		Pattern cleanDialPhoneNumberPattern = Pattern.compile("[^0-9]"); //$NON-NLS-1$

		//create dial phone number
		StringBuilder dialPhoneNumberSB = new StringBuilder();

		// First, we clean the country-code individually to decide whether there really is one (might be only spaces).
		String countryCode = phoneNumberDataField.getCountryCode();
		if (countryCode != null && !countryCode.isEmpty())
			countryCode = cleanDialPhoneNumberPattern.matcher(countryCode).replaceAll(""); //$NON-NLS-1$

		// In order to support local numbers (within the local PBX), we only append the
		// international-call-prefix, if we have a country code. Otherwise, we omit it.
		if (countryCode != null && !countryCode.isEmpty()) {
			dialPhoneNumberSB.append(phoneSystem.getInternationalCallPrefix());
			dialPhoneNumberSB.append(countryCode);
		}

		// Append the area code and the local number.
		dialPhoneNumberSB.append(phoneNumberDataField.getAreaCode());
		dialPhoneNumberSB.append(phoneNumberDataField.getLocalNumber());

		// Clean all spaces, minuses etc. to make it an ordinary-phone-callable-number
		// (i.e. purely digits from 0 to 9).
		String dialPhoneNumber = cleanDialPhoneNumberPattern.matcher(dialPhoneNumberSB.toString()).replaceAll(""); //$NON-NLS-1$
		return dialPhoneNumber;
	}

}
