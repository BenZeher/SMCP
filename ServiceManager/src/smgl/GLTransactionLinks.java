package smgl;

import javax.servlet.ServletContext;

import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableartransactions;
import SMDataDefinition.SMTableictransactions;
import smar.ARDocumentTypes;
import smcontrolpanel.SMUtilities;

public class GLTransactionLinks {

	public static String getSubledgerTransactionLink(
			String sSubledgerCode,
			String sSubledgerTransactionID,
			ServletContext context,
			String sDBID,
			String sLabel
		) throws Exception{
		
		String s = "";
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
			//throw new Exception("Error [20192111536280] " + "Could not get source ledger code for ledger '" + sSubledgerCode + "'.");
		}
		
		switch(iSourceLedgerCode){
			case GLSourceLedgers.SOURCE_LEDGER_AP:
				sSubledgerLinkClass = "smap.APViewTransactionInformation";
				sAdditionalParameters = "&" + SMTableaptransactions.lid + "=" + sSubledgerTransactionID;
				break;
			case GLSourceLedgers.SOURCE_LEDGER_AR:
				sSubledgerLinkClass = "smar.ARActivityDisplay";
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
				sSubledgerLinkClass = "";
				break;
			case GLSourceLedgers.SOURCE_LEDGER_IC:
				sSubledgerLinkClass = "smic.ICTransactionDetailsDisplay";
				String sBatchAndEntry[] = sSubledgerTransactionID.split(",");
				sAdditionalParameters = "&" + SMTableictransactions.loriginalbatchnumber + "=" + sBatchAndEntry[0]
					+ "&" + SMTableictransactions.loriginalentrynumber + "=" + sBatchAndEntry[1]
					+ "&CallingClass=smic.ICTransactionHistory";
				break;
			case GLSourceLedgers.SOURCE_LEDGER_GL:
				sSubledgerLinkClass = "";
				break;
			default:
				throw new Exception("Error [20192111519544] " + "Cannot build link for subledger " + Integer.toString(iSourceLedgerCode) + ".");
		}
		
		s += "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + sSubledgerLinkClass + "?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ sAdditionalParameters
			+ "\">" + sLabel + "</A>\n"
		;
		return s;
	}
}
