package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableservicetypes;
import SMDataDefinition.SMTablesmestimatelines;
import SMDataDefinition.SMTablesmestimates;
import SMDataDefinition.SMTablesmestimatesummaries;
import SMDataDefinition.SMTabletax;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;

public class SMListCommonEstimateItems extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static final String FORM_NAME = "MAINFORM";
	private static final String PREFIX_QTY = "ITEMQTY";
	private static final String ADD_LINES_CAPTION = "Add selected <B><FONT COLOR=RED>i</FONT></B>tems";
	
	private static final int COMMON_ITEMS_LIMIT = 50;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				SMEstimate.OBJECT_NAME,
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMListCommonEstimateItemsAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditSMEstimates
		);   
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditSMEstimates, request)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		SMEstimate estimate = (SMEstimate)smedit.getCurrentSession().getAttribute(SMEstimate.OBJECT_NAME);
		//System.out.println("[202006194016] - dumpData = " + estimate.dumpData());
		
	    smedit.printHeaderTable();
	    smedit.getPWOut().println(SMUtilities.getMasterStyleSheetLink());
	    smedit.getPWOut().println(SMUtilities.getShortcutJSIncludeString(getServletContext()));
		
	    try {
	    	createEditPage(
	    		getEditHTML(
	    			smedit, 
	    			estimate,
	    			request
	    		), 
	    		FORM_NAME,
				smedit.getPWOut(),
				smedit,
				estimate
			);
		} catch (Exception e) {
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditSMEstimateEdit"
				+ "?" + SMTablesmestimates.lid + "=" + estimate.getslid()
				+ "&Warning=" + SMUtilities.URLEncode(e.getMessage())
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
				return;
		}
	}
	public void createEditPage(
			String sEditHTML,
			String sFormClassName,
			PrintWriter pwOut,
			SMMasterEditEntry sm,
			SMEstimate estimate
	) throws Exception{
		//Create HTML Form
		String sFormString = "<FORM ID='" + sFormClassName + "' NAME='" + sFormClassName + "' ACTION='" 
			+ SMUtilities.getURLLinkBase(getServletContext()) + sm.getCalledClass() + "'";
		
		sFormString	+= " METHOD='POST'";
		if (sFormClassName.compareToIgnoreCase("") != 0){
			sFormString += " class=" + sFormClassName + ">";
		}
		pwOut.println(sFormString);
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sm.getsDBID() + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" 
				+ SMUtilities.getFullClassName(this.toString()) + "\">");

		//Create HTML Fields
		try {
			pwOut.println(sEditHTML);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		//Add save button
		//pwOut.println("<BR>" + createSaveButton());
		pwOut.println("</FORM>");
	}
	private String getEditHTML(SMMasterEditEntry sm, SMEstimate estimate, HttpServletRequest req) throws Exception{
		String s = "";
		
		s += sCommandScripts();
		
		//Make sure we load the summary first:
		try {
			estimate.loadSummary(getServletContext(), sm.getsDBID(), sm.getUserID());
		} catch (Exception e) {
			throw new Exception("Error [202006193335] - could not load summary for estimate number " + estimate.getslid() + " to get commonly used items - " + e.getMessage());
		}
		
		//Get the service type description:
		String sOrderTypeDescription = "";
		String SQL = "SELECT"
			+ " " + SMTableservicetypes.sName
			+ " FROM " + SMTableservicetypes.TableName
			+ " WHERE ("
				+ "(" + SMTableservicetypes.id + " = " + estimate.getsummary().getsiordertype() + ")"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sm.getsDBID(), "MySQL", SMUtilities.getFullClassName(this.toString()));
			if (rs.next()) {
				sOrderTypeDescription = rs.getString(SMTableservicetypes.sName);
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception("Error [202006220541] - reading service types with SQL: '" + SQL + "' - " + e1.getMessage());
		}
		
		s += "<BR>Items listed below are those most commonly used on <B><I>" + sOrderTypeDescription + "</I></B> estimates:" + "\n";
		
		s += "<BR>" + createAddLinesButton();
		
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\" >" + "\n";
		
		//Headings:
		s += "  <TR> \n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\""
				+ " style = \" color:white; font-weight:bold; background-color:black; \""
				+ ">"
				+ "Qty<BR>needed:"
				+ "</TD> \n"
			;

		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\""
				+ " style = \" color:white; font-weight:bold; background-color:black; \""
				+ ">"
				+ "Times<BR>used:"
				+ "</TD> \n"
			;
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\""
				+ " style = \" color:white; font-weight:bold; background-color:black; \""
				+ ">"
				+ "Item #"
				+ "</TD> \n"
			;
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\""
				+ " style = \" color:white; font-weight:bold; background-color:black; \""
				+ ">"
				+ "Description"
				+ "</TD> \n"
			;

		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\""
				+ " style = \" color:white; font-weight:bold; background-color:black; \""
				+ ">"
				+ "U/M"
				+ "</TD> \n"
			;
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\""
				+ " style = \" color:white; font-weight:bold; background-color:black; \""
				+ ">"
				+ "Stock<BR>item?"
				+ "</TD> \n"
			;
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\""
				+ " style = \" color:white; font-weight:bold; background-color:black; \""
				+ ">"
				+ "Most<BR>recent cost"
				+ "</TD> \n"
			;
		
		s += "  </TR> \n";
		
		//Get the list of commonly used items:
		SQL = "SELECT"
			+ " " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sItemDescription
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.bdmostrecentcost
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.inonstockitem
			+ ", COUNT(" + SMTablesmestimatelines.TableName + "." + SMTablesmestimatelines.sitemnumber + ") AS TIMESUSED"
			+ " FROM " + SMTablesmestimatelines.TableName
			+ " LEFT JOIN " + SMTableicitems.TableName
			+ " ON " + SMTablesmestimatelines.TableName + "." + SMTablesmestimatelines.sitemnumber + " = "
			+ SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			+ " LEFT JOIN " + SMTablesmestimatesummaries.TableName
			+ " ON " + SMTablesmestimatelines.TableName + "." + SMTablesmestimatelines.lsummarylid + " = "
				+ SMTablesmestimatesummaries.TableName + "." + SMTablesmestimatesummaries.lid
			+ " WHERE ("
				+ "(" + SMTablesmestimatesummaries.TableName + "." + SMTablesmestimatesummaries.iordertype + " = " + estimate.getsummary().getsiordertype() + ")"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " IS NOT NULL)"
			+ ")"
			+ " GROUP BY " + SMTablesmestimatelines.TableName + "." + SMTablesmestimatelines.sitemnumber
			+ " ORDER BY COUNT(" + SMTablesmestimatelines.TableName + "." + SMTablesmestimatelines.sitemnumber + ") DESC"
			+ " LIMIT " + COMMON_ITEMS_LIMIT
		;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sm.getsDBID(), "MySQL", SMUtilities.getFullClassName(this.toString()));
			while (rs.next()) {
				//Build rows here:
				s += "  <TR> \n";
				
				s += "    <TD>"
					+ "<INPUT TYPE = TEXT"
					+ " NAME = \"" 
						+ PREFIX_QTY + rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sItemNumber)
					+ "\""
					+ " ID = \"" 
						+ PREFIX_QTY + rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sItemNumber)
					+ "\""
					+ " style = \" width:75px; text-align:right; \" "
					+ "</TD> \n"
				;
				
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" >"
						+ Long.toString(rs.getLong("TIMESUSED"))
						+ "</TD> \n"
					;
				
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" >"
					+ rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sItemNumber)
					+ "</TD> \n"
				;
				
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" >"
						+ rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sItemDescription)
						+ "</TD> \n"
					;
				
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" >"
						+ rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure)
						+ "</TD> \n"
					;
				
				String sNonStockFlag = "Stock";
				if(rs.getLong(SMTableicitems.TableName + "." + SMTableicitems.inonstockitem) == 1L) {
					sNonStockFlag = "<B><FONT COLOR=RED>Non-stock</FONT></B>";
				}
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" >"
					+ sNonStockFlag + "</TD> \n";
				
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" >"
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableicitems.TableName + "." + SMTableicitems.bdmostrecentcost))
						+ "</TD> \n"
					;
				
				s += "  </TR> \n";
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [202006193605] - error reading most commonly used items with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		s += "</TABLE>" + "\n";
		
		s += createAddLinesButton();
		return s;
	}
	private String createAddLinesButton(){
		return "<button type=\"button\""
				+ " value=\"" + ADD_LINES_CAPTION + "\""
				+ " name=\"" + ADD_LINES_CAPTION + "\""
				+ " onClick=\"addselecteditems();\">"
				+ ADD_LINES_CAPTION
				+ "</button>\n";
	}
	private String sCommandScripts(
			) throws Exception{
			String s = "";
			
			s += "<NOSCRIPT>\n"
				+ "    <font color=red>\n"
				+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
				+ "    </font>\n"
				+ "</NOSCRIPT>\n"
			;
 
			s += "<script type='text/javascript'>\n";
					
			s += "window.onload = initShortcuts;\n";
			
			//Add selected items:
			s += "function addselecteditems(){\n"
				+ "    document.forms[\"" + FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
			
			s += "function initShortcuts() {\n";
			
			s += "    shortcut.add(\"Alt+i\",function() {\n";
			s += "        addselecteditems();\n";
			s += "    },{\n";
			s += "        'type':'keydown',\n";
			s += "        'propagate':false,\n";
			s += "        'target':document\n";
			s += "    });\n";
			
			s += "}\n";
			
			s += "</script>\n\n";
			return s;
		}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}