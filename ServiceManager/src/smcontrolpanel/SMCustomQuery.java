package smcontrolpanel;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import SMClasses.SMLogEntry;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsStringFunctions;

public class SMCustomQuery extends java.lang.Object{

	public static String USER_VARIABLE = "*USER*";
	public static String LINKBASE_VARIABLE = "*LINKBASE*";
	public static String DATEPICKER_PARAM_VARIABLE = "*DATEPICKER*";
	public static String DATABASE_ID_PARAM_VARIABLE = "*DBID*";
	
	//This is no longer used, but we leave the definition to check old queries:
	public static String SESSION_TAG_PARAM_VARIABLE = "\\*SESSIONTAG\\*";
	
	public static String DATEPICKER_TODAY_PARAM_VARIABLE = "TODAY";
	public static String DATEPICKER_FIRSTDAYOFYEAR_PARAM_VARIABLE = "FIRSTDAYOFYEAR";
	public static String DATEPICKER_FIRSTDAYOFMONTH_PARAM_VARIABLE = "FIRSTDAYOFMONTH";
	public static String DATEPICKER_LASTDAYOFYEAR_PARAM_VARIABLE = "LASTDAYOFYEAR";
	public static String DATEPICKER_LASTDAYOFMONTH_PARAM_VARIABLE = "LASTDAYOFMONTH";
	public static String DROPDOWN_PARAM_VARIABLE = "*DROPDOWNLIST*";
	public static String SETVARIABLECOMMAND = "*SETVARIABLES*";
	public static String STARTINGPARAMDELIMITER = "[[";
	public static String ENDINGPARAMDELIMITER = "]]";
	public static String STARTINGPARAMDATADELIMITER = "{";
	public static String ENDINGPARAMDATADELIMITER = "}";
	
	private String m_sErrorMessage;
	private boolean bDebugMode = false;
	
	public SMCustomQuery(
	){
		m_sErrorMessage = "";
	}
	
