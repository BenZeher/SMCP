package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMTax;
import SMDataDefinition.SMTabletax;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMSystemFunctions;
import ConnectionPool.WebContextParameters;

public class SMEditTaxSelection extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static String sCalledClassName = "SMEditTaxEdit";
	public static final String SUBMIT_EDIT_BUTTON_NAME = "SubmitEdit";
	public static final String SUBMIT_EDIT_BUTTON_VALUE = "Edit Selected " + SMTax.ParamObjectName;
	public static final String SUBMIT_DELETE_BUTTON_NAME = "SubmitDelete";
	public static final String SUBMIT_DELETE_BUTTON_VALUE = "Delete Selected " + SMTax.ParamObjectName;
	public static final String CONFIRM_DELETE_CHECKBOX_NAME = "ConfirmDelete";
	public static final String SUBMIT_ADD_BUTTON_NAME = "SubmitAdd";
	public static final String SUBMIT_ADD_BUTTON_VALUE = "Add New " + SMTax.ParamObjectName;
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditTaxes))
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
	    String title = "Manage " + SMTax.ParamObjectName + "es";
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
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.SMEditTaxes) 
	    		+ "\">Summary</A><BR><BR>");
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel." + sCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    String sOutPut = "";
	    
	    //Use a drop down list here:
	    SMTax entry = new SMTax(request);
		try{
	        String sSQL = "SELECT * FROM " + SMTabletax.TableName
	        	+ " ORDER BY " + SMTabletax.staxjurisdiction + ", " + SMTabletax.staxtype;
	        ResultSet rs = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	sDBID,
	        	"MySQL",
	        	this.toString() + ".doPost - User: " + sUserID
	        	+ " - "
	        	+ sUserFullName
	        		);
	     	out.println ("<SELECT NAME=\"" + SMTabletax.lid + "\">" );
        	String sSelected = "";
        	while (rs.next()){
        		String sLid = Long.toString(rs.getLong(SMTabletax.lid));
        		if (entry.get_slid().compareToIgnoreCase(sLid) == 0){
        			sSelected = "selected";
        		}else{
        			sSelected = "";
        		}
        		out.println ("<OPTION " + sSelected + " VALUE=\"" + sLid + "\">");
        		out.println (rs.getString(SMTabletax.staxjurisdiction) + " - " + rs.getString(SMTabletax.staxtype));
        	}
        	rs.close();
	        	//End the drop down list:
	        out.println ("</SELECT>");
	        out.println ("<BR>");
		}catch (SQLException ex){
			out.println("<BR><FONT COLOR=RED><B>Error [1453819422] reading " + SMTax.ParamObjectName + "es - " + ex.getMessage() + ".</B></FONT><BR>");
			//return false;
		}
		
		sOutPut += "<P><INPUT TYPE=SUBMIT NAME='" + SUBMIT_EDIT_BUTTON_NAME 
				+ "' VALUE='" + SUBMIT_EDIT_BUTTON_VALUE + "' STYLE='width: 2.25in; height: 0.24in'></P>\n";
		
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='" + SUBMIT_DELETE_BUTTON_NAME 
				+ "' VALUE='" + SUBMIT_DELETE_BUTTON_VALUE + "' STYLE='width: 2.25in; height: 0.24in'>\n";
		
		sOutPut = sOutPut + "  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_DELETE_CHECKBOX_NAME + "\">\n";
		
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='" + SUBMIT_ADD_BUTTON_NAME 
				+ "' VALUE='" + SUBMIT_ADD_BUTTON_VALUE + "' STYLE='width: 2.25in; height: 0.24in'></P>\n";
		
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