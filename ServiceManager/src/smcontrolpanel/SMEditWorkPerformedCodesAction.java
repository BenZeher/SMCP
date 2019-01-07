package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTableworkperformedcodes;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;

public class SMEditWorkPerformedCodesAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private String sDBID = "";
	private String sCompanyName = "";
	private static String sObjectName = "Work Performed Code";
	
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditWorkPerformedCodes))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sEditCode = clsStringFunctions.filter(request.getParameter("EditCode"));
		
	    String title = "Updating " + sObjectName +  " " + 
	    	clsStringFunctions.StringRight(sEditCode, SMTableworkperformedcodes.sWorkPerformedCodeLength) + "."; 
	    
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	
	    String sOutPut = "Work performed code was saved successfully";
	    String sWPCode = clsStringFunctions.StringRight(sEditCode, SMTableworkperformedcodes.sWorkPerformedCodeLength).trim();
	    String sServiceType = clsStringFunctions.StringLeft(sEditCode, SMTableworkperformedcodes.sCodeLength).trim();
		try {
			saveCode(
				sServiceType, 
				sWPCode, 
				clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTableworkperformedcodes.iSortOrder)), 
				clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTableworkperformedcodes.sWorkPerformedPhrase)), 
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME)
			);
		} catch (Exception e) {
			sOutPut = e.getMessage();
		}
	    
	    out.println(sOutPut);
	    out.println("</BODY></HTML>");
	}
	private void saveCode(
			String sServiceType, 
			String sWPCode, 
			String sSortOrder, 
			String sWorkPerformedPhrase,
			String sUser) throws Exception{
	    String sSQL = SMMySQLs.Update_WorkPerformedCode_SQL(
	    		sServiceType, 
	    		sWPCode, 
	    		sSortOrder, 
	    		sWorkPerformedPhrase
	    		);
	    Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".doPost - user: " + sUser);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			throw new Exception("Error getting database connection - " + e.getMessage() + " - work performed code could not be saved.");
		}
		
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(sSQL);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			throw new Exception("Error - work performed code could not be saved - " + e.getMessage() + ".");
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn);
	}
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
