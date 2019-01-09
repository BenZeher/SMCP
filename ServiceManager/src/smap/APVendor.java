package smap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import SMClasses.SMBatchStatuses;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableapbatches;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableapvendorremittolocations;
import SMDataDefinition.SMTableapvendorstatistics;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicvendoritems;
import SMDataDefinition.SMTableicvendors;
import SMDataDefinition.SMTablesecuritygroupfunctions;
import SMDataDefinition.SMTablesecurityusergroups;
import SMDataDefinition.SMTablesmoptions;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APVendor extends clsMasterEntry{

	public static final String ParamObjectName = "Vendor";
	private static String EMPTY_DATETIME_STRING = "00/00/0000 00:00 AM";
	
	//Particular to the specific class
	public static final String Paramsvendoracct = "svendoracct";
	public static final String Paramsname = "sname";
	public static final String Paramsaddressline1 = "saddressline1";
	public static final String Paramsaddressline2 = "saddressline2";
	public static final String Paramsaddressline3 = "saddressline3";
	public static final String Paramsaddressline4 = "saddressline4";
	public static final String Paramscity = "scity";
	public static final String Paramsstate = "sstate";
	public static final String Paramspostalcode = "spostalcode";
	public static final String Paramscountry = "scountry";
	public static final String Paramscontactname = "scontactname";
	public static final String Paramsphonenumber = "sphonenumber";
	public static final String Paramsfaxnumber = "sfaxnumber";
	public static final String Paramsterms = "sterms";
	public static final String Paramscompanyaccountcode = "scompanyaccountcode";
	public static final String Paramswebaddress = "swebaddress";
	public static final String Paramdatlastmaintained = "datlastmaintained";
	public static final String Paramslasteditedby = "slasteditedby";
	public static final String Paramiactive = "iactive";
	public static final String Paramipoconfirmationrequired = "ipoconfirmationrequired";
	public static final String Paramiapaccountset = "iapaccountset";
	public static final String Paramibankcode = "ibankcode";
	public static final String Paramsdefaultdistributioncode = "sdefaultdistributioncode";
	public static final String Paramsdefaultexpenseacct = "sdefaultexpenseacct";
	public static final String Paramsdefaultinvoicelinedescription = "sdefaultinvoicelinedescription";
	public static final String Paramsgdoclink = "sgdoclink";
	public static final String Paramsprimaryremittocode = "sprimaryremittocode";
	public static final String Paramstaxidentifyingnumber = "staxidentifyingnumber";
	public static final String Paramitaxreportingtype = "itaxreportingtype";
	public static final String Parami1099CPRSid = "i1099CPRSid";
	public static final String Paramitaxidnumbertype = "itaxidnumbertype";
	public static final String Paramigenerateseparatepaymentsforeachinvoice = "igenerateseparatepaymentsforeachinvoice";
	public static final String Paramivendorgroupid = "ivendorgroupid";
	
	////////////////////
	
	private String m_svendoracct;
	private String m_sname;
	private String m_saddressline1;
	private String m_saddressline2;
	private String m_saddressline3;
	private String m_saddressline4;
	private String m_scity;
	private String m_sstate;
	private String m_spostalcode;
	private String m_scountry;
	private String m_scontactname;
	private String m_sphonenumber;
	private String m_sfaxnumber;
	private String m_sterms;
	private String m_scompanyaccountcode;
	private String m_swebaddress;
	private String m_sdatlastmaintained;
	private String m_slasteditedby;
	private String m_sactive;
	private String m_spoconfirmationrequired;
	private String m_iapaccountset;
	private String m_ibankcode;
	private String m_sdefaultdistributioncode;
	private String m_sdefaultexpenseacct;
	private String m_sdefaultinvoicelinedescription;
	private String m_sgdoclink;
	private String m_sprimaryremittocode;
	private String m_staxidentifyingnumber;
	private String m_itaxreportingtype;
	private String m_i1099CPRSid;
	private String m_itaxidnumbertype;
	private String m_igenerateseparatepaymentsforeachinvoice;
	private String m_ivendorgroupid;
	
	public APVendor() {
		super();
		initBidVariables();
        }

	APVendor (HttpServletRequest req){
		super(req);
		initBidVariables();
		m_svendoracct = clsManageRequestParameters.get_Request_Parameter(APVendor.Paramsvendoracct, req).trim().toUpperCase();
		m_sname = clsManageRequestParameters.get_Request_Parameter(APVendor.Paramsname, req).trim();
		m_saddressline1 = clsManageRequestParameters.get_Request_Parameter(APVendor.Paramsaddressline1, req).trim();
		m_saddressline2 = clsManageRequestParameters.get_Request_Parameter(APVendor.Paramsaddressline2, req).trim();
		m_saddressline3 = clsManageRequestParameters.get_Request_Parameter(APVendor.Paramsaddressline3, req).trim();
		m_saddressline4 = clsManageRequestParameters.get_Request_Parameter(APVendor.Paramsaddressline4, req).trim();
		m_scity = clsManageRequestParameters.get_Request_Parameter(APVendor.Paramscity, req).trim();
		m_sstate = clsManageRequestParameters.get_Request_Parameter(APVendor.Paramsstate, req).trim();
		m_spostalcode = clsManageRequestParameters.get_Request_Parameter(APVendor.Paramspostalcode, req).trim();
		m_scountry = clsManageRequestParameters.get_Request_Parameter(APVendor.Paramscountry, req).trim();
		m_scontactname = clsManageRequestParameters.get_Request_Parameter(APVendor.Paramscontactname, req).trim();
		m_sphonenumber = clsManageRequestParameters.get_Request_Parameter(APVendor.Paramsphonenumber, req).trim();
		m_sfaxnumber = clsManageRequestParameters.get_Request_Parameter(APVendor.Paramsfaxnumber, req).trim();
		m_sterms = clsManageRequestParameters.get_Request_Parameter(Paramsterms, req).trim();
		m_scompanyaccountcode = clsManageRequestParameters.get_Request_Parameter(Paramscompanyaccountcode, req).trim();
		m_swebaddress = clsManageRequestParameters.get_Request_Parameter(Paramswebaddress, req).trim();
		if(clsManageRequestParameters.get_Request_Parameter(
				Paramdatlastmaintained, req).trim().compareToIgnoreCase("") != 0){
			m_sdatlastmaintained = clsManageRequestParameters.get_Request_Parameter(Paramdatlastmaintained, req).trim();
		}
		m_slasteditedby = clsManageRequestParameters.get_Request_Parameter(Paramslasteditedby, req).trim();
		if(req.getParameter(Paramiactive) == null){
			m_sactive = "0";
		}else{
			if(req.getParameter(Paramiactive).compareToIgnoreCase("0") ==0){
				m_sactive = "0";
			}else{
				m_sactive = "1";
			}
		}
		if(req.getParameter(Paramipoconfirmationrequired) == null){
			m_spoconfirmationrequired = "0";
		}else{
			if(req.getParameter(Paramipoconfirmationrequired).compareToIgnoreCase("0") ==0){
				m_spoconfirmationrequired = "0";
			}else{
				m_spoconfirmationrequired = "1";
			}
		}	
		if(req.getParameter(Paramiapaccountset) == null){
			m_iapaccountset = "";
		}else{
			m_iapaccountset = clsManageRequestParameters.get_Request_Parameter(Paramiapaccountset, req).trim();
		}	
		if(req.getParameter(Paramibankcode) == null){
			m_ibankcode = "";
		}else{
			m_ibankcode = clsManageRequestParameters.get_Request_Parameter(Paramibankcode, req).trim();
		}		
		m_sdefaultdistributioncode = clsManageRequestParameters.get_Request_Parameter(Paramsdefaultdistributioncode, req).trim();
		m_sdefaultexpenseacct = clsManageRequestParameters.get_Request_Parameter(Paramsdefaultexpenseacct, req).trim();
		m_sdefaultinvoicelinedescription = clsManageRequestParameters.get_Request_Parameter(Paramsdefaultinvoicelinedescription, req).trim();
		m_sgdoclink = clsManageRequestParameters.get_Request_Parameter(Paramsgdoclink, req).trim();
		m_sprimaryremittocode = clsManageRequestParameters.get_Request_Parameter(Paramsprimaryremittocode, req).trim();
		m_staxidentifyingnumber = clsManageRequestParameters.get_Request_Parameter(Paramstaxidentifyingnumber, req).trim();
		m_itaxreportingtype = clsManageRequestParameters.get_Request_Parameter(Paramitaxreportingtype, req).trim();
		m_i1099CPRSid = clsManageRequestParameters.get_Request_Parameter(Parami1099CPRSid, req).trim();
		m_itaxidnumbertype = clsManageRequestParameters.get_Request_Parameter(Paramitaxidnumbertype, req).trim();
		if(req.getParameter(Paramigenerateseparatepaymentsforeachinvoice) == null){
			m_igenerateseparatepaymentsforeachinvoice = "0";
		}else{
			if(req.getParameter(Paramigenerateseparatepaymentsforeachinvoice).compareToIgnoreCase("0") == 0){
				m_igenerateseparatepaymentsforeachinvoice = "0";
			}else{
				m_igenerateseparatepaymentsforeachinvoice = "1";
			}
		}	
		m_ivendorgroupid = clsManageRequestParameters.get_Request_Parameter(Paramivendorgroupid, req).trim();
	}
    public boolean load (ServletContext context, String sConf, String sUserID , String sUserFullname){
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sConf, 
    			"MySQL", 
    			this.toString() + " - user: " 
    			+ sUserID
    			+ " - "
    			+ sUserFullname
    			);
    	
    	if (conn == null){
    		super.addErrorMessage("Error opening data connection.");
    		return false;
    	}
    	
    	boolean bResult = load (conn);
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547059483]");
    	return bResult;
    	
    }
    public boolean load (Connection conn){
    	return load (m_svendoracct, conn);
    }
    public boolean load (String sVendorAcct, Connection conn){

    	sVendorAcct = sVendorAcct.trim();
    	if (sVendorAcct.compareToIgnoreCase("") == 0){
    		super.addErrorMessage("Vendor account cannot be blank.");
    		return false;
    	}
    	if (sVendorAcct.length() > SMTableicvendors.svendoracctLength){
    		super.addErrorMessage("Vendor account (" + sVendorAcct 
    				+ ") is too long.");
    		return false;
    	}
    	
		String SQL = " SELECT * FROM " + SMTableicvendors.TableName
			+ " WHERE ("
				+ SMTableicvendors.svendoracct + " = '" + sVendorAcct + "'"
			+ ")";
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_svendoracct = rs.getString(SMTableicvendors.svendoracct);
				m_sname = rs.getString(SMTableicvendors.sname);
				m_saddressline1 = rs.getString(SMTableicvendors.saddressline1);
				m_saddressline2 = rs.getString(SMTableicvendors.saddressline2);
				m_saddressline3 = rs.getString(SMTableicvendors.saddressline3);
				m_saddressline4 = rs.getString(SMTableicvendors.saddressline4);
				m_scity = rs.getString(SMTableicvendors.scity);
				m_sstate = rs.getString(SMTableicvendors.sstate);
				m_spostalcode = rs.getString(SMTableicvendors.spostalcode);
				m_scountry = rs.getString(SMTableicvendors.scountry);
				m_scontactname = rs.getString(SMTableicvendors.scontactname);
				m_sphonenumber = rs.getString(SMTableicvendors.sphonenumber);
				m_sfaxnumber = rs.getString(SMTableicvendors.sfaxnumber);
				m_sterms = rs.getString(SMTableicvendors.sterms);
				m_scompanyaccountcode = rs.getString(SMTableicvendors.scompanyacctcode);
				m_swebaddress = rs.getString(SMTableicvendors.swebaddress);
				m_sdatlastmaintained = clsDateAndTimeConversions.resultsetDateTimeStringToString(
						rs.getString(SMTableicvendors.datlastmaintained));
				m_slasteditedby = rs.getString(SMTableicvendors.slasteditedbyfullname);
				m_sactive = Integer.toString(rs.getInt(SMTableicvendors.iactive));
				m_spoconfirmationrequired = Integer.toString(rs.getInt(SMTableicvendors.ipoconfirmationrequired));
				m_iapaccountset = Integer.toString(rs.getInt(SMTableicvendors.iapaccountset));
				m_ibankcode = Integer.toString(rs.getInt(SMTableicvendors.ibankcode));
				m_sdefaultdistributioncode = rs.getString(SMTableicvendors.sdefaultdistributioncode);
				m_sdefaultexpenseacct = rs.getString(SMTableicvendors.sdefaultexpenseacct);
				m_sdefaultinvoicelinedescription = rs.getString(SMTableicvendors.sdefaultinvoicelinedesc);
				m_sgdoclink = rs.getString(SMTableicvendors.sgdoclink);
				m_sprimaryremittocode = rs.getString(SMTableicvendors.sprimaryremittocode);
				m_staxidentifyingnumber = rs.getString(SMTableicvendors.staxidentifyingnumber);
				m_itaxreportingtype = Integer.toString(rs.getInt(SMTableicvendors.itaxreportingtype));
				m_i1099CPRSid = Integer.toString(rs.getInt(SMTableicvendors.i1099CPRSid));
				m_itaxidnumbertype = Integer.toString(rs.getInt(SMTableicvendors.itaxidnumbertype));
				m_igenerateseparatepaymentsforeachinvoice = Integer.toString(rs.getInt(SMTableicvendors.igenerateseparatepaymentsforeachinvoice));
				m_ivendorgroupid = Integer.toString(rs.getInt(SMTableicvendors.ivendorgroupid));
				rs.close();
			} else {
				super.addErrorMessage("No " + ParamObjectName + " found for : '" + sVendorAcct
						+ "'");
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error reading " + ParamObjectName + " for : '" + sVendorAcct
					+ "' - " + e.getMessage());
			return false;
		}
		return true;
    }
    
    public boolean save_without_data_transaction (
    		ServletContext context, 
    		String sConf, 
    		String sUser,
    		String sUserID,
    		String sUserFullName,
    		String sCompany,
    		boolean bSavingNewVendor){
    	
       	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sConf, 
    			"MySQL", 
    			this.toString() + " - user: " 
    			+ sUserID
    			+ " - "
    			+ sUserFullName
    			);
    	
    	if (conn == null){
    		super.addErrorMessage("Error opening data connection.");
    		return false;
    	}
    	
    	boolean bResult = save_without_data_transaction (conn, sUserFullName, sUserID, sCompany, bSavingNewVendor);
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547059484]");
    	return bResult;	
    	
    }
    public boolean save_without_data_transaction (Connection conn, String sUserFullName, String sUserID, String sCompany, boolean bSavingNewVendor){

    	if (!validate_entry_fields(conn)){
    		return false;
    	}
    	
    	String SQL = "";

    	//First check to see if this vendor already exists:
    	boolean bNewVendor = true;
    	SQL = "SELECT"
    		+ " " + SMTableicvendors.svendoracct
    		+ " FROM " + SMTableicvendors.TableName
    		+ " WHERE ("
    			+ "(" + SMTableicvendors.svendoracct + " = '" + getsvendoracct() + "')"
    		+ ")"
    		;
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				bNewVendor = false;
			}
			rs.close();
		} catch (SQLException e) {
    	    super.addErrorMessage("Error checking for existing " + ParamObjectName + ": " + e.getMessage());
    	    return false;
		}
    	
    	//IF the vendor already exists, AND the user is trying to add a NEW vendor, throw an exception:
    	if (!bNewVendor && bSavingNewVendor){
    	    super.addErrorMessage("You tried to add a new vendor with account code '" + getsvendoracct() + "', but that account code is already in use.");
    	    return false;
    	}
		SQL = "INSERT INTO " + SMTableicvendors.TableName + " ("
			+ SMTableicvendors.saddressline1
			+ ", " + SMTableicvendors.saddressline2
			+ ", " + SMTableicvendors.saddressline3
			+ ", " + SMTableicvendors.saddressline4
			+ ", " + SMTableicvendors.scity
			+ ", " + SMTableicvendors.scontactname
			+ ", " + SMTableicvendors.scountry
			+ ", " + SMTableicvendors.sname
			+ ", " + SMTableicvendors.sphonenumber
			+ ", " + SMTableicvendors.sfaxnumber
			+ ", " + SMTableicvendors.spostalcode
			+ ", " + SMTableicvendors.sstate
			+ ", " + SMTableicvendors.svendoracct
			+ ", " + SMTableicvendors.sterms
			+ ", " + SMTableicvendors.scompanyacctcode
			+ ", " + SMTableicvendors.swebaddress
			+ ", " + SMTableicvendors.datlastmaintained
			+ ", " + SMTableicvendors.slasteditedbyfullname
			+ ", " + SMTableicvendors.llasteditedbyuserid
			+ ", " + SMTableicvendors.iactive
			+ ", " + SMTableicvendors.igenerateseparatepaymentsforeachinvoice
			+ ", " + SMTableicvendors.ipoconfirmationrequired
			+ ", " + SMTableicvendors.iapaccountset
			+ ", " + SMTableicvendors.ibankcode
			+ ", " + SMTableicvendors.sdefaultdistributioncode
			+ ", " + SMTableicvendors.sdefaultexpenseacct
			+ ", " + SMTableicvendors.sdefaultinvoicelinedesc
			+ ", " + SMTableicvendors.sgdoclink
			+ ", " + SMTableicvendors.sprimaryremittocode
			+ ", " + SMTableicvendors.staxidentifyingnumber
			+ ", " + SMTableicvendors.itaxreportingtype
			+ ", " + SMTableicvendors.i1099CPRSid
			+ ", " + SMTableicvendors.itaxidnumbertype
			+ ", " + SMTableicvendors.ivendorgroupid
			
			+ ") VALUES ("
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(m_saddressline1.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_saddressline2.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_saddressline3.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_saddressline4.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_scity.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_scontactname.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_scountry.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sname.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sphonenumber.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sfaxnumber.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_spostalcode.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sstate.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_svendoracct.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sterms.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_scompanyaccountcode.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_swebaddress.trim()) + "'"
			+ ", NOW()"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
			+ ", " + sUserID
			+ ", " + m_sactive
			+ ", " + m_igenerateseparatepaymentsforeachinvoice
			+ ", " + m_spoconfirmationrequired
			+ ", " + m_iapaccountset
			+ ", " + m_ibankcode
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sdefaultdistributioncode.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sdefaultexpenseacct.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sdefaultinvoicelinedescription.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdoclink.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sprimaryremittocode.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_staxidentifyingnumber.trim()) + "'"
			+ ", " + m_itaxreportingtype
			+ ", " + m_i1099CPRSid
			+ ", " + m_itaxidnumbertype
			+ ", " + m_ivendorgroupid
			+ ")"
			+ " ON DUPLICATE KEY UPDATE "
			+ SMTableicvendors.saddressline1
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_saddressline1.trim()) + "'"
			+ ", " + SMTableicvendors.saddressline2
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_saddressline2.trim()) + "'"
			+ ", " + SMTableicvendors.saddressline3
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_saddressline3.trim()) + "'"
			+ ", " + SMTableicvendors.saddressline4
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_saddressline4.trim()) + "'"
			+ ", " + SMTableicvendors.scity
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_scity.trim()) + "'"
			+ ", " + SMTableicvendors.scontactname
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_scontactname.trim()) + "'"
			+ ", " + SMTableicvendors.scountry
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_scountry.trim()) + "'"
			+ ", " + SMTableicvendors.sname
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sname.trim()) + "'"
			+ ", " + SMTableicvendors.sphonenumber
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sphonenumber.trim()) + "'"
			+ ", " + SMTableicvendors.sfaxnumber
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sfaxnumber.trim()) + "'"
			+ ", " + SMTableicvendors.spostalcode
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_spostalcode.trim()) + "'"
			+ ", " + SMTableicvendors.sstate
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sstate.trim()) + "'"
			+ ", " + SMTableicvendors.sterms
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sterms.trim()) + "'"
			+ ", " + SMTableicvendors.scompanyacctcode
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_scompanyaccountcode.trim()) + "'"
			+ ", " + SMTableicvendors.swebaddress
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_swebaddress.trim()) + "'"
			+ ", " + SMTableicvendors.datlastmaintained
				+ " = NOW()"
			+ ", " + SMTableicvendors.slasteditedbyfullname
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
			+ ", " + SMTableicvendors.llasteditedbyuserid
				+ " = " + sUserID
			+ ", " + SMTableicvendors.iactive
				+ " = " + m_sactive
			+ ", " + SMTableicvendors.igenerateseparatepaymentsforeachinvoice
				+ " = " + m_igenerateseparatepaymentsforeachinvoice
			+ ", " + SMTableicvendors.ipoconfirmationrequired
				+ " = " + m_spoconfirmationrequired
			+ ", " + SMTableicvendors.iapaccountset
				+ " = " + m_iapaccountset
			+ ", " + SMTableicvendors.ibankcode
				+ " = " + m_ibankcode
			+ ", " + SMTableicvendors.sdefaultdistributioncode
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sdefaultdistributioncode.trim()) + "'"
			+ ", " + SMTableicvendors.sdefaultexpenseacct
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sdefaultexpenseacct.trim()) + "'"
			+ ", " + SMTableicvendors.sdefaultinvoicelinedesc
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sdefaultinvoicelinedescription.trim()) + "'"
			+ ", " + SMTableicvendors.sgdoclink
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdoclink.trim()) + "'"
			+ ", " + SMTableicvendors.sprimaryremittocode
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sprimaryremittocode.trim()) + "'"
			+ ", " + SMTableicvendors.staxidentifyingnumber
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_staxidentifyingnumber.trim()) + "'"
			+ ", " + SMTableicvendors.itaxreportingtype
				+ " = " + m_itaxreportingtype
			+ ", " + SMTableicvendors.i1099CPRSid
				+ " = " + m_i1099CPRSid
			+ ", " + SMTableicvendors.itaxidnumbertype
				+ " = " + m_itaxidnumbertype
			+ ", " + SMTableicvendors.ivendorgroupid
				+ " = " + m_ivendorgroupid
			;
		//System.out.println("[1393267108] SQL = " + SQL);

    	try{
	    	if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
	    		//System.out.println(this.toString() + "Could not insert/update " + ParamObjectName + ".<BR>");
	    		super.addErrorMessage("Could not insert/update " + ParamObjectName + " with SQL: " + SQL);
	    		return false;
	    	}
    	}catch(SQLException ex){
    	    super.addErrorMessage("Error inserting " + ParamObjectName + ": " + ex.getMessage());
    	    return false;
    	}

    	//Now, re-read the vendor to get the last maintained date and last edit user:
		try {
			SQL = "SELECT"
				+ " " + SMTableicvendors.datlastmaintained
				+ ", " + SMTableicvendors.slasteditedbyfullname
				+ ", " + SMTableicvendors.llasteditedbyuserid
				+ " FROM " + SMTableicvendors.TableName
				+ " WHERE ("
					+ "(" + SMTableicvendors.svendoracct + " = '" + getsvendoracct() + "')"
				+ ")"
			;
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rs.next()){
				//Don't choke on this
				super.addErrorMessage("Could not read vendor to get last maintained information.");
				rs.close();
				return false;
			}else{
				m_sdatlastmaintained = clsDateAndTimeConversions.resultsetDateTimeStringToString(rs.getString(SMTableicvendors.datlastmaintained));
				m_slasteditedby = rs.getString(SMTableicvendors.slasteditedbyfullname);
				rs.close();
			}
		} catch (SQLException e1) {
			super.addErrorMessage("Could not read vendor to get last maintained information.");
			return false;
		}

		if (bNewVendor){
    		boolean bEmailSucceeded = true;
    		String sErrorMessage = "";
    		try {
				mailNewVendorNotification(sUserFullName, sCompany, conn);
			} catch (Exception e) {
				bEmailSucceeded = false;
				sErrorMessage = e.getMessage();
			}
    		SMLogEntry log = new SMLogEntry(conn);
    		if (bEmailSucceeded){
    			log.writeEntry(
    					sUserID, 
    					SMLogEntry.LOG_OPERATION_SENDNEWVENDOREMAIL, 
    					"EMAIL SENT", 
    					"Successfully added vendor '" + m_svendoracct.trim() + "'",
    					"[1385580402]");
    		}else{
    			log.writeEntry(
    					sUserID, 
    					SMLogEntry.LOG_OPERATION_SENDNEWVENDOREMAIL, 
    					"EMAIL NOT SENT", 
    					"Vendor '" + m_svendoracct.trim() + "' - email error: " + sErrorMessage,
    					"[1385580403]");
    		}
    	}
    	return true;
    }
	private void mailNewVendorNotification(String sUserFullName, String sCompany, Connection conn) throws Exception{
		//Notify any specified users that a new vendor was built:
		String SQL = "SELECT " + SMTableusers.semail 
			+ " FROM " 
				+ SMTableusers.TableName + ", "
				+ SMTablesecuritygroupfunctions.TableName + ", "
				+ SMTablesecurityusergroups.TableName
			+ " WHERE ("
			//TODO:
				+ "(" + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.ifunctionid
					+ " = " + SMSystemFunctions.APReceiveNewVendorNotification + ")"
				+ " AND (" + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.sGroupName
					+ " = " + SMTablesecurityusergroups.TableName + "." + SMTablesecurityusergroups.sSecurityGroupName + ")"
				+ " AND (" + SMTableusers.TableName + "." + SMTableusers.lid + " = "
					+ SMTablesecurityusergroups.TableName + "." + SMTablesecurityusergroups.luserid + ")"
			+ ")"
			;
		String sCurrentTime = "";
		String sSMTPServer = "";
		String sSMTPPort = "";
		String sSMTPSourceServerName = "";
		String sUserName = ""; 
		String sPassword = ""; 
		String sReplyToAddress = "";
		boolean bUsesSMTPAuthentication = false;
		ArrayList<String>arrNotifyEmails = new ArrayList<String>(0);
		try{
			ResultSet rsNotify = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rsNotify.next()){
				arrNotifyEmails.add(rsNotify.getString(SMTableusers.semail));
			}
			rsNotify.close();
			//If there is no one to notify, just exit out:
			if(arrNotifyEmails.size() == 0){
				return;
			}
			
			//Now we need to send an email to notify that a new customer was added:
			
			SQL = "SELECT " + SMTablesmoptions.TableName + ".*"
			+ ", DATE_FORMAT(NOW(),'%c/%e/%Y %h:%i:%s %p')"
				+ " AS CURRENTTIME FROM " 
				+ SMTablesmoptions.TableName;
			//System.out.println("In " + this.toString() + " current time SQL = " + SQL);
			ResultSet rsOptions = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rsOptions.next()){
				sCurrentTime = rsOptions.getString("CURRENTTIME");
				sSMTPServer = rsOptions.getString(SMTablesmoptions.ssmtpserver).trim();
				sSMTPPort = rsOptions.getString(SMTablesmoptions.ssmtpport).trim();
				sSMTPSourceServerName = rsOptions.getString(SMTablesmoptions.ssmtpsourceservername).trim();
				sUserName = rsOptions.getString(SMTablesmoptions.ssmtpusername).trim();
				sPassword = rsOptions.getString(SMTablesmoptions.ssmtppassword).trim(); 
				sReplyToAddress = rsOptions.getString(SMTablesmoptions.ssmtpreplytoname).trim();
				bUsesSMTPAuthentication = (rsOptions.getInt(SMTablesmoptions.iusesauthentication) == 1);
				rsOptions.close();
			}else{
				rsOptions.close();
				throw new Exception("Error reading smoptions to get email information. [1385579921]");
			}
		}catch(SQLException e){
			//System.out.println(" In " + this.toString() + " error sending email for new customer notification.");
			throw new Exception("Error getting email information [1385579992] - " + e.getMessage());
		}

		//Now construct the email:
		String sActive = "YES";
		if (getsactive().compareToIgnoreCase("0") == 0){
			sActive = "NO";
		}
		String sRequireConfirmation = "YES";
		if (getspoconfirmationrequired().compareToIgnoreCase("0") == 0){
			sRequireConfirmation = "NO";
		}
		String sBody = "Vendor '" + getsvendoracct() + "' was added " + sCurrentTime
			+ " by " + sUserFullName 
			+ " in company " + sCompany
			+ "\n\n"
			+ "NAME: " + getsname() + "\n"
			+ "ADDRESS 1: " + getsaddressline1() + "\n"
			+ "ADDRESS 2: " + getsaddressline2() + "\n"
			+ "ADDRESS 3: " + getsaddressline3() + "\n"
			+ "ADDRESS 4: " + getsaddressline4() + "\n"
			+ "CITY: " + getscity() + "\n"
			+ "STATE: " + getsstate() + "\n"
			+ "POSTAL CODE: " + getspostalcode() + "\n"
			+ "COUNTRY: " + getscountry() + "\n"
			+ "CONTACT NAME: " + getscontactname() + "\n"
			+ "PHONE: " + getsphonenumber() + "\n"
			+ "TERMS: " + getsterms() + "\n"
			+ "COMPANY ACCOUNT CODE: " + getscompanyaccountcode() + "\n"
			+ "WEB ADDRESS: " + getswebaddress() + "\n"
			+ "ACTIVE: " + sActive + "\n"
			+ "CONFIRMATION REQUIRED?: " + sRequireConfirmation + "\n"
			;

			int iSMTPPort;
			try {
				iSMTPPort = Integer.parseInt(sSMTPPort);
			} catch (NumberFormatException e) {
				throw new Exception("Error parsing email port '" + sSMTPPort + "' [1385579993] - " + e.getMessage());
			}
			
			try {
				SMUtilities.sendEmail(
						sSMTPServer, 
						sUserName, 
						sPassword, 
						sReplyToAddress,
						Integer.toString(iSMTPPort), 
						"New Vendor " + getsvendoracct(),
						sBody, 
						"SMIC@" + sSMTPSourceServerName,
						sSMTPSourceServerName, 
						arrNotifyEmails, 
						bUsesSMTPAuthentication,
						false
				);
			} catch (Exception e) {
				throw new Exception("Error sending email [1385579994] " + e.getMessage());
			}
			return;
	}
    public void delete (ServletContext context, String sConf, String sUser) throws Exception{
    	
    	Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
					context, 
					sConf, 
					"MySQL", 
					this.toString() + " - user: " + sUser
					);
		} catch (Exception e) {
			throw new Exception("Error [1508347460] getting data connection - " + e.getMessage());
		}
    	
    	try {
			delete (conn);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547059482]");
    	return;
    }
    public void delete (Connection conn) throws Exception{
    	
    	/*
    	The program checks for OPEN (entered or partially complete) PO's for that vendor - if it finds any, it prevents the 'delete' and notifies you.

		Then it checks for OPEN transactions (invoices/credit notes/debit notes with a 'current amt' left on them) for that vendor.  If it finds any, it prevents the 'delete' and notifies you.
		
		Then it checks for UNPOSTED BATCH ENTRIES (invoices, credit notes, debit notes, payments, pre-pays, misc payments 'apply-tos') for that vendor - if it finds any, it prevents the delete and notifies you.
		
		If it passes all those checks, then it goes ahead and deletes:
		
		1) The 'vendor items' (in inventory) for that vendor.
		2) The 'remit-to locations' built for that vendor
		3) The statistics for that vendor
		4) The vendor itself.
		
		It does NOT delete:
		1) Any historical transactions for that vendor
		2) Any completed or deleted PO's for that vendor
		3) Any posted batch entries for that vendor
		
		Transactions, PO's, and Batch entries just get cleared using the functions particular to them.
    	
    	*/
    	
    	//Validate deletions - if there are any pending PO's for this vendor, we can't delete it:
    	String SQL = "SELECT"
    		+ " " + SMTableicpoheaders.svendor
    		+ " FROM " + SMTableicpoheaders.TableName
    		+ " WHERE ("
    			+ "(" + SMTableicpoheaders.svendor + " = '" + m_svendoracct + "')"
    			+ " AND (" 
    				+ "(" + SMTableicpoheaders.lstatus + " != " + Long.toString(SMTableicpoheaders.STATUS_ENTERED) + ")" 
    				+ " OR (" + SMTableicpoheaders.lstatus + " != " + Long.toString(SMTableicpoheaders.STATUS_PARTIALLY_RECEIVED) + ")"
    			+ ")"
    		+ ")"
    		;
    	
    	ResultSet rs;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				rs.close();
				throw new Exception("Vendor " + m_svendoracct + " has POs pending and cannot be deleted.");
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception("Error [1508347461] checking for open PO's - " + e1.getMessage());
		}
		
		//Check for open transactions:
		SQL = "SELECT"
			+ " " + SMTableaptransactions.lid
			+ " FROM " + SMTableaptransactions.TableName
			+ " WHERE ("
				+ "(" + SMTableaptransactions.bdcurrentamt + " != 0.00)"
				+ " AND (" + SMTableaptransactions.svendor + " = '" + getsvendoracct() + "')"
			+ ")"
		;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				rs.close();
				throw new Exception("Vendor " + m_svendoracct + " has open transactions and cannot be deleted.");
			}
			rs.close();
		} catch (SQLException e1) {
			throw new Exception("Error [1508348494] checking for open transactions - " + e1.getMessage());
		}		
		
		//Unposted batch entries:
		SQL = "SELECT"
			+ " " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lid
			+ " FROM " + SMTableapbatchentries.TableName
			+ " LEFT JOIN " + SMTableapbatches.TableName
			+ " ON " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lbatchnumber + " = " + SMTableapbatches.TableName + "." + SMTableapbatches.lbatchnumber
			+ " WHERE ("
				+ "(" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.svendoracct + " = '" + m_svendoracct + "')"
				+ " AND ("
					+ "(" + SMTableapbatches.TableName + "." + SMTableapbatches.ibatchstatus + "=" + Integer.toString(SMBatchStatuses.ENTERED) + ")"
					+ " OR (" + SMTableapbatches.TableName + "." + SMTableapbatches.ibatchstatus + "=" + Integer.toString(SMBatchStatuses.IMPORTED) + ")"
				+ ")"
			+ ")"
		;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				rs.close();
				throw new Exception("Vendor " + m_svendoracct + " has pending entries in unposted batches and cannot be deleted.");
			}
			rs.close();
		} catch (SQLException e1) {
			throw new Exception("Error [1508348685] checking for open batches - " + e1.getMessage());
		}
		
		//Start a data transaction here:
    	if (!clsDatabaseFunctions.start_data_transaction(conn)){
			throw new Exception("Error [1508347462] starting data transaction.");
    	}
		
    	SQL = "DELETE FROM " + SMTableicvendoritems.TableName
		+ " WHERE ("
			+ "(" + SMTableicvendoritems.sVendor + " = '" + getsvendoracct() + "')"
		+ ")"
		;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception ("Error [1507925078] deleting vendor items with SQL: " + SQL + " - " + ex.getMessage());
		}
    	
		//Delete vendor remit-to locations:
    	SQL = "DELETE FROM " + SMTableapvendorremittolocations.TableName
    		+ " WHERE ("
    			+ SMTableapvendorremittolocations.svendoracct + " = '" + m_svendoracct + "'"
    		+ ")"
   		;
    	
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception ("Error [1507925076] deleting vendor remit-to locations with SQL: " + SQL + " - " + ex.getMessage());
		}
		
		//Vendor statistics - apvendorstatistics
    	SQL = "DELETE FROM " + SMTableapvendorstatistics.TableName
		+ " WHERE ("
			+ "(" + SMTableapvendorstatistics.svendoracct + " = '" + getsvendoracct() + "')"
		+ ")"
		;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception ("Error [1508348900] deleting vendor statistics with SQL: " + SQL + " - " + ex.getMessage());
		}

		
		//Don't need to delete:
		//aptransactions
		//aptransactionlines
		//apmatchinglines
		
		
		//Delete the vendor themselves:
    	SQL = "DELETE FROM " + SMTableicvendors.TableName
    		+ " WHERE ("
    			+ SMTableicvendors.svendoracct + " = '" + m_svendoracct + "'"
    		+ ")"
    		;
    	
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1507925077] deleting vendor with SQL: " + SQL + " - " + ex.getMessage());
		}
		
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1508347821] starting data transaction to delete vendor.");
		}
		
		//Empty the values:
		initBidVariables();
		return;
    }

    public boolean validate_entry_fields (Connection conn){
        //Validate the entries here:
    	boolean bEntriesAreValid = true;

    	//Vendor acct:
    	m_svendoracct = m_svendoracct.trim().replace(" ", "");
        if (m_svendoracct.compareToIgnoreCase("") == 0){
        	super.addErrorMessage("Vendor account code cannot be empty.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
        if (m_svendoracct.length() > SMTableicvendors.svendoracctLength){
        	super.addErrorMessage("Vendor account code is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
    	
        //Name:
        m_sname = m_sname.trim();
        if (m_sname.compareToIgnoreCase("") == 0){
        	super.addErrorMessage("Vendor name cannot be empty.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
        if (m_sname.length() > SMTableicvendors.snameLength){
        	super.addErrorMessage("Vendor name is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }

        //Address line 1:
        m_saddressline1 = m_saddressline1.trim();
        if (m_saddressline1.length() > SMTableicvendors.saddressline1Length){
        	super.addErrorMessage("Address line 1 is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
        //Address line 2:
        m_saddressline2 = m_saddressline2.trim();
        if (m_saddressline2.length() > SMTableicvendors.saddressline2Length){
        	super.addErrorMessage("Address line 2 is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
        //Address line 3:
        m_saddressline3 = m_saddressline3.trim();
        if (m_saddressline3.length() > SMTableicvendors.saddressline3Length){
        	super.addErrorMessage("Address line 3 is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
        //Address line 4:
        m_saddressline4 = m_saddressline4.trim();
        if (m_saddressline4.length() > SMTableicvendors.saddressline4Length){
        	super.addErrorMessage("Address line 4 is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
        //City:
        m_scity = m_scity.trim();
        if (m_scity.length() > SMTableicvendors.scityLength){
        	super.addErrorMessage("City is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
        //State:
        m_sstate = m_sstate.trim();
        if (m_sstate.length() > SMTableicvendors.sstateLength){
        	super.addErrorMessage("State is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
        //Postal code:
        m_spostalcode = m_spostalcode.trim();
        if (m_spostalcode.length() > SMTableicvendors.spostalcodeLength){
        	super.addErrorMessage("Postal code is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
        //Country:
        m_scountry = m_scountry.trim();
        if (m_scountry.length() > SMTableicvendors.scountryLength){
        	super.addErrorMessage("Country is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
        //Contact name:
        m_scontactname = m_scontactname.trim();
        if (m_scontactname.length() > SMTableicvendors.scontactnameLength){
        	super.addErrorMessage("Contact name is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
        //Phone number:
        m_sphonenumber = m_sphonenumber.trim();
        if (m_sphonenumber.length() > SMTableicvendors.sphonenumberLength){
        	super.addErrorMessage("Phone number is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }

        //Fax number:
        m_sfaxnumber = m_sfaxnumber.trim();
        if (m_sfaxnumber.length() > SMTableicvendors.sfaxnumberLength){
        	super.addErrorMessage("Fax number is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
        
        //Company account code:
        m_scompanyaccountcode = m_scompanyaccountcode.trim();
        if (m_scompanyaccountcode.length() > SMTableicvendors.scompanyaccountcodeLength){
        	super.addErrorMessage("Company account code is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }

        //Web address:
        m_swebaddress = m_swebaddress.trim();
        if (m_swebaddress.length() > SMTableicvendors.swebaddressLength){
        	super.addErrorMessage("Web address is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }

        //Terms:
        m_sterms = m_sterms.trim();
        APVendorTerms terms = new APVendorTerms();
        terms.setsTermsCode(m_sterms);
        if (!terms.load(conn)){
        	super.addErrorMessage("Terms code '" + m_sterms + "' is not valid.");
        	bEntriesAreValid = false;
        }
        
        //Default distribution code:
        m_sdefaultdistributioncode = m_sdefaultdistributioncode.trim();
        if (m_sdefaultdistributioncode.length() > SMTableicvendors.sdefaultdistributioncodelength){
        	super.addErrorMessage("Default distribution code is too long.");
        	bEntriesAreValid = false;
        }
        
        //Default Expense Account:
        m_sdefaultexpenseacct = m_sdefaultexpenseacct.trim();
        if (m_sdefaultexpenseacct.length() > SMTableicvendors.sdefaultexpenseacctlength){
        	super.addErrorMessage("Default Expense Account is too long.");
        	bEntriesAreValid = false;
        }
        
        m_sdefaultinvoicelinedescription = m_sdefaultinvoicelinedescription.trim();
        if (m_sdefaultinvoicelinedescription.length() > SMTableicvendors.sdefaultinvoicelinedesclength){
        	super.addErrorMessage("Default invoice line description is too long.");
        	bEntriesAreValid = false;
        }
        
        //Account set
        if (m_iapaccountset.compareToIgnoreCase("") == 0){
        	super.addErrorMessage("Account Set cannot be empty.");
        	bEntriesAreValid = false;
        }
        
        //Bank Account
        if (m_ibankcode.compareToIgnoreCase("") == 0){
        	super.addErrorMessage("Bank account cannot be empty.");
        	bEntriesAreValid = false;
        }

        //GDoc link:
        m_sgdoclink = m_sgdoclink.trim();
        if (m_sgdoclink.length() > SMTableicvendors.sgdoclinklength){
        	super.addErrorMessage("Google Drive link is too long.");
        	bEntriesAreValid = false;
        }
        
        //Primary remit to code:
        m_sprimaryremittocode = m_sprimaryremittocode.trim();
        if (m_sprimaryremittocode.length() > SMTableicvendors.sprimaryremittocodelength){
        	super.addErrorMessage("Primary remit to code is invalid (too long).");
        	bEntriesAreValid = false;
        }
        
        //Tax ID number:
        m_staxidentifyingnumber = m_staxidentifyingnumber.trim();
        if (m_staxidentifyingnumber.length() > SMTableicvendors.staxidentifyingnumberlength){
        	super.addErrorMessage("Tax ID number is too long).");
        	bEntriesAreValid = false;
        }
        
        //Vendor group ID:
        if (
        	(m_ivendorgroupid.compareToIgnoreCase("0") == 0)
        	|| (m_ivendorgroupid.compareToIgnoreCase("") == 0)
        ){
        	super.addErrorMessage("Vendor group is invalid).");
        	bEntriesAreValid = false;
        }
        
        //Set tax options to default value if reporting type is none:
        if (m_itaxreportingtype.compareToIgnoreCase(Integer.toString(SMTableicvendors.TAX_REPORTING_TYPE_NONE)) == 0){      	
        	m_staxidentifyingnumber = "";
        	m_itaxidnumbertype = "0";
        	m_i1099CPRSid = "0";      	
        }
        
    	return bEntriesAreValid;
    }

    public String read_out_debug_data(){
    	String sResult = "  ** ICVendorEntry read out: ";
    	sResult += "\nVendor acct code: " + this.getsvendoracct();
    	sResult += "\nAddress Line 1: " + this.getsaddressline1();
    	sResult += "\nAddress Line 2: " + this.getsaddressline2();
    	sResult += "\nAddress Line 3: " + this.getsaddressline3();
    	sResult += "\nAddress Line 4: " + this.getsaddressline4();
    	sResult += "\nCity: " + this.getscity();
    	sResult += "\nContact name: " + this.getscontactname();
    	sResult += "\nCountry: " + this.getscountry();
    	sResult += "\nName: " + this.getsname();
    	sResult += "\nPhone number: " + this.getsphonenumber();
    	sResult += "\nPostal code: " + this.getspostalcode();
    	sResult += "\nState: " + this.m_sstate;
    	sResult += "\nTerms: " + this.m_sterms;
    	sResult += "\nComoany account code: " + this.m_scompanyaccountcode;
    	sResult += "\nWeb address: " + this.m_swebaddress;
    	sResult += "\nDate last maintained: " + getsdatelastmaintained();
    	sResult += "\nLast edited by: " + getslasteditedby();
    	sResult += "\nActive: " + getsactive();
    	sResult += "\nConfirmation Required: " + getspoconfirmationrequired();
    	return sResult;
    }

    public void addErrorMessage(String sMsg){
    	super.addErrorMessage(sMsg);
    }
	public String getQueryString(){
		//Particular to the specific class
		String sQueryString = "";
		sQueryString += "&" + ParamObjectName + "=" 
			+ clsServletUtilities.URLEncode(this.getObjectName());
		sQueryString += "&" + Paramsaddressline1 + "=" 
			+ clsServletUtilities.URLEncode(this.getsaddressline1());
		sQueryString += "&" + Paramsaddressline2 + "=" 
			+ clsServletUtilities.URLEncode(getsaddressline2());
		sQueryString += "&" + Paramsaddressline3 + "=" 
			+ clsServletUtilities.URLEncode(getsaddressline3());
		sQueryString += "&" + Paramsaddressline4 + "=" 
			+ clsServletUtilities.URLEncode(getsaddressline4());
		sQueryString += "&" + Paramscity + "=" 
			+ clsServletUtilities.URLEncode(getscity());
		sQueryString += "&" + m_scontactname + "=" 
			+ clsServletUtilities.URLEncode(getscontactname());
		sQueryString += "&" + Paramscountry + "=" 
			+ clsServletUtilities.URLEncode(getscountry());
		sQueryString += "&" + Paramsname + "=" 
			+ clsServletUtilities.URLEncode(getsname());
		sQueryString += "&" + Paramsphonenumber + "=" 
			+ clsServletUtilities.URLEncode(getsphonenumber());
		sQueryString += "&" + Paramspostalcode + "=" 
			+ clsServletUtilities.URLEncode(getspostalcode());
		sQueryString += "&" + Paramsstate + "=" 
			+ clsServletUtilities.URLEncode(getsstate());
		sQueryString += "&" + Paramsvendoracct + "=" 
			+ clsServletUtilities.URLEncode(getsvendoracct());
		sQueryString += "&" + Paramsterms + "=" 
			+ clsServletUtilities.URLEncode(getsterms());
		sQueryString += "&" + Paramscompanyaccountcode + "=" 
			+ clsServletUtilities.URLEncode(getscompanyaccountcode());
		sQueryString += "&" + Paramswebaddress + "=" 
			+ clsServletUtilities.URLEncode(getswebaddress());
		sQueryString += "&" + Paramdatlastmaintained + "=" 
				+ clsServletUtilities.URLEncode(getsdatelastmaintained());
		sQueryString += "&" + Paramslasteditedby + "=" 
				+ clsServletUtilities.URLEncode(getslasteditedby());
		sQueryString += "&" + Paramiactive + "=" 
				+ clsServletUtilities.URLEncode(getsactive());
		sQueryString += "&" + Paramipoconfirmationrequired + "=" 
				+ clsServletUtilities.URLEncode(getspoconfirmationrequired());
		sQueryString += "&" + Paramiapaccountset + "=" 
				+ clsServletUtilities.URLEncode(getiapaccountset());
		sQueryString += "&" + Paramibankcode + "=" 
				+ clsServletUtilities.URLEncode(getibankcode());
		sQueryString += "&" + Paramsdefaultdistributioncode + "=" 
				+ clsServletUtilities.URLEncode(getsdefaultdistributioncode());
		sQueryString += "&" + Paramsdefaultexpenseacct + "=" 
				+ clsServletUtilities.URLEncode(getsdefaultexpenseacct());
		sQueryString += "&" + Paramsdefaultinvoicelinedescription + "=" 
				+ clsServletUtilities.URLEncode(getsdefaultinvoicelinedescription());
		sQueryString += "&" + Paramsgdoclink + "=" 
				+ clsServletUtilities.URLEncode(getsgdoclink());
		sQueryString += "&" + Paramsprimaryremittocode + "=" 
				+ clsServletUtilities.URLEncode(getsprimaryremittocode());
		sQueryString += "&" + Paramstaxidentifyingnumber + "=" 
				+ clsServletUtilities.URLEncode(getstaxidentifyingnumber());
		sQueryString += "&" + Paramitaxreportingtype + "=" 
				+ clsServletUtilities.URLEncode(getstaxreportingtype());
		sQueryString += "&" + Parami1099CPRSid + "=" 
				+ clsServletUtilities.URLEncode(gets1099CPRSid());
		sQueryString += "&" + Paramitaxidnumbertype + "=" 
				+ clsServletUtilities.URLEncode(getstaxidnumbertype());
		sQueryString += "&" + Paramigenerateseparatepaymentsforeachinvoice + "=" 
				+ clsServletUtilities.URLEncode(getsgenerateseparatepaymentsforeachinvoice());
		sQueryString += "&" + Paramivendorgroupid + "=" 
				+ clsServletUtilities.URLEncode(getsvendorgroupid());
		return sQueryString;
	}

	public String getsdefaultexpenseacct() {
		return m_sdefaultexpenseacct;
	}

	public String getsdefaultdistributioncode() {
		return m_sdefaultdistributioncode;
	}
	
	public String getsdefaultinvoicelinedescription(){
		return m_sdefaultinvoicelinedescription;
	}
	
	public String getibankcode() {
		return m_ibankcode;
	}

	public String getiapaccountset() {
		return m_iapaccountset;
	}

	public String getsvendoracct() {
		return m_svendoracct;
	}
	public void setsvendoracct(String sVendorAcct) {
		this.m_svendoracct = sVendorAcct;
	}
	public String getscompanyaccountcode() {
		return m_scompanyaccountcode;
	}
	public void setscompanyaccountcode(String scompanyaccountcode) {
		this.m_scompanyaccountcode = scompanyaccountcode;
	}
	public String getswebaddress() {
		return m_swebaddress;
	}
	public void setswebaddress(String swebaddress) {
		this.m_swebaddress = swebaddress;
	}
	public String getsterms() {
		return m_sterms;
	}
	public void setsterms(String sTerms) {
		this.m_sterms = sTerms;
	}
	public String getsname() {
		return m_sname;
	}
	public void setsname(String sName) {
		this.m_sname = sName;
	}
	public String getsaddressline1() {
		return m_saddressline1;
	}
	public void setsaddressline1(String sAddressLine1) {
		this.m_saddressline1 = sAddressLine1;
	}
	public String getsaddressline2() {
		return m_saddressline2;
	}
	public void setsaddressline2(String sAddressLine2) {
		this.m_saddressline2 = sAddressLine2;
	}
	public String getsaddressline3() {
		return m_saddressline3;
	}
	public void setsaddressline3(String sAddressLine3) {
		this.m_saddressline3 = sAddressLine3;
	}
	public String getsaddressline4() {
		return m_saddressline4;
	}
	public void setsaddressline4(String sAddressLine4) {
		this.m_saddressline4 = sAddressLine4;
	}
	public String getscity() {
		return m_scity;
	}
	public void setscity(String sCity) {
		this.m_scity = sCity;
	}	
	public String getsstate() {
		return m_sstate;
	}
	public void setsstate(String sState) {
		this.m_sstate = sState;
	}
	public String getspostalcode() {
		return m_spostalcode;
	}
	public void setspostalcode(String sPostalCode) {
		this.m_spostalcode = sPostalCode;
	}
	public String getscountry() {
		return m_scountry;
	}
	public void setscountry(String sCountry) {
		this.m_scountry = sCountry;
	}
	public String getscontactname() {
		return m_scontactname;
	}
	public void setscontactname(String sContactName) {
		this.m_scontactname = sContactName;
	}
	public String getsphonenumber() {
		return m_sphonenumber;
	}
	public void setsphonenumber(String sPhoneNumber) {
		this.m_sphonenumber = sPhoneNumber;
	}
	public String getsfaxnumber() {
		return m_sfaxnumber;
	}
	public void setsfaxnumber(String sFaxNumber) {
		this.m_sfaxnumber = sFaxNumber;
	}
	public String getsdatelastmaintained() {
		return m_sdatlastmaintained;
	}
	public void setsdatelastmaintained(String sdatelastmaintained) {
		m_sdatlastmaintained = sdatelastmaintained;
	}
	public String getslasteditedby() {
		return m_slasteditedby;
	}
	public void setslasteditedby(String slasteditedby) {
		m_slasteditedby = slasteditedby;
	}
	public String getsactive() {
		return m_sactive;
	}
	public void setsactive(String sactive) {
		m_sactive = sactive;
	}
	public String getspoconfirmationrequired() {
		return m_spoconfirmationrequired;
	}
	public void setspoconfirmationrequired(String spoconfirmationrequired) {
		m_spoconfirmationrequired = spoconfirmationrequired;
	}
	public String getsgdoclink() {
		return m_sgdoclink;
	}
	public void setsgdoclink(String sgdoclink) {
		m_sgdoclink = sgdoclink;
	}
	public String getsprimaryremittocode() {
		return m_sprimaryremittocode;
	}
	public void setsprimaryremittocode(String sprimaryremittocode) {
		m_sprimaryremittocode = sprimaryremittocode;
	}
	public String getstaxidentifyingnumber() {
		return m_staxidentifyingnumber;
	}
	public void setstaxidentifyingnumber(String staxidentifyingnumber) {
		m_staxidentifyingnumber = staxidentifyingnumber;
	}
	public String getstaxreportingtype() {
		return m_itaxreportingtype;
	}
	public void setstaxreportingtype(String staxreportingtype) {
		m_itaxreportingtype = staxreportingtype;
	}
	public String gets1099CPRSid() {
		return m_i1099CPRSid;
	}
	public void sets1099CPRSid(String s1099CPRSid) {
		m_i1099CPRSid = s1099CPRSid;
	}
	public String getstaxidnumbertype() {
		return m_itaxidnumbertype;
	}
	public void setstaxidnumbertype(String staxidnumbertype) {
		m_itaxidnumbertype = staxidnumbertype;
	}
	public String getsgenerateseparatepaymentsforeachinvoice() {
		return m_igenerateseparatepaymentsforeachinvoice;
	}
	public void setsgenerateseparatepaymentsforeachinvoice(String sgenerateseparatepaymentsforeachinvoice) {
		m_igenerateseparatepaymentsforeachinvoice = sgenerateseparatepaymentsforeachinvoice;
	}
	public String getsvendorgroupid() {
		return m_ivendorgroupid;
	}
	public void setsvendorgroupid(String svendorgroupid) {
		m_ivendorgroupid = svendorgroupid;
	}
	
	public static String getFindVendorLink(
		String sSearchingClassName, 
		String sReturnField, 
		String sParameterString, 
		ServletContext context,
		String sDBID){
		
		String m_sParameterString = sParameterString;
		
		if (m_sParameterString.startsWith("*")){
			m_sParameterString = m_sParameterString.substring(1);
		}
		
		return  
			SMUtilities.getURLLinkBase(context) + "smar.ObjectFinder"
			+ "?"+ "&ObjectName=" + APVendor.ParamObjectName
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&ResultClass=FinderResults"
			+ "&SearchingClass=" + sSearchingClassName
			+ "&ReturnField=" + sReturnField
			+ "&SearchField1=" + SMTableicvendors.sname
			+ "&SearchFieldAlias1=Name"
			+ "&SearchField2=" + SMTableicvendors.svendoracct
			+ "&SearchFieldAlias2=Account%20No."
			+ "&SearchField3=" + SMTableicvendors.saddressline1
			+ "&SearchFieldAlias3=Address%201"
			+ "&SearchField4=" + SMTableicvendors.saddressline2
			+ "&SearchFieldAlias4=Address%202"
			+ "&ResultListField1="  + SMTableicvendors.svendoracct
			+ "&ResultHeading1=Account%20No."
			+ "&ResultListField2="  + SMTableicvendors.sname
			+ "&ResultHeading2=Name"
			+ "&ResultListField3="  + SMTableicvendors.saddressline1
			+ "&ResultHeading3=Address%201"
			+ "&ResultListField4="  + SMTableicvendors.saddressline2
			+ "&ResultHeading4=Address%202"
			+ "&ResultListField5="  + SMTableicvendors.saddressline3
			+ "&ResultHeading5=Address%203"
			+ "&ResultListField6="  + SMTableicvendors.saddressline4
			+ "&ResultHeading6=Address%204"
			+ "&ResultListField7="  + SMTableicvendors.scity
			+ "&ResultHeading7=City"
			+ "&ResultListField8="  + SMTableicvendors.sstate
			+ "&ResultHeading8=State"
			+ "&ResultListField9="  + SMTableicvendors.spostalcode
			+ "&ResultHeading9=Zip"
			+ "&ResultListField10="  + SMTableicvendors.sdefaultexpenseacct
			+ "&ResultHeading10=Default%20GL"
			+ "&ResultListField11="  + "IF(" + SMTableicvendors.iactive + " = 1, 'Y', 'N')"
			+ "&ResultHeading11=Active?"
			+ "&ParameterString=*" + m_sParameterString
			;
	}
	
    private void initBidVariables(){
    	m_svendoracct = "";
    	m_sname = "";
    	m_saddressline1 = "";
    	m_saddressline2 = "";
    	m_saddressline3 = "";
    	m_saddressline4 = "";
    	m_scity = "";
    	m_sstate = "";
    	m_spostalcode = "";
    	m_scountry = "";
    	m_scontactname = "";
    	m_sphonenumber = "";
    	m_sfaxnumber = "";
    	m_sterms = "";
    	m_sdatlastmaintained = EMPTY_DATETIME_STRING;
    	m_sactive = "1";
    	m_spoconfirmationrequired = "0";
    	m_iapaccountset = "";
    	m_ibankcode = "";
    	m_sdefaultdistributioncode = "";
    	m_sdefaultexpenseacct = "";
    	m_sdefaultinvoicelinedescription = "";
    	m_sgdoclink = "";;
    	m_sprimaryremittocode = "";
    	m_staxidentifyingnumber = "";
    	m_itaxreportingtype = "0";
    	m_i1099CPRSid = "0";
    	m_itaxidnumbertype = "0";
    	m_igenerateseparatepaymentsforeachinvoice = "0";
    	m_ivendorgroupid = "0";
		super.initVariables();
		super.setObjectName(ParamObjectName);
    }
}