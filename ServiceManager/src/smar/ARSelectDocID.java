package smar;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//import SMClasses.*;
import SMDataDefinition.*;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMUtilities;

public class ARSelectDocID extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String sCompanyName = "";
	private static String sDBID = "";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		//This page just accepts a document ID, and validates it or returns to itself . . . 
		
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				-1L))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    //Get the variables for the class:
	    String sBatchNumber = (String) request.getParameter("BatchNumber");

	    String sBatchType = "";
	    if (request.getParameter("BatchType") != null){
	    	sBatchType = request.getParameter("BatchType");
	    }
	    String sDocumentID = "";
	    if (request.getParameter("DocumentID") != null){
	    	sDocumentID = request.getParameter("DocumentID");
	    }
	    String sDocumentType = "";
	    if (request.getParameter("DocumentType") != null){
	    	sDocumentType = request.getParameter("DocumentType");
	    }

	    String sWarning = "";
	    if (request.getParameter("Warning") != null){
	    	sWarning = request.getParameter("Warning");
	    }

	    String title = "Create new " + ARDocumentTypes.Get_Document_Type_Label(Integer.parseInt(sDocumentType)) ;
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, "", SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A><BR><BR>");

	    String sEditorClass = "";
	    if (sDocumentType.equalsIgnoreCase(ARDocumentTypes.CREDIT_STRING)){
	    	sEditorClass = "AREditCreditEntry";
	    }
	    if (sDocumentType.equalsIgnoreCase(ARDocumentTypes.REVERSAL_STRING)){
	    	sEditorClass = "AREditReversalEntry";
	    }
	    if (sDocumentType.equalsIgnoreCase(ARDocumentTypes.RETAINAGE_STRING)){
	    	sEditorClass = "AREditRetainageEntry";
	    }
	    if (sDocumentType.equalsIgnoreCase(ARDocumentTypes.APPLYTO_STRING)){
	    	sEditorClass = "AREditApplyToEntry";
	    }
	    
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." + sEditorClass 
	    		+ "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"BatchNumber\" VALUE=" + sBatchNumber + ">");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"EntryNumber\" VALUE=-1>");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"Editable\" VALUE=Yes>");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"BatchType\" VALUE=" + sBatchType + ">");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"DocumentType\" VALUE=" + sDocumentType + ">");
	    
	    out.println(
				"<P>Enter document ID: <INPUT TYPE=TEXT NAME=\"" 
				+ "DocumentID" + "\""
				+ " VALUE = \"" + sDocumentID + "\""
				+ " SIZE=28 MAXLENGTH=12" 
				+ " STYLE=\"width: 2.41in; height: 0.25in\">"
				);
	
		//Link to finder:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
			+ "?ObjectName=Document"
			+ "&ResultClass=FinderResults"
			+ "&SearchingClass=smar.ARSelectDocID"
			+ "&DocumentType=" + sDocumentType
			+ "&ParameterString="
				+ "*BatchNumber=" + sBatchNumber
				+ "*EntryNumber=-1"
				+ "*BatchType=" + sBatchType
				+ "*DocumentType=" + sDocumentType
				+ "*CallingURL=" + request.getRequestURI().toString()
				+ "*Editable=Yes"
				+ "*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&ReturnField=DocumentID"
			+ "&SearchField1=" + SMTableartransactions.spayeepayor
			+ "&SearchFieldAlias1=Customer%20Code"
			+ "&SearchField2=" + SMTableartransactions.sdocnumber
			+ "&SearchFieldAlias2=Document%20Number"
			+ "&SearchField3=" + SMTableartransactions.sdocdescription
			+ "&SearchFieldAlias3=Document%20Description"
			+ "&ResultListField1="  + SMTableartransactions.lid
			+ "&ResultHeading1=Document%20ID"
			+ "&ResultListField2="  + SMTableartransactions.spayeepayor
			+ "&ResultHeading2=Customer%20Number"
			+ "&ResultListField3="  + SMTableartransactions.sdocnumber
			+ "&ResultHeading3=Document%20Number"
			+ "&ResultListField4="  + SMTableartransactions.sdocdescription
			+ "&ResultHeading4=Document%20Description"
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "\""
			//+ "target=\"_blank\""
			+ "> Find document ID</A></P>"
		);
	    
	    if (sDocumentType.equalsIgnoreCase(ARDocumentTypes.RETAINAGE_STRING)){
		    out.println(
					"<P>Enter retainage percentage (digits only DO NOT include percent sign): <INPUT TYPE=TEXT NAME=\"" 
					+ "RetainagePercentage" + "\""
					+ " VALUE = \"" + "10" + "\""
					+ " SIZE=14 MAXLENGTH=5" 
					+ " STYLE=\"width: .75in; height: 0.25in\">"
					);
	    }

	    out.println("<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Add entry' STYLE='width: 2.00in; height: 0.24in'></P>");
		out.println("</FORM>");
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}