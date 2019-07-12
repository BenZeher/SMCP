package smar;

import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
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

}

