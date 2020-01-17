package smcontrolpanel;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableservicetypes;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsStringFunctions;

public class SMServiceTicketWithFormat extends java.lang.Object{

	private String m_sErrorMessage;
	//private double m_dBrowserScaleFactor = 1d;
	private int m_iPrintAreaWidth = 550;
	private int m_iPrintAreaHeight = 728;
	

	public SMServiceTicketWithFormat(
	){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sStartingOrderNumber,
			String sEndingOrderNumber,
			String sDBID,
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

    	//System.out.println("[1356725991] 2. iNumberOfCopies = " + iNumberOfCopies);
		for (int i = 0; i < iNumberOfCopies; i++){
			try {
				printWorkOrders(
						conn,
						sStartingOrderNumber,
						sEndingOrderNumber,
						sDBID,
						sUserName,
						i,
						out
				);
				/*
				if (i < iNumberOfCopies-1){
					out.println("<P CLASS=\"breakhere\">");
				}
				*/
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
			String sDBID,
			String sUserName,
			int iCopyNumber,
			PrintWriter out
	) throws SQLException{

    	//System.out.println("[1356725992] 3. iNumberOfCopies = " + iNumberOfCopies);
		String SQL = "SELECT"
			+ " " + SMTableorderheaders.TableName + ".* " 
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
							iCopyNumber,
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
							iCopyNumber,
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
							iCopyNumber,
							conn, 
							out);

					String sTicketComments = rs.getString(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.mTicketComments);
					if (sTicketComments == null){sTicketComments = "";}
					String sDirections = rs.getString(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.mDirections);
					if (sDirections == null){sDirections = "";}
					printTicketComments(sTicketComments, 
										sDirections, 
										iCopyNumber, 
										out);
					if (rs.getString(SMTableorderheaders.sServiceTypeCode).compareToIgnoreCase("SH0001") == 0){
						printResidentialCheckboxColumns(iCopyNumber,
														out);
					}else{
						printCommercialCheckboxColumns(iCopyNumber, 
													   out);
					}
					printPartsUsedTable(iCopyNumber, 
							   			out);
					printWorkCompleteSection(iCopyNumber, 
							   				 out);
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
			int iCopyNumber,
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
		out.println("<div style=\"position: absolute; " +
			 "left:0pt; " +
			 "top:" + (iCopyNumber*m_iPrintAreaHeight+204) + "pt; " +
			 "width: " + m_iPrintAreaWidth + "pt; " +
			 "height: 18pt; " +
			 "font-family:Arial; " +
			 "font-size:9pt; " +
			 "background-color:white\"" +
			 ">" + sNotes + "</div>"
	  );
	}

	private void printResidentialCheckboxColumns(int iCopyNumber, PrintWriter out){

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
		String s = "<table "
					+ "style = \"width:100%;"
					+ " border-style:none;"
					+ " font-family:Arial;"
					+ " font-size: 7pt;"
					+ " vertical-align:top;"
					+ " padding:0px;"
					+ "\" >";
		
		s += "<TR>\n";
		s += "<TD style = \"vertical-align:center; height:15pt; padding:0px; align-text:center; font-size:10pt ;\""
				+ " ALIGN=CENTER >"
				+ "<B>Work Performed Codes</B></TD>\n";
		s += "<TD style = \"vertical-align:center; height:15pt; padding:0px; align-text:center; font-size:10pt ;\""
				+ " ALIGN=CENTER COLSPAN=2>"
				+ "<B>General Service Checklist</B></TD>\n";
		s += "</TR>\n";
		
		int iCol = 1;
		for (int i = 0; i <= 26; i++){
			if (iCol == 1){
				s += "<TR>\n";
			}
			s += "<TD style = \"font-family: Arial; height:7pt; padding:0px; \"><img src=\"./images/checkbox.jpg\""
					+ "alt=\"checkbox\" / width=7pt height=7pt style = \" vertical-align:bottom; \">" 
					+ arrWPCs.get(i) + "</TD>\n";
			if (iCol == 3){
				s += "</TR>\n";
				iCol = 1;
			}else{
				iCol++;
			}
		}

		s += "<TR>\n";
		s += "<TD style = \"font-family: Arial; height:7pt; padding:0px; \"><img src=\"./images/checkbox.jpg\""
			+ "alt=\"checkbox\" / width=7pt height=7pt style = \" vertical-align:bottom; \">" 
			+ "<b>" + arrWPCs.get(27) + "</b></TD>\n";
		s += "<TD style = \"font-family: Arial; height:7pt; padding:0px; \"><img src=\"./images/checkbox.jpg\""
				+ "alt=\"checkbox\" / width=7pt height=7pt style = \" vertical-align:bottom; \">" 
				+ arrWPCs.get(28) + "</TD>\n";
		s += "</TR>\n";
		
		s += "<TR>\n";
		s += "<TD style = \"font-family: Arial; height:7pt; padding:0px; \"><img src=\"./images/checkbox.jpg\""
			+ "alt=\"checkbox\" / width=7pt height=7pt style = \" vertical-align:bottom; \">" 
			+ arrWPCs.get(29) + "</TD>\n";

		s += "<TD style = \" padding:0px; font-size:7pt; \" COLSPAN=2 ROWSPAN=9>";
		
				//+ "<BR>Additional comments _____________________________________________________________<BR><BR>"
				//+ "______________________________________________________________________________<BR><BR>"
				//+ "______________________________________________________________________________<BR><BR>"
				//+ "______________________________________________________________________________<BR><BR>"
				//+ "______________________________________________________________________________<BR><BR>"
		
		//+ "</TD>\n");
		//out.println("</TR>\n");
		
		//Here we print a table of 'underlines':
		s += "<table "
				+ "style = \"width:100%;"
				+ " border-style:none;"
				//+ " font-family:Arial;"
				//+ " font-size: 8pt;"
				+ " vertical-align:top;"
				//+ " padding:0px;"
				+ "\" >";
		s += "<TR>\n"
				+ "<TD style = \"vertical-align:center;"
				+ " height:7pt;"
				+ " padding:0px;"
				+ " align-text:left;"
				+ " border-width: 1px;"
				+ " border-color: black;"
				+ " border-style:none none solid none;"
				+ "\">"
				+ "Additional comments:"
				+ "</TD>\n"
				+ "</TR>\n";

		for (int i = 0; i < 3; i++){
			  s += "<TR>\n"
				+ "<TD style = \"vertical-align:center;"
				+ " height:9pt;"
				+ " padding:0px;"
				+ " align-text:center;"
				+ " border-width: 1px;"
				+ " border-color: black;"
				+ " border-style:none none solid none;"
				+ "\">"
				+ "</TD>\n"
				+ "</TR>\n";
		}
		
		s += "</table>";
		
		for (int i = 30; i < arrWPCs.size(); i++){
			s += "<TR>\n";
			s += "<TD style = \"font-family: Arial; height:7pt; padding:0px; \"><img src=\"./images/checkbox.jpg\""
				+ "alt=\"checkbox\" / width=7pt height=7pt style = \" vertical-align:bottom; \">" 
				+ arrWPCs.get(i) + "</TD>\n";
			s += "</TR>\n";
		}
		
		//Finally, we need three more lines in the left column to fill out space for the lines to the right:
		//s += "<TR>\n<TD style = \" height:8pt; padding:0px; \">" + "&nbsp;" + "</TD>\n</tr";
		//out.println("<TR>\n<TD style = \" height:15px; padding:0px; \">" + "&nbsp;" + "</TD>\n</tr");
		//out.println("<TR>\n<TD style = \" height:15px; padding:0px; \">" + "&nbsp;" + "</TD>\n</tr");
		
		//Close the whole table
		s += "</table>";
		
		out.println("<div style=\"position: absolute; " +
				 "left:0pt; " +
				 "top:" + (iCopyNumber*m_iPrintAreaHeight+237) + "pt; " +
				 "width:" + m_iPrintAreaWidth + "pt; " +
				 "height: 168pt; " +
				 "font-family:Arial; " +
				 //"font-size:small; " +
				 "background-color:white\"" +
				 ">" + s + "</div>"
	  ); 
	}
	
	private void printPartsUsedTable(int iCopyNumber, PrintWriter out){
		/*	
		
		*/

		String sTable = "<table class = \"parts\" width=100%>" +
				"<TR>\n<TD COLSPAN=6 ALIGN=CENTER><B>Parts used / Additional Work Performed Items</B></TD>\n</TR>\n" +
				"<TR>\n<TD  class = \"col1\"><U>QTY.</U></TD>\n" +
				"<TD  class = \"col2\"><U>PART NO.</U></TD>\n" +
				"<TD  class = \"col3\"><U>DESCRIPTION</U></TD>\n" +
				"<TD  class = \"col1\"><U>QTY.</U></TD>\n" +
				"<TD  class = \"col2\"><U>PART NO.</U></TD>\n" +
				"<TD  class = \"col3\"><U>DESCRIPTION</U></TD>\n</TR>\n";

		for (int i = 0; i < 8; i++){
			sTable += "<TR>\n";
			for (int j = 0; j < 6; j++){
				sTable += "<TD class = \"blankcol\" height:12pt;></TD>\n";
			}
			sTable += "</TR>\n";
		}
		
		sTable += "</table>";
		
		out.println("<div style=\"position: absolute; " +
								 "left:0pt; " +
								 "top:" + (iCopyNumber*m_iPrintAreaHeight+430) + "pt; " +
								 "width:" + m_iPrintAreaWidth + "pt; " +
								 "height: 130pt; " +
								 "font-family:Arial; " +
								 //"font-size:small; " +
								 "background-color:white\"" +
								 ">" + sTable + "</div>"
					  ); 
	}
	private void printWorkCompleteSection(int iCopyNumber, PrintWriter out){
		
		String s = "WORK COMPLETE ____________"
				+ " DATE ___________"
				+ " TRUCK # _______"
				+ " REG. ______"
				+ " O.T. ______"
				+ " D.T. ______";
		out.println("<div style=\"position: absolute; " +
								 "left:0pt; " +
								 "top:" + (iCopyNumber*m_iPrintAreaHeight+560) + "pt; " +
								 "width:" + m_iPrintAreaWidth + "pt; " +
								 //"height: 25px; " +
								 "font-family:Arial; " +
								 "font-size:9pt; " +
								 "background-color:white\"" +
								 ">" + s + "</div>"
					  ); 

		//Here we print a table for 'underline':
		s = "<TABLE "
				+ "style = \"width:100%;"
				+ " border-style:none;"
				+ " font-family: Arial;"
				+ " font-size:10px;"
				+ "\" >"
				+ "<TR>\n"
				+ "<TD style = \"vertical-align:center;"
				+ " height:18pt;"
				+ " padding:0px;"
				+ " align-text:left;"
				+ " border-width: 1px;"
				+ " border-color: black;"
				+ " border-style:none none solid none;"
				+ "\">"
				+ "&nbsp;"
				+ "</TD>\n</TR>\n</TABLE>";
		out.println("<div style=\"position: absolute; " +
								 "left:0pt; " +
								 "top:" + (iCopyNumber*m_iPrintAreaHeight+580) + "pt; " +
								 "width:" + m_iPrintAreaWidth + "pt; " +
								 //"height: 25px; " +
								 "font-family:Arial; " +
								 "font-size:9pt; " +
								 "background-color:white\"" +
								 ">" + s + "</div>"
					  ); 

		s = "X";
		out.println("<div style=\"position: absolute; " +
								 "left:90pt; " +
								 "top:" + (iCopyNumber*m_iPrintAreaHeight+585) + "pt; " +
								 //"width:660px; " +
								 //"height: 25px; " +
								 "font-family:Arial; " +
								 "font-size:9pt; " +
								 "background-color:white\"" +
								 "><B>" + s + "</B></div>"
					  ); 

		s = "Date";
		out.println("<div style=\"position: absolute; " +
								 "left:30pt; " +
								 "top:" + (iCopyNumber*m_iPrintAreaHeight+602) + "pt; " +
								 //"width:660px; " +
								 //"height: 25px; " +
								 "font-family:Arial; " +
								 "font-size:8pt; " +
								 "background-color:white\"" +
								 ">" + s + "</div>"
					  ); 

		s = "Authorized&nbsp;Signature";
		out.println("<div style=\"position: absolute; " +
								 "left:90pt; " +
								 "top:" + (iCopyNumber*m_iPrintAreaHeight+602) + "pt; " +
								 //"width:660px; " +
								 //"height: 25px; " +
								 "font-family:Arial; " +
								 "font-size:8pt; " +
								 "background-color:white\"" +
								 ">" + s + "</div>"
					  ); 

		s = "(Printed)";
		out.println("<div style=\"position: absolute; " +
								 "left:300pt; " +
								 "top:" + (iCopyNumber*m_iPrintAreaHeight+602) + "pt; " +
								 //"width:660px; " +
								 //"height: 25px; " +
								 "font-family:Arial; " +
								 "font-size:8pt; " +
								 "background-color:white\"" +
								 ">" + s + "</div>"
					  ); 

		s = "Title";
		out.println("<div style=\"position: absolute; " +
								 "left:450pt; " +
								 "top:" + (iCopyNumber*m_iPrintAreaHeight+602) + "pt; " +
								 //"width:660px; " +
								 //"height: 25px; " +
								 "font-family:Arial; " +
								 "font-size:8pt; " +
								 "background-color:white\"" +
								 ">" + s + "</div>"
					  ); 
		
		//Here we print a table of 'underlines':
		s = "<TABLE "
				+ "style = \"width:100%;"
				+ " border-style:none;"
				+ " font-family: Arial;"
				+ " font-size:9pt;"
				+ "\">"
				+ "<TR>\n"
				+ "<TD style = \"vertical-align:center;"
				+ " height:18pt;"
				+ " padding:0px;"
				+ " align-text:left;"
				+ " border-width: 1px;"
				+ " border-color: black;"
				+ " border-style:none none solid none;"
				+ "\">"
				+ "WORK INCOMPLETE - ADDITIONAL WORK REQUIRED:"
				+ "</TD>\n"
				+ "</TR>\n";

		for (int i = 0; i < 3; i++){
			s += "<TR>\n"
				+ "<TD style = \"vertical-align:center;"
				+ " height:16pt;"
				+ " padding:0px;"
				+ " align-text:center;"
				+ " border-width: 1px;"
				+ " border-color: black;"
				+ " border-style:none none solid none;"
				+ "\">"
				+ "</TD>\n"
				+ "</TR>\n"
			;
		}
		
		s += "</TABLE>";
		
		out.println("<div style=\"position: absolute; " +
								 "left:0pt; " +
								 "top:" + (iCopyNumber*m_iPrintAreaHeight+612) + "pt; " +
								 "width:" + m_iPrintAreaWidth + "pt; " +
								 //"height: 25px; " +
								 "font-family:Arial; " +
								 //"font-size:small; " +
								 "background-color:white\"" +
								 ">" + s + "</div>"
					  ); 

		
		s = "<TABLE "
				+ "style = \"width:100%;"
				+ " border-style:none;"
				+ " font-family: Arial;"
				+ " font-size:9pt;"
				+ "\"><TR>\n"
				+ "<TD style = \"vertical-align:center;"
				+ " height:18pt;"
				+ " padding:0px;"
				+ " align-text:left;"
				+ " border-width: 1px;"
				+ " border-color: black;"
				+ " border-style:none none solid none;"
				+ "\">"
				+ "CUSTOMER AUTHORIZATION<BR>FOR ADDITIONAL WORK:&nbsp;&nbsp;&nbsp;&nbsp;<B>X</B>"
				+ "</TD>\n"
				+ "</TR>\n</TABLE>"
			;
		
		out.println("<div style=\"position: absolute; " +
								 "left:0pt; " +
								 "top:" + (iCopyNumber*m_iPrintAreaHeight+680) + "pt; " +
								 "width:" + m_iPrintAreaWidth + "pt; " +
								 //"height: 25px; " +
								 "font-family:Arial; " +
								 //"font-size:small; " +
								 "background-color:white\"" +
								 ">" + s + "</div>"
					  ); 
		
		s = "<span style = \"font-family:Arial;  font-size:8pt; text-align:center; \"><B>Authorized&nbsp;Signature<B>"	+ "</span>";
		
		out.println("<div style=\"position: absolute; " +
								 "left:150pt; " +
								 "top:" + (iCopyNumber*m_iPrintAreaHeight+712) + "pt; " +
								 //"width:660px; " +
								 //"height: 25px; " +
								 "font-family:Arial; " +
								 //"font-size:small; " +
								 "background-color:white\"" +
								 ">" + s + "</div>"
					  ); 
		
		s = "<span style = \"font-family:Arial;  font-size:8pt; text-align:center; \"><B>(Printed)<B>"	+ "</span>";
		
		out.println("<div style=\"position: absolute; " +
								 "left:300pt; " +
								 "top:" + (iCopyNumber*m_iPrintAreaHeight+712) + "pt; " +
								 //"width:660px; " +
								 //"height: 25px; " +
								 "font-family:Arial; " +
								 //"font-size:small; " +
								 "background-color:white\"" +
								 ">" + s + "</div>"
					  ); 
		
		s = "<span style = \"font-family:Arial;  font-size:8pt; text-align:center; \"><B>Title<B>"	+ "</span>";
		
		out.println("<div style=\"position: absolute; " +
								 "left:450pt; " +
								 "top:" + (iCopyNumber*m_iPrintAreaHeight+712) + "pt; " +
								 //"width:660px; " +
								 //"height: 25px; " +
								 "font-family:Arial; " +
								 //"font-size:small; " +
								 "background-color:white\"" +
								 ">" + s + "</div>"
					  ); 
	}
	private void printCommercialCheckboxColumns(int iCopyNumber, PrintWriter out){
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
		String s = "<table "
				+ "style = \"width:100%;"
				+ " border-style:none;"
				+ " font-family:Arial;"
				+ " font-size: 7pt;"
				+ " vertical-align:top;"
				+ " padding:0px;"
				+ "\">";
		
		s += "<TR>\n";
		s += "<TD style = \"vertical-align:center; height:15pt; padding:0px; align-text:center; font-size:10pt ;\""
				+ " ALIGN=CENTER COLSPAN=3>"
				+ "<B>Work Performed Codes</B></TD>\n";
		s += "</TR>\n";

		int iCol = 1;
		for (int i = 0; i < arrWPCs.size(); i++){
			if (iCol == 1){
				s += "<TR>\n";
			}
			s += "<TD style = \"font-family: Arial; height:7pt; padding:0px; \"><img src=\"./images/checkbox.jpg\""
					+ "alt=\"checkbox\" / width=7pt height=7pt style = \"vertical-align:bottom;\">" 
					+ "" + arrWPCs.get(i) + "</TD>\n";
			if (iCol == 3){
				s += "</TR>\n";
				iCol = 1;
			}else{
				iCol++;
			}
		}
		if (iCol != 3){
			s += "</TR>\n";
		}
		
		//Close the whole table
		s += "</table>";
		
		s += "<table "
			+ "style = \"width:100%;"
			+ " border-style:none;"
			//+ " font-family:Arial;"
			//+ " font-size: 9pt;"
			//+ " vertical-align:top;"
			//+ " padding:0px;"
			+ "\" >";
		
		for (int i = 0; i < 3; i++){
			s += "<TR>\n"
				+ "<TD style = \"vertical-align:center;"
				+ " height:11pt;"
				+ " padding:0px;"
				+ " align-text:center;"
				+ " border-width: 1px;"
				+ " border-color: black;"
				+ " border-style:none none solid none;"
				+ "\">"
				+ "</TD>\n"
				+ "</TR>\n"
				;
		}
		s += "</table>";

		out.println("<div style=\"position: absolute; " +
								 "left:0pt; " +
								 "top:" + (iCopyNumber*m_iPrintAreaHeight+237) + "pt; " +
								 "width:" + m_iPrintAreaWidth + "pt; " +
								 "height: 168px; " +
								 "font-family:Arial; " +
								 //"font-size:small; " +
								 "background-color:white\"" +
								 ">" + s + "</div>"
					  ); 
	}
	private void printTicketHeader(
			String sOrderNumber,
			String sLocation,
			String sServiceTypeCode,
			String sUser,
			int iCopyNumber,
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
				System.out.println("[1579275084] In " + this.toString() 
						+ ".printTicketHeader - couldn't get location record.");
			}
			rs.close();
		} catch (SQLException e) {
			// Don't do anything - we just won't get the data printed:
			System.out.println("[1579275087] In " + this.toString() + ".printTicketHeader - couldn't read company address");
		}

