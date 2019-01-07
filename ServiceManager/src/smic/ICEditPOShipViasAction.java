package smic;

import java.io.IOException;
import java.io.PrintWriter;
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
import SMDataDefinition.SMTableicshipvias;
import ServletUtilities.clsDatabaseFunctions;

public class ICEditPOShipViasAction extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private static String sObjectName = "PO Ship Via";
	private String sCompanyName = "";
	private String sDBID = "";
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.ICEditPOShipVias)){
	    	return;
	    }

		String sEditCode = request.getParameter("EditCode");
		
	    String title = "Updating " + sObjectName + "'" + sEditCode + "'";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICEditPOShipVias) 
	    		+ "\">Summary</A><BR><BR>");
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID); 
	
	    String sOutPut = "PO Ship Via successfully updated";
	
	    String sSQL = "UPDATE " 
	    	+ SMTableicshipvias.TableName
			+ " SET " 
			+ SMTableicshipvias.sshipvianame + " = '" + request.getParameter(SMTableicshipvias.sshipvianame) + "'"
			+ " WHERE " + SMTableicshipvias.sshipviacode + " = '" + request.getParameter("EditCode") + "'";
		
	    try{
	    	clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sDBID);
	    }catch (SQLException ex){
		    sOutPut = "Error - could not update " + sObjectName + " with SQL: " + sSQL + " - " + ex.getMessage() + ".<BR>";
		}
	    
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
