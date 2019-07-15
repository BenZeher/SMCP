package smcontrolpanel;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTablemechanics;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesmoptions;
import SMDataDefinition.SMTableworkorders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsStringFunctions;
import ServletUtilities.clsNullOutputStream;
import TCSDataDefinition.Notes;
import TCSDataDefinition.SpecialEntryTypes;
import TCSDataDefinition.SpecialNoteTypes;
import TCSDataDefinition.TimeEntries;

public class SMJobCostDailyReport extends java.lang.Object{

	private String m_sErrorMessage;
	
	private BigDecimal bdTotalProductionHoursUsed = new BigDecimal(0);
	private BigDecimal bdTotalTravelHours = new BigDecimal(0);
	private String sTimeCardDatabase = "";
	private PrintWriter pwSuppressed;
	
	//Variables for mechanic average efficiencies:
	BigDecimal bdMechanicJobEfficiency = new BigDecimal(0); 
	BigDecimal bdMechanicNetEfficiency = new BigDecimal(0);
	BigDecimal bdMechanicJobEfficiencyProduct = new BigDecimal(0);
	BigDecimal bdChargedHoursForMechanic = new BigDecimal(0);
	BigDecimal bdHoursOnTheClockForMechanic = new BigDecimal(0);
	
	public SMJobCostDailyReport(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sStartingDate,
			String sEndingDate,
			ArrayList <String> sMechanics,
			String sDBID,
			String sUserID,
			boolean bSuppressDetail,
			PrintWriter out,
			ServletContext context,
			String sLicenseModuleLevel
			){

		//If there is output to be suppressed:
		if (bSuppressDetail){
			//pwSuppressed = new PrintWriter(System.out, true);
			//nullOutputStream nullout = new nullOutputStream();
			pwSuppressed = new PrintWriter(new clsNullOutputStream(), true);
		}else{
			pwSuppressed = out;
		}
		
		//Create string of mechanics:
		String sMechanicsString = "";
		for (int i = 0; i < sMechanics.size(); i++){
			sMechanicsString += "," 
				+ clsStringFunctions.PadRight(sMechanics.get(i), " ", SMTablemechanics.sMechInitialLength);
		}
		
    	//SQL Statement:
        String SQL = "SELECT "
        	+ SMTableworkorders.TableName + "." + SMTableworkorders.datscheduleddate
        	+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.bdqtyofhours
        	+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.bdtravelhours
        	+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.bdbackchargehours
        	+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.mworkdescription
        	+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.strimmedordernumber
        	+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.smechanicinitials
        	
//        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datCompletedDate
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.dEstimatedHour
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCodeDescription
        	+ " FROM " + SMTableworkorders.TableName
        	+ ", " + SMTableorderheaders.TableName
        	+ " WHERE ("
        		+ "(" + SMTableworkorders.TableName + "." + SMTableworkorders.datscheduleddate + " >= '" 
        			+ sStartingDate + "')"
        		+ " AND (" + SMTableworkorders.TableName + "." + SMTableworkorders.datscheduleddate + " <= '" 
        			+ sEndingDate + "')"
        		+ " AND (INSTR('" + sMechanicsString + "', RPAD(" + SMTableworkorders.TableName + "." 
        			+ SMTableworkorders.smechanicinitials + ", " + SMTablemechanics.sMechInitialLength + ",' ')) > 0)"
        			
        		//Link the tables:
        		+ " AND (" + SMTableworkorders.TableName + "." + SMTableworkorders.strimmedordernumber + " = " 
        			+ SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber + ")"
        		
        		//Don't pick up the zero value records that may have been put in by the truck schedule
        		//functions:
   			 	+ " AND ("
			 		+ "(" + SMTableworkorders.TableName + "." + SMTableworkorders.bdbackchargehours + " != 0.00)"
			 		+ " OR (" + SMTableworkorders.TableName + "." + SMTableworkorders.bdqtyofhours + " != 0.00)"
			 		+ " OR (" + SMTableworkorders.TableName + "." + SMTableworkorders.bdtravelhours + " != 0.00)"
			 		+ " OR (" + SMTableworkorders.TableName + "." + SMTableworkorders.mworkdescription + " != '')"
			 	+ ")" //End 'AND' clause
        	
        	+ ")" //End the 'WHERE' clause:
        	+ " ORDER BY " + SMTableworkorders.TableName + "." + SMTableworkorders.smechanicinitials
        		+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.datscheduleddate
        		+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.strimmedordernumber
        		+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.lid
        	;
    	//end SQL statement

        //System.out.println("[1431361866] SQL: " + SQL);
        
    	String sCurrentMechanic = "";
    	String sLastMechanic = "";
    	String sCurrentDay = "";
    	String sLastDay = "";
    	BigDecimal bdOneHundred = new BigDecimal(100);
    	BigDecimal bdChargedHoursForDay = new BigDecimal(0);
    	BigDecimal bdTravelHoursForDay = new BigDecimal(0);
    	BigDecimal bdBackChargeHoursForDay = new BigDecimal(0);
    	BigDecimal bdJobEfficiencyPercentage = new BigDecimal(0);
    	BigDecimal bdJobHoursUsed = new BigDecimal(0);
    	BigDecimal bdJobHoursTravel = new BigDecimal(0);
    	BigDecimal bdJobHoursBackCharge = new BigDecimal(0);
    	
    	//This number is the qty of hours times the efficiency of the job.  For multiple jobs per day,
    	//this gets added, then the 'avg daily efficiency' is the total product divided by the number
    	//of hours
    	BigDecimal bdDailyEfficiencyProduct = new BigDecimal(0);
    	
    	String sEmployeeID = "";
    	String sLastEmployeeID = "";
    	
		//Check permissions for viewing invoices and orders:
		boolean bViewOrderPermitted = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMViewOrderInformation,
			sUserID,
			conn,
			sLicenseModuleLevel);
		