		//First, we need a table to enclose the three inner tables:
		String s = "<table style = \"width:100%; border-style:none;\">";
		s += "<TR>\n";

		//*********************************************
		//Leftmost cell (company address cell):
		//Set up the left box, for the company address:
		s += "<TD valign=top "
				+ " style=\""
				+ " width:33%;"
				+ " border-style:none;"
				+ "\">";

		//Set up the table inside the leftmost cell:
		s += "<table style=\"width:100%; border-style:none; font-family: Arial; padding:0px; font-size:10pt;\">";
		s += "<tbody>";
		//Now print the lines in the leftmost table:
		s += "<TR>\n<TD align=\"LEFT\" style=\" height: 7pt; font-size: 7pt\"><b>" + sCompanyName + "</b></TD>\n</TR>\n";
		s += "<TR>\n<TD align=\"LEFT\" style=\" height: 7pt; font-size: 7pt\">" + sAddressLine1 + "</TD>\n</TR>\n";
		s += "<TR>\n<TD align=\"LEFT\" style=\" height: 7pt; font-size: 7pt\">" + sAddressLine2 + "&nbsp;&nbsp;&nbsp;&nbsp;" + sCompanyPhone + "</TD>\n</TR>\n";
		s += "<TR>\n<TD align=\"LEFT\" style=\" height: 7pt; font-size: 7pt\"><b>" + sBranchOffice + "</b>" + "&nbsp;" + sBranchPhone + "</TD>\n</TR>\n";

