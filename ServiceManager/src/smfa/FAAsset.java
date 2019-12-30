package smfa;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTablefadepreciationtype;
import SMDataDefinition.SMTablefamaster;
import SMDataDefinition.SMTablefatransactions;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class FAAsset extends Object{
	public static final String sObjectName = "Fixed Asset";
	public static final String ParamID = "ID";
	public static final String ParamTruckNumber = "TruckNumber";
	public static final String ParamNotePayableGLAcct = "NotPayableGLAcct";
	public static final String ParamState = "State";
	public static final String ParamAssetNumber = "AssetNumber";
	public static final String ParamDescription = "Description";
	public static final String ParamAcquisitionDate = "AcquisitionDate";
	public static final String ParamAcquisitionAmount = "AcquisitionAmount";
	public static final String ParamClass = "Class";
	public static final String ParamSerialNumber = "SerialNumber";
	public static final String ParamLicenseTagNumber = "LicenseTagNumber";
	public static final String ParamLocation = "Location";
	public static final String ParamDateSold = "DateSold";
	public static final String ParamGaragedLocation = "GaragedLocation";
	public static final String ParamLossOrGainGL = "LossOrGainGL";
	public static final String ParamDepreciationType = "DepreciationType";
	public static final String ParamCurrentValue = "CurrentValue";
	public static final String ParamComment = "Comment";
	public static final String ParamAmountSoldFor = "AmountSoldFor";
	public static final String ParamDepreciationGLAcct = "DepreciationGLAcct";
	public static final String ParamAccumulatedDepreciationGLAcct = "AccumulatedDepreciationGLAcct";
	public static final String ParamAccumulatedDepreciation = "AccumulatedDepreciation";
	public static final String ParamSalvageValue = "SalvageValue";
	public static final String ParamDriver = "Driver";
	public static final String ParamComment1 = "Comment1";
	public static final String ParamComment2 = "Comment2";
	public static final String ParamComment3 = "Comment3";
	public static final String Paramgdoclink = "gdoclinl";
	public static final String EMPTY_DATE_STRING = "00/00/0000";
	private String m_sID;
	private String m_sTruckNumber;
	private String m_sNotePayableGLAcct;
	private String m_sState;
	private String m_sAssetNumber;
	private String m_sDescription;
	private String m_sAcquisitionDate;
	private String m_sAcquisitionAmount;
	private String m_sClass;
	private String m_sSerialNumber;
	private String m_sLicenseTagNumber;
	private String m_sLocation;
	private String m_sDateSold;
	private String m_sGaragedLocation;
	private String m_sLossOrGainGL;
	private String m_sDepreciationType;
	private String m_sCurrentValue;
	private String m_sComment;
	private String m_sAmountSoldFor;
	private String m_sDepreciationGLAcct;
	private String m_sAccumulatedDepreciationGLAcct;
	private String m_sAccumulatedDepreciation;
	private String m_sSalvageValue;
	private String m_sDriver;
	private String m_sComment1;
	private String m_sComment2;
	private String m_sComment3;
	private String m_sgdoclink;
	private boolean bDebugMode = false;
	private String m_sErrorMessage;

	public FAAsset(
			String sAssetNumber
			) {
		m_sID = "-1";
		m_sTruckNumber = "";
		m_sNotePayableGLAcct = "";
		m_sState = "";
		m_sAssetNumber = sAssetNumber;
		m_sDescription = "";
		m_sAcquisitionDate = clsDateAndTimeConversions.now("MM/dd/yyyy");
		m_sAcquisitionAmount = "0";
		m_sClass = "";
		m_sSerialNumber = "";
		m_sLicenseTagNumber = "";
		m_sLocation = "";
		m_sDateSold = EMPTY_DATE_STRING;
		m_sGaragedLocation = "";
		m_sLossOrGainGL = "";
		m_sDepreciationType = "";
		m_sCurrentValue = "0";
		m_sComment = "";
		m_sAmountSoldFor = "0";
		m_sDepreciationGLAcct = "";
		m_sAccumulatedDepreciationGLAcct = "";
		m_sAccumulatedDepreciation = "0";
		m_sSalvageValue = "0";
		m_sDriver = "";
		m_sComment1 = "";
		m_sComment2 = "";
		m_sComment3 = "";
		m_sgdoclink = "";
		m_sErrorMessage = "";
	}

	public void loadFromHTTPRequest(HttpServletRequest req){

		m_sID = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamID, req).trim();
		m_sTruckNumber = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamTruckNumber, req).trim().replace("&quot;", "\"");
		m_sNotePayableGLAcct = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamNotePayableGLAcct, req).trim().replace("&quot;", "\"");
		m_sState = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamState, req).trim().replace("&quot;", "\"");
		if(req.getParameter("AssetNumberNEW") != null){
			m_sAssetNumber = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamAssetNumber+"NEW", req).trim().replace("&quot;", "\"");
		}else if (req.getParameter("AssetNumberEDIT") != null){
			m_sAssetNumber = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamAssetNumber+"EDIT", req).trim().replace("&quot;", "\"");
		}else{
			m_sAssetNumber = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamAssetNumber, req).trim().replace("&quot;", "\"");
		}
		m_sDescription = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamDescription, req).trim().replace("&quot;", "\"");
		m_sAcquisitionDate = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamAcquisitionDate, req).trim().replace("&quot;", "\"");
		if(m_sAcquisitionDate.compareToIgnoreCase("") == 0){
			m_sAcquisitionDate = EMPTY_DATE_STRING;
		}
		m_sAcquisitionAmount = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamAcquisitionAmount, req).trim().replace("&quot;", "\"");
		m_sClass = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamClass, req).trim().replace("&quot;", "\"");
		m_sSerialNumber = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamSerialNumber, req).trim().replace("&quot;", "\"");
		m_sLicenseTagNumber = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamLicenseTagNumber, req).trim().replace("&quot;", "\"");
		m_sLocation = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamLocation, req).trim().replace("&quot;", "\"");
		m_sDateSold = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamDateSold, req).trim().replace("&quot;", "\"");
		if(m_sDateSold.compareToIgnoreCase("") == 0){
			m_sDateSold = EMPTY_DATE_STRING;
		}
		m_sGaragedLocation = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamGaragedLocation, req).trim().replace("&quot;", "\"");
		m_sLossOrGainGL = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamLossOrGainGL, req).trim().replace("&quot;", "\"");
		m_sDepreciationType = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamDepreciationType, req).trim().replace("&quot;", "\"");
		m_sCurrentValue = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamCurrentValue, req).trim().replace("&quot;", "\"");
		m_sComment = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamComment, req).trim().replace("&quot;", "\"");
		m_sAmountSoldFor = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamAmountSoldFor, req).trim().replace("&quot;", "\"");
		m_sDepreciationGLAcct = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamDepreciationGLAcct, req).trim().replace("&quot;", "\"");
		m_sAccumulatedDepreciationGLAcct = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamAccumulatedDepreciationGLAcct, req).trim().replace("&quot;", "\"");
		m_sAccumulatedDepreciation = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamAccumulatedDepreciation, req).trim().replace("&quot;", "\"");
		m_sSalvageValue = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamSalvageValue, req).trim().replace("&quot;", "\"");
		m_sDriver = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamDriver, req).trim().replace("&quot;", "\"");
		m_sComment1 = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamComment1, req).trim().replace("&quot;", "\"");
		m_sComment2 = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamComment2, req).trim().replace("&quot;", "\"");
		m_sComment3 = clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamComment3, req).trim().replace("&quot;", "\"");
		m_sgdoclink = clsManageRequestParameters.get_Request_Parameter(FAAsset.Paramgdoclink, req).trim().replace("&quot;", "\"");
	}

	private boolean load(
			String sAssetNumber,
			ServletContext context, 
			String sDBID
			){
		m_sErrorMessage = "";
		String sSQL = "";
		try{
			//Get the record to edit:
			sSQL = "SELECT * FROM " + SMTablefamaster.TableName
					+ " WHERE ("
					+ SMTablefamaster.sAssetNumber + " = '" + sAssetNumber + "'"
					+ ")"
					;
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					sSQL, 
					context, 
					sDBID, 
					"MySQL", 
					this.toString() + ".load");
			try{
				loadFromResultSet(rs);
			}catch (SQLException e){
				rs.close();
				return false;
			}
		}catch (SQLException ex){
			m_sErrorMessage = "Error loading asset with SQL: " + sSQL + " - " + ex.getMessage();
			return false;
		}
		return true;
	}
	//Need this one with the connection:
	private boolean load(
			String sAssetNumber,
			Connection conn
			){
		m_sErrorMessage = "";
		String sSQL = "";
		try{
			//Get the record to edit:
			sSQL = "SELECT * FROM " + SMTablefamaster.TableName
					+ " WHERE ("
					+ SMTablefamaster.sAssetNumber + " = '" + sAssetNumber + "'"
					+ ")"
					;
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
			try{
				loadFromResultSet(rs);
			}catch (SQLException e){
				m_sErrorMessage = "Error loading asset '" + sAssetNumber + "' - " + e.getMessage();
				rs.close();
				return false;
			}

		}catch (SQLException ex){
			m_sErrorMessage = "Error loading asset with SQL: " + sSQL + " - " + ex.getMessage();
			return false;
		}
		return true;
	}

	private void loadFromResultSet(ResultSet rs) throws SQLException{
		try{
			if (rs.next()){

				m_sID = rs.getString(SMTablefamaster.sId);
				m_sTruckNumber = clsStringFunctions.checkStringForNull(rs.getString(SMTablefamaster.sTruckNumber));
				m_sNotePayableGLAcct = clsStringFunctions.checkStringForNull(rs.getString(SMTablefamaster.sNotePayableGLAcct));
				m_sState = clsStringFunctions.checkStringForNull(rs.getString(SMTablefamaster.sState));
				m_sAssetNumber = clsStringFunctions.checkStringForNull(rs.getString(SMTablefamaster.sAssetNumber));
				m_sDescription = clsStringFunctions.checkStringForNull(rs.getString(SMTablefamaster.sDescription));
				//String sDate = rs.getString(SMTablefamaster.datAcquisitionDate);
				//m_sAcquisitionDate = sDate.substring(5, 7) + "/" + sDate.substring(8, 10) + "/" + sDate.substring(0, 4);
				
				m_sAcquisitionDate = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTablefamaster.datAcquisitionDate));
				
				m_sAcquisitionAmount = clsManageBigDecimals.BigDecimalToFormattedString("########0.00",
						rs.getBigDecimal(SMTablefamaster.bdAcquisitionAmount));
				m_sClass = clsStringFunctions.checkStringForNull(rs.getString(SMTablefamaster.sClass));
				m_sSerialNumber = clsStringFunctions.checkStringForNull(rs.getString(SMTablefamaster.sSerialNumber));
				m_sLicenseTagNumber = clsStringFunctions.checkStringForNull(rs.getString(SMTablefamaster.sLicenseTagNumber));
				m_sLocation = clsStringFunctions.checkStringForNull(rs.getString(SMTablefamaster.sLocation));
				//sDate = rs.getString(SMTablefamaster.datDateSold);
				//m_sDateSold = sDate.substring(5, 7) + "/" + sDate.substring(8, 10) + "/" + sDate.substring(0, 4);
				m_sDateSold = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTablefamaster.datDateSold));

				m_sGaragedLocation = clsStringFunctions.checkStringForNull(rs.getString(SMTablefamaster.sGaragedLocation));
				m_sLossOrGainGL = clsStringFunctions.checkStringForNull(rs.getString(SMTablefamaster.sLossOrGainGL));
				m_sDepreciationType = clsStringFunctions.checkStringForNull(rs.getString(SMTablefamaster.sDepreciationType));
				m_sCurrentValue = clsManageBigDecimals.BigDecimalToFormattedString("########0.00",
						rs.getBigDecimal(SMTablefamaster.bdCurrentValue));
				m_sComment = clsStringFunctions.checkStringForNull(rs.getString(SMTablefamaster.sComment));
				m_sAmountSoldFor = clsManageBigDecimals.BigDecimalToFormattedString("########0.00",
						rs.getBigDecimal(SMTablefamaster.bdAmountSoldFor));
				m_sDepreciationGLAcct = clsStringFunctions.checkStringForNull(rs.getString(SMTablefamaster.sDepreciationGLAcct));
				m_sAccumulatedDepreciationGLAcct = clsStringFunctions.checkStringForNull(rs.getString(SMTablefamaster.sAccumulatedDepreciationGLAcct));
				m_sAccumulatedDepreciation = clsManageBigDecimals.BigDecimalToFormattedString("########0.00",
						rs.getBigDecimal(SMTablefamaster.bdAccumulatedDepreciation));
				m_sSalvageValue = clsManageBigDecimals.BigDecimalToFormattedString("########0.00",
						rs.getBigDecimal(SMTablefamaster.bdSalvageValue));
				m_sDriver = clsStringFunctions.checkStringForNull(rs.getString(SMTablefamaster.sdriver));
				m_sComment1 = clsStringFunctions.checkStringForNull(rs.getString(SMTablefamaster.scomment1));
				m_sComment2 = clsStringFunctions.checkStringForNull(rs.getString(SMTablefamaster.scomment2));
				m_sComment3 = clsStringFunctions.checkStringForNull(rs.getString(SMTablefamaster.scomment3));
				m_sgdoclink = clsStringFunctions.checkStringForNull(rs.getString(SMTablefamaster.sgdoclink));
				rs.close();
			}else{
				rs.close();
				throw new SQLException ("Could not load asset.");
			}
		}catch(SQLException ex){
			throw new SQLException ("Error in LoadFromResultSet - " + ex.getMessage());
		}
	}
	public boolean load(
			ServletContext context, 
			String sDBID
			){

		return load(m_sAssetNumber, context, sDBID);
	}
	public boolean load(
			Connection conn
			){

		return load(m_sAssetNumber, conn);
	}
	//Need a connection here for the data transaction:
	public boolean save(String sUserID, Connection conn){
		m_sErrorMessage = "";

		//First, validate information:
		if (!validateNewCode()){
			return false;
		}

		//Check to see if the record already exists:
		String SQL = "SELECT * FROM " + SMTablefamaster.TableName
				+ " WHERE ("
				+ SMTablefamaster.sAssetNumber + " = '" + m_sAssetNumber + "'"
				+ ")";

		try{

			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);

			if(rs.next()){

				rs.close();
				//update
				if(!validateEntries()){
					return false;
				}

				//System.out.println("Before conversion: " + m_sDateSold);
				if (m_sDateSold.trim().length() == 0 || 
						m_sDateSold.trim().compareTo(EMPTY_DATE_STRING) == 0){
					m_sDateSold = "0000-00-00 00:00:00";
				}else{
					m_sDateSold = clsDateAndTimeConversions.stdDateStringToSQLDateString(m_sDateSold);
				}
				//System.out.println("After conversion: " + m_sDateSold);

				SQL = "UPDATE " + SMTablefamaster.TableName + " SET"
						+ " " + SMTablefamaster.datAcquisitionDate + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_sAcquisitionDate) + "'"
						+ ", " + SMTablefamaster.datDateSold + " = '" + m_sDateSold + "'"
						+ ", " + SMTablefamaster.sTruckNumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sTruckNumber) + "'"
						+ ", " + SMTablefamaster.sNotePayableGLAcct + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sNotePayableGLAcct) + "'"
						+ ", " + SMTablefamaster.sState + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sState) + "'"
						+ ", " + SMTablefamaster.sDescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sDescription) + "'"
						+ ", " + SMTablefamaster.sClass + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sClass) + "'"
						+ ", " + SMTablefamaster.sSerialNumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sSerialNumber) + "'"
						+ ", " + SMTablefamaster.sLicenseTagNumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sLicenseTagNumber) + "'"
						+ ", " + SMTablefamaster.sLocation + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sLocation) + "'"
						+ ", " + SMTablefamaster.sGaragedLocation + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sGaragedLocation) + "'"
						+ ", " + SMTablefamaster.sLossOrGainGL + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sLossOrGainGL) + "'"
						+ ", " + SMTablefamaster.sDepreciationType + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sDepreciationType) + "'"
						+ ", " + SMTablefamaster.sDepreciationGLAcct + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sDepreciationGLAcct) + "'"
						+ ", " + SMTablefamaster.sComment + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sComment) + "'"
						+ ", " + SMTablefamaster.sAccumulatedDepreciationGLAcct + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sAccumulatedDepreciationGLAcct) + "'"
						+ ", " + SMTablefamaster.bdAcquisitionAmount + " = '" + m_sAcquisitionAmount.replace(",", "") + "'"
						+ ", " + SMTablefamaster.bdCurrentValue + " = '" + clsManageBigDecimals.BigDecimalToFormattedString(
							"########0.00", new BigDecimal(m_sAcquisitionAmount.replace(",", "")).subtract(
								new BigDecimal(m_sAccumulatedDepreciation.replace(",", "")))) + "'" //m_sCurrentValue.replace(",", "") + "'"
						+ ", " + SMTablefamaster.bdAmountSoldFor + " = '" + m_sAmountSoldFor.replace(",", "") + "'"
						+ ", " + SMTablefamaster.bdAccumulatedDepreciation + " = '" + m_sAccumulatedDepreciation.replace(",", "") + "'"
						+ ", " + SMTablefamaster.bdSalvageValue + " = '" + m_sSalvageValue.replace(",", "") + "'"
						+ ", " + SMTablefamaster.sdriver + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sDriver) + "'"
						+ ", " + SMTablefamaster.scomment1 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sComment1) + "'"
						+ ", " + SMTablefamaster.scomment2 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sComment2) + "'"
						+ ", " + SMTablefamaster.scomment3 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sComment3) + "'"
						+ ", " + SMTablefamaster.sgdoclink + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdoclink) + "'"
				+ " WHERE ("
				+ SMTablefamaster.sAssetNumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sAssetNumber) + "'"
				+ ")"
				;
				if (bDebugMode){
					clsServletUtilities.sysprint(this.toString(), sUserID, "UPDATE SQL = " + SQL);
				}
				try{
					Statement stmt = conn.createStatement();
					stmt.executeUpdate(SQL);
				}catch (SQLException e){
					m_sErrorMessage = "Cannot execute UPDATE sql: " + SQL + " - " + e.getMessage() + ".";
					return false;
				}
				return true;

			}else{

				rs.close();
				if(!validateEntries()){
					return false;
				}

				SQL = "INSERT INTO " + SMTablefamaster.TableName + " ("
						+ " " + SMTablefamaster.sTruckNumber
						+ ", " + SMTablefamaster.sNotePayableGLAcct
						+ ", " + SMTablefamaster.sState
						+ ", " + SMTablefamaster.sAssetNumber
						+ ", " + SMTablefamaster.sDescription
						+ ", " + SMTablefamaster.datAcquisitionDate
						+ ", " + SMTablefamaster.bdAcquisitionAmount
						+ ", " + SMTablefamaster.sClass
						+ ", " + SMTablefamaster.sSerialNumber
						+ ", " + SMTablefamaster.sLicenseTagNumber
						+ ", " + SMTablefamaster.sLocation
						+ ", " + SMTablefamaster.datDateSold
						+ ", " + SMTablefamaster.sGaragedLocation
						+ ", " + SMTablefamaster.sLossOrGainGL
						+ ", " + SMTablefamaster.sDepreciationType
						+ ", " + SMTablefamaster.bdCurrentValue
						+ ", " + SMTablefamaster.sComment
						+ ", " + SMTablefamaster.bdAmountSoldFor
						+ ", " + SMTablefamaster.sDepreciationGLAcct
						+ ", " + SMTablefamaster.sAccumulatedDepreciationGLAcct
						+ ", " + SMTablefamaster.bdAccumulatedDepreciation
						+ ", " + SMTablefamaster.bdSalvageValue
						+ ", " + SMTablefamaster.sdriver
						+ ", " + SMTablefamaster.scomment1
						+ ", " + SMTablefamaster.scomment2
						+ ", " + SMTablefamaster.scomment3
						+ ", " + SMTablefamaster.sgdoclink
						
						+ " ) VALUES ("
						+ "'" + clsDatabaseFunctions.FormatSQLStatement(m_sTruckNumber) + "'" 
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sNotePayableGLAcct) + "'" 
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sState) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sAssetNumber) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sDescription) + "'"
						+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_sAcquisitionDate) + "'"
						+ ", " + m_sAcquisitionAmount.replace(",", "")
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sClass) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sSerialNumber) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sLicenseTagNumber) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sLocation) + "'"
						+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_sDateSold) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sGaragedLocation) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sLossOrGainGL) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sDepreciationType) + "'"
						+ ", " + clsManageBigDecimals.BigDecimalToFormattedString("########0.00", new BigDecimal(m_sAcquisitionAmount.replace(",", "")).subtract(new BigDecimal(m_sAccumulatedDepreciation.replace(",", "")))) //m_sCurrentValue.replace(",", "") //new asset gets it book value set here.
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sComment) + "'"
						+ ", " + m_sAmountSoldFor.replace(",", "")
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sDepreciationGLAcct) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sAccumulatedDepreciationGLAcct) + "'"
						+ ", " + m_sAccumulatedDepreciation.replace(",", "")
						+ ", " + m_sSalvageValue.replace(",", "")
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sDriver) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sComment1) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sComment2) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sComment3) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdoclink) + "'"
						+ ")"
						;
				try{
					Statement stmt = conn.createStatement();
					stmt.executeUpdate(SQL);
				}catch (SQLException e){
					clsDatabaseFunctions.rollback_data_transaction(conn);
					m_sErrorMessage = "Cannot execute asset INSERT SQL: " + SQL + " - " + e.getMessage() + ".";
					return false;
				}

				//If we get to here, we've succeeded:
				return true;				
			}

		}catch(SQLException e){
			m_sErrorMessage = "Error saving asset info - " + e.getMessage();
			return false;
		}
	}

	private boolean validateNewCode(){
		m_sErrorMessage = "";
		//All upper case:
		m_sAssetNumber = m_sAssetNumber.toUpperCase();

		if(!clsStringFunctions.validateStringCharacters(m_sAssetNumber, "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-")){
			m_sErrorMessage = "Invalid characters in asset number - must be uppercase characters or digits";
			return false;
		}else{
			return true;
		}

	}
	private boolean validateEntries(){

		boolean bEntriesAreValid = true;
		m_sErrorMessage = "";

		if (m_sAssetNumber.trim().compareToIgnoreCase("") == 0){
			m_sErrorMessage = "Asset number cannot be blank";
			bEntriesAreValid = false;
		}

		m_sAssetNumber = m_sAssetNumber.replace("-", "");
		m_sAssetNumber = m_sAssetNumber.replace("'", "");
		m_sAssetNumber = m_sAssetNumber.replace(" ", "");
		m_sAssetNumber = m_sAssetNumber.toUpperCase();

		if (m_sAssetNumber.length() > SMTablefamaster.sAssetNumberLength){
			m_sErrorMessage = "Asset number cannot be longer than " 
					+ SMTablefamaster.sAssetNumberLength + " characters.";
			bEntriesAreValid = false;
		}

		if (m_sDescription.length() > SMTablefamaster.sDescriptionLength){
			m_sErrorMessage = "Asset description cannot be longer than " 
					+ SMTablefamaster.sDescriptionLength + " characters.";
			bEntriesAreValid = false;
		}
		if (m_sDescription.trim().compareToIgnoreCase("") == 0){
			m_sErrorMessage = "Asset description cannot be blank";
			bEntriesAreValid = false;
		}

		if(!clsDateAndTimeConversions.IsValidDateString("MM/dd/yyyy", m_sAcquisitionDate)){
			//Allow an empty date:
			if (m_sAcquisitionDate.compareToIgnoreCase(EMPTY_DATE_STRING) == 0){
				m_sErrorMessage = "Please select an acquisition date before continuing.";
			}else{
				m_sErrorMessage = "Invalid acquisition date: " + m_sAcquisitionDate; 
				bEntriesAreValid = false;
			}
		}
		if (m_sAcquisitionAmount.trim().compareToIgnoreCase("") == 0){
			m_sAcquisitionAmount = "0.00";
		}else{
			try{
				m_sAcquisitionAmount = m_sAcquisitionAmount.replace(",", "");
				BigDecimal bd = new BigDecimal(m_sAcquisitionAmount);
				bd = bd.setScale(SMTablefamaster.bdAcquisitionAmountScale, BigDecimal.ROUND_HALF_UP);
				m_sAcquisitionAmount =  clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablefamaster.bdAcquisitionAmountScale, bd);
			}catch (NumberFormatException e){
				m_sErrorMessage = "Invalid acquisition amount.";
				bEntriesAreValid = false;
			}
		}

		if (m_sDepreciationType.length() > SMTablefamaster.sDepreciationTypeLength){
			m_sErrorMessage = "Depreciation type cannot be longer than " 
					+ SMTablefamaster.sDepreciationTypeLength + " characters.";
			bEntriesAreValid = false;
		}	
		if (m_sDepreciationType.length() == 0){
			m_sErrorMessage = "Please select a depreciation type before continuing.";
			bEntriesAreValid = false;
		}
		if (m_sClass.length() > SMTablefamaster.sClassLength){
			m_sErrorMessage = "Class cannot be longer than " 
					+ SMTablefamaster.sClassLength + " characters.";
			bEntriesAreValid = false;
		}
		if (m_sClass.length() == 0){
			m_sErrorMessage = "Please select a class before continuing. ";
			bEntriesAreValid = false;
		}
		if (m_sSerialNumber.length() > SMTablefamaster.sSerialNumberLength){
			m_sErrorMessage = "Serial number cannot be longer than " 
					+ SMTablefamaster.sSerialNumberLength + " characters.";
			bEntriesAreValid = false;
		}
		if (m_sLocation.length() > SMTablefamaster.sLocationLength){
			m_sErrorMessage = "Location tag number cannot be longer than " 
					+ SMTablefamaster.sLocationLength + " characters.";
			bEntriesAreValid = false;
		}
		if (m_sLocation.length() == 0){
			m_sErrorMessage = "Please select a location before continuing.";
			bEntriesAreValid = false;
		}
		if (m_sState.length() > SMTablefamaster.sStateLength){
			m_sErrorMessage = "State cannot be longer than " 
					+ SMTablefamaster.sStateLength + " characters.";
			bEntriesAreValid = false;
		}

		if(!clsDateAndTimeConversions.IsValidDateString("MM/dd/yyyy", m_sDateSold)){
			//Allow an empty date:
			if (m_sDateSold.compareToIgnoreCase(EMPTY_DATE_STRING) == 0){
			}else{
				m_sErrorMessage = "Invalid date sold: " + m_sDateSold; 
				bEntriesAreValid = false;
			}
		}

		if (m_sAmountSoldFor.trim().compareToIgnoreCase("") == 0){
			m_sAmountSoldFor = "0.00";
		}else{
			try{
				m_sAmountSoldFor = m_sAmountSoldFor.replace(",", "");
				BigDecimal bd = new BigDecimal(m_sAmountSoldFor);
				bd = bd.setScale(SMTablefamaster.bdAmountSoldForScale, BigDecimal.ROUND_HALF_UP);
				m_sAmountSoldFor =  clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablefamaster.bdAmountSoldForScale, bd);
			}catch (NumberFormatException e){
				m_sErrorMessage = "Invalid amount sold for.";
				bEntriesAreValid = false;
			}
		}

		if (m_sLicenseTagNumber.length() > SMTablefamaster.sLicenseTagNumberLength){
			m_sErrorMessage = "License tag number cannot be longer than " 
					+ SMTablefamaster.sLicenseTagNumberLength + " characters.";
			bEntriesAreValid = false;
		}
		if (m_sGaragedLocation.length() > SMTablefamaster.sGaragedLocationLength){
			m_sErrorMessage = "Garaged location cannot be longer than " 
					+ SMTablefamaster.sGaragedLocationLength + " characters.";
			bEntriesAreValid = false;
		}
		if (m_sTruckNumber.length() > SMTablefamaster.sTruckNumberLength){
			m_sErrorMessage = "Truck number cannot be longer than " 
					+ SMTablefamaster.sTruckNumberLength + " characters.";
			bEntriesAreValid = false;
		}
		if (m_sSalvageValue.trim().compareToIgnoreCase("") == 0){
			m_sSalvageValue = "0.00";
		}else{
			try{
				m_sSalvageValue = m_sSalvageValue.replace(",", "");
				BigDecimal bd = new BigDecimal(m_sSalvageValue);
				bd = bd.setScale(SMTablefamaster.bdSalvageValueScale, BigDecimal.ROUND_HALF_UP);
				m_sSalvageValue =  clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablefamaster.bdSalvageValueScale, bd);
			}catch (NumberFormatException e){
				m_sErrorMessage = "Invalid salvage value.";
				bEntriesAreValid = false;
			}
		}

		if (m_sAccumulatedDepreciation.trim().compareToIgnoreCase("") == 0){
			m_sAccumulatedDepreciation = "0.00";
		}else{
			try{
				m_sAccumulatedDepreciation = m_sAccumulatedDepreciation.replace(",", "");
				BigDecimal bd = new BigDecimal(m_sAccumulatedDepreciation);
				bd = bd.setScale(SMTablefamaster.bdAccumulatedDepreciationScale, BigDecimal.ROUND_HALF_UP);
				m_sAccumulatedDepreciation =  clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablefamaster.bdAccumulatedDepreciationScale, bd);
			}catch (NumberFormatException e){
				m_sErrorMessage = "Invalid accumulated depreciation.";
				bEntriesAreValid = false;
			}
		}

		if (m_sCurrentValue.trim().compareToIgnoreCase("") == 0){
			m_sCurrentValue = "0.00";
		}else{
			try{
				m_sCurrentValue = m_sCurrentValue.replace(",", "");
				BigDecimal bd = new BigDecimal(m_sCurrentValue);
				bd = bd.setScale(SMTablefamaster.bdCurrentValueScale, BigDecimal.ROUND_HALF_UP);
				m_sCurrentValue =  clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablefamaster.bdCurrentValueScale, bd);
			}catch (NumberFormatException e){
				m_sErrorMessage = "Invalid current value.";
				bEntriesAreValid = false;
			}
		}
		if (m_sComment.length() > SMTablefamaster.sCommentLength){
			m_sErrorMessage = "Comment cannot be longer than " 
					+ SMTablefamaster.sCommentLength + " characters.";
			bEntriesAreValid = false;
		}
		if (m_sNotePayableGLAcct.length() > SMTablefamaster.sNotePayableGLAcctLength){
			m_sErrorMessage = "Note payable GL account number cannot be longer than " 
					+ SMTablefamaster.sNotePayableGLAcctLength + " characters.";
			bEntriesAreValid = false;
		}
		if (m_sLossOrGainGL.length() > SMTablefamaster.sLossOrGainGLLength){
			m_sErrorMessage = "Loss or gain GL account cannot be longer than " 
					+ SMTablefamaster.sLossOrGainGLLength + " characters.";
			bEntriesAreValid = false;
		}
		if (m_sDepreciationGLAcct.length() > SMTablefamaster.sDepreciationGLAcctLength){
			m_sErrorMessage = "Depreciation GL account number cannot be longer than " 
					+ SMTablefamaster.sDepreciationGLAcctLength + " characters.";
			bEntriesAreValid = false;
		}

		if (m_sDepreciationGLAcct.length() == 0){
			m_sErrorMessage = "Please select a depreciation GL account number before continuing.";
			bEntriesAreValid = false;
		}

		if (m_sAccumulatedDepreciationGLAcct.length() > SMTablefamaster.sAccumulatedDepreciationGLAcctLength){
			m_sErrorMessage = "Accumulated depreciation  GL account number cannot be longer than " 
					+ SMTablefamaster.sAccumulatedDepreciationGLAcctLength + " characters.";
			bEntriesAreValid = false;
		}

		if (m_sAccumulatedDepreciationGLAcct.length() == 0){
			m_sErrorMessage = "Please select a accumulated depreciation GL account number before continuing.";
			bEntriesAreValid = false;
		}

		if (m_sDriver.length() > SMTablefamaster.sdriverLength){
			m_sErrorMessage = "Driver cannot be longer than " 
					+ SMTablefamaster.sdriverLength + " characters.";
			bEntriesAreValid = false;
		}
		
		if (m_sComment1.length() > SMTablefamaster.sComment1Length){
			m_sErrorMessage = "Comment1 cannot be longer than " 
					+ SMTablefamaster.sComment1Length + " characters.";
			bEntriesAreValid = false;
		}
		
		if (m_sComment2.length() > SMTablefamaster.sComment2Length){
			m_sErrorMessage = "Comment2 cannot be longer than " 
					+ SMTablefamaster.sComment2Length + " characters.";
			bEntriesAreValid = false;
		}
		
		if (m_sComment3.length() > SMTablefamaster.sComment3Length){
			m_sErrorMessage = "Comment3 cannot be longer than " 
					+ SMTablefamaster.sComment3Length + " characters.";
			bEntriesAreValid = false;
		}
		return bEntriesAreValid;
	}

	public String getQueryString(){

		String sQueryString = "";

		sQueryString += ParamID + "=" + clsServletUtilities.URLEncode(m_sID);
		sQueryString += ParamTruckNumber + "=" + clsServletUtilities.URLEncode(m_sTruckNumber);
		sQueryString += "&" + ParamNotePayableGLAcct + "=" + clsServletUtilities.URLEncode(m_sNotePayableGLAcct);
		sQueryString += "&" + ParamState + "=" + clsServletUtilities.URLEncode(m_sState);
		sQueryString += "&" + ParamAssetNumber + "=" + clsServletUtilities.URLEncode(m_sAssetNumber);
		sQueryString += "&" + ParamDescription + "=" + clsServletUtilities.URLEncode(m_sDescription);
		sQueryString += "&" + ParamAcquisitionDate + "=" + clsServletUtilities.URLEncode(m_sAcquisitionDate);
		sQueryString += "&" + ParamAcquisitionAmount + "=" + clsServletUtilities.URLEncode(m_sAcquisitionAmount);
		sQueryString += "&" + ParamClass + "=" + clsServletUtilities.URLEncode(m_sClass);
		sQueryString += "&" + ParamSerialNumber + "=" + clsServletUtilities.URLEncode(m_sSerialNumber);
		sQueryString += "&" + ParamLicenseTagNumber + "=" + clsServletUtilities.URLEncode(m_sLicenseTagNumber);
		sQueryString += "&" + ParamLocation + "=" + clsServletUtilities.URLEncode(m_sLocation);
		sQueryString += "&" + ParamDateSold + "=" + clsServletUtilities.URLEncode(m_sDateSold);
		sQueryString += "&" + ParamGaragedLocation + "=" + clsServletUtilities.URLEncode(m_sGaragedLocation);
		sQueryString += "&" + ParamLossOrGainGL + "=" + clsServletUtilities.URLEncode(m_sLossOrGainGL);
		sQueryString += "&" + ParamDepreciationType + "=" + clsServletUtilities.URLEncode(m_sDepreciationType);
		sQueryString += "&" + ParamCurrentValue + "=" + clsServletUtilities.URLEncode(m_sCurrentValue);
		sQueryString += "&" + ParamComment + "=" + clsServletUtilities.URLEncode(m_sComment);
		sQueryString += "&" + ParamAmountSoldFor + "=" + clsServletUtilities.URLEncode(m_sAmountSoldFor);
		sQueryString += "&" + ParamDepreciationGLAcct + "=" + clsServletUtilities.URLEncode(m_sDepreciationGLAcct);
		sQueryString += "&" + ParamAccumulatedDepreciationGLAcct + "=" + clsServletUtilities.URLEncode(m_sAccumulatedDepreciationGLAcct);
		sQueryString += "&" + ParamAccumulatedDepreciation + "=" + clsServletUtilities.URLEncode(m_sAccumulatedDepreciation);
		sQueryString += "&" + ParamSalvageValue + "=" + clsServletUtilities.URLEncode(m_sSalvageValue);
		sQueryString += "&" + ParamDriver + "=" + clsServletUtilities.URLEncode(m_sDriver);
		sQueryString += "&" + ParamComment1 + "=" + clsServletUtilities.URLEncode(m_sComment1);
		sQueryString += "&" + ParamComment2 + "=" + clsServletUtilities.URLEncode(m_sComment2);
		sQueryString += "&" + ParamComment3 + "=" + clsServletUtilities.URLEncode(m_sComment3);
		sQueryString += "&" + Paramgdoclink + "=" + clsServletUtilities.URLEncode(m_sgdoclink);

		return sQueryString;
	}

	public void delete(String sAssetNumber, Connection conn) throws Exception{

		m_sErrorMessage = "";

		//First, check that the asset exists:
		BigDecimal bdRemainingDepreciation = new BigDecimal("0.00");
		String SQL = "SELECT * FROM " + SMTablefamaster.TableName
				+ " WHERE ("
				+ SMTablefamaster.sAssetNumber + " = '" + sAssetNumber + "'"
				+ ")"
				;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(!rs.next()){
				m_sErrorMessage = "Asset '" + sAssetNumber + "' cannot be found.";
				rs.close();
				throw new Exception("Error [1577735056] - asset " + sAssetNumber + " cannot be found.");
			}else{
				//Make sure the asset is fully depreciated:
				bdRemainingDepreciation = (rs.getBigDecimal(SMTablefamaster.bdAcquisitionAmount).subtract(
					rs.getBigDecimal(SMTablefamaster.bdSalvageValue))).subtract(
						rs.getBigDecimal(SMTablefamaster.bdAccumulatedDepreciation))
				;
				rs.close();
				if (bdRemainingDepreciation.compareTo(BigDecimal.ZERO) > 0){
					throw new Exception("Error [20193641455417] " + "This asset has depreciation remaining on it, so it cannot be deleted."
						+ "  If you really want to delete it, first set the salvage value to the amount of depreciation remaining, and this"
						+ " asset will be considered FULLY depreciated."
					);
				}
			}
		}catch(Exception e){
			throw new Exception("Error [1577735055] checking asset " + sAssetNumber + " to delete - " + e.getMessage());
		}
		
		//Start a data transaction:
		if (!ServletUtilities.clsDatabaseFunctions.start_data_transaction(conn)){
			throw new Exception("Error [20193641445232] " + "Could not start data transaction.");
		};
		
		//First, try to delete all the associated transactions:
		try{
			SQL = "DELETE FROM " + SMTablefatransactions.TableName
				+ " WHERE ("
				+ SMTablefatransactions.sTransAssetNumber + " = '" + sAssetNumber + "'"
				+ ")"
			;
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch(Exception e){
			ServletUtilities.clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [2019364145063] " + "deleting depreciation transactions with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		if (!ServletUtilities.clsDatabaseFunctions.commit_data_transaction(conn)){
			ServletUtilities.clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [20193641445232] " + "Could not start data transaction.");
		};	
		
		//Now delete the asset itself:
		try{
			SQL = "DELETE FROM " + SMTablefamaster.TableName
					+ " WHERE ("
					+ SMTablefamaster.sAssetNumber + " = '" + sAssetNumber + "'"
					+ ")"
					;
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch(Exception e){
			ServletUtilities.clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [2019364145053] " + "deleting asset with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		if (!ServletUtilities.clsDatabaseFunctions.commit_data_transaction(conn)){
			ServletUtilities.clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [20193641445232] " + "Could not start data transaction.");
		};		
		
		return;
	}

	//Depreciates the asset
	public BigDecimal Depreciate_Asset(int iFiscalYear,
			int iFiscalPeriod,
			boolean bProvisional,
			Date datTransDate,
			//BigDecimal bdDepAmount,
			String sUserID,
			Connection conn
			) throws Exception{

		BigDecimal bdDepreciationAmount = BigDecimal.ZERO;
		BigDecimal bdCurrentValue = new BigDecimal(m_sCurrentValue.replace(",", ""));
		BigDecimal bdAccumulatedDepreciation = new BigDecimal(m_sAccumulatedDepreciation.replace(",", ""));

		//Next, calculate the amount of depreciation:
		try{
			bdDepreciationAmount = Calculate_Depreciation(iFiscalYear,
					iFiscalPeriod,
					conn);
		}catch(Exception e){
			throw new Exception ("Error [1528994652] calculating depreciation for asset number " + m_sAssetNumber + ".<BR>" + e.getMessage());
		}

		if (bdDepreciationAmount.compareTo(BigDecimal.ZERO) == 0){
			return BigDecimal.ZERO;
		}

		//Next, add the depreciation record to the transactions table:
		try {
			Insert_Depreciation_Record(iFiscalYear,
					iFiscalPeriod,
					bProvisional,
					datTransDate,
					bdDepreciationAmount,
					conn);
		} catch (Exception e) {
			throw new Exception ("Error [1528994653] inserting depreciation record into transaction table for asset number " + m_sAssetNumber + " - " + e.getMessage());
		}

		//Next, update the current values on the asset master:
		if (!bProvisional){
			bdCurrentValue = bdCurrentValue.subtract(bdDepreciationAmount).setScale(2, BigDecimal.ROUND_HALF_UP);
			bdAccumulatedDepreciation = bdAccumulatedDepreciation.add(bdDepreciationAmount).setScale(2, BigDecimal.ROUND_HALF_UP);

			setCurrentValue(bdCurrentValue.toString());
			setAccumulatedDepreciation(bdAccumulatedDepreciation.toString());

			if (!save(sUserID, conn)){
				throw new Exception("Error [1528994654] Unable to save asset with new current value: " + m_sErrorMessage);
			}
		}  
		return bdDepreciationAmount;
	}

	private BigDecimal Calculate_Depreciation(int iFiscalYear, 
			int iFiscalPeriod,
			Connection conn) throws Exception{

		String sCalculationType = "";
		int iNoOfMonths = 0;

		//If the item is fully depreciated, then just return a zero, no matter what:
		if (new BigDecimal(m_sCurrentValue).compareTo(new BigDecimal(m_sSalvageValue)) <= 0){
			return BigDecimal.ZERO;
		}

		//Otherwise . . . .
		//First get the depreciation calculation factors:
		String sSQL = "SELECT " + 
				" " + SMTablefadepreciationtype.iLifeInMonths + "," +
				" " + SMTablefadepreciationtype.sCalculationType + 
				" FROM" + 
				" " + SMTablefadepreciationtype.TableName +
				" WHERE" +
				" " + SMTablefadepreciationtype.sDepreciationType + " = '" + m_sDepreciationType + "'";
		try{
			ResultSet rsDepfactors = clsDatabaseFunctions.openResultSet(sSQL, conn);
			if (rsDepfactors.next()){
				sCalculationType = rsDepfactors.getString(SMTablefadepreciationtype.sCalculationType);
				iNoOfMonths = rsDepfactors.getInt(SMTablefadepreciationtype.iLifeInMonths);
			}
			rsDepfactors.close();
		}catch(SQLException e){
			throw new Exception("Error [1528994655] Calculating depreciation using SQL: " + sSQL + " - " + e.getMessage());
		}
		if (sCalculationType.trim().compareTo("SL") == 0){
			return Calculate_SL(iFiscalYear, iFiscalPeriod, iNoOfMonths);
		}else{
			return BigDecimal.ZERO;
		}
	}

	private BigDecimal Calculate_SL(int iFiscalYear,
			int iFiscalPeriod,
			int iLifeInMonths) throws Exception{


		//Divide the total purchased price by the term to get the monthly depreciation:
		if (iLifeInMonths <= 0){
			throw new Exception ("Error [1528994656] Invalid depreciation term for asset number " + m_sAssetNumber + ".");
		}

		//Then the monthly depreciation is simply the total purchase price, less the salvage value, divided by the life in months:
		BigDecimal bdMonthlyDepreciation = 
				new BigDecimal(m_sAcquisitionAmount).setScale(2, BigDecimal.ROUND_HALF_UP).subtract(
						new BigDecimal(m_sSalvageValue).setScale(2, BigDecimal.ROUND_HALF_UP)).divide(new BigDecimal(iLifeInMonths), 2, BigDecimal.ROUND_HALF_UP);

		//If the calculated depreciation is less than the net value remaining, then just set the depreciation to the amount remaining.
		//And then this will be the last depreciation on this asset.
		if (bdMonthlyDepreciation.setScale(2, BigDecimal.ROUND_HALF_UP).compareTo(
				new BigDecimal(m_sCurrentValue).setScale(2, BigDecimal.ROUND_HALF_UP).subtract(
						new BigDecimal(m_sSalvageValue).setScale(2, BigDecimal.ROUND_HALF_UP)
						)
				) >= 0){
			return new BigDecimal(m_sCurrentValue).setScale(2, BigDecimal.ROUND_HALF_UP).subtract(
					new BigDecimal(m_sSalvageValue).setScale(2, BigDecimal.ROUND_HALF_UP)
					);
		}else{
			return bdMonthlyDepreciation.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
	}	
	private void Insert_Depreciation_Record(int iFiscalYear,
			int iFiscalPeriod,
			boolean bProvisional,
			Date datTransactionDate,
			BigDecimal bdDepreciationAmount,
			Connection conn) throws Exception{
		String sComment;

		if (bProvisional){
			sComment = "Provisional Depreciation processing";
		}else{
			sComment = "Depreciation processing";
		}

		try {
			Insert_Transaction_Record(bdDepreciationAmount,
					datTransactionDate,
					iFiscalPeriod,
					iFiscalYear,
					bProvisional,
					m_sAccumulatedDepreciationGLAcct,
					m_sDepreciationGLAcct,
					m_sAssetNumber,
					SMTablefatransactions.DEPRECIATION_FLAG, //TRANSACTION_DEPRECIATION_FLAG
					sComment, 
					conn);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	private void Insert_Transaction_Record(BigDecimal bdDepreciationAmount,
			Date datTransactionDate,
			int iFiscalPeriod,
			int iFiscalYear,
			boolean bProvisional,
			String sAccumulatedDepreciationGLAcct,
			String sDepreciationGLAcct,
			String sAssetNumber,
			String sFLAG,
			String sComment,
			Connection conn) throws Exception{

		int iProvisional = bProvisional? 1 : 0;

		String sSQL = 
				"INSERT INTO" + 
						" " + SMTablefatransactions.TableName + 
						"(" + 
						" " + SMTablefatransactions.dAmountDepreciated + "," +
						" " + SMTablefatransactions.datTransactionDate + "," +
						" " + SMTablefatransactions.datPostingDate + "," +
						" " + SMTablefatransactions.iFiscalPeriod + "," +
						" " + SMTablefatransactions.iFiscalYear + "," +
						" " + SMTablefatransactions.iProvisionalPosting + "," +
						" " + SMTablefatransactions.sTransAccumulatedDepreciationGLAcct + "," +
						" " + SMTablefatransactions.sTransAssetNumber + "," +
						" " + SMTablefatransactions.sTransDepreciationGLAcct + "," +
						" " + SMTablefatransactions.sTransactionType + "," +
						" " + SMTablefatransactions.sTransComment +
						") VALUES (" +
						" " + bdDepreciationAmount + "," +
						" '" + clsDateAndTimeConversions.utilDateToString(datTransactionDate, "yyyy-MM-dd") + "'," +
						" '" + clsDateAndTimeConversions.utilDateToString(new Date(System.currentTimeMillis()),"yyyy-MM-dd") + "'," +
						" " + iFiscalPeriod + "," +
						" " + iFiscalYear + "," +
						" " + iProvisional + "," +
						" '" + sAccumulatedDepreciationGLAcct + "'," +
						" '" + sAssetNumber + "'," +
						" '" + sDepreciationGLAcct + "'," +
						" '" + sFLAG + "'," +
						" '" + sComment + "'" +
						")";

		try{
			clsDatabaseFunctions.executeSQL(sSQL, conn);
		}catch(SQLException e){
			throw new Exception ("Error inserting depreciation transaction into database with SQL: " + sSQL + "<BR>" 
					+ e.getMessage() + "<BR>" + e.getSQLState());
		}
	}

	public String getID(){
		return m_sID;
	}
	public String getTruckNumber(){
		return m_sTruckNumber;
	}
	public void setTruckNumber(String s){
		m_sTruckNumber = s.trim();
	}
	public String getNotePayableGLAcct(){
		return m_sNotePayableGLAcct;
	}
	public void setNotePayableGLAcct(String s){
		m_sNotePayableGLAcct = s.trim();
	}
	public String getState(){
		return m_sState;
	}
	public void setState(String s){
		m_sState = s.trim();
	}
	public String getAssetNumber(){
		return m_sAssetNumber;
	}
	public void setAssetNumber(String s){
		m_sAssetNumber = s.trim();
	}
	public String getDescription(){
		return m_sDescription;
	}
	public void setDescription(String s){
		m_sDescription = s.trim();
	}
	public String getAcquisitionDate(){
		return m_sAcquisitionDate;
	}
	public void setAcquisitionDate(String s){
		m_sAcquisitionDate = s.trim();
	}
	public String getAcquisitionAmount(){
		return m_sAcquisitionAmount;
	}
	public void setAcquisitionAmount(String s){
		m_sAcquisitionAmount = s.trim().replace(",", "");
	}
	public String get_Class(){
		return m_sClass;
	}
	public void set_Class(String s){
		m_sClass = s.trim();
	}
	public String getSerialNumber(){
		return m_sSerialNumber;
	}
	public void setSerialNumber(String s){
		m_sSerialNumber = s.trim();
	}
	public String getLicenseTagNumber(){
		return m_sLicenseTagNumber;
	}
	public void setLicenseTagNumber(String s){
		m_sLicenseTagNumber = s.trim();
	}
	public String getLocation(){
		return m_sLocation;
	}
	public void setLocation(String s){
		m_sLocation = s.trim();
	}
	public String getDateSold(){
		return m_sDateSold;
	}
	public void setDateSold(String s){
		m_sDateSold = s.trim();
	}
	public String getGaragedLocation(){
		return m_sGaragedLocation;
	}
	public void setGaragedLocation(String s){
		m_sGaragedLocation = s.trim();
	}
	public String getLossOrGainGL(){
		return m_sLossOrGainGL;
	}
	public void setLossOrGainGL(String s){
		m_sLossOrGainGL = s.trim();
	}
	public String getDepreciationType(){
		return m_sDepreciationType;
	}
	public void setDepreciationType(String s){
		m_sDepreciationType = s.trim();
	}
	public String getCurrentValue(){
		return m_sCurrentValue;
	}
	public void setCurrentValue(String s){
		m_sCurrentValue = s.trim().replace(",", "");
	}
	public String getComment(){
		return m_sComment;
	}
	public void setComment(String s){
		m_sComment = s.trim();
	}
	public String getAmountSoldFor(){
		return m_sAmountSoldFor;
	}
	public void setAmountSoldFor(String s){
		m_sAmountSoldFor = s.trim().replace(",", "");
	}
	public String getDepreciationGLAcct(){
		return m_sDepreciationGLAcct;
	}
	public void setDepreciationGLAcct(String s){
		m_sDepreciationGLAcct = s.trim();
	}
	public String getAccumulatedDepreciationGLAcct(){
		return m_sAccumulatedDepreciationGLAcct;
	}
	public void setAccumulatedDepreciationGLAcct(String s){
		m_sAccumulatedDepreciationGLAcct = s.trim();
	}
	public String getAccumulatedDepreciation(){
		return m_sAccumulatedDepreciation;
	}
	public void setAccumulatedDepreciation(String s){
		m_sAccumulatedDepreciation = s.trim().replace(",", "");
	}
	public String getSalvageValue(){
		return m_sSalvageValue;
	}
	public void setSalvageValue(String s){
		m_sSalvageValue = s.trim().replace(",", "");
	}
	public String getDriver(){
		return m_sDriver;
	}
	public void setDriver(String s){
		m_sDriver = s.trim();
	}
	public String getComment1(){
		return m_sComment1;
	}
	public void setComment1(String s){
		m_sComment1 = s.trim();
	}
	public String getComment2(){
		return m_sComment2;
	}
	public void setComment2(String s){
		m_sComment2 = s.trim();
	}
	public String getComment3(){
		return m_sComment3;
	}
	public void setComment3(String s){
		m_sComment3 = s.trim();
	}
	public String getgdoclink(){
		return m_sgdoclink;
	}
	public void setgdoclink(String s){
		m_sgdoclink = s.trim();
	}
	public String getRemainingDepreciation(){
		BigDecimal bdAcquisitionAmt = new BigDecimal(getAcquisitionAmount().replace(",", ""));
		BigDecimal bdSalvageValue = new BigDecimal(getSalvageValue().replace(",", ""));
		BigDecimal bdAccumulatedDepreciation = new BigDecimal(getAccumulatedDepreciation().replace(",", ""));
		return ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat((bdAcquisitionAmt.subtract(bdSalvageValue)).subtract(bdAccumulatedDepreciation)).replace(",", "");
	}
	public String getErrorMessageString(){
		return clsServletUtilities.URLEncode(m_sErrorMessage);
	}
}
