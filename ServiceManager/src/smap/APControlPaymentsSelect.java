package smap;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.FinderResults;
import smap.APVendor;
import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableicvendors;
import ServletUtilities.clsManageRequestParameters;

public class APControlPaymentsSelect extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String DOCNUMBER_FINDER_LINK_ID = "DOCFINDERLINK"; 
	private static String sCalledClassName = "smap.APControlPaymentsEdit";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		SMMasterEditSelect smeditselect = new SMMasterEditSelect(
				request,
				response,
				"Open invoice",
				SMUtilities.getFullClassName(this.toString()),
				sCalledClassName,
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.APControlPayments
				);
		
		if (!smeditselect.processSession(getServletContext(), SMSystemFunctions.APEditVendors)){
			PrintWriter m_out = response.getWriter();
			m_out.println("Error in process session: " + smeditselect.getErrorMessages());
			return;
		}
		
		smeditselect.printHeaderTable();
		smeditselect.showAddNewButton(false);
		
		smeditselect.getPrintWriter().println(
			"<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ smeditselect.getsDBID() + "\">Return to Accounts Payable Main Menu</A><BR>");
		
	    try {
	    	smeditselect.createEditForm(getEditHTML(smeditselect, request));
		} catch (SQLException e) {
    		smeditselect.getPrintWriter().println("Could not create edit form - " + e.getMessage());
    		smeditselect.getPrintWriter().println("</HTML>");
			return;
		}
	    return;

	}
	private String getEditHTML(SMMasterEditSelect smselect, HttpServletRequest req) throws SQLException{

		String s = "";
		
		s += sCommandScripts(smselect) + "\n";
		
	    String sVendorCode = clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.svendor, req);
	    String sDocNumber = clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.sdocnumber, req);
	    
	    s += "<TABLE class=\"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">\n";
	    s += "  <TR>\n";
	    s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\" >"
	    	+ "Enter vendor number:"
	    	+ "    </TD>\n"
	    ;
	    	
	    s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" >"
	    	+ "<INPUT TYPE=TEXT"
	    	+ " NAME=\"" + SMTableaptransactions.svendor + "\""
	    	+ " ID=\"" + SMTableaptransactions.svendor + "\""
			+ " VALUE = \"" + sVendorCode + "\""
			+ " SIZE=32 MAXLENGTH=" + Integer.toString(SMTableaptransactions.svendorlength)
			+ " STYLE=\"width: 2.41in; height: 0.25in\""
			+ " onchange=updateDocFinderLink();"
			+ ">&nbsp;\n"
			;
		
		s += "    </TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >\n";
		//Link to finder:
		s += "<A HREF=\""
			+ APVendor.getFindVendorLink(
				SMUtilities.getFullClassName(this.toString()), 
				SMTableaptransactions.svendor, 
				"", 
				getServletContext(),
				smselect.getsDBID()
			)
			+ "\"> Find vendor</A>\n";
		s += "    </TD>\n";
		s += "  </TR>\n";
		
		
	    s += "  <TR>\n";
	    s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\" >"
		   	+ "Enter document number:"
		   	+ "    </TD>\n"
		;
	    
	    s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" >"
	    	+ "<INPUT TYPE=TEXT"
	    	+ " NAME=\"" + SMTableaptransactions.sdocnumber + "\""
			+ " VALUE = \"" + sDocNumber + "\""
			+ " SIZE=32 MAXLENGTH=" + Integer.toString(SMTableaptransactions.sdocnumberlength)
			+ " STYLE=\"width: 2.41in; height: 0.25in\">&nbsp;\n";
		
		s += "    </TD>\n";

		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >\n";		
		
		//Link to finder:
		s += "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "SMClasses.ObjectFinder"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID()
			+ "\""
			+ " ID=\"" + DOCNUMBER_FINDER_LINK_ID + "\""
			
			+ "> Find document</A>\n";
		s += "    </TD>\n";
		s += "  </TR>\n";
		
		
		s += "</TABLE>\n";
		return s;
	}
	private String sCommandScripts(SMMasterEditSelect smselect){
		
		String s = "";
		s += "<NOSCRIPT>\n"
				+ "    <font color=red>\n"
				+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
				+ "    </font>\n"
				+ "</NOSCRIPT>\n"
			;
		s += "<script type='text/javascript'>\n";
		
		s += "window.onload = function() {\n"
				+ "    updateDocFinderLink();\n"
				+ "}\n"
			;
		
		s += "function updateDocFinderLink(){ \n"
			+ "    var docfinderlink = document.getElementById(\"" + DOCNUMBER_FINDER_LINK_ID + "\");\n"
			+ "    if (docfinderlink != null){ \n"
			+ "        var svendor = encodeURIComponent(document.getElementById(\"" + SMTableaptransactions.svendor + "\").value);\n"
			+ "        docfinderlink.href = '"
			
				+ SMUtilities.getURLLinkBase(getServletContext()) + "SMClasses.ObjectFinder' +\n"
				+ " '?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID() + "' +\n"
				+ " '&ObjectName=" + SMTableaptransactions.OBJECT_NAME + "' +\n"
				+ " '&ResultClass=FinderResults" + "' +\n"
				+ " '&SearchingClass=" + SMUtilities.getFullClassName(this.toString()) + "' +\n"
				+ " '&ReturnField=" + SMTableaptransactions.sdocnumber + "' +\n"
				+ " '&SearchField1=" + SMTableaptransactions.sdocnumber + "' +\n"
				+ " '&SearchFieldAlias1=Document%20No." + "' +\n"
				+ " '&ResultListField1="  + SMTableaptransactions.sdocnumber + "' +\n"
				+ " '&ResultHeading1=Document%20No." + "' +\n"
				+ " '&ResultListField2="  + SMTableaptransactions.svendor + "' +\n"
				+ " '&ResultHeading2=Vendor" + "' +\n"
				+ " '&ResultListField3="  + SMTableicvendors.TableName + "." + SMTableicvendors.sname + "' +\n"
				+ " '&ResultHeading3=Vendor%20Name" + "' +\n"
				+ " '&ResultListField4="  + SMTableaptransactions.datdocdate + "' +\n"
				+ " '&ResultHeading4=Document%20Date" + "' +\n"
				+ " '&ResultListField5="  + SMTableaptransactions.datduedate + "' +\n"
				+ " '&ResultHeading5=Due%20Date" + "' +\n"
				+ " '&ResultListField6="  + SMTableaptransactions.bdoriginalamt + "' +\n"
				+ " '&ResultHeading6=Original%20Amt" + "' +\n"
				+ " '&ResultListField7="  + SMTableaptransactions.bdcurrentamt + "' +\n"
				+ " '&ResultHeading7=Current%20Amt" + "' +\n"
				+ " '&ResultListField8="  + SMTableaptransactions.datdiscountdate + "' +\n"
				+ " '&ResultHeading8=Discount%20Date" + "' +\n"
				+ " '&ResultListField9="  + SMTableaptransactions.bdoriginaldiscountavailable + "' +\n"
				+ " '&ResultHeading9=Original%20Discount%20Amt" + "' +\n"
				+ " '&ResultListField10="  + SMTableaptransactions.bdcurrentdiscountavailable + "' +\n"
				+ " '&ResultHeading10=Discount%20Available" + "' +\n"
				+ " '&ResultListField11="  + "IF(" + SMTableaptransactions.ionhold + " = 1, \\'Y\\', \\'N\\')" + "' +\n"
				+ " '&ResultHeading11=On%20Hold?" + "' +\n"
				+ " '&" + FinderResults.ADDITIONAL_WHERE_CLAUSE_PARAMETER + "=(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + " = \\'' + svendor + '\\')' +\n"
				+ " ' AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt + " != 0.00)' +\n"
				+ " ' AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype + " = " + SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_INVOICE + ")' +\n"
				+ " '&" + FinderResults.FINDER_BOX_TITLE + "=Open invoices for vendor: ' + svendor +\n"
				//+ " '&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID() + "' +\n"
				+ " '*" + SMTableaptransactions.svendor + "=' + svendor +\n"
			
				+ "''; \n"
			+ "    } \n"
			+ "} \n"
		;
		s += "</script>\n";
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}