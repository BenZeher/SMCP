package smcontrolpanel;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.ServletContext;

import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablewagescalerecords;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;

public class SMWageScaleReport extends java.lang.Object{

	private boolean bDebug = false;
	private String m_sErrorMessage;
	private static SimpleDateFormat USDateTimeformatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
	private static SimpleDateFormat USSQLDateformatter = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM/dd/yyyy");
	private static SimpleDateFormat USDayformatter = new SimpleDateFormat("EEEEEEEEEEEEEE");
	private static SimpleDateFormat Colheaderformatter = new SimpleDateFormat("EEE'<BR>'MM/dd");
	private static DecimalFormat dfforced2digits = new DecimalFormat("###,###,##0.00");
	private static DecimalFormat df2digits = new DecimalFormat("###,###,###.##");
	
//	private ArrayList<String> alColHeaders = new ArrayList<String> (7);
	private ArrayList<Date> alDates = new ArrayList<Date> (7);
	double m_dWSRegHoursTotal = 0;
	double m_dWSPayRate = 0;
	double m_dWSVacAllowed = 0;
	
	public SMWageScaleReport(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sPeriodEndDate,
			String sOrderNumber,
			String sCompanyName,
			PrintWriter out,
			ServletContext context,
			String sDecryptionKey
			){
		
    	try{
			Calendar cPeriodEndDate = Calendar.getInstance();
	    	if (bDebug){			
	    		System.out.println("[1369341073] sPeriodEndDate = " + sPeriodEndDate);
	    	}
	    	cPeriodEndDate.setTime(USDateformatter.parse(sPeriodEndDate));
	    	//get information for report header
	    	String sSQL = "SELECT * FROM" +
	    					" " + SMTableorderheaders.TableName + 
	    					" WHERE" +
	    						" " + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber + " = '" + sOrderNumber + "'";
	    	if (bDebug){			
	    		System.out.println("[1369341074] sSQL = " + sSQL);
	    	}
	    	ResultSet rsOrderInfo = clsDatabaseFunctions.openResultSet(sSQL, conn);
	    	if (rsOrderInfo.next()){
			  out.println("<TABLE BORDER=0 WIDTH=100%><TR>");
			  	//Section 1: 
				out.println("<TD ALIGN=LEFT VALIGN=TOP WIDTH=30%><FONT SIZE=2>");
				out.println("<B>" + sCompanyName + "</B>");
				out.println("<BR>Report Date: " + USDateTimeformatter.format(new Date(System.currentTimeMillis())));
				out.println("<BR>Week Ending Day: " + USDayformatter.format(cPeriodEndDate.getTime()));
				cPeriodEndDate.add(Calendar.DAY_OF_MONTH, -6);
				out.println("<BR>Week Starting Date: " + USDateformatter.format(cPeriodEndDate.getTime()));
				cPeriodEndDate.add(Calendar.DAY_OF_MONTH, 6);
				out.println("<BR>Week Ending Date: " + USDateformatter.format(cPeriodEndDate.getTime()));
				out.println("</FONT></TD>");
			  	//Section 2: 
				out.println("<TD ALIGN=CENTER VALIGN=CENTER WIDTH=40%>");
				out.println("<FONT SIZE=6><B>Wage Scale Report</B></FONT><BR><BR>");
				out.println("<FONT SIZE=3><B>Payroll Number: ___________</B></FONT>");
				out.println("</TD>");
			  	//Section 3: 
				out.println("<TD ALIGN=LEFT VALIGN=TOP WIDTH=30%><FONT SIZE=2>");	  
				out.println("<B>Job Number: " + sOrderNumber + "</B>");
				out.println("<BR>Contractor: " + rsOrderInfo.getString(SMTableorderheaders.sBillToName));
				out.println("<BR>Project: " + rsOrderInfo.getString(SMTableorderheaders.sShipToName) + "<BR>");
				String sAddress = "";
				if (rsOrderInfo.getString(SMTableorderheaders.sShipToAddress1).trim().length() > 0){
					sAddress += rsOrderInfo.getString(SMTableorderheaders.sShipToAddress1).trim() + "<BR>";
				}
				if (rsOrderInfo.getString(SMTableorderheaders.sShipToAddress2).trim().length() > 0){
					sAddress += rsOrderInfo.getString(SMTableorderheaders.sShipToAddress2).trim() + "<BR>";
				}
				if (rsOrderInfo.getString(SMTableorderheaders.sShipToAddress3).trim().length() > 0){
					sAddress += rsOrderInfo.getString(SMTableorderheaders.sShipToAddress3).trim() + "<BR>";
				}
				if (rsOrderInfo.getString(SMTableorderheaders.sShipToAddress4).trim().length() > 0){
					sAddress += rsOrderInfo.getString(SMTableorderheaders.sShipToAddress4).trim() + "<BR>";
				}
				out.println(sAddress);
				out.println("</FONT></TD>");
			    out.println("</TR></TABLE>");
	    	}else{
	    		//NO ORDER FOUND. 
	    		//This will be highly unlikely because order number has been validated in one of the previous steps.
	    	}
	    	rsOrderInfo.close();
	    	
	    	//load a list of days that the reports needs.
	    	alDates = load_date_list(sPeriodEndDate);
	    	//System.out.println("[1369341075] Date list loaded.");
		
			//REPORT START
	    	clsDatabaseFunctions.start_data_transaction(conn);
	    	
	    	sSQL = "SELECT " + SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sEmployeeName 
	    		+ ", " + "AES_DECRYPT("+ SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sEmployeeSSN +",'"+ sDecryptionKey +"') AS " + SMTablewagescalerecords.sEmployeeSSN
	    		+ ", " + "AES_DECRYPT("+ SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sEmployeeAddress +",'"+ sDecryptionKey +"') AS " + SMTablewagescalerecords.sEmployeeAddress
	    		+ ", " + "AES_DECRYPT("+ SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sEmployeeAddress2 +",'"+ sDecryptionKey +"') AS " + SMTablewagescalerecords.sEmployeeAddress2
	    		+ ", " + "AES_DECRYPT("+ SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sEmployeeCity +",'"+ sDecryptionKey +"') AS " + SMTablewagescalerecords.sEmployeeCity
	    		+ ", " + "AES_DECRYPT("+ SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sEmployeeState +",'"+ sDecryptionKey +"') AS " + SMTablewagescalerecords.sEmployeeState
	    		+ ", " + "AES_DECRYPT("+ SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sEmployeeZipCode +",'"+ sDecryptionKey +"') AS " + SMTablewagescalerecords.sEmployeeZipCode
	    		+ ", " + SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sEmployeeTitle 
	    		+ ", " + SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.datPeriodEndDate
	    		+ ", " + SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sCostNumber
	    		+ ", " + "AES_DECRYPT("+ SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sRegHours +",'"+ sDecryptionKey +"') AS " + SMTablewagescalerecords.sRegHours
	    		+ ", " + "AES_DECRYPT("+ SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sOTHours +",'"+ sDecryptionKey +"') AS " + SMTablewagescalerecords.sOTHours
	    		+ ", " + "AES_DECRYPT("+ SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sDTHours +",'"+ sDecryptionKey +"') AS " + SMTablewagescalerecords.sDTHours
	    		+ ", " + "AES_DECRYPT("+ SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sPayRate +",'"+ sDecryptionKey +"') AS " + SMTablewagescalerecords.sPayRate
	    		+ ", " + "AES_DECRYPT("+ SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sHolidayHours +",'"+ sDecryptionKey +"') AS " + SMTablewagescalerecords.sHolidayHours
	    	    + ", " + "AES_DECRYPT("+ SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sPersonalHours +",'"+ sDecryptionKey +"') AS " + SMTablewagescalerecords.sPersonalHours
	    		+ ", " + "AES_DECRYPT("+ SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sVacHours +",'"+ sDecryptionKey +"') AS " + SMTablewagescalerecords.sVacHours
	    		+ ", " + "AES_DECRYPT("+ SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sGross +",'"+ sDecryptionKey +"') AS " + SMTablewagescalerecords.sGross
	    		+ ", " + "AES_DECRYPT("+ SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sFederal +",'"+ sDecryptionKey +"') AS " + SMTablewagescalerecords.sFederal
	       	    + ", " + "AES_DECRYPT("+ SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sSS +",'"+ sDecryptionKey +"') AS " + SMTablewagescalerecords.sSS
	    		+ ", " + "AES_DECRYPT("+ SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sMedicare +",'"+ sDecryptionKey +"') AS " + SMTablewagescalerecords.sMedicare
	    		+ ", " + "AES_DECRYPT("+ SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sState +",'"+ sDecryptionKey +"') AS " + SMTablewagescalerecords.sState
	    	    + ", " + "AES_DECRYPT("+ SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sMiscDed +",'"+ sDecryptionKey +"') AS " + SMTablewagescalerecords.sMiscDed
	    		+ ", " + "AES_DECRYPT("+ SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sNetPay +",'"+ sDecryptionKey +"') AS " + SMTablewagescalerecords.sNetPay
	    		+ ", " + "AES_DECRYPT("+ SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sVacAllowed +",'"+ sDecryptionKey +"') AS " + SMTablewagescalerecords.sVacAllowed
	    		+ ", " + SMTablewagescalerecords.TableName + "." +SMTablewagescalerecords.sCostNumber
	    		+ " FROM " + SMTablewagescalerecords.TableName + 
	    			" WHERE" +
	    				" " + SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.datPeriodEndDate + "='" + USSQLDateformatter.format(cPeriodEndDate.getTime()) + "'" + 
	    			" ORDER BY" + " " + SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sEmployeeName;
	    	
	    	if (bDebug){			
	    		System.out.println("[1368734633] SQL = " + sSQL);
	    	}
	    	
	    	ResultSet rsMain = clsDatabaseFunctions.openResultSet(sSQL, conn);
	    	ArrayList<SMWageScaleLine> alLines = new ArrayList<SMWageScaleLine> (0);
	    	
	    	while (rsMain.next()){
		    	if (rsMain.getString(SMTablewagescalerecords.sCostNumber).trim().compareTo("") != 0 && 
		    		rsMain.getString(SMTablewagescalerecords.sCostNumber).substring(2).replaceAll("^0*", "").compareTo(sOrderNumber) == 0
		    		){
		    		SMWageScaleLine line = new SMWageScaleLine();
		    		//load data
		    		line.setM_EmployeeName(rsMain.getString(SMTablewagescalerecords.sEmployeeName));
		    		line.setM_EmployeeSSN(rsMain.getString(SMTablewagescalerecords.sEmployeeSSN));
		    		line.setM_EmployeeAddress(rsMain.getString(SMTablewagescalerecords.sEmployeeAddress));
		    		line.setM_EmployeeCity(rsMain.getString(SMTablewagescalerecords.sEmployeeCity));
		    		line.setM_EmployeeState(rsMain.getString(SMTablewagescalerecords.sEmployeeState));
		    		line.setM_EmployeeZipCode(rsMain.getString(SMTablewagescalerecords.sEmployeeZipCode));
		    		line.setM_EmployeeTitle(rsMain.getString(SMTablewagescalerecords.sEmployeeTitle));
		    		if (rsMain.getString(SMTablewagescalerecords.sCostNumber).trim().compareTo("") == 0){
		    			line.setM_Day("");
			    		line.setM_Date(null);
			    		line.setM_OrderNumber("");
		    		}else{
			    		line.setM_Day(Get_Day(rsMain.getString(SMTablewagescalerecords.sCostNumber).substring(0, 2)));
			    		line.setM_Date(Get_Date(rsMain.getString(SMTablewagescalerecords.sCostNumber).substring(0, 2),
			    										rsMain.getDate(SMTablewagescalerecords.datPeriodEndDate)));
			    		line.setM_OrderNumber(rsMain.getString(SMTablewagescalerecords.sCostNumber).substring(2));
		    		}
		    		line.setM_RegHours(Double.parseDouble(rsMain.getString(SMTablewagescalerecords.sRegHours)));
		    		line.setM_OTHours(Double.parseDouble(rsMain.getString(SMTablewagescalerecords.sOTHours)));
		    		line.setM_DTHours(Double.parseDouble(rsMain.getString(SMTablewagescalerecords.sDTHours)));
		    		line.setM_PayRate(Double.parseDouble(rsMain.getString(SMTablewagescalerecords.sPayRate)));
		    		line.setM_HolidayHours(Double.parseDouble(rsMain.getString(SMTablewagescalerecords.sHolidayHours)));
		    		line.setM_PersonalHours(Double.parseDouble(rsMain.getString(SMTablewagescalerecords.sPersonalHours)));
		    		line.setM_VacHours(Double.parseDouble(rsMain.getString(SMTablewagescalerecords.sVacHours)));
		    		line.setM_Gross(Double.parseDouble(rsMain.getString(SMTablewagescalerecords.sGross)));
		    		line.setM_Federal(Double.parseDouble(rsMain.getString(SMTablewagescalerecords.sFederal)));
		    		line.setM_SS(Double.parseDouble(rsMain.getString(SMTablewagescalerecords.sSS)));
		    		line.setM_Medicare(Double.parseDouble(rsMain.getString(SMTablewagescalerecords.sMedicare)));
		    		line.setM_State(Double.parseDouble(rsMain.getString(SMTablewagescalerecords.sState)));
		    		line.setM_MiscDed(Double.parseDouble(rsMain.getString(SMTablewagescalerecords.sMiscDed)));
		    		line.setM_NetPay(Double.parseDouble(rsMain.getString(SMTablewagescalerecords.sNetPay)));
		    		//line.setM_VacAllowed(rsMain.getDouble(SMTablewagescalerecords.dVacAllowed));
		    		
		    		alLines.add(line);
		    		
		    	}
	    	}
	    	rsMain.close();
	    	
	    	out.println("<TABLE BORDER=0 WIDTH=100%>");
	    	
	    	if (alLines.size() == 0){
	    		//nothing to display.
	    		out.println("<TR><TD COLSPAN=2 WIDTH=100%><p ALIGN=CENTER><BR><BR><BR><BR><B>NO DATA</B></p></TD></TR>");
	    		
	    	}else{
		    	if (!process_lines(alLines, 
				   sPeriodEndDate,
				   sDecryptionKey,
				   out,
				   conn)){
		    		//failed to process lines.
					//System.out.println("[1369766737] Failed to prices wage scale records.");
					m_sErrorMessage = "[1369766737] Failed to prices wage scale records.";
		    	}		
	    	}
		}catch(Exception e){
			//System.out.println("[1369766714] Error in " + this.toString() + ":processReport - " + e.getMessage());
			m_sErrorMessage = "[1369766714] Error in " + this.toString() + ":processReport - " + e.getMessage();
			return false;
		}
    	out.println("<TR><TD COLSPAN=2 WIDTH=100%><HR></TD></TR>");
		out.println("<TR><TD COLSPAN=2 WIDTH=100%><FONT SIZE=1><p ALIGN=LEFT>" +
							"&nbsp;&nbsp;* HDAY - Holiday fringe is calculated by multiplying the annual number of holidays (6) times the number of hours in a regular day (8) times the employee's rate, then dividing that total product by the number of work hours in a single year (2080)." +
							"<BR>&nbsp;** VAC - Vacation fringe is calculated by multiplying the number of hours of annual vacation to which the employee is entitled times the employee's rate, then dividing that total product by the number of work hours in a single year (2080)." +
							"<BR>*** H&W - Insurance fringe is simply a fixed amount per hour.</p></FONT></TD></TR>");
		out.println("</TABLE>");
		return true;
	}
	
