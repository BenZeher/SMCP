package ServletUtilities;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class clsManageBigDecimals {

	public static String BigDecimalTo2DecimalSTDFormat(BigDecimal bd){
		DecimalFormat TwoDecimal = new DecimalFormat("###,###,##0.00");
		if (bd == null){
			return "0.00";
		}else{
			return TwoDecimal.format(bd);
		}  
	}

	public static String BigDecimalTo2DecimalSQLFormat(BigDecimal bd){
		DecimalFormat TwoDecimal = new DecimalFormat("########0.00");
		if (bd == null){
			return "0.00";
		}else{
			return TwoDecimal.format(bd);
		}  
	}

	public static String BigDecimalToFormattedString(String sFormat, BigDecimal bd){
		DecimalFormat TwoDecimal = new DecimalFormat(sFormat);
		if (bd == null){
			BigDecimal bdZero = new BigDecimal(0);
			return TwoDecimal.format(bdZero);
		}else{
			return TwoDecimal.format(bd);
		}  
	}

	public static String BigDecimalToScaledFormattedString(
			int iScale, 
			BigDecimal bd
	){
		String sFormat = "###,###,##0." + clsStringFunctions.PadLeft("", "0", iScale);
		DecimalFormat scaledFormat = new DecimalFormat(sFormat);
		BigDecimal bdZero = new BigDecimal(0);
		try {
			if (bd == null){
				return scaledFormat.format(bdZero);
			}else{
				return scaledFormat.format(bd);
			}
		} catch (Exception e) {
			return scaledFormat.format(bdZero);
		}  
	}

	public static String doubleToDecimalFormat(Double d, int iScale){
		String sFormat = "###,###,##0";
		if (iScale > 0){
			sFormat = "###,###,##0." + clsStringFunctions.PadLeft("", "0", iScale);
		}
		DecimalFormat TwoDecimal = new DecimalFormat(sFormat);
		return TwoDecimal.format(d);
	}

	public static String doubleTo2DecimalSTDFormat(Double d){
		DecimalFormat TwoDecimal = new DecimalFormat("###,###,##0.00");
		return TwoDecimal.format(d);
	}
	


}
