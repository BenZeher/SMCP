package smcontrolpanel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import ConnectionPool.CompanyDataCredentials;
import ConnectionPool.ServerSettingsFileParameters;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTablecompanyprofile;
import SMDataDefinition.SMTableicoptions;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;
import ServletUtilities.clsURLRecord;

public class SMUtilities extends clsServletUtilities {

	public static String DEFAULT_FONT_FAMILY = "Arial";
	
	//SMCP Session parameters:
	public static String SMCP_SESSION_PARAM_DATABASE_ID = "DatabaseID";
	public static String SMCP_SESSION_PARAM_COMPANYNAME = "CompanyName";
	public static String SMCP_SESSION_PARAM_USERFIRSTNAME = "UserFirstName";
	public static String SMCP_SESSION_PARAM_USERLASTNAME = "UserLastName";
	public static String SMCP_SESSION_PARAM_USERNAME = "UserName";
	public static String SMCP_SESSION_PARAM_USERID = "UserID";
	public static String SMCP_SESSION_PARAM_MOBILE = "mobile";
	public static String SMCP_SESSION_PARAM_URLHISTORY = "URLHistory";
	public static String SMCP_SESSION_PARAM_SESSIONTAG = "SESSIONTAG";
	public static String SMCP_SESSION_PARAM_ACCESSCOUNTER = "ACCESSCOUNTER";
	public static String SMCP_SESSION_PARAM_OPTS = "OPTS";
	public static String SMCP_SESSION_PARAM_CHECK_SCHEDULE = "YES";
	public static String SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL = "MODULELEVEL";
	public static String SMCP_SESSION_PARAM_UPDATE_REQUIRED = "UPDATEREQUIRED";
	
	//Request parameters:
	public static String SMCP_REQUEST_PARAM_PASSWORD = "pw";
	public static String SMCP_REQUEST_PARAM_USER = "user";
	public static String REQUEST_PARAM_USERID = "userid";
	public static String SMCP_REQUEST_PARAM_DATABASE_ID = "db";
	public static String SMCP_REQUEST_PARAM_MOBILE = "mobile";
	public static String REQUEST_PARAM_SHOWCONTEXTPARAMETERS = "showcontextparams";
	//public static String REQUEST_PARAM_SESSIONTAG = "SESSIONTAG";
	public static String SMCP_REQUEST_PARAM_OPTS = "OPTS";
	public static String SMCP_REQUEST_PARAM_REDIRECT_CLASS = "redirectclass";
	
	//HTML Languages:
	public static String LANGUAGE_HTML_ENGLISH = "en";
	
	public static String PASSWORD_REQUIREMENTS = "Passwords must be at least 7 characters, must include"
		+ " at least one upper case character, one lower case character, and one digit.";
	public static int SMCP_MAX_SESSION_INTERVAL_IN_SECONDS = 7200; //2 hours
	public static SimpleDateFormat sdfSQLDate = new SimpleDateFormat("yyyy-MM-dd");
	public static SimpleDateFormat sdfNormalDate = new SimpleDateFormat("MM/dd/yyyy");
	public static SimpleDateFormat sdfTime = new SimpleDateFormat("hhmmss");
		
	//Constants for checking licenses:
	public static String SMCP_LICENSE_DIRECTORY = "smcplicense";
	public static String SMCP_LICENSE_SUBDIRECTORY = "WEB-INF";
	public static String SMCP_LICENSE_FILE = "license.bin";
	public static final int SMCP_LICENSE_PAD_LENGTH = 3;
	public static final String SMCP_LICENSE_WAR_FILE = "smcplicense.war";
	
	//Used to encrypt licenses:
	public static final String sFirstPart = "SMCP";
	public static final String LICENSE_KEY_DELIMITER = ",";
	public static final String sSecondPart = "@#$";
	
	//Local custom settings file:
	// TJR - 9/18/2017:
	//This file is independent of anything in the database.  It's used to 'turn on/off' functions, mainly in the 
	// system's background scheduler, for example, for the alarm system.  So that way, if we want to use a real
	// SMCP database for testing, etc., tomcat won't run the alarm checks on the test system, even if all the data is identical.
	// If we didn't use this, then ANY copy of the data would run the background schedule tasks, and we could wind up having them
	// run on several different systems redundantly.

	
	public static String SMCPTitleSubBGColor(
			String title, 
			String subtitle, 
			String backgroundcolor,
			String scompanyname
	){

		return SMCPTitleSubBGColor(
				title, 
				subtitle, 
				backgroundcolor,
				SMUtilities.DEFAULT_FONT_FAMILY,
				scompanyname,
				false,
				""
		);
	}

