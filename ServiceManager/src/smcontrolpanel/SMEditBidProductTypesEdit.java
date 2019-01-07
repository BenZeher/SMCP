package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTablebidproductamounts;
import SMDataDefinition.SMTablebidproducttypes;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;

public class SMEditBidProductTypesEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sObjectName = SMBidEntry.ParamObjectName + " Product Type";
	private static String sCalledClassName = "SMEditBidProductTypesAction";
	private String sDBID = "";
	private String sCompanyName = "";
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMEditBidProductTypes
		)
		){
			return;
		}
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sBidProductType = clsStringFunctions.filter(
				request.getParameter(SMEditBidProductTypesSelect.BIDPRODUCTTYPE_PARAM));

		String title = "";
		String subtitle = "";

		if(request.getParameter("SubmitEdit") != null){
			//User has chosen to edit:
			title = "Edit " + sObjectName + " " + sBidProductType;
			subtitle = "";
			out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

			//Print a link to the first page after login:
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

			Edit_Record(sBidProductType, out, sDBID, false);
		}
		if(request.getParameter("SubmitDelete") != null){
			//User has chosen to delete:
			title = "Delete " + sObjectName + ": " + sBidProductType;
			subtitle = "";
			out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

			//Print a link to the first page after login:
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

			if (request.getParameter("ConfirmDelete") == null){
				out.println ("You must check the 'confirming' check box to delete.");
			}
			else{
				if (Delete_Record(sBidProductType, out, sDBID) == false){
					out.println ("Error deleting product type: " + sBidProductType + ".");
				}
				else{
					out.println ("Successfully deleted product type: " + sBidProductType + ".");
				}
			}
		}
		if(request.getParameter("SubmitAdd") != null){

			String sNewCode = clsStringFunctions.filter(request.getParameter("New" + sObjectName));
			//User has chosen to add a new object:
			title = "Add " + sObjectName + ": " + sNewCode;
			subtitle = "";
			out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

			//Print a link to the first page after login:
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

			if (sNewCode == ""){
				out.println ("You chose to add a new " + sObjectName + ", but you did not enter a new " + sObjectName + " to add.");
			}
			else{
				Edit_Record(sNewCode, out, sDBID, true);
			}
		}

		out.println("</BODY></HTML>");
	}

	private void Edit_Record(
			String sBidProductType, 
			PrintWriter pwOut, 
			String sConf,
			boolean bAddNew){

		//first, add the record if it's an 'Add':
		if (bAddNew == true){
			if (Add_Record (sBidProductType, sConf, pwOut) == false){
				pwOut.println("ERROR - Could not add " + sBidProductType + ".<BR>");
				return;
			}
			pwOut.println("Successfully added " + sBidProductType + ".<BR>");
			return;
		}

		pwOut.println("<FORM NAME='MAINFORM' ACTION='" 
				+ SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel." + sCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		pwOut.println("<TABLE BORDER=12 CELLSPACING=2>");

		try{
			//Get the record to edit:
			String sSQL = "SELECT * FROM " + SMTablebidproducttypes.TableName
			+ " WHERE ("
			+ "(" + SMTablebidproducttypes.sProductType + " = '" + sBidProductType + "')"
			+ ")"
			;
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sConf);

			rs.next();
			//Display fields:
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTablebidproducttypes.lID
					+ "\" VALUE=\"" + Long.toString(rs.getLong(SMTablebidproducttypes.lID)) + "\">");

			//Product type:
			pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
					SMTablebidproducttypes.sProductType, 
					clsStringFunctions.filter(rs.getString(SMTablebidproducttypes.sProductType)), 
					SMTablebidproducttypes.sProductTypeLength, 
					"Product type:", 
			"The name of the product type.")
			);

			rs.close();
		}catch (SQLException ex){
			pwOut.println("<BR>Error reading product type information - " + ex.getMessage());
		}

		//********************
		pwOut.println("</TABLE><BR><P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update " + sObjectName 
				+ "' STYLE='height: 0.24in'></P></FORM>");
	}

	private boolean Delete_Record(
			String sBidProductType,
			PrintWriter pwOut,
			String sConf){

		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + ".Delete_Record")
		);

		if (conn == null){
			pwOut.println("Error getting connection to delete record.");
			System.out.println("Error getting connection to delete record.");
			return false;
		}

		//First get the ID of the record to be deleted:
		long lBidProductTypeID = 0;
		String sSQL = "SELECT * FROM " + SMTablebidproducttypes.TableName
		+ " WHERE ("
		+ "(" + SMTablebidproducttypes.sProductType + " = '" + sBidProductType + "')"
		+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sConf);
			if (rs.next()){
				lBidProductTypeID = rs.getLong(SMTablebidproducttypes.lID);
			}
			rs.close();
		} catch (SQLException e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			pwOut.println("Could not read ID of product type to be deleted with SQL: " 
					+ sSQL + " - " + e.getMessage() + ".");
			return false;
		}
		
		if (lBidProductTypeID == 0){
			pwOut.println("Could not read ID of product type to be deleted.");
			return false;
		}
		
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			pwOut.println("Error starting data transaction to delete product type.");
			return false;
		}

		String SQL = "DELETE FROM " + SMTablebidproductamounts.TableName
		+ " WHERE ("
		+ SMTablebidproductamounts.lBidProductTypeID + " = " + Long.toString(lBidProductTypeID)  
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch (SQLException ex){
			pwOut.println("Error deleting product type record with SQL: " + SQL 
					+ " - " + ex.getMessage());
			System.out.println("Error deleting product type record with SQL: " + SQL 
					+ " - " + ex.getMessage());
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			return false;
		}		

		SQL = "DELETE FROM " + SMTablebidproducttypes.TableName
		+ " WHERE ("
		+ SMTablebidproducttypes.lID + " = " + Long.toString(lBidProductTypeID) 
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch (SQLException ex){
			pwOut.println("Error deleting product type record with SQL: " + SQL 
					+ " - " + ex.getMessage());
			System.out.println("Error deleting product type record with SQL: " + SQL 
					+ " - " + ex.getMessage());
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			return false;
		}		

		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			pwOut.println("Could not commit data transaction to complete deletion.");
			return false;
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn);

		return true;
	}

	private boolean Add_Record(String sProductType, String sConf, PrintWriter pwOut){

		//First, make sure there isn't a product type with this name already:
		String sSQL = "SELECT * FROM " + SMTablebidproducttypes.TableName
		+  " WHERE ("
		+ "(" + SMTablebidproducttypes.sProductType + " = '" + sProductType + "')"
		+ ")"
		;

		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sConf);
			if (rs.next()){
				//This record already exists, so we can't add it:
				pwOut.println("The " + sObjectName + " '" + sProductType 
						+ "' already exists - it cannot be added.<BR>");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException ex){
			pwOut.println("Error checking for existing product type with SQL: " + sSQL + " - " + ex.getMessage());
			return false;
		}
		sSQL = "INSERT INTO " + SMTablebidproducttypes.TableName
		+ " ("
		+ SMTablebidproducttypes.sProductType
		+ ") VALUES ("
		+ "'" + clsDatabaseFunctions.FormatSQLStatement(sProductType) + "'"
		+ ")"
		;
		try {

			boolean bResult = clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sConf); 
			return bResult;
		}catch (SQLException ex){
			pwOut.println("Error inserting product type with SQL: "
					+ sSQL + " - " + ex.getMessage());
			System.out.println("Error inserting product type with SQL: "
					+ sSQL + " - " + ex.getMessage());
			return false;
		}
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
