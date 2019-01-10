package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import SMDataDefinition.SMTableservicetypes;
import ServletUtilities.clsDatabaseFunctions;

public class SMEditServiceTypeAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Service Type";
	private String sDBID = "";
	private String sCompanyName = "";
	private String sUserID = "";
	private String sUserFirstName = "";
	private String sUserLastName = "";
	//private boolean bDebugMode = true;
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMEditServiceTypes
			)
		){
			return;
		}
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFirstName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    sUserLastName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    
		long lID = Long.parseLong(request.getParameter(SMTableservicetypes.id));
		String sWorkOrderTerms = request.getParameter(SMTableservicetypes.mworkorderterms);
		String sWorkOrderReceiptComment = request.getParameter(SMTableservicetypes.mworeceiptcomment);
		
	    String title = "Updating " + sObjectName;
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "\">Return to user login</A><BR><BR>");
	
	    Connection conn = clsDatabaseFunctions.getConnection(
	    		getServletContext(), 
	    		sDBID, 
	    		"MySQL", 
	    		this.toString() 
	    		+ ".doPost - user: " 
	    		+ sUserID
	    		+ " - "
	    		+ sUserFirstName
	    		+ " "
	    		+ sUserLastName);
	    if (conn == null){
	    	out.println("Error getting database connection");
			out.println("</BODY></HTML>");
			return;
	    }
	    
	    if (!clsDatabaseFunctions.start_data_transaction(conn)){
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080540]");
	    	out.println("Error starting database transaction");
			out.println("</BODY></HTML>");
			return;
	    }
	    
    	String sSQL = "UPDATE " + SMTableservicetypes.TableName
    		+ " SET" 
    	    + " " + SMTableservicetypes.mworkorderterms + " = '" + clsDatabaseFunctions.FormatSQLStatement(sWorkOrderTerms) + "'"
    	    + ", " + SMTableservicetypes.mworeceiptcomment + " = '" + clsDatabaseFunctions.FormatSQLStatement(sWorkOrderReceiptComment) + "'" 	    
    		+ " WHERE" 
    		+ " " + SMTableservicetypes.id + " = " + lID 
    		;
	    try{
	    	clsDatabaseFunctions.executeSQL(sSQL, conn);
	    }catch (SQLException ex){
	    	clsDatabaseFunctions.rollback_data_transaction(conn);
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080541]");
	    	out.println("Error updating service types with SQL: " + sSQL
	    		+ " - " + ex.getMessage());
			out.println("</BODY></HTML>");
			return;
		}
	    if (!clsDatabaseFunctions.commit_data_transaction(conn)){
	    	clsDatabaseFunctions.rollback_data_transaction(conn);
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080542]");
	    	out.println("Unable to commit data transaction.");
				out.println("</BODY></HTML>");
				return;
	    }
	    clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080543]");
	    out.println("Successfully updated service type.");
	    out.println("</BODY></HTML>");
	}

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
