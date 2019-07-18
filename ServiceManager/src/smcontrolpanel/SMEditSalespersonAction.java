package smcontrolpanel;

import SMDataDefinition.SMTablesalesperson;
import ServletUtilities.clsServletUtilities;
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

import SMClasses.MySQLs;

public class SMEditSalespersonAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Salesperson";
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditSalespersons))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sEditCode = clsStringFunctions.filter(request.getParameter("EditCode"));
		
	    String title = "Updating " + sObjectName + "'" + sEditCode + "'";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	
	    String sOutPut = "";
	
	    String sSQL = MySQLs.Update_Salesperson_SQL(
				sEditCode, 
				clsServletUtilities.ConvertCheckboxResultToString(request.getParameter(SMTablesalesperson.iShowInSalesReport)),
				clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablesalesperson.sSalespersonFirstName)), 
				clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablesalesperson.sSalespersonLastName)), 
				clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablesalesperson.sSalespersonType)), 
				clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablesalesperson.lSalespersonUserID)),
				clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablesalesperson.sSalespersonTitle)),
				clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablesalesperson.sDirectDial)),
				clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablesalesperson.sSalespersonEmail))
				);
	    try{
	    	clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sDBID);
    		sOutPut = "Successfully updated " + sObjectName + ": " + sEditCode + ".";
	    }catch (SQLException ex){
		    sOutPut = "Error - could not update " + sObjectName + " with SQL = " + sSQL + " - " + ex.getMessage() + ".<BR>";
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
