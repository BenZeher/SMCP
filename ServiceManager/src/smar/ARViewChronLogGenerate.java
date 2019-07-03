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
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    //sCallingClass will look like: smar.ARAgedTrialBalanceReport
	    String sCallingClass = ARUtilities.get_Request_Parameter("CallingClass", request);
	    String m_sWarning = "";
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
    	 String sColor = SMUtilities.getInitBackGroundColor(getServletContext(), sDBID);
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
		   "<TABLE BORDER=0 WIDTH=100% BGCOLOR = " + sColor + ">"  +
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
	   out.println(SMUtilities.getMasterStyleSheetLink());
    	//Retrieve information
	   try {
		processReport(
				getServletContext(),
				sDBID,
				out,
				sCustomer,
				sStartingDocumentNumber,
				sEndingDocumentNumber,
				datStartingEventDate,
				datEndingEventDate,
				sJobNumber,
				sUserID,
				sUserFullName
			);
	} catch (Exception e) {
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + m_sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		);			
    	return;
	}

	   SMLogEntry log = new SMLogEntry(sDBID, getServletContext());
	   log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARVIEWCHRONLOG, "REPORT", "AR View Chron Log", "[1376509289]");

	    out.println("</BODY></HTML>");
	}
	
	private void processReport(
			ServletContext context,
			String sDBID,
			PrintWriter pwOut,
			String sCust,
			String sStartingDoc,
			String sEndingDoc,
			java.sql.Date datStartingDate,
			java.sql.Date datEndingDate,
			String sJobNumber,
			String sUserID,
			String sUserFullName
		) throws Exception{
		
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
		pwOut.println("<TABLE WIDTH = 100% CLASS = \""+ SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\">");
		printHeading(pwOut);
		int count = 0;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context,
					sDBID,
					"MySQL",
					this.toString() + ".processReport - User: " + sUserID
					+ " - "
					+ sUserFullName
					);
			
			while (rs.next()){
				if(count%2 == 0 ) {
					pwOut.println("<TR CLASS = \""+ SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
				}else {
					pwOut.println("<TR CLASS = \""+ SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
				}
				pwOut.println("<TD CLASS = \""+ SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP  + "\">" + rs.getString(SMTablearchronlog.datlogdate).replace(" ", "&nbsp;") +"</TD>");
				pwOut.println("<TD CLASS = \""+ SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP  + "\">" + rs.getString(SMTablearchronlog.suserfullname).trim() +"</TD>");
				pwOut.println("<TD CLASS = \""+ SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + rs.getString(SMTablearchronlog.sdescription) +"</TD>");
				pwOut.println("</TR>");
				count++;
			}
			rs.close();
		}catch (SQLException e){
			throw new Exception("Error [1548727230] processing data for report - " + e.getMessage() + ".");
		}
		pwOut.println("</FONT></TABLE>");
		
		return;
	}
	private void printHeading(PrintWriter pwOut){
		
		pwOut.println("<TR CLASS = \""+ SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		pwOut.println("<TD CLASS = \""+ SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Event date/time</TD>");
		pwOut.println("<TD CLASS = \""+ SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Name</TD>");
		pwOut.println("<TD CLASS = \""+ SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Description</TD>");
		pwOut.println("</TR>");
		
	}
}