	public static String SMCPTitleSubBGColor(
			String title, 
			String subtitle, 
			String backgroundcolor,
			String sfontfamily,
			String scompanyname,
			boolean bMobileView,
			String sOnLoadFunction
	){
		String sOnLoad = "";
		if (sOnLoadFunction.compareToIgnoreCase("") != 0){
			sOnLoad = " onload=\"" + sOnLoadFunction + "\"";
		}
		String s = DOCTYPE
		+ "<HTML>"
		+ "<HEAD>"
		+ "\n" + clsServletUtilities.getNoCacheHeaderString() + "\n";

		if (bMobileView){
			s += "<TITLE>" + subtitle + "</TITLE>"
			+ SMUtilities.faviconLink()
			//This line should keep the font widths 'screen' wide:
			+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\" />"
			+ "</HEAD>\n" 
			+ "<BODY BGCOLOR="
			+ "\"" 
			+ backgroundcolor
			+ "\""
			+ " style=\"font-family: " + sfontfamily + ";\""
			+ sOnLoad
			+ "\">"
			;
		}else{
			s += "<TITLE>" + title + "</TITLE>"
			+ SMUtilities.faviconLink()
			+ "</HEAD>\n<BR>" 
			+ "<BODY BGCOLOR=\"" + backgroundcolor + "\""
			+ " style=\"font-family: " + sfontfamily + ";\""
			+ sOnLoad
			+ ">"
			;
		}
		s += "<TABLE BORDER=0>"
		+"<TR><TD VALIGN=BOTTOM><H3>" + scompanyname + ": " + title + "</H3></TD>"
		;

		if (subtitle.compareTo("") != 0){  
			s = s + "<TD VALIGN=BOTTOM><H4>&nbsp;-&nbsp;" + subtitle + "</H4></TD>";
		}

		s = s + "</TR></TABLE>";
		return s;
	}
	public static String SMCPTitleSubBGColorWithAutoRefresh(
			String title, 
			String subtitle, 
			String backgroundcolor,
			String sfontfamily,
			String scompanyname,
			boolean bMobileView,
			String sOnLoadFunction,
			int iAutoRefreshIntervalInSeconds
	){
		String sOnLoad = "";
		if (sOnLoadFunction.compareToIgnoreCase("") != 0){
			sOnLoad = " onload=\"" + sOnLoadFunction + "\"";
		}
		String s = DOCTYPE
		+ "<HTML>"
		+ "<HEAD>"
		+ "\n" + clsServletUtilities.getNoCacheHeaderString() + "\n";
		
		if (iAutoRefreshIntervalInSeconds > 0){
			s += "<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"" + Integer.toString(iAutoRefreshIntervalInSeconds) + "\">";
		}

		if (bMobileView){
			s += "<TITLE>" + subtitle + "</TITLE>"
			+ SMUtilities.faviconLink()
			//This line should keep the font widths 'screen' wide:
			+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\" />"
			+ "</HEAD>\n" 
			+ "<BODY BGCOLOR="
			+ "\"" 
			+ backgroundcolor
			+ "\""
			+ " style=\"font-family: " + sfontfamily + ";\""
			+ sOnLoad
			+ "\">"
			;
		}else{
			s += "<TITLE>" + title + "</TITLE>"
			+ SMUtilities.faviconLink()
			+ "</HEAD>\n<BR>" 
			+ "<BODY BGCOLOR=\"" + backgroundcolor + "\""
			+ " style=\"font-family: " + sfontfamily + ";\""
			+ sOnLoad
			+ ">"
			;
		}
		s += "<TABLE BORDER=0>"
		+"<TR><TD VALIGN=BOTTOM><H3>" + scompanyname + ": " + title + "</H3></TD>"
		;

		if (subtitle.compareTo("") != 0){  
			s = s + "<TD VALIGN=BOTTOM><H4>&nbsp;-&nbsp;" + subtitle + "</H4></TD>";
		}

		s = s + "</TR></TABLE>";
		return s;
	}

	public static String lowProfileSMCPTitle(
			String title, 
			String subtitle, 
			String backgroundcolor,
			String sfontfamily,
			String scompanyname,
			boolean bMobileView,
			String sOnLoadFunction
	){

		String sOnLoad = "";
		if (sOnLoadFunction.compareToIgnoreCase("") != 0){
			sOnLoad = " onload=\"" + sOnLoadFunction + "\"";
		}
		String s = DOCTYPE
		+ "<HTML>"
		+ "<HEAD>"
		+ "\n" + clsServletUtilities.getNoCacheHeaderString() + "\n";

		if (bMobileView){
			s += "<TITLE>" + subtitle + "</TITLE>"
			+ SMUtilities.faviconLink()
			//This line should keep the font widths 'screen' wide:
			+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\" />\n"
			+ clsServletUtilities.getMasterStyleSheetLink()+"\n"
			+ "</HEAD>\n" 
			+ "<BODY BGCOLOR="
			+ "\"" 
			+ backgroundcolor
			+ "\""
			+ " style=\"font-family: " + sfontfamily + "\";"
			+ sOnLoad
			+ "\">"
			;
		}else{
			s += "<TITLE>" + title + "</TITLE>"
			+ SMUtilities.faviconLink()
			+ "</HEAD>\n<BR>" 
			+ "<BODY BGCOLOR=\"" + backgroundcolor + "\""
			+ " style=\"font-family: " + sfontfamily + ";\""
			+ sOnLoad
			+ ">"
			;
		}

		s += "<span style=\"font-size:medium;\" >" + scompanyname + ": " + title + "</span>";
		
		if (subtitle.compareTo("") != 0){  
			s = s + "<span style=\"font-size: small;\" >" + " - " + subtitle + "</span>";
		}

		return s;
	}
	
	public static String getMenuHead(
			String subtitle,
			String backgroundcolor,
			String sfontfamily,
			boolean bMobileView,
			ServletContext context) {

		String s = SMUtilities.DOCTYPE
		+ "<HTML>"
		+ "<HEAD>"
		+ "\n" + clsServletUtilities.getNoCacheHeaderString() + "\n";

		if (bMobileView){
			s += "<TITLE>" + subtitle + "</TITLE>"
			+ SMUtilities.faviconLink(context)
			//This line should keep the font widths 'screen' wide:
			+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\" />"
			+ "</HEAD>\n" 
			+ "<BODY BGCOLOR="
			+ "\"" 
			+ backgroundcolor
			+ "\""
			+ " style=\"font-family: " + sfontfamily + "\";"
			+ "\">"
			+ SMUtilities.getLogoLink(context)
			;
		}else{
			s += SMUtilities.faviconLink(context)
			+ "</HEAD>\n"
			+ "<BODY BGCOLOR=\"" + backgroundcolor + "\""
			+ " style=\"font-family: " + sfontfamily + "\";"
			+ ">"
			+ getLogoLink(context);

		}
		if (subtitle.compareTo("") != 0){
			s += "<span style = \"font-size:x-large; font-weight: bold; \">" + subtitle + "</span>";
		}
		s += "<BR>";
		return s;
	}
	
