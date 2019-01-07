package smfa;

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
import SMDataDefinition.SMTablefaclasses;
import SMDataDefinition.SMTablefatransactions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;

public class FAAssetListSelect extends HttpServlet {
	public static final String FISCALYEARSELECTION = "FISCALYRSELECT";
	public static final String NO_TRANSACTIONS_AVAILABLE = "NOTRANSACTIONSAVAILABLE";
	public static final String CLASS_CHECKBOX_PARAM = "CLASSCHECKBOXPARAM";
	private static final long serialVersionUID = 1L;
	private static String sCalledClassName = "FAAssetListGenerate";
	private String sDBID = "";
	private String sCompanyName = "";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.FAAssetList)){
	    	return;
	    }

	    //String sEditCode = (String) SMUtilities.filter(request.getParameter(sObjectName));
		String title = "Asset List for " + sCompanyName;
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
	    if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
	    if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>STATUS: " + sStatus + "</B><BR>");
		}
	    
	  //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Fixed Assets Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.FAAssetList) 
	    		+ "\">Summary</A><BR><BR>");
		
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa." + sCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='CALLINGCLASS' VALUE='" + SMUtilities.getFullClassName(this.toString()) + "'>");
	    String sOutPut = "<P><B>This function will print a list of all assets.</B></P>";
		sOutPut = sOutPut + "<INPUT TYPE=CHECKBOX NAME='INCLUDEDISPOSED'> Include disposed assets on the list.<BR>";
		sOutPut = sOutPut + "<INPUT TYPE=CHECKBOX NAME='INCLUDENONDISPOSED'> Include non-disposed assets on the list.<BR>";
		sOutPut = sOutPut + "<INPUT TYPE=CHECKBOX NAME='SHOWDETAILS'> Show details.<BR>";
		
		sOutPut += "<BR>Show year-to-date values for fiscal year:&nbsp;"
			+ "<SELECT NAME = \"" + FISCALYEARSELECTION + "\">";
		String SQL = "SELECT DISTINCT "
			+ SMTablefatransactions.iFiscalYear
			+ " FROM " + SMTablefatransactions.TableName
			+ " ORDER BY " + SMTablefatransactions.iFiscalYear + " DESC"
		;
		int iRecordCounter = 0;
		try {
			ResultSet rsFiscalYears = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID, "MySQL", this.toString() + ".doPost");
			while (rsFiscalYears.next()){
				sOutPut += "<OPTION VALUE=\"" + Integer.toString(rsFiscalYears.getInt(SMTablefatransactions.iFiscalYear)) + "\">"
					+ Integer.toString(rsFiscalYears.getInt(SMTablefatransactions.iFiscalYear)) + "</OPTION>";
				iRecordCounter++;
			}
			rsFiscalYears.close();
		} catch (SQLException e) {
			sOutPut += "Error [1484925442] reading fiscal years with SQL: " + SQL + " - " + e.getMessage();
		}
		
		if (iRecordCounter == 0){
			sOutPut += "<OPTION VALUE=\"" + NO_TRANSACTIONS_AVAILABLE + "\">"
					+ "(No transactions available)" + "</OPTION>";
		}
		sOutPut += " </SELECT>";
		
		ArrayList<String> sClassTable = new ArrayList<String>(0);
		//Allow the user to select which classes will appear:
		SQL = "SELECT * FROM " + SMTablefaclasses.TableName
			+ " ORDER BY " + SMTablefaclasses.sClass
		;
		try {
			ResultSet rsFAClasses = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID, "MySQL", this.toString() + ".doPost - getting classes");
			while (rsFAClasses.next()){
				sClassTable.add(
					"<INPUT TYPE=CHECKBOX " 
					+ clsServletUtilities.CHECKBOX_CHECKED_STRING
					+ " NAME=\"" + CLASS_CHECKBOX_PARAM +  rsFAClasses.getString(SMTablefaclasses.sClass) + "\">" 
					+ rsFAClasses.getString(SMTablefaclasses.sClass) 
					+ " - " + rsFAClasses.getString(SMTablefaclasses.sClassDescription)
				);
			}
			rsFAClasses.close();
		} catch (SQLException e) {
			sOutPut += "Error [1484925441] reading FA classes with SQL: " + SQL + " - " + e.getMessage();
		}
    	//Print the table:
		sOutPut = sOutPut + "<BR><BR><I><U>Show <B>ONLY</B> assets from these classes:</U></I><BR><BR>";
    	sOutPut = sOutPut + SMUtilities.Build_HTML_Table(4, sClassTable,1,true);		
		
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='SubmitList' VALUE='List assets' STYLE='width: 2.00in; height: 0.24in'>";
		
		sOutPut = sOutPut + "</FORM>";
		out.println(sOutPut);

		out.println("</BODY></HTML>");
	}
		
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
