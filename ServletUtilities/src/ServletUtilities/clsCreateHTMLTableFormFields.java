package ServletUtilities;

import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.ServletContext;

public class clsCreateHTMLTableFormFields {

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
		sRow += "</TR>\n";
		return sRow;
	
	}
	
	
	public static String Create_Edit_Form_Text_Input_Row_with_JavaScript (
			String sFieldName,
			String sValue,
			int iFieldLength,
			String sLabel,
			String sRemark,
			String Javascript
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
		sRow += "</TR>\n";
		return sRow;
	
	}
	
	public static String Create_File_Input (
			String sFieldName,
			String sLabel
	){
	
		String sRow = "<TR>\n";
		sRow += "<TD ALIGN=RIGHT><B>" + sLabel  + " </B></TD>\n";
		sRow += "<TD ALIGN=LEFT>";
		sRow += "<INPUT TYPE= FILE NAME=\"" + sFieldName + "\">";
		sRow += "</TD>\n";
		sRow += "</TR>\n";
		return sRow;
	
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
		sRow += "SIZE=" + sTextBoxWidth;
		sRow += " MAXLENGTH=" + Integer.toString(iFieldLength);
		//sRow += " STYLE=\"width: " + sTextBoxWidth + " in; height: 0.25in\"";
		sRow += "></TD>\n";
	
		sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>\n";
		sRow += "</TR>\n\n";
		return sRow;
	
	}

	
	public static String Create_Edit_Form_Disabled_Text_Input_Row (
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
		sRow += "SIZE=" + sTextBoxWidth;
		sRow += " MAXLENGTH=" + Integer.toString(iFieldLength);
		//sRow += " STYLE=\"width: " + sTextBoxWidth + " in; height: 0.25in\"";
		sRow += " disabled = true></TD>\n";
	
		sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>\n";
		sRow += "</TR>\n\n";
		return sRow;
	
	}

	public static String Create_Edit_Form_Text_Input_Row (
			String sFieldName,
			String sValue,
			int iFieldLength,
			String sLabel,
			String sRemark,
			String sTextBoxWidth,
			String sOnChange
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
		sRow += "SIZE=" + sTextBoxWidth;
		sRow += " MAXLENGTH=" + Integer.toString(iFieldLength);
		sRow += " ONCHANGE=\"" + sOnChange + "\"";
		//sRow += " STYLE=\"width: " + sTextBoxWidth + " in; height: 0.25in\"";
		sRow += "></TD>\n";
	
		sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>\n";
		sRow += "</TR>\n\n";
		return sRow;
	
	}

	//This function creates a row in a table for editing a date field on a form with a date picker:
	public static String Create_Edit_Form_Date_Input_Row (
			String sFieldName,
			String sValue,
			String sLabel,
			String sRemark,
			ServletContext context
	){
	
		String sRow = "<TR>\n";
		sRow += "<TD ALIGN=RIGHT><B>" + sLabel  + " </B></TD>\n";
	
		sRow += "<TD ALIGN=LEFT>";
		sRow += "<INPUT TYPE=TEXT NAME=\"" + sFieldName + "\"";
		//System.out.println(sLabel + " = " + sValue);
		if (sValue != null){
			sRow += " VALUE=\"" + sValue + "\"";
		}
		else{
			sRow += " VALUE=\"\"";
		}
		sRow += "SIZE=28";
		sRow += " MAXLENGTH=10";
		sRow += " STYLE=\"width: " + ".75" + " in; height: 0.25in\"";
		sRow += ">";
		sRow += clsServletUtilities.getDatePickerString(sFieldName, context);
	
		sRow += "</TD>\n";
	
		sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>\n";
		sRow += "</TR>\n\n";
		return sRow;
	
	}

	//This function creates a set of radio buttons for editing on an HTML form:
	public static String Create_Edit_Form_RadioButton_Input_Row (
			String sFieldName,
			String sLabel,
			String sRemark,
			ArrayList <String> sButtonLabels,
			ArrayList <String> sButtonValues,
			String sDefaultButtonValue
	){
	
		String sRow = "<TR>\n";
		sRow += "<TD ALIGN=RIGHT><B>" + sLabel  + " </B></TD>\n";
	
		sRow += "<TD ALIGN=LEFT>";
	
		sRow += clsCreateHTMLTableFormFields.Create_Edit_Form_RadioButton_Input_Field(
				sFieldName, 
				sButtonLabels, 
				sButtonValues, 
				sDefaultButtonValue);
	
		sRow += "</TD>\n";
	
		sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>\n";
		sRow += "</TR>\n\n";
		return sRow;
	}

	//This function creates a set of radio buttons for editing on an HTML form:
	public static String Create_Edit_Form_RadioButton_Input_Field (
			String sFieldName,
			ArrayList <String> sLabels,
			ArrayList <String> sValues,
			String sDefaultValue
	){
	
		String s = "";
		//NOTE: to separate the options on the screen, add a <BR> or spaces to the labels as needed.
		for (int i = 0; i < sLabels.size(); i ++){
	
			if (sValues.get(i).compareToIgnoreCase(sDefaultValue) == 0){
				//Surround it with a label, so the user can click on either the checkbox OR the label:
				s += "\n" + "<LABEL NAME= \"" + sLabels.get(i) + "LABEL" + "\" >" + "\n";
				s += "<INPUT TYPE=\"RADIO\" NAME=\"" + sFieldName + "\" VALUE=" + sValues.get(i) + " CHECKED >" + sLabels.get(i) + "\n";
				s += "</LABEL>" + "\n";
			}else{
				s += "\n" + "<LABEL NAME= \"" + sLabels.get(i) + "LABEL" + "\" >" + "\n";
				s += "<INPUT TYPE=\"RADIO\" NAME=\"" + sFieldName + "\" VALUE=" + sValues.get(i) + " >" + sLabels.get(i) + "\n";
				s += "</LABEL>" + "\n";
			}
		}
		return s;
	}

	//This function creates a set of radio buttons in separate rows, for editing on an HTML form.  Also, a selected row can be displayed with a different background color:
	public static String Create_Edit_Form_RadioButton_Input_Rows (
			String sFieldName,
			ArrayList <String> sLabels,
			ArrayList <String> sValues,
			ArrayList <String> sConfirmingLabels,
			String sDefaultValue,
			String sHighlightedValue,
			String sHighlightColor,
			String sDefaultBackGroundColor
	){
	
		String s = "";
		s += "\n<TABLE BORDER=1>\n";
		//NOTE: to separate the options on the screen, add a <BR> or spaces to the labels as needed.
		for (int i = 0; i < sLabels.size(); i ++){
	
			String sBackGroundColor = sDefaultBackGroundColor;
			if (sValues.get(i).compareToIgnoreCase(sHighlightedValue) == 0){
				sBackGroundColor = sHighlightColor;
			}
			String sCheckedValue = "";
			if (sValues.get(i).compareToIgnoreCase(sDefaultValue) == 0){
				sCheckedValue = " CHECKED ";
			}
	
			s += "  <TR style = \" background-color:" + sBackGroundColor + "; \" >\n";
			s += "    <TD ALIGN=LEFT >"
				+ "<LABEL>"
				+ "<INPUT TYPE=\"RADIO\" NAME=\"" + sFieldName 
				+ "\" VALUE=" + sValues.get(i) + sCheckedValue + ">"
				+ "&nbsp;" + sLabels.get(i)
				+ "</LABEL>"
				+ "</TD>\n"
			;
				
			s += "    <TD ALIGN=LEFT>"
				+ sConfirmingLabels.get(i)
				+ "</TD>\n"
			;
			
			s += "  </TR>\n\n";
		}
		s += "</TABLE>\n\n";
		return s;
	}

	//This function creates a field in a table for editing a date AND time field on a form with a date picker:
	public static String Create_Edit_Form_DateTime_Input_Field (
			String sDateFieldName,
			String sValue,
			ServletContext context
	){
	
		//The value of sValue should look something like this: "12/1/2010 03:59 PM"
		String sDatePortion = sValue.substring(0, sValue.indexOf(" ")).trim();
		String sTimePortion = sValue.substring(sValue.indexOf(" "), sValue.length()).trim();
	
		//System.out.println("In Create_Edit_Form_DateTime_Input_Field - sTimePortion = " + sTimePortion);
	
		String s = "<INPUT TYPE=TEXT NAME=\"" + sDateFieldName + "\"";
		s += " VALUE=\"" + sDatePortion + "\"";
		s += "SIZE=8";
		s += " MAXLENGTH=10";
		s += " STYLE=\"width: " + ".75" + " in; height: 0.25in\"";
		s += ">";
		s += clsServletUtilities.getDatePickerString(sDateFieldName, context) + "&nbsp;";
	
		int iMinute = Integer.parseInt(
				sTimePortion.substring(sTimePortion.indexOf(":") + 1, sTimePortion.indexOf(":") + 3));
		int iAMPM = 0;
		if (clsStringFunctions.StringRight(sTimePortion, 2).compareToIgnoreCase("AM") == 0){
			iAMPM = 0;
		}else{
			iAMPM = 1;
		}
		String sHour = sTimePortion.substring(0, sTimePortion.indexOf(":"));
		int iHour = Integer.parseInt(sHour);
		//if (iHour == 0 && iAMPM == 1){
		if (iHour == 0){
			iHour = 12;
		}
		s += "<B>Time:</B>&nbsp;<SELECT NAME=\"" + sDateFieldName + "SelectedHour\">";
		for (int i=1; i<=12;i++){
			if (i == iHour){
				s += "<OPTION SELECTED VALUE = " + i + ">" + i+"\n";
			}else{
				s += "<OPTION VALUE = " + i + ">" + i+"\n";
			}
		}
		s += "</SELECT>";
		s += "<B>:</B>&nbsp;<SELECT NAME=\"" + sDateFieldName + "SelectedMinute\">";
		for (int i=0; i<=59;i++){
			String sMinute = clsStringFunctions.PadLeft(Integer.toString(i), "0", 2);
			if (i == iMinute){
				s += "<OPTION SELECTED VALUE = " 
					+ sMinute + ">" + sMinute+"\n";
			}else{
				s += "<OPTION VALUE = " + sMinute + ">" + sMinute+"\n";
			}
		}
		s += "</SELECT>";	
		s += "&nbsp;<SELECT NAME=\"" + sDateFieldName + "SelectedAMPM\">";
		for (int i=Calendar.AM; i<=Calendar.PM;i++){
			if (i == iAMPM){
				if (i == Calendar.AM){
					s+= "<OPTION SELECTED VALUE = " + Calendar.AM + ">" + "AM"+"\n";
				}else{
					s += "<OPTION SELECTED VALUE = " + Calendar.PM + ">" + "PM"+"\n";
				}		
			}else{
				if (i == Calendar.AM){
					s += "<OPTION VALUE = " + Calendar.AM + ">" + "AM"+"\n";
				}else{
					s += "<OPTION VALUE = " + Calendar.PM + ">" + "PM"+"\n";
				}
			}
		}
	
		s += "</SELECT>";
	
		return s;
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
			sRow += clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}
		sRow += " NAME=\"" + sFieldName + "\" width=0.25></TD>\n";
	
		sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>\n";
		sRow += "</TR>\n\n";
		return sRow;
	}

	public static String Create_Edit_Form_Checkbox_Row (
			String sFieldName,
			int iValue,
			String sLabel,
			String sRemark,
			String sOnChange
	){
	
		String sRow = "<TR>\n";
		sRow += "<TD ALIGN=RIGHT><B>" + sLabel + " </B></TD>\n";
	
		sRow += "<TD ALIGN=LEFT> <INPUT TYPE=CHECKBOX ";
	
		if (iValue == 1){
			sRow += clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}
		sRow += " NAME=\"" + sFieldName + "\" width=0.25"
				+ " ONCHANGE=\"" + sOnChange + "\"></TD>\n";
	
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

	public static String Create_Edit_Form_MultilineText_Input_Row (
			String sFieldName,
			String sValue,
			String sLabel,
			String sRemark,
			int iRows,
			int iCols,
			String onchange
	){
	
		String sRow = "<TR>\n";
		sRow += "<TD ALIGN=RIGHT><B>" + sLabel  + " </B></TD>\n";
	
		sRow += "<TD ALIGN=LEFT>";
		sRow += "<TEXTAREA NAME=\"" + sFieldName + "\"";
		sRow += " rows=\"" + Integer.toString(iRows) + "\"";
		sRow += " cols=\"" + Integer.toString(iCols) + "\"";
		sRow += " onchange=\"" + onchange + "\"";
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
		sRow += "<TD ALIGN=LEFT>&nbsp;<SELECT NAME = \"" + sFieldName + "\">";
		for (int i = 0; i < sValues.size(); i++){
			sRow += "<OPTION";
			if (sValues.get(i).toString().compareTo(sDefaultValue) == 0){
				sRow += " selected=yes";
			}
			sRow += " VALUE=\"" + sValues.get(i).toString() + "\">" + sDescriptions.get(i).toString() + "\n";
		}
		sRow += "</SELECT></TD>\n";
	
		sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>\n";
		sRow += "</TR>\n\n";
		return sRow;
	}
	
	public static String Create_Edit_Form_List_Row_Adjustable_Width(
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
			String sFieldName,
			ArrayList<String> sValues,
			String sDefaultValue,
			ArrayList<String> sDescriptions,
			String sLabel,
			String sRemark,
			String sOnChange,
			int Width
	){
	
		String sRow = "<TR>\n";
		sRow += "<TD ALIGN=RIGHT WIDTH = \""+Width+"\"><B>" + sLabel + " </B></TD>\n";
		sRow += "<TD ALIGN=LEFT>&nbsp;<SELECT NAME = \"" + sFieldName + "\""
				+ " ID = \"" + sFieldName + "\""
				+ " ONCHANGE=\"" + sOnChange + "\">";
		for (int i = 0; i < sValues.size(); i++){
			sRow += "<OPTION";
			if (sValues.get(i).toString().compareTo(sDefaultValue) == 0){
				sRow += " selected=yes";
			}
			sRow += " VALUE=\"" + sValues.get(i).toString() + "\">" + sDescriptions.get(i).toString()+"\n";
		}
		sRow += "</SELECT></TD>\n";
	
		sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>\n";
		sRow += "</TR>\n\n";
		return sRow;
	}


	
	

	public static String Create_Edit_Form_List_Row (
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
			String sFieldName,
			ArrayList<String> sValues,
			String sDefaultValue,
			ArrayList<String> sDescriptions,
			String sLabel,
			String sRemark,
			String sOnChange
	){
		String sRow = "<TR>\n";
		sRow += "<TD ALIGN=RIGHT><B>" + sLabel + " </B></TD>\n";
		sRow += "<TD ALIGN=LEFT>&nbsp;<SELECT NAME = \"" + sFieldName + "\""
				+ " ID = \"" + sFieldName + "\""
				+ " ONCHANGE=\"" + sOnChange + "\">";
		for (int i = 0; i < sValues.size(); i++){
			sRow += "<OPTION";
			if (sValues.get(i).toString().compareTo(sDefaultValue) == 0){
				sRow += " selected=yes";
			}
			sRow += " VALUE=\"" + sValues.get(i).toString() + "\">" + sDescriptions.get(i).toString()+"\n";
		}
		sRow += "</SELECT></TD>\n";
	
		sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>\n";
		sRow += "</TR>\n\n";
		return sRow;
	}
	
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
		        sRow += clsServletUtilities.getDatePickerString(sFieldName, context);
		        sRow += "</TD>\n";
		  		
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
	  

}
