package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMInvoice;
import SMClasses.SMLogEntry;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTablesmoptions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsStringFunctions;

public class SMCreateMultipleInvoicesAction extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	
	private boolean bDebugMode = false;
	//private ArrayList<String> alCreatedInvoices = new ArrayList<String>(0);
	
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
	    String sUserID= (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)
	    		+ " " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    //sCallingClass will look like: smcontrolpanel.SMCreateMultipleInvoicesSelect
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    String sWarning = "";

	    boolean bCreateSingleInvoice = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(
	    		SMCreateMultipleInvoicesSelection.CREATE_SINGLE_INVOICE_PARAM, request).compareToIgnoreCase("") != 0;
    	String sTrimmedOrderNumber = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramstrimmedordernumber, request);
	    String sInvoiceDate = "";
	    sInvoiceDate = clsManageRequestParameters.get_Request_Parameter("INVOICEDATE", request).trim();
	    //System.out.println("[1514902771] - sInvoiceDate = '" + sInvoiceDate + "'");
		if(!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sInvoiceDate)){
			sWarning = "Invalid invoice date:\"" + sInvoiceDate + "\"";
			//here we want to redisplay the order list
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
			+ "Warning=" + SMUtilities.URLEncode(sWarning)
			+ request.getParameter("SELECTEDLOCATIONS") 
			+ request.getParameter("SELECTEDSERVICETYPES")
			;
    		if (bCreateSingleInvoice){
    			sRedirectString += "&" + SMCreateMultipleInvoicesSelection.CREATE_SINGLE_INVOICE_PARAM + "=Y";
    			sRedirectString += "&" + SMOrderHeader.Paramstrimmedordernumber + "="+ sTrimmedOrderNumber;
    			sRedirectString += "&" + SMCreateMultipleInvoicesSelection.LIST_ORDERS_TO_INVOICE_PARAM + "=Y";
    		}
    		sRedirectString +=  "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			;
    		response.sendRedirect(sRedirectString);			
            return;
		}
	    
    	//Customized title
    	String sReportTitle = "Creating Multiple Invoices";
    	ArrayList<String> alCreatedInvoices = new ArrayList<String>(0);
    	ArrayList<String> alOrders = new ArrayList<String>(0);
    	ArrayList<Double> alDeposits = new ArrayList<Double>(0);
		//Enumeration e = request.getParameterNames();
    	//String sParam = "";
    	//String sOrderNumber = "";
    	Double dDeposit = 0D;
    	String[] alParameterValues = request.getParameterValues("ORDER");
    	if (alParameterValues != null){
	    	for (int i=0;i<alParameterValues.length;i++){
	    		alOrders.add(clsStringFunctions.StringRight(alParameterValues[i], alParameterValues[i].length() - "ORDER".length()));
	    	}
    	}
    	//sort the order list we got.
    	Collections.sort(alOrders);
    	//get deposit for each order now.
    	for (int i=0;i<alOrders.size();i++){
    		String sDepo = request.getParameter("DEPO" + alOrders.get(i).toString());
			if (sDepo.trim().compareTo("") == 0){
				dDeposit = 0D;
			}else{
				try{    					 
					dDeposit = Double.valueOf(sDepo);
					if (dDeposit.compareTo(0D) < 0){
						throw new Exception("Error [1537446891] - the deposit amount ('" + sDepo + "') cannot be less than zero.");
					}
				}catch (Exception ex){
					//failed to convert, meaning the field has a non-recognizable number in it.
					sWarning = "You have entered an invalid deposit amount for order number '" + alOrders.get(i).toString() + "'";
					String sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + SMUtilities.URLEncode(sWarning)
					+ request.getParameter("SELECTEDLOCATIONS") 
					+ request.getParameter("SELECTEDSERVICETYPES")
					;
		    		if (bCreateSingleInvoice){
		    			sRedirectString += "&" + SMCreateMultipleInvoicesSelection.CREATE_SINGLE_INVOICE_PARAM + "=Y";
		    			sRedirectString += "&" + SMOrderHeader.Paramstrimmedordernumber + "="+ sTrimmedOrderNumber;
		    			sRedirectString += "&" + SMCreateMultipleInvoicesSelection.LIST_ORDERS_TO_INVOICE_PARAM + "=Y";
		    		}
		    		sRedirectString +=  "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					;
		    		response.sendRedirect(sRedirectString);
		    		return;
				}
			}
			alDeposits.add(dDeposit);
    	}
    	 
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"#FFFFFF\">" +
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
 	   log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMCREATEMULTIPLEINVOICES, "ACTION", "SMCreateMultipleInvoices", "[1376509315]");
 	   
 	   //reset warning message;
	   sWarning = "";
 	   //check to see if any thing is selected.
 	   if (alOrders.size()==0){
			sWarning = "You have to select at least one order to invoice.";
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
			+ "Warning=" + SMUtilities.URLEncode(sWarning)
			+ request.getParameter("SELECTEDLOCATIONS") 
			+ request.getParameter("SELECTEDSERVICETYPES")
			;
    		if (bCreateSingleInvoice){
    			sRedirectString += "&" + SMCreateMultipleInvoicesSelection.CREATE_SINGLE_INVOICE_PARAM + "=Y";
    			sRedirectString += "&" + SMOrderHeader.Paramstrimmedordernumber + "="+ sTrimmedOrderNumber;
    			sRedirectString += "&" + SMCreateMultipleInvoicesSelection.LIST_ORDERS_TO_INVOICE_PARAM + "=Y";
    		}
			sRedirectString +=  "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			;
			response.sendRedirect(sRedirectString);		
			return;
 	   }
 	   
    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(
    			getServletContext(), 
    			sDBID, 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString()) 
    			+ ".doGet - user: " 
    			+ sUserID
    			+ " - "
    			+ sUserFullName
    			);
    	if (conn == null){
    		sWarning = "Unable to get data connection.";
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
			+ "Warning=" + SMUtilities.URLEncode(sWarning)
			+ request.getParameter("SELECTEDLOCATIONS") 
			+ request.getParameter("SELECTEDSERVICETYPES")
			;
    		if (bCreateSingleInvoice){
    			sRedirectString += "&" + SMCreateMultipleInvoicesSelection.CREATE_SINGLE_INVOICE_PARAM + "=Y";
    			sRedirectString += "&" + SMOrderHeader.Paramstrimmedordernumber + "="+ sTrimmedOrderNumber;
    			sRedirectString += "&" + SMCreateMultipleInvoicesSelection.LIST_ORDERS_TO_INVOICE_PARAM + "=Y";
    		}
    		sRedirectString +=  "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			;
    		response.sendRedirect(sRedirectString);	
        	return;
    	}
    	Connection conncheck = clsDatabaseFunctions.getConnection(
    			getServletContext(), 
    			sDBID, 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString()) + ".doGet (conncheck) - user: " + sUserFullName
    			);
    	if (conncheck == null){
    		sWarning = "Unable to get data connection CONNCHECK.";
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
			+ "Warning=" + SMUtilities.URLEncode(sWarning)
			+ request.getParameter("SELECTEDLOCATIONS") 
			+ request.getParameter("SELECTEDSERVICETYPES")
			;
    		if (bCreateSingleInvoice){
    			sRedirectString += "&" + SMCreateMultipleInvoicesSelection.CREATE_SINGLE_INVOICE_PARAM + "=Y";
    			sRedirectString += "&" + SMOrderHeader.Paramstrimmedordernumber + "="+ sTrimmedOrderNumber;
    			sRedirectString += "&" + SMCreateMultipleInvoicesSelection.LIST_ORDERS_TO_INVOICE_PARAM + "=Y";
    		}
    		sRedirectString +=  "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			;
    		response.sendRedirect(sRedirectString);	
    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080423]");
        	return;
    	}

		//Check if there is already someone invoicing. 
		try {
			checkInvoicingFlag(conn, sDBID, sUserID);
		} catch (Exception e1) {
    		try {
    			//Remove invoicing flag
				setInvoicingFlag(false,conn, sDBID, sUserID);
			} catch (Exception e) {
				sWarning += e1.getMessage();
			}
    		sWarning = "Error when creating multiple invoices - " + e1.getMessage() + "<BR>" + sWarning;
    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080790]");
    		clsDatabaseFunctions.freeConnection(getServletContext(), conncheck, "[1547080791]");
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
			+ "Warning=" + SMUtilities.URLEncode(sWarning)
			+ request.getParameter("SELECTEDLOCATIONS") 
			+ request.getParameter("SELECTEDSERVICETYPES")
			;
    		if (bCreateSingleInvoice){
    			sRedirectString += "&" + SMCreateMultipleInvoicesSelection.CREATE_SINGLE_INVOICE_PARAM + "=Y";
    			sRedirectString += "&" + SMOrderHeader.Paramstrimmedordernumber + "="+ sTrimmedOrderNumber;
    			sRedirectString += "&" + SMCreateMultipleInvoicesSelection.LIST_ORDERS_TO_INVOICE_PARAM + "=Y";
    		}
    		sRedirectString +=  "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID;
    		response.sendRedirect(sRedirectString);
    		return;
		}
    	
    	try {
			//Set invoicing flag
			setInvoicingFlag(true, conn, sDBID, sUserID);
		} catch (Exception e1) {
    		sWarning = "Error when creating multiple invoices - " + e1.getMessage() + "<BR>" + sWarning;
    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080890]");
    		clsDatabaseFunctions.freeConnection(getServletContext(), conncheck, "[1547080891]");
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
			+ "Warning=" + SMUtilities.URLEncode(sWarning)
			+ request.getParameter("SELECTEDLOCATIONS") 
			+ request.getParameter("SELECTEDSERVICETYPES")
			;
    		if (bCreateSingleInvoice){
    			sRedirectString += "&" + SMCreateMultipleInvoicesSelection.CREATE_SINGLE_INVOICE_PARAM + "=Y";
    			sRedirectString += "&" + SMOrderHeader.Paramstrimmedordernumber + "="+ sTrimmedOrderNumber;
    			sRedirectString += "&" + SMCreateMultipleInvoicesSelection.LIST_ORDERS_TO_INVOICE_PARAM + "=Y";
    		}
    		sRedirectString +=  "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID;
    		response.sendRedirect(sRedirectString);
    		return;
		}

		//Create invoices
    	try{
			CreateInvoices(conn,
				conncheck,
				sInvoiceDate,
				alOrders,
				alDeposits,
				alCreatedInvoices,
				sDBID,
				sUserID,
				sUserFullName,
				out
			);
    	}catch(Exception e){
    		sWarning = "Invoicing process failed. <BR>" + e.getMessage();
    	}
		//Remove invoicing flag
		try {
			setInvoicingFlag(false,conn, sDBID, sUserID);
		} catch (Exception e) {
			//Can't do much about this - it will have to be unset manually
		}
    	//even if some invoices were not created, we still want to out put created invoice list.
		String sMessage = "";
    	if (alCreatedInvoices.size() > 0){
	    	for (int i=0;i<alCreatedInvoices.size();i++){
	    		sMessage +=  alCreatedInvoices.get(i).trim() + "/";
	    	}
    	}
    	if (bDebugMode){
    		System.out.println("[1540839354] In " + this.toString() + " sMessage = " + sMessage + " time: " + System.currentTimeMillis());
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080424]");
    	clsDatabaseFunctions.freeConnection(getServletContext(), conncheck, "[1547080425]");
		String sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
			+ SMUtilities.URLDecode(request.getParameter("SELECTEDLOCATIONS")) 
			+ SMUtilities.URLDecode(request.getParameter("SELECTEDSERVICETYPES"))
			+ "&" + SMCreateMultipleInvoicesSelection.LIST_OF_INVOICES_CREATED_PARAM + "=" + sMessage
			+ "&Warning=" + SMUtilities.URLEncode(sWarning)
		;
		if (bCreateSingleInvoice){
			sRedirectString += "&" + SMCreateMultipleInvoicesSelection.CREATE_SINGLE_INVOICE_PARAM + "=Y";
			sRedirectString += "&" + SMOrderHeader.Paramstrimmedordernumber + "="+ sTrimmedOrderNumber;
			sRedirectString += "&" + SMCreateMultipleInvoicesSelection.LIST_ORDERS_TO_INVOICE_PARAM + "=Y";
		}
		sRedirectString +=  "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID;
		response.sendRedirect(sRedirectString);
    	return;
	}

	private void CreateInvoices (Connection conn,
									Connection conncheck,
								   String sInvoiceDate,
								   ArrayList<String> alOrders,
								   ArrayList<Double> alDeposits,
								   ArrayList<String> alInvoices,
								   String sDBID,
								   String sUserID,
								   String sUserFullName,
								   PrintWriter pwOut
								   ) throws Exception{
	
		alInvoices.clear();
	    if (alOrders.size()>0){
	    }else{
	    	throw new Exception("Error [1548457237] - No order is selected.");
	    }
	    //Sort the orders:
	    Collections.sort(alOrders);
	    //loop though the list
	    for (int i=0;i<alOrders.size();i++){
	    	//load the order
	    	SMOrderHeader cOrder = new SMOrderHeader();
	    	SMInvoice cInvoice = new SMInvoice();
	    	cOrder.setM_strimmedordernumber((String) alOrders.get(i));
	    	if (!cOrder.load(conn)){
	    		//error loading order
	    		//out put a message and goto the next order.
	    		throw new Exception("Error [1454609295] loading order " + (String) alOrders.get(i) + " - " + cOrder.getErrorMessages() + "<BR>");
	    	}
	    	if (!cOrder.validate_for_invoicing(conn)){
	    		throw new Exception("Error [1454609296] Customer " + cOrder.getM_sBillToName() + " cannot be invoiced: " + cOrder.getErrorMessages() + "<BR>");
	    	}
            cInvoice.setM_dPrePayment(BigDecimal.valueOf(alDeposits.get(i)));
            SMCreateInvoice createinv = new SMCreateInvoice();

           // System.out.println("[1514902772] - cInvoice.getM_datInvoiceDate() = '" + cInvoice.getM_datInvoiceDate() + "'");
            
            try{
            	createinv.Create_Invoice(cOrder, cInvoice, sInvoiceDate, sUserID, sUserFullName, conn, sDBID, getServletContext());
            }catch(Exception ex){
            	if (bDebugMode){
            		System.out.println("[1540839355] - Creating invoice failed.<BR>"
            	  		+ ex.getMessage());
            	}
            	throw new Exception("Error [1454609297] Invoice creation failed.<BR>"
            		     + ex.getMessage());
            }

            //out put invoice number into an array.
            alInvoices.add(cInvoice.getM_sInvoiceNumber().trim());
            
            if (bDebugMode){
            	System.out.println("[1540839356] - In " + this.toString() + " Invoice number " 
            		+ cInvoice.getM_sInvoiceNumber() + " has been created successfully.");
            }
	    }
		return;
	}
	
	private void checkInvoicingFlag(Connection conn, String sDBID, String sUserID) throws Exception{
		String SQL = "SELECT "
				+ " " + SMTablesmoptions.iinvoicingflag 
				+ ", " + SMTablesmoptions.ilastinvoicinguserid 
				+ ", " + SMTablesmoptions.datlastinvoicingflagtime
					+ " FROM " + SMTablesmoptions.TableName;
		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		
		if(rs.next()) {
			if(rs.getInt(SMTablesmoptions.iinvoicingflag ) == 1) {
				String sInvoicingUser = SMUtilities.getFullNamebyUserID(Integer.toString(rs.getInt(SMTablesmoptions.ilastinvoicinguserid)), conn); 
				throw new Exception( sInvoicingUser + " started creating invoicing at " + rs.getString(SMTablesmoptions.datlastinvoicingflagtime) 
				+ ". Please try again a few minutes. ");
			}
		}
		rs.close();
		
	}
	
	private void setInvoicingFlag(boolean bSetInvoicingFlag, Connection conn, String sDBID, String sUserID) throws Exception{
		String SQL = "";
		if(bSetInvoicingFlag) {
			SQL = "UPDATE " + SMTablesmoptions.TableName + " SET"
				+ " " + SMTablesmoptions.iinvoicingflag + " = 1"
				+ ", " + SMTablesmoptions.ilastinvoicinguserid + " = " + sUserID
				+ ", " + SMTablesmoptions.datlastinvoicingflagtime + " = NOW()";
		}else {
			SQL = "UPDATE " + SMTablesmoptions.TableName + " SET"
					+ " " + SMTablesmoptions.iinvoicingflag + " = 0";
		}
		try{
			clsDatabaseFunctions.executeSQL(SQL, conn);
		}catch (SQLException ex){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error updating invoicing flag in smoptions table"
					+ "<BR>" + ex.getMessage()
					+ "<BR>SQL: " + ex.getSQLState());
		}
	}
}
