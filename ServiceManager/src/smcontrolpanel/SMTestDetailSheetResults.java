package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.*;
import ServletUtilities.clsManageRequestParameters;

public class SMTestDetailSheetResults extends HttpServlet {
	public static final String DETAIL_FORM_RESULT_TEXT = "DETAILFORMRESULTTEXT";
	private static final long serialVersionUID = 1L;
	//private static SimpleDateFormat USTimeOnlyformatter = new SimpleDateFormat("hh:mm:ss a");
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				-1L))
		{
			return;
		}

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
    	String sResultText = clsManageRequestParameters.get_Request_Parameter(DETAIL_FORM_RESULT_TEXT, request);

		out.println(SMUtilities.SMCPTitleSubBGColor(
				"Custom Detail Sheet Test Results", 
				"", 
				SMUtilities.getInitBackGroundColor(getServletContext(), sDBID),
				sCompanyName));
    	out.println("<BR>");
    	out.println("<B>Click the 'Back' button in your browser to return</B><BR>");
		out.println("<FORM NAME='MAINFORM' ACTION='" 
				+ SMUtilities.getURLLinkBase(getServletContext()) 
				+ "dummy' METHOD='POST'>");
    	
		out.println("<TEXTAREA NAME=\"" + SMTableworkorderdetailsheets.mtext + "\""
			//+ " rows=" + SMWorkOrderHeader.NUMBER_OF_DETAIL_SHEET_ROWS_DISPLAYED
			+ " rows=30"
			+ " cols=120"
			//+ "style=\"width:100%\""
			+ ">"
			+ sResultText.trim().replace("\"", "&quot;")
			+ "</TEXTAREA>"
		);
		out.println("</FORM>");
		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
