package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.math.BigDecimal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMProductivityReportGenerate extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private String sDBID = "";
	private String sUserID = "";
	private String sUserFullName = "";
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
				SMSystemFunctions.SMProductivityReport))
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

    	//Calculate time period
	    SimpleDateFormat USDateTimeformatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
    	SimpleDateFormat USDateOnlyformatter = new SimpleDateFormat("MM/dd/yyyy");

    	String title = "Productivity Report";
    	String sSQL = "";
    	ResultSet rs = null;

	    out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">" +
			        "<HTML>" +
			        "<HEAD><TITLE>" + title + "</TITLE></HEAD>\n<BR>" + 
				    "<BODY BGCOLOR=\"#"
			        + SMUtilities.getInitBackGroundColor(getServletContext(), sDBID)
			        + "\">");
		
	 	   //log usage of this this report
	 	   SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
	 	   log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMPRODUCTIVITYREPORT, "REPORT", "SMProductivityReport", "[1376509350]");

		String sStartDate = clsManageRequestParameters.get_Request_Parameter("StartingDate", request);
		if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sStartDate)){
			out.println("Invalid start date - '" + sStartDate);
			return;
		}
		String sEndDate = clsManageRequestParameters.get_Request_Parameter("EndingDate", request);
		if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sEndDate)){
			out.println("Invalid end date - '" + sEndDate);
			return;
		}
		
		Timestamp datStartingDate = clsDateAndTimeConversions.StringToTimestamp("M/d/yyyy", sStartDate);
		Timestamp datEndingDate = clsDateAndTimeConversions.StringToTimestamp("M/d/yyyy", sEndDate);
	    
	    //Location & Service Types & Item Categories
	    boolean bCheckItemCategories = false;
	    if (request.getParameter("CheckItemCategories") != null){
	    	bCheckItemCategories = true;
    	}
	    
		Enumeration<?> paramNames = request.getParameterNames();
		ArrayList<String> alLocations = new ArrayList<String>(0);
		ArrayList<String> alServiceTypes = new ArrayList<String>(0);
		ArrayList<String> alItemCategories = new ArrayList<String>(0);
		while(paramNames.hasMoreElements()) {
			String s = paramNames.nextElement().toString();
			//System.out.println("paramNames.nextElement() = " + s);
			
			try{
				//System.out.println("s.subString(1, 2) = " + s.substring(1, 2));
				int iSwitch = Integer.parseInt(s.substring(1, 2));

    			switch (iSwitch) {
	                case 1: 
            			alLocations.add(s.substring(3));
        				break;
	                case 2: 
            			alServiceTypes.add(s.substring(3));
        				break;
	                case 3: 
            			if (bCheckItemCategories){
            				alItemCategories.add(s.substring(3));
            			}else{
            				alItemCategories.add("ALLIC");
            			}
        				break;
    			}
			}catch(Exception ex){
				//System.out.println("Not a parameter we care about at this point.");
			}
		}
	    
	    //Status check
	    boolean bShowSubtotalOnly = false;
	    if (request.getParameter("CheckSubtotalOnly") != null){
	    	bShowSubtotalOnly = true;
    	}
	    /*************END of PARAMETER list***************/
	    
	    boolean bHasRecord = false;
	    sSQL = SMMySQLs.Get_Productivity_Report_SQL(datStartingDate,
	    											datEndingDate,
	    											alLocations,
	    											alServiceTypes,
	    											alItemCategories
	    											);
	    try{
	    	rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	    }catch (SQLException ex){
	    	System.out.println("Error when opening rs for productivity report!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
	    }
	    
	    try{
		    out.println("<TABLE BORDER=0 WIDTH=100%>");
		    out.println("<TR><TD ALIGN=CENTER><FONT SIZE=4><B>Mechanic Productivity Report</B></FONT><BR>" +
		    								 "<FONT SIZE=2>" + sCompanyName + "</FONT><BR>" +
		    								 "<FONT SIZE=2>" + USDateTimeformatter.format(new Date(System.currentTimeMillis())) + "</FONT><BR><BR>" +
		    								 "<FONT SIZE=2>Based on Invoice(s) From  " +
		    								 		USDateOnlyformatter.format(datStartingDate) + " To " + 
		    								 		USDateOnlyformatter.format(datEndingDate) + "</FONT><BR>" +
				    						 "<FONT SIZE=2>Printed by " + sUserFullName + "</FONT><BR>" +
						   "</TD></TR>");
		    
		    Long lCurrentMechanicID = 0L;
		    String sCurrentMechanicName = null;
		    String sCurrentInvoiceNumber = null;
		    BigDecimal bdInvoiceTotal = BigDecimal.ZERO;
		    BigDecimal bdMechanicTotal = BigDecimal.ZERO;
		    //BigDecimal bdGrandTotal = BigDecimal.ZERO;
		    
		    while (rs.next()){	    
		    	bHasRecord = true;
		    	if (lCurrentMechanicID == 0 
		    		|| rs.getLong(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.imechid) != lCurrentMechanicID){
		    		//new mechanic, printout last mechanic's total.
		    		if (lCurrentMechanicID != 0){
		    			if (!bShowSubtotalOnly){
			    			//print out total for this invoice 
			    			out.println("<TR><TD COLSPAN=5><HR></TD></TR>");
			    			out.println("<TR><TD>&nbsp;</TD>" +
			    							"<TD ALIGN=RIGHT COLSPAN=3>Total For Invoice#&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + SMUtilities.lnViewInvoice(sDBID, sCurrentInvoiceNumber ) + "\">" + sCurrentInvoiceNumber + "</A>:</TD>" + 
			    							"<TD ALIGN=RIGHT>" + bdInvoiceTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "</TD>" + 
			    							"</TR></TABLE></TD></TR></TABLE></TD></TR>");
		    			}
		    			//print out total for this mechanic
		    			out.println("<TR><TD ALIGN=RIGHT COLSPAN=4><B>Total For Mechanic " + sCurrentMechanicName + ":</B>   " + 
		    							bdMechanicTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + 
		    							"</TD></TR>");
		    			out.println("</TD></TR>");
		    		}
		    		//print out new name
		    		if (rs.getLong(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.imechid) == 0){
		    			out.println("<TR><TD><HR></TD></TR><TR><TD ALIGN=LEFT><FONT SIZE=4><B>Mechanic: N/A</B></FONT></TD></TR>");
		    		}else{
		    			out.println("<TR><TD><HR></TD></TR><TR><TD ALIGN=LEFT><FONT SIZE=4><B>Mechanic: " 
		    				+ rs.getString(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sMechFullName) + "</B></FONT></TD></TR>");
		    		}

		    		lCurrentMechanicID = rs.getLong(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.imechid);
		    		sCurrentMechanicName = rs.getString(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sMechFullName);
		    		sCurrentInvoiceNumber = rs.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber).trim();
		    		//System.out.println("Inv: " + sCurrentInvoiceNumber);

	    			bdMechanicTotal = BigDecimal.ZERO;
		    		bdInvoiceTotal = BigDecimal.ZERO;
		    		if (!bShowSubtotalOnly){
					    out.println("<TR><TD><TABLE BORDER=1 WIDTH=100%>" +
							    		"<TR><TD><TABLE BORDER=0 WIDTH=100%>" +
							    					"<TR>" +
							    						"<TD ALIGN=LEFT WIDTH=15%><FONT SIZE=2><B>Inv#:</B>&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + SMUtilities.lnViewInvoice(sDBID, rs.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber)) + "\">" + rs.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber).trim() + "</A></FONT></TD>" +
							    						"<TD ALIGN=LEFT WIDTH=15%><FONT SIZE=2><B>Date:</B>&nbsp;"+ USDateOnlyformatter.format(rs.getDate(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate)) + "</FONT></TD>" +
							    						"<TD ALIGN=LEFT WIDTH=35%><FONT SIZE=2><B>Bill To:</B>&nbsp;" + rs.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sBillToName) + "</FONT></TD>" +
							    						"<TD ALIGN=LEFT WIDTH=35%><FONT SIZE=2><B>Ship To:</B>&nbsp;" + rs.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sShipToName) + "</FONT></TD>" +
						    						"</TR>" + 
					    						"</TABLE>" + 
			    						"</TD></TR>" + 
					    
			    						"<TR><TD><TABLE BORDER=0 WIDTH=100%>" +
							    					"<TR>" +
							    						"<TD ALIGn=LEFT WIDTH=15%><FONT SIZE=1><B>Item Number</B></FONT></TD>" +
							    						"<TD ALIGN=LEFT WIDTH=50%><FONT SIZE=1><B>Item Description</B></FONT></TD>" +
							    						"<TD ALIGN=RIGHT WIDTH=10%><FONT SIZE=1><B>Qty.Shipped</B></FONT></TD>" +
							    						"<TD ALIGN=CENTER WIDTH=12%><FONT SIZE=1><B>UOM</B></FONT></TD>" +
							    						"<TD ALIGN=RIGHT WIDTH=13%><FONT SIZE=1><B>Ext. Price</B></FONT></TD>" + 
						    						"</TR>" + 
						    						"<TR><TD COLSPAN=5><HR></TD></TR>" +
										"</TD></TR>");
		    		}
		    	}

			    if (sCurrentInvoiceNumber.compareTo(rs.getString(SMTableinvoiceheaders.sInvoiceNumber).trim()) != 0){
			    	if (!bShowSubtotalOnly){
		    			//print out total for this invoice 
		    			out.println("<TR><TD COLSPAN=5><HR></TD></TR>");
		    			out.println("<TR><TD>&nbsp;</TD>" +
		    							"<TD ALIGN=RIGHT COLSPAN=3>Total For Invoice#&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + SMUtilities.lnViewInvoice(sDBID, sCurrentInvoiceNumber ) + "\">" + sCurrentInvoiceNumber + "</A>:</TD>" + 
		    							"<TD ALIGN=RIGHT>" + bdInvoiceTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "</TD>" + 
		    							"</TR></TABLE></TD></TR></TABLE></TD></TR>");
			    	}
	    			sCurrentInvoiceNumber = rs.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber).trim();
	    			//System.out.println("Inv: " + sCurrentInvoiceNumber);
	    			//reset invoice total counter
	    			bdInvoiceTotal = BigDecimal.ZERO;
	    			if (!bShowSubtotalOnly){
					    out.println("<TR><TD><TABLE BORDER=1 WIDTH=100%>" +
							    		"<TR><TD><TABLE BORDER=0 WIDTH=100%>" +
							    					"<TR>" +
							    						"<TD ALIGN=LEFT WIDTH=15%><FONT SIZE=2><B>Inv#:</B>&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + SMUtilities.lnViewInvoice(sDBID, rs.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber) ) + "\">" + rs.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber).trim() + "</A></FONT></TD>" +
							    						"<TD ALIGN=LEFT WIDTH=15%><FONT SIZE=2><B>Date:</B> "+ USDateOnlyformatter.format(rs.getDate(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate)) + "</FONT></TD>" +
							    						"<TD ALIGN=LEFT WIDTH=35%><FONT SIZE=2><B>Bill To:</B> " + rs.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sBillToName) + "</FONT></TD>" +
							    						"<TD ALIGN=LEFT WIDTH=35%><FONT SIZE=2><B>Ship To:</B> " + rs.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sShipToName) + "</FONT></TD>" +
						    						"</TR>" + 
					    						"</TABLE>" + 
			    						"</TD></TR>" + 
					    
			    						"<TR><TD><TABLE BORDER=0 WIDTH=100%>" +
							    					"<TR>" +
							    						"<TD ALIGn=LEFT WIDTH=15%><FONT SIZE=2><B>Item Number</B></FONT></TD>" +
							    						"<TD ALIGN=LEFT WIDTH=50%><FONT SIZE=2><B>Item Description</B></FONT></TD>" +
							    						"<TD ALIGN=RIGHT WIDTH=10%><FONT SIZE=2><B>Qty.Shipped</B></FONT></TD>" +
							    						"<TD ALIGN=CENTER WIDTH=12%><FONT SIZE=2><B>UOM</B></FONT></TD>" +
							    						"<TD ALIGN=RIGHT WIDTH=13%><FONT SIZE=2><B>Ext. Price</B></FONT></TD>" + 
						    						"</TR>" + 
						    						"<TR><TD COLSPAN=5><HR></TD></TR>" +
										"</TD></TR>");
	    			}
			    }

			    BigDecimal bdLine = BigDecimal.valueOf(rs.getDouble(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dExtendedPrice));
			    if (!bShowSubtotalOnly){
				    out.println("<TR>" +
									"<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemNumber).trim() + "</FONT></TD>" +
									"<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sDesc).trim() + "</FONT></TD>" +
									"<TD ALIGN=RIGHT><FONT SIZE=2>" + rs.getDouble(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dQtyShipped) + "</FONT></TD>" +
									"<TD ALIGN=CENTER><FONT SIZE=2>" + rs.getString(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sUnitOfMeasure).trim() + "</FONT></TD>" +
									"<TD ALIGN=RIGHT><FONT SIZE=2>" + bdLine.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "</FONT></TD>" + 
								"</TR>");
			    }
			    bdInvoiceTotal = bdInvoiceTotal.add(bdLine);
			    bdMechanicTotal = bdMechanicTotal.add(bdLine);
			    //bdGrandTotal = bdGrandTotal.add(bdLine);
			    //System.out.println("   " + bdLine + " / " + bdInvoiceTotal + " / " + bdMechanicTotal + " / " + bdGrandTotal);
			    
		    }
		    if (bHasRecord){
			    //print out last total
		    	if (!bShowSubtotalOnly){
				    out.println("<TR><TD COLSPAN=5><HR></TD></TR>");
					out.println("<TR><TD>&nbsp;</TD>" +
									"<TD ALIGN=RIGHT COLSPAN=3>Total For Invoice#&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + SMUtilities.lnViewInvoice(sDBID, sCurrentInvoiceNumber ) + "\">" + sCurrentInvoiceNumber + "</A>:</TD>" + 
									"<TD ALIGN=RIGHT>" + bdInvoiceTotal.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>" + 
									"</TR></TABLE></TD></TR></TABLE></TD></TR>");
		    	}
				//print out total for this mechanic
				out.println("<TR><TD ALIGN=RIGHT COLSPAN=4><B>Total For Mechanic " + sCurrentMechanicName + ":</B>   " + 
								bdMechanicTotal.setScale(2, BigDecimal.ROUND_HALF_UP) + 
								"</TD></TR>");
				out.println("</TD></TR>");
		    }else{
		    	out.println("<TR><TD ALIGN=CENTER><HR></TD></TR>");
		    	out.println("<TR><TD ALIGN=CENTER><B>No Record Found</B></TD></TR>");
		    	
		    }
			/*
			//print out grand total
			out.println("<TR><TD><HR></TD></TR>");
			out.println("<TR><TD ALIGN=RIGHT COLSPAN=3><FONT SIZE=5><B>Grand Total :   " + 
							bdGrandTotal.setScale(2, BigDecimal.ROUND_HALF_UP) + 
							"</B></FONT></TD></TR>");
			out.println("</TD></TR>");
			*/
		    out.println("</TABLE>");
		    rs.close();
		    
	    }catch (SQLException ex){
	    	System.out.println("Error in SMProductivityReportGenerate!!");
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