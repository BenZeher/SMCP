package smcontrolpanel;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesalesperson;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;

public class SMSalesEffortCheckReport extends java.lang.Object{

	private String m_sErrorMessage;
	
	public SMSalesEffortCheckReport(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sStartingDate,
			String sEndingDate,
			boolean bIncludeSales,
			boolean bIncludeService,
			boolean bShowIndividualOrders,
			String sDBID,
			String sUserID,
			ArrayList<String> sSalespersonList,
			PrintWriter out,
			ServletContext context,
			String sLicenseModuleLevel
			){
	
    //The first half of the statement doesn't care if the orders have been canceled, and the values are added as if it was not canceled:
    String SQL = "(" 
    	+ "SELECT"
    	+ " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber + " As ORDERNUMBER"
	    + ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.datLineBookedDate + " As SALEDATE"
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson + " As SALESPERSON"
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCodeDescription + " AS SALETYPE"
	    + ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered
	    + ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyShippedToDate
	    + ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dOrderUnitPrice
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.dTotalAmountItems
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSpecialWageRate
	    
	    //Add an aliased field to keep these records distinct from the 'canceled' records:
	    + ", 'N' As CANCELED"
    
	    + " FROM"
	    + " " + SMTableorderheaders.TableName + ", " + SMTableorderdetails.TableName
	    
	    + " WHERE ("
	    	//Date range:
	    	+ "(" + SMTableorderdetails.TableName + "." + SMTableorderdetails.datLineBookedDate + " >= '" 
	    		+ sStartingDate + " 00:00:00')"
	    	+ " AND (" + SMTableorderdetails.TableName + "." + SMTableorderdetails.datLineBookedDate + " <= '" 
    		+ sEndingDate + " 23:59:59')"
    		
    		//Link headers and details:
    		+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.dOrderUniqueifier + " = "
    			+ SMTableorderdetails.TableName + "." + SMTableorderdetails.dUniqueOrderID + ")"
    
    		//NO QUOTES!
			+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + " != "
				+ SMTableorderheaders.ORDERTYPE_QUOTE + ")";
    
		   	//SH0002 and SH0004 are sales
		    if(!bIncludeSales){
		        SQL = SQL + " AND (" + SMTableorderheaders.TableName + "." 
		        	+ SMTableorderheaders.sServiceTypeCode + " != 'SH0002')"
		        	+ " AND (" + SMTableorderheaders.TableName + "." 
		        	+ SMTableorderheaders.sServiceTypeCode + " != 'SH0004')";
		    }
		    
		    //SH0001 and SH0003 are service
		    if(!bIncludeService){
		        SQL = SQL + " AND (" + SMTableorderheaders.TableName + "." 
		        	+ SMTableorderheaders.sServiceTypeCode + " != 'SH0001')"
		        	+ " AND (" + SMTableorderheaders.TableName + "." 
		        	+ SMTableorderheaders.sServiceTypeCode + " != 'SH0003')";
		    }
		    
		    if (sSalespersonList.size() > 0){
		    	SQL = SQL + " AND (";
			    for (int i = 0; i < sSalespersonList.size(); i++){
			    	if (i > 0){
			    		SQL = SQL + " OR ";
			    	}
			    	SQL = SQL + "(" + SMTableorderheaders.sSalesperson 
			    		+ " = '" + sSalespersonList.get(i) + "')";
			    }
		    	SQL = SQL + ")";
		    }
		SQL = SQL + ")";  //End the 'WHERE' clause
    
		SQL = SQL + ")"
		//Now include all the records of the orders that were canceled in this period:
	    + " UNION ALL ("
	    + "SELECT"
	    + " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber + " As ORDERNUMBER"
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + " As SALEDATE"
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson + " As SALESPERSON"
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCodeDescription + " As SALETYPE"
	    + ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered
	    + ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyShippedToDate
	    + ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dOrderUnitPrice + " * -1 AS " 
	    	+ SMTableorderdetails.dOrderUnitPrice
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.dTotalAmountItems + " * -1 AS " 
	    	+ SMTableorderheaders.dTotalAmountItems
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSpecialWageRate

	    //Add an aliased field to keep these records distinct from the 'NOT canceled' records:
	    + ", 'Y' As CANCELED"
	    
	    + " FROM"
	    + " " + SMTableorderheaders.TableName + ", " + SMTableorderdetails.TableName
	    //Date range:
	    + " WHERE ("
    		+ "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + " >= '" 
    			+ sStartingDate + " 00:00:00')"
			+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + " <= '" 
				+ sEndingDate + " 23:59:59')"
				
			//Link headers and details:
		    + " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.dOrderUniqueifier + " = "
		    	+ SMTableorderdetails.TableName + "." + SMTableorderdetails.dUniqueOrderID + ")"
		
			//NO QUOTES!
			+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + " != "
				+ SMTableorderheaders.ORDERTYPE_QUOTE + ")";
			
		   	//SH0002 and SH0004 are sales
		    if(!bIncludeSales){
		        SQL = SQL + " AND (" + SMTableorderheaders.TableName + "." 
		        	+ SMTableorderheaders.sServiceTypeCode + " != 'SH0002')"
		        	+ " AND (" + SMTableorderheaders.TableName + "." 
		        	+ SMTableorderheaders.sServiceTypeCode + " != 'SH0004')";
		    }
		    
		    //SH0001 and SH0003 are service
		    if(!bIncludeService){
		        SQL = SQL + " AND (" + SMTableorderheaders.TableName + "." 
		        	+ SMTableorderheaders.sServiceTypeCode + " != 'SH0001')"
		        	+ " AND (" + SMTableorderheaders.TableName + "." 
		        	+ SMTableorderheaders.sServiceTypeCode + " != 'SH0003')";
		    }
	    
		    if (sSalespersonList.size() > 0){
		    	SQL = SQL + " AND (";
			    for (int i = 0; i < sSalespersonList.size(); i++){
			    	if (i > 0){
			    		SQL = SQL + " OR ";
			    	}
			    	SQL = SQL + "(" + SMTableorderheaders.sSalesperson 
			    		+ " = '" + sSalespersonList.get(i) + "')";
			    }
		    	SQL = SQL + ")";
		    }
		    
	    SQL = SQL + ")" //End While clause
	    
	    + ")"

    	+ " ORDER BY " + "SALETYPE, SALESPERSON, ORDERNUMBER"
	    ;

		//System.out.println("In " + this.toString() + ".processReport - main SQL = " + SQL);
		
		String sCurrentSalesType = "";
    	String sCurrentSalesperson = "";
    	String sCurrentOrderNumber = "";
    	
    	//Variables for order footer:
		String sCurrentBillToName = "";
		String sCurrentShipToName = "";
		String sCurrentLocation = "";
		java.sql.Date datSaleDate = null;
		String sCurrentWageScale = "";
		BigDecimal bdCurrentOrderTotal = BigDecimal.ZERO;
		
		//Variables for salesperson footer:
		long lNumberOfOrdersForSalesperson = 0L; 
		BigDecimal bdOrderAmountForSalesperson = BigDecimal.ZERO;

		//Variables for sale type:
		long lNumberOfOrdersForSaleType = 0L; 
		BigDecimal bdOrderAmountForSaleType = BigDecimal.ZERO;
		
		//Variables for report totals:
		long lTotalNumberOfOrders = 0L; 
		BigDecimal bdTotalAverageOrderAmount = BigDecimal.ZERO;
		BigDecimal bdTotalOrderAmount = BigDecimal.ZERO;
    	
		//Check permissions for viewing orders:
		boolean bViewOrderPermitted = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMViewOrderInformation,
			sUserID,
			conn,
			sLicenseModuleLevel);
		
