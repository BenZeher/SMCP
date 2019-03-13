package smgl;

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
import SMDataDefinition.SMTablecostcenters;
import SMDataDefinition.SMTableglaccountgroups;
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableglaccountstructures;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smar.ARUtilities;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLEditAccountsEdit extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final String sObjectName = "GL Account";
	private static final String sCalledClassName = "GLEditAccountsUpdate";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.GLEditChartOfAccounts))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + 
	    				" " + (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		//Load the input class from the request object - if it's a 'resubmit', then this will contain
	    //all the values typed from the previous screen.  If it's a 'first time' edit, then this will only
	    //contain the gl acct
		GLAccount glacct = new GLAccount("");
		glacct.loadFromHTTPRequest(request);
	    //First process if it's a 'delete':
	    if(request.getParameter("SubmitDelete") != null){
		    if (request.getParameter("ConfirmDelete") == null){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLEditAccounts"
					+ "?" + GLAccount.Paramsacctid + "=" + glacct.getM_sacctid()
					+ "&Warning=You must check the 'confirming' check box to delete."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
		    }
		    if (glacct.getM_sacctid().compareToIgnoreCase("") == 0){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLEditAccounts"
						+ "?" + GLAccount.Paramsacctid + "=" + glacct.getM_sacctid()
					+ "&Warning=You must enter a GL acct to delete."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
		    }else{
			    if (!glacct.delete(glacct.getM_sacctid(), getServletContext(), sDBID)){
    				response.sendRedirect(
    						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLEditAccounts"
    						+ "?" + GLAccount.Paramsacctid + "=" + glacct.getM_sacctid()
    					+ "&Warning=Error deleting gl acct: " + glacct.getErrorMessageString()
    					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    				);
						return;
			    }else{
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLEditAccounts"
    					+ "?" + GLAccount.Paramsacctid + "=" + glacct.getM_sacctid()
    					+ "&Status=Successfully deleted GL Account " + glacct.getM_sacctid() + "."
    					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    				);
					return;
			    }
		    }
	    }
	    
		String title = "";
		String subtitle = "";		
		if(request.getParameter("SubmitAdd") != null){
			glacct.setM_sacctid("");
			glacct.setM_bNewRecord("1");
		}
		//If we are coming here for the first time to edit (i.e., it's not a re-submit), then we need to 
		//load the input class from the database if possible:
		if(request.getParameter("SubmitEdit") != null){
			if(!glacct.load(getServletContext(), sDBID)){
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLEditAccounts"
						+ "?" + GLAccount.Paramsacctid + "=" + glacct.getM_sacctid()
					+ "&Warning=Could not load gl acct " + glacct.getM_sacctid() + "."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
	    	}
		}
		
		//System.out.println("glacct.getsactive = " + glacct.getM_sactive());
		
		//In any other case, such as the possibility that this is a 'resubmit', we need to edit the customer:
    	title = "Edit " + sObjectName;
	    subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.GLEditChartOfAccounts) 
	    		+ "\">Summary</A><BR><BR>");

		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
	    if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    try {
			Edit_Record(glacct, out, sDBID, sUserFullName);
		} catch (Exception e) {
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + e.getMessage() + "</FONT></B><BR>");
		}
		
		out.println("</BODY></HTML>");
		return;
	}
	private void Edit_Record(
			GLAccount glacct, 
			PrintWriter pwOut, 
			String sDBID,
			String sUserFullName
			) throws Exception{
	    
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl." + sCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		if(glacct.getM_iNewRecord().compareToIgnoreCase("0") == 0){
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + GLAccount.Paramsacctid + "\" VALUE=\"" + glacct.getM_sacctid() + "\">");
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + GLAccount.ParamsAddingNewRecord + "\" VALUE=0>");
		}else{
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + GLAccount.ParamsAddingNewRecord + "\" VALUE=1>");
		}
	    pwOut.println("<TABLE BORDER=12 CELLSPACING=2>");

        //Acct ID:
	    if(glacct.getM_iNewRecord().compareToIgnoreCase("1") == 0){
	        pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
	        		GLAccount.Paramsacctid, 
	        		glacct.getM_sacctid().replace("\"", "&quot;"),  
	        		SMTableglaccounts.sAcctIDLength, 
	        		"Account number:", 
	        		"Up to " + SMTableglaccounts.sAcctIDLength + " characters.",
	        		"1.6"
	        	)
	        );
	    	
	    }else{
	    	pwOut.println("<TD ALIGN=RIGHT><B>Account number:</B></TD><TD>" 
	    			+ glacct.getM_sacctid().replace("\"", "&quot;") + "</TD><TD>&nbsp;</TD>");
	    }
	    
        //Formatted ID:
		pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
        		GLAccount.Paramsformattedacctid, 
        		glacct.getM_sformattedacctid().replace("\"", "&quot;"), 
        		SMTableglaccounts.sFormattedAcctLength, 
        		"Formatted account number:", 
        		"Including hyphens or any additional formatting, up to " 
        			+ SMTableglaccounts.sFormattedAcctLength + " characters.",
        		"3.2"
        	)
        );

		//Account structure:
		ArrayList<String>arrAccountStructureIDs = new ArrayList<String>(0);
		ArrayList<String>arrAccountStructureDescriptions = new ArrayList<String>(0);
		String SQL = "SELECT * FROM " + SMTableglaccountstructures.TableName
			+ " ORDER BY " + SMTableglaccountstructures.sstructureid
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sDBID, 
				"MySQL", 
				ServletUtilities.clsServletUtilities.getFullClassName(this.toString()) + ".Edit_Record - user: - " + sUserFullName);
			while (rs.next()){
				arrAccountStructureIDs.add(Long.toString(rs.getLong(SMTableglaccountstructures.lid)));
				arrAccountStructureDescriptions.add(rs.getString(SMTableglaccountstructures.sstructureid) + " - " + rs.getString(SMTableglaccountstructures.sdescription));
			}
			rs.close();
		} catch (SQLException e) {
			throw new Exception("Error [1528143735] loading GL Account Structures - " + e.getMessage());
		}
		pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
			GLAccount.Paramlaccountstructureid, 
			arrAccountStructureIDs, 
			glacct.getM_laccountstructureid(), 
			arrAccountStructureDescriptions, 
			"GL Account Structures", "")
		);
		
		//Account group:
		ArrayList<String>arrAccountGroupIDs = new ArrayList<String>(0);
		ArrayList<String>arrAccountGroupDescriptions = new ArrayList<String>(0);
		SQL = "SELECT * FROM " + SMTableglaccountgroups.TableName
			+ " ORDER BY " + SMTableglaccountgroups.sgroupcode
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sDBID, 
				"MySQL", 
				ServletUtilities.clsServletUtilities.getFullClassName(this.toString()) + ".Edit_Record - user: - " + sUserFullName);
			while (rs.next()){
				arrAccountGroupIDs.add(Long.toString(rs.getLong(SMTableglaccountgroups.lid)));
				arrAccountGroupDescriptions.add(rs.getString(SMTableglaccountgroups.sgroupcode) + " - " + rs.getString(SMTableglaccountgroups.sdescription));
			}
			rs.close();
		} catch (SQLException e) {
			throw new Exception("Error [1528143736] loading GL Account Groups - " + e.getMessage());
		}
		pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
			GLAccount.Paramlaccountgroupid, 
			arrAccountGroupIDs, 
			glacct.getM_laccountgroupid(), 
			arrAccountGroupDescriptions, 
			"GL Account Groups", "")
		);
		
        //Description:
		pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
        		GLAccount.Paramsdescription, 
        		glacct.getM_sdescription().replace("\"", "&quot;"), 
        		SMTableglaccounts.sDescLength, 
        		"Account description:", 
        		"Up to " + SMTableglaccounts.sDescLength + " characters.",
        		"3.2"
        	)
        );

        //Type:
		ArrayList<String> sValues = new ArrayList<String>(0);
		ArrayList<String> sDescriptions = new ArrayList<String>(0);
		sValues.add("B");
		sValues.add("I");
		sValues.add("R");
		sDescriptions.add("Balance sheet");
		sDescriptions.add("Income statement");
		sDescriptions.add("Retained earnings");
		
        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
        	GLAccount.Paramstype, 
        	sValues, 
        	glacct.getM_stype(), 
        	sDescriptions, 
        	"Account type:", 
        	"Only one account can be 'retained earnings' type"
        	)
        );
        
        
        //Type:
        ArrayList<String>sBalanceTypeValues = new ArrayList<String>(0);
        ArrayList<String>sBalanceTypeDescriptions = new ArrayList<String>(0);
        sBalanceTypeValues.clear();
        sBalanceTypeDescriptions.clear();
        sBalanceTypeValues.add(Integer.toString(SMTableglaccounts.NORMAL_BALANCE_TYPE_DEBIT));
        sBalanceTypeValues.add(Integer.toString(SMTableglaccounts.NORMAL_BALANCE_TYPE_CREDIT));
        sBalanceTypeDescriptions.add("Normally a DEBIT balance");
        sBalanceTypeDescriptions.add("Normally a CREDIT balance");
		
        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
        	GLAccount.Paraminormalbalancetype, 
        	sBalanceTypeValues, 
        	glacct.getsbinormalbalancetype(),
        	sBalanceTypeDescriptions, 
        	"Normal balance type:", 
        	"Accounts normally carry a DEBIT or CREDIT balance"
        	)
        );
        
        //Cost Center
        sValues.clear();
        sDescriptions.clear();
        sValues.add("0");
        sDescriptions.add("NONE");
        try{
			//Get the record to edit:
	        String sSQL = "SELECT " + "*" + " FROM " + SMTablecostcenters.TableName;
	        ResultSet rsCostCenters = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	sDBID,
	        	"MySQL",
	        	this.toString() + ".Edit_Record - User: " + sUserFullName
	        	);
	        
	        while (rsCostCenters.next()){
	        	sValues.add((String) rsCostCenters.getString(SMTablecostcenters.lid).trim());
	        	String sInactive = "";
	        	if(rsCostCenters.getString(SMTablecostcenters.iactive).trim().compareToIgnoreCase("0") == 0){
	        		sInactive = " (Inactive)";        		
	        	}
	        	sDescriptions.add((String) rsCostCenters.getString(SMTablecostcenters.scostcentername).trim() + sInactive);
			}
	        rsCostCenters.close();
		}catch (SQLException ex){
			pwOut.println("<BR><FONT COLOR=RED><B>Error [1450321765] reading GL accounts - " + ex.getMessage() + ".</FONT></B><BR>");
		}
		
        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
        	GLAccount.Paramicostcenterid, 
        	sValues, 
        	glacct.getM_scostcenterid(), 
        	sDescriptions, 
        	"Cost Center:", 
        	"Select cost center this GL Account will report under."
        	)
        );
        
        //Active?
        String sActive = "false";
        if (glacct.getM_sactive().compareToIgnoreCase("1") == 0){
        	sActive = "true";
        }
		pwOut.println(
				ARUtilities.Create_Edit_Form_Checkbox_Row(
						GLAccount.Paramlactive, 
						sActive, 
						"Active?:", 
						"Is this account active?"
				)
		);
        
        //Allow as an expense account on PO lines?
        String sAllowOnPOLines = "false";
        if (glacct.getM_iallowpoasexpense().compareToIgnoreCase("1") == 0){
        	sAllowOnPOLines = "true";
        }
		pwOut.println(
				ARUtilities.Create_Edit_Form_Checkbox_Row(
						GLAccount.Paramiallowaspoexpense, 
						sAllowOnPOLines, 
						"Allow on PO lines?:", 
						"Do you want to allow users to pick this as an expense account when ordering items in the PO system?"
				)
		);
		
		//Annual budget:
		pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
        		GLAccount.Parambdannualbudget, 
        		glacct.getsbdannualbudget().replace("\"", "&quot;"), 
        		13, 
        		"Annual budget:", 
        		"",
        		"7"
        	)
        );
		
        pwOut.println("</TABLE>");
        pwOut.println("<BR>");
        pwOut.println("<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update " + sObjectName + "' STYLE='height: 0.24in'></P>");
        pwOut.println("</FORM>");
		
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
