package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTablelabelprinters;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ICPrintUPCSelection extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final int NUMBER_OF_ROWS_TO_DISPLAY = 50;
	private static final int NUMBER_OF_COLUMNS_TO_DISPLAY = 2;
	private static String sCalledClassName = "ICPrintUPCAction";

	public static final String BUTTON_PRINT_LABELS = "Printlabels";
	public static final String BUTTON_PRINT_LABELS_LABEL = "Print labels";
	
	public static final String BUTTON_POPULATE_ITEMS = "PopulateItems";
	public static final String BUTTON_POPULATE_ITEMS_LABEL = "Populate items using the starting and ending range";
	
	public static final String PARAM_STARTINGITEM = "STARTINGITEM";
	public static final String PARAM_ENDINGITEM = "ENDINGITEM";
	
	//LTO 20131231
	//This determines when the "too many labels" message will show before printing.
	public static final int MAX_NUMBER_OF_LABELS = NUMBER_OF_ROWS_TO_DISPLAY * NUMBER_OF_COLUMNS_TO_DISPLAY * 3;
	private static String sCompanyName = "";
	private static String sDBID = "";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request,	
				response,
				getServletContext(),
				SMSystemFunctions.ICPrintUPCLabels
			)
		){
			return;
		}
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    
	    String sScript = "";
		
		sScript += "<NOSCRIPT>\n"
			+ "    <font color=red>\n"
			+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "    </font>\n"
			+ "</NOSCRIPT>\n"
		;
		sScript += "<script type=\"text/javascript\">\n";
		
		sScript += "function calculateLabelTotal() {\n";
		//sScript += "   	alert('Checking label totals.');\n";
		sScript += "   	var iTotalLabelCount = 0;\n";
		sScript += "   	for (var i=1;i<=" + Integer.toString(NUMBER_OF_ROWS_TO_DISPLAY * NUMBER_OF_COLUMNS_TO_DISPLAY) + ";i++){\n";
		sScript += "    	if (document.forms[\"MAINFORM\"].elements[\"" + ICPrintUPCAction.PARAM_ITEMNUMMARKER + "\" + i].value != ''){\n";
		sScript += "    		iTotalLabelCount += document.forms[\"MAINFORM\"].elements[\"" + ICPrintUPCAction.PARAM_QTYMARKER + "\" + i].value *\n";
		sScript += "    							document.forms[\"MAINFORM\"].elements[\"" + ICPrintUPCAction.PARAM_NUMPIECESMARKER +  "\" + i].value;\n";
		sScript += "    	}\n";
		sScript += "    }\n";
		sScript += "    if (iTotalLabelCount > " + MAX_NUMBER_OF_LABELS + "){\n";
		sScript += "    	if (!confirm('WARNING: You are trying to print ' + iTotalLabelCount + ' labels. Are you sure you want to continue?')){\n";
		sScript += "       	 	return;\n";
		sScript += "    	}\n";
		sScript += "    }\n";
		sScript += "    //Print labels;\n";
		//sScript += "    alert('printing labels....');\n";
		sScript += "    document.forms[\"MAINFORM\"].submit();\n";
		sScript += "    return;\n";
		sScript += "}\n";
		sScript += "</script>\n";
	    
		out.println(sScript);
		
	    String title = "Print UPC Barcode Labels.";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
	    if (sWarning.compareToIgnoreCase("") != 0){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		//If there is a status from trying to input previously, print it here:
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (sStatus.compareToIgnoreCase("") != 0){
			out.println("<B>" + sStatus + "</B><BR>");
		}

	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICPrintUPCLabels) 
	    		+ "\">Summary</A><BR>");
	    
	    //If this is a re-submit, asking to populate the item numbers, do that now:
	    ArrayList<String>arrItemNumbersToPrint = new ArrayList<String>(0);
	    if (clsManageRequestParameters.get_Request_Parameter(BUTTON_POPULATE_ITEMS, request).compareToIgnoreCase("") != 0){
	    	try {
				arrItemNumbersToPrint = populateItemNumbers(request, sDBID);
			} catch (Exception e) {
				out.println("<BR><FONT COLOR=RED><B>WARNING: "
					+ e.getMessage()
					+ "</B></FONT>"
					+ "<BR>"
				);
			}
	    }
	    
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) 
	    		+ "smic." + sCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + ICPrintUPCAction.PARAM_NUMBEROFDIFFERENTLABELS 
	    	+ "' VALUE='" + Integer.toString(NUMBER_OF_ROWS_TO_DISPLAY * NUMBER_OF_COLUMNS_TO_DISPLAY) + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" + "ICPrintUPCSelection" + "\">");

	    out.println("<BR>" + "\n");
	    out.println("<TABLE BORDER=1>" + "\n");
	    out.println("  <TR>" + "\n");
	    out.println("    <TD COLSPAN=2>Populate the item numbers using a starting and ending range of items:</TD>" + "\n");
	    out.println("  </TR>" + "\n");
	    
	    out.println("  <TR>" + "\n");
	    out.println("    <TD>"
	    	+ "Starting item number: " 
	    	+ "<INPUT TYPE=TEXT NAME=\"" + PARAM_STARTINGITEM 
	    	+ "\" VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(PARAM_STARTINGITEM, request)
	    	+ "\"SIZE=24 MAXLENGTH=24 STYLE="
	    	+ "\"width: 1.2in; height: 0.25in\">"
	    	+ "</TD>" + "\n");
	    
	    out.println("    <TD>"
	    	+ "Ending item number: " 
	    	+ "<INPUT TYPE=TEXT NAME=\"" + PARAM_ENDINGITEM 
	    	+ "\" VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(PARAM_ENDINGITEM, request)
	    	+ "\"SIZE=24 MAXLENGTH=24 STYLE="
	    	+ "\"width: 1.2in; height: 0.25in\">"
	    	+ "</TD>" + "\n");
	    out.println("  </TR>" + "\n");
	    
	    out.println("  <TR>" + "\n");
	    out.println("    <TD COLSPAN=2<BR><input type=\"submit\""
				+ " value=\"" + BUTTON_POPULATE_ITEMS_LABEL + "\""
				+ " name=\"" + BUTTON_POPULATE_ITEMS + "\""
				+ " >" 
				+ "</input>\n"
				+ "</TD>" + "\n"
				);
	    out.println("  </TR>" + "\n");
	    
	    out.println("</TABLE>" + "\n");
	    
	    //TODO LTO 20131219
	    //add java script to this button press to check for label total
	    out.println("<BR><button type=\"button\""
				+ " value=\"" + BUTTON_PRINT_LABELS_LABEL + "\""
				+ " name=\"" + BUTTON_PRINT_LABELS + "\""
				+ " onClick=\"calculateLabelTotal();\">" + BUTTON_PRINT_LABELS_LABEL + "</button>\n"
				);
	    
	    //Add a drop down for the destinations:
	    out.println("Print to: ");
	    out.println("<SELECT NAME = \"" + ICPrintUPCItemLabel.LABELPRINTER_LIST + "\">");
	    out.println("<OPTION selected=yes VALUE=\"" + "0" + "\">" + "Print to screen</OPTION>");
	    String SQL = "SELECT * FROM " + SMTablelabelprinters.TableName + " ORDER BY " + SMTablelabelprinters.sName;
	    try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), sDBID, "MySQL", SMUtilities.getFullClassName(this.toString()));
			while (rs.next()){
				out.println("<OPTION VALUE=\"" + Long.toString(rs.getLong(SMTablelabelprinters.lid)) + "\">" 
					+ rs.getString(SMTablelabelprinters.sName) + " - " 
					+ rs.getString(SMTablelabelprinters.sDescription) + "</OPTION>");
			}
			rs.close();
		} catch (SQLException e) {
			out.println("Error reading label printers data - " + e.getMessage());
			out.println("</BODY></HTML>");
			return;
		}
		out.println("</SELECT>");
	    
	    String sOutPut = "";
	    
	    String sItemNumber = "";
	    String sQuantity = "";
	    String sPieceQuantity = "";
	    int iCounter = 0;
	    
	    out.println("<TABLE BORDER=0>");
	    
	    int iRowCount = NUMBER_OF_ROWS_TO_DISPLAY;
	    int iColCount = NUMBER_OF_COLUMNS_TO_DISPLAY;
	    for (int i = 0;i < iRowCount; i++){
	    	out.println("<TR>");
	    	for (int j = 0; j < iColCount; j++){
	    		iCounter = (iRowCount * j) + i + 1;
	    	    sQuantity = clsManageRequestParameters.get_Request_Parameter(ICPrintUPCAction.PARAM_QTYMARKER 
	    	    		+ Integer.toString(iCounter), request);
	    	    if (sQuantity.compareToIgnoreCase("") == 0){
	    	    	sQuantity = "1";
	    	    }
	    
	    	    out.println("<TD ALIGN=RIGHT>Item " + Integer.toString(iCounter) + " qty:</TD>");
	    	    out.println("<TD><INPUT TYPE=TEXT NAME=\"" + ICPrintUPCAction.PARAM_QTYMARKER 
	    	    		+ Integer.toString(iCounter) + "\" VALUE=\""
	    	    		+ sQuantity + "\"SIZE=4 MAXLENGTH=4 STYLE="
	    	    		+ "\"width: .5in; height: 0.25in\"></TD>");
	    	    
	        	//If the 'populate items' flag was set, then use the populated value for each item number:
	        	if (clsManageRequestParameters.get_Request_Parameter(BUTTON_POPULATE_ITEMS, request).compareToIgnoreCase("") != 0){
	        		if (arrItemNumbersToPrint.size() >=  iCounter){
	        			sItemNumber = arrItemNumbersToPrint.get(iCounter - 1);
	        		}else{
	        			sItemNumber = "";
	        		}
	        	}else{
	        		//Otherwise, get the item number from the query, coming back from the 're-submit':
	        		sItemNumber = clsManageRequestParameters.get_Request_Parameter(ICPrintUPCAction.PARAM_ITEMNUMMARKER 
		        		+ Integer.toString(iCounter), request);
	        	}
	        	
	    	    out.println("<TD ALIGN=RIGHT>Item " + ":</TD>");
	    	    out.println("<TD><INPUT TYPE=TEXT NAME=\"" + ICPrintUPCAction.PARAM_ITEMNUMMARKER 
	    	    		+ Integer.toString(iCounter) + "\" VALUE=\""
	    	    		+ sItemNumber + "\"SIZE=24 MAXLENGTH=24 STYLE="
	    	    		+ "\"width: 1.2in; height: 0.25in\"></TD>");
	    	    /*
	        	sComment = SMUtilities.get_Request_Parameter("Comment" + Integer.toString(iCounter), request);
	    	    out.println("<TD ALIGN=RIGHT>Comment :</TD>");
	    	    out.println("<TD><INPUT TYPE=TEXT NAME=\"Comment" + Integer.toString(iCounter) + "\" VALUE=\""
	    	    		+ sComment + "\"SIZE=15 MAXLENGTH=15 STYLE="
	    	    		+ "\"width: 1.2in; height: 0.25in\"></TD>");
	    	    */
	    	    sPieceQuantity = clsManageRequestParameters.get_Request_Parameter(ICPrintUPCAction.PARAM_NUMPIECESMARKER 
	    	    	+ Integer.toString(iCounter), request);
	    	    if (sPieceQuantity.compareToIgnoreCase("") == 0){
	    	    	sPieceQuantity = "1";
	    	    }
	    	    out.println("<TD ALIGN=RIGHT>Pieces: </TD>");
	    	    out.println("<TD><INPUT TYPE=TEXT NAME=\"" + ICPrintUPCAction.PARAM_NUMPIECESMARKER 
	    	    		+ Integer.toString(iCounter) + "\" VALUE=\""
	    	    		+ sPieceQuantity + "\"SIZE=4 MAXLENGTH=4 STYLE="
	    	    		+ "\"width: .5in; height: 0.25in\"></TD>"
	    	    		+ "<TD>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</TD>"
	    	    );
	    	}
	    	out.println("</TR>");
	    }
	    
	    out.println("</TABLE>");

	    //TODO LTO 20131219
	    //add java script to this button press to check for label total
	    sOutPut += "<BR><button type=\"button\""
				+ " value=\"Print UPC Labels\""
				+ " name=\"SubmitEdit\""
				+ " onClick=\"calculateLabelTotal();\">Print UPC Labels</button>\n";
	    
		//sOutPut += "<INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Print UPC Labels' STYLE='width: 2.00in; height: 0.24in'>";
		sOutPut = sOutPut + "</FORM>";
		out.println(sOutPut);
		
		out.println("</BODY></HTML>");
	}
	
	private ArrayList<String> populateItemNumbers(HttpServletRequest req, String sDBID) throws Exception{
		ArrayList<String>arrItemNumbers = new ArrayList<String>(0);
		String sErrorMessage = "";
		
		String sStartingNumber = clsManageRequestParameters.get_Request_Parameter(PARAM_STARTINGITEM, req).trim();
		String sEndingNumber = clsManageRequestParameters.get_Request_Parameter(PARAM_ENDINGITEM, req).trim();
		
		if (sStartingNumber.compareToIgnoreCase("") == 0){
			if (sErrorMessage.compareToIgnoreCase("") != 0){
				sErrorMessage += "  ";
			}
			sErrorMessage += "  The starting number cannot be blank.";
		}
		
		if (sEndingNumber.compareToIgnoreCase("") == 0){
			if (sErrorMessage.compareToIgnoreCase("") != 0){
				sErrorMessage += "  ";
			}
			sErrorMessage += "The ending number cannot be blank.";
		}

		if (sErrorMessage.compareToIgnoreCase("") != 0){
			throw new Exception("Error [1539290210] - " + sErrorMessage);
		}
		
		//Check that the item numbers are in the right order:
		if (sStartingNumber.compareTo(sEndingNumber) > 0){
			throw new Exception("Error [1539290211] - the starting number '" + sStartingNumber 
				+ "' is HIGHER than the ending number '" + sEndingNumber + " '.");
		}
		
		String SQL = "SELECT"
			+ " " + SMTableicitems.sItemNumber
			+ " FROM " + SMTableicitems.TableName
			+ " WHERE ("
				+ "(" + SMTableicitems.sItemNumber + " >= '" + sStartingNumber + "')"
				+ " AND (" + SMTableicitems.sItemNumber + " <= '" + sEndingNumber + "')"
			+ ") ORDER BY " + SMTableicitems.sItemNumber
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
			while (rs.next()){
				arrItemNumbers.add(rs.getString(SMTableicitems.sItemNumber));
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1539290212] reading item numbers with SQL: '" + SQL + "' - " + e.getMessage() + ".");
		}
		
		//System.out.println("[1539290312] - arrItemNumbers.size() = " + arrItemNumbers.size());
		
		return arrItemNumbers;
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}