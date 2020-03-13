package smic;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import smap.APVendor;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTableaptransactionlines;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicpolines;
import SMDataDefinition.SMTableicporeceiptheaders;
import SMDataDefinition.SMTableicporeceiptlines;
import SMDataDefinition.SMTableicshipvias;
import SMDataDefinition.SMTableicvendoritems;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsValidateFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class ICPOHeader extends clsMasterEntry{

	public static final String ParamObjectName = "Purchase Order";
	
	//Particular to the specific class
	public static final String Paramlid = "lid";
	public static final String Paramsvendor = "svendor";
	public static final String Paramsponumber = "sponumber";
	public static final String Paramsreference = "sreference";
	public static final String Paramsvendorname = "svendorname";
	public static final String Paramllstatus = "lstatus";
	public static final String Paramdatpodate = "datpodate";
	public static final String Paramsshipcode = "sshipcode";
	public static final String Paramsshipname = "sshipname";
	public static final String Paramsshipaddress1 = "sshipaddress1";
	public static final String Paramsshipaddress2 = "sshipaddress2";
	public static final String Paramsshipaddress3 = "sshipaddress3";
	public static final String Paramsshipaddress4 = "sshipaddress4";
	public static final String Paramsshipcity = "sshipcity";
	public static final String Paramsshipstate = "sshipstate";
	public static final String Paramsshippostalcode = "sshippostalcode";
	public static final String Paramsshipcountry = "sshipcountry";
	public static final String Paramsshipphone = "sshipphone";
	public static final String Paramsshipfax = "sshipfax";
	public static final String Paramsshipcontactname = "sshipcontactname";
	public static final String Paramsbillcode = "sbillcode";
	public static final String Paramsbillname = "sbillname";
	public static final String Paramsbilladdress1 = "sbilladdress1";
	public static final String Paramsbilladdress2 = "sbilladdress2";
	public static final String Paramsbilladdress3 = "sbilladdress3";
	public static final String Paramsbilladdress4 = "sbilladdress4";
	public static final String Paramsbillcity = "sbillcity";
	public static final String Paramsbillstate = "sbillstate";
	public static final String Paramsbillpostalcode = "sbillpostalcode";
	public static final String Paramsbillcountry = "sbillcountry";
	public static final String Paramsbillphone = "sbillphone";
	public static final String Paramsbillfax = "sbillfax";
	public static final String Paramsbillcontactname = "sbillcontactname";
	public static final String Paramsshipviacode = "sshipviacode";
	public static final String Paramsshipvianame = "sshipvianame";
	public static final String Paramsdatexpecteddate = "datexpecteddate";
	public static final String Paramdatassigned = "datassigned";
	public static final String Paramsassignedtofullname = "sassignedtofullname";
	public static final String Paramscomment = "scomment";
	public static final String Paramsdescription = "sdescription";
	public static final String Paramsdeletedbyfullname = "sdeletedbyfullname";
	public static final String Paramsdatdeleted = "datdeleted";
	public static final String Paramsgdoclink = "sgdoclink";
	public static final String Paramscreatedbyfullname = "screatedbyfullname";
	
	public static final String Paramipaymentonhold = SMTableicpoheaders.ipaymentonhold;
	public static final String Paramspaymentonholdbyfullname = SMTableicpoheaders.spaymentonholdbyfullname;
	public static final String Paramlpaymentonholdbyuserid = SMTableicpoheaders.lpaymentonholdbyuserid;
	public static final String Paramdatpaymentplacedonhold = SMTableicpoheaders.datpaymentplacedonhold;
	public static final String Parammpaymentonholdreason = SMTableicpoheaders.mpaymentonholdreason;
	public static final String Parammpaymentonholdvendorcomment = SMTableicpoheaders.mpaymentonholdvendorcomment;
	
	private String m_slid;
	private String m_svendor;
	private String m_sponumber;
	private String m_sreference;
	private String m_svendorname;
	private String m_sstatus;
	private String m_sdatpodate;
	private String m_sshipcode;
	private String m_sshipname;
	private String m_sshipaddress1;
	private String m_sshipaddress2;
	private String m_sshipaddress3;
	private String m_sshipaddress4;
	private String m_sshipcity;
	private String m_sshipstate;
	private String m_sshippostalcode;
	private String m_sshipcountry;
	private String m_sshipphone;
	private String m_sshipfax;
	private String m_sshipcontactname;
	private String m_sbillcode;
	private String m_sbillname;
	private String m_sbilladdress1;
	private String m_sbilladdress2;
	private String m_sbilladdress3;
	private String m_sbilladdress4;
	private String m_sbillcity;
	private String m_sbillstate;
	private String m_sbillpostalcode;
	private String m_sbillcountry;
	private String m_sbillphone;
	private String m_sbillfax;
	private String m_sbillcontactname;	
	private String m_sshipviacode;
	private String m_sshipvianame;
	private String m_sdatexpecteddate;
	private String m_sdatassigned;
	private String m_sassignedtofullname;
	private String m_scomment;
	private String m_sdescription;
	private String m_sdeletedbyfullname;
	private String m_sdatdeleted;
	private String m_sgdoclink;
	private String m_screatedbyfullname;
	private String m_ipaymentonhold;
	private String m_spaymentonholdbyfullname;
	private String m_lpaymentonholdbyuserid;
	private String m_datpaymentplacedonhold;
	private String m_mpaymentonholdreason;
	private String m_mpaymentonholdvendorcomment;
	
	private boolean bDebugMode = false;
	
	public ICPOHeader() {
		super();
		initPOVariables();
        }

	public ICPOHeader (HttpServletRequest req){
		super(req);
		initPOVariables();
		m_slid = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramlid, req).trim();
		if (m_slid.compareToIgnoreCase("") == 0){
			m_slid = "-1";
		}
		if (bDebugMode){
			System.out.println("[1579203415] In " + this.toString() + ".ICPOHeaderEntry - ICPOHeaderEntry.Paramlid = " + m_slid);
		}
		m_svendor = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsvendor, req).trim().toUpperCase();
		m_sponumber = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsponumber, req).trim().toUpperCase();
		m_sreference = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsreference, req).trim();
		m_svendorname = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsvendorname, req).trim();
		if(clsManageRequestParameters.get_Request_Parameter(
				ICPOHeader.Paramdatpodate, req).trim().compareToIgnoreCase("") != 0){
			m_sdatpodate = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramdatpodate, req).trim();
		}
		m_sstatus = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramllstatus, req).trim();
		if (m_sstatus.compareToIgnoreCase("") == 0){
			m_sstatus = Integer.toString(SMTableicpoheaders.STATUS_ENTERED);
		}

		m_sshipcode = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsshipcode, req).trim();
		m_sshipname = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsshipname, req).trim();
		m_sshipaddress1 = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsshipaddress1, req).trim();
		m_sshipaddress2 = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsshipaddress2, req).trim();
		m_sshipaddress3 = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsshipaddress3, req).trim();
		m_sshipaddress4 = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsshipaddress4, req).trim();
		m_sshipcity = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsshipcity, req).trim();
		m_sshipstate = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsshipstate, req).trim();
		m_sshippostalcode = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsshippostalcode, req).trim();
		m_sshipcountry = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsshipcountry, req).trim();
		m_sshipphone = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsshipphone, req).trim();
		m_sshipfax = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsshipfax, req).trim();
		m_sshipcontactname = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsshipcontactname, req).trim();
		m_sbillcode = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsbillcode, req).trim();
		m_sbillname = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsbillname, req).trim();
		m_sbilladdress1 = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsbilladdress1, req).trim();
		m_sbilladdress2 = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsbilladdress2, req).trim();
		m_sbilladdress3 = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsbilladdress3, req).trim();
		m_sbilladdress4 = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsbilladdress4, req).trim();
		m_sbillcity = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsbillcity, req).trim();
		m_sbillstate = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsbillstate, req).trim();
		m_sbillpostalcode = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsbillpostalcode, req).trim();
		m_sbillcountry = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsbillcountry, req).trim();
		m_sbillphone = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsbillphone, req).trim();
		m_sbillfax = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsbillfax, req).trim();
		m_sbillcontactname = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsbillcontactname, req).trim();
		m_sshipviacode = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsshipviacode, req).trim();
		m_sshipvianame = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsshipvianame, req).trim();
		if(clsManageRequestParameters.get_Request_Parameter(
				ICPOHeader.Paramsdatexpecteddate, req).trim().compareToIgnoreCase("") != 0){
			m_sdatexpecteddate = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsdatexpecteddate, req).trim();
		}
		m_sdatassigned = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramdatassigned, req).trim();
		if (m_sdatassigned.compareToIgnoreCase("") == 0){
			m_sdatassigned = EMPTY_DATETIME_STRING;
		}
		m_sassignedtofullname = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsassignedtofullname, req).trim();
		m_scomment = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramscomment, req).trim();
		m_sdescription = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsdescription, req).trim();
		m_sdeletedbyfullname = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsdeletedbyfullname, req).trim();
		m_sdatdeleted = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsdatdeleted, req).trim();
		if (m_sdatdeleted.compareToIgnoreCase("") == 0){
			m_sdatdeleted = EMPTY_DATETIME_STRING;
		}
		m_sgdoclink = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsgdoclink, req).trim();
		m_screatedbyfullname = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramscreatedbyfullname, req).trim();
		
		if (clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramipaymentonhold, req).compareToIgnoreCase("") != 0){
			m_ipaymentonhold = "1";
		}else{
			m_ipaymentonhold = "0";
		}
		m_spaymentonholdbyfullname = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramspaymentonholdbyfullname, req).trim();
		m_lpaymentonholdbyuserid = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramlpaymentonholdbyuserid, req).trim();
		m_datpaymentplacedonhold = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramdatpaymentplacedonhold, req).trim();
		if (m_datpaymentplacedonhold.compareToIgnoreCase("") == 0){
			m_datpaymentplacedonhold = EMPTY_DATETIME_STRING;
		}
		m_mpaymentonholdreason = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Parammpaymentonholdreason, req).trim();
		m_mpaymentonholdvendorcomment = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Parammpaymentonholdvendorcomment, req).trim();
	}
    public boolean load (ServletContext context, String sDBID, String sUserID, String sUserFullName){
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBID, 
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
    	
    	boolean bResult = load (conn);
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080899]");
    	return bResult;
    	
    }
    public boolean load (Connection conn){
    	return load (m_slid, conn);
    }
    private boolean load (String sPOID, Connection conn){

    	sPOID = sPOID.trim();
    	if (sPOID.compareToIgnoreCase("") == 0){
    		super.addErrorMessage("PO Number cannot be blank.");
    		return false;
    	}
		long lID;
		try {
			lID = Long.parseLong(sPOID);
		} catch (NumberFormatException n) {
			super.addErrorMessage("Invalid PO number: '" + sPOID + "'");
			return false;
		}
		
		//In case we get a negative one, that indicates that this is actually a NEW PO, and in that
		//case it can't be loaded:
		if (lID == -1){
			super.addErrorMessage("Invalid PO number.");
			return false;
		}
    	
		String SQL = " SELECT * FROM " + SMTableicpoheaders.TableName
			+ " WHERE ("
				+ SMTableicpoheaders.lid + " = " + sPOID
			+ ")";
		if (bDebugMode){
			System.out.println("[1579203421] In " + this.toString() + " - load SQL = " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_slid = Long.toString(rs.getLong(SMTableicpoheaders.lid));
				m_svendor = rs.getString(SMTableicpoheaders.svendor).trim();
				m_sponumber = rs.getString(SMTableicpoheaders.sponumber).trim();
				m_sreference = rs.getString(SMTableicpoheaders.sreference).trim();
				m_svendorname = rs.getString(SMTableicpoheaders.svendorname).trim();
				m_sstatus = Long.toString(rs.getLong(SMTableicpoheaders.lstatus));
				m_sdatpodate = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTableicpoheaders.datpodate));
				m_sshipcode = rs.getString(SMTableicpoheaders.sshipcode).trim();
				m_sshipname = rs.getString(SMTableicpoheaders.sshipname).trim();
				m_sshipaddress1 = rs.getString(SMTableicpoheaders.sshipaddress1).trim();
				m_sshipaddress2 = rs.getString(SMTableicpoheaders.sshipaddress2).trim();
				m_sshipaddress3 = rs.getString(SMTableicpoheaders.sshipaddress3).trim();
				m_sshipaddress4 = rs.getString(SMTableicpoheaders.sshipaddress4).trim();
				m_sshipcity = rs.getString(SMTableicpoheaders.sshipcity).trim();
				m_sshipstate = rs.getString(SMTableicpoheaders.sshipstate).trim();
				m_sshippostalcode = rs.getString(SMTableicpoheaders.sshippostalcode).trim();
				m_sshipcountry = rs.getString(SMTableicpoheaders.sshipcountry).trim();
				m_sshipphone = rs.getString(SMTableicpoheaders.sshipphone).trim();
				m_sshipfax = rs.getString(SMTableicpoheaders.sshipfax).trim();
				m_sshipcontactname = rs.getString(SMTableicpoheaders.sshipcontactname).trim();
				m_sbillcode = rs.getString(SMTableicpoheaders.sbillcode).trim();
				m_sbillname = rs.getString(SMTableicpoheaders.sbillname).trim();
				m_sbilladdress1 = rs.getString(SMTableicpoheaders.sbilladdress1).trim();
				m_sbilladdress2 = rs.getString(SMTableicpoheaders.sbilladdress2).trim();
				m_sbilladdress3 = rs.getString(SMTableicpoheaders.sbilladdress3).trim();
				m_sbilladdress4 = rs.getString(SMTableicpoheaders.sbilladdress4).trim();
				m_sbillcity = rs.getString(SMTableicpoheaders.sbillcity).trim();
				m_sbillstate = rs.getString(SMTableicpoheaders.sbillstate).trim();
				m_sbillpostalcode = rs.getString(SMTableicpoheaders.sbillpostalcode).trim();
				m_sbillcountry = rs.getString(SMTableicpoheaders.sbillcountry).trim();
				m_sbillphone = rs.getString(SMTableicpoheaders.sbillphone).trim();
				m_sbillfax = rs.getString(SMTableicpoheaders.sbillfax).trim();
				m_sbillcontactname = rs.getString(SMTableicpoheaders.sbillcontactname).trim();
				m_sshipviacode = rs.getString(SMTableicpoheaders.sshipviacode).trim();
				m_sshipvianame = rs.getString(SMTableicpoheaders.sshipvianame).trim();
				m_sdatexpecteddate = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTableicpoheaders.datexpecteddate));
				m_sdatassigned = clsDateAndTimeConversions.resultsetDateTimeToTheMinuteStringToString(
						rs.getString(SMTableicpoheaders.datassigned));
				m_sassignedtofullname = rs.getString(SMTableicpoheaders.sassignedtofullname).trim();
				m_scomment = rs.getString(SMTableicpoheaders.scomment).trim();
				m_sdescription = rs.getString(SMTableicpoheaders.sdescription).trim();
				m_sdeletedbyfullname = rs.getString(SMTableicpoheaders.sdeletedbyfullname).trim();
				m_sdatdeleted = clsDateAndTimeConversions.resultsetDateTimeToTheMinuteStringToString(
						rs.getString(SMTableicpoheaders.datdeleted));
				if (rs.getString(SMTableicpoheaders.sgdoclink) == null){
					m_sgdoclink = "";
				}else{
					m_sgdoclink = rs.getString(SMTableicpoheaders.sgdoclink).trim();
				}
				m_screatedbyfullname = rs.getString(SMTableicpoheaders.screatedbyfullname).trim();
				m_ipaymentonhold = Integer.toString(rs.getInt(SMTableicpoheaders.ipaymentonhold));
				m_spaymentonholdbyfullname = rs.getString(SMTableicpoheaders.spaymentonholdbyfullname).trim();
				m_lpaymentonholdbyuserid = Long.toString(rs.getLong(SMTableicpoheaders.lpaymentonholdbyuserid));
				m_datpaymentplacedonhold = clsDateAndTimeConversions.resultsetDateTimeToTheSecondStringToString(
					rs.getString(SMTableicpoheaders.datpaymentplacedonhold));
				m_mpaymentonholdreason = rs.getString(SMTableicpoheaders.mpaymentonholdreason).trim();
				m_mpaymentonholdvendorcomment = rs.getString(SMTableicpoheaders.mpaymentonholdvendorcomment).trim();
				rs.close();
			} else {
				super.addErrorMessage("No " + ParamObjectName + " found for : '" + sPOID
						+ "'");
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error reading " + ParamObjectName + " for : '" + sPOID
					+ "' - " + e.getMessage());
			return false;
		}
		return true;
    }
    
    public boolean save_without_data_transaction (ServletContext context, String sDBID, String sUserID, String sUserFullName, boolean bForceSave){
    	
       	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBID, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID
    			+" - "
    			+ sUserFullName
    			);
    	
    	if (conn == null){
    		super.addErrorMessage("Error opening data connection.");
    		return false;
    	}
    	
    	boolean bResult = save_without_data_transaction (conn, sUserFullName, sUserID, bForceSave);
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080900]");
    	return bResult;	
    	
    }
    public boolean updatePOStatus (Connection conn){
    	
    	if (this.getsstatus().compareToIgnoreCase(
    		SMTableicpoheaders.getStatusDescription(SMTableicpoheaders.STATUS_DELETED)) == 0){
    		return true;
    	}
    	
    	String SQL = "SELECT"
    		+ " " + SMTableicpolines.bdqtyordered
    		+ ", " + SMTableicpolines.bdqtyreceived
    		+ " FROM " + SMTableicpolines.TableName
    		+ " WHERE ("
    			+ "(" + SMTableicpolines.lpoheaderid + " = " + this.getsID() + ")"
    		+ ")"
    	;
    	int iLineCounter = 0;
    	int iLinesPartiallyReceived = 0;
    	int iLinesCompletelyReceived = 0;
    	int iPOStatus = SMTableicpoheaders.STATUS_ENTERED;

    	//Determine the status of the PO line here:
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()) {
				iLineCounter++;
				BigDecimal bdQtyReceived = new BigDecimal(0);
				bdQtyReceived = rs.getBigDecimal(SMTableicpolines.bdqtyreceived);
				//If it HAS a qty received, check further:
				if (bdQtyReceived.compareTo(BigDecimal.ZERO) != 0){
					//If the qty ordered is still more than the qty received, then count it as
					// a partially received line:
					if (rs.getBigDecimal(SMTableicpolines.bdqtyordered).compareTo(
							bdQtyReceived) > 0) {
						iLinesPartiallyReceived++;
					//Otherwise, if the qty received is equal to or MORE than the qty ordered, count it as 
					// a completely received line:
					}else{
						iLinesCompletelyReceived++;						
					}
				}
			}
			//IF there are no lines at all, it's an ENTERED PO with no lines:
			if (iLineCounter == 0) {
				iPOStatus = SMTableicpoheaders.STATUS_ENTERED;
			}else{
				//If there ARE lines, but none are received at all, it's ENTERED:
				if ((iLinesPartiallyReceived == 0) && (iLinesCompletelyReceived == 0)){
					iPOStatus = SMTableicpoheaders.STATUS_ENTERED;
				}else{
					//If every line is completely received, it's COMPLETE:
					if (iLinesCompletelyReceived == iLineCounter){
						iPOStatus = SMTableicpoheaders.STATUS_COMPLETE;
					//In any other case, it's PARTIALLY RECEIVED:
					}else{
						iPOStatus = SMTableicpoheaders.STATUS_PARTIALLY_RECEIVED;
					}
				}
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Error checking PO lines for complete status - " + e.getMessage());
			return false;
		}
		
		SQL = "UPDATE " + SMTableicpoheaders.TableName
			+ " SET " + SMTableicpoheaders.lstatus + " = " + iPOStatus
			+ " WHERE ("
				+ "(" + SMTableicpoheaders.lid + " = " + this.getsID() + ")"
			+ ")"
		;
		
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
    		//System.out.println(this.toString() + "Could not update " + ParamObjectName 
    		//		+ " status - " + ex.getMessage() + ".<BR>");
    		super.addErrorMessage("Could not update " + ParamObjectName + " status with SQL: " + SQL
    				+ " - " + ex.getMessage());
    		return false;
		}
		
		return true;
    }
    public boolean save_without_data_transaction (Connection conn, String sUserFullName, String sUserID, boolean bForceSave){
    	//bForceSave tells the system to save the PO even if it's completed or deleted, e.g. when we are trying to update a vendor
    	if (!bForceSave){
	    	if (
	    			//We're allowing 'completed' orders to be updated so that people can update 'on hold' information for them:
	    			//(getsstatus().compareToIgnoreCase(Integer.toString(SMTableicpoheaders.STATUS_COMPLETE)) == 0)
	    			(getsstatus().compareToIgnoreCase(Integer.toString(SMTableicpoheaders.STATUS_DELETED)) == 0)
	    	){
	    		super.addErrorMessage(SMTableicpoheaders.getStatusDescription(Integer.parseInt(getsstatus())) 
	    			+ " purchase orders cannot be updated.");
	    		return false;
	    	}
    	}
    	if (!validate_entry_fields(conn, sUserID)){
    		return false;
    	}
    	String SQL = "";
    	ResultSet rs;

		//If it's a new record, do an insert:
		if (m_slid.compareToIgnoreCase("-1") == 0){
			SQL = "INSERT INTO " + SMTableicpoheaders.TableName + " ("
			+ SMTableicpoheaders.lstatus
			+ ", " + SMTableicpoheaders.sponumber
			+ ", " + SMTableicpoheaders.sreference
			+ ", " + SMTableicpoheaders.svendor
			+ ", " + SMTableicpoheaders.svendorname
			+ ", " + SMTableicpoheaders.datexpecteddate
			+ ", " + SMTableicpoheaders.datpodate
			+ ", " + SMTableicpoheaders.sbilladdress1
			+ ", " + SMTableicpoheaders.sbilladdress2
			+ ", " + SMTableicpoheaders.sbilladdress3
			+ ", " + SMTableicpoheaders.sbilladdress4
			+ ", " + SMTableicpoheaders.sbillcity
			+ ", " + SMTableicpoheaders.sbillcode
			+ ", " + SMTableicpoheaders.sbillcontactname
			+ ", " + SMTableicpoheaders.sbillcountry
			+ ", " + SMTableicpoheaders.sbillfax
			+ ", " + SMTableicpoheaders.sbillname
			+ ", " + SMTableicpoheaders.sbillphone
			+ ", " + SMTableicpoheaders.sbillpostalcode
			+ ", " + SMTableicpoheaders.sbillstate
			+ ", " + SMTableicpoheaders.sshipaddress1
			+ ", " + SMTableicpoheaders.sshipaddress2
			+ ", " + SMTableicpoheaders.sshipaddress3
			+ ", " + SMTableicpoheaders.sshipaddress4
			+ ", " + SMTableicpoheaders.sshipcity
			+ ", " + SMTableicpoheaders.sshipcode
			+ ", " + SMTableicpoheaders.sshipcontactname
			+ ", " + SMTableicpoheaders.sshipcountry
			+ ", " + SMTableicpoheaders.sshipfax
			+ ", " + SMTableicpoheaders.sshipname
			+ ", " + SMTableicpoheaders.sshipphone
			+ ", " + SMTableicpoheaders.sshippostalcode
			+ ", " + SMTableicpoheaders.sshipstate
			+ ", " + SMTableicpoheaders.sshipviacode
			+ ", " + SMTableicpoheaders.sshipvianame
			+ ", " + SMTableicpoheaders.datassigned
			+ ", " + SMTableicpoheaders.sassignedtofullname
			+ ", " + SMTableicpoheaders.lassignedtouserid
			+ ", " + SMTableicpoheaders.scomment
			+ ", " + SMTableicpoheaders.sdescription
			+ ", " + SMTableicpoheaders.sgdoclink
			+ ", " + SMTableicpoheaders.screatedbyfullname
			+ ", " + SMTableicpoheaders.lcreatedbyuserid
			+ ", " + SMTableicpoheaders.ipaymentonhold
			+ ", " + SMTableicpoheaders.spaymentonholdbyfullname
			+ ", " + SMTableicpoheaders.lpaymentonholdbyuserid
			+ ", " + SMTableicpoheaders.datpaymentplacedonhold
			+ ", " + SMTableicpoheaders.mpaymentonholdreason
			+ ", " + SMTableicpoheaders.mpaymentonholdvendorcomment
			+ ") VALUES ("
			+ " 0" //POs start as 'ENTERED'
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sponumber.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sreference.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_svendor.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_svendorname.trim()) + "'"
			+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_sdatexpecteddate) + "'"
			+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_sdatpodate) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sbilladdress1.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sbilladdress2.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sbilladdress3.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sbilladdress4.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sbillcity.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sbillcode.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sbillcontactname.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sbillcountry.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sbillfax.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sbillname.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sbillphone.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sbillpostalcode.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sbillstate.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipaddress1.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipaddress2.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipaddress3.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipaddress4.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipcity.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipcode.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipcontactname.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipcountry.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipfax.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipname.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipphone.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sshippostalcode.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipstate.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipviacode.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipvianame.trim()) + "'"
			+ ", " + "NOW()"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName).trim() + "'"
			+ ", " + sUserID + ""
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_scomment).trim() + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sdescription).trim() + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdoclink).trim() + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName).trim() + "'"
			+ ", " + sUserID + ""
			+ ", " + m_ipaymentonhold
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_spaymentonholdbyfullname).trim() + "'"
			+ ", " + m_lpaymentonholdbyuserid
			+ ", '" + clsDateAndTimeConversions.stdDateTimeToSQLDateTimeInSecondsString(m_datpaymentplacedonhold) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_mpaymentonholdreason).trim() + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_mpaymentonholdvendorcomment).trim() + "'"
			+ ")"
			;
		}else{
			SQL = "UPDATE " + SMTableicpoheaders.TableName + " SET "
			+ SMTableicpoheaders.lstatus + " = " + m_sstatus
			+ ", " + SMTableicpoheaders.sreference
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sreference.trim()) + "'"
			+ ", " + SMTableicpoheaders.svendor
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_svendor.trim()) + "'"
			+ ", " + SMTableicpoheaders.svendorname
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_svendorname.trim()) + "'"
			+ ", " + SMTableicpoheaders.datexpecteddate
				+ " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_sdatexpecteddate) + "'"
			+ ", " + SMTableicpoheaders.datpodate
				+ " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_sdatpodate) + "'"
			+ ", " + SMTableicpoheaders.sbilladdress1
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sbilladdress1.trim()) + "'"
			+ ", " + SMTableicpoheaders.sbilladdress2
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sbilladdress2.trim()) + "'"
			+ ", " + SMTableicpoheaders.sbilladdress3
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sbilladdress3.trim()) + "'"
			+ ", " + SMTableicpoheaders.sbilladdress4
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sbilladdress4.trim()) + "'"
			+ ", " + SMTableicpoheaders.sbillcity
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sbillcity.trim()) + "'"
			+ ", " + SMTableicpoheaders.sbillcode
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sbillcode.trim()) + "'"
			+ ", " + SMTableicpoheaders.sbillcontactname
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sbillcontactname.trim()) + "'"
			+ ", " + SMTableicpoheaders.sbillcountry
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sbillcountry.trim()) + "'"
			+ ", " + SMTableicpoheaders.sbillfax
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sbillfax.trim()) + "'"
			+ ", " + SMTableicpoheaders.sbillname
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sbillname.trim()) + "'"
			+ ", " + SMTableicpoheaders.sbillphone
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sbillphone.trim()) + "'"
			+ ", " + SMTableicpoheaders.sbillpostalcode
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sbillpostalcode.trim()) + "'"
			+ ", " + SMTableicpoheaders.sbillstate
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sbillstate.trim()) + "'"
			+ ", " + SMTableicpoheaders.sponumber
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sponumber.trim()) + "'"
			+ ", " + SMTableicpoheaders.sshipaddress1
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipaddress1.trim()) + "'"
			+ ", " + SMTableicpoheaders.sshipaddress2
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipaddress2.trim()) + "'"
			+ ", " + SMTableicpoheaders.sshipaddress3
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipaddress3.trim()) + "'"
			+ ", " + SMTableicpoheaders.sshipaddress4
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipaddress4.trim()) + "'"
			+ ", " + SMTableicpoheaders.sshipcity
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipcity.trim()) + "'"
			+ ", " + SMTableicpoheaders.sshipcode
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipcode.trim()) + "'"
			+ ", " + SMTableicpoheaders.sshipcontactname
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipcontactname.trim()) + "'"
			+ ", " + SMTableicpoheaders.sshipcountry
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipcountry.trim()) + "'"
			+ ", " + SMTableicpoheaders.sshipfax
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipfax.trim()) + "'"
			+ ", " + SMTableicpoheaders.sshipname
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipname.trim()) + "'"
			+ ", " + SMTableicpoheaders.sshipphone
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipphone.trim()) + "'"
			+ ", " + SMTableicpoheaders.sshippostalcode
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sshippostalcode.trim()) + "'"
			+ ", " + SMTableicpoheaders.sshipstate
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipstate.trim()) + "'"
			+ ", " + SMTableicpoheaders.sshipviacode
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipviacode.trim()) + "'"
			+ ", " + SMTableicpoheaders.sshipvianame
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sshipvianame.trim()) + "'"
			//+ ", " + SMTableicpoheaders.datassigned
			//	+ " = '" + super.ampmDateTimeToSQLDateTime(m_sdatassigned) + "'"
			//+ ", " + SMTableicpoheaders.sassignedtoname
			//	+ " = '" + SMUtilities.FormatSQLStatement(m_sassignedtoname.trim()) + "'"
			+ ", " + SMTableicpoheaders.scomment
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_scomment.trim()) + "'"
			+ ", " + SMTableicpoheaders.sdescription
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sdescription.trim()) + "'"
			+ ", " + SMTableicpoheaders.sgdoclink
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdoclink.trim()) + "'"
			+ ", " + SMTableicpoheaders.ipaymentonhold
				+ " = " + m_ipaymentonhold
			+ ", " + SMTableicpoheaders.spaymentonholdbyfullname
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_spaymentonholdbyfullname.trim()) + "'"
			+ ", " + SMTableicpoheaders.lpaymentonholdbyuserid
				+ " = " + m_lpaymentonholdbyuserid
			+ ", " + SMTableicpoheaders.datpaymentplacedonhold
				+ " = '" + clsDateAndTimeConversions.stdDateTimeToSQLDateTimeInSecondsString(m_datpaymentplacedonhold) + "'"
			+ ", " + SMTableicpoheaders.mpaymentonholdreason
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_mpaymentonholdreason.trim()) + "'"
			+ ", " + SMTableicpoheaders.mpaymentonholdvendorcomment
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_mpaymentonholdvendorcomment.trim()) + "'"						
			+ " WHERE ("
				+ "(" + SMTableicpoheaders.lid + " = " + m_slid + ")"
			+ ")"
		;
		}

		if (bDebugMode){
			System.out.println("[1394565124] In " + this.toString() + " - save SQL = " + SQL);
		}
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
    		// TBDL
			//System.out.println(this.toString() + "Could not insert/update " + ParamObjectName 
    		//		+ " - " + ex.getMessage() + ".<BR>");
    		super.addErrorMessage("Could not insert/update " + ParamObjectName + " with SQL: " + SQL
    				+ " - " + ex.getMessage());
    		return false;
		}
    	
    	//If it's a NEW record, get the last insert ID:
    	if (m_slid.compareToIgnoreCase("-1") == 0){
    			SQL = "SELECT LAST_INSERT_ID()";
    			try {
    				rs = clsDatabaseFunctions.openResultSet(SQL, conn);
    				if (rs.next()) {
    					m_slid = Long.toString(rs.getLong(1));
    				} else {
    					super.addErrorMessage("Could not get last ID number with SQL: " + SQL);
    					return false;
    				}
    				rs.close();
    			} catch (SQLException e) {
    				super.addErrorMessage("Could not get last ID number - with SQL: " + SQL + " - " + e.getMessage());
    				return false;
    			}
    			// If something went wrong, we can't get the last ID:
    			if (m_slid.compareToIgnoreCase("-1") == 0) {
    				super.addErrorMessage("Could not get last ID number.");
    				return false;
    			}
    			
    			//Now read the assigned date and name:
    			SQL = "SELECT"
    				+ " " + SMTableicpoheaders.datassigned
    				+ ", " + SMTableicpoheaders.sassignedtofullname
    				+ " FROM " + SMTableicpoheaders.TableName
    				+ " WHERE ("
    					+ "(" + SMTableicpoheaders.lid + " = " + m_slid + ")" 
    				+ ")"
    				;
    			try {
    				rs = clsDatabaseFunctions.openResultSet(SQL, conn);
    				if (rs.next()) {
    					m_sassignedtofullname = rs.getString(SMTableicpoheaders.sassignedtofullname);
    					m_sdatassigned = clsDateAndTimeConversions.resultsetDateTimeToTheMinuteStringToString(
    							rs.getString(SMTableicpoheaders.datassigned));
    				} else {
    					super.addErrorMessage("Could not get last ID number with SQL: " + SQL);
    					return false;
    				}
    				rs.close();
    			} catch (SQLException e) {
    				super.addErrorMessage("Could not get assigned date/name - with SQL: " 
    					+ SQL + " - " + e.getMessage());
    				return false;
    			}
    	}
			//If the PO is on hold, update any AP invoices that include any receipts on this PO:
    	//System.out.println("[2020731433530] " + "Going to place AP invoices on hold");
		if (getspaymentonhold().compareToIgnoreCase("1") == 0){
			try {
				placeRelatedInvoicesOnHold(conn, getsID());
			} catch (Exception e) {
				//System.out.println("[2020731436170] " + "Caught error placing invoices on hold - " + e.getMessage());
				super.addErrorMessage("Error [1584025716] - unable to place related AP invoices on hold - " + e.getMessage() + ".");
				return false;
			}
		}
    	return true;
    }

    public void placeRelatedInvoicesOnHold(Connection conn, String sPOHeaderID) throws Exception{
    	//We only worry about open AP invoices which might include receipts for this PO:
    	//Get all the receipts for this PO:
    	
    	//System.out.println("[2020731434135] " + "Placing invoices on hold for PO " + sPOHeaderID);
		boolean bSomeReceiptsAreStillUnpaid = false;
		boolean bInvoicesForThisReceiptExist = false;
    	String SQLReceiptHeaders = "SELECT"
    		+ " " + SMTableicporeceiptheaders.lid
    		+ " FROM " + SMTableicporeceiptheaders.TableName
    		+ " WHERE ("
    			+ "(" + SMTableicporeceiptheaders.lpoheaderid + " = " + sPOHeaderID + ")"
    		+ ")"
    	;
    	
    	try {
			ResultSet rsReceiptHeaders = clsDatabaseFunctions.openResultSet(SQLReceiptHeaders, conn);
			String SQLInvoiceTransactions = "";
			while (rsReceiptHeaders.next()){
				bInvoicesForThisReceiptExist = false;
				//If this receipt is on an open AP invoice, put it in hold:
				//Get all the open AP invoice lines with this receipt on them:
				SQLInvoiceTransactions = "SELECT"
					+ " " + SMTableaptransactions.TableName + "." + SMTableaptransactions.lid
					+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt
					+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.ionhold
					+ " FROM " + SMTableaptransactionlines.TableName
					+ " LEFT JOIN " + SMTableaptransactions.TableName
					+ " ON " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.ltransactionheaderid
					+ " = " + SMTableaptransactions.TableName + "." + SMTableaptransactions.lid
					+ " WHERE ("
						+ "(" + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.lreceiptheaderid + " = " + Long.toString(rsReceiptHeaders.getLong(SMTableicporeceiptheaders.lid)) + ")"
						+ " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype + " = " 
							+ Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_INVOICE) + ")"
					+ ")"
					+ " GROUP BY " + SMTableaptransactions.TableName + "." + SMTableaptransactions.lid
				;
				//If there is an AP invoice that is open, update its On Hold status now:
				try {
					ResultSet rsAPInvoices = clsDatabaseFunctions.openResultSet(SQLInvoiceTransactions, conn);
					int iInvoiceCounter = 0;
					while(rsAPInvoices.next()){
						iInvoiceCounter++;
						bInvoicesForThisReceiptExist = true;
						//Track the number of unpaid receipts:
						if (rsAPInvoices.getBigDecimal(SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt).compareTo(BigDecimal.ZERO) != 0){
							bSomeReceiptsAreStillUnpaid = true;
							//Put the invoice on hold if it's not already:
							if (rsAPInvoices.getInt(SMTableaptransactions.TableName + "." + SMTableaptransactions.ionhold) == 0){
								String SQLUpdate = "";
								try {
									SQLUpdate = "UPDATE"
										+ " " + SMTableaptransactions.TableName
										+ " SET " + SMTableaptransactions.ionhold + " = 1"
										+ ", " + SMTableaptransactions.sonholdbyfullname + " = '" + getspaymentonholdbyfullname() + "'"
										+ ", " + SMTableaptransactions.lonholdbyuserid + " = " + getlpaymentonholdbyuserid()
										+ ", " + SMTableaptransactions.lonholdpoheaderid + " = " + sPOHeaderID
										+ ", " + SMTableaptransactions.monholdreason + " = '" + getmpaymentonholdreason() + "'"
										+ ", " + SMTableaptransactions.datplacedonhold + " = '" 
											+ clsDateAndTimeConversions.stdDateTimeToSQLDateTimeInSecondsString(
												getdatpaymentplacedonhold()
											) + "'"
										+ " WHERE ("
											+ "(" + SMTableaptransactions.lid + " = " + rsAPInvoices.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.lid) + ")"
										+ ")"
									;
									Statement stmt = conn.createStatement();
									stmt.execute(SQLUpdate);
								} catch (Exception e) {
									throw new Exception("Error [2020721417255] " + "Could not update ON HOLD status of AP Invoice with SQL: '" + SQLUpdate + "' - " + e.getMessage());
								}
								
							}
						}
					}
					rsAPInvoices.close();
					//If there were NO invoices for this receipt, then there are still some unpaid receipts:
					if (iInvoiceCounter == 0){
						bSomeReceiptsAreStillUnpaid = true;
					}
				} catch (Exception e) {
					throw new Exception("Error [202072142158] " + " reading AP invoices to update on hold status with SQL '" 
						+ SQLInvoiceTransactions + "' - " + e.getMessage());
				}
			}
			rsReceiptHeaders.close();
		} catch (Exception e) {
			throw new Exception("Error [2020721424327] " + "reading receipt headers to put AP invoices on hold with SQL '" 
				+ SQLReceiptHeaders + "' - " + e.getMessage());
		}
    	
    	//System.out.println("[2020731434525] " + "PO status is " + getsstatus());
    	//System.out.println("[2020731435249] " + "bInvoicesForThisReceiptExist = " + bInvoicesForThisReceiptExist);
    	//System.out.println("[2020731435398] " + "!bSomeReceiptsAreStillUnpaid = " + !bSomeReceiptsAreStillUnpaid);
    	//If this PO is complete,and all the receipts have been paid, notify the user that they can't put it on hold:
    	if(getsstatus().compareToIgnoreCase(Integer.toString(SMTableicpoheaders.STATUS_COMPLETE)) == 0){
    		//If invoices for it exist:
    		if(
    			(bInvoicesForThisReceiptExist) && (!bSomeReceiptsAreStillUnpaid)
    		){
				//Notify the user that they can't put it on hold because every receipt has been invoiced and paid:
    			//System.out.println("[2020731433305] " + "Cannot be put on hold");
				throw new Exception("This PO is received completely, and all the invoices have been fully paid," 
					+ " so it can no longer be put on hold.");
    		}
    	}
    	return;
    }
    
    public boolean delete (ServletContext context, String sDBID, String sUserID, String sUserFullName){
    	
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBID, 
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
    	
    	boolean bResult = delete (conn, sUserFullName, sUserID);
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080898]");
    	return bResult;
    	
    }
    public boolean delete (Connection conn, String sUserFullName, String sUserID){
    	
    	//Validate deletion - do not allow deleting if there is at least one undeleted receipt on this PO:
    	String SQL = "SELECT"
    		+ " " + SMTableicporeceiptheaders.lid
    		+ " FROM " + SMTableicporeceiptheaders.TableName
    		+ " WHERE ("
    			+ "(" + SMTableicporeceiptheaders.lpoheaderid + " = " + m_slid + ")"
    			+ " AND (" + SMTableicporeceiptheaders.lstatus + " = " 
    				+ Integer.toString(SMTableicporeceiptheaders.STATUS_ENTERED) + ")"
    		+ ")"
    	;
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				super.addErrorMessage("There is at least one undeleted receipt for this PO; it cannot be deleted.");
				rs.close();
				return false;
			}
			rs.close();
		} catch (SQLException e1) {
			super.addErrorMessage(
					"Error checking for receipts before deleting this PO - " + e1.getMessage() + ".");
			return false;
		}
    	
    	//Don't actually delete - just flag record as deleted:
    	SQL = "UPDATE " + SMTableicpoheaders.TableName
    		+ " SET " + SMTableicpoheaders.datdeleted + " = NOW()"
    		+ ", " + SMTableicpoheaders.sdeletedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
    		+ ", " + SMTableicpoheaders.ldeletedbyuserid + " = " + sUserID + ""
    		+ ", " + SMTableicpoheaders.lstatus + " = " + Integer.toString(SMTableicpoheaders.STATUS_DELETED)
    		+ " WHERE ("
    			+ "(" + SMTableicpoheaders.lid + " = " + m_slid + ")"
    		+ ")"
    	;
    	try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			super.addErrorMessage("Could not flag PO as deleted - " + e.getMessage());
			return false;
		}
		
		if (!load(conn)){
			super.addErrorMessage("Could not reload PO after saving.");
			return false;
		}
    	
	return true;
    }

    public boolean validate_entry_fields (Connection conn, String sUserID){
        //Validate the entries here:
    	boolean bEntriesAreValid = true;

    	long lID;
		try {
			lID = Long.parseLong(m_slid);
		} catch (NumberFormatException e) {
        	super.addErrorMessage("Invalid PO ID: '" + m_slid + "'.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
		}
    	
    	if (lID < -1){
        	super.addErrorMessage("Invalid PO ID: '" + m_slid + "'.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
    	}
    	
    	//PO Number:
        if (m_sponumber.length() > SMTableicpoheaders.sponumberLength){
        	super.addErrorMessage("PO Number is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
    	
        //Status:
        if (
        		(m_sstatus.compareToIgnoreCase("0") != 0)
        		&& (m_sstatus.compareToIgnoreCase("1") != 0)
        		&& (m_sstatus.compareToIgnoreCase("2") != 0)
        		&& (m_sstatus.compareToIgnoreCase("3") != 0)
        ){
        	super.addErrorMessage("'Is Complete' status (" + m_sstatus + ") is invalid.");
        	bEntriesAreValid = false;	
        }
        
        //Reference:
        m_sreference = m_sreference.trim();
        if (m_sreference.length() > SMTableicpoheaders.sreferenceLength){
        	super.addErrorMessage("Reference is too long.");
        	bEntriesAreValid = false;
        }

        //Vendor:
        m_svendor = m_svendor.trim();
        APVendor ven = new APVendor();
        ven.setsvendoracct(m_svendor);
        if (!ven.load(conn)){
        	super.addErrorMessage("Vendor code " + m_svendor + " is not valid - " + ven.getErrorMessages());
        	bEntriesAreValid = false;
        }
        
        //If the vendor is inactive, don't validate the PO:
        if (ven.getsactive().compareToIgnoreCase("0") == 0){
        	super.addErrorMessage("Vendor '" + m_svendor + "' is NOT active - " + ven.getErrorMessages());
        	bEntriesAreValid = false;
        }
        
        //Vendor name:
        //TODO - should we ALWAYS update this? or only when adding a new PO?
        m_svendorname = m_svendorname.trim().replace(" ", "");
        if (ven.getsname().trim().compareToIgnoreCase("") == 0){
        	m_svendorname = "";
        }else{
        	m_svendorname = ven.getsname();
        }

    	//m_sdatpodate;
        if (m_sdatpodate.compareTo(EMPTY_DATE_STRING) != 0){
	        if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", m_sdatpodate)){
	        	super.addErrorMessage("PO date '" + m_sdatpodate + "' is invalid.  ");
	        	bEntriesAreValid = false;
	        }
        }
        
    	//m_sbillcode;
        m_sbillcode = m_sbillcode.trim();
        if (m_sbillcode.length() > SMTableicpoheaders.sbillcodeLength){
        	super.addErrorMessage("Bill to code is too long.");
        	bEntriesAreValid = false;
        }
    	//m_sbillname;
        m_sbillname = m_sbillname.trim();
        if (m_sbillname.length() > SMTableicpoheaders.sbillnameLength){
        	super.addErrorMessage("Bill to name is too long.");
        	bEntriesAreValid = false;
        }
    	//m_sbilladdress1;
        m_sbilladdress1 = m_sbilladdress1.trim();
        if (m_sbilladdress1.length() > SMTableicpoheaders.sbilladdress1Length){
        	super.addErrorMessage("Bill to address line 1 is too long.");
        	bEntriesAreValid = false;
        }
    	//m_sbilladdress2;
        m_sbilladdress2 = m_sbilladdress2.trim();
        if (m_sbilladdress2.length() > SMTableicpoheaders.sbilladdress2Length){
        	super.addErrorMessage("Bill to address line 2 is too long.");
        	bEntriesAreValid = false;
        }
    	//m_sbilladdress3;
        m_sbilladdress3 = m_sbilladdress3.trim();
        if (m_sbilladdress3.length() > SMTableicpoheaders.sbilladdress3Length){
        	super.addErrorMessage("Bill to address line 3 is too long.");
        	bEntriesAreValid = false;
        }
    	//m_sbilladdress4;
        m_sbilladdress4 = m_sbilladdress4.trim();
        if (m_sbilladdress4.length() > SMTableicpoheaders.sbilladdress4Length){
        	super.addErrorMessage("Bill to address line 4 is too long.");
        	bEntriesAreValid = false;
        }
    	//m_sbillcity;
        m_sbillcity = m_sbillcity.trim();
        if (m_sbillcity.length() > SMTableicpoheaders.sbillcityLength){
        	super.addErrorMessage("Bill to city is too long.");
        	bEntriesAreValid = false;
        }
    	//m_sbillstate;
        m_sbillstate = m_sbillstate.trim();
        if (m_sbillstate.length() > SMTableicpoheaders.sbillstateLength){
        	super.addErrorMessage("Bill to state is too long.");
        	bEntriesAreValid = false;
        }
    	//m_sbillpostalcode;
        m_sbillpostalcode = m_sbillpostalcode.trim();
        if (m_sbillpostalcode.length() > SMTableicpoheaders.sbillpostalcodeLength){
        	super.addErrorMessage("Bill to postal code is too long.");
        	bEntriesAreValid = false;
        }
    	//m_sbillcountry;
        m_sbillcountry = m_sbillcountry.trim();
        if (m_sbillcountry.length() > SMTableicpoheaders.sbillcountryLength){
        	super.addErrorMessage("Bill to country is too long.");
        	bEntriesAreValid = false;
        }        
    	//m_sbillphone;
        m_sbillphone = m_sbillphone.trim();
        if (m_sbillphone.length() > SMTableicpoheaders.sbillphoneLength){
        	super.addErrorMessage("Bill to phone is too long.");
        	bEntriesAreValid = false;
        }
    	//m_sbillfax;
        m_sbillfax = m_sbillfax.trim();
        if (m_sbillfax.length() > SMTableicpoheaders.sbillfaxLength){
        	super.addErrorMessage("Bill to fax is too long.");
        	bEntriesAreValid = false;
        }
    	//m_sbillcontactname;
        m_sbillcontactname = m_sbillcontactname.trim();
        if (m_sbillcontactname.length() > SMTableicpoheaders.sbillcontactnameLength){
        	super.addErrorMessage("Bill to contact name is too long.");
        	bEntriesAreValid = false;
        }
    	//m_sshipcode;
        m_sshipcode = m_sshipcode.trim();
        if (m_sshipcode.length() > SMTableicpoheaders.sshipcodeLength){
        	super.addErrorMessage("Ship to name is too long.");
        	bEntriesAreValid = false;
        }
    	//m_sshipname;
        m_sshipname = m_sshipname.trim();
        if (m_sshipname.length() > SMTableicpoheaders.sshipnameLength){
        	super.addErrorMessage("Ship to name is too long.");
        	bEntriesAreValid = false;
        }
    	//m_sshipaddress1;
        m_sshipaddress1 = m_sshipaddress1.trim();
        if (m_sshipaddress1.length() > SMTableicpoheaders.sshipaddress1Length){
        	super.addErrorMessage("Ship to address line 1 is too long.");
        	bEntriesAreValid = false;
        }
    	//m_sshipaddress2;
        m_sshipaddress2 = m_sshipaddress2.trim();
        if (m_sshipaddress2.length() > SMTableicpoheaders.sshipaddress2Length){
        	super.addErrorMessage("Ship to address line 2 is too long.");
        	bEntriesAreValid = false;
        }
    	//m_sshipaddress3;
        m_sshipaddress3 = m_sshipaddress3.trim();
        if (m_sshipaddress3.length() > SMTableicpoheaders.sshipaddress3Length){
        	super.addErrorMessage("Ship to address line 3 is too long.");
        	bEntriesAreValid = false;
        }
    	//m_sshipaddress4;
        m_sshipaddress4 = m_sshipaddress4.trim();
        if (m_sshipaddress4.length() > SMTableicpoheaders.sshipaddress4Length){
        	super.addErrorMessage("Ship to address line 4 is too long.");
        	bEntriesAreValid = false;
        }

    	//m_sshipcity;
        m_sshipcity = m_sshipcity.trim();
        if (m_sshipcity.length() > SMTableicpoheaders.sshipcityLength){
        	super.addErrorMessage("Ship to city is too long.");
        	bEntriesAreValid = false;
        }
    	//m_sshipstate;
        m_sshipstate = m_sshipstate.trim();
        if (m_sshipstate.length() > SMTableicpoheaders.sshipstateLength){
        	super.addErrorMessage("Ship to state is too long.");
        	bEntriesAreValid = false;
        }
    	//m_sshippostalcode;
        m_sshippostalcode = m_sshippostalcode.trim();
        if (m_sshippostalcode.length() > SMTableicpoheaders.sshippostalcodeLength){
        	super.addErrorMessage("Ship to postal code is too long.");
        	bEntriesAreValid = false;
        }
    	//m_sshipcountry;
        m_sshipcountry = m_sshipcountry.trim();
        if (m_sshipcountry.length() > SMTableicpoheaders.sshipcountryLength){
        	super.addErrorMessage("Ship to country is too long.");
        	bEntriesAreValid = false;
        }        
    	//m_sshipphone;
        m_sshipphone = m_sshipphone.trim();
        if (m_sshipphone.length() > SMTableicpoheaders.sshipphoneLength){
        	super.addErrorMessage("Ship to phone is too long.");
        	bEntriesAreValid = false;
        }
    	//m_sshipfax;
        m_sshipfax = m_sshipfax.trim();
        if (m_sshipfax.length() > SMTableicpoheaders.sshipfaxLength){
        	super.addErrorMessage("Ship to fax is too long.");
        	bEntriesAreValid = false;
        }
    	//m_sshipcontactname;
        m_sshipcontactname = m_sshipcontactname.trim();
        if (m_sshipcontactname.length() > SMTableicpoheaders.sshipcontactnameLength){
        	super.addErrorMessage("Ship to contact name is too long.");
        	bEntriesAreValid = false;
        }
    	//m_sshipviacode;
        m_sshipviacode = m_sshipviacode.trim();
        if (m_sshipviacode.length() > SMTableicpoheaders.sshipviacodeLength){
        	super.addErrorMessage("Ship via code is too long.");
        	bEntriesAreValid = false;
        }
        if (m_sshipviacode.compareToIgnoreCase(ICEditPOEdit.SHIPVIA_LIST_OPTION_NOT_CHOSEN_VALUE) == 0){
        	super.addErrorMessage("No ship via code selected from the list.");
        	bEntriesAreValid = false;
        }
    	//m_sshipvianame;
        //If there's a ship via code, the ship via name will be updated.  Otherwise, it's whatever
        //was passed in here:
        loadShipViaInformation(conn);
        m_sshipvianame = m_sshipvianame.trim();
        if (m_sshipviacode.length() > SMTableicpoheaders.sshipvianameLength){
        	super.addErrorMessage("Ship via name is too long.");
        	bEntriesAreValid = false;
        }

    	//m_sdatexpecteddate;
        if (m_sdatexpecteddate.compareTo(EMPTY_DATE_STRING) != 0){
	        if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", m_sdatexpecteddate)){
	        	super.addErrorMessage("Expected date '" + m_sdatexpecteddate + "' is invalid.  ");
	        	bEntriesAreValid = false;
	        }
        }
        
        m_sassignedtofullname = m_sassignedtofullname.trim();
        if (m_sassignedtofullname.length() > SMTableicpoheaders.sassignedtonameLength){
        	super.addErrorMessage("Assigned to name is too long.");
        	bEntriesAreValid = false;
        }
        
        m_scomment = m_scomment.trim();
        if (m_scomment.length() > SMTableicpoheaders.scommentLength){
        	super.addErrorMessage("Comment is too long.");
        	bEntriesAreValid = false;
        }

        m_sdescription = m_sdescription.trim();
        if (m_sdescription.length() > SMTableicpoheaders.sdescriptionLength){
        	super.addErrorMessage("Description is too long.");
        	bEntriesAreValid = false;
        }

        //Payment on hold logic:
		try {
			m_ipaymentonhold  = clsValidateFormFields.validateLongIntegerField(m_ipaymentonhold, "Payment on hold", 0L, 1L);
		} catch (Exception e) {
			super.addErrorMessage(e.getMessage() + ".");
			bEntriesAreValid = false;
		}
        
        //Get the existing record, if there is one, and determine if it was already on hold:

        //If the payment is on hold, then if it's a new PO we need to update the related fields:
        boolean bOnHoldIsBeingSet = false;
        if (getspaymentonhold().compareToIgnoreCase("1") == 0){
        	//If it's a NEW PO, then we know the on hold is being set now:
        	if (lID <= 0){
        		bOnHoldIsBeingSet = true;
        	//But if it's an EXISTING PO, then we have to see if it was already set or not:
        	}else{
            	ICPOHeader pohead = new ICPOHeader();
            	pohead.setsID(m_slid);
            	if (!pohead.load(conn)){
                	super.addErrorMessage("Could not load previous version of PO with ID '" + m_slid + "' - " + pohead.getErrorMessages() + ".");
                	bEntriesAreValid = false;
            	}else{
            		//Now figure out if the payment on hold status has changed:
            		//If it was NOT on hold before, but it is now:
            		if (pohead.getspaymentonhold().compareToIgnoreCase("0") == 0){
            			bOnHoldIsBeingSet = true;
            		}
            	}
        	}
        }
        if (bOnHoldIsBeingSet){
			//We need to update the user id, date, etc.
			setlpaymentonholdbyuserid(sUserID);
			//Get the full user name:
			String SQL = "SELECT"
				+ " " + SMTableusers.sUserFirstName
				+ ", " + SMTableusers.sUserLastName
				+ " FROM " + SMTableusers.TableName
				+ " WHERE ("
					+ "(" + SMTableusers.lid + " = " + sUserID + ")"
				+ ")"
			;
			try {
				ResultSet rsUser = ServletUtilities.clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rsUser.next()){
					setspaymentonholdbyfullname(rsUser.getString(SMTableusers.sUserFirstName) + " " + rsUser.getString(SMTableusers.sUserLastName));
				}else{
					setspaymentonholdbyfullname("N/A");
				}
				rsUser.close();
			} catch (SQLException e) {
				super.addErrorMessage("Error [1583526341] getting full user name for placing payment on hold with SQL: '"
					+ SQL + "' - " + e.getMessage() + ".");
				bEntriesAreValid = false;
			}
			//Get the on hold date:
			ServletUtilities.clsDBServerTime dbtime;
			try {
				dbtime = new ServletUtilities.clsDBServerTime(conn);
				setdatpaymentplacedonhold(dbtime.getCurrentDateTimeInSelectedFormat(SMUtilities.DATETIME_FORMAT_FOR_DISPLAY));
				//System.out.println("[202066171436] " + "this.getdatpaymentplacedonhold = '" + this.getdatpaymentplacedonhold() + "'.");
			} catch (Exception e) {
				super.addErrorMessage("Error [1583526342] getting current date/time for placing payment on hold - " + e.getMessage() + ".");
					bEntriesAreValid = false;
			}
        }

		//If the payment is not on hold, make sure we clear all the related fields:
		if (m_ipaymentonhold.compareToIgnoreCase("0") == 0){
			m_spaymentonholdbyfullname = "";
			m_lpaymentonholdbyuserid = "0";
			m_datpaymentplacedonhold = SMUtilities.EMPTY_DATETIME_VALUE;
			m_mpaymentonholdreason = "";
			m_mpaymentonholdvendorcomment = "";
		}
		
        m_spaymentonholdbyfullname = m_spaymentonholdbyfullname.trim();
        if (m_spaymentonholdbyfullname.length() > SMTableicpoheaders.spaymentonholdbyfullnamelength){
        	super.addErrorMessage("Payment on hold by full name is too long.");
        	bEntriesAreValid = false;
        }
        
		try {
			m_lpaymentonholdbyuserid  = clsValidateFormFields.validateLongIntegerField(m_lpaymentonholdbyuserid, "Payment on hold user ID", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			super.addErrorMessage(e.getMessage() + ".");
			bEntriesAreValid = false;
		}
		
		try {
			m_datpaymentplacedonhold  = clsValidateFormFields.validateDateTimeField(
				m_datpaymentplacedonhold, 
				"Date payment placed on hold", 
				SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, 
				true);
		} catch (Exception e1) {
			super.addErrorMessage(e1.getMessage() + ".");
			bEntriesAreValid = false;
		}
		
		//System.out.println("[20206617470] " + "m_datpaymentplacedonhold = '" + m_datpaymentplacedonhold + "'.");
		
        m_mpaymentonholdreason = m_mpaymentonholdreason.trim();
        if (m_ipaymentonhold.compareToIgnoreCase("1") == 0){
        	if (m_mpaymentonholdreason.compareToIgnoreCase("") == 0){
        		//Set the on hold fields back to blanks:
        		setipaymentonhold("0");
        		setdatpaymentplacedonhold(SMUtilities.EMPTY_DATETIME_VALUE);
        		setlpaymentonholdbyuserid("0");
        		setspaymentonholdbyfullname("");
        		setmpaymentonholdreason("");
        		setmpaymentonholdvendorcomment("");
        		super.addErrorMessage("If the payment is on hold, it must have a reason noted.");
        		bEntriesAreValid = false;
        	}
        }
        
        m_mpaymentonholdvendorcomment = m_mpaymentonholdvendorcomment.trim();
        
        return bEntriesAreValid;
    }

    public String read_out_debug_data(){
    	String sResult = "  ** ICPOHeader read out: ";
    	sResult += "\nID: " + this.getsID();
    	sResult += "\nPO Number: " + this.getsponumber();
    	sResult += "\nVendor: " + this.getsvendor();
    	sResult += "\nVendor Name: " + this.getsvendorname();
    	sResult += "\nReference: " + this.getsreference();
    	sResult += "\nStatus: " + this.getsstatus();
    	sResult += "\nPO Date: " + this.getspodate();
    	sResult += "\nBill to code: " + this.getsbillname();
    	sResult += "\nBill to name: " + this.getsbillname();
    	sResult += "\nBill address 1: " + this.getsbilladdress1();
    	sResult += "\nBill address 1: " + this.getsbilladdress2();
    	sResult += "\nBill address 1: " + this.getsbilladdress3();
    	sResult += "\nBill address 1: " + this.getsbilladdress4();
    	sResult += "\nBill city: " + this.getsbillcity();
    	sResult += "\nBill state: " + this.getsbillstate();
    	sResult += "\nBill postal code: " + this.getsbillpostalcode();
    	sResult += "\nBill country: " + this.getsbillcountry();
    	sResult += "\nBill phone: " + this.getsbillphone();
    	sResult += "\nBill fax: " + this.getsbillfax();
    	sResult += "\nBill contact name: " + this.getsbillcontactname();
    	sResult += "\nShip to code: " + this.getsshipname();
    	sResult += "\nShip to name: " + this.getsshipname();
    	sResult += "\nShip address 1: " + this.getsshipaddress1();
    	sResult += "\nShip address 1: " + this.getsshipaddress2();
    	sResult += "\nShip address 1: " + this.getsshipaddress3();
    	sResult += "\nShip address 1: " + this.getsshipaddress4();
    	sResult += "\nShip city: " + this.getsshipcity();
    	sResult += "\nShip state: " + this.getsshipstate();
    	sResult += "\nShip postal code: " + this.getsshippostalcode();
    	sResult += "\nShip country: " + this.getsshipcountry();
    	sResult += "\nShip phone: " + this.getsshipphone();
    	sResult += "\nShip fax: " + this.getsshipfax();
    	sResult += "\nShip contact name: " + this.getsshipcontactname();
    	sResult += "\nShip via code: " + this.getsshipviacode();
    	sResult += "\nShip via name: " + this.getsshipvianame();
    	sResult += "\nExpected date: " + this.getsdatexpecteddate();
    	sResult += "\nAssigned date: " + this.getsassigneddate();
    	sResult += "\nAssigned to name: " + this.getsassignedtofullname();
    	sResult += "\nComment: " + this.getscomment();
    	sResult += "\nDescription: " + this.getsdescription();
    	sResult += "\nDeleted by: " + this.getsdeletedbyfullname();
    	sResult += "\nDeleted date: " + this.getsdeleteddate();
    	sResult += "\nObject name: " + this.getObjectName();
    	sResult += "\nGDoc Link: " + this.getsgdoclink();
    	sResult += "\nPayment on hold: " + this.getspaymentonhold();
    	sResult += "\nPayment on hold by full user name: " + this.getspaymentonholdbyfullname();
    	sResult += "\nPayment on hold by user ID: " + this.getlpaymentonholdbyuserid();
    	sResult += "\nDate payment placed on hold: " + this.getdatpaymentplacedonhold();
    	sResult += "\nPayment on hold reason: " + this.getmpaymentonholdreason();
    	sResult += "\nOn hold vendor comment: " + this.getmpaymentonholdvendorcomment();
    	return sResult;
    }

    public void addErrorMessage(String sMsg){
    	super.addErrorMessage(sMsg);
    }
	public String getQueryString(){
		//Particular to the specific class
		String sQueryString = "";
		sQueryString += ParamObjectName + "=" + clsServletUtilities.URLEncode(this.getObjectName());
		sQueryString += "&" + Paramlid + "=" + clsServletUtilities.URLEncode(this.getsID());
		sQueryString += "&" + Paramllstatus + "=" + clsServletUtilities.URLEncode(getsstatus());
		sQueryString += "&" + Paramsponumber + "=" + clsServletUtilities.URLEncode(getsponumber());
		sQueryString += "&" + Paramsreference + "=" + clsServletUtilities.URLEncode(getsreference());
		sQueryString += "&" + Paramsvendor + "=" + clsServletUtilities.URLEncode(getsvendor());
		sQueryString += "&" + Paramsvendorname + "=" + clsServletUtilities.URLEncode(getsvendorname());
		sQueryString += "&" + Paramdatpodate + "=" + clsServletUtilities.URLEncode(getspodate());
		sQueryString += "&" + Paramsbillcode + "=" + clsServletUtilities.URLEncode(getsbillcode());
		sQueryString += "&" + Paramsbillname + "=" + clsServletUtilities.URLEncode(getsbillname());
		sQueryString += "&" + Paramsbilladdress1 + "=" + clsServletUtilities.URLEncode(getsbilladdress1());
		sQueryString += "&" + Paramsbilladdress2 + "=" + clsServletUtilities.URLEncode(getsbilladdress2());
		sQueryString += "&" + Paramsbilladdress3 + "=" + clsServletUtilities.URLEncode(getsbilladdress3());
		sQueryString += "&" + Paramsbilladdress4 + "=" + clsServletUtilities.URLEncode(getsbilladdress4());
		sQueryString += "&" + Paramsbillcity + "=" + clsServletUtilities.URLEncode(getsbillcity());
		sQueryString += "&" + Paramsbillstate + "=" + clsServletUtilities.URLEncode(getsbillstate());
		sQueryString += "&" + Paramsbillpostalcode + "=" + clsServletUtilities.URLEncode(getsbillpostalcode());
		sQueryString += "&" + Paramsbillcountry + "=" + clsServletUtilities.URLEncode(getsbillcountry());
		sQueryString += "&" + Paramsbillphone + "=" + clsServletUtilities.URLEncode(getsbillphone());
		sQueryString += "&" + Paramsbillfax + "=" + clsServletUtilities.URLEncode(getsbillfax());
		sQueryString += "&" + Paramsbillcontactname + "=" + clsServletUtilities.URLEncode(getsbillcontactname());
		sQueryString += "&" + Paramsshipcode + "=" + clsServletUtilities.URLEncode(getsshipcode());
		sQueryString += "&" + Paramsshipname + "=" + clsServletUtilities.URLEncode(getsshipname());
		sQueryString += "&" + Paramsshipaddress1 + "=" + clsServletUtilities.URLEncode(getsshipaddress1());
		sQueryString += "&" + Paramsshipaddress2 + "=" + clsServletUtilities.URLEncode(getsshipaddress2());
		sQueryString += "&" + Paramsshipaddress3 + "=" + clsServletUtilities.URLEncode(getsshipaddress3());
		sQueryString += "&" + Paramsshipaddress4 + "=" + clsServletUtilities.URLEncode(getsshipaddress4());
		sQueryString += "&" + Paramsshipcity + "=" + clsServletUtilities.URLEncode(getsshipcity());
		sQueryString += "&" + Paramsshipstate + "=" + clsServletUtilities.URLEncode(getsshipstate());
		sQueryString += "&" + Paramsshippostalcode + "=" + clsServletUtilities.URLEncode(getsshippostalcode());
		sQueryString += "&" + Paramsshipcountry + "=" + clsServletUtilities.URLEncode(getsshipcountry());
		sQueryString += "&" + Paramsshipphone + "=" + clsServletUtilities.URLEncode(getsshipphone());
		sQueryString += "&" + Paramsshipfax + "=" + clsServletUtilities.URLEncode(getsshipfax());
		sQueryString += "&" + Paramsshipcontactname + "=" + clsServletUtilities.URLEncode(getsshipcontactname());
		sQueryString += "&" + Paramsshipviacode + "=" + clsServletUtilities.URLEncode(getsshipviacode());
		sQueryString += "&" + Paramsshipvianame + "=" + clsServletUtilities.URLEncode(getsshipvianame());
		sQueryString += "&" + Paramsdatexpecteddate + "=" + clsServletUtilities.URLEncode(getsdatexpecteddate());
		sQueryString += "&" + Paramdatassigned + "=" + clsServletUtilities.URLEncode(getsassigneddate());
		sQueryString += "&" + Paramsassignedtofullname + "=" + clsServletUtilities.URLEncode(getsassignedtofullname());
		sQueryString += "&" + Paramscomment + "=" + clsServletUtilities.URLEncode(getscomment());
		sQueryString += "&" + Paramsdescription + "=" + clsServletUtilities.URLEncode(getsdescription());
		sQueryString += "&" + Paramsdeletedbyfullname + "=" + clsServletUtilities.URLEncode(getsdeletedbyfullname());
		sQueryString += "&" + Paramsdatdeleted + "=" + clsServletUtilities.URLEncode(getsdeleteddate());
		sQueryString += "&" + Paramsgdoclink + "=" + clsServletUtilities.URLEncode(getsgdoclink());
		
		sQueryString += "&" + Paramsgdoclink + "=" + clsServletUtilities.URLEncode(getsgdoclink());
		sQueryString += "&" + Paramsgdoclink + "=" + clsServletUtilities.URLEncode(getsgdoclink());
		sQueryString += "&" + Paramsgdoclink + "=" + clsServletUtilities.URLEncode(getsgdoclink());
		sQueryString += "&" + Paramsgdoclink + "=" + clsServletUtilities.URLEncode(getsgdoclink());
		sQueryString += "&" + Paramsgdoclink + "=" + clsServletUtilities.URLEncode(getsgdoclink());
		sQueryString += "&" + Paramsgdoclink + "=" + clsServletUtilities.URLEncode(getsgdoclink());
		
		sQueryString += "&" + Paramipaymentonhold + "=" + clsServletUtilities.URLEncode(getspaymentonhold());
		sQueryString += "&" + Paramspaymentonholdbyfullname + "=" + clsServletUtilities.URLEncode(getspaymentonholdbyfullname());
		sQueryString += "&" + Paramlpaymentonholdbyuserid + "=" + clsServletUtilities.URLEncode(getlpaymentonholdbyuserid());
		sQueryString += "&" + Paramdatpaymentplacedonhold + "=" + clsServletUtilities.URLEncode(getdatpaymentplacedonhold());
		sQueryString += "&" + Parammpaymentonholdreason + "=" + clsServletUtilities.URLEncode(getmpaymentonholdreason());
		sQueryString += "&" + Parammpaymentonholdvendorcomment + "=" + clsServletUtilities.URLEncode(getmpaymentonholdvendorcomment());
		return sQueryString;
	}

	public String getsID() {
		return m_slid;
	}
	public void setsID(String sID) {
		this.m_slid = sID;
	}
	public String getsvendor() {
		return m_svendor;
	}
	public void setsvendor(String svendor) {
		this.m_svendor = svendor;
	}
	public String getsponumber() {
		return m_sponumber;
	}
	public void setsponumber(String sponumber) {
		this.m_sponumber = sponumber;
	}
	public String getsvendorname() {
		return m_svendorname;
	}
	public void setsvendorname(String svendorname) {
		this.m_svendorname = svendorname;
	}
	public String getsreference() {
		return m_sreference;
	}
	public void setsreference(String sreference) {
		this.m_sreference = sreference;
	}
	public String getsstatus() {
		return m_sstatus;
	}
	public void setsstatus(String siscomplete) {
		this.m_sstatus = siscomplete;
	}
	public String getspodate() {
		return m_sdatpodate;
	}
	public void setspodate(String spodate) {
		this.m_sdatpodate = spodate;
	}
	public String getsshipcode() {
		return m_sshipcode;
	}
	public void setsshipcode(String sshipcode) {
		this.m_sshipcode = sshipcode;
	}
	public String getsshipname() {
		return m_sshipname;
	}
	public void setsshipname(String sshipname) {
		this.m_sshipname = sshipname;
	}
	public String getsshipaddress1() {
		return m_sshipaddress1;
	}
	public void setsshipaddress1(String sshipaddress1) {
		this.m_sshipaddress1 = sshipaddress1;
	}
	public String getsshipaddress2() {
		return m_sshipaddress2;
	}
	public void setsshipaddress2(String sshipaddress2) {
		this.m_sshipaddress2 = sshipaddress2;
	}
	public String getsshipaddress3() {
		return m_sshipaddress3;
	}
	public void setsshipaddress3(String sshipaddress3) {
		this.m_sshipaddress3 = sshipaddress3;
	}
	public String getsshipaddress4() {
		return m_sshipaddress4;
	}
	public void setsshipaddress4(String sshipaddress4) {
		this.m_sshipaddress4 = sshipaddress4;
	}
	public String getsshipcity() {
		return m_sshipcity;
	}
	public void setsshipcity(String sshipcity) {
		this.m_sshipcity = sshipcity;
	}
	public String getsshipstate() {
		return m_sshipstate;
	}
	public void setsshipstate(String sshipstate) {
		this.m_sshipstate = sshipstate;
	}
	public String getsshippostalcode() {
		return m_sshippostalcode;
	}
	public void setsshippostalcode(String sshippostalcode) {
		this.m_sshippostalcode = sshippostalcode;
	}
	public String getsshipcountry() {
		return m_sshipcountry;
	}
	public void setsshipcountry(String sshipcountry) {
		this.m_sshipcountry = sshipcountry;
	}
	public String getsshipphone() {
		return m_sshipphone;
	}
	public void setsshipphone(String sshipphone) {
		this.m_sshipphone = sshipphone;
	}
	public String getsshipfax() {
		return m_sshipfax;
	}
	public void setsshipfax(String sshipfax) {
		this.m_sshipfax = sshipfax;
	}
	public String getsshipcontactname() {
		return m_sshipcontactname;
	}
	public void setsshipcontactname(String sshipcontactname) {
		this.m_sshipcontactname = sshipcontactname;
	}
	public String getsbillcode() {
		return m_sbillcode;
	}
	public void setsbillcode(String sbillcode) {
		this.m_sbillcode = sbillcode;
	}
	public String getsbillname() {
		return m_sbillname;
	}
	public void setsbillname(String sbillname) {
		this.m_sbillname = sbillname;
	}
	public String getsbilladdress1() {
		return m_sbilladdress1;
	}
	public void setsbilladdress1(String sbilladdress1) {
		this.m_sbilladdress1 = sbilladdress1;
	}
	public String getsbilladdress2() {
		return m_sbilladdress2;
	}
	public void setsbilladdress2(String sbilladdress2) {
		this.m_sbilladdress2 = sbilladdress2;
	}
	public String getsbilladdress3() {
		return m_sbilladdress3;
	}
	public void setsbilladdress3(String sbilladdress3) {
		this.m_sbilladdress3 = sbilladdress3;
	}
	public String getsbilladdress4() {
		return m_sbilladdress4;
	}
	public void setsbilladdress4(String sbilladdress4) {
		this.m_sbilladdress4 = sbilladdress4;
	}
	public String getsbillcity() {
		return m_sbillcity;
	}
	public void setsbillcity(String sbillcity) {
		this.m_sbillcity = sbillcity;
	}
	public String getsbillstate() {
		return m_sbillstate;
	}
	public void setsbillstate(String sbillstate) {
		this.m_sbillstate = sbillstate;
	}
	public String getsbillpostalcode() {
		return m_sbillpostalcode;
	}
	public void setsbillpostalcode(String sbillpostalcode) {
		this.m_sbillpostalcode = sbillpostalcode;
	}
	public String getsbillcountry() {
		return m_sbillcountry;
	}
	public void setsbillcountry(String sbillcountry) {
		this.m_sbillcountry = sbillcountry;
	}
	public String getsbillphone() {
		return m_sbillphone;
	}
	public void setsbillphone(String sbillphone) {
		this.m_sbillphone = sbillphone;
	}
	public String getsbillfax() {
		return m_sbillfax;
	}
	public void setsbillfax(String sbillfax) {
		this.m_sbillfax = sbillfax;
	}
	public String getsbillcontactname() {
		return m_sbillcontactname;
	}
	public void setsbillcontactname(String sbillcontactname) {
		this.m_sbillcontactname = sbillcontactname;
	}
	public String getsshipviacode() {
		return m_sshipviacode;
	}
	public void setsshipviacode(String sshipviacode) {
		this.m_sshipviacode = sshipviacode;
	}
	public String getsshipvianame() {
		return m_sshipvianame;
	}
	public void setsshipvianame(String sshipvianame) {
		this.m_sshipvianame = sshipvianame;
	}
	public String getsdatexpecteddate() {
		return m_sdatexpecteddate;
	}
	public void setsdatexpecteddate(String sdatexpecteddate) {
		this.m_sdatexpecteddate = sdatexpecteddate;
	}
	public String getsassigneddate() {
		return m_sdatassigned;
	}
	public void setsassigneddate(String sassigneddate) {
		this.m_sdatassigned = sassigneddate;
	}
	public String getsassignedtofullname() {
		return m_sassignedtofullname;
	}
	public void setsassignedtofullname(String sassignedtofullname) {
		this.m_sassignedtofullname = sassignedtofullname;
	}
	public String getscomment(){
		return m_scomment;
	}
	public void setscomment(String scomment){
		m_scomment = scomment;
	}
	public String getsdescription(){
		return m_sdescription;
	}
	public void setsdescription(String sdescription){
		m_sdescription = sdescription;
	}
	public String getsgdoclink(){
		return m_sgdoclink;
	}
	public void setsgdoclink(String sgdoclink){
		m_sgdoclink = sgdoclink;
	}
	public String getsdeletedbyfullname(){
		return m_sdeletedbyfullname;
	}
	public String getsdeleteddate() {
		return m_sdatdeleted;
	}
	public String getscreatedbyfullname(){
		return m_screatedbyfullname;
	}
	public void setcreatedbyfullname(String screatedbyfullname){
		m_screatedbyfullname = screatedbyfullname;
	}
	public String getspaymentonhold(){
		return m_ipaymentonhold;
	}
	public void setipaymentonhold(String spaymentonhold){
		m_ipaymentonhold = spaymentonhold;
	}
	public String getspaymentonholdbyfullname(){
		return m_spaymentonholdbyfullname;
	}
	public void setspaymentonholdbyfullname(String spaymentonholdbyfullname){
		m_spaymentonholdbyfullname = spaymentonholdbyfullname;
	}
	public String getlpaymentonholdbyuserid(){
		return m_lpaymentonholdbyuserid;
	}
	public void setlpaymentonholdbyuserid(String smpaymentonholdbyuserid){
		m_lpaymentonholdbyuserid = smpaymentonholdbyuserid;
	}
	public String getdatpaymentplacedonhold(){
		return m_datpaymentplacedonhold;
	}
	public void setdatpaymentplacedonhold(String sdatpaymentplacedonhold){
		m_datpaymentplacedonhold = sdatpaymentplacedonhold;
	}
	public String getmpaymentonholdreason(){
		return m_mpaymentonholdreason;
	}
	public void setmpaymentonholdreason(String mpaymentonholdreason){
		m_mpaymentonholdreason = mpaymentonholdreason;
	}
	public String getmpaymentonholdvendorcomment(){
		return m_mpaymentonholdvendorcomment;
	}
	public void setmpaymentonholdvendorcomment(String mpaymentonholdvendorcomment){
		m_mpaymentonholdvendorcomment = mpaymentonholdvendorcomment;
	}
	
	public boolean loadDefaultBillToInformation(ServletContext context, String sDBID, String sUser){
		//If the ship to code is blank, don't update the ship to info:
		if (m_sbillcode.compareToIgnoreCase("") == 0){
			return true;
		}
		String SQL = "SELECT * FROM " + SMTablelocations.TableName
			+ " WHERE ("
				+ "(" + SMTablelocations.sLocation + " = '" + m_sbillcode + "')"
			+ ")"
			;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) 
						+ ".loadDefaultBillToInformation - user: " + sUser
			);
			if (rs.next()){
				m_sbillname = rs.getString(SMTablelocations.sCompanyDescription);
				m_sbilladdress1 = rs.getString(SMTablelocations.sAddress1);
				m_sbilladdress2 = rs.getString(SMTablelocations.sAddress2);
				m_sbilladdress3 = rs.getString(SMTablelocations.sAddress3);
				m_sbilladdress4 = rs.getString(SMTablelocations.sAddress4);
				m_sbillcity = rs.getString(SMTablelocations.sCity);
				m_sbillstate = rs.getString(SMTablelocations.sState);
				m_sbillpostalcode = rs.getString(SMTablelocations.sZip);
				m_sbillcountry = rs.getString(SMTablelocations.sCountry);
				m_sbillphone = rs.getString(SMTablelocations.sPhone);
				m_sbillfax = rs.getString(SMTablelocations.sFax);
				m_sbillcontactname = rs.getString(SMTablelocations.sContact);
				rs.close();
			}else{
				rs.close();
				super.addErrorMessage("No location record for bill to code '" + m_sbillcode + "'");
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error reading location record for bill to code '" + m_sbillcode + "' = "
				+ e.getMessage());
		}
		
		return true;
	}
	public boolean loadDefaultShipToInformation(ServletContext context, String sDBID, String sUser){
		
		//If the ship to code is blank, don't update the ship to info:
		if (m_sshipcode.compareToIgnoreCase("") == 0){
			return true;
		}
		
		String SQL = "SELECT * FROM " + SMTablelocations.TableName
			+ " WHERE ("
				+ "(" + SMTablelocations.sLocation + " = '" + m_sshipcode + "')"
			+ ")"
			;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) 
						+ ".loadDefaultShipToInformation - user: " + sUser
			);
			if (rs.next()){
				m_sshipname = rs.getString(SMTablelocations.sCompanyDescription);
				m_sshipaddress1 = rs.getString(SMTablelocations.sAddress1);
				m_sshipaddress2 = rs.getString(SMTablelocations.sAddress2);
				m_sshipaddress3 = rs.getString(SMTablelocations.sAddress3);
				m_sshipaddress4 = rs.getString(SMTablelocations.sAddress4);
				m_sshipcity = rs.getString(SMTablelocations.sCity);
				m_sshipstate = rs.getString(SMTablelocations.sState);
				m_sshippostalcode = rs.getString(SMTablelocations.sZip);
				m_sshipcountry = rs.getString(SMTablelocations.sCountry);
				m_sshipphone = rs.getString(SMTablelocations.sPhone);
				m_sshipfax = rs.getString(SMTablelocations.sFax);
				m_sshipcontactname = rs.getString(SMTablelocations.sContact);
				rs.close();
			}else{
				rs.close();
				super.addErrorMessage("No location record for ship to code '" + m_sshipcode + "'");
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error reading location record for ship to code '" + m_sshipcode + "' = "
				+ e.getMessage());
		}
		
		return true;
	}
	public boolean loadShipViaInformation(Connection conn){
		
		//If the ship to code is blank, don't update the ship to info:
		if (m_sshipviacode.compareToIgnoreCase("") == 0){
			return true;
		}
		
		String SQL = "SELECT * FROM " + SMTableicshipvias.TableName
			+ " WHERE ("
				+ "(" + SMTableicshipvias.sshipviacode + " = '" + m_sshipviacode + "')"
			+ ")"
			;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					conn
			);
			if (rs.next()){
				m_sshipvianame = rs.getString(SMTableicshipvias.sshipvianame);
				rs.close();
			}else{
				rs.close();
				super.addErrorMessage("No ship via record for ship via code.");
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error reading ship via record for ship via code '" + m_sshipviacode + "' = "
				+ e.getMessage());
		}
		
		return true;
	}
	public void updateVendor(String sVendorAcct, ServletContext context, String sDBID, String sUserID, String sUserFullName) throws Exception{
		//First, check to make sure that no receipts on this PO have already been invoiced:
		String SQL = "SELECT"
			+ " " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lpoinvoiceid
			+ " FROM " + SMTableicporeceiptheaders.TableName + " INNER JOIN " + SMTableicporeceiptlines.TableName
			+ " ON " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lreceiptheaderid + " = "
			+ SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
			+ " WHERE ("
				+ "(" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpoheaderid + " = " + getsID() + ")"
				+ " AND (" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lpoinvoiceid + " > 0)"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".updateVendor - user: " + sUserFullName);
			if (rs.next()){
				rs.close();
				throw new Exception("Some receipts for PO #" + getsID() + " have already been invoiced so the vendor cannot be updated.");
			}else{
				rs.close();
			}
		} catch (Exception e) {
			throw new Exception("Error [1400010702] reading receipts for PO #" + getsID() + " to update vendor. SQL = " + SQL + " - " + e.getMessage() );
		}
		//Remember the original vendor:
		String sOriginalVendor = getsvendor();
		
		//Next, update the vendor
		APVendor ven = new APVendor();
		ven.setsvendoracct(sVendorAcct);
		if (!ven.load(context, sDBID, sUserID, sUserFullName)){
			throw new Exception("Could not load vendor to update - " + ven.getErrorMessages());
		}
		setsvendor(sVendorAcct);
		setsvendorname(ven.getsname());
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, sDBID, "MySQL", SMUtilities.getFullClassName(this.toString()) + ".updateVendor - user: " + sUserFullName);
		} catch (Exception e) {
			throw new Exception("Error [1400098223] getting connection - " + e.getMessage());
		}
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080906]");
			throw new Exception("Error [1400098224] - could not start data transaction.");
		}
		if(!save_without_data_transaction(conn, sUserFullName, sUserID, true)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080907]");
			throw new Exception("Error saving updated PO - " + getErrorMessages());
		}
		
		//Modify the corresponding vendor item record:
		//Get the polines and current vendor item records we need to work with:
		SQL = "SELECT *"
			+ " FROM " + SMTableicpolines.TableName + " LEFT JOIN " + SMTableicvendoritems.TableName + " ON "
			+ "(" + SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber + " = "
			+ SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sItemNumber + ")"
			+ " AND (" + SMTableicpolines.TableName + "." + SMTableicpolines.svendorsitemnumber + " = "
			+ SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendorItemNumber + ")"
			+ " WHERE ("
				+ "(" + SMTableicpolines.TableName + "." + SMTableicpolines.lpoheaderid + " = " + getsID() + ")"
				+ " AND (" + SMTableicpolines.TableName + "." + SMTableicpolines.lnoninventoryitem + " = 0)"
				+ " AND (" + SMTableicpolines.TableName + "." + SMTableicpolines.svendorsitemnumber + " != '')"
				+ " AND (" + SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendor + " = '" + sOriginalVendor + "')"
			+ ")"
		;
		ResultSet rs = clsDatabaseFunctions.openResultSet(
			SQL, 
			context, 
			sDBID, 
			"MySQL", 
			SMUtilities.getFullClassName(this.toString()) + ".updateVendorOnReceipt - user: " + sUserFullName);
		while (rs.next()){
			//UPDATE any current record for the NEW vendor:
			Statement stmt;
			try {
				SQL = " UPDATE"
					+ " " + SMTableicvendoritems.TableName
					+ " SET " + SMTableicvendoritems.sComment + " = '" 
						+ clsDatabaseFunctions.FormatSQLStatement(rs.getString(SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sComment)) + "'"
					+ ", " + SMTableicvendoritems.sCost + " = " + rs.getBigDecimal(SMTableicvendoritems.TableName + "." 
						+ SMTableicvendoritems.sCost)
					+ " WHERE ("
						+ "(" + SMTableicvendoritems.sItemNumber + " = '" 
						+ rs.getString(SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber) + "')"
						+ " AND (" + SMTableicvendoritems.sVendor + " = '" + sVendorAcct + "')"
					+ ")"
				;
				stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				clsDatabaseFunctions.freeConnection(context, conn, "[1547080908]");
				throw new Exception("Error updating new vendor item records with SQL: " + SQL + " - " + e.getMessage());
			}
			
			//Now insert records for the new vendor if they don't already exist:
			try {
				SQL = "INSERT INTO " + SMTableicvendoritems.TableName
					+ "("
					+ SMTableicvendoritems.sComment
					+ ", " + SMTableicvendoritems.sCost
					+ ", " + SMTableicvendoritems.sItemNumber
					+ ", " + SMTableicvendoritems.sVendor
					+ ", " + SMTableicvendoritems.sVendorItemNumber
					+ ") VALUES ("
					+ "'" + clsDatabaseFunctions.FormatSQLStatement(rs.getString(SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sComment)) + "'" //Comment
					+ ", " + rs.getBigDecimal(SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sCost)
					+ ", '" + rs.getString(SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber) + "'"
					+ ", '" + sVendorAcct + "'"
					+ ", '" + rs.getString(SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendorItemNumber)+ "'"
					+ ")"
					+ " ON DUPLICATE KEY UPDATE"
					+ " " + SMTableicvendoritems.sComment + " = '" 
						+ clsDatabaseFunctions.FormatSQLStatement(rs.getString(SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sComment)) + "'"
					+ ", " + SMTableicvendoritems.sCost + " = " 
						+ rs.getBigDecimal(SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sCost)
					+ ", " + SMTableicvendoritems.sItemNumber + " = '" 
						+ rs.getString(SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber) + "'"
					+ ", " + SMTableicvendoritems.sVendor + " = '" 
						+ sVendorAcct + "'"
					+ ", " + SMTableicvendoritems.sVendorItemNumber + " = '" 
						+ rs.getString(SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendorItemNumber) + "'"
				;
				stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				clsDatabaseFunctions.freeConnection(context, conn, "[1547080909]");
				throw new Exception("Error inserting new vendor item records with SQL: " + SQL + " - " + e.getMessage());
			}
		}
		//Finally remove the current vendor item record with the wrong vendor:
		try {
			SQL = "DELETE " + SMTableicvendoritems.TableName + " FROM " + SMTableicvendoritems.TableName
				+ " LEFT JOIN " + SMTableicpolines.TableName + " ON "
				+ "(" + SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber + " = "
				+ SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sItemNumber + ")"
				+ " AND (" + SMTableicpolines.TableName + "." + SMTableicpolines.svendorsitemnumber + " = "
				+ SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendorItemNumber + ")"
				+ " WHERE ("
					+ "(" + SMTableicpolines.TableName + "." + SMTableicpolines.lpoheaderid + " = " + getsID() + ")"
					+ " AND (" + SMTableicpolines.TableName + "." + SMTableicpolines.lnoninventoryitem + " = 0)"
					+ " AND (" + SMTableicpolines.TableName + "." + SMTableicpolines.svendorsitemnumber + " != '')"
					+ " AND (" + SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendor + " = '" + sOriginalVendor + "')"
				+ ")"			
			;
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080910]");
			throw new Exception("Error deleting previous vendor item records with SQL: " + SQL + " - " + e.getMessage());
		}
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080911]");
			throw new Exception("Error committing data transaction to update vendor");
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080912]");
	}
	
    private void initPOVariables(){
    	m_slid = "-1";
    	m_sponumber = "";
    	m_svendor = "";
    	m_svendorname = "";
    	m_sreference = "";
    	m_sstatus = "0";
    	m_sdatpodate = clsDateAndTimeConversions.now("MM/dd/yyyy");
    	m_sbillcode = "";
    	m_sbillname = "";
    	m_sbilladdress1 = "";
    	m_sbilladdress2 = "";
    	m_sbilladdress3 = "";
    	m_sbilladdress4 = "";
    	m_sbillcity = "";
    	m_sbillstate = "";
    	m_sbillpostalcode = "";
    	m_sbillcountry = "";
    	m_sbillphone = "";
    	m_sbillfax = "";
    	m_sbillcontactname = "";
    	m_sshipcode = "";
    	m_sshipname = "";
    	m_sshipaddress1 = "";
    	m_sshipaddress2 = "";
    	m_sshipaddress3 = "";
    	m_sshipaddress4 = "";
    	m_sshipcity = "";
    	m_sshipstate = "";
    	m_sshippostalcode = "";
    	m_sshipcountry = "";
    	m_sshipphone = "";
    	m_sshipfax = "";
    	m_sshipcontactname = "";
    	m_sshipviacode = "";
    	m_sshipvianame = "";
    	m_sdatexpecteddate = "00/00/0000";
    	m_sdatassigned = EMPTY_DATETIME_STRING;
    	m_sassignedtofullname = "";
    	m_scomment = "";
    	m_sdescription = "";
    	m_sdeletedbyfullname = "";
    	m_sdatdeleted = EMPTY_DATETIME_STRING;
    	m_sgdoclink = "";
    	m_screatedbyfullname = "";
    	m_ipaymentonhold = "0";
    	m_spaymentonholdbyfullname = "";
    	m_lpaymentonholdbyuserid = "0";
    	m_datpaymentplacedonhold = EMPTY_DATETIME_STRING;
    	m_mpaymentonholdreason = "";
    	m_mpaymentonholdvendorcomment = "";
		super.initVariables();
		super.setObjectName(ParamObjectName);
    }
    public boolean updateLineNumbersAfterLineDeletion(Connection conn){
    	String rsSQL = "SELECT"
    		+ " " + SMTableicpolines.lid
    		+ " FROM " + SMTableicpolines.TableName
    		+ " WHERE ("
    			+ "(" + SMTableicpolines.lpoheaderid + " = " + m_slid + ")"
    		+ ")"
    		+ " ORDER BY " + SMTableicpolines.llinenumber
    		;
    	long iLineNumber = 0;
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(rsSQL, conn);
			while (rs.next()){
				iLineNumber++;
				String SQL = "UPDATE " + SMTableicpolines.TableName
					+ " SET " + SMTableicpolines.llinenumber + " = " + Long.toString(iLineNumber)
					+ " WHERE ("
						+ "(" + SMTableicpolines.lid + " = " + rs.getLong(SMTableicpolines.lid) + ")"
					+ ")"
				;
				try{
				    Statement stmt = conn.createStatement();
				    stmt.executeUpdate(SQL);
				}catch (Exception ex) {
					super.addErrorMessage("Error updating po line number with SQL: " + SQL + " - " + ex.getMessage());
					return false;
				}
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Error reading po line numbers with SQL: " + rsSQL + " - " + e.getMessage());
			return false;
		}
    	
    	return true;
    }
    
	public boolean updateLineNumbersAfterSorting(
			String PONumber, 
			HttpServletRequest request, 
			ServletContext context, 
			String sDBID,
			String sUserID) {
		
		//Get DB connection
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, sDBID, "MySQL", SMUtilities.getFullClassName(this.toString()) + ".updateLineNumbersAfterSorting - userID: " + sUserID);
		} catch (Exception e) {
			super.addErrorMessage("Error [1523371456] getting connection - " + e.getMessage());
			return false;
		}
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080901]");
			super.addErrorMessage("Error [1523371457] - could not start data transaction.");
			return false;
		}		
		
		//Clear all line numbers
		String SQL = "UPDATE " + SMTableicpolines.TableName 
				+ " SET " + SMTableicpolines.llinenumber + " = " + SMTableicpolines.llinenumber + " * -1"
				+ " WHERE " + "(" + SMTableicpolines.lpoheaderid + " =" + PONumber + ")";
		
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		} catch (Exception ex) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080902]");
			super.addErrorMessage("Error updating po line number with SQL: " + SQL + " - " + ex.getMessage());
			return false;
		}
		
		// Get the line IDs and update with new line number:
		String sOrginalLineID = "";
		Enumeration<String> paramNames = request.getParameterNames();
		String sMarker = "POLINEID";
		
		while (paramNames.hasMoreElements()) {
			String sParamName = paramNames.nextElement();

			if (sParamName.contains(sMarker)) {

				sOrginalLineID = sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length());

				//Update line numbers
				 SQL = "UPDATE " + SMTableicpolines.TableName 
						+ " SET " + SMTableicpolines.llinenumber + " = " + request.getParameter(sParamName) 
						+ " WHERE " + "(" + SMTableicpolines.lid + " = " + sOrginalLineID + ")";
				
				try {
					Statement stmt = conn.createStatement();
					stmt.executeUpdate(SQL);
				} catch (Exception ex) {
					clsDatabaseFunctions.rollback_data_transaction(conn);
					clsDatabaseFunctions.freeConnection(context, conn, "[1547080903]");
					super.addErrorMessage("Error updating po line number with SQL: " + SQL + " - " + ex.getMessage());
					return false;
				}
			}
		}

		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080904]");
			super.addErrorMessage("Error committing data transaction to update po line numbers");
			return false;
		}
		
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080905]");
		return true;
    }
}