	public boolean processReport(
			Connection conn,
			String sDBID,
			String sQueryID,
			String sQueryTitle,
			String sQueryString,
			String sRawQueryString,
			String sUserName,
			String sUserID,
			PrintWriter out,
			boolean bOutputToCSV,
			boolean bIncludeBorder,
			String sFontSize,
			boolean bAlternateRowColors,
			boolean bTotalNumericFields,
			boolean bShowSQLCommand,
			boolean bHideHeaderFooter,
			boolean bHideColumnLabels,
			ServletContext context
	){

		if (bDebugMode){
			System.out.println("In " + this.toString() + " bTotalNumericFields = " + bTotalNumericFields);
		}
		long lStartingTime = System.currentTimeMillis();
		
		//Log the report usage:
		
		if(sQueryID.compareToIgnoreCase("") != 0){
			SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(conn);
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMQUERY, "SMQUERY" + " Saved Query: " + sQueryID, sQueryTitle + " - SQL: '" + sQueryString + "'", "[1376509318]");
		}
		else{
			SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(conn);
		    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMQUERY, "SMQUERY", sQueryTitle + " - SQL: '" + sQueryString + "'", "[1376509318]");
		
		};

		sQueryString = sQueryString.replaceAll("\\n", "");
		sQueryString = sQueryString.replaceAll("\\r", "");
		sQueryString = sQueryString.replace(USER_VARIABLE, sUserName);
		sQueryString = sQueryString.replace(LINKBASE_VARIABLE, SMUtilities.getURLLinkBase(context));
		sQueryString = sQueryString.replace(DATABASE_ID_PARAM_VARIABLE, sDBID);
		
		//Create arrays for the numeric totals:
		ArrayList<Integer>arrInteger = new ArrayList<Integer>(0);
		ArrayList<BigDecimal>arrDecimal = new ArrayList<BigDecimal>(0);
		ArrayList<Double>arrDouble = new ArrayList<Double>(0);
		ArrayList<Float>arrFloat = new ArrayList<Float>(0);
		//Create arrays to track which columns are which types:
		ArrayList<Integer>arrIntegerColumns = new ArrayList<Integer>(0);
		ArrayList<Integer>arrDecimalColumns = new ArrayList<Integer>(0);
		ArrayList<Integer>arrDoubleColumns = new ArrayList<Integer>(0);
		ArrayList<Integer>arrFloatColumns = new ArrayList<Integer>(0);
		
		//System.out.println("[1457987416] sQueryString = '" + sQueryString + "'");
		
		//First pick off any 'SET VARIABLES' commands and run each one:
		try {
			sQueryString = processSetCommands(sQueryString, conn);
		} catch (Exception e1) {
			m_sErrorMessage = "Error [1433866016] processing 'SET' commands - " + e1.getMessage();
			return false;
		}
		
		//System.out.println("[1457987417] sQueryString = '" + sQueryString + "'");
		
		//Remember how many columns we had:
		int iColumnCount = 0;
		if (bOutputToCSV){
			String sHeading = "";
			try{
				ResultSet rs = clsDatabaseFunctions.openResultSet(sQueryString, conn);
				iColumnCount = rs.getMetaData().getColumnCount();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
					if (i != 1){
						sHeading += ",";
					}
					sHeading += "\"" + rs.getMetaData().getColumnLabel(i) + "\"";
					if (bTotalNumericFields){
						//Now flag the numeric columns so we know which ones to total:
						if (
							(rs.getMetaData().getColumnType(i) == java.sql.Types.BIGINT)
							|| (rs.getMetaData().getColumnType(i) == java.sql.Types.INTEGER)
							|| (rs.getMetaData().getColumnType(i) == java.sql.Types.NUMERIC)
							|| (rs.getMetaData().getColumnType(i) == java.sql.Types.SMALLINT)
							|| (rs.getMetaData().getColumnType(i) == java.sql.Types.TINYINT)		
						){
							arrIntegerColumns.add(i);
							arrInteger.add(0);
						}
						if (rs.getMetaData().getColumnType(i) == java.sql.Types.DECIMAL){
							arrDecimalColumns.add(i);
							arrDecimal.add(BigDecimal.ZERO);
						}
						if (rs.getMetaData().getColumnType(i) == java.sql.Types.DOUBLE){
							arrDoubleColumns.add(i);
							arrDouble.add(new Double(0.00));
						}
						if (rs.getMetaData().getColumnType(i) == java.sql.Types.FLOAT){
							arrFloatColumns.add(i);
							arrFloat.add(new Float(0.00));
						}
					}
				}
				if(!bHideColumnLabels){
					out.println(sHeading);
				}
				while(rs.next()){
					String sLine = "";
					for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
						if (i != 1){
							sLine += ",";
						}
						//Have to double any double quotes:
						sLine += "\"" + getFieldValue(rs, i, bOutputToCSV, sQueryString, sUserName).replace("\"", "\"\"") + "\"";
						if (bTotalNumericFields){
							//Now flag the numeric columns so we know which ones to total:
							//Find the array for this column and add to the value in it:
							for (int j = 0; j < arrIntegerColumns.size(); j++){
								if (arrIntegerColumns.get(j) == i){
									arrInteger.set(j, arrInteger.get(j) + rs.getInt(i));
								}
							}
							for (int j = 0; j < arrDecimalColumns.size(); j++){
								if ((arrDecimalColumns.get(j) == i) && (rs.getBigDecimal(i) != null)){
									arrDecimal.set(j, arrDecimal.get(j).add(rs.getBigDecimal(i)));
								}
							}
							for (int j = 0; j < arrDoubleColumns.size(); j++){
								if (arrDoubleColumns.get(j) == i){
									arrDouble.set(j, arrDouble.get(j) + rs.getDouble(i));
								}
							}
							for (int j = 0; j < arrFloatColumns.size(); j++){
								if (arrFloatColumns.get(j) == i){
									arrFloat.set(j, arrFloat.get(j) + rs.getFloat(i));
								}
							}
						}
					}
					out.println(sLine);
				}
				rs.close();
				//Now print the TOTALS line:
				if (bTotalNumericFields){
					String sTotalsLine = "";
					for (int i = 1; i <= iColumnCount; i++){
						if (i != 1){
							sTotalsLine += ",";
						}
						boolean bColumnTotalPrinted = false;
						for (int j = 0; j < arrIntegerColumns.size(); j++){
							if (arrIntegerColumns.get(j) == i){
								sTotalsLine += "\"" + Integer.toString(arrInteger.get(j)) + "\"";
								bColumnTotalPrinted = true;
							}
						}
						for (int j = 0; j < arrDecimalColumns.size(); j++){
							if (arrDecimalColumns.get(j) == i){
								sTotalsLine += "\"" + arrDecimal.get(j).toString() + "\"";
								bColumnTotalPrinted = true;
							}
						}
						for (int j = 0; j < arrDoubleColumns.size(); j++){
							if (arrDoubleColumns.get(j) == i){
								sTotalsLine += "\"" + Double.toString(arrDouble.get(j)) + "\"";
								bColumnTotalPrinted = true;
							}
						}
						for (int j = 0; j < arrFloatColumns.size(); j++){
							if (arrFloatColumns.get(j) == i){
								sTotalsLine += "\"" + Float.toString(arrFloat.get(j)) + "\"";
								bColumnTotalPrinted = true;
							}
						}
						if (!bColumnTotalPrinted){
							sTotalsLine += "\"" + "" + "\""; 
						}
					}
					out.println(sTotalsLine);
				}
				
				out.flush();
				out.close();
			}catch (SQLException e){
				m_sErrorMessage = "Error reading resultset - " + e.getMessage();
				return false;
			}

		}else{
			String sBorderSize = "0";
			if (bIncludeBorder){
				sBorderSize = "1";
			}

			out.println("<style type=\"text/css\">");
			out.println(
					"table.main {"
					+ "border-width: " + sBorderSize + "px; "
					+ "border-spacing: 2px; "
					+ "border-style: outset; "
					+ "border-color: gray; "
					+ "border-collapse: separate; "
					+ "font-size: " + sFontSize + "; "
					//+ "background-color: white; "
					+ "}"
			);

			out.println(
					"table.main th {"
					+ "border-width: " + sBorderSize + "px; "
					+ "padding: 2px; "
					+ "border-style: inset; "
					+ "border-color: gray; "
					//+ "background-color: white; "
					+ "}"
			);

			out.println(
					"tr.d0 td {"
					+ "background-color: #FFFFFF; "
					+"}"
			);
			out.println(
					"tr.d1 td {"
					+ "background-color: #EEEEEE; "
					+ "}"
			);

			out.println(
					"table.main td {"
					+ "border-width: " + sBorderSize + "px; "
					+ "padding: 2px; "
					+ "border-style: inset; "
					+ "border-color: gray; "
					+ "vertical-align: text-top;"
					//+ "background-color: #EEEEEE; "
					+ "}"
			);

			out.println("</style>");

			String sHeading = "";
			long lRecordCount = 0;
			String sHAlign = "";
			try{
				out.println("<table class=\"main\">");
				out.println("<TR>");
				if (bDebugMode){
					System.out.println("In " + this.toString() + " - main SQL = " + sQueryString);
				}
				ResultSet rs = clsDatabaseFunctions.openResultSet(sQueryString, conn);
				iColumnCount = rs.getMetaData().getColumnCount();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
					if (
							(rs.getMetaData().getColumnType(i) == java.sql.Types.BIGINT)
							|| (rs.getMetaData().getColumnType(i) == java.sql.Types.DECIMAL)
							|| (rs.getMetaData().getColumnType(i) == java.sql.Types.DOUBLE)
							|| (rs.getMetaData().getColumnType(i) == java.sql.Types.FLOAT)
							|| (rs.getMetaData().getColumnType(i) == java.sql.Types.INTEGER)
							|| (rs.getMetaData().getColumnType(i) == java.sql.Types.NUMERIC)
							|| (rs.getMetaData().getColumnType(i) == java.sql.Types.SMALLINT)
							|| (rs.getMetaData().getColumnType(i) == java.sql.Types.TINYINT)
					){
						
						if (bTotalNumericFields){
							//Now flag the numeric columns so we know which ones to total:
							if (
								(rs.getMetaData().getColumnType(i) == java.sql.Types.BIGINT)
								|| (rs.getMetaData().getColumnType(i) == java.sql.Types.INTEGER)
								|| (rs.getMetaData().getColumnType(i) == java.sql.Types.NUMERIC)
								|| (rs.getMetaData().getColumnType(i) == java.sql.Types.SMALLINT)
								|| (rs.getMetaData().getColumnType(i) == java.sql.Types.TINYINT)		
							){
								arrIntegerColumns.add(i);
								arrInteger.add(0);
							}
							if (rs.getMetaData().getColumnType(i) == java.sql.Types.DECIMAL){
								arrDecimalColumns.add(i);
								arrDecimal.add(BigDecimal.ZERO);
							}
							if (rs.getMetaData().getColumnType(i) == java.sql.Types.DOUBLE){
								arrDoubleColumns.add(i);
								arrDouble.add(new Double(0.00));
							}
							if (rs.getMetaData().getColumnType(i) == java.sql.Types.FLOAT){
								arrFloatColumns.add(i);
								arrFloat.add(new Float(0.00));
							}
						}
						sHAlign = "ALIGN=RIGHT";

					}else{
						sHAlign = "ALIGN=LEFT";
					}
					sHeading += "<TH " + sHAlign + " ><B><U>" + rs.getMetaData().getColumnLabel(i) + "</U></B></TH>";
				}
			if(!bHideColumnLabels){
				out.println(sHeading);
			}
				out.println("</TR>");

				boolean bWhiteRow = true;
				while(rs.next()){
					if (bAlternateRowColors){
						bWhiteRow = !bWhiteRow;
					}
					String sLine = "";
					for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
						if (
								(rs.getMetaData().getColumnType(i) == java.sql.Types.BIGINT)
								|| (rs.getMetaData().getColumnType(i) == java.sql.Types.DECIMAL)
								|| (rs.getMetaData().getColumnType(i) == java.sql.Types.DOUBLE)
								|| (rs.getMetaData().getColumnType(i) == java.sql.Types.FLOAT)
								|| (rs.getMetaData().getColumnType(i) == java.sql.Types.INTEGER)
								|| (rs.getMetaData().getColumnType(i) == java.sql.Types.NUMERIC)
								|| (rs.getMetaData().getColumnType(i) == java.sql.Types.SMALLINT)
								|| (rs.getMetaData().getColumnType(i) == java.sql.Types.TINYINT)
						){
							sHAlign = "ALIGN=RIGHT";
							if (bTotalNumericFields){
								//Now flag the numeric columns so we know which ones to total:
								//Find the array for this column and add to the value in it:
								for (int j = 0; j < arrIntegerColumns.size(); j++){
									if (arrIntegerColumns.get(j) == i){
										arrInteger.set(j, arrInteger.get(j) + rs.getInt(i));
									}
								}
								for (int j = 0; j < arrDecimalColumns.size(); j++){
									if ((arrDecimalColumns.get(j) == i) && (rs.getBigDecimal(i) != null)){
										arrDecimal.set(j, arrDecimal.get(j).add(rs.getBigDecimal(i)));
									}
								}
								for (int j = 0; j < arrDoubleColumns.size(); j++){
									if (arrDoubleColumns.get(j) == i){
										arrDouble.set(j, arrDouble.get(j) + rs.getDouble(i));
									}
								}
								for (int j = 0; j < arrFloatColumns.size(); j++){
									if (arrFloatColumns.get(j) == i){
										arrFloat.set(j, arrFloat.get(j) + rs.getFloat(i));
									}
								}
							}
						}else{
							sHAlign = "ALIGN=LEFT";
						}
						sLine += "<TD " + sHAlign + " >" + getFieldValue(rs, i, bOutputToCSV, sQueryString, sUserName) + "</TD>";
					}
					if (bWhiteRow){
						out.println("<TR class=\"d0\">" + sLine + "</TR>");
					}else{
						out.println("<TR class=\"d1\">" + sLine + "</TR>");
					}
					lRecordCount++;
				}
				rs.close();
				//Now print the TOTALS line:
				if (bTotalNumericFields){
					if (bAlternateRowColors){
						bWhiteRow = !bWhiteRow;
					}
					
					String sTotalsLine = "";
					for (int i = 1; i <= iColumnCount; i++){
						boolean bColumnTotalPrinted = false;
						for (int j = 0; j < arrIntegerColumns.size(); j++){
							if (arrIntegerColumns.get(j) == i){
								sTotalsLine += "<TD " + "ALIGN=RIGHT" + " ><B>" + Integer.toString(arrInteger.get(j)) + "</B></TD>";
								bColumnTotalPrinted = true;
							}
						}
						for (int j = 0; j < arrDecimalColumns.size(); j++){
							if (arrDecimalColumns.get(j) == i){
								sTotalsLine += "<TD " + "ALIGN=RIGHT" + " ><B>" + arrDecimal.get(j).toString() + "</B></TD>";
								bColumnTotalPrinted = true;
							}
						}
						for (int j = 0; j < arrDoubleColumns.size(); j++){
							if (arrDoubleColumns.get(j) == i){
								sTotalsLine += "<TD " + "ALIGN=RIGHT" + " ><B>" + Double.toString(arrDouble.get(j)) + "</B></TD>";
								bColumnTotalPrinted = true;
							}
						}
						for (int j = 0; j < arrFloatColumns.size(); j++){
							if (arrFloatColumns.get(j) == i){
								sTotalsLine += "<TD " + "ALIGN=RIGHT" + " ><B>" + Float.toString(arrFloat.get(j)) + "</B></TD>";
								bColumnTotalPrinted = true;
							}
						}
						if (!bColumnTotalPrinted){
							sTotalsLine += "<TD " + "ALIGN=LEFT" + " >" + "&nbsp;" + "</TD>";
						}
					}
					if (bWhiteRow){
						out.println("<TR class=\"d0\">" + sTotalsLine + "</TR>");
					}else{
						out.println("<TR class=\"d1\">" + sTotalsLine + "</TR>");
					}

				}
			}catch(SQLException e){
				m_sErrorMessage = "Error reading resultset - " + e.getMessage();
				return false;
			}
			
			if(!bHideHeaderFooter){
				out.println("</table>");
				out.println(lRecordCount + " lines printed.");
				long lEndingTime = System.currentTimeMillis();
				out.println("<BR>Database processing took " + (lEndingTime - lStartingTime)/1000L + " seconds.<BR>");
			}
			
			if (bShowSQLCommand){
//				out.println("SQL Command:");
//				out.println("<TABLE>");
//				out.println("<TR><TD>");
//				//We have to use the 'filter' to convert characters that won't display properly, but we have to put the spaces
//				//back in to make the table wrap - otherwise the query runs all the way across the screen:
//				//ALSO - convert the '&lt;BR&gt; back to <BR> so it wraps, in case there are any '\n's in the query - this also keeps it wrapping where intended:
//				out.println("<B>" + SMUtilities.filter(sRawQueryString).replaceAll("&lt;", "<").replaceAll("&gt;", ">") + "</B>");
//				out.println("</TD></TR>");
//				out.println("</TABLE>");

				out.println("SQL Command:<BR>");
				out.println(clsStringFunctions.filter(sRawQueryString).replaceAll("&lt;", "<").replaceAll("&gt;", ">"));

			}
		}
		return true;
	}
	private String getFieldValue(
		ResultSet rs, 
		int iFieldIndex, 
		boolean bOutputToCSV,
		String sSQL,
		String sUser
		) throws SQLException{
		String sFieldValue = "";

		int iType = rs.getMetaData().getColumnType(iFieldIndex);
		//out.println(" FIELDTYPE = " + iType + " ");
		switch (iType){
		case java.sql.Types.VARCHAR:
			if (rs.getString(iFieldIndex) == null){
				sFieldValue = "";
			}else{
				sFieldValue = rs.getString(iFieldIndex);
			}
			break;
		case java.sql.Types.DECIMAL:
			if (rs.getBigDecimal(iFieldIndex) == null){
				sFieldValue = clsManageBigDecimals.BigDecimalToScaledFormattedString(
					rs.getMetaData().getScale(iFieldIndex), BigDecimal.ZERO);
			}else{
				sFieldValue = clsManageBigDecimals.BigDecimalToScaledFormattedString(
					rs.getMetaData().getScale(iFieldIndex), rs.getBigDecimal(iFieldIndex));
			}
			break;
		case java.sql.Types.BIGINT:
			sFieldValue = Long.toString(rs.getLong(iFieldIndex));
			break;
		case java.sql.Types.BOOLEAN:
			boolean bValue = rs.getBoolean(iFieldIndex);
			if (bValue == true){
				sFieldValue = "Y";
			}else{
				sFieldValue = "N";
			}
		case java.sql.Types.CHAR:
			if (rs.getString(iFieldIndex) == null){
				sFieldValue = "";
			}else{
				sFieldValue = rs.getString(iFieldIndex);
			}
			break;
		case java.sql.Types.DATE:
			sFieldValue = clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(iFieldIndex));
			/* - TJR - 9/26/2014 - Changed this to prevent 'null date' errors
			if (datTest == null){
				sFieldValue = "N/A";
			}else{
				try {
					sFieldValue = SMUtilities.sqlDateToString(datTest, "MM/dd/yyyy");
				} catch (IllegalArgumentException e) {
					throw new SQLException("Error reading date in field number " +iFieldIndex + ": '" 
						+ rs.getMetaData().getColumnName(iFieldIndex) + "' - " + e.getMessage());
				}
			}
			*/
			break;
		case java.sql.Types.DOUBLE:
			sFieldValue = Double.toString(rs.getDouble(iFieldIndex));
			break;
		case java.sql.Types.FLOAT:
			sFieldValue = Float.toString(rs.getFloat(iFieldIndex));
			break;
		case java.sql.Types.INTEGER:
			sFieldValue = Integer.toString(rs.getInt(iFieldIndex));
			break;
		case java.sql.Types.SMALLINT:
			sFieldValue = Long.toString(rs.getLong(iFieldIndex));
			break;
		case java.sql.Types.TIME:
			if (rs.getTime(iFieldIndex) == null){
				sFieldValue = "N/A";
			}else{
				sFieldValue = rs.getTime(iFieldIndex).toString();
			}
			break;
		case java.sql.Types.TINYINT:
			sFieldValue = Integer.toString(rs.getInt(iFieldIndex));
			break;
		case java.sql.Types.TIMESTAMP:
			String sTest = rs.getString(iFieldIndex);
			try{ if (sTest.compareToIgnoreCase("0000-00-00 00:00:00") == 0){
				 sFieldValue = "00/00/0000 00:00:00";
			}else{
				sFieldValue = rs.getTimestamp(iFieldIndex).toString();
			}
		}catch (Exception e){ 
			m_sErrorMessage = "Error [1418229759] reading resultset from SQL: " + sSQL 
				+ ", field index '" + iFieldIndex + "' - " + e.getMessage();
			clsServletUtilities.sysprint(this.toString(), sUser, m_sErrorMessage);
			System.out.println(m_sErrorMessage);
			return m_sErrorMessage;
		}

			break;
		case java.sql.Types.LONGVARCHAR:
			if (rs.getString(iFieldIndex) == null){
				sFieldValue = "";
			}else{
				sFieldValue = rs.getString(iFieldIndex);
			}
			break;

		default:
			sFieldValue = "UNHANDLED FIELD TYPE = " + iType 
			+ " for field name: " + rs.getMetaData().getColumnLabel(iFieldIndex);
		}

		String sBlank = "";
		if (!bOutputToCSV){
			sBlank = "&nbsp;";
		}
		if (sFieldValue == null){
			sFieldValue = sBlank;
		}else{
			sFieldValue = sFieldValue.trim();
			if (sFieldValue.compareToIgnoreCase("") == 0){
				sFieldValue = sBlank;
			}
		}

		return sFieldValue;
	}
	private String processSetCommands(String sQueryString, Connection conn) throws Exception{
		String s = sQueryString;
		ArrayList<String>arrSetCommands = new ArrayList<String>(0);
	    //Pattern p = Pattern.compile("\\[\\[");
		Pattern p = null;
		String[] x = null;
		try {
			p = Pattern.compile(clsStringFunctions.convertStringToRegex(SMCustomQuery.STARTINGPARAMDELIMITER));
			x = p.split(sQueryString);
		}catch(Exception e){
			throw new Exception("Error [1433865319] splitting query '" + sQueryString + "' into parameters - " + e.getMessage());
		}
		for (int i=0; i<x.length; i++) {
			int iEnd = x[i].indexOf(SMCustomQuery.ENDINGPARAMDELIMITER);
			if (iEnd > -1){
				String sParam = x[i].substring(0, iEnd);
				//First test for SET VARIABLE command:
				if (isSetVariableParameter(sParam)){
					//First, add the whole string to the array so we can remove it from the query string afterwards:
					arrSetCommands.add(sParam);
					try {
						
						String sSetCommand = sParam.replace(SMCustomQuery.SETVARIABLECOMMAND, "");
						sSetCommand = sSetCommand.replace(SMCustomQuery.STARTINGPARAMDELIMITER, "");
						sSetCommand = sSetCommand.replace(SMCustomQuery.ENDINGPARAMDELIMITER, "");
						
						//System.out.println("[1457987418] sSetCommand = '" + sSetCommand + "'");
						
						//Now try to run the set command:
						try {
							ResultSet rs = clsDatabaseFunctions.openResultSet(sSetCommand, conn);
							rs.close();
						} catch (Exception e) {
							throw new Exception("Error [1454524947] running 'SET' command: '" + sParam + "' - " + e.getMessage());
						}
						
					} catch (Exception e) {
						throw new Exception("Error [1416325829] in date picker prompt '" + sParam + "' - " + e.getMessage());
					}
					
					//Now strip the SET command line out of the query string:
					s = s.replace(SMCustomQuery.STARTINGPARAMDELIMITER + sParam + SMCustomQuery.ENDINGPARAMDELIMITER, "");
				}
			}
		}
		
		return s;
	}
	private boolean isSetVariableParameter(String sParameter) throws Exception{
		try {
			if (sParameter.length()>=SMCustomQuery.SETVARIABLECOMMAND.length()){
				if (sParameter.substring(
					0, SMCustomQuery.SETVARIABLECOMMAND.length()).compareToIgnoreCase(SMCustomQuery.SETVARIABLECOMMAND) == 0){
					return true;
				}else{
					return false;
				}
			}else{
				return false;
			}
		} catch (Exception e) {
			throw new Exception("Error [1433865481] checking for set variable command in parameter string '" + sParameter + "' - " + e.getMessage());
		}
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
