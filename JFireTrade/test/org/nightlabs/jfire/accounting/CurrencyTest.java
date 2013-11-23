package org.nightlabs.jfire.accounting;

import java.math.BigDecimal;

import org.junit.Test;

import junit.framework.Assert;


public class CurrencyTest {
	
	@Test
	public void testEquals() {
		Assert.assertEquals("Currency#equals() is not correctly implemented", new Currency("xx", "", 0), new Currency("xx", "", 0));
		Assert.assertFalse("Currency#equals() is not correctly implemented", new Currency("xx", "", 0).equals(new Currency("xxx", "", 0)));
	}
	
	@Test
	public void testToDouble() {
		long test = 12345;
		testToDouble(test, 1, 1234.5);
		testToDouble(test, 2, 123.45);
		testToDouble(test, 3, 12.345);
		testToDouble(test, 4, 1.2345);
		testToDouble(test, 5, .12345);
	}

	private void testToDouble(long test, int decimalDigits, double expected) {
		double is = new Currency("XX", "XX", decimalDigits).toDouble(test);
		Assert.assertTrue("toDouble decimalCount(" + decimalDigits +") failed. Expected " + expected + ", actual: " + is, expected == is);
	}
	

	@Test
	public void testToLong() {
		double test = 1.2345f;
		testToLong(test, 1, 12); // round down
		testToLong(test, 2, 123); // round down
		testToLong(test, 3, 1235); // round up
		testToLong(test, 4, 12345); // no rounding
		testToLong(test, 5, 123450); // no rounding
	}

	private void testToLong(double test, int decimalDigits, long expected) {
		long is = new Currency("XX", "XX", decimalDigits).toLong(test);
		Assert.assertTrue("toDouble decimalCount(" + decimalDigits +") failed. Expected " + expected + ", actual: " + is, expected == is);
	}
	
	@Test
	public void testToBigDecimal() {
		long test = 12345;
		// String-constructor of BigDecimal is predictable. double-constructor is not, as some numbers can't be exactly represented as double
		testToBigDecimal(test, 1, new BigDecimal("1234.5"));
		testToBigDecimal(test, 2, new BigDecimal("123.45"));
		testToBigDecimal(test, 3, new BigDecimal("12.345"));
		testToBigDecimal(test, 4, new BigDecimal("1.2345"));
		testToBigDecimal(test, 5, new BigDecimal(".12345"));
	}

	private void testToBigDecimal(long test, int decimalDigits, BigDecimal expected) {
		BigDecimal is = new Currency("XX", "XX", decimalDigits).toBigDecimal(test);
		Assert.assertEquals("toDouble decimalCount(" + decimalDigits +") failed.", expected, is);
	}
	
	@Test
	public void testToLongFromBigDecimal() {
		BigDecimal test = new BigDecimal("1.2345");
		testToLong(test, 1, 12); // round down
		testToLong(test, 2, 123); // round down
		testToLong(test, 3, 1235); // round up
		testToLong(test, 4, 12345); // no rounding
		testToLong(test, 5, 123450); // no rounding
	}

	private void testToLong(BigDecimal test, int decimalDigits, long expected) {
		long is = new Currency("XX", "XX", decimalDigits).toLong(test);
		Assert.assertTrue("toDouble decimalCount(" + decimalDigits +") failed. Expected " + expected + ", actual: " + is, expected == is);
	}
	
}
 