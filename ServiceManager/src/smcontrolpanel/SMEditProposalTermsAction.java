package smcontrolpanel;

import SMDataDefinition.SMTableproposalterms;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SMEditProposalTermsAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Proposal Terms";
	private String sDBID = "";
	private String sCompanyName = "";
	private boolean bDebugMode = false;
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMEditProposalTerms
			)
		){
			return;
		}
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		int iID = Integer.parseInt(request.getParameter(SMTableproposalterms.sID));
		String sProposalTermCode = request.getParameter(SMTableproposalterms.sProposalTermCode);
		String sProposalTermDesc = request.getParameter(SMTableproposalterms.mProposalTermDesc);
		if (bDebugMode){
			System.out.println("[1365711842] sProposalTermDesc = '" + sProposalTermDesc + "'");
		}

		if (sProposalTermCode.trim().compareToIgnoreCase("") == 0){
			out.println("Name cannot be blank - click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}

		if (sProposalTermDesc.trim().compareToIgnoreCase("") == 0){
			out.println("Description cannot be blank - click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}
		
	    String title = "Updating " + sObjectName + "'" + sProposalTermCode + "'";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	
	    String sSQL = "";
	    if (iID < 0){
		    sSQL = "INSERT INTO " + SMTableproposalterms.TableName
		    	+ " (" + SMTableproposalterms.sProposalTermCode
		    	+ ", " + SMTableproposalterms.mProposalTermDesc
		    	+ ", " + SMTableproposalterms.sdaystoaccept
		    	+ ", " + SMTableproposalterms.sdefaultpaymentterms
		    	+ ") VALUES ("
		    	+ " '" + clsDatabaseFunctions.FormatSQLStatement(sProposalTermCode) + "'"
		    	+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sProposalTermDesc) + "'"
		    	+ ", '" + clsDatabaseFunctions.FormatSQLStatement(clsManageRequestParameters.get_Request_Parameter(SMTableproposalterms.sdaystoaccept, request)) + "'"
		    	+ ", '" + clsDatabaseFunctions.FormatSQLStatement(clsManageRequestParameters.get_Request_Parameter(SMTableproposalterms.sdefaultpaymentterms, request)) + "'"
		    	+ ")"
		    	;
	    }else{
	    	sSQL = "UPDATE " + SMTableproposalterms.TableName
	    		+ " SET" 
	    	    + " " + SMTableproposalterms.sProposalTermCode + " = '" + clsDatabaseFunctions.FormatSQLStatement(sProposalTermCode) + "'"
	    		+ ", " + SMTableproposalterms.mProposalTermDesc + " = '" + clsDatabaseFunctions.FormatSQLStatement(sProposalTermDesc) + "'"
	    		+ ", " + SMTableproposalterms.sdaystoaccept + " = '" 
	    			+ clsDatabaseFunctions.FormatSQLStatement(clsManageRequestParameters.get_Request_Parameter(SMTableproposalterms.sdaystoaccept, request)) + "'"
	    		+ ", " + SMTableproposalterms.sdefaultpaymentterms + " = '" 
	    			+ clsDatabaseFunctions.FormatSQLStatement(clsManageRequestParameters.get_Request_Parameter(SMTableproposalterms.sdefaultpaymentterms, request)) + "'"
	    		+ " WHERE" 
	    		+ " " + SMTableproposalterms.sID + " = " + iID 
	    		;
	    }
		if (bDebugMode){
			System.out.println("[1365711843] sSQL = '" + sSQL + "'");
		}
	    try{
	    	clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sDBID);
	    }catch (SQLException ex){
	    	out.println("Error updating proposal terms with SQL: " + sSQL
	    		+ " - " + ex.getMessage());
			//System.out.println("Error updating proposal terms with SQL: " + sSQL
		    //		+ " - " + ex.getMessage());
			out.println("</BODY></HTML>");
			return;
		}
	    out.println("Successfully updated proposal terms " + sProposalTermCode + ".");
	    out.println("</BODY></HTML>");
	}

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
