package smic;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMTableicitems;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ICAddRemovePhysInvItemsEdit  extends HttpServlet {

	public static final String PARAM_ADD_OR_REMOVE = "ADDORREMOVE";
	public static final String PARAM_ADD_VALUE = "ADD";
	public static final String PARAM_REMOVE_VALUE = "REMOVE";
	public static final String PARAM_STARTING_ITEM_NUMBER = "STARTINGITEMNUMBER";
	public static final String PARAM_ENDING_ITEM_NUMBER = "ENDINGITEMNUMBER";
	public static final String PARAM_STARTING_REPORTGROUP1 = "STARTINGREPORTGROUP1";
	public static final String PARAM_ENDING_REPORTGROUP1 = "ENDINGREPORTGROUP1";
	public static final String PARAM_STARTING_REPORTGROUP2 = "STARTINGREPORTGROUP2";
	public static final String PARAM_ENDING_REPORTGROUP2 = "ENDINGREPORTGROUP2";
	public static final String PARAM_STARTING_REPORTGROUP3 = "STARTINGREPORTGROUP3";
	public static final String PARAM_ENDING_REPORTGROUP3 = "ENDINGREPORTGROUP3";
	public static final String PARAM_STARTING_REPORTGROUP4 = "STARTINGREPORTGROUP4";
	public static final String PARAM_ENDING_REPORTGROUP4 = "ENDINGREPORTGROUP4";
	public static final String PARAM_STARTING_REPORTGROUP5 = "STARTINGREPORTGROUP5";
	public static final String PARAM_ENDING_REPORTGROUP5 = "ENDINGREPORTGROUP5";
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		//PrintWriter debug_out = new PrintWriter(System.out);
		//SMUtilities.printRequestParameters(debug_out, request);
		
		ICPhysicalInventoryEntry entry = new ICPhysicalInventoryEntry(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				"Items Included In Physical Inventory",
				SMUtilities.getFullClassName(this.toString()),
				"smic.ICAddRemovePhysInvItemsAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.ICEditPhysicalInventory
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.ICEditPhysicalInventory)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
	    smedit.printHeaderTable();
	    
	    //Add a link to the inventory menu:
	    smedit.getPWOut().println(
	    	"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
	    		+ smedit.getsDBID() + "\">Return to Inventory Control Main Menu</A><BR>");
	    
	    //Add a link to physical inventory list:
	    smedit.getPWOut().println(
	    	"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICListPhysicalInventories?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
	    		+ smedit.getsDBID() + "\">Return to physical inventory list</A><BR>");
	    
	    //Add a link to parent physical inventory:
	    smedit.getPWOut().println(
	    	"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPhysicalInventory"
	    		+ "?" + ICPhysicalInventoryEntry.ParamID + "=" + entry.slid()
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() 
	    		+ "\">Return to physical inventory</A><BR>");
	    
		smedit.getPWOut().println("<BR>");
		
		smedit.setbIncludeDeleteButton(false);
		
	    try {
			smedit.createEditPage(getEditHTML(smedit, entry), "");
		} catch (SQLException e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + ICPhysicalInventoryEntry.ParamID + "=" + entry.slid()
				+ "&Warning=Could not load count ID: " + entry.slid() + sError
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
				return;
		}
		
	    return;
	}

	private String getEditHTML(SMMasterEditEntry sm, ICPhysicalInventoryEntry entry)
		throws SQLException{

		String s = "";

		s += "<I>You can EITHER specify the items that will be allowed in your count here, on this screen - OR you can simply "
			+ " allow the system to ADD items to your physical inventory as you add counts.  "
			+ "The purpose of pre-specifying (on this screen) which items you wish to include in your physical inventory"	
			+ " is so that if you want certain items included in the count, but you find NONE in your warehouse, those items will automatically"
			+ " be set to a zero count in the inventory worksheet." + "<BR>" + "\n"
		;
		
		//Store the calling class in a hidden variable here:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" 
			+ clsManageRequestParameters.get_Request_Parameter("CallingClass", sm.getRequest()) + "\""
			+ "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPhysicalInventoryEntry.ParamID + "\" VALUE=\"" 
				+ entry.slid() + "\""
				+ "\">";
		
		s += "<TABLE BORDER=1>";
		
		//Add or remove?
		s += "<TR><TD ALIGN=RIGHT><B>ADD</B> or <B>REMOVE</B> the selected items from the physical inventory?:</TD>"
				+ "<TD>" 
				+ "<SELECT NAME = \"" +  PARAM_ADD_OR_REMOVE + "\">"
				+ "<OPTION VALUE = \"" + PARAM_ADD_VALUE + "\">ADD items selected below"
				+ "<OPTION VALUE = \"" + PARAM_REMOVE_VALUE + "\">REMOVE items selected below"
				+ "</SELECT>"
				+ "</TD>"
				+ "</TR>"
				;
		
		//Item numbers
		String sTemp = clsManageRequestParameters.get_Request_Parameter(PARAM_STARTING_ITEM_NUMBER, sm.getRequest());
		s += "<TR>";
		s +=  "<TD ALIGN=RIGHT>" + "Starting with item number:&nbsp;";
		s +=  "<INPUT TYPE=TEXT NAME=\"" + PARAM_STARTING_ITEM_NUMBER + "\"";
		s += " VALUE=\"" + sTemp + "\"";
		s += "SIZE=" + "40";
		s += " MAXLENGTH=" + Integer.toString(SMTableicitems.sItemNumberLength);
		s += "></TD>";
		sTemp = clsManageRequestParameters.get_Request_Parameter(PARAM_ENDING_ITEM_NUMBER, sm.getRequest());
		if (sTemp.compareToIgnoreCase("") == 0){
			sTemp = "ZZZZZZZZZZZZZZZZZZZZ";
		}
		s +=  "<TD ALIGN=RIGHT>" + "&nbsp;and ending with item number:&nbsp;";
		s +=  "<INPUT TYPE=TEXT NAME=\"" + PARAM_ENDING_ITEM_NUMBER + "\"";
		s += " VALUE=\"" + sTemp + "\"";
		s += "SIZE=" + "40";
		s += " MAXLENGTH=" + Integer.toString(SMTableicitems.sItemNumberLength);
		s += "></TD>";
		s += "</TR>";
		
		//Report group 1
		sTemp = clsManageRequestParameters.get_Request_Parameter(PARAM_STARTING_REPORTGROUP1, sm.getRequest());
		s += "<TR>";
		s +=  "<TD ALIGN=RIGHT>" + "AND starting with report group 1:&nbsp;";
		s +=  "<INPUT TYPE=TEXT NAME=\"" + PARAM_STARTING_REPORTGROUP1 + "\"";
		s += " VALUE=\"" + sTemp + "\"";
		s += "SIZE=" + "40";
		s += " MAXLENGTH=" + Integer.toString(SMTableicitems.sreportgroup1Length);
		s += "></TD>";
		sTemp = clsManageRequestParameters.get_Request_Parameter(PARAM_ENDING_REPORTGROUP1, sm.getRequest());
		if (sTemp.compareToIgnoreCase("") == 0){
			sTemp = "ZZZZZZZZZZZZZZZZZZZZ";
		}
		s +=  "<TD ALIGN=RIGHT>" + "&nbsp;and ending with report group 1:&nbsp;";
		s +=  "<INPUT TYPE=TEXT NAME=\"" + PARAM_ENDING_REPORTGROUP1 + "\"";
		s += " VALUE=\"" + sTemp + "\"";
		s += "SIZE=" + "40";
		s += " MAXLENGTH=" + Integer.toString(SMTableicitems.sreportgroup1Length);
		s += "></TD>";
		s += "</TR>";

		//Report group 2
		sTemp = clsManageRequestParameters.get_Request_Parameter(PARAM_STARTING_REPORTGROUP2, sm.getRequest());
		s += "<TR>";
		s +=  "<TD ALIGN=RIGHT>" + "AND starting with report group 2:&nbsp;";
		s +=  "<INPUT TYPE=TEXT NAME=\"" + PARAM_STARTING_REPORTGROUP2 + "\"";
		s += " VALUE=\"" + sTemp + "\"";
		s += "SIZE=" + "40";
		s += " MAXLENGTH=" + Integer.toString(SMTableicitems.sreportgroup2Length);
		s += "></TD>";
		sTemp = clsManageRequestParameters.get_Request_Parameter(PARAM_ENDING_REPORTGROUP2, sm.getRequest());
		if (sTemp.compareToIgnoreCase("") == 0){
			sTemp = "ZZZZZZZZZZZZZZZZZZZZ";
		}
		s +=  "<TD ALIGN=RIGHT>" + "&nbsp;and ending with report group 2:&nbsp;";
		s +=  "<INPUT TYPE=TEXT NAME=\"" + PARAM_ENDING_REPORTGROUP2 + "\"";
		s += " VALUE=\"" + sTemp + "\"";
		s += "SIZE=" + "40";
		s += " MAXLENGTH=" + Integer.toString(SMTableicitems.sreportgroup2Length);
		s += "></TD>";
		s += "</TR>";

		//Report group 3
		sTemp = clsManageRequestParameters.get_Request_Parameter(PARAM_STARTING_REPORTGROUP3, sm.getRequest());
		s += "<TR>";
		s +=  "<TD ALIGN=RIGHT>" + "AND starting with report group 3:&nbsp;";
		s +=  "<INPUT TYPE=TEXT NAME=\"" + PARAM_STARTING_REPORTGROUP3 + "\"";
		s += " VALUE=\"" + sTemp + "\"";
		s += "SIZE=" + "40";
		s += " MAXLENGTH=" + Integer.toString(SMTableicitems.sreportgroup3Length);
		s += "></TD>";
		sTemp = clsManageRequestParameters.get_Request_Parameter(PARAM_ENDING_REPORTGROUP3, sm.getRequest());
		if (sTemp.compareToIgnoreCase("") == 0){
			sTemp = "ZZZZZZZZZZZZZZZZZZZZ";
		}
		s +=  "<TD ALIGN=RIGHT>" + "&nbsp;and ending with report group 3:&nbsp;";
		s +=  "<INPUT TYPE=TEXT NAME=\"" + PARAM_ENDING_REPORTGROUP3 + "\"";
		s += " VALUE=\"" + sTemp + "\"";
		s += "SIZE=" + "40";
		s += " MAXLENGTH=" + Integer.toString(SMTableicitems.sreportgroup3Length);
		s += "></TD>";
		s += "</TR>";

		//Report group 4
		sTemp = clsManageRequestParameters.get_Request_Parameter(PARAM_STARTING_REPORTGROUP4, sm.getRequest());
		s += "<TR>";
		s +=  "<TD ALIGN=RIGHT>" + "AND starting with report group 4:&nbsp;";
		s +=  "<INPUT TYPE=TEXT NAME=\"" + PARAM_STARTING_REPORTGROUP4 + "\"";
		s += " VALUE=\"" + sTemp + "\"";
		s += "SIZE=" + "40";
		s += " MAXLENGTH=" + Integer.toString(SMTableicitems.sreportgroup4Length);
		s += "></TD>";
		sTemp = clsManageRequestParameters.get_Request_Parameter(PARAM_ENDING_REPORTGROUP4, sm.getRequest());
		if (sTemp.compareToIgnoreCase("") == 0){
			sTemp = "ZZZZZZZZZZZZZZZZZZZZ";
		}
		s +=  "<TD ALIGN=RIGHT>" + "&nbsp;and ending with report group 4:&nbsp;";
		s +=  "<INPUT TYPE=TEXT NAME=\"" + PARAM_ENDING_REPORTGROUP4 + "\"";
		s += " VALUE=\"" + sTemp + "\"";
		s += "SIZE=" + "40";
		s += " MAXLENGTH=" + Integer.toString(SMTableicitems.sreportgroup4Length);
		s += "></TD>";
		s += "</TR>";

		//Report group 5
		sTemp = clsManageRequestParameters.get_Request_Parameter(PARAM_STARTING_REPORTGROUP5, sm.getRequest());
		s += "<TR>";
		s +=  "<TD ALIGN=RIGHT>" + "AND starting with report group 5:&nbsp;";
		s +=  "<INPUT TYPE=TEXT NAME=\"" + PARAM_STARTING_REPORTGROUP5 + "\"";
		s += " VALUE=\"" + sTemp + "\"";
		s += "SIZE=" + "40";
		s += " MAXLENGTH=" + Integer.toString(SMTableicitems.sreportgroup5Length);
		s += "></TD>";
		sTemp = clsManageRequestParameters.get_Request_Parameter(PARAM_ENDING_REPORTGROUP5, sm.getRequest());
		if (sTemp.compareToIgnoreCase("") == 0){
			sTemp = "ZZZZZZZZZZZZZZZZZZZZ";
		}
		s +=  "<TD ALIGN=RIGHT>" + "&nbsp;and ending with report group 5:&nbsp;";
		s +=  "<INPUT TYPE=TEXT NAME=\"" + PARAM_ENDING_REPORTGROUP5 + "\"";
		s += " VALUE=\"" + sTemp + "\"";
		s += "SIZE=" + "40";
		s += " MAXLENGTH=" + Integer.toString(SMTableicitems.sreportgroup5Length);
		s += "></TD>";
		s += "</TR>";

		s += "</TABLE>";
		
		s += "<B>NOTE: </B>Non-stock items will NOT be added to the physical inventory.";
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
