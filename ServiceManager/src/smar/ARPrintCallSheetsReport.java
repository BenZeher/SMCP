package smar;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTablearcustomerstatistics;
import SMDataDefinition.SMTableartransactions;
import SMDataDefinition.SMTablecallsheets;
import SMDataDefinition.SMTablesalesperson;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsStringFunctions;

public class ARPrintCallSheetsReport extends java.lang.Object{

	private String m_sErrorMessage;
	private boolean bDebugMode = false;

	public ARPrintCallSheetsReport(
	){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sCallSheetID,
			String sStartingCustomer,
			String sEndingCustomer,
			String sCollector,
			String sResponsibility,
			String sStartingLastContactDate,
			String sEndingLastContactDate,
			String sStartingNextContactDate,
			String sEndingNextContactDate,
			String sPassedInOrderNumber,
			boolean bPrintWithNotes,
			boolean bPrintOnlyAlerts,
			boolean bPrintZeroBalanceCustomers,
			boolean bPrintWithResponsibilityOnly,
			String sDBID,
			String sUserID,
			PrintWriter out,
			boolean bOutputToCSV,
			ServletContext context,
			String sLicenseModuleLevel
	){

		String SQL = "SELECT"
			+ " " + SMTablecallsheets.TableName + "." + SMTablecallsheets.datLastContact
			+ ", " + SMTablecallsheets.TableName + "." + SMTablecallsheets.datNextContact
			+ ", " + SMTablecallsheets.TableName + "." + SMTablecallsheets.mNotes
			+ ", " + SMTablecallsheets.TableName + "." + SMTablecallsheets.sAccountTerms
			+ ", " + SMTablecallsheets.TableName + "." + SMTablecallsheets.sAcct
			+ ", " + SMTablecallsheets.TableName + "." + SMTablecallsheets.sAlertInits
			+ ", " + SMTablecallsheets.TableName + "." + SMTablecallsheets.sCallSheetName
			+ ", " + SMTablecallsheets.TableName + "." + SMTablecallsheets.sCollector
			+ ", " + SMTablecallsheets.TableName + "." + SMTablecallsheets.sID
			+ ", " + SMTablecallsheets.TableName + "." + SMTablecallsheets.sJobPhone
			+ ", " + SMTablecallsheets.TableName + "." + SMTablecallsheets.sOrderNumber
			+ ", " + SMTablecallsheets.TableName + "." + SMTablecallsheets.sPhone
			+ ", CONCAT(COALESCE(" + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonFirstName + ",'')"
			+ ", ' ' ,"
			+ " COALESCE(" + SMTablesalesperson.TableName +"."+SMTablesalesperson.sSalespersonLastName +", ''))"
			+ " AS sResponsibility"
			+ ", " + SMTablearcustomer.TableName + "." + SMTablearcustomer.dCreditLimit
			+ ", " + SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerName
			+ ", " + SMTablearcustomer.TableName + "." + SMTablearcustomer.iOnHold
			+ ", " + SMTablearcustomerstatistics.TableName + "." + SMTablearcustomerstatistics.sCurrentBalance

			+ " FROM (((" + SMTablecallsheets.TableName + " LEFT JOIN " + SMTablearcustomer.TableName
			+ " ON " + SMTablecallsheets.TableName + "." + SMTablecallsheets.sAcct + " = " 
			+ SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerNumber + ")"
			+ " LEFT JOIN " + SMTablearcustomerstatistics.TableName + " ON " + SMTablecallsheets.TableName + "." 
			+ SMTablecallsheets.sAcct + " = " + SMTablearcustomerstatistics.TableName + "." 
			+ SMTablearcustomerstatistics.sCustomerNumber+ ")"
			+ "LEFT JOIN " + SMTablesalesperson.TableName + " ON " + SMTablecallsheets.TableName + "."
			+ SMTablecallsheets.sResponsibility + " = " + SMTablesalesperson.TableName +"."
			+ SMTablesalesperson.sSalespersonCode +")"
			+ " WHERE ("
			;
		
			//If the user selected a Call Sheet ID, just get that record:
		//Print call sheets for the selected call sheet ID:
			if (sCallSheetID.compareToIgnoreCase("") != 0){
				SQL += "(" + SMTablecallsheets.TableName
				+ "." + SMTablecallsheets.sID + " = " + sCallSheetID + ")";
			}else{
				//Last contact Dates
				SQL += "(" + SMTablecallsheets.TableName + "." + SMTablecallsheets.datLastContact
				+ " >= '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(sStartingLastContactDate) + " 00:00:00')"
				+ " AND (" + SMTablecallsheets.TableName + "." + SMTablecallsheets.datLastContact
				+ " <= '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(sEndingLastContactDate) + " 23:59:59')"
	
				//Next contact dates
				+ " AND (" + SMTablecallsheets.TableName + "." + SMTablecallsheets.datNextContact
				+ " >= '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(sStartingNextContactDate) + " 00:00:00')"
				+ " AND (" + SMTablecallsheets.TableName + "." + SMTablecallsheets.datNextContact
				+ " <= '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(sEndingNextContactDate) + " 23:59:59')"
	
				//Customers:
				+ " AND (" + SMTablecallsheets.TableName + "." + SMTablecallsheets.sAcct + " >= '"
				+ sStartingCustomer + "')"
				+ " AND (" + SMTablecallsheets.TableName + "." + SMTablecallsheets.sAcct + " <= '"
				+ sEndingCustomer + "')";
	
				//Collectors:
				if (sCollector.compareToIgnoreCase("") != 0){
					SQL += " AND (" + SMTablecallsheets.TableName + "." + SMTablecallsheets.sCollector + " = '"
					+ sCollector + "')";
				}

				//Responsibility:
				if (sResponsibility.compareToIgnoreCase("") != 0){
					SQL += " AND (" + SMTablecallsheets.TableName + "." + SMTablecallsheets.sResponsibility + " = '"
					+ sResponsibility + "')";
				}

				//Print customers with zero balance?
				if (!bPrintZeroBalanceCustomers){
					SQL += " AND (" + SMTablearcustomerstatistics.TableName 
					+ "." + SMTablearcustomerstatistics.sCurrentBalance + " != 0.00)"; 
				}
		
				//Print only call sheets with responsibility assigned:
				if (bPrintWithResponsibilityOnly){
					SQL += " AND (" + SMTablecallsheets.TableName 
					+ "." + SMTablecallsheets.sResponsibility + " != '')"; 
				}
				
				//Print only call sheets with an alert on them:
				if (bPrintOnlyAlerts){
					SQL += " AND (" + SMTablecallsheets.TableName 
					+ "." + SMTablecallsheets.sAlertInits + " != '')"; 
				}
		
				//Print call sheets for the selected order number:
				if (sPassedInOrderNumber.compareToIgnoreCase("") != 0){
					SQL += " AND (" + SMTablecallsheets.TableName
					+ "." + SMTablecallsheets.sOrderNumber + " = '" + sPassedInOrderNumber + "')";
				}

			}
			SQL += ")"	//Complete the 'where' clause
			+ " ORDER BY "
			+ SMTablecallsheets.TableName + "." + SMTablecallsheets.sAcct
			+ ", " + SMTablecallsheets.TableName + "." + SMTablecallsheets.datNextContact
			;

		//Check permissions for viewing items:
		boolean bEditCallSheetPermitted = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.AREditCallSheets,
				sUserID,
				conn,
				sLicenseModuleLevel);

