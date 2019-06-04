package smar;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTablearcustomerstatistics;
import SMDataDefinition.SMTableartransactions;
import SMDataDefinition.SMTablecallsheets;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;

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
			+ ", CONCAT(COALESCE(" + SMTableusers.TableName + "." + SMTableusers.sUserFirstName + ",'')"
			+ ", ' ' ,"
			+ " COALESCE(" + SMTableusers.TableName +"."+SMTableusers.sUserLastName +", ''))"
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
			+ "LEFT JOIN " + SMTableusers.TableName + " ON " + SMTablecallsheets.TableName + "."
			+ SMTablecallsheets.sResponsibility + " = " + SMTableusers.TableName +"."
			+ SMTableusers.lid +")"
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
			printTableDef(out);
			
			try{
				if (bDebugMode){
					System.out.println("In " + this.toString() + " SQL: " + SQL);
				}
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				while(rs.next()){
					out.println("<TABLE class=\"main\" >");

					//Print the first line heading:
					out.println("<TR class=\"dshaded\" >");
					out.println("<th class=\"th\" ALIGN=RIGHT >");
					out.println("ID#");
					out.println("</th>");
					out.println("<th class=\"th\" ALIGN=LEFT >");
					out.println("Collector");
					out.println("</th>");
					out.println("<th class=\"th\" ALIGN=LEFT >");
					out.println("Alert from");
					out.println("</th>");
					out.println("<th class=\"th\" ALIGN=LEFT >");
					out.println("Account");
					out.println("</th>");
					out.println("<th class=\"th\" ALIGN=LEFT >");
					out.println("On hold?");
					out.println("</th>");
					out.println("<th class=\"th\" ALIGN=LEFT >");
					out.println("Responsibility");
					out.println("</th>");
					out.println("<th class=\"th\" ALIGN=LEFT >");
					out.println("Customer name");
					out.println("</th>");
					out.println("<th class=\"th\" ALIGN=LEFT >");
					out.println("Call sheet name");
					out.println("</th>");

					//Print the first line:
					out.println("<TR class=\"dshaded\" >");
					out.println("<td class=\"r\" >");
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
					out.println(sIDLink);
					out.println("</td>");
					out.println("<td class=\"l\" >");
					out.println(rs.getString(SMTablecallsheets.sCollector));
					out.println("</td>");
					out.println("<td class=\"l\" >");
					String sAlertInits = rs.getString(SMTablecallsheets.sAlertInits);
					if (sAlertInits == null){sAlertInits = "";}
					out.println(sAlertInits);
					out.println("</td>");
					out.println("<td class=\"l\" >");
					
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
					out.println(sCustomerCodeLink);
					out.println("</td>");
					String sOnHold = "N";
					if (rs.getLong(SMTablearcustomer.iOnHold) != 0){
						sOnHold = "Y";
					}
					out.println("<td class=\"c\" >");
					out.println(sOnHold);
					out.println("</td>");
					out.println("<td class=\"l\" >");
					out.println(rs.getString(SMTablecallsheets.sResponsibility));
					out.println("</td>");
					out.println("<td class=\"l\" >");
					out.println(rs.getString(SMTablearcustomer.sCustomerName));
					out.println("</td>");
					out.println("<td class=\"l\" >");
					out.println(rs.getString(SMTablecallsheets.sCallSheetName));
					out.println("</td>");

					out.println("</TR>");
					out.println("</table>");
					
					//Print the second line:
					out.println("<TABLE class=\"main\" >");
					out.println("<TR class=\"dwhite\" >");
					out.println("<th class=\"th\" ALIGN=LEFT>");
					out.println("View Activity:");
					out.println("</th>");
					out.println("<th class=\"th\" ALIGN=LEFT>");
					out.println("View Activity:");
					out.println("</th>");
					out.println("<th class=\"th\" ALIGN=LEFT >");
					out.println("Order");
					out.println("</th>");
					out.println("<th class=\"th\" ALIGN=LEFT >");
					out.println("Terms");
					out.println("</th>");
					out.println("<th class=\"th\" ALIGN=LEFT >");
					out.println("Last contact");
					out.println("</th>");
					out.println("<th class=\"th\" ALIGN=LEFT >");
					out.println("Next contact");
					out.println("</th>");
					out.println("<th class=\"th\" ALIGN=LEFT >");
					out.println("Phone");
					out.println("</th>");
					out.println("<th class=\"th\" ALIGN=LEFT >");
					out.println("Job phone");
					out.println("</th>");
					out.println("<th class=\"th\" ALIGN=RIGHT >");
					out.println("Credit limit");
					out.println("</th>");
					out.println("<th class=\"th\" ALIGN=RIGHT >");
					out.println("Balance due");
					out.println("</th>");

					out.println("<TR class=\"dwhite\" >");
					
					out.println("<td class=\"l\" >");
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
					out.println(sActivityLink);
					out.println("</td>");
					
					out.println("<td class=\"l\" >");
					sActivityLink = sCustomerCode;
					if (bAllowActivityView){
						sActivityLink = 
							"<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
							+ "smar.ARActivityDisplay"
							+ "?CustomerNumber=" + clsServletUtilities.URLEncode(sCustomerCode)
							+ "&StartingDate=01/01/1900"
							+ "&EndingDate=" + clsDateAndTimeConversions.now("MM/dd/yyyy")
							+ "&OpenTransactionsOnly=true"
							+ "&OrderBy=" + SMTableartransactions.datdocdate
						;
					    for (int i=0;i <= 10;i++){
					    	sActivityLink += "&" + ARDocumentTypes.Get_Document_Type_Label(i)+ "=Y";
					    }
						sActivityLink += "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + "For <B><I>ALL</I></B> orders" + "</A>";
					}else{
						sActivityLink = "N/A";
					}
					out.println(sActivityLink);
					out.println("</td>");
					
					out.println("<td class=\"l\" >");
					String sOrderNumberLink = sOrderNumber;
					if (bAllowOrderView){
						sOrderNumberLink = 
							"<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
							+ "smcontrolpanel.SMDisplayOrderInformation"
							+ "?" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber 
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sOrderNumber + "</A>"
							;
					}
					out.println(sOrderNumberLink);
					out.println("</td>");
					out.println("<td class=\"l\" >");
					out.println(rs.getString(SMTablecallsheets.sAccountTerms));
					out.println("</td>");
					out.println("<td class=\"l\" >");
					out.println(clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTablecallsheets.datLastContact)));
					out.println("</td>");
					out.println("<td class=\"l\" >");
					out.println(clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTablecallsheets.datNextContact)));
					out.println("</td>");
					out.println("<td class=\"l\" >");
					out.println(rs.getString(SMTablecallsheets.sPhone));
					out.println("</td>");
					out.println("<td class=\"l\" >");
					out.println(rs.getString(SMTablecallsheets.sJobPhone));
					out.println("</td>");
					out.println("<td class=\"r\" >");
					out.println(clsManageBigDecimals.BigDecimalToScaledFormattedString(
						2, rs.getBigDecimal(SMTablearcustomer.dCreditLimit)));
					out.println("</td>");
					out.println("<td class=\"r\" >");
					out.println(clsManageBigDecimals.BigDecimalToScaledFormattedString(
						2, rs.getBigDecimal(SMTablearcustomerstatistics.sCurrentBalance)));
					out.println("</td>");
					
					out.println("</table>");
					
					//Print the notes:
					if (bPrintWithNotes){
					out.println("<U><B>Notes:</B></U>");
						out.println("<span style = \"font-size:small; \" >" 
							+ rs.getString(SMTablecallsheets.mNotes).replace("\n", "<BR>")
							+ "</span>"
						);
					}
					//Finish out call sheet:
					out.println("<table class = \"main\">");
					out.println("<tr>");
					out.println("<td>");
					out.println("<hr>");
					out.println("</td>");
					out.println("</tr>");
					out.println("</table>");
					
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

	private void printTableDef(
			PrintWriter out
	){
		String s = "";
		String sBorderSize = "0";
		String sFontSize = "small";
		s += "<style type=\"text/css\">\n";
		
		//TJR - 5/13/2011 - I left all these comments in to use as samples here or elsewhere:
		//Set hyperlink style:
		//s += "a {font-family : Arial; Font-size : 12px; text-decoration : none}\n";
		
		//s += "amenu {font-family : Arial; text-decoration : none; font-weight: 900}\n";
		//s += "amenu:link {color : white}\n";
		//s += "amenu:visited {color : #99FFFF}\n";
		//s += "amenu:active {color : #99FFFF}\n";
		//s += "amenu:hover {color : white}\n";
		
		//s += "a {font-family : Arial; text-decoration : none; font-weight: 900}\n";
		//s += "a:link {color : #99FFFF}\n";
		//s += "a:visited {color : #99FFFF}\n";
		//s += "a:active {color : #99FFFF}\n";
		//s += "a:hover {color : white}\n";
		
		//Layout table:
		s +=
			"table.main {"
			+ "border-width: " + sBorderSize + "px; "
			+ "border-spacing: 0px; "
			//+ "border-style: outset; "
			+ "border-style: none; "
			+ "border-color: white; "
			+ "border-collapse: collapse; "
			+ "width: 100%; "
			+ "font-size: " + sFontSize + "; "
			+ "font-family : Arial; "
			+ "color: black; "
			+ "background-color: white; "
			+ "}"
			+ "\n"
			;

		s +=
			"table.main th {"
			//+ "border-width: " + sBorderSize + "px; "
			+ "padding: 2px; "
			//+ "border-style: inset; "
			+ "border-style: none; "
			//+ "border-color: white; "
			//+ "background-color: white; "
			+ "color: black; "
			+ "font-family : Arial; "
			+ "vertical-align: text-middle; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;

		s +=
			"tr.dwhite td {"
			+ "background-color: #FFFFFF; "
			+"}"
			;
		s +=
			"tr.dshaded td {"
			+ "background-color: #DCDCDC; "
			+ "}"
			+ "\n"
			;
		s +=
			"tr.dwhite th {"
			+ "background-color: #FFFFFF; "
			+"}"
			;
		s +=
			"tr.dshaded th {"
			+ "background-color: #DCDCDC; "
			+ "}"
			+ "\n"
			;
		//This is the def for a left-aligned TD:
		s +=
			"td.l {"
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			//+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "text-align: left; "
			//+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;

		//This is the default TD def - it is right aligned
		s +=
			"td.r {"
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			//+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "text-align: right; "
			//+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		
		//This is a centered cell
		s +=
			"td.c {"
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			//+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "text-align: center; "
			//+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		
		s += "</style>"
			+ "\n"
			;

		out.println(s);
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
