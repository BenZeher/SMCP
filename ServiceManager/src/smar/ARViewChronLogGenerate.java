package smar;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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
import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class ARViewChronLogGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;

	//formats
	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	
	private String m_sWarning = "";
	private String sCallingClass = "";
	private static String sDBID = "";
	private static String sUserID = "";
	private static String sUserFullName = "";
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ARViewChronologicalLog))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    //sCallingClass will look like: smar.ARAgedTrialBalanceReport
	    sCallingClass = ARUtilities.get_Request_Parameter("CallingClass", request);
	    /**************Get Parameters**************/
    	String sCustomer = request.getParameter("CustomerNumber");
    	String sStartingDocumentNumber = request.getParameter("StartingDocumentNumber");
    	String sEndingDocumentNumber = request.getParameter("EndingDocumentNumber");
    	String sStartingEventDate = request.getParameter("StartingEventDate");
    	String sJobNumber = request.getParameter("JobNumber");

    	//Convert the date to a SQL one:
    	java.sql.Date datStartingEventDate;
		try {
			datStartingEventDate = clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sStartingEventDate);
		} catch (ParseException e) {
			m_sWarning = "Error:[1423843443] Invalid starting event date: '" + sStartingEventDate + "' - " + e.getMessage();
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
			);
			return;
		}

    	String sEndingEventDate = request.getParameter("EndingEventDate");
    	//Convert the date to a SQL one:
    	java.sql.Date datEndingEventDate;
		try {
			datEndingEventDate = clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndingEventDate);
		} catch (ParseException e) {
			m_sWarning = "Error:[1423843444] Invalid ending event date: '" + sEndingEventDate + "' - " + e.getMessage();
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
			);
			return;
		}
    	
    	/**************End Parameters**************/
    	
    	//Customized title
    	String sReportTitle = "AR Chronological Log";

    	String sCriteria = "For customer <B>" + sCustomer + "</B>"
    		+ ", starting with document number <B>" + sStartingDocumentNumber + "</B>"
    		+ ", ending with document number <B>" + sEndingDocumentNumber + "</B>";
    	
    		if (sJobNumber.compareTo("") != 0){
    			sCriteria += ", for job number <B>" + sJobNumber + "</B>";
    		}
    		sCriteria += ", starting on log date <B>" + clsDateAndTimeConversions.utilDateToString(datStartingEventDate, "M/d/yyyy") + "</B>"
    		+ ", ending on log date <B>" + clsDateAndTimeConversions.utilDateToString(datEndingEventDate, "M/d/yyyy") + "</B>."
    		;
    	
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + " - " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME) + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"#FFFFFF\">" +
		   "<TABLE BORDER=0 WIDTH=100%>" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
		   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) 
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME).toString() + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>" +
		   
		   "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");
				   
	   out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
							+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
							+ "\">Return to user login</A><BR>" +
					   "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
							+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
							+ "\">Return to Accounts Receivable Main Menu</A></TD></TR></TABLE>");
    	
    	//Retrieve information
    	if (!processReport(
    			getServletContext(),
    			sDBID,
    			out,
    			sCustomer,
    			sStartingDocumentNumber,
    			sEndingDocumentNumber,
    			datStartingEventDate,
    			datEndingEventDate,
    			sJobNumber
    	)){
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "Warning=" + m_sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
    	}else{
    	    SMLogEntry log = new SMLogEntry(sDBID, getServletContext());
    	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARVIEWCHRONLOG, "REPORT", "AR View Chron Log", "[1376509289]");
    	}

	    out.println("</BODY></HTML>");
	}
	
	private boolean processReport(
			ServletContext context,
			String sConf,
			PrintWriter pwOut,
			String sCust,
			String sStartingDoc,
			String sEndingDoc,
			java.sql.Date datStartingDate,
			java.sql.Date datEndingDate,
			String sJobNumber
		){
		
		String SQL = "";
		
		//If the user has chosen to look only at one job number, limit the
		//list to documents referencing that job number:
		if (sJobNumber.compareToIgnoreCase("") != 0){
			SQL = "SELECT * FROM " + SMTablearchronlog.TableName
				+ ", " + SMTableartransactions.TableName
				+ " WHERE ("

					//Limit by date:
					+ "(" + SMTablearchronlog.TableName + "." + SMTablearchronlog.datlogdate 
						+ " >= '" + clsDateAndTimeConversions.utilDateToString(datStartingDate, "yyyy-MM-dd") + " 00:00:00')"
					+ " AND (" + SMTablearchronlog.TableName + "." + SMTablearchronlog.datlogdate + " <= '" 
						+ clsDateAndTimeConversions.utilDateToString(datEndingDate, "yyyy-MM-dd") + " 23:59:59')"
						
					//Link the tables:
					+ " AND (" + SMTablearchronlog.TableName + "." + SMTablearchronlog.spayeepayor + " = " 
						+ SMTableartransactions.TableName + "." + SMTableartransactions.spayeepayor + ")"
					+ " AND (" + SMTablearchronlog.TableName + "." + SMTablearchronlog.sapplytodoc + " = " 
						+ SMTableartransactions.TableName + "." + SMTableartransactions.sdocnumber + ")"
					
					//Limit to invoices and credit notes:
					+ " AND (" 
						+ "(" + SMTableartransactions.TableName + "." + SMTableartransactions.idoctype + " = " 
							+ ARDocumentTypes.CREDIT + ")"
						+ " OR (" + SMTableartransactions.TableName + "." + SMTableartransactions.idoctype + " = " 
							+ ARDocumentTypes.INVOICE + ")"
					+ ")"
					
					//Limit to docs with the selected job number:
					+ " AND ("  + SMTableartransactions.TableName + "." + SMTableartransactions.sordernumber
						+ " = '" + sJobNumber + "')"
				+ ")"
				+ " ORDER BY " + SMTablearchronlog.TableName + "." + SMTablearchronlog.datlogdate 
					+ ", " + SMTablearchronlog.TableName + "." + SMTablearchronlog.id
		;
			
		}else{
			SQL = "SELECT * FROM " + SMTablearchronlog.TableName
				+ " WHERE ("
					//Limit by customer:
					+ "(" + SMTablearchronlog.spayeepayor + " = '" 
						+ clsDatabaseFunctions.FormatSQLStatement(sCust) + "')"
						
					//Limit by doc number:
					+ " AND (" + SMTablearchronlog.sapplytodoc + " >= '" 
						+ clsDatabaseFunctions.FormatSQLStatement(sStartingDoc) + "')"
					+ " AND (" + SMTablearchronlog.sapplytodoc + " <= '" 
						+ clsDatabaseFunctions.FormatSQLStatement(sEndingDoc) + "')"
					
					//Limit by date:
						+ " AND (" + SMTablearchronlog.datlogdate + " >= '" 
						+ clsDateAndTimeConversions.utilDateToString(datStartingDate, "yyyy-MM-dd") + " 00:00:00')"
					+ " AND (" + SMTablearchronlog.datlogdate + " <= '" 
						+ clsDateAndTimeConversions.utilDateToString(datEndingDate, "yyyy-MM-dd") + " 23:59:59')"
										
					+ ")"
				+ " ORDER BY " + SMTablearchronlog.datlogdate + ", " + SMTablearchronlog.id
			;
		}
		//System.out.println("In " + this.toString() + ".processReport SQL = " + SQL);
		pwOut.println("<TABLE BORDER=0 WIDTH=100%>");
		printHeading(pwOut);
		pwOut.println("<TR><TDCOLSPAN=3><HR></TD><TR>");
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context,
					sConf,
					"MySQL",
					this.toString() + ".processReport - User: " + sUserID
					+ " - "
					+ sUserFullName
					);
			
			while (rs.next()){
				pwOut.println("<TR>");
				pwOut.println("<TD VALIGN=\"TOP\"><FONT SIZE=2>" + rs.getString(SMTablearchronlog.datlogdate).replace(" ", "&nbsp;") + "</FONT></TD>");
				pwOut.println("<TD VALIGN=\"TOP\"><FONT SIZE=2>" + rs.getString(SMTablearchronlog.suserfullname).trim() + "</FONT></TD>");
				pwOut.println("<TD VALIGN=\"TOP\"><FONT SIZE=2>" + rs.getString(SMTablearchronlog.sdescription) + "</FONT></TD>");
				pwOut.println("</TR>");
			}
			rs.close();
		}catch (SQLException e){
			m_sWarning = "Error processing data for report - " + e.getMessage() + ".";
			return false;
		}
		pwOut.println("</FONT></TABLE>");
		
		return true;
	}
	private void printHeading(PrintWriter pwOut){
		
		pwOut.println("<TR>");
		pwOut.println("<TR><TD><FONT SIZE=2><B><I>Event date/time</I></B></FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B><I>Name</I></B></FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B><I>Description</I></B></FONT></TD>");
		pwOut.println("</TR>");
		
	}
}
