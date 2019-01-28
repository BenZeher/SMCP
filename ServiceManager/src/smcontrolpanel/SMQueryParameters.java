package smcontrolpanel;


import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMTableusers;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMQueryParameters  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String QUERYPARAMBASE = "QPBASE";
	public static final String QUERYDATEPICKERPARAMBASE = "QPDATEPICKERBASE";
	private static final String CALLED_CLASS_NAME = "smcontrolpanel.SMQueryGenerate";

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMQuerySelector
		)
		){
			return;
		}
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				"Query Parameters",
				SMUtilities.getFullClassName(this.toString()),
				CALLED_CLASS_NAME,
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMQuerySelector
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMQuerySelector)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
				
	    smedit.printHeaderTable();
	    
		smedit.getPWOut().println("<BR>");
		
		smedit.setbIncludeDeleteButton(false);
		smedit.setUpdateButtonLabel("Process query");
		
	    try {
			smedit.createEditPage(getEditHTML(smedit), "");
		} catch (SQLException e) {
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?Warning=Could not process query."
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
				return;
		}
	    return;
	}
	private String getEditHTML(SMMasterEditEntry sm) throws SQLException{

		String s = layoutEditTable();
		
		//Get the parameters from the SQL statement:
		String sQueryString = clsManageRequestParameters.get_Request_Parameter(SMQuerySelect.PARAM_QUERYSTRING, sm.getRequest());
		String sSystemQueryID = clsManageRequestParameters.get_Request_Parameter(SMQuerySelect.PARAM_SYSTEMQUERYID, sm.getRequest());
		
		//If we've passed in a 'system query' ID, then that means we aren't passing in a query string, but instead
		// we are calling for a pre-built ('system') query from within the program:
		if (sSystemQueryID.compareToIgnoreCase("") != 0){
			sQueryString = SMSystemQueries.getSystemQuery(Integer.parseInt(sSystemQueryID));
			//System.out.println("[1512578289] - SMSystemQueries.getSystemQuery(Integer.parseInt(sSystemQueryID)) = '" + sQueryString);
		}
		String sRawQueryString = sQueryString.replace("\n", "<BR>");
		
		String sQueryID = clsManageRequestParameters.get_Request_Parameter(SMQuerySelect.PARAM_QUERYID, sm.getRequest());
		String sQueryTitle = clsManageRequestParameters.get_Request_Parameter(SMQuerySelect.PARAM_QUERYTITLE, sm.getRequest());
		String sFontSize = clsManageRequestParameters.get_Request_Parameter(SMQuerySelect.PARAM_FONTSIZE, sm.getRequest());
		String sPassword = clsManageRequestParameters.get_Request_Parameter(SMQuerySelect.PARAM_PWFORQUICKLINK, sm.getRequest());
		boolean bIncludeBorder = (sm.getRequest().getParameter(SMQuerySelect.PARAM_INCLUDEBORDER) != null);
		boolean bExportAsCommaDelimited = clsManageRequestParameters.get_Request_Parameter(
				SMQuerySelect.PARAM_EXPORTOPTIONS, 
				sm.getRequest()).compareToIgnoreCase(SMQuerySelect.EXPORT_COMMADELIMITED_VALUE) == 0;
		boolean bExportAsHTML = clsManageRequestParameters.get_Request_Parameter(
				SMQuerySelect.PARAM_EXPORTOPTIONS, 
				sm.getRequest()).compareToIgnoreCase(SMQuerySelect.EXPORT_HTML_VALUE) == 0;
	    boolean bAlternateRowColors = (sm.getRequest().getParameter(SMQuerySelect.PARAM_ALTERNATEROWCOLORS) != null);
	    boolean bTotalNumericFields = (sm.getRequest().getParameter(SMQuerySelect.PARAM_TOTALNUMERICFIELDS) != null);
	    boolean bShowSQLCommand = (sm.getRequest().getParameter(SMQuerySelect.PARAM_SHOWSQLCOMMAND) != null);
	    boolean bHideHeaderFooter = (sm.getRequest().getParameter(SMQuerySelect.PARAM_HIDEHEADERFOOTER) != null);
	    boolean bHideColumnLabels = (sm.getRequest().getParameter(SMQuerySelect.PARAM_HIDECOLUMNLABELS) != null);
		
		//Store hidden variables:
		s += "<INPUT TYPE=HIDDEN NAME=\"" 
			+ SMQuerySelect.PARAM_QUERYSTRING
			+ "\" VALUE=\"" + clsServletUtilities.URLEncode(sQueryString)
			+ "\">" + "\n\n";

		s += "<INPUT TYPE=HIDDEN NAME=\"" 
			+ SMQuerySelect.PARAM_RAWQUERYSTRING
			+ "\" VALUE=\"" + clsServletUtilities.URLEncode(sRawQueryString)
			+ "\">" + "\n\n";

		//System.out.println("[1512578290] - SMUtilities.URLEncode(sQueryString) = '" + SMUtilities.URLEncode(sQueryString));
		s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ SMQuerySelect.PARAM_QUERYID
				+ "\" VALUE=\"" + clsServletUtilities.URLEncode(sQueryID)
				+ "\">" + "\n\n";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" 
			+ SMQuerySelect.PARAM_QUERYTITLE
			+ "\" VALUE=\"" + clsStringFunctions.filter(sQueryTitle)
			+ "\">" + "\n\n";

		s += "<INPUT TYPE=HIDDEN NAME=\"" 
			+ SMQuerySelect.PARAM_FONTSIZE
			+ "\" VALUE=\"" + sFontSize
			+ "\">" + "\n\n";
			
		if (bAlternateRowColors){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ SMQuerySelect.PARAM_ALTERNATEROWCOLORS
				+ "\" VALUE=\"" + "Y"
				+ "\">" + "\n\n";
		}
		if (bTotalNumericFields){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ SMQuerySelect.PARAM_TOTALNUMERICFIELDS
				+ "\" VALUE=\"" + "Y"
				+ "\">" + "\n\n";
		}
		if (bShowSQLCommand){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ SMQuerySelect.PARAM_SHOWSQLCOMMAND
				+ "\" VALUE=\"" + "Y"
				+ "\">" + "\n\n";
		}
		if (bHideHeaderFooter){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ SMQuerySelect.PARAM_HIDEHEADERFOOTER
				+ "\" VALUE=\"" + "Y"
				+ "\">" + "\n\n";
		}
		if (bHideColumnLabels){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ SMQuerySelect.PARAM_HIDECOLUMNLABELS
				+ "\" VALUE=\"" + "Y"
				+ "\">" + "\n\n";
		}
		if (bIncludeBorder){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ SMQuerySelect.PARAM_INCLUDEBORDER
				+ "\" VALUE=\"" + "Y"
				+ "\">" + "\n\n";
		}
		if (bExportAsCommaDelimited){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ SMQuerySelect.PARAM_EXPORTOPTIONS
				+ "\" VALUE=\"" + SMQuerySelect.EXPORT_COMMADELIMITED_VALUE
				+ "\">" + "\n\n";
		}
		if (bExportAsHTML){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ SMQuerySelect.PARAM_EXPORTOPTIONS
				+ "\" VALUE=\"" + SMQuerySelect.EXPORT_HTML_VALUE
				+ "\">" + "\n\n";
		}
		
		s += "Query title: <B>" + sQueryTitle + "</B><BR>";

		try {
			s += createQuickLink(
				sQueryID,	
				sQueryString,
				sQueryTitle,
				sFontSize,
				sPassword,
				bIncludeBorder,
				bExportAsCommaDelimited,
				bExportAsHTML,
				bAlternateRowColors,
				bTotalNumericFields,
				bShowSQLCommand,
				bHideHeaderFooter,
				bHideColumnLabels,
				sm);
		} catch (Exception e) {
			s += "Error creating Quick Link - " + e.getMessage();
		}
		
		//Display the parameters:
		try {
			s += createParameterEntryFields(sQueryString, sm.getsDBID(), sm.getUserID(), sm.getFullUserName());
		} catch (Exception e1) {
			s += "<BR>" + e1.getMessage() + "<BR>";
		}
		return s;
	}
	private String createParameterEntryFields(String sQuery, String sDatabaseID, String sUserID, String sUserFullName) throws Exception{
		String s = "";
	    //Pattern p = Pattern.compile("\\[\\[");
		Pattern p = null;
		String[] x = null;
		try {
			p = Pattern.compile(clsStringFunctions.convertStringToRegex(SMCustomQuery.STARTINGPARAMDELIMITER));
			x = p.split(sQuery);
		}catch(Exception e){
			throw new Exception("Error [1416325304] splitting query '" + sQuery + "' into parameters - " + e.getMessage());
		}
		for (int i=0; i<x.length; i++) {
			int iEnd = x[i].indexOf(SMCustomQuery.ENDINGPARAMDELIMITER);
			if (iEnd > -1){
				String sParam = x[i].substring(0, iEnd);
				//First test for a date picker:
				//If it's a DATEPICKER parameter, then don't pick it up:
				if (isDatePickerParameter(sParam)){
					try {
						//Pick off the prompt, and the default date:
						// [[*DATEPICKER*{Prompt}{DefaultDate}]]
						String sDatePickerData = sParam.substring(SMCustomQuery.DATEPICKER_PARAM_VARIABLE.length());
						Pattern q = Pattern.compile(clsStringFunctions.convertStringToRegex(SMCustomQuery.STARTINGPARAMDATADELIMITER));
						String [] y = q.split(sDatePickerData);
						//Validate the syntax:
						if (
							(y.length < 3)
							|| (y.length < 0)
						){
							throw new Exception("Date picker syntax in '" + sParam + "' is incorrect.");
						}
						int iParamDataEnd = y[1].indexOf(SMCustomQuery.ENDINGPARAMDATADELIMITER);
						if (iParamDataEnd <= 0){
							throw new Exception("Date picker syntax in '" + sParam + "' is incorrect.");
						}
						String sParamPrompt = y[1].substring(0, iParamDataEnd);
						iParamDataEnd = y[2].indexOf(SMCustomQuery.ENDINGPARAMDATADELIMITER);
						if (iParamDataEnd <= 0){
							throw new Exception("Date picker syntax in '" + sParam + "' is incorrect.");
						}
						String sParamDefault = y[2].substring(0, iParamDataEnd);
						//Test here to see if the default date is one of the pre-established dates (TODAY, FIRST DAY OF THE YEAR, etc.)
						sParamDefault = getDefaultDatePickerDate(sParamDefault, sDatabaseID, sUserID, sUserFullName);
						String sDatePickerField = "<BR>" + sParamPrompt;
						sDatePickerField += "<BR>" 
							+ clsCreateHTMLFormFields.Create_Edit_Form_Date_Input_Field(
								clsStringFunctions.PadLeft(Integer.toString(i), "0", 3) + QUERYDATEPICKERPARAMBASE, 
								sParamDefault, 
								getServletContext()
							)
						;
						s += sDatePickerField;
					} catch (Exception e) {
						throw new Exception("Error [1416325829] in date picker prompt '" + sParam + "' - " + e.getMessage());
					}
				} else if (isDropDownParameter(sParam)){
					try {
						//If it's a drop down list:
						//Pick off the prompt:
						// [[*DROPDOWNLIST*{Prompt}{\"1\",\"2\",\"3\"}{\"First choice\",\"Second choice\",\"Third choice\"}]]
						String sDropDownData = sParam.substring(SMCustomQuery.DROPDOWN_PARAM_VARIABLE.length());
						Pattern q = Pattern.compile(clsStringFunctions.convertStringToRegex(SMCustomQuery.STARTINGPARAMDATADELIMITER));
						String [] y = q.split(sDropDownData);
						//Validate the syntax:
						if (
							(y.length < 4)
							|| (y.length < 0)
						){
							throw new Exception("Drop down list syntax in '" + sParam + "' is incorrect.");
						}
						int iParamDataEnd = y[1].indexOf(SMCustomQuery.ENDINGPARAMDATADELIMITER);
						if (iParamDataEnd <= 0){
							throw new Exception("Drop down list syntax in '" + sParam + "' is incorrect.");
						}
						String sParamPrompt = y[1].substring(0, iParamDataEnd);
						//Get the drop down list values:
						iParamDataEnd = y[2].indexOf(SMCustomQuery.ENDINGPARAMDATADELIMITER);
						if (iParamDataEnd <= 0){
							throw new Exception("Drop down list syntax in '" + sParam + "' is incorrect.");
						}
						String[] sValues = y[2].substring(0, iParamDataEnd).split(",");
						ArrayList<String> alValues = new ArrayList<String>(0);
						for (int k = 0; k < sValues.length; k++){
							alValues.add(sValues[k].replace("\"", ""));
						}
						
						//Get the drop down list descriptions:
						iParamDataEnd = y[3].indexOf(SMCustomQuery.ENDINGPARAMDATADELIMITER);
						if (iParamDataEnd <= 0){
							throw new Exception("Drop down list syntax in '" + sParam + "' is incorrect.");
						}
						String[] sDescriptions = y[3].substring(0, iParamDataEnd).split(",");
						ArrayList<String> alDescriptions = new ArrayList<String>(0);
						for (int m = 0; m < sDescriptions.length; m++){
							alDescriptions.add(sDescriptions[m].replace("\"", ""));
						}
						String sDropDownListField = "<BR>" + sParamPrompt;
						sDropDownListField += "<BR>"
							+ clsCreateHTMLFormFields.Create_Edit_Form_List_Field(
								clsStringFunctions.PadLeft(Integer.toString(i), "0", 3) + QUERYPARAMBASE, 
								alValues, 
								"", 
								alDescriptions
							)
						;
						s += sDropDownListField;
					} catch (Exception e) {
						throw new Exception("Error [1416325830] in drop down prompt '" + sParam + "' - " + e.getMessage());
					}
				} else if (isSetVariableParameter(sParam)){
					//Ignore it, and pick it up later
				}else{
					//It's a regular string parameter:
					s += "<BR>" + sParam;
					s += "<BR>" + clsCreateHTMLFormFields.Create_Edit_Form_Text_Input_Field(
						clsStringFunctions.PadLeft(Integer.toString(i), "0", 3) + QUERYPARAMBASE, 
						"", 
						100, 
						"", 
						""
					);
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
	private String getDefaultDatePickerDate(String sDefaultDateEntered, String sDatabaseID, String sUserID, String sUserFullName) throws Exception{
		String s = sDefaultDateEntered;

		String SQL = "";
		if (sDefaultDateEntered.compareToIgnoreCase(SMCustomQuery.DATEPICKER_FIRSTDAYOFMONTH_PARAM_VARIABLE) == 0){
			//SQL = " SELECT DATE_FORMAT(date_add(date_add(LAST_DAY(NOW()),interval 1 DAY),interval -1 MONTH), '%c/%e/%Y')";
			SQL = "SELECT DATE_FORMAT (NOW(), '%c/1/%Y')";
		}
		if (sDefaultDateEntered.compareToIgnoreCase(SMCustomQuery.DATEPICKER_FIRSTDAYOFYEAR_PARAM_VARIABLE) == 0){
			SQL = "SELECT DATE_FORMAT(NOW() ,'1/1/%Y')";
		}
		if (sDefaultDateEntered.compareToIgnoreCase(SMCustomQuery.DATEPICKER_LASTDAYOFMONTH_PARAM_VARIABLE) == 0){
			SQL = "SELECT DATE_FORMAT(LAST_DAY(NOW()), '%c/%e/%Y')";
		}
		if (sDefaultDateEntered.compareToIgnoreCase(SMCustomQuery.DATEPICKER_LASTDAYOFYEAR_PARAM_VARIABLE) == 0){
			SQL = "SELECT DATE_FORMAT(NOW() ,'12/31/%Y')";
		}
		if (sDefaultDateEntered.compareToIgnoreCase(SMCustomQuery.DATEPICKER_TODAY_PARAM_VARIABLE) == 0){
			SQL = "SELECT DATE_FORMAT(NOW(), '%c/%e/%Y')";
		}
		if (SQL.compareToIgnoreCase("") != 0){
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sDatabaseID,
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + ".getDefaultDatePickerDate - user: " + sUserID
											+ " - "
											+ sUserFullName
						));
			if (rs.next()){
				return(rs.getString(1));
			}else{
				throw new Exception("Error [1423244660] could not read pre-set date '" + sDefaultDateEntered + "' for datepicker");
			}
		}else{
			//If the default date doesn't match any of the pre-sets, just return it:
			return s;
		}
	}
	private boolean isDatePickerParameter(String sParameter) throws Exception{
		try {
			if (sParameter.length()>=SMCustomQuery.DATEPICKER_PARAM_VARIABLE.length()){
				if (sParameter.substring(
					0, SMCustomQuery.DATEPICKER_PARAM_VARIABLE.length()).compareToIgnoreCase(SMCustomQuery.DATEPICKER_PARAM_VARIABLE) == 0){
					return true;
				}else{
					return false;
				}
			}else{
				return false;
			}
		} catch (Exception e) {
			throw new Exception("Error [1416328945] checking for date picker in parameter string '" + sParameter + "' - " + e.getMessage());
		}
	}
	private boolean isDropDownParameter(String sParameter) throws Exception{
		try {
			if (sParameter.length()>=SMCustomQuery.DROPDOWN_PARAM_VARIABLE.length()){
				if (sParameter.substring(
					0, SMCustomQuery.DROPDOWN_PARAM_VARIABLE.length()).compareToIgnoreCase(SMCustomQuery.DROPDOWN_PARAM_VARIABLE) == 0){
					return true;
				}else{
					return false;
				}
			}else{
				return false;
			}
		} catch (Exception e) {
			throw new Exception("Error [1416328946] checking for drop down in parameter string '" + sParameter + "' - " + e.getMessage());
		}
	}
	private String createQuickLink(
			String sQueryID,
			String sQueryString,
			String sQueryTitle,
			String sFontSize,
			String sPassword,
			boolean bIncludeBorder,
			boolean bExportAsCommaDelimited,
			boolean bExportAsHTML,
			boolean bAlternateRowColors,
			boolean bTotalNumericFields,
			boolean bShowSQLCommand,
			boolean bHideHeaderFooter,
			boolean bHideColumnLabels,
			SMMasterEditEntry sm) throws Exception{

		String s = "";
		String sDbName = sm.getsDBID();

		if (sPassword.compareToIgnoreCase("") == 0){
			return s;
		}

		//check password
		try{
			String SQL = "SELECT "
				+ SMTableusers.sHashedPw + " from " + SMTableusers.TableName 
				+ " where " + SMTableusers.lid + " = " + sm.getUserID() + "";
			ResultSet rsHashedPw = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), sm.getsDBID(),
					"MySQL", 
					this.toString() + ".createQuickLink" + " User: " + sm.getUserID()
					+ " - "
					+ sm.getFullUserName()
					);
			if (!rsHashedPw.next()){
				rsHashedPw.close();
				throw new Exception("Error - The user " + sm.getUserID() + " - " + sm.getFullUserName() + "can't be found in the database.");
			}
			String sDBPw = rsHashedPw.getString(SMTableusers.sHashedPw);
			rsHashedPw.close();
			SQL = "SELECT SHA('" + sPassword + "') AS Passwd";
			ResultSet rsEnteredPw = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sm.getsDBID(),
					"MySQL", 
					this.toString() + ".createQuickLinks" + " User: " + sm.getUserID()
					+ " - "
					+ sm.getFullUserName()
					);
			rsEnteredPw.next();
			String sEtrPw = rsEnteredPw.getString("Passwd");
			if (sEtrPw.compareTo(sDBPw) != 0){
				throw new Exception("Error - The password you entered doesn't match your current password.");
			}
		}catch (SQLException ex){
			throw new Exception("Error reading passwords - " + ex.getMessage());
		}
		s += "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
			+ SMUtilities.getFullClassName(this.toString())
			+ "?CallingClass=smcontrolpanel.SMQuerySelect"
			+ "&user=" + sm.getUserName()
			+ "&pw=" + sPassword 
			+ "&db=" + sDbName
			+ "&" + SMQuerySelect.PARAM_QUERYTITLE + "=" + clsStringFunctions.filter(sQueryTitle)
			+ "&" + SMQuerySelect.PARAM_QUERYSTRING + "=" + clsServletUtilities.URLEncode(sQueryString)
			+ "&" + SMQuerySelect.PARAM_FONTSIZE + "=" + sFontSize
			+ "&" + SMQuerySelect.PARAM_QUERYID + "=" + sQueryID;
		;
			
			if (bIncludeBorder){
				s += "&" + SMQuerySelect.PARAM_INCLUDEBORDER + "=Y";
			}
			if (bExportAsCommaDelimited){
				s += "&" + SMQuerySelect.PARAM_EXPORTOPTIONS + "=" + SMQuerySelect.EXPORT_COMMADELIMITED_VALUE;
			}
			if (bExportAsHTML){
				s += "&" + SMQuerySelect.PARAM_EXPORTOPTIONS + "=" + SMQuerySelect.EXPORT_HTML_VALUE;
			}
			if (bAlternateRowColors){
				s += "&" + SMQuerySelect.PARAM_ALTERNATEROWCOLORS + "=Y";
			}
			if (bTotalNumericFields){
				s += "&" + SMQuerySelect.PARAM_TOTALNUMERICFIELDS + "=Y";
			}
			if (bShowSQLCommand){
				s += "&" + SMQuerySelect.PARAM_SHOWSQLCOMMAND + "=Y";
			}
			if (bHideHeaderFooter){
				s += "&" + SMQuerySelect.PARAM_HIDEHEADERFOOTER + "=Y";
			}
			if (bHideColumnLabels){
				s += "&" + SMQuerySelect.PARAM_HIDECOLUMNLABELS + "=Y";
			}
			s += "\">"
			+ "This is a <I>'Quick Link'</I> to this query"
			+ "</A>"
		;
		return s + "<BR>";
	}
	public static String layoutEditTable(){
		String s = "";
		String sBorderSize = "2";
		String sFontSize = "small";
		s += "<style type=\"text/css\">\n";
		
		//Set hyperlink style:
		//s += "a {font-family : Arial; Font-size : 12px; text-decoration : none}\n";
		
		//s += "amenu {font-family : Arial; text-decoration : none; font-weight: 900}\n";
		//s += "amenu:link {color : white}\n";
		//s += "amenu:visited {color : #99FFFF}\n";
		//s += "amenu:active {color : #99FFFF}\n";
		//s += "amenu:hover {color : white}\n";
		
		//s += "a {font-family : Arial; text-decoration : none; font-weight: 900}\n";
		//s += "a:link {color : #99FFFF}\n";
		//s += "a:visited {color : #99FFFF}\n";
		//s += "a:active {color : #99FFFF}\n";
		//s += "a:hover {color : white}\n";
		
		//Layout table:
		s +=
			"table.main {"
			+ "border-width: " + sBorderSize + "px; "
			+ "border-spacing: 2px; "
			+ "border-style: outset; "
			+ "border-style: solid; "
			+ "border-color: black; "
			+ "border-collapse: separate; "
			+ "width: 100%; "
			+ "font-size: " + sFontSize + "; "
			+ "font-family : Arial; "
			//+ "background-color: white; "
			+ "}"
			+ "\n"
			;

		s +=
			"table.main th {"
			+ "border-width: " + sBorderSize + "px; "
			+ "padding: 2px; "
			//+ "border-style: inset; "
			+ "border-style: none; "
			+ "border-color: white; "
			//+ "background-color: white; "
			+ "color: black; "
			+ "font-family : Arial; "
			+ "vertical-align: text-middle; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;

		//s +=
		//	"tr.d0 td {"
		//	+ "background-color: #FFFFFF; "
		//	+"}"
		//	;
		//s +=
		//	"tr.d1 td {"
		//	+ "background-color: #EEEEEE; "
		//	+ "}"
		//	+ "\n"
		//	;

		s +=
			"td.r {"
			+ "border-width: " + sBorderSize + "px; "
			+ "padding: 2px; "
			//+ "border-style: solid; "
			//+ "border-color: black; "
			+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "text-align: right; "
			+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;

		s +=
			"td.l {"
			+ "border-width: " + sBorderSize + "px; "
			+ "padding: 2px; "
			//+ "border-style: solid; "
			//+ "border-color: black; "
			+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "text-align: left; "
			+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;

		s += "</style>"
			+ "\n"
			;

		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
