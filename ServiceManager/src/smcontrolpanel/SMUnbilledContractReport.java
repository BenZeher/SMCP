package smcontrolpanel;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import SMClasses.SMLogEntry;
import SMClasses.SMOrderDetail;
import SMClasses.SMWorkOrderHeader;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableicitemlocations;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesalesgroups;
import SMDataDefinition.SMTablesalesperson;
import SMDataDefinition.SMTableworkorders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsStringFunctions;

public class SMUnbilledContractReport extends java.lang.Object{
	public static final String NONE_SELECTED = "NONESELECTED";
	private static final String INITIAL_VALUE = "**INITIALVALUE**";
	private static final int NUMBER_OF_COLUMNS_IN_DETAIL = 11;
	
	public SMUnbilledContractReport(	){
	}
	public void processReport(
			String sDBID,
			ServletContext context,
			String sUserID,
			String sUserFirstName,
			String sUserLastName,
			String sCompanyName,
			ArrayList <String>arSalesGroups,
			ArrayList <String>arServiceTypes,
			ArrayList <String>arSalespersons,
			boolean bShowActive,
			boolean bShowStanding,
			boolean bShowIndividualOrders,
			boolean bShowWorkOrders,
			//boolean bShowStatistics,
			PrintWriter out,
			String sLicenseModuleLevel
	) throws Exception{

		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sDBID, 
			"MySQL", 
			SMUtilities.getFullClassName(this.toString()) 
			+ ".processReport - userID: " 
			+ sUserID
			+ " - "
			+ sUserFirstName
			+ " "
			+ sUserLastName
			
				);
		
		if (conn == null){
			throw new Exception("Could not get data connection");
		}

		printReport(
				conn, 
				sUserID,
				sCompanyName,
				arSalesGroups, 
				arServiceTypes, 
				arSalespersons, 
				bShowActive, 
				bShowStanding, 
				bShowIndividualOrders,
				bShowWorkOrders,
				//bShowStatistics,
				sDBID, 
				context,
				out,
				sLicenseModuleLevel);
		
