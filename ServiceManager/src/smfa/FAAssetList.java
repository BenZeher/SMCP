package smfa;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTablefaclasses;
import SMDataDefinition.SMTablefamaster;
import SMDataDefinition.SMTablefatransactions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
public class FAAssetList extends java.lang.Object{

	private String m_sErrorMessage;

	public FAAssetList(){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sDBID,
			String sUserID,
			boolean bIncludeDisposed,
			boolean bIncludeNonDisposed,
			boolean bShowDetail,
			int iFiscalYear,
			PrintWriter out,
			ServletContext context,
			String sLicenseModuleLevel,
			ArrayList<String>arrClasses,
			ArrayList<String>arrLocations
			){

		if (arrClasses.size() == 0){
			m_sErrorMessage = "<BR><B>You must select at least one class to run the report.</B><BR>";
			return false;
		}
		
		String sCurrentClass = "";

		//create the report now.
		String SQL = "SELECT" + 
			" " + SMTablefamaster.TableName + "." + SMTablefamaster.bdAcquisitionAmount + "," +
			" " + SMTablefamaster.TableName + "." + SMTablefamaster.bdAmountSoldFor + "," +
			" " + SMTablefamaster.TableName + "." + SMTablefamaster.datAcquisitionDate + "," +
			" " + SMTablefamaster.TableName + "." + SMTablefamaster.datDateSold + "," +
			" " + SMTablefamaster.TableName + "." + SMTablefamaster.bdAccumulatedDepreciation + "," +
			" " + SMTablefamaster.TableName + "." + SMTablefamaster.bdCurrentValue + "," +
			" " + SMTablefamaster.TableName + "." + SMTablefamaster.bdSalvageValue + "," +
			" " + SMTablefamaster.TableName + "." + SMTablefamaster.sAccumulatedDepreciationGLAcct + "," +
			" " + SMTablefamaster.TableName + "." + SMTablefamaster.sAssetNumber + "," +
			" " + SMTablefamaster.TableName + "." + SMTablefamaster.sClass + "," +
			" " + SMTablefamaster.TableName + "." + SMTablefamaster.sComment + "," +
			" " + SMTablefamaster.TableName + "." + SMTablefamaster.sDepreciationGLAcct + "," +
			" " + SMTablefamaster.TableName + "." + SMTablefamaster.sDepreciationType + "," +
			" " + SMTablefamaster.TableName + "." + SMTablefamaster.sDescription + "," +
			" " + SMTablefamaster.TableName + "." + SMTablefamaster.sGaragedLocation + "," +
			" " + SMTablefamaster.TableName + "." + SMTablefamaster.sLicenseTagNumber + "," +
			" " + SMTablefamaster.TableName + "." + SMTablefamaster.sLocation + "," +
			" " + SMTablefamaster.TableName + "." + SMTablefamaster.sLossOrGainGL + "," +
			" " + SMTablefamaster.TableName + "." + SMTablefamaster.sNotePayableGLAcct + "," +
			" " + SMTablefamaster.TableName + "." + SMTablefamaster.sSerialNumber + "," +
			" " + SMTablefamaster.TableName + "." + SMTablefamaster.sState + "," +
			" " + SMTablefamaster.TableName + "." + SMTablefamaster.sTruckNumber + "," +
			" " + SMTablefaclasses.TableName + "." + SMTablefaclasses.sClassDescription +
			", IF (YEAR(" + SMTablefamaster.datAcquisitionDate + ") = " + Integer.toString(iFiscalYear) + ", " + SMTablefamaster.bdAcquisitionAmount
			+ ", 0.00) AS YTDPURCHASED" 
			+ ", IF (YEAR(" + SMTablefamaster.datDateSold + ") = " + Integer.toString(iFiscalYear) + ", " + SMTablefamaster.bdAcquisitionAmount
			+ ", 0.00) AS YTDDISPOSED"
			+ ", TRANSQUERY.YTDDEPAMT"
			+ ", IF (" + SMTablefamaster.TableName + "." + SMTablefamaster.datDateSold + " > '1900-01-01 00:00:00', 'Y', 'N') AS DISPOSED"
			+ " FROM" 
			+ " " + SMTablefamaster.TableName + " LEFT JOIN " 
			+ " " + SMTablefaclasses.TableName + " ON "
			+ SMTablefamaster.TableName + "." + SMTablefamaster.sClass + " = "
			+ SMTablefaclasses.TableName + "." + SMTablefaclasses.sClass
			+ " LEFT JOIN "
			+ "(SELECT "
			+ " SUM(" + SMTablefatransactions.dAmountDepreciated + ") AS YTDDEPAMT"
			+ ", " + SMTablefatransactions.sTransAssetNumber + " AS ASSETNUM"
			//+ ", " + SMTablefatransactions.iProvisionalPosting
			//+ ", " + SMTablefatransactions.iFiscalYear
			+ " FROM " + SMTablefatransactions.TableName 
			+ " WHERE ("
			;
			if (iFiscalYear != -1){
				SQL += "(" + SMTablefatransactions.iFiscalYear + " = " + Integer.toString(iFiscalYear) + ") AND ";
			}
			SQL += "(" + SMTablefatransactions.iProvisionalPosting + " != 1)"
			+ ")"
			+ " GROUP BY " + SMTablefatransactions.sTransAssetNumber
			
			+ ") AS TRANSQUERY"
			+ " ON " + SMTablefamaster.TableName + "." + SMTablefamaster.sAssetNumber + " = "
			+ " TRANSQUERY.ASSETNUM"

    		+ " WHERE (" +
    		" (" + SMTablefamaster.TableName + "." + SMTablefamaster.sClass + " =" +
    		" " + SMTablefaclasses.TableName + "." + SMTablefaclasses.sClass + ")";

		if (!bIncludeDisposed){
			//If we DON'T WANT disposed assets, then ONLY get assets with no real 'date sold'
			SQL += " AND (" + SMTablefamaster.TableName + "." + SMTablefamaster.datDateSold + " < '1900-01-01 00:00:00')";
		}

		if (!bIncludeNonDisposed){
			//If we DON'T want 'non-disposed' items, then ONLY get assets with a 'date sold':
			SQL += " AND (" + SMTablefamaster.TableName + "." + SMTablefamaster.datDateSold + " > '1900-01-01 00:00:00')";
		}	
		//Include only the classes selected:
		SQL += " AND (";
		for (int i = 0; i < arrClasses.size(); i++){
			if (i == 0){
				SQL += "(" + SMTablefamaster.TableName + "." + SMTablefamaster.sClass + " = '" + arrClasses.get(i) + "')";
			}else{
				SQL += " OR (" + SMTablefamaster.TableName + "." + SMTablefamaster.sClass + " = '" + arrClasses.get(i) + "')";
			}
		}
		SQL += ")";
		
		//Include only the locations selected:
		SQL += " AND (";
		for (int i = 0; i < arrLocations.size(); i++){
			if (i == 0){
				SQL += "(" + SMTablefamaster.TableName + "." + SMTablefamaster.sLocation + " = '" + arrLocations.get(i) + "')";
			}else{
				SQL += " OR (" + SMTablefamaster.TableName + "." + SMTablefamaster.sLocation + " = '" + arrLocations.get(i) + "')";
			}
		}
		SQL += ")";
		
		
		//End the 'WHERE' clause:
			SQL += ")";
			
		SQL += " ORDER BY " + SMTablefamaster.TableName + "." + SMTablefamaster.sClass + ", " + 
				SMTablefamaster.TableName + "." + SMTablefamaster.sLocation + ", " +
				SMTablefamaster.TableName + "." + SMTablefamaster.datAcquisitionDate +
				", " + SMTablefamaster.TableName + "." + SMTablefamaster.sAssetNumber;
		
		System.out.println("[1551287752] - SQL = '" + SQL + "'");
		
		BigDecimal bdYTDPurch = BigDecimal.ZERO;
		BigDecimal bdYTDDisp = BigDecimal.ZERO;
		BigDecimal bdCost = BigDecimal.ZERO;
		BigDecimal bdYTDDep = BigDecimal.ZERO;
		BigDecimal bdAccuDep = BigDecimal.ZERO;
		BigDecimal bdBookValue = BigDecimal.ZERO;
		BigDecimal bdCostOfNonDisposedAssets = BigDecimal.ZERO;
		BigDecimal bdAccumulatedDepreciationOfNonDisposedAssets = BigDecimal.ZERO;
		BigDecimal bdBookValueOfNonDisposedAssets = BigDecimal.ZERO;

		BigDecimal bdGrandYTDPurch = BigDecimal.ZERO;
		BigDecimal bdGrandYTDDisp = BigDecimal.ZERO;
		BigDecimal bdGrandCost = BigDecimal.ZERO;
		BigDecimal bdGrandYTDDep = BigDecimal.ZERO;
		BigDecimal bdGrandAccuDep = BigDecimal.ZERO;
		BigDecimal bdGrandBookValue = BigDecimal.ZERO;
		BigDecimal bdGrandCostOfNonDisposedAssets = BigDecimal.ZERO;
		BigDecimal bdGrandAccumulatedDepreciationOfNonDisposedAssets = BigDecimal.ZERO;
		BigDecimal bdGrandBookValueOfNonDisposedAssets = BigDecimal.ZERO;

		boolean bAllowAssetEditing = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.FAManageAssets, 
			sUserID, 
			conn,
			sLicenseModuleLevel);
		