	public static String getLoginHead(
			String subtitle,
			String backgroundcolor,
			String sfontfamily,
			boolean bMobileView,
			ServletContext context) {

		String s = SMUtilities.DOCTYPE
		+ "<HTML>"
		+ "<HEAD>"
		+ "\n" + clsServletUtilities.getNoCacheHeaderString() + "\n";

		if (bMobileView){
			s += "<TITLE>" + subtitle + "</TITLE>"
			+ SMUtilities.faviconLink(context)
			//This line should keep the font widths 'screen' wide:
			+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\" />"
			+ "</HEAD>\n" 
			+ "<BODY BGCOLOR="
			+ "\"" 
			+ backgroundcolor
			+ "\""
			+ " style=\"font-family: " + sfontfamily + "\";"
			+ "\">"
			+ SMUtilities.getLogoLink(context)
			;
			//if (subtitle.compareTo("") != 0){
			//	sHeading += "<TABLE BORDER=0><TR>";
			//	sHeading += "<TD VALIGN=BOTTOM><H3>" + subtitle + "</H3></TD>";
			//	sHeading += "</TR></TABLE>";
		}else{
			s += SMUtilities.faviconLink(context)
			+ "</HEAD>\n"
			+ "<BODY BGCOLOR=\"" + backgroundcolor + "\""
			+ " style=\"font-family: " + sfontfamily + "\";"
			+ ">"
			+ getLargeLogoLink(context);

		}
		if (subtitle.compareTo("") != 0){
			s += "<span style = \"font-size:x-large; font-weight: bold; \">" + subtitle + "</span><BR>";
		}
		return s;
	}
	
	public static String faviconLink(ServletContext context){

		String sImagePath = WebContextParameters.getInitImagePath(context);

		if (sImagePath != null){
			return 	"<link rel=\"shortcut icon\" href=\"" + sImagePath + "SMCP16.ico\" type=\"image/x-icon\" />";
		}else{
			return 	"<link rel=\"shortcut icon\" href=\"../images/SMCP16.ico\" type=\"image/x-icon\" />";
		}
	}
	
	public static String faviconLink(){
		return 	"<link rel=\"shortcut icon\" href=\"/sm/images/SMCP16.ico\" type=\"image/x-icon\" />";
	}

	public static String getOpenTruckMarker (ServletContext context, String sColor){

		String sImagePath = WebContextParameters.getInitImagePath(context);

		if (sImagePath != null){
			return 	sImagePath + "opentruck" + sColor + ".png";
		}else{
			return 	"/images/opentruck" + sColor + ".png";
		}
	}
	
	public static String getOpenTruckMarker (ServletContext context){

		String sImagePath = WebContextParameters.getInitImagePath(context);

		if (sImagePath != null){
			return 	sImagePath + "opentruck.png";
		}else{
			return 	"/images/opentruck.png";
		}
	}
	
	public static String getMechanicMarkerSprite (ServletContext context){

		String sImagePath = WebContextParameters.getInitImagePath(context);

		if (sImagePath != null){
			return 	sImagePath + "mechanicicons.png";
		}else{
			return 	"/images/mechanicicons.png";
		}
	}
	
	public static String getMechanicMarkerShadow (ServletContext context){

		String sImagePath = WebContextParameters.getInitImagePath(context);

		if (sImagePath != null){
			return 	sImagePath + "shadow.png";
		}else{
			return 	"/images/shadow.png";
		}
	}
	
	public static String getClosedTruckMarker (ServletContext context){

		String sImagePath = WebContextParameters.getInitImagePath(context);

		if (sImagePath != null){
			return 	sImagePath + "closedtruck.png";
		}else{
			return 	"/images/closedtruck.png";
		}
	}
	
	public static String getTruckMarkerShadow (ServletContext context){

		String sImagePath = WebContextParameters.getInitImagePath(context);

		if (sImagePath != null){
			return 	sImagePath + "shadow-truck.png";
		}else{
			return 	"/images/shadow-truck.png";
		}
	}

	public static String setMobileButtonStyle(){
		String s = "";

		//Set the colors:
		s += "<style type=text/css>"
			//s += "<style>"
			+ "body { background-color : white; color: black; font-family : Arial; text-decoration : none; font-weight: 900;}"

			//Hyperlink colors:
			//+ "a {font-family : Arial; text-decoration : none; font-weight: 900}\n"
			//+ "a:link {color : #99FFFF}\n"
			//+ "a:visited {color : #99FFFF}\n"
			//+ "a:active {color : #99FFFF}\n"
			//+ "a:hover {color : white}\n"
			+ "</style>\n"

			+ "<style type=text/css>\n"
			//Button style:
			+ "input.buttonstyle {width: 100%; height: 50px; color: #99FFFF; background-color: black; font-weight: 900;}\n"
			//Input text style:
			+ "input.text {height: 30px; color: black; background-color: white; font-weight: 400;}\n"
			+ "</style>\n"
			;

		return s;
	}

	public static String layoutMobileMenu(){
		String s = "";
		String sBorderSize = "2";
		//String sFontSize = "3";
		s += "<style type=\"text/css\">\n";

		//Set hyperlink style:
		s += "a {font-family : Arial; text-decoration : none; font-weight: 900}\n";
		s += "a:link {color : #99FFFF}\n";
		s += "a:visited {color : #99FFFF}\n";
		s += "a:active {color : #99FFFF}\n";
		s += "a:hover {color : white}\n";

		//Layout table:
		s +=
			"table.main {"
			+ "border-width: " + sBorderSize + "px; "
			+ "border-spacing: 2px; "
			+ "border-style: outset; "
			+ "border-style: none; "
			+ "border-color: white; "
			+ "border-collapse: separate; "
			+ "width: 100%; "
			//+ "font-size: " + sFontSize + "; "
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
			+ "background-color: #99FFFF; "
			+ "color: black; "
			+ "font-family : Arial; "
			+ "vertical-align: text-middle; "
			+ "height: 50px; "
			+ "}"
			+ "\n"
			;

		//s +=
		//	"tr.d0 td {"
		//	+ "background-color: " + BACKGROUND_WHITE + "; "
		//	+"}"
		//	;
		//s +=
		//	"tr.d1 td {"
		//	+ "background-color: #EEEEEE; "
		//	+ "}"
		//	+ "\n"
		//	;

		s +=
			"table.main td {"
			+ "border-width: " + sBorderSize + "px; "
			+ "padding: 2px; "
			+ "border-style: none; "
			+ "border-color: white; "
			+ "vertical-align: text-middle;"
			+ "background-color: black; "
			+ "text-align: center; "
			+ "color: #99FFFF; "
			+ "height: 50px; "
			+ "}"
			+ "\n"
			;

		s += "</style>"
			+ "\n"
			;

		return s;
	}
	
