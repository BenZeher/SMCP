package smar;

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
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTablearcustomershiptos;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class AREditCustomerShipTos extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sObjectName = "Customer Ship To";
	private static final String sCalledClassName = "AREditCustomerShipTosEdit";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.AREditCustomerShipToLocations))
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
	    String title = "Manage " + sObjectName + "s.";
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
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.AREditCustomerShipToLocations) 
	    		+ "\">Summary</A><BR><BR>");
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." + sCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    //Add drop down list
		try{
	        String sSQL = "SELECT " 
	        		+ SMTablearcustomershiptos.sCustomerNumber + ", "
	        		+ SMTablearcustomershiptos.sShipToCode + ", "
	        		+ SMTablearcustomershiptos.sDescription
	        		+ " FROM " + SMTablearcustomershiptos.TableName
	        		+ " ORDER BY " 
	        		+ SMTablearcustomershiptos.sCustomerNumber + ", "
	        		+ SMTablearcustomershiptos.sShipToCode;
	        ResultSet rs = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	sDBID,
	        	"MySQL",
	        	this.toString() + ".doPost - User: " + sUserID
	        	+ " - "
	        	+ sUserFullName
	        		);
	     	out.println ("<SELECT NAME=\"" + "EditCode" + "\">" );
        	
        	while (rs.next()){
        		String sOptionValue = "<OPTION VALUE=\"";
        		sOptionValue = sOptionValue +
        				clsStringFunctions.PadLeft(rs.getString(
        				SMTablearcustomershiptos.sCustomerNumber).trim()," ", 
        					SMTablearcustomershiptos.sCustomerNumberLength);

        		sOptionValue = sOptionValue +
        				clsStringFunctions.PadLeft(rs.getString(
        				SMTablearcustomershiptos.sShipToCode).trim()," ",
        					SMTablearcustomershiptos.sShipToCodeLength);
        		
        		sOptionValue = sOptionValue + "\">";
        		out.println(sOptionValue);
        		out.println(rs.getString(SMTablearcustomershiptos.sCustomerNumber).trim());
        		out.println(" - ");
        		out.println(rs.getString(SMTablearcustomershiptos.sShipToCode).trim());
        		out.println(" - ");
        		out.println(rs.getString(SMTablearcustomershiptos.sDescription).trim());
        	}
        	rs.close();
	        	//End the drop down list:
	        out.println ("</SELECT>");
	        out.println ("<BR>");
		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString() + " class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
		//Display text boxes for the new password and a confirmation:
		out.println("<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Edit Selected " + sObjectName + "' STYLE='width: 2.50in; height: 0.24in'></P>");
		out.println("<P><INPUT TYPE=SUBMIT NAME='SubmitDelete' VALUE='Delete Selected " + sObjectName + "' STYLE='width: 2.50in; height: 0.24in'>");
		out.println("  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmDelete\">");
		
		//Allow the user to enter a customer number here:
		out.println("<BR><B>Add a new ship to location:</B><BR>");
	    String sCustomerCode = "";
	    if (request.getParameter(ARCustomerShipTo.ParamsCustomerNumber) != null){
	    	sCustomerCode = request.getParameter(ARCustomerShipTo.ParamsCustomerNumber);
	    }
		out.println( 
			"<P>Enter Customer number for new ship to: <INPUT TYPE=TEXT NAME=\"" 
			+ ARCustomerShipTo.ParamsCustomerNumber + "\""
			+ " VALUE = \"" + sCustomerCode + "\""
			+ " SIZE=28 MAXLENGTH=" 
			+ Integer.toString(SMTablearcustomer.sCustomerNumberLength) 
			+ " STYLE=\"width: 2.41in; height: 0.25in\">"
			);

		//Link to finder:
		out.println(
			"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
			+ "?ObjectName=Customer"
			+ "&ResultClass=FinderResults"
			+ "&SearchingClass=smar.AREditCustomerShipTos"
			+ "&ReturnField=" + ARCustomerShipTo.ParamsCustomerNumber
			+ "&SearchField1=" + SMTablearcustomer.sCustomerName
			+ "&SearchFieldAlias1=Name"
			+ "&SearchField2=" + SMTablearcustomer.sCustomerNumber
			+ "&SearchFieldAlias2=Customer%20Code"
			+ "&SearchField3=" + SMTablearcustomer.sAddressLine1
			+ "&SearchFieldAlias3=Address%20Line%201"
			+ "&SearchField4=" + SMTablearcustomer.sPhoneNumber
			+ "&SearchFieldAlias4=Phone"
			+ "&ResultListField1="  + SMTablearcustomer.sCustomerNumber
			+ "&ResultHeading1=Customer%20Number"
			+ "&ResultListField2="  + SMTablearcustomer.sCustomerName
			+ "&ResultHeading2=Customer%20Name"
			+ "&ResultListField3="  + SMTablearcustomer.sAddressLine1
			+ "&ResultHeading3=Address%20Line%201"
			+ "&ResultListField4="  + SMTablearcustomer.sPhoneNumber
			+ "&ResultHeading4=Phone"
			+ "&ResultListField5="  + SMTablearcustomer.iActive
			+ "&ResultHeading5=Active"
			+ "&ResultListField6="  + SMTablearcustomer.iOnHold
			+ "&ResultHeading6=On%20Hold"
			//+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "\"> Find customer</A></P>"
			);
		
		out.println("<P><INPUT TYPE=SUBMIT NAME='SubmitAdd' VALUE='Add New " + sObjectName + "' STYLE='width: 2.00in; height: 0.24in'></P>");
		out.println("</FORM>");
		
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}