	private boolean process_lines(ArrayList<SMWageScaleLine> alist, 
								  String sPeriodEndDate, 
								  String sDecryptionKey,
								  PrintWriter out,
								  Connection conn){
		
		String sCurrentEmployeeSSN = "";
		ArrayList<SMWageScaleLine> alTimeDisplay = new ArrayList<SMWageScaleLine>(0);		
		for (int i=0;i<7;i++){
			SMWageScaleLine temp = new SMWageScaleLine();
			temp.setM_Day(USDayformatter.format(alDates.get(i)));
			alTimeDisplay.add(temp);
		}
    	if (bDebug){			
    		System.out.println("[1369767831] Finished initializing time display array");
    	}
		for (int i=0;i<alist.size();i++){
			
			SMWageScaleLine wsl = alist.get(i);
			
			if (wsl.getM_EmployeeSSN().compareTo(sCurrentEmployeeSSN) != 0){
				//starting with a new employee
				if (sCurrentEmployeeSSN.length() > 0){
					//this means this is not the very first employee, therefore
					//print out wage scale time log for current employee base on time display array

					//Detailed time log section
					print_detailed_timelog_section(alTimeDisplay, out);
					
					//print out Grand total section for this employee 
					if (!print_gross_section(sPeriodEndDate, 
											sCurrentEmployeeSSN, 
											sDecryptionKey,
											conn, 
											out)
						){
						//System.out.println("[1369835300] Error printing gross section for employee " + sCurrentEmployeeSSN);
						m_sErrorMessage = "[1369835300] Error printing gross section for employee " + sCurrentEmployeeSSN + "<BR>";
						return false;
					}
					
				}
				//print out employee info for new employee
				print_employee_header(wsl, out);
				//reset all temp variables
				sCurrentEmployeeSSN = wsl.getM_EmployeeSSN();
				m_dWSRegHoursTotal = 0;
				m_dWSPayRate = 0;
				alTimeDisplay = new ArrayList<SMWageScaleLine>(0);		
				for (int j=0;j<7;j++){
					SMWageScaleLine temp = new SMWageScaleLine();
					temp.setM_Day(USDayformatter.format(alDates.get(j)));
					alTimeDisplay.add(temp);
				}
				if (bDebug){
					System.out.println("[1369767989] Finished initializing for current employee.");
				}
			}
		
			//if this is in the middle of an employee, insert his time record into the time array
			//TODO: This is not right. the time needs to be put in on cell.

			for (int j=0;j<7;j++){
				if (alTimeDisplay.get(j).getM_Day().compareTo(alist.get(i).getM_Day()) == 0){
					alTimeDisplay.get(j).setM_RegHours(alist.get(i).getM_RegHours());
					alTimeDisplay.get(j).setM_OTHours(alist.get(i).getM_OTHours());
					alTimeDisplay.get(j).setM_DTHours(alist.get(i).getM_DTHours());
					m_dWSPayRate = alist.get(i).getM_PayRate();
					m_dWSVacAllowed = alist.get(i).getM_VacAllowed();
				}
			}
		}

		//Detailed time log section for last employee
		print_detailed_timelog_section(alTimeDisplay, out);
		
		//print out Grand total section for last employee 
		if (!print_gross_section(sPeriodEndDate, 
								sCurrentEmployeeSSN,
							    sDecryptionKey,
								conn, 
								out)
			){
			//System.out.println("[1369835300] Error printing gross section for employee " + sCurrentEmployeeSSN);
			m_sErrorMessage = "[1369835300] Error printing gross section for employee " + sCurrentEmployeeSSN + "<BR>";
			return false;
		}
		return true;
	}
	private void print_employee_header(SMWageScaleLine wsl, PrintWriter out){

		//initialize new employee
		out.println("<TR><TD COLSPAN=2><HR></TD></TR>");
		out.println("<TR><TD COLSPAN=2>" +
			"<TABLE WIDTH=100% BORDER=0>" +
				"<TR><TD><FONT SIZE=1><B>" + wsl.getM_EmployeeName() + "</B></FONT> - <FONT SIZE=1>" + 
							 wsl.getM_EmployeeAddress() + ", " + 
							 wsl.getM_EmployeeCity() + ", " + 
							 wsl.getM_EmployeeState() + " " + 
							 wsl.getM_EmployeeZipCode() +
					 "</FONT></TD></TR>" +
				"<TR><TD><FONT SIZE=1>" + wsl.getM_EmployeeSSN() + "&nbsp;&nbsp;&nbsp;&nbsp;" +
				 wsl.getM_EmployeeTitle() +
				 "</FONT></TD></TR>" +
			"</TABLE>" +
		"</TD></TR>");
	}
	
