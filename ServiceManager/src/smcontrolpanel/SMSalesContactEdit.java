package smcontrolpanel;

import SMClasses.MySQLs;
import SMClasses.SMAppointment;
import SMDataDefinition.*;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SMSalesContactEdit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static SimpleDateFormat Dateformatter = new SimpleDateFormat("MM/dd/yyyy");
 
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditSalesContacts))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID); 
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String title = "";
		String subtitle = "";
		
    	//User has chosen to edit:
		title = "Edit Sales Contact:";
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
		Script(out);
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		//If there is a warning from trying to input previously, print it here:
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>" + sStatus + "</B><BR>");
		}
		
		if (request.getParameter("id").trim().compareTo("") == 0){
	    	sWarning = "The sales contact ID cannot be empty.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSalesContactSelect?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + sWarning
			);
			return;
	    }
		
		out.println(SMUtilities.getMasterStyleSheetLink());
		//get current URL
		String sCurrentURL;
		sCurrentURL = clsServletUtilities.URLEncode(request.getRequestURI().toString() 
				+ "?" + request.getQueryString());
	    
	    out.println("<FORM ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSalesContactAction' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
	    out.println("<INPUT TYPE=HIDDEN NAME=OriginalURL VALUE=\"" 
	    		+ clsManageRequestParameters.get_Request_Parameter("OriginalURL", request) + "\">");
	    out.println("<TABLE BORDER=1 WIDTH=\"80%\">");
	    
	    boolean bIsNewRecord = false;
	    if (request.getParameter("id") == null){
	    	bIsNewRecord = true;
	    }else{
		    int iID = -1;
		    try {
				iID = Integer.parseInt(request.getParameter("id"));
			} catch (NumberFormatException e1) {
				iID = -1;
			}
		    if (iID < 0){
		    	bIsNewRecord = true;
		    }
	    }
	    if (bIsNewRecord){
	    	//adding new sales contact record
	    	try{
	    		//Contact ID:
	    		out.println("<TR><TD ALIGN=RIGHT>Sales Contact ID:&nbsp;</TD><TD>" 
		    			+ "<B>(NEW)</B>" + "</TD></TR>");
	    		//Salesperson
	    		String sSQL = MySQLs.Get_Salesperson_List_SQL();
	    		ResultSet rsSalespersons = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	        	sSQL = MySQLs.Get_User_By_Username((String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME));
	        	ResultSet rsUserInfo = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	        	String sDefaultSPCode;
	        	if (rsUserInfo.next()){
	        		sDefaultSPCode = (rsUserInfo.getString(SMTableusers.sDefaultSalespersonCode) + "").trim();
	        	}else{
	        		sDefaultSPCode = "";
	        	}
	        	rsUserInfo.close();
	    		out.println("<TR><TD ALIGN=RIGHT>Salesperson:&nbsp;</TD><TD><SELECT NAME=\"SelectedSalesperson\">"); 
    			while (rsSalespersons.next()){
    				if (rsSalespersons.getString(SMTablesalesperson.sSalespersonCode).compareTo(sDefaultSPCode) == 0){
                		out.println("<OPTION SELECTED VALUE=\"" + rsSalespersons.getString(SMTablesalesperson.sSalespersonCode) + "\">" + 
    		            									    rsSalespersons.getString(SMTablesalesperson.sSalespersonCode) + " - " + 
    		            									    rsSalespersons.getString(SMTablesalesperson.sSalespersonFirstName) + " " + 
    		            									    rsSalespersons.getString(SMTablesalesperson.sSalespersonLastName)); 
            		}else{
    	        		out.println("<OPTION VALUE=\"" + rsSalespersons.getString(SMTablesalesperson.sSalespersonCode) + "\">" + 
    	        									   rsSalespersons.getString(SMTablesalesperson.sSalespersonCode) + " - " + 
    	        									   rsSalespersons.getString(SMTablesalesperson.sSalespersonFirstName) + " " + 
    	        									   rsSalespersons.getString(SMTablesalesperson.sSalespersonLastName));
            		}
    			}
    			rsSalespersons.close();
	    		out.println("</TD></TR>");
	    		
	    		//Customer Number and name
	        	out.println("<TR><TD ALIGN=RIGHT>Customer Account #:&nbsp;</TD>");
	        	out.println("<TD>" + clsCreateHTMLFormFields.TDTextBox(
								"SelectedCustomer", 
								clsManageRequestParameters.get_Request_Parameter("SelectedCustomer", request), 
								10, 
								10, 
								""
								) + "&nbsp;<FONT COLOR=RED>*Requires a valid customer number.</FONT>&nbsp;&nbsp;"
	    		
    				    + "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "SMClasses.ObjectFinder"
	    				+ "?ObjectName=Customer"
	    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    				+ "&ResultClass=FinderResults"
	    				+ "&SearchingClass=" + "smcontrolpanel.SMSalesContactEdit"
	    				+ "&ReturnField=" + "SelectedCustomer"
	    				+ "&SearchField1=" + SMTablearcustomer.sCustomerName
	    				+ "&SearchFieldAlias1=Customer%20Name"
	    				+ "&SearchField2=" + SMTablearcustomer.sAddressLine1
	    				+ "&SearchFieldAlias2=Customer%20Address"
	    				+ "&SearchField3=" + SMTablearcustomer.sContactName
	    				+ "&SearchFieldAlias3=Contact%20Name"
	    				+ "&SearchField4=" + SMTablearcustomer.sEmailAddress
	    				+ "&SearchFieldAlias4=Email%20Address"
	    				+ "&ResultListField1="  + SMTablearcustomer.sCustomerNumber
	    				+ "&ResultHeading1=Customer%20Number"
	    				+ "&ResultListField2="  + SMTablearcustomer.sCustomerName
	    				+ "&ResultHeading2=Customer%20Name"
	    				+ "&ResultListField3="  + SMTablearcustomer.sAddressLine1
	    				+ "&ResultHeading3=Customer%20Address"
	    				+ "&ResultListField4="  + SMTablearcustomer.sContactName
	    				+ "&ResultHeading4=Contact%20Name"
	    				+ "&ResultListField5="  + SMTablearcustomer.sEmailAddress
	    				+ "&ResultHeading5=Email%20Address"
	    				+ "&ResultListField6="  + SMTablearcustomer.iActive
	    				+ "&ResultHeading6=Active"
	    				+ "&ResultListField7="  + SMTablearcustomer.iOnHold
	    				+ "&ResultHeading7=On%20Hold"
	    				+ "&ParameterString=*id=-1"
	    								 + "*OriginalURL=" + request.getParameter("OriginalURL")
	    								 + "*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    				+ "\"> Find customer</A>");
	    		out.println("</TD></TR>");
	    		
	    		//customer contact information
	    		out.println("<TR><TD ALIGN=RIGHT>Contact Name:&nbsp;</TD><TD><INPUT TYPE=TEXT NAME=\"ContactName\" VALUE=\"\" SIZE=30 MAXLENGTH=75><FONT COLOR=RED>*Required.</FONT></TR>");
	    		out.println("<TR><TD ALIGN=RIGHT>Phone Number:&nbsp;</TD><TD><INPUT TYPE=TEXT NAME=\"PhoneNumber\" VALUE=\"\"></TR>");
	    		out.println("<TR><TD ALIGN=RIGHT>Email Address:&nbsp;</TD><TD><INPUT TYPE=TEXT NAME=\"EmailAddress\" VALUE=\"\" SIZE=30 MAXLENGTH=75></TR>");
	    		
	        	//Is Active?
	        	out.println ("<TR><TD ALIGN=RIGHT>Active?&nbsp;</TD><TD>");
	        	out.println ("<LABEL><INPUT TYPE=\"RADIO\" NAME=\"IsInActive\" VALUE=0 CHECKED>Yes</LABEL><BR>");
	        	out.println ("<LABEL><INPUT TYPE=\"RADIO\" NAME=\"IsInActive\" VALUE=1>No</LABEL><BR>");
		        out.println ("</TD></TR>");
		        
		        //Description
		        out.println("<TR><TD ALIGN=RIGHT>Description:&nbsp;</TD><TD><INPUT TYPE=TEXT NAME=\"Description\" VALUE=\"\" SIZE=92 MAXLENGTH=128></TR>");
		        
	        	//notes
	        	out.println("<TR><TD ALIGN=RIGHT>Notes:&nbsp;</TD>" +
	        				"<TD VALIGN=CENTER><TEXTAREA NAME=\"Note\" ROWS=\"8\" COLS=\"70\"></TEXTAREA></TD></TR>");
	        	
	    	}catch(SQLException ex){
	    		out.println("<BR>Error [1396989524] displaying blank form for sales contact - " + ex.getMessage() + ".");
	    	}
	    }else{
	    	//modify existing sales contact record
	    	//TODO make the boxes into editable fields and not just text
	    	try{
	    		String sSQL = "SELECT * FROM " + SMTablesalescontacts.TableName + 
	    				" WHERE" + 
	    				" " + SMTablesalescontacts.id + " = " + Integer.parseInt(request.getParameter("id"));
	    		ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
		    	if (rs.next()){
		    		//Contact ID:
		    		out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"id\" VALUE=" + request.getParameter("id") + "><BR>");
		    		out.println("<TR><TD ALIGN=RIGHT>Sales Contact ID:&nbsp;</TD><TD><B>" 
		    			+ request.getParameter("id") + "</B></TD></TR>");
		    		
		    		//Salesperson
		    		out.println("<TR><TD ALIGN=RIGHT>Salesperson:&nbsp;</TD><TD><SELECT NAME=\"SelectedSalesperson\">"); 
		    		out.println("<OPTION SELECTED VALUE=\"" + rs.getString(SMTablesalescontacts.salespersoncode) + "\"> " + rs.getString(SMTablesalescontacts.salespersoncode) +" - " + rs.getString(SMTablesalescontacts.sSalespersonName));
		    		sSQL = MySQLs.Get_Salesperson_List_SQL();
		    		ResultSet rsSalespersons = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
		    		while(rsSalespersons.next()) {
		    		out.println("<OPTION VALUE=\"" + rsSalespersons.getString(SMTablesalesperson.sSalespersonCode) + "\">" + 
							   rsSalespersons.getString(SMTablesalesperson.sSalespersonCode) + " - " + 
							   rsSalespersons.getString(SMTablesalesperson.sSalespersonFirstName) + " " + 
							   rsSalespersons.getString(SMTablesalesperson.sSalespersonLastName));
		    		}
		    		out.println("</TD></TR>");

		    		//Customer Number and name
		    		boolean bAllowCustomerView = 
		    				SMSystemFunctions.isFunctionPermitted(
		    						SMSystemFunctions.ARDisplayCustomerInformation, 
		    						sUserID, 
		    						getServletContext(), 
		    						sDBID,
		    						(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
		    						);
		    		
		    		String sCustomerNumber = rs.getString(SMTablesalescontacts.scustomernumber).toUpperCase();
		    		String sCustomerInfoLink = sCustomerNumber;
					if (bAllowCustomerView){
						sCustomerInfoLink = 
							"<A ID=\"CustomerLink\" HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARDisplayCustomerInformation?CustomerNumber=" 
							+ sCustomerNumber + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" 
							+ rs.getString(SMTablesalescontacts.scustomername) + "</A>"
						;
					}
		    		out.println("<TR><TD ALIGN=RIGHT>Customer Account #:&nbsp;</TD	>" );
		    		out.println("<TD><INPUT TYPE=TEXT ONCHANGE=\"customer()\" NAME=\"SelectedCustomer\" VALUE=\"" + rs.getString(SMTablesalescontacts.scustomernumber) + "\">&nbsp;" + sCustomerInfoLink + "</TD></TR>");
		    		
		    		//customer contact information
		    		out.println("<TR><TD ALIGN=RIGHT>Contact Name:&nbsp;</TD><TD>");
		    		out.println("<INPUT TYPE=TEXT NAME=\"ContactName\" VALUE=\"" + rs.getString(SMTablesalescontacts.scontactname) + "\">"+ "</TD></TR>");
		    		String PhoneNumber = rs.getString(SMTablesalescontacts.sphonenumber);
		    		if(PhoneNumber.compareToIgnoreCase("")==0) {
		    		out.println("<TR><TD ALIGN=RIGHT>Phone Number:&nbsp;</TD><TD><INPUT TYPE=TEXT NAME=\"PhoneNumber\" VALUE=\"" + PhoneNumber + "\"></TD></TR>");
		    		} else {
			    		out.println("<TR><TD ALIGN=RIGHT><A HREF=\"tel:" + PhoneNumber + "\">Phone Number:</A>&nbsp;</TD><TD><INPUT TYPE=TEXT NAME=\"PhoneNumber\" VALUE=\"" + PhoneNumber+ "\"></TD></TR>");
		    		}
		    		out.println("<TR><TD ALIGN=RIGHT>Email Address:&nbsp;</TD><TD><INPUT TYPE=TEXT NAME=\"EmailAddress\" VALUE=\"" + rs.getString(SMTablesalescontacts.semailaddress) + "\"></TD></TR>");
		    		
		        	//Is Active?
		        	out.println ("<TR><TD ALIGN=RIGHT>Active?&nbsp;</TD><TD>");
		        	if (rs.getInt(SMTablesalescontacts.binactive) == 0){
		        		out.println ("<INPUT TYPE=\"RADIO\" NAME=\"IsInActive\" VALUE=0 CHECKED>Yes<BR>");
		        		out.println ("<INPUT TYPE=\"RADIO\" NAME=\"IsInActive\" VALUE=1>No<BR>");
		        	}else{
		        		out.println ("<INPUT TYPE=\"RADIO\" NAME=\"IsInActive\" VALUE=0>Yes<BR>");
		        		out.println ("<INPUT TYPE=\"RADIO\" NAME=\"IsInActive\" VALUE=1 CHECKED>No<BR>");
		        	}
			        out.println ("</TD></TR>");
			        
			        //Description
			        out.println("<TR><TD ALIGN=RIGHT>Description:&nbsp;</TD><TD><INPUT TYPE=TEXT NAME=\"Description\" VALUE=\"" + rs.getString(SMTablesalescontacts.sdescription) + "\" SIZE=92 MAXLENGTH=128></TD></TR>");
			        
		        	//notes
		        	out.println("<TR><TD ALIGN=RIGHT>Notes:&nbsp;</TD>" +
		        				"<TD VALIGN=CENTER><TEXTAREA NAME=\"Note\" ROWS=\"8\" COLS=\"70\">" + 
		        				rs.getString(SMTablesalescontacts.mnotes).trim() + 
		        				"</TEXTAREA></TD></TR>");

		        	//Related critical dates
		        	Connection conn;
    				try {
    					conn = clsDatabaseFunctions.getConnectionWithException(
    						getServletContext(), 
    						sDBID, 
    						"MySQL", 
    						SMUtilities.getFullClassName(this.toString()) + ".displayOrder - userID: " + sUserID
    					);
    				} catch (Exception e) {
    					out.println ("Error [1411071471] getting connection - " + e.getMessage());
    					return;
    				}
    				String sLicenseModuleLevel = (String) request.getSession(true).getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL);
		        	boolean bAllowEditCriticaldDates = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMEditCriticalDate, sUserID, conn, sLicenseModuleLevel);
		        	
		        	out.println("<TR><TD ALIGN=RIGHT>Critical Dates:&nbsp;</TD>" +
		        				"<TD VALIGN=CENTER>");
		        	
		        	String SQL = "SELECT * "
		    				+ " FROM " + SMTablecriticaldates.TableName			
		    				+ " WHERE ("
		    				+ "		(" + SMTablecriticaldates.itype + " = " + Integer.toString(SMTablecriticaldates.SALES_CONTACT_RECORD_TYPE) + ")"
		    				+ " AND (" + SMTablecriticaldates.sdocnumber + " = '" + request.getParameter("id") + "')"
		    				+ " )"
		    				+ " ORDER BY " + SMTablecriticaldates.sCriticalDate + " , " + SMTablecriticaldates.sId
		    				;    
		        	ResultSet rsCriticalDates =  clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
		        	out.println("<TABLE BORDER=0 WIDTH=100%><TR>");
		        	
		        	if(rsCriticalDates.isBeforeFirst()) {	
		        		out.println("<TD class = \" centerjustifiedheading \" ><FONT SIZE=2><B>ID</B></FONT></TD>");
		        		out.println("<TD class = \" leftjustifiedheading \"><FONT SIZE=2><B>Date</B></FONT></TD>");
		        		out.println("<TD class = \" leftjustifiedheading \"><FONT SIZE=2><B>Resolved?</B></FONT></TD>");
		        		out.println("<TD class = \" leftjustifiedheading \"><FONT SIZE=2><B>Responsible</FONT></B></TD>");
		        		out.println("<TD class = \" leftjustifiedheading \"><FONT SIZE=2><B>Assigned&nbsp;by</FONT></B></TD>");
		        		out.println("<TD class = \" leftjustifiedheading \"><FONT SIZE=2><B>Comments</FONT></B></TD>");
					}
		        	out.println("</TR>");
		        	
					boolean bOddRow = false;
					String sRowColorClass = "";
					while (rsCriticalDates.next()){
    					
						if(!bOddRow){
							sRowColorClass = "\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + "\"";
						}else{
							sRowColorClass = "\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY + "\"";
						}
						if (rsCriticalDates.getLong(SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sId) > 0L){
							out.println("<TR  class =" + sRowColorClass +" >");
							String sCriticalDateID = Long.toString(rsCriticalDates.getLong(SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sId)); 
							String sCriticalDateIDLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
							+ "smcontrolpanel.SMCriticalDateEdit"
							+ "?" + SMTablecriticaldates.sId + "=" + sCriticalDateID 
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
							+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sCriticalDateID) + "</A>";
			
							if (bAllowEditCriticaldDates){
								out.println("<TD ALIGN=CENTER style=\"vertical-align:top; border-style:none; font-family: Arial; font-weight: normal; font-size: small;\"><FONT SIZE=2>" + sCriticalDateIDLink + "</FONT></TD>");	
							}else{
								out.println("<TD ALIGN=CENTER style=\"vertical-align:top; border-style:none; font-family: Arial; font-weight: normal; font-size: small;\"><FONT SIZE=2>" + sCriticalDateID + "</FONT></TD>");
							}
			
							String sFontColor = "BLACK";
							//Critical Date
							out.println("<TD ALIGN=CENTER style=\"vertical-align:top; border-style:none; font-family: Arial; font-weight: normal; font-size: small;\"><FONT SIZE=2 COLOR=" + sFontColor + ">" 
								+ clsDateAndTimeConversions.resultsetDateStringToString(
										rsCriticalDates.getString(SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sCriticalDate)) 
								+ "</FONT></TD>")
							;
							
							//Resolved:
							out.println("<TD  ALIGN=CENTER style=\"vertical-align:top; border-style:none; font-family: Arial; font-weight: normal; font-size: small;\"><FONT SIZE=2 COLOR=" + sFontColor + " >");

							if(rsCriticalDates.getInt(SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sResolvedFlag) == 1) {
								out.println("Yes");
							}else {
								out.println("No");
							}
							
							out.println("</FONT></TD>");
							
							//Responsible
							out.println("<TD VALIGN=CENTER style=\"vertical-align:top; border-style:none; font-family: Arial; font-weight: normal; font-size: small;\" nowrap><FONT SIZE=2 COLOR=" + sFontColor + ">" 
								+ rsCriticalDates.getString(SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sresponsibleuserfullname) 
								+ "</FONT></TD>")
							;
			
							//Assigned by				
							out.println("<TD ALIGN=CENTER style=\"vertical-align:top; border-style:none; font-family: Arial; font-weight: normal; font-size: small;\" nowrap><FONT SIZE=2 COLOR=" + sFontColor + ">" 
									+ rsCriticalDates.getString(SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sassignedbyuserfullname) 
									+ "</FONT></TD>")
								;
							
							//Comments
							out.println("<TD ALIGN=TOP style=\"vertical-align:top; border-style:none; font-family: Arial; font-weight: normal; font-size: small;\"><FONT SIZE=2 COLOR=" + sFontColor + ">" 
									+ rsCriticalDates.getString(SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sComments) 
									+ "</FONT></TD>")
								;
							out.println("</TR>");
							
							bOddRow = !bOddRow;
						}
					}
					rsCriticalDates.close();
	  
					out.println("<TR><TD COLSPAN=6>");
					out.println(SMCriticalDateEntry.addNewCriticalDateLink(
							Integer.toString(SMTablecriticaldates.SALES_CONTACT_RECORD_TYPE), 
							request.getParameter("id"), 
							sUserID, 
							getServletContext(), 
							sDBID));
					out.println("</TD></TR>");	
					out.println("</TD></TR>");
		   				out.println("</TABLE>");		
		   				
		        	//Related sales leads
		        	sSQL = "SELECT * FROM" 
		        		+ " " + SMTablebids.TableName 
		        		+ " WHERE (" 
		        			+ SMTablebids.isalescontactid + "=" + rs.getInt(SMTablesalescontacts.id)
		        		+ ") ORDER BY " + SMTablebids.dattimeoriginationdate + ""; 
		        	
		        	out.println("<TR><TD ALIGN=RIGHT>Related " + SMBidEntry.ParamObjectName + "s:&nbsp;</TD>" +
		        				"<TD VALIGN=CENTER>");
		        	ResultSet rsbids =  clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	        		String sOriginationDate = "";
	        		out.println("<TABLE WIDTH=100%>");
	        		if(rsbids.isBeforeFirst()) {
	        			
	        			out.println("<TR>" 
    						+ "<TD class = \" centerjustifiedheading \" ><FONT SIZE=2><B>" + "Origination&nbsp;Date</B></FONT></TD>"
    						+ "<TD class = \" centerjustifiedheading \" ><FONT SIZE=2><B>ID</B></FONT></TD>" 
    						+ "<TD class = \" leftjustifiedheading \" ><FONT SIZE=2><B>Customer&nbsp;Name</B></FONT></TD>" 
    						+ "<TD class = \" leftjustifiedheading \" ><FONT SIZE=2><B>Project&nbsp;Name</B></FONT></TD>" 
    						+ "<TD class = \" centerjustifiedheading \" ><FONT SIZE=2><B>Status</B></FONT></TD>" 
    						+ "</TR>"
    					);
	        		}
	        		
	        		bOddRow = false;
					sRowColorClass = "";
					while (rsbids.next()){
		        		try{
		        			sOriginationDate = Dateformatter.format(rsbids.getDate(SMTablebids.dattimeoriginationdate));
		        		}catch(SQLException e){
			        		sOriginationDate = "N/A";
		        		}
		        		if (rsbids.getString(SMTablebids.dattimeoriginationdate).compareTo("0000-00-00 00:00:00") == 0){
		        			sOriginationDate = "00/00/0000";
		        		}
		        		
		        		if(!bOddRow){
							sRowColorClass = "\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + "\"";
						}else{
							sRowColorClass = "\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY + "\"";
						}
		        		
    					out.println("<TR class=" +sRowColorClass + ">" 
    						+ "<TD ALIGN=CENTER style=\"vertical-align:bottom; border-style:none; font-family: Arial; font-weight: normal; font-size: small;\">" + sOriginationDate + "</TD>" 
    						+ "<TD ALIGN=CENTER style=\"vertical-align:bottom; border-style:none; font-family: Arial; font-weight: normal; font-size: small;\">" 
    						+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditBidEntry" 
    						+ "?" + SMBidEntry.ParamID + "=" + rsbids.getInt(SMTablebids.lid) 
    						+ "&OriginalURL=" + sCurrentURL 
    						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
    						+ "\">" 
    						+ rsbids.getInt(SMTablebids.lid) + "</A></TD>" 
    						+ "<TD ALIGN=LEFT style=\"vertical-align:bottom; border-style:none; font-family: Arial; font-weight: normal; font-size: small;\">" + rsbids.getString(SMTablebids.scustomername) + "</TD>" 
    						+ "<TD ALIGN=LEFT style=\"vertical-align:bottom; border-style:none; font-family: Arial; font-weight: normal; font-size: small;\">" + rsbids.getString(SMTablebids.sprojectname) + "</TD>" 
    						+ "<TD ALIGN=CENTER style=\"vertical-align:bottom; border-style:none; font-family: Arial; font-weight: normal; font-size: small;\">" + rsbids.getString(SMTablebids.sstatus) + "</TD>" 
    						+ "</TR>"
   						);
    					bOddRow = !bOddRow;
					}
    				rsbids.close();
    				
    				out.println("<TR><TD COLSPAN=6>");
    				out.println(addNewSalesLeadLink(
    						request, 
    						sUserID, 
    						getServletContext(), 
    						sDBID));
    				out.println("</TD></TR>");	
    				
					out.println("</TABLE>");
    				out.println("</TD></TR>");
		        	
    				//Related appointments

    				boolean bAllowEditAppointments = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMEditAppointmentCalendar, sUserID, conn, sLicenseModuleLevel);
    				
		        	sSQL = "SELECT * FROM" 
		        		+ " " + SMTableappointments.TableName 
		        		+ " WHERE (" 
		        			+ SMTableappointments.isalescontactid + "=" + rs.getInt(SMTablesalescontacts.id)
		        		+ ") ORDER BY " + SMTableappointments.datentrydate + ", " + SMTableappointments.iminuteofday ; 
		        	
		        	out.println("<TR><TD ALIGN=RIGHT>Related " + SMAppointment.ParamObjectName + "s:&nbsp;</TD>" +
	        				"<TD VALIGN=CENTER>");
		        	ResultSet rsappointments =  clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
		        	out.println("<TABLE WIDTH=100%>");
		        	if(rsappointments.isBeforeFirst()) {
		        		out.println( "<TR>" 
    						+ "<TD class = \" centerjustifiedheading \" ><FONT SIZE=2><B>" + "Date/Time</B></FONT></TD>"
    						+ "<TD class = \" leftjustifiedheading \" ><FONT SIZE=2><B>ID</B></FONT></TD>" 
    						+ "<TD class = \" leftjustifiedheading \" ><FONT SIZE=2><B>Schedule&nbsp;for</B></FONT></TD>" 
    						+ "<TD class = \" leftjustifiedheading \" ><FONT SIZE=2><B>Comment</B></FONT></TD>" 
    						+ "</TR>"
    					);
		        	}
					String sAppointmentDate = "";
					bOddRow = false;
					sRowColorClass = "";
					while (rsappointments.next()){
		        		try{
		        			sAppointmentDate = Dateformatter.format(rsappointments.getDate(SMTableappointments.datentrydate));
		        		}catch(Exception e){
			        		sOriginationDate = "N/A";
		        		}

		        		if(!bOddRow){
							sRowColorClass = "\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + "\"";
						}else{
							sRowColorClass = "\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY + "\"";
						}
		        		
    					out.println("<TR class=" + sRowColorClass +">");
    					out.println("</TD>" 
        						+ "<TD ALIGN=CENTER style=\"vertical-align:top; border-style:none; font-family: Arial; font-weight: normal; font-size: small;\">" 
        						 + sAppointmentDate + "<br> " 
        						//+ SMAppointment.timeIntegerToString(rsappointments.getInt(SMTableappointments.iminuteofday)) 
        						+ "</TD>");
    					
    					out.println("<TD ALIGN=CENTER style=\"vertical-align:top; border-style:none; font-family: Arial; font-weight: normal; font-size: small;\">"); 
    							
    					if(bAllowEditAppointments){		
    						out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditAppointmentEdit?" 
    								+ SMAppointment.Paramlid + "=" + rsappointments.getString(SMTableappointments.lid) 
    								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    								+ "\">"
    								+ rsappointments.getString(SMTableappointments.lid) + "</A>");
    					}else{
    						out.println(Integer.toString(rsappointments.getInt(SMTableappointments.lid)));
    					}
    							

    					out.println("<TD ALIGN=LEFT style=\"vertical-align:top; border-style:none; font-family: Arial; font-weight: normal; font-size: small;\">" + SMUtilities.getFullNamebyUserID(rsappointments.getString(SMTableappointments.luserid), conn) + "</TD>"); 
    					out.println("<TD ALIGN=LEFT style=\"vertical-align:top; border-style:none; font-family: Arial; font-weight: normal; font-size: small;\">" + rsappointments.getString(SMTableappointments.mcomment) + "</TD>"); 
    					out.println("</TR>");
    					bOddRow = !bOddRow;
					}	    	
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080658]");
				rsappointments.close();

				out.println("<TR><TD COLSPAN=6>");
				out.println(addNewAppointmentLink(
						request, 
						sUserID, 
						getServletContext(), 
						sDBID));
				out.println("</TD></TR>");	
				
				out.println("</TABLE>");
				out.println("</TD></TR>");
	    	    
				//save and remove buttons
				out.println("</TABLE>");
				out.println("<INPUT TYPE=\"SUBMIT\" NAME=\"SUBMITSAVE\" VALUE=\" Save \">");
				out.println("<INPUT TYPE=\"SUBMIT\" NAME=\"SUBMITREMOVE\" VALUE=\" Remove \" ONCLICK=\"return confirm('This Sales Contact will be deleted.')\">  ");
				out.println(" <INPUT TYPE=\"BUTTON\" "
						+ "ONCLICK=\"location.href = \'/sm/smcontrolpanel.SMSalesContactEdit?id=-1&OriginalURL=%2Fsm%2Fsmcontrolpanel.SMSalesContactSelect%3Fdb%3DServMgr1%26SalesContactID%3D4&db=ServMgr1\'\""
						+ " VALUE =\"Create New Sales Contact\"/>");

				
		    	}else{
		    		out.println("<BR>No sales contact record found with this ID.");
		    	}
		    	rs.close();
	    	}catch(SQLException ex){

	    		out.println("<BR>Error [1396989523] reading sales contact record - " + ex.getMessage() + ".");
	    	}    	
	    }
	    if(bIsNewRecord) {
			out.println("</TABLE>");
			out.println("<INPUT TYPE=\"SUBMIT\" NAME=\"SUBMITSAVE\" VALUE=\" Save \">");
	    }
		out.println("</BODY></HTML>");
	}

	private String addNewSalesLeadLink(
			HttpServletRequest request, 
			String sUserID, 
			ServletContext servletContext,
			String sDBID) {
		//Create redirect string for appointment
		String s = ""; 
		String sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditBidEntry?"
				+ SMBidEntry.ParamID + "=-1"
				+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=Y" 
				+ "&" + SMBidEntry.Paramisalescontactid + "=" + request.getParameter("id")
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				;

		if (SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMEditBids, 
				sUserID, 
				getServletContext(), 
				sDBID,
				(String) request.getSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
			s+="&nbsp;<FONT size=2><A HREF=\"" + sRedirectString + "\"/>Add new sales lead</A></FONT>";
		}

		return s;
	}

	private String addNewAppointmentLink(
			HttpServletRequest request,
			String sUserID, 
			ServletContext servletContext,
			String sDBID) {
		String s = "";
       int iSalesContactID = -1;
    	
    	try {
			iSalesContactID = Integer.parseInt(request.getParameter("id"));
		} catch (NumberFormatException e1) {
			iSalesContactID = -1;
		}
    	
    	//If the sales contact already exists.
		if (iSalesContactID >= 0){
    		String sDefautUserID = "";
    		String sRedirectString = "";
    		String sSalespersonCode = "";
    		String sPhoneNumber = "";
    		String sEmail = "";
    		String sContactName = "";
    		String sCustomerName = "";
    		//Get the default user to create an appointment with
   		 try {
	    	String sSQL = "SELECT * FROM " + SMTablesalescontacts.TableName + 
	  			  " WHERE" + 
				  	" " + SMTablesalescontacts.id + " = " + iSalesContactID;
	    	ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	    	if(rs.next()){
	    	 sSalespersonCode = rs.getString(SMTablesalescontacts.salespersoncode);
	    	 sPhoneNumber = rs.getString(SMTablesalescontacts.sphonenumber);
	    	 sEmail = rs.getString(SMTablesalescontacts.semailaddress);
	    	 sContactName = rs.getString(SMTablesalescontacts.scontactname);
	    	 sCustomerName = rs.getString(SMTablesalescontacts.scustomername);
	    	}
    		rs.close();

    		String SQL = "SELECT " + SMTableusers.TableName + "." + SMTableusers.sUserName
    				+ ", " + SMTableusers.TableName + "." + SMTableusers.lid
    				+ " FROM " + SMTableusers.TableName
    				+ " LEFT JOIN " + SMTablesalesperson.TableName
    				+ " ON " + SMTablesalesperson.TableName + "." + SMTablesalesperson.lSalespersonUserID + " = "
    				+ SMTableusers.TableName + "." + SMTableusers.lid
    				+ " WHERE ("
    				+ SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode + " = '" + sSalespersonCode + "'"
    				+ ")"
    				
    				;

					ResultSet rsDefaultUser = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
							sDBID, "MySQL", SMUtilities
									.getFullClassName(this.toString())
									+ ".getEditHTML - user: " 
									+ sUserID
							);
					if (rsDefaultUser.next()) {
						sDefautUserID = rsDefaultUser.getString(SMTableusers.TableName + "." + SMTableusers.lid);
					}
					rsDefaultUser.close();

   		 //Create redirect string for appointment
			 sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditAppointmentEdit?"
					+ SMAppointment.Paramlid + "=-1"
	    			+ "&" + SMAppointment.Paramluserid + "=" + clsServletUtilities.URLEncode(sDefautUserID)
	    			+ "&" + SMAppointment.Paramsphone + "=" + clsServletUtilities.URLEncode(sPhoneNumber)
	    			+ "&" + SMAppointment.Paramsemail + "=" +  clsServletUtilities.URLEncode(sEmail)
	    			+ "&" + SMAppointment.Paramsbilltoname + "=" + clsServletUtilities.URLEncode(sCustomerName)
	    			+ "&" + SMAppointment.Paramscontactname + "=" + clsServletUtilities.URLEncode(sContactName)
	    			+ "&" + SMAppointment.Paramisalescontactid + "=" + clsServletUtilities.URLEncode(Integer.toString(iSalesContactID))
	    			+ "&" + SMAppointment.Paramdatentrydate + "=" + SMUtilities.EMPTY_DATE_VALUE
	    			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					;

			} catch (SQLException e) {
				System.out.println("[1579274920] ERROR: " + e.getMessage());
			}
    		if (SMSystemFunctions.isFunctionPermitted(
    				SMSystemFunctions.SMEditAppointmentCalendar, 
    				sUserID, 
    				getServletContext(), 
    				sDBID,
    				(String) request.getSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
       			s+="&nbsp;<FONT size=2><A HREF=\"" + sRedirectString + "\"/>Add new appointment</A></FONT>";
       			}
		}
		return s;
	}
	
	public void Script(PrintWriter out) {
		 out.println("<script>\n"
		 		+ "function customer(){\n"
		 		+ "\tdocument.getElementById(\"CustomerLink\").style.visibility = \"hidden\";\n"
		 		+ "}\n"
				 +"</script>");
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		doPost(request, response);
	}
}
