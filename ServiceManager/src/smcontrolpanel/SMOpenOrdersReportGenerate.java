package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;

public class SMOpenOrdersReportGenerate extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private String sDBID = "";
	private String sUserID = "0";
	private String sCompanyName = "";
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMOpenOrdersReport))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);

    	SimpleDateFormat USDateOnlyformatter = new SimpleDateFormat("MM/dd/yyyy");

    	String title = "Open Orders Report";
    	String subtitle = "";
    	
    	out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
		   
	 	   //log usage of this this report
	 	   SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
	 	   log.writeEntry(sUserID , SMLogEntry.LOG_OPERATION_SMOPENORDERSREPORT, "REPORT", "SMOpenOrdersReport", "[1376509331]");

	    try{

		    boolean bHasRecord = false;
		    String sSQL = SMMySQLs.Get_Open_Orders_Report_SQL();
		    ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
		    
		    out.println("<TABLE BORDER=0 WIDTH=100%>");
		    String sCurrentLocation = "";
		    String sCurrentServiceType = "";
		    boolean bFlipper = false;
		    String sbgColor = "";
		    while (rs.next()){	  
		    	
		    	bHasRecord = true;
		    	
		    	if (sCurrentLocation.compareTo(rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation)) != 0){
		    		out.println("<TR><TD COLSPAN=5><HR><BR><BR></TD></TR>");
		    		out.println("<TR><TD COLSPAN=5><TABLE BORDER=1><TR><TD><B>Location:&nbsp;&nbsp;</B>  " + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation) + "<B>&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;Service Type:&nbsp;&nbsp;</B>  " + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCodeDescription) + "</TD></TR></TABLE></TD></TR>");
		    		sCurrentLocation = rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation);
		    		sCurrentServiceType = rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode);
			    	out.println("<TR><TD COLSPAN=5><HR></TD></TR>" + 
			    				"<TR>" +
		    						"<TD ALIGN=CENTER WIDTH=10%><FONT SIZE=2><B>Sales #</B></FONT></TD>" +
		    						"<TD ALIGN=CENTER WIDTH=15%><FONT SIZE=2><B>Order Date</B></FONT></TD>" +
		    						"<TD ALIGN=LEFT WIDTH=15%><FONT SIZE=2><B>Order Number</B></FONT></TD>" +
		    						"<TD ALIGN=LEFT WIDTH=30%><FONT SIZE=2><B>Bill To Name</B></FONT></TD>" +
		    						"<TD ALIGN=LEFT WIDTH=30%><FONT SIZE=2><B>Ship To Name</B></FONT></TD>" + 
								"</TR>" + 
			    				"<TR><TD COLSPAN=5><HR></TD></TR>");
		    	}

		    	if (sCurrentServiceType.compareTo(rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode)) != 0){
		    		out.println("<TR><TD COLSPAN=5><HR><BR><BR></TD></TR>");
		    		out.println("<TR><TD COLSPAN=5><TABLE BORDER=1><TR><TD><B>Location:&nbsp;&nbsp;</B>  " + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation) + "<B>&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;Service Type:&nbsp;&nbsp;</B>  " + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCodeDescription) + "</TD></TR></TABLE></TD></TR>");
		    		sCurrentServiceType = rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode);
			    	out.println("<TR><TD COLSPAN=5><HR></TD></TR>" + 
		    				"<TR>" +
	    						"<TD ALIGN=CENTER WIDTH=10%><FONT SIZE=2><B>Sales #</B></FONT></TD>" +
	    						"<TD ALIGN=CENTER WIDTH=15%><FONT SIZE=2><B>Order Date</B></FONT></TD>" +
	    						"<TD ALIGN=LEFT WIDTH=15%><FONT SIZE=2><B>Order Number</B></FONT></TD>" +
	    						"<TD ALIGN=LEFT WIDTH=30%><FONT SIZE=2><B>Bill To Name</B></FONT></TD>" +
	    						"<TD ALIGN=LEFT WIDTH=30%><FONT SIZE=2><B>Ship To Name</B></FONT></TD>" + 
							"</TR>" + 
		    				"<TR><TD COLSPAN=5><HR></TD></TR>");
		    	} 
		    	bFlipper = !bFlipper;
		    	if (bFlipper){
		    		sbgColor = "\"#FFFFFF\"";
		    	}else{
		    		sbgColor = "\"#DDDDDD\"";
		    	}
		    	out.println("<TR BGCOLOR=" + sbgColor + ">" +
								"<TD ALIGN=CENTER><FONT SIZE=2>" + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson) + "</FONT></TD>" +
								"<TD ALIGN=CENTER><FONT SIZE=2>" + USDateOnlyformatter.format(rs.getDate(SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderDate)) + "</FONT></TD>" +
								"<TD ALIGN=LEFT><FONT SIZE=2><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber) 
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" 
								+ rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber).trim() + "</A></FONT></TD>" +
								"<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName).trim() + "</FONT></TD>" +
								"<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName).trim() + "</FONT></TD>" + 
							"</TR>");    
		    }
		    if (!bHasRecord){
		    	out.println("<TR><TD ALIGN=CENTER><HR></TD></TR>");
		    	out.println("<TR><TD ALIGN=CENTER><B>No Record Found</B></TD></TR>");
		    	
		    }
			
		    out.println("</TABLE>");
		    rs.close();
		    
	    }catch (SQLException ex){
	    	System.out.println("Error in SMOpenOrdersReportGenerate!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
	    }
	    
		out.println("</BODY></HTML>");
	}
	
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}