		//out.println("<BR>SQL = " + SQL);
		
		//print table header
		 out.println("<TABLE WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\">");

		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			int iCount = 0;
			if (bShowDetail){
				Print_Column_Header(out);
			}

			while(rs.next()){
				if (sCurrentClass.length() == 0){
					sCurrentClass = rs.getString(SMTablefamaster.sClass);
					Print_Class_Header(sCurrentClass, 
							rs.getString(SMTablefaclasses.sClassDescription),
							out);
				}

				if (rs.getString(SMTablefamaster.sClass).compareTo(sCurrentClass) != 0){
					Print_Class_Totals(bdYTDPurch,
							bdYTDDisp,
							bdCost,
							bdYTDDep,
							bdAccuDep,
							bdBookValue,
							bdCostOfNonDisposedAssets,
							bdAccumulatedDepreciationOfNonDisposedAssets,
							bdBookValueOfNonDisposedAssets,
							sCurrentClass,
							out);
					iCount =0;
					bdYTDPurch = BigDecimal.ZERO;
					bdYTDDisp = BigDecimal.ZERO;
					bdCost = BigDecimal.ZERO;
					bdYTDDep = BigDecimal.ZERO;
					bdAccuDep = BigDecimal.ZERO;
					bdBookValue = BigDecimal.ZERO;
					bdCostOfNonDisposedAssets = BigDecimal.ZERO;
					bdAccumulatedDepreciationOfNonDisposedAssets = BigDecimal.ZERO;
					bdBookValueOfNonDisposedAssets = BigDecimal.ZERO;
					sCurrentClass = rs.getString(SMTablefamaster.sClass);

					if (bShowDetail){
						Print_Column_Header(out);
					}
					Print_Class_Header(sCurrentClass, 
							rs.getString(SMTablefaclasses.sClassDescription),
							out);
				}
				BigDecimal bdYTDDepreciation = new BigDecimal("0.00");
				if (rs.getBigDecimal("YTDDEPAMT") != null){
					bdYTDDepreciation = rs.getBigDecimal("YTDDEPAMT");
				}
				if (bShowDetail){
					Print_Asset_Info(
							rs.getString(SMTablefamaster.datAcquisitionDate),
							rs.getString(SMTablefamaster.sAssetNumber),
							rs.getString(SMTablefamaster.sDescription),
							rs.getString(SMTablefamaster.sDepreciationType),
							rs.getBigDecimal(SMTablefamaster.bdAcquisitionAmount),
							bdYTDDepreciation,
							rs.getBigDecimal(SMTablefamaster.bdAccumulatedDepreciation),
							rs.getBigDecimal(SMTablefamaster.bdCurrentValue),
							rs.getString("DISPOSED").compareToIgnoreCase("Y") == 0,
							rs.getString(SMTablefamaster.sSerialNumber),
							rs.getString(SMTablefamaster.sLocation),
							bAllowAssetEditing,
							context,
							sDBID,
							out,
							iCount);
				}

				bdCost = bdCost.add(rs.getBigDecimal(SMTablefamaster.bdAcquisitionAmount));
				bdAccuDep = bdAccuDep.add(rs.getBigDecimal(SMTablefamaster.bdAccumulatedDepreciation));
				bdBookValue = bdBookValue.add(rs.getBigDecimal(SMTablefamaster.bdCurrentValue));
				bdYTDPurch = bdYTDPurch.add(rs.getBigDecimal("YTDPURCHASED"));
				bdYTDDisp = bdYTDDisp.add(rs.getBigDecimal("YTDDISPOSED"));
				bdYTDDep = bdYTDDep.add(bdYTDDepreciation);
				if (rs.getString("DISPOSED").compareToIgnoreCase("N") == 0){
					bdCostOfNonDisposedAssets = bdCostOfNonDisposedAssets.add(rs.getBigDecimal(SMTablefamaster.bdAcquisitionAmount));
					bdAccumulatedDepreciationOfNonDisposedAssets = bdAccumulatedDepreciationOfNonDisposedAssets.add(rs.getBigDecimal(SMTablefamaster.bdAccumulatedDepreciation));
					bdBookValueOfNonDisposedAssets = bdBookValueOfNonDisposedAssets.add(rs.getBigDecimal(SMTablefamaster.bdCurrentValue));
				}
				bdGrandCost = bdGrandCost.add(rs.getBigDecimal(SMTablefamaster.bdAcquisitionAmount));
				bdGrandAccuDep = bdGrandAccuDep.add(rs.getBigDecimal(SMTablefamaster.bdAccumulatedDepreciation));
				bdGrandBookValue = bdGrandBookValue.add(rs.getBigDecimal(SMTablefamaster.bdCurrentValue));
				bdGrandYTDPurch = bdGrandYTDPurch.add(rs.getBigDecimal("YTDPURCHASED"));
				bdGrandYTDDisp = bdGrandYTDDisp.add(rs.getBigDecimal("YTDDISPOSED"));
				bdGrandYTDDep = bdGrandYTDDep.add(bdYTDDepreciation);
				if (rs.getString("DISPOSED").compareToIgnoreCase("N") == 0){
					bdGrandCostOfNonDisposedAssets = bdGrandCostOfNonDisposedAssets.add(rs.getBigDecimal(SMTablefamaster.bdAcquisitionAmount));
					bdGrandAccumulatedDepreciationOfNonDisposedAssets = bdGrandAccumulatedDepreciationOfNonDisposedAssets.add(rs.getBigDecimal(SMTablefamaster.bdAccumulatedDepreciation));
					bdGrandBookValueOfNonDisposedAssets = bdGrandBookValueOfNonDisposedAssets.add(rs.getBigDecimal(SMTablefamaster.bdCurrentValue));
				}
				iCount++;
			}
			rs.close();
		}catch (SQLException e){
			m_sErrorMessage = "Error reading resultset on SQL: " + SQL + " - " + e.getMessage();
			return false;
		}

		Print_Class_Totals(
			bdYTDPurch,
			bdYTDDisp,
			bdCost,
			bdYTDDep,
			bdAccuDep,
			bdBookValue,
			bdCostOfNonDisposedAssets,
			bdAccumulatedDepreciationOfNonDisposedAssets,
			bdBookValueOfNonDisposedAssets,
			sCurrentClass,
			out);

		Print_Grand_Totals(
			bdGrandYTDPurch,
			bdGrandYTDDisp,
			bdGrandCost,
			bdGrandYTDDep,
			bdGrandAccuDep,
			bdGrandBookValue,
			bdGrandCostOfNonDisposedAssets,
			bdGrandAccumulatedDepreciationOfNonDisposedAssets,
			bdGrandBookValueOfNonDisposedAssets,
			out);
		
		//Print the SQL statement itself at the bottom:
		//out.println("<BR> - SQL Statement:<BR><B>" + SQL + "</B>");
		
		return true;
	}

	private void Print_Asset_Info(
			String sAcquisitionDate,
			String sAssetNumber,
			String sDesc,
			String sDepType,
			BigDecimal bdCost,
			BigDecimal bdYTDDep,
			BigDecimal bdAccuDep,
			BigDecimal bdCurrentValue,
			boolean bAssetIsDisposed,
			String sSerialNumber,
			String sLocation,
			boolean bAllowAssetEditing,
			ServletContext context,
			String sDBID,
			PrintWriter out,
			int iCount){

		String sAssetLink = sAssetNumber;
		
		BigDecimal bdCostOfNonDisposedAsset = BigDecimal.ZERO;
		BigDecimal bdAccuDepOfNonDisposedAsset = BigDecimal.ZERO;
		BigDecimal bdCurrentValueofNonDisposedAsset = BigDecimal.ZERO;
		
		if (!bAssetIsDisposed){
			bdCostOfNonDisposedAsset = bdCost;
			bdAccuDepOfNonDisposedAsset = bdAccuDep;
			bdCurrentValueofNonDisposedAsset = bdCurrentValue;
		}
		
		if (bAllowAssetEditing){
			sAssetLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smfa.FAEditAssetsEdit?" +
					"AssetNumber=" + sAssetNumber +
					"&SubmitEdit=1" + 
					"&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sAssetNumber + "</A>"
			;
		}
		if(iCount % 2 == 0) {
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\" >");
		}else {
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\" >");
		}

		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + sLocation + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + clsDateAndTimeConversions.resultsetDateStringToString(sAcquisitionDate) + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + sAssetLink + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + sDesc + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + sSerialNumber + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + sDepType + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdCost.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdYTDDep.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdAccuDep.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdCurrentValue.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdCostOfNonDisposedAsset.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdAccuDepOfNonDisposedAsset.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdCurrentValueofNonDisposedAsset.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
	    out.println("</TR>");

	}

	private void Print_Column_Header(PrintWriter out){

		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\" >");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Location</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Date Acquired</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Asset#</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Description</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Serial Number</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Dep. Type</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Cost</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>YTD Dep.</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Accu. Dep.</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Book Value</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Cost Of Non-Disposed Assets</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Accu. Dep. Of Non-Disposed Assets</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Book Value. Of Non-Disposed Assets</B></TD>");
	    out.println("</TR>");
	}

	private void Print_Class_Totals(BigDecimal bdYTDPurch,
			BigDecimal bdYTDDisp,
			BigDecimal bdCost,
			BigDecimal bdYTDDep,
			BigDecimal bdAccuDep,
			BigDecimal bdBookValue,
			BigDecimal bdCostOfNonDisposedAssets,
			BigDecimal bdAccuDepOfNonDisposedAssets,
			BigDecimal bdBookValueOfNonDisposedAssets,
			String sClass,
			PrintWriter out){
		
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_SUB_HEADING + "\" >");
		out.println("<TD COLSPAN = \"13\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\" >&nbsp;&nbsp;&nbsp;&nbsp;</TD>");
		out.println("</TR>");
		
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_SUB_HEADING + "\" >");
		out.println("<TD COLSPAN = \"4\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >&nbsp;</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>YTD Purchases</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>YTD Disposed</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Cost</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>YTD Depreciation</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Accu. Depreciation</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Book Value</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Cost Of Non-Disposed Assets</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Accu. Depreciation Of Non-Disposed Assets</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Book Value Of Non-Disposed Assets</B></TD>");
	    out.println("</TR>");
	    
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_SUB_HEADING + "\" >");
		out.println("<TD COLSPAN = \"4\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >" + sClass + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdYTDPurch.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdYTDDisp.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdCost.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdYTDDep.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdAccuDep.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdBookValue.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdCostOfNonDisposedAssets.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdAccuDepOfNonDisposedAssets.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdBookValueOfNonDisposedAssets.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
	    out.println("</TR>");
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_SUB_HEADING + "\" >");
		out.println("<TD COLSPAN = \"13\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\" >&nbsp;&nbsp;&nbsp;&nbsp;</TD>");
		out.println("</TR>");

	}

	private void Print_Grand_Totals(BigDecimal bdYTDPurch,
			BigDecimal bdYTDDisp,
			BigDecimal bdCost,
			BigDecimal bdYTDDep,
			BigDecimal bdAccuDep,
			BigDecimal bdBookValue,
			BigDecimal bdCostOfNonDisposedAssets,
			BigDecimal bdAccuDepOfNonDisposedAssets,
			BigDecimal bdBookValueOfNonDisposedAssets,
			PrintWriter out){

		
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\" >");
		out.println("<TD COLSPAN = \"13\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\" >&nbsp;&nbsp;&nbsp;&nbsp;</TD>");
		out.println("</TR>");
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\" >");
		out.println("<TD COLSPAN = \"4\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B> GRAND TOTALS </B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdYTDPurch.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdYTDDisp.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdCost.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdYTDDep.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdAccuDep.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdBookValue.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdCostOfNonDisposedAssets.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdAccuDepOfNonDisposedAssets.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + bdBookValueOfNonDisposedAssets.setScale(2, BigDecimal.ROUND_HALF_UP) + "</TD>");
	    out.println("</TR>");
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\" >");
		out.println("<TD COLSPAN = \"13\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\" >&nbsp;&nbsp;&nbsp;&nbsp;</TD>");
		out.println("</TR>");

	}

	private void Print_Class_Header(String sClass, 
			String sClassDescription,
			PrintWriter out){
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\" >");
		out.println("<TD COLSPAN = \"13\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\" >&nbsp;&nbsp;&nbsp;&nbsp;</TD>");
		out.println("</TR>");
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\" >");
		out.println("<TD COLSPAN = \"13\"  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><B>Asset Class:&nbsp;&nbsp;&nbsp;&nbsp;" + sClass + " &nbsp;&nbsp;"  + sClassDescription + " </B></TD>");
		out.println("</TR>");
	}

	public String getErrorMessageString(){
		return m_sErrorMessage;
	}
}