	public static void buildMobileMenuTable(
			String sTableTitle,
			ArrayList<Long> arPermittedFunctions,
			ArrayList<String> arPermittedFunctionNames,
			ArrayList<String> arPermittedFunctionLinks,
			ArrayList<Long> arRequestedFunctions,
			String sDBID,
			PrintWriter pwOut,
			ServletContext context
	){
		//Mobile view:
		pwOut.println("<table class=\"main\">");
		if (sTableTitle.compareToIgnoreCase("") != 0){
			pwOut.println("<tr><th>" + sTableTitle + "</th></tr>");
		}
		for (int i= 0;i < arRequestedFunctions.size(); i++){
			for (int j= 0;j < arPermittedFunctions.size(); j++){
				// If one of the requested functions is also one of the permitted functions, then we load it into
				// the array to be displayed:
				if(arRequestedFunctions.get(i).compareTo(arPermittedFunctions.get(j)) == 0){
					//Create the link to be added:
					String sLink = 
						"<a "
						+ "HREF='" + WebContextParameters.getURLLinkBase(context)
						+ arPermittedFunctionLinks.get(j)
						;

					//If the link already has a parameter in it (indicated by a '?', then add an
					//ampersand to it:
					if (arPermittedFunctionLinks.get(j).contains("?")){
						sLink += "&";
						//If not, then add a question mark to it:
					}else{
						sLink += "?"; 
					}
					sLink +=
						SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "' NAME='"
						+ arPermittedFunctionNames.get(j).replace(" ", "") + "Link"
						+ "'>"
						+ arPermittedFunctionNames.get(j)
						+ " (" + Long.toString(arPermittedFunctions.get(j)) + ")"
						+ "</a>"
						;
					pwOut.println("<tr><td>" + sLink + "</td></tr>");
				}
			}
		}
		//Close the table:
		pwOut.println("<table class=\"main\">");
	}

	public static void buildMenuTable(
			String sTableTitle,
			ArrayList<Long> arPermittedFunctions,
			ArrayList<String> arPermittedFunctionNames,
			ArrayList<String> arPermittedFunctionLinks,
			ArrayList<Long> arRequestedFunctions,
			String sDBID,
			PrintWriter pwOut,
			ServletContext context
	){
		pwOut.println("<TABLE BORDER=0 WIDTH=100%>");

		ArrayList<String> arMenu = new ArrayList<String>(0);

		for (int i= 0;i < arRequestedFunctions.size(); i++){

			for (int j= 0;j < arPermittedFunctions.size(); j++){
				// If one of the requested functions is also one of the permitted functions, then we load it into
				// the array to be displayed:
				if(arRequestedFunctions.get(i).compareTo(arPermittedFunctions.get(j)) == 0){
					//Add it to the menu to display:

					//Create the link to be added:
					String sLink = 
						"<A HREF='" + WebContextParameters.getURLLinkBase(context)
						+ arPermittedFunctionLinks.get(j)
						;

					//If the link already has a parameter in it (indicated by a '?', then add an
					//ampersand to it:
					if (arPermittedFunctionLinks.get(j).contains("?")){
						sLink += "&";
						//If not, then add a question mark to it:
					}else{
						sLink += "?"; 
					}
					sLink +=
						SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "' NAME='"
						+ arPermittedFunctionNames.get(j).replace(" ", "") + "Link"
						+ "'><U><FONT SIZE=3 STYLE='font-size: 11pt'><FONT COLOR='#000080'>"
						+ arPermittedFunctionNames.get(j)
						+ " (" + Long.toString(arPermittedFunctions.get(j)) + ")"
						+ "</FONT></FONT></U></A><BR>"
						;
					arMenu.add(sLink);
				}
			}
		}

		//If any functions were permitted, then build the table:
		if(arMenu.size() > 0){
			pwOut.println("<B>" + sTableTitle + "</B>");
			pwOut.println(SMUtilities.Build_HTML_Table(4, arMenu,1,true));
			pwOut.println("<BR>");
		}
	}

