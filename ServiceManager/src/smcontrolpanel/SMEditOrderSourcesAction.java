package smcontrolpanel;

import SMDataDefinition.SMTableordersources;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SMEditOrderSourcesAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Order Source";
	private String sDBID = "";
	private String sCompanyName = "";
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditOrderSources))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sEditCode = clsStringFunctions.filter(request.getParameter("EditCode"));
		
	    String title = "Updating " + sObjectName + "'" + sEditCode + "'";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	
	    String sOutPut = "";
	
	    String sSQL = SMMySQLs.Update_OrderSource_SQL(
	    		sEditCode,
	    		clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTableordersources.sSourceDesc))
				);
	    
	    //System.out.println("SQL = " + sSQL);
		
	    try{
	    	if (clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sDBID) == false){
	    		sOutPut = "Could not complete update transaction - " + sObjectName + " was not updated.<BR>";
	    	}else{
	    		sOutPut = "Successfully updated " + sObjectName + ": " + sEditCode + ".";
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
