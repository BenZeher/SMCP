package smar;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import smcontrolpanel.SMUtilities;

/** Utility class for MADG Intranet.*/

public class ARUtilities extends ServletUtilities.clsServletUtilities{
  public static final String DOCTYPE =
    "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
    "Transitional//EN\">";

  /** Read a parameter with the specified name, convert it
   *  to an int, and return it. Return the designated default
   *  value if the parameter doesn't exist or if it is an
   *  illegal integer format.
  */
  
  //This function creates a row in a table for editing a text field on a form:
  public static String Create_Edit_Form_Text_Input_Row (
		  String sFieldName,
		  String sValue,
		  int iFieldLength,
		  String sLabel,
		  String sRemark
		  ){
			
	        String sRow = "<TR>\n";
	        sRow += "<TD ALIGN=RIGHT><B>" + sLabel  + " </B></TD>\n";
	        
	        sRow += "<TD ALIGN=LEFT>";
	        sRow += "<INPUT TYPE=TEXT NAME=\"" + sFieldName + "\"";
	        if (sValue != null){
	        	sRow += " VALUE=\"" + sValue + "\"";
	        }
	        else{
	        	sRow += " VALUE=\"\"";
	        }
	        sRow += "SIZE=28";
	        sRow += " MAXLENGTH=" + Integer.toString(iFieldLength);
	        sRow += " STYLE=\"width: 2.41in; height: 0.25in\"";
	        sRow += "></TD>\n";
	  		
	        sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>\n";
	        sRow += "</TR>\n\n";
	  		return sRow;
	  		
		  }
  
  public static String Create_Edit_Form_Text_Input_Field (
		  String sFieldName,
		  String sValue,
		  int iFieldLength,
		  String sLabel,
		  String sRemark
		  ){
			
	        String sField = sLabel;
	        sField += " <INPUT TYPE=TEXT NAME=\"" + sFieldName + "\"";
	        if (sValue != null){
	        	sField += " VALUE=\"" + sValue + "\"";
	        }
	        else{
	        	sField += " VALUE=\"\"";
	        }
	        //sField += "SIZE=28";
	        sField += " MAXLENGTH=" + Integer.toString(iFieldLength);
	        sField += ">";
	        //sField += " STYLE=\"width: 2.41in; height: 0.25in\"";
	  		return sField;
	  		
		  }
  public static String Create_Edit_Form_Text_Input_Field (
		  String sFieldName,
		  String sValue,
		  int iMaxFieldLength,
		  String sLabel,
		  String sRemark,
		  int iFieldSize
		  ){
			
	        String sField = sLabel;
	        sField += " <INPUT TYPE=TEXT NAME=\"" + sFieldName + "\"";
	        if (sValue != null){
	        	sField += " VALUE=\"" + sValue + "\"";
	        }
	        else{
	        	sField += " VALUE=\"\"";
	        }
	        sField += "SIZE=" + Integer.toString(iFieldSize);
	        sField += "; MAXLENGTH=" + Integer.toString(iMaxFieldLength);
	        sField += ">";
	        //sField += " STYLE=\"width: 2.41in; height: 0.25in\"";
	  		return sField;
	  		
		  }
  
  //This function creates a row in a table for editing a text field on a form:
  public static String Create_Edit_Form_Text_Input_Row (
		  String sFieldName,
		  String sValue,
		  int iFieldLength,
		  String sLabel,
		  String sRemark,
		  String sTextBoxWidth
		  ){
			
	        String sRow = "<TR>\n";
	        sRow += "<TD ALIGN=RIGHT><B>" + sLabel  + " </B></TD>\n";
	        
	        sRow += "<TD ALIGN=LEFT>";
	        sRow += "<INPUT TYPE=TEXT NAME=\"" + sFieldName + "\"";
	        if (sValue != null){
	        	sRow += " VALUE=\"" + sValue + "\"";
	        }
	        else{
	        	sRow += " VALUE=\"\"";
	        }
	        sRow += "SIZE=28";
	        sRow += " MAXLENGTH=" + Integer.toString(iFieldLength);
	        sRow += " STYLE=\"width: " + sTextBoxWidth + " in; height: 0.25in\"";
	        sRow += "></TD>\n";
	  		
	        sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>\n";
	        sRow += "</TR>\n\n";
	  		return sRow;
	  		
		  }
  