	    SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(conn);
	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMPRINTUNBILLEDCONTRACTREPORT, "REPORT", "SMUnbilledContractReport", "[1429043469]");


		clsDatabaseFunctions.freeConnection(context, conn, "[1547080674]");
		return;
	}
	private void printReport(
			Connection conn, 
			String sUserID,
			String sCompanyName,
			ArrayList <String>arSalesGroups,
			ArrayList <String>arServiceTypes,
			ArrayList <String>arSalespersons,
			boolean bShowActive,
			boolean bShowStanding,
			boolean bShowIndividualOrders,
			boolean bShowWorkOrders,
			//boolean bShowStatistics,
			String sDBID,
			ServletContext context,
			PrintWriter out,
			String sLicenseModuleLevel) throws Exception{
		
		String s = "";

		long iStartingTime = System.currentTimeMillis();
		boolean bAllowItemViewing = 
				SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.ICDisplayItemInformation,
					sUserID,
					conn,
					sLicenseModuleLevel
			);

		boolean bViewOrderInformationAllowed = 
			SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMViewOrderInformation,
				sUserID,
				conn,
				sLicenseModuleLevel
			);
		
		boolean bEditWorkOrderAllowed = 
			SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMEditWorkOrders,
					sUserID,
					conn,
					sLicenseModuleLevel
				);
		
		boolean bAllowOrderEditing = 
				SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.SMEditOrders,
						sUserID,
						conn,
						sLicenseModuleLevel
					);
		//Set up the style scripts:
		//add bookmark to statistics
		s += "<a href=\"#Statistics\"><strong>GO TO STATISTICS</strong></a>";
		//Set up the table:
		
		String SQL = getMainSQLCommand(bShowActive, bShowStanding, arSalesGroups, arServiceTypes, arSalespersons, sUserID, sCompanyName);

		String sLastOrderMarker = INITIAL_VALUE;
		String sLastSalespersonMarker = INITIAL_VALUE;
		String sLastSalesGroupMarker = INITIAL_VALUE;
		String sLastServiceTypeMarker = INITIAL_VALUE;
		String sLastOrderBillTo = "";
		String sLastOrderShipTo = "";
		BigDecimal bdCalculatedCostPerLine = new BigDecimal("0.00");
		
		BigDecimal bdOrderCostAccumulator = new BigDecimal("0.00");
		BigDecimal bdOrderPriceAccumulator = new BigDecimal("0.00");
		
		BigDecimal bdSalespersonCostAccumulator = new BigDecimal("0.00");
		BigDecimal bdSalespersonPriceAccumulator = new BigDecimal("0.00");
		int iSalespersonOrderCount = 0;
		
		BigDecimal bdSalesGroupCostAccumulator = new BigDecimal("0.00");
		BigDecimal bdSalesGroupPriceAccumulator = new BigDecimal("0.00");
		int iSalesGroupOrderCount = 0;
		
		BigDecimal bdServiceTypeCostAccumulator = new BigDecimal("0.00");
		BigDecimal bdServiceTypePriceAccumulator = new BigDecimal("0.00");
		int iServiceTypeOrderCount = 0;
		
		boolean bOddRow = true;
		
		//Salesperson accumulators:
		ArrayList <String>arrSalespersons = new ArrayList<String>(0);
		ArrayList <Integer>arrSalespersonsOrderCount = new ArrayList<Integer>(0);
		ArrayList <BigDecimal>arrSalespersonsCost = new ArrayList<BigDecimal>(0);
		ArrayList <BigDecimal>arrSalespersonsPrice = new ArrayList<BigDecimal>(0);

		//Sales group accumulators:
		ArrayList <String>arrSalesGroups = new ArrayList<String>(0);
		ArrayList <Integer>arrSalesGroupOrderCount = new ArrayList<Integer>(0);
		ArrayList <BigDecimal>arrSalesGroupsCost = new ArrayList<BigDecimal>(0);
		ArrayList <BigDecimal>arrSalesGroupsPrice = new ArrayList<BigDecimal>(0);

		//Service type accumulators:
		ArrayList <String>arrServiceTypes = new ArrayList<String>(0);
		ArrayList <Integer>arrServiceTypeOrderCount = new ArrayList<Integer>(0);
		ArrayList <BigDecimal>arrServiceTypesCost = new ArrayList<BigDecimal>(0);
		ArrayList <BigDecimal>arrServiceTypesPrice = new ArrayList<BigDecimal>(0);

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				//If we are on the next sales group, record the sales group accumulators and clear them:
				if (
					(sLastSalesGroupMarker.compareToIgnoreCase(
						rs.getString(SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupCode) + " " 
						+ rs.getString(SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupDesc)
					) != 0)
					&& (sLastSalesGroupMarker.compareToIgnoreCase(INITIAL_VALUE) != 0)	
				){
					accumulateStatistic(
						arrSalesGroups,
						arrSalesGroupOrderCount,
						arrSalesGroupsCost,
						arrSalesGroupsPrice,
						sLastSalesGroupMarker,
						bdSalesGroupCostAccumulator,
						bdSalesGroupPriceAccumulator,
						iSalesGroupOrderCount
					);
					bdSalesGroupCostAccumulator = BigDecimal.ZERO;
					bdSalesGroupPriceAccumulator = BigDecimal.ZERO;
					iSalesGroupOrderCount = 0;
				}
				
				//If we are on the next service type, record the service type accumulators and clear them:
				if (
					(sLastServiceTypeMarker.compareToIgnoreCase(rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCodeDescription)) != 0)
					&& (sLastServiceTypeMarker.compareToIgnoreCase(INITIAL_VALUE) != 0)	
				){
					accumulateStatistic(
						arrServiceTypes,
						arrServiceTypeOrderCount,
						arrServiceTypesCost,
						arrServiceTypesPrice,
						sLastServiceTypeMarker,
						bdServiceTypeCostAccumulator,
						bdServiceTypePriceAccumulator,
						iServiceTypeOrderCount
					);
					bdServiceTypeCostAccumulator = BigDecimal.ZERO;
					bdServiceTypePriceAccumulator = BigDecimal.ZERO;
					iServiceTypeOrderCount = 0;
				}
				
				
				//If we are on the next order:
				if (rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber).compareToIgnoreCase(sLastOrderMarker) != 0){
					//Print the totals from the previous order:
					if(bShowIndividualOrders){
						s += printOrderFooter(rs, bdOrderCostAccumulator, bdOrderPriceAccumulator, sLastOrderMarker, sLastOrderBillTo, sLastOrderShipTo);
						bOddRow = true;
					}
					//If we are on to the next salesperson, then print the salesperson totals:
					if (
						(sLastSalespersonMarker.compareToIgnoreCase(rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson) + " " + rs.getString("SALESPERSONNAME")) != 0)
						&& (sLastSalespersonMarker.compareTo(INITIAL_VALUE) != 0)	
					){
						if(bShowIndividualOrders){
							s += printSalespersonFooter(sLastSalespersonMarker, bdSalespersonCostAccumulator, bdSalespersonPriceAccumulator, iSalespersonOrderCount);
							bOddRow = true;
						}
						//Pick up the accumulators for this salesperson:
						accumulateStatistic(
							arrSalespersons,
							arrSalespersonsOrderCount,
							arrSalespersonsCost,
							arrSalespersonsPrice,
							sLastSalespersonMarker,
							bdSalespersonCostAccumulator,
							bdSalespersonPriceAccumulator,
							iSalespersonOrderCount
						);
						
						bdSalespersonCostAccumulator = BigDecimal.ZERO;
						bdSalespersonPriceAccumulator = BigDecimal.ZERO;
						iSalespersonOrderCount = 0;
					}
					
					//Next print the header for THIS order:
					//Increment the order counters:
					iSalespersonOrderCount += 1;
					iSalesGroupOrderCount += 1;
					iServiceTypeOrderCount += 1;
					if(bShowIndividualOrders){
						s += printOrderHeader(
							rs, 
							bViewOrderInformationAllowed,
							bEditWorkOrderAllowed,
							bShowWorkOrders,
							context,
							sDBID,
							conn,
							iSalespersonOrderCount
						);
					}
					//Reset the accumulators:
					bdOrderCostAccumulator = new BigDecimal("0.00");
					bdOrderPriceAccumulator = new BigDecimal("0.00");
					
					//Print the detail header:
					if(bShowIndividualOrders){
						s += printOrderDetailHeader();
					}
				}

				//Determine the 'calculated cost' here:
				bdCalculatedCostPerLine = getCalculatedCost(rs);
				
				//Now print the details for this order
				if (bShowIndividualOrders){
					try {
						s += printOrderDetail(rs, bAllowItemViewing, bAllowOrderEditing, sDBID, context, bOddRow);
					} catch (Exception e) {
						throw new Exception(e.getMessage());
					}
				}
				sLastOrderMarker = rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber);
				sLastOrderBillTo = rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName);
				sLastOrderShipTo = rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName);
				sLastSalespersonMarker = rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson) + " " + rs.getString("SALESPERSONNAME");
				sLastSalesGroupMarker = rs.getString(SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupCode) + " " 
					+ rs.getString(SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupDesc);
				sLastServiceTypeMarker = rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCodeDescription);
				
				//Accumulate:
				//Order accumulators
				bdOrderCostAccumulator = bdOrderCostAccumulator.add(bdCalculatedCostPerLine);
				bdOrderPriceAccumulator = bdOrderPriceAccumulator.add(BigDecimal.valueOf(rs.getDouble("ExtPrice")));
				
				//Salesperson accumulators:
				bdSalespersonCostAccumulator = bdSalespersonCostAccumulator.add(bdCalculatedCostPerLine);
				bdSalespersonPriceAccumulator = bdSalespersonPriceAccumulator.add(BigDecimal.valueOf(rs.getDouble("ExtPrice")));
				
				//Sales group accumulators:
				bdSalesGroupCostAccumulator = bdSalesGroupCostAccumulator.add(bdCalculatedCostPerLine);
				bdSalesGroupPriceAccumulator = bdSalesGroupPriceAccumulator.add(BigDecimal.valueOf(rs.getDouble("ExtPrice")));
				
				//Service type accumulators:
				bdServiceTypeCostAccumulator = bdServiceTypeCostAccumulator.add(bdCalculatedCostPerLine);
				bdServiceTypePriceAccumulator = bdServiceTypePriceAccumulator.add(BigDecimal.valueOf(rs.getDouble("ExtPrice")));
				
				bOddRow = !bOddRow;
				//Flush the output:
				out.println(s);
				s = "";
			}
			//Print the last footer:
			if(bShowIndividualOrders){
				out.println(printOrderFooter(rs, bdOrderCostAccumulator, bdOrderPriceAccumulator, sLastOrderMarker, sLastOrderBillTo, sLastOrderShipTo));
			}
			//Pick up the accumulators for the LAST salesperson:
			accumulateStatistic(
					arrSalespersons,
					arrSalespersonsOrderCount,
					arrSalespersonsCost,
					arrSalespersonsPrice,
					sLastSalespersonMarker,
					bdSalespersonCostAccumulator,
					bdSalespersonPriceAccumulator,
					iSalespersonOrderCount
				);
			
			//Pick up the accumulators for the last sales group:
			accumulateStatistic(
					arrSalesGroups,
					arrSalesGroupOrderCount,
					arrSalesGroupsCost,
					arrSalesGroupsPrice,
					sLastSalesGroupMarker,
					bdSalesGroupCostAccumulator,
					bdSalesGroupPriceAccumulator,
					iSalesGroupOrderCount
				);
			//Pick up the accumulators for the last service type:
			accumulateStatistic(
					arrServiceTypes,
					arrServiceTypeOrderCount,
					arrServiceTypesCost,
					arrServiceTypesPrice,
					sLastServiceTypeMarker,
					bdServiceTypeCostAccumulator,
					bdServiceTypePriceAccumulator,
					iServiceTypeOrderCount
				);
			
			rs.close();
		
		} catch (Exception e) {
			throw new Exception("Error [1430856206] reading records with SQL '" + SQL + "' - " + e.getMessage() + ".");
		}
		
		s += "</TABLE>";
		
	    if (bShowIndividualOrders){
		    s += "<HR ALIGN=LEFT WIDTH=100%>";
		    s += "<A NAME=\"COSTEXPL\"><B>*</B> \"Note about \"Extended Cost\":&nbsp;"
		    	+ "For a given item on an order, if there are none of that item on hand at the location on the order,"
		    	+ " then the \"Extended Cost\" will appear as zero.  "
		    	+ "If there ARE any on hand, then the \"Extended Cost\" will be equal to the most recent cost TIMES the "
		    	+ "quantity on the order.  "
		    	+ "\"Most recent cost\" is the cost from the most recent purchase, but it may not equal the actual cost on hand at "
		    	+ " any given time.<BR>"
		    ;
		    s += "<A NAME=\"ITEMNOEXPL\"><B>*</B> An item number in red indicates that this item is no longer in the master inventory.<BR>";
	    }
		out.println(s);
		
		//if (bShowStatistics){
			out.println("<BR><U><B>STATISTICS</B></U><a name=\"Statistics\">");
			out.println("<BR>");
			//Show the salesperson statistics:
			printGroupStatistics("SALESPERSON", out, arrSalespersons, arrSalespersonsOrderCount, arrSalespersonsCost, arrSalespersonsPrice);

			out.println("<BR>");
			//Show the sales group statistics:
			printGroupStatistics("SALES GROUP", out, arrSalesGroups, arrSalesGroupOrderCount, arrSalesGroupsCost, arrSalesGroupsPrice);
			
			out.println("<BR>");
			//Show the sales group statistics:
			printGroupStatistics("SERVICE TYPE", out, arrServiceTypes, arrServiceTypeOrderCount, arrServiceTypesCost, arrServiceTypesPrice);

		//}
		BigDecimal bdStartingTime = new BigDecimal(iStartingTime);
		BigDecimal bdEndingTime = new BigDecimal(System.currentTimeMillis());
		BigDecimal bdProcessingTime = (bdEndingTime.subtract(bdStartingTime)).divide(new BigDecimal("1000.00"));
		out.println("Total processing time: <B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdProcessingTime) + "</B> seconds.");
		return;
	}
	private void accumulateStatistic(
			ArrayList <String>arrGroupNames,
			ArrayList <Integer>arrGroupOrderCount,
			ArrayList <BigDecimal>arrGroupCost,
			ArrayList <BigDecimal>arrGroupPrice,
			String sGroupName,
			BigDecimal bdCost,
			BigDecimal bdPrice,
			int iGroupOrderCount
			){
		
		boolean bGroupIsAlreadyListed = false;
		for (int i = 0; i < arrGroupNames.size(); i++){
			if(arrGroupNames.get(i).compareToIgnoreCase(sGroupName) == 0){
				arrGroupOrderCount.set(i, arrGroupOrderCount.get(i) + iGroupOrderCount);
				arrGroupCost.set(i, arrGroupCost.get(i).add(bdCost));
				arrGroupPrice.set(i, arrGroupPrice.get(i).add(bdPrice));
				bGroupIsAlreadyListed = true;
				break;
			}
		}
		if (!bGroupIsAlreadyListed){
			arrGroupNames.add(sGroupName);
			arrGroupOrderCount.add(iGroupOrderCount);
			arrGroupCost.add(bdCost);
			arrGroupPrice.add(bdPrice);
		}
	}
	private String getMainSQLCommand(
			boolean bShowActive,
			boolean bShowStanding,
			ArrayList <String>arSalesGroups,
			ArrayList <String>arServiceTypes,
			ArrayList <String>arSalespersons,
			String sUserID,
			String sCompany
			){
		String s = "SELECT" 
			+ " 'Unbilled Contract Report Main SQL Command' AS REPORTNAME"
			+ ", '" + sUserID + "' AS USERNAME"
			+ ", '" + sCompany + "' AS COMPANY"
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson 
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderDate
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datExpectedShipDate 
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber 
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType 
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName 
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName 
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sDefaultItemCategory 
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode 
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCodeDescription 
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate 
			+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.iLineNumber 
			+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.iDetailNumber
			+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.mInternalComments		
			+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered 
			+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dOrderUnitCost 
			+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemDesc 
			+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.mInternalComments 
			+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dExtendedOrderCost 
			+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered 
			+ " * " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dOrderUnitPrice + " AS ExtPrice" 
		    + ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber
		    + ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sLocationCode
		    + ", " + SMTableorderdetails.TableName +"." + SMTableorderdetails.dUniqueOrderID
		    + ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand
		    + ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sTotalCost
		    + ", " + SMTableicitems.TableName + "." + SMTableicitems.bdmostrecentcost
		    + ", " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
		    + ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.datDetailExpectedShipDate
			+ ", if (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + "=1, 'Active', if (" 
		    	+ SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + "=3, 'Standing', 'Unknown Order Type')) AS OrderType"
		    + ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup
		    + ", " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupCode
		    + ", " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupDesc
		    + ", IF (" + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode + " IS NULL, '(Name not found)'," 
		    	+ " CONCAT(" + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonFirstName + ", "
				+ "' ', " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonLastName
				+ ")) AS SALESPERSONNAME" 
				
			+ " FROM " + SMTableorderdetails.TableName + " LEFT JOIN " + SMTableorderheaders.TableName 
			+ " ON " + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber + " = "
			+ SMTableorderdetails.TableName + "." + SMTableorderdetails.strimmedordernumber
			
			//Join the sales groups table
			+ " LEFT JOIN " + SMTablesalesgroups.TableName + " ON " 
			+ SMTablesalesgroups.TableName + "." + SMTablesalesgroups.iSalesGroupId + " = "
			+ SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup 
		 	
			//Join the salespersons table:
			+ " LEFT JOIN " + SMTablesalesperson.TableName 
			+ " ON " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson + " = " 
			+ SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode
			
			//Join the item locations table:
			+ " LEFT JOIN " + SMTableicitemlocations.TableName
			+ " ON (" + SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber + "=" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber + ")"
			+ " AND (" + SMTableorderdetails.TableName + "." + SMTableorderdetails.sLocationCode + "=" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation + ")"
			
			//Join the items table:
			+ " LEFT JOIN " + SMTableicitems.TableName
			+ " ON " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber + " = " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			
			+ " WHERE (" 
		 		//NO QUOTES!
				+ " (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + " != "
					+ SMTableorderheaders.ORDERTYPE_QUOTE + ")"
				+ " AND (" + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered + " != 0)"
				+ " AND (" 
		 			+ "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + " = '1899/12/31')"
		 			+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + " = '0000-00-00 00:00:00')"
		 		+ ")";

				if (arSalesGroups.size() > 0){
					s += " AND ("; 
					for (int i = 0; i < arSalesGroups.size(); i++){
						s = s + "(" + SMTableorderheaders.iSalesGroup + " = " + arSalesGroups.get(i) + ") OR";
					}
					s = s.substring(0, s.length() - " OR".length()) + ")";
				}else{
					s += " AND (" + SMTableorderheaders.iSalesGroup + " = '" + NONE_SELECTED + "')";
				}
	
				if (arServiceTypes.size() > 0){
					s += " AND ("; 
					for (int i = 0; i < arServiceTypes.size(); i++){
						s += "(" + SMTableorderheaders.sServiceTypeCode + " = '" + arServiceTypes.get(i) + "') OR";
					}
					s = s.substring(0, s.length() - " OR".length()) + ")";
				}else{
					s += " AND (" + SMTableorderheaders.sServiceTypeCode + " = '" + NONE_SELECTED + "')";
				}
				
				if (arSalespersons.size() > 0){
					s += " AND ("; 
					for (int i = 0; i < arSalespersons.size(); i++){
						s += "(" + SMTableorderheaders.sSalesperson + " = '" + arSalespersons.get(i) + "') OR";
					}
					s = s.substring(0, s.length() - " OR".length()) + ")";
				}else{
					s += " AND (" + SMTableorderheaders.sSalesperson + " = '" + NONE_SELECTED + "')";
				}
		
				s += " AND ((1=0)";
				if (bShowActive){
					s += " OR (" + SMTableorderheaders.iOrderType + " = 1)";
				}
				if (bShowStanding){
					s += " OR (" + SMTableorderheaders.iOrderType + " = 3)";
				}
				s += ")";
			s += ")" //END 'WHERE' CLAUSE
				+ " ORDER BY " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson
				+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderDate
				+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
				+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.iLineNumber
			;
			
		
			
			/*
			Alternate formula - saved a little but not much:
			SELECT
			 'Unbilled Contract Report Main SQL Command' AS REPORTNAME
			, 'airo' AS USERNAME
			, orderheaders.sSalesperson
			, orderheaders.datOrderDate
			, orderheaders.strimmedordernumber
			, orderheaders.datExpectedShipDate
			, orderheaders.sOrderNumber
			, orderheaders.iOrderType
			, orderheaders.sBillToName
			, orderheaders.sShipToName
			, orderheaders.sDefaultItemCategory
			, orderheaders.sServiceTypeCode
			, orderheaders.sServiceTypeCodeDescription
			, orderheaders.datOrderCanceledDate
			, DETAILQUERY.iLineNumber
			, DETAILQUERY.iDetailNumber
			, DETAILQUERY.minternalcomments
			, DETAILQUERY.dQtyOrdered
			, DETAILQUERY.dOrderUnitCost
			, DETAILQUERY.sItemDesc
			, DETAILQUERY.minternalcomments
			, DETAILQUERY.dExtendedOrderCost
			, DETAILQUERY.ExtPrice
			, DETAILQUERY.sItemNumber
			, DETAILQUERY.sLocationCode
			, DETAILQUERY.dUniqueOrderID
			, icitemlocations.bdqtyonhand
			, icitemlocations.bdtotalcost
			, icitems.bdmostrecentcost
			, icitems.sitemnumber
			, DETAILQUERY.datDetailExpectedShipDate
			, if (orderheaders.iOrderType=1, 'Active', if (orderheaders.iOrderType=3, 'Standing', 'Unknown Order Type')) AS OrderType
			, orderheaders.iSalesGroup
			, salesgroups.sSalesGroupCode
			, salesgroups.sSalesGroupDesc
			, IF (salesperson.sSalespersonCode IS NULL, '(Name not found)', CONCAT(salesperson.sSalespersonFirstName, ' ', salesperson.sSalespersonLastName)) AS SALESPERSONNAME
			 FROM 
			( SELECT
			 orderdetails.iLineNumber
			, orderdetails.iDetailNumber
			, orderdetails.minternalcomments
			, orderdetails.dQtyOrdered
			, orderdetails.dOrderUnitCost
			, orderdetails.sItemDesc
			, orderdetails.dExtendedOrderCost
			, orderdetails.dQtyOrdered * orderdetails.dOrderUnitPrice AS ExtPrice
			, orderdetails.sItemNumber
			, orderdetails.sLocationCode
			, orderdetails.dUniqueOrderID
			, orderdetails.datDetailExpectedShipDate
			, orderdetails.strimmedordernumber
			 FROM orderdetails
			 WHERE (
			(orderdetails.dQtyOrdered > 0.00)
			)
			
			) AS DETAILQUERY
			
			 LEFT JOIN orderheaders
			 ON orderheaders.strimmedordernumber = DETAILQUERY.strimmedordernumber
			 LEFT JOIN salesgroups ON salesgroups.iSalesGroupId = orderheaders.iSalesGroup
			 LEFT JOIN salesperson ON orderheaders.sSalesperson = salesperson.sSalespersonCode
			 LEFT JOIN icitemlocations ON (DETAILQUERY.sItemNumber=icitemlocations.sitemnumber) AND (DETAILQUERY.sLocationCode=icitemlocations.slocation)
			 LEFT JOIN icitems ON DETAILQUERY.sItemNumber = icitems.sitemnumber
			 WHERE (
			 (orderheaders.iOrderType != 4)
			 AND ((orderheaders.datOrderCanceledDate = '1899/12/31') OR (orderheaders.datOrderCanceledDate = '0000-00-00 00:00:00'))
			 AND ((iSalesGroup = 0) OR(iSalesGroup = 1) OR(iSalesGroup = 2) OR(iSalesGroup = 3))
			 AND ((sServiceTypeCode = 'SH0001') OR(sServiceTypeCode = 'SH0002') OR(sServiceTypeCode = 'SH0003') OR(sServiceTypeCode = 'SH0004'))
			 AND ((sSalesperson = '') OR(sSalesperson = '101') OR(sSalesperson = '104') OR(sSalesperson = '105') OR(sSalesperson = '106') OR(sSalesperson = '107') OR(sSalesperson = '108') OR(sSalesperson = '109') OR(sSalesperson = '111') OR(sSalesperson = '112') OR(sSalesperson = '113') OR(sSalesperson = '114') OR(sSalesperson = '115') OR(sSalesperson = '117') OR(sSalesperson = '118') OR(sSalesperson = '120') OR(sSalesperson = '122') OR(sSalesperson = '123') OR(sSalesperson = '124') OR(sSalesperson = '125') OR(sSalesperson = '126') OR(sSalesperson = '127') OR(sSalesperson = '128') OR(sSalesperson = '129') OR(sSalesperson = '130') OR(sSalesperson = '131') OR(sSalesperson = '132') OR(sSalesperson = '133') OR(sSalesperson = '134') OR(sSalesperson = '135') OR(sSalesperson = '136') OR(sSalesperson = '137') OR(sSalesperson = '138') OR(sSalesperson = '139') OR(sSalesperson = '140') OR(sSalesperson = '142') OR(sSalesperson = '143') OR(sSalesperson = '145') OR(sSalesperson = '146') OR(sSalesperson = '147') OR(sSalesperson = '149') OR(sSalesperson = '150') OR(sSalesperson = '151') OR(sSalesperson = '200') OR(sSalesperson = '201') OR(sSalesperson = '202') OR(sSalesperson = '203') OR(sSalesperson = '204') OR(sSalesperson = '205') OR(sSalesperson = '206') OR(sSalesperson = '207') OR(sSalesperson = '208') OR(sSalesperson = '209') OR(sSalesperson = '210') OR(sSalesperson = '211') OR(sSalesperson = '212') OR(sSalesperson = '213') OR(sSalesperson = '215') OR(sSalesperson = '218') OR(sSalesperson = '219') OR(sSalesperson = '220') OR(sSalesperson = '255') OR(sSalesperson = 'ADC') OR(sSalesperson = 'AJB') OR(sSalesperson = 'BDC') OR(sSalesperson = 'BF') OR(sSalesperson = 'CLB') OR(sSalesperson = 'CLL') OR(sSalesperson = 'CLM') OR(sSalesperson = 'COG') OR(sSalesperson = 'CSW') OR(sSalesperson = 'DAD') OR(sSalesperson = 'DLH') OR(sSalesperson = 'DLW') OR(sSalesperson = 'DMS') OR(sSalesperson = 'DPW') OR(sSalesperson = 'EEC') OR(sSalesperson = 'GAF') OR(sSalesperson = 'GLH') OR(sSalesperson = 'GVB') OR(sSalesperson = 'HRH') OR(sSalesperson = 'JAR') OR(sSalesperson = 'JDT') OR(sSalesperson = 'JEN') OR(sSalesperson = 'JF') OR(sSalesperson = 'JGC') OR(sSalesperson = 'JGW') OR(sSalesperson = 'JH') OR(sSalesperson = 'JMH') OR(sSalesperson = 'JT') OR(sSalesperson = 'JYW') OR(sSalesperson = 'KB') OR(sSalesperson = 'KD') OR(sSalesperson = 'KDB') OR(sSalesperson = 'KLB') OR(sSalesperson = 'LDB') OR(sSalesperson = 'LFG') OR(sSalesperson = 'LJK') OR(sSalesperson = 'LNB') OR(sSalesperson = 'LSC') OR(sSalesperson = 'LSW') OR(sSalesperson = 'MAB') OR(sSalesperson = 'MAH') OR(sSalesperson = 'MAM') OR(sSalesperson = 'MCB') OR(sSalesperson = 'MFT') OR(sSalesperson = 'MN') OR(sSalesperson = 'MOB') OR(sSalesperson = 'MYS') OR(sSalesperson = 'NRH') OR(sSalesperson = 'NRP') OR(sSalesperson = 'PKB') OR(sSalesperson = 'RAM') OR(sSalesperson = 'RLH') OR(sSalesperson = 'RR') OR(sSalesperson = 'SCB') OR(sSalesperson = 'SEJ') OR(sSalesperson = 'SLS') OR(sSalesperson = 'SMH') OR(sSalesperson = 'SPO') OR(sSalesperson = 'SRH') OR(sSalesperson = 'TAC') OR(sSalesperson = 'TAO') OR(sSalesperson = 'TLM') OR(sSalesperson = 'TME') OR(sSalesperson = 'TNL') OR(sSalesperson = 'TPH') OR(sSalesperson = 'VJM'))
			 AND ((iOrderType = 1) OR (iOrderType = 3))
			)
			 ORDER BY orderheaders.sSalesperson, orderheaders.datOrderDate, orderheaders.strimmedordernumber, DETAILQUERY.iLineNumber

			 */
			//System.out.println("[1431105254] " + s);
		return s;
	}
	private String printSalespersonFooter(
			String sLastSalespersonMarker, 
			BigDecimal bdSalespersonCostAccumulator,
			BigDecimal bdSalespersonPriceAccumulator,
			int iOrdersPerSalesperson) throws Exception{
		String s = "";
		
		return s;
	}
	private String printOrderHeader(
			ResultSet rsOrder, 
			boolean bViewOrderInformationAllowed,
			boolean bEditWorkOrderAllowed,
			boolean bShowWorkOrders,
			ServletContext context, 
			String sDBID,
			Connection conn,
			int iSalespersonOrderCount) throws Exception{
		String s = "";
		 s += "<TABLE CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + " \" >";
		String sOrderNumberLink = rsOrder.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber);
		if (bViewOrderInformationAllowed){
			sOrderNumberLink = "<A href=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" +
			sOrderNumberLink + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sOrderNumberLink + "</A>";
		}
		//Get the list of schedule entries for this order:
		String sWorkOrdersLink = "";
		if(bShowWorkOrders){
			String SQL = "SELECT"
					+ " " + SMTableworkorders.datscheduleddate
					+ ", " + SMTableworkorders.lid
					+ ", " + SMTableworkorders.smechanicname
					+ " FROM " + SMTableworkorders.TableName
					+ " WHERE ("
						+ "(" + SMTableworkorders.strimmedordernumber + "='" + rsOrder.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber) + "')"
					+ ")"
					+ " ORDER BY " + SMTableworkorders.datscheduleddate
			;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				while (rs.next()){
					if (bEditWorkOrderAllowed){
						sWorkOrdersLink += "<A href=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMWorkOrderEdit?" + SMWorkOrderHeader.Paramlid + "="
							+ Integer.toString(rs.getInt(SMTableworkorders.lid)) 
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" 
							+ clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTableworkorders.datscheduleddate)) + " "
							+ rs.getString(SMTableworkorders.smechanicname)
							+ "</A>"
							+ ", ";
					}else{
						sWorkOrdersLink += 
							clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTableworkorders.datscheduleddate)) + " "
							+ rs.getString(SMTableworkorders.smechanicname)
							+ ", "
							;	
					}
				}
				rs.close();
			} catch (Exception e) {
				throw new Exception("Error [1431097957] reading work orders with SQL: '" + SQL + "' - " + e.getMessage() + ".");
			}
		}
		if (sWorkOrdersLink.compareToIgnoreCase("") != 0){
			sWorkOrdersLink = sWorkOrdersLink.substring(0, sWorkOrdersLink.length() - ", ".length());
		}
		s += "<TR CLASS  = \" " + SMMasterStyleSheetDefinitions.TABLE_HEADING + " \" >";
		s += "<TD COLSPAN = " + Integer.toString(NUMBER_OF_COLUMNS_IN_DETAIL) + " style = \" "
			+ "border: 1px solid; "
			+ "bordercolor: 000; "
			+ "padding: 2px; "
			+ "font-family : Arial; "
			+ "font-size: small; "
			//+ "font-weight: bold; "
			+ "text-align: left; "
			+ "vertical-align:bottom"
			+ " \" >"
			+ "<B>Order count:</B>&nbsp;"
			+ Integer.toString(iSalespersonOrderCount) + "&nbsp;"
			+ "<B>Salesperson:</B>&nbsp;"
			+ rsOrder.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson) + " - "
			+ rsOrder.getString("SALESPERSONNAME") + "&nbsp;"
			+ "<B>Order Date:</B>&nbsp;"
			+ clsDateAndTimeConversions.resultsetDateStringToString(rsOrder.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderDate)) + "&nbsp;"
			+ "<B>Sales group:</B>&nbsp;"
			+ rsOrder.getString(SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupCode) + "&nbsp;"
			+ "<B>Service Type:</B>&nbsp;"
			+ rsOrder.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCodeDescription) + "&nbsp;"
			+ "<B>Order type:</B>&nbsp;"
			+ rsOrder.getString("OrderType") + "&nbsp;"
			+ "<BR>"
			+ "<B>Order #:</B>&nbsp;"
			+ sOrderNumberLink + "&nbsp;"
			+ rsOrder.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName) + " - "
			+ rsOrder.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName)
			+ "&nbsp;<B>Expected ship date:</B>&nbsp;"
			+ clsDateAndTimeConversions.resultsetDateStringToString(rsOrder.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.datExpectedShipDate)) + "&nbsp;"
		;
		if (bShowWorkOrders){
			s += "<B>Scheduled for:</B>&nbsp;"
				+ sWorkOrdersLink + "&nbsp;";
		}
		s += "</TD>";
		s += "</TR>";
		return s;
	}
	private String printOrderFooter(ResultSet rsOrder,
			BigDecimal bdOrderCostAccumulator,
			BigDecimal bdOrderPriceAccumulator,
			String sLastOrderMarker,
			String sBillTo,
			String sShipTo) throws Exception{
		String s = "";
		if (sLastOrderMarker.compareToIgnoreCase(INITIAL_VALUE) == 0){
			return s;
		}
		s += "<TR CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\" >";
		s += "<TD COLSPAN = " + Integer.toString(NUMBER_OF_COLUMNS_IN_DETAIL - 2) + " CLASS = \" " +SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL +" \" ><B>"
				+ "TOTALS FOR ORDER #" + sLastOrderMarker
				+ " " + sBillTo + " - " + sShipTo
				+ ":"
				+ "</B></TD>"
		;
		s += "<TD CLASS = \" "+SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL+" \" ><B>"
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdOrderCostAccumulator)
				+ "</B></TD>"
		;
		s += "<TD CLASS = \" "+SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL+" \" ><B>"
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdOrderPriceAccumulator)
				+ "</B></TD>"
		;
		s += "</TR>";
		
		s += "<TR><TD COLSPAN = " + Integer.toString(NUMBER_OF_COLUMNS_IN_DETAIL) + ">&nbsp;</TD></TR>";
		s += "</TABLE>";
		return s;
	}
	private String printOrderDetailHeader(){
		String s = "";
		s += "<TR CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_HEADING + " \" >";
		
		s += "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" ><B>"
				+ "Line"
				+ "</B></TD>"
			;
		
		s += "<TD CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL +  "  \" ><B>"
			+ "Item"
			+ "</B></TD>"
		;

		s += "<TD CLASS = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL+" \" ><B>"
			+ "Description"
			+ "</B></TD>"
		;

		s += "<TD CLASS = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL+" \" ><B>"
			+ "Expected<BR>Ship&nbsp;Date"
			+ "</B></TD>"
		;

		s += "<TD CLASS = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL+" \" ><B>"
				+ "Location"
				+ "</B></TD>"
			;
		
		s += "<TD CLASS = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL+" \" ><B>"
			+ "Qty&nbsp;On&nbsp;Hand<BR>At&nbsp;Location"
			+ "</B></TD>"
		;

		s += "<TD CLASS = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL+" \" ><B>"
				+ "Total&nbsp;Cost&nbsp;On<BR>Hand&nbsp;At&nbsp;Location"
				+ "</B></TD>"
			;

		s += "<TD CLASS = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL+" \" ><B>"
				+ "Qty<BR>Ordered"
				+ "</B></TD>"
			;
		
		s += "<TD CLASS = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL+" \" ><B>"
				+ "Recent<BR>Unit&nbsp;Cost"
				+ "</B></TD>"
			;
		
		s += "<TD CLASS = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL+" \" ><B>"
				+ "Calculated<BR>Extended&nbsp;Cost"
				+ "&nbsp;<A HREF=\"#COSTEXPL\">?</A>"
				+ "</B></TD>"
			;
		
		s += "<TD CLASS = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL+" \" ><B>"
				+ "Billing<BR>Value"
				+ "</B></TD>"
			;

		s += "</TR>";
		return s;
	}
	private String printOrderDetail(
			ResultSet rsOrder,
			boolean bAllowItemInformationViewing,
			boolean bAllowOrderEditing,
			String sDBID,
			ServletContext context,
			boolean bOddRow) throws Exception{
		String s = "";
		String sBackgroundColor = SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN ;
		if (bOddRow){
			sBackgroundColor = SMMasterStyleSheetDefinitions.TABLE_ROW_ODD;
		}
		s += "<TR CLASS = \"" + sBackgroundColor +  " \" >";
		
		//Line number
		String sLineNumberLink = clsStringFunctions.PadLeft(Integer.toString(rsOrder.getInt(SMTableorderdetails.TableName + "." + SMTableorderdetails.iLineNumber)), "0", 4);
		if (bAllowOrderEditing){
			sLineNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context)
				+ "smcontrolpanel.SMEditOrderDetailEdit"
				+ "?" + SMOrderDetail.ParamiDetailNumber + "=" + Integer.toString(rsOrder.getInt(SMTableorderdetails.TableName + "." + SMTableorderdetails.iDetailNumber))
				+ "&" + SMOrderDetail.Paramstrimmedordernumber + "=" + rsOrder.getString(SMTableorderheaders.TableName +"." + SMTableorderheaders.strimmedordernumber)
				+ "&" + SMOrderDetail.ParamiLineNumber + "=" + Integer.toString(rsOrder.getInt(SMTableorderdetails.TableName + "." + SMTableorderdetails.iLineNumber))
				+ "&" + SMOrderDetail.ParamdUniqueOrderID + "=" + Long.toString(rsOrder.getLong(SMTableorderdetails.TableName +"." + SMTableorderdetails.dUniqueOrderID))
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">"
				+ sLineNumberLink
				+ "</A>"
			;
		}
		s += "<TD CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
			+ sLineNumberLink
			+ "</TD>"
		;
		
		//Item number:
		boolean bItemRecordIsMissing = rsOrder.getString(SMTableicitems.TableName + "." + SMTableicitems.sItemNumber) == null;
		
		String sItemNumberLink = rsOrder.getString(SMTableorderdetails.TableName + "." +SMTableorderdetails.sItemNumber);
		if (bAllowItemInformationViewing){
			sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
			+ "smic.ICDisplayItemInformation?ItemNumber=" 
			+ sItemNumberLink
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
			+ "\">" + sItemNumberLink + "</A>";
		}
		if (bItemRecordIsMissing){
			sItemNumberLink = "<FONT COLOR=RED>" + sItemNumberLink + "<A HREF=\"#ITEMNOEXPL\">?</A></B></FONT>";
		}
		s += "<TD CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ sItemNumberLink
				+ "</TD>"
		;

		//Description:
		s += "<TD CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ rsOrder.getString(SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemDesc)
				+ "</TD>"
		;
		
		//Expected ship date:
		s += "<TD CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ clsDateAndTimeConversions.resultsetDateStringToString(rsOrder.getString(SMTableorderdetails.TableName + "." + SMTableorderdetails.datDetailExpectedShipDate))
				+ "</TD>"
		;
		
		//Location:
		s += "<TD CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ rsOrder.getString(SMTableorderdetails.TableName + "." + SMTableorderdetails.sLocationCode)
				+ "</TD>"
		;
			
		//Qty on hand at location:
		s += "<TD CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ clsManageBigDecimals.doubleToDecimalFormat(rsOrder.getDouble(SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand), SMTableorderdetails.dQtyOrderedScale)
				+ "</TD>"
		;
		
		//Total cost on hand at location:
		s += "<TD CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ clsManageBigDecimals.doubleToDecimalFormat(rsOrder.getDouble(SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sTotalCost), 2)
				+ "</TD>"
		;
		
		//Qty ordered:
		s += "<TD CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ clsManageBigDecimals.doubleToDecimalFormat(rsOrder.getDouble(SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered), SMTableorderdetails.dQtyOrderedScale)
				+ "</TD>"
		;
		
		//Recent cost:
		s += "<TD CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ clsManageBigDecimals.doubleTo2DecimalSTDFormat(rsOrder.getDouble(SMTableicitems.TableName + "." + SMTableicitems.bdmostrecentcost))
				+ "</TD>"
		;
		
		//Extended cost:
		s += "<TD CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(getCalculatedCost(rsOrder))
				+ "</TD>"
		;
		
		//Billing value:
		s += "<TD CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ clsManageBigDecimals.doubleTo2DecimalSTDFormat(rsOrder.getDouble("ExtPrice"))
				+ "</TD>"
		;
		
		s += "</TR>";
		return s;
	}
	private BigDecimal getCalculatedCost(ResultSet rsOrder) throws Exception{
		BigDecimal bdExtendedCost = new BigDecimal("0.00");
		BigDecimal bdQtyOrdered;
		BigDecimal bdMostRecentCost;
		BigDecimal bdQtyOnHand;
		try {
			bdQtyOrdered = new BigDecimal(clsManageBigDecimals.doubleToDecimalFormat(
				rsOrder.getDouble(SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered), SMTableorderdetails.dQtyOrderedScale).replace(",", ""));
			bdMostRecentCost = new BigDecimal(clsManageBigDecimals.doubleToDecimalFormat(
				rsOrder.getDouble(SMTableicitems.TableName + "." + SMTableicitems.bdmostrecentcost), SMTableicitems.bdmostrecentcostScale).replace(",", ""));
			bdQtyOnHand = new BigDecimal(clsManageBigDecimals.doubleToDecimalFormat(
				rsOrder.getDouble(SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand), SMTableorderdetails.dQtyOrderedScale).replace(",", ""));
		} catch (Exception e) {
			throw new Exception("Error [1431027064] getting calculated cost - " + e.getMessage());
		}
		if (bdQtyOnHand.compareTo(BigDecimal.ZERO) != 0){
			bdExtendedCost = bdQtyOrdered.multiply(bdMostRecentCost);
		}
		return bdExtendedCost;
	}
	private void printGroupStatistics(
			String sTitle,
			PrintWriter out, 
			ArrayList<String>arrGroupNames,
			ArrayList<Integer>arrGroupOrderCount, 
			ArrayList<BigDecimal>arrGroupCost, 
			ArrayList<BigDecimal>arrGroupPrice) throws Exception {

		int iTotalOrderCount = 0;
		BigDecimal bdTotalCost = new BigDecimal("0.00");
		BigDecimal bdTotalPrice = new BigDecimal("0.00");
		
		//System.out.println("[1431351974] arrSalespersons.size() = " + arrSalespersons.size());
		String s = "<TABLE CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + " \" >";
		s += "<TR CLASS = \""+SMMasterStyleSheetDefinitions.TABLE_BREAK +"\">"
			+ "<TD WIDTH=25% CLASS = \" " +SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" style = \" font-weight:bold; color:white; \" >"
			+ sTitle
			+ "</TD>"
			+ "<TD  WIDTH=25% CLASS = \" " +SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL +" \" style = \" font-weight:bold; color:white; \" >"
			+ "NO. OF ORDERS"
			+ "</TD>"
			+ "<TD  WIDTH=25% CLASS = \" "+ SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL +" \" style = \" font-weight:bold; color:white; \" >"
			+ "TOTAL CALCULATED COST"
			+ "</TD>"
			+ "<TD  WIDTH=25% CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL +  "  \" style = \" font-weight:bold; color:white; \" >"
			+ "TOTAL BILLING VALUE"
			+ "</TD>"
			+ "<TR>"
		;
		boolean bOddRow = true;
		for (int i = 0; i < arrGroupNames.size(); i++){
			String sBackgroundColor = SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN;
			if (bOddRow){
				sBackgroundColor = SMMasterStyleSheetDefinitions.TABLE_ROW_ODD;
			}
			s += "<TR CLASS = \"" + sBackgroundColor + "\" >"
				+ "<TD CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL +" \" >"
				+ arrGroupNames.get(i)
				+ "</TD>"
				+ "<TD CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "  \" >"
				+ Integer.toString(arrGroupOrderCount.get(i))
				+ "</TD>"
				+ "<TD CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "  \" >"
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(arrGroupCost.get(i))
				+ "</TD>"
				+ "<TD CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL+  "  \" >"
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(arrGroupPrice.get(i))
				+ "</TD>"
				+ "</TR>"
			;
			iTotalOrderCount += arrGroupOrderCount.get(i);
			bdTotalCost = bdTotalCost.add(arrGroupCost.get(i));
			bdTotalPrice = bdTotalPrice.add(arrGroupPrice.get(i));
			bOddRow = !bOddRow;
		}
		String sBackgroundColor = SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN;
		if (bOddRow){
			sBackgroundColor = SMMasterStyleSheetDefinitions.TABLE_ROW_ODD;
		}
		s += "<TR CLASS = \"" + sBackgroundColor + " \" >"
				+ "<TD CLASS = \" " +SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL+ " \" >"
				+ "<B>TOTALS</B>"
				+ "</TD>"
				+ "<TD CLASS = \" "+SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL+" \" >"
				+ "<B>" + Integer.toString(iTotalOrderCount) + "</B>"
				+ "</TD>"
				+ "<TD CLASS = \" "+SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL+" \" >"
				+ "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalCost) + "</B>"
				+ "</TD>"
				+ "<TD CLASS = \" "+SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL+" \" >"
				+ "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalPrice) + "</B>"
				+ "</TD>"
				+ "</TR>"
			;
		s += "</TABLE>";
		out.println(s);
	}

	
}
