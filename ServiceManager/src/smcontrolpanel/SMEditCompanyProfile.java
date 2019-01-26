package smcontrolpanel;

import SMDataDefinition.SMTablecompanyprofile;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SMEditCompanyProfile extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sObjectName = "Company Profile";
	private static final String sCalledClassName = "SMEditCompanyProfileAction";
	
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditCompanyProfile))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Manage " + sObjectName;
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	    
	    Edit_Record(out, sDBID, false);
		out.println("</BODY></HTML>");
	}
	
	private void Edit_Record(
			PrintWriter pwOut, 
			String sDBID,
			boolean bAddNew){
	    		
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel." + sCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    String sOutPut = "";
	  
        sOutPut = "<TABLE BORDER=12 CELLSPACING=2>";
        
		try{
			// Get the record to edit:
	        String sSQL = SMMySQLs.Get_CompanyProfile_SQL();
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	        
	        rs.next();
	        
	        // Store the database ID in a hidden field:
	        pwOut.println("<INPUT TYPE=HIDDEN NAME=\"EditCode\" VALUE=\"" + rs.getString(SMTablecompanyprofile.sDatabaseID) + "\">");
	        //Display fields:
	    	// sCompanyName
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablecompanyprofile.sCompanyName,
	        		clsStringFunctions.filter(rs.getString(SMTablecompanyprofile.sCompanyName)), 
	        		SMTablecompanyprofile.sCompanyNameLength, 
	        		"Company name:", 
	        		"Corporate name.");
	    	
	        // sAddress01
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablecompanyprofile.sAddress01,
	        		clsStringFunctions.filter(rs.getString(SMTablecompanyprofile.sAddress01)), 
	        		SMTablecompanyprofile.sAddress01Length, 
	        		"Address line 1:", 
	        		"First line of address.");
	    		        
	        // sAddress02
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablecompanyprofile.sAddress02,
	        		clsStringFunctions.filter(rs.getString(SMTablecompanyprofile.sAddress02)), 
	        		SMTablecompanyprofile.sAddress02Length, 
	        		"Line 2:", 
	        		"Second line of address.");
	        
	        // sAddress03
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablecompanyprofile.sAddress03,
	        		clsStringFunctions.filter(rs.getString(SMTablecompanyprofile.sAddress03)), 
	        		SMTablecompanyprofile.sAddress03Length, 
	        		"Line 3:", 
	        		"Third line of address.");
	        
	        // sAddress04
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablecompanyprofile.sAddress04,
	        		clsStringFunctions.filter(rs.getString(SMTablecompanyprofile.sAddress04)), 
	        		SMTablecompanyprofile.sAddress04Length, 
	        		"Line 4:", 
	        		"Fourth line of address.");
	        
	        // sCity
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablecompanyprofile.sCity,
	        		clsStringFunctions.filter(rs.getString(SMTablecompanyprofile.sCity)), 
	        		SMTablecompanyprofile.sCityLength, 
	        		"City:", 
	        		"Full name of city.");
	        
	        // sState
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablecompanyprofile.sState,
	        		clsStringFunctions.filter(rs.getString(SMTablecompanyprofile.sState)), 
	        		SMTablecompanyprofile.sStateLength, 
	        		"State:", 
	        		"Normally abbreviated.");
	        
	        // sZipCode
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablecompanyprofile.sZipCode,
	        		clsStringFunctions.filter(rs.getString(SMTablecompanyprofile.sZipCode)), 
	        		SMTablecompanyprofile.sZipCodeLength, 
	        		"Zip code:", 
	        		"Formatted without punctuation (e.g.: 999999999).");
	        
	        // sCountry
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablecompanyprofile.sCountry,
	        		clsStringFunctions.filter(rs.getString(SMTablecompanyprofile.sCountry)), 
	        		SMTablecompanyprofile.sCountryLength, 
	        		"Country:", 
	        		"Normally abbreviated - e.g., 'USA'.");
	        
	        // sContactName
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablecompanyprofile.sContactName,
	        		clsStringFunctions.filter(rs.getString(SMTablecompanyprofile.sContactName)), 
	        		SMTablecompanyprofile.sContactNameLength, 
	        		"Contact:", 
	        		"Name of primary company contact.");
	        
	        // sPhoneNumber
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablecompanyprofile.sPhoneNumber,
	        		clsStringFunctions.filter(rs.getString(SMTablecompanyprofile.sPhoneNumber)), 
	        		SMTablecompanyprofile.sPhoneNumberLength, 
	        		"Phone number:", 
	        		"Use any desired formatting.");	    	
	        
	        // sFaxNumber
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablecompanyprofile.sFaxNumber,
	        		clsStringFunctions.filter(rs.getString(SMTablecompanyprofile.sFaxNumber)), 
	        		SMTablecompanyprofile.sFaxNumberLength, 
	        		"FAX number:", 
	        		"Use any desired formatting.");
  
	        // dDatabaseVersion
	        sOutPut += "<TR><TD ALIGN=RIGHT><B>Actual database revision number:</B></TD>"
	        		+ "<TD ALIGN=RIGHT>" + rs.getString(SMTablecompanyprofile.iDatabaseVersion) + "</TD>"
	        		+ "<TD>Database revision number SHOULD be " + SMUpdateData.getDatabaseVersion() + ".</TD></TR>";
	        
	        rs.close();

		}catch (SQLException ex){
	    	System.out.println("Error [1488206119] in " + this.toString()+ " class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
		
		//********************
        sOutPut += "</TABLE>";
        sOutPut += "<BR>";
		sOutPut += "<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update " + sObjectName + "' STYLE='height: 0.24in'></P>";
		sOutPut += "</FORM>";
		pwOut.println(sOutPut);
		
	}
		
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