  //This function creates a row in a table for editing a text field on a form:
  public static String Create_Edit_Form_DateText_Input_Row (
		  String sFieldName,
		  String sValue,
		  int iFieldLength,
		  String sLabel,
		  String sRemark,
		  String sTextBoxWidth,
		  ServletContext context
		  ){
			
	        String sRow = "<TR>\n";
	        sRow += "<TD ALIGN=RIGHT><B>" + sLabel  + " </B></TD>\n";
	        
	        sRow += "<TD ALIGN=LEFT>";
	        sRow += "<INPUT TYPE=TEXT NAME=\"" + sFieldName + "\"";
	        if (sValue != null){
	        	sRow += " VALUE=\"" + sValue + "\"";
	        }
	        else{
	        	sRow += " VALUE=\"\"";
	        }
	        sRow += "SIZE=28";
	        sRow += " MAXLENGTH=" + Integer.toString(iFieldLength);
	        sRow += " STYLE=\"width: " + sTextBoxWidth + " in; height: 0.25in\"";
	        sRow += ">";
	        sRow += SMUtilities.getDatePickerString(sFieldName, context);
	        sRow += "</TD>\n";
	  		
	        sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>\n";
	        sRow += "</TR>\n\n";
	  		return sRow;
	  		
		  }
  
  //This function creates a row in a table for editing a date field on a form:
  public static String Create_Edit_Form_Date_Input_Row (
		  String sYearFieldName,
		  String sMonthFieldName,
		  String sDayFieldName,
		  Calendar datDefaultDate,
		  String sLabel,
		  String sRemark
		  ){

	  String sRow = "<TR>\n";
	  sRow += "<TD ALIGN=RIGHT><B>" + sLabel + " </B></TD>\n";
	  sRow += "<TD ALIGN=LEFT>";
	  
	  //Month
	  sRow += " Month: ";
	  sRow += "<SELECT NAME = \"" + sMonthFieldName + "\">";
	  for (int i = 1; i <= 12; i++){
    	  sRow += "<OPTION";
    	  //'Calendar.MONTH is zero-based:
    	  if (i == datDefaultDate.get(Calendar.MONTH) + 1){
    		  sRow += " selected=yes";
    	  }
    	  sRow += " VALUE=\"" + Integer.toString(i) + "\">" + Integer.toString(i);
      }
	  sRow += "</SELECT>";
	  
	  //Day
	  sRow += " Day: ";
	  sRow += "<SELECT NAME = \"" + sDayFieldName + "\">";
	  for (int i = 1; i <= 31; i++){
    	  sRow += "<OPTION";
    	  if (i == datDefaultDate.get(Calendar.DAY_OF_MONTH)){
    		  sRow += " selected=yes";
    	  }
    	  sRow += " VALUE=\"" + Integer.toString(i) + "\">" + Integer.toString(i);
      }
	  sRow += "</SELECT>";
	  
	  //Year
	  sRow += " Year: ";
	  sRow += "<SELECT NAME = \"" + sYearFieldName + "\">";
	  for (int i = 1990; i <= 2015; i++){
    	  sRow += "<OPTION";
    	  if (i == datDefaultDate.get(Calendar.YEAR)){
    		  sRow += " selected=yes";
    	  }
    	  sRow += " VALUE=\"" + Integer.toString(i) + "\">" + Integer.toString(i);
      }
	  sRow += "</SELECT>";
	  
	  sRow += "</TD>\n";
	  
	  sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>\n";
	  sRow += "</TR>\n\n";
	  return sRow;
  }

