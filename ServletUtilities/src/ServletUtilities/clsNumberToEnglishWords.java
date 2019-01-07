package ServletUtilities;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class clsNumberToEnglishWords {

	private static final String[] tensNames = {
			"",
			" Ten",
			" Twenty",
			" Thirty",
			" Forty",
			" Fifty",
			" Sixty",
			" Seventy",
			" Eighty",
			" Ninety"
	};

	private static final String[] numNames = {
			"",
			" One",
			" Two",
			" Three",
			" Four",
			" Five",
			" Six",
			" Seven",
			" Eight",
			" Nine",
			" Ten",
			" Eleven",
			" Twelve",
			" Thirteen",
			" Fourteen",
			" Fifteen",
			" Sixteen",
			" Seventeen",
			" Eighteen",
			" Nineteen"
	};

	public clsNumberToEnglishWords() {}

	private static String convertLessThanOneThousand(int number) {
		String soFar;

		if (number % 100 < 20){
			soFar = numNames[number % 100];
			number /= 100;
		}
		else {
			soFar = numNames[number % 10];
			number /= 10;

			soFar = tensNames[number % 10] + soFar;
			number /= 10;
		}
		if (number == 0) return soFar;
		return numNames[number] + " Hundred" + soFar;
	}

	public static String convertCurrencyFromDecimalFormattedString(String sCurrencyAmount) throws Exception{
		long lDollarValue = 0L;
		long lCentsValue = 0L;
		
		String sDollarValue = "";
		String sCentsValue = "";
		
		//Here we'll strip off any minus signs - we're assuming that even if the number is negative, we want the 'written' amount
		//to look the same for positive or negative numbers:
		sCurrencyAmount = sCurrencyAmount.replace("-", "");

		//If we are passed in only the 'cents':
		if(sCurrencyAmount.substring(0, 1).compareToIgnoreCase(".") == 0){
			sCurrencyAmount = "0" + sCurrencyAmount;
		}
		
		try {
			@SuppressWarnings("unused")
			BigDecimal bdTest = new BigDecimal(sCurrencyAmount.replaceAll(",", ""));
		} catch (Exception e1) {
			throw new Exception("Parsing error [1503343820] converting decimal string '" + sCurrencyAmount + "' into written English - " + e1.getMessage());
		}

		try {
			if (sCurrencyAmount.indexOf(".") > 0){
				lDollarValue = Long.parseLong(sCurrencyAmount.substring(0, sCurrencyAmount.indexOf(".")).replaceAll(",", ""));
				lCentsValue = Long.parseLong(sCurrencyAmount.substring(sCurrencyAmount.indexOf(".") + 1, sCurrencyAmount.length()).replaceAll(",", ""));
			}else{
				lDollarValue = Long.parseLong(sCurrencyAmount.replaceAll(",", ""));
			}
			
			sDollarValue = convertLongToWrittenEnglish(lDollarValue) + " Dollars";
			//sCentsValue = " And " + convertLongToWrittenEnglish(lCentsValue) + " Cents";
			sCentsValue = " And " + clsStringFunctions.PadLeft(Long.toString(lCentsValue), "0", 2)  + "/100";
			
		} catch (Exception e) {
			throw new Exception("Error [1503343819] converting decimal string '" + sCurrencyAmount + "' into written English - " + e.getMessage());
		}
		return sDollarValue + sCentsValue;
	}

	public static String convertLongToWrittenEnglish(long number) {
		// 0 to 999 999 999 999
		if (number == 0) { return "Zero"; }

		String snumber = Long.toString(number);

		// pad with "0"
		String mask = "000000000000";
		DecimalFormat df = new DecimalFormat(mask);
		snumber = df.format(number);

		// XXX,nnn,nnn,nnn
		int billions = Integer.parseInt(snumber.substring(0,3));
		// nnn,XXX,nnn,nnn
		int millions  = Integer.parseInt(snumber.substring(3,6));
		// nnn,nnn,XXX,nnn
		int hundredThousands = Integer.parseInt(snumber.substring(6,9));
		// nnnnnnnnnXXX
		int thousands = Integer.parseInt(snumber.substring(9,12));
		//int thousands = Integer.parseInt(snumber.substring(6,9));
		
		String tradBillions;
		switch (billions) {
		case 0:
			tradBillions = "";
			break;
		case 1 :
			tradBillions = convertLessThanOneThousand(billions)
			+ " Billion ";
			break;
		default :
			tradBillions = convertLessThanOneThousand(billions)
			+ " Billion ";
		}
		String result =  tradBillions;

		String tradMillions;
		switch (millions) {
		case 0:
			tradMillions = "";
			break;
		case 1 :
			tradMillions = convertLessThanOneThousand(millions)
			+ " Million ";
			break;
		default :
			tradMillions = convertLessThanOneThousand(millions)
			+ " Million ";
		}
		result =  result + tradMillions;

		String tradHundredThousands;
		switch (hundredThousands) {
		case 0:
			tradHundredThousands = "";
			break;
		case 1 :
			tradHundredThousands = "One Thousand ";
			break;
		default :
			tradHundredThousands = convertLessThanOneThousand(hundredThousands)
			+ " Thousand ";
		}
		result =  result + tradHundredThousands;
		String tradThousand;
		tradThousand = convertLessThanOneThousand(thousands);
		result =  result + tradThousand;

		// remove extra spaces!
		return result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
	}

}