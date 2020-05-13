package smcontrolpanel;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.servlet.ServletContext;

import SMClasses.SMWorkOrderHeader;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableworkorders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;

public class SMListNearbyOrdersReport extends java.lang.Object{

	private String m_sErrorMessage;
	private boolean bDebugMode = false;
	//private static final long serialVersionUID = 1L;

	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	private static SimpleDateFormat USDateOnlyformatter = new SimpleDateFormat("MM-dd-yyyy");
	private static final String SEARCH_RADIUS = ".25"; 
	
	public SMListNearbyOrdersReport(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(String sgeocode,
								 String sBaseOrderNumber,
								 Connection conn,
								 String sUserID,
								 PrintWriter out,
								 ServletContext context,
								 String sDBID,
								 String sLicenseModuleLevel
									){
		
		if (bDebugMode){
			System.out.println("[1360089432] GeoCode = " + sgeocode);
			System.out.println("[1360089544] BaseOrderNumber = " + sBaseOrderNumber);
		}
		if (
			(sgeocode.trim().compareTo("") == 0)
			|| (sgeocode.contains("NaN"))
		){
			//this order does not have geocode attached, don't do anything.
			printReportHeader(sUserID,
							  sBaseOrderNumber,
							  out,
							  context,
							  sDBID);
			
			out.println("<p class=warning>&nbsp;&nbsp;&nbsp;&nbsp;There is no geocode info for this order. Report can't be generated.</p>");
			return true;
		}
		String SQL = "SELECT" +
				 		" * " +
					  " FROM" +
						" (SELECT" +
							" " + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber + "," +
							"IF (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber + " = '" + sBaseOrderNumber.trim() 
								+ "', 0, 1) AS ISBASEORDER, " +
							
							" CAST(LEFT(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sgeocode + ", instr(sgeocode, ',')-1) AS DECIMAL(17,8)) AS LATITUDE," +
							" CAST(RIGHT(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sgeocode + ", LENGTH(sgeocode) - instr(sgeocode, ',')) AS DECIMAL(17,8)) AS LONGITUDE," +
							/*
							" SQRT(POW(" +
										"((" +
											"CAST(" + sgeocode.substring(0, sgeocode.indexOf(",") - 1) + " AS DECIMAL(17,8))" +
											" - " +
											"CAST(LEFT(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sgeocode + ", instr(sgeocode, ',')-1) AS DECIMAL(17,8))" +
										") / 2), " +
										"2) + " +
										
								  "POW(" +
								  		"(" + 
								  			"CAST(" + sgeocode.substring(sgeocode.indexOf(",") + 1) + " AS DECIMAL(17,8))" + 
								  			" - " +
								  			"CAST(RIGHT(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sgeocode + ", LENGTH(sgeocode) - instr(sgeocode, ',')) AS DECIMAL(17,8))" +
								  		")," +
								  		"2)" +
								 ") AS DISTANCE," +
							*/  	
								 
							" 3960 * acos(" + 
									" cos(radians(" + sgeocode.substring(0, sgeocode.indexOf(",") - 1) + "))" + 
									" *" + 
									" cos(radians(LEFT(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sgeocode + ", instr(sgeocode, ',')-1)))" + 
									" *" + 
									" cos(radians(RIGHT(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sgeocode + ", LENGTH(sgeocode) - instr(sgeocode, ',')))" +
											" -" +
										" radians(" + sgeocode.substring(sgeocode.indexOf(",") + 1) + ")" +
										")" + 
									" +" + 
									" sin(radians(" + sgeocode.substring(0, sgeocode.indexOf(",") - 1) + "))" +  
									" *" + 
									" sin(radians(LEFT(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sgeocode + ", instr(sgeocode, ',')-1)))" + 
							 	") AS DISTANCE," +
							
							" " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderDate + "," +
							" " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sgeocode + "," +
							" " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName + "," +
							" " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName + "," +
							" " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress1 + "," +
							" " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress2 + "," +
							" " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress3 + "," +
							" " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress4 + "," +
							" " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToCity + "," +
							" " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToState + "," +
							" " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToZip + "," +
							" " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToPhone + "," +
							" " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToContact + "," +
							" " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToFax + "," +
							" " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sshiptoemail + "," +
							" " + SMTableorderheaders.TableName + "." + SMTableorderheaders.ssecondaryshiptophone + 
						  " FROM" + 
							" " + SMTableorderheaders.TableName +
							
						  " WHERE (" +	
							" (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sgeocode + "!= '')" +
							" AND (" +
							" INSTR(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sgeocode + ", 'NaN') = 0)" +
							" AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + " != " +
								SMTableorderheaders.ORDERTYPE_QUOTE + ")" +
							" AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + " <= '1950-00-00')" +
							")" +
						") AS GEOQUERY" + 
					  " WHERE" +
					  	" " + "GEOQUERY.DISTANCE < " + SEARCH_RADIUS + //adjustable parameter for searching radius.
					  " ORDER BY GEOQUERY.ISBASEORDER"
					  + ", GEOQUERY.DISTANCE, GEOQUERY." + SMTableorderheaders.datOrderDate + " DESC LIMIT 1000"
			;
		
		if (bDebugMode){
			System.out.println("[1360089099] SQL = " + SQL);
			System.out.println("[1361313011] DISTANCE = (3960 * acos(" + 
					" cos(radians(" + sgeocode.substring(0, sgeocode.indexOf(",") - 1) + "))" + 
					" *" + 
					" cos(radians(LEFT(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sgeocode + ", instr(sgeocode, ',')-1)))" + 
					" *" + 
					" cos(radians(RIGHT(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sgeocode + ", LENGTH(sgeocode) - instr(sgeocode, ',')))" +
							" -" +
						" radians(" + sgeocode.substring(sgeocode.indexOf(",") + 1) + ")" +
						")" + 
					" +" + 
					" sin(radians(" + sgeocode.substring(0, sgeocode.indexOf(",") - 1) + "))" +  
					" *" + 
					" sin(radians(LEFT(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sgeocode + ", instr(sgeocode, ',')-1)))" + 
					") AS DISTANCE)");
		}
		//Check permissions:
		//boolean bViewJobCostPermitted = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.JobCostSummaryReport,
		//																	  sUserName,
		//																	  conn);

		boolean bViewOrderPermitted = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMViewOrderInformation,
																			sUserID,
																			conn,
																			sLicenseModuleLevel);
		printReportHeader(sUserID,
						  sBaseOrderNumber,
						  out,
						  context,
						  sDBID);
		
		printColumnHeader(out);
		
		boolean bAllowEditWorkOrders = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMEditWorkOrders, sUserID, context, sDBID, sLicenseModuleLevel);
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				//Print the line:
				String sOrderNumber = rs.getString(SMTableorderheaders.strimmedordernumber);
				String sClass = "normal";
				if(sOrderNumber.compareTo(sBaseOrderNumber) == 0){
					sClass = "dark";
				}
				out.println("<tr class=" + sClass + ">");
				
				//checkbox for detailed Job Cost info:
				String sJobCostInfo = "";
				SQL = "SELECT"
				+ " " + SMTableworkorders.TableName + "." + SMTableworkorders.datscheduleddate
				//+ ", " + SMTablemechanics.TableName + "." + SMTablemechanics.sMechFullName 
				+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.smechanicname
				+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.bdqtyofhours
				+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.mworkdescription
				+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.lid
				+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.iposted
				+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.mcomments
				+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.madditionalworkcomments
				+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.mdetailsheettext
				+ " FROM " + SMTableworkorders.TableName 
				//+ " LEFT JOIN " + SMTablemechanics.TableName + " ON " + SMTableworkorders.TableName + "." + SMTableworkorders.imechid
				//+ " = " + SMTablemechanics.TableName + "." + SMTablemechanics.lid
				+ " WHERE (" 
					+ SMTableworkorders.TableName + "." + SMTableworkorders.strimmedordernumber + " = '" + sOrderNumber + "'"
				+ ")"
				+ " ORDER BY" + " " + SMTableworkorders.TableName + "." + SMTableworkorders.datscheduleddate + " DESC"; 
				if (bDebugMode){
					System.out.println("[1360089100] Jobcost SQL = " + SQL);
				}
				ResultSet rsJobCost = clsDatabaseFunctions.openResultSet(SQL, conn);
				String sJobCostLink = "";
				if (rsJobCost.next() 
						//&& sOrderNumber.compareTo(sBaseOrderNumber) != 0 
						//&& bViewJobCostPermitted
						){
					sJobCostLink = "<input type=\"checkbox\" id=\"jcchoice" + sOrderNumber + "\" onclick=\"showJobCostInfo('" + sOrderNumber + "')\">";
				}else{
					sJobCostLink = "&nbsp;";
				}
				/*
				out.println("<td class=noborder ALIGN=CENTER VALIGN=TOP>" + sJobCostLink + "</td>");
				 */
				//Distance:
				if (sOrderNumber.compareTo(sBaseOrderNumber) == 0){
					//if this line is for base order, show 0 as distance.
					out.println("<td class=noborder ALIGN=CENTER VALIGN=TOP>0.00&nbsp;ft</td>");
				}else{
					if (rs.getBigDecimal("DISTANCE").compareTo(new BigDecimal("0.1")) < 0){
						out.println("<td class=noborder ALIGN=CENTER VALIGN=TOP><p>" + rs.getBigDecimal("DISTANCE").multiply(new BigDecimal("5280")).setScale(2, BigDecimal.ROUND_HALF_UP) + "&nbsp;ft</p></td>");
					}else{
						out.println("<td class=noborder ALIGN=CENTER VALIGN=TOP><p>" + rs.getBigDecimal("DISTANCE").setScale(2, BigDecimal.ROUND_HALF_UP) + "&nbsp;mi</p></td>");
					}
						
				}

				//Order Number
				String sOrderNumberLink = "";
				if (bViewOrderPermitted){
					sOrderNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
					+ "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" + sOrderNumber 
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" 
					+ sOrderNumber + "</A>";
				}else{
					sOrderNumberLink = sOrderNumber;
				}
				out.println("<td class=noborder ALIGN=RIGHT VALIGN=TOP>" + sJobCostLink + "&nbsp;" + sOrderNumberLink + "</td>");
				
				//Order Date
				java.sql.Date datSaleDate = rs.getDate(SMTableorderheaders.datOrderDate);
				out.println("<td class=noborder ALIGN=CENTER VALIGN=TOP>" + clsDateAndTimeConversions.utilDateToString(datSaleDate, "MM/dd/yyyy")+ "</td>");
			
				//Bill to Name:
				out.println("<td class=noborder ALIGN=LEFT VALIGN=TOP>" + rs.getString(SMTableorderheaders.sBillToName) + "</td>");
				
				//Ship to Name:
				out.println("<td class=noborder ALIGN=LEFT VALIGN=TOP>" + rs.getString(SMTableorderheaders.sShipToName) + "</td>");
				
				//Ship to Address:
				String sAddress = rs.getString(SMTableorderheaders.sShipToAddress1).trim();

				if (rs.getString(SMTableorderheaders.sShipToAddress2).trim().compareToIgnoreCase("") != 0){
					sAddress += "&nbsp;" + rs.getString(SMTableorderheaders.sShipToAddress2).trim();
				}

				if (rs.getString(SMTableorderheaders.sShipToAddress3).trim().compareToIgnoreCase("") != 0){
					sAddress += "&nbsp;" + rs.getString(SMTableorderheaders.sShipToAddress3).trim();
				}
				if (rs.getString(SMTableorderheaders.sShipToAddress4).trim().compareToIgnoreCase("") != 0){
					sAddress += "&nbsp;" + rs.getString(SMTableorderheaders.sShipToAddress4).trim();
				}
				sAddress += ",&nbsp;" + rs.getString(SMTableorderheaders.sShipToCity).trim() + ",&nbsp;" +
									 rs.getString(SMTableorderheaders.sShipToState).trim() + "&nbsp;" +
									 rs.getString(SMTableorderheaders.sShipToZip).trim();
				out.println("<td class=noborder ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + sAddress + "</td>");
				
				//Contact Name:
				out.println("<td class=noborder ALIGN=LEFT VALIGN=TOP>" + rs.getString(SMTableorderheaders.sShipToContact) + "</td>");
				
				//Phone:
				String sPhones = rs.getString(SMTableorderheaders.sShipToPhone);
				if (rs.getString(SMTableorderheaders.ssecondaryshiptophone).trim().compareTo("") != 0){
					sPhones += "<br>" + rs.getString(SMTableorderheaders.ssecondaryshiptophone);
				}
				out.println("<td class=noborder ALIGN=CENTER VALIGN=TOP>" + sPhones + "</td>");
				
				//Fax:
				out.println("<td class=noborder ALIGN=CENTER VALIGN=TOP>" + rs.getString(SMTableorderheaders.sShipToFax) + "</td>");
				
				//Email:
				out.println("<td class=noborder ALIGN=CENTER VALIGN=TOP>" + rs.getString(SMTableorderheaders.sshiptoemail) + "</td>");
				out.println("</tr><tr><td class=noborder HEIGHT=2px>&nbsp;</td><td class=noborder COLSPAN=9 HEIGHT=2px>");
				
				//print out job cost info
				sJobCostInfo += "<div id= \"jc" + sOrderNumber + "\" style=\"display:none;\">" +
								"<table class=noborder Width=100%>";
				rsJobCost.beforeFirst();
				if (rsJobCost.next()){
					//print column header
					sJobCostInfo += "<tr class=dark>" +
							"<td ALIGN=CENTER VALIGN=VTOP WIDTH=15%><B>Date</B></td>" +
							"<td ALIGN=CENTER VALIGN=VTOP WIDTH=10%><B>Mechanic</B></td>" +
							"<td ALIGN=CENTER VALIGN=VTOP WIDTH=10%><B>Hour</B></td>" +
							"<td ALIGN=LEFT VALIGN=VTOP WIDTH=55%><B>Work Description</B></td>"
							+ "<td ALIGN=LEFT VALIGN=VTOP WIDTH=10%><B>Work order</B></td>"
						+ "</tr>";
					String sWorkOrderID = Long.toString(rsJobCost.getLong(SMTableworkorders.TableName + "." + SMTableworkorders.lid));
					if (sWorkOrderID == null){
						sWorkOrderID = "N/A";
					}else {
						if (sWorkOrderID.compareToIgnoreCase("0") == 0){
							sWorkOrderID = "N/A";
						}
					}
					//First, a link to the ID for editing:
					String sWOLink = sWorkOrderID;
					if (bAllowEditWorkOrders && (sWorkOrderID.compareToIgnoreCase("N/A") != 0)){
						sWOLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMWorkOrderEdit?"
							+ SMWorkOrderHeader.Paramlid + "=" + sWorkOrderID
							+ "&" + SMWorkOrderHeader.Paramstrimmedordernumber + "=" + sOrderNumber.trim()
							+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
							+ "\">" + sWorkOrderID 
							+ "</A>"
						;
					}
					//Next a link to VIEW the work order:
					String sViewWOLink = "(Not posted)";
					if (rsJobCost.getInt(SMTableworkorders.TableName + "." + SMTableworkorders.iposted) == 1){
						sViewWOLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMWorkOrderAction?"
							+ SMWorkOrderEdit.COMMAND_FLAG + "=" + SMWorkOrderEdit.PRINTRECEIPTCOMMAND_VALUE
							+ "&" + SMWorkOrderHeader.Paramlid + "=" + sWorkOrderID
							+ "&" + SMWorkOrderHeader.Paramstrimmedordernumber + "=" + sOrderNumber.trim()
							+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
							+ "\">" + "View" 
							+ "</A>"
						;
					}
					SMWorkOrderHeader wo = new SMWorkOrderHeader();
					String sWorkDescription = wo.createWorkDescription(
							rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.mworkdescription), 
							rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.mcomments), 
							rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.madditionalworkcomments),
							rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.mdetailsheettext),
							Double.toString(rsJobCost.getDouble(SMTableworkorders.TableName + "." + SMTableworkorders.dPrePostingWODiscountAmount)));
					sJobCostInfo += "<tr style = \" vertical-align:top; \">" +
							"<td ALIGN=CENTER VALIGN=VTOP>" + USDateOnlyformatter.format(rsJobCost.getDate(SMTableworkorders.TableName + "." + SMTableworkorders.datscheduleddate)) + "</td>" +
							"<td ALIGN=CENTER VALIGN=VTOP>" + rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.smechanicname) + "</td>" + 
							"<td ALIGN=CENTER VALIGN=VTOP>" + clsManageBigDecimals.BigDecimalToScaledFormattedString(
								SMTableworkorders.bdqtyofhoursScale, rsJobCost.getBigDecimal(SMTableworkorders.TableName + "." + SMTableworkorders.bdqtyofhours))  + "</td>" +
							"<td ALIGN=LEFT VALIGN=VTOP>" + sWorkDescription + "</td>"
							+ "<td ALIGN=LEFT VALIGN=VTOP>" + sWOLink + "&nbsp;" + sViewWOLink + "</td>"
						+ "</tr>";
				
					while (rsJobCost.next()){
						sWorkDescription = wo.createWorkDescription(
								rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.mworkdescription), 
								rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.mcomments), 
								rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.madditionalworkcomments),
								rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.mdetailsheettext),
								rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.dPrePostingWODiscountAmount));
						sJobCostInfo += "<tr style = \" vertical-align:top; \">" +
							"<TD ALIGN=CENTER VALIGN=VTOP>" + USDateOnlyformatter.format(rsJobCost.getDate(SMTableworkorders.TableName + "." + SMTableworkorders.datscheduleddate)) + "</td>" +
							"<TD ALIGN=CENTER VALIGN=VTOP>" + rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.smechanicname) + "</td>" +
							"<TD ALIGN=CENTER VALIGN=VTOP>" + clsManageBigDecimals.BigDecimalToScaledFormattedString(
								SMTableworkorders.bdqtyofhoursScale, rsJobCost.getBigDecimal(SMTableworkorders.TableName + "." + SMTableworkorders.bdqtyofhours)) + "</td>" +
							"<TD ALIGN=LEFT VALIGN=VTOP>" + sWorkDescription + "</td>"
							+ "<td ALIGN=LEFT VALIGN=VTOP>" + sWOLink + "&nbsp;" + sViewWOLink + "</td>"
							+ "</tr>";
					}
				}else{
					sJobCostInfo += "<tr>" +
										"<td ALIGN=CENTER VALIGN=VTOP><B>No job cost information</B></td>" +
									"</tr>";
				}
				rsJobCost.close();
				sJobCostInfo += "</table>" + 
								"</div>" +
								"</td></tr>"
								;
				out.println(sJobCostInfo);
			}
			rs.close();
    	}catch (SQLException e){
    		m_sErrorMessage = "[1360089057]Error reading resultset for SQL: '" + SQL + " - " + e.getMessage();
    		return false;
    	}

    	out.println("</TABLE>");
    	
		return true;
	}
	
	private void printColumnHeader(PrintWriter out){
		
		out.println(printScripts());
		out.println("<table WIDTH=100%>");
		out.println("<tr>");
		//out.println("<th width=3%><B>Show&nbsp;Job<BR>Cost&nbsp;Info</B></th>");
		out.println("<th width=3%><B>Distance</FONT></B></TD>");
		out.println("<th width=3%><B>Order<br>Number</B></th>");
		out.println("<th width=5%><B>Order<br>Date</B></th>");
		out.println("<th width=14%><B>Bill&nbsp;to<br>Name</B></th>");
		out.println("<th width=14%><B>Ship&nbsp;to<br>Name</B></th>");
		out.println("<th width=30%><B>Ship&nbsp;to<br>Address</B></th>");
		out.println("<th width=9%><B>Contact<br>Name</B></th>");
		out.println("<th width=6%><B>Phone</B></th>");
		out.println("<th width=6%><B>Fax</B></th>");
		out.println("<th width=10%><B>Email</B></th>");
		out.println("</tr>");
	}
	
	private void printReportHeader(String sUserName,
								   String sOrderNumber,
								   PrintWriter out,
								   ServletContext context,
								   String sDBID){
		
    	//Customized title
    	String sReportTitle = "Proximity Order List";
    	String sCriteria = "Previous orders within the proximity of order number <FONT SIZE=3><B>" + sOrderNumber + "</B></FONT>";
    	
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + "</TITLE>" +
	       	"<link rel=\"stylesheet\" type=\"text/css\" href=\"scripts/smcp.css\">" +
	       /*
	        "<script type=\"text/javascript\"" + 
	        "src=\"https://maps.googleapis.com/maps/api/js?sensor=false\">" +
	        "</script>" +
	        */
	       "</HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"#FFFFFF\">" +
		   "<table class=noborder WIDTH=100%>" +
		   "<tr><td class=noborder ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
		   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) + " Printed by " + sUserName 
		   + "</FONT></TD></TR>" +
		   "<tr><td class=noborder VALIGN=BOTTOM COLSPAN=2><B>" + sReportTitle + "</B></td></tr>" +
		   
		   "<tr><td class=noborder COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></td></tr></table>");
    	
		out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMUserLogin?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to user login</A><BR>");
	
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMURLHistoryList?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
					+ sDBID + "\">Return to...</A><BR>");

	    	out.println("<p> ** Click on the check box next to order number to display job cost information.</p>");
	}
	
	private String printScripts(){
		String s = "";
		
		s += "<NOSCRIPT>\n"
			+ "    <font color=red>\n"
			+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "    </font>\n"
			+ "</NOSCRIPT>\n"
		;

		s += "<script type='text/javascript'\n"
		    + "src=\"https://maps.googleapis.com/maps/api/js?libraries=geometry&sensor=false\">\n"
		    + "</script>\n";

		s += "<script type='text/javascript'>\n";
		s += "function showJobCostInfo(snumber) {\n" 
				+ "    var status = document.getElementById('jcchoice' + snumber).checked;\n" 
				+ "    if (status == true) {\n"
				+ "        document.getElementById('jc' + snumber).style.display = \"inline\";\n"
				+ "    } else {\n"
				+ "        document.getElementById('jc' + snumber).style.display = \"none\";\n"
				+ "    }\n" 
				+ "}\n"
			;
		
		s += "function calculateDistance(snumber, " +
										"sbasecorLat, " +
										"sbasecorLng, " +
										"stargetcorLat, " +
										"stargetcorLng){\n" +
				//" alert('calculating distance for order ' + snumber + '.');\n" +  
				"	var baseLatLng = new google.maps.LatLng(parseFloat(sbasecorLat), parseFloat(sbasecorLng));\n" +
				"	var targetLatLng = new google.maps.LatLng(parseFloat(stargetcorLat), parseFloat(stargetcorLng));\n" +
				"	var distance = google.maps.geometry.spherical.computeDistanceBetween(baseLatLng, targetLatLng) ;\n" +
				//"	alert('distance = ' + distance);\n" +
				"	if (distance < 160){\n" +
				//"		alert('use ft.');\n" +
				"		document.getElementById('dist' + snumber).innerHTML =\"<p>\" + (distance * 3.28084).toFixed(2) + \"&nbsp;ft</p>\";\n" +
				" 	}else{\n" +
				//"		alert('use mi');\n" +
				"		document.getElementById('dist' + snumber).innerHTML =\"<p>\" + (distance * 0.000621371).toFixed(4) + \"&nbsp;mi</p>\";\n" +
				"	}\n" +
				/*
				"	var distance = function(sbasecorLat, sbasecorLng, stargetcorLat, stargetcorLng){\n" +
				"		var R = 6371;\n" +
				"		var dLat  = rad(parseFloat(stargetcorLat) - pareFloat(sbasecorLat));\n" +
				"		var dLong = rad(parseFloat(stargetcorLng) - parseFloat(sbasecorLng));\n" +
				"		var a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(rad(parseFloat(sbasecorLat)) * Math.cos(parseFloat(stargetcorLat)) * Math.sin(dLong/2) * Math.sin(dLong/2);\n" +
				"		var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));\n" +
				"		var d = R * c;\n" +
				"		return d.toFixed(2);\n" +
				"	};\n" +
				*/
				"}\n";
		
		s += " function rad(x) {return x*Math.PI/180;}\n";
		
		s += "</script>";
		return s;
	}
	
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