  //This function creates a field for editing a date field on a form:
  public static String Create_Edit_Form_Date_Input_Field (
		  String sYearFieldName,
		  String sMonthFieldName,
		  String sDayFieldName,
		  Calendar datDefaultDate,
		  String sLabel,
		  String sRemark
		  ){

	  String sField = sLabel; 
	  sField += "<SELECT NAME = \"" + sMonthFieldName + "\">";
	  for (int i = 1; i <= 12; i++){
    	  sField += "<OPTION";
    	  //'Calendar.MONTH is zero-based:
    	  if (i == datDefaultDate.get(Calendar.MONTH) + 1){
    		  sField += " SELECTED ";
    	  }
    	  sField += " VALUE=\"" + Integer.toString(i) + "\">" + Integer.toString(i);
      }
	  sField += "</SELECT>";
	  
	  //Day
	  sField += "/";
	  sField += "<SELECT NAME = \"" + sDayFieldName + "\">";
	  for (int i = 1; i <= 31; i++){
    	  sField += "<OPTION";
    	  if (i == datDefaultDate.get(Calendar.DAY_OF_MONTH)){
    		  sField += " SELECTED ";
    	  }
    	  sField += " VALUE=\"" + Integer.toString(i) + "\">" + Integer.toString(i);
      }
	  sField += "</SELECT>";
	  
	  //Year
	  sField += "/";
	  sField += "<SELECT NAME = \"" + sYearFieldName + "\">";
	  for (int i = 1990; i <= 2015; i++){
    	  sField += "<OPTION";
    	  if (i == datDefaultDate.get(Calendar.YEAR)){
    		  sField += " SELECTED ";
    	  }
    	  sField += " VALUE=\"" + Integer.toString(i) + "\">" + Integer.toString(i);
      }
	  sField += "</SELECT>";
	  
	  sField += sRemark;
	  
	  return sField;
  }

  
  public static String Create_Edit_Form_Checkbox_Row (
		  String sFieldName,
		  int iValue,
		  String sLabel,
		  String sRemark
		  ){

	  String sRow = "<TR>\n";
	  sRow += "<TD ALIGN=RIGHT><B>" + sLabel + " </B></TD>\n";
    
	  sRow += "<TD ALIGN=LEFT> <INPUT TYPE=CHECKBOX";
	  if (iValue == 1){
		  sRow += " CHECKED ";
	  }
	  sRow += " NAME=\"" + sFieldName + "\" width=0.25></TD>\n";
    
	  sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>\n";
	  sRow += "</TR>\n\n";
	  return sRow;
  }
  public static String Create_Edit_Form_Checkbox_Row (
		  String sFieldName,
		  String sValueAsTrueOrFalse,
		  String sLabel,
		  String sRemark
		  ){

	  String sRow = "<TR>\n";
	  sRow += "<TD ALIGN=RIGHT><B>" + sLabel + " </B></TD>\n";
    
	  sRow += "<TD ALIGN=LEFT> <INPUT TYPE=CHECKBOX";
	  if (sValueAsTrueOrFalse.compareToIgnoreCase("true") == 0){
		  sRow += " CHECKED ";
	  }
	  sRow += " NAME=\"" + sFieldName + "\" width=0.25></TD>\n";
    
	  sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>\n";
	  sRow += "</TR>\n\n";
	  return sRow;
  }

  public static String Create_Edit_Form_MultilineText_Input_Row (
		  String sFieldName,
		  String sValue,
		  String sLabel,
		  String sRemark
		  ){
	
	        String sRow = "<TR>\n";
	        sRow += "<TD ALIGN=RIGHT><B>" + sLabel  + " </B></TD>\n";
	        
	        sRow += "<TD ALIGN=LEFT>";
	        sRow += "<TEXTAREA NAME=\"" + sFieldName + "\"";
//	        sRow += " STYLE=\"width: 2.41in; height: 0.25in\"";
	        
	        if (sValue != null){
	        	sRow += ">" + sValue + "</TEXTAREA>";
	        }
	        else{
	        	sRow += "></TEXTAREA>";
	        }
	        sRow += "</TD>\n";
	  		
	        sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>\n";
	        sRow += "</TR>\n\n";
	  		return sRow;
	  		
		  }

