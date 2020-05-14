package smcontrolpanel;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMTableproposalphrases;
import ServletUtilities.clsDatabaseFunctions;

public class SMProposalPhrasesAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static final String sObjectName = "Proposal Phrase";
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMEditProposalPhrases)){return;}
		smaction.setCallingClass("smcontrolpanel.SMProposalPhrasesEdit");
	    
	    long lID = Long.parseLong(request.getParameter(SMTableproposalphrases.sid));
		String sProposalPhraseName = request.getParameter(SMTableproposalphrases.sproposalphrasename);
		String sProposalPhrase = request.getParameter(SMTableproposalphrases.mproposalphrase);
		String sProposalPhraseGroupID = request.getParameter(SMTableproposalphrases.iphrasegroupid);
		
		int iSortOrder = 0;
		try {
			iSortOrder = Integer.parseInt(request.getParameter(SMTableproposalphrases.isortorder));
		} catch (NumberFormatException e) {
			//out.println("Sort order '" + request.getParameter(SMTableproposalphrases.isortorder) + "' is invalid; it must be a whole integer - click 'Back' to correct.");

		}

		if (sProposalPhraseName.trim().compareToIgnoreCase("") == 0){
			//out.println("Name cannot be blank - click 'Back' to correct.");
			return;
		}

		if (sProposalPhrase.trim().compareToIgnoreCase("") == 0){
			//out.println("Phrase cannot be blank - click 'Back' to correct.");
			return;
		}
		
		if (sProposalPhrase.trim().compareToIgnoreCase("") == 0){
			//out.println("Phrase cannot be blank - click 'Back' to correct.");
			return;
		}
		
		if (sProposalPhraseGroupID.trim().compareToIgnoreCase("") == 0){
			//out.println("You must select a proposal phrase group - click 'Back' to correct.");
			return;
		}
	
		//Get connection
	    Connection conn = clsDatabaseFunctions.getConnection(
	    		getServletContext(), 
	    		smaction.getsDBID(), 
	    		"MySQL", 
	    		this.toString() 
	    		+ ".doPost - user: " 
	    		+ smaction.getFullUserName()
	    		);
	    if (conn == null){
	    	smaction.redirectAction("Error getting connection", "", 
	    			"&" + SMTableproposalphrases.sproposalphrasename + "=" + Long.toString(lID) + "&SubmitEdit=Y");
			return;
	    }
	    
		
		//if it a request to update
		if(request.getParameter("SubmitEdit") != null) {
			//Start transaction
			if (!clsDatabaseFunctions.start_data_transaction(conn)){
		    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080636]");
		    	smaction.redirectAction("Error starting transaction", "", 
		    	"&" + SMTableproposalphrases.sproposalphrasename + "=" + Long.toString(lID)+ "&SubmitEdit=Y");
				return;
		    }
			
			try {
				//Update or insert the proposal phrase
				updateProposalPhrase(conn, iSortOrder, lID, sProposalPhrase, sProposalPhraseName, sProposalPhraseGroupID);
				
				//Get the last insert ID.
			    if(lID < 0) {
			    	try {
						lID = clsDatabaseFunctions.getLastInsertID(conn);
					} catch (Exception e) {
						
					}
			    }
			} catch (Exception e) {
				smaction.redirectAction("Error updating proposalphrase" + e.getMessage(), "", 
						"&" + SMTableproposalphrases.sproposalphrasename + "=" + Long.toString(lID) + "&SubmitEdit=Y");
				return;
			}
			
		    
		    //Now we update the sort oder
		    try {
				updateSortOrder(conn, iSortOrder, lID);
			} catch (Exception e) {
				smaction.redirectAction("Error updating sort order of proposal phrase" + e.getMessage(), "", 
						"&" + SMTableproposalphrases.sproposalphrasename + "=" + Long.toString(lID)+ "&SubmitEdit=Y");
				return;
			}
			
		    //commit transaction
			if (!clsDatabaseFunctions.commit_data_transaction(conn)){
				clsDatabaseFunctions.rollback_data_transaction(conn);
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080642]");
				smaction.redirectAction("Error updating poposal phrase","",
						"&" + SMTableproposalphrases.sproposalphrasename + "=" + Long.toString(lID)+ "&SubmitEdit=Y");
				return;
	    }
			
			smaction.redirectAction("", "Update successfull",
					"&" + SMTableproposalphrases.sproposalphrasename + "=" + Long.toString(lID)+ "&SubmitEdit=Y");
			return;
		}
		
		//if its a request to delete
		if(request.getParameter("DeleteEdit") != null) {
			try {
				deleteProposalPhrase(conn, lID);
			} catch (Exception e) {
				smaction.redirectAction("Error deleting proposal phrase" + e.getMessage(), "", 
						"&" + SMTableproposalphrases.sproposalphrasename + "=" + Long.toString(lID)+ "&SubmitEdit=Y");
				return;
			}
			smaction.setCallingClass("smcontrolpanel.SMProposalPhraseGroupSelect");
			smaction.redirectAction("", "Delete successful", 
					"&" + SMTableproposalphrases.sproposalphrasename + "=" + Long.toString(lID)+ "&SubmitEdit=Y");
			return;
		}
	    //Free connection
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1589392293]");
		smaction.redirectAction("", "", 
				"&" + SMTableproposalphrases.sproposalphrasename + "=" + Long.toString(lID)+ "&SubmitEdit=Y");
	    
	    return;
	}
	
	
	private void updateProposalPhrase(
			Connection conn, 
			int iSortOrder, 
			Long lID, 
			String sProposalPhrase, 
			String sProposalPhraseName, 
			String sProposalPhraseGroupID) throws Exception{
	   
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
	    	throw new Exception("Error executing SQL: " + sSQL + ": " + ex.getMessage() );
		}
	    
		return;
	}
		
	private void deleteProposalPhrase(Connection conn, Long lID) throws Exception{
		String sSQL = "DELETE FROM " + SMTableproposalphrases.TableName
				+ " WHERE ("
    			+ " (" + SMTableproposalphrases.sid + " = " + Long.toString(lID) + ")"
    			+ ")"
    			;
    
			try{
				clsDatabaseFunctions.executeSQL(sSQL, conn);
			}catch (Exception ex){
	    		throw new Exception("Error deleting proposal phrase with SQL: " + ex.getMessage());
			}
	}

	private void updateSortOrder(Connection conn, int iSortOrder, Long lID) throws Exception{
		//If a sort order was saved that is a DUPLICATE on another phrase, we'll want to bump all the subsequent sort orders down one:
		boolean bNeedsReOrdering = false;
		String  sSQL = "SELECT"
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
	    		return;
			}
		}
}

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
