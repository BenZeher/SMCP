package smcontrolpanel;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMTableproposalphrasegroups;
import SMDataDefinition.SMTableproposalphrases;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;


public class SMProposalPhraseGroupAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMEditProposalPhraseGroups)){return;}
		smaction.setCallingClass("smcontrolpanel.SMProposalPhraseGroupEdit");
		
		//Get parameters
	    String sProposalPhraseGroupID = clsManageRequestParameters.get_Request_Parameter(SMTableproposalphrasegroups.sid, request);
	    if(sProposalPhraseGroupID.compareToIgnoreCase("") == 0) {
	    	sProposalPhraseGroupID = "-1";
	    }
		String sProposalPhraseGroupName = clsManageRequestParameters.get_Request_Parameter(SMTableproposalphrasegroups.sgroupname, request);

		//If it's sorting lines process new line numbers:
	    if((clsManageRequestParameters.get_Request_Parameter(
	        	"COMMANDFLAG", request).compareToIgnoreCase(SMProposalPhraseGroupEdit.SORT_LINE_COMMAND_VALUE)) == 0){

	    	if(!updateProposalSortOrderAfterSorting(
	    			sProposalPhraseGroupID,
	    			request,
	    			getServletContext(),
	    			smaction.getsDBID(), 
					smaction.getUserID())) {

				smaction.redirectAction(
					"[1589219955] - Could not sort proposals", 
					"", 
					SMTableproposalphrasegroups.sid + "=" + sProposalPhraseGroupID
					+ "&SubmitEdit=Y"
					);
	    		
	    	}else {
				smaction.redirectAction(
					"", 
					"Proposal sort order changed successfully.",
					SMTableproposalphrasegroups.sid + "=" + sProposalPhraseGroupID
					+ "&SubmitEdit=Y"
					 );
				     
	    	}
			return;    	
	    }
	    
	    //Otherwise, its a request to update the proposal group name or create a new proposal group
	    
	    Connection conn = clsDatabaseFunctions.getConnection(
	    		getServletContext(), 
	    		smaction.getsDBID(), 
				"MySQL", 
				this.toString() + " - user: " +  smaction.getFullUserName()
		);
		if (conn == null){
			smaction.redirectAction(
					"[1589219956] - Error getting connection. ", 
					"",
					SMTableproposalphrasegroups.sid + "=" + sProposalPhraseGroupID
					+ "&SubmitEdit=Y"
					 );
				     return;
		}

	    String sSQL = "";
	    if (Integer.parseInt(sProposalPhraseGroupID) < 0){
		    sSQL = "INSERT INTO " + SMTableproposalphrasegroups.TableName
		    	+ " (" + SMTableproposalphrasegroups.sgroupname
		    	+ ") VALUES ("
		    	+ "'" + clsDatabaseFunctions.FormatSQLStatement(sProposalPhraseGroupName) + "'"
		    	+ ")"
		    	;
	    }else{
	    	sSQL = "UPDATE " + SMTableproposalphrasegroups.TableName
	    		+ " SET" 
	    	    + " " + SMTableproposalphrasegroups.sgroupname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sProposalPhraseGroupName) + "'"
	    		+ " WHERE" 
	    		+ " " + SMTableproposalphrasegroups.sid + " = " + sProposalPhraseGroupID 
	    		;
	    }
	    
	    try{
	    	clsDatabaseFunctions.executeSQL(sSQL, conn );
	    }catch (SQLException ex){
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1589220091]");
	    	smaction.redirectAction(
	    			"[1589219958] - Error saving proposal group.", 
					"",
					SMTableproposalphrasegroups.sid + "=" + sProposalPhraseGroupID
					+ "&SubmitEdit=Y"
					 );
			return;
		}
	    //Get the last insert ID.
	    if(Integer.parseInt(sProposalPhraseGroupID) < 0) {
	    	try {
				sProposalPhraseGroupID = Long.toString(clsDatabaseFunctions.getLastInsertID(conn));
			} catch (Exception e) {
				
			}
	    }
	    
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1589220090]");
	    smaction.redirectAction(
    			"", 
    			"Proposal group updated successfully.",
				SMTableproposalphrasegroups.sid + "=" + sProposalPhraseGroupID
				+ "&SubmitEdit=Y"
				 );
	    return;
	}


	  public boolean updateProposalSortOrderAfterDeletion(String sProposalPhraseGroupID, Connection conn){
	    	String rsSQL = "SELECT"
	    		+ " " + SMTableproposalphrases.sid
	    		+ " FROM " + SMTableproposalphrases.TableName
	    		+ " WHERE ("
	    			+ "(" + SMTableproposalphrases.iphrasegroupid + " = " + sProposalPhraseGroupID + ")"
	    		+ ")"
	    		+ " ORDER BY " + SMTableproposalphrases.isortorder
	    		;
	    	long iLineNumber = 0;
	    	try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(rsSQL, conn);
				while (rs.next()){
					iLineNumber++;
					String SQL = "UPDATE " + SMTableproposalphrases.TableName
						+ " SET " + SMTableproposalphrases.isortorder + " = " + Long.toString(iLineNumber)
						+ " WHERE ("
							+ "(" + SMTableproposalphrases.sid + " = " + rs.getLong(SMTableproposalphrases.sid) + ")"
						+ ")"
					;
					try{
					    Statement stmt = conn.createStatement();
					    stmt.executeUpdate(SQL);
					}catch (Exception ex) {
						System.out.println("Error updating porposal phrase line number with SQL: " + SQL + " - " + ex.getMessage());
						return false;
					}
				}
				rs.close();
			} catch (SQLException e) {
				System.out.println("Error reading porposal phrase line numbers with SQL: " + rsSQL + " - " + e.getMessage());
				return false;
			}
	    	
	    	return true;
	    }
	    
		public boolean updateProposalSortOrderAfterSorting(
				String sProposalPhraseGroupID, 
				HttpServletRequest request, 
				ServletContext context, 
				String sDBID,
				String sUserID) {
			
			//Get DB connection
			Connection conn = null;
			try {
				conn = clsDatabaseFunctions.getConnectionWithException(
					context, sDBID, "MySQL", SMUtilities.getFullClassName(this.toString()) + ".updateProposalSortOrderAfterSorting - userID: " + sUserID);
			} catch (Exception e) {
				System.out.println("Error [1588954955] getting connection - " + e.getMessage());
				return false;
			}
			if (!clsDatabaseFunctions.start_data_transaction(conn)){
				clsDatabaseFunctions.freeConnection(context, conn, "[1588954956]");
				System.out.println("Error [1588954958] - could not start data transaction.");
				return false;
			}		
			
			//Clear all line numbers
			String SQL = "UPDATE " + SMTableproposalphrases.TableName 
					+ " SET " + SMTableproposalphrases.isortorder + " = " + SMTableproposalphrases.isortorder + " * -1"
					+ " WHERE " + "(" + SMTableproposalphrases.iphrasegroupid + " =" + sProposalPhraseGroupID + ")";
			
			try {
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			} catch (Exception ex) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				clsDatabaseFunctions.freeConnection(context, conn, "[1588954959]");
				System.out.println("Error updating proposal phrase sort order with SQL: " + SQL + " - " + ex.getMessage());
				return false;
			}
			
			// Get the sort order and update with new   sort number:
			String sOrginalPhraseID = "";
			Enumeration<String> paramNames = request.getParameterNames();
			String sMarker = "PROPOSALID";
			
			while (paramNames.hasMoreElements()) {
				String sParamName = paramNames.nextElement();

				if (sParamName.contains(sMarker)) {

					sOrginalPhraseID = sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length());

					//Update line numbers
					 SQL = "UPDATE " + SMTableproposalphrases.TableName 
							+ " SET " + SMTableproposalphrases.isortorder + " = " + request.getParameter(sParamName) 
							+ " WHERE " + "(" + SMTableproposalphrases.sid + " = " + sOrginalPhraseID + ")";
				
					try {
						Statement stmt = conn.createStatement();
						stmt.executeUpdate(SQL);
					} catch (Exception ex) {
						clsDatabaseFunctions.rollback_data_transaction(conn);
						clsDatabaseFunctions.freeConnection(context, conn, "[1588954960]");
						System.out.println("Error updating porposal phrase sort order with SQL: " + SQL + " - " + ex.getMessage());
						return false;
					}
				}
			}

			if (!clsDatabaseFunctions.commit_data_transaction(conn)){
				clsDatabaseFunctions.rollback_data_transaction(conn);
				clsDatabaseFunctions.freeConnection(context, conn, "[1588954961]");
				System.out.println("Error committing data transaction to update proposal phrase sort order");
				return false;
			}
			
			clsDatabaseFunctions.freeConnection(context, conn, "[1588954962]");
			return true;
	    }
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
