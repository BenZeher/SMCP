package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTablelabelprinters;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;

public class SMEditLabelPrintersEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Label Printer";
	private static String sCalledClassName = "SMEditLabelPrintersAction";
	private String sDBID = "";
	private String sCompanyName = "";
	private String sUserID = "";
	private String sUserFirstName = "";
	private String sUserLastName = "";
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMEditLabelPrinters
		)
		){
			return;
		}
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		sUserFirstName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
		sUserLastName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		String sLabelPrinter = clsStringFunctions.filter(
				request.getParameter(SMEditLabelPrintersSelect.LABELPRINTER_PARAM));

		String title = "";
		String subtitle = "";

		if(request.getParameter("SubmitEdit") != null){
			//User has chosen to edit:
			title = "Edit " + sObjectName + " " + sLabelPrinter;
			subtitle = "";
			out.println(SMUtilities.SMCPTitleSubBGColor(
					title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

			//Print a link to the first page after login:
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

			Edit_Record(sLabelPrinter, out, sDBID, false);
		}
		if(request.getParameter("SubmitDelete") != null){
			//User has chosen to delete:
			title = "Delete " + sObjectName + ": " + sLabelPrinter;
			subtitle = "";
			out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

			//Print a link to the first page after login:
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

			if (request.getParameter("ConfirmDelete") == null){
				out.println ("You must check the 'confirming' check box to delete.");
			}
			else{
				if (Delete_Record(sLabelPrinter, out, sDBID) == false){
					out.println ("Error deleting label printer: " + sLabelPrinter + ".");
				}
				else{
					out.println ("Successfully deleted label printer: " + sLabelPrinter + ".");
				}
			}
		}
		if(request.getParameter("SubmitAdd") != null){

			String sNewCode = clsStringFunctions.filter(request.getParameter("New" + sObjectName));
			//User has chosen to add a new object:
			title = "Add " + sObjectName + ": " + sNewCode;
			subtitle = "";
			out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

			//Print a link to the first page after login:
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

			if (sNewCode == ""){
				out.println ("You chose to add a new " + sObjectName + ", but you did not enter a new " + sObjectName + " to add.");
			}
			else{
				Edit_Record(sNewCode, out, sDBID, true);
			}
		}

		out.println("</BODY></HTML>");
	}

	private void Edit_Record(
			String sLabelPrinter, 
			PrintWriter pwOut, 
			String sConf,
			boolean bAddNew){

		pwOut.println("<FORM NAME='MAINFORM' ACTION='" 
				+ SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel." + sCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		pwOut.println("<TABLE BORDER=12 CELLSPACING=2>");

		String sDescription = "";
		String sHost = "";
		String sFont = "";
		long lPort = 0;
		long lTopMargin = 0;
		long lLeftMargin = 0;
		long lBarCodeHeight = 0;
		long lBarCodeWidth = 0;
		long lDarkness = 0;
		long lID = 0;
		long lLanguage = 0;

		if (!bAddNew){
			try{
				//Get the record to edit:
				String sSQL = "SELECT * FROM " + SMTablelabelprinters.TableName
				+ " WHERE ("
				+ "(" + SMTablelabelprinters.sName + " = '" + sLabelPrinter + "')"
				+ ")"
				;
				ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sConf);

				rs.next();
				sHost = rs.getString(SMTablelabelprinters.sHost);
				sDescription = rs.getString(SMTablelabelprinters.sDescription);
				lPort = rs.getLong(SMTablelabelprinters.iport);
				lTopMargin = rs.getLong(SMTablelabelprinters.iTopMargin);
				lLeftMargin = rs.getLong(SMTablelabelprinters.iLeftMargin);
				sFont = rs.getString(SMTablelabelprinters.sFont);
				lBarCodeHeight = rs.getLong(SMTablelabelprinters.iBarCodeHeight);
				lBarCodeWidth = rs.getLong(SMTablelabelprinters.iBarCodeWidth);
				lDarkness = rs.getLong(SMTablelabelprinters.iDarkness);
				lID = rs.getLong(SMTablelabelprinters.lid);
				lLanguage = rs.getLong(SMTablelabelprinters.iprinterlanguage);
				rs.close();
			}catch (SQLException ex){
				pwOut.println("<BR>Error reading label printer information - " + ex.getMessage());
				System.out.println("Error reading label printer information - " + ex.getMessage());
			}
		}

		//Display fields:
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTablelabelprinters.lid
				+ "\" VALUE=\"" + Long.toString(lID) + "\">");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTablelabelprinters.sName
				+ "\" VALUE=\"" + sLabelPrinter + "\">");
		//Name:
		pwOut.println("<TR><TD ALIGN=RIGHT><B>Printer name:</B></TD>"
				+ "<TD>" + sLabelPrinter + "</TD>"
				+ "<TD>&nbsp;</TD></TR>"
		);

		//Description:
		pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				SMTablelabelprinters.sDescription, 
				clsStringFunctions.filter(sDescription), 
				SMTablelabelprinters.sDescriptionLength, 
				"Description:", 
		"Longer description of the printer.")
		);

		//Host:
		pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				SMTablelabelprinters.sHost, 
				clsStringFunctions.filter(sHost), 
				SMTablelabelprinters.sHostLength, 
				"Host name:", 
		"Host name or IP of the printer.")
		);

		//Port:
		pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				SMTablelabelprinters.iport, 
				Long.toString(lPort), 
				6, 
				"Port number:", 
		"Port number of the printer.")
		);
		
		//Printer language:
		ArrayList <String>sLanguageValues = new ArrayList<String>(0);
		ArrayList <String>sLanguageDescriptions = new ArrayList<String>(0);
		sLanguageValues.add(Integer.toString(SMTablelabelprinters.PRINTER_LANGUAGE_ZPL));
		sLanguageValues.add(Integer.toString(SMTablelabelprinters.PRINTER_LANGUAGE_EPL));
		sLanguageDescriptions.add(SMTablelabelprinters.getPrinterLanguageDescription(
			SMTablelabelprinters.PRINTER_LANGUAGE_ZPL) + " Printer Language");
		sLanguageDescriptions.add(SMTablelabelprinters.getPrinterLanguageDescription(
			SMTablelabelprinters.PRINTER_LANGUAGE_EPL) + " Printer Language");
		pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
				SMTablelabelprinters.iprinterlanguage, 
				sLanguageValues, 
				Long.toString(lLanguage), 
				sLanguageDescriptions, 
				"Printer language", 
				"")
		);
		
		//Top margin:
		pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				SMTablelabelprinters.iTopMargin, 
				Long.toString(lTopMargin), 
				6, 
				"Top margin:", 
		"Top margin (in dots) for the print area.")
		);
		
		//Left Margin:
		pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				SMTablelabelprinters.iLeftMargin, 
				Long.toString(lLeftMargin), 
				6, 
				"Left Margin:", 
		"Left margin (in dots) for the print area.")
		);

		ArrayList <String>sValues = new ArrayList<String>(0);
		ArrayList <String>sDescriptions = new ArrayList<String>(0);
		sValues.add("P");
		sValues.add("Q");
		sValues.add("R");
		sValues.add("S");
		sValues.add("T");
		sValues.add("U");
		sValues.add("V");
		sValues.add("1");
		sValues.add("2");
		sValues.add("3");
		sValues.add("4");
		sValues.add("5");
		sDescriptions.add("Zebra Font P (ZPL Only)");
		sDescriptions.add("Zebra Font Q (ZPL Only)");
		sDescriptions.add("Zebra Font R (ZPL Only)");
		sDescriptions.add("Zebra Font S (ZPL Only)");
		sDescriptions.add("Zebra Font T (ZPL Only)");
		sDescriptions.add("Zebra Font U (ZPL Only)");
		sDescriptions.add("Zebra Font V (ZPL Only)");
		sDescriptions.add("Zebra Font 1 (EPL Only)");
		sDescriptions.add("Zebra Font 2 (EPL Only)");
		sDescriptions.add("Zebra Font 3 (EPL Only)");
		sDescriptions.add("Zebra Font 4 (EPL Only)");
		sDescriptions.add("Zebra Font 5 (EPL Only)");
		pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
				SMTablelabelprinters.sFont, 
				sValues, 
				sFont, 
				sDescriptions, 
				"Font", 
				"Default to 'U' for ZPL, '3' for EPL.")
		);
		
		//Bar code height:
		pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				SMTablelabelprinters.iBarCodeHeight, 
				Long.toString(lBarCodeHeight), 
				6, 
				"Bar code height (in dots):", 
				"Range: 1 to 32000 - default is 75.")
		);		
		
		//Bar code width:
		sValues.clear();
		sDescriptions.clear();
		sValues.add("1");
		sValues.add("2");
		sValues.add("3");
		sValues.add("4");
		sValues.add("5");
		sValues.add("6");
		sValues.add("7");
		sValues.add("8");
		sValues.add("9");
		sValues.add("10");
		sDescriptions.add("1");
		sDescriptions.add("2");
		sDescriptions.add("3");
		sDescriptions.add("4");
		sDescriptions.add("5");
		sDescriptions.add("6");
		sDescriptions.add("7");
		sDescriptions.add("8");
		sDescriptions.add("9");
		sDescriptions.add("10");
		pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
				SMTablelabelprinters.iBarCodeWidth, 
				sValues, 
				Long.toString(lBarCodeWidth), 
				sDescriptions, 
				"Bar code width", 
				"Default to '4' for ZPL, '2' for EPL.")
		);

		//Darkness:
		sValues.clear();
		sDescriptions.clear();
		sValues.add("01");
		sValues.add("02");
		sValues.add("03");
		sValues.add("04");
		sValues.add("05");
		sValues.add("06");
		sValues.add("07");
		sValues.add("08");
		sValues.add("09");
		sValues.add("10");
		sValues.add("11");
		sValues.add("12");
		sValues.add("13");
		sValues.add("14");
		sValues.add("15");
		sValues.add("16");
		sValues.add("17");
		sValues.add("18");
		sValues.add("19");
		sValues.add("20");
		sValues.add("21");
		sValues.add("22");
		sValues.add("23");
		sValues.add("24");
		sValues.add("25");
		sValues.add("26");
		sValues.add("27");
		sValues.add("28");
		sValues.add("29");
		sValues.add("30");
		sDescriptions.add("01");
		sDescriptions.add("02");
		sDescriptions.add("03");
		sDescriptions.add("04");
		sDescriptions.add("05");
		sDescriptions.add("06");
		sDescriptions.add("07");
		sDescriptions.add("08");
		sDescriptions.add("09");
		sDescriptions.add("10");
		sDescriptions.add("11");
		sDescriptions.add("12");
		sDescriptions.add("13");
		sDescriptions.add("14");
		sDescriptions.add("15");
		sDescriptions.add("16");
		sDescriptions.add("17");
		sDescriptions.add("18");
		sDescriptions.add("19");
		sDescriptions.add("20");
		sDescriptions.add("21");
		sDescriptions.add("22");
		sDescriptions.add("23");
		sDescriptions.add("24");
		sDescriptions.add("25");
		sDescriptions.add("26");
		sDescriptions.add("27");
		sDescriptions.add("28");
		sDescriptions.add("29");
		sDescriptions.add("30");
		pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
				SMTablelabelprinters.iDarkness, 
				sValues, 
				clsStringFunctions.PadLeft(Long.toString(lDarkness), "0", 2),
				sDescriptions, 
				"Darkness", 
				"Use caution setting to maximum value (30).  NOTE: This setting is ignored for EPL printing - the program simply uses the default density setting.")
		);
		
		pwOut.println("</TABLE><BR><P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update " + sObjectName 
				+ "' STYLE='height: 0.24in'></P></FORM>");
	}

	private boolean Delete_Record(
			String sLabelPrinter,
			PrintWriter pwOut,
			String sConf){

		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() 
				+ ".Delete_Record"
				+ "- user: "
				+ sUserID
				+ " - "
				+ sUserFirstName
				+ " "
				+ sUserLastName)
		);

		if (conn == null){
			pwOut.println("Error getting connection to delete record.");
			System.out.println("Error getting connection to delete record.");
			return false;
		}

		//First get the ID of the record to be deleted:
		long lLabelPrinterID = 0;
		String sSQL = "SELECT * FROM " + SMTablelabelprinters.TableName
		+ " WHERE ("
		+ "(" + SMTablelabelprinters.sName + " = '" + sLabelPrinter + "')"
		+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sConf);
			if (rs.next()){
				lLabelPrinterID = rs.getLong(SMTablelabelprinters.lid);
			}
			rs.close();
		} catch (SQLException e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			pwOut.println("Could not read ID of label printer to be deleted with SQL: " 
					+ sSQL + " - " + e.getMessage() + ".");
			return false;
		}

		if (lLabelPrinterID == 0){
			pwOut.println("Could not read ID of label printer to be deleted.");
			return false;
		}

		String SQL = "DELETE FROM " + SMTablelabelprinters.TableName
		+ " WHERE ("
		+ SMTablelabelprinters.lid + " = " + Long.toString(lLabelPrinterID) 
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch (SQLException ex){
			pwOut.println("Error deleting label printer record with SQL: " + SQL 
					+ " - " + ex.getMessage());
			System.out.println("Error deleting label printer record with SQL: " + SQL 
					+ " - " + ex.getMessage());
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			return false;
		}		

		clsDatabaseFunctions.freeConnection(getServletContext(), conn);

		return true;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
