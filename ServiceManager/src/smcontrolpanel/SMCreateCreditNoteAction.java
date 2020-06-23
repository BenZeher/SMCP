package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.sql.Connection;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMCreateCreditNoteAction extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	
	private boolean bDebugMode = false;
	private String sCreatedCreditNoteNumber = "";
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), -1L)){
			return;
		}
		
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)
	    		+ " " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    //sCallingClass will look like: smcontrolpanel.SMCreateMultipleInvoicesSelect
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);

    	String sInvoiceNumber = clsManageRequestParameters.get_Request_Parameter("INVOICENUMBER", request);
    	String sCreditNoteInfo = clsManageRequestParameters.get_Request_Parameter("CREDITNOTEINFO", request).trim();
	    String sCreditNoteDate = clsManageRequestParameters.get_Request_Parameter("CREDITNOTEDATE", request).trim();
	    
	    String sWarning = "";
	    
		if(!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sCreditNoteDate)){
			sWarning = "Invalid credit note date:\"" + sCreditNoteDate + "\"";
			//here we want to redisplay the order list
			String sRedirectString = sCallingClass + "?"
			+ "Warning=" + clsServletUtilities.URLEncode(sWarning)
			+ "&CREDITNOTEDATE=" + sCreditNoteDate 
			+ "&CREDITNOTEINFO=" + clsServletUtilities.URLEncode(sCreditNoteInfo)
			+ "&INVOICENUMBER=" + sInvoiceNumber
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			;
    		response.sendRedirect(sRedirectString);			
            return;
		}
	    
    	//Customized title
    	String sReportTitle = "Creating Credit Notes";
    	    	 
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\">" +
		   "<TABLE BORDER=0 WIDTH=100%>" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
		   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) 
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>");
     	
	   out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR></TD></TR>"
				+ "<TR><TD COLSPAN=2><FONT SIZE=2>Please wait....</FONT></TD></TR></TABLE>");
				   
 	   //log usage of this this report
 	   SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
 	   log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMCREATECREDITNOTE, "ACTION", "SMCreateCreditNote", "[1376509312]");
 	   
 	   //reset warning message;
	   sWarning = "";
 	   
    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(
    			getServletContext(), 
    			sDBID, 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString()) 
    			+ " - user: " 
    			+ sUserID
    			+ " - "
    			+ sUserFullName
    			);
    	if (conn == null){
    		sWarning = "Unable to get data connection.";
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
			+ "Warning=" + clsServletUtilities.URLEncode(sWarning)
			+ "&CREDITNOTEDATE=" + sCreditNoteDate 
			+ "&CREDITNOTEINFO=" + clsServletUtilities.URLEncode(sCreditNoteInfo)
			+ "&INVOICENUMBER=" + sInvoiceNumber
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			;
    		response.sendRedirect(sRedirectString);	
        	return;
    	}

    	try {
			CreateCreditNote(conn,
				  sDBID,
				  sCreditNoteDate,
				  sInvoiceNumber,
				  sCreditNoteInfo,
				  sUserID,
				  sUserFullName,
				  out
			)
			;
		} catch (Exception e) {
			sWarning = "Crediting process failed. <BR>" + e.getMessage();
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080416]");
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass + "?"
				+ "Warning=" + clsServletUtilities.URLEncode(sWarning)
				+ "&CREDITNOTEDATE=" + sCreditNoteDate 
				+ "&CREDITNOTEINFO=" + clsServletUtilities.URLEncode(sCreditNoteInfo)
				+ "&INVOICENUMBER=" + sInvoiceNumber
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			;
			//System.out.println("sRedirectString = " + sRedirectString);
			response.sendRedirect(sRedirectString);
			return;
		}
		String sPrintCredit = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + 
				"smcontrolpanel.SMPrintInvoiceCriteriaSelection?" +
				"" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID +
				"&InvoiceNumberFrom=" + sCreatedCreditNoteNumber.trim() +
		"\">" + sCreatedCreditNoteNumber.trim() + "</A> (Click here to print)";

		//even if some credit was not created, we still want to out put created credit list.
		String sMessage = "Invoice #" + sInvoiceNumber + " is credited successfully.<BR>" +
		"Credit note # is: " + sPrintCredit;
		if (bDebugMode){
			System.out.println("[1579267587] In " + this.toString() + " sMessage = " + sMessage);
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080417]");
		//System.out.println("connections freed.");
		String sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMCreateCreditNoteCriteriaSelection" + "?"
			+ "MESSAGE=" + clsServletUtilities.URLEncode(sMessage)
			+ "&Warning=" + clsServletUtilities.URLEncode(sWarning)
			+ "&CREDITNOTEDATE=" + sCreditNoteDate 
			+ "&CREDITNOTEINFO=" + clsServletUtilities.URLEncode(sCreditNoteInfo)
			+ "&INVOICENUMBER=" + sInvoiceNumber
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		;
		//System.out.println("sRedirectString = " + sRedirectString);
		response.sendRedirect(sRedirectString);
    	return;
	}
	
	private void CreateCreditNote (Connection conn,
									  String sDBID,
									  String sCreditNoteDate,
									  String sInvoiceNumber,
									  String sCreditNoteInfo,
									  String sUserID,
									  String sUserFullName,
									  PrintWriter pwOut
									) throws Exception{
	
		
		
        SMCreateCreditNote createcrdt = new SMCreateCreditNote();

        try{
        	sCreatedCreditNoteNumber = createcrdt.Create_Credit_Note(sInvoiceNumber, 
							        								  sCreditNoteDate, 
							        								  sCreditNoteInfo, 
							        								  sUserID, 
							        								  sUserFullName,
							        								  conn, 
							        								  sDBID,
							        								  getServletContext());
        }catch(Exception ex){
        	if (bDebugMode){
        		System.out.println("[1579267594] Creating credit note failed.<BR>"
        	  		+ ex.getMessage());
        	}
        	throw new Exception ("Credit note creation failed.<BR>"
        		     + ex.getMessage());
        }
        
        if (bDebugMode){
        	System.out.println("[1579267598] In " + this.toString() + " Credit note number " 
        		+ "" + " has been created successfully.");
        }
	    
		return;
	}
}
