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

import SMClasses.SMLogEntry;
import SMDataDefinition.*;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class SMBidTODOGenerate extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String CELL_BORDER_COLOR = "#808080";
	private static final String DARK_ROW_BG_COLOR = "#DCDCDC";
	private static final String LIGHT_ROW_BG_COLOR = "#FFFFFF";
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMBidFollowUpReport))
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
    	
    	String title = "";
    	String subtitle = "";
    	String sSQL = "";
    	ResultSet rs = null;

	    /*************GET the PARAMETERs***************/
	    
	    title = "Pending " + SMBidEntry.ParamObjectName + "s";
	    subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
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
		    sSQL = SMMySQLs.Get_Project_Type_List_SQL();
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

    	out.println(SMUtilities.Build_HTML_Table(4, alCriteria, 100, 0, false, false));
	    sSQL = SMMySQLs.Get_Bid_TO_DO_List_SQL(sSalespersonCode,
											   iProjectType,
											   iCheckBidDate,
											   datBidDateStartDate,
											   datBidDateEndDate,
											   sSelectedSortOrder,
											   iStatusPending,
											   iStatusSuccessful,
											   iStatusUnsuccessful,
											   iStatusInactive);
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
	    int iColumnCount = 0;
	    try{
	    	if (rs != null){
			    
			    //print out column headers
			    //Original:
	    		//out.println("<TABLE BORDER=1 WIDTH=100%> style = \" table-layout:fixed");
	    		
	    		//table-layout:fixed; 
	    		out.println("<TABLE style= \" border: 1px solid black; width:100%;\" >");
			    out.println("<TR>");
   	
		    	out.println("<TD class = \" centerjustifiedheading \" ><B>ID</B></TD>");
		    	iColumnCount++;
		    	out.println("<TD class = \" leftjustifiedheading \" ><B>SP</B></TD>");
		    	iColumnCount++;
		    	out.println("<TD class = \" leftjustifiedheading \" ><B>Ori. Date</B></TD>");
		    	iColumnCount++;
		    	out.println("<TD class = \" leftjustifiedheading \" ><B>Bid Date/Time</B></TD>");
		    	iColumnCount++;
		    	out.println("<TD class = \" leftjustifiedheading \" ><B>Appointments</B></TD>");
		    	iColumnCount++;
		    	out.println("<TD class = \" leftjustifiedheading \" ><B>Bill-to Name</B></TD>");
		    	iColumnCount++;
		    	out.println("<TD class = \" leftjustifiedheading \" ><B>Ship-to Name</B></TD>");
		    	iColumnCount++;
		    	out.println("<TD class = \" leftjustifiedheading \" ><B>T/O Complete</B></TD>");
		    	iColumnCount++;
		    	out.println("<TD class = \" leftjustifiedheading \" ><B>Price Complete</B></TD>");
		    	iColumnCount++;
		    	out.println("<TD class = \" leftjustifiedheading \" ><B>Description</B></TD>");
		    	iColumnCount++;
		    	
				out.println("</TR>");
				
				boolean bOddRow = true;
				String sBackgroundColor = "";
			    while (rs.next()){
					if(bOddRow){
						sBackgroundColor = "\"" + DARK_ROW_BG_COLOR + "\"";
					}else{
						sBackgroundColor = "\"" + LIGHT_ROW_BG_COLOR + "\"";
					}
					bOddRow = !bOddRow;
				    out.println("<TR bgcolor =" + sBackgroundColor + ">");
				    iBidCount++;
				    //id
				    out.println("<TD class = \" centerjustifiedcell \" >"
					    	+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditBidEntry"
					    	+ "?" + SMBidEntry.ParamID + "=" + rs.getInt(SMTablebids.lid) 
					    	+ "&OriginalURL=" + sCurrentURL 
					    	+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					    	+ "\">" 
					    	+ rs.getInt(SMTablebids.TableName + "." + SMTablebids.lid) + "</A></TD>");
				    
				    //salesperson code
				    out.println("<TD class = \" leftjustifiedcell \" >" + rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode) + "</TD>");
				    //Origination Date
				    out.println("<TD class = \" leftjustifiedcell \" >" + USDateOnlyformatter.format(rs.getTimestamp(SMTablebids.dattimeoriginationdate)) + "</TD>");
				    //Date
				    if (rs.getString(SMTablebids.dattimebiddate).compareTo("0000-00-00 00:00:00") == 0){
				    	out.println("<TD class = \" leftjustifiedcell \" >N/A</TD>");
				    }else{
				    	out.println("<TD class = \" leftjustifiedcell \" >" + USDateTimeformatter.format(rs.getTimestamp(SMTablebids.dattimebiddate)) + "</TD>"); //rs.getString(SMTablebids.mfollwupnotes).trim()
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
				    out.println("<TD class = \" leftjustifiedcell \" >");
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
				    out.println("<TD class = \" leftjustifiedcell \" >" + rs.getString(SMTablebids.scustomername) 
				    		+ "<BR>" + SMUtilities.addPhoneNumberLink(rs.getString(SMTablebids.sphonenumber))
				    		+ "</TD>");
				    //project name
				    out.println("<TD class = \" leftjustifiedcell \" >" + rs.getString(SMTablebids.sprojectname) + "</TD>");

				    //take off complete date
				    if (rs.getString(SMTablebids.dattimetakeoffcomplete).compareTo("0000-00-00 00:00:00") == 0){
				    	out.println("<TD class = \" leftjustifiedcell \" >N/A&nbsp;-&nbsp;" + rs.getString(SMTablebids.stakeoffpersoncode) + "</TD>");
				    }else{
				    	out.println("<TD class = \" leftjustifiedcell \" >" + USDateOnlyformatter.format(rs.getTimestamp(SMTablebids.dattimetakeoffcomplete)) 
				    		+ "&nbsp;-&nbsp;" + rs.getString(SMTablebids.stakeoffpersoncode) + "</TD>");
				    }
				    //price complete date
				    if (rs.getString(SMTablebids.dattimepricecomplete).compareTo("0000-00-00 00:00:00") == 0){
				    	out.println("<TD class = \" leftjustifiedcell \" >N/A&nbsp;-&nbsp;" + rs.getString(SMTablebids.spricingpersoncode) + "</TD>");
				    }else{
				    	out.println("<TD class = \" leftjustifiedcell \" >" + USDateOnlyformatter.format(rs.getTimestamp(SMTablebids.dattimepricecomplete)) 
				    			+ "&nbsp;-&nbsp;" + rs.getString(SMTablebids.spricingpersoncode) + "</TD>");
				    }
				    //description
				    out.println("<TD class = \" leftjustifiedcell \" >" + rs.getString(SMTablebids.mdescription).replace("\n", "<BR>").replaceAll("[^\\x00-\\x7F]", " ") + "</TD>");
				    /*
				    //bin number
				    out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + rs.getString(SMTablebids.sbinnumber).trim() + "&nbsp;</FONT></TD>");
					*/
				    out.println("</TR>");
			    }
			    out.println("<TR><TD COLSPAN=" + Integer.toString(iColumnCount) + "><HR></TD></TR>");
			    out.println("<TR><TD ALIGN=RIGHT COLSPAN=" + Integer.toString(iColumnCount) + "><FONT SIZE=4><B>Total Pending " + SMBidEntry.ParamObjectName + "s: " + iBidCount + "</B></FONT></TD></TR>");
			    
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