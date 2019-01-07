package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import SMDataDefinition.SMTableproposalphrasegroups;
import ServletUtilities.clsDatabaseFunctions;

public class SMProposalPhraseGroupAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Proposal Phrase Group";
	private String sDBID = "";
	private String sCompanyName = "";
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
				SMSystemFunctions.SMEditProposalPhraseGroups
			)
		){
			return;
		}
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    int iID = Integer.parseInt(request.getParameter(SMTableproposalphrasegroups.sid));
		String sProposalPhraseGroupName = request.getParameter(SMTableproposalphrasegroups.sgroupname);
		if (sProposalPhraseGroupName.trim().compareToIgnoreCase("") == 0){
			out.println("Name cannot be blank - click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}

	    String title = "Updating " + sObjectName + "'" + sProposalPhraseGroupName + "'";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	
	    String sSQL = "";
	    if (iID < 0){
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
	    		+ " " + SMTableproposalphrasegroups.sid + " = " + iID 
	    		;
	    }
	    try{
	    	clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sDBID);
	    }catch (SQLException ex){
	    	out.println("Error updating proposal phrase groups with SQL: " + sSQL
	    		+ " - " + ex.getMessage());
			out.println("</BODY></HTML>");
			return;
		}
	    out.println("Successfully updated proposal phrase group " + sProposalPhraseGroupName + ".");
	    out.println("</BODY></HTML>");
	}

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