	public static String lnViewInvoice(String sDBID, String sInvoiceNumber){

		//We can use this as a default link and change it in one place later if we need to:
		String sLink = "smcontrolpanel.SMPrintInvoice?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "&InvoiceNumberFrom=" + sInvoiceNumber;

		/* Parameters, if needed later:
		 * InvoiceNumber=123456
		 * ShowExtendedPriceForEachItem=1
		 * ShowLaborAndMaterialSubtotals=1
		 * ShowALLItemsOnInvoiceIncludingDNP=1
		 */

		return sLink;
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList<clsURLRecord> updateURLHistory(String sTitle, String sAddress, Object objList, Object objMaxSize){

		ArrayList<clsURLRecord> alHistory = new ArrayList<clsURLRecord>(0);
		int iURLMaxSize = 20;

		if (objList != null){
			try{
				alHistory = (ArrayList<clsURLRecord>) objList;
				//System.out.println("Casting arrayList succeeded.");

			}catch(Exception ex){
				//if casting fail, use an empty arraylist
				//System.out.println("Casting arrayList failed.");
				//System.out.println("Exception: " + ex.getMessage());
			}
		}

		if (objMaxSize != null){
			try{
				//System.out.println("objMaxSize.toString() = " + objMaxSize.toString());
				iURLMaxSize = Integer.parseInt(objMaxSize.toString());
				//System.out.println("Casting integer succeeded.");
			}catch(Exception ex){
				//if casting fail, use 0
				//System.out.println("Casting integer failed.");
				//System.out.println("Exception: " + ex.getMessage());

			}
		}
		//System.out.println("alHistory.size() = " + alHistory.size());
		//System.out.println("iURLMaxSize = " + iURLMaxSize);

		//check to see if this new URL is already in the list
		for (int i=0;i<alHistory.size();i++){
			try{
				//if (((URLRecord)alHistory.get(i)).Address().compareToIgnoreCase(sAddress) == 0){
				////this URL exists already, move it to the top of the list.
				//URLRecord u = (URLRecord)alHistory.get(i);
				//for (int j=i;j>0;j--){
				//	alHistory.set(j, alHistory.get(j-1));
				//}
				//alHistory.set(0, u);
				//return alHistory;
				//}	
				//TJR - 7/6/10 - changed this because sometimes the URL was the same, but the parameters
				//were in a different order, so it didn't look like a match.  We'll try using the titles
				//to see if that works better at finding a match:
				if (((clsURLRecord)alHistory.get(i)).Title().compareToIgnoreCase(sTitle) == 0){
					//Remove this item from the list:
					alHistory.remove(i);
				}
			}catch(ClassCastException ex){
				//System.out.println("Failed to cast URL, possibly means there is no URL in the list yet.");
				break;
			}
		} 
		//System.out.println("alHistory.size() == " + alHistory.size());
		if (alHistory.size() < iURLMaxSize){
			//increase array size by one
			alHistory.add(new clsURLRecord());
		}
		for (int i=alHistory.size()-1;i>0;i--){
			alHistory.set(i, alHistory.get(i-1));
		}
		//move old history one position down
		alHistory.set(0, new clsURLRecord(sTitle, sAddress));

		//System.out.println("Finished updating URL history.");
		return alHistory;
	}
	
	public static void addURLToHistory(String sTitle, HttpSession session, HttpServletRequest request) throws Exception{
		String sCurrentCompleteURL = clsServletUtilities.URLEncode(request.getRequestURI().toString() 
				+ clsManageRequestParameters.getQueryStringFromPost(request));
		
		try {
			session.setAttribute(SMUtilities.SMCP_SESSION_PARAM_URLHISTORY,
					SMUtilities.updateURLHistory(sTitle, 
							sCurrentCompleteURL.replace("&", "*"), 
							session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_URLHISTORY),
							session.getAttribute("URLMaxSize"))
			);
		} catch (Exception e) {
			throw new Exception("Could not update URL history [1385408769] - " + e.getMessage());
		}
	}
	
