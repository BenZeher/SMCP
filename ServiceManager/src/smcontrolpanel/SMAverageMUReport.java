package smcontrolpanel;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.servlet.ServletContext;

import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTablechangeorders;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesalesperson;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsStringFunctions;

public class SMAverageMUReport extends java.lang.Object{

	private String m_sErrorMessage;

	public SMAverageMUReport(
	){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sStartingDate,
			String sEndingDate,
			ArrayList<String> arServiceTypes,
			String sGroupBy,
			boolean bSummaryOnly,
			boolean bPrintIndividual,
			String sIndividualSalesperson,
			String sDBID,
			PrintWriter out,
			ServletContext context
	){

		//Make sure at least one service type is included:
		if(arServiceTypes.size() < 1){
			m_sErrorMessage = "You must choose at least one order type.";
			return false;
		}

		//If only one order type is requested, we don't need to print totals for that type:
		boolean bShowServiceTypeTotals = false;
		if(arServiceTypes.size() > 1){
			bShowServiceTypeTotals = true;
		}

		//variables for total calculations
		//Need amount, mark up, truck days, and average MU/day:
		BigDecimal dServiceTypeAmount = new BigDecimal(0);
		BigDecimal dServiceTypeMU = new BigDecimal(0);
		BigDecimal dServiceTypeTruckDays = new BigDecimal(0);

		BigDecimal dSalespersonAmount = new BigDecimal(0);
		BigDecimal dSalespersonMU = new BigDecimal(0);
		BigDecimal dSalespersonTruckDays = new BigDecimal(0);

		BigDecimal dSourceAmount = new BigDecimal(0);
		BigDecimal dSourceMU = new BigDecimal(0);
		BigDecimal dSourceTruckDays = new BigDecimal(0);

		//variables for grand total calculations
		BigDecimal dGrandTotalAmount = new BigDecimal(0);
		BigDecimal dGrandTotalMU = new BigDecimal(0);
		BigDecimal dGrandTotalTruckDays = new BigDecimal(0);

		//print out the column headers.
		//Salesperson, date, job#, customer, amount, MU, truck days, avg MU/TD
		out.println("<TABLE BORDER=0 WIDTH=100%>");
		if(!bSummaryOnly){
			out.println("<TR>" + 
					"<TD ALIGN=LEFT VALIGN=BOTTOM><B><FONT SIZE=2>Salesperson</FONT></B></TD>" +
					"<TD ALIGN=LEFT VALIGN=BOTTOM><B><FONT SIZE=2>Date</FONT></B></TD>" +
					"<TD ALIGN=LEFT VALIGN=BOTTOM><B><FONT SIZE=2>Order #</FONT></B></TD>" +
			"<TD ALIGN=LEFT VALIGN=BOTTOM><B><FONT SIZE=2>Bill to</FONT></B></TD>");
		}else{
			out.println("<TR>" + 
					"<TD ALIGN=LEFT VALIGN=BOTTOM><B><FONT SIZE=2>&nbsp;</FONT></B></TD>" +
					"<TD ALIGN=LEFT VALIGN=BOTTOM><B><FONT SIZE=2>&nbsp;</FONT></B></TD>" +
					"<TD ALIGN=LEFT VALIGN=BOTTOM><B><FONT SIZE=2>&nbsp;</FONT></B></TD>" +
			"<TD ALIGN=LEFT VALIGN=BOTTOM><B><FONT SIZE=2>&nbsp;</FONT></B></TD>");
		}
		out.println(
				"<TD ALIGN=RIGHT VALIGN=BOTTOM><B><FONT SIZE=2>Amount</FONT></B></TD>" +
				"<TD ALIGN=RIGHT VALIGN=BOTTOM><B><FONT SIZE=2>Mark Up</FONT></B></TD>" +
				"<TD ALIGN=RIGHT VALIGN=BOTTOM><B><FONT SIZE=2>Truck Days</FONT></B></TD>" +
				"<TD ALIGN=RIGHT VALIGN=BOTTOM><B><FONT SIZE=2>Avg MU/day</FONT></B></TD>" +
				"</TR>" + 
		"<TR><TD COLSPAN=8><HR></TD></TR>");

		String sCurrentServiceType = "";
		String sCurrentSalesPerson = "";
		String sCurrentSource = "";	

		BigDecimal bdAmount = new BigDecimal(0);
		BigDecimal bdMarkUp = new BigDecimal(0);
		BigDecimal bdTruckDays = new BigDecimal(0);
		BigDecimal bdAvgMU = new BigDecimal(0);

		try {
			createTemporaryTable(
					conn, 
					sStartingDate, 
					sEndingDate, 
					arServiceTypes, 
					bPrintIndividual, 
					sIndividualSalesperson);
		} catch (SQLException e1) {
			m_sErrorMessage = "Error creating temporary table for report - " + e1.getMessage();
			return false;
		}

		String sOrderBy = "";
		if (sGroupBy.compareTo("Salesperson") == 0){
			sOrderBy = "Salesperson, ServiceTypeDesc, Source, DocDate";
		}else{
			sOrderBy = "ServiceTypeDesc, Salesperson, Source, DocDate";
		}
		String SQL = "SELECT * FROM AVGMU ORDER BY " + sOrderBy;

		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				//Print the header for any new source OR salesperson OR service type:
				if (
						(rs.getString("Source").compareToIgnoreCase(sCurrentSource) != 0)
						|| ((rs.getString("Salesperson") + " - " + rs.getString("SalespersonName")).compareToIgnoreCase(sCurrentSalesPerson) != 0)
						|| (rs.getString("ServiceTypeDesc").compareToIgnoreCase(sCurrentServiceType) != 0)
				){
					//Print the footer, if the record is for a new source:
					if (sCurrentSource.compareToIgnoreCase("") != 0){
						printSourceFooter(
								sCurrentServiceType, 
								sCurrentSalesPerson,
								sCurrentSource,
								dSourceAmount, 
								dSourceMU, 
								dSourceTruckDays,
								bShowServiceTypeTotals,
								out
						);

						//Reset the tax class totals:
						dSourceAmount = BigDecimal.ZERO;
						dSourceMU = BigDecimal.ZERO;
						dSourceTruckDays = BigDecimal.ZERO;
					}
				}
				//Depending on the 'Group By' variable, these next two groupings can print in one
				//order or another:
				if (sGroupBy.compareTo("Salesperson") == 0){
					//Print the header for any new COMBINATION of source and service type:
					if (
							((rs.getString("Salesperson") + " - " + rs.getString("SalespersonName")).compareToIgnoreCase(sCurrentSalesPerson) != 0)
							|| (rs.getString("ServiceTypeDesc").compareToIgnoreCase(sCurrentServiceType) != 0)
					){
						//Print the footer, if the record is for a new service type:
						if (sCurrentServiceType.compareToIgnoreCase("") != 0){
							printServiceTypeFooter(
									sCurrentServiceType,
									sCurrentSalesPerson,
									dServiceTypeAmount, 
									dServiceTypeMU, 
									dServiceTypeTruckDays, 
									bShowServiceTypeTotals,
									sGroupBy,
									out);

							//Reset the service type totals:
							dServiceTypeAmount = BigDecimal.ZERO;
							dServiceTypeMU = BigDecimal.ZERO;
							dServiceTypeTruckDays = BigDecimal.ZERO;
						}
					}
					//Print the header for any new salesperson:
					if ((rs.getString("Salesperson") + " - " + rs.getString("SalespersonName")).compareToIgnoreCase(sCurrentSalesPerson) != 0){
						//Print the footer, if the record is for a new salesperson:
						if (sCurrentSalesPerson.compareToIgnoreCase("") != 0){
							printSalespersonFooter(
									sCurrentServiceType, 
									sCurrentSalesPerson, 
									dSalespersonAmount, 
									dSalespersonMU, 
									dSalespersonTruckDays,
									bShowServiceTypeTotals,
									sGroupBy,
									out
							);

							//Reset the tax group totals:
							dSalespersonAmount = BigDecimal.ZERO;
							dSalespersonMU = BigDecimal.ZERO;
							dSalespersonTruckDays = BigDecimal.ZERO;
						}
					}
				}else{
					//Print the header for any new COMBINATION of source and service type:
					if (
							((rs.getString("Salesperson") + " - " + rs.getString("SalespersonName")).compareToIgnoreCase(sCurrentSalesPerson) != 0)
							|| (rs.getString("ServiceTypeDesc").compareToIgnoreCase(sCurrentServiceType) != 0)
					){
						//Print the footer, if the record is for a new salesperson:
						if (sCurrentSalesPerson.compareToIgnoreCase("") != 0){
							printSalespersonFooter(
									sCurrentServiceType, 
									sCurrentSalesPerson, 
									dSalespersonAmount, 
									dSalespersonMU, 
									dSalespersonTruckDays,
									bShowServiceTypeTotals,
									sGroupBy,
									out
							);

							//Reset the tax group totals:
							dSalespersonAmount = BigDecimal.ZERO;
							dSalespersonMU = BigDecimal.ZERO;
							dSalespersonTruckDays = BigDecimal.ZERO;
						}

					}
					//Print the header for any new service type:
					if (rs.getString("ServiceTypeDesc").compareToIgnoreCase(sCurrentServiceType) != 0){
						//Print the footer, if the record is for a new service type:
						if (sCurrentServiceType.compareToIgnoreCase("") != 0){
							printServiceTypeFooter(
									sCurrentServiceType,
									sCurrentSalesPerson,
									dServiceTypeAmount, 
									dServiceTypeMU, 
									dServiceTypeTruckDays, 
									bShowServiceTypeTotals,
									sGroupBy,
									out);

							//Reset the service type totals:
							dServiceTypeAmount = BigDecimal.ZERO;
							dServiceTypeMU = BigDecimal.ZERO;
							dServiceTypeTruckDays = BigDecimal.ZERO;
						}
					}
				}

				bdAmount = rs.getBigDecimal("Amount");
				bdMarkUp = rs.getBigDecimal("MarkUp");
				bdTruckDays = rs.getBigDecimal("TruckDays");
				bdAvgMU = rs.getBigDecimal("AvgMU");

				if(!bSummaryOnly){
					out.println("<TR>");
					out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString("Salesperson") + "</FONT></TD>");
					out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + clsDateAndTimeConversions.utilDateToString(rs.getDate("DocDate"),"MM/dd/yyyy") + "</FONT></TD>");
					out.println("<TD ALIGN=LEFT><FONT SIZE=2>" 
							+ "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
							+ rs.getString("OrderNumber") 
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
							+ "\">" + rs.getString("OrderNumber") + "</A>"
							+ "</FONT></TD>");
					out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString("Customer") + "</FONT></TD>");
					out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAmount) + "</FONT></TD>");
					out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdMarkUp) + "</FONT></TD>");
					out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTruckDays) + "</FONT></TD>");
					out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAvgMU) + "</FONT></TD>");
					out.println("</TR>");
				}
				//Set the totals:
				dSourceAmount = dSourceAmount.add(bdAmount);
				dSourceMU = dSourceMU.add(bdMarkUp);
				dSourceTruckDays = dSourceTruckDays.add(bdTruckDays);

				dSalespersonAmount = dSalespersonAmount.add(bdAmount);
				dSalespersonMU = dSalespersonMU.add(bdMarkUp);
				dSalespersonTruckDays = dSalespersonTruckDays.add(bdTruckDays);

				dServiceTypeAmount = dServiceTypeAmount.add(bdAmount);
				dServiceTypeMU = dServiceTypeMU.add(bdMarkUp);
				dServiceTypeTruckDays = dServiceTypeTruckDays.add(bdTruckDays);

				//Accumulate the grand totals:
				dGrandTotalAmount = dGrandTotalAmount.add(bdAmount);
				dGrandTotalMU = dGrandTotalMU.add(bdMarkUp);
				dGrandTotalTruckDays = dGrandTotalTruckDays.add(bdTruckDays);

				//Reset:
				sCurrentSalesPerson = rs.getString("Salesperson") + " - " + rs.getString("SalespersonName");
				sCurrentServiceType = rs.getString("ServiceTypeDesc");
				sCurrentSource = rs.getString("Source");
			}
			rs.close();

			//Print the last source totals, if at least one source was listed:
			if (sCurrentSource.compareToIgnoreCase("") != 0){
				printSourceFooter(
						sCurrentServiceType, 
						sCurrentSalesPerson, 
						sCurrentSource,
						dSourceAmount, 
						dSourceMU, 
						dSourceTruckDays,
						bShowServiceTypeTotals,
						out
				);
			}

			if (sGroupBy.compareTo("Salesperson") == 0){
				//Print the last service type totals, if at least one service type was listed:
				if (sCurrentServiceType.compareToIgnoreCase("") != 0){
					printServiceTypeFooter(
							sCurrentServiceType,
							sCurrentSalesPerson,
							dServiceTypeAmount, 
							dServiceTypeMU, 
							dServiceTypeTruckDays,
							bShowServiceTypeTotals,
							sGroupBy,
							out
					);
				}

				//Print the last salesperson totals, if at least one tax group was listed:
				if (sCurrentSalesPerson.compareToIgnoreCase("") != 0){
					printSalespersonFooter(
							sCurrentServiceType, 
							sCurrentSalesPerson, 
							dSalespersonAmount, 
							dSalespersonMU, 
							dSalespersonTruckDays,
							bShowServiceTypeTotals,
							sGroupBy,
							out
					);
				}
			}else{
				//Print the last salesperson totals, if at least one tax group was listed:
				if (sCurrentSalesPerson.compareToIgnoreCase("") != 0){
					printSalespersonFooter(
							sCurrentServiceType, 
							sCurrentSalesPerson, 
							dSalespersonAmount, 
							dSalespersonMU, 
							dSalespersonTruckDays,
							bShowServiceTypeTotals,
							sGroupBy,
							out
					);
				}

				//Print the last service type totals, if at least one service type was listed:
				if (sCurrentServiceType.compareToIgnoreCase("") != 0){
					printServiceTypeFooter(
							sCurrentServiceType,
							sCurrentSalesPerson,
							dServiceTypeAmount, 
							dServiceTypeMU, 
							dServiceTypeTruckDays,
							bShowServiceTypeTotals,
							sGroupBy,
							out
					);
				}
			}
			//Print the grand totals:
			out.println("<TD colspan=\"8\">&nbsp;</TD>");
			out.println("<TR>");
			out.println("<TD ALIGN=RIGHT colspan=\"4\"><B><FONT SIZE=2>Report totals:</FONT></B></TD>");
			out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotalAmount) + "</B></FONT></TD>");
			out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotalMU) + "</B></FONT></TD>");
			out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotalTruckDays) + "</B></FONT></TD>");

			MathContext mc=new MathContext(5, RoundingMode.HALF_EVEN);
			if(dGrandTotalTruckDays.compareTo(BigDecimal.ZERO) != 0){
				out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotalMU.divide(dGrandTotalTruckDays, mc)) + "</B></FONT></TD>");
			}else{
				out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>0.00</B></FONT></TD>");
			}
			out.println("</TR>");
			out.println("</TABLE>");

		}catch(SQLException e){
			System.out.println("Error in " + this.toString() + ":processReport - " + e.getMessage());
			m_sErrorMessage = "Error in " + this.toString() + ":processReport - " + e.getMessage();
			return false;
		}
		out.println("</TABLE>");
		return true;
	}
	private void printSourceFooter(
			String sCurrentServiceType, 
			String sCurrentSalesPerson,
			String sCurrentSource,
			BigDecimal dSourceAmount,
			BigDecimal dSourceMarkUp, 
			BigDecimal dSourceTruckDays,
			boolean bShowServiceTypeTotals,
			PrintWriter out
	){
		out.println("<TR>");
		out.println("<TD ALIGN=RIGHT colspan=\"4\"><B><FONT SIZE=2>Total for ");
		if (bShowServiceTypeTotals){
			out.println(sCurrentServiceType + ", ");
		}

		out.println("Salesperson " + sCurrentSalesPerson
				+ ", " + sCurrentSource
				+ ":</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dSourceAmount) + "</B></FONT></TD>");
		out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dSourceMarkUp) + "</B></FONT></TD>");
		out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dSourceTruckDays) + "</B></FONT></TD>");

		MathContext mc=new MathContext(5, RoundingMode.HALF_EVEN);
		if(dSourceTruckDays.compareTo(BigDecimal.ZERO) != 0){
			out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dSourceMarkUp.divide(dSourceTruckDays, mc)) + "</B></FONT></TD>");
		}else{
			out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>0.00</B></FONT></TD>");
		}

		out.println("</TR>");
		out.println("<TR></TR>");
	}
	private void printSalespersonFooter(
			String sCurrentServiceType, 
			String sCurrentSalesperson,
			BigDecimal dSalespersonAmount,
			BigDecimal dSalespersonMarkUp, 
			BigDecimal dSalespersonTruckDays,
			boolean bShowServiceTypeTotals,
			String sGroupBy,
			PrintWriter out
	){

		String sOutPut = "";
		sOutPut = "<TR>";
		sOutPut = sOutPut + "<TD ALIGN=RIGHT colspan=\"4\"><B><FONT SIZE=2>Total for ";

		if (sGroupBy.compareTo("Salesperson") == 0){
			sOutPut = sOutPut + "Salesperson " + sCurrentSalesperson;
		}else{
			if (bShowServiceTypeTotals){
				sOutPut = sOutPut + sCurrentServiceType + ", ";
			}
			sOutPut = sOutPut + "Salesperson " + sCurrentSalesperson;
		}

		sOutPut = sOutPut +  ":</FONT></B></TD>";

		out.println(sOutPut);
		out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dSalespersonAmount) + "</B></FONT></TD>");
		out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dSalespersonMarkUp) + "</B></FONT></TD>");
		out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dSalespersonTruckDays) + "</B></FONT></TD>");

		MathContext mc=new MathContext(5, RoundingMode.HALF_EVEN);
		if(dSalespersonTruckDays.compareTo(BigDecimal.ZERO) != 0){
			//out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
			//	+ dSalespersonMarkUp.divide(dSalespersonTruckDays) + "</FONT></TD>");
			out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dSalespersonMarkUp.divide(dSalespersonTruckDays, mc)) + "</B></FONT></TD>");
		}else{
			out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>0.00</B></FONT></TD>");
		}
		out.println("</TR>");
		out.println("<TR></TR>");
	}
	private void printServiceTypeFooter(
			String sCurrentServiceType,
			String sCurrentSalesperson,
			BigDecimal dServiceTypeAmount,
			BigDecimal dServiceTypeMarkUp, 
			BigDecimal dServiceTypeTruckDays,
			boolean bShowServiceType,
			String sGroupBy,
			PrintWriter out
	){
		if (!bShowServiceType){
			return;
		}
		out.println("<TR>");

		if (sGroupBy.compareTo("Salesperson") == 0){
			out.println("<TD ALIGN=RIGHT colspan=\"4\"><B><FONT SIZE=2>Total for salesperson " 
					+ sCurrentSalesperson + ", " + sCurrentServiceType + ":</FONT></B></TD>");
		}else{
			out.println("<TD ALIGN=RIGHT colspan=\"4\"><B><FONT SIZE=2>Total for " + sCurrentServiceType + ":</FONT></B></TD>");
		}

		out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dServiceTypeAmount) + "</B></FONT></TD>");
		out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dServiceTypeMarkUp) + "</B></FONT></TD>");
		out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dServiceTypeTruckDays) + "</B></FONT></TD>");
		MathContext mc=new MathContext(5, RoundingMode.HALF_EVEN);
		if(dServiceTypeTruckDays.compareTo(BigDecimal.ZERO) != 0){
			out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dServiceTypeMarkUp.divide(dServiceTypeTruckDays, mc)) + "</B></FONT></TD>");
		}else{
			out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>0.00</B></FONT></TD>");
		}
		out.println("</TR>");
		out.println("<TR></TR>");
	}
	private void createTemporaryTable(
			Connection conn, 
			String sStartDate, 
			String sEndDate,
			ArrayList<String> arServiceType,
			boolean bPrintIndividual,
			String sSalesPerson
	) throws SQLException {
		String SQL = "";

		//Drop the table if it's there already:
		SQL = "DROP TEMPORARY TABLE AVGMU";
		try{
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				//Don't stop here - maybe the table had never been created.
			}
		}catch (SQLException e){
			//Don't stop here - just keep going
			//m_sErrorMessage = "Error dropping temporary table - " + e.getMessage();
			//throw new SQLException(m_sErrorMessage);
		}

		//Create the table:
		SQL = "CREATE"
			+ " TEMPORARY"
			+ " TABLE AVGMU ("
			+ "Salesperson varchar(" + Integer.toString(SMTableorderheaders.sSalespersonLength) + ") NOT NULL DEFAULT ''"
			+ ", SalespersonName varchar(" 
			+ Integer.toString(
					SMTablesalesperson.sSalespersonFirstNameLength + 1 + SMTablesalesperson.sSalespersonLastNameLength)
					+ ") NOT NULL DEFAULT ''"
					+ ", ServiceType varchar (" + Integer.toString(SMTableorderheaders.sServiceTypeCodeLength) + ") NOT NULL DEFAULT ''"
					+ ", ServiceTypeDesc varchar (" + Integer.toString(SMTableorderheaders.sServiceTypeCodeDescriptionLength) + ") NOT NULL DEFAULT ''"
					+ ", DocDate date NOT NULL default '0000-00-00'"
					+ ", OrderNumber varchar(" + Integer.toString(SMTableorderheaders.sOrderNumberLength) + ") NOT NULL DEFAULT ''"
					+ ", Customer varchar (" + Integer.toString(SMTableorderheaders.sBillToNameLength) + ") NOT NULL DEFAULT ''"
					+ ", Amount DECIMAL (17,2) NOT NULL default '0.00'"
					+ ", MarkUp DECIMAL (17,2) NOT NULL default '0.00'"
					+ ", TruckDays DECIMAL (17,2) NOT NULL default '0.0000'"
					+ ", Source varchar(13) NOT NULL default ''"
					+ ", AvgMU DECIMAL (17,4) NOT NULL default '0.00'"
					+ ", KEY SalesPersonKey (Salesperson)"
					+ ", KEY ServiceTypeKey (ServiceType)"
					+ ") Engine=InnoDb"
					;
		//System.out.println("In " + this.toString() + ".createTemporaryTable - CREATE SQL = " + SQL);
		try{
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch (SQLException e){
			m_sErrorMessage = "Error creating temporary table - " + e.getMessage();
			throw new SQLException ("Error creating temporary table - " + e.getMessage());
		}

		//Insert lines from the orders table:
		SQL = "INSERT INTO AVGMU ("
			+ "Salesperson"
			+ ", SalespersonName"
			+ ", ServiceType"
			+ ", ServiceTypeDesc"
			+ ", DocDate"
			+ ", OrderNumber"
			+ ", Customer"
			+ ", Amount"
			+ ", MarkUp"
			+ ", TruckDays"
			+ ", Source"
			+ ", AvgMU"
			+ ") SELECT"
			//+ " " + SMTableorderheaders.sSalesperson
			
		    + " IF(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson + " = '', "
			+ "'" + SMOrderHeader.UNLISTEDSALESPERSON_MARKER + "', " 
			+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson
			+ ") AS " + SMTableorderheaders.sSalesperson
			
			+ ", CONCAT(" + SMTablesalesperson.sSalespersonFirstName + ", ' ', " + SMTablesalesperson.sSalespersonLastName + ")"
			+ ", " + SMTableorderheaders.sServiceTypeCode
			+ ", " + SMTableorderheaders.sServiceTypeCodeDescription
			+ ", " + SMTableorderheaders.datOrderDate
			+ ", TRIM(" + SMTableorderheaders.sOrderNumber + ")"
			+ ", " + SMTableorderheaders.sBillToName
			+ ", " + SMTableorderheaders.bdtotalcontractamount
			+ ", " + SMTableorderheaders.bdtotalmarkup
			+ ", " + SMTableorderheaders.bdtruckdays
			+ ", " + "'New Orders'"
			+ ", " + "0.00"
			+ " FROM " + SMTableorderheaders.TableName + ", " 
			+ SMTablesalesperson.TableName
			+ " WHERE ("
			+ "(" + SMTableorderheaders.datOrderDate + ">='" + sStartDate + "')"
			+ " AND (" + SMTableorderheaders.iOrderType + " != " + SMTableorderheaders.ORDERTYPE_QUOTE + ")"
			;

		if (bPrintIndividual){
			SQL +=
				" AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson 
				+ "='" + sSalesPerson + "')";
		}
		SQL += " AND (" + SMTableorderheaders.datOrderDate + "<='" + sEndDate + "')"
		+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson 
			+ " = " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode + ")"
		+ " AND (";

		for(int i = 0; i < arServiceType.size(); i++){
			SQL = SQL + "(" + SMTableorderheaders.sServiceTypeCode + " = '" + arServiceType.get(i) + "') OR";
		}

		//Trim off the last 'OR':
		SQL = clsStringFunctions.StringLeft(SQL, SQL.length() - " OR".length());

		SQL = SQL + ")"

		+ ")"
		;
		//System.out.println("In " + this.toString() + ".createTemporaryTable - 1st INSERT SQL = " + SQL);
		try{
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch (SQLException e){
			m_sErrorMessage = "Error inserting project lines - " + e.getMessage();
			throw new SQLException ("Error inserting project lines - " + e.getMessage());
		}		
		//Insert lines from the change orders table:
		SQL = "INSERT INTO AVGMU ("
			+ "Salesperson"
			+ ", SalespersonName"
			+ ", ServiceType"
			+ ", ServiceTypeDesc"
			+ ", DocDate"
			+ ", OrderNumber"
			+ ", Customer"
			+ ", Amount"
			+ ", MarkUp"
			+ ", TruckDays"
			+ ", Source"
			+ ", AvgMU"
			+ ") SELECT"
			//+ " " + SMTableorderheaders.sSalesperson
			
		    + " IF(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson + " = '', "
			+ "'" + SMOrderHeader.UNLISTEDSALESPERSON_MARKER + "', " 
			+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson
			+ ") AS " + SMTableorderheaders.sSalesperson
			
			+ ", CONCAT(" + SMTablesalesperson.sSalespersonFirstName + ", ' ', " + SMTablesalesperson.sSalespersonLastName + ")"
			+ ", " + SMTableorderheaders.sServiceTypeCode
			+ ", " + SMTableorderheaders.sServiceTypeCodeDescription
			+ ", " + SMTablechangeorders.datChangeOrderDate
			+ ", TRIM(" + SMTableorderheaders.sOrderNumber + ")"
			+ ", " + SMTableorderheaders.sBillToName
			+ ", " + SMTablechangeorders.dAmount
			+ ", " + SMTablechangeorders.dTotalMarkUp
			+ ", " + SMTablechangeorders.dTruckDays
			+ ", " + "'Change Orders'"
			+ ", " + "0.00"
			+ " FROM " + SMTableorderheaders.TableName + ", " 
			+ SMTablechangeorders.TableName + ", " 
			+ SMTablesalesperson.TableName
			+ " WHERE ("
			+ "(" + SMTablechangeorders.datChangeOrderDate + ">='" + sStartDate + "')"
			+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + " != " 
			+ SMTableorderheaders.ORDERTYPE_QUOTE + ")"
			;

		if (bPrintIndividual){
			SQL +=
				" AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson 
				+ "='" + sSalesPerson + "')";
		}
		SQL += " AND (" + SMTablechangeorders.datChangeOrderDate + "<='" + sEndDate + "')"
			+ " AND (" + SMTablechangeorders.TableName + "." + SMTablechangeorders.sJobNumber
				+ " = " + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber + ")"
			+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson 
				+ " = " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode + ")"
			+ " AND (";

		for(int i = 0; i < arServiceType.size(); i++){
			SQL = SQL + "(" + SMTableorderheaders.sServiceTypeCode + " = '" + arServiceType.get(i) + "') OR";
		}

		//Trim off the last 'OR':
		SQL = clsStringFunctions.StringLeft(SQL, SQL.length() - " OR".length());

		SQL = SQL + ")"
		+ ")"
		;
		//System.out.println("In " + this.toString() + ".createTemporaryTable - 2nd INSERT SQL = " + SQL);
		try{
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch (SQLException e){
			m_sErrorMessage = "Error inserting change order lines - " + e.getMessage();
			throw new SQLException ("Error inserting change order lines - " + e.getMessage());
		}		
		//Set the Average MU per truck day:
		SQL = "UPDATE AVGMU SET AvgMU = IF(TruckDays != 0.0000, MarkUp / Truckdays, 0.00)";
		;
		try{
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch (SQLException e){
			m_sErrorMessage = "Error inserting avg mu for report - " + e.getMessage();
			throw new SQLException ("Error inserting avg mu for report - " + e.getMessage());
		}
		//Eliminate any zero amounts:
		SQL = "DELETE FROM AVGMU WHERE Amount = 0.00";
		;
		try{
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch (SQLException e){
			m_sErrorMessage = "Error deleting zero amounts for report - " + e.getMessage();
			throw new SQLException ("Error deleting zero amounts for report - " + e.getMessage());
		}

		return;
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
