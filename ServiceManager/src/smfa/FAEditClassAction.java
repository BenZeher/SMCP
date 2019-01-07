package smfa;

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
import SMDataDefinition.SMTablefaclasses;
import ServletUtilities.clsDatabaseFunctions;

public class FAEditClassAction extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Class";
	private String sCompanyName = "";
	private String sDBID = "";
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.FAEditClasses)){
	    	return;
	    }
		
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    

		String sEditCode = request.getParameter("EditCode");
		
	    String title = "Updating " + sObjectName + "'" + sEditCode + "'";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Fixed Assets Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.FAEditClasses) 
	    		+ "\">Summary</A><BR><BR>");
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID); 
	
	    String sOutPut = "";
	
	    String sSQL = "UPDATE " + SMTablefaclasses.TableName
					+ " SET " 
					+ SMTablefaclasses.sClassDescription + " = '" + request.getParameter(SMTablefaclasses.sClassDescription) + "'"
					+ " WHERE " + SMTablefaclasses.sClass + " = '" + request.getParameter("EditCode") + "'";
		
	    try{
	    	if (clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sDBID) == false){
	    		sOutPut = "Could not complete update transaction - " + sObjectName + " was not updated.<BR>";
	    	}else{
	    		sOutPut = "Successfully updated " + sObjectName + ": '" + sEditCode + "'.";
	    	}
	    }catch (SQLException ex){
			System.out.println("Error in " + this.toString() + " class!!");
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("SQL: " + ex.getErrorCode());
		    sOutPut = "Error - could not update " + sObjectName + ".<BR>";
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