  public static String Create_Edit_Form_MultilineText_Input_Row (
		  String sFieldName,
		  String sValue,
		  String sLabel,
		  String sRemark,
		  int iRows,
		  int iCols
		  ){
	
	        String sRow = "<TR>\n";
	        sRow += "<TD ALIGN=RIGHT><B>" + sLabel  + " </B></TD>\n";
	        
	        sRow += "<TD ALIGN=LEFT>";
	        sRow += "<TEXTAREA NAME=\"" + sFieldName + "\"";
	        sRow += " rows=\"" + Integer.toString(iRows) + "\"";
	        sRow += " cols=\"" + Integer.toString(iCols) + "\"";
	        if (sValue != null){
	        	sRow += ">" + sValue + "</TEXTAREA>";
	        }
	        else{
	        	sRow += "></TEXTAREA>";
	        }
	        sRow += "</TD>\n";
	  		
	        sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>\n";
	        sRow += "</TR>\n\n";
	  		return sRow;
	  		
		  }
  
  public static String Create_Edit_Form_MultilineText_Input_Field (
		  String sFieldName,
		  String sValue,
		  String sLabel,
		  int iRows,
		  int iCols
		  ){
			
	        String sField = sLabel;
	        sField += " <TEXTAREA NAME=\"" + sFieldName + "\"";
	        sField += " rows=\"" + Integer.toString(iRows) + "\"";
	        sField += " cols=\"" + Integer.toString(iCols) + "\"";
	        if (sValue != null){
	        	sField += ">" + sValue + "</TEXTAREA>";
	        }
	        else{
	        	sField += "></TEXTAREA>";
	        }
	  		return sField;
	  		
		  }

  // sFieldName: the name of the field we are reading.
  // sValues: an Arraylist of the actual values that are going to be read back 
  //	from the SELECT list.
  // sDefaultValue: the default (string) value from the list which we want selected
  //	by default.
  // sDescriptions: an Arraylist, with indices synched with sValues, of the
  //	descriptions we want listed in the SELECT list.
  // sLabel: the label we assign to the field, which appears in the first column
  //	to the left of the field.
  // sRemark: an explanatory remark that appears in the rightmost column, usually
  //	for more detailed instructions about what can appear in the field.
  //This function creates a row in a table for editing a text field on a form:
  
  public static String Create_Edit_Form_List_Row (
		  String sFieldName,
		  ArrayList<String> sValues,
		  String sDefaultValue,
		  ArrayList<String> sDescriptions,
		  String sLabel,
		  String sRemark
		  ){

	  String sRow = "<TR>\n";
	  sRow += "<TD ALIGN=RIGHT><B>" + sLabel + " </B></TD>\n";
	  sRow += "<TD ALIGN=LEFT> <SELECT NAME = \"" + sFieldName + "\">";
	  for (int i = 0; i < sValues.size(); i++){
    	  sRow += "<OPTION";
    	  if (sValues.get(i).toString().compareTo(sDefaultValue) == 0){
    		  sRow += " selected=yes";
    	  }
    	  sRow += " VALUE=\"" + sValues.get(i).toString() + "\">" + sDescriptions.get(i).toString();
      }
	  sRow += "</SELECT></TD>\n";
	  
	  sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>\n";
	  sRow += "</TR>\n\n";
	  return sRow;
  }

  public static String Create_Edit_Form_List_Field (
		  String sFieldName,
		  ArrayList<String> sValues,
		  String sDefaultValue,
		  ArrayList<String> sDescriptions
		  ){

	  String sField = "<SELECT NAME = \"" + sFieldName + "\">";
	  for (int i = 0; i < sValues.size(); i++){
    	  sField += "<OPTION";
    	  if (sValues.get(i).toString().compareTo(sDefaultValue) == 0){
    		  sField += " selected=yes";
    	  }
    	  sField += " VALUE=\"" + sValues.get(i).toString() + "\">" + sDescriptions.get(i).toString();
      }
	  sField += "</SELECT>";
	  
	  return sField;
  }
  
