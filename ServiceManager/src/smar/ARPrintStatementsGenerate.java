package smar;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTablearcustomerstatistics;
import SMDataDefinition.SMTablearmatchingline;
import SMDataDefinition.SMTableartransactions;
import SMDataDefinition.SMTablecompanyprofile;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;

public class ARPrintStatementsGenerate extends HttpServlet {

	//A/R print statements

	private static final long serialVersionUID = 1L;
	//formats
	private String sWarning = "";
	private String sCallingClass = "";

	//variables for customer total calculations
	private BigDecimal dTotalCurrent = new BigDecimal(0);
	private BigDecimal dTotal1st = new BigDecimal(0);
	private BigDecimal dTotal2nd = new BigDecimal(0);
	private BigDecimal dTotal3rd = new BigDecimal(0);
	private BigDecimal dTotalOver = new BigDecimal(0);

	private String sTempTableName = "";
	
	private int iCurrent;
	private int i1st;
	private int i2nd;
	private int i3rd;
	//private static SimpleDateFormat USTimeOnlyformatter = new SimpleDateFormat("hh:mm:ss a");
	private static String sUserID = "";
	private static String sUserFullName = "";
	private static String sDBID = "";
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ARPrintStatements))
			{
				return;
			}

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
						+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		//sCallingClass will look like: smar.ARAgedTrialBalanceReport
		sCallingClass = ARUtilities.get_Request_Parameter("CallingClass", request);

		boolean bPrintOnlyOverCurrent = false;
		if (request.getParameter("OnlyOverCurrent") != null){
			bPrintOnlyOverCurrent = true;
		}
		
		boolean bPrintZeroBalanceStatements = false;
		if (request.getParameter("PrintZeroBalanceStatements") != null){
			bPrintZeroBalanceStatements = true;
		}

		boolean bPrintOnlyRequireStatement = false;
		if (request.getParameter("OnlyRequireStatement") != null){
			bPrintOnlyRequireStatement = true;
		}
		
		
		java.sql.Date datAgeAsOf;
		java.sql.Date datCutOffDate;
		try {
			String sAgeAsOf = request.getParameter("AsOfDate");
			
			try {
				datAgeAsOf = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyy", sAgeAsOf);
			} catch (Exception e) {
				sWarning = "Error:[1423840379] Invalid 'Age as of' date: '" + sAgeAsOf + "' - " + e.getMessage();
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);			
				return;
			}

			//Get the cut off date:
			String sCutOffDate = request.getParameter("CutOffDate");
			
			try {
				datCutOffDate = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyy", sCutOffDate);
			} catch (Exception e) {
				sWarning = "Error:[1423845049] Invalid cut off date. '" + sCutOffDate + "' - " + e.getMessage();
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);			
				return;
			}
			
			String sStartingCustomer = request.getParameter("StartingCustomer");
			String sEndingCustomer = request.getParameter("EndingCustomer");
			iCurrent = Integer.parseInt(request.getParameter("Current").trim());
			i1st = Integer.parseInt(request.getParameter("1st").trim());
			i2nd = Integer.parseInt(request.getParameter("2nd").trim());
			i3rd = Integer.parseInt(request.getParameter("3rd").trim());
			String sAgedBy = request.getParameter("AgedBy");
			//get selected doc types
			ArrayList<Integer> alDocTypes = new ArrayList<Integer>(0);
			for (int i=0;i <= 10;i++){
				if (request.getParameter(ARDocumentTypes.Get_Document_Type_Label(i)) != null){
					alDocTypes.add(i);
				}
			}

			out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
					"Transitional//EN\">" +
					"<HTML>" +
					"<HEAD><BODY BGCOLOR=\"#FFFFFF\">" 
					+ "<STYLE TYPE=\"text/css\">P.breakhere {page-break-before: always}</STYLE>"
					+ "</HEAD>"
			);

			//Retrieve information
			Connection conn = clsDatabaseFunctions.getConnection(
					getServletContext(), 
					(String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID),
					"MySQL",
					this.toString() + ".doGet - User: " 
					+ sUserID
					+ " - "
					+ sUserFullName
					
					);
			if (conn == null){
				sWarning = "Unable to get data connection.";
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);			
				return;
			}
			//System.out.println("In " + this.toString() + ".main - going in to temporary tables");
			if(!createTemporaryTables(
					conn, 
					sStartingCustomer, 
					sEndingCustomer, 
					clsDateAndTimeConversions.utilDateToString(datAgeAsOf, "yyyy-MM-dd"),
					clsDateAndTimeConversions.utilDateToString(datCutOffDate, "yyyy-MM-dd"),
					Integer.toString(iCurrent),
					Integer.toString(i1st),
					Integer.toString(i2nd),
					Integer.toString(i3rd),
					sAgedBy,
					bPrintOnlyOverCurrent,
					bPrintZeroBalanceStatements,
					bPrintOnlyRequireStatement
			)){
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067570]");
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);			
				return;
			}
			//System.out.println("In " + this.toString() + ".main - created temporary tables");
			String sSQL = "SELECT * FROM " + sTempTableName
				+ " WHERE ("
						+ "(dapplytodoccurrentamt != 0.00)"
						//Need to pick up any 'zero balance' records, too, if there are any:
						+ " OR (ldocid = -1)"
					+ ")"
				+ " ORDER BY scustomer, lappliedto, ssource, datdocdate";
			ResultSet rsBalanceList = clsDatabaseFunctions.openResultSet(sSQL, conn);

			String sCurrentCustomer = "";
			BigDecimal bdCreditLimit = new BigDecimal(0);
			//System.out.println("In " + this.toString() + ".main - looping through rsBalanceList");
			long lNumberOfRecords = 0;
			while (rsBalanceList.next()){
				lNumberOfRecords++;
				//Print the header for any new customer:
				if (rsBalanceList.getString("scustomer").compareToIgnoreCase(sCurrentCustomer) != 0){

					if (sCurrentCustomer.compareToIgnoreCase("") != 0){
						//Print the footer, if the record is for a new customer:
						printCustomerFooter(out, rsBalanceList.getBigDecimal("dcreditlimit"));
					}

					//Print the customer header:
					if(!printCompanyInformationTable(
							out, 
							conn, 
							rsBalanceList.getString("scustomer"),
							clsDateAndTimeConversions.utilDateToString(datCutOffDate, "MM/dd/yyyy")
					)){
						rsBalanceList.close();
						clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067571]");
						sWarning = "Error printing customer information table.";
						response.sendRedirect(
								"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
								+ "Warning=" + sWarning
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						);			
						return;
					}
					//Reset:
					sCurrentCustomer = rsBalanceList.getString("scustomer");

					//Print the table heading for the transactions:
					out.println("<TABLE BORDER=0 WIDTH=100%>");
					out.println("<TR>");
					out.println(

							"<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=15% bgcolor=\"black\"><B><FONT SIZE=2 color=\"white\">Applied to</FONT></B></TD>"
							+ "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=3% bgcolor=\"black\"><B><FONT SIZE=2 color=\"white\">Type</FONT></B></TD>"
							+ "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=15% bgcolor=\"black\"><B><FONT SIZE=2 color=\"white\">Doc #</FONT></B></TD>"
							+ "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=10% bgcolor=\"black\"><B><FONT SIZE=2 color=\"white\">Doc ID</FONT></B></TD>"
							+ "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=8% bgcolor=\"black\"><B><FONT SIZE=2 color=\"white\">Doc. Date</FONT></B></TD>" 
							+ "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=8% bgcolor=\"black\"><B><FONT SIZE=2 color=\"white\">Due Date</FONT></B></TD>"
							+ "<TD ALIGN=RIGHT VALIGN=BOTTOM WIDTH=12% bgcolor=\"black\"><B><FONT SIZE=2 color=\"white\">Amount</FONT></B></TD>"
					);
					out.println("</TR>");
				}
				//if (iPrintTransactionsIn == 0){
				//Print the table for the transactions:
				//LTO 2009-05-08
				//If it's a 'zero balance' customer, just print these values:
				if (rsBalanceList.getLong("ldocid") == -1){
					out.println("<TR>");
					out.println("<TD ALIGN=LEFT COLSPAN=7><FONT SIZE=2>" + "(No open transactions for this customer in this date range)" + "</FONT></TD>");
					out.println("</TR>");
				}else{
					//ONLY SELECTED TYPES ARE PRINTED OUT, BUT CALCULATION STAYS THE SAME
					if (CheckDocType(rsBalanceList.getInt("idoctype"), alDocTypes)){
						out.println("<TR>");
						out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + rsBalanceList.getString("sdocappliedto") + "</FONT></TD>");
						out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + getDocumentTypeLabel(rsBalanceList.getInt("idoctype")) + "</FONT></TD>");
						out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + rsBalanceList.getString("sdocnumber") + "</FONT></TD>");
						out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + Long.toString(rsBalanceList.getLong("ldocid")) + "</FONT></TD>");
						String sDate = "N/A";
						if(clsDateAndTimeConversions.IsValidDateString("yyyy-MM-dd", rsBalanceList.getString("datdocdate"))){
							sDate = clsDateAndTimeConversions.utilDateToString(rsBalanceList.getDate("datdocdate"), "MM/dd/yyyy");
						}
						out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + sDate + "</FONT></TD>");
						if(clsDateAndTimeConversions.IsValidDateString("yyyy-MM-dd", rsBalanceList.getString("datduedate"))){
							sDate = clsDateAndTimeConversions.utilDateToString(rsBalanceList.getDate("datduedate"), "MM/dd/yyyy");
						}
						out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + sDate + "</FONT></TD>");
						out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsBalanceList.getBigDecimal("doriginalamt")) + "</FONT></TD>");
						out.println("</TR>");

						//Set the totals:
						dTotalCurrent = dTotalCurrent.add(rsBalanceList.getBigDecimal("dagingcolumncurrent"));
						dTotal1st = dTotal1st.add(rsBalanceList.getBigDecimal("dagingcolumnfirst"));
						dTotal2nd = dTotal2nd.add(rsBalanceList.getBigDecimal("dagingcolumnsecond"));
						dTotal3rd = dTotal3rd.add(rsBalanceList.getBigDecimal("dagingcolumnthird"));
						dTotalOver = dTotalOver.add(rsBalanceList.getBigDecimal("dagingcolumnover"));
					}
					//Save this value for the last customer footer . . . .
					bdCreditLimit = rsBalanceList.getBigDecimal("dcreditlimit");
				}

			}
			rsBalanceList.close();

			//Print the footer for the last customer, if at least one customer was found:
			if (sCurrentCustomer.compareToIgnoreCase("") != 0){
				printCustomerFooter(out, bdCreditLimit);
			}

			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067572]");

			out.println("</TABLE>");

			//If there were NO records, we still want to print a statement with zeroes:
			if (lNumberOfRecords == 0){
				out.println("(No transactions to be listed in the ranges selected.)<BR>");
			}
			
			SMLogEntry log = new SMLogEntry(conn);
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARPRINTSTATEMENT, "REPORT", "AR Print Statements", "[1376509287]");

		} catch (Exception ex) {
			// handle any errors
			out.println("<BR><BR>Error!!<BR>");
			out.println("Exception: " + ex.toString() + "<BR>");
		}

		out.println("</BODY></HTML>");
	}
	private boolean printCompanyInformationTable(
			PrintWriter pwOut, 
			Connection conn, 
			String sCustomerNumber,
			String sStatementDate
	){

		pwOut.println("<TABLE BORDER=0 WIDTH=100%>");

		//Print the company name cell:
		pwOut.println("<TR><TD style=\"vertical-align:top; text-align:center; width:33%; align:left; \">");
		//+ "<SPAN style=\"color:black; font-size:large; font-weight:bolder\">"
		//+ "STATEMENT"
		//+ "</SPAN>"
		//+ "</TD></TR>"
		//);
		String SQL = "SELECT * FROM " + SMTablecompanyprofile.TableName;
		try{
			ResultSet rsCompanyProfile = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rsCompanyProfile.next()){
				String sCompanyName = processStringForNull(
						rsCompanyProfile.getString(SMTablecompanyprofile.sCompanyName)).trim();
				pwOut.println(
						"<SPAN style=\"color:black; font-size:large; font-weight:bolder\">"
						+ sCompanyName + "<BR>"
						+ "</SPAN>"		
				);
				int LineCounter = 0;
				String sAddress1 = processStringForNull(
						rsCompanyProfile.getString(SMTablecompanyprofile.sAddress01)).trim();
				if(sAddress1.compareToIgnoreCase("") !=0){
					pwOut.println(rsCompanyProfile.getString(SMTablecompanyprofile.sAddress01) + "<BR>");
					LineCounter++;
				}
				String sAddress2 = processStringForNull(
						rsCompanyProfile.getString(SMTablecompanyprofile.sAddress02)).trim();
				if(sAddress2.compareToIgnoreCase("") !=0){
					pwOut.println(rsCompanyProfile.getString(SMTablecompanyprofile.sAddress02) + "<BR>");
					LineCounter++;
				}
				String sAddress3 = processStringForNull(
						rsCompanyProfile.getString(SMTablecompanyprofile.sAddress03)).trim();
				if(sAddress3.compareToIgnoreCase("") !=0){
					pwOut.println(rsCompanyProfile.getString(SMTablecompanyprofile.sAddress03) + "<BR>");
					LineCounter++;
				}
				String sAddress4 = processStringForNull(
						rsCompanyProfile.getString(SMTablecompanyprofile.sAddress04)).trim();
				if(sAddress4.compareToIgnoreCase("") !=0){
					pwOut.println(rsCompanyProfile.getString(SMTablecompanyprofile.sAddress04) + "<BR>");
					LineCounter++;
				}
				String sCity = processStringForNull(rsCompanyProfile.getString(SMTablecompanyprofile.sCity)).trim();
				String sState = processStringForNull(rsCompanyProfile.getString(SMTablecompanyprofile.sState)).trim();
				String sZip = processStringForNull(rsCompanyProfile.getString(SMTablecompanyprofile.sZipCode)).trim();
				String sCityStateZip = "";
				if(sCity.compareToIgnoreCase("") != 0){
					sCityStateZip = sCity;
				}
				if(sState.compareToIgnoreCase("") != 0){
					sCityStateZip = sCityStateZip + ", " + sState;
				}
				if(sZip.compareToIgnoreCase("") != 0){
					sCityStateZip = sCityStateZip + " " + sZip;
				}
				if(sCityStateZip.compareToIgnoreCase("") !=0){
					pwOut.println(sCityStateZip);
				}

				for (int i = LineCounter; i<5;i++){
					pwOut.println("<BR>");
				}
				pwOut.println("<BR>");
			}
			rsCompanyProfile.close();

		}catch(SQLException e){
			sWarning = "Error reading company/customer information - " + e.getMessage();
			return false;
		}
		pwOut.println("</TD>");

		//Print the 'STATEMENT' line:
		//pwOut.println("<TR><TD ALIGN=CENTER WIDTH=100% bgcolor=\"black\"><B><font size=\"3\" color=\"white\">STATEMENT</font></B></TD></TR>");
		pwOut.println("<TD style=\"vertical-align:top; text-align:center; width:33%; align:center\">"
				+ "<SPAN style=\"background-color:black; color:white; font-size:large; font-weight:bolder\">"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
				+ "STATEMENT"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
				+ "</SPAN>"
				+ "</TD>"
		);

		//Print the account/date table:
		pwOut.println("<TD style=\"vertical-align:top; width:33%; test-align:center\">");
		pwOut.println(
				"<B>CUSTOMER ACCOUNT:  </B>" + sCustomerNumber 
				+ "<BR>"
				+ "<B>STATEMENT DATE:  </B>" + sStatementDate 
				+ "<BR>"
		);
		pwOut.println("</TD>");

		pwOut.println("</TR>");
		pwOut.println("</TABLE>");
		//End the STATEMENT line

		//Print a table to contain the other tables (COMPANY INFO TABLE):
		printHeaderTable(pwOut, conn, sCustomerNumber, sStatementDate);

		return true;
	}
	private boolean printHeaderTable(
			PrintWriter pwOut,
			Connection conn,
			String sCustomerNumber, 
			String sStatementDate
	){

		//Print the 'header' table - this will contain the SOLD TO and REMIT TO tables within it:
		pwOut.println("<TABLE style=\" width:100%\">");
		pwOut.println("<TR>");

		//Print the sold to table:
		pwOut.println("<TD style=\"vertical-align:top; width:50%; text-align=left\">");
		if (!printSoldToTable(pwOut, conn, sCustomerNumber, sStatementDate)){
			return false;
		}
		pwOut.println("</TD>");

		//Print the REMIT TO table:
		pwOut.println("<TD style=\"vertical-align:top; width:50%; text-align=left\">");
		if (!printRemitToTable(pwOut, conn, sCustomerNumber, sStatementDate)){
			return false;
		}
		pwOut.println("</TD>");
		pwOut.println("</TR>");
		//End the company information table itself (COMPANY INFO TABLE):

		pwOut.println("</TABLE>");
		return true;

	}
	private boolean printSoldToTable(
			PrintWriter pwOut,
			Connection conn,
			String sCustomerNumber, 
			String sStatementDate
	){

		pwOut.println("<B>BILL TO:</B><BR><BR>");

		String SQL = "SELECT * FROM " + SMTablearcustomer.TableName
		+ " WHERE (" + SMTablearcustomer.sCustomerNumber + " = '" + sCustomerNumber + "')"
		;

		try{
			ResultSet rsCustomer = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rsCustomer.next()){
				pwOut.println("<B>" + rsCustomer.getString(SMTablearcustomer.sCustomerName) + "</B><BR>");
				int LineCounter = 0;
				String sAddress1 = rsCustomer.getString(SMTablearcustomer.sAddressLine1).trim();
				if(sAddress1.compareToIgnoreCase("") !=0){
					pwOut.println(rsCustomer.getString(SMTablearcustomer.sAddressLine1) + "<BR>");
					LineCounter++;
				}
				String sAddress2 = rsCustomer.getString(SMTablearcustomer.sAddressLine2).trim();
				if(sAddress2.compareToIgnoreCase("") !=0){
					pwOut.println(rsCustomer.getString(SMTablearcustomer.sAddressLine2) + "<BR>");
					LineCounter++;
				}
				String sAddress3 = rsCustomer.getString(SMTablearcustomer.sAddressLine3).trim();
				if(sAddress3.compareToIgnoreCase("") !=0){
					pwOut.println(rsCustomer.getString(SMTablearcustomer.sAddressLine3) + "<BR>");
					LineCounter++;
				}
				String sAddress4 = rsCustomer.getString(SMTablearcustomer.sAddressLine4).trim();
				if(sAddress4.compareToIgnoreCase("") !=0){
					pwOut.println(rsCustomer.getString(SMTablearcustomer.sAddressLine4) + "<BR>");
					LineCounter++;
				}
				String sCity = rsCustomer.getString(SMTablearcustomer.sCity).trim();
				String sState = rsCustomer.getString(SMTablearcustomer.sState).trim();
				String sZip = rsCustomer.getString(SMTablearcustomer.sPostalCode).trim();
				String sCityStateZip = "";
				if(sCity.compareToIgnoreCase("") != 0){
					sCityStateZip = sCity;
				}
				if(sState.compareToIgnoreCase("") != 0){
					sCityStateZip = sCityStateZip + ", " + sState;
				}
				if(sZip.compareToIgnoreCase("") != 0){
					sCityStateZip = sCityStateZip + " " + sZip;
				}
				if(sCityStateZip.compareToIgnoreCase("") !=0){
					pwOut.println(sCityStateZip);
				}

				for (int i = LineCounter; i<5;i++){
					pwOut.println("<BR>");
				}
			}
			rsCustomer.close();

		}catch(SQLException e){
			sWarning = "Error reading company/customer information - " + e.getMessage();
			return false;
		}

		return true;
	}

	private boolean printRemitToTable(
			PrintWriter pwOut,
			Connection conn,
			String sCustomerNumber, 
			String sStatementDate
	){

		//REMIT TO:
		//Begin the REMIT TO table:
		pwOut.println("<B>REMIT TO:</B><BR><BR>");
		String SQL = "SELECT * FROM " + SMTablecompanyprofile.TableName;
		try{
			ResultSet rsCompanyProfile = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rsCompanyProfile.next()){
				String sCompanyName = processStringForNull(
						rsCompanyProfile.getString(SMTablecompanyprofile.sCompanyName)).trim();
				pwOut.println("<B>" + sCompanyName + "</B><BR>");
				int LineCounter = 0;
				String sAddress1 = processStringForNull(
						rsCompanyProfile.getString(SMTablecompanyprofile.sAddress01)).trim();
				if(sAddress1.compareToIgnoreCase("") !=0){
					pwOut.println(rsCompanyProfile.getString(SMTablecompanyprofile.sAddress01) + "<BR>");
					LineCounter++;
				}
				String sAddress2 = processStringForNull(
						rsCompanyProfile.getString(SMTablecompanyprofile.sAddress02)).trim();
				if(sAddress2.compareToIgnoreCase("") !=0){
					pwOut.println(rsCompanyProfile.getString(SMTablecompanyprofile.sAddress02) + "<BR>");
					LineCounter++;
				}
				String sAddress3 = processStringForNull(
						rsCompanyProfile.getString(SMTablecompanyprofile.sAddress03)).trim();
				if(sAddress3.compareToIgnoreCase("") !=0){
					pwOut.println(rsCompanyProfile.getString(SMTablecompanyprofile.sAddress03) + "<BR>");
					LineCounter++;
				}
				String sAddress4 = processStringForNull(
						rsCompanyProfile.getString(SMTablecompanyprofile.sAddress04)).trim();
				if(sAddress4.compareToIgnoreCase("") !=0){
					pwOut.println(rsCompanyProfile.getString(SMTablecompanyprofile.sAddress04) + "<BR>");
					LineCounter++;
				}
				String sCity = processStringForNull(rsCompanyProfile.getString(SMTablecompanyprofile.sCity)).trim();
				String sState = processStringForNull(rsCompanyProfile.getString(SMTablecompanyprofile.sState)).trim();
				String sZip = processStringForNull(rsCompanyProfile.getString(SMTablecompanyprofile.sZipCode)).trim();
				String sCityStateZip = "";
				if(sCity.compareToIgnoreCase("") != 0){
					sCityStateZip = sCity;
				}
				if(sState.compareToIgnoreCase("") != 0){
					sCityStateZip = sCityStateZip + ", " + sState;
				}
				if(sZip.compareToIgnoreCase("") != 0){
					sCityStateZip = sCityStateZip + " " + sZip;
				}
				if(sCityStateZip.compareToIgnoreCase("") !=0){
					pwOut.println(sCityStateZip);
				}

				for (int i = LineCounter; i<5;i++){
					pwOut.println("<BR>");
				}
			}
			rsCompanyProfile.close();

		}catch(SQLException e){
			sWarning = "Error reading company/customer information - " + e.getMessage();
			return false;
		}

		//End the REMIT TO table:

		return true;
	}
	private void printCustomerFooter(
			PrintWriter out,
			BigDecimal bdCreditLimit){
		//End the transactions table:
		out.println("</TABLE>");

		//Start the table with the credit limit and totals:
		BigDecimal bdCustomerTotal = new BigDecimal(0);
		BigDecimal bdCreditAvailable = new BigDecimal(0);
		bdCustomerTotal = dTotalCurrent.add(dTotal1st).add(dTotal2nd).add(dTotal3rd).add(dTotalOver);
		bdCreditAvailable = bdCreditLimit.subtract(bdCustomerTotal);

		if(bdCreditAvailable.compareTo(BigDecimal.ZERO) == -1){
			bdCreditAvailable = BigDecimal.ZERO;
		}
		out.println("<TABLE BORDER=1 WIDTH=100%>");
		out.println("<TR>");
		out.println("<TD ALIGN=RIGHT VALIGN=TOP WIDTH=55%><B><FONT SIZE=2>Credit limit:"
				+ "<BR>Credit Available:</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT VALIGN=TOP WIDTH=12%><FONT SIZE=2>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCreditLimit)
				+ "<BR>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
						bdCreditAvailable)
						+ "</FONT></TD>");

		out.println("<TD ALIGN=RIGHT VALIGN=TOP WIDTH=21%><B><FONT SIZE=2>TOTAL:</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT VALIGN=TOP WIDTH=12%><FONT SIZE=2>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCustomerTotal)
				+ "</FONT></TD>");

		//End the credit limit and total table
		out.println("</TABLE>");

		//Add a table for the document type legend:
		//Print the legends:
		out.println("<TABLE BORDER=0 WIDTH=100%>");
		out.println("<TR>");
		for (int i = 0;i <= 9; i++){
			out.println("<TD><FONT SIZE=2><I>" + ARDocumentTypes.Get_Document_Type_Label(i) + " = " + getDocumentTypeLabel(i) + "</I></FONT></TD>");
		}
		out.println("</TR>");
		out.println("</TABLE>");

		//Create the aging table:
		out.println("<TABLE BORDER=1 WIDTH=100%>");

		out.println("<TR>");
		out.println("<TD ALIGN=CENTER>" + (iCurrent + 1) + " to " + (i1st) + " DAYS OVERDUE</TD>");
		out.println("<TD ALIGN=CENTER>" + (i1st + 1) + " to " + i2nd + " DAYS OVERDUE</TD>");
		out.println("<TD ALIGN=CENTER>" + (i2nd + 1) + " to " + i3rd + " DAYS OVERDUE</TD>");
		out.println("<TD ALIGN=CENTER>OVER " + i3rd + " DAYS OVERDUE</TD>");
		out.println("</TR>");

		out.println("<TR>");
		out.println("<TD ALIGN=CENTER>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTotal1st) + "</TD>");
		out.println("<TD ALIGN=CENTER>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTotal2nd) + "</TD>");
		out.println("<TD ALIGN=CENTER>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTotal3rd) + "</TD>");
		out.println("<TD ALIGN=CENTER>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTotalOver) + "</TD>");
		out.println("</TR>");

		//End the aging table:
		out.println("</TABLE>");

		//Page Break:
		out.println("<P CLASS=\"breakhere\">");

		//Reset the customer totals:
		dTotalCurrent = BigDecimal.ZERO;
		dTotal1st = BigDecimal.ZERO;
		dTotal2nd = BigDecimal.ZERO;
		dTotal3rd = BigDecimal.ZERO;
		dTotalOver = BigDecimal.ZERO;

		return;

	}
	private boolean createTemporaryTables(
			Connection conn, 
			String sStartingCustomer, 
			String sEndingCustomer,
			String sAgedAsOfDate,
			String sCutOffDate,
			String sCurrentAgingColumn,
			String sFirstAgingColumn,
			String sSecondAgingColumn,
			String sThirdAgingColumn,
			String sAgedBy,
			boolean bPrintOnlyOverCurrent,
			boolean bIncludeZeroBalanceStatements,
			boolean bPrintOnlyRequireStatement
	){

		sTempTableName = "ARSTATMENTTMP" + Long.toString(System.currentTimeMillis());
		String SQL;

		try{
			SQL = ARSQLs.Drop_Temporary_Aging_Table(sTempTableName);
			try {
				if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
					//System.out.println("Error dropping temporary aging table");
					//sWarning = "Error dropping temporary aging table";
					//return false;
				}
			} catch (SQLException e) {
				// Don't choke over this
			}
			SQL = ARSQLs.Create_Temporary_Aging_Table(sTempTableName, true);
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				//System.out.println("Error creating temporary aging table");
				sWarning = "Error creating temporary aging table";
				return false;
			}
			//Insert the transactions:
			SQL = "INSERT INTO " + sTempTableName + " ("
				+ "scustomer,"
				+ " ldocid,"
				+ " idoctype,"
				+ " sdocnumber,"
				+ " datdocdate,"
				+ " datduedate,"
				+ " datapplytodate,"
				+ " doriginalamt,"
				+ " dcurrentamt,"
				+ " sordernumber,"
				+ " ssource,"
				+ " lappliedto,"
				+ " sdocappliedto,"
				+ " dapplytodoccurrentamt,"
				+ " lparenttransactionid,"
				+ " scustomername,"
				+ " dcreditlimit,"
				+ " dbalance"
				+ ") SELECT"
				+ " " + SMTableartransactions.spayeepayor
				+ ", " + SMTableartransactions.lid
				+ ", " + SMTableartransactions.idoctype
				+ ", " + SMTableartransactions.sdocnumber
				+ ", " + SMTableartransactions.datdocdate
				+ ", " + SMTableartransactions.datduedate;

			//sAgedBy: '0' is 'Due Date', '1' is 'Doc date'
			if(sAgedBy.compareToIgnoreCase("1") == 0){
				SQL = SQL + ", " + SMTableartransactions.datdocdate;
			}else{
				SQL = SQL + ", " + SMTableartransactions.datduedate;
			}
			SQL = SQL + ", " + SMTableartransactions.doriginalamt
			+ ", " + SMTableartransactions.dcurrentamt
			+ ", " + SMTableartransactions.sordernumber
			+ ", 'CONTROL'"
			+ ", " + SMTableartransactions.lid
			+ ", " + SMTableartransactions.sdocnumber
			+ ", " + SMTableartransactions.dcurrentamt
			+ ", " + SMTableartransactions.lid
			+ ", " + SMTablearcustomer.sCustomerName
			+ ", " + SMTablearcustomer.dCreditLimit
			+ ", " + SMTablearcustomerstatistics.sCurrentBalance
			+ " FROM " + SMTableartransactions.TableName + ", " 
			+ SMTablearcustomer.TableName + ", " + SMTablearcustomerstatistics.TableName
			+ " WHERE ("
			+ "(" + SMTableartransactions.spayeepayor + ">='" + sStartingCustomer + "')"
			+ " AND (" + SMTableartransactions.spayeepayor + "<='" + sEndingCustomer + "')";

			//If the user chose to print ONLY customers over the current date, qualify that here:
			if (bPrintOnlyOverCurrent){
				SQL = SQL + " AND ((TO_DAYS('" + sAgedAsOfDate + "') - TO_DAYS(";
				if(sAgedBy.compareToIgnoreCase("1") == 0){
					SQL = SQL + SMTableartransactions.TableName + "." 
					+ SMTableartransactions.datdocdate;
				}else{
					SQL = SQL + SMTableartransactions.TableName + "." 
					+ SMTableartransactions.datduedate;
				}

				SQL += ")) > " + sCurrentAgingColumn + ")"
				;
			}
			
			//If the user chose to print ONLY customers require monthly statement, qualify that here:
			if (bPrintOnlyRequireStatement){
				SQL = SQL + " AND (" + SMTablearcustomer.TableName +"." + SMTablearcustomer.irequiresstatements + " = 1)";
			}
			
			// Not using retainage as a filter on statements:
			//if(sRetainageFlag.compareToIgnoreCase("") != 0){
			//	SQL = SQL + " AND (" + SMTableartransactions.iretainage + "=" + sRetainageFlag + ")";
			//}
			SQL = SQL + " AND (" + SMTableartransactions.datdocdate + "<='" + sCutOffDate + " 23:59:59')"
			+ " AND (" + SMTableartransactions.spayeepayor + " = " + SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerNumber + ")"
			+ " AND (" + SMTableartransactions.spayeepayor + " = " + SMTablearcustomerstatistics.TableName + "." + SMTablearcustomerstatistics.sCustomerNumber + ")";

			//Not including paid transactions:
			SQL = SQL + " AND (" + SMTableartransactions.dcurrentamt + " != 0.00)";
			SQL = SQL + ")"
			; 			

			//System.out.println("In " + this.toString() + ".createTempTables - insert SQL = " + SQL);
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				//System.out.println("Error inserting transactions into aging table");
				sWarning = "Error inserting transactions into aging table";
				return false;
			}

			//Insert the matching lines:
			SQL = "INSERT INTO " + sTempTableName + " ("
				+ "scustomer,"
				+ " ldocid,"
				+ " sdocnumber,"
				+ " datdocdate,"
				+ " datduedate,"
				+ " datapplytodate,"
				+ " doriginalamt,"
				+ " dcurrentamt,"
				+ " sordernumber,"
				+ " ssource,"
				+ " lappliedto,"
				+ " sdocappliedto,"
				+ " dapplytodoccurrentamt,"
				+ " lparenttransactionid,"
				+ " scustomername,"
				+ " dcreditlimit,"
				+ " dbalance"

				+ ") SELECT"
				+ " " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.spayeepayor
				+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.lid
				+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sdocnumber
				+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.dattransactiondate
				+ ", " + SMTableartransactions.TableName + "." + SMTableartransactions.datduedate;
			//sAgedBy: '0' is 'Due Date', '1' is 'Doc date'
			if(sAgedBy.compareToIgnoreCase("1") == 0){
				SQL = SQL + ", " + SMTableartransactions.datdocdate;
			}else{
				SQL = SQL + ", " + SMTableartransactions.datduedate;
			}
			//Applied amounts have the same sign as the apply-to amount, and so they must be negated:
			SQL = SQL + ", -1 * " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.damount
			+ ", -1 * " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.damount
			+ ", ''"
			+ ", 'DIST'"
			+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.ldocappliedtoid
			+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sapplytodoc
			+ ", " + SMTableartransactions.TableName + "." + SMTableartransactions.dcurrentamt
			+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.lparenttransactionid
			+ ", " + SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerName
			+ ", " + SMTablearcustomer.TableName + "." + SMTablearcustomer.dCreditLimit
			+ ", " + SMTablearcustomerstatistics.TableName + "." + SMTablearcustomerstatistics.sCurrentBalance
			+ " FROM " + SMTablearmatchingline.TableName 
			+ ", " + SMTableartransactions.TableName
			+ ", " + SMTablearcustomer.TableName
			+ ", " + SMTablearcustomerstatistics.TableName
			+ " WHERE ("
			+ "(" + SMTableartransactions.TableName + "." + SMTableartransactions.spayeepayor + ">='" 
			+ sStartingCustomer + "')"
			+ " AND (" + SMTableartransactions.TableName + "." + SMTableartransactions.spayeepayor + "<='" 
			+ sEndingCustomer + "')";
			//Ignoring retainage here:
			//if(sRetainageFlag.compareToIgnoreCase("") != 0){
			//	SQL = SQL + " AND (" + SMTableartransactions.TableName + "." 
			//	+ SMTableartransactions.iretainage + "=" + sRetainageFlag + ")";
			//}
			
			//If the user chose to print ONLY customers require monthly statement, qualify that here:
			if (bPrintOnlyRequireStatement){
				SQL = SQL + " AND (" + SMTablearcustomer.TableName +"." + SMTablearcustomer.irequiresstatements + " = 1)";
			}

			SQL = SQL + " AND (" + SMTablearmatchingline.dattransactiondate + "<='" + sCutOffDate + " 23:59:59')"

			//Link the tables:
			+ " AND (" + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.ldocappliedtoid + "="
			+ SMTableartransactions.TableName + "." + SMTableartransactions.lid + ")"
			+ " AND (" + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.spayeepayor + "="
			+ SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerNumber + ")"
			+ " AND (" + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.spayeepayor + "="
			+ SMTablearcustomerstatistics.TableName + "." + SMTablearcustomerstatistics.sCustomerNumber + ")"
			+ ")"
			;

			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				//System.out.println("Error inserting distribution lines into aging table");
				sWarning = "Error inserting distribution lines into aging table";
				return false;
			}

			//Insert_Parent_Document_Type_Into_Aging_Table
			SQL = ARSQLs.Update_Parent_Document_Type_In_Aging_Table(sTempTableName);
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				//System.out.println("Error updating parent document types into aging table");
				sWarning = "Error updating parent document types into aging table";
				return false;
			}

			//Update the aging columns on all lines, based on their 'due' dates: 
			//applied-to documents:
			SQL = ARSQLs.Update_AgingColumns_In_Aging_Table(
					sTempTableName,
					sAgedAsOfDate,
					sCurrentAgingColumn,
					sFirstAgingColumn, 
					sSecondAgingColumn, 
					sThirdAgingColumn);
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				//System.out.println("Error updating aging columns aging table");
				sWarning = "Error updating aging columns aging table";
				return false;
			}
		}catch(SQLException e){
			sWarning = "Unable to create temporary tables - " + e.getMessage();
			return false;
		}

		//Finally, if the user chooses to print zero balance statements, add 'dummy' lines for customers who have
		//no transactions in the list:
		if (bIncludeZeroBalanceStatements){
			//First, get a list of customers that are in the range, but have no transactions OR a zero-balance:
			SQL = "SELECT"
				+ " DISTINCT " + SMTablearcustomer.sCustomerNumber
				+ " FROM " + SMTablearcustomer.TableName + " LEFT JOIN " + sTempTableName + " ON"
				+ " " + SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerNumber + " ="
				+ " " + sTempTableName + ".scustomer"
				+ " WHERE ("
					+ "("
						+ "(" + sTempTableName + ".scustomer IS NULL)"
						+ " OR (" + sTempTableName + ".dbalance = 0.00)"  
					+ ")"
				+ " AND (" + SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerNumber + ">='" 
				+ sStartingCustomer + "')"
				+ " AND (" + SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerNumber + "<='" 
				+ sEndingCustomer + "')";

				//If the user chose to print ONLY customers require monthly statement, qualify that here:
				if (bPrintOnlyRequireStatement){
					SQL = SQL + " AND " + SMTablearcustomer.TableName +"." + SMTablearcustomer.irequiresstatements + " = 1";
				}
				
				SQL += ")";
				
			ArrayList <String>arrZeroBalanceCustomers = new ArrayList<String>(0);
			try {
				ResultSet rsNulls = clsDatabaseFunctions.openResultSet(SQL, conn);
				while (rsNulls.next()){
					arrZeroBalanceCustomers.add(rsNulls.getString(SMTablearcustomer.sCustomerNumber));
				}
				rsNulls.close();
			} catch (SQLException e) {
				sWarning = "Error getting zero value customers - " + e.getMessage();
				return false;
			}

			//Now, insert those 'zero balance' customers into the virtual lines:
			for (int i = 0; i < arrZeroBalanceCustomers.size(); i++){
				//System.out.println("In " + this.toString() 
				//		+ ".createTemporaryTables - arrZeroCust.get(" + i + ") = " 
				//		+ arrZeroBalanceCustomers.get(i));
				SQL = "INSERT INTO " + sTempTableName + "("
					+ "scustomer,"
					+ " ldocid,"
					+ " sdocnumber,"
					+ " datdocdate,"
					+ " datduedate,"
					+ " datapplytodate,"
					+ " doriginalamt,"
					+ " dcurrentamt,"
					+ " sordernumber,"
					+ " ssource,"
					+ " lappliedto,"
					+ " sdocappliedto,"
					+ " dapplytodoccurrentamt,"
					+ " lparenttransactionid,"
					+ " scustomername,"
					+ " dcreditlimit,"
					+ " dbalance"

					+ ") SELECT"
					+ " " + SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerNumber
					+ ", -1"
					+ ", ''"
					+ ", NOW()"
					+ ", NOW()"
					+ ", NOW()"
					+ ", 0.00"
					+ ", 0.00"
					+ ", ''"
					+ ", ''"
					+ ", -1"
					+ ", ''"
					+ ", 0.00"
					+ ", -1"
					+ ", " + SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerName
					+ ", " + SMTablearcustomer.TableName + "." + SMTablearcustomer.dCreditLimit
					+ ", 0.00"
					+ " FROM " + SMTablearcustomer.TableName
					+ " WHERE ("
					+ "(" + SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerNumber + " = '" + arrZeroBalanceCustomers.get(i) + "')"
					+ ")"
					;
				//System.out.println("In " + this.toString() 
				//		+ ".createTemporaryTables - insert zero record SQL = " + SQL);

				try {
					Statement stmt = conn.createStatement();
					stmt.executeUpdate(SQL);
				} catch (SQLException e) {
					sWarning = "Error inserting zero balance record with SQL: " + SQL + " - " + e.getMessage();
					return false;
				}
			}
		}
		return true;
	}
	private String getDocumentTypeLabel(int lDocType){

		switch (lDocType){
		//Invoice
		case 0: return "IN";
		//Credit
		case 1: return "CR";
		//Payment
		case 2: return "PY";
		//Prepay
		case 3: return "PI";
		//Reversal
		case 4: return "RV";
		//Invoice adjustment
		case 5: return "IA";
		//Misc Receipt
		case 6: return "MR";
		//Cash adjustment
		case 7: return "CA";
		//Credit adjustment:
		case 8: return "RA";
		//Retainage transaction:
		case 9: return "RT";

		default: return "IN";
		}
	}
	private String processStringForNull(String sSource){
		if (sSource == null){
			return "";
		}else{
			return sSource;
		}
	}
	private boolean CheckDocType(int iDocType, ArrayList <Integer>alDocTypes){
		for (int i=0;i<alDocTypes.size();i++){
			if (iDocType == Integer.parseInt(alDocTypes.get(i).toString())){
				return true;
			}
		}
		return false;
	}
}