    	try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){

				//If the current order total is NOT zero:
				if (bdCurrentOrderTotal.compareTo(BigDecimal.ZERO) != 0){
				
					//If the current order number is not blank, and the order number has changed, print the
					//order footer:
					if (
						(rs.getString("ORDERNUMBER").compareToIgnoreCase(sCurrentOrderNumber) != 0)
						&& (sCurrentOrderNumber.compareToIgnoreCase("") != 0)
					){
						lTotalNumberOfOrders++;
						lNumberOfOrdersForSalesperson++;
						lNumberOfOrdersForSaleType++;
						
						if (bShowIndividualOrders){
							if (lNumberOfOrdersForSalesperson == 1){
								printOrderHeader(out);
							}
							printOrderFooter(
								sCurrentSalesperson,
								sCurrentOrderNumber.trim(),
								sCurrentLocation.trim(),
								sCurrentBillToName.trim(),
								sCurrentShipToName.trim(),
								datSaleDate,
								sCurrentWageScale.trim(),
								bdCurrentOrderTotal,
								bViewOrderPermitted,
								sDBID,
								out,
								context
							);
						}
						bdCurrentOrderTotal = BigDecimal.ZERO;	
					}
				}
	    		//Print the salesperson footer for any new sales type OR location
				// OR salesperson:
				if (
					((rs.getString("SALETYPE").compareToIgnoreCase(sCurrentSalesType) != 0)
					|| ((rs.getString("SALESPERSON")).compareToIgnoreCase(sCurrentSalesperson) != 0))
					&& (sCurrentSalesperson.compareToIgnoreCase("") != 0)
				){
					BigDecimal bdAverageOrderAmountForSalesperson = BigDecimal.ZERO;
					
					if (lNumberOfOrdersForSalesperson > 0){
						bdAverageOrderAmountForSalesperson = bdOrderAmountForSalesperson.divide(BigDecimal.valueOf(lNumberOfOrdersForSalesperson), 2, RoundingMode.HALF_UP);
					}else{
						bdAverageOrderAmountForSalesperson = BigDecimal.ZERO;
					}

					printSalespersonFooter(
						lNumberOfOrdersForSalesperson, 
						bdAverageOrderAmountForSalesperson,
						sCurrentSalesperson,
						sCurrentSalesType,
						bdOrderAmountForSalesperson,
						out,
						bShowIndividualOrders,
						conn
					);
					
					//Initialize the salesperson variables:
					lNumberOfOrdersForSalesperson = 0L;
					bdOrderAmountForSalesperson = BigDecimal.ZERO;
				}

				//Sale Type footer
	    		//Print the sale type footer for any new sales OR site type
				if (
						(rs.getString("SALETYPE").compareToIgnoreCase(sCurrentSalesType) != 0)
					&& (sCurrentSalesType.compareToIgnoreCase("") != 0)
				){
					BigDecimal bdAverageOrderAmountForSaleType = BigDecimal.ZERO;
					
					if (lNumberOfOrdersForSaleType > 0){
						bdAverageOrderAmountForSaleType = bdOrderAmountForSaleType.divide(BigDecimal.valueOf(lNumberOfOrdersForSaleType), 2, RoundingMode.HALF_UP);
					}else{
						bdAverageOrderAmountForSaleType = BigDecimal.ZERO;
					}
					printSaleTypeFooter(
							lNumberOfOrdersForSaleType, 
							bdAverageOrderAmountForSaleType,
							sCurrentSalesType,
							bdOrderAmountForSaleType,
							out
							);
					
					//Initialize the sale type variables:
					lNumberOfOrdersForSaleType = 0L; 
					bdOrderAmountForSaleType = BigDecimal.ZERO;
				}
			
				//Update the variables:
				sCurrentSalesType = rs.getString("SALETYPE");
		    	sCurrentSalesperson = rs.getString("SALESPERSON");
		    	sCurrentOrderNumber = rs.getString("ORDERNUMBER");

		    	//Update the order variables:
		    	sCurrentLocation = rs.getString(SMTableorderheaders.sLocation);
				sCurrentBillToName = rs.getString(SMTableorderheaders.sBillToName);
				sCurrentShipToName = rs.getString(SMTableorderheaders.sShipToName);
				datSaleDate = rs.getDate("SALEDATE");
				sCurrentWageScale = rs.getString(SMTableorderheaders.sSpecialWageRate);
				
				BigDecimal bdTotalLineQtyOrdered = BigDecimal.ZERO;
				if (rs.getString("CANCELED").compareToIgnoreCase("N") == 0){
					bdTotalLineQtyOrdered = 
						rs.getBigDecimal(SMTableorderdetails.dQtyOrdered).add(rs.getBigDecimal(SMTableorderdetails.dQtyShippedToDate));
				}else{
					bdTotalLineQtyOrdered = 
						rs.getBigDecimal(SMTableorderdetails.dQtyOrdered);
				}
				BigDecimal bdLineAmount = 
					bdTotalLineQtyOrdered.multiply(rs.getBigDecimal(SMTableorderdetails.dOrderUnitPrice)).setScale(2, BigDecimal.ROUND_HALF_UP);
				bdCurrentOrderTotal = bdCurrentOrderTotal.add(bdLineAmount);
			
				//Calculate the report totals:
				bdTotalOrderAmount = bdTotalOrderAmount.add(bdLineAmount);
				bdOrderAmountForSalesperson = bdOrderAmountForSalesperson.add(bdLineAmount);
				bdOrderAmountForSaleType = bdOrderAmountForSaleType.add(bdLineAmount);
				
			}
			rs.close();
    	}catch (SQLException e){
    		m_sErrorMessage = "Error reading resultset - " + e.getMessage();
    		return false;
    	}
    	
		//Print the last order footer, if there was at least one order:
		if (sCurrentOrderNumber.compareToIgnoreCase("") != 0){
			if (bShowIndividualOrders){
				lTotalNumberOfOrders++;
				lNumberOfOrdersForSalesperson++;
				lNumberOfOrdersForSaleType++;
				if (lNumberOfOrdersForSalesperson == 1){
					printOrderHeader(out);
				}
				printOrderFooter(
						sCurrentSalesperson,
						sCurrentOrderNumber.trim(),
						sCurrentLocation.trim(),
						sCurrentBillToName.trim(),
						sCurrentShipToName.trim(),
						datSaleDate,
						sCurrentWageScale.trim(),
						bdCurrentOrderTotal,
						bViewOrderPermitted,
						sDBID,
						out,
						context
					);
			}
			bdCurrentOrderTotal = BigDecimal.ZERO;
		}

		//Print the salesperson footer if there was at least one salesperson:
		if (sCurrentSalesperson.compareToIgnoreCase("") != 0){
			BigDecimal bdAverageOrderAmountForSalesperson = BigDecimal.ZERO;
			
			if (lNumberOfOrdersForSalesperson > 0){
				bdAverageOrderAmountForSalesperson = bdOrderAmountForSalesperson.divide(BigDecimal.valueOf(lNumberOfOrdersForSalesperson), 2, RoundingMode.HALF_UP);
			}else{
				bdAverageOrderAmountForSalesperson = BigDecimal.ZERO;
			}

			printSalespersonFooter(
				lNumberOfOrdersForSalesperson, 
				bdAverageOrderAmountForSalesperson,
				sCurrentSalesperson,
				sCurrentSalesType,
				bdOrderAmountForSalesperson,
				out,
				bShowIndividualOrders,
				conn
			);
		}

		//Print the sale type footer for any new sales type
		if (sCurrentSalesType.compareToIgnoreCase("") != 0){
			BigDecimal bdAverageOrderAmountForSaleType = BigDecimal.ZERO;
			
			if (lNumberOfOrdersForSaleType > 0){
				bdAverageOrderAmountForSaleType = bdOrderAmountForSaleType.divide(BigDecimal.valueOf(lNumberOfOrdersForSaleType), 2, RoundingMode.HALF_UP);
			}else{
				bdAverageOrderAmountForSaleType = BigDecimal.ZERO;
			}
			printSaleTypeFooter(
					lNumberOfOrdersForSaleType, 
					bdAverageOrderAmountForSaleType,
					sCurrentSalesType,
					bdOrderAmountForSaleType,
					out
					);
		}
				
		//Print the grand totals:
		if (lTotalNumberOfOrders > 0){
			bdTotalAverageOrderAmount = bdTotalOrderAmount.divide(BigDecimal.valueOf(lTotalNumberOfOrders), 2, RoundingMode.HALF_UP);
		}else{
			bdTotalAverageOrderAmount = BigDecimal.ZERO;
		}

		printReportFooter(
				lTotalNumberOfOrders, 
				bdTotalAverageOrderAmount,
				bdTotalOrderAmount,
				out
				);
		return true;
	}
	private void printOrderFooter(
		String sSalesperson,
		String sOrderNumber,
		String sLocation,
		String sBillToName,
		String sShipToName,
		java.sql.Date datSale,
		String sWageScale,
		BigDecimal bdOrderTotal,
		boolean bViewOrderPermitted,
		String sDBID,
		PrintWriter out,
		ServletContext context
		){
		
		String sOrderNumberLink = "";
		if (bViewOrderPermitted){
			sOrderNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" + sOrderNumber 
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sOrderNumber + "</A>";
		}else{
			sOrderNumberLink = sOrderNumber;
		}
		out.println(
				"<TR>"
			    + "<TD ALIGN=LEFT VALIGN=BOTTOM><FONT SIZE=2>" + sSalesperson + "</FONT></TD>"
			    + "<TD ALIGN=LEFT VALIGN=BOTTOM><FONT SIZE=2>" 
		    		+ clsDateAndTimeConversions.utilDateToString(datSale, "MM/dd/yyyy")+ "</FONT></TD>"
			    + "<TD ALIGN=LEFT VALIGN=BOTTOM><FONT SIZE=2>" + sOrderNumberLink + "</FONT></TD>"
			    + "<TD ALIGN=LEFT VALIGN=BOTTOM><FONT SIZE=2>" + sLocation + "</FONT></TD>"
			    + "<TD ALIGN=LEFT VALIGN=BOTTOM><FONT SIZE=2>" + sWageScale + "</FONT></TD>"
			    + "<TD ALIGN=LEFT VALIGN=BOTTOM><FONT SIZE=2>" + sBillToName + "</FONT></TD>"
			    + "<TD ALIGN=LEFT VALIGN=BOTTOM><FONT SIZE=2>" + sShipToName + "</FONT></TD>"
				+ "<TD ALIGN=RIGHT VALIGN=BOTTOM><FONT SIZE=2>" 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdOrderTotal) + "</FONT></TD>"
				+ "</TR>"
				);	
	}
	private void printOrderHeader(
			PrintWriter out
			){
		
		out.println(
				"<TABLE BORDER=0 WIDTH = 100%>"
				+ "<TR>"
			    + "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=5%><B><U><FONT SIZE=2>SP #</FONT></U></B></TD>"
			    + "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=7%><B><U><FONT SIZE=2>Date</FONT></U></B></TD>"
			    + "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=6%><B><U><FONT SIZE=2>Order #</FONT></U></B></TD>"
			    + "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=5%><B><U><FONT SIZE=2>Loc.</FONT></U></B></TD>"
			    + "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=5%><B><U><FONT SIZE=2>WS</FONT></U></B></TD>"
			    + "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=33%><B><U><FONT SIZE=2>Bill to</FONT></U></B></TD>"
				+ "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=33%><B><U><FONT SIZE=2>Ship to</FONT></U></B></TD>"
				+ "<TD ALIGN=RIGHT VALIGN=BOTTOM WIDTH=11%><B><U><FONT SIZE=2>Amount</FONT></U></B></TD>"
				+ "</TR>"
				);
	}
	private void printSalespersonFooter(
			long lNumberOfOrdersForSalesperson, 
			BigDecimal bdAverageOrderAmountForSalesperson,
			String sSalesperson,
			String sSaleType,
			BigDecimal bdTotalAmountForSalesperson,
			PrintWriter out,
			boolean bShowIndividualOrders,
			Connection conn
			){

		//First, end the order header table:
		if (bShowIndividualOrders){
			out.println("</TABLE>");
		}
		//Suppress this if there are no orders:
		if (lNumberOfOrdersForSalesperson == 0){
			return;
		}
		
		String sSalespersonName = "";

		try{
			String SQL = "SELECT * FROM " + SMTablesalesperson.TableName
				+ " WHERE ("
					+ SMTablesalesperson.sSalespersonCode + " = '" + sSalesperson + "'"
				+ ")"
				;
			
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				sSalespersonName = rs.getString(SMTablesalesperson.sSalespersonFirstName).trim()
					+ " " + rs.getString(SMTablesalesperson.sSalespersonLastName).trim();
			}
			rs.close();
		}catch(SQLException e){
			System.out.println("In " + this.toString() + " could not read salesperson name - " + e.getMessage());
		}
		
		out.println("<TABLE BORDER=1 WIDTH = 100%>"
			/*
			+ "<TR>"
			+ "<TD ALIGN=RIGHT WIDTH=85%><FONT SIZE=2>Orders for " + sSaleType + ", salesperson " + sSalesperson + ":</FONT></TD>"
			+ "<TD ALIGN=RIGHT WIDTH=15%><FONT SIZE=2>" + Long.toString(lNumberOfOrdersForSalesperson) + "</FONT></TD>"
			+ "</TR>"
			+ "<TR>"
			+ "<TD ALIGN=RIGHT WIDTH=85%><FONT SIZE=2>Avg. order amount:</FONT></TD>"
			+ "<TD ALIGN=RIGHT WIDTH=15%><FONT SIZE=2>" + SMUtilities.BigDecimalTo2DecimalSTDFormat(bdAverageOrderAmountForSalesperson) + "</FONT></TD>"
			+ "</TR>"
			+ "<TR>"
			*/
			+ "<TD ALIGN=RIGHT WIDTH=85%><FONT SIZE=2>"
				+ "<B>SALESPERSON&nbsp;" + sSalesperson + "</B>:&nbsp;" + sSalespersonName + ",&nbsp;"
				+ sSaleType + " - "
				+ Long.toString(lNumberOfOrdersForSalesperson) 
				+ " order(s),&nbsp;avg.&nbsp;amt.&nbsp;" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAverageOrderAmountForSalesperson)
				+ "&nbsp;&nbsp;<B>TOTAL:</B></FONT></TD>"
			+ "<TD ALIGN=RIGHT WIDTH=15%><FONT SIZE=2><B>"
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalAmountForSalesperson) + "</B></FONT></TD>"
			+ "</TR>"
			+ "</TABLE><TR>"
			);
	}
	private void printSaleTypeFooter(
			long lNumberOfOrdersForSaleType, 
			BigDecimal bdAverageOrderAmountForSaleType,
			String sSaleType,
			BigDecimal bdTotalAmountForSaleType,
			PrintWriter out
			){
		out.println("<TABLE BORDER=0 WIDTH = 100%>"
				+ "<TR>"
				+ "<TD ALIGN=RIGHT WIDTH=85%><FONT SIZE=2>Orders for " + sSaleType + ": </FONT></TD>"
				+ "<TD ALIGN=RIGHT WIDTH=15%><FONT SIZE=2>" + Long.toString(lNumberOfOrdersForSaleType) + "</FONT></TD>"
				+ "</TR>"
				+ "<TR>"
				+ "<TD ALIGN=RIGHT WIDTH=85%><FONT SIZE=2>Avg. order amount:</FONT></TD>"
				+ "<TD ALIGN=RIGHT WIDTH=15%><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAverageOrderAmountForSaleType) + "</FONT></TD>"
				+ "</TR>"
				+ "<TR>"
				+ "<TD ALIGN=RIGHT WIDTH=85%><FONT SIZE=2>Total for " + sSaleType + ":</FONT></TD>"
				+ "<TD ALIGN=RIGHT WIDTH=15%><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalAmountForSaleType) + "</FONT></TD>"
				+ "</TR>"
				+ "</TABLE><BR>"
				);
	}

	private void printReportFooter(
			long lTotalNumberOfOrders, 
			BigDecimal bdTotalAverageOrderAmount,
			BigDecimal bdTotalAmount,
			PrintWriter out
			){
		out.println("<TABLE BORDER=0 WIDTH = 100%>"
				+ "<TR>"
				+ "<TD ALIGN=RIGHT WIDTH=85%><FONT SIZE=2><B>Total Orders:</B></FONT></TD>"
				+ "<TD ALIGN=RIGHT WIDTH=15%><FONT SIZE=2><B>" + Long.toString(lTotalNumberOfOrders) + "</B></FONT></TD>"
				+ "</TR>"
				+ "<TR>"
				+ "<TD ALIGN=RIGHT WIDTH=85%><FONT SIZE=2><B>Avg. order amount:</B></FONT></TD>"
				+ "<TD ALIGN=RIGHT WIDTH=15%><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalAverageOrderAmount) + "</B></FONT></TD>"
				+ "</TR>"
				+ "<TR>"
				+ "<TD ALIGN=RIGHT WIDTH=85%><FONT SIZE=2><B>Company Total:</B></FONT></TD>"
				+ "<TD ALIGN=RIGHT WIDTH=15%><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalAmount) + "</B></FONT></TD>"
				+ "</TR>"
				+ "</TABLE>"
				);
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}

