package ServletUtilities;

public class clsStringFunctions {

	public static String FormatSQLResult(String s){
	
		//System.out.println("s before: " + s);
		if (s != null){
			s = s.replace("\"", "&quot;");
		}
		//System.out.println("s after: " + s);
		return s;
	}

	public static String filter(String input) {
	
		// Note that Javadoc is not used for the more detailed
		// documentation due to the difficulty of making the
		// special chars readable in both plain text and HTML.
		//
		// Given a string, this method replaces all occurrences of
		//  '<' with '&lt;', all occurrences of '>' with
		//  '&gt;', and (to handle cases that occur inside attribute
		//  values), all occurrences of double quotes with
		//  '&quot;' and all occurrences of '&' with '&amp;'.
		//  Without such filtering, an arbitrary string
		//  could not safely be inserted in a Web page.
	
		if (!clsStringFunctions.hasSpecialChars(input)) {
			return(input);
		}
		StringBuffer filtered = new StringBuffer(input.length());
		char c;
		for(int i=0; i<input.length(); i++) {
			c = input.charAt(i);
			switch(c) {
			case '<': filtered.append("&lt;"); break;
			case '>': filtered.append("&gt;"); break;
			case '"': filtered.append("&quot;"); break;
			case '&': filtered.append("&amp;"); break;
			case ' ': filtered.append("&nbsp;"); break;
			default: filtered.append(c);
			}
		}
		return(filtered.toString());
	}

	public static boolean hasSpecialChars(String input) {
		boolean flag = false;
		if ((input != null) && (input.length() > 0)) {
			char c;
			for(int i=0; i<input.length(); i++) {
				c = input.charAt(i);
				switch(c) {
				case '<': flag = true; break;
				case '>': flag = true; break;
				case '"': flag = true; break;
				case '&': flag = true; break;
				}
			}
		}
		return(flag);
	}

	public static boolean validateStringCharacters(String sTarget, String sAllowedCharacters){
	
		for(int i=0; i<sTarget.length();i++){
			if(!sAllowedCharacters.contains(sTarget.subSequence(i, i+1))){
				return false;
			}
		}
	
		return true;
	}

	public static String PadLeft(String sStr, String sPadChar, int iTotalStringLength){
		if (sStr.length()> iTotalStringLength){
			return clsStringFunctions.StringLeft(sStr,iTotalStringLength);
		}
	
		String sResult = "";
		//System.out.println("SPADCHAR = " + sPadChar);
		for (int i = 0; i < iTotalStringLength - sStr.length(); i++){
			sResult += sPadChar;
			//System.out.println("sResult = " + sResult);
		}
		sResult += sStr;
	
		return sResult;
	}

	public static String PadRight (String sStr, String sPadChar, int iTotalStringLength){
		if (sStr.length()> iTotalStringLength){
			return clsStringFunctions.StringLeft(sStr,iTotalStringLength);
		}
	
		sStr = sStr.trim();
		int iLength = sStr.length();
		for (int i = 0; i < iTotalStringLength - iLength; i++){
			sStr += sPadChar;
		}
	
		return sStr;
	}

	//Returns the left 'iLength' characters of a string:
	public static String StringLeft(String sSource, int iLength){
	
		if (iLength < 0){
			return sSource;
		}
	
		if (sSource.length() > iLength){
			return sSource.substring(0, iLength);
		}
		else{
			return sSource;
		}
	}

	//Returns the right 'iLength' characters of a string:
	public static String StringRight(String sSource, int iLength){
	
		if (iLength < 0){
			return sSource;
		}
	
		if (sSource.length() > iLength){
			//return sSource.substring(0, iLength);
			return sSource.substring(sSource.length() - iLength, sSource.length());
		}
		else{
			return sSource;
		}
	}

	public static String convertStringToRegex(String s) {  
	    StringBuilder b = new StringBuilder();  
	    for(int i=0; i<s.length(); ++i) {  
	        char ch = s.charAt(i);  
	        if ("\\.^$|?*+[]{}()".indexOf(ch) != -1)  
	            b.append('\\').append(ch);  
	        else if (Character.isLetter(ch))  
	            b.append("[A-Za-z]");  
	        else if (Character.isDigit(ch))  
	            b.append("\\d");  
	        else  
	            b.append(ch);  
	    }  
	    return b.toString();  
	}

	public static String removeDoubleQuotesInDelimitedLine(String sLine){
		String s = "";
		boolean bInDoubledQuotedString = false;
		if (sLine == null){
			return null;
		}
		for (int i = 0;i<sLine.length();i++){
			//If we hit a double quote, then turn on or off the flag that says we are inside a pair of double quotes:
			if (sLine.substring(i, i+1).compareToIgnoreCase("\"") == 0){
				bInDoubledQuotedString = !bInDoubledQuotedString;
			//If it's not a double quote, then drop any commas IF we are inside double quotes:
			}else{
				if (bInDoubledQuotedString && (sLine.substring(i, i+1).compareToIgnoreCase(",") == 0)){
				}else{
					s += sLine.substring(i, i+1);
				}
			}
		}
		return s;
	}

	public static String checkStringForNull(String sTest){
		if (sTest == null){
			return "";
		}else{
			return sTest;
		}
	}
	
	public static String filterZeroStringToEmptyString(String input) {
		
		if (input.trim().compareToIgnoreCase("0") != 0) {
			return(input);
		}
			return("");
	}
	
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    } catch(NullPointerException e) {
	        return false;
	    }
	    return true;
	}

}
