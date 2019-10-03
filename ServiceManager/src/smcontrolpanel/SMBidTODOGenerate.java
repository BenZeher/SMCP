package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.MySQLs;
import SMClasses.SMLogEntry;
import SMDataDefinition.*;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class SMBidTODOGenerate extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String CELL_BORDER_COLOR = "#808080";
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMPendingBidsReport))
			{
				return;
			}

	    //Get the session info:
		//First make sure these are initialized every time:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);

    	//Calculate time period
	    SimpleDateFormat USDateTimeformatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
    	SimpleDateFormat USDateOnlyformatter = new SimpleDateFormat("MM/dd/yyyy");

    	//get current URL
    	String sCurrentURL;
    	sCurrentURL = clsServletUtilities.URLEncode(request.getRequestURI().toString() + "?" 
    			+ request.getQueryString());
    	
	    CurrentSession.setAttribute(SMUtilities.SMCP_SESSION_PARAM_URLHISTORY,
				SMUtilities.updateURLHistory("Pending Sales Lead report", 
						sCurrentURL.replace("&", "*"), 
						CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_URLHISTORY),
					CurrentSession.getAttribute("URLMaxSize"))
		);
    	
    	String title = "";
    	String subtitle = "";
    	String sSQL = "";
    	ResultSet rs = null;

	    /*************GET the PARAMETERs***************/
	    
	    title = "Pending " + SMBidEntry.ParamObjectName + "s";
	    subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, "#FFFFFF", sCompanyName));
	    String sColor = SMUtilities.getInitBackGroundColor(getServletContext(), sDBID);
	    out.println(sStyleScripts());
	    
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

    	//Display all selected criteria
    	ArrayList<String> alCriteria = new ArrayList<String>(0);
    	
	    String sSalespersonCode = request.getParameter("SelectedSalesperson");
	    if (sSalespersonCode.compareTo("ALLSP") == 0){
	    	alCriteria.add("<FONT SIZE=2><B>Salesperson:</B>&nbsp;All</FONT>");
	    }else{
	    	alCriteria.add("<FONT SIZE=2><B>Salesperson:</B>&nbsp;" + sSalespersonCode + "</FONT>");
	    }
	    int iProjectType = Integer.parseInt(request.getParameter("ProjectType"));
	    if (iProjectType == 0){
	    	alCriteria.add("<FONT SIZE=2><B>Project Type:</B>&nbsp;ALL</FONT>");
	    }else{
		    sSQL = MySQLs.Get_Project_Type_List_SQL();
		    try{
			    ResultSet rsProjectTypes = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
			    while (rsProjectTypes.next()){
			    	if (rsProjectTypes.getInt(SMTableprojecttypes.iTypeId) == iProjectType){
			    		alCriteria.add("<FONT SIZE=2><B>Project Type:</B>&nbsp;" + rsProjectTypes.getString(SMTableprojecttypes.sTypeDesc) + "</FONT>");
			    		break;
			    	}
			    }
			    rsProjectTypes.close();
		    }catch(SQLException ex){
		    	System.out.println("Error when getting project type information!!");
		        System.out.println("SQLException: " + ex.getMessage());
		        System.out.println("SQLState: " + ex.getSQLState());
		        System.out.println("SQL: " + ex.getErrorCode());
		    }
	    }
    	int iCheckBidDate = 0;

	    Timestamp datBidDateStartDate = new Timestamp(System.currentTimeMillis());
	    Timestamp datBidDateEndDate = new Timestamp(System.currentTimeMillis());
	    //Selected Sort Order
	    String sSelectedSortOrder = request.getParameter("SelectedSortOrder");
	    
	    //Status check
	    int iStatusPending = 0;
	    int iStatusSuccessful = 0;
	    int iStatusUnsuccessful = 0;
	    int iStatusInactive = 0;
	    
    	if (request.getParameter("StatusPending") != null){
    		iStatusPending = 1;
    		alCriteria.add("<FONT SIZE=2><B>Show Pending:</B>&nbsp;Yes</FONT>");
    	}else{
    		alCriteria.add("<FONT SIZE=2><B>Show Pending:</B>&nbsp;No</FONT>");
    	}
    	if (request.getParameter("StatusSuccessful") != null){
    		iStatusSuccessful = 1;
    		alCriteria.add("<FONT SIZE=2><B>Show Successful:</B>&nbsp;Yes</FONT>");
    	}else{
    		alCriteria.add("<FONT SIZE=2><B>Show Successful:</B>&nbsp;No</FONT>");
    	}
    	if (request.getParameter("StatusUnsuccessful") != null){
    		iStatusUnsuccessful = 1;
    		alCriteria.add("<FONT SIZE=2><B>Show Unsuccessful:</B>&nbsp;Yes</FONT>");
    	}else{
    		alCriteria.add("<FONT SIZE=2><B>Show Unsuccessful:</B>&nbsp;No</FONT>");
    	}
    	if (request.getParameter("StatusInactive") != null){
    		iStatusInactive = 1;
    		alCriteria.add("<FONT SIZE=2><B>Show Inactive:</B>&nbsp;Yes</FONT>");
    	}else{
    		alCriteria.add("<FONT SIZE=2><B>Show Inactive:</B>&nbsp;No</FONT>");
    	}
    
    	
	    //log usage of this report
	    SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMPENDINGBIDSREPORT, "REPORT", "SMPendingBidsReport", "[1376509310]");

    	out.println(SMUtilities.Build_HTML_Table(4, alCriteria, 100, 0, false, false,sColor));
    	out.println(SMUtilities.getMasterStyleSheetLink());
    	
		sSQL = "SELECT * FROM " + SMTablebids.TableName + ", " + SMTablesalesperson.TableName + 
				  " WHERE" + 
				  	" " + SMTablebids.TableName + "." + SMTablebids.ssalespersoncode + " =" + 
				  	" " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode + 
				  	" AND" + 
				  	" " + SMTablebids.dattimeactualbiddate + " = '0000-00-00 00:00:00'";
			if (sSalespersonCode.compareTo("ALLSP") != 0){
				  sSQL = sSQL + " AND " + SMTablebids.TableName + "." + SMTablebids.ssalespersoncode + " = '" + sSalespersonCode + "'";
			}
			if (iProjectType > 0){
				  sSQL = sSQL + " AND " + SMTablebids.TableName + "." + SMTablebids.iprojecttype + " = " + iProjectType;
			}
				  
			if (iCheckBidDate == 1){
				  sSQL = sSQL + " AND " + SMTablebids.TableName + "." + SMTablebids.dattimebiddate + " >= '" + datBidDateStartDate.toString() + "'" + 
				  			  " AND " + SMTablebids.TableName + "." + SMTablebids.dattimebiddate + " <= '" + datBidDateEndDate.toString() + "'";
			}
			
			if (iStatusPending == 0){
				sSQL = sSQL + " AND " + SMTablebids.TableName + "." + SMTablebids.sstatus + " <> '" + SMTablebids.STATUS_PENDING + "'";
			}
			if (iStatusSuccessful == 0){
				sSQL = sSQL + " AND " + SMTablebids.TableName + "." + SMTablebids.sstatus + " <> '" + SMTablebids.STATUS_SUCCESSFUL + "'";
			}
			if (iStatusUnsuccessful == 0){
				sSQL = sSQL + " AND " + SMTablebids.TableName + "." + SMTablebids.sstatus + " <> '" + SMTablebids.STATUS_UNSUCCESSFUL + "'";
			}
			if (iStatusInactive == 0){
				sSQL = sSQL + " AND " + SMTablebids.TableName + "." + SMTablebids.sstatus + " <> '" + SMTablebids.STATUS_INACTIVE + "'";
			}
			
			//default to sort by bidding date.
			if (sSelectedSortOrder.compareTo("Salesperson") == 0){
				sSQL = sSQL + " ORDER BY " + SMTablebids.TableName + "." + SMTablebids.ssalespersoncode + "," + 
						" " + SMTablebids.TableName + "." + SMTablebids.dattimebiddate;
			}else{
				sSQL = sSQL + " ORDER BY " + SMTablebids.TableName + "." + SMTablebids.dattimebiddate + "," + 
						" " + SMTablebids.TableName + "." + SMTablebids.ssalespersoncode;
			}
			
	    try{
	    	rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	    }catch (SQLException ex){
	    	System.out.println("Error when opening rs for sales lead followup list!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
	    }
	    /*************END of PARAMETER list***************/

	    int iBidCount = 0;
	    try{
	    	if (rs != null){
			    
			    //print out column headers
			    //Original:
	    		//out.println("<TABLE BORDER=1 WIDTH=100%> style = \" table-layout:fixed");

	    		out.println("<TABLE WIDTH=100% CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");
	    		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
	    		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">ID </TD>");
	    		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> SP</TD>");
	    		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> Origination Date</TD>");
	    		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> Bid Date/Time</TD>");
	    		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> Appointments</TD>");
	    		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> Bill-to Name</TD>");
	    		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> Ship-to Name</TD>");
	    		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> T/O Complete</TD>");
	    		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> Price Complete</TD>");
	    		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> Description</TD>");
	    		out.println("</TR>");

	    		
				
				boolean bOddRow = true;
				while (rs.next()){
					if(bOddRow){
			    		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
					}else{
			    		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
					}
					bOddRow = !bOddRow;
				    iBidCount++;
				    //id
				    out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">"
					    	+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditBidEntry"
					    	+ "?" + SMBidEntry.ParamID + "=" + rs.getInt(SMTablebids.lid) 
					    	+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					    	+ "\">" 
					    	+ rs.getInt(SMTablebids.TableName + "." + SMTablebids.lid) + "</A></TD>");
				    
				    //salesperson code
				    out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">"+ rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode) + "</TD>");
				    //Origination Date
				    out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + USDateOnlyformatter.format(rs.getTimestamp(SMTablebids.dattimeoriginationdate)) + "</TD>");
				    //Date
				    if (rs.getString(SMTablebids.dattimebiddate).compareTo("0000-00-00 00:00:00") == 0){
				    	out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">N/A</TD>");
				    }else{
				    	out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + USDateTimeformatter.format(rs.getTimestamp(SMTablebids.dattimebiddate)) + "</TD>"); //rs.getString(SMTablebids.mfollwupnotes).trim()
				    }
				    //Appointments
				    sSQL = "SELECT " + SMTableappointments.TableName + "." + SMTableappointments.datentrydate 
				    		+ ", " +  SMTableappointments.TableName + "." + SMTableappointments.lid
				    		+ " FROM " + SMTableappointments.TableName
				    		+ " WHERE ("
				    		+ SMTableappointments.TableName + "." + SMTableappointments.ibidid + "=" + Integer.toString(rs.getInt(SMTablebids.lid))
				    		+ ")"
				    		+ " ORDER BY " + SMTableappointments.TableName + "." + SMTableappointments.datentrydate;
				    ResultSet rsAppointments = null;
				    try{
				    	rsAppointments = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
				    }catch (SQLException ex){
				    	System.out.println("Error in SQL: " + sSQL + " Failed to get appointments. " + ex.getMessage());
				    }
				    out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">");
				    while(rsAppointments.next()){
				    	try {
							out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditAppointmentEdit"
									+ "?" + SMTableappointments.lid + "=" + Integer.toString(rsAppointments.getInt(SMTableappointments.TableName + "." + SMTableappointments.lid))
									+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
									+ "\">" 
									+  Integer.toString(rsAppointments.getInt(SMTableappointments.TableName + "." + SMTableappointments.lid)) + "</A>"
									+ "&nbsp;-&nbsp;"
									 + clsDateAndTimeConversions.resultsetDateStringToFormattedString(rsAppointments.getString(SMTableappointments.TableName + "." + SMTableappointments.datentrydate), 
											 SMUtilities.DATE_FORMAT_FOR_DISPLAY, 
											 SMUtilities.EMPTY_DATE_VALUE)
									);
						} catch (Exception e) {
						}
				    }
				    out.println("</TD>");
				    rsAppointments.close();			    
				    //customer name and phone number:
				    out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">"+ rs.getString(SMTablebids.scustomername) 
				    		+ "<BR>" + SMUtilities.addPhoneNumberLink(rs.getString(SMTablebids.sphonenumber))
				    		+ "</TD>");
				    //project name
				    out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + rs.getString(SMTablebids.sprojectname) + "</TD>");

				    //take off complete date
				    if (rs.getString(SMTablebids.dattimetakeoffcomplete).compareTo("0000-00-00 00:00:00") == 0){
				    	out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">N/A&nbsp;-&nbsp;" + rs.getString(SMTablebids.stakeoffpersoncode) + "</TD>");
				    }else{
				    	out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + USDateOnlyformatter.format(rs.getTimestamp(SMTablebids.dattimetakeoffcomplete)) 
				    		+ "&nbsp;-&nbsp;" + rs.getString(SMTablebids.stakeoffpersoncode) + "</TD>");
				    }
				    //price complete date
				    if (rs.getString(SMTablebids.dattimepricecomplete).compareTo("0000-00-00 00:00:00") == 0){
				    	out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">N/A&nbsp;-&nbsp;" + rs.getString(SMTablebids.spricingpersoncode) + "</TD>");
				    }else{
				    	out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">"+ USDateOnlyformatter.format(rs.getTimestamp(SMTablebids.dattimepricecomplete)) 
				    			+ "&nbsp;-&nbsp;" + rs.getString(SMTablebids.spricingpersoncode) + "</TD>");
				    }
				    //description
				    out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">"+ rs.getString(SMTablebids.mdescription).replace("\n", "<BR>").replaceAll("[^\\x00-\\x7F]", " ") + "</TD>");
				    /*
				    //bin number
				    out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + rs.getString(SMTablebids.sbinnumber).trim() + "&nbsp;</FONT></TD>");
					*/
				    out.println("</TR>");
			    }
			    out.println("<TR><TD COLSPAN=10 CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp;</TD></TR>");
			    out.println("<TD COLSPAN=10 CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Total Pending Sales Leads: " + iBidCount + "</TD></TR>");
			    out.println("</TABLE>");
			    rs.close();
	    	}else{
		    	//no report type is passed in, out blank.
		    	out.println("<BR>Error retrieving " + SMBidEntry.ParamObjectName + " information.");
		    }
		    
	    }catch (SQLException ex){
	    	System.out.println("Error in SMBidTODOGenerate!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
	    }
	    
		out.println("</BODY></HTML>");
	}
	private String sStyleScripts(){
		String s = "";
		String sBorderSize = "0";
		String sRowHeight = "22px";
		s += "<style type=\"text/css\">\n";
		
		//Layout table:
		s +=
			"table.basic {"
			+ "border-width: " + sBorderSize + "px; "
			+ "border-spacing: 2px; "
			//+ "border-style: outset; "
			+ "border-style: none; "
			+ "border-color: white; "
			+ "border-collapse: separate; "
			+ "width: 100%; "
			+ "font-size: " + "small" + "; "
			+ "font-family : Arial; "
			+ "color: black; "
			//+ "background-color: white; "
			+ "}"
			+ "\n"
			;
		
		/*
		//This is the def for a label field:
		s +=
			"td.fieldlabel {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			//+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "font-weight: bold; "
			+ "text-align: right; "
			//+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		*/
		//This is the def for a table cell, left justified:
		s +=
			"td.leftjustifiedcell {"
			+ "height: " + sRowHeight + "; "
			+ "border: 0px solid; "
			+ "padding: 2px; "
			+ "border-color: " + CELL_BORDER_COLOR + "; "
			+ "vertical-align: top;"
			+ "font-family : Arial; "
			+ "font-weight: normal; "
			+ "font-size: small; "
			+ "text-align: left; "
			+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;

	    //style= \" word-wrap:break-word; \"
	    //style= \" word-wrap:normal; white-space:pre-wrap; \" 
		s +=
			"td.leftjustifiedcellforcewrap {"
			+ "height: " + sRowHeight + "; "
			+ "border: 0px solid; "
			+ "padding: 2px; "
			+ "border-color: " + CELL_BORDER_COLOR + "; "
			+ "vertical-align: top;"
			+ "font-family : Arial; "
			+ "font-weight: normal; "
			+ "font-size: small; "
			+ "text-align: left; "
			+ "color: black; "
			+ "word-wrap:break-word; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		//This is the def for a table cell, right justified:
		s +=
			"td.rightjustifiedcell {"
			+ "height: " + sRowHeight + "; "
			+ "border: 0px solid; "
			+ "padding: 2px; "
			+ "border-color: " + CELL_BORDER_COLOR + "; "
			+ "vertical-align: top;"
			+ "font-family : Arial; "
			+ "font-weight: normal; "
			+ "font-size: small; "
			+ "text-align: right; "
			+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a table cell, center justified:
		s +=
			"td.centerjustifiedcell {"
			+ "height: " + sRowHeight + "; "
			+ "border: 0px solid; "
			+ "padding: 2px; "
			+ "border-color: " + CELL_BORDER_COLOR + "; "
			+ "vertical-align: top;"
			+ "font-family : Arial; "
			+ "font-weight: normal; "
			+ "font-size: small; "
			+ "text-align: center; "
			+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a left-aligned heading on a table:
		s +=
			"td.leftjustifiedheading {"
			+ "border: 1px solid; "
			+ "bordercolor: 000; "
			+ "padding: 2px; "
			+ "font-family : Arial; "
			+ "font-size: small; "
			+ "font-weight: bold; "
			+ "text-align: left; "
			+ "vertical-align:bottom; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a right-aligned heading on a table:
		s +=
			"td.rightjustifiedheading {"
			+ "border: 1px solid; "
			+ "bordercolor: 000; "
			+ "padding: 2px; "
			+ "font-family : Arial; "
			+ "font-size: small; "
			+ "font-weight: bold; "
			+ "text-align: right; "
			+ "vertical-align:bottom; "
			+ "}"
			+ "\n"
			;

		//This is the def for a center-aligned heading on a table:
		s +=
			"td.centerjustifiedheading {"
			+ "border: 1px solid; "
			+ "bordercolor: 000; "
			+ "padding: 2px; "
			+ "font-family : Arial; "
			+ "font-size: small; "
			+ "font-weight: bold; "
			+ "text-align: center; "
			+ "vertical-align:bottom; "
			+ "}"
			+ "\n"
			;
		
		s += "</style>"
			+ "\n"
			;

		return s;
	}
	
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}