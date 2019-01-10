package smgl;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMTableapcheckforms;
import SMDataDefinition.SMTableglstatementforms;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLViewStatementForm extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.GLEditFinancialStatements
		)
		){
			return;
		}

		String sFinancialStatementFormID = clsManageRequestParameters.get_Request_Parameter(SMTableglstatementforms.lid, request);
		//String sCalledClassName = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), -1, request)){return;}

		String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + clsManageRequestParameters.get_Request_Parameter("CallingClass", request)
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&" + SMTableapcheckforms.lid + "=" + sFinancialStatementFormID
				+ "&CallingClass=" + clsManageRequestParameters.get_Request_Parameter("CallingClass", request)
				+ "&" + SMTableapcheckforms.lid + "=" + request.getParameter(SMTableglstatementforms.lid)
				+ "&" + GLEditFinancialStatementsEdit.BUTTON_SUBMIT_ADD + "=" + clsManageRequestParameters.get_Request_Parameter(GLEditFinancialStatementsEdit.BUTTON_SUBMIT_ADD, request)
				+ "&" + GLEditFinancialStatementsEdit.BUTTON_SUBMIT_EDIT + "=" + clsManageRequestParameters.get_Request_Parameter(GLEditFinancialStatementsEdit.BUTTON_SUBMIT_EDIT, request)
				+ "&" + GLEditFinancialStatementsEdit.BUTTON_SUBMIT_DELETE + "=" + clsManageRequestParameters.get_Request_Parameter(GLEditFinancialStatementsEdit.BUTTON_SUBMIT_DELETE, request)
			;
		
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(getServletContext(), smaction.getsDBID(), "MySQL", SMUtilities.getFullClassName(this.toString()));
		} catch (Exception e1) {
			sRedirectString += 
				"&" + "Warning" + "=" + "Error [1534454844] getting data connection - " + e1.getMessage()
			;
			redirectProcess(sRedirectString, response);
			return;
		}
		
		out.println("<HTML><BODY>");
		
		
		try{
			out.println(GLStatementFormProcessor.printStatement(conn, sFinancialStatementFormID, true, "1", "1"));
		} catch (Exception e) {
			sRedirectString += 
				"&" + "Warning" + "=" + "Error [1536083725] displaying GL statement form - " + e.getMessage()
			;
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080759]");
			redirectProcess(sRedirectString, response);
			return;
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080760]");
		out.println("</BODY></HTML>");
		
	}
	
	private void redirectProcess(String sRedirectString, HttpServletResponse res ){
		try {
			res.sendRedirect(sRedirectString);
		} catch (IOException e1) {
			System.out.println("Error [1534454845] in " + this.toString() + ".redirectAction - IOException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
			return;
		} catch (IllegalStateException e1) {
			System.out.println("Error [1534454846] in " + this.toString() + ".redirectAction - IllegalStateException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
			return;
		}
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
