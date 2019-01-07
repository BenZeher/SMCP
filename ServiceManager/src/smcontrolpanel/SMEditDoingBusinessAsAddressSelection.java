package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import SMClasses.SMDoingbusinessasaddress;
import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTabledoingbusinessasaddresses;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;

public class SMEditDoingBusinessAsAddressSelection extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sCalledClassName = "smcontrolpanel.SMEditDoingBusinessAsAddressEdit";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		SMDoingbusinessasaddress entry = new SMDoingbusinessasaddress();
		SMMasterEditSelect smeditselect = new SMMasterEditSelect(
				request,
				response,
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				sCalledClassName,
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditDoingBusinessAsAddresses
				);
		
		if (!smeditselect.processSession(getServletContext(), SMSystemFunctions.SMEditDoingBusinessAsAddresses)){
			PrintWriter m_out = response.getWriter();
			m_out.println("Error in process session: " + smeditselect.getErrorMessages());
			return;
		}
		
		smeditselect.printHeaderTable();
		
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

		String s = "<BR><BR>";	
		//Doing business as address selection:
		ArrayList<String> arrAddressIDs = new ArrayList<String>(0);
		ArrayList<String> arrAddressDescriptions = new ArrayList<String>(0);
		 String sSQL = "SELECT"
			+ " " + SMTabledoingbusinessasaddresses.lid
			+ ", " + SMTabledoingbusinessasaddresses.sDescription
			+ " FROM " + SMTabledoingbusinessasaddresses.TableName
			+ " ORDER BY " + SMTabledoingbusinessasaddresses.sDescription
		;
		//First, add a bank account so we can be sure the user chose one:
		 arrAddressIDs.add("0");
		 arrAddressDescriptions.add("*** Select address ***");
				
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(),
					smselect.getsDBID(), "MySQL", SMUtilities.getFullClassName(this.toString())
					+ ".getEditHTML - user: " + smselect.getUserID()
					+ " - "
					+ smselect.getFullUserName()
					);
			while (rs.next()) {
				arrAddressIDs.add(rs.getString(SMTabledoingbusinessasaddresses.lid));
				arrAddressDescriptions.add(rs.getString(SMTabledoingbusinessasaddresses.sDescription)
				);
			}
			rs.close();
		} catch (SQLException e) {
			s += "<BR><B>Error [14516001533] reading doing business as addresses - " + e.getMessage() + ".</B><BR>";
		}			
		s += clsCreateHTMLFormFields.Create_Edit_Form_List_Field(
				SMDoingbusinessasaddress.Paramlid, 
				arrAddressIDs, 
				"0", 
				arrAddressDescriptions
			);

		
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}