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

import SMDataDefinition.SMTableproposalphrases;
import ServletUtilities.clsDatabaseFunctions;

public class SMProposalPhrasesAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static final String sObjectName = "Proposal Phrase";
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMEditProposalPhrases
			)
		){
			return;
		}
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFirstName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    String sUserLastName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    
	    long lID = Long.parseLong(request.getParameter(SMTableproposalphrases.sid));
		String sProposalPhraseName = request.getParameter(SMTableproposalphrases.sproposalphrasename);
		String sProposalPhrase = request.getParameter(SMTableproposalphrases.mproposalphrase);
		String sProposalPhraseGroupID = request.getParameter(SMTableproposalphrases.iphrasegroupid);
		int iSortOrder = 0;
		try {
			iSortOrder = Integer.parseInt(request.getParameter(SMTableproposalphrases.isortorder));
		} catch (NumberFormatException e) {
			out.println("Sort order '" + request.getParameter(SMTableproposalphrases.isortorder) + "' is invalid; it must be a whole integer - click 'Back' to correct.");
			out.println("</BODY></HTML>");
		}
		if (sProposalPhraseName.trim().compareToIgnoreCase("") == 0){
			out.println("Name cannot be blank - click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}

		if (sProposalPhrase.trim().compareToIgnoreCase("") == 0){
			out.println("Phrase cannot be blank - click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}
		
		if (sProposalPhrase.trim().compareToIgnoreCase("") == 0){
			out.println("Phrase cannot be blank - click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}
		
		if (sProposalPhraseGroupID.trim().compareToIgnoreCase("") == 0){
			out.println("You must select a proposal phrase group - click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}
		
	    String title = "Updating " + sObjectName + "'" + sProposalPhraseName + "'";
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
	    		+ sUserLastName
	    		);
	    if (conn == null){
	    	out.println("Error getting database connection");
			out.println("</BODY></HTML>");
			return;
	    }
	    
	    if (!clsDatabaseFunctions.start_data_transaction(conn)){
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080636]");
	    	out.println("Error starting database transaction");
			out.println("</BODY></HTML>");
			return;
	    }
	    
	    String sSQL = "";
	    if (lID < 0){
		    sSQL = "INSERT INTO " + SMTableproposalphrases.TableName
		    	+ " (" + SMTableproposalphrases.mproposalphrase
		    	+ ", " + SMTableproposalphrases.sproposalphrasename
		    	+ ", " + SMTableproposalphrases.iphrasegroupid
		    	+ ", " + SMTableproposalphrases.isortorder
		    	+ ") VALUES ("
		    	+ " '" + clsDatabaseFunctions.FormatSQLStatement(sProposalPhrase) + "'"
		    	+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sProposalPhraseName) + "'"
		    	+ ", " +  sProposalPhraseGroupID
		    	+ ", " +  Integer.toString(iSortOrder)
		    	+ ")"
		    	;
	    }else{
	    	sSQL = "UPDATE " + SMTableproposalphrases.TableName
	    		+ " SET" 
	    	    + " " + SMTableproposalphrases.mproposalphrase + " = '" + clsDatabaseFunctions.FormatSQLStatement(sProposalPhrase) + "'"
	    		+ ", " + SMTableproposalphrases.sproposalphrasename + " = '" + clsDatabaseFunctions.FormatSQLStatement(sProposalPhraseName) + "'"
	    		+ ", " + SMTableproposalphrases.iphrasegroupid + " = " + sProposalPhraseGroupID
	    		+ ", " + SMTableproposalphrases.isortorder + " = " + Integer.toString(iSortOrder)
	    		+ " WHERE" 
	    		+ " " + SMTableproposalphrases.sid + " = " + lID 
	    		;
	    }
	    try{
	    	clsDatabaseFunctions.executeSQL(sSQL, conn);
	    }catch (SQLException ex){
	    	clsDatabaseFunctions.rollback_data_transaction(conn);
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080637]");
	    	out.println("Error updating proposal phrases with SQL: " + sSQL
	    		+ " - " + ex.getMessage());
			out.println("</BODY></HTML>");
			return;
		}
	    
	    //Now get the last insert ID:
	    if (lID < 0){
			sSQL = "SELECT LAST_INSERT_ID()";
			try {
				java.sql.ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
				if (!rs.next()){
			    	clsDatabaseFunctions.rollback_data_transaction(conn);
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080638]");
			    	out.println("Error getting last insert ID with SQL: " + sSQL);
					out.println("</BODY></HTML>");
					return;
				}else{
					lID = rs.getLong(1);
					rs.close();
				}
			} catch (SQLException e) {
		    	clsDatabaseFunctions.rollback_data_transaction(conn);
		    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080639]");
		    	out.println("Error getting last insert ID with SQL: " + sSQL);
				out.println("</BODY></HTML>");
				return;
			}
	    }
	    
	    //Now if a sort order was saved that is a DUPLICATE on another phrase, we'll want to bump all the subsequent sort orders down one:
	    boolean bNeedsReOrdering = false;
	    sSQL = "SELECT"
	    	+ " COUNT(*)"
	    	+ " FROM " + SMTableproposalphrases.TableName
	    	+ " WHERE ("
	    		+ "(" + SMTableproposalphrases.isortorder + " = " + Integer.toString(iSortOrder) + ")"
	    	+ ")"
	    ;
	    try {
			java.sql.ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
			rs.first();
			if (rs.getInt(1) > 1){
				bNeedsReOrdering = true;
			}
			rs.close();
		} catch (SQLException e) {
	    	clsDatabaseFunctions.rollback_data_transaction(conn);
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080640]");
	    	out.println("Error checking for duplcated sort orders with SQL: " + sSQL
	    		+ " - " + e.getMessage());
			out.println("</BODY></HTML>");
			return;
		}
	    
	    if (bNeedsReOrdering){
	    	sSQL = "UPDATE " + SMTableproposalphrases.TableName
	    		+ " SET " + SMTableproposalphrases.isortorder
	    		+ " = " + SMTableproposalphrases.isortorder + " + 1"
	    		+ " WHERE ("
	    			+ "(" + SMTableproposalphrases.isortorder + " >= " + Integer.toString(iSortOrder) + ")"
	    			+ " AND (" + SMTableproposalphrases.sid + " != " + Long.toString(lID) + ")"
	    		+ ")"
	    	;
	    
		    try{
		    	clsDatabaseFunctions.executeSQL(sSQL, conn);
		    }catch (SQLException ex){
		    	clsDatabaseFunctions.rollback_data_transaction(conn);
		    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080641]");
		    	out.println("Error updating subsequent sort orders with SQL: " + sSQL
		    		+ " - " + ex.getMessage());
				out.println("</BODY></HTML>");
				return;
			}
	    }
	    if (!clsDatabaseFunctions.commit_data_transaction(conn)){
	    	clsDatabaseFunctions.rollback_data_transaction(conn);
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080642]");
	    	out.println("Unable to commit data transaction.");
				out.println("</BODY></HTML>");
				return;
	    }
	    clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080643]");
	    out.println("Successfully updated proposal phrases " + sProposalPhraseName + ".");
	    out.println("</BODY></HTML>");
	}
	

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