	private void print_detailed_timelog_section(ArrayList<SMWageScaleLine> alTimeDisplay, PrintWriter out){

		String sRegRow = "";
		String sOTRow = "";
		String sDTRow = "";
		double dWSOTHoursTotal = 0;
		double dWSDTHoursTotal = 0;
		double dHolidayHoursTotal = 0;
		double dVacHoursTotal = 0;
		double dPersonalHoursTotal = 0;
		
		out.println("<TR><TD VALIGN=TOP WIDTH=60%>" +
			"<TABLE BORDER=0 WIDTH=100%>" +
			//print column header row for detailed time table
			"<TR>" +
				"<TD ALIGN=CENTER VALIGN=TOP WIDTH=11%><FONT SIZE=1><B>&nbsp;</B></FONT></TD>" +
				"<TD ALIGN=CENTER VALIGN=TOP WIDTH=7%><FONT SIZE=1><B>" + Colheaderformatter.format(alDates.get(0)) + "</B></FONT></TD>" +
				"<TD ALIGN=CENTER VALIGN=TOP WIDTH=7%><FONT SIZE=1><B>" + Colheaderformatter.format(alDates.get(1)) + "</B></FONT></TD>" +
				"<TD ALIGN=CENTER VALIGN=TOP WIDTH=7%><FONT SIZE=1><B>" + Colheaderformatter.format(alDates.get(2)) + "</B></FONT></TD>" +
				"<TD ALIGN=CENTER VALIGN=TOP WIDTH=7%><FONT SIZE=1><B>" + Colheaderformatter.format(alDates.get(3)) + "</B></FONT></TD>" +
				"<TD ALIGN=CENTER VALIGN=TOP WIDTH=7%><FONT SIZE=1><B>" + Colheaderformatter.format(alDates.get(4)) + "</B></FONT></TD>" +
				"<TD ALIGN=CENTER VALIGN=TOP WIDTH=7%><FONT SIZE=1><B>" + Colheaderformatter.format(alDates.get(5)) + "</B></FONT></TD>" +
				"<TD ALIGN=CENTER VALIGN=TOP WIDTH=7%><FONT SIZE=1><B>" + Colheaderformatter.format(alDates.get(6)) + "</B></FONT></TD>" +
				"<TD ALIGN=CENTER VALIGN=TOP WIDTH=8%><FONT SIZE=1><B>Hours</B></FONT></TD>" +
				"<TD ALIGN=CENTER VALIGN=TOP WIDTH=8%><FONT SIZE=1><B>Rate</B></FONT></TD>" +
				"<TD ALIGN=CENTER VALIGN=TOP WIDTH=8%><FONT SIZE=1><B>HDAY*</B></FONT></TD>" +
				"<TD ALIGN=CENTER VALIGN=TOP WIDTH=8%><FONT SIZE=1><B>VAC**</B></FONT></TD>" +
				"<TD ALIGN=CENTER VALIGN=TOP WIDTH=8%><FONT SIZE=1><B>HW***</B></FONT></TD>" +
			"</TR>");	

		sRegRow = "<TR><TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=1><B>Reg</B></FONT></TD>";
		sOTRow = "<TR><TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=1><B>OT</B></FONT></TD>";
		sDTRow = "<TR><TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=1><B>DT</B></FONT></TD>";
		
		for (int k=0;k<7;k++){
			sRegRow += "<TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=1>" + alTimeDisplay.get(k).getM_RegHours() + "</FONT></TD>";
			m_dWSRegHoursTotal += alTimeDisplay.get(k).getM_RegHours();
			sOTRow += "<TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=1>" + alTimeDisplay.get(k).getM_OTHours() + "</FONT></TD>";
			dWSOTHoursTotal += alTimeDisplay.get(k).getM_OTHours();
			sDTRow += "<TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=1>" + alTimeDisplay.get(k).getM_DTHours() + "</FONT></TD>";
			dWSDTHoursTotal += alTimeDisplay.get(k).getM_DTHours();
			/*
			dHolidayHoursTotal += alTimeDisplay.get(k).getM_HolidayHours();
			dVacHoursTotal += alTimeDisplay.get(k).getM_VacHours();
			dPersonalHoursTotal += alTimeDisplay.get(k).getM_PersonalHours();
			*/
		}
		sRegRow += "<TD ALIGN=CENTER VALIGN=TOP ><FONT SIZE=1>" + df2digits.format(m_dWSRegHoursTotal) + "</FONT></TD>" +
				   "<TD ALIGN=CENTER VALIGN=TOP ><FONT SIZE=1>" + dfforced2digits.format(m_dWSPayRate) + "</FONT></TD>";
		//calculate holiday hours.
		dHolidayHoursTotal = (48d / 2080d) * m_dWSPayRate;
		sRegRow += "<TD ALIGN=CENTER VALIGN=TOP ><FONT SIZE=1>" + df2digits.format(dHolidayHoursTotal) + "</FONT></TD>";
		
		//calculate vacation hours
		dVacHoursTotal = (m_dWSVacAllowed / 2080) * m_dWSPayRate;
		sRegRow += "<TD ALIGN=CENTER VALIGN=TOP ><FONT SIZE=1>" + df2digits.format(dVacHoursTotal) + "</FONT></TD>";
		
		sRegRow += "<TD ALIGN=CENTER VALIGN=TOP ><FONT SIZE=1>" + df2digits.format(dPersonalHoursTotal) + "</FONT></TD>" +
				   "</TR>"; 
		sOTRow += "<TD ALIGN=CENTER VALIGN=TOP ><FONT SIZE=1>" + df2digits.format(dWSOTHoursTotal) + "</FONT></TD></TR>";
		sDTRow += "<TD ALIGN=CENTER VALIGN=TOP ><FONT SIZE=1>" + df2digits.format(dWSDTHoursTotal) + "</FONT></TD></TR>";
		out.println(sRegRow);
		out.println(sOTRow);
		out.println(sDTRow);
		out.println("</TABLE></TD>");
	}

