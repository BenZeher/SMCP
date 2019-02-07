package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMExportTypes;
import SMDataDefinition.SMTableicoptions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ICEditICOptions extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		String m_sWarning;
		ICOptionInput m_OptionInput;
		PrintWriter m_pwOut;
		HttpServletRequest m_hsrRequest;
		boolean m_bInputLoaded = false;
		
		m_hsrRequest = request;
		//Get the session info:
		HttpSession CurrentSession = m_hsrRequest.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		if (m_hsrRequest.getParameter("OptionInput") != null){
			if (clsManageRequestParameters.get_Request_Parameter("OptionInput", m_hsrRequest).equalsIgnoreCase("True")){
				m_bInputLoaded = true; 
			}else{
				m_bInputLoaded = false;
			}
		}else{
			m_bInputLoaded = false;
		}

		m_sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", m_hsrRequest);

		m_pwOut = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICEditICOptions))
		{
			return;
		}

		//If the class has been passed an AREntryInput query string, just load from that:
		if (m_bInputLoaded){
			m_OptionInput = new ICOptionInput(m_hsrRequest);
		}else{
			//Have to construct the AREntryInput object here:
			m_OptionInput = new ICOptionInput();
			//Load the existing entry:
			ICOption option = new ICOption();

			Connection conn = clsDatabaseFunctions.getConnection(
					getServletContext(), sDBID, "MySQL", "smic.ICEditICOptions");
			if (conn == null){
				m_sWarning = "Could not load icoptions record - connection = null";
				m_pwOut.println(
						"<META http-equiv='Refresh' content='0;URL=" 
						+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu"
						+ "?Warning=" + m_sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "'>"		
						);
				return;
			}	
			if (!option.load(conn)){
				m_sWarning = "Could not load icoptions record - option.load() failed: " + option.getErrorMessage();
				//free the connection
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080826]");
				m_pwOut.println(
						"<META http-equiv='Refresh' content='0;URL=" 
						+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu"
						+ "?Warning=" + m_sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "'>"		
						);
				return;
			}
			if (!m_OptionInput.loadFromICOptionClass(option)){
				m_sWarning = "Could not load IC option input from IC Option record";
				//free the connection
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080827]");
				m_pwOut.println(
						"<META http-equiv='Refresh' content='0;URL=" 
						+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu"
						+ "?Warning=" + m_sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "'>"		
						);
				return;

			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080828]");
		}
		
		String title;
		String subtitle = "";
		title = "Edit Inventory Options";

		m_pwOut.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		//If there is a warning from trying to input previously, print it here:
		if (! m_sWarning.equalsIgnoreCase("")){
			m_pwOut.println("<B><FONT COLOR=\"RED\">WARNING: " + m_sWarning + "</FONT></B><BR>");
		}

		//Print a link to main menu:
		m_pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Main Menu</A><BR>");

		//Print a link to main menu:
		m_pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Main Menu</A><BR>");

		if (!createEntryScreen(m_OptionInput, m_pwOut, sDBID)){
			m_pwOut.println(
					"<META http-equiv='Refresh' content='0;URL=" 
					+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditICOptions"
					+ "?Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "'>"		
			);
			return;
		}

		//End the page:
		m_pwOut.println("</BODY></HTML>");
	}

	/*
	private boolean loadICOptionInput(
			HttpServletRequest m_hsrRequest, 
			ICOptionInput m_OptionInput, 
			boolean m_bInputLoaded, 
			String sDBID) throws Exception{

		//If the class has been passed an AREntryInput query string, just load from that:
		if (m_bInputLoaded){
			m_OptionInput = new ICOptionInput(m_hsrRequest);
		}else{
			//Have to construct the AREntryInput object here:
			m_OptionInput = new ICOptionInput();
			//Load the existing entry:
			ICOption option = new ICOption();

			Connection conn = clsDatabaseFunctions.getConnection(
					getServletContext(), sDBID, "MySQL", "smic.ICEditICOptions");
			if (conn == null){
				throw new Exception("Could not load icoptions record - connection = null");
			}	
			if (!option.load(conn)){
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080826]");
				throw new Exception("Could not load icoptions record - option.load() failed: " + option.getErrorMessage());
			}
			if (!m_OptionInput.loadFromICOptionClass(option)){
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080827]");
				throw new Exception("Could not load IC option input from IC Option record");
				

			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080828]");
		}
		return true;
	}
	*/
	private boolean createEntryScreen(ICOptionInput m_OptionInput, PrintWriter m_pwOut, String sDBID){
		//Start the entry edit form:
		m_pwOut.println("<FORM NAME='ENTRYEDIT' ACTION='" 
				+ SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smic.ICOptionUpdate' METHOD='POST'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "\" VALUE=\"" + sDBID + "\">");

		//Start the table:
		m_pwOut.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">");		

		//Export to:
		m_pwOut.println("<TR><TD>Export to:</TD>");
		m_pwOut.println("<TD><SELECT NAME = \"" + ICOptionInput.Paramiexportto + "\">");

		for (int i = 0; i < SMExportTypes.NUMBER_OF_EXPORT_FORMATS; i++){
			m_pwOut.println("<OPTION");
			if (m_OptionInput.getExportTo().compareToIgnoreCase(
					Long.toString(i)) == 0){
				m_pwOut.println( " selected=yes ");
			}
			m_pwOut.println(" VALUE=\"" + Integer.toString(i) + "\">");
			m_pwOut.println(SMExportTypes.getExportFormatLabel(i));
			m_pwOut.println("</OPTION>");
		}
		m_pwOut.println("</SELECT></TD>");
		m_pwOut.println("<TD>Select the accounting package to which you want to export.</TD></TD>");
		m_pwOut.println("</TR>");


		//Costing method:
		m_pwOut.println("<TR><TD>Costing method:</TD>");
		m_pwOut.println("<TD><SELECT NAME = \"" + ICOptionInput.ParamCostingMethod + "\">");

		//add the first line as a default, so we can tell if they didn't pick one:
		m_pwOut.println("<OPTION");
		m_pwOut.println(" VALUE=\"" + "" + "\">");
		m_pwOut.println(" - Select a costing method - ");
		m_pwOut.println("</OPTION>");

		m_pwOut.println("<OPTION");
		if (m_OptionInput.getCostingMethod().compareToIgnoreCase(
				Long.toString(SMTableicoptions.COSTING_METHOD_LIFO)) == 0){
			m_pwOut.println( " selected=yes ");
		}
		m_pwOut.println(" VALUE=\"" + Integer.toString(SMTableicoptions.COSTING_METHOD_LIFO) + "\">");
		m_pwOut.println(SMTableicoptions.COSTING_METHOD_LIFO_LABEL);
		m_pwOut.println("</OPTION>");

		m_pwOut.println("<OPTION");
		if (m_OptionInput.getCostingMethod().compareToIgnoreCase(
				Long.toString(SMTableicoptions.COSTING_METHOD_FIFO)) == 0){
			m_pwOut.println( " selected=yes ");
		}
		m_pwOut.println(" VALUE=\"" + Integer.toString(SMTableicoptions.COSTING_METHOD_FIFO) + "\">");
		m_pwOut.println(SMTableicoptions.COSTING_METHOD_FIFO_LABEL);
		m_pwOut.println("</OPTION>");

		m_pwOut.println("<OPTION");
		if (m_OptionInput.getCostingMethod().compareToIgnoreCase(
				Long.toString(SMTableicoptions.COSTING_METHOD_AVERAGECOST)) == 0){
			m_pwOut.println( " selected=yes ");
		}
		m_pwOut.println(" VALUE=\"" + Integer.toString(SMTableicoptions.COSTING_METHOD_AVERAGECOST) + "\">");
		m_pwOut.println(SMTableicoptions.COSTING_METHOD_AVERAGECOST_LABEL);
		m_pwOut.println("</OPTION>");
		m_pwOut.println("</SELECT></TD>");

		m_pwOut.println("<TD>Select a costing method.</TD></TD>");

		//Allow negative qtys:
		m_pwOut.println("<TR><TD>Allow negative quantities:</TD>");
		if (m_OptionInput.getAllowNegativeQtys().compareToIgnoreCase("0") == 0){
			m_pwOut.println("<TD><INPUT TYPE=\"CHECKBOX\" NAME=\"" + ICOptionInput.ParamAllowNegativeQtys 
					+ "\" VALUE=\"1\"></TD>");
		}else{
			m_pwOut.println("<TD><INPUT TYPE=\"CHECKBOX\" NAME=\"" + ICOptionInput.ParamAllowNegativeQtys 
					+ "\" VALUE=\"1\" CHECKED></TD>");
		}
		m_pwOut.println("<TD>Allow the system to drop quantities below zero at locations?</TD></TR>");

		//Suppress bar codes on non-stock items:
		m_pwOut.println("<TR><TD>Suppress bar codes on non-stock item labels:</TD>");
		if (m_OptionInput.getSuppressBarCodesOnNonStockItems().compareToIgnoreCase("0") == 0){
			m_pwOut.println("<TD><INPUT TYPE=\"CHECKBOX\" NAME=\"" + ICOptionInput.Paramisuppressbarcodesonnonstockitems 
					+ "\" VALUE=\"1\"></TD>");
		}else{
			m_pwOut.println("<TD><INPUT TYPE=\"CHECKBOX\" NAME=\"" + ICOptionInput.Paramisuppressbarcodesonnonstockitems 
					+ "\" VALUE=\"1\" CHECKED></TD>");
		}
		m_pwOut.println("<TD>Use this to prevent non-stock ('expensed') items from being counted with a bar code reader in the warehouse.</TD></TR>");
		
		//Display the fields:
		//Batch posting in process:
		m_pwOut.println("<TR><TD ALIGN=LEFT>Batch posting in process?:</TD>");
		if (m_OptionInput.getBatchPostingInProcess().compareToIgnoreCase("1") == 0){
			m_pwOut.println("<TD><B>" + "YES" + "</B></TD>");
		}else{
			m_pwOut.println("<TD><B>" + "NO" + "</B></TD>");
		}
		m_pwOut.println("<TD>Indicates whether a batch posting is not complete.</TD>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICOptionInput.ParamBatchPostingInProcess 
				+ "\" VALUE=\"" + m_OptionInput.getBatchPostingInProcess() + "\">");
		m_pwOut.println("</TR>");

		//Posting user:
		m_pwOut.println("<TR><TD ALIGN=LEFT>Currently posting user:</TD>");
		m_pwOut.println("<TD><B>" + m_OptionInput.getPostingUserFullName() + "</B></TD>");
		m_pwOut.println("<TD>Blank if posting processes are complete.</TD>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICOptionInput.ParamPostingUserFullName
				+ "\" VALUE=\"" + m_OptionInput.getPostingUserFullName() + "\">");
		m_pwOut.println("</TR>");

		//Posting process
		m_pwOut.println("<TR><TD ALIGN=LEFT>Currently posting process:</TD>");
		m_pwOut.println("<TD><B>" + m_OptionInput.getPostingProcess() + "</B></TD>");
		m_pwOut.println("<TD>Blank if posting processes are complete.</TD>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICOptionInput.ParamPostingProcess
				+ "\" VALUE=\"" + m_OptionInput.getPostingProcess() + "\">");
		m_pwOut.println("</TR>");

		//Posting start date
		m_pwOut.println("<TR><TD ALIGN=LEFT>Posting start time:</TD>");
		m_pwOut.println("<TD><B>" + m_OptionInput.getPostingStartDate() + "</B></TD>");
		m_pwOut.println("<TD>Starting date and time of any current/incomplete posting process.</TD>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICOptionInput.ParamPostingStartDate
				+ "\" VALUE=\"" + m_OptionInput.getPostingStartDate() + "\">");
		m_pwOut.println("</TR>");
		
		//Google Drive integration:
		m_pwOut.println("<TR><TD ALIGN=LEFT COLSPAN=3><B><U>GOOGLE DRIVE FOLDER CREATION:</U></B></TD></TR>");
				
	    //GDrive purchase orders parent folder ID:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Google Drive Purchase Order parent folder ID</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ ICOptionInput.Paramsgdrivepurchaseordersparentfolderid + "\""
				+ " VALUE=\"" + m_OptionInput.getgdrivepurchaseordersparentfolderid() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTableicoptions.gdrivepurchaseordersparentfolderidlength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>This is the Google Drive 'ID' of the PARENT folder in which new purchase order folders will be created.</TD></TR>");
				
	   //GDrive purchase orders folder prefix:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>PURCHASE ORDER folder prefix</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ ICOptionInput.Paramsgdrivepurchaseordersfolderprefix + "\""
				+ " VALUE=\"" + m_OptionInput.getgdrivepurchaseordersfolderprefix() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTableicoptions.gdrivepurchaseordersfolderprefixlength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>If you want a short prefix in front of the created purchase order folder name, enter it here.</TD></TR>");
				
		//GDrive purchase orders folder suffix:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>PURCHASE ORDER folder suffix</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ ICOptionInput.Paramsgdrivepurchaseordersfoldersuffix + "\""
				+ " VALUE=\"" + m_OptionInput.getgdrivepurchaseordersfoldersuffix() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTableicoptions.gdrivepurchaseordersfoldersuffixlength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>If you want a short suffix at the end of the created purchase order folder name, enter it here.</TD></TR>");
		
		m_pwOut.println("</TABLE>");

		m_pwOut.println("<BR><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Save changes' STYLE='height: 0.24in'>");

		//End the edit form:
		m_pwOut.println("</FORM>");  

		return true;

	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
