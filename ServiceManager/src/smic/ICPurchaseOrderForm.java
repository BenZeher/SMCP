package smic;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicpolines;
import SMDataDefinition.SMTableicvendors;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;

public class ICPurchaseOrderForm extends java.lang.Object{

	private String m_sErrorMessage;
	public final static String PRINTUNRECEIVEDONLY = "PrintUnreceivedOnly";
	public ICPurchaseOrderForm(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sStartingPOID,
			String sEndingPOID,
			String sDBID,
			String sUserName,
			int iLinesToPrintPerPage,
			boolean bOnlyPrintUnreceived,
			PrintWriter out
			){
	
		if (
				(sStartingPOID.compareToIgnoreCase("") == 0)
				&& sEndingPOID.compareToIgnoreCase("") == 0
		){
    		m_sErrorMessage = "You must enter a PO number";
    		return false;
		}
		
		String SQL = "SELECT"
			+ " * FROM"
			+ " " + SMTableicpoheaders.TableName
			+ " LEFT JOIN " + SMTableicpolines.TableName + " ON " 
			+ SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid 
			+ " = " + SMTableicpolines.TableName + "." + SMTableicpolines.lpoheaderid
			+ " WHERE ("
			;
			if (sStartingPOID.compareToIgnoreCase("") == 0){
				SQL += "(" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid 
					+ " = " + sEndingPOID + ")";
			}
			if (sEndingPOID.compareToIgnoreCase("") == 0){
				SQL += "(" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid 
					+ " = " + sStartingPOID + ")";
			}
			if (
					(sStartingPOID.compareToIgnoreCase("") != 0)
					&& (sEndingPOID.compareToIgnoreCase("") != 0)
					
			){
				SQL += "(" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid 
					+ " >= " + sStartingPOID + ")"
				+ " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid 
					+ " <= " + sEndingPOID+ ")";
			}
			
			//If the user has chosen to ONLY print the 'unreceived' items, qualify that here:
			if (bOnlyPrintUnreceived){
				SQL += " AND (" + SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyordered
					+ " >=" + SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyreceived + ")";
			}
			
			
			SQL += ")"	//Complete the 'where' clause
				+ " ORDER BY"
				+ " " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid
				+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.llinenumber
			;
		
    	long lLastPOID = 0;
    	
		int ilinesPrintedOnPageSoFar = 0;
		BigDecimal bdPOTotal = new BigDecimal(0);
		String sPOComment = "";
    	try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				long lCurrentPOID = rs.getLong(SMTableicpoheaders.TableName 
						+ "." + SMTableicpoheaders.lid);
				
				//If there was a previous line, and the PO ID has changed, print the PO footer:
				if ((lLastPOID != lCurrentPOID) && (lLastPOID != 0)){
					printPOFooter(
							ilinesPrintedOnPageSoFar, 
							iLinesToPrintPerPage, 
							clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdPOTotal),
							sPOComment,
							out,
							true
					);

					bdPOTotal = BigDecimal.ZERO;
				}

				//Print the PO Header IF it's either a new PO ID OR if we've already printed a pageful of lines:
				if (
						(lLastPOID != lCurrentPOID)
						|| (ilinesPrintedOnPageSoFar >= iLinesToPrintPerPage)
						
				){
					
					//If we've printed a pageful already, print a page break:
					 if (ilinesPrintedOnPageSoFar >= iLinesToPrintPerPage){
						 out.println("</TABLE><P CLASS=\"breakhere\">");
					 }
					printPOHeader(
						clsDatabaseFunctions.getRecordsetStringValue(
							rs, SMTableicpolines.TableName + "." + SMTableicpolines.slocation).trim(),
						clsDateAndTimeConversions.resultsetDateStringToString(
							rs.getString(SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datpodate)),
						Long.toString(rs.getLong(SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid)),
						rs.getString(SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sponumber).trim(),
						rs.getString(SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor).trim(),
						bOnlyPrintUnreceived,
						out,
						conn		
					);
					//Start counting lines on the page:
					ilinesPrintedOnPageSoFar = 0;
				}

