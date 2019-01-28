package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import SMDataDefinition.SMTableicinvoiceexportsequences;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ICRecreateAPExportSelection extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final String STARTING_APEXPORTSEQUENCEDATE = "STARTING_APEXPORTSEQUENCEDATE";
	public static final String ENDING_APEXPORTSEQUENCEDATE = "ENDING_APEXPORTSEQUENCEDATE";
	public static final String SUBMIT_BUTTON_NAME = "SUBMIT_BUTTON";
	public static final String SUBMIT_BUTTON_LABEL = "List AP Export Sequences";
	public static final String SHOW_INDIVIDUAL_INVOICES_CHECKBOX = "SHOW_INDIVIDUAL_INVOICES";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		String sCalledClassName = "smic.ICRecreateAPExportEdit";
		
		SMMasterEditSelect smeditselect = new SMMasterEditSelect(
				request,
				response,
				"PO Invoice Export Sequence",
				SMUtilities.getFullClassName(this.toString()),
				sCalledClassName,
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.ICRecreateAPInvoiceExport
				);
		
		if (!smeditselect.processSession(getServletContext(), SMSystemFunctions.ICRecreateAPInvoiceExport)){
			PrintWriter m_out = response.getWriter();
			m_out.println("Error in process session: " + smeditselect.getErrorMessages());
			return;
		}
		//We'll create our own submit button:
		smeditselect.showEditButton(false);
		smeditselect.showAddNewButton(false);
		
		smeditselect.setFormTitle("Re-create AP Invoice Export");
		smeditselect.printHeaderTable();
		smeditselect.getPrintWriter().println(
			"<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ smeditselect.getsDBID() + "\">Return to Inventory Control Main Menu</A><BR>");
		
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
	    String sStartingSequence = "";
	    String sEndingSequence = "";
	    
	    //Get the range of available export dates:
	    String SQL = "SELECT"
	    	+ " " + SMTableicinvoiceexportsequences.datexported
	    	+ " FROM " + SMTableicinvoiceexportsequences.TableName
	    	+ " ORDER BY " + SMTableicinvoiceexportsequences.datexported
	    ;
	    try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					smselect.getsDBID(), 
					"MySQL", 
					this.toString() + ".getEditHTML - user: " + smselect.getUser()
			);
			while (rs.next()){
				if (sStartingSequence.compareToIgnoreCase("") == 0){
					sStartingSequence = clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTableicinvoiceexportsequences.datexported));
				}
				sEndingSequence = clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTableicinvoiceexportsequences.datexported));
			}
			rs.close();
		} catch (Exception e) {
			throw new SQLException("Could not read export sequence posting dates - " + e.getMessage());
		}
	    
	    if (req.getParameter(STARTING_APEXPORTSEQUENCEDATE) != null){
	    	sStartingSequence = req.getParameter(STARTING_APEXPORTSEQUENCEDATE);
	    }
	    if (req.getParameter(ENDING_APEXPORTSEQUENCEDATE) != null){
	    	sEndingSequence = req.getParameter(ENDING_APEXPORTSEQUENCEDATE);
	    }
	    
		s+= "<BR>List AP Invoice Exports starting from export date:&nbsp;";
		s+= "<INPUT TYPE=TEXT NAME=\"" 
				+ STARTING_APEXPORTSEQUENCEDATE 
				+ "\" VALUE=\"" + sStartingSequence 
				+ "\" SIZE = " + "10" 
				+ " MAXLENGTH = " + "10" + ">" 
				+ SMUtilities.getDatePickerString(STARTING_APEXPORTSEQUENCEDATE, getServletContext());
		s+= "&nbsp;Up to export date:&nbsp;";
		s+= "<INPUT TYPE=TEXT NAME=\"" 
				+ ENDING_APEXPORTSEQUENCEDATE 
				+ "\" VALUE=\"" + sEndingSequence 
				+ "\" SIZE = " + "10" 
				+ " MAXLENGTH = " + "10" + ">" 
				+ SMUtilities.getDatePickerString(ENDING_APEXPORTSEQUENCEDATE, getServletContext());
		
		s+= "<BR>Show individual invoices in each export?<INPUT TYPE=CHECKBOX NAME='" + SHOW_INDIVIDUAL_INVOICES_CHECKBOX + "' width=0.25>";
		
		s += "<BR><BR><INPUT TYPE=SUBMIT NAME='" + SUBMIT_BUTTON_NAME 
				+ "' VALUE='" + SUBMIT_BUTTON_LABEL + "' STYLE='height: 0.24in'></P>";
		
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}