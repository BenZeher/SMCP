package smar;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMClasses.SMBatchTypes;
import SMClasses.SMEntryBatch;
import SMClasses.SMModuleTypes;
import SMClasses.TRANSACTIONSQLs;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class ARSelectForPostingJournal extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ARPostingJournal))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String m_sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		String m_sStartingBatchNumber = clsManageRequestParameters.get_Request_Parameter("StartingBatchNumber", request);
		String m_sEndingBatchNumber = clsManageRequestParameters.get_Request_Parameter("EndingBatchNumber", request);
    
		String title = "AR Posting Journal";
		String subtitle = "";
		subtitle = "";
		
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    //Display any warnings:
	    if (!m_sWarning.equalsIgnoreCase("")){
	    	String sWarning = request.getParameter("Warning");
	    	if (!sWarning.equalsIgnoreCase("")){
	    		out.println("<B><FONT COLOR=\"RED\">" + sWarning + "</FONT></B><BR>");
	    	}
	    }
	    
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ARPostingJournal) 
	    		+  "\">Summary</A><BR><BR>");
	    
	    
	    //Load the batch list:
	    ArrayList<String> arrBatchList = new ArrayList<String>(0);
	    ArrayList<String> arrBatchDescList = new ArrayList<String>(0);
	    try{
	        String sSQL = TRANSACTIONSQLs.Get_PostedTransactionBatch_List_SQL(SMModuleTypes.AR, "");
	        ResultSet rs = clsDatabaseFunctions.openResultSet(
	        		sSQL, 
	        		getServletContext(), 
	        		sDBID,
	        		"MySQL",
	        		this.toString() + ".loadBatchList - User: " + sUserID
	        		+ " - "
	        		+ sUserFullName
	        		);
	        
        	while (rs.next()){
        		arrBatchList.add(
        				Integer.toString(rs.getInt(SMEntryBatch.ibatchnumber)));
        		arrBatchDescList.add(
        				clsStringFunctions.PadLeft(Integer.toString(rs.getInt(SMEntryBatch.ibatchnumber)),"0",6)
        				+ " - " + SMBatchTypes.Get_Batch_Type(rs.getInt(SMEntryBatch.ibatchtype)));
        	}
        	rs.close();
		}catch (SQLException ex){
			out.println("<BR><B>Error [1548726677] - Could not load batch list - " + ex.getMessage());
			out.println("</BODY></HTML>");
			return;
		}
	    
    	//Build the form for choosing batches:
    	out.println("<FORM NAME='ENTRYEDIT' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARPrintPostingJournal' METHOD='POST'>");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		//Start the table:
		out.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">");	
		
		//Starting number:
		out.println(ARUtilities.Create_Edit_Form_List_Row(
				"StartingBatchNumber", 
				arrBatchList, 
				m_sStartingBatchNumber, 
				arrBatchDescList, 
				"Starting batch number:", 
		"Choose the first batch you want to see in the posting journal"));
		
		//Ending number:
		out.println(ARUtilities.Create_Edit_Form_List_Row(
				"EndingBatchNumber", 
				arrBatchList, 
				m_sEndingBatchNumber, 
				arrBatchDescList, 
				"Ending batch number:", 
				"Choose the last batch you want to see in the posting journal"));
		
		out.println("</TABLE>");
		
		//Create checkboxes for batch types:
		out.println("<INPUT TYPE=CHECKBOX NAME=\"IncludeInvoiceBatches\" VALUE=1 CHECKED>Include invoice batches<BR>");
		out.println("<INPUT TYPE=CHECKBOX NAME=\"IncludeCashBatches\" VALUE=1 CHECKED>Include cash batches<BR>");
		
		//Submit button:
		out.println("<BR><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Print posting journal' STYLE='height: 0.24in'>");

	    //End the edit form:
		out.println("</FORM>");  
	    
		out.println("</BODY></HTML>");
	
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
