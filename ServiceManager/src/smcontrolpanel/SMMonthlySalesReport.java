package smcontrolpanel;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import SMClasses.SMOrderHeader;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesalesgroups;
import SMDataDefinition.SMTablesalesperson;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;

public class SMMonthlySalesReport extends java.lang.Object{

	private String m_sErrorMessage;
	
	public SMMonthlySalesReport(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sStartingDate,
			String sEndingDate,
			ArrayList<String> arrServiceTypes,
			boolean bShowIndividualOrders,
			String sDBID,
			String sUserID,
			ArrayList<String> arrSalespersonList,
			ArrayList<String> arrSalesGroupList,
			PrintWriter out,
			ServletContext context,
			String sLicenseModuleLevel
			){
		
    //The first half of the statement doesn't care if the orders have been canceled, and the values are added as if it was not canceled:
    String SQL = "SELECT" + "\n"
    	+ " " + "'Monthly Sales Report' AS REPORTNAME"  + "\n"
    	+ ", '" + sUserID + "' AS USERID" + "\n"
    	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber + " As ORDERNUMBER" + "\n"
	    + ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.datLineBookedDate + " As SALEDATE" + "\n"
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName + "\n"
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName + "\n"
	    
	    + ", IF(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson + " = '', "
	    		+ "'" + SMOrderHeader.UNLISTEDSALESPERSON_MARKER + "', " 
	    		+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson
	    		+ ") AS SALESPERSON" + "\n"
	    
	    + ", CONCAT(" + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonFirstName + ",' '," 
			+ SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonLastName + ") AS SALESPERSONNAME" + "\n"

	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode + "\n"
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCodeDescription + " AS SALETYPE" + "\n"
	    + ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered + "\n"
	    + ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyShippedToDate + "\n"
	    + ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dOrderUnitPrice + "\n"
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation + "\n"
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.dTotalAmountItems + "\n"
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSpecialWageRate + "\n"
	    + ", IF(ISNULL(" + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupCode + "), '(N/A)'," 
    	+ SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupCode + ") AS SALESGROUP" + "\n"
	    //Add an aliased field to keep these records distinct from the 'canceled' records:
	    + ", 'N' As CANCELED" + "\n"
    
	    + " FROM " + "\n"
	    
		+ SMTableorderdetails.TableName + " LEFT JOIN " + SMTableorderheaders.TableName + " ON " 
		+ SMTableorderdetails.TableName + "." + SMTableorderdetails.strimmedordernumber + " = "
		+ SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber + "\n"
	    
	    + " LEFT JOIN " + SMTablesalesgroups.TableName + " ON " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.iSalesGroupId
	    + " = " + SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup + "\n"
	    
	    //Link salesperson table:
	    + " LEFT JOIN " + SMTablesalesperson.TableName + " ON " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson
	    	+ " = " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode + "\n"
	    
	    + " WHERE (" + "\n"
	    	//Date range:
	    	+ "(" + SMTableorderdetails.TableName + "." + SMTableorderdetails.datLineBookedDate + " >= '" 
	    		+ sStartingDate + " 00:00:00')" + "\n"
	    	+ " AND (" + SMTableorderdetails.TableName + "." + SMTableorderdetails.datLineBookedDate + " <= '" 
    		+ sEndingDate + " 23:59:59')" + "\n"
    		
		    + " AND (" + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered + " + "
	    		+ SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyShippedToDate + " <> 0)" + "\n"

    		//NO QUOTES!
			+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + " != "
			+ SMTableorderheaders.ORDERTYPE_QUOTE + ")" + "\n"
			;
    
    		//Get service types
    		if (arrServiceTypes.size() > 0){
    			SQL = SQL + " AND (" + "\n";
    			for (int i = 0; i < arrServiceTypes.size(); i++){
    				if (i > 0){
    					SQL = SQL + " OR ";
    				}
    				SQL = SQL + "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode
    						+ " = '" + arrServiceTypes.get(i) + "')" + "\n";
    			}
    		}
    		SQL += ")" + "\n";
		    //Get salesperson's
		    if (arrSalespersonList.size() > 0){
		    	SQL = SQL + " AND (" + "\n";
			    for (int i = 0; i < arrSalespersonList.size(); i++){
			    	if (i > 0){
			    		SQL = SQL + " OR ";
			    	}
			    	SQL = SQL + "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson
			    		+ " = '" + arrSalespersonList.get(i) + "')" + "\n";
			    }
		    }
		    SQL += ")" + "\n";
    		//Get the sales groups:
		    if (arrSalesGroupList.size() > 0){
		    	SQL += " AND (" + "\n";
	    		for (int i = 0; i < arrSalesGroupList.size(); i++){
	    			if (i == 0){
	    				SQL += "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup + " = " 
	    					+ arrSalesGroupList.get(i).substring(arrSalesGroupList.get(i).indexOf(SMMonthlySalesReportSelection.SALESGROUP_PARAM_SEPARATOR) + 1) + ")" + "\n";
	    			}else{
	    				SQL += " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup + " = " 
	    					+ arrSalesGroupList.get(i).substring(arrSalesGroupList.get(i).indexOf(SMMonthlySalesReportSelection.SALESGROUP_PARAM_SEPARATOR) + 1) + ")" + "\n";
	    			}
	    		}
	    		SQL = SQL + ")" + "\n";
		    }
		SQL += ")" //End the 'WHERE' clause
    
		//Now include all the records of the orders that were canceled in this period:
	    + " UNION ALL " + "\n"
	    + "SELECT" + "\n"
	    + " " + "'Monthly Sales Report' AS REPORTNAME" + "\n"
	    + ", '" + sUserID + "' AS USERID" + "\n"
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber + " As ORDERNUMBER" + "\n"
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + " As SALEDATE" + "\n"
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName + "\n"
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName + "\n"
	    
	    + ", IF(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson + " = '', "
		+ "'" + SMOrderHeader.UNLISTEDSALESPERSON_MARKER + "', " 
		+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson
		+ ") AS SALESPERSON" + "\n"
	    
		+ ", CONCAT(" + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonFirstName + ",' '," 
			+ SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonLastName + ") AS SALESPERSONNAME" + "\n"
		
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode + "\n"
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCodeDescription + " As SALETYPE" + "\n"
	    + ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered + "\n"
	    + ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyShippedToDate + "\n"
	    + ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dOrderUnitPrice + " * -1 AS " 
	    	+ SMTableorderdetails.dOrderUnitPrice + "\n"
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation + "\n"
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.dTotalAmountItems + " * -1 AS " 
	    	+ SMTableorderheaders.dTotalAmountItems + "\n"
	    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSpecialWageRate + "\n"
	    + ", IF(ISNULL(" + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupCode + "), '(N/A)'," 
	    	+ SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupCode + ") AS SALESGROUP" + "\n"

	    //Add an aliased field to keep these records distinct from the 'NOT canceled' records:
	    + ", 'Y' As CANCELED" + "\n"
	    
	    + " FROM " + "\n"
	    
		+ SMTableorderdetails.TableName + " LEFT JOIN " + SMTableorderheaders.TableName + " ON " 
		+ SMTableorderdetails.TableName + "." + SMTableorderdetails.strimmedordernumber + " = "
		+ SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber + "\n"
	    
	    + " LEFT JOIN " + SMTablesalesgroups.TableName + " ON " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.iSalesGroupId
	    + " = " + SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup + "\n"

	    //Link salesperson table:
	    + " LEFT JOIN " + SMTablesalesperson.TableName + " ON " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson
	    	+ " = " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode + "\n"

	    //Date range:
	    + " WHERE (" + "\n"
    	    + "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + " >= '" + sStartingDate + " 00:00:00')" + "\n"
			+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + " <= '" + sEndingDate + " 23:59:59')" + "\n"
				
