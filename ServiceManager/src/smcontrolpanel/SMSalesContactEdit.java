package smcontrolpanel;

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
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SMSalesContactEdit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String sDBID = "";
	//private static String sUserName = "";
	private static String sUserID = "";
	private static String sUserFullName = "";
	private static String sCompanyName = "";
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
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID); 
	    sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " " 
    					+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String title = "";
		String subtitle = "";
		
    	//User has chosen to edit:
		title = "Edit Sales Contact:";
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
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
		
		//get current URL
		String sCurrentURL;
		sCurrentURL = clsServletUtilities.URLEncode(request.getRequestURI().toString() 
				+ "?" + request.getQueryString());
	    
	    out.println("<FORM ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSalesContactAction' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
	    out.println("<INPUT TYPE=HIDDEN NAME=OriginalURL VALUE=\"" 
	    		+ clsManageRequestParameters.get_Request_Parameter("OriginalURL", request) + "\">");
	    out.println("<TABLE BORDER=1>");
	    
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
	    		String sSQL = SMMySQLs.Get_Salesperson_List_SQL();
	    		ResultSet rsSalespersons = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	        	sSQL = SMMySQLs.Get_User_By_Username((String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME));
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
	    		
    				    + "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
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
	    		
	    		Calendar c = Calendar.getInstance();
	    		//last contact date, default to today.
	    		c.setTimeInMillis(System.currentTimeMillis());
	    		out.println("<TR><TD ALIGN=RIGHT>Last Contact Date:&nbsp;</TD>"); 
	    		out.println("<TD>");
	    		out.println(
	    				clsCreateHTMLFormFields.TDTextBox(
	    						"LastContactDate",
	    						clsDateAndTimeConversions.now("M/d/yyyy"),
	    						10, 
	    						10, 
	    						""
	    					) 
	    				+ SMUtilities.getDatePickerString("LastContactDate", getServletContext())
	    				);
	    		out.println("</TD>");
		    	out.println("</TR>");
        	
	    		//next contact date, default to a month from now
	    		c.add(Calendar.MONTH, 1);
	    		out.println("<TR><TD ALIGN=RIGHT>Next Contact Date:&nbsp;</TD>"); 
	    		out.println("<TD>"); 
	    		out.println(
	    				clsCreateHTMLFormFields.TDTextBox(
	    						"NextContactDate", 
	    						clsDateAndTimeConversions.utilDateToString((new Date(c.getTimeInMillis())),"M/d/yyyy"), 
	    						10, 
	    						10, 
	    						""
	    					) 
	    				+ SMUtilities.getDatePickerString("NextContactDate", getServletContext())
	    				);
	    		out.println("</TD>"); 
	    		out.println("</TR>");
	    		
	        	//Is Active?
	        	out.println ("<TR><TD ALIGN=RIGHT>Active?&nbsp;</TD><TD>");
	        	out.println ("<INPUT TYPE=\"RADIO\" NAME=\"IsInActive\" VALUE=0 CHECKED>Yes<BR>");
	        	out.println ("<INPUT TYPE=\"RADIO\" NAME=\"IsInActive\" VALUE=1>No<BR>");
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
	    	try{
		    	String sSQL = SMMySQLs.Get_Sales_Contact_By_ID_SQL(Integer.parseInt(request.getParameter("id")));
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
		    	if (rs.next()){
		    		//Contact ID:
		    		out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"id\" VALUE=" + request.getParameter("id") + "><BR>");
		    		out.println("<TR><TD ALIGN=RIGHT>Sales Contact ID:&nbsp;</TD><TD><B>" 
		    			+ request.getParameter("id") + "</B></TD></TR>");
		    		
		    		//Salesperson
		    		sSQL = SMMySQLs.Get_Salesperson_By_Salescode(rs.getString(SMTablesalescontacts.salespersoncode));
		    		ResultSet rsSalesperson = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
		    		out.println("<TR><TD ALIGN=RIGHT>Salesperson:&nbsp;</TD><TD>"); 
	    			while (rsSalesperson.next()){
		    			out.print(rsSalesperson.getString(SMTablesalesperson.sSalespersonCode) + " - " + 
							      rsSalesperson.getString(SMTablesalesperson.sSalespersonLastName) + ", " + 
							      rsSalesperson.getString(SMTablesalesperson.sSalespersonFirstName));
	    			}
	    			rsSalesperson.close();
	    			out.println("<INPUT TYPE=HIDDEN NAME=\"SelectedSalesperson\" VALUE=\"" + rs.getString(SMTablesalescontacts.salespersoncode) + "\">");
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
							"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARDisplayCustomerInformation?CustomerNumber=" 
							+ sCustomerNumber + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" 
							+ sCustomerNumber + "</A>"
						;
					}
		    		out.println("<TR><TD ALIGN=RIGHT>Customer Account #:&nbsp;</TD><TD>" 
		    				+ sCustomerInfoLink 
		    				+ " - " + rs.getString(SMTablesalescontacts.scustomername) + "</TR>");
		    		out.println("<INPUT TYPE=HIDDEN NAME=\"SelectedCustomer\" VALUE=\"" + rs.getString(SMTablesalescontacts.scustomernumber) + "\">");
		    		
		    		//customer contact information
		    		out.println("<TR><TD ALIGN=RIGHT>Contact Name:&nbsp;</TD><TD>&nbsp;" + rs.getString(SMTablesalescontacts.scontactname) + "</TD></TR>");
		    		out.println("<INPUT TYPE=HIDDEN NAME=\"ContactName\" VALUE=\"" + rs.getString(SMTablesalescontacts.scontactname) + "\">");
		    		
		    		out.println("<TR><TD ALIGN=RIGHT>Phone Number:&nbsp;</TD><TD><INPUT TYPE=TEXT NAME=\"PhoneNumber\" VALUE=\"" + rs.getString(SMTablesalescontacts.sphonenumber) + "\"></TD></TR>");
		    		out.println("<TR><TD ALIGN=RIGHT>Email Address:&nbsp;</TD><TD><INPUT TYPE=TEXT NAME=\"EmailAddress\" VALUE=\"" + rs.getString(SMTablesalescontacts.semailaddress) + "\"></TD></TR>");
		    		
		    		//last contact date
		    		out.println("<TR><TD ALIGN=RIGHT>Last Contact Date:&nbsp;</TD>"); 
		    		out.println("<TD>");
		    		out.println(
		    				clsCreateHTMLFormFields.TDTextBox(
		    						"LastContactDate",
		    						clsDateAndTimeConversions.utilDateToString(rs.getDate(SMTablesalescontacts.datlastcontactdate), "M/d/yyyy"),
		    						10, 
		    						10, 
		    						""
		    					) 
		    				+ SMUtilities.getDatePickerString("LastContactDate", getServletContext())
		    				);
		    		out.println("</TD>");
			    	out.println("</TR>");
	        	
		    		//next contact date
		    		out.println("<TR><TD ALIGN=RIGHT>Next Contact Date:&nbsp;</TD>"); 
		    		out.println("<TD>");
		    		out.println(
		    				clsCreateHTMLFormFields.TDTextBox(
		    						"NextContactDate",
		    						clsDateAndTimeConversions.utilDateToString(rs.getDate(SMTablesalescontacts.datnextcontactdate), "M/d/yyyy"),
		    						10, 
		    						10, 
		    						""
		    					) 
		    				+ SMUtilities.getDatePickerString("NextContactDate", getServletContext())
		    				);
		    		out.println("</TD>");
			    	out.println("</TR>");
		        	
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

		        	//Related sales leads
		        	sSQL = "SELECT * FROM" 
		        		+ " " + SMTablebids.TableName 
		        		+ " WHERE (" 
		        			+ SMTablebids.isalescontactid + "=" + rs.getInt(SMTablesalescontacts.id)
		        		+ ") ORDER BY " + SMTablebids.dattimebiddate + " DESC"; 
		        	
		        	out.println("<TR><TD ALIGN=RIGHT>Related " + SMBidEntry.ParamObjectName + "s:&nbsp;</TD>" +
		        				"<TD VALIGN=CENTER>");
		        	ResultSet rsbids =  clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	        		String sOriginationDate = "";
					out.println("<TABLE WIDTH=100%>"
    						+ "<TR>" 
    						+ "<TD ALIGN=CENTER WIDTH=15% style=\"vertical-align:bottom; border-bottom:thin solid black; font-family: Arial; font-weight: normal; font-size: small;\"><B>" + "Origination Date</B></TD>"
    						+ "<TD ALIGN=CENTER WIDTH=10% style=\"vertical-align:bottom; border-bottom:thin solid black; font-family: Arial; font-weight: normal; font-size: small;\"><B>ID</B></TD>" 
    						+ "<TD ALIGN=LEFT WIDTH=30% style=\"vertical-align:bottom; border-bottom:thin solid black; font-family: Arial; font-weight: normal; font-size: small;\"><B>Customer Name</B></TD>" 
    						+ "<TD ALIGN=LEFT WIDTH=35% style=\"vertical-align:bottom; border-bottom:thin solid black; font-family: Arial; font-weight: normal; font-size: small;\"><B>Project Name</B></TD>" 
    						+ "<TD ALIGN=CENTER WIDTH=10% style=\"vertical-align:bottom; border-bottom:thin solid black; font-family: Arial; font-weight: normal; font-size: small;\"><B>Status</B></TD>" 
    						+ "</TR>"
    					);
					while (rsbids.next()){
		        		try{
		        			sOriginationDate = Dateformatter.format(rsbids.getDate(SMTablebids.dattimeoriginationdate));
		        		}catch(SQLException e){
			        		sOriginationDate = "N/A";
		        		}
		        		if (rsbids.getString(SMTablebids.dattimeoriginationdate).compareTo("0000-00-00 00:00:00") == 0){
		        			sOriginationDate = "00/00/0000";
		        		}
    					out.println("<TR>" 
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
					}
    				rsbids.close();
    				
					out.println("</TABLE>");
    				out.println("</TD></TR>");
		        	
    				//Related appointments
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
    				boolean bAllowEditAppointments = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMEditAppointmentCalendar, sUserID, conn, sLicenseModuleLevel);
    	//			boolean bAllowViewAppointments = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMViewAppointmentCalendar, sUserName, conn, sLicenseModuleLevel);
    				
		        	sSQL = "SELECT * FROM" 
		        		+ " " + SMTableappointments.TableName 
		        		+ " WHERE (" 
		        			+ SMTableappointments.isalescontactid + "=" + rs.getInt(SMTablesalescontacts.id)
		        		+ ") ORDER BY " + SMTableappointments.datentrydate + ", " + SMTableappointments.iminuteofday ; 
		        	
		        	out.println("<TR><TD ALIGN=RIGHT>Related " + SMAppointment.ParamObjectName + "s:&nbsp;</TD>" +
	        				"<TD VALIGN=CENTER>");
		        	ResultSet rsappointments =  clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
					out.println("<TABLE WIDTH=100%>"
    						+ "<TR>" 
    						+ "<TD ALIGN=CENTER WIDTH=15% style=\"vertical-align:bottom; border-bottom:thin solid black; font-family: Arial; font-weight: normal; font-size: small;\"><B>" + "Date/Time</B></TD>"
    						+ "<TD ALIGN=CENTER WIDTH=10% style=\"vertical-align:bottom; border-bottom:thin solid black; font-family: Arial; font-weight: normal; font-size: small;\"><B>ID</B></TD>" 
    						+ "<TD ALIGN=LEFT WIDTH=25% style=\"vertical-align:bottom; border-bottom:thin solid black; font-family: Arial; font-weight: normal; font-size: small;\"><B>Schedule for</B></TD>" 
    						+ "<TD ALIGN=LEFT WIDTH=50% style=\"vertical-align:bottom; border-bottom:thin solid black; font-family: Arial; font-weight: normal; font-size: small;\"><B>Comment</B></TD>" 
    						+ "</TR>"
    					);
					String sAppointmentDate = "";
					while (rsappointments.next()){
		        		try{
		        			sAppointmentDate = Dateformatter.format(rsappointments.getDate(SMTableappointments.datentrydate));
		        		}catch(Exception e){
			        		sOriginationDate = "N/A";
		        		}

    					out.println("<TR>");
    					out.println("</TD>" 
        						+ "<TD ALIGN=CENTER style=\"vertical-align:top; border-style:none; font-family: Arial; font-weight: normal; font-size: small;\">" 
        						 + sAppointmentDate + "<br> " 
        						//+ SMAppointment.timeIntegerToString(rsappointments.getInt(SMTableappointments.iminuteofday)) 
        						+ "</TD>");
    					
    					out.println("<TD ALIGN=CENTER style=\"vertical-align:top; border-style:none; font-family: Arial; font-weight: normal; font-size: small;\">"); 
    							
    					if(bAllowEditAppointments){		
    						out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditAppointmentEdit?" 
    								+ SMAppointment.Paramlid + "=" + rsappointments.getString(SMTableappointments.lid) + "\">"
    								+ rsappointments.getString(SMTableappointments.lid) + "</A>");
    					}else{
    						out.println(Integer.toString(rsappointments.getInt(SMTableappointments.lid)));
    					}
    							

    					out.println("<TD ALIGN=LEFT style=\"vertical-align:top; border-style:none; font-family: Arial; font-weight: normal; font-size: small;\">" + SMUtilities.getFullNamebyUserID(rsappointments.getString(SMTableappointments.luserid), conn) + "</TD>"); 
    					out.println("<TD ALIGN=LEFT style=\"vertical-align:top; border-style:none; font-family: Arial; font-weight: normal; font-size: small;\">" + rsappointments.getString(SMTableappointments.mcomment) + "</TD>"); 
    					out.println("</TR>");
					}	    	
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
				rsappointments.close();
				out.println("</TABLE>");
				out.println("</TD></TR>");
		    	}else{
		    		out.println("<BR>No sales contact record found with this ID.<>");
		    	}
	
		    	rs.close();
		    	
	    	}catch(SQLException ex){

	    		out.println("<BR>Error [1396989523] reading sales contact record - " + ex.getMessage() + ".");
	    	}    	
	    }
	    out.println("</TABLE>");
	    	    
	    //save and remove buttons
		out.println("<INPUT TYPE=\"SUBMIT\" NAME=\"SUBMITSAVE\" VALUE=\" Save \">");
		
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
	    	String sSQL = SMMySQLs.Get_Sales_Contact_By_ID_SQL(iSalesContactID);
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
									+ " - "
									+ sUserFullName
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
					;

			} catch (SQLException e) {
				System.out.println("ERROR: " + e.getMessage());
			}
		out.println("<INPUT TYPE=\"SUBMIT\" NAME=\"SUBMITREMOVE\" VALUE=\" Remove \" ONCLICK=\"return confirm('This Sales Contact will be deleted.')\">  ");
   		if (SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMEditAppointmentCalendar, 
				sUserID, 
				getServletContext(), 
				sDBID,
				(String) request.getSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
   			out.println("&nbsp;<input type=\"button\" onclick=\"window.open('" + sRedirectString + "');\" value=\"Create Appointment\" />"
					);
   			}

		}
		out.println("</BODY></HTML>");
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		doPost(request, response);
	}
}
