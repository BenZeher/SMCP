package ServletUtilities;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.ServletContext;

public class clsCreateHTMLFormFields {

	public static String TDTextBox(String sName, String sValue, int iSize, int iMaxLength, String sDesc){
	
		String s = "<INPUT TYPE=TEXT NAME=\"" + sName + "\" VALUE=\"" + sValue + "\" SIZE = " + iSize + " MAXLENGTH = " + iMaxLength + ">" + sDesc;
		//System.out.println(s);
		return s;
	}

	public static String TDTextBox(String sName, String sAlign, String sValue, int iSize, int iMaxLength, String sDesc){
	
		String s = "<INPUT TYPE=TEXT STYLE=\"TEXT-ALIGN: " + sAlign + "\" NAME=\"" + sName + "\" VALUE=\"" + sValue + "\" SIZE = " + iSize + " MAXLENGTH = " + iMaxLength + ">" + sDesc;
		//System.out.println(s);
		return s;
	}

	public static String TDDateSelection(String sName, Date datValue, String sDesc){
	
		Calendar c = Calendar.getInstance();
		c.setTime(datValue);
		String s;
		s = "Month <SELECT NAME=\"" + sName + "SelectedMonth\">";
		for (int i=1; i<=12;i++){
			if (i == c.get(Calendar.MONTH) + 1){
				s = s + "<OPTION SELECTED VALUE = " + i + ">" + i;
			}else{
				s = s + "<OPTION VALUE = " + i + ">" + i;
			}
		}
		s = s + "</SELECT>";
	
		s = s + "Day <SELECT NAME=\"" + sName + "SelectedDay\">";
		for (int i=1; i<=31;i++){
			if (i == c.get(Calendar.DAY_OF_MONTH)){
				s = s + "<OPTION SELECTED VALUE = " + i + ">" + i;
			}else{
				s = s + "<OPTION VALUE = " + i + ">" + i;
			}
		}
		s = s + "</SELECT>";	
		s = s + "Year <SELECT NAME=\"" + sName + "SelectedYear\">";
		for (int i=1900; i<=2069;i++){
			if (i == c.get(Calendar.YEAR)){
				s = s + "<OPTION SELECTED VALUE = " + i + ">" + i;
			}else{
				s = s + "<OPTION VALUE = " + i + ">" + i;
			}
		}
		s = s + "</SELECT>" + sDesc;	
		//System.out.println(s);
		return s;
	}

	public static String TDTimeSelection(String sName, Timestamp tsValue, String sDesc){
	
		String sValue = tsValue.toString().substring(11, 19);
		int iMinute = Integer.parseInt(sValue.substring(3, 5));
		int iAMPM = Integer.parseInt(sValue.substring(0, 2)) / 12;
		int iHour = Integer.parseInt(sValue.substring(0, 2)) % 12;
		if (iHour == 0 && iAMPM == 1){
			iHour = 12;
		}
		String s;
		s = "Hour <SELECT NAME=\"" + sName + "SelectedHour\">";
		for (int i=0; i<=12;i++){
			if (i == iHour){
				s = s + "<OPTION SELECTED VALUE = " + i + ">" + i;
			}else{
				s = s + "<OPTION VALUE = " + i + ">" + i;
			}
		}
		s = s + "</SELECT>";
		s = s + "Minute <SELECT NAME=\"" + sName + "SelectedMinute\">";
		for (int i=0; i<=59;i++){
			if (i == iMinute){
				s = s + "<OPTION SELECTED VALUE = " + i + ">" + i;
			}else{
				s = s + "<OPTION VALUE = " + i + ">" + i;
			}
		}
		s = s + "</SELECT>";	
		s = s + "AM/PM <SELECT NAME=\"" + sName + "SelectedAMPM\">";
		for (int i=Calendar.AM; i<=Calendar.PM;i++){
			if (i == iAMPM){
				if (i == Calendar.AM){
					s = s + "<OPTION SELECTED VALUE = " + Calendar.AM + ">" + "AM";
				}else{
					s = s + "<OPTION SELECTED VALUE = " + Calendar.PM + ">" + "PM";
				}		
			}else{
				if (i == Calendar.AM){
					s = s + "<OPTION VALUE = " + Calendar.AM + ">" + "AM";
				}else{
					s = s + "<OPTION VALUE = " + Calendar.PM + ">" + "PM";
				}
			}
		}
	
		s = s + "</SELECT>" + sDesc;	
		//System.out.println(s);
		return s;
	}

	public static String TDDoubleBox(String sName, double dValue, int iSize, int iMaxLength, String sDesc){
	
		String s =  "<INPUT TYPE=TEXT NAME=\"" + sName + "\" VALUE=\"" + dValue + "\" SIZE = " + iSize + " MAXLENGTH = " + iMaxLength + ">" + sDesc;
		//System.out.println(s);
		return s;
	}

	public static String TDYesNo(String sName, int iValue, String sDesc){
	
		String s;
		if (iValue == 1){
			s = "<INPUT TYPE=\"RADIO\" NAME=\"" + sName + "\" VALUE=1 CHECKED>Yes<BR>" + 
			"<INPUT TYPE=\"RADIO\" NAME=\"" + sName + "\" VALUE=0>No<BR>";
		}else{
			s = "<INPUT TYPE=\"RADIO\" NAME=\"" + sName + "\" VALUE=1>Yes<BR>" + 
			"<INPUT TYPE=\"RADIO\" NAME=\"" + sName + "\" VALUE=0 CHECKED>No<BR>";;
		}
	
		//System.out.println(s);
		return s;
	}

