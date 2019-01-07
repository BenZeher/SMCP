package ServletUtilities;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class clsValidateFormFields {

	public static final long MAX_LONG_VALUE = java.lang.Long.MAX_VALUE;
	public static final long MIN_LONG_VALUE = java.lang.Long.MIN_VALUE;
	public static final int MAX_INT_VALUE = java.lang.Integer.MAX_VALUE;
	public static final int MIN_INT_VALUE = java.lang.Integer.MIN_VALUE;
	
	public static String validateStringField(String sTestField, int iMaxLength, String sFieldName, boolean bAllowBlank) throws Exception{
		String s = "";
		
		if(sTestField == null || sTestField == ""){
			//If this field cannot be empty (required) then throw exception if it is empty or null
			if(!bAllowBlank){
				throw new Exception(sFieldName + " cannot be empty.");
			//Otherwise, if the field can be empty return an empty string
			}else{
				return "";
			}
		}

		s = sTestField.trim();
		if (s.length() > iMaxLength){
			throw new Exception(sFieldName + " cannot be longer than " + iMaxLength + " characters.");
		}
		return s;
	}
	public static String validateIntegerField(String sTestField, String sFieldName, int iMinimumValue, int iMaximumValue) throws Exception{
		String s = "";
		if (sTestField == null){
			throw new Exception(sFieldName + " cannot be empty.");
		}
		s = sTestField.trim();
		try {
			@SuppressWarnings("unused")
			int iTest = Integer.parseInt(sTestField);
		} catch (Exception e) {
			throw new Exception(sFieldName + " value '" + sTestField + "' is invalid - " + e.getMessage());
		}
		return s;
	}
	public static String validateLongIntegerField(String sTestField, String sFieldName, long iMinimumValue, long iMaximumValue) throws Exception{
		String s = "";
		if (sTestField == null){
			throw new Exception(sFieldName + " cannot be empty.");
		}
		s = sTestField.trim();
		try {
			@SuppressWarnings("unused")
			long iTest = Long.parseLong(sTestField);
		} catch (Exception e) {
			throw new Exception(sFieldName + " value '" + sTestField + "' is invalid - " + e.getMessage() + ".");
		}
		return s;
	}
	public static String validateBigdecimalField(
			String sTestField, 
			String sFieldName, 
			int iScale, 
			BigDecimal bdMinimumValue, 
			BigDecimal bdMaximumValue) throws Exception{
		@SuppressWarnings("unused")
		String s = "";
		if (sTestField == null){
			throw new Exception(sFieldName + " cannot be empty.");
		}
		s = sTestField.trim().replace(",", "");
		BigDecimal bdTest = null;
		try {
			bdTest = new BigDecimal(sTestField);
		} catch (Exception e) {
			throw new Exception(sFieldName + " value '" + sTestField + "' is invalid");
		}
		if (bdTest.compareTo(bdMinimumValue) < 0){
			throw new Exception(sFieldName + " cannot be less than " + clsManageBigDecimals.BigDecimalToScaledFormattedString(iScale, bdMinimumValue));
		}
		if (bdTest.compareTo(bdMaximumValue) > 0){
			throw new Exception(sFieldName + " cannot be more than " + clsManageBigDecimals.BigDecimalToScaledFormattedString(iScale, bdMaximumValue));
		}
		return clsManageBigDecimals.BigDecimalToScaledFormattedString(iScale, bdTest);
	}
	public static String validateStandardDateField(String sDateField, String sDateFieldLabel, boolean bAllowEmptyDate) throws Exception{
		String s = sDateField;
		s = s.trim();
        if (s.compareToIgnoreCase("") == 0){
        	s = "00/00/0000";
        }
        if (s.compareToIgnoreCase("00/00/0000") != 0){
        	if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", s)){
        		throw new Exception(sDateFieldLabel + " '" + sDateField + "' is invalid.");
        	}
        }
        if (!bAllowEmptyDate){
        	if (s.compareToIgnoreCase("00/00/0000") == 0){
        		throw new Exception("An empty date is not allowed in the " + sDateFieldLabel + " field.");
        	}
        }
        return s;
	}
	
	public static String validateDateTimeField(
		String sDateTimeField, 
		String sDateFieldLabel, 
		String sDateTimeFormat, 
		boolean bAllowEmptyDate
		) throws Exception{
		
		String s = sDateTimeField.trim();
		if (bAllowEmptyDate && s.compareToIgnoreCase("00/00/0000") == 0){
			return s;
		}
		
        DateFormat sDateFormat = new SimpleDateFormat(sDateTimeFormat);
		try {
			@SuppressWarnings("unused")
			java.util.Date datTestDate = null;
			datTestDate = sDateFormat.parse(s);
		} catch (Exception e) {
			throw new Exception(sDateFieldLabel + " has an invalid date format - '" + sDateTimeField + "'.");
		}
        return s;
	}
}
