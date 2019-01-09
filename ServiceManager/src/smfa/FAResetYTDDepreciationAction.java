
package smfa;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import ServletUtilities.clsDatabaseFunctions;

public class FAResetYTDDepreciationAction extends HttpServlet {

	private static final long serialVersionUID = 1L;
	//private static String sObjectName = "Depreciation Type";
	//private static String sCalledClassName = "FAEditDepreciationTypeAction";
	private String sDBID = "";
	private String sCompanyName = "";
	private String sUserID = "";
	private String sUserFullName = "";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.FAResetYearToDateDepreciation))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    		+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);

		String title = "";
		String subtitle = "";
		
	    if(request.getParameter("SubmitReset") != null){
			title = "Reset Year-To-Date Depreciation";
		    subtitle = "";
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
		    
		    if (request.getParameter("ConfirmReset") == null){
		    	out.println ("You must check the 'confirming' check box to reset.");
		    }else{

				Connection conn = clsDatabaseFunctions.getConnection(
						getServletContext(), 
						sDBID, 
						"MySQL", 
						this.toString() + ".doPost - user: " + sUserID
						+ " - "
						+ sUserFullName
						+ " [1332276846]");
				if (conn == null){
					response.sendRedirect(
							"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAResetYTDDepreciationSelect"
							+ "?Warning=" + "Could not get data connection."
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					);
					return;
				}

				clsDatabaseFunctions.start_data_transaction(conn);
				try{
					Reset_Depreciation(out, conn);
					
					clsDatabaseFunctions.commit_data_transaction(conn);
				    clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067478]");
					response.sendRedirect(
							"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAResetYTDDepreciationSelect"
							+ "?Status=Successfully reset YTD depreciation."
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					);
					
				}catch(Exception e){
					clsDatabaseFunctions.rollback_data_transaction(conn);
				    clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067479]");
					response.sendRedirect(
							"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAResetYTDDepreciationSelect"
							+ "?Warning=Error reseting YTD depreciation."
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					);
			    }
		    }
	    }
		
		out.println("</BODY></HTML>");
	}
	
	private void Reset_Depreciation(PrintWriter pwOut, Connection conn) throws Exception{
		
		/*
		String SQL = "UPDATE " + SMTablefamaster.TableName + 
	    			 " SET " + SMTablefamaster.bdYTDDepreciation + " = 0";
	    SMUtilities.executeSQL(SQL, conn);
		
		SQL = "UPDATE " + SMTablefamaster.TableName + 
			  " SET " + SMTablefamaster.bdYTDDisposedAmount + " = 0";
		SMUtilities.executeSQL(SQL, conn);
		
		SQL = "UPDATE " + SMTablefamaster.TableName + 
			  " SET " + SMTablefamaster.bdYTDPurchaseAmount + " = 0";
		SMUtilities.executeSQL(SQL, conn);
		*/
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
