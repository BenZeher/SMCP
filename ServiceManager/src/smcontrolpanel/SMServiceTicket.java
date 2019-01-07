package smcontrolpanel;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTableorderheaders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsStringFunctions;

public class SMServiceTicket extends java.lang.Object{

	private String m_sErrorMessage;

	public SMServiceTicket(
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
				printWorkOrders(
						conn,
						sStartingOrderNumber,
						sEndingOrderNumber,
						sUserName,
						iNumberOfCopies,
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
			int iNumberOfCopies,
			PrintWriter out
	) throws SQLException{

		String SQL = "SELECT"
			+ " " + SMTableorderheaders.TableName + ".*" 
			+ " FROM"
			+ " " + SMTableorderheaders.TableName
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

		SQL += ")"	//Complete the 'where' clause
			+ " ORDER BY"
			+ " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber
			;

		long lLastOrderID = 0;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				//If there are more than one orders to print, print a page break before any consecutive page:
				//if (lLastOrderID != 0L){
				//	out.println("<P CLASS=\"breakhere\">");
				//}
				long lCurrentOrderID = rs.getLong(SMTableorderheaders.TableName 
						+ "." + SMTableorderheaders.dOrderUniqueifier);

				//Print the PO Header IF it's either a new PO ID OR if we've already printed a pageful of lines:
				if (lLastOrderID != lCurrentOrderID){
					printTicketHeader(
							rs.getString(SMTableorderheaders.sOrderNumber),
							rs.getString(SMTableorderheaders.sLocation),
							rs.getString(SMTableorderheaders.sServiceTypeCode),
							sUserName,
							out,
							conn		
					);
					printHeaderAddressBox(
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sBillToName)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sBillToAddressLine1)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sBillToAddressLine2)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sBillToAddressLine3)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sBillToAddressLine4)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sBillToCity)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sBillToState)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sBillToZip)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sEmailAddress)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sBillToContact)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sBillToPhone)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sBillToFax)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.ssecondarybilltophone)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sShipToName)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sShipToAddress1)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sShipToAddress2)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sShipToAddress3)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sShipToAddress4)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sShipToCity)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sShipToState)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sShipToZip)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sShipToCountry)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sShipToContact)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sShipToPhone)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sShipToFax)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.ssecondaryshiptophone)),
							clsStringFunctions.checkStringForNull(rs.getString(SMTableorderheaders.sOrderNumber)),
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
							rs.getString(SMTableorderheaders.sTerms),
							conn, 
							out);

					String sTicketComments = rs.getString(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.mTicketComments);
					if (sTicketComments == null){sTicketComments = "";}
					String sDirections = rs.getString(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.mDirections);
					if (sDirections == null){sDirections = "";}
					printTicketComments(sTicketComments, sDirections, out);
					if (rs.getString(SMTableorderheaders.sServiceTypeCode).compareToIgnoreCase("SH0001") == 0){
						printResidentialCheckboxColumns(out);
					}else{
						printCommercialCheckboxColumns(out);
					}
					printPartsUsedTable(out);
					printWorkCompleteSection(out);
					out.println("<P CLASS=\"breakhere\">");
				}
				//Reset the marker:
				lLastOrderID = lCurrentOrderID;
			}
			rs.close();
		}catch (SQLException e){
			throw new SQLException("Error reading resultset - " + e.getMessage());
		}

	}
	private void printTicketComments(
			String sTicketComments,
			String sDirections,
			PrintWriter out
	){
		String sNotes = "";
		sTicketComments = sTicketComments.trim();
		sDirections = sDirections.trim();
		
		if (sTicketComments.compareToIgnoreCase("") != 0){
			sNotes = "<B>Work Order Note:</B>&nbsp;" + sTicketComments;
		}
		if (sDirections.compareToIgnoreCase("") != 0){
			if (sNotes.compareToIgnoreCase("") == 0){
				sNotes += "<B>Directions:</B>&nbsp;" + sDirections;
			}else{
				sNotes += "<BR><B>Directions:</B>&nbsp;" + sDirections;
			}
		}
		out.println(
				"<span style=\" font-family:Arial; font-size:small; \" >"
				+ sNotes
				+ "</span>"
		);
	}
	private void printResidentialCheckboxColumns(PrintWriter out){

		ArrayList<String> arrWPCs = new ArrayList<String>(0);
		//Build in the WPC's here:
		arrWPCs.add(" 502 Adj. Track/Guides");
		arrWPCs.add(" 500 Performed General Service");
		arrWPCs.add(" 838 Chk'd Sect. Condition");
		
		arrWPCs.add(" 504 Demonstrate Operation");
		arrWPCs.add(" 831 Inspected Fasteners/Replaced Worn Parts");
		arrWPCs.add(" 839 Inspected track Radius");
		
		arrWPCs.add(" 506 Checked/Adj. Counterbalance Springs");
		arrWPCs.add(" 832 Inspected Rollers/Replaced Worn Parts");
		arrWPCs.add(" 840 Tested Operator Rev. Mechanism");
		
		arrWPCs.add(" 509 Inspect/Tighten Set Screw");
		arrWPCs.add(" 501 Lubricated Moving Parts");
		arrWPCs.add(" 841 Inspected Exposed Wiring");
		
		arrWPCs.add(" 511 Repair/Replace Drive Chain");
		arrWPCs.add(" 833 Lubricated Springs");
		arrWPCs.add(" 842 Chk'd/Inst OHD Service Sticker");
		
		arrWPCs.add(" 512 Repair/Replace Lock");
		arrWPCs.add(" 834 Inspected Cables/Replaced Worn Parts");
		arrWPCs.add(" 843 Inspected/Replaced Perimeter");
		
		arrWPCs.add(" 516 Replace Damaged/Worn Hardware");
		arrWPCs.add(" 835 Chk'd Proper Balance/Alignment of Door");
		arrWPCs.add(" 844 Insp'd/Replaced Weather Seal");
		
		arrWPCs.add(" 601 Measure/Temp Repair Spring");
		arrWPCs.add(" 836 Chk'd Bottom Fixtures/Replc'd Worn Parts");
		arrWPCs.add(" 510 Inspected Moving Parts");
		
		arrWPCs.add(" 604 Replaced Section(s)");
		arrWPCs.add(" 837 Chk'd Cable Condition/Replc'd Worn Parts");
		arrWPCs.add(" 513 Tested Operation");
		
		arrWPCs.add(" 801 Check/Adj. Opr. Limit Switch");
		arrWPCs.add(" 704 Programmed Radio/Receiver");
	
		arrWPCs.add(" 818 Replaced Extension Spring(s)");
		arrWPCs.add(" 819 Installed Safety Cable(s)");
		arrWPCs.add(" 824 Replaced Torsion Spring(s)");
		arrWPCs.add(" 829 Replaced Torsion Spring Plug(s)");
		arrWPCs.add(" 830 Replaced Fasteners");
		arrWPCs.add(" 808 Realigned Photo Cell");
		
		//First, we need a table to enclose the three inner tables:
		out.println ("<table "
				+ "style = \"width:100%;"
				+ " border-style:none;"
				+ " font-family:Arial;"
				+ " font-size: 9pt;"
				+ " vertical-align:top;"
				+ " padding:0px;"
				+ "\" >");
		
		out.println("<tr>");
		out.println("<TD style = \"vertical-align:center; height:20px; padding:0px; align-text:center; font-size:12pt ;\""
				+ " ALIGN=CENTER >"
				+ "<B>Work Performed Codes</B></TD>");
		out.println("<TD style = \"vertical-align:center; height:20px; padding:0px; align-text:center; font-size:12pt ;\""
				+ " ALIGN=CENTER COLSPAN=2>"
				+ "<B>General Service Checklist</B></TD>");
		out.println("</tr>");

		int iCol = 1;
		for (int i = 0; i <= 26; i++){
			if (iCol == 1){
				out.println("<tr>");
			}
			out.println("<td style = \" height:15px; padding:0px; \"><img src=\"./images/checkbox.jpg\""
					+ "alt=\"checkbox\" / width=12px height=12px style = \" vertical-align:bottom; \">" 
					+ arrWPCs.get(i) + "</td>");
			if (iCol == 3){
				out.println("</tr>");
				iCol = 1;
			}else{
				iCol++;
			}
		}

		out.println("<tr>");
		out.println("<td style = \" height:15px; padding:0px; \"><img src=\"./images/checkbox.jpg\""
			+ "alt=\"checkbox\" / width=12px height=12px style = \" vertical-align:bottom; \">" 
			+ arrWPCs.get(27) + "</td>");
		out.println("<td style = \" height:15px; padding:0px; \"><img src=\"./images/checkbox.jpg\""
				+ "alt=\"checkbox\" / width=12px height=12px style = \" vertical-align:bottom; \">" 
				+ arrWPCs.get(28) + "</td>");
		out.println("</tr>");
		
		out.println("<tr>");
		out.println("<td style = \" height:15px; padding:0px; \"><img src=\"./images/checkbox.jpg\""
			+ "alt=\"checkbox\" / width=12px height=12px style = \" vertical-align:bottom; \">" 
			+ arrWPCs.get(29) + "</td>");

		out.println("<td style = \" padding:0px; font-size:11pt; \" COLSPAN=2 ROWSPAN=9>");
		
				//+ "<BR>Additional comments _____________________________________________________________<BR><BR>"
				//+ "______________________________________________________________________________<BR><BR>"
				//+ "______________________________________________________________________________<BR><BR>"
				//+ "______________________________________________________________________________<BR><BR>"
				//+ "______________________________________________________________________________<BR><BR>"
		
		//+ "</td>");
		//out.println("</tr>");
		
		//Here we print a table of 'underlines':
		out.println ("<table "
				+ "style = \"width:100%;"
				+ " border-style:none;"
				//+ " font-family:Arial;"
				//+ " font-size: 9pt;"
				+ " vertical-align:top;"
				//+ " padding:0px;"
				+ "\" >");
		out.println("<TR>"
				+ "<TD style = \"vertical-align:center;"
				+ " height:20px;"
				+ " padding:0px;"
				+ " align-text:left;"
				+ " border-width: 1px;"
				+ " border-color: black;"
				+ " border-style:none none solid none;"
				+ "\">"
				+ "Additional comments:"
				+ "</TD>"
				+ "</TR>"
			);

		for (int i = 0; i < 3; i++){
			out.println("<TR>"
				+ "<TD style = \"vertical-align:center;"
				+ " height:20px;"
				+ " padding:0px;"
				+ " align-text:center;"
				+ " border-width: 1px;"
				+ " border-color: black;"
				+ " border-style:none none solid none;"
				+ "\">"
				+ "</TD>"
				+ "</TR>"
			);
		}
		
		out.println("</table>");
		
		for (int i = 30; i < arrWPCs.size(); i++){
			out.println("<tr>");
			out.println("<td style = \" height:15px; padding:0px; \"><img src=\"./images/checkbox.jpg\""
				+ "alt=\"checkbox\" / width=12px height=12px style = \" vertical-align:bottom; \">" 
				+ arrWPCs.get(i) + "</td>");
			out.println("</tr>");
		}
		
		//Finally, we need three more lines in the left column to fill out space for the lines to the right:
		out.println("<tr><td style = \" height:15px; padding:0px; \">" + "&nbsp;" + "</td></tr");
		//out.println("<tr><td style = \" height:15px; padding:0px; \">" + "&nbsp;" + "</td></tr");
		//out.println("<tr><td style = \" height:15px; padding:0px; \">" + "&nbsp;" + "</td></tr");
		
		//Close the whole table
		out.println("</table>");
	}
	private void printPartsUsedTable(PrintWriter out){
				
		out.println(
			"<style>\n"
			+ "table.parts { "
			+ "border-style: none;"
			+ " width:100%;"
			+ " padding:0px;"
			+ " border-collapse:collapse;"
			+ " font-family: Arial;"
			+ " font-size: 11pt;"
			+ "}"
			+ "td.col1 { border: 1px solid black; width:6%; padding:0px; text-align:center; height:20px;}"
			+ "td.col2 { border: 1px solid black; width:14%; padding:0px; text-align:center; height:20px;}"
			+ "td.col3 { border: 1px solid black; width:30%; padding:0px; text-align:center; height:20px;}"
			+ "td.blankcol { border: 1px solid black; padding:0px; height:20px; }"
			
			+ "</style>\n"
		);

		out.println("<table class = \"parts\" width=100%>");
		
		out.println("<tr><td COLSPAN=6 ALIGN=CENTER>"
			+ "Parts used / Additional Work Performed Items</td></tr>");
		out.println("<tr><td  class = \"col1\"><U>QTY.</U></td>");
		out.println("<td  class = \"col2\"><U>PART NO.</U></td>");
		out.println("<td  class = \"col3\"><U>DESCRIPTION</U></td>");
		out.println("<td  class = \"col1\"><U>QTY.</U></td>");
		out.println("<td  class = \"col2\"><U>PART NO.</U></td>");
		out.println("<td  class = \"col3\"><U>DESCRIPTION</U></td></tr>");

		for (int i = 0; i < 8; i++){
			out.println("<tr>");
			for (int j = 0; j < 6; j++){
				out.println("<td class = \"blankcol\" ></td>");
			}
			out.println("</tr>");
		}
		
		out.println("</table>");
	}
	private void printWorkCompleteSection(PrintWriter out){
		
		out.println(
			"<span style = \" font-size:5pt; \" >&nbsp;<BR></span>"
			+ "<span style = \" font-family: Arial; \" >"	
			+ "WORK COMPLETE ____________"
			+ " DATE ___________"
			+ " TRUCK # _______"
			+ " REG. ______"
			+ " O.T. ______"
			+ " D.T. ______"
			+ "</span>"
			+ "<BR>"
			+ "<BR>"
			+ "<B><U>" 
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<span style = \" font-family: Arial; font-size:12pt; font-weight:bold; \" >"
			+ "X"
			+ "</span>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "</U></B>"
			+ "<BR>"
			+ "<span style = \" font-family: Arial; font-size:8pt; font-weight:bold; \" >"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "Date"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "Authorized Signature"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "(Printed)"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "Title</span>"
			+ "<BR>"
		);
		//Here we print a table of 'underlines':
		out.println ("<table "
				+ "style = \"width:100%;"
				+ " border-style:none;"
				+ " font-family: Arial;"
				+ " font-size:12pt;"
				//+ " font-weight:bold;"
				//+ " font-size: 9pt;"
				//+ " vertical-align:top;"
				//+ " padding:0px;"
				+ "\" >");
		out.println("<TR>"
				+ "<TD style = \"vertical-align:center;"
				+ " height:20px;"
				+ " padding:0px;"
				+ " align-text:left;"
				+ " border-width: 1px;"
				+ " border-color: black;"
				+ " border-style:none none solid none;"
				+ "\">"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;WORK INCOMPLETE - ADDITIONAL WORK REQUIRED:"
				+ "</TD>"
				+ "</TR>"
			);

		for (int i = 0; i < 2; i++){
			out.println("<TR>"
				+ "<TD style = \"vertical-align:center;"
				+ " height:20px;"
				+ " padding:0px;"
				+ " align-text:center;"
				+ " border-width: 1px;"
				+ " border-color: black;"
				+ " border-style:none none solid none;"
				+ "\">"
				+ "</TD>"
				+ "</TR>"
			);
		}
		
		out.println("<TR>"
				+ "<TD style = \"vertical-align:center;"
				+ " height:20px;"
				+ " padding:0px;"
				+ " align-text:left;"
				+ " border-width: 1px;"
				+ " border-color: black;"
				+ " border-style:none none solid none;"
				+ "\">"
				+ "CUSTOMER AUTHORIZATION<BR>FOR ADDITIONAL WORK: <B>X</B>"
				+ "</TD>"
				+ "</TR>"
			);
		out.println("<TR>"
				+ "<TD style = \"vertical-align:center;"
				+ " height:20px;"
				+ " padding:0px;"
				+ " align-text:left;"
				+ " border-width: 1px;"
				+ " border-color: black;"
				+ " border-style:none none none none;"
				+ " font-weight:bold;"
				+ " font-size: 9pt;"
				+ "\">"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
				+ "Authorized signature"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
				+ "(Printed)"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
				+ "Title"
				+ "</TD>"
				+ "</TR>"
			);
		//out.println("<TR>"
		//		+ "<TD style = \"vertical-align:center;"
		//		+ " height:20px;"
		//		+ " padding:0px;"
		//		+ " align-text:left;"
		//		+ " border-width: 1px;"
		//		+ " border-color: black;"
		//		+ " border-style:none none solid none;"
		//		+ "\">"
		//		+ "REFERRED TO:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
		//		+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
		//		+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
		//		+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
		//		+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
		//		+ " DATE:"
		//		+ "</TD>"
		//		+ "</TR>"
		//	);
		out.println("</table>");

	}
	private void printCommercialCheckboxColumns(PrintWriter out){
		ArrayList<String> arrWPCs = new ArrayList<String>(0);
		//Build in the WPC's here:
		
		arrWPCs.add(" 500 Performed General Service"); 
		arrWPCs.add(" 518 Replaced Gear Reducer");
		arrWPCs.add(" 843 Inspect/Replace W/S");
		arrWPCs.add(" 501 Lubricated Moving Parts");
		arrWPCs.add(" 512 Repair/Replace Lock");
		arrWPCs.add(" 709 Unjammed Curtain");
		arrWPCs.add(" 502 Adj. Track/Guides");
		arrWPCs.add(" 514 Align & Tighten Sprockets");
		arrWPCs.add(" 705 Replaced Slats");
		arrWPCs.add(" 503 Leveled/Aligned Door");
		arrWPCs.add(" 516 Replace Damaged/Worn Hardware");
		arrWPCs.add(" 706 Replaced Bottom Bar");
		arrWPCs.add(" 506 Checked/Adj. Counterbalance Springs");
		arrWPCs.add(" 601 Measure/Temp Repair Spring");
		arrWPCs.add(" 707 Replace Endlock/Windlocks");
		arrWPCs.add(" 507 Installed Key Stock");
		arrWPCs.add(" 602 Replaced Spring(s)");
		arrWPCs.add(" 801 Checked/Adjusted Operator Limit Switch");
		arrWPCs.add(" 508 Replace/Tighten Fasteners");
		arrWPCs.add(" 603 Measure/Temp Repair Section");
		arrWPCs.add(" 816 Replaced Safety Edge");
		arrWPCs.add(" 509 Inspect/Tighten Set Screw");
		arrWPCs.add(" 604 Replaced Sections");
		arrWPCs.add(" 817 Traced/Repair Wiring");
		arrWPCs.add(" 511 Repair/Replace Drive Chain");
		arrWPCs.add(" 606 Retracked/Replaced Cable(s)");
		arrWPCs.add(" 510 Inspected Moving Parts");
		arrWPCs.add(" 833 Lubricated Springs");
		arrWPCs.add(" 613 Retracked Rollers");
		arrWPCs.add(" 513 Tested Operation");
		arrWPCs.add(" 820 Adjust \"V\" Belt");
		arrWPCs.add(" 842 Checked/Installed Service Sticker");
		arrWPCs.add(" 504 Demonstrate Operations");
		arrWPCs.add(" 826 Replace \"V\" Belts");

		
		//First, we need a table to enclose the three inner tables:
		out.println ("<table "
				+ "style = \"width:100%;"
				+ " border-style:none;"
				+ " font-family:Arial;"
				+ " font-size: 9pt;"
				+ " vertical-align:top;"
				+ " padding:0px;"
				+ "\" >");
		
		out.println("<tr>");
		out.println("<TD style = \"vertical-align:center; height:20px; padding:0px; align-text:center; font-size:12pt ;\""
				+ " ALIGN=CENTER COLSPAN=3>"
				+ "<B>Work Performed Codes</B></TD>");
		out.println("</tr>");

		int iCol = 1;
		for (int i = 0; i < arrWPCs.size(); i++){
			if (iCol == 1){
				out.println("<tr>");
			}
			out.println("<td style = \" height:15px; padding:0px; \"><img src=\"./images/checkbox.jpg\""
					+ "alt=\"checkbox\" / width=12px height=12px style = \" vertical-align:bottom; \">" 
					+ arrWPCs.get(i) + "</td>");
			if (iCol == 3){
				out.println("</tr>");
				iCol = 1;
			}else{
				iCol++;
			}
		}
		if (iCol != 3){
			out.println("</tr>");
		}
		
		//Close the whole table
		out.println("</table>");
		
		out.println ("<table "
			+ "style = \"width:100%;"
			+ " border-style:none;"
			//+ " font-family:Arial;"
			//+ " font-size: 9pt;"
			//+ " vertical-align:top;"
			//+ " padding:0px;"
			+ "\" >");
		
		for (int i = 0; i < 3; i++){
			out.println("<TR>"
					+ "<TD style = \"vertical-align:center;"
					+ " height:20px;"
					+ " padding:0px;"
					+ " align-text:center;"
					+ " border-width: 1px;"
					+ " border-color: black;"
					+ " border-style:none none solid none;"
					+ "\">"
					+ "</TD>"
					+ "</TR>"
				);
		}
		out.println("</table>");
	}

	private void printTicketHeader(
			String sOrderNumber,
			String sLocation,
			String sServiceTypeCode,
			String sUser,
			PrintWriter out,
			Connection conn
	){

		String sCompanyName = "";
		String sAddressLine1 = "";
		String sBranchOffice = "";
		String sAddressLine2 = "";
		String sCompanyPhone = "";
		String sBranchPhone = "";

		String SQL = "SELECT *"
			+ " FROM " + SMTablelocations.TableName
			+ " WHERE ("
			+ "(" + SMTablelocations.sLocation + " = '" + sLocation + "')"
			+ ")"
			;

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				sCompanyName = rs.getString(SMTablelocations.sCompanyDescription);
				sAddressLine1 = rs.getString(SMTablelocations.sAddress1);
				sBranchOffice = rs.getString(SMTablelocations.sSecondOfficeName);
				sAddressLine2 = rs.getString(SMTablelocations.sCity).trim() + ", "
				+ rs.getString(SMTablelocations.sState).trim() + " "
				+ rs.getString(SMTablelocations.sZip);
				sCompanyPhone = rs.getString(SMTablelocations.sPhone);
				sBranchPhone = rs.getString(SMTablelocations.sSecondOfficePhone);

			}else{
				m_sErrorMessage = "Could not read data for location '" + sLocation + "'.";
				System.out.println("In " + this.toString() 
						+ ".printTicketHeader - couldn't get location record.");
			}
			rs.close();
		} catch (SQLException e) {
			// Don't do anything - we just won't get the data printed:
			System.out.println("In " + this.toString() + ".printTicketHeader - couldn't read company address");
		}

		//First, we need a table to enclose the three inner tables:
		out.println ("<table style = \"width:100%; border-style:none;\" >");
		out.println("<tr>");

		//*********************************************
		//Leftmost cell (company address cell):
		//Set up the left box, for the company address:
		out.println("<TD style=\""
				+ " width:34%;"
				+ " border-style:none;"
				+ "\">");

		//Set up the table inside the leftmost cell:
		out.println ("<table style = \"width:100%; border-style:none; font-family: Arial; font-size:small; \" >");
		//Now print the lines in the leftmost table:
		out.println("<tr><td><B>" + sCompanyName + "</B></td></tr>");
		out.println("<tr><td>" + sAddressLine1 + "</td></tr>");
		out.println("<tr><td>" + sAddressLine2 + "</td></tr>");
		out.println("<tr><td>" + sCompanyPhone + "</td></tr>");
		out.println("<tr><td><B>" + sBranchOffice + "</B>" + "&nbsp;" + sBranchPhone + "</td></tr>");

		//Close the table in the leftmost cell:
		out.println("</table>");
		//End the leftmost cell:
		out.println("</td>");
		//******************************************

		//*********************************************
		//Center cell (for title):
		//Set up the center box, for the title:
		out.println("<TD style=\""
				+ " width:32%;"
				+ " border-style:none;"
				+ "\">");

		//Set up the table inside the center cell:
		out.println ("<table style = \"width:100%; border-style:none;\" >");
		//Now print the lines in the center table:
		String sServiceType = "Commercial";
		if (sServiceTypeCode.compareToIgnoreCase("SH0001") == 0 || 
			sServiceTypeCode.compareToIgnoreCase("SH0002") == 0){
			sServiceType = "Residential";
		}
		out.println("<tr><td><H1 class = \"western\" align=center>" + sServiceType + "</H1>" 
				+ "<H1 class = \"western\" align=center>" + "Work Order" + "</H1>"
				+ "</td></tr>"
				);

		//Close the table in the center cell:
		out.println("</table>");
		//End the center cell:
		out.println("</td>");
		//******************************************

		//*********************************************
		//Rightmost cell (order number, etc.):
		//Set up the right box, for the order number:
		out.println("<TD style=\""
				+ " width:34%;"
				+ " border-style:none;"
				+ " vertical-align:top;"
				+ " text-align:right;"
				+ " font-family:Arial;"
				+ " font-weight:bold;"
				+ "\">");

		//Set up the table inside the rightmost cell:
		//out.println ("<table style = \"width:100%; border-style:none; \" >");
		//Now print the lines in the rightmost table:
		out.println("Order Number:&nbsp;" + sOrderNumber.trim());

		out.println("<BR>"
				+ "<span style = \"font-family:Arial; font-size:small; text-align:center; \"> Printed:&nbsp;" 
				+ clsDateAndTimeConversions.now("M/d/yyyy hh:mm a") + "&nbsp;by&nbsp;" + sUser
				+ "</span>"
				);
		
		//Close the table in the rightmost cell:
		//out.println("</table>");
		//End the leftmost cell:
		out.println("</td>");
		//******************************************

		//End the one row in the largest table
		out.println("</tr>");
		
		//Close the whole table
		out.println("</table>");
	}
	private void printSecondHeaderBox(
			String sOrderCreatedByFullName,
			String sPONumber,
			String sCustomerCode,
			String sSalesperson,
			String sSpecialWageRate,
			String sExpectedShipDate,
			String sTerms,
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
		pwOut.println("<TD ALIGN=CENTER><B>Terms</B></TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=CENTER>" + sOrderCreatedByFullName + "</TD>");
		pwOut.println("<TD ALIGN=CENTER>" + sPONumber + "</TD>");
		pwOut.println("<TD ALIGN=CENTER>" + sCustomerCode + "</TD>");
		pwOut.println("<TD ALIGN=CENTER>" + sSalesperson + "</TD>");
		pwOut.println("<TD ALIGN=CENTER>" + sWageScale + "</TD>");
		pwOut.println("<TD ALIGN=CENTER>" + sExpectedShipDate + "</TD>");
		pwOut.println("<TD ALIGN=CENTER>" + sTerms + "</TD>");
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
			String sBillToFax,
			String sBillTo2ndPhone,
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
			String sShipToFax,
			String sShipTo2ndPhone,
			String sOrderNumber,
			Connection conn,
			PrintWriter pwOut
	){
		String sRowHeight = "20px";
		pwOut.println("<TABLE style=\"width:100%;"
				+ " border-color:black;"
				+ " border-width:1px;"
				+ " border-collapse:collapse;"
				+ " font-family: Arial;"
				+ " font-size:small;"
				+ " padding:0px;"
				+ "\">");

		pwOut.println("<TR>");
		//Print the left box, containing the BILL TO name and address:
		//******************************************
		pwOut.println("<TD style=\""
				+ " width:50%;"
				+ " border-style:solid;"
				+ " border-color:black;"
				+ " border-width:1px;"
				+ " padding:0px;"
				+ "\">");

		//This is the inner left table, with the bill-to information:
		pwOut.println("<table style=\"width:100%; border-style:none; font-family: Arial; font-size:small; padding:0px; \">");
		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; \">" + "<B>Bill to:</B>&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; \">" + sBillToName + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; \">" + "&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; \">" + sBillToAdd1.trim() + "</TD>");
		pwOut.println("</TR>");

		if (sBillToAdd2.trim().compareToIgnoreCase("") != 0){
			pwOut.println("<TR>");
			pwOut.println("<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; \">" + "&nbsp;" + "</TD>");
			pwOut.println("<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; \">" + sBillToAdd2.trim() + "</TD>");
			pwOut.println("</TR>");
		}
		
		if (sBillToAdd3.trim().compareToIgnoreCase("") != 0){
			pwOut.println("<TR>");
			pwOut.println("<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; \">" + "&nbsp;" + "</TD>");
			pwOut.println("<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; \">" + sBillToAdd3.trim() + "</TD>");
			pwOut.println("</TR>");
		}
		
		if (sBillToAdd4.trim().compareToIgnoreCase("") != 0){
			pwOut.println("<TR>");
			pwOut.println("<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; \">" + "&nbsp;" + "</TD>");
			pwOut.println("<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; \">" + sBillToAdd4.trim() + "</TD>");
			pwOut.println("</TR>");
		}

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; \">" + "&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; \">" + (sCity.trim() + " " + sState.trim() 
				+ " " + sPostalCode).trim()  + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; \">" + "<B>Email Address:</B>&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; \">" + sEmail.trim() + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; \">" + "<B>Authorized:</B>&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; \">" + sProjectManager.trim() + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; \">" + "<B>Phone:</B>&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; \">" + sProjectManagerPhone.trim()
				+ "&nbsp;<B>2nd phone:</B>&nbsp;" + sBillTo2ndPhone.trim()
				+ "&nbsp;<B>Fax:</B>&nbsp;" + sBillToFax.trim()
				+ "</TD>");

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
				+ " padding:0px;"
				+ "\">");

		//This is the inner left table, with the bill-to information:
		pwOut.println("<table style=\"width:100%; border-style:none; font-family: Arial; font-size:small; padding:0px; \">");
		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; \">" + "<B>Ship to:</B>&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; \">" + sShipToName + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; \">" + "&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; \">" + sShipToAdd1.trim() + "</TD>");
		pwOut.println("</TR>");

		if (sShipToAdd2.trim().compareToIgnoreCase("") != 0){
			pwOut.println("<TR>");
			pwOut.println("<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; \">" + "&nbsp;" + "</TD>");
			pwOut.println("<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; \">" + sShipToAdd2.trim() + "</TD>");
			pwOut.println("</TR>");
		}
		
		if (sShipToAdd3.trim().compareToIgnoreCase("") != 0){
			pwOut.println("<TR>");
			pwOut.println("<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; \">" + "&nbsp;" + "</TD>");
			pwOut.println("<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; \">" + sShipToAdd3.trim() + "</TD>");
			pwOut.println("</TR>");
		}

		if (sShipToAdd4.trim().compareToIgnoreCase("") != 0){
			pwOut.println("<TR>");
			pwOut.println("<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; \">" + "&nbsp;" + "</TD>");
			pwOut.println("<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; \">" + sShipToAdd4.trim() + "</TD>");
			pwOut.println("</TR>");
		}
		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; \">" + "&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; \">" + (sShipCity.trim() + " " + sShipState.trim() 
				+ " " + sShipPostalCode).trim()  + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; \">" + "<B>Map:</B>&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; \">" + sMap.trim() + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; \">" + "<B>Job Contact:</B>&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; \">" + sSuperintendent.trim() + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; \">" + "<B>Job Phone:</B>&nbsp;" + "</TD>");
		pwOut.println("<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; \">" + sSuperintendentPhone.trim()
				+ "&nbsp;<B>2nd phone:</B>&nbsp;" + sShipTo2ndPhone.trim()
				+ "&nbsp;&nbsp;<B>Fax:</B>&nbsp;" + sShipToFax.trim()

				+ "</TD>");
		pwOut.println("</TR>");

		pwOut.println("</table>");
		//End the right box:
		pwOut.println("</TD>");

		pwOut.println("</TR></TABLE>");
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