  public static String Create_Edit_Form_RadioButton_Row (
		  String sFieldName,
		  ArrayList<String> sValues,
		  String sDefaultValue,
		  ArrayList<String> sDescriptions,
		  String sLabel,
		  String sRemark
		  ){

	  String sRow = "<TR>\n";
	  sRow += "<TD ALIGN=RIGHT><B>" + sLabel + " </B></TD>\n";
	  
	  sRow += "<TD ALIGN=LEFT>"; 
	  for (int i = 0; i < sValues.size(); i++){
		  sRow += "<LABEL><INPUT TYPE=RADIO NAME = \"" + sFieldName + "\"";
		  sRow += " VALUE=\"" + sValues.get(i).toString() + "\"";
    	  if (sValues.get(i).toString().compareTo(sDefaultValue) == 0){
    		  sRow += " CHECKED ";
    	  }
    	  sRow += ">" + sDescriptions.get(i).toString() + "</LABEL>\n<BR>";
      }
	  sRow += "</SELECT></TD>\n";
	  
	  sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>\n";
	  sRow += "</TR>\n\n";
	  return sRow;
  }
  
  
  //Returns the left 'iLength' characters of a string:
  public static String StringLeft(String sSource, int iLength){
	
	  if (sSource.length() > iLength){
		  return sSource.substring(0, iLength);
	  }
	  else{
		  return sSource;
	  }
  }
  
  //Returns the right 'iLength' characters of a string:
  public static String StringRight(String sSource, int iLength){
	
	  if (sSource.length() > iLength){
		  //return sSource.substring(0, iLength);
		  return sSource.substring(sSource.length() - iLength, sSource.length());
	  }
	  else{
		  return sSource;
	  }
  };
  
