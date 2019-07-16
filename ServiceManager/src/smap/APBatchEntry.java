package smap;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMClasses.SMBatchStatuses;
import SMClasses.SMOption;
import SMDataDefinition.SMTableapaccountsets;
import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableapbatchentrylines;
import SMDataDefinition.SMTableapbatches;
import SMDataDefinition.SMTableapchecks;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableicpoinvoiceheaders;
import SMDataDefinition.SMTableicvendors;
import SMDataDefinition.SMTabletax;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;
import ServletUtilities.clsValidateFormFields;
import smbk.BKBank;
import smcontrolpanel.SMUtilities;
import smgl.GLAccount;

public class APBatchEntry {

	public static final String OBJECT_NAME = "AP Batch Entry";
	
	private String m_slid  = "lid";
	private String m_sbatchnumber;
	private String m_sentrynumber;
	private String m_sentrytype;
	private String m_sdocnumber;
	private String m_sentrydescription;
	private String m_sdatentrydate;
	private String m_slastline;
	private String m_sentryamount;
	private String m_svendoracct;
	private String m_scontrolacct;
	private String m_sdatdocdate;
	private String m_sdatdiscount;
	private String m_sdatduedate;
	private String m_sterms;
	private String m_sbddiscount;
	private String m_svendorname;
	private String m_staxjurisdiction;
	private String m_sitaxid;
	private String m_sbdtaxrate;
	private String m_staxtype;
	private String m_sicalculateonpurchaseorsale;
	private String m_schecknumber;
	private String m_sremittocode;
	private String m_sremittoname;
	private String m_sremittoaddressline1; 	 
	private String m_sremittoaddressline2;
	private String m_sremittoaddressline3;
	private String m_sremittoaddressline4;
	private String m_sremittocity;
	private String m_sremittostate;	 
	private String m_sremittopostalcode;
	private String m_sremittocountry;
	private String m_sionhold;
	private String m_slbankid;
	private String m_slsalesordernumber;
	private String m_slpurchaseordernumber;
	private String m_sapplytoinvoicenumber;
	private String m_siprintcheck;
	private String m_siprintingfinalized;
	private String m_siinvoiceincludestax;
	
	private ArrayList<APBatchEntryLine>m_arrBatchEntryLines;

	public static final int LINE_NUMBER_PADDING_LENGTH = 6;
	public static final String LINE_NUMBER_PARAMETER = "LINENOPARAM";

	public APBatchEntry() 
	{
		initializeVariables();
	}
	public APBatchEntry(HttpServletRequest req){
		//Read the batch fields from a servlet request:
		initializeVariables();
		
		//System.out.println("[1541185711] entry.getsdatentrydate() = '" + getsdatentrydate() + "'");
		
		setslid(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.lid, req).replace("&quot;", "\""));
		if(getslid().compareToIgnoreCase("") == 0){
			setslid("-1");
		}
		