	private boolean print_gross_section(String sPeriodEndDate, 
										String sCurrentEmployeeSSN,
										String sDecryptionKey,
										Connection conn, 
										PrintWriter out){

		String sSQLPEDate = "";
		try{
			sSQLPEDate = USSQLDateformatter.format(USDateformatter.parse(sPeriodEndDate));
		} catch (java.text.ParseException ex) {
			//System.out.println("[1369773427] Error parsing period end date: " + sPeriodEndDate);
			m_sErrorMessage = "[1369773427] Error parsing period end date: " + sPeriodEndDate;
			return false;
		}
		//get total time  
		String sSQL = "SELECT"
				+ " SUM(CAST(AES_DECRYPT(" + SMTablewagescalerecords.sGross + ", '" + sDecryptionKey + "') AS DECIMAL(17,2))) AS GROSSTOTAL "
				+ ", SUM(CAST(AES_DECRYPT(" + SMTablewagescalerecords.sFederal + ", '" + sDecryptionKey + "') AS DECIMAL(17,2))) AS FEDERALTOTAL "
				+ ", SUM(CAST(AES_DECRYPT(" + SMTablewagescalerecords.sSS + ", '" + sDecryptionKey + "') AS DECIMAL(17,2))) AS SSTOTAL"
				+ ", SUM(CAST(AES_DECRYPT(" + SMTablewagescalerecords.sMedicare + ", '" + sDecryptionKey + "') AS DECIMAL(17,2))) AS MEDICARETOTAL "
				+ ", SUM(CAST(AES_DECRYPT(" + SMTablewagescalerecords.sState + ", '" + sDecryptionKey + "') AS DECIMAL(17,2))) AS STATETOTAL "
				+ ", SUM(CAST(AES_DECRYPT(" + SMTablewagescalerecords.sMiscDed + ", '" + sDecryptionKey + "') AS DECIMAL(17,2))) AS MISCDEDTOTAL "
				+ ", SUM(CAST(AES_DECRYPT(" + SMTablewagescalerecords.sNetPay + ", '" + sDecryptionKey + "') AS DECIMAL(17,2))) AS NETPAYTOTAL "
				
				+ " FROM " + SMTablewagescalerecords.TableName
				+ " WHERE ("
				+ " " + SMTablewagescalerecords.datPeriodEndDate + " = '" + sSQLPEDate + "'"
				+ " AND" 
				+ " " + "AES_DECRYPT(" + SMTablewagescalerecords.sEmployeeSSN + ", '"+ sDecryptionKey + "') = '"  + sCurrentEmployeeSSN + "' )";
				/*
				+ " GROUP BY "
				+ " " + SMTablewagescalerecords.sEmployeeSSN;
				 */
			if (bDebug){
				System.out.println("[1369772492] SQL = " + sSQL);
			}
		
		try{
			ResultSet rsTotals = clsDatabaseFunctions.openResultSet(sSQL, conn);
			if (rsTotals.next()){
				BigDecimal bdTotalDeducations = new BigDecimal("0.00");
				bdTotalDeducations = rsTotals.getBigDecimal("FEDERALTOTAL").add(rsTotals.getBigDecimal("SSTOTAL")).add(
					rsTotals.getBigDecimal("MEDICARETOTAL")).add(rsTotals.getBigDecimal("STATETOTAL")).add(rsTotals.getBigDecimal("MISCDEDTOTAL"));
				
				out.println("<TD VALIGN=TOP WIDTH=40%>" +
					"<TABLE BORDER=0 WIDTH=100%>" +
						"<TR><TD ALIGN=RIGHT VALIGN=TOP WIDTH=25%><FONT SIZE=1><B>Job Gross</B></FONT></TD><TD ALIGN=RIGHT VALIGN=TOP WIDTH=25%><FONT SIZE=1>" 
						+ dfforced2digits.format(m_dWSRegHoursTotal * m_dWSPayRate) + "</FONT></TD><TD WIDTH=25%>&nbsp;</TD><TD WIDTH=25%>&nbsp;</TD></TR>" 
					+ "<TR><TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=1><B>Total Gross</B></FONT></TD><TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=1>" 
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsTotals.getBigDecimal("GROSSTOTAL")) + "</FONT></TD>" 
					+ "<TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=1><B>Misc Ded</B></FONT></TD><TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=1>" 
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsTotals.getBigDecimal("MISCDEDTOTAL")) + "</FONT></TD></TR>" 
					+ "<TR><TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=1><B>Fed W/H</B></FONT></TD><TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=1>" 
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsTotals.getBigDecimal("FEDERALTOTAL")) + "</FONT></TD>" 
					+ "<TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=1><B>Total Ded</B></FONT></TD><TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=1>" 
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalDeducations) + "</FONT></TD></TR>" 
					+ "<TR><TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=1><B>SS</B></FONT></TD><TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=1>" 
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsTotals.getBigDecimal("SSTOTAL")) + "</FONT></TD>" 
					+ "<TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=1><B>Net Pay</B></FONT></TD><TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=1>" 
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsTotals.getBigDecimal("NETPAYTOTAL")) + "</FONT></TD></TR>" 
					+ "<TR><TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=1><B>MED</B></FONT></TD><TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=1>" 
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsTotals.getBigDecimal("MEDICARETOTAL")) + "</FONT></TD><TD>&nbsp;</TD><TD>&nbsp;</TD></TR>" 
					+ "<TR><TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=1><B>St W/H</B></FONT></TD><TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=1>" 
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsTotals.getBigDecimal("STATETOTAL")) + "</FONT></TD><TD>&nbsp;</TD><TD>&nbsp;</TD></TR>" 
					+ "</TABLE>" 
					+ "</TD></TR>"
				);	
			}else{
				//got no total time info for this employee. This is highly unlikely to happen
			}
			rsTotals.close();
		}catch(SQLException ex){
			//System.out.println("[1368734673] Error retrieving totals for employee " + sCurrentEmployeeSSN + ": " + ex.getMessage());
			m_sErrorMessage = "[1368734673] Error retreving totals for employee " + sCurrentEmployeeSSN + ": " + ex.getMessage();
			return false;
		}
		return true;
	}
	private String Get_Day(String s){
		if (s.compareTo("MO") == 0){
			return "Monday";
		}else if (s.compareTo("TU") == 0){
			return "Tuesday";
		}else if (s.compareTo("WE") == 0){
			return "Wednesday";
		}else if (s.compareTo("TH") == 0){
			return "Thursday";
		}else if (s.compareTo("FR") == 0){
			return "Friday";
		}else if (s.compareTo("SA") == 0){
			return "Saturday";
		}else if (s.compareTo("SU") == 0){
			return "Sunday";
		}
		return "Error";
	}
	
	private Date Get_Date(String s, Date d){
		
		Calendar cPeriodEndDate = Calendar.getInstance();
		cPeriodEndDate.setTime(d);
		int iTarget = cPeriodEndDate.get(Calendar.DAY_OF_WEEK);
	
		if (s.compareTo("MO") == 0){
			iTarget = Calendar.MONDAY;
		}else if (s.compareTo("TU") == 0){
			iTarget = Calendar.TUESDAY;
		}else if (s.compareTo("WE") == 0){
			iTarget = Calendar.WEDNESDAY;
		}else if (s.compareTo("TH") == 0){
			iTarget = Calendar.THURSDAY;
		}else if (s.compareTo("FR") == 0){
			iTarget = Calendar.FRIDAY;
		}else if (s.compareTo("SA") == 0){
			iTarget = Calendar.SATURDAY;
		}else if (s.compareTo("SU") == 0){
			iTarget = Calendar.SUNDAY;
		}
		
		while (cPeriodEndDate.get(Calendar.DAY_OF_WEEK) != iTarget){
			cPeriodEndDate.add(Calendar.DAY_OF_MONTH, -1);
		}
		
		return new Date(cPeriodEndDate.getTimeInMillis());
	}

	private ArrayList<Date> load_date_list(String sPeriodEndDate){

		ArrayList<Date> list = new ArrayList<Date> (0);
		Calendar cPeriodEndDate = Calendar.getInstance();
		try{
			cPeriodEndDate.setTime(USDateformatter.parse(sPeriodEndDate));
			cPeriodEndDate.add(Calendar.DAY_OF_MONTH, -7);
			for (int i=0;i<7;i++){
				
				cPeriodEndDate.add(Calendar.DAY_OF_MONTH, 1);
				//System.out.println("date_list(" + i + ") = " + USDateTimeformatter.format(new Date(cPeriodEndDate.getTimeInMillis())));
				list.add(new Date(cPeriodEndDate.getTimeInMillis()));
			}
		}catch(Exception e){
    		//System.out.println("[1369342015] Error loading date list - " + e.getMessage() + ".");
    		m_sErrorMessage = "[1369342016] Error loading date list - " + e.getMessage() + ".";
		}
		
		return list;
	}
		
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
