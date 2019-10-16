package smgl;

import javax.servlet.ServletContext;

import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableartransactions;
import SMDataDefinition.SMTablegltransactionbatchentries;
import SMDataDefinition.SMTableictransactions;
import smar.ARDocumentTypes;
import smcontrolpanel.SMUtilities;
import smfa.FATransactionListSelect;

public class GLTransactionLinks {

	public static String getSubledgerTransactionLink(
			String sSubledgerCode,
			String sSubledgerTransactionID,
			ServletContext context,
			String sDBID,
			String sLabel
		) throws Exception{
		
		String s;
		
		//If the transaction ID is blank, then just return without building the link:
		if (sSubledgerTransactionID.compareToIgnoreCase("") == 0){
			return sLabel;
		}
		try {
			s = "";
			String sSubledgerLinkClass = "";
			String sAdditionalParameters = "";
			int iSourceLedgerCode = -1;
			for(int i = 0; i < GLSourceLedgers.NO_OF_SOURCELEDGERS; i++){
				if (sSubledgerCode.compareToIgnoreCase(GLSourceLedgers.getSourceLedgerDescription(i)) == 0){
					iSourceLedgerCode = i;
					break;
				}
			}
			
			if (iSourceLedgerCode == -1){
				throw new Exception("Error [20192111536280] " + "Could not get source ledger code for ledger '" + sSubledgerCode + "'.");
			}
			
			switch(iSourceLedgerCode){
				case GLSourceLedgers.SOURCE_LEDGER_AP:
					sSubledgerLinkClass = "smap.APViewTransactionInformation";
					sAdditionalParameters = "&" + SMTableaptransactions.lid + "=" + sSubledgerTransactionID;
					break;
				case GLSourceLedgers.SOURCE_LEDGER_AR:
					sSubledgerLinkClass = "smar.ARActivityDisplay";
					//In case the required comma is missing, then just return without building the link:
					if (!sSubledgerTransactionID.contains(",")){
						return sLabel;
					}
					String sAcctDocConcatenation[] = sSubledgerTransactionID.split(",");
					sAdditionalParameters = 
						"&" + "CustomerNumber" + "=" + ServletUtilities.clsServletUtilities.URLEncode(sAcctDocConcatenation[0])
						+ "&" + "StartingDate=1/1/1990"
						+ "&" + "EndingDate=12/31/2099"
						+ "&" + "OrderBy=datdocdate"
						+ "&" + "OpenTransactionsOnly=false"
						+ "&" + SMTableartransactions.sdocnumber + "=" + ServletUtilities.clsServletUtilities.URLEncode(sAcctDocConcatenation[1]);
					// The ARActivityDisplay class needs any possible document types passed in as well:
					for (int i = 0; i < ARDocumentTypes.NUMBER_OF_AR_DOCUMENT_TYPES; i++){
						sAdditionalParameters += "&" + ARDocumentTypes.Get_Document_Type_Label(i) + "=Y";
					}
					break;
				case GLSourceLedgers.SOURCE_LEDGER_FA:
					sSubledgerLinkClass = "smfa.FATransactionListGenerate";
					//In case the required comma is missing, then just return without building the link:
					if (!sSubledgerTransactionID.contains(",")){
						return sLabel;
					}
					String sFiscalYearAndPeriod[] = sSubledgerTransactionID.split(",");
					sAdditionalParameters = 
						"&STARTFISCALYEAR=" + sFiscalYearAndPeriod[0]
						+ "&STARTFISCALPERIOD=" + sFiscalYearAndPeriod[1]
						+ "&ENDFISCALYEAR=" + sFiscalYearAndPeriod[0]
						+ "&ENDFISCALPERIOD=" + sFiscalYearAndPeriod[1]
						+ "&PRINTACTUALTRANSACTION=Y"
						+ "&SHOWDETAILS=Y"
						+ "&GROUPBY=GROUPBYGL"
						+ "&" + FATransactionListSelect.SHOWALLLOCATIONS_PARAMETER + "=Y"
						+ "&CallingClass=smfa.FATransactionListSelect";
					break;
				case GLSourceLedgers.SOURCE_LEDGER_IC:
					sSubledgerLinkClass = "smic.ICTransactionDetailsDisplay";
					//In case the required comma is missing, then just return without building the link:
					if (!sSubledgerTransactionID.contains(",")){
						return sLabel;
					}
					String sBatchAndEntry[] = sSubledgerTransactionID.split(",");
					sAdditionalParameters = "&" + SMTableictransactions.loriginalbatchnumber + "=" + sBatchAndEntry[0]
						+ "&" + SMTableictransactions.loriginalentrynumber + "=" + sBatchAndEntry[1]
						+ "&CallingClass=smic.ICTransactionHistory";
					break;
				case GLSourceLedgers.SOURCE_LEDGER_GL:
					sSubledgerLinkClass = "smgl.GLEditEntryEdit";
					String sGLBatchAndEntry[] = sSubledgerTransactionID.split(",");
					//In case the required comma is missing, then just return without building the link:
					if (!sSubledgerTransactionID.contains(",")){
						return sLabel;
					}
					sAdditionalParameters = "&" + SMTablegltransactionbatchentries.lbatchnumber + "=" + sGLBatchAndEntry[0]
						+ "&" + SMTablegltransactionbatchentries.lentrynumber + "=" + sGLBatchAndEntry[1]
						+ "&Editable=No"
						+ "&CallingClass=smgl.GLEditBatchesEdit";
					break;
				default:
					throw new Exception("Error [20192111519544] " + "Cannot build link for subledger " + Integer.toString(iSourceLedgerCode) + ".");
			}
			
			if (sSubledgerLinkClass.compareToIgnoreCase("") == 0){
				s += sLabel;
			}else{
				s += "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + sSubledgerLinkClass + "?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ sAdditionalParameters
					+ "\">" + sLabel + "</A>\n"
				;
			}
		} catch (Exception e) {
			throw new Exception("Error [2019268154271] " + e.getMessage());
		}

		return s;
	}
}
