package smcontrolpanel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMClasses.SMOrderDetail;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTabledoingbusinessasaddresses;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableproposals;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMProposal extends Object{
	
	public static final String Paramstrimmedordernumber = "strimmedordernumber";
	public static final String ParamdatproposalDate = "datproposaldate";
	public static final String Paramsfurnishandinstallstring = "sfurnishandinstallstring";
	public static final String Paramlapprovedbyuserid = "lapprovedbyuserid";
	public static final String Paramsapprovedbyfullname = "sapprovedbyfullname";
	public static final String Paramdattimeapproved = "dattimeapproved";
	public static final String Paramsbodydescription = "sbodydescription";
	public static final String Paramsalternate1 = "salternate1";
	public static final String Paramsalternate2 = "salternate2";
	public static final String Paramsalternate3 = "salternate3";
	public static final String Paramsalternate4 = "salternate4";
	public static final String Paramsalternate1price = "salternate1price";
	public static final String Paramsalternate2price = "salternate2price";
	public static final String Paramsalternate3price = "salternate3price";
	public static final String Paramsalternate4price = "salternate4price";
	public static final String Paramswrittenproposalamt = "swrittenproposalamt";
	public static final String Paramsnumericproposalamt = "snumericproposalamt";
	public static final String Paramsterms = "sterms";
	public static final String Paramiprintlogo = "iprintlogo";
	public static final String Paramitermsid = "itermsid";
	public static final String Paramsdaystoaccept = "sdaystoaccept";
	public static final String Paramspaymentterms = "spaymentterms";
	public static final String Paramisigned = "isigned";
	public static final String Paramlsignedbyuserid = "lsignedbyuserid";
	public static final String Paramssignedbyfullname = "ssignedbyfullname";
	public static final String Paramdattimesigned = "dattimesigned";
	public static final String Paramsdbaproposallogo = "sdbaproposallogo";
	
	public static String EMPTY_DATETIME_STRING = "00/00/0000 00:00 AM";
	
	private String m_strimmedordernumber;
	private String m_sdatproposaldate;
	private String m_sfurnishandinstallstring;
	private String m_lapprovedbyuserid;
	private String m_sapprovedbyfullname;
	private String m_dattimeapproved;
	private String m_sbodydescription;
	private String m_salternate1;
	private String m_salternate2;
	private String m_salternate3;
	private String m_salternate4;
	private String m_salternate1price;
	private String m_salternate2price;
	private String m_salternate3price;
	private String m_salternate4price;
	private String m_swrittenproposalamt;
	private String m_snumericproposalamt;
	private String m_sterms;
	private String m_iprintlogo;
	private String m_itermsid;
	private String m_sdaystoaccept;
	private String m_spaymentterms;
	private String m_isigned = "isigned";
	private String m_lsignedbyuserid;
	private String m_ssignedbyfullname;
	private String m_dattimesigned;
	private String m_sdbaproposallogo;
	private boolean m_bApprovalWasChecked;
	private boolean m_bSignatureWasChecked;
	//This indicates that proposal data was changed, but not necessarily the approval itself:
	private boolean m_bProposalDataWasChanged;
	private boolean bDebugMode = false;

	public SMProposal(
			String strimmedordernumber
	) {
		initProposalVariables();
		m_strimmedordernumber = strimmedordernumber;
	}

	public void loadFromHTTPRequest(HttpServletRequest req){

		m_strimmedordernumber = clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramstrimmedordernumber, req).trim();
		m_sdatproposaldate = clsManageRequestParameters.get_Request_Parameter(SMProposal.ParamdatproposalDate, req).trim();
		if(m_sdatproposaldate.compareToIgnoreCase("") == 0){
			m_sdatproposaldate = "00/00/0000";
		}
		
		m_sfurnishandinstallstring = clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramsfurnishandinstallstring, req).trim();
		m_lapprovedbyuserid = clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramlapprovedbyuserid, req).trim();
		m_sapprovedbyfullname = clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramsapprovedbyfullname, req).trim();
		m_dattimeapproved = clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramdattimeapproved, req).trim();
		if (m_dattimeapproved.compareToIgnoreCase("") == 0){
			m_dattimeapproved = EMPTY_DATETIME_STRING;
		}
		m_bApprovalWasChecked = req.getParameter(SMProposalEdit.APPROVED_CHECKBOX) != null;
		m_bProposalDataWasChanged = clsManageRequestParameters.get_Request_Parameter(
				SMProposalEdit.PROPOSALDATAWASCHANGED_FLAG, req).compareToIgnoreCase(SMProposalEdit.PROPOSALDATAWASCHANGED_FLAG_VALUE) == 0;
		
		m_sbodydescription = clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramsbodydescription, req).trim();
		m_salternate1 = clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramsalternate1, req).trim();
		m_salternate2 = clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramsalternate2, req).trim();
		m_salternate3 = clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramsalternate3, req).trim();
		m_salternate4 = clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramsalternate4, req).trim();
		m_salternate1price = clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramsalternate1price, req).trim();
		m_salternate2price = clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramsalternate2price, req).trim();
		m_salternate3price = clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramsalternate3price, req).trim();
		m_salternate4price = clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramsalternate4price, req).trim();
		m_swrittenproposalamt = clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramswrittenproposalamt, req).trim();
		m_snumericproposalamt = clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramsnumericproposalamt, req).trim();
		m_sterms = clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramsterms, req).trim();
		if (req.getParameter(SMProposal.Paramiprintlogo) == null){m_iprintlogo = "0";}else{m_iprintlogo = "1";}
		m_itermsid = clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramitermsid, req).trim();
		m_sdaystoaccept = clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramsdaystoaccept, req).trim();
		m_spaymentterms = clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramspaymentterms, req).trim();
		m_bSignatureWasChecked = req.getParameter(SMProposalEdit.SIGNATURE_CHECKBOX) != null;
		m_isigned = clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramisigned, req).trim();
		m_lsignedbyuserid = clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramlsignedbyuserid, req).trim();
		m_ssignedbyfullname = clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramssignedbyfullname, req).trim();
		m_dattimesigned = clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramdattimesigned, req).trim();
		if (m_dattimesigned.compareToIgnoreCase("") == 0){
			m_dattimesigned = EMPTY_DATETIME_STRING;
		}
	}
	private void load(
			String sTrimmedOrderNumber,
			ServletContext context, 
			String sDBID
	) throws Exception{
		try{
			//Get the record to edit:
			String sSQL = "SELECT * FROM " + SMTableproposals.TableName
			+ " WHERE ("
			+ SMTableproposals.strimmedordernumber + " = '" + sTrimmedOrderNumber + "'"
			+ ")"
			;
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					sSQL, 
					context, 
					sDBID, 
					"MySQL", 
					this.toString() + ".load");
			loadFromResultSet(rs);
		}catch (SQLException ex){
			throw new Exception("Cannot load proposal '" + sTrimmedOrderNumber + "' - " + ex.getMessage());
		}
		
	}

	private void loadFromResultSet(ResultSet rs) throws SQLException{
		try{
			if (rs.next()){
				m_strimmedordernumber = clsStringFunctions.checkStringForNull(rs.getString(SMTableproposals.strimmedordernumber));
				m_sdatproposaldate = clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTableproposals.sdatproposaldate));
				m_sfurnishandinstallstring = clsStringFunctions.checkStringForNull(rs.getString(SMTableproposals.sfurnishandinstallstring));
				m_lapprovedbyuserid = Integer.toString(rs.getInt(SMTableproposals.lapprovedbyuserid));
				m_sapprovedbyfullname = clsStringFunctions.checkStringForNull(rs.getString(SMTableproposals.sapprovedbyfullname));
				m_dattimeapproved = clsDateAndTimeConversions.resultsetDateTimeStringToString(rs.getString(SMTableproposals.dattimeapproved));
				m_sbodydescription = clsStringFunctions.checkStringForNull(rs.getString(SMTableproposals.sbodydescription));
				m_salternate1 = clsStringFunctions.checkStringForNull(rs.getString(SMTableproposals.salternate1));
				m_salternate2 = clsStringFunctions.checkStringForNull(rs.getString(SMTableproposals.salternate2));
				m_salternate3 = clsStringFunctions.checkStringForNull(rs.getString(SMTableproposals.salternate3));
				m_salternate4 = clsStringFunctions.checkStringForNull(rs.getString(SMTableproposals.salternate4));
				m_salternate1price = clsStringFunctions.checkStringForNull(rs.getString(SMTableproposals.salternate1price));
				m_salternate2price = clsStringFunctions.checkStringForNull(rs.getString(SMTableproposals.salternate2price));
				m_salternate3price = clsStringFunctions.checkStringForNull(rs.getString(SMTableproposals.salternate3price));
				m_salternate4price = clsStringFunctions.checkStringForNull(rs.getString(SMTableproposals.salternate4price));
				m_swrittenproposalamt = clsStringFunctions.checkStringForNull(rs.getString(SMTableproposals.swrittenproposalamt));
				m_snumericproposalamt = clsStringFunctions.checkStringForNull(rs.getString(SMTableproposals.snumericproposalamt));
				m_sterms = clsStringFunctions.checkStringForNull(rs.getString(SMTableproposals.sterms));
				m_iprintlogo = Integer.toString(rs.getInt(SMTableproposals.iprintlogo));
				m_itermsid = Integer.toString(rs.getInt(SMTableproposals.itermsid));
				m_sdaystoaccept = rs.getString(SMTableproposals.sdaystoaccept);
				m_spaymentterms = rs.getString(SMTableproposals.spaymentterms);
				m_isigned = Integer.toString(rs.getInt(SMTableproposals.isigned));
				m_sdbaproposallogo = rs.getString(SMTableproposals.sdbaproposallogo);
				m_lsignedbyuserid = Integer.toString(rs.getInt(SMTableproposals.lsignedbyuserid));
				m_ssignedbyfullname = clsStringFunctions.checkStringForNull(rs.getString(SMTableproposals.ssignedbyfullname));
				m_dattimesigned = clsDateAndTimeConversions.resultsetDateTimeStringToString(rs.getString(SMTableproposals.dattimesigned));
				rs.close();
			}
			else{
				rs.close();
			}
		}catch(SQLException ex){
			throw new SQLException("Error loading from resultset - " + ex.getMessage());
		}
	}
	public void getDBALogo(Connection conn , String sOrderNumber) throws Exception{
		String SQL = "SELECT "+SMTableorderheaders.idoingbusinessasaddressid
				+" FROM "+SMTableorderheaders.TableName
				+" WHERE "+SMTableorderheaders.strimmedordernumber +" = "+sOrderNumber;
		String sDBAID = "";
		ResultSet rs;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()) {
				sDBAID = rs.getString(SMTableorderheaders.idoingbusinessasaddressid);
			}
			rs.close();
			SQL = "SELECT "+SMTabledoingbusinessasaddresses.sProposalLogo 
				+ " FROM "+SMTabledoingbusinessasaddresses.TableName
				+ " WHERE "+SMTabledoingbusinessasaddresses.lid +" = "+sDBAID+"";
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()) {
				m_sdbaproposallogo = rs.getString(SMTabledoingbusinessasaddresses.sProposalLogo);
			}
			rs.close();
		}catch(Exception e) {
			throw new Exception("ERROR [1546538960] loading DBA " + e.getMessage());
		}
	}
	
	public void load(
			ServletContext context, 
			String sDBID
	) throws Exception{
		try {
			load(m_strimmedordernumber, context, sDBID);
		} catch (Exception e) {
			throw new Exception("Could not load proposal for '" + m_strimmedordernumber + "' - " + e.getMessage());
		}
	}
	public void save (Connection conn, String sUserID, String sUserName, String sLicenseModuleLevel) throws Exception{
		
		try {
			 getDBALogo(conn ,m_strimmedordernumber);
			validateEntries(conn);
		} catch (Exception e1) {
			throw new Exception("Error validating entries - " + e1.getMessage());
		}

		//Get the full user's name here:
		String sFullUserName = "(USERNAME NOT FOUND)";
		String SQL = "SELECT"
			+ " " + SMTableusers.sUserFirstName
			+ ", " + SMTableusers.sUserLastName
			+ " FROM " + SMTableusers.TableName
			+ " WHERE ("
				+ "(" + SMTableusers.lid + " = " + sUserID + ")"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				sFullUserName = rs.getString(SMTableusers.sUserFirstName) + " " + rs.getString(SMTableusers.sUserLastName); 
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception("Error reading user full name with SQL: " + SQL + " - " + e1.getMessage());
		}
		
		//Trying to figure out the logic for saving approval (and signature) information.  This is tricky because on the form, if we disable the
		// 'Approved' checkbox, then if the user doesn't have the ability to approve proposals, but doesn't make any changes
		// the disabled checkbox doesn't tell us that the proposal was previously approved.
		//We also have to know, IF the checkbox IS 'Approved', if that happened this time or if it was done previously.
		
		String sCurrentApprovalDate = "";
		String sCurrentApprovalUserID = "0";
		String sCurrentApprovalFullName = "";
		String sCurrentSignedDate = "";
		String sCurrentSignedUserID = "0";
		String sCurrentSignedFullName = "";
		String sCurrentSignedStatus = "";
		
		//Default values are 'unapproved' ones:
		String sUnApprovedDate = "'0000-00-00 00:00:00'";
		String sUnApprovedUserID = "0";
		String sUnApprovedFullName = "''";
		String sUnSignedDate = "'0000-00-00 00:00:00'";
		String sUnSignedUserID = "0";
		String sUnSignedFullName = "''";
		
		//Use these to carry the final values:
		String sApprovedDate = "";
		String sApprovedUserID = "";
		String sApprovedFullName = "";
		String sSignedDate = "";
		String sSignedUserID = "";
		String sSignedFullName = "";
		
		//Store the previous 'approved' and 'signed' values for this proposal:
		SQL = "SELECT"
			+ " " + SMTableproposals.dattimeapproved
			+ ", " + SMTableproposals.sapprovedbyfullname
			+ ", " + SMTableproposals.lapprovedbyuserid
			+ ", " + SMTableproposals.isigned
			+ ", " + SMTableproposals.ssignedbyfullname
			+ ", " + SMTableproposals.lsignedbyuserid
			+ ", " + SMTableproposals.dattimesigned
			+ " FROM " + SMTableproposals.TableName
			+ " WHERE ("
				+ "(" + SMTableproposals.strimmedordernumber + " = '" + getstrimmedordernumber() + "')"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
			if (rs.next()){
				//Get the current 'approved' values:
				sCurrentApprovalDate = "'" + rs.getString(SMTableproposals.dattimeapproved) + "'";
				sCurrentApprovalUserID =  Long.toString(rs.getLong(SMTableproposals.lapprovedbyuserid));
				sCurrentApprovalFullName = "'" + rs.getString(SMTableproposals.sapprovedbyfullname) + "'";	
				sCurrentSignedDate = "'" + rs.getString(SMTableproposals.dattimesigned) + "'";
				sCurrentSignedUserID =  Long.toString(rs.getLong(SMTableproposals.lsignedbyuserid));
				sCurrentSignedFullName = "'" + rs.getString(SMTableproposals.ssignedbyfullname) + "'";	
				sCurrentSignedStatus = Long.toString(rs.getLong(SMTableproposals.isigned));
			}else{
				sCurrentApprovalDate = sUnApprovedDate;
				sCurrentApprovalUserID = sUnApprovedUserID;
				sCurrentApprovalFullName = sUnApprovedFullName;
				sCurrentSignedDate = sUnSignedDate;
				sCurrentSignedUserID = sUnSignedUserID;
				sCurrentSignedFullName = sUnSignedFullName;
				sCurrentSignedStatus = "0";
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception("Error reading current approval date - " + e1.getMessage());
		}
		boolean bProposalApprovalPermitted = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMApproveProposals,
				sUserID,
				conn,
				sLicenseModuleLevel);
		//If the user CAN approve proposals:
		if (bProposalApprovalPermitted){
			//If it is approved, we have to figure out whether it was JUST approved, or if it was already approved before this edit:
			if (m_bApprovalWasChecked){
				//If it was previously NOT approved, then the approval is 'fresh':
				if (sCurrentApprovalUserID.trim().compareToIgnoreCase(sUnApprovedUserID) == 0){
					sApprovedDate = "NOW()";
					sApprovedUserID = "" + clsDatabaseFunctions.FormatSQLStatement(sUserID) + "";
					sApprovedFullName = "'" + clsDatabaseFunctions.FormatSQLStatement(sFullUserName) + "'";
				}else{
					sApprovedDate = sCurrentApprovalDate;
					sApprovedUserID = sCurrentApprovalUserID;
					sApprovedFullName = sCurrentApprovalFullName;
				}
			}else{
				sApprovedDate = sUnApprovedDate;
				sApprovedUserID = sUnApprovedUserID;
				sApprovedFullName = sUnApprovedFullName;
			}

		//Otherwise, if the user does NOT have permission to approve the proposal:
		}else{
			//If the proposal data was changed, then the approval has to be revoked:
			if (m_bProposalDataWasChanged){
				//We just drop to the default here, which is 'not approved';
				sApprovedDate = sUnApprovedDate;
				sApprovedUserID = sUnApprovedUserID;
				sApprovedFullName = sUnApprovedFullName;
			}else{
				//IF the proposal data was NOT changed, then keep whatever approval status is already on the proposal:
				sApprovedDate = sCurrentApprovalDate;
				sApprovedUserID = sCurrentApprovalUserID;
				sApprovedFullName = sCurrentApprovalFullName;
			}
		}
		
		//Figure out the signature and date:
		boolean bSigningPermitted = checkSigningPermission(sUserID, conn, sLicenseModuleLevel);
		//If the user CAN sign proposals:
		if (bSigningPermitted){
			//If it is signed, we have to figure out whether it was JUST signed, or if it was already signed before this edit:
			if (m_bSignatureWasChecked){
				m_isigned = "1";
				//If it was previously NOT signed, then the signing is 'fresh':
				if (sCurrentSignedUserID.trim().compareToIgnoreCase(sUnSignedUserID) == 0){
					sSignedDate = "NOW()";
					sSignedUserID = sUserID;
					sSignedFullName = "'" + clsDatabaseFunctions.FormatSQLStatement(sFullUserName) + "'";
				}else{
					sSignedDate = sCurrentSignedDate;
					sSignedUserID = sCurrentSignedUserID;
					sSignedFullName = sCurrentSignedFullName;
				}
			}else{
				m_isigned = "0";
				sSignedDate = sUnSignedDate;
				sSignedUserID = sUnSignedUserID;
				sSignedFullName = sUnSignedFullName;
			}

		//Otherwise, if the user does NOT have permission to sign the proposal:
		}else{
			//If the proposal data was changed, then the signature has to be removed:
			if (m_bProposalDataWasChanged){
				//We just drop to the default here, which is 'not signed';
				m_isigned = "0";
				sSignedDate = sUnSignedDate;
				sSignedUserID = sUnSignedUserID;
				sSignedFullName = sUnSignedFullName;
			}else{
				//IF the proposal data was NOT changed, then keep whatever signature status is already on the proposal:
				m_isigned = sCurrentSignedStatus;
				sSignedDate = sCurrentSignedDate;
				sSignedUserID = sCurrentSignedUserID;
				sSignedFullName = sCurrentSignedFullName;
			}
		}
		
		SQL = "INSERT INTO " + SMTableproposals.TableName + "("
			+ SMTableproposals.strimmedordernumber
			+ ", " + SMTableproposals.dattimeapproved
			+ ", " + SMTableproposals.sapprovedbyfullname
			+ ", " + SMTableproposals.lapprovedbyuserid
			+ ", " + SMTableproposals.iprintlogo
			+ ", " + SMTableproposals.salternate1
			+ ", " + SMTableproposals.salternate1price
			+ ", " + SMTableproposals.salternate2
			+ ", " + SMTableproposals.salternate2price
			+ ", " + SMTableproposals.salternate3
			+ ", " + SMTableproposals.salternate3price
			+ ", " + SMTableproposals.salternate4
			+ ", " + SMTableproposals.salternate4price
			+ ", " + SMTableproposals.sbodydescription
			+ ", " + SMTableproposals.sdatproposaldate
			//+ ", " + SMTableproposals.sextranotes
			+ ", " + SMTableproposals.sfurnishandinstallstring
			+ ", " + SMTableproposals.snumericproposalamt
			//+ ", " + SMTableproposals.soptions
			+ ", " + SMTableproposals.sterms
			+ ", " + SMTableproposals.swrittenproposalamt
			+ ", " + SMTableproposals.itermsid
			+ ", " + SMTableproposals.sdaystoaccept
			+ ", " + SMTableproposals.spaymentterms
			+ ", " + SMTableproposals.isigned
			+ ", " + SMTableproposals.lsignedbyuserid
			+ ", " + SMTableproposals.ssignedbyfullname
			+ ", " + SMTableproposals.dattimesigned
			+ ", " + SMTableproposals.sdbaproposallogo
			
			+ ") VALUES ("
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(getstrimmedordernumber()) + "'"
			+ ", " + sApprovedDate
			+ ", " + sApprovedFullName
			+ ", " + sApprovedUserID
			+ ", " + clsDatabaseFunctions.FormatSQLStatement(this.getiprintlogo())
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(this.getsalternate1()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(this.getsalternate1price()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(this.getsalternate2()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(this.getsalternate2price()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(this.getsalternate3()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(this.getsalternate3price()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(this.getsalternate4()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(this.getsalternate4price()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(this.getsbodydescription()) + "'"
			+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(this.getdatproposaldate()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(this.getsfurnishandinstallstring()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(this.getsnumericproposalamt()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(this.getsterms()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(this.getswrittenproposalamt()) + "'"
			+ ", " + clsDatabaseFunctions.FormatSQLStatement(getitermsid())
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsdaystoaccept()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getspaymentterms()) + "'"
			+ ", " + clsDatabaseFunctions.FormatSQLStatement(this.getisigned())
			+ ", " + sSignedUserID
			+ ", " + sSignedFullName
			+ ", " + sSignedDate
			+ ", '" +m_sdbaproposallogo+"' "
			+ ")"
			
			+ " ON DUPLICATE KEY UPDATE"
			+ " " + SMTableproposals.sdatproposaldate + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getdatproposaldate()) + "'"
			+ ", " + SMTableproposals.dattimeapproved + " = " + sApprovedDate
			+ ", " + SMTableproposals.sapprovedbyfullname + " = " + sApprovedFullName
			+ ", " + SMTableproposals.lapprovedbyuserid + " = " + sApprovedUserID
			+ ", " + SMTableproposals.iprintlogo + " = " + getiprintlogo()
			+ ", " + SMTableproposals.salternate1 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsalternate1()) + "'"
			+ ", " + SMTableproposals.salternate2 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsalternate2()) + "'"
			+ ", " + SMTableproposals.salternate3 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsalternate3()) + "'"
			+ ", " + SMTableproposals.salternate4 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsalternate4()) + "'"
			+ ", " + SMTableproposals.salternate1price + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsalternate1price()) + "'"
			+ ", " + SMTableproposals.salternate2price + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsalternate2price()) + "'"
			+ ", " + SMTableproposals.salternate3price + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsalternate3price()) + "'"
			+ ", " + SMTableproposals.salternate4price + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsalternate4price()) + "'"
			+ ", " + SMTableproposals.sbodydescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsbodydescription()) + "'"
			+ ", " + SMTableproposals.sfurnishandinstallstring + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsfurnishandinstallstring()) + "'"
			+ ", " + SMTableproposals.snumericproposalamt + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsnumericproposalamt()) + "'"
			+ ", " + SMTableproposals.sterms + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsterms()) + "'"
			+ ", " + SMTableproposals.swrittenproposalamt + " = '" + clsDatabaseFunctions.FormatSQLStatement(getswrittenproposalamt()) + "'"
			+ ", " + SMTableproposals.itermsid + " = " + clsDatabaseFunctions.FormatSQLStatement(getitermsid())
			+ ", " + SMTableproposals.sdaystoaccept + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsdaystoaccept()) + "'"
			+ ", " + SMTableproposals.spaymentterms + " = '" + clsDatabaseFunctions.FormatSQLStatement(getspaymentterms()) + "'"
			+ ", " + SMTableproposals.isigned + " = " + getisigned()
			+ ", " + SMTableproposals.lsignedbyuserid + " = " + sSignedUserID
			+ ", " + SMTableproposals.ssignedbyfullname + " = " + sSignedFullName
			+ ", " + SMTableproposals.dattimesigned + " = " + sSignedDate
			+ ", " + SMTableproposals.sdbaproposallogo + "= '"+m_sdbaproposallogo+"'";
		;
		if (bDebugMode){
			clsServletUtilities.sysprint(this.toString(), sUserName, " [1377108768] UPDATE SQL = " + SQL);
		}
		try{
			clsDatabaseFunctions.executeSQL(SQL, conn);
		}catch (SQLException e){
			throw new Exception("Error saving proposal - " + e.getMessage());
		}
	}
	public void save (String sUser, String sUserID, String sUserFullName, ServletContext context, String sConf, String sLicenseModuleLevel) throws Exception{

		Connection conn = clsDatabaseFunctions.getConnection(context, sConf, "MySQL", this.toString() + ".save - user: " 
		+ sUserID
		+ " - "
		+ sUserFullName
				);
		if (conn == null){
			throw new Exception("Could not get connection to save proposal");
		}
		
		try {
			save(conn, sUserID, sUser, sLicenseModuleLevel);
		} catch (Exception e2) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080628]");
			throw new Exception(e2.getMessage());
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080629]");
	}

	private void validateEntries(Connection conn) throws Exception{

		if (m_strimmedordernumber.trim().compareToIgnoreCase("") == 0){
			throw new Exception("Order number cannot be blank");
		}

		if(!clsDateAndTimeConversions.IsValidDateString("MM/dd/yyyy", m_sdatproposaldate)){
			//Allow an empty date:
			//if (m_sdatproposaldate.compareToIgnoreCase("00/00/0000") == 0){
			//}else{
				throw new Exception("Invalid proposal date: '" + m_sdatproposaldate + "'"); 
			//}
		}
		
		m_sfurnishandinstallstring = m_sfurnishandinstallstring.trim();
		if (m_sfurnishandinstallstring.length() > SMTableproposals.sfurnishandinstallstringLength){
			throw new Exception ("'Furnish and install' phrase is too long.");
		}

		m_lapprovedbyuserid = m_lapprovedbyuserid.trim();
		m_sapprovedbyfullname = m_sapprovedbyfullname.trim();
		if(!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy hh:mm a", m_dattimeapproved)){
			//Allow an empty date:
			if (m_dattimeapproved.compareToIgnoreCase(EMPTY_DATETIME_STRING) == 0){
			}else{
				throw new Exception("Invalid approved date: '" + m_dattimeapproved + "'"); 
			}
		}
		
		m_salternate1 = m_salternate1.trim();
		if (m_salternate1.length() > SMTableproposals.salternate1Length){
			throw new Exception ("Description of Alternate 1 cannot be longer than " 
				+ Integer.toString(SMTableproposals.salternate1Length) + " characters.");
		}
		m_salternate2 = m_salternate2.trim();
		if (m_salternate2.length() > SMTableproposals.salternate2Length){
			throw new Exception ("Description of Alternate 2 cannot be longer than " 
				+ Integer.toString(SMTableproposals.salternate2Length) + " characters.");
		}
		m_salternate3 = m_salternate3.trim();
		if (m_salternate3.length() > SMTableproposals.salternate3Length){
			throw new Exception ("Description of Alternate 3 cannot be longer than " 
				+ Integer.toString(SMTableproposals.salternate3Length) + " characters.");
		}
		m_salternate4 = m_salternate4.trim();
		if (m_salternate4.length() > SMTableproposals.salternate4Length){
			throw new Exception ("Description of Alternate 4 cannot be longer than " 
				+ Integer.toString(SMTableproposals.salternate4Length) + " characters.");
		}
		m_salternate1price = m_salternate1price.trim();
		if (m_salternate1price.length() > SMTableproposals.salternate1priceLength){
			throw new Exception ("Price of Alternate 1 cannot be longer than " 
				+ Integer.toString(SMTableproposals.salternate1priceLength) + " characters.");
		}
		m_salternate2price = m_salternate2price.trim();
		if (m_salternate2price.length() > SMTableproposals.salternate2priceLength){
			throw new Exception ("Price of Alternate 2 cannot be longer than " 
				+ Integer.toString(SMTableproposals.salternate2priceLength) + " characters.");
		}
		m_salternate3price = m_salternate3price.trim();
		if (m_salternate3price.length() > SMTableproposals.salternate3priceLength){
			throw new Exception ("Price of Alternate 3 cannot be longer than " 
				+ Integer.toString(SMTableproposals.salternate3priceLength) + " characters.");
		}
		m_salternate4price = m_salternate4price.trim();
		if (m_salternate4price.length() > SMTableproposals.salternate4priceLength){
			throw new Exception ("Price of Alternate 4 cannot be longer than " 
				+ Integer.toString(SMTableproposals.salternate4priceLength) + " characters.");
		}
		m_swrittenproposalamt = m_swrittenproposalamt.trim();
		if (m_swrittenproposalamt.length() > SMTableproposals.swrittenproposalamtLength){
			throw new Exception ("Written proposal amount cannot be longer than " 
				+ Integer.toString(SMTableproposals.swrittenproposalamtLength) + " characters.");
		}
		m_snumericproposalamt = m_snumericproposalamt.trim();
		if (m_snumericproposalamt.length() > SMTableproposals.snumericproposalamtLength){
			throw new Exception ("Proposal amount cannot be longer than " 
				+ Integer.toString(SMTableproposals.snumericproposalamtLength) + " characters.");
		}

		if (
			(m_iprintlogo.compareToIgnoreCase("0") != 0)
			&& (m_iprintlogo.compareToIgnoreCase("1") != 0)
		){
			throw new Exception("Invalid value ('"+ m_iprintlogo + "') for iprintlogo.");
		}
		if (m_itermsid.trim().compareToIgnoreCase("") == 0){
			throw new Exception ("'Payment terms' must be selected.");
		}
		m_sdaystoaccept = m_sdaystoaccept.trim();
		if (m_sdaystoaccept.compareToIgnoreCase("") == 0){
			throw new Exception ("'Days to accept' cannot be blank.");
		}
		if (m_sdaystoaccept.length() > SMTableproposals.sdaystoacceptLength){
			throw new Exception ("'Days to accept' cannot be longer than " 
				+ Integer.toString(SMTableproposals.sdaystoacceptLength) + " characters.");
		}
		
		m_spaymentterms = m_spaymentterms.trim();
		if (m_spaymentterms.compareToIgnoreCase("") == 0){
			throw new Exception ("'Payment terms' cannot be blank.");
		}
		if (m_spaymentterms.length() > SMTableproposals.spaymenttermsLength){
			throw new Exception ("'Payment terms' cannot be longer than " 
				+ Integer.toString(SMTableproposals.spaymenttermsLength) + " characters.");
		}
		
		//Make sure that the order as correct info:
		SMOrderHeader order = new SMOrderHeader();
		order.setM_strimmedordernumber(getstrimmedordernumber());
		if (!order.load(conn)){
			throw new Exception("Could not load order/quote - " + order.getErrorMessages());
		}
		//Is a real salesperson from the salesperson table on the order
		if (order.getM_sSalesperson().trim().compareToIgnoreCase("") == 0){
			throw new Exception("This quote/order does not have a valid salesperson.");
		}
		//Is the DBA id on the order valid
		if (!order.isDBAValid(conn)){
			throw new Exception("The 'Doing Business As Address' on the quote/order as been deleted or is invalid.");
		}
	}

	//Requires connection since it is used as part of a data transaction in places:
	public void delete(String sTrimmedOrderNumber, String sConf, ServletContext context, String sUser) throws Exception{

			String SQL = "DELETE FROM " + SMTableproposals.TableName
				+ " WHERE ("
				+ SMTableproposals.strimmedordernumber + " = '" + sTrimmedOrderNumber + "'"
				+ ")"
			;
			try {
				clsDatabaseFunctions.executeSQL(
					SQL, 
					context, 
					sConf, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".delete - user: " + sUser
				);
			} catch (Exception e) {
				throw new Exception("Could not delete proposal - " + e.getMessage());
			}
	}
	public void pasteOrderLines(
			String sTrimmedOrderNumber,
			ArrayList<String>sDetailNumbers, 
			String sConf, 
			ServletContext context, 
			String sUserID,
			String sUserFullName) throws Exception{
		
		if (sDetailNumbers.size() == 0){
			throw new SQLException("No lines were selected.");
		}
		SMOrderHeader order = new SMOrderHeader();
		order.setM_strimmedordernumber(sTrimmedOrderNumber);
		if (!order.load(context, sConf, sUserID, sUserFullName)){
			throw new Exception("Could not load order - " + order.getErrorMessages());
		}
		//Ship the lines from the array by matching the detail numbers:
		List<SMOrderDetail> arrDetailLines = new ArrayList<SMOrderDetail>(0);
		for (int i = 0; i < sDetailNumbers.size(); i++){
			for (int j = 0; j < order.get_iOrderDetailCount(); j++){
				SMOrderDetail line = order.getM_arrOrderDetails().get(j);
				if (line.getM_iDetailNumber().compareToIgnoreCase(sDetailNumbers.get(i)) == 0){
					try {
						arrDetailLines.add(line);
					} catch (Exception e) {
						throw new Exception("Could not insert order line - " + e.getMessage());
					}
				}
			}
		}
		//Sort by line number:
		Collections.sort(arrDetailLines, new compareOrderDetails());
		insertOrderLines(arrDetailLines, sTrimmedOrderNumber, context, sConf, sUserFullName);
	}
	private class compareOrderDetails implements Comparator<SMOrderDetail>{
	    public int compare(SMOrderDetail line1, SMOrderDetail line2) {
	    	if (Long.parseLong(line1.getM_iLineNumber()) > Long.parseLong(line1.getM_iLineNumber())){
	    		return 0;
	    	}else{
	    		return 1;
	    	}
	    }
	}
	private void insertOrderLines(
			List <SMOrderDetail>arrDetailLines,
			String sTrimmedOrderNumber,
			ServletContext context, 
			String sDBID, 
			String sUserFullName) throws Exception{
		for (int i = 0; i < arrDetailLines.size(); i++){
			String SQL = "UPDATE " + SMTableproposals.TableName
				+ " SET " + SMTableproposals.sbodydescription + " = CONCAT(" + SMTableproposals.sbodydescription
				+ ",'\n'" 
				+ ",'" + arrDetailLines.get(i).getM_dQtyOrdered() + "'"
				+ ",'   '"
				+ ",'(" + clsDatabaseFunctions.FormatSQLStatement(arrDetailLines.get(i).getM_sOrderUnitOfMeasure()) + ")   '"
				+ ",'" + clsDatabaseFunctions.FormatSQLStatement(arrDetailLines.get(i).getM_sItemDesc()) + "'"
				+ ") WHERE ("
					+ SMTableproposals.strimmedordernumber + " = '" + sTrimmedOrderNumber + "'"
				+ ")"
			;
			if (!clsDatabaseFunctions.executeSQL(
				SQL, 
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + ".insertOrderLine - user: " + sUserFullName)){
				throw new Exception("Could not execute SQL " + SQL);
			}
		}
	}
	public void cloneProposal(Connection conn, String sUserID, String sUserName, String sNewTrimmedOrderNumber, String sLicenseModuleLevel) throws Exception{
		SMProposal newproposal = new SMProposal(sNewTrimmedOrderNumber);
		//newproposal.setdatproposaldate(getdatproposaldate());
		newproposal.setiprintlogo(getiprintlogo());
		newproposal.setitermsid(getitermsid());
		newproposal.setsalternate1(getsalternate1());
		newproposal.setsalternate2(getsalternate2());
		newproposal.setsalternate3(getsalternate3());
		newproposal.setsalternate4(getsalternate4());
		newproposal.setsalternate1price(getsalternate1price());
		newproposal.setsalternate2price(getsalternate2price());
		newproposal.setsalternate3price(getsalternate3price());
		newproposal.setsalternate4price(getsalternate4price());
		newproposal.setsbodydescription(getsbodydescription());
		newproposal.setsdaystoaccept(getsdaystoaccept());
		//newproposal.setsextranotes(getsextranotes());
		newproposal.setsfurnishandinstallstring(getsfurnishandinstallstring());
		newproposal.setsnumericproposalamt(getsnumericproposalamt());
		//newproposal.setsoptions(getsoptions());
		newproposal.setspaymentterms(getspaymentterms());
		newproposal.setsterms(getsterms());
		newproposal.setswrittenproposalamt(getswrittenproposalamt());
		try {
			newproposal.save(conn, sUserID, sUserName, sLicenseModuleLevel);
		} catch (Exception e) {
			throw new Exception("Cloned proposal could not be saved - " + e.getMessage());
		}
	}
	private boolean checkSigningPermission(String sUserID, Connection conn, String sLicenseModuleLevel) throws Exception{
		String sOrderSalesperson = "";
		String SQL = "SELECT"
			+ " " + SMTableorderheaders.sSalesperson
			//+ ", " + SMTableusers.TableName + "." + SMTableusers.sDefaultSalespersonCode
			+ " FROM " + SMTableorderheaders.TableName
			+ " WHERE ("
				+ SMTableorderheaders.strimmedordernumber + " = '" + getstrimmedordernumber() + "'"
			+ ")"
		;
		try {
			ResultSet rsOrder = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsOrder.next()){
				sOrderSalesperson = rsOrder.getString(SMTableorderheaders.sSalesperson);
			}
			rsOrder.close();
		} catch (Exception e) {
			throw new SQLException("Error reading salesperson for this order - " + e.getMessage());
		}
		
		String sUserSalesperson = "";
		SQL = "SELECT"
				+ " " + SMTableusers.sDefaultSalespersonCode
				+ " FROM " + SMTableusers.TableName
				+ " WHERE ("
					+ SMTableusers.lid + " = " + sUserID + ""
				+ ")"
		;
		try {
			ResultSet rsUsers = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsUsers.next()){
				sUserSalesperson = rsUsers.getString(SMTableusers.sDefaultSalespersonCode);
			}
			rsUsers.close();
		} catch (Exception e) {
			throw new SQLException("Error reading salesperson for this user - " + e.getMessage());
		}
		//If there's an actual salesperson on the order
		if (sOrderSalesperson.compareToIgnoreCase("") != 0){
			//If this user is that salesperson, then they can print a signature
			if ((sUserSalesperson.compareToIgnoreCase("") != 0) && (sUserSalesperson.compareToIgnoreCase(sOrderSalesperson) == 0)){
				return true;
			}
			//Or of this user has permission to print ANY signature, he can do it
			if (SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMSignAnySalespersonsProposals, 
					sUserID,
					conn,
					sLicenseModuleLevel)){
				return true;
			}
		}
		return false;
	}
	public String getstrimmedordernumber(){
		return m_strimmedordernumber;
	}
	public void setstrimmedordernumber(String sTrimmedOrderNumber){
		m_strimmedordernumber = sTrimmedOrderNumber.trim();
	}
	public String getdatproposaldate(){
		return m_sdatproposaldate;
	}
	public void setdatproposaldate(String sProposalDate) {
		m_sdatproposaldate = sProposalDate;
	}
	
	public String getsfurnishandinstallstring() {
		return m_sfurnishandinstallstring;
	}

	public void setsfurnishandinstallstring(String sfurnishandinstallstring) {
		m_sfurnishandinstallstring = sfurnishandinstallstring;
	}

	public String getlapprovedbyuserid() {
		return m_lapprovedbyuserid;
	}

	public void setlapprovedbyuserid(String lapprovedbyuserid) {
		m_lapprovedbyuserid = lapprovedbyuserid;
	}

	public String getsapprovedbyfullname() {
		return m_sapprovedbyfullname;
	}

	public void setsapprovedbyfullname(String sapprovedbyfullname) {
		m_sapprovedbyfullname = sapprovedbyfullname;
	}

	public String getdattimeapproved() {
		return m_dattimeapproved;
	}

	public void setdattimeapproved(String dattimeapproved) {
		m_dattimeapproved = dattimeapproved;
	}
	
	public boolean isproposalapproved(){
		if (m_dattimeapproved.compareToIgnoreCase(EMPTY_DATETIME_STRING) == 0){
			return false;
		}else{
			return true;
		}
	}

	public String getsbodydescription() {
		return m_sbodydescription;
	}

	public void setsbodydescription(String sbodydescription) {
		m_sbodydescription = sbodydescription;
	}

	public String getsalternate1() {
		return m_salternate1;
	}

	public void setsalternate1(String salternate1) {
		m_salternate1 = salternate1;
	}

	public String getsalternate2() {
		return m_salternate2;
	}

	public void setsalternate2(String salternate2) {
		m_salternate2 = salternate2;
	}

	public String getsalternate3() {
		return m_salternate3;
	}

	public void setsalternate3(String salternate3) {
		m_salternate3 = salternate3;
	}

	public String getsalternate4() {
		return m_salternate4;
	}

	public void setsalternate4(String salternate4) {
		m_salternate4 = salternate4;
	}

	public String getsalternate1price() {
		return m_salternate1price;
	}

	public void setsalternate1price(String salternate1price) {
		m_salternate1price = salternate1price;
	}

	public String getsalternate2price() {
		return m_salternate2price;
	}

	public void setsalternate2price(String salternate2price) {
		m_salternate2price = salternate2price;
	}

	public String getsalternate3price() {
		return m_salternate3price;
	}

	public void setsalternate3price(String salternate3price) {
		m_salternate3price = salternate3price;
	}

	public String getsalternate4price() {
		return m_salternate4price;
	}

	public void setsalternate4price(String salternate4price) {
		m_salternate4price = salternate4price;
	}

	public String getswrittenproposalamt() {
		return m_swrittenproposalamt;
	}

	public void setswrittenproposalamt(String swrittenproposalamt) {
		m_swrittenproposalamt = swrittenproposalamt;
	}

	public String getsnumericproposalamt() {
		return m_snumericproposalamt;
	}

	public void setsnumericproposalamt(String snumericproposalamt) {
		m_snumericproposalamt = snumericproposalamt;
	}

	public String getsterms() {
		return m_sterms;
	}

	public void setsterms(String sterms) {
		m_sterms = sterms;
	}

	public String getiprintlogo() {
		return m_iprintlogo;
	}

	public void setiprintlogo(String sprintlogo) {
		m_iprintlogo = sprintlogo;
	}

	public String getitermsid() {
		return m_itermsid;
	}

	public void setitermsid(String stermsid) {
		m_itermsid = stermsid;
	}
	
	public String getsdaystoaccept() {
		return m_sdaystoaccept;
	}

	public void setsdaystoaccept(String sdaystoaccept) {
		m_sdaystoaccept = sdaystoaccept;
	}
	
	public String getspaymentterms() {
		return m_spaymentterms;
	}
	public void setspaymentterms(String spaymentterms){
		m_spaymentterms = spaymentterms;
	}
	public String getlsignedbyuserid() {
		return m_lsignedbyuserid;
	}
	public void setlsignedbyuserid(String slignedbyuserid) {
		m_lsignedbyuserid = slignedbyuserid;
	}
	public String getssignedbyfullname() {
		return m_ssignedbyfullname;
	}
	public void setssignedbyfullname(String ssignedbyfullname) {
		m_ssignedbyfullname = ssignedbyfullname;
	}
	public String getisigned() {
		return m_isigned;
	}
	public void setisigned(String sisigned) {
		m_iprintlogo = sisigned;
	}
	public String getdattimesigned() {
		return m_dattimesigned;
	}
	public void setdattimesigned(String dattimesigned) {
		m_dattimesigned = dattimesigned;
	}
	
	private void initProposalVariables(){
		m_strimmedordernumber = "";
		m_sdatproposaldate = clsDateAndTimeConversions.now("MM/dd/yyyy");
		m_sfurnishandinstallstring = "";
		m_lapprovedbyuserid = "";
		m_sapprovedbyfullname = "";
		m_dattimeapproved = EMPTY_DATETIME_STRING;
		m_sbodydescription = "";
		m_salternate1 = "";
		m_salternate2 = "";
		m_salternate3 = "";
		m_salternate4 = "";
		m_salternate1price = "";
		m_salternate2price = "";
		m_salternate3price = "";
		m_salternate4price = "";
		m_swrittenproposalamt = "";
		m_snumericproposalamt = "";
		m_sterms = "";
		m_iprintlogo = "1";
		m_itermsid = "0";
		m_sdaystoaccept = "";
		m_spaymentterms = "";
		m_isigned = "0";
		m_lsignedbyuserid = "";
		m_ssignedbyfullname = "";
		m_sdbaproposallogo = "";
		m_dattimesigned = EMPTY_DATETIME_STRING;
		m_bApprovalWasChecked = false;
	}
}