	public static String TDCheckBox(String sName, boolean bChecked, String sDesc){
	
		String s = "";
		s += "<LABEL NAME=CHKBOX>";
		if (bChecked){
			s += "<INPUT TYPE=\"CHECKBOX\" NAME=\"" + sName + "\" VALUE=1 CHECKED>" + sDesc + "<BR>";
		}else{
			s += "<INPUT TYPE=\"CHECKBOX\" NAME=\"" + sName + "\" VALUE=1>" + sDesc + "<BR>"; 
		}
		s += "<LABEL NAME=CHKBOX>";
		//System.out.println(s);
		return s;
	}

	public static String TDCheckBoxWithoutReturns(String sName, boolean bChecked, String sDesc){
	
		String s = "";
		s += "<LABEL NAME=CHKBOX>";
		if (bChecked){
			s += "<INPUT TYPE=\"CHECKBOX\" NAME=\"" + sName + "\" VALUE=1 CHECKED>" + sDesc;
		}else{
			s += "<INPUT TYPE=\"CHECKBOX\" NAME=\"" + sName + "\" VALUE=1>" + sDesc; 
		}
		s += "</LABEL>";
		//System.out.println(s);
		return s;
	}

	public static String TDDropDownBox(String sName, ArrayList<String> alValues, ArrayList<String> alTexts){
	
		//check to see if the size of value array and description array is identical.
		if (alValues.size()==alTexts.size()){
	
			String s = "<SELECT NAME=\"" + sName + "\">";
			for (int i=0;i<alValues.size();i++){
				s = s + "<OPTION VALUE=\"" + (String)alValues.get(i) + "\"> " + (String)alTexts.get(i);
			}
			s = s + "</SELECT>";
			//System.out.println(s);
			return s;
	
		}else{
			//arraylist sizes don't match, return a blank text box with the same name.
			return "<INPUT TYPE=TEXT NAME=\"" + sName + "\" VALUE=\"\">";
		}
	}

	public static String TDDropDownBox(String sName, ArrayList<String> alValues, ArrayList<String> alTexts, String sSelected){
	
		//check to see if the size of value array and description array is identical.
		if (alValues.size()==alTexts.size()){
	
			String s = "<SELECT NAME=\"" + sName + "\">";
			for (int i=0;i<alValues.size();i++){
				s = s + "<OPTION VALUE=\"" + (String)alValues.get(i) + "\"";
				if (((String)alValues.get(i)).compareTo(sSelected) == 0){
					s = s + " SELECTED";
				}
				s = s  + "> " + (String)alTexts.get(i);
			}
			s = s + "</SELECT>";
			//System.out.println(s);
			return s;
	
		}else{
			//arraylist sizes don't match, return a blank text box with the same name.
			return "<INPUT TYPE=TEXT NAME=\"" + sName + "\" VALUE=\"\">";
		}
	}

	public static String TDListBox(
			String sName, 
			ArrayList<String> alValues, 
			ArrayList<String> alTexts, 
			int iSize,
			String sDefaultValue){
	
		//check to see if the size of value array and description array is identical.
		if (alValues.size()==alTexts.size()){
	
			String s = "<SELECT NAME=\"" + sName + "\" SIZE=" + iSize + ">";
			for (int i=0;i<alValues.size();i++){
				if (alValues.get(i).compareToIgnoreCase(sDefaultValue) == 0){
					s = s + "<OPTION VALUE=\"" + (String)alValues.get(i) + "\" CHECKED > " + (String)alTexts.get(i);
				}else{
					s = s + "<OPTION VALUE=\"" + (String)alValues.get(i) + "\"> " + (String)alTexts.get(i);
				}
			}
			s = s + "</SELECT>";
			//System.out.println(s);
			return s;
	
		}else{
			//arraylist sizes don't match, return a blank text box with the same name.
			return "<INPUT TYPE=TEXT NAME=\"" + sName + "\" VALUE=\"\" SIZE = " + iSize + ">";
		}
	}

	public static String TDMemoBox(String sName, int iRows, int iCols, String mNote, String sDesc){
	
		String s = "<TEXTAREA NAME=\"" + sName + "\" ROWS=\"" + iRows + "\" COLS=\"" + iCols + "\">" + 
		mNote + "</TEXTAREA>" + 
		sDesc;
		//System.out.println(s);
		return s;
	}

	//This function creates a field for editing a date field on a form with a date picker:
	public static String Create_Edit_Form_Date_Input_Field (
			String sFieldName,
			String sValue,
			ServletContext context
	){
	
		String s = "<INPUT TYPE=TEXT NAME=\"" + sFieldName + "\"";
		if (sValue != null){
			s += " VALUE=\"" + sValue + "\"";
		}
		else{
			s += " VALUE=\"\"";
		}
		s += "SIZE=8";
		s += " MAXLENGTH=10";
		s += " STYLE=\"width: " + ".75" + " in; height: 0.25in\"";
		s += ">";
		s += clsServletUtilities.getDatePickerString(sFieldName, context);
		return s;
	}

	public static String Create_Edit_Form_List_Field (
			String sFieldName,
			ArrayList<String> sValues,
			String sDefaultValue,
			ArrayList<String> sDescriptions
	){
	
		String sField = "<SELECT NAME = \"" + sFieldName + "\">\n";
		for (int i = 0; i < sValues.size(); i++){
			sField += "<OPTION";
			if (sValues.get(i).toString().compareTo(sDefaultValue) == 0){
				sField += " selected=yes";
			}
			sField += " VALUE=\"" + sValues.get(i).toString() + "\">" + sDescriptions.get(i).toString() + "\n";
		}
		sField += "</SELECT>";
	
		return sField;
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

}