		//Close the table in the leftmost cell:
		s += "</table>";
		//End the leftmost cell:
		s += "</TD>\n";
		//******************************************

		//*********************************************
		//Center cell (for title):
		//Set up the center box, for the title:
		s += "<TD style=\""
				+ " width:34%;"
				+ " border-style:none;"
				+ "\">";

		//Set up the table inside the center cell:
				s += "<table style = \"width:100%; border-style:none;\">";
		//Now print the lines in the center table:
		String sServiceType = "";
		SQL = "SELECT " + SMTableservicetypes.TableName + "." + SMTableservicetypes.sName
			+ " FROM " + SMTableservicetypes.TableName
			+ " WHERE ("
			+ "(" + SMTableservicetypes.TableName + "." + SMTableservicetypes.sCode + " = '" + sServiceTypeCode + "')"
			+ ")"
			;
		try {
			ResultSet rsServiceType = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsServiceType.next()){
				sServiceType = rsServiceType.getString(SMTableservicetypes.TableName + "." + SMTableservicetypes.sName);
			}
			rsServiceType.close();
		} catch (SQLException e) {
			//Don't do anything... the service type will not be displayed
		}

		s += "<TR>\n<TD>\n<H2 class = \"western\" align=center font-size:9pt;>" + sServiceType + "</H2>" 
				+ "<H2 class = \"western\" align=center font-size:9pt;>" + "Work Order" + "</H2>"
				+ "</TD>\n</TR>\n";

		//Close the table in the center cell:
		s += "</table>";
		//End the center cell:
		s += "</TD>\n";
		//******************************************

		//*********************************************
		//Rightmost cell (order number, etc.):
		//Set up the right box, for the order number:
		s += "<TD style=\""
				+ " width:33%;"
				+ " border-style:none;"
				+ " vertical-align:top;"
				+ " text-align:right;"
				+ " font-family:Arial;"
				+ " font-size:9pt;"
				+ " font-weight:bold;"
				+ "\">";

		//Set up the table inside the rightmost cell:
		//out.println ("<table style = \"width:100%; border-style:none; \" >");
		//Now print the lines in the rightmost table:
		s += "Order Number:&nbsp;" + sOrderNumber.trim();

		s += "<BR>"
				+ "<span style = \"font-family:Arial;  font-size:8pt; text-align:center; \"> Printed:&nbsp;" 
				+ clsDateAndTimeConversions.now("M/d/yyyy hh:mm a") + "&nbsp;by&nbsp;" + sUser
				+ "</span>";
		
		//Close the table in the rightmost cell:
		//out.println("</table>");
		//End the leftmost cell:
		s += "</TD>\n";
		//******************************************

		//End the one row in the largest table
		s += "</TR>\n";
		
		//Close the whole table
		s += "</table>";
		
		s +="<style>\n"
				+ "table.parts { "
				+ "border-style: none;"
				+ " width:100%;"
				+ " padding:0px;"
				+ " border-collapse:collapse;"
				+ " font-family: Arial;"
				+ " font-size: 9pt;"
				+ "}"
				+ "td.col1 { border: 1px solid black; width:6%; padding:0px; text-align:center; height:20px;}"
				+ "td.col2 { border: 1px solid black; width:14%; padding:0px; text-align:center; height:20px;}"
				+ "td.col3 { border: 1px solid black; width:30%; padding:0px; text-align:center; height:20px;}"
				+ "td.blankcol { border: 1px solid black; padding:0px; height:12pt; }"
				
				+ "</style>\n";
		
		out.println("<div style=\"position: absolute; " +
								 "left:0pt; " +
								 "top:" + (iCopyNumber*m_iPrintAreaHeight) + "pt; " +
								 "width:" + m_iPrintAreaWidth + "pt; " +
								 "height: 75pt; " +
								 "font-family:Arial; " +
								 //"font-size:small; " +
								 "background-color:white\"" +
								 ">" + s + "</div>"
					  ); 
	}
	private void printSecondHeaderBox(
			String sOrderCreatedByFullName,
			String sPONumber,
			String sCustomerCode,
			String sSalesperson,
			String sSpecialWageRate,
			String sExpectedShipDate,
			String sTerms,
			int iCopyNumber,
			Connection conn, 
			PrintWriter pwOut){

		String sWageScale = "N";
		if (sSpecialWageRate.compareToIgnoreCase("T") == 0){
			sWageScale = "Y";
		}

		String s = "<TABLE style=\"width:100%;"
				+ " border-color:black;"
				+ " border-width:1px;"
				+ " border-collapse:collapse;"
				+ " border-style:solid;"
				+ " font-family: Arial;"
				+ " font-align:center;"
				+ "\">";

		s += "<TR>\n";
		s += "<TD ALIGN=CENTER style=\"font-family: Arial; font-align:center; font-size:8pt; height:8pt; padding:0px;\"><B>Created By</B></TD>\n";
		s += "<TD ALIGN=CENTER style=\"font-family: Arial; font-align:center; font-size:8pt; height:8pt; padding:0px;\"><B>PO Number</B></TD>\n";
		s += "<TD ALIGN=CENTER style=\"font-family: Arial; font-align:center; font-size:8pt; height:8pt; padding:0px;\"><B>Customer</B></TD>\n";
		s += "<TD ALIGN=CENTER style=\"font-family: Arial; font-align:center; font-size:8pt; height:8pt; padding:0px;\"><B>Salesperson</B></TD>\n";
		s += "<TD ALIGN=CENTER style=\"font-family: Arial; font-align:center; font-size:8pt; height:8pt; padding:0px;\"><B>Wage Scale</B></TD>\n";
		s += "<TD ALIGN=CENTER style=\"font-family: Arial; font-align:center; font-size:8pt; height:8pt; padding:0px;\"><B>Ship Date</B></TD>\n";
		s += "<TD ALIGN=CENTER style=\"font-family: Arial; font-align:center; font-size:8pt; height:8pt; padding:0px;\"><B>Terms</B></TD>\n";
		s += "</TR>\n";

		s += "<TR>\n";
		s += "<TD ALIGN=CENTER style=\"font-family: Arial; font-align:center; font-size:8pt; height:8pt; padding:0px;\">" + sOrderCreatedByFullName + "</TD>\n";
		s += "<TD ALIGN=CENTER style=\"font-family: Arial; font-align:center; font-size:8pt; height:8pt; padding:0px;\">" + sPONumber + "</TD>\n";
		s += "<TD ALIGN=CENTER style=\"font-family: Arial; font-align:center; font-size:8pt; height:8pt; padding:0px;\">" + sCustomerCode + "</TD>\n";
		s += "<TD ALIGN=CENTER style=\"font-family: Arial; font-align:center; font-size:8pt; height:8pt; padding:0px;\">" + sSalesperson + "</TD>\n";
		s += "<TD ALIGN=CENTER style=\"font-family: Arial; font-align:center; font-size:8pt; height:8pt; padding:0px;\">" + sWageScale + "</TD>\n";
		s += "<TD ALIGN=CENTER style=\"font-family: Arial; font-align:center; font-size:8pt; height:8pt; padding:0px;\">" + sExpectedShipDate + "</TD>\n";
		s += "<TD ALIGN=CENTER style=\"font-family: Arial; font-align:center; font-size:8pt; height:8pt; padding:0px;\">" + sTerms + "</TD>\n";
		s += "</TR>\n";

		s += "</TABLE>";

		pwOut.println("<div style=\"position: absolute; " +
								 "left:0pt; " +
								 "top:"+ (iCopyNumber*m_iPrintAreaHeight+180) + "pt; " +
								 "width:"+ m_iPrintAreaWidth + "pt; " +
								 "height: 30pt; " +
								 "font-family:Arial; " +
								 //"font-size:small; " +
								 "background-color:white\"" +
								 ">" + s + "</div>"
					  ); 
		
		
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
			int iCopyNumber,
			Connection conn,
			PrintWriter pwOut
	){
		String sRowHeight = "8pt";
		String s = "<TABLE style=\"width:100%;"
				+ " border-color:black;"
				+ " border-style:solid;"
				+ " border-width:0px;"
				+ " border-collapse:collapse;"
				+ " font-family: Arial;"
				+ " padding:0pt;"
				+ "\">";

		s += "<TR>\n";
		//Print the left box, containing the BILL TO name and address:
		//******************************************
		s +="<TD style=\""
				+ " width:50%;"
				+ " border-style:solid;"
				+ " border-color:black;"
				+ " border-width:1pt;"
				+ " padding:0pt;"
				+ "\">";

		//This is the inner left table, with the bill-to information:
		s +="<table style=\"width:100%; border-collapse:collapse; border-style:none; font-family: Arial; padding:0px; font-size:8pt;\">";
		s +="<TR>\n";
		s +="<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + "<B>Bill to:</B>&nbsp;" + "</TD>\n";
		s +="<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + sBillToName + "</TD>\n";
		s +="</TR>\n";

		s +="<TR>\n";
		s +="<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + "&nbsp;" + "</TD>\n";
		s +="<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + sBillToAdd1.trim() + "</TD>\n";
		s +="</TR>\n";

		//if (sBillToAdd2.trim().compareToIgnoreCase("") != 0){
			s +="<TR>\n";
			s +="<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + "&nbsp;" + "</TD>\n";
			s +="<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + sBillToAdd2.trim() + "</TD>\n";
			s +="</TR>\n";
		//}
		
		//if (sBillToAdd3.trim().compareToIgnoreCase("") != 0){
			s +="<TR>\n";
			s +="<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + "&nbsp;" + "</TD>\n";
			s +="<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + sBillToAdd3.trim() + "</TD>\n";
			s +="</TR>\n";
		//}
		
		//if (sBillToAdd4.trim().compareToIgnoreCase("") != 0){
			s +="<TR>\n";
			s +="<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + "&nbsp;" + "</TD>\n";
			s +="<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + sBillToAdd4.trim() + "</TD>\n";
			s +="</TR>\n";
		//}

		s +="<TR>\n";
		s +="<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + "&nbsp;" + "</TD>\n";
		s +="<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + (sCity.trim() + " " + sState.trim() 
				+ " " + sPostalCode).trim()  + "</TD>\n";
		s +="</TR>\n";

		s +="<TR>\n";
		s +="<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + "<B>Email Address:</B>&nbsp;" + "</TD>\n";
		s +="<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + sEmail.trim() + "</TD>\n";
		s +="</TR>\n";

		s +="<TR>\n";
		s +="<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + "<B>Authorized:</B>&nbsp;" + "</TD>\n";
		s +="<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + sProjectManager.trim() + "</TD>\n";
		s +="</TR>\n";

		s +="<TR>\n";
		s +="<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + "<B>Phone:</B>&nbsp;" + "</TD>\n";
		s +="<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + sProjectManagerPhone.trim()
				+ "&nbsp;<B>2nd phone:</B>&nbsp;" + sBillTo2ndPhone.trim()
				+ "&nbsp;<B>Fax:</B>&nbsp;" + sBillToFax.trim()
				+ "</TD>\n";

		s +="</TR>\n";

		s +="</table>";
		s +="</TD>\n";


		//Print the box to the right, with the date, id, etc.
		//******************************************
		s +="<TD style=\""
				+ " width:50%;"
				+ " border-style:solid;"
				+ " border-color:black;"
				+ " border-width:1pt;"
				+ " padding:0pt;"
				+ "\">";

		//This is the inner left table, with the bill-to information:
		s +="<table style=\"width:100%; border-collapse:collapse; border-style:none; font-family: Arial; padding:0px; font-size:8pt;\">";
		s +="<TR>\n";
		s +="<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + "<B>Ship to:</B>&nbsp;" + "</TD>\n";
		s +="<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + sShipToName + "</TD>\n";
		s +="</TR>\n";

		s +="<TR>\n";
		s +="<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + "&nbsp;" + "</TD>\n";
		s +="<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + sShipToAdd1.trim() + "</TD>\n";
		s +="</TR>\n";

		//if (sShipToAdd2.trim().compareToIgnoreCase("") != 0){
			s +="<TR>\n";
			s +="<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + "&nbsp;" + "</TD>\n";
			s +="<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + sShipToAdd2.trim() + "</TD>\n";
			s +="</TR>\n";
		//}
		
		//if (sShipToAdd3.trim().compareToIgnoreCase("") != 0){
			s +="<TR>\n";
			s +="<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + "&nbsp;" + "</TD>\n";
			s +="<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + sShipToAdd3.trim() + "</TD>\n";
			s +="</TR>\n";
		//}

		//if (sShipToAdd4.trim().compareToIgnoreCase("") != 0){
			s +="<TR>\n";
			s +="<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + "&nbsp;" + "</TD>\n";
			s +="<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + sShipToAdd4.trim() + "</TD>\n";
			s +="</TR>\n";
		//}
		s +="<TR>\n";
		s +="<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + "&nbsp;" + "</TD>\n";
		s +="<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + (sShipCity.trim() + " " + sShipState.trim() 
				+ " " + sShipPostalCode).trim()  + "</TD>\n";
		s +="</TR>\n";

		s +="<TR>\n";
		s +="<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + "<B>Map:</B>&nbsp;" + "</TD>\n";
		s +="<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + sMap.trim() + "</TD>\n";
		s +="</TR>\n";

		s +="<TR>\n";
		s +="<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + "<B>Job Contact:</B>&nbsp;" + "</TD>\n";
		s +="<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + sSuperintendent.trim() + "</TD>\n";
		s +="</TR>\n";

		s +="<TR>\n";
		s +="<TD ALIGN=RIGHT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + "<B>Job Phone:</B>&nbsp;" + "</TD>\n";
		s +="<TD ALIGN=LEFT style=\" height: " + sRowHeight + "; font-size: 8pt\">" + sSuperintendentPhone.trim()
				+ "&nbsp;<B>2nd phone:</B>&nbsp;" + sShipTo2ndPhone.trim()
				+ "&nbsp;&nbsp;<B>Fax:</B>&nbsp;" + sShipToFax.trim()

				+ "</TD>\n";
		s +="</TR>\n";

		s +="</table>";
		//End the right box:
		s +="</TD>\n";

		s +="</TR>\n</TABLE>";
		
		pwOut.println("<div style=\"position: absolute; " +
								 "left:0pt; " +
								 "top: " + (iCopyNumber*m_iPrintAreaHeight+51) + "pt; " +
								 "width:"+ m_iPrintAreaWidth + "pt; " +
								 "height: 100pt; " +
								 "font-family:Arial; " +
								 //"font-size:small; " +
								 "background-color:white\"" +
								 ">" + s + "</div>"
					  ); 
		
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}

