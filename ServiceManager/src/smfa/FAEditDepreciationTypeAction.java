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
import SMDataDefinition.SMTablefadepreciationtype;
import ServletUtilities.clsDatabaseFunctions;


public class FAEditDepreciationTypeAction extends HttpServlet{
	private static final long serialVersionUID = 1L;
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		String sDepreciationTypeObjectName = "Depreciation Type";
		
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.FAEditDepreciationType)){
	    	return;
	    }

		String sEditCode = request.getParameter("EditCode");
		
	    String title = "Updating " + sDepreciationTypeObjectName + "'" + sEditCode + "'";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Fixed Assets Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICEditItems) 
	    		+ "\">Summary</A><BR><BR>");
	
	    String sOutPut = "";
	
	    String sSQL = "UPDATE " + SMTablefadepreciationtype.TableName
					+ " SET " 
					+ SMTablefadepreciationtype.sCalculationType + " = '" + request.getParameter(SMTablefadepreciationtype.sCalculationType) + "', "
					+ SMTablefadepreciationtype.iLifeInMonths + " = " + request.getParameter(SMTablefadepreciationtype.iLifeInMonths)
					
					+ " WHERE " + SMTablefadepreciationtype.sDepreciationType+ " = '" + request.getParameter("EditCode") + "'";
		
	    try{
	    	if (clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sDBID) == false){
	    		sOutPut = "Could not complete update transaction - " + sDepreciationTypeObjectName + " was not updated.<BR>";
	    	}else{
	    		sOutPut = "Successfully updated " + sDepreciationTypeObjectName + ": " + sEditCode + ".";
	    	}
	    }catch (SQLException ex){
			System.out.println("[1579189411] Error in " + this.toString() + " class!!");
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("SQL: " + ex.getErrorCode());
		    sOutPut = "Error - could not update " + sDepreciationTypeObjectName + ".<BR>";
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
