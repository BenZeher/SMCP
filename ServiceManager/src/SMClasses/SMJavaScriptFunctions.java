package SMClasses;

public class SMJavaScriptFunctions {
	public static final String sDiscountAmountChange = " onchange=\"discountAmountChanged();\"";
	public static final String sDiscountPercentChange =  " onchange=\"discountPercentageChanged();\"";

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