		setsbatchnumber(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.lbatchnumber, req).replace("&quot;", "\""));
		if(getsbatchnumber().compareToIgnoreCase("") == 0){
			setsbatchnumber("-1");
		}
		setsentrynumber(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.lentrynumber, req).replace("&quot;", "\""));
		if(getsentrynumber().compareToIgnoreCase("") == 0){
			setsentrynumber("-1");
		}
		setsentrytype(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.ientrytype, req).replace("&quot;", "\""));
		setsdocnumber(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.sdocnumber, req).replace("&quot;", "\""));
		setsentrydescription(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.sentrydescription, req).replace("&quot;", "\""));
		
		setsdatentrydate(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.datentrydate, req).replace("&quot;", "\""));
		//System.out.println("[1541185712] entry.getsdatentrydate() = '" + getsdatentrydate() + "'");
		if (clsDateAndTimeConversions.IsValidDateString(SMUtilities.DATE_FORMAT_SIX_DIGIT_FOR_DISPLAY, getsdatentrydate())){
			try {
				System.out.println("[1541185713] entry.getsdatentrydate() = '" + getsdatentrydate() + "'");
				setsdatentrydate(clsDateAndTimeConversions.convertDateFormat(getsdatentrydate(), SMUtilities.DATE_FORMAT_SIX_DIGIT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE));
			} catch (Exception e) {
				//Don't have to do anything here, since we only have to deal with it IF the user entered a valid '6 digit' date (e.g. 060117)
			}
		}
		//System.out.println("[1541185714] entry.getsdatentrydate() = '" + getsdatentrydate() + "'");
		if(getsdatentrydate().compareToIgnoreCase("") == 0){
			//System.out.println("[1541185715] entry.getsdatentrydate() = '" + getsdatentrydate() + "'");
			//setsdatentrydate(clsDateAndTimeConversions.now(SMUtilities.DATE_FORMAT_FOR_DISPLAY));
			setsdatentrydate(SMUtilities.EMPTY_DATE_VALUE);
		}
		//System.out.println("[1541185716] entry.getsdatentrydate() = '" + getsdatentrydate() + "'");
		
		setslastline(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.llastline, req).replace("&quot;", "\""));
		if(getslastline().compareToIgnoreCase("") == 0){
			setslastline("0");
		}
		setsentryamount(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.bdentryamount, req).replace("&quot;", "\""));
		if(getsentryamount().compareToIgnoreCase("") == 0){
			setsentryamount("0.00");
		}
		//Convert credits and payments, etc. into negative numbers for saving, etc.:
		setsentryamount(convertEntryAmtsToSignedValueForStorage(getsentryamount().trim().replace(",", ""), getientrytype()));

		setsvendoracct(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.svendoracct, req).replace("&quot;", "\""));
		setscontrolacct(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.scontrolacct, req).replace("&quot;", "\""));
		
		setsdatdocdate(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.datdocdate, req).replace("&quot;", "\""));
		if (clsDateAndTimeConversions.IsValidDateString(SMUtilities.DATE_FORMAT_SIX_DIGIT_FOR_DISPLAY, getsdatdocdate())){
			try {
				setsdatdocdate(clsDateAndTimeConversions.convertDateFormat(getsdatdocdate(), SMUtilities.DATE_FORMAT_SIX_DIGIT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE));
			} catch (Exception e) {
				//Don't have to do anything here, since we only have to deal with it IF the user entered a valid '6 digit' date (e.g. 060117)
			}
		}
		if(getsdatdocdate().compareToIgnoreCase("") == 0){
			setsdatdocdate(clsDateAndTimeConversions.now(SMUtilities.DATE_FORMAT_FOR_DISPLAY));
		}
		
		setsdatdiscount(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.datdiscount, req).replace("&quot;", "\""));
		if (clsDateAndTimeConversions.IsValidDateString(SMUtilities.DATE_FORMAT_SIX_DIGIT_FOR_DISPLAY, getsdatdiscount())){
			try {
				setsdatdiscount(clsDateAndTimeConversions.convertDateFormat(getsdatdiscount(), SMUtilities.DATE_FORMAT_SIX_DIGIT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE));
			} catch (Exception e) {
				//Don't have to do anything here, since we only have to deal with it IF the user entered a valid '6 digit' date (e.g. 060117)
			}
		}
		if(getsdatdiscount().compareToIgnoreCase("") == 0){
			setsdatdiscount(clsDateAndTimeConversions.now(SMUtilities.DATE_FORMAT_FOR_DISPLAY));
		}
		
		setsdatduedate(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.datduedate, req).replace("&quot;", "\""));
		if (clsDateAndTimeConversions.IsValidDateString(SMUtilities.DATE_FORMAT_SIX_DIGIT_FOR_DISPLAY, getsdatduedate())){
			try {
				setsdatduedate(clsDateAndTimeConversions.convertDateFormat(getsdatduedate(), SMUtilities.DATE_FORMAT_SIX_DIGIT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE));
			} catch (Exception e) {
				//Don't have to do anything here, since we only have to deal with it IF the user entered a valid '6 digit' date (e.g. 060117)
			}
		}
		if(getsdatduedate().compareToIgnoreCase("") == 0){
			setsdatduedate(clsDateAndTimeConversions.now(SMUtilities.DATE_FORMAT_FOR_DISPLAY));
		}
		
		setsterms(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.sterms, req).replace("&quot;", "\""));
		
		setsdiscountamt(clsManageRequestParameters.get_Request_Parameter(
			SMTableapbatchentries.bddiscount, req).trim().replace("&quot;", "\""));
		if (getsdiscountamt().compareToIgnoreCase("") == 0){
			setsdiscountamt("0.00");
		}
		//Convert credits and payments, etc. into negative numbers for saving, etc.:
		setsdiscountamt(convertEntryAmtsToSignedValueForStorage(getsdiscountamt().trim().replace(",", ""), getientrytype()));
		
		setsvendorname(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.svendorname, req).replace("&quot;", "\""));
		setstaxjurisdiction(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.staxjurisdiction, req).replace("&quot;", "\""));
		setsitaxid(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.itaxid, req).replace("&quot;", "\""));
		
		setsbdtaxrate(clsManageRequestParameters.get_Request_Parameter(
			SMTableapbatchentries.bdtaxrate, req).trim().replace("&quot;", "\""));
		if (getsbdtaxrate().compareToIgnoreCase("") == 0){
			setsbdtaxrate("0.00");
		}

		setstaxtype(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.staxtype, req).replace("&quot;", "\""));
		setsicalculateonpurchaseorsale(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.icalculateonpurchaseorsale, req).replace("&quot;", "\""));
		
		//Read the additional payment entry fields:
		setschecknumber(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.schecknumber, req).replace("&quot;", "\""));
		setsremittocode(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.sremittocode, req).replace("&quot;", "\""));
		setsremittoname(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.sremittoname, req).replace("&quot;", "\""));
		setsremittoaddressline1(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.sremittoaddressline1, req).replace("&quot;", "\"")); 	 
		setsremittoaddressline2(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.sremittoaddressline2, req).replace("&quot;", "\""));
		setsremittoaddressline3(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.sremittoaddressline3, req).replace("&quot;", "\""));
		setsremittoaddressline4(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.sremittoaddressline4, req).replace("&quot;", "\""));
		setsremittocity(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.sremittocity, req).replace("&quot;", "\""));
		setsremittostate(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.sremittostate, req).replace("&quot;", "\""));	 
		setsremittopostalcode(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.sremittopostalcode, req).replace("&quot;", "\""));
		setsremittocountry(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.sremittocountry, req).replace("&quot;", "\""));
		
		if (clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.ionhold, req).compareToIgnoreCase("") != 0){
			setsionhold("1");
		}else{
			setsionhold("0");
		}
		
		setslbankid(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.lbankid, req).replace("&quot;", "\""));
		if (getslbankid().compareToIgnoreCase("") == 0){
			setslbankid("0");
		}
		
		setslsalesordernumber(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.lsalesordernumber, req).replace("&quot;", "\""));
		if (getslsalesordernumber().compareToIgnoreCase("") == 0){
			setslsalesordernumber("0");
		}

		setslpurchaseordernumber(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.lpurchaseordernumber, req).replace("&quot;", "\""));
		if (getslpurchaseordernumber().compareToIgnoreCase("") == 0){
			setslpurchaseordernumber("0");
		}
		setsapplytoinvoicenumber(clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.sapplytoinvoicenumber, req).replace("&quot;", "\""));
		
		if (clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.iprintcheck, req).compareToIgnoreCase("") != 0){
			setsiprintcheck("1");
		}else{
			setsiprintcheck("0");
		}

		if (clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.iprintingfinalized, req).compareToIgnoreCase("1") == 0){
			setsiprintingfinalized("1");
		}else{
			setsiprintingfinalized("0");
		}
		
		if (clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.iinvoiceincludestax, req).compareToIgnoreCase("1") == 0){
			setsiinvoiceincludestax("1");
		}else{
			if (clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.iinvoiceincludestax, req).compareToIgnoreCase("0") == 0){
				setsiinvoiceincludestax("0");
			}else{
				setsiinvoiceincludestax("-1");
			}
		}

		readEntryLines(req);
	}
	public APBatchEntry copyEntry(){
		APBatchEntry newentry = new APBatchEntry();
		newentry.setsapplytoinvoicenumber(getsapplytoinvoicenumber());
		newentry.setsbatchnumber(getsbatchnumber());
		newentry.setsbdtaxrate(getsbdtaxrate());
		newentry.setschecknumber(getschecknumber());
		newentry.setscontrolacct(getscontrolacct());
		newentry.setsdatdiscount(getsdatdiscount());
		newentry.setsdatdocdate(getsdatdocdate());
		newentry.setsdatduedate(getsdatduedate());
		newentry.setsdatentrydate(getsdatentrydate());
		newentry.setsdiscountamt(getsdiscountamt());
		newentry.setsdocnumber(getsdocnumber());
		newentry.setsentryamount(getsentryamount());
		newentry.setsentrydescription(getsentrydescription());
		newentry.setsentrynumber(getsentrynumber());
		newentry.setsentrytype(getsentrytype());
		newentry.setsiprintingfinalized(getsiprintingfinalized());
		newentry.setsicalculateonpurchaseorsale(getsicalculateonpurchaseorsale());
		newentry.setsiinvoiceincludestax(getsiinvoiceincludestax());
		newentry.setsionhold(getsionhold());
		newentry.setsiprintcheck(getsiprintcheck());
		newentry.setsitaxid(getsitaxid());
		newentry.setslastline(getslastline());
		newentry.setslbankid(getslbankid());
		newentry.setslid(getslid());
		newentry.setslpurchaseordernumber(getslpurchaseordernumber());
		newentry.setslsalesordernumber(getslsalesordernumber());
		newentry.setsremittoaddressline1(getsremittoaddressline1());
		newentry.setsremittoaddressline2(getsremittoaddressline2());
		newentry.setsremittoaddressline3(getsremittoaddressline3());
		newentry.setsremittoaddressline4(getsremittoaddressline4());
		newentry.setsremittocity(getsremittocity());
		newentry.setsremittocode(getsremittocode());
		newentry.setsremittocountry(getsremittocountry());
		newentry.setsremittoname(getsremittoname());
		newentry.setsremittopostalcode(getsremittopostalcode());
		newentry.setsremittostate(getsremittostate());
		newentry.setstaxjurisdiction(getstaxjurisdiction());
		newentry.setstaxtype(getstaxtype());
		newentry.setsterms(getsterms());
		newentry.setsvendoracct(getsvendoracct());
		newentry.setsvendorname(getsvendorname());
		
		for (int i = 0; i < this.m_arrBatchEntryLines.size(); i++){
			newentry.addLine(m_arrBatchEntryLines.get(i));
		}
		
		return newentry;
		
	}
	private void readEntryLines(HttpServletRequest request){
		//Read the entry lines:
    	Enumeration <String> eParams = request.getParameterNames();
    	String sLineParam = "";
    	String sLineNumber = "";
    	int iLineNumber = 0;
    	String sFieldName = "";
    	String sParamValue = "";
    	boolean bAddingNewLine = false;
    	APBatchEntryLine newline = new APBatchEntryLine();
    	while (eParams.hasMoreElements()){
    		sLineParam = eParams.nextElement();
    		//System.out.println("[1490711688] sLineParam = '" + sLineParam +"'");
    		//If it contains a line number parameter, then it's an apbatchentryline field:
    		if (sLineParam.startsWith(APBatchEntry.LINE_NUMBER_PARAMETER)){
    			//System.out.println("[1490711588] sLineParam = '" + sLineParam +"'");
    			sLineNumber = sLineParam.substring(APBatchEntry.LINE_NUMBER_PARAMETER.length(), APBatchEntry.LINE_NUMBER_PARAMETER.length() + APBatchEntry.LINE_NUMBER_PADDING_LENGTH);
    			iLineNumber = Integer.parseInt(sLineNumber);
    			//System.out.println("[1490711589] sLineNumber = '" + sLineNumber +"'");
    			sFieldName = sLineParam.substring(APBatchEntry.LINE_NUMBER_PARAMETER.length() + APBatchEntry.LINE_NUMBER_PADDING_LENGTH);
    			//System.out.println("[1490711590] sFieldName = '" + sFieldName +"'");
    			sParamValue = clsManageRequestParameters.get_Request_Parameter(sLineParam, request).trim();
    			//System.out.println("[1490711591] sParamValue = '" + sParamValue +"'");
    			//If the line array needs another row to fit all the line numbers, add it now:
				while (m_arrBatchEntryLines.size() < iLineNumber){
					APBatchEntryLine line = new APBatchEntryLine();
					m_arrBatchEntryLines.add(line);
				}
				
				//If any of the line fields have a '0' for their line number, then that means the user is adding a new field:
    			if (iLineNumber == 0){
    				bAddingNewLine = true;

    				//Now update the new line, and we'll add it to the entry down below:
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.bdamount) == 0){
        				newline.setsbdamount(sParamValue);
        				//Convert credits and payments, etc. into negative numbers for saving, etc.:
        				
        				newline.setsbdamount(convertEntryAmtsToSignedValueForStorage(newline.getsbdamount().trim().replace(",", ""), getientrytype()));
        				//System.out.println("[1511887695] - sParamValue = '" + sParamValue + "', newline.getsbdamount() = " + newline.getsbdamount());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.bdapplieddiscountamt) == 0){
        				newline.setsbddiscountappliedamt(sParamValue);
        				//Convert credits and payments, etc. into negative numbers for saving, etc.:
        				newline.setsbddiscountappliedamt(convertEntryAmtsToSignedValueForStorage(newline.getsbddiscountappliedamt().trim().replace(",", ""), getientrytype()));
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.bdpayableamount) == 0){
        				newline.setsbdpayableamt(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.iapplytodoctype) == 0){
        				newline.setsiapplytodoctype(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.lapplytodocid) == 0){
        				newline.setslapplytodocid(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.lid) == 0){
        				newline.setslid(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.llinenumber) == 0){
        				newline.setslinenumber(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.lpoheaderid) == 0){
        				newline.setslpoheaderid(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.lreceiptheaderid) == 0){
        				newline.setslreceiptheaderid(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.sapplytodocnumber) == 0){
        				newline.setsapplytodocnumber(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.scomment) == 0){
        				newline.setscomment(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.sdescription) == 0){
        				newline.setsdescription(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.sdistributionacct) == 0){
        				newline.setsdistributionacct(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.sdistributioncodename) == 0){
        				newline.setsdistributioncodename(sParamValue);
        			}
        			
    			}else{
        			//Now update the field on the line we're reading:
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.bdamount) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setsbdamount(sParamValue);
           				//Convert credits and payments, etc. into negative numbers for saving, etc.:
        				m_arrBatchEntryLines.get(iLineNumber - 1).setsbdamount(convertEntryAmtsToSignedValueForStorage(
        					m_arrBatchEntryLines.get(iLineNumber - 1).getsbdamount().trim().replace(",", ""), getientrytype()));
        				//System.out.println("[1511887696] - sParamValue = '" + sParamValue + "', m_arrBatchEntryLines.get(iLineNumber - 1).getsbdamount() = " + m_arrBatchEntryLines.get(iLineNumber - 1).getsbdamount());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.bdapplieddiscountamt) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setsbddiscountappliedamt(sParamValue);
           				//Convert credits and payments, etc. into negative numbers for saving, etc.:
        				m_arrBatchEntryLines.get(iLineNumber - 1).setsbddiscountappliedamt(convertEntryAmtsToSignedValueForStorage(
        					m_arrBatchEntryLines.get(iLineNumber - 1).getsbddiscountappliedamt().trim().replace(",", ""), getientrytype()));
        			}
           			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.bdpayableamount) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setsbdpayableamt(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.iapplytodoctype) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setsiapplytodoctype(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.lapplytodocid) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setslapplytodocid(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.lid) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setslid(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.llinenumber) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setslinenumber(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.lpoheaderid) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setslpoheaderid(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.lreceiptheaderid) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setslreceiptheaderid(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.scomment) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setscomment(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.sapplytodocnumber) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setsapplytodocnumber(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.sdescription) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setsdescription(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.sdistributionacct) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setsdistributionacct(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTableapbatchentrylines.sdistributioncodename) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setsdistributioncodename(sParamValue);
        			}
    			}
    		}
    	}

    	//If the user was adding a new line, then....
    	if (bAddingNewLine){
    		//Just add that line to the entry:
    		//If the user has actually added anything to the new line:
    		if (
    			(newline.getsbdamount().compareToIgnoreCase("0.00") != 0)
    			|| (newline.getscomment().compareToIgnoreCase("") != 0)
    			|| (newline.getsdescription().compareToIgnoreCase("") != 0)
    			|| (newline.getsdistributionacct().compareToIgnoreCase("") != 0)
    			|| (newline.getsdistributioncodename().compareToIgnoreCase("") != 0)
    				
    		){
    			addLine(newline);
    		}
    	}
    	//Make sure we set the batch number and entry number:
    	for (int i = 0; i < m_arrBatchEntryLines.size(); i++){
    		m_arrBatchEntryLines.get(i).setsbatchnumber(getsbatchnumber());
    		m_arrBatchEntryLines.get(i).setsentrynumber(getsentrynumber());
    	}
	}

	public void updateCheckNumber (Connection conn, String sCheckNumber) throws Exception{
		String SQL = "UPDATE " + SMTableapbatchentries.TableName
			+ " SET " + SMTableapbatchentries.schecknumber + " = '" + sCheckNumber + "'"
			+ " WHERE ("
				+ " (" + SMTableapbatchentries.lid + " = " + getslid() + ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1543352639] - could not update check number with SQL: '" + SQL + "' - " + e.getMessage() + ".");
		}
		setschecknumber(sCheckNumber);
		return;
	}
	
	public void save_without_data_transaction (Connection conn, String sUserID, boolean bBatchIsBeingPosted) throws Exception{

		//long lStarttime = System.currentTimeMillis();
		
		try {
			validate_fields(conn, sUserID, bBatchIsBeingPosted);
		} catch (Exception e1) {
			throw new Exception(e1.getMessage());
		}
		
		//System.out.println("[1543341850] - elapsed time 10 = " + (System.currentTimeMillis() - lStarttime) + " ms");
		
		String SQL = "";
		SQL = "INSERT into " + SMTableapbatchentries.TableName
			+ " (" 
			+ SMTableapbatchentries.bdentryamount
			+ ", " + SMTableapbatchentries.datentrydate
			+ ", " + SMTableapbatchentries.ientrytype
			+ ", " + SMTableapbatchentries.iinvoiceincludestax
			+ ", " + SMTableapbatchentries.ionhold
			+ ", " + SMTableapbatchentries.iprintcheck
			+ ", " + SMTableapbatchentries.iprintingfinalized
			+ ", " + SMTableapbatchentries.lbankid
			+ ", " + SMTableapbatchentries.lbatchnumber
			+ ", " + SMTableapbatchentries.lentrynumber
			+ ", " + SMTableapbatchentries.llastline
			+ ", " + SMTableapbatchentries.lpurchaseordernumber
			+ ", " + SMTableapbatchentries.lsalesordernumber
			+ ", " + SMTableapbatchentries.sapplytoinvoicenumber
			+ ", " + SMTableapbatchentries.schecknumber
			+ ", " + SMTableapbatchentries.scontrolacct
			+ ", " + SMTableapbatchentries.sdocnumber
			+ ", " + SMTableapbatchentries.sentrydescription
			+ ", " + SMTableapbatchentries.sremittoaddressline1
			+ ", " + SMTableapbatchentries.sremittoaddressline2
			+ ", " + SMTableapbatchentries.sremittoaddressline3
			+ ", " + SMTableapbatchentries.sremittoaddressline4
			+ ", " + SMTableapbatchentries.sremittocity
			+ ", " + SMTableapbatchentries.sremittocode
			+ ", " + SMTableapbatchentries.sremittocountry
			+ ", " + SMTableapbatchentries.sremittoname
			+ ", " + SMTableapbatchentries.sremittopostalcode
			+ ", " + SMTableapbatchentries.sremittostate
			+ ", " + SMTableapbatchentries.svendoracct
			+ ", " + SMTableapbatchentries.datdocdate
			+ ", " + SMTableapbatchentries.datdiscount
			+ ", " + SMTableapbatchentries.datduedate
			+ ", " + SMTableapbatchentries.sterms
			+ ", " + SMTableapbatchentries.bddiscount
			+ ", " + SMTableapbatchentries.svendorname
			+ ", " + SMTableapbatchentries.staxjurisdiction
			+ ", " + SMTableapbatchentries.itaxid
			+ ", " + SMTableapbatchentries.bdtaxrate
			+ ", " + SMTableapbatchentries.staxtype
			+ ", " + SMTableapbatchentries.icalculateonpurchaseorsale
			+ ")"
			+ " VALUES ("
			+ "" + getsentryamount().trim().replace(",", "")
			+ ", '" + getsentrydateInSQLFormat() + "'"
			+ ", " + getsentrytype()
			+ ", " + getsiinvoiceincludestax()
			+ ", " + getsionhold()
			+ ", " + getsiprintcheck()
			+ ", " + getsiprintingfinalized()
			+ ", " + getslbankid()
			+ ", " + getsbatchnumber()
			+ ", " + getsentrynumber()
			+ ", " + getslastline()
			+ ", " + getslpurchaseordernumber()
			+ ", " + getslsalesordernumber()
			+ ", '" + getsapplytoinvoicenumber() + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getschecknumber()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getscontrolacct()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsdocnumber()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsentrydescription()) + "'"
			
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsremittoaddressline1()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsremittoaddressline2()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsremittoaddressline3()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsremittoaddressline4()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsremittocity()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsremittocode()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsremittocountry()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsremittoname()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsremittopostalcode()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsremittostate()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsvendoracct()) + "'"
			+ ", '" + getsdatdocdateInSQLFormat() + "'"
			+ ", '" + getsdatdiscountInSQLFormat() + "'"
			+ ", '" + getsdatduedateInSQLFormat() + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsterms()) + "'"
			+ ", " + getsdiscountamt().trim().replace(",", "")
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsvendorname()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getstaxjurisdiction()) + "'"
			+ ", " + getsitaxid()
			+ ", " + getsbdtaxrate()
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getstaxtype()) + "'"
			+ ", " + getsicalculateonpurchaseorsale()
			
			+ ")"
			
			+ " ON DUPLICATE KEY UPDATE"
			+ " " + SMTableapbatchentries.bdentryamount + " = " + getsentryamount().trim().replace(",", "")
			+ ", " + SMTableapbatchentries.ientrytype + " = " + getsentrytype()
			+ ", " + SMTableapbatchentries.iinvoiceincludestax + " = " + getsiinvoiceincludestax()
			+ ", " + SMTableapbatchentries.ionhold + " = " + getsionhold()
			+ ", " + SMTableapbatchentries.iprintcheck + " = " + getsiprintcheck()
			+ ", " + SMTableapbatchentries.iprintingfinalized + " = " + getsiprintingfinalized()
			+ ", " + SMTableapbatchentries.datentrydate + " = '" + getsentrydateInSQLFormat() + "'"
			+ ", " + SMTableapbatchentries.lbankid + " = " + getslbankid()
			+ ", " + SMTableapbatchentries.lbatchnumber + " = " + getsbatchnumber()
			+ ", " + SMTableapbatchentries.lentrynumber + " = " + getsentrynumber()
			+ ", " + SMTableapbatchentries.llastline + " = " + getslastline()
			+ ", " + SMTableapbatchentries.lpurchaseordernumber + " = " + getslpurchaseordernumber()
			+ ", " + SMTableapbatchentries.lsalesordernumber + " = " + getslsalesordernumber()
			+ ", " + SMTableapbatchentries.sapplytoinvoicenumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsapplytoinvoicenumber()) + "'"
			+ ", " + SMTableapbatchentries.schecknumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(getschecknumber()) + "'"
			+ ", " + SMTableapbatchentries.scontrolacct + " = '" + clsDatabaseFunctions.FormatSQLStatement(getscontrolacct()) + "'"
			+ ", " + SMTableapbatchentries.sdocnumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsdocnumber()) + "'"
			+ ", " + SMTableapbatchentries.sentrydescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsentrydescription()) + "'"
			+ ", " + SMTableapbatchentries.sremittoaddressline1 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsremittoaddressline1()) + "'"
			+ ", " + SMTableapbatchentries.sremittoaddressline2 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsremittoaddressline2()) + "'"
			+ ", " + SMTableapbatchentries.sremittoaddressline3 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsremittoaddressline3()) + "'"
			+ ", " + SMTableapbatchentries.sremittoaddressline4 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsremittoaddressline4()) + "'"
			+ ", " + SMTableapbatchentries.sremittocity + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsremittocity()) + "'"
			+ ", " + SMTableapbatchentries.sremittocode + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsremittocode()) + "'"
			+ ", " + SMTableapbatchentries.sremittocountry + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsremittocountry()) + "'"
			+ ", " + SMTableapbatchentries.sremittoname + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsremittoname()) + "'"
			+ ", " + SMTableapbatchentries.sremittopostalcode + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsremittopostalcode()) + "'"
			+ ", " + SMTableapbatchentries.sremittostate + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsremittostate()) + "'"
			+ ", " + SMTableapbatchentries.svendoracct + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsvendoracct()) + "'"
			+ ", " + SMTableapbatchentries.datdocdate + " = '" + getsdatdocdateInSQLFormat() + "'"
			+ ", " + SMTableapbatchentries.datdiscount + " = '" + getsdatdiscountInSQLFormat() + "'"
			+ ", " + SMTableapbatchentries.datduedate + " = '" + getsdatduedateInSQLFormat() + "'"
			+ ", " + SMTableapbatchentries.sterms + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsterms()) + "'"
			+ ", " + SMTableapbatchentries.bddiscount + " = " + getsdiscountamt().trim().replace(",", "")
			+ ", " + SMTableapbatchentries.svendorname + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsvendorname()) + "'"
			+ ", " + SMTableapbatchentries.staxjurisdiction + " = '" + clsDatabaseFunctions.FormatSQLStatement(getstaxjurisdiction()) + "'"
			+ ", " + SMTableapbatchentries.itaxid + " = " + getsitaxid()
			+ ", " + SMTableapbatchentries.bdtaxrate + " = " + getsbdtaxrate().trim().replace(",", "")
			+ ", " + SMTableapbatchentries.staxtype + " = '" + clsDatabaseFunctions.FormatSQLStatement(getstaxtype()) + "'"
			+ ", " + SMTableapbatchentries.icalculateonpurchaseorsale + " = " + getsicalculateonpurchaseorsale()
		;
		
		//System.out.println("[1494260359] - SQL = '" + SQL + "'");
		
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1488916092] updating AP batch entry " + getsentrynumber() + " with SQL: '" + SQL + "' - " + e.getMessage());
		}

		
		//If the entry was newly created, get the new ID:

		/*
		if (
				(getslid().compareToIgnoreCase("-1") == 0)
				|| (getslid().compareToIgnoreCase("") == 0)
				|| (getslid().compareToIgnoreCase("0") == 0)
				
			){
			String sSQL = "SELECT last_insert_id() AS LAST_ID";
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
				if (rs.next()) {
					System.out.println("[1543586793] - rs.getLong('LAST_ID') = '" + rs.getLong("LAST_ID") + "'");
					setslid(Long.toString(rs.getLong("LAST_ID")));
				}else {
					setslid("0");
				}
				rs.close();
			} catch (SQLException e) {
				throw new Exception("Error [1488916462] Could not get last ID number - " + e.getMessage());
			}
			//If something went wrong, we can't get the last ID:
			if (getslid().compareToIgnoreCase("0") == 0){
				throw new Exception("Error [1488916463] Could not get last ID number.");
			}
		}
		*/
		
		if (
				(getslid().compareToIgnoreCase("-1") == 0)
				|| (getslid().compareToIgnoreCase("") == 0)
				|| (getslid().compareToIgnoreCase("0") == 0)
				
			){
			String sSQL = "SELECT "
				+ SMTableapbatchentries.lid
				+ " FROM " + SMTableapbatchentries.TableName
				+ " WHERE ("
					+ "(" + SMTableapbatchentries.lbatchnumber + " = " + getsbatchnumber() + ")"
					+ " AND (" + SMTableapbatchentries.lentrynumber + " = " + getsentrynumber() + ")"
				+ ")"
			;
					
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
				if (rs.next()) {
					//System.out.println("[1543586793] - rs.getLong('SMTableapbatchentries.lid') = '" + rs.getLong(SMTableapbatchentries.lid) + "'");
					setslid(Long.toString(rs.getLong(SMTableapbatchentries.lid)));
				}else {
					setslid("0");
				}
				rs.close();
			} catch (SQLException e) {
				throw new Exception("Error [1488916462] Could not get last ID number - " + e.getMessage());
			}
			//If something went wrong, we can't get the last ID:
			if (getslid().compareToIgnoreCase("0") == 0){
				throw new Exception("Error [1488916463] Could not get last ID number.");
			}
		}
		
		//System.out.println("[1543341852] - elapsed time 12 = " + (System.currentTimeMillis() - lStarttime) + " ms");
		
		//NOW - if it's a payment, prepay, apply-to, or misc payment, set the doc number using the appropriate prefix, and the ID no:
		if (
			(getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO)
			|| (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_MISCPAYMENT)
			|| (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT)
			|| (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT)
		){
			//And if the doc number is blank at this point (which could be if it's a new entry):
			if (getsdocnumber().compareTo("") == 0){
				String sDocNumber = SMTableapbatchentries.getPaymentDocNumberPrefix(getientrytype())
					+ clsStringFunctions.PadLeft(getslid(), "0", SMTableapbatchentries.PAYMENT_DOCNUMBER_LENGTH);
				SQL = "UPDATE " + SMTableapbatchentries.TableName
						+ " SET " + SMTableapbatchentries.sdocnumber + " = '" + sDocNumber + "'"
						+ " WHERE ("
							+ "(" + SMTableapbatchentries.lid + " = " + getslid() + ")"
						+ ")"
					;
			
				stmt = conn.createStatement();
				try {
					stmt.execute(SQL);
				} catch (Exception e) {
					throw new Exception("Error [1497880739] updating entry document number with SQL: '" 
						+ SQL + "' - " + e.getMessage());
				}
				setsdocnumber(sDocNumber);
			}
		}
		
		//System.out.println("[1543341853] - elapsed time 13 = " + (System.currentTimeMillis() - lStarttime) + " ms");
		
		//If it's a reversal, use the appropriate prefix:
		if (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_REVERSAL){
			//And if the doc number is blank at this point (which could be if it's a new entry):
			if (getsdocnumber().compareTo("") == 0){
				String sDocNumber = SMTableapbatchentries.getPaymentDocNumberPrefix(getientrytype())
					+ clsStringFunctions.PadLeft(getslid(), "0", SMTableapbatchentries.REVERSAL_DOCNUMBER_LENGTH);
				SQL = "UPDATE " + SMTableapbatchentries.TableName
						+ " SET " + SMTableapbatchentries.sdocnumber + " = '" + sDocNumber + "'"
						+ " WHERE ("
							+ "(" + SMTableapbatchentries.lid + " = " + getslid() + ")"
						+ ")"
					;
			
				stmt = conn.createStatement();
				try {
					stmt.execute(SQL);
				} catch (Exception e) {
					throw new Exception("Error [1497880739] updating entry document number with SQL: '" 
						+ SQL + "' - " + e.getMessage());
				}
				setsdocnumber(sDocNumber);
			}
		}
		
		//System.out.println("[1543341854] - elapsed time 14 = " + (System.currentTimeMillis() - lStarttime) + " ms");
		
		//Finally, save the lines....
		try {
			saveLines(conn, sUserID, bBatchIsBeingPosted);
		} catch (Exception e) {
			throw new Exception("Error [1488988522] saving entry lines - " + e.getMessage() + ".");
		}
		
		//System.out.println("[1543341855] - elapsed time 15 = " + (System.currentTimeMillis() - lStarttime) + " ms");
		
		return;
	}
	public void validate_fields(Connection conn, String sUserID, boolean bBatchIsBeingPosted) throws Exception{
		
		String sResult = "";
		int iEntryType = Integer.parseInt(getsentrytype());
		int iBatchType = APBatch.getBatchTypeFromEntryType(iEntryType);
		
		//First, validate the fields that are common to all types of entries:
		try {
			m_slid  = clsValidateFormFields.validateLongIntegerField(m_slid, "Entry ID", -1L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		try {
			m_sbatchnumber = clsValidateFormFields.validateLongIntegerField(
				m_sbatchnumber, 
				"Batch number", 
				-1L, 
				clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		try {
			m_sentrynumber = clsValidateFormFields.validateLongIntegerField(
					m_sentrynumber, 
				"Entry number", 
				1, 
				clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		//If this is a check reversal, and if we are just adding it, then update all the info from the check we are reversing:
		if (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_REVERSAL){
			update_check_reversal_data(conn, sUserID);
		}
		
		//We allow blank doc numbers for all the 'payment' types, because doc numbers for those are not set by the user - they are set automatically by the system when the record is first saved
		boolean bAllowBlankDocNumber = 
			(getientrytype() == 	SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO)
			|| (getientrytype() == 	SMTableapbatchentries.ENTRY_TYPE_PAYMENT_MISCPAYMENT)
			|| (getientrytype() == 	SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT)
			|| (getientrytype() == 	SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT)
			|| (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_REVERSAL)
		;
		try {
			m_sdocnumber = clsValidateFormFields.validateStringField(
				m_sdocnumber, 
				SMTableapbatchentries.sdocnumberLength, 
				"Document number", 
				bAllowBlankDocNumber
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		boolean bAllowBlankDescription = true;
		//Allow blank descriptions on ALL types of entries - TJR - 2/26/2018:
//		if (
//			(getientrytype() == 	SMTableapbatchentries.ENTRY_TYPE_INV_INVOICE)
//			|| (getientrytype() == 	SMTableapbatchentries.ENTRY_TYPE_INV_CREDITNOTE)
//			|| (getientrytype() == 	SMTableapbatchentries.ENTRY_TYPE_INV_DEBITNOTE)
//			|| (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_REVERSAL)
//		){
//			bAllowBlankDescription = false;
//		}
		
		try {
			m_sentrydescription = clsValidateFormFields.validateStringField(
					m_sentrydescription, 
				SMTableapbatchentries.sentrydescriptionLength, 
				"Entry description", 
				bAllowBlankDescription
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		@SuppressWarnings("unused")
		java.sql.Date datEntry = null;
		try {
			datEntry = clsDateAndTimeConversions.StringTojavaSQLDate(SMUtilities.DATE_FORMAT_FOR_DISPLAY, m_sdatentrydate);
		} catch (ParseException e) {
			try {
				m_sdatentrydate  = clsValidateFormFields.validateStandardDateField(m_sdatentrydate, "Entry date", false);
			} catch (Exception e1) {
				sResult += "  " + e.getMessage() + ".";
			}
		}

		// Make sure the entry date is always within the posting date range:
        SMOption opt = new SMOption();
        if (!opt.load(conn)){
        	sResult += "  " + "Error [1490628508] loading SM Options to check posting date range - " + opt.getErrorMessage() + ".";
        }else{
            try {
    			opt.checkDateForPosting(m_sdatentrydate, "Entry Date", conn, sUserID);
    		} catch (Exception e) {
    			sResult += "  " + "Error [1490628509]  - " + e.getMessage() + ".";
    		}
        }

		try {
			m_sdatdocdate  = clsValidateFormFields.validateStandardDateField(m_sdatdocdate, "Document date", false);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_slastline = clsValidateFormFields.validateLongIntegerField(
				Integer.toString(m_arrBatchEntryLines.size()), 
				"Last line", 
				0, 
				clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		m_sentryamount = m_sentryamount.replaceAll(",", "");
		try {
			m_sentryamount = clsValidateFormFields.validateBigdecimalField(
				m_sentryamount, 
				"Entry amount", 
				SMTableapbatchentries.bdentryamountScale,
				new BigDecimal("-9999999.99"),
				new BigDecimal("9999999.99")
				).replaceAll(",", "");
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		//Validate the vendor account:
		try {
			m_svendoracct = clsValidateFormFields.validateStringField(
				m_svendoracct, 
				SMTableapbatchentries.svendoracctLength, 
				"Vendor account", 
				false
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		//Check to see if the vendor has changed from the previously saved vendor:
		if (getslid().compareToIgnoreCase("-1") != 0){ //If this entry had already been saved
			String SQL = "SELECT"
				+ " " + SMTableapbatchentries.svendoracct
				+ " FROM " + SMTableapbatchentries.TableName
				+ " WHERE ("
					+ "(" + SMTableapbatchentries.lid + " = " + getslid() + ")"
				+ ")"
			;
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				if (rs.getString(SMTableapbatchentries.svendoracct).compareToIgnoreCase(getsvendoracct()) != 0){
					//The vendor has been changed, so we'll remove all the existing lines from the entry:
					m_arrBatchEntryLines.clear();
				}
			}
			rs.close();
		}
		
		//Here we get the correct control account for this vendor, and update it:
    	APVendor ven = new APVendor();
    	ven.setsvendoracct(getsvendoracct());
    	String sVendorAPControlAccount = "";
    	if (!ven.load(conn)){
    		sResult += "  " +  " Could not load vendor information for vendor : '" + getsvendoracct() 
    			+ "' - " + ven.getErrorMessages() + ".";
		}else{
			
			//Now get the GL acct info:
	    	APAccountSet apset = new APAccountSet();
	    	apset.setlid(ven.getiapaccountset());
	    	if (!apset.load(conn)){
	    		sResult += "  " +  "Could not load AP account set info for vendor : '" + getsvendoracct()
	    			+ "' - " + ven.getErrorMessages() + ".";
			}else{
				sVendorAPControlAccount = apset.getspayablescontrolacct();
			}
			
			if (iBatchType == SMTableapbatches.AP_BATCH_TYPE_INVOICE){
				setscontrolacct(sVendorAPControlAccount);
			}
			
			//By choosing the bank, the user sets the control account on payments, because the control account is the
			//CASH (bank) account from which the payments are made:
			if (
				(iEntryType == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_MISCPAYMENT)
				|| (iEntryType == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT)
				|| (iEntryType == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT)
			){
		    	BKBank bank = new BKBank();
		    	bank.setslid(getslbankid());
		    	if (!bank.load(conn)){
		    		sResult += "  " +  "Could not load Bank with Bank ID : '" + ven.getibankcode()
		    			+ "' - " + bank.getErrorMessages() + ".";
				}else{
					setscontrolacct(bank.getsglaccount());
				}
			}
		}
		
		try {
			m_scontrolacct = clsValidateFormFields.validateStringField(
				m_scontrolacct, 
				SMTableapbatchentries.scontrolacctLength, 
				"Control account", 
				false
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		//Now validate the control acct:
		GLAccount glacct = new GLAccount(m_scontrolacct);
		if(!glacct.load(conn)){
			sResult += "  GL Account '" + m_scontrolacct + "' could not be found.";
		}
		
		//Can't have this vendor and doc number in the system already, from some other batch and entry:
		String SQL = "SELECT *"
			+ " FROM " + SMTableaptransactions.TableName
			+ " WHERE ("
				+ "(" + SMTableaptransactions.svendor + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsvendoracct()) + "')"
				+ " AND (" + SMTableaptransactions.sdocnumber + "= '" + clsDatabaseFunctions.FormatSQLStatement(getsdocnumber()) + "')"
				+ " AND (" + SMTableaptransactions.loriginalbatchnumber + " != " + getsbatchnumber() + ")"
				+ " AND (" + SMTableaptransactions.loriginalentrynumber + " != " + getsentrynumber() + ")"
			+ ")"
		;
		try {
			//System.out.println("[1492099361] SQL = '" + SQL + "'.");
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				sResult += "  There is already a transaction (" + SMTableapbatchentries.getDocumentTypeLabel(rs.getInt(SMTableaptransactions.idoctype)) + ")"
					+ " with document date " + clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableaptransactions.datdocdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE)
					+ " with this document number ('" + getsdocnumber() + "') and vendor ('" + getsvendoracct() + "').";
				//System.out.println("[1492099855] - rs.getString(SMTableaptransactions.svendor) = '" + rs.getString(SMTableaptransactions.svendor) + ", rs.getString(SMTableaptransactions.sdocnumber) = '" + rs.getString(SMTableaptransactions.sdocnumber) + "'.");
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception("Error [1491341530] checking for duplicate vendor/doc combination with SQL '" + SQL + "' - " + e1.getMessage() + ".");
		}
		
		//Check here to make sure it's not in an unposted batch somewhere, either:
		SQL = "SELECT *"
				+ " FROM " + SMTableapbatchentries.TableName
				+ " LEFT JOIN " + SMTableapbatches.TableName
				+ " ON " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lbatchnumber
				+ " = " + SMTableapbatches.TableName + "." + SMTableapbatches.lbatchnumber
				+ " WHERE ("
					+ "(" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.svendoracct + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsvendoracct()) + "')"
					+ " AND (" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.sdocnumber + "= '" + clsDatabaseFunctions.FormatSQLStatement(getsdocnumber()) + "')"
					+ " AND (" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lbatchnumber + " != " + getsbatchnumber() + ")"
					+ " AND (" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lentrynumber + " != " + getsentrynumber() + ")"
					+ " AND (" 
						+ "(" + SMTableapbatches.TableName + "." + SMTableapbatches.ibatchstatus + " = " + SMBatchStatuses.ENTERED + ")"
						+ " OR (" + SMTableapbatches.TableName + "." + SMTableapbatches.ibatchstatus + " = " + SMBatchStatuses.IMPORTED + ")"
					+ ")"
				+ ")"
			;
			try {
				//System.out.println("[1492099361] SQL = '" + SQL + "'.");
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					sResult += "  Entry number " + Long.toString(rs.getLong(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lentrynumber))
						+ " in batch " + Long.toString(rs.getLong(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lbatchnumber))
						+ " is already using this document number ('" + getsdocnumber() + "') for this vendor ('" + getsvendoracct() + "')"
					;
				}
				rs.close();
			} catch (Exception e1) {
				throw new Exception("Error [1491341530] checking for duplicate vendor/doc combination in batches with SQL '" + SQL + "' - " + e1.getMessage() + ".");
			}
		
		//Tax calculation type:
		if (
			(getsicalculateonpurchaseorsale().compareToIgnoreCase(Integer.toString(SMTabletax.TAX_CALCULATION_BASED_ON_PURCHASE_COST)) != 0)
			&& (getsicalculateonpurchaseorsale().compareToIgnoreCase(Integer.toString(SMTabletax.TAX_CALCULATION_BASED_ON_SELLING_PRICE)) != 0)
		){
			setsicalculateonpurchaseorsale(Integer.toString(SMTabletax.TAX_CALCULATION_BASED_ON_SELLING_PRICE));
		}
		
		//If this is an invoice batch:
		if (iBatchType == SMTableapbatches.AP_BATCH_TYPE_INVOICE){
			try {
				sResult += validate_invoice_entry(conn, sUserID);
			} catch (Exception e1) {
				sResult += "  " + e1.getMessage() + ".";
			}
		}
		
		//If this is a payment batch:
		if (iBatchType == SMTableapbatches.AP_BATCH_TYPE_PAYMENT){
			try {
				sResult += validate_payment_entry(conn, sUserID, sVendorAPControlAccount);
			} catch (Exception e1) {
				sResult += "  " + e1.getMessage() + ".";
			}
		}

		//If this is a check reversal batch:
		if (iBatchType == SMTableapbatches.AP_BATCH_TYPE_REVERSALS){
			try {
				sResult += validate_checkreversal_entry(conn, sUserID);
			} catch (Exception e1) {
				sResult += "  " + e1.getMessage() + ".";
			}
		}
		
		//Purchase order number:
		setslpurchaseordernumber(getslpurchaseordernumber().trim());
		try {
			clsValidateFormFields.validateLongIntegerField(
				getslpurchaseordernumber(), 
				"Purchase order number", 
				-1L, 
				clsValidateFormFields.MAX_LONG_VALUE
			);
		} catch (Exception e1) {
			sResult += "  " + e1.getMessage();
		}
		
		//Sales order number:
		setslsalesordernumber(getslsalesordernumber().trim());
		try {
			clsValidateFormFields.validateLongIntegerField(
				getslsalesordernumber(), 
				"Sales order number", 
				-1L, 
				clsValidateFormFields.MAX_LONG_VALUE
			);
		} catch (Exception e1) {
			sResult += "  " + e1.getMessage();
		}
		
		setsapplytoinvoicenumber(getsapplytoinvoicenumber().trim());
		
		//Validate the lines:
		for (int i = 0; i < m_arrBatchEntryLines.size(); i++){
			APBatchEntryLine line = m_arrBatchEntryLines.get(i);
			line.setsentrynumber(getsentrynumber());
			line.setslinenumber(Integer.toString(i + 1));
			try {
				//Debugging:
				//if (getsvendoracct().compareToIgnoreCase("OHD02") == 0){
				//	if (line.getsapplytodocnumber().compareToIgnoreCase("4085665") == 0){
				//		System.out.println("[1509386339]");
				//	}
				//}
				line.validate_fields(conn, iEntryType, bBatchIsBeingPosted);
			} catch (Exception e) {
				sResult += "  In line " + line.getslinenumber() + " - " + e.getMessage() + ".";
			}
		}
		
		if (sResult.compareToIgnoreCase("") != 0){
			throw new Exception(sResult);
		}
		
		return;
	}
	public String validate_invoice_entry(Connection conn, String sUserID) throws Exception{
		boolean bIsCreditNote = getsentrytype().compareToIgnoreCase(Integer.toString(SMTableapbatchentries.ENTRY_TYPE_INV_CREDITNOTE)) == 0;
		boolean bIsDebitNote = getsentrytype().compareToIgnoreCase(Integer.toString(SMTableapbatchentries.ENTRY_TYPE_INV_DEBITNOTE)) == 0;
		boolean bIsInvoice = getsentrytype().compareToIgnoreCase(Integer.toString(SMTableapbatchentries.ENTRY_TYPE_INV_INVOICE)) == 0;
		String s = "";
		
		//In an invoice batch, these fields aren't used and should just be set to blanks:
		setschecknumber("");
		setsremittoaddressline1(""); 	 
		setsremittoaddressline2("");
		setsremittoaddressline3("");
		setsremittoaddressline4("");
		setsremittocity("");
		setsremittocode("");
		setsremittocountry("");
		setsremittoname("");
		setsremittopostalcode("");
		setsremittostate("");
		
		//If the entry is a credit or debit note, then we know we can set the due date and the discount date to empty dates:
		if ((bIsCreditNote) || (bIsDebitNote)){
			m_sdatduedate = SMUtilities.EMPTY_DATE_VALUE;
			m_sdatdiscount = SMUtilities.EMPTY_DATE_VALUE;
			m_sbddiscount = "0.00";
		}
		
		try {
			m_sdatduedate  = clsValidateFormFields.validateStandardDateField(m_sdatduedate, "Due date", (bIsCreditNote || bIsDebitNote));
		} catch (Exception e) {
			s += "  " + e.getMessage() + ".";
		}
		
		try {
			m_sdatdiscount  = clsValidateFormFields.validateStandardDateField(m_sdatdiscount, "Discount date", (true));
		} catch (Exception e) {
			s += "  " + e.getMessage() + ".";
		}
		
		//If the discount date is earlier than the invoice date, reject it:
		//Don't bother if there's no 'discount date':
		if (m_sdatdiscount.compareToIgnoreCase(SMUtilities.EMPTY_DATE_VALUE) != 0){
			String sSQLDiscountDate = clsDateAndTimeConversions.convertDateFormat(m_sdatdiscount, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATETIME_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE);
			String sSQLDocumentDate = clsDateAndTimeConversions.convertDateFormat(m_sdatdocdate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATETIME_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE);
			if (sSQLDiscountDate.compareToIgnoreCase(sSQLDocumentDate) < 0){
				s += "  The discount date (" + m_sdatdiscount + "') cannot be earlier than the document date (" + m_sdatdocdate + ")";
			}
		}
		
		//If the due date is earlier than the invoice date, reject it:
		String sSQLDueDate = clsDateAndTimeConversions.convertDateFormat(m_sdatduedate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATETIME_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE);
		String sSQLDocumentDate = clsDateAndTimeConversions.convertDateFormat(m_sdatdocdate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATETIME_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE);
		if (m_sdatduedate.compareToIgnoreCase(SMUtilities.EMPTY_DATE_VALUE) != 0){
			if (sSQLDueDate.compareToIgnoreCase(sSQLDocumentDate) < 0){
				s += "  The due date (" + m_sdatduedate + "') cannot be earlier than the document date (" + m_sdatdocdate + ")";
			}
		}
		
		if (
			(m_siinvoiceincludestax.compareToIgnoreCase("0") !=0)
			&& (m_siinvoiceincludestax.compareToIgnoreCase("1") !=0)
		){
			s += "  You must indicate whether this invoice includes tax or not.";
		}
		
		//Use the 'Tax ID' to get all the other fields:
		if (getsitaxid().compareToIgnoreCase("") == 0){
			s += "  Tax cannot be blank.";
		}else{
			String SQL = "SELECT"
				+ " * FROM " + SMTabletax.TableName
				+ " WHERE ("
					+ "(" + SMTabletax.lid + " = " + getsitaxid() + ")"
				+ ")"
			;
			try {
				ResultSet rsTax = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rsTax.next()){
					m_staxtype = rsTax.getString(SMTabletax.staxtype);
					m_staxjurisdiction = rsTax.getString(SMTabletax.staxjurisdiction);
					m_sicalculateonpurchaseorsale = Long.toString(rsTax.getLong(SMTabletax.icalculateonpurchaseorsale));
					m_sbdtaxrate = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableicpoinvoiceheaders.bdtaxrateScale, rsTax.getBigDecimal(SMTabletax.bdtaxrate)).replace(",", "");
				}else{
					throw new Exception("No tax record found for ID '" + getsitaxid() + "'");
				}
				rsTax.close();
			} catch (Exception e) {
				throw new Exception("Error [1490629400] - Unable to read tax table with SQL '" + SQL + "' - " + e.getMessage());
			}
		}
		
		BigDecimal bdEntryAmount = new BigDecimal(getsentryamount().replaceAll(",", ""));
		//Credit notes must be negative:
		if (bIsCreditNote){
			if (bdEntryAmount.compareTo(BigDecimal.ZERO) > 0){
				s += "  Credit note amounts MUST be enntered as POSITIVE numbers.";
			}
		}
		if (bIsDebitNote || bIsInvoice){
			if (bdEntryAmount.compareTo(BigDecimal.ZERO) < 0){
				s += "  Invoice and debit note amounts MUST be entered as POSITIVE numbers.";
			}
		}
		
		//Add the vendor's initial line, if it's an invoice:
		try {
			addDefaultInvoiceLine(conn, sUserID);
		} catch (Exception e1) {
			throw new Exception("Error [1490910530] adding initial default line for this vendor - " + e1.getMessage());
		}
		
		return s;
	}
	public String validate_payment_entry(Connection conn, String sUserID, String sAPControlAcct) throws Exception{

		//In a payment batch, these are meaningless:
		m_sdatduedate = SMUtilities.EMPTY_DATE_VALUE;
		m_sdatdiscount = SMUtilities.EMPTY_DATE_VALUE;
		m_sbddiscount = "0.00";
		m_sionhold = "0";
		setsitaxid("0");
		String s = "";
		
		try {
			m_schecknumber = clsValidateFormFields.validateStringField(
				m_schecknumber, 
				SMTableapbatchentries.schecknumberLength, 
				"Check number", 
				true
			);
		} catch (Exception e) {
			s += "  " + e.getMessage() + ".";
		}
		
		//If the user has chosen NOT to print a check, then he must enter a document number on the payment:
		if(
			(getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT)
			|| (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_MISCPAYMENT)
			|| (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT)
		){
			if (getsiprintcheck().compareToIgnoreCase("0") == 0){
				if (getschecknumber().compareToIgnoreCase("") == 0){
					s += "  If you choose NOT to have a check printed, then you MUST enter a 'check number' manually.";
				}
			}else{
				//IF the user has chosen to have a check printed AND if the check has not been finalized for this entry, 
				// then we need to clear the check number:
				if (this.getsiprintingfinalized().compareToIgnoreCase("1") != 0){
					setschecknumber("");
				}
			}
			
			//Validate the bank:
			BKBank bank = new BKBank();
			bank.setslid(this.getslbankid());
			try {
				if(!bank.load(conn)){
					s += "  Could not load bank with ID '" + getslbankid() + "' - " + bank.getErrorMessages() + ".";
				}
			} catch (Exception e) {
				s += "  Could not load bank with ID '" + getslbankid() + "' - " + e.getMessage() + ".";
			}
			
			//Now validate that the bank on this entry matches the bank on ALL the other entries in this batch:
			String SQL = "SELECT"
				+ " " + SMTableapbatchentries.lentrynumber
				+ " FROM " + SMTableapbatchentries.TableName
				+ " WHERE ("
					+ "(" + SMTableapbatchentries.lbatchnumber + " = " + getsbatchnumber() + ")"
					+ " AND (" + SMTableapbatchentries.lbankid + " != " + getslbankid() + ")"
					// Apply-tos don't get a 'bank', so don't consider any or those:
					+ " AND (" + SMTableapbatchentries.ientrytype + " != " + SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO + ")"
				+ ")"
			;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					s += "  The bank on this entry MUST BE the same as the bank on all the other batch entries.";
				}
				rs.close();
			} catch (Exception e) {
				throw new Exception("Error [1506716694] - could not check banks on other batch entries with SQL '" + SQL + "' - " + e.getMessage());
			}
		}
		
		try {
			m_sremittoaddressline1 = clsValidateFormFields.validateStringField(
				m_sremittoaddressline1, 
				SMTableapbatchentries.sremittoaddressline1Length, 
				"Remit-to address line 1", 
				true
			);
		} catch (Exception e) {
			s += "  " + e.getMessage() + ".";
		}
		try {
			m_sremittoaddressline2 = clsValidateFormFields.validateStringField(
				m_sremittoaddressline2, 
				SMTableapbatchentries.sremittoaddressline2Length, 
				"Remit-to address line 2", 
				true
			);
		} catch (Exception e) {
			s += "  " + e.getMessage() + ".";
		}
		try {
			m_sremittoaddressline3 = clsValidateFormFields.validateStringField(
				m_sremittoaddressline3, 
				SMTableapbatchentries.sremittoaddressline3Length, 
				"Remit-to address line 3", 
				true
			);
		} catch (Exception e) {
			s += "  " + e.getMessage() + ".";
		}
		try {
			m_sremittoaddressline4 = clsValidateFormFields.validateStringField(
				m_sremittoaddressline4, 
				SMTableapbatchentries.sremittoaddressline4Length, 
				"Remit-to address line 4", 
				true
			);
		} catch (Exception e) {
			s += "  " + e.getMessage() + ".";
		}
		try {
			m_sremittocity = clsValidateFormFields.validateStringField(
					m_sremittocity, 
				SMTableapbatchentries.sremittocityLength, 
				"Remit-to city", 
				true
			);
		} catch (Exception e) {
			s += "  " + e.getMessage() + ".";
		}
		try {
			m_sremittocode = clsValidateFormFields.validateStringField(
				m_sremittocode, 
				SMTableapbatchentries.sremittocodeLength, 
				"Remit-to code", 
				true
			);
		} catch (Exception e) {
			s += "  " + e.getMessage() + ".";
		}
		try {
			m_sremittocountry = clsValidateFormFields.validateStringField(
					m_sremittocountry, 
				SMTableapbatchentries.sremittocountryLength, 
				"Remit-to country", 
				true
			);
		} catch (Exception e) {
			s += "  " + e.getMessage() + ".";
		}
		try {
			m_sremittoname = clsValidateFormFields.validateStringField(
				m_sremittoname, 
				SMTableapbatchentries.sremittonameLength, 
				"Remit-to name", 
				true
			);
		} catch (Exception e) {
			s += "  " + e.getMessage() + ".";
		}
		try {
			m_sremittopostalcode = clsValidateFormFields.validateStringField(
					m_sremittopostalcode, 
				SMTableapbatchentries.sremittopostalcodeLength, 
				"Remit-to postal code", 
				true
			);
		} catch (Exception e) {
			s += "  " + e.getMessage() + ".";
		}
		try {
			m_sremittostate = clsValidateFormFields.validateStringField(
					m_sremittostate, 
				SMTableapbatchentries.sremittostateLength, 
				"Remit-to state", 
				true
			);
		} catch (Exception e) {
			s += "  " + e.getMessage() + ".";
		}
		
		//Here we are going to read the vendor and apply-to docnumber so we can populate the other line fields to match:
		
		//Also, for any payment type, we are going to just set the entry amount equal to the total of the lines:
		BigDecimal bdLineTotal = new BigDecimal("0.00");
		String sApplyToDoc = "";
		String sApplyToDocType = "0";
		for (int i = 0; i < getLineArray().size(); i++){
			APBatchEntryLine line = getLineArray().get(i);
			bdLineTotal = bdLineTotal.add(new BigDecimal(line.getsbdamount().replace(",", "")));
			
			//Validate the 'apply-to document number':
			if (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT){
				String SQL = "SELECT * FROM " + SMTableaptransactions.TableName
						+ " WHERE ("
						+ "(" + SMTableaptransactions.svendor + " = '" + getsvendoracct() + "')"
						+ " AND (" + SMTableaptransactions.sdocnumber + " = '" + line.getsapplytodocnumber() + "')"
						+ ")"
						;
				
				
				try {
					ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
					if (rs.next()){
						//If it's a payment, set the distribution acct to the control acct of the apply-to transaction:
						line.setsdistributionacct(rs.getString(SMTableaptransactions.scontrolacct));
						line.setslapplytodocid(Long.toString(rs.getLong(SMTableaptransactions.lid)));
						line.setsdescription("Applying to document '" + line.getsapplytodocnumber() + "'");
						line.setscomment("(Default comment)");
					}else{
						s += "  Apply-to document '" + line.getsapplytodocnumber() + "' on line " + Integer.toString(i + 1) + " was not found.";
					}
					rs.close();
				} catch (Exception e) {
					s += "  Error [1492439991] reading AP Transactions table with SQL '" + SQL + "' - " + e.getMessage() + ".";
				}
			}
			
			if (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO){
				String SQL = "SELECT * FROM " + SMTableaptransactions.TableName
						
					+ " LEFT JOIN " + SMTableicvendors.TableName
					+ " ON " + SMTableicvendors.TableName + "." +  SMTableicvendors.svendoracct + " = " + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor
					
					+ " LEFT JOIN " + SMTableapaccountsets.TableName
					+ " ON " + SMTableicvendors.TableName + "." +  SMTableicvendors.iapaccountset + " = " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.lid
					
					+ " WHERE ("
					+ "(" + SMTableaptransactions.svendor + " = '" + getsvendoracct() + "')"
					+ " AND (" + SMTableaptransactions.sdocnumber + " = '" + line.getsapplytodocnumber() + "')"
					+ ")"
					;
				
				
				try {
					ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
					if (rs.next()){
						//If the applying to transaction is a PRE-PAY, then we apply our distribution line to the pre-pay liability account.
						if (rs.getInt(SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype) == SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PREPAYMENT){
							line.setsdistributionacct(rs.getString(SMTableapaccountsets.TableName + "." + SMTableapaccountsets.sprepaymentacct));
						}else{
						// IF the transaction was NOT a pre-pay, then we just apply it to the transaction's CONTROL account, which is normally just the AP control acct
						// for that vendor:
							line.setsdistributionacct(rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.scontrolacct));
						}						
						
						line.setslapplytodocid(Long.toString(rs.getLong(SMTableaptransactions.lid)));
						line.setsdescription("Applying to document '" + line.getsapplytodocnumber() + "'");
						line.setscomment("(Default comment)");
					}else{
						s += "  Apply-to document '" + line.getsapplytodocnumber() + "' on line " + Integer.toString(i + 1) + " was not found.";
					}
					rs.close();
				} catch (Exception e) {
					s += "  Error [14924399891] reading AP Transactions table with SQL '" + SQL + "' - " + e.getMessage() + ".";
				}
			}
			
			//If it's a prepay, we have to verify that all the lines point to the same apply-to document:
			if (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT){
				if (i == 0){
					if (line.getsiapplytodoctype().compareToIgnoreCase(Integer.toString(SMTableapbatchentrylines.APPLY_TO_DOC_TYPE_INVOICENUMBER)) == 0){
						setsapplytoinvoicenumber(line.getsapplytodocnumber());
					}
					if (line.getsiapplytodoctype().compareToIgnoreCase(Integer.toString(SMTableapbatchentrylines.APPLY_TO_DOC_TYPE_ORDERNUMBER)) == 0){
						setslsalesordernumber(line.getsapplytodocnumber());
					}
					if (line.getsiapplytodoctype().compareToIgnoreCase(Integer.toString(SMTableapbatchentrylines.APPLY_TO_DOC_TYPE_PONUMBER)) == 0){
						setslpurchaseordernumber(line.getsapplytodocnumber());
					}
					
					sApplyToDoc = line.getsapplytodocnumber();
					sApplyToDocType = line.getsiapplytodoctype();
				}else{
					if (
						(sApplyToDoc.compareTo(line.getsapplytodocnumber()) != 0)
						|| (sApplyToDocType.compareTo(line.getsiapplytodoctype()) != 0)
					){
						s += "  All the lines on a pre-pay must apply to the same document.";
					}
				}
			}
		}
		
		//Set the entry amount to the total of the applied amounts:
		setsentryamount(clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapbatchentries.bdentryamountScale, bdLineTotal));
		
		if ((getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT)
				|| (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_MISCPAYMENT)
				|| (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT)){
			if (bdLineTotal.compareTo(BigDecimal.ZERO) > 0){
				s += "  Payment amounts MUST be negative.";
			}
		}
		
		//If it's a Pre-pay, we'll need to get the vendor's AP pre-pay liability account, to use it as the distribution account on the lines:
		if (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT){
			String sPrePayLiabilityAccount = "";
			String SQL = "SELECT"
				+ " " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.sprepaymentacct
				+ " FROM " + SMTableicvendors.TableName + " LEFT JOIN " + SMTableapaccountsets.TableName
				+ " ON " + SMTableicvendors.TableName + "." + SMTableicvendors.iapaccountset + " = "
				+ SMTableapaccountsets.TableName + "." + SMTableapaccountsets.lid
				+ " WHERE ("
					+ "(" + SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct + " = '" + getsvendoracct() + "')"
				+ ")"
			;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					sPrePayLiabilityAccount = rs.getString(SMTableapaccountsets.TableName + "." + SMTableapaccountsets.sprepaymentacct);
				}
				rs.close();
			} catch (Exception e) {
				s += "  Error [1495134614] getting AP Control Account for vendor '" + getsvendoracct() + "' - " + e.getMessage() + ".";
			}
			for (int i = 0; i < m_arrBatchEntryLines.size(); i++){
				m_arrBatchEntryLines.get(i).setsdistributionacct(sPrePayLiabilityAccount);
			}
		}
		
		return s;
	}

	public String validate_checkreversal_entry(Connection conn, String sUserID) throws Exception{

		//In a reversal batch, these are meaningless:
		m_sdatduedate = SMUtilities.EMPTY_DATE_VALUE;
		m_sdatdiscount = SMUtilities.EMPTY_DATE_VALUE;
		m_sbddiscount = "0.00";
		m_sionhold = "0";
		m_siprintcheck = "0";
		
		setsitaxid("0");
		String s = "";
		try {
			m_schecknumber = clsValidateFormFields.validateStringField(
				m_schecknumber, 
				SMTableapbatchentries.schecknumberLength, 
				"Check number", 
				false
			);
		} catch (Exception e) {
			s += "  " + e.getMessage() + ".";
		}
		
		//Validate the bank:
		BKBank bank = new BKBank();
		bank.setslid(this.getslbankid());
		try {
			if(!bank.load(conn)){
				s += "  Could not load bank with ID '" + getslbankid() + "' - " + bank.getErrorMessages() + ".";
			}
		} catch (Exception e) {
			s += "  Could not load bank with ID '" + getslbankid() + "' - " + e.getMessage() + ".";
		}
		
		try {
			m_sremittoaddressline1 = clsValidateFormFields.validateStringField(
				m_sremittoaddressline1, 
				SMTableapbatchentries.sremittoaddressline1Length, 
				"Remit-to address line 1", 
				true
			);
		} catch (Exception e) {
			s += "  " + e.getMessage() + ".";
		}
		try {
			m_sremittoaddressline2 = clsValidateFormFields.validateStringField(
				m_sremittoaddressline2, 
				SMTableapbatchentries.sremittoaddressline2Length, 
				"Remit-to address line 2", 
				true
			);
		} catch (Exception e) {
			s += "  " + e.getMessage() + ".";
		}
		try {
			m_sremittoaddressline3 = clsValidateFormFields.validateStringField(
				m_sremittoaddressline3, 
				SMTableapbatchentries.sremittoaddressline3Length, 
				"Remit-to address line 3", 
				true
			);
		} catch (Exception e) {
			s += "  " + e.getMessage() + ".";
		}
		try {
			m_sremittoaddressline4 = clsValidateFormFields.validateStringField(
				m_sremittoaddressline4, 
				SMTableapbatchentries.sremittoaddressline4Length, 
				"Remit-to address line 4", 
				true
			);
		} catch (Exception e) {
			s += "  " + e.getMessage() + ".";
		}
		try {
			m_sremittocity = clsValidateFormFields.validateStringField(
					m_sremittocity, 
				SMTableapbatchentries.sremittocityLength, 
				"Remit-to city", 
				true
			);
		} catch (Exception e) {
			s += "  " + e.getMessage() + ".";
		}
		try {
			m_sremittocode = clsValidateFormFields.validateStringField(
				m_sremittocode, 
				SMTableapbatchentries.sremittocodeLength, 
				"Remit-to code", 
				true
			);
		} catch (Exception e) {
			s += "  " + e.getMessage() + ".";
		}
		try {
			m_sremittocountry = clsValidateFormFields.validateStringField(
					m_sremittocountry, 
				SMTableapbatchentries.sremittocountryLength, 
				"Remit-to country", 
				true
			);
		} catch (Exception e) {
			s += "  " + e.getMessage() + ".";
		}
		try {
			m_sremittoname = clsValidateFormFields.validateStringField(
				m_sremittoname, 
				SMTableapbatchentries.sremittonameLength, 
				"Remit-to name", 
				true
			);
		} catch (Exception e) {
			s += "  " + e.getMessage() + ".";
		}
		try {
			m_sremittopostalcode = clsValidateFormFields.validateStringField(
					m_sremittopostalcode, 
				SMTableapbatchentries.sremittopostalcodeLength, 
				"Remit-to postal code", 
				true
			);
		} catch (Exception e) {
			s += "  " + e.getMessage() + ".";
		}
		try {
			m_sremittostate = clsValidateFormFields.validateStringField(
					m_sremittostate, 
				SMTableapbatchentries.sremittostateLength, 
				"Remit-to state", 
				true
			);
		} catch (Exception e) {
			s += "  " + e.getMessage() + ".";
		}
		
		//Make sure that this check has not already been voided:
		//System.out.println("[1513618408] - got here");
		APCheck check = new APCheck();
		boolean bPrintedCheckWasFound = false;
		try {
			check.loadUsingCheckNumber(conn, sUserID, getsvendoracct(), getschecknumber());
			bPrintedCheckWasFound = true;
		} catch (Exception e) {
			//Dont catch this - it may fail just because there was no PRINTED check, but we'll want
			//it to go on in that case so we can still reverse the payment...
			//s += " " + e.getMessage() + ".";
			bPrintedCheckWasFound = false;
		}
		//System.out.println("[1513618409] - check.getsivoid() = '" + check.getsivoid() + ".");
		if (bPrintedCheckWasFound){
			if (check.getsivoid().compareToIgnoreCase("1") == 0){
				s += " " + "Check number " + getschecknumber() + " for vendor " + getsvendoracct() + " has already been reversed.";
			}
		}
		return s;
	}
	public void update_check_reversal_data(String sDBID, ServletContext context, String sUser) throws Exception {
		
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + ".update_check_reversal_data - user: " + sUser));
		} catch (Exception e) {
			throw new Exception("Error [1511879071] getting database connection - " + e.getMessage());
		}
		
		try {
			update_check_reversal_data(conn, sUser);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1546998960]");
			throw new Exception(e.getMessage());
		}
		
		clsDatabaseFunctions.freeConnection(context, conn, "[1546998961]");
		
		return;
		
	}
	private void update_check_reversal_data(Connection conn, String sUserID) throws Exception{
		
		//Get the batch the original check was in:
		String sOriginalBatchNumber = "";
		String sOriginalEntryNumber = "";
		String SQL = "SELECT"
			+ " " + SMTableapbatchentries.lbatchnumber
			+ ", " + SMTableapbatchentries.lentrynumber
			+ " FROM " + SMTableapbatchentries.TableName
			+ " WHERE ("
				+ "(" + SMTableapbatchentries.schecknumber + " = '" + getschecknumber() + "')"
				+ " AND (" + SMTableapbatchentries.svendoracct + " = '" + getsvendoracct() + "')"
				+ " AND (" + SMTableapbatchentries.ientrytype + " != " + Integer.toString(SMTableapbatchentries.ENTRY_TYPE_REVERSAL) + ")"
			+ ")"
		;
		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		if (rs.next()){
			sOriginalBatchNumber = Long.toString(rs.getLong(SMTableapbatchentries.lbatchnumber));
			sOriginalEntryNumber = Long.toString(rs.getLong(SMTableapbatchentries.lentrynumber));
			rs.close();
		}else{
			rs.close();
			throw new Exception("Error [1511811857] - could not find check number '" + getschecknumber() + "' for vendor '" + getsvendoracct() + "' in any previous payment batches.");
		}
		
		APBatch originalbatch = new APBatch(sOriginalBatchNumber);
		try {
			originalbatch.loadBatch(conn);
		} catch (Exception e) {
			throw new Exception("Error [1511811858] - could not load batch number '" + sOriginalBatchNumber + "' to read information from check to be reversed - " + e.getMessage());
		}
		
		APBatchEntry originalentry = originalbatch.getEntryByEntryNumber(sOriginalEntryNumber);
		if (originalentry == null){
			throw new Exception("Error [1511811859] - could not load entry number '" + sOriginalEntryNumber + "' from original batch number '" + sOriginalBatchNumber 
				+ " to read information from check to be reversed.");
		}
		
		//Now populate the current entry from the original check information:
		setscontrolacct(originalentry.getscontrolacct());
		BigDecimal bdEntryAmt = new BigDecimal(originalentry.getsentryamount().replaceAll(",", "")).negate();  //We are reversing a payment
		setsentryamount(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdEntryAmt));
		setsiprintcheck("0");
		setslbankid(originalentry.getslbankid());
		setsremittoaddressline1(originalentry.getsremittoaddressline1());
		setsremittoaddressline2(originalentry.getsremittoaddressline2());
		setsremittoaddressline3(originalentry.getsremittoaddressline3());
		setsremittoaddressline4(originalentry.getsremittoaddressline4());
		setsremittocity(originalentry.getsremittocity());
		setsremittocode(originalentry.getsremittocode());
		setsremittocountry(originalentry.getsremittocountry());
		setsremittoname(originalentry.getsremittoname());
		setsremittopostalcode(originalentry.getsremittopostalcode());
		setsremittostate(originalentry.getsremittopostalcode());
		setsvendoracct(originalentry.getsvendoracct());
		setsvendorname(originalentry.getsvendorname());
		
		//Populate the entrylines:
		m_arrBatchEntryLines.clear();
		for(int i = 0; i < originalentry.getLineArray().size(); i++){
			APBatchEntryLine line = originalentry.getLineArray().get(i);
			line.setsbdamount(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(new BigDecimal(line.getsbdamount().replaceAll(",", "")).negate()));
			line.setsbddiscountappliedamt(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(new BigDecimal(line.getsbddiscountappliedamt().replaceAll(",", "")).negate()));
			line.setsbdpayableamt(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(new BigDecimal(line.getsbdpayableamt().replaceAll(",", "")).negate()));
			line.setscomment("Reversed invoice application");
			line.setslid("0");
			line.setsbatchnumber(getsbatchnumber());
			line.setsentrynumber(getsentrynumber());
			m_arrBatchEntryLines.add(line);
		}
	}
	public void validate_entry_totals_before_posting(Connection conn) throws Exception{
		
		//Pre-pays have no lines, so they ALWAYS pass this check:
		if (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT){
			return;
		}
		
		BigDecimal bdLineTotalAmount = new BigDecimal("0.00");
		
		for (int i = 0; i < m_arrBatchEntryLines.size(); i++){
			APBatchEntryLine line = m_arrBatchEntryLines.get(i);
			bdLineTotalAmount = bdLineTotalAmount.add(new BigDecimal(line.getsbdamount().replaceAll(",", "")));

			//ALL entry lines must have a valid GL account:
			try {
				line.checkDistributionAccount(conn);
			} catch (Exception e) {
				throw new Exception("Error [1497548870] - invalid GL account '" + line.getsdistributionacct() 
				+ " on entry number " + getsentrynumber()
					+ ", line " + line.getslinenumber() + "."
				);
			}
		}
				
		//Apply-to entries have to ALWAYS total to ZERO:
		if (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO){
			if (bdLineTotalAmount.compareTo(BigDecimal.ZERO) != 0){
				throw new Exception("Apply-to entries must total to zero.");
			}
			return;
		}
		
		//Confirm that the line total matches the entry total:
		if (new BigDecimal(getsentryamount().replaceAll(",", "")).compareTo(bdLineTotalAmount) != 0 ){
			throw new Exception("The total amount on the line(s) (" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdLineTotalAmount) 
				+ ") doesn't match the entry amount (" + getsentryamount() + ") on entry number " + getsentrynumber() + ".");
		}
	}

	public void checkLinePostability(Connection conn) throws Exception{
		
		String sCheckAccountsResult = "";
		for (int i = 0; i < m_arrBatchEntryLines.size(); i++){
			APBatchEntryLine line = m_arrBatchEntryLines.get(i);
			try {
				line.checkDistributionAccount(conn);
			} catch (Exception e) {
				sCheckAccountsResult += e.getMessage() + ".  ";
			}
		}
		if (sCheckAccountsResult.compareToIgnoreCase("") != 0){
			throw new Exception(sCheckAccountsResult);
		}
	}
	
	public String load_payment_line_document_fields(Connection conn) throws Exception{
		String s = "";
		for (int i = 0; i < getLineArray().size(); i++){
			APBatchEntryLine line = getLineArray().get(i);
			String SQL = "SELECT * FROM " + SMTableaptransactions.TableName
				+ " WHERE ("
					+ "(" + SMTableaptransactions.svendor + " = '" + getsvendoracct() + "')"
					+ " AND (" + SMTableaptransactions.sdocnumber + " = '" + line.getsapplytodocnumber() + "')"
				+ ")"
			;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					line.setsdistributionacct(rs.getString(SMTableaptransactions.scontrolacct));
					line.setslapplytodocid(Long.toString(rs.getLong(SMTableaptransactions.lid)));
					line.setsdescription("Applying cash to document '" + line.getsapplytodocnumber() + "'");
					line.setscomment("(Default comment)");
				}else{
					s += "  Apply-to document '" + line.getsapplytodocnumber() + "' on line " + Integer.toString(i + 1) + " was not found.";
				}
				rs.close();
			} catch (Exception e) {
				s += "  Error [1492439991] reading AP Transactions table with SQL '" + SQL + "' - " + e.getMessage() + ".";
			}
		}
		return s;
	}
	
	public boolean entryIsInBalance(Connection conn){
		try {
			validate_entry_totals_before_posting(conn);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public void addDefaultInvoiceLine(ServletContext context, String sDBID, String sUser) throws Exception{
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(context, sDBID, "MySQL", clsServletUtilities.getFullClassName(this.toString()) + ".addDefaultInvoiceLine - user: '" + sUser + "'");
		} catch (Exception e) {
			throw new Exception("Error [1520013821] - could not get connection to add default invoice line - " + e.getMessage());
		}
		
		try {
			addDefaultInvoiceLine(conn, sUser);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1546998957]");
			throw new Exception("Error [1520013822] - " + e.getMessage());
		}
		
		clsDatabaseFunctions.freeConnection(context, conn, "[1546998958]");
		return;
	}
	
	public void addDefaultInvoiceLine(Connection conn, String sUserID) throws Exception{
		
		//System.out.println("[1490918961] getsentrytype() = '" + getsentrytype() + "', getLineArray().size() = '" + getLineArray().size() + "'");
		//We only do this for invoices:
		if (getsentrytype().compareToIgnoreCase(Integer.toString(SMTableapbatchentries.ENTRY_TYPE_INV_INVOICE)) != 0){
			return;
		}
		
		//System.out.println("[1520014318] - getLineArray().size() = " + getLineArray().size());
		//clsServletUtilities.sysprint(this.toString(), sUser, "[1520014318] - getLineArray().size() = " + getLineArray().size());
		
		//We only do it if there are no lines on the invoice already:
		if (getLineArray().size() > 0){
			return;
		}
		
		APVendor ven = new APVendor();
		ven.setsvendoracct(getsvendoracct());
		if(!ven.load(getsvendoracct(), conn)){
			//Don't stop if it fails, just get out:
			//System.out.println("[1490918962] - couldn't load vendor - " + ven.getErrorMessages() + " for '" + getsvendoracct() + "'");
			return;
		}
		APBatchEntryLine line = new APBatchEntryLine();
		//System.out.println("[1490918963] ven.getsdefaultdistributioncode() = '" + ven.getsdefaultdistributioncode() + "',"
		//	+ " vendor code = '" + getsvendoracct() + "'."
		//);
		
		//If there's a 'default distribution code', then add a line to the invoice for that:
		if (
			(ven.getsdefaultdistributioncode().compareToIgnoreCase("-1") != 0)
			&& (ven.getsdefaultdistributioncode().compareToIgnoreCase("") != 0)
		){
			
			//System.out.println("[1490918964] adding line");
			line.setsdistributioncodename(ven.getsdefaultdistributioncode());
			APDistributionCode distcode = new APDistributionCode();
			distcode.loadByDistributionCode(conn, sUserID, ven.getsdefaultdistributioncode());
			line.setsdistributionacct(distcode.getsglacct());
			//System.out.println("[1490918965] distcode.getsglacct() = '" + distcode.getsglacct() + "'");
			line.setsdescription(ven.getsdefaultinvoicelinedescription());
			line.setscomment("(Default invoice line for vendor)");
			line.setslinenumber("1");
			addLine(line);
			return;
		}
		
		//If there's a 'default expense account', then add a line to the invoice for that:
		if (
			(ven.getsdefaultexpenseacct().compareToIgnoreCase("-1") != 0)
			&& (ven.getsdefaultexpenseacct().compareToIgnoreCase("") != 0)
		){
			//System.out.println("[1521489397] ven.getsdefaultexpenseacct() = '" + ven.getsdefaultexpenseacct() + "',"
			//	+ " vendor code = '" + getsvendoracct() + "'."
			//);
			
			line.setsdistributionacct(ven.getsdefaultexpenseacct());
			//System.out.println("[1521489398] line.getsdistributionacct() = '" + line.getsdistributionacct() + "',"
			//	+ " vendor code = '" + getsvendoracct() + "'."
			//);
			
			line.setsdescription(ven.getsdefaultinvoicelinedescription());
			line.setscomment("(Default invoice line for vendor)");
			line.setslinenumber("1");
			addLine(line);
			return;
		}
	}
	public void load(ServletContext context, String sDBID, String sUserID) throws Exception{
		
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".load - user: " + sUserID);
		} catch (Exception e) {
			throw new Exception("Error [1489508149] getting connection - " + e.getMessage());
		}
		
		try {
			load(conn);
		} catch (Exception e) {
			throw new Exception("Error [1489508150] loading - " + e.getMessage());
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1546998959]");
		
	}
	
	public boolean bCheckHasBeenPrintedAndIsNotVoid(String sEntryID, ServletContext context, String sDBID, String sUserID, String sUserFullName) throws Exception{
		
		boolean bResult = false;
		
		String SQL = "SELECT"
			+ " " + SMTableapchecks.lbatchentryid
			+ ", " + SMTableapchecks.iprinted
			+ " FROM " + SMTableapchecks.TableName
			+ " WHERE ("
				+ "(" + SMTableapchecks.lbatchentryid + " = " + sEntryID + ")"
				+ " AND (" + SMTableapchecks.ivoid + " = 0)"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + ".bCheckHasBeenPrinted - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
						)
			);
			if (rs.next()){
				if (rs.getInt(SMTableapchecks.iprinted) == 1){
					bResult = true;
				}
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1509634607] - could not get resultset to determine if check has been printed with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		return bResult;
	}
	
	public void load(Connection conn) throws Exception{
		
		String SQL = "";
		if (
			(getslid().compareToIgnoreCase("") != 0)
			&& (getslid().compareToIgnoreCase("-1") != 0)
			&& (getslid().compareToIgnoreCase("0") != 0)	
		){
			SQL = "SELECT * FROM " + SMTableapbatchentries.TableName
				+ " WHERE ("
					+ "(" + SMTableapbatchentries.lid + " = " + getslid() + ")"
				+ ")"
			;
		}else{
			if (
				(getsbatchnumber().compareToIgnoreCase("") != 0)
				&& (getsentrynumber().compareToIgnoreCase("") != 0)
			){
			SQL = "SELECT * FROM " + SMTableapbatchentries.TableName
					+ " WHERE ("
						+ "(" + SMTableapbatchentries.lbatchnumber + " = " + getsbatchnumber() + ")"
						+ " AND (" + SMTableapbatchentries.lentrynumber + " = " + getsentrynumber() + ")"
					+ ")"
				;
			}
		}
		
		if (SQL.compareToIgnoreCase("") == 0){
			throw new Exception("Error [1507227936] - can't load batch entry without an ID or batch and entry number.");
		}
		
		ResultSet rs = null;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				setsapplytoinvoicenumber(rs.getString(SMTableapbatchentries.sapplytoinvoicenumber));
				setsbatchnumber(Long.toString(rs.getLong(SMTableapbatchentries.lbatchnumber)));
				setscontrolacct(rs.getString(SMTableapbatchentries.scontrolacct));
				setschecknumber(rs.getString(SMTableapbatchentries.schecknumber));
				setsdatdiscount(clsDateAndTimeConversions.resultsetDateStringToFormattedString(
					rs.getString(SMTableapbatchentries.datdiscount), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE));
				setsdatdocdate(clsDateAndTimeConversions.resultsetDateStringToFormattedString(
					rs.getString(SMTableapbatchentries.datdocdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE));
				setsdatduedate(clsDateAndTimeConversions.resultsetDateStringToFormattedString(
					rs.getString(SMTableapbatchentries.datduedate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE));
				setsdatentrydate(clsDateAndTimeConversions.resultsetDateStringToFormattedString(
					rs.getString(SMTableapbatchentries.datentrydate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE));
				setsdocnumber(rs.getString(SMTableapbatchentries.sdocnumber));
				setsentrytype(Integer.toString(rs.getInt(SMTableapbatchentries.ientrytype)));
				setsentryamount(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableapbatchentries.bdentryamount)));
				setsentrydescription(rs.getString(SMTableapbatchentries.sentrydescription));
				setsentrynumber(Long.toString(rs.getLong(SMTableapbatchentries.lentrynumber)));
				setsiinvoiceincludestax(Integer.toString(rs.getInt(SMTableapbatchentries.iinvoiceincludestax)));
				setsionhold(Integer.toString(rs.getInt(SMTableapbatchentries.ionhold)));
				setsiprintcheck(Integer.toString(rs.getInt(SMTableapbatchentries.iprintcheck)));
				setsiprintingfinalized(Integer.toString(rs.getInt(SMTableapbatchentries.iprintingfinalized)));
				setslbankid(Long.toString(rs.getLong(SMTableapbatchentries.lbankid)));
				setslid(Long.toString(rs.getLong(SMTableapbatchentries.lid)));
				setslastline(Long.toString(rs.getLong(SMTableapbatchentries.llastline)));
				setslpurchaseordernumber(Long.toString(rs.getLong(SMTableapbatchentries.lpurchaseordernumber)));
				setslsalesordernumber(Long.toString(rs.getLong(SMTableapbatchentries.lsalesordernumber)));
				setsremittoaddressline1(rs.getString(SMTableapbatchentries.sremittoaddressline1));
				setsremittoaddressline2(rs.getString(SMTableapbatchentries.sremittoaddressline2));
				setsremittoaddressline3(rs.getString(SMTableapbatchentries.sremittoaddressline3));
				setsremittoaddressline4(rs.getString(SMTableapbatchentries.sremittoaddressline4));
				setsremittocity(rs.getString(SMTableapbatchentries.sremittocity));
				setsremittocode(rs.getString(SMTableapbatchentries.sremittocode));
				setsremittocountry(rs.getString(SMTableapbatchentries.sremittocountry));
				setsremittoname(rs.getString(SMTableapbatchentries.sremittoname));
				setsremittopostalcode(rs.getString(SMTableapbatchentries.sremittopostalcode));
				setsremittostate(rs.getString(SMTableapbatchentries.sremittostate));
				setsvendoracct(rs.getString(SMTableapbatchentries.svendoracct));
				setsterms(rs.getString(SMTableapbatchentries.sterms));
				setsdiscountamt(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableapbatchentries.bddiscount)));
				setsvendorname(rs.getString(SMTableapbatchentries.svendorname));
				setstaxjurisdiction(rs.getString(SMTableapbatchentries.staxjurisdiction));
				setsitaxid(Integer.toString(rs.getInt(SMTableapbatchentries.itaxid)));
				setsbdtaxrate(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableapbatchentries.bdtaxrate)));
				setstaxtype(rs.getString(SMTableapbatchentries.staxtype));
				setsicalculateonpurchaseorsale(Integer.toString(rs.getInt(SMTableapbatchentries.icalculateonpurchaseorsale)));
			}else{
				rs.close();
				throw new Exception("Error [1489248040] - No AP batch entry found with lid = " + getslid() + ".");
			}
			rs.close();
		} catch (Exception e) {
			rs.close();
			throw new Exception("Error [1489248041] - loading " + OBJECT_NAME + " with ID " + getslid() + " - " + e.getMessage());
		}
		
		//Load the lines:
		m_arrBatchEntryLines.clear();
		SQL = "SELECT"
			+ " " + SMTableapbatchentrylines.lid
			+ " FROM " + SMTableapbatchentrylines.TableName 
			+ " WHERE ("
				+ "(" + SMTableapbatchentrylines.lbatchnumber + " = " + getsbatchnumber() + ")"
				+ " AND (" + SMTableapbatchentrylines.lentrynumber + " = " + getsentrynumber() + ")"
			+ ") ORDER BY " + SMTableapbatchentrylines.llinenumber
		;
		rs = null;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				APBatchEntryLine line = new APBatchEntryLine();
				line.load(conn, Long.toString(rs.getLong(SMTableapbatchentrylines.lid)), Integer.parseInt(getsentrytype()));
				addLine(line);
			}
			rs.close();
		} catch (Exception e) {
			rs.close();
			throw new Exception("Error [1489248807] loading batch entry lines - " + e.getMessage());
		}
	}
	
	public void loadVendorInformation(ServletContext context, String sDBID, String sUserName,  String sUserID, String sUserFullName) throws Exception{
		
		boolean bVendorWasChangedSinceLastSave = false;
		String SQL = "";
		
		//If the vendor isn't blank:
		if (this.getsvendoracct().compareToIgnoreCase("") != 0){
			//And if the entry has already been saved:
			if ((getslid().compareToIgnoreCase("") != 0) && (getslid().compareToIgnoreCase("-1") != 0)){
				//Then check the saved vendor for this entry and see if the user is changing it:
				SQL = "SELECT " + SMTableapbatchentries.svendoracct
					+ " FROM " + SMTableapbatchentries.TableName
					+ " WHERE ("
						+ "(" + SMTableapbatchentries.lid + " = " + getslid() + ")"
					+ ")"
				;
				try {
					ResultSet rs = clsDatabaseFunctions.openResultSet(
						SQL, 
						context, 
						sDBID, 
						"MySQL", 
						clsServletUtilities.getFullClassName(this.toString()) + ".loadVendorInformation - user: '" + sUserID 
						+ " - "
						+ sUserFullName
						+ "'"
					);
					if (rs.next()){
						if (getsvendoracct().compareToIgnoreCase(rs.getString(SMTableapbatchentries.svendoracct)) != 0){
							bVendorWasChangedSinceLastSave = true;
						}
					}
					rs.close();
				} catch (Exception e) {
					throw new Exception("Error [1520013382] - Could not check for vendor change on '" + getsvendoracct() + "' when loading vendor information.");
				}
			}
		}

		SQL = "SELECT"
			+ " " + SMTableicvendors.TableName + "." + "*"
			+ ",  " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.spayablescontrolacct
			+ ",  " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.sprepaymentacct
			+ ",  " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.spurchasediscountacct
			+ " FROM " + SMTableicvendors.TableName
			+ " LEFT JOIN " + SMTableapaccountsets.TableName
			+ " ON " + SMTableicvendors.TableName + "." + SMTableicvendors.iapaccountset
			+ " = " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.lid
			+ " WHERE ("
			+ "(" + SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct 
			+ " = '" + getsvendoracct() + "')"
			+ ")"
			;

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, context, sDBID,
					"MySQL", SMUtilities.getFullClassName(this.toString())
					+ ".loadVendorInformation - user: " + sUserID
					+ " - "
					+ sUserFullName);
			if (rs.next()) {
				m_svendorname = rs.getString(
						SMTableicvendors.TableName + "."
						+ SMTableicvendors.sname);
				if (m_svendorname == null){
					m_svendorname = "";
				}
				m_sterms = rs.getString(
						SMTableicvendors.TableName + "."
						+ SMTableicvendors.sterms);
				if (m_sterms == null){
					m_sterms = "";
				}
				
				//If it's a pre-pay, then the 'prepay account' is our control account:
				if (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT){
					m_scontrolacct = rs.getString(SMTableapaccountsets.TableName + "." + SMTableapaccountsets.sprepaymentacct);
				}else{
					m_scontrolacct = rs.getString(SMTableapaccountsets.TableName + "." + SMTableapaccountsets.spayablescontrolacct);
				}
				
				if (m_scontrolacct == null){
					m_scontrolacct = "";
				}
				//IF this is a payment or a pre-pay, or an apply-to or a misc payment, then load the default 'remit to' fields as well:
				if (
					(getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT)
					|| (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT)
					|| (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO)
					|| (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_MISCPAYMENT)
						
				){
					setsremittoaddressline1(rs.getString(SMTableicvendors.TableName + "." + SMTableicvendors.saddressline1)); 	 
					setsremittoaddressline2(rs.getString(SMTableicvendors.TableName + "." + SMTableicvendors.saddressline2));
					setsremittoaddressline3(rs.getString(SMTableicvendors.TableName + "." + SMTableicvendors.saddressline3));
					setsremittoaddressline4(rs.getString(SMTableicvendors.TableName + "." + SMTableicvendors.saddressline4));
					setsremittocity(rs.getString(SMTableicvendors.TableName + "." + SMTableicvendors.scity));
					setsremittocode("");
					setsremittocountry(rs.getString(SMTableicvendors.TableName + "." + SMTableicvendors.scountry));
					setsremittoname(rs.getString(SMTableicvendors.TableName + "." + SMTableicvendors.sname));
					setsremittopostalcode(rs.getString(SMTableicvendors.TableName + "." + SMTableicvendors.spostalcode));
					setsremittostate(rs.getString(SMTableicvendors.TableName + "." + SMTableicvendors.sstate));
				}
				
				//If this is a payment or a pre-pay, load the default bank as well:
				if (
						(getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT)
						|| (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT)
							
					){
						setslbankid(Long.toString(rs.getLong(SMTableicvendors.TableName + "." + SMTableicvendors.ibankcode))); 	 
					}
				
			} else {
				rs.close();
				throw new Exception("Error [1490632647] - Could not read records for vendor '" + getsvendoracct() + "' when loading vendor information.");
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1490632648] - Could not read records for vendor '" + getsvendoracct() + "' when loading vendor information - " + e.getMessage());
		}

		//TODO:
		try {
			calculateTerms(context, sDBID, sUserName);
		} catch (Exception e) {
			throw new Exception("Error [1490632649] - Could not calculate terms for vendor '" + getsvendoracct() + "' - " + e.getMessage());
		}

		//If it's a payment or an 'apply-to', we have to remove ALL the lines from the payment, because if the vendor has changed, those lines would
		//be from the previous vendor, and would no long be valid:
		//clsServletUtilities.sysprint(this.toString(), sUserName, "[1522432011] loadVendorInformation");
		if (bVendorWasChangedSinceLastSave){
			if (
				(getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT)
				|| (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO)
				|| (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_INV_CREDITNOTE)
				|| (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_INV_DEBITNOTE)
				|| (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_INV_INVOICE)
				|| (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_MISCPAYMENT)
				|| (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT)
				|| (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_REVERSAL)
			){
				m_arrBatchEntryLines.clear();
				//clsServletUtilities.sysprint(this.toString(), sUserName, "[1522432010] lines cleared");
			}
		}
		return;
	}
	private void calculateTerms(ServletContext context, String sDBID, String sUser) throws Exception{
		//TODO
		
	}
	public String getslid(){
		return m_slid;
	}
	public void setslid(String slid){
		m_slid = slid;
	}

	public String getsbatchnumber(){
		return m_sbatchnumber;
	}
	public void setsbatchnumber(String sBatchNumber){
		m_sbatchnumber = sBatchNumber;
	}

	public String getsentrynumber(){
		return m_sentrynumber;
	}
	public void setsentrynumber(String sEntryNumber){
		m_sentrynumber = sEntryNumber;
	}

	public String getsdocnumber(){
		return m_sdocnumber;
	}
	public void setsdocnumber(String sDocNumber){
		m_sdocnumber = sDocNumber;
	}

	public String getsentrydescription(){
		return m_sentrydescription;
	}
	public void setsentrydescription(String sEntryDescription){
		m_sentrydescription = sEntryDescription;
	}

	public String getsdatentrydate(){
		return m_sdatentrydate;
	}
	public String getsentrydateInSQLFormat() throws Exception{
		if (m_sdatentrydate.compareToIgnoreCase("") == 0){
			return SMUtilities.EMPTY_SQL_DATE_VALUE;
		}else{
			return clsDateAndTimeConversions.convertDateFormat(m_sdatentrydate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE);
		}
	}
	public void setsdatentrydate(String sdatEntryDate){
		m_sdatentrydate = sdatEntryDate;
	}

	public String getsionhold(){
		return m_sionhold;
	}
	public void setsionhold(String sOnHold){
		m_sionhold = sOnHold;
	}
	public String getslastline(){
		return m_slastline;
	}
	public void setslastline(String sLastLine){
		m_slastline = sLastLine;
	}

	public String getsentryamount(){
		return m_sentryamount;
	}
	public void setsentryamount(String sEntryAmount){
		m_sentryamount = sEntryAmount;
	}

	public String getscontrolacct(){
		return m_scontrolacct;
	}
	public void setscontrolacct(String scontrolacct){
		m_scontrolacct = scontrolacct;
	}
	
	public String getsvendoracct(){
		return m_svendoracct;
	}
	public void setsvendoracct(String svendoracct){
		m_svendoracct = svendoracct;
	}
	
	public String getsdatdocdate(){
		return m_sdatdocdate;
	}
	public String getsdatdocdateInSQLFormat() throws Exception{
		if (m_sdatdocdate.compareToIgnoreCase("") == 0){
			return SMUtilities.EMPTY_SQL_DATE_VALUE;
		}else{
			return clsDateAndTimeConversions.convertDateFormat(m_sdatdocdate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE);
		}
	}
	public void setsdatdocdate(String sdatDocDate){
		m_sdatdocdate = sdatDocDate;
	}
	
	public String getsdatdiscount(){
		return m_sdatdiscount;
	}
	public String getsdatdiscountInSQLFormat() throws Exception{
		if (m_sdatdiscount.compareToIgnoreCase("") == 0){
			return SMUtilities.EMPTY_SQL_DATE_VALUE;
		}else{
			return clsDateAndTimeConversions.convertDateFormat(m_sdatdiscount, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE);
		}
	}
	public void setsdatdiscount(String sdatDiscount){
		m_sdatdiscount = sdatDiscount;
	}
	
	public String getsdatduedate(){
		return m_sdatduedate;
	}
	public String getsdatduedateInSQLFormat() throws Exception{
		if (m_sdatduedate.compareToIgnoreCase("") == 0){
			return SMUtilities.EMPTY_SQL_DATE_VALUE;
		}else{
			return clsDateAndTimeConversions.convertDateFormat(m_sdatduedate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE);
		}
	}
	public void setsdatduedate(String sdatDueDate){
		m_sdatduedate = sdatDueDate;
	}
	public String getsterms(){
		return m_sterms;
	}
	public void setsterms(String sTerms){
		m_sterms = sTerms;
	}
	public String getsdiscountamt(){
		return m_sbddiscount;
	}
	public void setsdiscountamt(String sDiscountAmt){
		m_sbddiscount = sDiscountAmt;
	}
	/*
	public String getsdiscountamountforSQLSave(){
		if (getsentrytype().compareToIgnoreCase(Integer.toString(SMTableapbatchentries.ENTRY_TYPE_INV_CREDITNOTE)) == 0){
			//We have to reverse the sign of the amount before saving:
			BigDecimal bdAmt = new BigDecimal(m_sbddiscount.replace(",", ""));
			return SMUtilities.BigDecimalTo2DecimalSTDFormat(bdAmt.multiply(new BigDecimal("-1.00"))).replaceAll(",", "");
		}else{
			return m_sbddiscount.replace(",", "");
		}
	}
	*/
	public String getsvendorname(){
		return m_svendorname;
	}
	public void setsvendorname(String sVendorName){
		m_svendorname = sVendorName;
	}
	public String getstaxjurisdiction(){
		return m_staxjurisdiction;
	}
	public void setstaxjurisdiction(String sTaxJurisdiction){
		m_staxjurisdiction = sTaxJurisdiction;
	}
	public String getsitaxid(){
		return m_sitaxid;
	}
	public void setsitaxid(String siTaxID){
		m_sitaxid = siTaxID;
	}
	public String getsbdtaxrate(){
		return m_sbdtaxrate;
	}
	public void setsbdtaxrate(String sbdTaxRate){
		m_sbdtaxrate = sbdTaxRate;
	}
	public String getstaxtype(){
		return m_staxtype;
	}
	public void setstaxtype(String sTaxType){
		m_staxtype = sTaxType;
	}
	public String getsicalculateonpurchaseorsale(){
		return m_sicalculateonpurchaseorsale;
	}
	public void setsicalculateonpurchaseorsale(String siCalculateOnPurchaseOrSale){
		m_sicalculateonpurchaseorsale = siCalculateOnPurchaseOrSale;
	}
	public String getsentrytype(){
		return m_sentrytype;
	}
	public void setsentrytype(String sEntryType){
		m_sentrytype = sEntryType;
	}
	public int getientrytype(){
		try {
			return Integer.parseInt(m_sentrytype);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	//Payment entry fields:
	public String getschecknumber(){
		return m_schecknumber;
	}
	public void setschecknumber(String schecknumber){
		m_schecknumber = schecknumber;
	}
	public String getsremittocode(){
		return m_sremittocode;
	}
	public void setsremittocode(String sremittocode){
		m_sremittocode = sremittocode;
	}
	public String getsremittoname(){
		return m_sremittoname;
	}
	public void setsremittoname(String sremittoname){
		m_sremittoname = sremittoname;
	}
	public String getsremittoaddressline1(){
		return m_sremittoaddressline1;
	}
	public void setsremittoaddressline1(String sremittoaddressline1){
		m_sremittoaddressline1 = sremittoaddressline1;
	}
	public String getsremittoaddressline2(){
		return m_sremittoaddressline2;
	}
	public void setsremittoaddressline2(String sremittoaddressline2){
		m_sremittoaddressline2 = sremittoaddressline2;
	}
	public String getsremittoaddressline3(){
		return m_sremittoaddressline3;
	}
	public void setsremittoaddressline3(String sremittoaddressline3){
		m_sremittoaddressline3 = sremittoaddressline3;
	}
	public String getsremittoaddressline4(){
		return m_sremittoaddressline4;
	}
	public void setsremittoaddressline4(String sremittoaddressline4){
		m_sremittoaddressline4 = sremittoaddressline4;
	}
	public String getsremittocity(){
		return m_sremittocity;
	}
	public void setsremittocity(String sremittocity){
		m_sremittocity = sremittocity;
	}
	public String getsremittostate(){
		return m_sremittostate;
	}
	public void setsremittostate(String sremittostate){
		m_sremittostate = sremittostate;
	}
	public String getsremittopostalcode(){
		return m_sremittopostalcode;
	}
	public void setsremittopostalcode(String sremittopostalcode){
		m_sremittopostalcode = sremittopostalcode;
	}
	public String getsremittocountry(){
		return m_sremittocountry;
	}
	public void setsremittocountry(String sremittocountry){
		m_sremittocountry = sremittocountry;
	}
	public String getslbankid(){
		return m_slbankid;
	}
	public void setslbankid(String sBankID){
		m_slbankid = sBankID;
	}
	public String getslsalesordernumber(){
		return m_slsalesordernumber;
	}
	public void setslsalesordernumber(String ssalesordernumber){
		m_slsalesordernumber = ssalesordernumber;
	}
	public String getslpurchaseordernumber(){
		return m_slpurchaseordernumber;
	}
	public void setslpurchaseordernumber(String spurchaseordernumber){
		m_slpurchaseordernumber = spurchaseordernumber;
	}
	public String getsapplytoinvoicenumber(){
		return m_sapplytoinvoicenumber;
	}
	public void setsapplytoinvoicenumber(String sapplytoinvoicenumber){
		m_sapplytoinvoicenumber = sapplytoinvoicenumber;
	}
	public String getsiprintcheck(){
		return m_siprintcheck;
	}
	public void setsiprintcheck(String sPrintCheck){
		m_siprintcheck = sPrintCheck;
	}
	public String getsiprintingfinalized(){
		return m_siprintingfinalized;
	}
	public void setsiprintingfinalized(String sPrintingFinalized){
		m_siprintingfinalized = sPrintingFinalized;
	}
	public String getsiinvoiceincludestax(){
		return m_siinvoiceincludestax;
	}
	public void setsiinvoiceincludestax(String siinvoiceincludestax){
		m_siinvoiceincludestax = siinvoiceincludestax;
	}
	
	//Negative numbers, like credit amounts, payment amts, etc., are carried ALL THROUGH THE SYSTEM as negatives.  But when they are displayed 
	//to the user on the entry screen, they appear without the negative sign.  So just before we display them on the screen, we need a function
	//that intelligently knows when to display them without their negative signs.
	//Also, we need a function to read amounts from credits, payments, etc., which appear positive on the entry screen, back into negatives so we
	//can carry them all through the system as negatives.  So we use this function below to do either task:
	public static String displayAbsoluteValueForPaymentInputScreen(String sAmount){
    	String sAmt = "0.00";
    	BigDecimal bdTemp;
		try {
			bdTemp = new BigDecimal(sAmount.trim().replace(",", ""));
			bdTemp = bdTemp.abs();
			sAmt = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapbatchentries.bdentryamountScale, bdTemp);
		} catch (Exception e2) {
			sAmt = sAmount;
		}
		
		return sAmt;
	}
	public static String convertEntryAmtsToSignedValueForStorage(String sAmount, int iEntryType){
    	String sAmt = "0.00";
    	BigDecimal bdTemp;
		try {
			bdTemp = new BigDecimal(sAmount.trim().replace(",", ""));
			
			//For all types, we need to leave the sign that the user entered, so he can enter positives AND negatives here:
			
			
			//The user will enter positive numbers for credit notes, but as soon as we get them from the input screen, we'll reverse their sign so that
			//credit note entry mounts will be stored as negatives:
			if (
					(iEntryType == SMTableapbatchentries.ENTRY_TYPE_INV_CREDITNOTE)
			){
				bdTemp = bdTemp.negate();
			}
			
			//WE have to allow negatives on debit notes and credit notes, so we can't do this:
			//if(
			//	(iEntryType == SMTableapbatchentries.ENTRY_TYPE_INV_DEBITNOTE)
			//	|| (iEntryType == SMTableapbatchentries.ENTRY_TYPE_INV_INVOICE)
			//){
			//	bdTemp = bdTemp.abs();
			//}

			if(
				(iEntryType == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_MISCPAYMENT)
				|| (iEntryType == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT)
				|| (iEntryType == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT)
			){
				//These types of entries ALWAYS get reversed from what appears on the screen:
				bdTemp = bdTemp.negate();
			}

			if(
				(iEntryType == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO)
			){
				//These types of entries REVERSE the sign they came in with
				//TESTINGAPPLYTOS
				//bdTemp = bdTemp.negate();
			}

			sAmt = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapbatchentries.bdentryamountScale, bdTemp);
		} catch (Exception e2) {
			sAmt = sAmount;
		}
		
		return sAmt;
	}

	public void addLine(APBatchEntryLine line){
		m_arrBatchEntryLines.add(line);
	}

	public ArrayList<APBatchEntryLine> getLineArray(){
		return m_arrBatchEntryLines;
	}
	private void saveLines(Connection conn, String sUser, boolean bBatchIsBeingPosted) throws Exception{
		for (int i = 0; i < m_arrBatchEntryLines.size(); i++){
			APBatchEntryLine line = m_arrBatchEntryLines.get(i);
			line.setsbatchnumber(getsbatchnumber());
			try {
				line.save_without_data_transaction(conn, sUser, Integer.parseInt(getsentrytype()), bBatchIsBeingPosted);
			} catch (Exception e) {
				throw new Exception("Error [1488988407] saving line number " + line.getslinenumber() 
					+ " on entry number " + this.getsentrynumber() + " - " + e.getMessage()
				);
			}
		}
		//We also have to delete any EXTRA lines that might be left that are higher than our current highest line number:
		//This can happen if we removed a line and we now have fewer lines than we previously had:
		String SQL = "DELETE FROM " + SMTableapbatchentrylines.TableName
			+ " WHERE ("
				+ "(" + SMTableapbatchentrylines.lbatchnumber + " = " + getsbatchnumber() + ")"
				+ " AND (" + SMTableapbatchentrylines.lentrynumber + " = " + getsentrynumber() + ")"
				+ " AND (" + SMTableapbatchentrylines.llinenumber + " > " + Integer.toString(m_arrBatchEntryLines.size()) + ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1490384889] deleting leftover entry lines - " + e.getMessage());
		}
		
	}
	public void removeLineByLineNumber(String sLineNumber) throws Exception{
    	//System.out.println("[1489528400] - sEntryNumber = " + sEntryNumber);
		
		boolean bLineNumberWasFound = false;
		for (int i = 0; i < m_arrBatchEntryLines.size(); i++){
			if (m_arrBatchEntryLines.get(i).getslinenumber().compareToIgnoreCase(sLineNumber) == 0){
				m_arrBatchEntryLines.remove(i);
				bLineNumberWasFound = true;
			}
		}
		
    	BigDecimal bdLineTotal = new BigDecimal("0.00");
    	for (int i = 0; i < m_arrBatchEntryLines.size(); i++){
    		//Reset the line numbers:
    		m_arrBatchEntryLines.get(i).setslinenumber(Integer.toString(i + 1));
    		bdLineTotal = bdLineTotal.add(new BigDecimal(m_arrBatchEntryLines.get(i).getsbdamount().replaceAll(",", "")));
    	}

    	//Recalculate the entry total to match the total of the lines:
    	//Commented this out because we are not requiring that an entry be in balance before it can be saved:
    	//setsentryamount(SMUtilities.BigDecimalTo2DecimalSTDFormat(bdLineTotal));
    	
    	if (!bLineNumberWasFound){
    		throw new Exception("Line number '" + sLineNumber + "' was not found in the entry.");
    	}
	}
	public void applyLineToDocNumber(String sDocNumber, ServletContext context, String sDBID, String sUserID, String sUserFullName) throws Exception{
    	//System.out.println("[1489528400] - sEntryNumber = " + sEntryNumber);
		
		APBatchEntryLine line = new APBatchEntryLine();
		String SQL = "SELECT * FROM " + SMTableaptransactions.TableName
			+ " WHERE ("
				+ "(" + SMTableaptransactions.svendor + " = '" + getsvendoracct() + "')"
				+ " AND (" + SMTableaptransactions.sdocnumber + " = '" + sDocNumber + "')"
			+ ")"
		;
		//System.out.println("[1537902165] - SQL = '" + SQL + "'");
		ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".applyPaymentLineToDocNumber - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
		);
		
		if (rs.next()){
			line.setsapplytodocnumber(sDocNumber);
			line.setsbatchnumber(getsbatchnumber());
			
			// TJR - Added 1/5/2019:
			//We have to determine whether we are eligible for the discount, based on the payment date:
			BigDecimal bdEligibleDiscountAmt = rs.getBigDecimal(SMTableaptransactions.bdcurrentdiscountavailable);
			String sDiscountDate = rs.getString(SMTableaptransactions.datdiscountdate);
			String sPaymentDate = getsdatdocdateInSQLFormat();
			if (sPaymentDate.compareToIgnoreCase(sDiscountDate) > 0){
				bdEligibleDiscountAmt = BigDecimal.ZERO;
			}
			
			//When we are creating an 'apply-to' line, the sign of the amount one the batch entry line, is OPPOSITE to the sign of the original amt of the apply-to transaction: 
			
			//If it's a PAYMENT or a CREDIT NOTE, then the credits and negative values should appear as positive:
			if (
				(getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT)
				|| (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_INV_CREDITNOTE)
			){
				line.setsbddiscountappliedamt(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableapbatchentrylines.bdapplieddiscountamtScale, bdEligibleDiscountAmt.negate()));
				
				//If it's a PAYMENT, We apply only the DISCOUNTED amt against the CURRENT amt, we don't apply the whole CURRENT amt against it:
				if((getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT)){
					line.setsbdamount(clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableapbatchentrylines.bdamountScale, (rs.getBigDecimal(SMTableaptransactions.bdcurrentamt).subtract(bdEligibleDiscountAmt)).negate()));
				}else{
					line.setsbdamount(clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableapbatchentrylines.bdamountScale, (rs.getBigDecimal(SMTableaptransactions.bdcurrentamt).negate())));
				}
			}
			
			//If it's a DEBIT NOTE, then the amount should appear  as POSITIVE ( BigDecimal.abs() ):
			if (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_INV_DEBITNOTE){
				line.setsbddiscountappliedamt(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableapbatchentrylines.bdapplieddiscountamtScale, bdEligibleDiscountAmt.abs()));
				//Set the line amount to equal the current amount of the transaction, and it should always be POSITIVE:
				line.setsbdamount(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableapbatchentrylines.bdamountScale, (rs.getBigDecimal(SMTableaptransactions.bdcurrentamt).abs())));
			}
			
			//But if it's an 'apply-to', then we need to see the negatives appear on the screen:
			//The line amount should be the OPPOSITE ARITHMETIC SIGN from the amount it's being applied TO:
			if (getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO){
				line.setsbddiscountappliedamt(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableapbatchentrylines.bdapplieddiscountamtScale, bdEligibleDiscountAmt.negate()));
				
				line.setsbdamount(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableapbatchentrylines.bdamountScale, (rs.getBigDecimal(SMTableaptransactions.bdcurrentamt).negate())));
			}

			line.setscomment("(Default comment)");
			line.setsdescription("Applying line to document '" + sDocNumber + "'");
			
			//IF it's a credit note or debit note, we'll leave the distribution acct blank, but otherwise, we'll use the control account from the apply-to doc:
			String sDistributionAcct = "";
			if (
				(getientrytype() != SMTableapbatchentries.ENTRY_TYPE_INV_CREDITNOTE)
				&& (getientrytype() != SMTableapbatchentries.ENTRY_TYPE_INV_DEBITNOTE)
			){
				sDistributionAcct = rs.getString(SMTableaptransactions.scontrolacct);
			}
			
			line.setsdistributionacct(sDistributionAcct);
			line.setsentrynumber(getsentrynumber());
			line.setslapplytodocid(Long.toString(rs.getLong(SMTableaptransactions.lid)));
			line.setslinenumber(Integer.toString(m_arrBatchEntryLines.size() + 1));
			
		}else{
			rs.close();
			throw new Exception("Apply to document with number '" + sDocNumber + "' for vendor '" + getsvendoracct() + "' was not found.");
		}
		rs.close();
		m_arrBatchEntryLines.add(line);
		return;
	}
	
	public String getQueryString(){
		//Particular to the specific class
		String sQueryString = "";
		sQueryString += SMTableapbatchentries.bdentryamount + "=" + clsServletUtilities.URLEncode(getsentryamount());
		sQueryString += "&" + SMTableapbatchentries.datdiscount + "=" + clsServletUtilities.URLEncode(getsdatdiscount());
		sQueryString += "&" + SMTableapbatchentries.datdocdate + "=" + clsServletUtilities.URLEncode(getsdatdocdate());
		sQueryString += "&" + SMTableapbatchentries.datduedate + "=" + clsServletUtilities.URLEncode(getsdatduedate());
		sQueryString += "&" + SMTableapbatchentries.datentrydate + "=" + clsServletUtilities.URLEncode(getsdatentrydate());
		sQueryString += "&" + SMTableapbatchentries.lbatchnumber + "=" + clsServletUtilities.URLEncode(getsbatchnumber());
		sQueryString += "&" + SMTableapbatchentries.lentrynumber + "=" + clsServletUtilities.URLEncode(getsentrynumber());
		sQueryString += "&" + SMTableapbatchentries.lbankid + "=" + clsServletUtilities.URLEncode(getslbankid());
		sQueryString += "&" + SMTableapbatchentries.lid + "=" + clsServletUtilities.URLEncode(getslid());
		sQueryString += "&" + SMTableapbatchentries.llastline + "=" + clsServletUtilities.URLEncode(getslastline());
		sQueryString += "&" + SMTableapbatchentries.sapplytoinvoicenumber + "=" + clsServletUtilities.URLEncode(getsapplytoinvoicenumber());
		sQueryString += "&" + SMTableapbatchentries.scontrolacct + "=" + clsServletUtilities.URLEncode(getscontrolacct());
		sQueryString += "&" + SMTableapbatchentries.sdocnumber + "=" + clsServletUtilities.URLEncode(getsdocnumber());
		sQueryString += "&" + SMTableapbatchentries.sentrydescription + "=" + clsServletUtilities.URLEncode(getsentrydescription());
		sQueryString += "&" + SMTableapbatchentries.svendoracct + "=" + clsServletUtilities.URLEncode(getsvendoracct());
		sQueryString += "&" + SMTableapbatchentries.sterms + "=" + clsServletUtilities.URLEncode(getsterms());
		sQueryString += "&" + SMTableapbatchentries.bddiscount + "=" + clsServletUtilities.URLEncode(getsdiscountamt());
		sQueryString += "&" + SMTableapbatchentries.svendorname + "=" + clsServletUtilities.URLEncode(getsvendorname());
		sQueryString += "&" + SMTableapbatchentries.staxjurisdiction + "=" + clsServletUtilities.URLEncode(getstaxjurisdiction());
		sQueryString += "&" + SMTableapbatchentries.itaxid + "=" + clsServletUtilities.URLEncode(getsitaxid());
		sQueryString += "&" + SMTableapbatchentries.bdtaxrate + "=" + clsServletUtilities.URLEncode(getsbdtaxrate());
		sQueryString += "&" + SMTableapbatchentries.staxtype + "=" + clsServletUtilities.URLEncode(getstaxtype());
		sQueryString += "&" + SMTableapbatchentries.icalculateonpurchaseorsale + "=" + clsServletUtilities.URLEncode(getsicalculateonpurchaseorsale());
		sQueryString += "&" + SMTableapbatchentries.ientrytype + "=" + clsServletUtilities.URLEncode(getsentrytype());
		sQueryString += "&" + SMTableapbatchentries.sremittoaddressline1 + "=" + clsServletUtilities.URLEncode(getsremittoaddressline1());
		sQueryString += "&" + SMTableapbatchentries.sremittoaddressline2 + "=" + clsServletUtilities.URLEncode(getsremittoaddressline2());
		sQueryString += "&" + SMTableapbatchentries.sremittoaddressline3 + "=" + clsServletUtilities.URLEncode(getsremittoaddressline3());
		sQueryString += "&" + SMTableapbatchentries.sremittoaddressline4 + "=" + clsServletUtilities.URLEncode(getsremittoaddressline4());
		sQueryString += "&" + SMTableapbatchentries.sremittocity + "=" + clsServletUtilities.URLEncode(getsremittocity());
		sQueryString += "&" + SMTableapbatchentries.sremittocode + "=" + clsServletUtilities.URLEncode(getsremittocode());
		sQueryString += "&" + SMTableapbatchentries.sremittocountry + "=" + clsServletUtilities.URLEncode(getsremittocountry());
		sQueryString += "&" + SMTableapbatchentries.sremittoname + "=" + clsServletUtilities.URLEncode(getsremittoname());
		sQueryString += "&" + SMTableapbatchentries.sremittopostalcode + "=" + clsServletUtilities.URLEncode(getsremittopostalcode());
		sQueryString += "&" + SMTableapbatchentries.sremittostate + "=" + clsServletUtilities.URLEncode(getsremittostate());
		sQueryString += "&" + SMTableapbatchentries.iprintcheck + "=" + clsServletUtilities.URLEncode(getsiprintcheck());
		sQueryString += "&" + SMTableapbatchentries.iprintingfinalized + "=" + clsServletUtilities.URLEncode(getsiprintingfinalized());
		sQueryString += "&" + SMTableapbatchentries.iinvoiceincludestax + "=" + clsServletUtilities.URLEncode(getsiinvoiceincludestax());
		
		return sQueryString;
	}
	public String dumpData(){
		String s = "";
		s += "  Entry amount: " + getsentryamount() + "\n";
		s += "  Disc date: " + getsdatdiscount() + "\n";
		s += "  Doc date: " + getsdatdocdate() + "\n";
		s += "  Due date: " + getsdatduedate() + "\n";
		s += "  Entry date: " + getsdatentrydate() + "\n";
		s += "  Batch: " + getsbatchnumber() + "\n";
		s += "  Entry: " + getsentrynumber() + "\n";
		s += "  Bank ID: " + getslbankid() + "\n";
		s += "  Entry ID: " + getslid() + "\n";
		s += "  Last line: " + getslastline() + "\n";
		s += "  Control acct: " + getscontrolacct() + "\n";
		s += "  Doc number: " + getsdocnumber() + "\n";
		s += "  Desc: " + getsentrydescription() + "\n";
		s += "  Vendor: " + getsvendoracct() + "\n";
		s += "  Terms: " + getsterms() + "\n";
		s += "  Disc amt: " + getsdiscountamt() + "\n";
		s += "  Vendor name: " + getsvendorname() + "\n";
		s += "  Tax jurisdiction: " + getstaxjurisdiction() + "\n";
		s += "  Tax id: " + getsitaxid() + "\n";
		s += "  Tax rate: " + getsbdtaxrate() + "\n";
		s += "  Tax type: " + getstaxtype() + "\n";
		s += "  Tax calc on purchase or sale: " + getsicalculateonpurchaseorsale() + "\n";
		s += "  Entry type: " + getsentrytype() + "\n";
		s += "  Remit address 1: " + getsremittoaddressline1() + "\n";
		s += "  Remit address 2: " + getsremittoaddressline2() + "\n";
		s += "  Remit address 3: " + getsremittoaddressline3() + "\n";
		s += "  Remit address 4: " + getsremittoaddressline4() + "\n";
		s += "  Remit city: " + getsremittocity() + "\n";
		s += "  Remit to code: " + getsremittocode() + "\n";
		s += "  Remit country: " + getsremittocountry() + "\n";
		s += "  Remit name: " + getsremittoname() + "\n";
		s += "  Remit zip: " + getsremittopostalcode() + "\n";
		s += "  Remit state: " + getsremittostate() + "\n";
		s += "  Purchase order number: " + getslpurchaseordernumber() + "\n";
		s += "  Sales order number: " + getslsalesordernumber() + "\n";
		s += "  Apply-to invoice number: " + getsapplytoinvoicenumber() + "\n";
		s += "  Print check?: " + getsiprintcheck() + "\n";
		s += "  Printing finalized?: " + getsiprintingfinalized() + "\n";
		s += "  Invoice includes tax?: " + getsiinvoiceincludestax() + "\n";
		
		s += "  -- Number of lines: " + m_arrBatchEntryLines.size() + "\n";
		
		for (int i = 0; i < m_arrBatchEntryLines.size(); i++){
			s += "  LINE " + (i + 1) + ":\n";
			s += m_arrBatchEntryLines.get(i).dumpData();
		}
		
		return s;
	}
	private void initializeVariables(){
		m_slid  = "-1";
		m_sbatchnumber = "-1";
		m_sentrynumber = "-1";
		m_sdocnumber = "";
		m_sentrydescription = "";
		m_sdatentrydate = SMUtilities.EMPTY_DATE_VALUE;  //clsDateAndTimeConversions.now(SMUtilities.DATE_FORMAT_FOR_DISPLAY);
		m_slastline = "0";
		m_sentryamount = "0.00";
		m_svendoracct = "";
		m_scontrolacct = "";
		m_sdatdocdate = SMUtilities.EMPTY_DATE_VALUE;
		m_sdatdiscount = SMUtilities.EMPTY_DATE_VALUE;
		m_sdatduedate = SMUtilities.EMPTY_DATE_VALUE;
		m_sterms = "";
		m_sbddiscount = "0.0000";
		m_svendorname = "";
		m_staxjurisdiction = "";
		m_sitaxid = "0";
		m_sbdtaxrate = "0.00";
		m_staxtype = "0";
		m_sicalculateonpurchaseorsale = "0";
		m_sentrytype = "0";
		m_schecknumber = "";
		m_sremittocode = "";
		m_sremittoname = "";
		m_sremittoaddressline1 = ""; 	 
		m_sremittoaddressline2 = "";
		m_sremittoaddressline3 = "";
		m_sremittoaddressline4 = "";
		m_sremittocity = "";
		m_sremittostate = "";	 
		m_sremittopostalcode = "";
		m_sremittocountry = "";
		m_sionhold = "0";
		m_slbankid = "0";
		m_slpurchaseordernumber = "0";
		m_slsalesordernumber = "0";
		m_sapplytoinvoicenumber = "";
		m_siprintcheck = "0";
		m_siprintingfinalized = "0";
		m_siinvoiceincludestax = "0";
		m_arrBatchEntryLines = new ArrayList<APBatchEntryLine>(0);
	}
}
