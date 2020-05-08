package ServletUtilities;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class clsValidateFormFields {

	public static final long MAX_LONG_VALUE = java.lang.Long.MAX_VALUE;
	public static final long MIN_LONG_VALUE = java.lang.Long.MIN_VALUE;
	public static final int MAX_INT_VALUE = java.lang.Integer.MAX_VALUE;
	public static final int MIN_INT_VALUE = java.lang.Integer.MIN_VALUE;
	public static final String sDiscountAmountChange = " onchange=\"discountAmountChanged();\"";
	public static final String sDiscountPercentChange =  " onchange=\"discountPercentageChanged();\"";
	
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
	
	public static boolean IsValidInteger(String sInt){
		sInt = sInt.replace(",", "");
    	try{
    		Integer.parseInt(sInt);
    		return true;
    	}catch (NumberFormatException e){
    		System.out.println("[1579026397] In IsValidInteger: Error converting number from string: " + sInt + ".");
    		System.out.println("In IsValidInteger: " + e.getMessage());
    		return false;
    	}
	}
	
	public static String validateLongIntegerField(String sTestField, String sFieldName, long iMinimumValue, long iMaximumValue) throws Exception{
		String s = "";
		if (sTestField == null){
			throw new Exception(sFieldName + " cannot be empty.");
		}
		s = sTestField.trim();
		try {
			long iTest = Long.parseLong(sTestField);
			if (iTest < iMinimumValue){
				throw new Exception(sFieldName + " value '" + sTestField + "' is less than the minimum value of " + Long.toString(iMinimumValue) + ".");
			}
			if (iTest > iMaximumValue){
				throw new Exception(sFieldName + " value '" + sTestField + "' is greater than the maximum value of " + Long.toString(iMaximumValue) + ".");
			}

		} catch (Exception e) {
			throw new Exception(sFieldName + " value '" + sTestField + "' is invalid - " + e.getMessage() + ".");
		}
		return s;
	}
	
	public static boolean IsValidLong(String sLong){
		String sTestLong = sLong.replace(",", "");
    	try{
    		Long.parseLong(sTestLong);
    		return true;
    	}catch (NumberFormatException e){
    		//System.out.println("In IsValidLong: Error converting number from string: " + sLong + ".");
    		//System.out.println("In IsValidLong: " + e.getMessage());
    		return false;
    	}
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
	
	public static boolean IsValidBigDecimal(String sNumber, int iScale){
		sNumber = sNumber.replace(",", "");
    	try{
    		BigDecimal bd = new BigDecimal(sNumber);
    		bd = bd.setScale(iScale, BigDecimal.ROUND_HALF_UP);
    		return true;
    	}catch (NumberFormatException e){
    		//System.out.println("In IsValidBigDecimal: Error converting number from string: " + sNumber + ".");
    		//System.out.println("In IsValidBigDecimal: " + e.getMessage());
    		return false;
    	}
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
		
		if (bAllowEmptyDate && s.compareToIgnoreCase(clsServletUtilities.EMPTY_DATETIME_VALUE) == 0){
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
	
	public static String validateDiscountCalculation(
			String DiscountDesc,
			String DiscountPercentage,
			String DiscountAmount,
			String DISCOUNTCHANGE_FLAG,
			String DISCOUNTCHANGED_VALUE,
			String OriginalAmount
			) {
		String s="";
		s += "function validateForm(){\n"
				
			+ "    var sdiscdesc = document.getElementById(\"" 
						+ DiscountDesc + "\").value;\n"
						//If the discount amt or percentage are not BOTH zero, there must be a discount description:
			+ "    if (sdiscdesc == ''){\n"
			+ "        var sdiscountpercentage = document.getElementById(\"" 
				+ DiscountPercentage + "\").value;\n"
			+ "        if (isNumeric(sdiscountpercentage) == false){\n"
			+ "            alert(\"If the discount percentage is not zero, you MUST enter a discount description.\");\n"
			+ "            document.getElementById(\"" + DiscountDesc + "\").focus();\n"
			+ "            document.getElementById(\"" + DiscountDesc + "\").select();\n"
			+ "            return false;\n"
			+ "        }\n"
			+ "        discountpercentage = getFloat(sdiscountpercentage);\n"
			+ "        if (discountpercentage != 0.00){\n"
			+ "            alert(\"If the discount percentage is not zero, you MUST enter a discount description.\");\n"
			+ "            document.getElementById(\"" + DiscountDesc + "\").focus();\n"
			+ "            document.getElementById(\"" + DiscountDesc + "\").select();\n"
			+ "            return false;\n"
			+ "        }\n"
			//Now check the discount AMT:
			+ "        var sdiscountamount = document.getElementById(\"" 
				+ DiscountAmount + "\").value;\n"
			+ "        if (isNumeric(sdiscountamount) == false){\n"
			+ "            alert(\"If the discount amount is not zero, you MUST enter a discount description.\");\n"
			+ "            document.getElementById(\"" + DiscountDesc + "\").focus();\n"
			+ "            document.getElementById(\"" + DiscountDesc + "\").select();\n"
			+ "            return false;\n"
			+ "        }\n"
			+ "        discountamount = getFloat(sdiscountamount);\n"
			+ "        if (discountamount != 0.00){\n"
			+ "            alert(\"If the discount amount is not zero, you MUST enter a discount description.\");\n"
			+ "            document.getElementById(\"" + DiscountDesc + "\").focus();\n"
			+ "            document.getElementById(\"" + DiscountDesc + "\").select();\n"
			+ "            return false;\n"
			+ "        }\n"
			+ "    }\n"

			//Validate number fields:
			+ "    var sdiscountpercentage = document.getElementById(\"" 
				+ DiscountPercentage + "\").value;\n"
			+ "    if (isNumeric(sdiscountpercentage) == false){\n"
			+ "        alert(\"Discount percentage '\" + sdiscountpercentage + \"' is invalid.\");\n"
			+ "        document.getElementById(\"" + DiscountPercentage + "\").focus();\n"
			+ "        document.getElementById(\"" + DiscountPercentage + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    discountpercentage = getFloat(sdiscountpercentage);\n"
			+ "    if (discountpercentage < 0.00){\n"
			+ "        alert(\"Discount percentage cannot be less than zero.\");\n"
			+ "        document.getElementById(\"" + DiscountPercentage + "\").focus();\n"
			+ "        document.getElementById(\"" + DiscountPercentage + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    if (discountpercentage > 100.00){\n"
			+ "        alert(\"Discount percentage cannot be greater than 100 percent.\");\n"
			+ "        document.getElementById(\"" + DiscountPercentage + "\").focus();\n"
			+ "        document.getElementById(\"" + DiscountPercentage + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			
			+ "    var sdiscountamount = document.getElementById(\"" 
				+ DiscountAmount + "\").value;\n"
			+ "    if (isNumeric(sdiscountamount) == false){\n"
			+ "        alert(\"Discount amount '\" + sdiscountamount + \"' is invalid.\");\n"
			+ "        document.getElementById(\"" + DiscountAmount + "\").focus();\n"
			+ "        document.getElementById(\"" + DiscountAmount + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    discountamount = getFloat(sdiscountamount);\n"
			+ "    if (discountamount < 0.00){\n"
			+ "        alert(\"Discount amount cannot be less than zero.\");\n"
			+ "        document.getElementById(\"" + DiscountAmount + "\").focus();\n"
			+ "        document.getElementById(\"" + DiscountAmount + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    return true;\n"
			+ "}\n"
			;
	
		s += "function isInteger(value) {\n"
			+ "    try {\n"
			+ "        var inpVal = parseInt(value.replace(',', ''), 10);\n"
			+ "        if (isNaN(inpVal)) {\n"
			+ "            return false;\n"
			+ "        }\n"
			+ "    } catch (e) {\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    return true;\n"
			+ "}\n"
		;

		s += "function isNumeric(value) {\n"
			+ "    if ((value == null) || (value == '')) return false;\n"
			+ "    var strippedstring = value.replace(',', '');\n"
			+ "    if (!strippedstring.toString().match(/^[-]?\\d*\\.?\\d*$/)) return false;\n"
			+ "    return true\n"
			+ "    }\n"
		;
		
		s += "function getFloat(value) {\n"
			+ "    return parseFloat(value, 10);\n"
			+ "}\n"
		;
		
		s += "function discountPercentageChanged() {\n"
			+ "    document.getElementById(\"" + DISCOUNTCHANGE_FLAG + "\").value = \"" 
				+ DISCOUNTCHANGED_VALUE + "\";\n"
			+ "    flagDirty();\n"
			+ "    var sdiscountpercentage = document.getElementById(\"" 
				+ DiscountPercentage + "\").value.replace(',','');\n"
			+ "    if (isNumeric(sdiscountpercentage) == false){\n"
			+ "        alert(\"Discount percentage '\" + sdiscountpercentage + \"' is invalid.\");\n"
			+ "        document.getElementById(\"" + DiscountPercentage + "\").focus();\n"
			+ "        document.getElementById(\"" + DiscountPercentage + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    discountpercentage = getFloat(sdiscountpercentage);\n"
			+ "    if (discountpercentage < 0.00){\n"
			+ "        alert(\"Discount percentage cannot be less than zero.\");\n"
			+ "        document.getElementById(\"" + DiscountPercentage + "\").focus();\n"
			+ "        document.getElementById(\"" + DiscountPercentage + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    if (discountpercentage > 100.00){\n"
			+ "        alert(\"Discount percentage cannot be greater than 100 percent.\");\n"
			+ "        document.getElementById(\"" + DiscountPercentage + "\").focus();\n"
			+ "        document.getElementById(\"" + DiscountPercentage + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			
			//Recalculate the discount amount based on the new discount percentage:
			+ "    var extorderprice = getFloat(document.getElementById(\"" 
				+ OriginalAmount + "\").value.replace(',',''));"
			+ "    var discountamt = Math.round((discountpercentage * extorderprice / 100.00) * 100) / 100.00;"
			+ "    document.getElementById(\"" 
					+ DiscountAmount + "\").value = discountamt;\n"
			+ "}\n"
		;
		
		s += "function discountAmountChanged() {\n"
			+ "    document.getElementById(\"" + DISCOUNTCHANGE_FLAG + "\").value = \"" 
				 + DISCOUNTCHANGED_VALUE + "\";\n"
			+ "    flagDirty();\n"
			+ "    var sdiscountamount = document.getElementById(\"" 
				+ DiscountAmount + "\").value.replace(',','');\n"
			+ "    if (isNumeric(sdiscountamount) == false){\n"
			+ "        alert(\"Discount amount '\" + sdiscountamount + \"' is invalid.\");\n"
			+ "        document.getElementById(\"" + DiscountAmount + "\").focus();\n"
			+ "        document.getElementById(\"" + DiscountAmount + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    discountamount = getFloat(sdiscountamount);\n"
			+ "    if (discountamount < 0.00){\n"
			+ "        alert(\"Discount amount cannot be less than zero.\");\n"
			+ "        document.getElementById(\"" + DiscountAmount + "\").focus();\n"
			+ "        document.getElementById(\"" + DiscountAmount + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"

			+ "    var extorderprice = getFloat(document.getElementById(\"" 
				+ OriginalAmount + "\").value.replace(',',''));"
			+ "    if (extorderprice == 0.00){\n"
			+ "        document.getElementById(\"" 
				+ DiscountPercentage + "\").value = 0.00;\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    var discpercentage = Math.round(((discountamount * 100.00) / extorderprice) * 100) / 100;"
			+ "    document.getElementById(\"" 
				+ DiscountPercentage + "\").value = discpercentage;\n"
			+ "}\n"
		;
		
		return s;
	}
	
}