		    + " AND ((" + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered + " + " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyShippedToDate + ") <> 0)" + "\n"
		
			//NO QUOTES!
			+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + " != "
			+ SMTableorderheaders.ORDERTYPE_QUOTE + ")" + "\n"
			;
		
		
		//Get service types
		if (arrServiceTypes.size() > 0){
			SQL = SQL + " AND (" + "\n";
			for (int i = 0; i < arrServiceTypes.size(); i++){
				if (i > 0){
					SQL = SQL + " OR ";
				}
				SQL = SQL + "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode
						+ " = '" + arrServiceTypes.get(i) + "')" + "\n";
			}
			SQL = SQL + ")" + "\n";
		}
		
		    if (arrSalespersonList.size() > 0){
		    	SQL = SQL + " AND (" + "\n";
			    for (int i = 0; i < arrSalespersonList.size(); i++){
			    	if (i > 0){
			    		SQL = SQL + " OR ";
			    	}
			    	SQL = SQL + "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson 
			    		+ " = '" + arrSalespersonList.get(i) + "')" + "\n";
			    }
		    	SQL = SQL + ")" + "\n";
		    }
		    
    		//Get the sales groups:
		    if (arrSalesGroupList.size() > 0){
		    	SQL += " AND (" + "\n";
	    		for (int i = 0; i < arrSalesGroupList.size(); i++){
	    			if (i == 0){
	    				SQL += "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup + " = " 
	    					+ arrSalesGroupList.get(i).substring(arrSalesGroupList.get(i).indexOf(SMMonthlySalesReportSelection.SALESGROUP_PARAM_SEPARATOR) + 1) + ")" + "\n";
	    			}else{
	    				SQL += " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup + " = " 
	    					+ arrSalesGroupList.get(i).substring(arrSalesGroupList.get(i).indexOf(SMMonthlySalesReportSelection.SALESGROUP_PARAM_SEPARATOR) + 1) + ")" + "\n";
	    			}
	    		}
	    		SQL = SQL + ")" + "\n";
		    }
		SQL += ")";
		
SQL += " ORDER BY " + "SALESGROUP, SALETYPE DESC, SALESPERSON, ORDERNUMBER" + "\n"
	    ;
	   // System.out.println("[1376426113] In " + this.toString() + ".processReport - main SQL = " + SQL);
		