		boolean bViewJobCostSummaryPermitted = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMJobCostSummaryReport,
				sUserID,
				conn,
				sLicenseModuleLevel);
		
		sTimeCardDatabase = getTimeCardDatabase(conn);
		
    	try{
    		String sOrderNumber = "";
    		String sOrderNumberLink = "";
    		int iCount = 0;
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				
				//Get the mechanic and date for this record:
				sCurrentMechanic = rs.getString(SMTableworkorders.TableName + "." 
						+ SMTableworkorders.smechanicinitials);
				
				//Get the employee ID of this mechanic if the mechanic has changed:
				if (sCurrentMechanic.compareToIgnoreCase(sLastMechanic) != 0){
					sEmployeeID = getEmployeeID(sCurrentMechanic, conn);
				}
				
				java.sql.Date datWorked = rs.getDate(SMTableworkorders.TableName + "." + SMTableworkorders.datscheduleddate);
				sCurrentDay = clsDateAndTimeConversions.utilDateToString(datWorked, "EEE M/d/yyyy");
				
				//If this is a new day AND there was a previous one, print the day footer,
				//OR if the mechanic has changed, print a day footer in any case:
				if (
					(
						(sCurrentDay.compareToIgnoreCase(sLastDay) != 0)
						&& (sLastDay.compareToIgnoreCase("") != 0)
					) || (
						(sCurrentMechanic.compareToIgnoreCase(sLastMechanic) != 0)
						&& (sLastMechanic.compareToIgnoreCase("") != 0)
					)
					){
					printDayFooter(
						sLastMechanic,
						sLastEmployeeID,
						sLastDay,
						bdChargedHoursForDay,
						bdTravelHoursForDay,
						bdBackChargeHoursForDay,
						bdDailyEfficiencyProduct,
						pwSuppressed,
						conn);
					
					//Reset the daily variables:
					bdChargedHoursForDay = BigDecimal.ZERO;
					bdTravelHoursForDay = BigDecimal.ZERO;
					bdBackChargeHoursForDay = BigDecimal.ZERO;
					bdJobEfficiencyPercentage = BigDecimal.ZERO;
					bdDailyEfficiencyProduct = BigDecimal.ZERO;
					iCount = 0;
				}
				
				//If this is new mechanic AND there was a previous one, print the mechanic footer:
				if (
					(sCurrentMechanic.compareToIgnoreCase(sLastMechanic) != 0)
					&& (sLastMechanic.compareToIgnoreCase("") != 0)
					){
					
					//Calculate the overall job efficiency for the mechanic:
					//System.out.println("bdMechanicJobEfficiencyProduct = " + bdMechanicJobEfficiencyProduct.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
					//System.out.println("bdChargedHoursForMechanic = " + bdChargedHoursForMechanic.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
					if (bdChargedHoursForMechanic.compareTo(BigDecimal.ZERO) == 0){
						bdMechanicJobEfficiency = BigDecimal.ZERO;
					}else{
						bdMechanicJobEfficiency = bdMechanicJobEfficiencyProduct.divide(
								bdChargedHoursForMechanic, 2, BigDecimal.ROUND_HALF_UP);
					}
					
					//Calculate the overall net efficiency for the mechanic, taking into account
					//his hours on the clock:
					if (bdHoursOnTheClockForMechanic.compareTo(BigDecimal.ZERO) == 0){
						bdMechanicNetEfficiency = BigDecimal.ZERO;
					}else{
						bdMechanicNetEfficiency = bdMechanicJobEfficiencyProduct.divide(
							bdHoursOnTheClockForMechanic, 2, BigDecimal.ROUND_HALF_UP);
					}
					printMechanicFooter(
						sLastMechanic, 
						bdMechanicJobEfficiency, 
						bdMechanicNetEfficiency,
						sStartingDate,
						sEndingDate,
						out, 
						conn);
					iCount= 0;
				}

				//If this is a new day OR a new mechanic, print the day header:
				if (
					(sCurrentDay.compareToIgnoreCase(sLastDay) != 0)
					|| (sCurrentMechanic.compareToIgnoreCase(sLastMechanic) != 0)
						
				){
					printDayHeader(sCurrentDay, sCurrentMechanic, pwSuppressed, conn);
					iCount = 0;
				}

				//Add a link for the order number if the user has those rights: 
				sOrderNumber = rs.getString(SMTableworkorders.TableName + "." 
						+ SMTableworkorders.strimmedordernumber);
				if (bViewOrderPermitted){
					sOrderNumberLink = 
						"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
						+ sOrderNumber + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sOrderNumber + "</A>";
				}else{
					sOrderNumberLink = sOrderNumber;
				}
				if(iCount % 2 == 0) {
					pwSuppressed.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN +"\">");
				}else {
					pwSuppressed.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD +"\">");
				}

				pwSuppressed.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\">" + sOrderNumberLink + "</TD>");

				pwSuppressed.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\">"
						+ rs.getString(SMTableorderheaders.TableName + "." 
								+ SMTableorderheaders.sServiceTypeCodeDescription).trim() + "</FONT></TD>");
				
				pwSuppressed.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\">" + rs.getString(SMTableorderheaders.TableName + "." 
						+ SMTableorderheaders.sShipToName).trim() + "</FONT></TD>");
				
				//Hours
				bdJobHoursUsed = rs.getBigDecimal(SMTableworkorders.TableName + "." 
					+ SMTableworkorders.bdqtyofhours);
				bdJobHoursUsed.setScale(2, BigDecimal.ROUND_HALF_UP);
				pwSuppressed.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\">"
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdJobHoursUsed) + "</FONT></TD>");
				
				//Travel
				bdJobHoursTravel = rs.getBigDecimal(SMTableworkorders.TableName + "." 
						+ SMTableworkorders.bdtravelhours);
				bdJobHoursTravel = bdJobHoursTravel.setScale(2, BigDecimal.ROUND_HALF_UP);
				pwSuppressed.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\">"
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdJobHoursTravel) + "</FONT></TD>");
				
				//Backcharge
				bdJobHoursBackCharge = rs.getBigDecimal(SMTableworkorders.TableName + "." 
						+ SMTableworkorders.bdbackchargehours);
				pwSuppressed.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\">"
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdJobHoursBackCharge) + "</FONT></TD>");
				
				//Total estimated
				BigDecimal bdTotalEstimated = new BigDecimal(Double.toString(rs.getDouble(SMTableorderheaders.TableName + "." 
						+ SMTableorderheaders.dEstimatedHour)));
				bdTotalEstimated = bdTotalEstimated.setScale(2, BigDecimal.ROUND_HALF_UP);
				pwSuppressed.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\">"
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalEstimated) + "</FONT></TD>");
				
				//Total used
				String sTotalUsed = "";
				//System.out.println("In " + this.toString() + "01");
				if (getTotalHoursUsed(
						sOrderNumber,
						conn)
						){
					sTotalUsed = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
							bdTotalProductionHoursUsed.add(bdTotalTravelHours));
					}else{
						sTotalUsed = "N/A";
					}
				if (bViewJobCostSummaryPermitted){
					pwSuppressed.println(
							"<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\"><A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel."
						+ "SMDisplayJobCostInformation?OrderNumber=" + sOrderNumber 
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sTotalUsed + "</A></TD>");
				}else{
					pwSuppressed.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\">" + sTotalUsed + "</FONT></TD>");
				}

				//Efficiency
				//Trap the math error we are seeing:
			 	try{
					if (bdTotalProductionHoursUsed.add(bdTotalTravelHours).compareTo(BigDecimal.ZERO) > 0){
						bdJobEfficiencyPercentage = 
							bdTotalEstimated.setScale(4).divide(
								bdTotalProductionHoursUsed.add(bdTotalTravelHours).setScale(4), 2,
								BigDecimal.ROUND_HALF_UP).multiply(bdOneHundred);
						pwSuppressed.println(
								"<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\">"+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
								bdJobEfficiencyPercentage) + "</FONT></TD>");
						//System.out.println("In " + this.toString() + "04 - bdTotalProductionHoursUsed.compareTo(BigDecimal.ZERO) > 0");
					}else{
						bdJobEfficiencyPercentage = BigDecimal.ZERO;
						pwSuppressed.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\">"+ "N/A" + "</FONT></TD>");
						//System.out.println("In " + this.toString() + "05 - bdTotalProductionHoursUsed.compareTo(BigDecimal.ZERO) <= 0");
					}
			 	}catch (ArithmeticException e){
			 		pwSuppressed.println(
							"Caught arithmetic exception [1412714605] - mechanic: " + sCurrentMechanic + ", day: "
							+ sCurrentDay + ", job: " + sOrderNumber + ", hrs: " + bdJobHoursUsed.toString()
							+ ", travel: " + bdJobHoursTravel.toString() + ", total estimated: "
							+ bdTotalEstimated.toString() + ", bdTotalProductionHoursUsed = "
							+ bdTotalProductionHoursUsed.toString() + ", bdTotalTravelHours: "
							+ bdTotalTravelHours.toString()
			 		);
			 	}			
				pwSuppressed.println ("</TR>");
				iCount++;
				//Reset the counters
				sLastMechanic = sCurrentMechanic;
				sLastEmployeeID = sEmployeeID;
				sLastDay = sCurrentDay;
				
				//Accumulate:
				bdChargedHoursForDay = bdChargedHoursForDay.add(bdJobHoursUsed);
				bdTravelHoursForDay = bdTravelHoursForDay.add(bdJobHoursTravel);
				bdBackChargeHoursForDay = bdBackChargeHoursForDay.add(bdJobHoursBackCharge);
				bdDailyEfficiencyProduct = bdDailyEfficiencyProduct.add(
					(bdJobHoursUsed.add(bdJobHoursTravel)).multiply(bdJobEfficiencyPercentage));
				
				bdMechanicJobEfficiency = new BigDecimal(0); 
				bdMechanicNetEfficiency = new BigDecimal(0);
				bdChargedHoursForMechanic = bdChargedHoursForMechanic.add(bdJobHoursUsed);
				bdChargedHoursForMechanic = bdChargedHoursForMechanic.add(bdJobHoursTravel);
				bdMechanicJobEfficiencyProduct = bdMechanicJobEfficiencyProduct.add(
					(bdJobHoursUsed.add(bdJobHoursTravel)).multiply(bdJobEfficiencyPercentage));
			}
			rs.close();
    	}catch (SQLException e){
    		m_sErrorMessage = "Error reading resultset - " + e.getMessage();
    		return false;
    	}

    	//Print the day footer is there was at least one day:
		if (sCurrentDay.compareToIgnoreCase("") != 0){
			printDayFooter(
				sLastMechanic,
				sLastEmployeeID,
				sLastDay,
				bdChargedHoursForDay,
				bdTravelHoursForDay,
				bdBackChargeHoursForDay,
				bdDailyEfficiencyProduct,
				pwSuppressed,
				conn);
		}

		//Print the mechanic footer if there was at least one salesperson:
		if (sCurrentMechanic.compareToIgnoreCase("") != 0){
			//Calculate the overall job efficiency for the mechanic:
			if (bdChargedHoursForMechanic.compareTo(BigDecimal.ZERO) != 0){
				bdMechanicJobEfficiency = bdMechanicJobEfficiencyProduct.divide(
				bdChargedHoursForMechanic, 2, BigDecimal.ROUND_HALF_UP);
			}else{
				bdMechanicJobEfficiency = BigDecimal.ZERO;
			}
			//Calculate the overall net efficiency for the mechanic, taking into account
			//his hours on the clock:
			if (bdHoursOnTheClockForMechanic.compareTo(BigDecimal.ZERO) != 0){
				bdMechanicNetEfficiency = bdMechanicJobEfficiencyProduct.divide(
				bdHoursOnTheClockForMechanic, 2, BigDecimal.ROUND_HALF_UP);
			}else{
				bdMechanicNetEfficiency = BigDecimal.ZERO;
			}
			printMechanicFooter(
					sLastMechanic,
					bdMechanicJobEfficiency, 
					bdMechanicNetEfficiency,
					sStartingDate,
					sEndingDate,
					out, 
					conn);
		}
		printReportFooter(
				out,
				pwSuppressed
				);
		return true;
	}
	private String getEmployeeID(String sMechanicInitials, Connection conn){
		String sEmployeeID = "";
		
		String SQL = " SELECT"
			+ " " + SMTablemechanics.semployeeid
			+ " FROM " + SMTablemechanics.TableName
			+ " WHERE ("
				+ "(" + SMTablemechanics.sMechInitial + " = '" + sMechanicInitials + "')"
			+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				sEmployeeID = rs.getString(SMTablemechanics.semployeeid);
			}
			rs.close();
		}catch(SQLException e){
			//System.out.println("Error reading total hours - " + e.getMessage());
		}
		return sEmployeeID;
	}
	private String getTimeCardDatabase(Connection conn){
		
		String sTCDatabase = "";
		
		String SQL = " SELECT"
			+ " " + SMTablesmoptions.stimecarddatabase
			+ " FROM " + SMTablesmoptions.TableName
			;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				sTCDatabase = rs.getString(SMTablesmoptions.stimecarddatabase);
			}
			rs.close();
		}catch(SQLException e){
			System.out.println("Error reading time card database - " + e.getMessage());
		}
		
		return sTCDatabase;
	}
	private boolean getTotalHoursUsed (
			String sOrderNumber,
			Connection conn){
		
		boolean bTotalsRead = false;
		
		String SQL = " SELECT"
			+ " SUM(" + SMTableworkorders.bdqtyofhours + ") AS TotalHoursUsed"
			+ ", SUM(" + SMTableworkorders.bdbackchargehours + ") AS TotalBackchargeHours"
			+ ", SUM(" + SMTableworkorders.bdtravelhours + ") AS TotalTravelHours"
			+ " FROM " + SMTableworkorders.TableName
			+ " WHERE ("
				+ SMTableworkorders.strimmedordernumber + " = '" + sOrderNumber + "'"
			+ ")"
			;
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				bdTotalProductionHoursUsed = new BigDecimal(Double.toString(rs.getDouble("TotalHoursUsed")));
				bdTotalProductionHoursUsed.setScale(2, BigDecimal.ROUND_HALF_UP);
				bdTotalTravelHours = new BigDecimal(Double.toString(rs.getDouble("TotalTravelHours")));
				bdTotalTravelHours = bdTotalTravelHours.setScale(2, BigDecimal.ROUND_HALF_UP);
				bTotalsRead = true;
			}
			rs.close();
		}catch(SQLException e){
			System.out.println("Error reading total hours - " + e.getMessage());
		}
		
		return bTotalsRead;
	}
	private void printMechanicFooter(
			String sMechanic,
			BigDecimal bdMechJobEfficiency,
			BigDecimal bdMechNetEfficiency,
			String sStartDate,
			String sEndDate,
			PrintWriter pwOut,
			Connection conn
			){
		
		String sMechanicName = "";
		try{
			String SQL = "SELECT"
				+ " " + SMTablemechanics.sMechFullName
				+ " FROM " + SMTablemechanics.TableName
				+ " WHERE ("
					+ SMTablemechanics.sMechInitial + " = '" + sMechanic + "'"
				+ ")"
				;
			
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				sMechanicName = rs.getString(SMTablemechanics.sMechFullName).trim();
			}
			rs.close();
		}catch(SQLException e){
			System.out.println("In " + this.toString() + " could not read mechanic's name - " + e.getMessage());
		}
		
		pwOut.println("<BR>");
		pwOut.println("<TABLE  WIDTH = 100% CLASS = \"" +SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER  + "\">");
		
		pwOut.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
		pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\">"+"<B>Overall ratings for " + sMechanic + " - " + sMechanicName 
			+ "</B> starting <B>" + sStartDate + "</B> through <B>" + sEndDate + "</B>:</FONT></TD>");
		pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\">"+"<B>Overall Avg. Job Efficiency %: </B>" 
			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdMechJobEfficiency) + "</FONT></TD>");
		
		pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\">"+"<B>Overall Avg. Net Efficiency%: </B>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdMechNetEfficiency) + "</FONT></TD></TR>");

		pwOut.println("</TABLE>");
		
		pwOut.println("<BR>");
		
		//Reset the mechanic subtotal variables:
		bdMechanicJobEfficiency = new BigDecimal(0); 
		bdMechanicNetEfficiency = new BigDecimal(0);
		bdMechanicJobEfficiencyProduct = new BigDecimal(0);
		bdChargedHoursForMechanic = new BigDecimal(0);
		bdHoursOnTheClockForMechanic = new BigDecimal(0);
	}
	private void printDayFooter(
			String sMechanic,
			String sEmployeeID,
			String sDate,
			BigDecimal bdChargedHoursForDay,
			BigDecimal bdTravelHoursForDay,
			BigDecimal bdBackChargeHoursForDay,
			BigDecimal bdDailyEfficiencyProduct,
			PrintWriter pwOut,
			Connection conn
			){
			
			String sEmployeeNote = "";
			java.sql.Date datSQLDate = null;
			try {
				datSQLDate = clsDateAndTimeConversions.StringTojavaSQLDate("EEE M/d/yyyy", sDate);
			} catch (ParseException e1) {
				System.out.println("Invalid Date - Error:[1423572086] - '" + sDate +"' -" + e1.getMessage());
			}
			String sSQLDate = clsDateAndTimeConversions.utilDateToString(datSQLDate, "yyyy-MM-dd");
			//Calculate the time clocked from the time card system:
			String SQL = "SELECT * "
				+ ", IF ("
					+ "(TimeEntries.sPeriodDate = '0000-00-00')"
					+ " OR (TimeEntries.sPeriodDate = '9999-99-99'),'', '(FINALIZED)'"
					+ ") as Finalized"
				+ " FROM " + sTimeCardDatabase + "." + TimeEntries.TableName + ", " 
						   + sTimeCardDatabase + "." + SpecialEntryTypes.TableName
			+ " WHERE ("
				+ "(" + TimeEntries.TableName + "." + TimeEntries.sEmployeeID + " = '" + sEmployeeID + "')"
           
				//entry types are: regular/vacation/personal leave/holiday etc. There is a table 
				//for every type, except "regular", which is default
				//+ " AND (" + TimeEntries.iEntryTypeID + " = " + iEntryType + ")"   
           
				//this should give you all time entries that cover the given date.
				+ " AND (" + TimeEntries.TableName + "." + TimeEntries.dtInTime + " <= '" 
					+ sSQLDate + " 23:59:59')"
				+ " AND (" + TimeEntries.TableName + "." + TimeEntries.dtOutTime + " >= '" 
					+ sSQLDate + " 00:00:00')" 
           
				//this part will eliminate any time pair, in and out, 
				//which is not complete thus "uncalculateable".
				+ " AND (" + TimeEntries.TableName + "." + TimeEntries.dtInTime + " <> '0000-00-00')" 
				+ " AND (" + TimeEntries.TableName + "." + TimeEntries.dtOutTime + " <> '0000-00-00')"

				//Link the entries to the special entry types:
				+ " AND (" + TimeEntries.TableName + "." + TimeEntries.iEntryTypeID + " = "
					+ SpecialEntryTypes.TableName + "." + SpecialEntryTypes.iTypeID + ")"

				//We only need time with a 'SpecialEntryType' which is flagged as actual
				// 'work' time:
				+ " AND (" + SpecialEntryTypes.TableName + "." + SpecialEntryTypes.iWorkTime + " != 0)"
					
				//this part will determine if finalized entries are shown or not. 
				//All finalized entries have a real date. Fresh entries have 0000-00-00 
				//while posted entries  have 9999-99-99
				/*
				if (!bIncludeFinalizedEntry){
					SQL = SQL + " AND (" + TimeEntries.sPeriodDate + " = '0000-00-00'" + " OR " 
						+ TimeEntries.sPeriodDate + " = '9999-99-99')";
				}
                SQL = SQL +
                */

           + ")"
           + " ORDER BY" + " " + TimeEntries.iEntryTypeID + ", " + TimeEntries.dtInTime;

		//System.out.println("SQL:");
		//System.out.println(SQL);
			
		//End the embedded hours table:
		pwOut.println("</TABLE></TD></TR>");
		
		pwOut.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_SUB_HEADING + "\">");
		
		pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\"><U><B>Total Hours For Day From Job Cost: </B></U><BR>" 
			+ "Working: " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdChargedHoursForDay) + "<BR>"
			+ "Travel: " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTravelHoursForDay) + "<BR>"
			+ "<B>TOTAL CHARGED: </B>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdChargedHoursForDay.add(bdTravelHoursForDay)) + "<BR>"
			+ "BackCharge: " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdBackChargeHoursForDay) + "<BR>"
		);
		
		pwOut.println("</TD>");
		
		BigDecimal bdDailyJobEfficiency = BigDecimal.ZERO;
		BigDecimal bdWorkAndTravelHoursForDay = bdChargedHoursForDay.add(bdTravelHoursForDay);
		if (bdChargedHoursForDay.add(bdTravelHoursForDay).compareTo(BigDecimal.ZERO) != 0){
			if (bdWorkAndTravelHoursForDay.compareTo(BigDecimal.ZERO) == 0){
				bdDailyJobEfficiency = BigDecimal.ZERO;
			}else{
				try{
					bdDailyJobEfficiency = bdDailyEfficiencyProduct.setScale(4).divide(
						bdWorkAndTravelHoursForDay.setScale(4), 2, BigDecimal.ROUND_HALF_UP);
				}catch (ArithmeticException e){
					pwOut.println(
						"Caught arithmetic exception ln 624 - mechanic: " + sMechanic + ", day: "
						+ sDate + ", bdDailyEfficiencyProduct: " + bdDailyEfficiencyProduct.toString() 
						+ ", bdWorkAndTravelHoursForDay: " + bdWorkAndTravelHoursForDay.toString()
					);
				}
			}
			pwOut.println("<TD CLASS =\" " + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP +  "\" ><B>Avg JOB Efficiency For Day %: </B>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdDailyJobEfficiency) + "</TD>");
		}else{
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP +  " \"><FONT SIZE=2><B>Avg JOB Efficiency %: </B>" 
				+ "N/A" + "</TD>");
		}
		
		pwOut.println("<TD CLASS =\" " + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP +  "\"><FONT SIZE=2><B>On The Clock: </B><BR>");
		BigDecimal bdTotalHoursOnTheClock = new BigDecimal(0);
		if (sEmployeeID.compareToIgnoreCase("") != 0){
			try{
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				//int iCounter = 0;
			while (rs.next()){
					//iCounter++;

					long lStartingTimeInMilliseconds = datSQLDate.getTime();
					long lEndingTimeInMilliseconds = lStartingTimeInMilliseconds + (24L * 60L * 60L * 1000L) - 1;
					
					Timestamp tsInTime = rs.getTimestamp(TimeEntries.TableName + "." + TimeEntries.dtInTime);
					//System.out.println("In time = " + tsInTime.toString());
					Timestamp tsOutTime = rs.getTimestamp(TimeEntries.TableName + "." + TimeEntries.dtOutTime);
					//System.out.println("Out time = " + tsOutTime.toString());
					
					//System.out.println("InMill = " + new Timestamp(lStartingTimeInMilliseconds).toString());
					//System.out.println("OutMill = " + new Timestamp(lEndingTimeInMilliseconds).toString());
					
		
					//If the starting time read is EARLIER than the start date entered, set the starting time
					//for our calculations to the start date entered:
					if (tsInTime.getTime() > lStartingTimeInMilliseconds){
						lStartingTimeInMilliseconds = tsInTime.getTime();
					}
					
					//Similar with ending times:
					if (tsOutTime.getTime() < lEndingTimeInMilliseconds){
						lEndingTimeInMilliseconds = tsOutTime.getTime();
					}
					
					long lElapsedTime = lEndingTimeInMilliseconds - lStartingTimeInMilliseconds;
					BigDecimal bdElapsedTime = new BigDecimal(lElapsedTime);
					BigDecimal bdMillsToHours = new BigDecimal (3600000);
					BigDecimal bdElapsedHours = BigDecimal.ZERO;
					if (bdMillsToHours.compareTo(BigDecimal.ZERO) == 0){
					}else{
						bdElapsedHours = 
							bdElapsedTime.divide(bdMillsToHours, 2, BigDecimal.ROUND_HALF_UP);
					}
					bdTotalHoursOnTheClock = bdTotalHoursOnTheClock.add(bdElapsedHours);
					bdHoursOnTheClockForMechanic = bdHoursOnTheClockForMechanic.add(bdElapsedHours);
					pwOut.println(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdElapsedHours) + " hrs " 
						+ rs.getString(SpecialEntryTypes.TableName + "." + SpecialEntryTypes.sTypeDesc)
						+ " " + rs.getString("Finalized")
						+ "<BR>");
					
					//record available notes.
					if (sTimeCardDatabase.compareToIgnoreCase("") != 0){
					String sSQL = Get_Notes(rs.getInt(TimeEntries.id));
					ResultSet rsNotes = clsDatabaseFunctions.openResultSet(sSQL, conn);
					while (rsNotes.next()){
						sEmployeeNote = sEmployeeNote + "<B>" + rsNotes.getString(SpecialNoteTypes.TableName + "." + SpecialNoteTypes.sTypeTitle) + "</B>: " +
												 				rsNotes.getString(Notes.TableName + "." + Notes.mNote) + 
												 				"<BR>";
					}
					}
				}
				//System.out.println("Couter = " + iCounter);
				rs.close();
			}catch(SQLException e){
				System.out.println("In " + this.toString() + " could not read time entries - " + e.getMessage());
			}
		}
		pwOut.println("<B>TOTAL:&nbsp;</B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalHoursOnTheClock) 
			+ "</FONT>");
		
		//TEST - add diagnostics here:
		//pwOut.println("<BR> - SQL to read TimeCardHours: '" + SQL + "'");
		
		//END TEST
		pwOut.println("</TD>");
		
		//Calculate Daily Efficiency:
		//Daily Efficiency = ((Working + Travel) * (JobEfficiency/100)) + BackChargeHrs) / on the clock hours
		BigDecimal bdOneHundred = new BigDecimal(100);
		BigDecimal bdDailyEfficiencyPercentage = new BigDecimal(0);
		if (bdTotalHoursOnTheClock.compareTo(BigDecimal.ZERO) > 0){
			bdDailyEfficiencyPercentage = (bdChargedHoursForDay.add(bdTravelHoursForDay)).multiply(
					bdDailyJobEfficiency.divide(bdOneHundred));
			bdDailyEfficiencyPercentage = bdDailyEfficiencyPercentage.add(bdBackChargeHoursForDay);
			bdDailyEfficiencyPercentage = bdDailyEfficiencyPercentage.divide(
				bdTotalHoursOnTheClock.setScale(2), 2, BigDecimal.ROUND_HALF_UP);
			bdDailyEfficiencyPercentage = bdDailyEfficiencyPercentage.multiply(bdOneHundred);
			pwOut.println(
					"<TD CLASS =\" " + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP +  "\"><B>Net Daily Efficiency %:&nbsp;<B>" 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdDailyEfficiencyPercentage) + "</FONT></TD>");
		}else{
			pwOut.println("<TD CLASS =\" " + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP +  "\" ><B>Net Daily Efficiency %:&nbsp;<B>" + "N/A" + "</TD>");
		}
		
		pwOut.println("</TR>");
		
		//if there are any notes, display them
		if (sEmployeeNote.length() > 0){
			pwOut.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_HIGHLIGHT + "\">");
			pwOut.println("<TD COLSPAN=4 CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\"><B>Note from time card system:</B><HR ALIGN=LEFT WIDTH=30%>" +
					sEmployeeNote +
					"</TD>");		
			pwOut.println("</TR>");
		}
		
		//Close the table:
		pwOut.println("</TABLE>");
		pwOut.println("</TD>");
		pwOut.println("</TR>");
		pwOut.println("<BR>");
	}
	
	private void printDayHeader(
			String sDate,
			String sMechanic,
			PrintWriter pwOut,
			Connection conn
			){
		
		String sMechanicName = "";
		try{
			String SQL = "SELECT"
				+ " " + SMTablemechanics.sMechFullName
				+ " FROM " + SMTablemechanics.TableName
				+ " WHERE ("
					+ SMTablemechanics.sMechInitial + " = '" + sMechanic + "'"
				+ ")"
				;
			
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				sMechanicName = rs.getString(SMTablemechanics.sMechFullName).trim();
			}
			rs.close();
		}catch(SQLException e){
			System.out.println("In " + this.toString() + " could not read mechanic's name - " + e.getMessage());
		}
		
		pwOut.println("<TABLE  WIDTH = 100% CLASS = \"" +SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER  + "\">");
		pwOut.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		pwOut.println("<TD COLSPAN=\"4\" CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \"><B>Date:&nbsp;</B>" + sDate 
			+ "&nbsp;<B>Mechanic:&nbsp;</B> " + sMechanicName + "</TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		//Embed the hours table:
		pwOut.println("<TD COLSPAN = \"4\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
		pwOut.println("<TABLE  WIDTH = 100% CLASS = \"" +SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER  + "\">");
		//Start the row
		pwOut.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		pwOut.println("<TD  CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \"><B><U>Order #</U></B></TD>");
		pwOut.println("<TD  CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \"><B><U>Type</U></B></TD>");
		pwOut.println("<TD  CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \"><B><U>Ship to</U></B></TD>");
		pwOut.println("<TD  CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \"><B><U>Hrs</U></B></TD>");
		pwOut.println("<TD  CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \"><B><U>Travel</U></B></TD>");
		pwOut.println("<TD  CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \"><B><U>Backcharge</U></B></TD>");
		pwOut.println("<TD  CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \"><B><U>Total Estimated</U></B></TD>");
		pwOut.println("<TD  CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \"><B><U>Total Used</U></B></TD>");
		pwOut.println("<TD  CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \"><B><U>Efficiency %</U></B></TD>");
		pwOut.println("</TR>");
		//End this row
	}
	
	private void printReportFooter(
			PrintWriter pwOut,
			PrintWriter pwSuppressedOut
			){
		pwOut.println("<FONT SIZE=2><U><B>DEFINITIONS:</B></U><BR>");
		pwSuppressedOut.println("<FONT SIZE=2><B>'Total Estimated':&nbsp</B>Total number of hours estimated"
			+ " for the particular job.<BR>");
		pwSuppressedOut.println("<FONT SIZE=2><B>'Total Used':&nbsp</B>"
			+ "Total number of hours logged against the particular job;"
			+ " this includes regular hours AND travel hours logged into "
			+ "the job cost system for that job.<BR>");
		pwSuppressedOut.println("<FONT SIZE=2><B>'Job Efficiency %':&nbsp</B>Hours Used divided by Hours Estimated,"
			+ "expressed as a percentage.<BR>");
		pwSuppressedOut.println("<FONT SIZE=2><B>'Total Charged Hours For Day':&nbsp</B>Total of hours logged"
			+ " into job cost system for this mechanic, for this day.<BR>");
		pwSuppressedOut.println("<FONT SIZE=2><B>'Avg JOB Efficiency For Day %':&nbsp</B>Average efficiency of"
			+ " ALL jobs for this mechanic for this day.  This is a weighted average: the hours logged"
			+ " in the job cost system for each job are multiplied by the job efficency for that job"
			+ " giving a product for each job for that day."
			+ "  These products are added for the entire day, then divided by the Total Charged Hours"
			+ " For The Day, yielding an average daily JOB efficiency.  (This does not take into account"
			+ " hours on the clock.)<BR>"
			+ "(This calculation in detail: First, an 'efficiency product' is calculated for EACH entry "
			+ "in the day, by adding the hours used and the travel hours (NO backcharge) and multiplying "
			+ "that sum times the efficiency of the job for that entry.  Then all these 'efficiency products' "
			+ "for all the entries are added into a combined 'efficiency product' for the entire day:<BR>"
			+ "'bdDailyEfficiencyProduct = SUM for each entry in the day of ((bdJobHoursUsed + "
			+ "bdJobHoursTravel) * bdJobEfficiencyPercentage)'<BR>"
			+ "Then the 'AVG Job Efficiency' for the day is calculated by dividing the 'efficiency product' "
			+ " by the number of hours of work and travel (NO backcharge) for that day - this is printed "
			+ "on the report:<BR>"
			+ "'bdDailyJobEfficiency = bdDailyEfficiencyProduct / bdWorkAndTravelHoursForDay')"
			+ "<BR>");
		pwSuppressedOut.println("<FONT SIZE=2><B>'On The Clock':&nbsp</B>Total number of hours recorded"
			+ " for the mechanic for the day, read from the time card system data.<BR>");
		pwSuppressedOut.println("<FONT SIZE=2><B>'Net Daily Efficiency':&nbsp</B>Efficiency, expressed as a"
			+ " percentage, based on the job efficiency multiplied by the ratio of hours logged on"
			+ " jobs to hours on the clock.  This number reflects the job efficiency increased"
			+ " or decreased depending on whether the hours on the clock for the day are less"
			+ " or more than the hours logged into the job cost system for the day.<BR>"
			+ "(This calculation in detail:<BR>"
			+ "Net Daily Efficiency = ((Working + Travel) * (DailyJobEfficiency/100)) + BackChargeHrs) / "
			+ "on the clock hours)<BR>"
		);
		pwOut.println("<FONT SIZE=2><B>'Overall Avg. Job Efficiency %':&nbsp</B>Overall average efficiency of"
			+ " the jobs this mechanic worked on during the selected time period.  This DOES NOT "
			+ " take into account time on the clock, only the efficiency of those jobs based on "
			+ " hours entered into the job cost system.<BR>");
		pwOut.println("<FONT SIZE=2><B>'Overall Avg. Net Efficiency%':&nbsp</B>Average efficiency for the"
			+ " mechanic for the selected period, factoring in the job efficiency AND time on the"
			+ " clock.<BR>");
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
	
	private String Get_Notes(int iID){
		String SQL = "SELECT *" +
					 " FROM" +
					 " " + sTimeCardDatabase + "." + Notes.TableName  + "," +
					 " " + sTimeCardDatabase + "." + SpecialNoteTypes.TableName + 
					 " WHERE" +
					 " " + sTimeCardDatabase + "." + Notes.TableName + "." + Notes.iNoteTypeID + " = " +
					 " " + sTimeCardDatabase + "." + SpecialNoteTypes.TableName + "." + SpecialNoteTypes.iTypeID + 
					 " AND" + 
					 " " + sTimeCardDatabase + "." + Notes.TableName + "." + Notes.iLinkID + " = " + iID;
		return SQL;
	}
}
