package TimeCardSystem;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;
import TCSDataDefinition.TCSTablecompanyprofile;

public class TCQueryParameters  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String QUERYPARAMBASE = "QPBASE";
	public static final String QUERYDATEPICKERPARAMBASE = "QPDATEPICKERBASE";
	private static final String CALLED_CLASS_NAME = "TimeCardSystem.TCQueryGenerate";
	private static final String SUBMIT_BUTTON_VALUE = "Process query";

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		String sDBID = "";
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		
		try {
			sDBID = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
		} catch (Exception e1) {
			sDBID = "";
		}
		if (sDBID == null){
			sDBID = "";
		}
		
		if (sDBID.compareToIgnoreCase("") == 0){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + "No database name found in session.</FONT></B><BR>");
			return;
		}
		
		String sUserID = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID);
		if (sUserID == null){
			sUserID = "";
		}
		
		if (sUserID.compareToIgnoreCase("") == 0){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + "No employee ID found in session.</FONT></B><BR>");
			return;
		}
		
		//Get the company information:
		String sSQL = "SELECT * FROM " + TCSTablecompanyprofile.TableName;

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sDBID, 
					"MySQL",
					this.toString() + ".reading company name"
					);
			if (rs.next()){
				CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_COMPANYNAME, rs.getString(TCSTablecompanyprofile.sCompanyName));
				rs.close();
			}else{
				out.println("<BR>Could not read company name.");
				out.println("</BODY></HTML>");
				rs.close();
				return;
			}
		} catch (SQLException e) {
			out.println("<BR>Error reading read company name: " + e.getMessage()+ ".");
			out.println("</BODY></HTML>");
			return;
		}
		
		String sTitle = "Time Card System";
		String sSubtitle = "Time Card System - " + CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_COMPANYNAME).toString();
		out.println(TimeCardUtilities.TCTitleSubBGColorWithFont(sTitle, sSubtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS, TimeCardUtilities.BASE_FONT_FAMILY));

		out.println(clsServletUtilities.getDatePickerIncludeString(getServletContext()) + "\n");
		
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"BLACK\">STATUS: " + sStatus + "</FONT></B><BR>");
		}
		
    	out.println("<TD><A HREF=\"" + clsServletUtilities.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain?" 
			+ TimeCardUtilities.SESSION_ATTRIBUTE_DB + "=" + sDBID 
			+ "&" + TimeCardUtilities.SESSION_ATTRIBUTE_PINNUMBER + " = " + sUserID
			+ "\">Return to main admin menu</A><BR>");

		out.println("<BR>");
		
		//Print a link to the main query page:
		out.println("<BR><A HREF=\"" + clsServletUtilities.getURLLinkBase(getServletContext()) + "TimeCardSystem.TCQuerySelect" 
				+ "\">Return to Manage Queries</A>");

		out.println("<BR><BR>");
		
		//Now print the edit page:
		try {
			out.println(getEditHTML(request, sDBID, sUserID));
		} catch (Exception e) {
			response.sendRedirect(
				"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + "" + clsManageRequestParameters.get_Request_Parameter("CallingClass", request)
				+ "?Warning=Could not process query - " + e.getMessage()
			);
			return;
		}

	    return;
	}
	private String getEditHTML(HttpServletRequest req, String sDBID, String sUserID) throws Exception{

		String s = layoutEditTable();
		
		//Get the parameters from the SQL statement:
		String sQueryString = clsManageRequestParameters.get_Request_Parameter(TCQuerySelect.PARAM_QUERYSTRING, req);
		String sSystemQueryID = clsManageRequestParameters.get_Request_Parameter(TCQuerySelect.PARAM_SYSTEMQUERYID, req);
		
		//If we've passed in a 'system query' ID, then that means we aren't passing in a query string, but instead
		// we are calling for a pre-built ('system') query from within the program:
		if (sSystemQueryID.compareToIgnoreCase("") != 0){
			sQueryString = TCSystemQueries.getSystemQuery(Integer.parseInt(sSystemQueryID));
			//System.out.println("[1512578289] - SMSystemQueries.getSystemQuery(Integer.parseInt(sSystemQueryID)) = '" + sQueryString);
		}
		String sRawQueryString = sQueryString.replace("\n", "<BR>");
		
		String sQueryID = clsManageRequestParameters.get_Request_Parameter(TCQuerySelect.PARAM_QUERYID, req);
		String sQueryTitle = clsManageRequestParameters.get_Request_Parameter(TCQuerySelect.PARAM_QUERYTITLE, req);
		String sFontSize = clsManageRequestParameters.get_Request_Parameter(TCQuerySelect.PARAM_FONTSIZE, req);
		boolean bIncludeBorder = (req.getParameter(TCQuerySelect.PARAM_INCLUDEBORDER) != null);
		boolean bExportAsCommaDelimited = clsManageRequestParameters.get_Request_Parameter(
				TCQuerySelect.PARAM_EXPORTOPTIONS, 
				req).compareToIgnoreCase(TCQuerySelect.EXPORT_COMMADELIMITED_VALUE) == 0;
		boolean bExportAsHTML = clsManageRequestParameters.get_Request_Parameter(
				TCQuerySelect.PARAM_EXPORTOPTIONS, 
				req).compareToIgnoreCase(TCQuerySelect.EXPORT_HTML_VALUE) == 0;
	    boolean bAlternateRowColors = (req.getParameter(TCQuerySelect.PARAM_ALTERNATEROWCOLORS) != null);
	    boolean bTotalNumericFields = (req.getParameter(TCQuerySelect.PARAM_TOTALNUMERICFIELDS) != null);
	    boolean bShowSQLCommand = (req.getParameter(TCQuerySelect.PARAM_SHOWSQLCOMMAND) != null);
	    boolean bHideHeaderFooter = (req.getParameter(TCQuerySelect.PARAM_HIDEHEADERFOOTER) != null);
	    boolean bHideColumnLabels = (req.getParameter(TCQuerySelect.PARAM_HIDECOLUMNLABELS) != null);
		
	    s += "<FORM ID='MAINFORM' NAME='MAINFORM' ACTION='" 
	    	+ clsServletUtilities.getURLLinkBase(getServletContext()) 
	    	+ CALLED_CLASS_NAME 
	    	+ "' METHOD='POST'>";
	    
		//Store hidden variables:
		s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ "CallingClass"
				+ "\" VALUE=\"" + TimeCardUtilities.getFullClassName(this.toString())
				+ "\">" + "\n\n";
	    
		s += "<INPUT TYPE=HIDDEN NAME=\"" 
			+ TCQuerySelect.PARAM_QUERYSTRING
			+ "\" VALUE=\"" + TimeCardUtilities.URLEncode(sQueryString)
			+ "\">" + "\n\n";

		s += "<INPUT TYPE=HIDDEN NAME=\"" 
			+ TCQuerySelect.PARAM_RAWQUERYSTRING
			+ "\" VALUE=\"" + TimeCardUtilities.URLEncode(sRawQueryString)
			+ "\">" + "\n\n";

		//System.out.println("[1512578290] - TimeCardUtilities.URLEncode(sQueryString) = '" + TimeCardUtilities.URLEncode(sQueryString));
		s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ TCQuerySelect.PARAM_QUERYID
				+ "\" VALUE=\"" + TimeCardUtilities.URLEncode(sQueryID)
				+ "\">" + "\n\n";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" 
			+ TCQuerySelect.PARAM_QUERYTITLE
			+ "\" VALUE=\"" + clsStringFunctions.filter(sQueryTitle)
			+ "\">" + "\n\n";

		s += "<INPUT TYPE=HIDDEN NAME=\"" 
			+ TCQuerySelect.PARAM_FONTSIZE
			+ "\" VALUE=\"" + sFontSize
			+ "\">" + "\n\n";
			
		if (bAlternateRowColors){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ TCQuerySelect.PARAM_ALTERNATEROWCOLORS
				+ "\" VALUE=\"" + "Y"
				+ "\">" + "\n\n";
		}
		if (bTotalNumericFields){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ TCQuerySelect.PARAM_TOTALNUMERICFIELDS
				+ "\" VALUE=\"" + "Y"
				+ "\">" + "\n\n";
		}
		if (bShowSQLCommand){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ TCQuerySelect.PARAM_SHOWSQLCOMMAND
				+ "\" VALUE=\"" + "Y"
				+ "\">" + "\n\n";
		}
		if (bHideHeaderFooter){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ TCQuerySelect.PARAM_HIDEHEADERFOOTER
				+ "\" VALUE=\"" + "Y"
				+ "\">" + "\n\n";
		}
		if (bHideColumnLabels){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ TCQuerySelect.PARAM_HIDECOLUMNLABELS
				+ "\" VALUE=\"" + "Y"
				+ "\">" + "\n\n";
		}
		if (bIncludeBorder){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ TCQuerySelect.PARAM_INCLUDEBORDER
				+ "\" VALUE=\"" + "Y"
				+ "\">" + "\n\n";
		}
		if (bExportAsCommaDelimited){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ TCQuerySelect.PARAM_EXPORTOPTIONS
				+ "\" VALUE=\"" + TCQuerySelect.EXPORT_COMMADELIMITED_VALUE
				+ "\">" + "\n\n";
		}
		if (bExportAsHTML){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ TCQuerySelect.PARAM_EXPORTOPTIONS
				+ "\" VALUE=\"" + TCQuerySelect.EXPORT_HTML_VALUE
				+ "\">" + "\n\n";
		}
		
		s += "Query title: <B>" + sQueryTitle + "</B><BR>";

		//Display the parameters:
		try {
			s += createParameterEntryFields(sQueryString, sDBID, sUserID);
		} catch (Exception e1) {
			s += "<BR>" + e1.getMessage() + "<BR>";
		}
		
		s += "<BR><BR>"
			+ "<INPUT TYPE=SUBMIT" + " VALUE='" + SUBMIT_BUTTON_VALUE + "' >" + "\n";
		;
		
		s += "</FORM>";
		
		return s;
	}
	private String createParameterEntryFields(String sQuery, String sDatabaseID, String sUser) throws Exception{
		String s = "";
	    //Pattern p = Pattern.compile("\\[\\[");
		Pattern p = null;
		String[] x = null;
		
		try {
			p = Pattern.compile(clsStringFunctions.convertStringToRegex(TCCustomQuery.STARTINGPARAMDELIMITER));
			x = p.split(sQuery);
		}catch(Exception e){
			throw new Exception("Error [1416325304] splitting query '" + sQuery + "' into parameters - " + e.getMessage());
		}
		
		for (int i=0; i<x.length; i++) {
			int iEnd = x[i].indexOf(TCCustomQuery.ENDINGPARAMDELIMITER);
			if (iEnd > -1){
				String sParam = x[i].substring(0, iEnd);
				//First test for a date picker:
				//If it's a DATEPICKER parameter, then don't pick it up:
				if (isDatePickerParameter(sParam)){
					try {
						//Pick off the prompt, and the default date:
						// [[*DATEPICKER*{Prompt}{DefaultDate}]]
						String sDatePickerData = sParam.substring(TCCustomQuery.DATEPICKER_PARAM_VARIABLE.length());
						Pattern q = Pattern.compile(clsStringFunctions.convertStringToRegex(TCCustomQuery.STARTINGPARAMDATADELIMITER));
						String [] y = q.split(sDatePickerData);
						//Validate the syntax:
						if (
							(y.length < 3)
							|| (y.length < 0)
						){
							throw new Exception("Date picker syntax in '" + sParam + "' is incorrect.");
						}
						int iParamDataEnd = y[1].indexOf(TCCustomQuery.ENDINGPARAMDATADELIMITER);
						if (iParamDataEnd <= 0){
							throw new Exception("Date picker syntax in '" + sParam + "' is incorrect.");
						}
						String sParamPrompt = y[1].substring(0, iParamDataEnd);
						iParamDataEnd = y[2].indexOf(TCCustomQuery.ENDINGPARAMDATADELIMITER);
						if (iParamDataEnd <= 0){
							throw new Exception("Date picker syntax in '" + sParam + "' is incorrect.");
						}
						String sParamDefault = y[2].substring(0, iParamDataEnd);
						//Test here to see if the default date is one of the pre-established dates (TODAY, FIRST DAY OF THE YEAR, etc.)
						sParamDefault = getDefaultDatePickerDate(sParamDefault, sDatabaseID, sUser);
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
						String sDropDownData = sParam.substring(TCCustomQuery.DROPDOWN_PARAM_VARIABLE.length());
						Pattern q = Pattern.compile(clsStringFunctions.convertStringToRegex(TCCustomQuery.STARTINGPARAMDATADELIMITER));
						String [] y = q.split(sDropDownData);
						//Validate the syntax:
						if (
							(y.length < 4)
							|| (y.length < 0)
						){
							throw new Exception("Drop down list syntax in '" + sParam + "' is incorrect.");
						}
						int iParamDataEnd = y[1].indexOf(TCCustomQuery.ENDINGPARAMDATADELIMITER);
						if (iParamDataEnd <= 0){
							throw new Exception("Drop down list syntax in '" + sParam + "' is incorrect.");
						}
						String sParamPrompt = y[1].substring(0, iParamDataEnd);
						//Get the drop down list values:
						iParamDataEnd = y[2].indexOf(TCCustomQuery.ENDINGPARAMDATADELIMITER);
						if (iParamDataEnd <= 0){
							throw new Exception("Drop down list syntax in '" + sParam + "' is incorrect.");
						}
						String[] sValues = y[2].substring(0, iParamDataEnd).split(",");
						ArrayList<String> alValues = new ArrayList<String>(0);
						for (int k = 0; k < sValues.length; k++){
							alValues.add(sValues[k].replace("\"", ""));
						}
						
						//Get the drop down list descriptions:
						iParamDataEnd = y[3].indexOf(TCCustomQuery.ENDINGPARAMDATADELIMITER);
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
			if (sParameter.length()>=TCCustomQuery.SETVARIABLECOMMAND.length()){
				if (sParameter.substring(
					0, TCCustomQuery.SETVARIABLECOMMAND.length()).compareToIgnoreCase(TCCustomQuery.SETVARIABLECOMMAND) == 0){
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
	private String getDefaultDatePickerDate(String sDefaultDateEntered, String sDatabaseID, String sUser) throws Exception{
		String s = sDefaultDateEntered;

		String SQL = "";
		if (sDefaultDateEntered.compareToIgnoreCase(TCCustomQuery.DATEPICKER_FIRSTDAYOFMONTH_PARAM_VARIABLE) == 0){
			//SQL = " SELECT DATE_FORMAT(date_add(date_add(LAST_DAY(NOW()),interval 1 DAY),interval -1 MONTH), '%c/%e/%Y')";
			SQL = "SELECT DATE_FORMAT (NOW(), '%c/1/%Y')";
		}
		if (sDefaultDateEntered.compareToIgnoreCase(TCCustomQuery.DATEPICKER_FIRSTDAYOFYEAR_PARAM_VARIABLE) == 0){
			SQL = "SELECT DATE_FORMAT(NOW() ,'1/1/%Y')";
		}
		if (sDefaultDateEntered.compareToIgnoreCase(TCCustomQuery.DATEPICKER_LASTDAYOFMONTH_PARAM_VARIABLE) == 0){
			SQL = "SELECT DATE_FORMAT(LAST_DAY(NOW()), '%c/%e/%Y')";
		}
		if (sDefaultDateEntered.compareToIgnoreCase(TCCustomQuery.DATEPICKER_LASTDAYOFYEAR_PARAM_VARIABLE) == 0){
			SQL = "SELECT DATE_FORMAT(NOW() ,'12/31/%Y')";
		}
		if (sDefaultDateEntered.compareToIgnoreCase(TCCustomQuery.DATEPICKER_TODAY_PARAM_VARIABLE) == 0){
			SQL = "SELECT DATE_FORMAT(NOW(), '%c/%e/%Y')";
		}
		if (SQL.compareToIgnoreCase("") != 0){
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sDatabaseID,
				"MySQL", 
				TimeCardUtilities.getFullClassName(this.toString() + ".getDefaultDatePickerDate - user: " + sUser));
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
			if (sParameter.length()>=TCCustomQuery.DATEPICKER_PARAM_VARIABLE.length()){
				if (sParameter.substring(
					0, TCCustomQuery.DATEPICKER_PARAM_VARIABLE.length()).compareToIgnoreCase(TCCustomQuery.DATEPICKER_PARAM_VARIABLE) == 0){
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
			if (sParameter.length()>=TCCustomQuery.DROPDOWN_PARAM_VARIABLE.length()){
				if (sParameter.substring(
					0, TCCustomQuery.DROPDOWN_PARAM_VARIABLE.length()).compareToIgnoreCase(TCCustomQuery.DROPDOWN_PARAM_VARIABLE) == 0){
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