		boolean bAllowCustomerView = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.ARDisplayCustomerInformation,
				sUserID,
				conn,
				sLicenseModuleLevel);
		
		boolean bAllowCustomerEdit = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.AREditCustomers,
				sUserID,
				conn,
				sLicenseModuleLevel);
		
		boolean bAllowOrderView = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMViewOrderInformation,
				sUserID,
				conn,
				sLicenseModuleLevel);
		
		boolean bAllowActivityView = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.ARCustomerActivity,
				sUserID,
				conn,
				sLicenseModuleLevel);
		
		if (bOutputToCSV){
		/*
			String sHeading = "\"LOCATION\""
				+ ",\"PO\""
				+ ",\"PODATE\""
				+ ",\"QTY\""
				+ ",\"ITEM\""
				+ ",\"DESCRIPTION\""
				+ ",\"ARRIVALDATE\""
				+ ",\"REFERENCE\""
				+ ",\"VENDOR\""
				+ ",\"COMMENT1\""
				;
			out.println(sHeading);
			try{
				ResultSet rs = SMUtilities.openResultSet(SQL, conn);
				while(rs.next()){
					String sComment1 = rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sComment1);
					if (sComment1 == null){
						sComment1 = "";
					}
					sComment1 = sComment1.trim().replace("\"", "\"\"");

					//Print each line:
					String sLine =

						//Location:
						"\""
						+ rs.getString(SMTableicpolines.TableName + "." 
								+ SMTableicpolines.slocation).replace("\"", "\"\"")
								+ "\""
								+ ","

								//PO
								+ Long.toString(rs.getLong(SMTableicpoheaders.TableName + "." 
										+ SMTableicpoheaders.lid)).replace("\"", "\"\"")
										+ ","

										//PO date
										+ SMUtilities.resultsetDateStringToString(rs.getString(SMTableicpoheaders.TableName + "." 
												+ SMTableicpoheaders.datpodate))
												+ ","

												//Qty
												+ SMUtilities.BigDecimalToFormattedString(
														"########0.0000", rs.getBigDecimal(
																SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyordered))
																+ ","	

																//Item:
																+ "\""
																+ rs.getString(SMTableicpolines.TableName + "." 
																		+ SMTableicpolines.sitemnumber).replace("\"", "\"\"")
																		+ "\""
																		+ ","

																		//Description:
																		+ "\""
																		+ rs.getString(SMTableicpolines.TableName + "." 
																				+ SMTableicpolines.sitemdescription).replace("\"", "\"\"")
																				+ "\""
																				+ ","

																				//Arrival date
																				+ SMUtilities.resultsetDateStringToString(rs.getString(SMTableicpoheaders.TableName + "." 
																						+ SMTableicpoheaders.datexpecteddate))
																						+ ","

																						//Reference:
																						+ "\""
																						+ rs.getString(SMTableicpoheaders.TableName + "." 
																								+ SMTableicpoheaders.sreference).replace("\"", "\"\"")
																								+ "\""
																								+ ","

																								//Vendor
																								+ "\""
																								+ rs.getString(SMTableicpoheaders.TableName + "." 
																										+ SMTableicpoheaders.svendorname).replace("\"", "\"\"")
																										+ "\""
																										+ ","

																										//Comment1
																										+ "\""
																										+ sComment1
																										+ "\""

																										;
					out.println(sLine);
				}
				rs.close();
				out.flush();
				out.close();
			}catch (SQLException e){
				m_sErrorMessage = "Error reading resultset - " + e.getMessage();
				return false;
			}
		*/
		}else{
			//System.out.println("In " + this.toString() + " SQL = " + SQL);
			
			out.println(SMUtilities.getMasterStyleSheetLink());
			
			try{
				if (bDebugMode){
					System.out.println("In " + this.toString() + " SQL: " + SQL);
				}
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				out.println("<TABLE WIDTH= 100%  class=\"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER  + "\" >");
				while(rs.next()){

					//Here we get Links
					
					//Add link here to edit call sheet:
					String sID = Long.toString(rs.getLong(SMTablecallsheets.sID));
					String sIDLink = sID;
					if (bEditCallSheetPermitted){
						sIDLink = 
							"<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
							+ "smar.AREditCallSheetsEdit?" + ARCallSheet.ParamsID + "=" 
							+ sIDLink + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">&nbsp;" + sIDLink + "&nbsp;</A>"
							;
					}
					
					//Customer Code Link
					String sCustomerCode = rs.getString(SMTablecallsheets.sAcct);
					String sCustomerCodeLink = sCustomerCode;
					if (bAllowCustomerView){
						sCustomerCodeLink = 
							"<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
							+ "smar.ARDisplayCustomerInformation?CustomerNumber=" 
							+ sCustomerCode + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sCustomerCode + "</A>"
							;
					}
					if (bAllowCustomerEdit){
						sCustomerCodeLink += 
							"&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
							+ "smar.AREditCustomersEdit?" + ARCustomer.ParamsCustomerNumber + "=" 
							+ sCustomerCode 
							+ "&SubmitEdit=Y"
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + "Edit" + "</A>"
							;
					}
					
					//On hold?
					String sOnHold = "N";
					if (rs.getLong(SMTablearcustomer.iOnHold) != 0){
						sOnHold = "Y";
					}
					
					//Customer Activity for This order
					String sActivityLink = sCustomerCode;
					String sOrderNumber = rs.getString(SMTablecallsheets.sOrderNumber);
					if (bAllowActivityView){
						sActivityLink = 
							"<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
							+ "smar.ARActivityDisplay"
							+ "?CustomerNumber=" + clsServletUtilities.URLEncode(sCustomerCode)
							+ "&" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber
							+ "&StartingDate=01/01/1900"
							+ "&EndingDate=" + clsDateAndTimeConversions.now("MM/dd/yyyy")
							+ "&OpenTransactionsOnly=true"
							+ "&OrderBy=" + SMTableartransactions.datdocdate
						;
					    for (int i=0;i <= 10;i++){
					    	sActivityLink += "&" + ARDocumentTypes.Get_Document_Type_Label(i)+ "=Y";
					    }
						sActivityLink += "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + "For <B><I>THIS</I></B> order" 
						+ "</A>";
					}else{
						sActivityLink = "N/A";
					}
					
					//Activity Link for All orders
					String sActivityLinkAll = sCustomerCode;
					if (bAllowActivityView){
						sActivityLinkAll = 
							"<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
							+ "smar.ARActivityDisplay"
							+ "?CustomerNumber=" + clsServletUtilities.URLEncode(sCustomerCode)
							+ "&StartingDate=01/01/1900"
							+ "&EndingDate=" + clsDateAndTimeConversions.now("MM/dd/yyyy")
							+ "&OpenTransactionsOnly=true"
							+ "&OrderBy=" + SMTableartransactions.datdocdate
						;
					    for (int i=0;i <= 10;i++){
					    	sActivityLinkAll += "&" + ARDocumentTypes.Get_Document_Type_Label(i)+ "=Y";
					    }
					    sActivityLinkAll += "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + "For <B><I>ALL</I></B> orders" + "</A>";
					}else{
						sActivityLinkAll = "N/A";
					}
					
					String sOrderNumberLink = sOrderNumber;
					if (bAllowOrderView){
						sOrderNumberLink = 
							"<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
							+ "smcontrolpanel.SMDisplayOrderInformation"
							+ "?" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber 
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sOrderNumber + "</A>"
							;
					}

					//Print the first line heading:
					out.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\" >");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD +"\"  >ID#</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD +"\"  >Collector</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD +"\"  >Alert from</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD +"\"  >Account</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD +"\"  >On hold?</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD +"\"  >Responsibility</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD +"\"  >Customer name</TD>");
					out.println("<TD COLSPAN = \"3\"  CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD +"\"  >Call sheet name</TD>");
					out.println("</TR>");

					//Print the first line:
					out.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\" >");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +"\"  >" + sIDLink +"</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +"\"  >" + rs.getString(SMTablecallsheets.sCollector) + "</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +"\"  >" + clsStringFunctions.checkStringForNull( rs.getString(SMTablecallsheets.sAlertInits)) + "</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +"\"  >" + sCustomerCodeLink +"</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +"\"  >" + sOnHold + "</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +"\"  >" + rs.getString(SMTablecallsheets.sResponsibility) + "</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +"\"  >" + rs.getString(SMTablearcustomer.sCustomerName) + "</TD>");
					out.println("<TD COLSPAN = \"3\" CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +"\"  >" + rs.getString(SMTablecallsheets.sCallSheetName) + "</TD>");
					out.println("</TR>");

					
					//Print the second line:
					out.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW + "\" >");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD +"\"  >View Activity:</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD +"\"  >View Activity:</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD +"\"  >Order</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD +"\"  >Terms</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD +"\"  >Last contact</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD +"\"  >Next contact</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD +"\"  >Phone</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD +"\"  >Job phone</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD +"\"  >Credit limit</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD +"\"  >Balance due</TD>");
					out.println("</TR>");

					out.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW + "\" >");
					out.println("<TD NOWRAP CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +"\"  >" + sActivityLink +"</TD>");
					out.println("<TD NOWRAP CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +"\"  >" + sActivityLinkAll +"</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +"\"  >" + sOrderNumberLink +"</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +"\"  >" + rs.getString(SMTablecallsheets.sAccountTerms) + "</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +"\"  >" + clsDateAndTimeConversions.utilDateToString(rs.getDate(SMTablecallsheets.datLastContact),"MM/dd/yyy") + "</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +"\"  >" + clsDateAndTimeConversions.utilDateToString(rs.getDate(SMTablecallsheets.datNextContact),"MM/dd/yyy") + "</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +"\"  >" + rs.getString(SMTablecallsheets.sPhone) + "</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +"\"  >" + rs.getString(SMTablecallsheets.sJobPhone) + "</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +"\"  >" + clsManageBigDecimals.BigDecimalToScaledFormattedString(2, rs.getBigDecimal(SMTablearcustomer.dCreditLimit)) + "</TD>");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +"\"  >" + clsManageBigDecimals.BigDecimalToScaledFormattedString(2, rs.getBigDecimal(SMTablearcustomerstatistics.sCurrentBalance)) + "</TD>");
					out.println("</TR>");
					
					
					//Print the notes:
					if (bPrintWithNotes){
						out.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_NOTES + "\" >");
						out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP +"\"  ><B>Notes:</B></TD>");
						out.println("<TD COLSPAN = \"9\"  CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +"\"  >" 	+ rs.getString(SMTablecallsheets.mNotes).replace("\n", "<BR>") + "</TD>");
						out.println("</TR>");
					}
					//Finish out call sheet:
					out.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\" >");
					out.println("<TD COLSPAN = \"10\"  CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK +"\"  >&nbsp;</TD>");
					out.println("</TR>");
					
				}
				rs.close();
			}catch (SQLException e){
				m_sErrorMessage = "Error reading resultset - " + e.getMessage();
				return false;
			}

			out.println("</TABLE>");
		}
		return true;
	}

	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
