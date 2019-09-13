package smar;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTabledoingbusinessasaddresses;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;

public class ARPrintStatementsSelection  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String DBA_INPUT = "DBA_INPUT";
	public static final String DBA_DEFAULT = "DBA_DEFAULT";
	public static final String DBA_CHOOSE = "DBA_CHOOSE";
	public static final String DBA_CHOICE = "DBA_CHOICE";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ARPrintStatements))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Print Statements";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    //Print a link to main menu:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + SMSystemFunctions.ARPrintStatements + "\">Summary</A><BR><BR>");
	    
	    try {
	    	
	    	boolean bSelectAllTypes = true;
	    	if (clsManageRequestParameters.get_Request_Parameter("SelectAllTypes", request).compareToIgnoreCase("0") == 0){
	    		bSelectAllTypes = false;
	    	}
	    	
	    	ArrayList<String> alValues = new ArrayList<String>(0);
	    	ArrayList<String> alOptions = new ArrayList<String>(0);
	    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARPrintStatementsGenerate\">");
	    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    	out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
	    	out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>");
	    	//Starting date will be 1990-01-01
	        out.println("<INPUT TYPE=HIDDEN NAME=SelectedStartYear VALUE=\"1990\">");
	        out.println("<INPUT TYPE=HIDDEN NAME=SelectedStartMonth VALUE=\"1\">");
	        out.println("<INPUT TYPE=HIDDEN NAME=SelectedStartDay VALUE=\"1\">");
	
	    	out.println("<TR><TD ALIGN=CENTER WIDTH=100%><TABLE BORDER=0 WIDTH=100%>");
	    	
	    	out.println("<TR><TD ALIGN=LEFT WIDTH=30%><B>Age As Of:</B></TD>" +
	    					"<TD ALIGN=LEFT WIDTH=35%><B>Aged By:</B></TD>" +
	    					"<TD ALIGN=LEFT WIDTH=35%><B>Cutoff Date:</B></TD>" +
	    				"</TR>");
	    	//out.println("<TR><TD ALIGN=LEFT>" + ARUtilities.TDDateSelection("AsOfDate", new Date(System.currentTimeMillis()), "") + "</TD>");
	    	out.println(
	    			"<TR><TD ALIGN=LEFT>" 
	    			+ "<INPUT TYPE=TEXT NAME=\"" + "AsOfDate" + "\""
	    			+ " VALUE=\"" + clsDateAndTimeConversions.now("MM/dd/yyyy") + "\""
	    	        + " SIZE=12"
	    	        + " MAXLENGTH=10"
	    	        + ">"
	    			+ SMUtilities.getDatePickerString("AsOfDate", getServletContext())
	    			+ "</TD>");
	    	
	    	
	    	alValues.clear(); alOptions.clear();
	    	alValues.add("0"); alOptions.add("Due Date");
	    	alValues.add("1"); alOptions.add("Doc. Date");
			out.println("<TD ALIGN=LEFT>" + clsCreateHTMLFormFields.TDDropDownBox("AgedBy", alValues, alOptions, "1") + "</TD>");
			
	    	out.println(
	    			"<TD ALIGN=LEFT>" 
	    			+ "<INPUT TYPE=TEXT NAME=\"" + "CutOffDate" + "\""
	    			+ " VALUE=\"" + clsDateAndTimeConversions.now("MM/dd/yyyy") + "\""
	    	        + " SIZE=12"
	    	        + " MAXLENGTH=10"
	    	        + ">"
	    			+ SMUtilities.getDatePickerString("CutOffDate", getServletContext())
	    			+ "</TD>");

			out.println("</TR>");
	    	
	    	out.println("<TR><TD ALIGN=LEFT><B>Customer Selection:</B></TD>");
	    	//get customer list from database
	    	String sSQL =  "SELECT " 
	    			+ SMTablearcustomer.sCustomerNumber + ", "
	    			+ SMTablearcustomer.sCustomerName
	    			+ " FROM " + SMTablearcustomer.TableName
	    			+ " ORDER BY " + SMTablearcustomer.sCustomerNumber + " ASC LIMIT 1";
	    	ResultSet rsCustomers = clsDatabaseFunctions.openResultSet(
	    		sSQL, 
	    		getServletContext(), 
	    		sDBID,
	    		"MySQL",
	    		this.toString() + ".doPost (1) - User: " + sUserID
	    		+ " - "
	    		+ sUserFullName
	    			);
	    	String sStartingCustomerNumber = "";
	    	if (rsCustomers.next()){
	    		sStartingCustomerNumber = rsCustomers.getString(SMTablearcustomer.sCustomerNumber);
	    	}
	    	rsCustomers.close();
	    	out.println("<TD WIDTH=30%>" + "<B>Starting with:</B> " 
	    		+ clsCreateHTMLFormFields.TDTextBox(
	    		"StartingCustomer", sStartingCustomerNumber, 10, SMTablearcustomer.sCustomerNumberLength, "") + "</TD>");
	    	
	    	sSQL =  "SELECT " 
	    			+ SMTablearcustomer.sCustomerNumber + ", "
	    			+ SMTablearcustomer.sCustomerName
	    			+ " FROM " + SMTablearcustomer.TableName
	    			+ " ORDER BY " + SMTablearcustomer.sCustomerNumber + " DESC LIMIT 1";
	    	rsCustomers = clsDatabaseFunctions.openResultSet(
		    		sSQL, 
		    		getServletContext(), 
		    		sDBID,
		    		"MySQL",
		    		this.toString() + ".doPost (2) - User: " + sUserID
		    		+ " - "
		    		+ sUserFullName
	    			);
	    	String sEndingCustomerNumber = "";
	    	if (rsCustomers.next()){
	    		sEndingCustomerNumber = rsCustomers.getString(SMTablearcustomer.sCustomerNumber);
	    	}
	    	rsCustomers.close();
	    	out.println("<TD WIDTH=30%>" + "<B>Ending with:</B> " 
	    		+ clsCreateHTMLFormFields.TDTextBox(
	    		"EndingCustomer", sEndingCustomerNumber, 10, SMTablearcustomer.sCustomerNumberLength, "") + "</TD>");
	    	out.println("</TABLE></TD></TR>");
	    	
	    	out.println("<TR><TD ALIGN=CENTER WIDTH=100%>");
		    out.println("<TABLE BORDER=0 WIDTH=100%>");
			out.println("<TR><TD ALIGN=LEFT WIDTH=20%><B>&nbsp;</B></TD>" +
									"<TD ALIGN=LEFT WIDTH=20%><B>Aging columns:</B></TD>" +
									"<TD ALIGN=LEFT WIDTH=12%><B>Current</B></TD>" +
									"<TD ALIGN=LEFT WIDTH=12%><B>1st</B></TD>" +
									"<TD ALIGN=LEFT WIDTH=12%><B>2nd</B></TD>" +
									"<TD ALIGN=LEFT WIDTH=12%><B>3rd</B></TD>" +
								"</TR>");
			out.println("<TR>");
			out.println("<TD>&nbsp;</TD>");
			out.println("<TD>&nbsp;</TD>");
			//deadline for current
			out.println("<TD>" + clsCreateHTMLFormFields.TDTextBox("Current", "30", 6, 10, "") + "</TD>");
			//deadline for 1st
			out.println("<TD>" + clsCreateHTMLFormFields.TDTextBox("1st", "60", 6, 10, "Days") + "</TD>");
			//deadline for 2nd
			out.println("<TD>" + clsCreateHTMLFormFields.TDTextBox("2nd", "90", 6, 10, "Days") + "</TD>");
			//deadline for 3rd
			out.println("<TD>" + clsCreateHTMLFormFields.TDTextBox("3rd", "120", 6, 10, "Days") + "</TD>");
			//dead debts - always equals to 3rd
			//out.println("<TD WIDTH=11%>" + ARUtilities.TDTextBox("Over", "120", 10, 10, "Where?") + "</TD>");
			out.println("</TR>");
		    out.println("</TABLE>");
		    out.println("</TD></TR>");

		    //Only print for customers over 'current':
	    	out.println("<TR><TD ALIGN=CENTER WIDTH=100%>");
		    out.println("<TABLE BORDER=0 WIDTH=100%>");
			out.println("<TR><TD><B>"
					+ "Only print statements for customers over the 'Current' aging period"
					+ "&nbsp;"
					+ "<INPUT TYPE=CHECKBOX NAME=\"" + "OnlyOverCurrent" + "\" width=0.25>"
					+ "</B</TD></TR>");
		    out.println("</TABLE>");
		    out.println("</TD></TR>");
		    
		    //Print statements for 'zero balance' customers:
	    	out.println("<TR><TD ALIGN=CENTER WIDTH=100%>");
		    out.println("<TABLE BORDER=0 WIDTH=100%>");
			out.println("<TR><TD><B>"
					+ "Print statements for customers with no open transactions in the range"
					+ "&nbsp;"
					+ "<INPUT TYPE=CHECKBOX NAME=\"" + "PrintZeroBalanceStatements" + "\" width=0.25>"
					+ "</B</TD></TR>");
		    out.println("</TABLE>");
		    out.println("</TD></TR>");

		    //Only print for customers require statements:
	    	out.println("<TR><TD ALIGN=CENTER WIDTH=100%>");
		    out.println("<TABLE BORDER=0 WIDTH=100%>");
			out.println("<TR><TD><B>"
					+ "Only print statements for customers who require monthly statements"
					+ "&nbsp;"
					+ "<INPUT TYPE=CHECKBOX NAME=\"" + "OnlyRequireStatement" + "\" width=0.25>"
					+ "</B</TD></TR>");
		    out.println("</TABLE>");
		    out.println("</TD></TR>");
		    
		    //TODO make the dropdown and radial buttons here for Remit-To as choice. Default to the system's setting and create a dropdown list to choose from 
		    out.println("<TR><TD ALIGN CENTER WIDTH = 100%>");
		    out.println("<TABLE BORDER=0 WIDTH=100%>");
		    sSQL = "SELECT * " +
		    		" FROM " + SMTabledoingbusinessasaddresses.TableName
		    		+ " ORDER BY " + SMTabledoingbusinessasaddresses.TableName + "." +SMTabledoingbusinessasaddresses.sDescription + " DESC" ;
		    ResultSet rsDBA = clsDatabaseFunctions.openResultSet(
		    		sSQL, 
		    		getServletContext(), 
		    		sDBID,
		    		"MySQL",
		    		this.toString() + ".doPost (2) - User: " + sUserID
		    		+ " - "
		    		+ sUserFullName
	    			);
	    	ArrayList <String> saDBAName = new ArrayList<String>();
	    	ArrayList <String> saDBAID = new ArrayList<String>();
	    	while (rsDBA.next()){
	    		if(rsDBA.getString(SMTabledoingbusinessasaddresses.sDescription).compareToIgnoreCase("")!=0 || rsDBA.getString(SMTabledoingbusinessasaddresses.sDescription)!=null) {
	    			saDBAName.add(rsDBA.getString(SMTabledoingbusinessasaddresses.sDescription));
	    			saDBAID.add(rsDBA.getString(SMTabledoingbusinessasaddresses.lid));
	    		}
	    	}
	    	rsDBA.close();
		    out.println("<TR><TD><B>"
		    		+ "Use Remit-to address from : "
		    		+ "&nbsp; </B>"
		    		+ "<INPUT TYPE=\"RADIO\" NAME=\"" + DBA_INPUT + "\" VALUE=\"" + DBA_DEFAULT + "\" ID=\"" +DBA_DEFAULT + "\" CHECKED><LABEL FOR=\""  + DBA_DEFAULT + "\"> Company Profile <B>OR</B>  &nbsp;</LABEL>"
		    		+ "<INPUT TYPE=\"RADIO\" NAME=\"" + DBA_INPUT + "\" VALUE=\"" + DBA_CHOOSE + "\" ID=\"" + DBA_CHOOSE + "\"><LABEL FOR=\"" + DBA_CHOOSE + "\"> Selected DBA &nbsp;</LABEL>"
		    		+ "");
		    out.println("<SELECT NAME=\"" + DBA_CHOICE + "\">");
		    for(int i = 0; i < saDBAName.size(); i ++) {
		    	out.println("\t<OPTION VALUE =\"" + saDBAID.get(i) + "\">" + saDBAName.get(i) +"</OPTION>");
		    }
		    out.println("</SELECT>");
		    out.println( "</TD></TR>");
		    out.println("</TABLE>");
		    out.println("</TD></TR>");
		    
		    //		  document types to show
		    out.println("<TR><TD><TABLE WIDTH=100% BORDER=0>");
		    out.println("<TR>" +
							"<TD ALIGN=LEFT WIDTH=15%><B>Document Types: <BR>");
		    if (!bSelectAllTypes){
				out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARPrintStatementsSelection?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\"><FONT SIZE=2>select all types</FONT></A>");
		    }else{
		    	out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARPrintStatementsSelection?SelectAllTypes=0&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\"><FONT SIZE=2>clear all types</FONT></A>");
		    }
			out.println("</B></TD><TD ALIGN=LEFT WIDTH=85%>");
		    ArrayList<String> alDocTypes = new ArrayList<String>(0);
		    for (int i=0;i<=10;i++){
		    	if (bSelectAllTypes){
		    		alDocTypes.add("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + ARDocumentTypes.Get_Document_Type_Label(i) + "\" VALUE=1 CHECKED>" + ARDocumentTypes.Get_Document_Type_Label(i) + "<BR></LABEL>");
		    	}else{
		    		alDocTypes.add("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + ARDocumentTypes.Get_Document_Type_Label(i) + "\" VALUE=1>" + ARDocumentTypes.Get_Document_Type_Label(i) + "<BR></LABEL>");
		    	}
		    }
		    out.println(clsServletUtilities.Build_HTML_Table(5, alDocTypes, 0, false));
		    out.println("</TD></TR></TABLE></TD></TR>");
		    
	        out.println("</TABLE><BR><BR>");
	        
	    	out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Print statements----\">");
	    	out.println("</FORM>");
	    	
	    } catch (SQLException ex) {
	        // handle any errors
	    	System.out.println("Error in " + this.toString() + " class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
	    }
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
