package smar;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMClasses.SMLogEntry;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTableartransactions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
public class ARActivityDisplay extends HttpServlet {

	private static final long serialVersionUID = 1L;
	/*
	 * Parameters:
	 * CustomerNumber
	 * OpenTransactionsOnly (true or false) - null = true
	 * OrderNumber IF this function should ONLY display transactions for a particular order
	 */
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(
	    	request, 
	    	response, 
	    	getServletContext(), 
	    	SMSystemFunctions.ARCustomerActivity)){
	    	return;
	    }
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    			+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //Get the variables for the class:
	    String sCustomerNumber = "";
	    
	    if (request.getParameter("CustomerNumber") != null){
	    	sCustomerNumber = request.getParameter("CustomerNumber").toUpperCase();
	    }
	    
	    if (request.getParameter("SubmitSelectAll") != null){
	    	response.sendRedirect(
		    		SMUtilities.getURLLinkBase(getServletContext()) 
		    		+ "" + "smar.ARActivityInquiry"
		    		+ "?CustomerNumber=" + sCustomerNumber
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    	);
	    }else if (request.getParameter("SubmitClearAll") != null){
	    	response.sendRedirect(
		    		SMUtilities.getURLLinkBase(getServletContext()) 
		    		+ "" + "smar.ARActivityInquiry"
		    		+ "?CustomerNumber=" + sCustomerNumber
					  + "&SelectAllTypes=0"
					  + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    	);
	    }else{

		    boolean bOpenTransactionsOnly = true;
		    
		    if ("false".compareToIgnoreCase(clsManageRequestParameters.get_Request_Parameter("OpenTransactionsOnly", request)) == 0){
		    		bOpenTransactionsOnly = false;
		    }
 
		    //If this button was chosen from this page previously, we want to reverse the state of the
		    // 'OpenTransactionsOnly' variable:
		    if (request.getParameter("ToggleTransactionView") != null){
		    	if (bOpenTransactionsOnly){
		    		bOpenTransactionsOnly = false;
		    	}else{
		    		bOpenTransactionsOnly = true;
		    	}
		    }
	
		    String sOrderBy = request.getParameter("OrderBy");
		    	    
		    String sOpenTransactionsOnly = "true";
		    if (bOpenTransactionsOnly){
		    	sOpenTransactionsOnly = "true";
		    }else{
		    	sOpenTransactionsOnly = "false";
		    }
		    
		    ARCustomer cust = new ARCustomer(sCustomerNumber);
		    
		    if (!cust.load(getServletContext(), sDBID)){
		    	response.sendRedirect(
		    		SMUtilities.getURLLinkBase(getServletContext()) 
		    		+ "" + "smar.ARActivityInquiry"
					+ "?Warning=Error loading customer from database!<BR>" + cust.getErrorMessageString()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&CustomerNumber=" + cust.getM_sCustomerNumber()
		    	);
		    	return;
		    }
	
			String sSQLStartingDate;
			try {
				sSQLStartingDate = clsDateAndTimeConversions.utilDateToString(
					clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", request.getParameter("StartingDate")),"yyyy-MM-dd");
			} catch (ParseException e1) {
		    	response.sendRedirect(
				    	SMUtilities.getURLLinkBase(getServletContext()) 
				    	+ "" + "smar.ARActivityInquiry"
				    	+ "?Warning=Invalid starting date: '" + request.getParameter("StartingDate") + "'"
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "&CustomerNumber=" + cust.getM_sCustomerNumber()
				    	);	
		            	return;
			}
			String sSQLEndingDate;
			try {
				sSQLEndingDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate(
						"M/d/yyyy", request.getParameter("EndingDate")),"yyyy-MM-dd");
			} catch (ParseException e) {
		    	response.sendRedirect(
				    	SMUtilities.getURLLinkBase(getServletContext()) 
				    	+ "" + "smar.ARActivityInquiry"
				    	+ "?Warning=Invalid ending date: '" + request.getParameter("EndingDate") + "'"
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "&CustomerNumber=" + cust.getM_sCustomerNumber()
				    	);	
	            	return;
			}
		    
			String sOrderNumber = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsOrderNumber, request);
			
		    String title = "Customer activity.";
		    String subtitle = cust.getM_sCustomerNumber() + " - " + cust.getM_sCustomerName();
		    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		    
		    SimpleDateFormat sdfDateOnly = new SimpleDateFormat("MM-dd-yyyy");
	
		    //Print a link to the first page after login:
		    String sShowAll = "";
		    if(bOpenTransactionsOnly){
		    	sShowAll = "Show ALL transactions";
		    }else{
		    	sShowAll = "Show OPEN transactions only";
		    }
		    
		    //get selected doc types
		    ArrayList<Integer> alDocTypes = new ArrayList<Integer>(0);
		    for (int i=0;i <= 10;i++){
		    	if (request.getParameter(ARDocumentTypes.Get_Document_Type_Label(i)) != null){
		    		alDocTypes.add(i);
		    	}
		    }
		    
		    out.println("<TABLE BORDER=0><TR><TD WIDTH=60%>");
			    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
			    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">Return to Accounts Receivable Main Menu</A><BR>");
			    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARActivityDisplay' METHOD='POST'>");
			    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
			    out.println("<INPUT TYPE=HIDDEN NAME=\"CustomerNumber\" VALUE=\"" + cust.getM_sCustomerNumber() + "\">");
			    out.println("<INPUT TYPE=HIDDEN NAME=\"StartingDate\" VALUE=\"" + request.getParameter("StartingDate") + "\">");
			    out.println("<INPUT TYPE=HIDDEN NAME=\"EndingDate\" VALUE=\"" + request.getParameter("EndingDate") + "\">");
			    out.println("<INPUT TYPE=HIDDEN NAME=\"OrderBy\" VALUE=\"" + sOrderBy + "\">");
			    out.println("<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamsOrderNumber + "\" VALUE=\"" + sOrderNumber + "\">");
			    for (int i=0;i<alDocTypes.size();i++){
			    	out.println("<INPUT TYPE=HIDDEN NAME=\"" + ARDocumentTypes.Get_Document_Type_Label(Integer.parseInt(alDocTypes.get(i).toString())) + "\" VALUE=1>");
			    }
			    out.println("<INPUT TYPE=HIDDEN NAME=\"OpenTransactionsOnly\" VALUE=\"" + sOpenTransactionsOnly + "\">");
			    out.println("<INPUT TYPE=SUBMIT NAME='ToggleTransactionView' VALUE=\"" + sShowAll + "\" STYLE='height: 0.24in'>");
			    out.println("</FORM>");
		    out.println("</TD>");
		    out.println("<TD WIDTH=40%>");
			    out.println("<FORM ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARActivityDisplay' METHOD='POST'>");
			    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
			    out.println("<INPUT TYPE=TEXT NAME=\"CustomerNumber\" SIZE=28 MAXLENGTH=" + Integer.toString(SMTablearcustomer.sCustomerNumberLength) + " STYLE=\"width: 2.41in; height: 0.25in\">");
			    out.println("<INPUT TYPE=HIDDEN NAME=\"StartingDate\" VALUE=\"" + request.getParameter("StartingDate") + "\">");
			    out.println("<INPUT TYPE=HIDDEN NAME=\"EndingDate\" VALUE=\"" + request.getParameter("EndingDate") + "\">");
			    out.println("<INPUT TYPE=HIDDEN NAME=\"OrderBy\" VALUE=\"" + sOrderBy + "\">");
			    out.println("<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamsOrderNumber + "\" VALUE=\"" + sOrderNumber + "\">");
			    for (int i=0;i<alDocTypes.size();i++){
			    	out.println("<INPUT TYPE=HIDDEN NAME=\"" + ARDocumentTypes.Get_Document_Type_Label(Integer.parseInt(alDocTypes.get(i).toString())) + "\" VALUE=1>");
			    }
			    out.println("<INPUT TYPE=HIDDEN NAME=\"OpenTransactionsOnly\" VALUE=\"" + sOpenTransactionsOnly + "\">");
			    out.println("<INPUT TYPE=SUBMIT VALUE=\"Show New Customer\" STYLE='height: 0.24in'>");
			    out.println("</FORM>");
		    out.println("</TD></TR></TABLE>");
		    
		    //Build table header for listing:
		    String sLinkMain = request.getRequestURI().toString() + "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID;
		    sLinkMain += "&CustomerNumber=" + sCustomerNumber;
		    for (int i=0;i<alDocTypes.size();i++){
		    	sLinkMain += "&" + ARDocumentTypes.Get_Document_Type_Label(Integer.parseInt(alDocTypes.get(i).toString())) + "=1";
		    }
		    if (bOpenTransactionsOnly){
		    	sLinkMain += "&OpenTransactionsOnly=true";
		    }else{
		    	sLinkMain += "&OpenTransactionsOnly=false";
		    }
		    sLinkMain += "&StartingDate=" + request.getParameter("StartingDate");
		    sLinkMain += "&EndingDate=" + request.getParameter("EndingDate");
		    
		    sLinkMain += "&" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber;
		    
		    String sDocumentTypeList = "";
		    for (int i=0;i<alDocTypes.size();i++){
		    	if (i == 0){
		    		sDocumentTypeList += "<B>" + ARDocumentTypes.Get_Document_Type_Label(
			    		Integer.parseInt(alDocTypes.get(i).toString())) + "</B>";
		    	}else{
		    		sDocumentTypeList += ", <B>" + ARDocumentTypes.Get_Document_Type_Label(
		    			Integer.parseInt(alDocTypes.get(i).toString()))+ "</B>";
		    	}
		    }
		    
		    if (sOrderNumber.compareToIgnoreCase("") != 0){
		    	out.println("<B>ONLY SHOWING TRANSACTIONS WITH ORDER NUMBER '" + sOrderNumber + "'.<BR>");
		    }
		    
		    out.println(
		    	"Starting with document date <B>" + request.getParameter("StartingDate") + "</B> and ending with document date <B>" 
		    	+ request.getParameter("EndingDate")
		    	+ "</B> including document types: " + sDocumentTypeList + ".<BR>"
		    );
		    
		    out.println("Current Balance: <B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(cust.getCurrentStoredBalance(getServletContext(), sDBID))+ "</B>,");
		    out.println(" Retainage Balance: <B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(cust.getCalculatedRetainageBalance(getServletContext(), sDBID))+ "</B>,");
		    out.println(" Credit Limit: <B>" + cust.getM_dCreditLimit() + "</B>,");
		    out.println(" Terms: <B>" + cust.getM_sTerms() + "</B>");
		    out.println("<BR>");
		    
		    try{
		    	if (sOrderBy.compareTo(SMTableartransactions.datdocdate) == 0){
		    		out.println("<B>Ordered By: Document Date</B>");
		    	}else if (sOrderBy.compareTo(SMTableartransactions.idoctype) == 0){
		    		out.println("<B>Ordered By: Document Type</B>");
		    	}else if (sOrderBy.compareTo(SMTableartransactions.sordernumber) == 0){
		    		out.println("<B>Ordered By: Order Number</B>");
		    	}
		    }catch (NullPointerException npe){
		    	out.println("<BR><FONT COLOR=RED>Error [1445009626] reading Sort By '" + sOrderBy + "' - " + npe.getMessage() + ".</FONT><BR>");
		    }
		    out.println("<HR><FONT SIZE=2>&nbsp;(Click on underscored column headers to sort them.)</FONT>");
		    
		    int iLineCounter = 0;
	        BigDecimal bdTotalOriginalAmt = new BigDecimal(0);
	        BigDecimal bdTotalCurrentAmt = new BigDecimal(0);
		    try{
		        String sSQL = "SELECT *" 
	    			+ " FROM " + SMTableartransactions.TableName
	    			+ " WHERE ("
	    				+ "(" + SMTableartransactions.spayeepayor + " = '" + sCustomerNumber + "')"
	    				
	    				+ " AND (" + SMTableartransactions.datdocdate + " >= '" + sSQLStartingDate + "')"
	    				+ " AND (" + SMTableartransactions.datdocdate + " <= '" + sSQLEndingDate + " 23:59:59')"
	    				;
	    		
	    				if (bOpenTransactionsOnly){
	    					sSQL += " AND " + SMTableartransactions.dcurrentamt + " != 0.00";
	    				}
	    				if (alDocTypes.size() == 0){
	    					sSQL += " AND 1=0";
	    				}else{
	    					sSQL += " AND (";
	    					for (int i=0;i<alDocTypes.size();i++){
	    						sSQL += " " + SMTableartransactions.TableName + "." + SMTableartransactions.idoctype + " = " + alDocTypes.get(i) + " OR";
	    					}
	    				//cut the last unnecessary " OR"
	    				sSQL = sSQL.substring(0, sSQL.length() - 3) + ")";	
	    				}
	    				
	    				//If we are filtering on an order number:
	    				if (sOrderNumber.compareToIgnoreCase("") != 0){
	    					sSQL += " AND (" + SMTableartransactions.TableName + "." 
	    					+ SMTableartransactions.sordernumber + " = '" + sOrderNumber + "')";
	    				}
	    				
	    			sSQL += ")"
	    			+ " ORDER BY " 
	    			+ sOrderBy + ", "
	    			+ SMTableartransactions.sdocnumber;
	    		
	    		//System.out.println("In " + this.toString() + ".doPost - sSQL = " + sSQL);
		        ResultSet rs = clsDatabaseFunctions.openResultSet(
		        	sSQL, 
		        	getServletContext(), 
		        	sDBID,
		        	"MySQL",
		        	this.toString() + ".doPost - User: " + sUserID
		        	+ " "
		        	+ sUserFullName
		        		);
		        boolean bHasRecord = false;
		        
		        BigDecimal bdOriginalAmt = new BigDecimal(0);
		        BigDecimal bdCurrentAmt = new BigDecimal(0);
	
	        	while (rs.next()){
	        		bHasRecord = true;
	        		//Start a row:
	
	        		if (iLineCounter == 50){
	        			out.println("</TABLE>");
	        			out.println("<BR>");
	        			iLineCounter = 0;
	        		}
	        		if (iLineCounter == 0){
	        			out.println("<TABLE BORDER=1 CELLSPACING=2 WIDTH=100% style=\"font-size:75%\">");
	        			printTableHeader(out, sOrderBy, sLinkMain);
	        		}
	        		out.println("<TR>");
	        		
	        		bdOriginalAmt = rs.getBigDecimal(SMTableartransactions.doriginalamt);
	        		bdTotalOriginalAmt = bdTotalOriginalAmt.add(bdOriginalAmt);
	        		bdCurrentAmt = rs.getBigDecimal(SMTableartransactions.dcurrentamt);
	        		bdTotalCurrentAmt = bdTotalCurrentAmt.add(bdCurrentAmt);
	        		
	    			Build_Row(		
	        			out,
	        			sCustomerNumber,
	        			Long.toString(rs.getLong(SMTableartransactions.lid)),
	        			sOpenTransactionsOnly,
	        			sdfDateOnly.format(rs.getDate(SMTableartransactions.datdocdate)),
	        			rs.getString(SMTableartransactions.sdocnumber),
	        			rs.getInt(SMTableartransactions.idoctype),
	        			sdfDateOnly.format(rs.getDate(SMTableartransactions.datduedate)),
	        			clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdOriginalAmt),
	        			clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCurrentAmt),
	        			Long.toString(rs.getLong(SMTableartransactions.iretainage)),
	        			rs.getString(SMTableartransactions.sordernumber),
	        			rs.getString(SMTableartransactions.sdocdescription),
	        			rs.getString(SMTableartransactions.sponumber),
	        			sOrderNumber,
	        			getServletContext(),
	        			sDBID
	    			);
	        		//End the row:
	        		out.println("</TR>");
	        		iLineCounter++;
	        	}
	        	if (!bHasRecord){
	        		out.println ("<TR><TD COLSPAN=11><B>No Record Found</B></TD></TR>");
	        	}else {
	     		   SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
	     	 	   log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARACTIVITYDISPLAY, "REPORT", "ARActivityDisplay", "[1564762127]");
	        	}
	        	rs.close();
		        out.println ("<BR>");
			}catch (SQLException ex){
		    	System.out.println("Error in " + this.toString() + " class!!");
		        System.out.println("SQLException: " + ex.getMessage());
		        System.out.println("SQLState: " + ex.getSQLState());
		        System.out.println("SQL: " + ex.getErrorCode());
				//return false;
			}
			
			out.println("</TABLE>");
			
			out.println(
				"<BR><B>TOTALS:</B>&nbsp;Original Amount:&nbsp;<B>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalOriginalAmt)
				+ "</B>,&nbsp;Current Amount: <B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalCurrentAmt)
				+ "</B>"
				);
	    }
		
		//***************************************
		out.println("</BODY></HTML>");
	}

	private static void Build_Row (
			PrintWriter pwout,
			String sCustomerNumber,
			String sTransactionID,
			String sOpenTransactionsOnly,
			String sDocDate,
			String sDocNumber,
			int iDocType,
			String sDueDate,
			String sOriginalAmt,
			String sCurrentAmt,
			String sRetainageFlag,
			String sOrderNumber,
			String sDocDesc,
			String sPONumber,
			String sOrderNum,
			ServletContext context,
			String sDBID
			){

		pwout.println("<TD>" + sDocDate + "</TD>");
		pwout.println("<TD>");
		//Transaction ID:
		//Build a link into this field:
		pwout.println("<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smar.ARDisplayMatchingTransactions" 
    		+ "?MatchedTransactionID=" + sTransactionID
    		+ "&CustomerNumber=" + clsServletUtilities.URLEncode(sCustomerNumber)
    		+ "&DocNumber=" + clsServletUtilities.URLEncode(sDocNumber)
    		+ "&OpenTransactionsOnly=" + sOpenTransactionsOnly
    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		+ "\">"
    		+ sTransactionID
    		+ "</A>");
		pwout.println("</TD>");
		//Doc number:
		//Build a link into this field:
		pwout.println("<TD>");
		pwout.println("<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smar.ARDisplayMatchingTransactions" 
    		+ "?MatchedTransactionID=" + sTransactionID
    		+ "&CustomerNumber=" + clsServletUtilities.URLEncode(sCustomerNumber)
    		+ "&DocNumber=" + clsServletUtilities.URLEncode(sDocNumber)
    		+ "&OpenTransactionsOnly=" + sOpenTransactionsOnly
    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		+ "\">"
    		+ sDocNumber
    		+ "</A>");
		pwout.println("</TD>");
		
		//If it's an invoice or credit, see if we can link to it:
		if( (iDocType == ARDocumentTypes.CREDIT)
			|| (iDocType == ARDocumentTypes.INVOICE)
				
		){
			pwout.println("<TD>");
			pwout.println("<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "" + SMUtilities.lnViewInvoice(sDBID, sDocNumber)
	    		+ "\">"
	    		+ "View"
	    		+ "</A>");
			pwout.println("</TD>");
		}else{
			pwout.println("<TD>&nbsp;</TD>");
		}
		
		pwout.println("<TD>" + ARDocumentTypes.Get_Document_Type_Label(iDocType) + "</TD>");
		pwout.println("<TD>" + sDueDate + "</TD>");
		pwout.println("<TD ALIGN = RIGHT>" + sOriginalAmt + "</TD>");
		pwout.println("<TD ALIGN = RIGHT>" + sCurrentAmt + "</TD>");
		
		if(sRetainageFlag.compareToIgnoreCase("0") == 0){
			pwout.println("<TD>" + "&nbsp;" + "</TD>");
		}else{
			pwout.println("<TD>" + "YES" + "</TD>");
		}
		
		pwout.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
		+ sOrderNumber 
		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
		+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sOrderNumber) + "</A></TD>");
		
		pwout.println("<TD>" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sPONumber) + "</TD>");
		pwout.println("<TD>" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sDocDesc) + "</TD>");
	}
	
	private void printTableHeader(PrintWriter pwOut, String sOrderBy, String sLinkMain){
		pwOut.println("<TR>");
	    //out.println("Link = " + sLinkMain + "&OrderBy=" + SMTableartransactions.datdocdate);
	    if (sOrderBy.compareTo(SMTableartransactions.datdocdate) == 0){
	    	pwOut.println(
	    		"<TD><B><A HREF=\"" 
	    		+ sLinkMain 
	    		+ "&OrderBy=" 
	    		+ SMTableartransactions.datdocdate 
	    		+ "\">Doc. Date</A></B></TD>"
	    	);
	    }else{
	    	pwOut.println(
	    		"<TD><A HREF=\"" 
	    		+ sLinkMain 
	    		+ "&OrderBy=" 
	    		+ SMTableartransactions.datdocdate 
	    		+ "\">Doc. Date</A></TD>"
	    	);
	    }
	    pwOut.println("<TD>Doc. ID</TD>");
	    pwOut.println("<TD>Doc. #</TD>");
	    pwOut.println("<TD>View?</TD>");
	    if (sOrderBy.compareTo(SMTableartransactions.idoctype) == 0){
	    	pwOut.println("<TD><B><A HREF=\"" 
	    		+ sLinkMain 
	    		+ "&OrderBy=" 
	    		+ SMTableartransactions.idoctype 
	    		+ "\">Doc. Type</A></B></TD>"
	    );
	    }else{
	    	pwOut.println("<TD><A HREF=\"" 
	    		+ sLinkMain 
	    		+ "&OrderBy=" 
	    		+ SMTableartransactions.idoctype 
	    		+ "\">Doc. Type</A></TD>"
	    	);
	    }
	    	
	    pwOut.println("<TD>Due Date</TD>");
	    pwOut.println("<TD>Original Amt.</TD>");
	    pwOut.println("<TD>Current Amt.</TD>");
	    pwOut.println("<TD>Retainage ?</TD>");
	    if (sOrderBy.compareTo(SMTableartransactions.sordernumber) == 0){
	    	pwOut.println(
	    		"<TD><B><A HREF=\"" 
	    		+ sLinkMain 
	    		+ "&OrderBy=" 
	    		+ SMTableartransactions.sordernumber 
	    		+ "\">Order #</A></B></TD>"
	    	);
	    }else{
	    	pwOut.println(
	    		"<TD><A HREF=\"" 
	    		+ sLinkMain 
	    		+ "&OrderBy=" 
	    		+ SMTableartransactions.sordernumber 
	    		+ "\">Order #</A></TD>"
	    	);
	    }
	    pwOut.println("<TD>PO Number</TD>");
	    pwOut.println("<TD>Description</TD>");
	    
	    pwOut.println("</TR>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}