	    String sCurrentSalesGroup = "";
		String sCurrentSalesType = "";
    	String sCurrentSalesperson = "";
    	String sCurrentSalespersonName = "";
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
		
		//Variables for sales group:
		long lNumberOfOrdersForSalesGroup = 0L; 
		BigDecimal bdOrderAmountForSalesGroup = BigDecimal.ZERO;
		
		//Variables for report totals:
		long lTotalNumberOfOrders = 0L; 
		BigDecimal bdTotalAverageOrderAmount = BigDecimal.ZERO;
		BigDecimal bdTotalOrderAmount = BigDecimal.ZERO;
    	
		//Variables for order type totals:
		ArrayList<String> arrOrderTypes = new ArrayList<String>(0);
		ArrayList<BigDecimal> arrOrderTypeTotals = new ArrayList<BigDecimal>(0);
		ArrayList<Long> arrOrderTypeCounts = new ArrayList<Long>(0);
		
		long lEndingTime = 0L;
		long lStartingTime = 0L;
		
		//Check permissions for viewing orders:
		boolean bViewOrderPermitted = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMViewOrderInformation,
			sUserID,
			conn,
			sLicenseModuleLevel);
		
		int iCount = 0;
		out.println("<TABLE WIDTH = 100% CLASS=\""+ SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\">" );
		
    	try{
    		lStartingTime = System.currentTimeMillis();
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			lEndingTime = System.currentTimeMillis();

			while(rs.next()){
				boolean bIsNewOrder = false;
				//If the current order total is NOT zero:
				if (bdCurrentOrderTotal.compareTo(BigDecimal.ZERO) != 0){
				
					//If the current order number is not blank, and the order number has changed, print the
					//order footer:
					if (
						(rs.getString("ORDERNUMBER").compareToIgnoreCase(sCurrentOrderNumber) != 0)
						&& (sCurrentOrderNumber.compareToIgnoreCase("") != 0)
					){
						bIsNewOrder = true;
						lTotalNumberOfOrders++;
						lNumberOfOrdersForSalesperson++;
						lNumberOfOrdersForSaleType++;
						lNumberOfOrdersForSalesGroup++;
						
						if (bShowIndividualOrders){
							if (lNumberOfOrdersForSalesperson == 1){
								printOrderHeader(out);
								
							}
							printOrderFooter(
								sCurrentSalesGroup,
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
								context,
								iCount
							);
							iCount++;
						}
						bdCurrentOrderTotal = BigDecimal.ZERO;	
					}
				}
	    		//Print the salesperson footer for any new sales type OR location
				// OR salesperson:
				if (
					((rs.getString("SALESGROUP").compareToIgnoreCase(sCurrentSalesGroup) != 0)
					|| (rs.getString("SALETYPE").compareToIgnoreCase(sCurrentSalesType) != 0)
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
						sCurrentSalespersonName,
						sCurrentSalesType,
						sCurrentSalesGroup,
						bdOrderAmountForSalesperson,
						out,
						bShowIndividualOrders,
						conn
					);
					
					//Initialize the salesperson variables:
					lNumberOfOrdersForSalesperson = 0L;
					bdOrderAmountForSalesperson = BigDecimal.ZERO;
					iCount = 0;
				}

				//Sale Type footer
	    		//Print the sale type footer for any new sales OR site type
				if ((rs.getString("SALESGROUP").compareToIgnoreCase(sCurrentSalesGroup) != 0
					|| (rs.getString("SALETYPE").compareToIgnoreCase(sCurrentSalesType) != 0))
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
							sCurrentSalesGroup,
							bdOrderAmountForSaleType,
							out
							);
					
					//Initialize the sale type variables:
					lNumberOfOrdersForSaleType = 0L; 
					bdOrderAmountForSaleType = BigDecimal.ZERO;
					iCount = 0;
				}
				
				//Sales group footer:
				if ((rs.getString("SALESGROUP").compareToIgnoreCase(sCurrentSalesGroup) != 0)
					&& (sCurrentSalesGroup.compareToIgnoreCase("") != 0)
				){
					BigDecimal bdAverageOrderAmountForSalesGroup = BigDecimal.ZERO;
					
					if (lNumberOfOrdersForSalesGroup > 0){
						bdAverageOrderAmountForSalesGroup = bdOrderAmountForSalesGroup.divide(BigDecimal.valueOf(lNumberOfOrdersForSalesGroup), 2, RoundingMode.HALF_UP);
					}else{
						bdAverageOrderAmountForSalesGroup = BigDecimal.ZERO;
					}
					printSalesGroupFooter(
							lNumberOfOrdersForSalesGroup, 
							bdAverageOrderAmountForSalesGroup,
							sCurrentSalesGroup,
							bdOrderAmountForSalesGroup,
							out
							);
					
					//Initialize the salesgroup variables:
					lNumberOfOrdersForSalesGroup = 0L; 
					bdOrderAmountForSalesGroup = BigDecimal.ZERO;
					iCount = 0;
				}
			
				//Update the variables:
				sCurrentSalesType = rs.getString("SALETYPE");
				sCurrentSalesGroup = rs.getString("SALESGROUP");
		    	sCurrentSalesperson = rs.getString("SALESPERSON");
		    	sCurrentSalespersonName = rs.getString("SALESPERSONNAME");
		    	if (sCurrentSalespersonName == null){
		    		sCurrentSalespersonName = "(N/A)";
		    	}
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
			
				//Calculate the sale type totals:
				boolean bSaleTypeNotFound = true;
				for (int i = 0; i < arrOrderTypes.size(); i++){
					if (arrOrderTypes.get(i).compareToIgnoreCase(sCurrentSalesType) == 0){
						bSaleTypeNotFound = false;
						arrOrderTypeTotals.set(i, arrOrderTypeTotals.get(i).add(bdLineAmount));
						if (bIsNewOrder){
							arrOrderTypeCounts.set(i, arrOrderTypeCounts.get(i) + 1);
						}
						break;
					}
				}
				if (bSaleTypeNotFound){
					arrOrderTypes.add(sCurrentSalesType);
					arrOrderTypeTotals.add(bdLineAmount);
					arrOrderTypeCounts.add(1L);
				}
				
				//Calculate the report totals:
				bdTotalOrderAmount = bdTotalOrderAmount.add(bdLineAmount);
				bdOrderAmountForSalesperson = bdOrderAmountForSalesperson.add(bdLineAmount);
				bdOrderAmountForSaleType = bdOrderAmountForSaleType.add(bdLineAmount);
				bdOrderAmountForSalesGroup = bdOrderAmountForSalesGroup.add(bdLineAmount);

			}
			rs.close();
    	}catch (SQLException e){
    		m_sErrorMessage = "Error reading resultset - " + e.getMessage();
    		return false;
    	}
    	
		//Print the last order footer, if there was at least one order AND it totalled more than zero:
		if (
				(sCurrentOrderNumber.compareToIgnoreCase("") != 0)
				&& (bdCurrentOrderTotal.compareTo(BigDecimal.ZERO) != 0)
		){
			if (bShowIndividualOrders){
				lTotalNumberOfOrders++;
				lNumberOfOrdersForSalesperson++;
				lNumberOfOrdersForSaleType++;
				lNumberOfOrdersForSalesGroup++;
				if (lNumberOfOrdersForSalesperson == 1){
					printOrderHeader(out);
				}
				printOrderFooter(
						sCurrentSalesGroup,
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
						context,
						iCount
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
				sCurrentSalespersonName,
				sCurrentSalesType,
				sCurrentSalesGroup,
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
					sCurrentSalesGroup,
					bdOrderAmountForSaleType,
					out
					);
		}
				
		//Print the sales group footer for any new sales group
		if (sCurrentSalesGroup.compareToIgnoreCase("") != 0)
		{
			BigDecimal bdAverageOrderAmountForSalesGroup = BigDecimal.ZERO;
			
			if (lNumberOfOrdersForSalesGroup > 0){
				bdAverageOrderAmountForSalesGroup = bdOrderAmountForSalesGroup.divide(BigDecimal.valueOf(lNumberOfOrdersForSalesGroup), 2, RoundingMode.HALF_UP);
			}else{
				bdAverageOrderAmountForSalesGroup = BigDecimal.ZERO;
			}
			printSalesGroupFooter(
					lNumberOfOrdersForSalesGroup, 
					bdAverageOrderAmountForSalesGroup,
					sCurrentSalesGroup,
					bdOrderAmountForSalesGroup,
					out
					);
		}
		
		//Print the order type grand totals:
		try {
			printOrderTypeGrandTotals(
				arrOrderTypes,
				arrOrderTypeTotals,
				arrOrderTypeCounts,
				out
			);
		} catch (Exception e) {
			out.println("Error printing order type grand totals - " + e.getMessage());
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
		
		out.println("<BR><P STYLE = \"font-family:arial;\">Query took " + (lEndingTime - lStartingTime)/1000L + " seconds (" + Long.toString(lEndingTime - lStartingTime) + "ms) on database server.</P>");
		return true;
	}
	private void printOrderFooter(
		String sSalesGroup,
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
		ServletContext context,
		int iCount
		){
		
		String sOrderNumberLink = "";
		if (bViewOrderPermitted){
			sOrderNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" + sOrderNumber 
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sOrderNumber + "</A>";
		}else{
			sOrderNumberLink = sOrderNumber;
		}
		if(iCount % 2 == 0) {
			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
		}else {
			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
		}
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + sSalesGroup + "/" + sSalesperson +"</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+ clsDateAndTimeConversions.utilDateToString(datSale, "MM/dd/yyyy")+" </TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + sOrderNumberLink + "</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+ sLocation +" </TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+ sWageScale +" </TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + sBillToName + " </TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + sShipToName +" </TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdOrderTotal) +" </TD>");
		out.println("</TR>");

	}
	private void printOrderHeader(
			PrintWriter out
			){
		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Grp/SP #</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Date </TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Order #</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Loc. </TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">WS </TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Bill to </TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Ship to </TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Amount </TD>");
		out.println("</TR>");

	}
	private void printSalespersonFooter(
			long lNumberOfOrdersForSalesperson, 
			BigDecimal bdAverageOrderAmountForSalesperson,
			String sSalesperson,
			String sSalespersonName,
			String sSaleType,
			String sSalesGroup,
			BigDecimal bdTotalAmountForSalesperson,
			PrintWriter out,
			boolean bShowIndividualOrders,
			Connection conn
			){
		
		//First, end the order header table:
		if (bShowIndividualOrders){
			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_SUB_HEADING + "\">");
		}

		//Suppress this if there are no orders:
		if (lNumberOfOrdersForSalesperson == 0){
			return;
		}

		out.println("<TD COLSPAN = \"7\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">SALES GROUP " + sSalesGroup + ", "
				+ "SALESPERSON&nbsp;" + sSalesperson + ":&nbsp;" + sSalespersonName + ",&nbsp;"
				+ sSaleType + " - "
				+ Long.toString(lNumberOfOrdersForSalesperson) 
				+ " order(s),&nbsp;avg.&nbsp;amt.&nbsp;" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAverageOrderAmountForSalesperson)
				+ "&nbsp;&nbsp;<B>TOTAL:</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalAmountForSalesperson) + " </TD>");
		out.println("</TR>");
		
	}
	private void printSaleTypeFooter(
			long lNumberOfOrdersForSaleType, 
			BigDecimal bdAverageOrderAmountForSaleType,
			String sSaleType,
			String sSalesGroup,
			BigDecimal bdTotalAmountForSaleType,
			PrintWriter out
			){

		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
		out.println("<TD COLSPAN=\"7\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"><FONT COLOR = \"GREEN\" >ORDER TYPE TOTALS * * * </FONT>Orders for sales group " + sSalesGroup + ", "  + sSaleType +":</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"> " + Long.toString(lNumberOfOrdersForSaleType) +"</TD>");
		out.println("</TR>");
		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
		out.println("<TD COLSPAN = \"7\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Avg. order amount:</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"> " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAverageOrderAmountForSaleType) +"</TD>");
		out.println("</TR>");
		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
		out.println("<TD COLSPAN = \"7\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Total for sales group " + sSalesGroup + ", " + sSaleType +":</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"> " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalAmountForSaleType) +"</TD>");
		out.println("</TR>");
		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
		out.println("<TD COLSPAN = \"8\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp; </TD>");
		out.println("</TR>");
		
	}
	private void printSalesGroupFooter(
			long lNumberOfOrdersForSalesGroup, 
			BigDecimal bdAverageOrderAmountForSalesGroup,
			String sSalesGroup,
			BigDecimal bdTotalAmountForSalesGroup,
			PrintWriter out
			){
		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
		out.println("<TD COLSPAN = \"7\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"><FONT COLOR = \"BLUE\" >SALES GROUP TOTALS * * * </FONT>Orders for sales group " + sSalesGroup  +":</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"> " + Long.toString(lNumberOfOrdersForSalesGroup) +"</TD>");
		out.println("</TR>");
		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
		out.println("<TD COLSPAN = \"7\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Avg. order amount:</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"> " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAverageOrderAmountForSalesGroup) +"</TD>");
		out.println("</TR>");
		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
		out.println("<TD COLSPAN = \"7\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Total for sales group " + sSalesGroup  +":</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"> " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalAmountForSalesGroup) +"</TD>");
		out.println("</TR>");
		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
		out.println("<TD COLSPAN = \"8\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp; </TD>");
		out.println("</TR>");

	}
	private void printOrderTypeGrandTotals(
		ArrayList<String> arrOrderTypes,
		ArrayList<BigDecimal> arrOrderTypeTotals,
		ArrayList<Long> arrOrderTypeCounts,
		PrintWriter out
		) throws Exception{
		
		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
		out.println("<TD COLSPAN = \"7\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"><FONT COLOR = \"PURPLE\" >ORDER TYPE GRAND TOTALS * * * </FONT></TD>");
		out.println("<TD  CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">&nbsp;</TD>");
		out.println("</TR>");

		for (int i = 0; i < arrOrderTypes.size(); i++){
			BigDecimal bdAvgOrderAmt = arrOrderTypeTotals.get(i).divide(BigDecimal.valueOf(arrOrderTypeCounts.get(i)), 2, RoundingMode.HALF_UP);
			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
			out.println("<TD COLSPAN = \"7\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Orders for order type " + arrOrderTypes.get(i)  +":</TD>");
			out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> " + Long.toString(arrOrderTypeCounts.get(i)) +"</TD>");
			out.println("</TR>");
			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
			out.println("<TD COLSPAN = \"7\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Avg. order amount:</TD>");
			out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAvgOrderAmt) +"</TD>");
			out.println("</TR>");
			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
			out.println("<TD COLSPAN = \"7\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Total for sales group " + arrOrderTypes.get(i)  +":</TD>");
			out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(arrOrderTypeTotals.get(i)) +"</TD>");
			out.println("</TR>");
		}
		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
		out.println("<TD COLSPAN = \"8\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp; </TD>");
		out.println("</TR>");
	}
	private void printReportFooter(
			long lTotalNumberOfOrders, 
			BigDecimal bdTotalAverageOrderAmount,
			BigDecimal bdTotalAmount,
			PrintWriter out
			){
		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
		out.println("<TD COLSPAN = \"7\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"><FONT COLOR=RED>GRAND TOTALS * * * </FONT>Total Orders: </TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> " + Long.toString(lTotalNumberOfOrders) +"</TD>");
		out.println("</TR>");
		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
		out.println("<TD COLSPAN = \"7\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Avg. order amount:</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalAverageOrderAmount) +"</TD>");
		out.println("</TR>");
		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
		out.println("<TD COLSPAN = \"7\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Company Total :</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalAmount) +"</TD>");
		out.println("</TR>");
		out.println("</TABLE>");

	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