				//If there's a valid line:
				if (clsDatabaseFunctions.getRecordsetStringValue(
						rs, SMTableicpolines.TableName + "." 
						+ SMTableicpolines.sitemnumber).trim().compareToIgnoreCase("") != 0){
					String sQtyToList = "";
					if (bOnlyPrintUnreceived){
						sQtyToList = clsManageBigDecimals.BigDecimalToFormattedString(
							"########0.0000", rs.getBigDecimal(
							SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyordered).subtract(
							rs.getBigDecimal(SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyreceived)));
					}else{
						sQtyToList = clsManageBigDecimals.BigDecimalToFormattedString(
							"########0.0000", rs.getBigDecimal(
							SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyordered));
					}
					printPOLine(
						sQtyToList,
						rs.getString(SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber).trim(),
						rs.getString(SMTableicpolines.TableName + "." + SMTableicpolines.svendorsitemnumber).trim(),
						rs.getString(SMTableicpolines.TableName + "." + SMTableicpolines.sitemdescription).trim(),
						clsManageBigDecimals.BigDecimalToFormattedString(
							"########0.0000", rs.getBigDecimal(
								SMTableicpolines.TableName + "." + SMTableicpolines.bdunitcost)),
						rs.getString(SMTableicpolines.TableName + "." + SMTableicpolines.sunitofmeasure).trim(),
						clsManageBigDecimals.BigDecimalToFormattedString(
							"########0.0000", rs.getBigDecimal(
								SMTableicpolines.TableName + "." + SMTableicpolines.bdextendedordercost)),
						rs.getString(SMTableicpolines.TableName + "." + SMTableicpolines.sinstructions).trim(),
						out
					);
					bdPOTotal = bdPOTotal.add(
						rs.getBigDecimal(SMTableicpolines.TableName + "." + SMTableicpolines.bdextendedordercost));
				}
				//Just keep picking this up on every record, even though they are probably repeating,
				//since we get a record for every line:
				sPOComment = rs.getString(SMTableicpoheaders.TableName + "." + SMTableicpoheaders.scomment);
				ilinesPrintedOnPageSoFar++;
				//Reset the marker:
				lLastPOID = lCurrentPOID;
			}
			rs.close();
    	}catch (SQLException e){
    		m_sErrorMessage = "Error reading resultset - " + e.getMessage();
    		return false;
    	}
    	
    	//If there was anything printed, print the last footer:
		if (lLastPOID != 0)
		{
			printPOFooter(
					ilinesPrintedOnPageSoFar, 
					iLinesToPrintPerPage, 
					clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdPOTotal),
					sPOComment,
					out,
					false
			);
		}
		return true;
	}
	private void printPOHeader(
			String sLocation,
			String sPODate,
			String sPOID,
			String sPONumber,
			String sVendorCode,
			boolean bOnlyPrintUnreceived,
			PrintWriter out,
			Connection conn
	){
		
		String SQL = "SELECT *"
			+ " FROM " + SMTableicpoheaders.TableName
			+ " WHERE ("
				+ "(" + SMTableicpoheaders.lid + " = " + sPOID + ")"
			+ ")"
			;
		
		try {
			ResultSet rsPOHeader = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rsPOHeader.next()){

			}

			SQL = "SELECT"
				+ " * FROM " + SMTableicvendors.TableName
				+ " WHERE ("
					+ "(" + SMTableicvendors.svendoracct + " = '" + sVendorCode + "')"
				+ ")"
				;
			
			ResultSet rsVendor = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rsVendor.next()){
		
			}
			//First, print the 'TITLE' box:
			printPOHeaderTitleBox(
				rsPOHeader.getString(SMTableicpoheaders.sbillname),
				rsPOHeader.getString(SMTableicpoheaders.sbilladdress1),
				rsPOHeader.getString(SMTableicpoheaders.sbilladdress2),
				rsPOHeader.getString(SMTableicpoheaders.sbilladdress3),
				rsPOHeader.getString(SMTableicpoheaders.sbilladdress4),
				rsPOHeader.getString(SMTableicpoheaders.sbillcity),
				rsPOHeader.getString(SMTableicpoheaders.sbillstate),
				rsPOHeader.getString(SMTableicpoheaders.sbillpostalcode),
				rsPOHeader.getString(SMTableicpoheaders.sbillphone),
				rsPOHeader.getString(SMTableicpoheaders.sbillfax),
				sPODate,
				sPOID,
				sPONumber,
				out,
				conn
				);
			
			//Next, print the 'ADDRESS' box
			printPOHeaderAddressBox(
				rsPOHeader,
				rsVendor,
				out,
				conn
			);
			
			//Finally, print the 'INFO' box
			printPOHeaderInfoBox(
					rsPOHeader,
					out,
					conn
			);
			
			printPOLineHeader(bOnlyPrintUnreceived, out);

			rsPOHeader.close();
		} catch (SQLException e) {
			// Don't do anything - we just won't get the data printed:
			//System.out.println("In " + this.toString() + ".printPOHeaderAddressBox - couldn't read company address");
		}

		//Now print the line headings:
		
	}
	private void printPOHeaderTitleBox(
			String sCompanyDescription,
			String sBillToAddress1,
			String sBillToAddress2,
			String sBillToAddress3,
			String sBillToAddress4,
			String sBillToCity,
			String sBillToState,
			String sBillToZip,
			String sBillToPhone,
			String sBillToFax,
			String sPODate,
			String sPOID,
			String sPONumber,
			PrintWriter out,
			Connection conn
	){
		out.println("<TABLE style=\"width:100%\"><TR>");
		
		//Print the left box, containing the company name and address:
		//******************************************
		out.println("<TD style=\""
				+ " width:40%;"
				+ " text-align:center;"
				+ " vertical-align:top;"
				+ " border-style:none;"
				+ " border-color:black;"
				+ " background-color: white;"
				+ " border-width: 1px;"
				+ " padding: 1px;"
				+ " border-style:solid;"
				+ " font-size:small;"
				+ "\">"
		);
				
		out.println("<B>" + sCompanyDescription + "</B><BR>");
		if (sBillToAddress1.compareToIgnoreCase("") != 0){
			out.println(sBillToAddress1);
		}
		if (sBillToAddress2.compareToIgnoreCase("") != 0){
			out.println(sBillToAddress2 + "<BR>");
		}
		if (sBillToAddress3.compareToIgnoreCase("") != 0){
			out.println(sBillToAddress3 + "<BR>");
		}
		if (sBillToAddress4.compareToIgnoreCase("") != 0){
			out.println(sBillToAddress4 + "<BR>");
		}
		String sCityStateZip = sBillToCity;
		if (sCityStateZip.compareToIgnoreCase("") != 0){
			sCityStateZip += ", ";
		}
		sCityStateZip += sBillToState;
		sCityStateZip += " " + sBillToZip;
		sCityStateZip = sCityStateZip.trim();
		if (sCityStateZip.compareToIgnoreCase("") != 0){
			out.println(sCityStateZip + "<BR>");
		}
		
		out.println("Phone: " + sBillToPhone);
		out.println("FAX: " + sBillToFax);
		
		out.println("</TD>");
		//******************************************
		
		//Print the middle box with the title
		//******************************************
		out.println("<TD style="
				+ "\"text-align:center;"
				+ " vertical-align:top;"
				+ " width:30%;"
				+ " border-color:black;"
				+ " background-color: white;"
				+ " border-width: 1px;"
				+ " padding: 1px;"
				+ " border-style:solid;"
				+ "\">");
		out.println("<B>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;PURCHASE ORDER&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</B><BR>");
		out.println("<B>" + sPOID + "</B>");
		out.println("</TD>");
		//******************************************
		
		//Print the box to the right, with the date, id, etc.
		//******************************************
		out.println("<TD style="
				+ "\"text-align:center;"
				+ " vertical-align:top;"
				+ " width:30%;"
				+ " border-color:black;"
				+ " background-color: white;"
				+ " border-width: 1px;"
				+ " padding: 1px;"
				+ " border-style:solid;"
				+ "\">");
		out.println("<TABLE style=\"width:100%\">");
		out.println("<TR>");
		out.println("<TD style="
				+ "\"text-align:center;"
				+ " vertical-align:top;"
				+ " width:50%;"
				+ " border-style:none;"
				+ " font-size:small;"
				+ "\">");
		out.println("<B>Date</B><BR>");
		out.println(sPODate);
		out.println("</TD>");
		out.println("<TD style="
				+ "\"text-align:center;"
				+ " vertical-align:top;"
				+ " width:50%;"
				+ " border-style:none;"
				+ " font-size:small;"
				+ "\">");
		//out.println("<B>Purchase Order ID</B><BR>");
		out.println("<B>" + sPONumber + "</B>");
		out.println("</TD>");
		out.println("</TR>");
		out.println("</TABLE>");
		out.println("</TD>");
		//******************************************
		
		out.println("</TR></TABLE>");
	}
	private void printPOHeaderInfoBox(
			ResultSet rsPOHeader,
			PrintWriter out,
			Connection conn
	){
		out.println("<TABLE style=\"width:100%\"><TR>");
		
		//Print the left box, containing the company name and address:
		//******************************************
		try {
			out.println(
				"<TD style="
				+ "\"width:33%;"
				+ " vertical-align:top;"
				+ " border-width: 1px;"
				+ " padding: 1px;"
				+ " border-style: inset;"
				+ " border-color: black;"
				+ " background-color: white;"
				+ " font-size:small;"
				+ "\">"
				+ "<SPAN style="
				+ "\"font-weight:bold; text-align:center"
				+ "\">"
				+ "Reference</SPAN><BR>"
				+ rsPOHeader.getString(SMTableicpoheaders.sreference).trim()
				+ "</TD>"
			);
			out.println(
					"<TD style="
					+ "\"width:25%;"
					+ " vertical-align:top;"
					+ " border-width: 1px;"
					+ " padding: 1px;"
					+ " border-style: inset;"
					+ " border-color: black;"
					+ " background-color: white;"
					+ " font-size:small;"
					+ "\">"
					+ "<SPAN style=\"font-weight:bold; text-align:center \">Contact</SPAN><BR>"
					+ rsPOHeader.getString(SMTableicpoheaders.sbillcontactname).trim()
					+ "</TD>"
				);
			out.println(
					"<TD style="
					+ "\"width:25%;"
					+ " vertical-align:top;"
					+ " border-width: 1px;"
					+ " padding: 1px;"
					+ " border-style: inset;"
					+ " border-color: black;"
					+ " background-color: white;"
					+ " font-size:small;"
					+ "\">"
					+ "<SPAN style=\"font-weight:bold; text-align:center \">Ship Via</SPAN><BR>"
					+ rsPOHeader.getString(SMTableicpoheaders.sshipvianame).trim()
					+ "</TD>"
				);
			out.println(
					"<TD style="
					+ "\"width:25%;"
					+ " vertical-align:top;"
					+ " border-width: 1px;"
					+ " padding: 1px;"
					+ " border-style: inset;"
					+ " border-color: black;"
					+ " background-color: white;"
					+ " font-size:small;"
					+ "\">"
					+ "<SPAN style=\"font-weight:bold; text-align:center \">Expected</SPAN><BR>"
					+ clsDateAndTimeConversions.resultsetDateStringToString(rsPOHeader.getString(SMTableicpoheaders.datexpecteddate))
					//+ SMUtilities.sqlDateToString(rsPOHeader.getDate(SMTableicpoheaders.datexpecteddate), "MM/dd/yyyy")
					+ "</TD>"
				);
			
		} catch (SQLException e) {
			System.out.println("[1579203930] Error printing POHeaderInfoBox - " + e.getMessage());
		}
		
		out.println("</TR></TABLE>");
		//******************************************

	}
	private void printPOHeaderAddressBox(
		ResultSet rsPOHeader,
		ResultSet rsVendor,
		PrintWriter out,
		Connection conn
	){
		
		out.println("<TABLE style=\"border-style:none; width:100%\"><TR>");
		
		//Print the vendor info box:
		out.println(
				"<TD style=\""
				+ " text-align:center;"
				+ " vertical-align:top;"
				+ " width:50%;"
				+ " border-style:none;"
				+ " background-color: white;"
				+ " border-width: 1px;"
				+ " padding: 1px;"
				+ "\">"
		);
		out.println("<TABLE style="
				+ "\"border-style:solid;"
				+ " width:100%;"
				+ " height:100%;"
				+ " vertical-align:top;"
				+ " background-color: white;"
				+ " border-width: 1px;"
				+ " padding: 1px;"
				+ " border-style:solid;"
				+ "\">");
		out.println("<TR>");
		//****************************************************************
		out.println(
				"<TD style=\""
				+ " text-align:left;"
				+ " vertical-align:top;"
				+ " border-style:none;"
				+ " font-size:small;"
				+ "\">"
		);
		
		try {
			out.println("<B>VENDOR #:&nbsp;" + rsVendor.getString(SMTableicvendors.svendoracct) + "</B><BR>");
			
			out.println(rsVendor.getString(SMTableicvendors.sname).trim() + "<BR>");
			if (rsVendor.getString(SMTableicvendors.saddressline1).trim().compareToIgnoreCase("") != 0){
				out.println(rsVendor.getString(SMTableicvendors.saddressline1).trim() + "<BR>");
			}
			if (rsVendor.getString(SMTableicvendors.saddressline2).trim().compareToIgnoreCase("") != 0){
				out.println(rsVendor.getString(SMTableicvendors.saddressline2).trim() + "<BR>");
			}
			if (rsVendor.getString(SMTableicvendors.saddressline3).trim().compareToIgnoreCase("") != 0){
				out.println(rsVendor.getString(SMTableicvendors.saddressline3).trim() + "<BR>");
			}
			if (rsVendor.getString(SMTableicvendors.saddressline4).trim().compareToIgnoreCase("") != 0){
				out.println(rsVendor.getString(SMTableicvendors.saddressline4).trim() + "<BR>");
			}
			String sCityStateZip = rsVendor.getString(SMTableicvendors.scity).trim();
			if (sCityStateZip.compareToIgnoreCase("") != 0){
				sCityStateZip += ", ";
			}
			sCityStateZip += rsVendor.getString(SMTableicvendors.sstate).trim();
			sCityStateZip += " " + rsVendor.getString(SMTableicvendors.spostalcode).trim();
			sCityStateZip = sCityStateZip.trim();
			if (sCityStateZip.compareToIgnoreCase("") != 0){
				out.println(sCityStateZip + "<BR>");
			}
			
			out.println("Phone: " + rsVendor.getString(SMTableicvendors.sphonenumber).trim() 
				+ "Fax: " + rsVendor.getString(SMTableicvendors.sfaxnumber).trim()
				+ "<BR>"
					
			);
		} catch (SQLException e) {
			// Don't do anything - we just won't get the company name printed:
			//System.out.println("In " + this.toString() + ".printPOHeaderAddressBox - couldn't read company address");
		}
		
		out.println("</TD>");
		out.println("</TR>");
		out.println("</TABLE>");
		out.println("</TD>");
		//****************************************************************
		
		//Print the ship to info:
		//****************************************************************
		out.println(
				"<TD style=\""
				+ " text-align:center;"
				+ " vertical-align:top;"
				+ " width:50%;"
				+ " border-style:none;"
				+ " background-color: white;"
				+ " border-width: 1px;"
				+ " padding: 1px;"
				+ "\">"
		);
		out.println("<TABLE style="
				+ "\"border-style:solid;"
				+ " width:100%;"
				+ " height:100%;"
				+ " vertical-align:top;"
				+ " background-color: white;"
				+ " border-width: 1px;"
				+ " padding: 1px;"
				+ " border-style:solid;"
				+ "\">");
		out.println("<TR>");
		out.println(
				"<TD style=\""
				+ " text-align:left;"
				+ " vertical-align:top;"
				+ " font-size:small;"
				+ " border-style:none;"
				+ "\">"
		);
		
		try {
			out.println("<B>" + "SHIP TO:" + "</B><BR>");
			out.println("<B>" + rsPOHeader.getString(SMTableicpoheaders.sshipname).trim() + "</B><BR>");
			if (rsPOHeader.getString(SMTableicpoheaders.sshipaddress1).trim().compareToIgnoreCase("") != 0){
				out.println(rsPOHeader.getString(SMTableicpoheaders.sshipaddress1).trim() + "<BR>");
			}
			if (rsPOHeader.getString(SMTableicpoheaders.sshipaddress2).trim().compareToIgnoreCase("") != 0){
				out.println(rsPOHeader.getString(SMTableicpoheaders.sshipaddress2).trim() + "<BR>");
			}
			if (rsPOHeader.getString(SMTableicpoheaders.sshipaddress3).trim().compareToIgnoreCase("") != 0){
				out.println(rsPOHeader.getString(SMTableicpoheaders.sshipaddress3).trim() + "<BR>");
			}
			if (rsPOHeader.getString(SMTableicpoheaders.sshipaddress4).trim().compareToIgnoreCase("") != 0){
				out.println(rsPOHeader.getString(SMTableicpoheaders.sshipaddress4).trim() + "<BR>");
			}
			String sCityStateZip = rsPOHeader.getString(SMTableicpoheaders.sshipcity).trim();
			if (sCityStateZip.compareToIgnoreCase("") != 0){
				sCityStateZip += ", ";
			}
			sCityStateZip += rsPOHeader.getString(SMTableicpoheaders.sshipstate).trim();
			sCityStateZip += " " + rsPOHeader.getString(SMTableicpoheaders.sshippostalcode).trim();
			sCityStateZip = sCityStateZip.trim();
			if (sCityStateZip.compareToIgnoreCase("") != 0){
				out.println(sCityStateZip + "<BR>");
			}
			
			out.println("Phone: " + rsPOHeader.getString(SMTableicpoheaders.sshipphone).trim());
		} catch (SQLException e) {
			// Don't do anything - we just won't get the company name printed:
			//System.out.println("In " + this.toString() 
			//	+ ".printPOHeaderTitleBox - couldn't read company address"
			//	+ " - " + e.getMessage()
			//);
		}
		
		out.println("</TD>");
		out.println("</TR>");
		out.println("</TABLE>");
		out.println("</TD>");
		//****************************************************************
		out.println("</TD>");
		out.println("</TR></TABLE>");
	}
	private void printPOFooter(
			int iLinesPrintedSoFar,
			int iLinesToBePrinted,
			String sTotalPOAmt,
			String sPOComment,
			PrintWriter out,
			boolean bPrintEndingPageBreak
	){
		//Push the footer to the bottom of the page:
		//for(int i=iLinesPrintedSoFar; i < iLinesToBePrinted; i++){
		//	out.println("<TR><TD>&nbsp;</TD></TR>");
		//}
		
		sPOComment = sPOComment.trim().replace("\n", "<BR>");
		out.println("<TR>"
				+ "<TD colspan=5"
				+ " style=\""
				+ " border-top-style:solid;"
				+ " border-color:black;"
				+ " border-width:1px;"
				+ " text-align:left;"
				+ " vertical-align:top;"
				+ " font-weight:normal;"
				+ "\">"
				+ sPOComment
				+ "</TD>"
		);
		
		out.println("<TD"
				+ " style=\""
				+ " border-top-style:solid;"
				+ " border-color:black;"
				+ " border-width:1px;"
				+ " text-align:right;"
				+ " vertical-align:top"
				+ " font-weight:bold;"
				+ "\">"
				+ "TOTAL:"
				+ "</TD>"
				+ "<TD"
				+ " style=\""
				+ " border-top-style:solid;"
				+ " border-color:black;"
				+ " border-width:1px;"
				+ " text-align:right;"
				+ " vertical-align:top"
				+ " font-weight:bold;"
				+ "\">"
				+ sTotalPOAmt
				+ "</TD></TR>"
		);
		//End the po line table:
		out.println("</TABLE>");
		
		//Page break:
		if (bPrintEndingPageBreak){
			out.println("<P CLASS=\"breakhere\">");
		}
	}
	private void printPOLine(
			String sQtyOrdered,
			String sItem,
			String sVendorItem,
			String sDescription,
			String sUnitCost,
			String sUOM,
			String sExtendedCost,
			String sInstructions,
			PrintWriter out
	){
		//Print the line:
		out.println("<TR>");
		out.println("<TD style=\"text-align:right;\">" + sQtyOrdered + "</TD>");
		out.println("<TD style=\"text-align:left;\">" + sItem + "</TD>");
		out.println("<TD style=\"text-align:left;\">" + sVendorItem + "</TD>");
		out.println("<TD style=\"text-align:left;\">" + sDescription + "</TD>");
		out.println("<TD style=\"text-align:right;\">" + sUnitCost + "</TD>");
		out.println("<TD style=\"text-align:left;\">" + sUOM + "</TD>");
		out.println("<TD style=\"text-align:right;\"\">" + sExtendedCost + "</TD>");
		out.println("</TR>");
		
		sInstructions = sInstructions.trim().replace("\n", "<BR>");
		if (sInstructions.compareToIgnoreCase("") != 0){
			out.println("<TR><TD></TD><TD></TD><TD></TD>");
			out.println("<TD style=\"text-align:left;\">" + sInstructions + "</TD>");
			out.println("<TD></TD><TD></TD>");
			out.println("</TR>");
		}
	}
	private void printPOLineHeader(
		boolean bOnlyPrintUnreceived,
		PrintWriter out
	){
		out.println("<style type=\"text/css\">");
		out.println(
				"table.polines {"
					+ " border-style: none;"
					+ " width:100%;"
					+ " font-size:small;"
				+ "}"		
		);
		out.println(
				"table.polines th {"
					+ " vertical-align:bottom;"
					+ " font-weight:bold;"
					+ " font-size:small;"
					+ " border-style: none;"
					+ "}"	
		);
		out.println(
				"table.polines td {"
					+ " font-weight:normal;"
					+ " font-size:small;"
					+ " border-style: none;"
					+ "}"	
		);
		
		out.println("</style>");
		
		out.println("<TABLE class=\"polines\">");
		
		out.println("<TR>");
		if (bOnlyPrintUnreceived){
			out.println("<TH style=\"text-align:center;\"><U>Qty<BR>Remaining</U></TH>");
		}else{
			out.println("<TH style=\"text-align:center;\"><U>Qty<BR>Ordered</U></TH>");
		}
		
		out.println("<TH style=\"text-align:left;\"><U>Our&nbsp;item</U></TH>");
		out.println("<TH style=\"text-align:left;\"><U>Vendor&nbsp;item</U></TH>");
		out.println("<TH style=\"text-align:left;\"><U>Description</U></TH>");
		out.println("<TH style=\"text-align:right;\"><U>Unit&nbsp;Cost</U></TH>");
		out.println("<TH style=\"text-align:left;\"><U>UOM</U></TH>");
		out.println("<TH style=\"text-align:right;\"><U>Extended&nbsp;cost</U></TH>");
		out.println("</TR>");
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
