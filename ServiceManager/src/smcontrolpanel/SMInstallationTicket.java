package smcontrolpanel;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsStringFunctions;

public class SMInstallationTicket extends java.lang.Object{

	private String m_sErrorMessage;
	private boolean bDebugMode = false;
	public SMInstallationTicket(
	){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sStartingOrderNumber,
			String sEndingOrderNumber,
			String sUserName,
			int iNumberOfCopies,
			PrintWriter out
	){

		if (sStartingOrderNumber == null){
			sStartingOrderNumber = "";
		}
		if (sEndingOrderNumber == null){
			sEndingOrderNumber = "";
		}
		if (
				(sStartingOrderNumber.compareToIgnoreCase("") == 0)
				&& sEndingOrderNumber.compareToIgnoreCase("") == 0
		){
			m_sErrorMessage = "You must enter an order number.";
			return false;
		}

		for (int i = 0; i < iNumberOfCopies; i++){
			try {
				if (i > 0){
					out.println("<P CLASS=\"breakhere\">");
				}
				printWorkOrders(
						conn,
						sStartingOrderNumber,
						sEndingOrderNumber,
						sUserName,
						//iNumberOfCopies,
						out
				);
			} catch (SQLException e) {
				m_sErrorMessage = e.getMessage();
				return false;
			}
		}
		return true;
	}
	private void printWorkOrders(
			Connection conn,
			String sStartingOrderNumber,
			String sEndingOrderNumber,
			String sUserName,
			//int iNumberOfCopies,
			PrintWriter out
	) throws SQLException{
		
		String SQL = "SELECT"
			+ " * FROM"
			+ " " + SMTableorderheaders.TableName
			+ " LEFT JOIN " + SMTableorderdetails.TableName + " ON " 
			+ SMTableorderheaders.TableName + "." + SMTableorderheaders.dOrderUniqueifier
			+ " = " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dUniqueOrderID
			
			//
			+ " LEFT JOIN " + SMTableicitems.TableName + " ON " 
			+ SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber 
			+ " = " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber

			+ " LEFT JOIN " + SMTablearcustomer.TableName + " ON " 
			+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sCustomerCode
			+ " = " + SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerNumber

			//
			+ " WHERE ("
			;
		if (sStartingOrderNumber.compareToIgnoreCase("") == 0){
			SQL += "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber
			+ " = " + clsStringFunctions.PadLeft(sEndingOrderNumber, " ", 8) + ")";
		}
		if (sEndingOrderNumber.compareToIgnoreCase("") == 0){
			SQL += "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber 
			+ " = " + clsStringFunctions.PadLeft(sStartingOrderNumber, " ", 8) + ")";
		}
		if (
				(sStartingOrderNumber.compareToIgnoreCase("") != 0)
				&& (sEndingOrderNumber.compareToIgnoreCase("") != 0)

		){
			SQL += "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber 
			+ " >= " + clsStringFunctions.PadLeft(sStartingOrderNumber, " ", 8) + ")"
			+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber 
			+ " <= " + clsStringFunctions.PadLeft(sEndingOrderNumber, " ", 8) + ")";
		}

		//SQL += " AND (" + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered
		//+ " > 0.00)";

		SQL += ")"	//Complete the 'where' clause
			+ " ORDER BY"
			+ " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber
			+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.iLineNumber
			;

		if (bDebugMode){
			System.out.println("In " + this.toString() + " - Main SQL = " + SQL);
		}
		
		long lLastOrderID = 0;
		long lPrintedLineNumber = 0;
		String sTicketComments = "";
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				long lCurrentOrderID = rs.getLong(SMTableorderheaders.TableName 
						+ "." + SMTableorderheaders.dOrderUniqueifier);

				//If there was a previous line, and the Order ID has changed, print the ticket footer:
				if ((lLastOrderID != lCurrentOrderID) && (lLastOrderID != 0)){
					printOrderFooter(sTicketComments, out);
					//Reset the line counter:
					lPrintedLineNumber = 0;
				}

				//Print the PO Header IF it's either a new PO ID OR if we've already printed a pageful of lines:
				if (lLastOrderID != lCurrentOrderID){
					printTicketHeader(
							rs.getString(SMTableorderheaders.sOrderNumber),
							rs.getString(SMTableorderheaders.sLocation),
							sUserName,
							out,
							conn		
					);
					printHeaderAddressBox(
							rs.getString(SMTableorderheaders.sBillToName),
							rs.getString(SMTableorderheaders.sBillToAddressLine1),
							rs.getString(SMTableorderheaders.sBillToAddressLine2),
							rs.getString(SMTableorderheaders.sBillToAddressLine3),
							rs.getString(SMTableorderheaders.sBillToAddressLine4),
							rs.getString(SMTableorderheaders.sBillToCity),
							rs.getString(SMTableorderheaders.sBillToState),
							rs.getString(SMTableorderheaders.sBillToZip),
							rs.getString(SMTableorderheaders.sEmailAddress),
							rs.getString(SMTableorderheaders.sBillToContact),
							rs.getString(SMTableorderheaders.sBillToPhone),
							rs.getString(SMTableorderheaders.ssecondarybilltophone),
							rs.getString(SMTableorderheaders.sShipToName),
							rs.getString(SMTableorderheaders.sShipToAddress1),
							rs.getString(SMTableorderheaders.sShipToAddress2),
							rs.getString(SMTableorderheaders.sShipToAddress3),
							rs.getString(SMTableorderheaders.sShipToAddress4),
							rs.getString(SMTableorderheaders.sShipToCity),
							rs.getString(SMTableorderheaders.sShipToState),
							rs.getString(SMTableorderheaders.sShipToZip),
							rs.getString(SMTableorderheaders.sShipToCountry),
							rs.getString(SMTableorderheaders.sShipToContact),
							rs.getString(SMTableorderheaders.sShipToPhone),
							rs.getString(SMTableorderheaders.ssecondaryshiptophone),
							rs.getString(SMTableorderheaders.sOrderNumber),
							rs.getString(SMTableorderheaders.sTerms),
							conn,
							out
					);
					printSecondHeaderBox(
							rs.getString(SMTableorderheaders.sOrderCreatedByFullName),
							rs.getString(SMTableorderheaders.sPONumber),
							rs.getString(SMTableorderheaders.sCustomerCode),
							rs.getString(SMTableorderheaders.sSalesperson),
							rs.getString(SMTableorderheaders.sSpecialWageRate),
							clsDateAndTimeConversions.resultsetDateStringToString(
									rs.getString(SMTableorderheaders.datExpectedShipDate)),
									conn, 
									out);
					String sDirections = rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.mDirections);
					if (sDirections == null){sDirections = "";}
					printDirections(sDirections, out);
					printLineHeader(out);
				}
				lPrintedLineNumber++;
				String sItemComment1 = rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sComment1);
				if (sItemComment1 == null){
					sItemComment1 = "";
				}
				BigDecimal bdQtyOrdered = rs.getBigDecimal(
						SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered);
				String sQtyOrdered = clsManageBigDecimals.BigDecimalToFormattedString(
						"########0.0000", bdQtyOrdered);
				if (bdQtyOrdered.compareTo(BigDecimal.ZERO) != 0){
					printOELine(
						rs.getString(SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber),
						rs.getString(SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemDesc),
						sQtyOrdered,
						rs.getString(SMTableorderdetails.TableName + "." + SMTableorderdetails.sOrderUnitOfMeasure),
						sItemComment1,
						lPrintedLineNumber,
						rs.getString(SMTableorderdetails.TableName + "." + SMTableorderdetails.mTicketComments),
						rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sworkordercomment),
						out);
				}
				//Reset the marker:
				lLastOrderID = lCurrentOrderID;
				sTicketComments = rs.getString(SMTableorderheaders.mTicketComments);
				if (sTicketComments == null){sTicketComments = "";}
			}
			rs.close();
		}catch (SQLException e){
			throw new SQLException("Error reading resultset - " + e.getMessage());
		}

		//If there was anything printed, print the last footer:
		if (lLastOrderID != 0)
		{
			printOrderFooter(sTicketComments, out);
		}
	}
	private void printTicketHeader(
			String sOrderNumber,
			String sLocation,
			String sUser,
			PrintWriter out,
			Connection conn
	){

		String sCompanyName = "";
		String sCompanyAddress = "";
		
		String SQL = "SELECT"
			+ " " + SMTablelocations.sCompanyDescription + ", " 
			+ " " + SMTablelocations.sRemitToAddress1 + ", " 
			+ " " + SMTablelocations.sRemitToAddress2 + ", " 
			+ " " + SMTablelocations.sRemitToAddress3 + ", " 
			+ " " + SMTablelocations.sRemitToAddress4 + ", "
			+ " " + SMTablelocations.sRemitToCity + ", " 
			+ " " + SMTablelocations.sRemitToState + ", " 
			+ " " + SMTablelocations.sRemitToPhone + ", "
			+ " " + SMTablelocations.sWebSite + ", " 
			+ " " + SMTablelocations.sRemitToZip  
			+ " FROM " + SMTablelocations.TableName
			+ " WHERE ("
			+ "(" + SMTablelocations.sLocation + " = '" + sLocation + "')"
			+ ")"
			;

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				sCompanyName = rs.getString(SMTablelocations.sCompanyDescription);
				sCompanyAddress = rs.getString(SMTablelocations.sRemitToAddress1).trim();
				if (rs.getString(SMTablelocations.sRemitToAddress2).trim().length() > 0){
					sCompanyAddress += "&nbsp;" + rs.getString(SMTablelocations.sRemitToAddress2).trim(); 
				}
				if (rs.getString(SMTablelocations.sRemitToAddress3).trim().length() > 0){
					sCompanyAddress += "&nbsp;" + rs.getString(SMTablelocations.sRemitToAddress3).trim(); 
				}
				if (rs.getString(SMTablelocations.sRemitToAddress4).trim().length() > 0){
					sCompanyAddress += "&nbsp;" + rs.getString(SMTablelocations.sRemitToAddress4).trim(); 
				}
				if (rs.getString(SMTablelocations.sRemitToCity).trim().length() > 0){
					sCompanyAddress += ",&nbsp;" + rs.getString(SMTablelocations.sRemitToCity).trim(); 
				}
				if (rs.getString(SMTablelocations.sRemitToState).trim().length() > 0){
					sCompanyAddress += ",&nbsp;" + rs.getString(SMTablelocations.sRemitToState).trim(); 
				}
				if (rs.getString(SMTablelocations.sRemitToZip).trim().length() > 0){
					sCompanyAddress += "&nbsp;" + rs.getString(SMTablelocations.sRemitToZip).trim(); 
				}
				if (rs.getString(SMTablelocations.sRemitToPhone).trim().length() > 0){
					sCompanyAddress += "&nbsp;-&nbsp;" + rs.getString(SMTablelocations.sRemitToPhone).trim(); 
				}
				if (rs.getString(SMTablelocations.sWebSite).trim().length() > 0){
					sCompanyAddress += "&nbsp;-&nbsp;" + rs.getString(SMTablelocations.sWebSite).trim(); 
				}
			}
			rs.close();
		} catch (SQLException e) {
			// Don't do anything - we just won't get the data printed:
			System.out.println("[1341948940] In " + this.toString() + ".printTicketHeader - couldn't read company address - " + e.getMessage());
		}
		
		//Now print the line headings:
		out.println ("<H3 class = \"western\" align=center>" + sCompanyName + "</H3>");
		out.println ("<H4 class = \"western\" align=center>" + sCompanyAddress + "</H4>");
		//out.println ("<p align=center><font size=1>" + sCompanyAddress + "</font></P>");
		out.println("<H2 class = \"western\" align=center>Installation Work Order - Order #: " 
				+ sOrderNumber.trim() + "</H2>");
		out.println("<H4 class = \"western\" align=center>Printed: " 
				+ clsDateAndTimeConversions.now("M/d/yyyy hh:mm a") + "&nbsp;by&nbsp;" + sUser + "</H4>");
	}
	private void printSecondHeaderBox(
			String sOrderCreatedByFullName,
			String sPONumber,
			String sCustomerCode,
			String sSalesperson,
			String sSpecialWageRate,
			String sExpectedShipDate,
			Connection conn, 
			PrintWriter pwOut){

		String sWageScale = "N";
		if (sSpecialWageRate.compareToIgnoreCase("T") == 0){
			sWageScale = "Y";
		}

		pwOut.println("<TABLE style=\"width:100%;"
				+ " border-color:black;"
				+ " border-width:1px;"
				+ " border-collapse:separate;"
				+ " border-style:solid;"
				+ " font-family: Arial;"
				+ " font-size:small;"
				+ " font-align:center;"
				+ "\">");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=CENTER><B>Created By</B></TD>");
		pwOut.println("<TD ALIGN=CENTER><B>PO Number</B></TD>");
		pwOut.println("<TD ALIGN=CENTER><B>Customer</B></TD>");
		pwOut.println("<TD ALIGN=CENTER><B>Salesperson</B></TD>");
		pwOut.println("<TD ALIGN=CENTER><B>Wage Scale</B></TD>");
		pwOut.println("<TD ALIGN=CENTER><B>Ship Date</B></TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=CENTER>" + sOrderCreatedByFullName + "</TD>");
		pwOut.println("<TD ALIGN=CENTER>" + sPONumber + "</TD>");
		pwOut.println("<TD ALIGN=CENTER>" + sCustomerCode + "</TD>");
		pwOut.println("<TD ALIGN=CENTER>" + sSalesperson + "</TD>");
		pwOut.println("<TD ALIGN=CENTER>" + sWageScale + "</TD>");
		pwOut.println("<TD ALIGN=CENTER>" + sExpectedShipDate + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("</TABLE>");
	}
	private void printHeaderAddressBox(
			String sBillToName,
			String sBillToAdd1,
			String sBillToAdd2,
			String sBillToAdd3,
			String sBillToAdd4,
			String sCity,
			String sState,
			String sPostalCode,
			String sEmail,
			String sProjectManager,
			String sProjectManagerPhone,
			String sBillToPhone2,
			String sShipToName,
			String sShipToAdd1,
			String sShipToAdd2,
			String sShipToAdd3,
			String sShipToAdd4,
			String sShipCity,
			String sShipState,
			String sShipPostalCode,
			String sMap,
			String sSuperintendent,
			String sSuperintendentPhone,
			String sShipToPhone2,
			String sOrderNumber,
			String sTerms,
			Connection conn,
			PrintWriter pwOut
	){
		pwOut.println("<TABLE style=\"width:100%;"
				+ " border-color:black;"
				+ " border-width:1px;"
				+ " border-collapse:collapse;"
				+ " font-family: Arial;"
				+ " font-size:small;"
				+ "\">");

		pwOut.println("<TR>");
		//Print the left box, containing the BILL TO name and address:
		//******************************************
		pwOut.println("<TD style=\""
				+ " width:50%;"
				+ " border-style:solid;"
				+ " border-color:black;"
				+ " border-width:1px;"
				+ "\">");

		//This is the inner left table, with the bill-to information:
		pwOut.println("<table style=\"width:100%; border-style:none; font-family: Arial; font-size:small;\">");
		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT>" + "<B>Bill to:</B>&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT COLSPAN=2>" + sBillToName + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT>" + "&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT COLSPAN=2>" + sBillToAdd1.trim() + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT>" + "&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT COLSPAN=2>" + sBillToAdd2.trim() + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT>" + "&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT COLSPAN=2>" + sBillToAdd3.trim() + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT>" + "&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT COLSPAN=2>" + sBillToAdd4.trim() + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT>" + "&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT COLSPAN=2>" + (sCity.trim() + " " + sState.trim() 
				+ " " + sPostalCode).trim()  + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT>" + "<B>Email Address:</B>&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT COLSPAN=2>" + sEmail.trim() + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT>" + "<B>Project Manager:</B>&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT COLSPAN=2>" + sProjectManager.trim() + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT>" + "<B>Phone:</B>&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT>" + sProjectManagerPhone.trim() + "&nbsp;<B>2nd:</B>&nbsp;" + sBillToPhone2 + "</TD>");
		pwOut.println("<TD ALIGN=RIGHT style = \"font-size: medium;\" >" + sTerms.trim() + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("</table>");
		pwOut.println("</TD>");


		//Print the box to the right, with the date, id, etc.
		//******************************************
		pwOut.println("<TD style=\""
				+ " width:50%;"
				+ " border-style:solid;"
				+ " border-color:black;"
				+ " border-width:1px;"
				+ "\">");

		//This is the inner left table, with the bill-to information:
		pwOut.println("<table style=\"width:100%; border-style:none; font-family: Arial; font-size:small;\">");
		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT>" + "<B>Ship to:</B>&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT>" + sShipToName + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT>" + "&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT>" + sShipToAdd1.trim() + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT>" + "&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT>" + sShipToAdd2.trim() + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT>" + "&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT>" + sShipToAdd3.trim() + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT>" + "&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT>" + sShipToAdd4.trim() + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT>" + "&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT>" + (sShipCity.trim() + " " + sShipState.trim() 
				+ " " + sShipPostalCode).trim()  + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT>" + "<B>Map:</B>&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT>" + sMap.trim() + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT>" + "<B>Superintendent:</B>&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT>" + sSuperintendent.trim() + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT>" + "<B>Phone:</B>&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT>" + sSuperintendentPhone.trim() + "&nbsp;<B>2nd:</B>&nbsp;" + sShipToPhone2 + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("</table>");
		//End the right box:
		pwOut.println("</TD>");

		pwOut.println("</TR></TABLE>");
	}
	private void printOrderFooter(
			String sTicketComment,
			PrintWriter out
	){

		sTicketComment = "Work order notes:<BR>" + sTicketComment.trim().replace("\n", "<BR>");

		//First end the OE Lines table:
		out.println("</table>");

		out.println(
				"<TABLE style = \"width:100%; border-color:black; border-width:1px; \">"
				+ "<TR>"
				+ "<TD"
				+ " style=\""
				+ " width:100%;"
				+ " border-style:solid;"
				+ " border-color:black;"
				+ " border-width:1px;"
				+ " border-collapse:separate;"
				+ " text-align:left;"
				+ " vertical-align:top;"
				+ " font-weight:normal;"
				+ " font-family: Arial;"
				+ " font-size: small;"
				+ "\""
				+ ">"
				+ sTicketComment
				+ "</TD>"
				+ "</TR>"
				+"</TABLE>"
		);
/*
		//Page break:
		out.println("<P CLASS=\"breakhere\">");
		*/
	}
	private void printDirections(
			String sDirections,
			PrintWriter out
	){

		sDirections = "<B>Directions:&nbsp;</B>" + sDirections.trim().replace("\n", "<BR>");

		out.println(
				"<TABLE style = \"width:100%; border-color:black; border-width:1px; \">"
				+ "<TR>"
				+ "<TD"
				+ " style=\""
				+ " width:100%;"
				+ " border-style:none;"
				+ " border-color:black;"
				+ " border-width:1px;"
				+ " border-collapse:separate;"
				+ " text-align:left;"
				+ " vertical-align:top;"
				+ " font-weight:normal;"
				+ " font-family: Arial;"
				+ " font-size: small;"
				+ "\""
				+ ">"
				+ sDirections
				+ "</TD>"
				+ "</TR>"
				+"</TABLE>"
		);

	}
	private void printOELine(
			String sItem,
			String sDescription,
			String sQtyOrdered,
			String sUOM,
			String sFactoryNumber,
			long lPrintedLineNumber,
			String sTicketComments,
			String sWorkOrderComments,
			PrintWriter out
	){
		//Print the line:
		out.println("<TR>");
		out.println("<TD ALIGN=RIGHT>" + clsStringFunctions.PadLeft(Long.toString(lPrintedLineNumber), "0", 4) + "</TD>");
		out.println("<TD ALIGN=LEFT>" + sItem + "</TD>");
		out.println("<TD ALIGN=LEFT>" + sDescription + "</TD>");
		out.println("<TD ALIGN=RIGHT>" + sQtyOrdered + "</TD>");
		out.println("<TD ALIGN=LEFT>" + sUOM + "</TD>");
		out.println("<TD ALIGN=LEFT>" + sFactoryNumber + "</TD>");
		out.println("</TR>");

		if (sTicketComments == null){
			sTicketComments = "";
		}
		if (sTicketComments.compareToIgnoreCase("") != 0){
			out.println("<TR>");
			out.println("<TD ALIGN=RIGHT>" + "&nbsp;" + "</TD>");
			out.println("<TD ALIGN=LEFT>" + "&nbsp;" + "</TD>");
			out.println("<TD ALIGN=LEFT COLSPAN=4>" + sTicketComments + "</TD>");
			out.println("</TR>");
		}
		
		if (sWorkOrderComments == null){
			sWorkOrderComments = "";
		}
		if (sWorkOrderComments.compareToIgnoreCase("") != 0){
			out.println("<TR>");
			out.println("<TD ALIGN=RIGHT COLSPAN=2>" + "Work order comment:" + "</TD>");
			out.println("<TD ALIGN=LEFT COLSPAN=4>" + sWorkOrderComments + "</TD>");
			out.println("</TR>");
		}
		
	}
	private void printLineHeader(
			PrintWriter out
	){
		out.println("<style type=\"text/css\">");
		out.println(
				"table.oelines {"
				+ " border-style: none;"
				+ " border-collapse:separate;"
				+ " width:100%;"
				+ " font-size:small;"
				+ " font-family:Arial;"
				+ "}"		
		);
		out.println(
				"table.oelines th {"
				+ " vertical-align:bottom;"
				+ " font-weight:bold;"
				+ " font-size:small;"
				+ " text-decoration:underline;"
				+ " border-style: none;"
				//+ " text-align:left;"
				+ "}"	
		);
		out.println(
				"table.oelines td {"
				+ " font-weight:normal;"
				+ " font-size:small;"
				+ " border-style: none;"
				+ "}"	
		);

		out.println("</style>");

		out.println("<TABLE class=\"oelines\">");

		out.println("<TR>");
		out.println("<th ALIGN=RIGHT>Line #</TH>");
		out.println("<th ALIGN=LEFT>Item Number</TH>");
		out.println("<th ALIGN=LEFT>Item Description</TH>");
		out.println("<th ALIGN=RIGHT>Qty Ordered</TH>");
		out.println("<th ALIGN=LEFT>UOM</TH>");
		out.println("<th ALIGN=LEFT>Factory Number</TH>");
		out.println("</TR>");
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
