package smgl;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableglfiscalperiods;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLEditFiscalPeriodsSelect extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static String sCalledClassName = "GLEditFiscalPeriodsEdit";
	public static final String OBJECT_NAME = GLFiscalYear.ParamObjectName;
	public static final String SUBMIT_EDIT_BUTTON_NAME = "SubmitEdit";
	public static final String SUBMIT_EDIT_BUTTON_VALUE = "Edit Selected Fiscal Year";
	public static final String SUBMIT_DELETE_BUTTON_NAME = "SubmitDelete";
	public static final String SUBMIT_DELETE_BUTTON_VALUE = "Delete Selected Fiscal Year";
	public static final String CONFIRM_DELETE_CHECKBOX_NAME = "ConfirmDelete";
	public static final String SUBMIT_ADD_BUTTON_NAME = "SubmitAdd";
	public static final String SUBMIT_ADD_BUTTON_VALUE = "Add New Fiscal Year";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.GLEditFiscalPeriods))
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
	    String title = "Manage " + "Fiscal Years";
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
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to General Ledger Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.GLEditFiscalPeriods) 
	    		+ "\">Summary</A><BR><BR>");
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl." + sCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    String sOutPut = "";
	    
	    //Use a drop down list here:
	    GLFiscalYear entry = new GLFiscalYear(request);
		try{
	        String sSQL = "SELECT * FROM " + SMTableglfiscalperiods.TableName
	        	+ " ORDER BY " + SMTableglfiscalperiods.TableName + "." + SMTableglfiscalperiods.ifiscalyear + " DESC"
	        ;
	        ResultSet rs = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	sDBID,
	        	"MySQL",
	        	this.toString() + ".doPost - User: " + sUserID
	        	+ " - "
	        	+ sUserFullName
	        		);
	        out.println("<I><U><B>GL Fiscal Years:</B></U></I><BR>");
	     	out.println ("<SELECT NAME=\"" + SMTableglfiscalperiods.ifiscalyear + "\">" );
	     	String sSelected = "";
        	while (rs.next()){
        		String sFiscalYear = Long.toString(rs.getLong(SMTableglfiscalperiods.ifiscalyear));
        		if (entry.get_sifiscalyear().compareToIgnoreCase(sFiscalYear) == 0){
        			sSelected = "selected";
        		}else{
        			sSelected = "";
        		}
        		out.println ("<OPTION " + sSelected + " VALUE=\"" + Long.toString(rs.getLong(SMTableglfiscalperiods.ifiscalyear)) + "\">");
        		out.println (rs.getString(SMTableglfiscalperiods.TableName + "." + SMTableglfiscalperiods.ifiscalyear));
        	}
        	rs.close();
	        	//End the drop down list:
	        out.println ("</SELECT>");
	        out.println ("<BR>");
		}catch (SQLException ex){
			out.println("<BR><FONT COLOR=RED><B>Error [1530906912] reading " + OBJECT_NAME + "s  - " + ex.getMessage() + ".</B></FONT><BR>");
			//return false;
		}
		
		sOutPut += "<P><INPUT TYPE=SUBMIT NAME='" + SUBMIT_EDIT_BUTTON_NAME 
				+ "' VALUE='" + SUBMIT_EDIT_BUTTON_VALUE + "' STYLE='width: 3in; height: 0.24in'></P>";
		
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='" + SUBMIT_DELETE_BUTTON_NAME 
				+ "' VALUE='" + SUBMIT_DELETE_BUTTON_VALUE + "' STYLE='width: 3in; height: 0.24in'>";
		
		sOutPut = sOutPut + "  <LABEL NAME=DELBUTTON>Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_DELETE_CHECKBOX_NAME + "\"></LABEL>";
		
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='" + SUBMIT_ADD_BUTTON_NAME 
				+ "' VALUE='" + SUBMIT_ADD_BUTTON_VALUE + "' STYLE='width: 3in; height: 0.24in'></P>";
		
		sOutPut = sOutPut + "</FORM>";
		out.println(sOutPut);
		
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}