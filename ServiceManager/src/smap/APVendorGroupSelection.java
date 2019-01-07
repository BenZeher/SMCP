package smap;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableapvendorgroups;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;


public class APVendorGroupSelection extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static String sCalledClassName = "APVendorGroupEdit";
	private static String sDBID = "";
	private static String sUserID = "";
	private static String sUserFullName = "";
	private static String sCompanyName = "";
	public static final String SUBMIT_EDIT_BUTTON_NAME = "SubmitEdit";
	public static final String SUBMIT_EDIT_BUTTON_VALUE = "Edit Selected " + APVendorGroup.ParamObjectName;
	public static final String SUBMIT_DELETE_BUTTON_NAME = "SubmitDelete";
	public static final String SUBMIT_DELETE_BUTTON_VALUE = "Delete Selected " + APVendorGroup.ParamObjectName;
	public static final String CONFIRM_DELETE_CHECKBOX_NAME = "ConfirmDelete";
	public static final String SUBMIT_ADD_BUTTON_NAME = "SubmitAdd";
	public static final String SUBMIT_ADD_BUTTON_VALUE = "Add New " + APVendorGroup.ParamObjectName;
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.APEditVendorGroups))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    		+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Manage " + APVendorGroup.ParamObjectName + "s";
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
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Payable Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.APEditAccountSets) 
	    		+ "\">Summary</A><BR><BR>");
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smap." + sCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    String sOutPut = "";
	    
	    //Use a drop down list here:
	    APVendorGroup entry = new APVendorGroup(request);
		try{
	        String sSQL = "SELECT * FROM " + SMTableapvendorgroups.TableName 
	        	+ " ORDER BY " + SMTableapvendorgroups.sgroupid;
	        ResultSet rs = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	sDBID,
	        	"MySQL",
	        	this.toString() + ".doPost - User: " + sUserID
	        	+ " - "
	        	+ sUserFullName
	        		);
	     	out.println ("<SELECT NAME=\"" + APVendorGroup.Paramlid + "\">" );
        	String sSelected = "";
        	while (rs.next()){
        		String sLid = Long.toString(rs.getLong(SMTableapvendorgroups.lid));
        		if (entry.getlid().compareToIgnoreCase(sLid) == 0){
        			sSelected = "selected";
        		}else{
        			sSelected = "";
        		}
        		out.println ("<OPTION " + sSelected + " VALUE=\"" + rs.getString(SMTableapvendorgroups.lid) + "\">");
        		out.println (rs.getString(SMTableapvendorgroups.sgroupid) + " - " + rs.getString(SMTableapvendorgroups.sdescription));
        	}
        	rs.close();
	        	//End the drop down list:
	        out.println ("</SELECT>");
	        out.println ("<BR>");
		}catch (SQLException ex){
			out.println("<BR><FONT COLOR=RED><B>Error [1450379132] reading vendor groups - " + ex.getMessage() + ".</B></FONT><BR>");
			//return false;
		}
		
		sOutPut += "<P><INPUT TYPE=SUBMIT NAME='" + SUBMIT_EDIT_BUTTON_NAME 
				+ "' VALUE='" + SUBMIT_EDIT_BUTTON_VALUE + "' STYLE='width: 2.25in; height: 0.24in'></P>";
		
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='" + SUBMIT_DELETE_BUTTON_NAME 
				+ "' VALUE='" + SUBMIT_DELETE_BUTTON_VALUE + "' STYLE='width: 2.25in; height: 0.24in'>";
		
		sOutPut = sOutPut + "  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_DELETE_CHECKBOX_NAME + "\">";
		
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='" + SUBMIT_ADD_BUTTON_NAME 
				+ "' VALUE='" + SUBMIT_ADD_BUTTON_VALUE + "' STYLE='width: 2.25in; height: 0.24in'></P>";
		
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