	public static boolean getICImportFlag(String sDBID, ServletContext context){

		boolean bResult = false;
		Connection conn = clsDatabaseFunctions.getConnection(
				context, sDBID, "MySQL", "SMUtilities.getICImportFlag");
		if (conn == null){
			clsServletUtilities.sysprint("SMUtilities.getICImportFlag", "SYSTEM", "Error getting connection");
			return false;
		}
		String SQL = "SELECT"
			+ " " + SMTableicoptions.iflagimports
			+ " FROM " + SMTableicoptions.TableName
			;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				if (rs.getLong(SMTableicoptions.iflagimports) == 1) {
					bResult = true;
				}
			}
			rs.close();
		} catch (SQLException e) {
			clsServletUtilities.sysprint("SMUtilities.getICImportFlag", "SYSTEM", "Error reading IC import flag - " + e.getMessage());
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080677]");
		return bResult;
	}
	
	public static boolean getICImportFlag(Connection conn){

		boolean bResult = false;
		String SQL = "SELECT"
			+ " " + SMTableicoptions.iflagimports
			+ " FROM " + SMTableicoptions.TableName
			;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				if (rs.getLong(SMTableicoptions.iflagimports) == 1) {
					bResult = true;
				}
			}
			rs.close();
		} catch (SQLException e) {
			clsServletUtilities.sysprint("SMUtilities.getICImportFlag", "SYSTEM", "Error reading IC import flag - " + e.getMessage());
		}
		return bResult;
	}
	
	public static void checkSMCPPassword(String sPW) throws Exception {
		sPW = sPW.trim();
		//Length must be at least 7 characters:
		if (sPW.length() < 8){
			throw new Exception ("Password must be at least 8 characters");
		}
		boolean bContainsDigits = false;
		boolean bContainsLowerCase = false;
		boolean bContainsUpperCase = false;
        for (int i = 0; i < sPW.length(); i++) {
            if (Character.isDigit(sPW.charAt(i))){bContainsDigits = true;}
            if (Character.isLowerCase(sPW.charAt(i))){bContainsLowerCase = true;}
            if (Character.isUpperCase(sPW.charAt(i))){bContainsUpperCase = true;}
        }
        if (!bContainsDigits){
        	throw new Exception ("Password must contain digits");
        }
        if (!bContainsLowerCase){
        	throw new Exception ("Password must contain lower case letters");
        }
        if (!bContainsUpperCase){
        	throw new Exception ("Password must contain upper case letters");
        }
	}
	
	public static String getSMCP_CSSIncludeString (ServletContext context){
		String SMCP_CSS_SCRIPT_NAME = "smcp.css";
		String sScriptPath = WebContextParameters.getInitScriptPath(context);

		if (sScriptPath != null){
			return "<script type='text/JavaScript' src='" + sScriptPath + SMCP_CSS_SCRIPT_NAME + "'></script>";
		}else{
			return "<script type='text/JavaScript' src='../javascript/" + SMCP_CSS_SCRIPT_NAME + "'></script>";
		}
	}
	
	public static String getDatabaseServerURL(HttpServletRequest request, HttpSession session, ServletContext context) throws Exception{
		
		String sDatabaseServer = "";
		String sDatabaseID;
		try {
			sDatabaseID = (String) session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		} catch (Exception e1) {
			sDatabaseID = "";
		}
		if (sDatabaseID == null){
			sDatabaseID = "";
		}
		if (sDatabaseID.compareToIgnoreCase("") == 0){
			sDatabaseID = clsManageRequestParameters.get_Request_Parameter(SMCP_REQUEST_PARAM_DATABASE_ID, request);
		}
		
		ServerSettingsFileParameters serverSettingsFile = new ServerSettingsFileParameters(ServerSettingsFileParameters.getFullPathToResourceFolder(context) + ServerSettingsFileParameters.SERVER_SETTINGS_FILENAME);
		//If there is no control database name in the server settings file, then just use the database URL context parameter:
		if (serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_NAME).compareToIgnoreCase("") == 0){
			sDatabaseServer = serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_URL);
		//Otherwise, go and read the database from the credentials table:
		}else{
			CompanyDataCredentials cdc = new CompanyDataCredentials();
			try {
				cdc.load(
					sDatabaseID, 
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_URL), 
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_PORT),
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_NAME), 
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_USERNAME),
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_PASSWORD)
				);
				sDatabaseServer = cdc.get_databaseurl();
			} catch (Exception e) {
				throw new Exception("Could not read control database - " + e.getMessage());
			}
		}
		return sDatabaseServer;
	}

	public static String getGDriveFolderID (String URL) throws Exception{
		
		String sIDString = "id=";
		String sFolderString = "folders/";
		String sDString = "d/";
		String sAmpersand = "&";
		String sBackSlash = "/";
		String sFolderID = "";
		String sFolderIDStart = "";
		int indexStart = 0;
		int indexEnd = 0;
		
		if (URL.trim().compareToIgnoreCase("") == 0){
			return "";
		}
		
		//Check for beginning of ID
		if (URL.indexOf(sIDString) != -1){	
			indexStart = URL.indexOf(sIDString) + sIDString.length();
		}
		else if (URL.indexOf(sFolderString) != -1){	
			indexStart = URL.indexOf(sFolderString) + sFolderString.length();
		}
		else if (URL.indexOf(sDString) != -1){
			indexStart = URL.indexOf(sDString) + sDString.length();
		}
		else{
			throw new Exception("Error [1443721861] - No Folder ID Found in URL '" + URL + "'.");
		}
		sFolderIDStart = URL.substring(indexStart);
		
		//Check for End of ID
		if (sFolderIDStart.indexOf(sAmpersand) != -1){
			indexEnd = sFolderIDStart.indexOf(sAmpersand);
		}
		else if (sFolderIDStart.indexOf(sBackSlash) != -1){
			indexEnd = sFolderIDStart.indexOf(sBackSlash);
		}
		else{
			indexEnd =sFolderIDStart.length(); 
		}
		sFolderID = sFolderIDStart.substring(0,indexEnd);
		return sFolderID;
	}
	
	public static String getCreateGDriveReturnURL(HttpServletRequest req, ServletContext context) throws Exception{
		String sTemp = "";
		try {
			sTemp = clsServletUtilities.getServerURL(req, context);
		}catch(Exception e) {
			throw new Exception("Error [1542748060] "+e.getMessage());
		}
		sTemp += "/" + WebContextParameters.getInitWebAppName(context) + "/";
		sTemp += "smcontrolpanel.SMCreateGDriveFolder";
		return sTemp;
	}
	
	public static String getUserFullName(String sUser, ServletContext context, String sDBID, String sCallingFunction) {
		String sFullName = "(NOT FOUND)";
		String SQL = "SELECT"
			+ " " + SMTableusers.sUserFirstName
			+ ", " + SMTableusers.sUserLastName
			+ " FROM " + SMTableusers.TableName
			+ " WHERE ("
				+ "(" + SMTableusers.sUserName + " = '" + sUser + "')"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sDBID, 
				"MySQL",
				sCallingFunction
				);
			if (rs.first()){
				sFullName = rs.getString(SMTableusers.sUserFirstName).trim() + " " + rs.getString(SMTableusers.sUserLastName).trim();
			}
			rs.close();
		} catch (SQLException e) {
			//Don't do anything, just let the function return the 'not found' string...
		}
		
		return sFullName;
		
	}
	
	public static String getUserFullName(String sUser, Connection conn) {
		String sFullName = "(NOT FOUND)";
		String SQL = "SELECT"
			+ " " + SMTableusers.sUserFirstName
			+ ", " + SMTableusers.sUserLastName
			+ " FROM " + SMTableusers.TableName
			+ " WHERE ("
				+ "(" + SMTableusers.sUserName + " = '" + sUser + "')"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.first()){
				sFullName = rs.getString(SMTableusers.sUserFirstName).trim() + " " + rs.getString(SMTableusers.sUserLastName).trim();
			}
			rs.close();
		} catch (SQLException e) {
			//Don't do anything, just let the function return the 'not found' string...
		}
		
		return sFullName;
		
	}
	
	public static String getFullNamebyUserName(String sUser, Connection conn) {
		String sFullName = "(NOT FOUND)";
		String SQL = "SELECT"
			+ " " + SMTableusers.sUserFirstName
			+ ", " + SMTableusers.sUserLastName
			+ " FROM " + SMTableusers.TableName
			+ " WHERE ("
				+ "(" + SMTableusers.sUserName + " = '" + sUser + "')"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.first()){
				sFullName = rs.getString(SMTableusers.sUserFirstName).trim() + " " + rs.getString(SMTableusers.sUserLastName).trim();
			}
			rs.close();
		} catch (SQLException e) {
			//Don't do anything, just let the function return the 'not found' string...
		}
		
		return sFullName;
		
	}
	
	public static String getFullNamebyUserID(String sUserID, ServletContext context, String sDBID, String sCallingFunction) {
		String sFullName = "(NOT FOUND)";
		String SQL = "SELECT"
			+ " " + SMTableusers.sUserFirstName
			+ ", " + SMTableusers.sUserLastName
			+ " FROM " + SMTableusers.TableName
			+ " WHERE ("
				+ "(" + SMTableusers.lid + " = " + sUserID + ")"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context,
					sDBID, 
					"MySQL",
					sCallingFunction
					);
			if (rs.first()){
				sFullName = rs.getString(SMTableusers.sUserFirstName).trim() + " " + rs.getString(SMTableusers.sUserLastName).trim();
			}
			rs.close();
		} catch (SQLException e) {
			//Don't do anything, just let the function return the 'not found' string...
		}	
		return sFullName;	
	}
	
	public static String getFullNamebyUserID(String sUserID, Connection conn) {
		String sFullName = "(NOT FOUND)";
		String SQL = "SELECT"
			+ " " + SMTableusers.sUserFirstName
			+ ", " + SMTableusers.sUserLastName
			+ " FROM " + SMTableusers.TableName
			+ " WHERE ("
				+ "(" + SMTableusers.lid + " = " + sUserID + ")"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.first()){
				sFullName = rs.getString(SMTableusers.sUserFirstName).trim() + " " + rs.getString(SMTableusers.sUserLastName).trim();
			}
			rs.close();
		} catch (SQLException e) {
			//Don't do anything, just let the function return the 'not found' string...
		}
		
		return sFullName;
		
	}
	
	public static String getUserIDbyUserName(String sUserName, ServletContext context, String sDatabaseID) {
		String sUserID = "";
		String SQL = "SELECT"
			+ " " + SMTableusers.lid
			+ " FROM " + SMTableusers.TableName
			+ " WHERE ("
				+ "(" + SMTableusers.sUserName + " = '" + sUserName + "')"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, context, sDatabaseID, "MySQL"
					, "SMUtilities" + ".getUserIDbyUserName = UserName: " + sUserName + " [1346736997]");
			if (rs.first()){
				sUserID = Integer.toString(rs.getInt(SMTableusers.lid));
			}
			rs.close();
		} catch (SQLException e) {
			//Don't do anything, just let the function return the empty string...
		}
		
		return sUserID;
		
	}
	
	public static String getUserIDbyUserName(String sUserName, Connection conn) {
		String sUserID = "";
		String SQL = "SELECT"
			+ " " + SMTableusers.lid
			+ " FROM " + SMTableusers.TableName
			+ " WHERE ("
				+ "(" + SMTableusers.sUserName + " = '" + sUserName + "')"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.first()){
				sUserID = Integer.toString(rs.getInt(SMTableusers.lid));
			}
			rs.close();
		} catch (SQLException e) {
			//Don't do anything, just let the function return the empty string...
		}
		
		return sUserID;
		
	}
	
	public static String getUserNamebyUserID(String sUserID, Connection conn) {
		String sUserName = "";
		String SQL = "SELECT"
			+ " " + SMTableusers.sUserName
			+ " FROM " + SMTableusers.TableName
			+ " WHERE ("
				+ "(" + SMTableusers.lid + " = " + sUserID + ")"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.first()){
				sUserName = rs.getString(SMTableusers.sUserName);
			}
			rs.close();
		} catch (SQLException e) {
			//Don't do anything, just let the function return the empty string...
		}
		
		return sUserName;
		
	}
	public static String getSMCPModuleLevel(ServletContext context, String DBID) throws Exception{
		
		//example: getAbsoluteRootPath = '/opt/apache-tomcat-8.0.20/webapps/sm/';
		//String sFilePath = getAbsoluteRootPath(req, context);
		String sFilePath = clsServletUtilities.getRootPath(context);
		//Pick off the last file separator, if there is one:
		if (sFilePath.compareToIgnoreCase("") != 0){
			if (sFilePath.substring(sFilePath.length() - 1, sFilePath.length()).compareToIgnoreCase(System.getProperty("file.separator")) == 0){
				sFilePath = sFilePath.substring(0, sFilePath.length() - 1);
			}
			//System.out.println("[1466801033] - sFilePath = '" + sFilePath + "'");
			
			//Pick off the last folder, normally 'sm':
			String sWebAppName = WebContextParameters.getInitWebAppName(context);
			sFilePath = sFilePath.substring(0, sFilePath.length() - sWebAppName.length());
		}
		sFilePath += 
			SMCP_LICENSE_DIRECTORY + System.getProperty("file.separator") 
			+ SMCP_LICENSE_SUBDIRECTORY + System.getProperty("file.separator")
			+ SMCP_LICENSE_FILE
		;
		//System.out.println("[1466801032] - sFilePath = '" + sFilePath + "'");	
		//Read the file:
		String sModuleLevel = "";
		try {
			File file = new File(sFilePath);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				//Read and decrypt each line:
				String sDecryptedLine = decryptLicenseLine(line);
				String sDecryptedLicenseLine[] = sDecryptedLine.split(LICENSE_KEY_DELIMITER);
				
				//System.out.println("[1551824959] - DBID = '" + DBID + "'");
				//System.out.println("[1551824960] - sDecryptedLine = '" + sDecryptedLine + "'");
				if (sDecryptedLicenseLine[0].compareToIgnoreCase(DBID) == 0){
					sModuleLevel = sDecryptedLicenseLine[1].trim();
					break;
				}
			}
			bufferedReader.close();
		} catch (Exception e) {
			throw new Exception("Error [1466801031] reading config file '" + sFilePath + "' - " + e.getMessage());
		}

		//Make sure it's a valid long integer:
		try{
			@SuppressWarnings("unused")
			long lModuleLevel = Long.parseLong(sModuleLevel);
		} catch (Exception e){
			throw new Exception ("Error [1467151117] - invalid module level (" + sModuleLevel + ") read from license file.");
		}
		return sModuleLevel;
	}

	public static String getSMCPLicenseExpirationDate(ServletContext context, String DBID){
		
		//System.out.println("[1551823793] checking expiry date");
		
		//example: getAbsoluteRootPath = '/opt/apache-tomcat-8.0.20/webapps/sm/';
		//String sFilePath = getAbsoluteRootPath(req, context);
		String sFilePath = clsServletUtilities.getRootPath(context);
		//Pick off the last file separator, if there is one:
		if (sFilePath.compareToIgnoreCase("") != 0){
			if (sFilePath.substring(sFilePath.length() - 1, sFilePath.length()).compareToIgnoreCase(System.getProperty("file.separator")) == 0){
				sFilePath = sFilePath.substring(0, sFilePath.length() - 1);
			}
			//System.out.println("[1466801033] - sFilePath = '" + sFilePath + "'");
			
			//Pick off the last folder, normally 'sm':
			String sWebAppName = WebContextParameters.getInitWebAppName(context);
			sFilePath = sFilePath.substring(0, sFilePath.length() - sWebAppName.length());
		}
		sFilePath += 
			SMCP_LICENSE_DIRECTORY + System.getProperty("file.separator") 
			+ SMCP_LICENSE_SUBDIRECTORY + System.getProperty("file.separator")
			+ SMCP_LICENSE_FILE
		;
		//System.out.println("[1466801032] - sFilePath = '" + sFilePath + "'");	
		//Read the file:
		String sExpirationDate = SMUtilities.EMPTY_SQL_DATE_VALUE;
		try {
			File file = new File(sFilePath);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				//System.out.println("[1551823795] inside while loop");
				//Read and decrypt each line:
				String sDecryptedLine = decryptLicenseLine(line);
				String sDecryptedLicenseLine[] = sDecryptedLine.split(LICENSE_KEY_DELIMITER);
				if (sDecryptedLicenseLine[0].compareToIgnoreCase(DBID) == 0){
					//System.out.println("[1551823791] sDecryptedLicenseLine.length = " + sDecryptedLicenseLine.length);
					if (sDecryptedLicenseLine.length > 2){
						sExpirationDate = sDecryptedLicenseLine[2].trim();
					}
					break;
				}
			}
			bufferedReader.close();
		} catch (Exception e) {
			return sExpirationDate;
		}

		//Make sure it's a valid date:
			if(!ServletUtilities.clsDateAndTimeConversions.IsValidDateString(SMUtilities.DATE_FORMAT_FOR_SQL, sExpirationDate)){
				return SMUtilities.EMPTY_SQL_DATE_VALUE;
			}
		return sExpirationDate;
	}
	
	public static String getLicenseEncryptionKey(){
		String s = "";
		s = sFirstPart + sSecondPart;
		return s;
	}

	public static String encryptLicenseLine(String sCompanyID, String sModuleLevel, String sExpirationDate) throws Exception {
		String sInputLine = sCompanyID + "," + sModuleLevel + "," + sExpirationDate;
		String sOutPutLine = "";
		for (int i = 0; i < sInputLine.length(); i++){
			int iAsciiValue = (int) sInputLine.charAt(i);
			iAsciiValue += sInputLine.length(); 
			sOutPutLine += clsStringFunctions.PadLeft(Integer.toString(iAsciiValue), "0", SMCP_LICENSE_PAD_LENGTH);
		}
		return sOutPutLine;
	}
	public static String decryptLicenseLine (String sLine) throws Exception {
		String sDecryptedLine = "";
		int iEncryptionOffset = sLine.length() / SMUtilities.SMCP_LICENSE_PAD_LENGTH;
		for (int i = 0; i < sLine.length() / SMUtilities.SMCP_LICENSE_PAD_LENGTH; i++){
			String sAsciiDigit = sLine.substring(
				i * SMUtilities.SMCP_LICENSE_PAD_LENGTH, 
				i * SMUtilities.SMCP_LICENSE_PAD_LENGTH + SMUtilities.SMCP_LICENSE_PAD_LENGTH);
			int iAsciiDigit = Integer.parseInt(sAsciiDigit);
			//Subtract back out the 'encryption offset':
			iAsciiDigit -= iEncryptionOffset;
			sDecryptedLine += Character.toString ((char) iAsciiDigit);
		}
		return sDecryptedLine;
	}
	
	public static String getFullPathToResourceFolder(ServletContext context) throws Exception{
		
			//Get catalina base
			String sFullFilePath = System.getProperty( "catalina.base" );
				
			//Add webapps folder to the path
			sFullFilePath += System.getProperty("file.separator") + "webapps";
				
			//Add local resources folder to the path
			sFullFilePath = sFullFilePath + System.getProperty("file.separator");

			if (WebContextParameters.getLocalResourcesPath(context).startsWith(System.getProperty("file.separator"))){
				sFullFilePath += WebContextParameters.getLocalResourcesPath(context).substring(1);
			}else{
				sFullFilePath += WebContextParameters.getLocalResourcesPath(context);
			}
			//End with file separator
			while (sFullFilePath.endsWith(System.getProperty("file.separator"))){
				sFullFilePath = sFullFilePath.substring(0, sFullFilePath.length() - 1);
			}
			sFullFilePath += System.getProperty("file.separator");
				
			//return full path to resource folder (e.g. '/var/lib/tomcat7/webapps/smlocalresources/')
			return sFullFilePath;
	}

	public static String getDatabaseRevisionNumber(ServletContext context, String sDBID, String sCallingClass, String sUserID, String sUserFullName){
		String s = "";
		String SQL = "SELECT"
			+ " " + SMTablecompanyprofile.iDatabaseVersion
			+ " FROM " + SMTablecompanyprofile.TableName
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context, 
				sDBID, 
				"MySQL", 
				sCallingClass + " - user: " + sUserID
				+ " - " + sUserFullName);
			if (rs.next()){
				s += rs.getString(SMTablecompanyprofile.iDatabaseVersion);
			}else{
				s += "(N/A)";
			}
			rs.close();
		} catch (SQLException e) {
			s += "(N/A)";
		}
		
		return s;
	}
	public static String getAbsoluteSMTempPath(HttpServletRequest req, ServletContext context){

		String sPath = "";
		
		if ((WebContextParameters.getInitWebAppName(context)) != null){
			sPath = System.getProperty( "catalina.base" ) 
					+ System.getProperty("file.separator")  + "webapps" 
					+ WebContextParameters.getsmtempfolder(context);
		}else{
			sPath = System.getProperty( "catalina.base" ) + System.getProperty("file.separator") + "webapps";
		}

		//Strip off any file separators we don't need:
		while (
				sPath.endsWith(System.getProperty("file.separator"))
				|| sPath.endsWith(".")
		){
			sPath = sPath.substring(0, sPath.length() - 1);
		}

		//Now add back one file separator:
		sPath = sPath + System.getProperty("file.separator");

		return sPath;
	}
}