  public static String PadLeft(String sStr, String sPadChar, int iTotalStringLength){
	  if (sStr.length()> iTotalStringLength){
		  return StringLeft(sStr,iTotalStringLength);
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
  
  //Data transactions:
  public static boolean start_data_transaction (
  		ServletContext context, 
  		String sDBID
  		){
	
	    String SQL = "START TRANSACTION";
		try{
	    	if (clsDatabaseFunctions.executeSQL(SQL, context, sDBID) == false){
	    		return false;
	    	}
	    }catch (SQLException ex){
			System.out.println("Error in ARUtilities - start transaction!!");
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("SQL: " + ex.getErrorCode());
		    return false;
		}
  	return true;
  }

  public static boolean rollback_data_transaction (
	  		ServletContext context, 
	  		String sDBID
	  		){
		
		    String SQL = "ROLLBACK";
			try{
		    	if (clsDatabaseFunctions.executeSQL(SQL, context, sDBID) == false){
		    		return false;
		    	}
		    }catch (SQLException ex){
				System.out.println("Error in ARUtilities - rollback transaction!!");
			    System.out.println("SQLException: " + ex.getMessage());
			    System.out.println("SQLState: " + ex.getSQLState());
			    System.out.println("SQL: " + ex.getErrorCode());
			    return false;
			}
	  	return true;
	  }
  
  public static boolean commit_data_transaction (
	  		ServletContext context, 
	  		String sDBID
	  		){
		
		    String SQL = "COMMIT";
			try{
		    	if (clsDatabaseFunctions.executeSQL(SQL, context, sDBID) == false){
		    		return false;
		    	}
		    }catch (SQLException ex){
				System.out.println("Error in ARUtilities - rollback transaction!!");
			    System.out.println("SQLException: " + ex.getMessage());
			    System.out.println("SQLState: " + ex.getSQLState());
			    System.out.println("SQL: " + ex.getErrorCode());
			    return false;
			}
	  	return true;
	  }
  
  public static String Fill_In_Empty_String_For_HTML_Cell(String s){
	  if (s.length() ==0){
		  return "&nbsp;";
	  }else{
		  return s;
	  }
  }
  
  public static String get_Request_Parameter(String sParameterName, HttpServletRequest req){
	  if (req.getParameter(sParameterName) == null){
		  return "";
	  }else{
		  return req.getParameter(sParameterName);
	  }
  }
  
	public static void forwardToURL(
	  String sURL,
		HttpServletRequest request, 
		HttpServletResponse response,
		ServletContext context) throws ServletException, IOException{
	
		RequestDispatcher rd = context.getRequestDispatcher(sURL);
		rd.forward(request, response);
	}
	public static boolean confirm_order_for_customer (
			String sOrderNumber, 
			String sCustomerNumber,
			ServletContext context,
			String sDBID
			){
		
		String SQL = SMClasses.MySQLs.Get_Orders_For_Client(sCustomerNumber);
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
					sDBID,
					"MySQL",
					"ARUtilities.confirm_order_for_customer"); 
			if (rs.next()){
				rs.close();
				return true;
			}else {
				rs.close();
				return false;
			}
		}catch (SQLException ex){
	    	System.out.println("Error in SMOrderUtilities.confirm_order_for_customer class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
	        return false;
		}	
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
	public static boolean IsValidInteger(String sInt){
		sInt = sInt.replace(",", "");
    	try{
    		Integer.parseInt(sInt);
    		return true;
    	}catch (NumberFormatException e){
    		System.out.println("In IsValidInteger: Error converting number from string: " + sInt + ".");
    		System.out.println("In IsValidInteger: " + e.getMessage());
    		return false;
    	}
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
	public static int convertBooleanToInt(Boolean bValue){
		if(bValue){
			return 1;
		}else{
			return 0;
		}
	}
	public static String formatDoubleQuoteForHTML(String s){
		return s.replace("\"", "\\\"");
	}
	public static String checkStringForNull(String s){
		if (s == null){
			return "";
		}else{
			return s;
		}
	}
	public static boolean testResultSetTSFieldForNull(ResultSet rs, String sFieldName){
		
		try{
			@SuppressWarnings("unused")
			Timestamp ts = rs.getTimestamp(sFieldName);
		}catch(SQLException e){
			return false;
		}
		return true;
	}
	public static long DateStringToLong (String sDateString){
		
		try{
			return Long.parseLong(sDateString.replace("-", ""));
		}catch (NumberFormatException e) {
			return 0;
		}
	}
	public static java.sql.Date StringToSQLDateStrict (String sFormat, String s){
		SimpleDateFormat sdf = new SimpleDateFormat(sFormat);

//		strFormat is the required format of the date.

		sdf.setLenient(false); // This is very important

//		Parse the date entered by the user to check the
//		format.
		try{

//		 get the valid value into the Date class object.
		java.util.Date myDate = sdf.parse(s);
		return clsDateAndTimeConversions.UtilDateToSQLDate(myDate);

		}catch(ParseException pse){
//		Handle Your invalid date format exception here.
			return null;
		}

	}
	public static boolean isValidDateStr(String date, String format) {
	    try {
	      SimpleDateFormat sdf = new SimpleDateFormat(format);
	      sdf.setLenient(false);
	      //System.out.println(sdf.get2DigitYearStart());
	      sdf.parse(date);
	    }
	    catch (ParseException e) {
	      return false;
	    }
	    catch (IllegalArgumentException e) {
	      return false;
	    }
	    return true;
	}
    public static BigDecimal bdStringToBigDecimal (String sValue, int iScale){
    	sValue = sValue.replace(",", "");
    	try{
    		BigDecimal bd = new BigDecimal(sValue);
    		bd = bd.setScale(iScale, BigDecimal.ROUND_HALF_UP);
    		return  bd;
    	}catch (NumberFormatException e){
    		System.out.println("ARUtilities.bdStringToDecimal - Error converting Original amount from string: " + sValue + ".");
    		System.out.println("ARUtilities.bdStringToDecimal - " + e.getMessage());
    		return null;
    	}
    }

}

