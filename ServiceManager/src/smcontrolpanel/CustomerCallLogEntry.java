package smcontrolpanel;

import SMDataDefinition.*;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CustomerCallLogEntry extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private String sCompanyName = "";
	private String sDBID = "";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMCustomerCallLogEntryForm))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String title = "Customer Call Log";
		String subtitle = "";
		
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	    Calendar c = Calendar.getInstance();
	    c.setTimeInMillis(System.currentTimeMillis());
	    
	    out.println("<FORM ACTION=" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.CustomerCallLogSave METHOD=POST>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("<TABLE BORDER=1 WIDTH=95%>");
	    out.println("<TR><TD WIDTH=20% VALIGN=CENTER><FONT SIZE=4><B>Date and Time</B></FONT></TD><TD WIDTH=80% VALIGN=CENTER><TABLE BORDER=0><TR><TD><INPUT TYPE=CHECKBOX NAME=USENOW VALUE=0>&nbsp;<B>Use current time and date.</B></TD></TR>" +
					   "<TR><TD>&nbsp;</TD></TR>" +
					   "<TR><TD>" +
					   "<TABLE BORDER=0>" +
					   "<TR><TD COLSPAN=6><INPUT TYPE=CHECKBOX NAME=USECUSTOM VALUE=0><B>Use selected time and date.</B><BR></TD></TR>");
		        	   
   		out.println("<TR><TD>"
				+ clsCreateHTMLFormFields.TDTextBox(
						"StartingDate", 
						clsDateAndTimeConversions.utilDateToString((new Date(c.getTimeInMillis())),"M/d/yyyy"), 
						10, 
						10, 
						""
					) 
				+ SMUtilities.getDatePickerString("StartingDate", getServletContext())
				+ "&nbsp;&nbsp;&nbsp;&nbsp;");			    		

		int iHour;
		if (c.get(Calendar.HOUR) == 0 && c.get(Calendar.AM_PM) == Calendar.PM){
			iHour = 12;
		}else{
			iHour = c.get(Calendar.HOUR);
		}
		out.println("<TR><TD ALIGN=RIGHT>&nbsp;&nbsp;Hour</TD><TD><SELECT NAME=\"SelectedHour\">");
    		for (int i=0; i<=12;i++){
    			if (i == iHour){
    				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
    			}else{
    				out.println("<OPTION VALUE=" + i + ">" + i);
    			}
    		}
    	out.println("</SELECT></TD>");
    	out.println("<TD ALIGN=RIGHT>&nbsp;&nbsp;Minute</TD><TD><SELECT NAME=\"SelectedMinute\">");
    		for (int i=0; i<=59;i++){
    			if (i == c.get(Calendar.MINUTE)){
    				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
    			}else{
    				out.println("<OPTION VALUE=" + i + ">" + i);
    			}
    		}
		out.println("</SELECT></TD>");	
    	out.println("<TD ALIGN=RIGHT>&nbsp;&nbsp;AM/PM</TD><TD><SELECT NAME=\"SelectedAMPM\">");
    		for (int i=Calendar.AM; i<=Calendar.PM;i++){
    			if (i == c.get(Calendar.AM_PM)){
    				if (i == Calendar.AM){
    					out.println("<OPTION SELECTED VALUE=" + Calendar.AM + ">" + "AM");
    				}else{
    					out.println("<OPTION SELECTED VALUE=" + Calendar.PM + ">" + "PM");
    				}		
    			}else{
    				if (i == Calendar.AM){
    					out.println("<OPTION VALUE=" + Calendar.AM + ">" + "AM");
    				}else{
    					out.println("<OPTION VALUE=" + Calendar.PM + ">" + "PM");
    				}
    			}
    		}
		out.println("</SELECT></TD></TR></TABLE>");
	   	out.println("</TD></TR>" +
	   "</TABLE></TD></TR>");
	    
	    out.println("<TR><TD VALIGN=CENTER><FONT SIZE=4><B>Customer Name</B></FONT></TD><TD VALIGN=CENTER><INPUT TYPE=TEXT NAME=\"CustomerName\" VALUE=\"\" SIZE=50 MAXLENGTH=50> Up to 50 characters</TD></TR>");
	    
	    out.println ("<TR><TD VALIGN=CENTER><FONT SIZE=4><B>Phone Number</B></FONT></TD><TD VALIGN=CENTER>" + 
		      		 "(<INPUT TYPE=TEXT NAME=\"PhoneAreaCode\" VALUE=\"\" SIZE=3 MAXLENGTH=3>)" +
		      		 " <INPUT TYPE=TEXT NAME=\"PhoneSwitch\" VALUE=\"\" SIZE=3 MAXLENGTH=3>-" +
		      		 " <INPUT TYPE=TEXT NAME=\"PhoneEndUnit\" VALUE=\"\" SIZE=4 MAXLENGTH=4>&nbsp;&nbsp;Ext" +
		      		 " <INPUT TYPE=TEXT NAME=\"PhoneExtension\" VALUE=\"\" SIZE=5 MAXLENGTH=5>" +
		      		 "</TD></TR>");
	    
	    //out.println("<TR><TD><H3>Phone Number</H3></TD><TD><INPUT TYPE=TEXT NAME=\"Phone\" VALUE=\"\" SIZE=20 MAXLENGTH=20> Up to 20 alpha-numerics</TD></TR>");
	    out.println("<TR><TD VALIGN=CENTER><FONT SIZE=4><B>City/State</B></FONT></TD><TD VALIGN=CENTER><INPUT TYPE=TEXT NAME=\"CityState\" VALUE=\"\" SIZE=30 MAXLENGTH=30> Up to 30 alpha-numerics</TD></TR>");
	    
	    try{ 
		    //order source list
	        String sSQL = SMMySQLs.Get_OrderSource_List_SQL();
	        //System.out.println("OrderSource SQL: " + sSQL);
	        ResultSet rsOrderSources = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID));
	    	out.println ("<TR><TD VALIGN=CENTER><FONT SIZE=4><B>Order Source</B></FONT></TD><TD VALIGN=CENTER><SELECT NAME=\"OrderSource\">");
	    	out.println("<OPTION VALUE=0>****Select An Order Source****");
	    	while (rsOrderSources.next()){
	    		out.println("<OPTION VALUE=\"" + rsOrderSources.getInt(SMTableordersources.iSourceID) + "\">" + rsOrderSources.getInt(SMTableordersources.iSourceID) + " - " + rsOrderSources.getString(SMTableordersources.sSourceDesc));
	    	}
	    	rsOrderSources.close();
	    	out.println("</SELECT></TD></TR>");
	    }catch (SQLException ex){
	    	//catch SQL exceptions
	    	System.out.println("Error when creating and displaying order source drop down list.");
	    	System.out.println("Error: " + ex.getErrorCode() + " - " + ex.getMessage());
	    	System.out.println("SQL: " + ex.getSQLState());
	    }
	    out.println("<TR><TD VALIGN=CENTER><FONT SIZE=4><B>Note</B></FONT></TD><TD VALIGN=CENTER><TEXTAREA NAME=\"CallNote\" ROWS=\"8\" COLS=\"50\"></TEXTAREA></TD></TR>");
	    
	    out.println("</TABLE>");

    	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Save----\">");
    	out.println ("</FORM>");
	    
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
