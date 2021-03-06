package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import SMClasses.SMOption;
import SMClasses.SMWorkOrderHeader;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableinvoicedetails;
import SMDataDefinition.SMTableinvoiceheaders;
import SMDataDefinition.SMTablemechanics;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesalesperson;
import SMDataDefinition.SMTableworkorders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMDisplayJobCostInformation extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a");
	NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);
	private boolean bDebugMode = false;
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				-1L))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    String sUserFullName = SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, sCallingClass);
    	String sOrderNumber = clsManageRequestParameters.get_Request_Parameter("OrderNumber", request);
    	
    	//Customized title
    	String sReportTitle = "Job Cost Summary";
    	out.println(SMUtilities.SMCPTitleSubBGColor(sReportTitle, "", "" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "", sCompanyName));
    	out.println("<FONT SIZE=2>Printed by:" + sUserFullName + "&nbsp;&nbsp;" + USDateformatter.format(System.currentTimeMillis()) + "</FONT><BR>");

	    out.println("<FONT SIZE=2><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A></FONT><BR><BR>");
    	//log usage of this this report
	    SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMDISPLAYJOBCOSTINFO, "REPORT", "SMDisplayJobCostInformation", "[1376509319]");
	    
		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
	    if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    
    	Connection conn = clsDatabaseFunctions.getConnection(getServletContext(), sDBID, "MySQL", "smcontrolpanel.SMDisplayJobCostInformation/View mode - " + sUserFullName + "@" + (new Timestamp(System.currentTimeMillis())).toString());
    	if (conn == null){
    		sWarning = "Unable to get data connection.";
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
    	}

    	if (!displayJobCostInfo(
    		conn, 
    		sOrderNumber, 
    		out,
    		(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL),
    		sDBID,
    		sUserID)
    	){
    		
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080460]");
    	out.println("<BR><B>*  </B> Total cost is the sum of extended cost in all invoice(s) and credit note(s) under the selected order.<BR>" +
    			"							  <B>** </B> Total invoiced is the sum of price after discount in all invoice(s) and credit note(s) under the selected order.");
	    out.println("</BODY></HTML>");
	}
	
	private boolean displayJobCostInfo(
		Connection conn, 
		String sOrderNum, 
		PrintWriter pwOut, 
		String sLicenseModuleLevel, 
		String sDBID,
		String sUserID
		){
	
		//TJR - 11/20/09 - changed from a straight join to a left join to pick up records that
		//didn't have a corresponding record in the salespersons table:
		String SQL = "SELECT * FROM"
			+ " " + SMTableorderheaders.TableName
			 			
			+ " LEFT JOIN " + SMTableworkorders.TableName + " ON " 
			+ SMTableworkorders.TableName + "." + SMTableworkorders.strimmedordernumber 
			+ " = " + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
			
			+ " LEFT JOIN " + SMTablesalesperson.TableName + " ON "
			+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson 
			+ " = " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode
			
			//TJR - 11/11/11 - changed this to a left join to make sure we pick up records
			//if the mechanic has been deleted:
			+ " LEFT JOIN " + SMTablemechanics.TableName + " ON "
			+ SMTableworkorders.TableName + "." + SMTableworkorders.imechid + " = "
		 	+ SMTablemechanics.TableName + "." + SMTablemechanics.lid

			+ " WHERE ("
				+ SMTableworkorders.TableName + "." + SMTableworkorders.strimmedordernumber + " = '" 
			 		+ sOrderNum.trim() + "'"
			+ ")"
			+ " ORDER BY"
			+ " " + SMTableworkorders.TableName + "." + SMTableworkorders.datscheduleddate
			+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.lid
			;

		if (bDebugMode){
			System.out.println("[1543512406] In " + this.toString() + " SQL: " + SQL);
		}
		 String sColor = SMUtilities.getInitBackGroundColor(getServletContext(), sDBID);
		try{
			ResultSet rsOrder = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rsOrder.next()){
				pwOut.println("<TABLE BORDER=0 WIDTH=100% BGCOLOR=\"" + sColor +  "\"><TR>");
				pwOut.println("<TD ALIGN=LEFT WIDTH=20%><FONT SIZE=2><B>Order #:</B><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
										     		+ rsOrder.getString(SMTableorderheaders.sOrderNumber).trim() 
										     		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
										     		+ "\">" + rsOrder.getString(SMTableorderheaders.sOrderNumber).trim() + "</A></FONT></TD>" +
							  "<TD ALIGN=LEFT WIDTH=40%><FONT SIZE=2><B>Bill To Name:</B> " + rsOrder.getString(SMTableorderheaders.sBillToName).trim() + "</FONT></TD>" +
							  "<TD ALIGN=LEFT COLSPAN=2 WIDTH=40%><FONT SIZE=2><B>Ship To Name:</B> " + rsOrder.getString(SMTableorderheaders.sShipToName).trim() + "</FONT></TD>" +
							 "</TR><TR>" +
							  "<TD ALIGN=LEFT><FONT SIZE=2><B>Salesperson:</B> " + rsOrder.getString(SMTableorderheaders.sSalesperson) + " - " + 
																				     rsOrder.getString(SMTablesalesperson.sSalespersonFirstName) + " " + 
																				     rsOrder.getString(SMTablesalesperson.sSalespersonLastName)+ "</FONT></TD>"
//							  "<TD ALIGN=LEFT><FONT SIZE=2><B>Completed Date:</B> " + sCompletedDate + "</FONT></TD>"
																				     );
				
				//get material cost from invoices associated with this order.
				SQL = Get_Cost_From_Invoice_SQL(sOrderNum);
				if (bDebugMode){
					System.out.println("[1543512407] 'Get_Cost_From_Invoice_SQL' -  In " + this.toString() + " SQL: " + SQL);
				}
				ResultSet rsCostFromInvoice = clsDatabaseFunctions.openResultSet(SQL, conn);
				
				BigDecimal bdTotalCost = BigDecimal.ZERO;
				BigDecimal bdTotalInvoiced = BigDecimal.ZERO;
				while (rsCostFromInvoice.next()){
					bdTotalCost = bdTotalCost.add(new BigDecimal(rsCostFromInvoice.getDouble(SMTableinvoicedetails.dExtendedCost)));
					bdTotalInvoiced = bdTotalInvoiced.add(new BigDecimal(rsCostFromInvoice.getDouble(SMTableinvoicedetails.dExtendedPriceAfterDiscount)));
				}
				rsCostFromInvoice.close();
				pwOut.println("<TD ALIGN=LEFT><FONT SIZE=2><B>Total cost*:</B> " + currency.format(bdTotalCost) + "</FONT></TD>");
				pwOut.println("<TD ALIGN=LEFT><FONT SIZE=2><B>Total Invoiced**:</B> " + currency.format(bdTotalInvoiced) + "</FONT></TD>");
				pwOut.println("</TR></TABLE>");

				BigDecimal bdTotalHoursBid = BigDecimal.valueOf(rsOrder.getDouble(SMTableorderheaders.dEstimatedHour));
				rsOrder.beforeFirst();
				boolean bOddRow = true;
				BigDecimal bdTotalQtyOfHours = BigDecimal.ZERO;
				BigDecimal bdTotalBackChargeHours = BigDecimal.ZERO;
				BigDecimal bdTotalTravelHours = BigDecimal.ZERO;
				//double dOverUnder = 0;

				pwOut.println(SMUtilities.getMasterStyleSheetLink());
				//headers
				pwOut.println("<TABLE WIDTH=100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");
				pwOut.println("<TR CLASS=\""+ SMMasterStyleSheetDefinitions.TABLE_HEADING +  "\">");
				pwOut.println("<TD CLASS=\"" +SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\">ID</B></TD>");
				pwOut.println("<TD CLASS=\"" +SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\">Date</B></TD>");
				pwOut.println("<TD CLASS=\"" +SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\">Work order</B></TD>");
				pwOut.println("<TD CLASS=\"" +SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\">Posted</B></TD>");
				pwOut.println("<TD CLASS=\"" +SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\">Technician</B></TD>");
				pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED+ "\">Job description</B></TD>");
				pwOut.println("<TD CLASS=\"" +SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Qty of hours&nbsp;&nbsp;</B></TD>");
				pwOut.println("<TD CLASS=\"" +SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Travel&nbsp;&nbsp;</B></TD>");
				pwOut.println("<TD CLASS=\"" +SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Back charge hours</B></TD>"); 
				pwOut.println("</TR>");
				
				int iLineNumber = 0;
				boolean bAllowEditWorkOrders = SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.SMEditWorkOrders, 
						sUserID, 
						getServletContext(), 
						sDBID,
						sLicenseModuleLevel);
				boolean bAllowConfigureWorkOrders = SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.SMConfigureWorkOrders, 
						sUserID, 
						getServletContext(), 
						sDBID,
						sLicenseModuleLevel);
				boolean bAllowDocumentView = SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.SMViewWorkOrderDocuments, 
						sUserID, 
						conn, 
						sLicenseModuleLevel); 
				
				while (rsOrder.next()){
					iLineNumber++;
					if(bOddRow){
						pwOut.println("<TR CLASS=\""+ SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN +  "\">");
					}else{
						pwOut.println("<TR CLASS=\""+ SMMasterStyleSheetDefinitions.TABLE_ROW_ODD +  "\">");
					}
					//sCellStyle = "style=\"border: 0px solid\" bordercolor=" + sBackgroundColor;
					String sConfigureWorkOrderLink = clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", 4);
					if (bAllowConfigureWorkOrders){
						sConfigureWorkOrderLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMConfigWorkOrderEdit?"
								+ SMWorkOrderHeader.Paramlid + "=" + rsOrder.getInt(SMTableworkorders.lid) 
								+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
								+ "&" + SMConfigWorkOrderEdit.REMOVE_WORK_ORDER_ATTRIBUTE_FROM_SESSION + "=Y"
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					     		+ "\">" 
					     		+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", 4) 
					     		+ "</A>"
					     ;
					}
					pwOut.println("<TD CLASS=\"" +SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">"
							+ sConfigureWorkOrderLink
							+ "</FONT></TD>");

					pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP+ "\">" 
							+ clsDateAndTimeConversions.resultsetDateStringToString(rsOrder.getString(SMTableworkorders.datscheduleddate)) + "</FONT></TD>");
					
					//Add work order info here:
					String sWorkOrderID = Long.toString(rsOrder.getLong(SMTableworkorders.TableName + "." + SMTableworkorders.lid));
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
						sWOLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMWorkOrderEdit?"
							+ SMWorkOrderHeader.Paramlid + "=" + sWorkOrderID
							+ "&" + SMWorkOrderHeader.Paramstrimmedordernumber + "=" + rsOrder.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber).trim()
							+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
							+ "\">" + sWorkOrderID 
							+ "</A>"
						;
					}
					//Next a link to VIEW the work order documents:
					SMOption smopt = new SMOption();
					try {
						smopt.load(conn);
					} catch (Exception e1) {
						pwOut.println("<BR>Error getting SMOptions [1565634531] - " + e1.getMessage() + "<BR>");
					}
					String sWODocumentLink = "";
					if (bAllowDocumentView){
						//Try to build a link to the Google Docs folder:
						String sGDocLink = rsOrder.getString(SMTableworkorders.TableName + "." + SMTableworkorders.sgdoclink);
						if (sGDocLink == null){
							sGDocLink = "";
						}
						if (sGDocLink.compareToIgnoreCase("") != 0){
							sWODocumentLink = "<FONT SIZE=2><A HREF=\"" + sGDocLink 
							+ "\">View&nbsp;folder</A>&nbsp;&nbsp;</FONT>\n";
						}
					}
					pwOut.println("<TD CLASS=\"" +SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + sWOLink + "&nbsp;" + sWODocumentLink + "</FONT></TD>");
					
					//Next a link to VIEW the work order:
					String sViewWOLink = "(Not posted)";
					if (rsOrder.getInt(SMTableworkorders.TableName + "." + SMTableworkorders.iposted) == 1){
						sViewWOLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMWorkOrderAction?"
							+ SMWorkOrderEdit.COMMAND_FLAG + "=" + SMWorkOrderEdit.PRINTRECEIPTCOMMAND_VALUE
							+ "&" + SMWorkOrderHeader.Paramlid + "=" + sWorkOrderID
							+ "&" + SMWorkOrderHeader.Paramstrimmedordernumber + "=" + rsOrder.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber).trim()
							+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
							+ "\">" + "View" 
							+ "</A>"
						;
					}
					pwOut.println("<TD CLASS=\"" +SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + sViewWOLink + "</FONT></TD>");
					
					
					
					String sMechFullName = rsOrder.getString(SMTablemechanics.TableName + "." + SMTablemechanics.sMechFullName);
					if (sMechFullName == null){
						sMechFullName =  rsOrder.getString(SMTableworkorders.TableName + "." + SMTableworkorders.smechanicname).trim() 
						+ " (REMOVED)";
					}
					pwOut.println("<TD CLASS=\"" +SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + sMechFullName + "</FONT></TD>");
					
					//The 'work description' will include the job cost 'work description' PLUS mechanic's comments, additional work needed, 
					//and detail sheet text from the work order:
					SMWorkOrderHeader wo = new SMWorkOrderHeader();
					String sWorkDescription;
					sWorkDescription = wo.createWorkDescription(
						rsOrder.getString(SMTableworkorders.TableName + "." + SMTableworkorders.mworkdescription), 
						rsOrder.getString(SMTableworkorders.TableName + "." + SMTableworkorders.mcomments), 
						rsOrder.getString(SMTableworkorders.TableName + "." + SMTableworkorders.madditionalworkcomments),
						rsOrder.getString(SMTableworkorders.TableName + "." + SMTableworkorders.mdetailsheettext).replaceAll("[^\\x00-\\x7F]", " "),
							Double.toString(rsOrder.getDouble(SMTableworkorders.TableName + "." + SMTableworkorders.dPrePostingWODiscountAmount)));
					pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + sWorkDescription + "</FONT></TD>");
					pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableworkorders.bdqtyofhoursScale, rsOrder.getBigDecimal(SMTableworkorders.bdqtyofhours))  
						+ "</FONT></TD>");
					pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableworkorders.bdtravelhoursScale, rsOrder.getBigDecimal(SMTableworkorders.bdtravelhours))  
						+ "</FONT></TD>");
					pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableworkorders.bdbackchargehoursScale, rsOrder.getBigDecimal(SMTableworkorders.bdbackchargehours))  
						+ "</FONT></TD>");
					//TODO Add in Discount Info
					pwOut.println("</TR>");

					//accumulate totals
					bdTotalQtyOfHours = bdTotalQtyOfHours.add(rsOrder.getBigDecimal(SMTableworkorders.bdqtyofhours));
					bdTotalTravelHours = bdTotalTravelHours.add(rsOrder.getBigDecimal(SMTableworkorders.bdtravelhours));
					bdTotalBackChargeHours = bdTotalBackChargeHours.add(rsOrder.getBigDecimal(SMTableworkorders.bdbackchargehours));
					bOddRow = ! bOddRow;
				}
				pwOut.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\"><TD COLSPAN=6 CLASS=\"" +SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP  + "\">Subtotals:</TD>" 
					+ "<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableworkorders.bdqtyofhoursScale, bdTotalQtyOfHours) + "</TD>" 
					+ "<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableworkorders.bdtravelhoursScale, bdTotalTravelHours) + "</TD>"
					+ "<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableworkorders.bdbackchargehoursScale, bdTotalBackChargeHours) + "</TD>"
					+  "</TR>");
				pwOut.println("</TABLE>");
				pwOut.println("<TABLE WIDTH=100% CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");
					pwOut.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
					pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" WIDTH=80%>TOTAL HOURS BID:</TD>");
					pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" WIDTH=20%>" +  clsManageBigDecimals.BigDecimalToScaledFormattedString(2, bdTotalHoursBid) + "</TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
					pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" WIDTH=80%>TOTAL ACTUAL HOURS:</TD>");
					pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" WIDTH=20%>" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableworkorders.bdqtyofhoursScale, (bdTotalQtyOfHours.add(bdTotalTravelHours)).add(bdTotalBackChargeHours)) + "</TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
					pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" WIDTH=80%>TOTAL ACTUAL HOURS LESS BACK CHARGE:</TD>");
						
					pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" WIDTH=20%>" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableworkorders.bdqtyofhoursScale, (bdTotalQtyOfHours.add(bdTotalTravelHours))) + "</TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
					pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" WIDTH=80%>OVER / UNDER:</TD>");
					pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" WIDTH=20%>" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableworkorders.bdqtyofhoursScale, (bdTotalQtyOfHours.add(bdTotalTravelHours)).subtract(bdTotalHoursBid)) + "</TD>");
					pwOut.println("</TR>");
				pwOut.println("</TABLE>");
			}else{
				//no job cost information for this job.
				pwOut.println("<TABLE BORDER=1 WIDTH=100%><TR><TD ALIGN=CENTER><B>No job cost information found for this job.</B></TD></TR></TABLE>");  
			}
			rsOrder.close();
		}catch(SQLException e){
			pwOut.println("Error opening details query: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	private String Get_Cost_From_Invoice_SQL(String sOrderNumber){

		String SQL = "SELECT" + 
						" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber + "," + 
						" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sOrderNumber + "," + 
						" " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dExtendedCost + "," + 
						" " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dExtendedPriceAfterDiscount +  
					 " FROM " +
			 			" " + SMTableinvoiceheaders.TableName + "," +
			 			" " + SMTableinvoicedetails.TableName +
			 		 " WHERE" +
			 		 	" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber + " =" +
			 		 	" " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber +
			 		 	" AND" +
			 		 	" " + SMTableinvoiceheaders.TableName + "." 
			 		 	+ SMTableinvoiceheaders.strimmedordernumber + " = '" + sOrderNumber.trim() + "'";
	
		//System.out.println("SQL:");
		//System.out.println(SQL);
		return SQL;
	}
}
