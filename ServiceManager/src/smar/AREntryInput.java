package smar;

import java.util.ArrayList;
import java.util.Enumeration;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;

import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;

import javax.servlet.ServletContext;
public class AREntryInput extends java.lang.Object{

	public static final String ParamEntryID = "EntryID";
	public static final String ParamBatchNumber = "BatchNumber";
	public static final String ParamEntryNumber = "EntryNumber";
	public static final String ParamDocumentType = "DocumentType";
	public static final String ParamCustomerNumber = "CustomerNumber";
	public static final String ParamDocNumber = "DocNumber";
	public static final String ParamDocDescription = "DocDescription";
	public static final String ParamDocDate = "DocDate";
	public static final String ParamTerms = "Terms";
	public static final String ParamDueDate = "DueDate";
	public static final String ParamControlAcct = "ControlAcct";
	public static final String ParamOrderNumber = "OrderNumber";
	public static final String ParamPONumber = "PONumber";
	public static final String ParamOriginalAmount = "OriginalAmount";
	public static final String ParamCurrentAmount = "CurrentAmount";
	public static final String ParamUndistributedAmount = "UndistributedAmount";
	public static final String ParamRetainageDateYear = "RetainageDateYear";
	public static final String ParamRetainageDateMonth = "RetainageDateMonth";
	public static final String ParamRetainageDateDay = "RetainageDateDay";
	public static final String ParamBatchType = "BatchType";
	public static final String ParamNumberOfLines = "NumberOfLines";
	
	private String m_sEntryID;
	private String m_sBatchNumber;
	private String m_sEntryNumber;
	private String m_sDocumentType;
	private String m_sCustomerNumber;
	private String m_sDocNumber;
	private String m_sDocDescription;
	private String m_sDocDate;
	private String m_sTerms;
	private String m_sDueDate;
	private String m_sControlAcct;
	private String m_sOrderNumber;
	private String m_sPONumber;
	private String m_sOriginalAmount;
	private String m_sUndistributedAmount;
	private String m_sBatchType;
	private ArrayList<ARLineInput> m_LineInputArray;
	private int m_iNumberOfLines;
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);

	AREntryInput(){
		m_sEntryID = "";
		m_sBatchNumber = "";
		m_sEntryNumber = "";
		m_sDocumentType = "";
		m_sCustomerNumber = "";
		m_sDocNumber = "";
		m_sDocDescription = "";
		m_sDocDate = "";
		m_sTerms = "";
		m_sDueDate = "";
		m_sControlAcct = "";
		m_sOrderNumber = "";
		m_sPONumber = "";
		m_sOriginalAmount = "0.00";
		m_sUndistributedAmount = "0.00";
		m_sBatchType = "";
		m_LineInputArray = new ArrayList<ARLineInput> (0);
		m_iNumberOfLines = 0;
		m_sErrorMessageArray = new ArrayList<String> (0);
	}
	
	AREntryInput (HttpServletRequest req) throws Exception{

		m_sEntryID = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamEntryID, req).trim();
		m_sBatchType = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamBatchType, req).trim();
		m_sBatchNumber = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamBatchNumber, req).trim();
		m_sEntryNumber = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamEntryNumber, req).trim();
		m_sDocumentType = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamDocumentType, req).trim();
		m_sDocNumber = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamDocNumber, req).trim();
		m_sDocDescription = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamDocDescription, req).trim();
		m_sDocDate = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamDocDate, req).trim();
		//m_sDocDate = 
		//	clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamDocDateYear, req)
		//	+ "-" + clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamDocDateMonth, req)
		//	+ "-" + clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamDocDateDay, req);
		m_sControlAcct = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamControlAcct, req).trim();
		m_sOrderNumber = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamOrderNumber, req).trim();
		m_sPONumber = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamPONumber, req).trim();
		m_sOriginalAmount = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamOriginalAmount, req).trim();
		m_sUndistributedAmount = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamUndistributedAmount, req).trim();
		
		//Based on the document types, we'll set values differently:
		if(m_sDocumentType.equalsIgnoreCase(ARDocumentTypes.APPLYTO_STRING)){
			m_sCustomerNumber = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamCustomerNumber, req).trim().toUpperCase();
			m_sTerms = "";
			m_sDueDate = "";
		}
		if(m_sDocumentType.equalsIgnoreCase(ARDocumentTypes.CASHADJUSTMENT_STRING)){
			m_sCustomerNumber = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamCustomerNumber, req).trim().toUpperCase();
			m_sTerms = "";
			m_sDueDate = "";
		}
		if(m_sDocumentType.equalsIgnoreCase(ARDocumentTypes.CREDIT_STRING)){
			m_sCustomerNumber = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamCustomerNumber, req).trim().toUpperCase();
			m_sTerms = "";
			m_sDueDate = "";
		}
		if(m_sDocumentType.equalsIgnoreCase(ARDocumentTypes.CREDITADJUSTMENT_STRING)){
			m_sCustomerNumber = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamCustomerNumber, req).trim().toUpperCase();
			m_sTerms = "";
			m_sDueDate = "";
		}
		if(m_sDocumentType.equalsIgnoreCase(ARDocumentTypes.INVOICE_STRING)){
			m_sCustomerNumber = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamCustomerNumber, req).trim().toUpperCase();
			m_sTerms = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamTerms, req).trim();
			m_sDueDate = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamDueDate, req).trim();
		}
		if(m_sDocumentType.equalsIgnoreCase(ARDocumentTypes.INVOICEADJUSTMENT_STRING)){
			m_sCustomerNumber = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamCustomerNumber, req).trim().toUpperCase();
			m_sTerms = "";
			m_sDueDate = "";
		}
		if(m_sDocumentType.equalsIgnoreCase(ARDocumentTypes.MISCRECEIPT_STRING)){
			m_sCustomerNumber = "";
			m_sTerms = "";
			m_sDueDate = "";
		}
		if(m_sDocumentType.equalsIgnoreCase(ARDocumentTypes.PREPAYMENT_STRING)){
			m_sCustomerNumber = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamCustomerNumber, req).trim().toUpperCase();
			m_sTerms = "";
			m_sDueDate = "";
		}
		if(m_sDocumentType.equalsIgnoreCase(ARDocumentTypes.RECEIPT_STRING)){
			m_sCustomerNumber = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamCustomerNumber, req).trim().toUpperCase();
			m_sTerms = "";
			m_sDueDate = "";
		}
		if(m_sDocumentType.equalsIgnoreCase(ARDocumentTypes.RETAINAGE_STRING)){
			m_sCustomerNumber = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamCustomerNumber, req).trim().toUpperCase();
			m_sTerms = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamTerms, req).trim();
			m_sDueDate = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamDueDate, req).trim();
		}
		if(m_sDocumentType.equalsIgnoreCase(ARDocumentTypes.REVERSAL_STRING)){
			m_sCustomerNumber = clsManageRequestParameters.get_Request_Parameter(AREntryInput.ParamCustomerNumber, req).trim().toUpperCase();
			m_sTerms = "";
			m_sDueDate = "";
		}

		//Load the lines from the servlet request:
		try {
			m_iNumberOfLines = Integer.parseInt(req.getParameter(AREntryInput.ParamNumberOfLines));
		} catch (NumberFormatException e) {
			m_iNumberOfLines = 0;
			clsServletUtilities.sysprint(this.toString(), "SYSTEM", "Error [1416603102] in AREntryInput - m_iNumberOfLines = " 
					+ m_iNumberOfLines + ".");
			throw new Exception("Error [1389107798] - Number of lines is invalid: '" + req.getParameter(AREntryInput.ParamNumberOfLines) + "'.");
		}
		m_LineInputArray = new ArrayList<ARLineInput>(0);
		for (int i = 0; i < m_iNumberOfLines; i ++){
			ARLineInput lineInput = new ARLineInput();
			m_LineInputArray.add((ARLineInput) lineInput);
		}
		
		//System.out.println("In AREntryInput.AREntryInput(req), m_iNumberOfLines = " + m_iNumberOfLines);
		//System.out.println("In AREntryInput.AREntryInput(req), m_LineInputArray.size() = " + m_LineInputArray.size());
		
	    Enumeration <String> paramNames = req.getParameterNames();
	    int iLineNumber = 0;
	    try {
			while(paramNames.hasMoreElements()) {
				String sParamName = paramNames.nextElement();
				//System.out.println("In AREntryInput.AREntryInput(req), sParamName = " + sParamName);
				if (sParamName.contains(ARLineInput.ParamDistAcct)){
					//Strip off the line number
					iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamDistAcct.length(), sParamName.length()));
					m_LineInputArray.get(iLineNumber).setLineAcct((String) clsManageRequestParameters.get_Request_Parameter(sParamName, req).trim());
				}
				if (sParamName.contains(ARLineInput.ParamLineAmt)){
					//Strip off the line number
					//System.out.println("In AREntryInput.AREntryInput(req), clsManageRequestParameters.get_Request_Parameter(sParamName, req) = " + clsManageRequestParameters.get_Request_Parameter(sParamName, req));
					
					iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamLineAmt.length(), sParamName.length()));
					m_LineInputArray.get(iLineNumber).setAmount((String) clsManageRequestParameters.get_Request_Parameter(sParamName, req).trim());
				}
				if (sParamName.contains(ARLineInput.ParamLineApplyToOrderNumber)){
					//Strip off the line number
					iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamLineApplyToOrderNumber.length(), sParamName.length()));
					m_LineInputArray.get(iLineNumber).setApplyToOrderNumber((String) clsManageRequestParameters.get_Request_Parameter(sParamName, req).trim());
				}
				if (sParamName.contains(ARLineInput.ParamLineComment)){
					//Strip off the line number
					iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamLineComment.length(), sParamName.length()));
					m_LineInputArray.get(iLineNumber).setComment((String) clsManageRequestParameters.get_Request_Parameter(sParamName, req).trim());
				}
				if (sParamName.contains(ARLineInput.ParamLineDesc)){
					//Strip off the line number
					iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamLineDesc.length(), sParamName.length()));
					m_LineInputArray.get(iLineNumber).setDescription((String) clsManageRequestParameters.get_Request_Parameter(sParamName, req).trim());
				}
				if (sParamName.contains(ARLineInput.ParamLineID)){
					//Strip off the line number
					iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamLineID.length(), sParamName.length()));
					m_LineInputArray.get(iLineNumber).setLineID((String) clsManageRequestParameters.get_Request_Parameter(sParamName, req).trim());
				}
				if(m_sDocumentType.equalsIgnoreCase(ARDocumentTypes.APPLYTO_STRING)){
					if (sParamName.contains(ARLineInput.ParamDocAppliedTo)){
						//Strip off the line number
						iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamDocAppliedTo.length(), sParamName.length()));
						m_LineInputArray.get(iLineNumber).setDocAppliedTo((String) clsManageRequestParameters.get_Request_Parameter(sParamName, req).trim());
					}
					if (sParamName.contains(ARLineInput.ParamLineDocAppliedToID)){
						//Strip off the line number
						iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamLineDocAppliedToID.length(), sParamName.length()));
						m_LineInputArray.get(iLineNumber).setDocAppliedToID((String) clsManageRequestParameters.get_Request_Parameter(sParamName, req).trim());
					}
				}
				if(m_sDocumentType.equalsIgnoreCase(ARDocumentTypes.CASHADJUSTMENT_STRING)){
					if (sParamName.contains(ARLineInput.ParamDocAppliedTo)){
						//Strip off the line number
						iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamDocAppliedTo.length(), sParamName.length()));
						m_LineInputArray.get(iLineNumber).setDocAppliedTo((String) clsManageRequestParameters.get_Request_Parameter(sParamName, req).trim());
					}
					if (sParamName.contains(ARLineInput.ParamLineDocAppliedToID)){
						//Strip off the line number
						iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamLineDocAppliedToID.length(), sParamName.length()));
						m_LineInputArray.get(iLineNumber).setDocAppliedToID((String) clsManageRequestParameters.get_Request_Parameter(sParamName, req).trim());
					}
				}
				if(m_sDocumentType.equalsIgnoreCase(ARDocumentTypes.CREDIT_STRING)){
					if (sParamName.contains(ARLineInput.ParamDocAppliedTo)){
						//Strip off the line number
						iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamDocAppliedTo.length(), sParamName.length()));
						m_LineInputArray.get(iLineNumber).setDocAppliedTo((String) clsManageRequestParameters.get_Request_Parameter(sParamName, req).trim());
					}
					if (sParamName.contains(ARLineInput.ParamLineDocAppliedToID)){
						//Strip off the line number
						iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamLineDocAppliedToID.length(), sParamName.length()));
						m_LineInputArray.get(iLineNumber).setDocAppliedToID((String) clsManageRequestParameters.get_Request_Parameter(sParamName, req).trim());
					}
				}
				if(m_sDocumentType.equalsIgnoreCase(ARDocumentTypes.CREDITADJUSTMENT_STRING)){
					if (sParamName.contains(ARLineInput.ParamDocAppliedTo)){
						//Strip off the line number
						iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamDocAppliedTo.length(), sParamName.length()));
						m_LineInputArray.get(iLineNumber).setDocAppliedTo((String) clsManageRequestParameters.get_Request_Parameter(sParamName, req).trim());
					}
					if (sParamName.contains(ARLineInput.ParamLineDocAppliedToID)){
						//Strip off the line number
						iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamLineDocAppliedToID.length(), sParamName.length()));
						m_LineInputArray.get(iLineNumber).setDocAppliedToID((String) clsManageRequestParameters.get_Request_Parameter(sParamName, req).trim());
					}
				}
				if(m_sDocumentType.equalsIgnoreCase(ARDocumentTypes.INVOICE_STRING)){
					if (sParamName.contains(ARLineInput.ParamDocAppliedTo)){
						//Strip off the line number
						iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamDocAppliedTo.length(), sParamName.length()));
						m_LineInputArray.get(iLineNumber).setDocAppliedTo("UNAPPLIED");
					}
					if (sParamName.contains(ARLineInput.ParamLineDocAppliedToID)){
						//Strip off the line number
						iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamLineDocAppliedToID.length(), sParamName.length()));
						m_LineInputArray.get(iLineNumber).setDocAppliedToID("-1");
					}
				}
				if(m_sDocumentType.equalsIgnoreCase(ARDocumentTypes.INVOICEADJUSTMENT_STRING)){
					if (sParamName.contains(ARLineInput.ParamDocAppliedTo)){
						//Strip off the line number
						iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamDocAppliedTo.length(), sParamName.length()));
						m_LineInputArray.get(iLineNumber).setDocAppliedTo((String) clsManageRequestParameters.get_Request_Parameter(sParamName, req).trim());
					}
					if (sParamName.contains(ARLineInput.ParamLineDocAppliedToID)){
						//Strip off the line number
						iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamLineDocAppliedToID.length(), sParamName.length()));
						m_LineInputArray.get(iLineNumber).setDocAppliedToID((String) clsManageRequestParameters.get_Request_Parameter(sParamName, req).trim());
					}
				}
				if(m_sDocumentType.equalsIgnoreCase(ARDocumentTypes.MISCRECEIPT_STRING)){
					if (sParamName.contains(ARLineInput.ParamDocAppliedTo)){
						//Strip off the line number
						iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamDocAppliedTo.length(), sParamName.length()));
						m_LineInputArray.get(iLineNumber).setDocAppliedTo("UNAPPLIED");
					}
					if (sParamName.contains(ARLineInput.ParamLineDocAppliedToID)){
						//Strip off the line number
						iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamLineDocAppliedToID.length(), sParamName.length()));
						m_LineInputArray.get(iLineNumber).setDocAppliedToID("-1");
					}
				}
				if(m_sDocumentType.equalsIgnoreCase(ARDocumentTypes.PREPAYMENT_STRING)){
					if (sParamName.contains(ARLineInput.ParamDocAppliedTo)){
						//Strip off the line number
						iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamDocAppliedTo.length(), sParamName.length()));
						m_LineInputArray.get(iLineNumber).setDocAppliedTo("UNAPPLIED");
					}
					if (sParamName.contains(ARLineInput.ParamLineDocAppliedToID)){
						//Strip off the line number
						iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamLineDocAppliedToID.length(), sParamName.length()));
						m_LineInputArray.get(iLineNumber).setDocAppliedToID("-1");
					}
				}
				if(m_sDocumentType.equalsIgnoreCase(ARDocumentTypes.RECEIPT_STRING)){
					if (sParamName.contains(ARLineInput.ParamDocAppliedTo)){
						//Strip off the line number
						iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamDocAppliedTo.length(), sParamName.length()));
						m_LineInputArray.get(iLineNumber).setDocAppliedTo((String) clsManageRequestParameters.get_Request_Parameter(sParamName, req).trim());
					}
					if (sParamName.contains(ARLineInput.ParamLineDocAppliedToID)){
						//Strip off the line number
						iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamLineDocAppliedToID.length(), sParamName.length()));
						m_LineInputArray.get(iLineNumber).setDocAppliedToID((String) clsManageRequestParameters.get_Request_Parameter(sParamName, req).trim());
					}
				}
				if(m_sDocumentType.equalsIgnoreCase(ARDocumentTypes.RETAINAGE_STRING)){
					if (sParamName.contains(ARLineInput.ParamDocAppliedTo)){
						//Strip off the line number
						iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamDocAppliedTo.length(), sParamName.length()));
						m_LineInputArray.get(iLineNumber).setDocAppliedTo((String) clsManageRequestParameters.get_Request_Parameter(sParamName, req).trim());
					}
					if (sParamName.contains(ARLineInput.ParamLineDocAppliedToID)){
						//Strip off the line number
						iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamLineDocAppliedToID.length(), sParamName.length()));
						m_LineInputArray.get(iLineNumber).setDocAppliedToID((String) clsManageRequestParameters.get_Request_Parameter(sParamName, req).trim());
					}
				}
				if(m_sDocumentType.equalsIgnoreCase(ARDocumentTypes.REVERSAL_STRING)){
					if (sParamName.contains(ARLineInput.ParamDocAppliedTo)){
						//Strip off the line number
						iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamDocAppliedTo.length(), sParamName.length()));
						m_LineInputArray.get(iLineNumber).setDocAppliedTo((String) clsManageRequestParameters.get_Request_Parameter(sParamName, req).trim());
					}
					if (sParamName.contains(ARLineInput.ParamLineDocAppliedToID)){
						//Strip off the line number
						iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamLineDocAppliedToID.length(), sParamName.length()));
						m_LineInputArray.get(iLineNumber).setDocAppliedToID((String) clsManageRequestParameters.get_Request_Parameter(sParamName, req).trim());
					}
				}
				
				if (sParamName.contains(ARLineInput.ParamLineApplyCashToChk)){
					//Strip off the line number
					iLineNumber =  Integer.parseInt(sParamName.substring(ARLineInput.ParamLineApplyCashToChk.length(), sParamName.length()));
					m_LineInputArray.get(iLineNumber).setApplyCashToChk((String) clsManageRequestParameters.get_Request_Parameter(sParamName, req).trim());
				}
			}
		} catch (NumberFormatException e) {
			clsServletUtilities.sysprint(this.toString(), "SYSTEM", "Error [1389107799] in AREntryInput - m_iNumberOfLines = " 
				+ m_iNumberOfLines + ", m_LineInputArray.size = " + m_LineInputArray.size());
		}
	    
	    //Remove any lines with a "0.00" amount:
	    remove_zero_amount_lines();
		m_iNumberOfLines = m_LineInputArray.size();
	}
	
    private void remove_zero_amount_lines(){
    	
    	ArrayList<ARLineInput> tempLineArray = new ArrayList<ARLineInput>(0);

    	//Copy the lines into the temporary array:
    	
    	for (int i = 0; i < m_LineInputArray.size(); i++){
    		tempLineArray.add(m_LineInputArray.get(i));
    	}
    	
    	//Now copy back ONLY the non-zero amount lines, UNLESS the user checked them to get their
    	//amounts from the apply to transactions:
    	m_LineInputArray.clear();
    	for (int i = 0; i < tempLineArray.size(); i++){
    		ARLineInput line = tempLineArray.get(i); 
    		//If the line is one the user checked, add it back to the array:
			if (!line.getApplyCashToChk().equalsIgnoreCase("")){
				m_LineInputArray.add(tempLineArray.get(i));
			}else{
				//If it's NOT a line the user checked, if it has a valid amount in it, then
				//add it back into the array:
				if (!line.getAmount().equalsIgnoreCase("0.00")){
					m_LineInputArray.add(tempLineArray.get(i));
	    		}
    		}
    	}
    }
	
	public int getNumberOfLines(){
		return m_LineInputArray.size();
	}
	public void clearErrorMessages(){
		m_sErrorMessageArray.clear();
	}
	public ArrayList<String> getErrorMessages(){
		return m_sErrorMessageArray;
	}
	public String getErrorMessageString(){
		String s = "";
		for (int i = 0; i < m_sErrorMessageArray.size(); i ++){
			s += "\n" + m_sErrorMessageArray.get(i);
		}
		return s;
	}
	public boolean loadToEntry (AREntry entry, ServletContext context, String sDBID){
		
		boolean bEntriesAreValid = true;
		//clearErrorMessages();
		//m_sErrorMessageArray = new ArrayList<String>(0);
		//Set the entry values:
		if(!entry.slid(m_sEntryID)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid entry id - " + m_sEntryID + ".");
		}
		if(!entry.sBatchNumber(m_sBatchNumber)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid batch number - " + m_sBatchNumber + "'.");
		}
		if(!entry.sBatchType(m_sBatchType)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid batch type - " + m_sBatchType + "'.");
		}
		if(!entry.sEntryNumber(m_sEntryNumber)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid entry number - '" + m_sEntryNumber + "'.");
		}
		if(!entry.sDocumentType(m_sDocumentType)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid document type - '" + m_sDocumentType + "'.");
		}
		if(!entry.sCustomerNumber(m_sCustomerNumber)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid customer number - '" + m_sCustomerNumber + "'.");
		}
		if(!entry.sDocNumber(m_sDocNumber.trim())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid document number - '" + m_sDocNumber + "'.");
		}
		if(!entry.sDocDescription(m_sDocDescription.trim())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid document description - '" + m_sDocDescription + "'.");
		}
		if(!entry.datDocDate("MM/dd/yyyy", m_sDocDate)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid document date - '" + m_sDocDate + "'.");
		}
		if(!entry.sTerms(m_sTerms)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid terms - '" + m_sTerms + "'.");
		}
		if(!entry.datDueDate("MM/dd/yyyy", m_sDueDate)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid due date - '" + m_sDueDate + "'.");
		}
		if(!entry.sControlAcct(m_sControlAcct)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid control account - '" + m_sControlAcct + "'.");
		}
		if(!entry.sOrderNumber(m_sOrderNumber.trim())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid order number - '" + m_sOrderNumber + "'.");
		}
		if(!entry.sPONumber(m_sPONumber.trim())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid PO number - '" + m_sPONumber + "'.");
		}
		if (m_sOriginalAmount.equalsIgnoreCase("")){
			m_sOriginalAmount = "0.00";
		}
		if(!entry.sOriginalAmount(m_sOriginalAmount)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid entry amount - '" + m_sOriginalAmount + "'.");
		}
		//Set the sign on the entry:
		if (entry.getDocumentType() == ARDocumentTypes.APPLYTO){
			//Sign is opposite the sign of the entry string
			entry.dOriginalAmount(entry.dOriginalAmount().negate());
		}
		if (entry.getDocumentType() == ARDocumentTypes.CASHADJUSTMENT){
			//Sign is opposite the sign of the entry string
			entry.dOriginalAmount(entry.dOriginalAmount().negate());
		}
		if (entry.getDocumentType() == ARDocumentTypes.CREDIT){
			//Sign is opposite the sign of the entry string
			entry.dOriginalAmount(entry.dOriginalAmount().negate());
		}
		if (entry.getDocumentType() == ARDocumentTypes.CREDITADJUSTMENT){
			//Sign is opposite the sign of the entry string
			entry.dOriginalAmount(entry.dOriginalAmount().negate());
		}
		if (entry.getDocumentType() == ARDocumentTypes.INVOICE){
			//Sign remains the same as the entry string
		}
		if (entry.getDocumentType() == ARDocumentTypes.INVOICEADJUSTMENT){
			//Sign remains the same as the entry string
		}
		if (entry.getDocumentType() == ARDocumentTypes.MISCRECEIPT){
			//Sign is opposite the sign of the entry string
			entry.dOriginalAmount(entry.dOriginalAmount().negate());
		}
		if (entry.getDocumentType() == ARDocumentTypes.PREPAYMENT){
			//Sign is opposite the sign of the entry string
			entry.dOriginalAmount(entry.dOriginalAmount().negate());
		}
		if (entry.getDocumentType() == ARDocumentTypes.RECEIPT){
			//Sign is opposite the sign of the entry string
			entry.dOriginalAmount(entry.dOriginalAmount().negate());
		}
		if (entry.getDocumentType() == ARDocumentTypes.RETAINAGE){
			//Sign remains the same as the entry string
		}
		if (entry.getDocumentType() == ARDocumentTypes.REVERSAL){
			//Sign remains the same as the entry string
		}			

		//Now load the lines:
		//If it's a retainage transaction, the lines are automatically determined:
		if (entry.getDocumentType() == ARDocumentTypes.RETAINAGE){
			ARLine line = new ARLine(entry.sBatchNumber(), entry.sEntryNumber());
			line.dAmount(entry.dOriginalAmount());
			line.sComment("Retainage");
			line.sGLAcct(m_LineInputArray.get(0).getLineAcct());
			line.sDocAppliedToId(m_LineInputArray.get(0).getDocAppliedToID());
			line.sDocAppliedTo(m_LineInputArray.get(0).getDocAppliedTo());
			entry.add_line(line);
		}else{
			for (int i = 0; i < m_LineInputArray.size(); i ++){
				if (entry.sDocumentType().equalsIgnoreCase(ARDocumentTypes.APPLYTO_STRING)){
					//Load the lines as apply-to lines
					if(!loadToApplyToLine(i, entry, context, sDBID)){
						bEntriesAreValid = false;
					}
				}
				if (entry.sDocumentType().equalsIgnoreCase(ARDocumentTypes.RECEIPT_STRING)){
					//Load the lines as cash lines
					if(!loadToCashLine(i, entry, context, sDBID)){
						bEntriesAreValid = false;
					}
				}
				if (entry.sDocumentType().equalsIgnoreCase(ARDocumentTypes.PREPAYMENT_STRING)){
					//Load the lines as prepays:
					if(!loadToPrepayLine(i, entry)){
						bEntriesAreValid = false;
					}
				}
				if (
					entry.sDocumentType().equalsIgnoreCase(ARDocumentTypes.CASHADJUSTMENT_STRING)
					|| entry.sDocumentType().equalsIgnoreCase(ARDocumentTypes.CREDIT_STRING)
					|| entry.sDocumentType().equalsIgnoreCase(ARDocumentTypes.CREDITADJUSTMENT_STRING)
					|| entry.sDocumentType().equalsIgnoreCase(ARDocumentTypes.INVOICE_STRING)
					|| entry.sDocumentType().equalsIgnoreCase(ARDocumentTypes.INVOICEADJUSTMENT_STRING)
					|| entry.sDocumentType().equalsIgnoreCase(ARDocumentTypes.MISCRECEIPT_STRING)
					|| entry.sDocumentType().equalsIgnoreCase(ARDocumentTypes.REVERSAL_STRING)
					){
					//Load any other kind of document:
					if(!loadToLine(i, entry)){
						bEntriesAreValid = false;
					}
				}
			}
		}
		
		//for (int j = 0; j < m_sErrorMessageArray.size(); j ++ ){
		//	System.out.println("\nIn AREntryInput.loadToEntry error message: " + m_sErrorMessageArray.get(j));
		//}
		return bEntriesAreValid;
	}
	
	private boolean loadToLine(int iLineIndex, AREntry entry){

		boolean bEntriesAreValid = true;
		
		ARLine line = new ARLine(entry.sBatchNumber(), entry.sEntryNumber());
		
		if (m_LineInputArray.get(iLineIndex).getAmount().equalsIgnoreCase("")){
			m_LineInputArray.get(iLineIndex).setAmount("0.00");
		}
		
		if (!line.setAmountString(m_LineInputArray.get(iLineIndex).getAmount())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid amount on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getAmount() + "'.");
		}
		
		if (entry.getDocumentType() == ARDocumentTypes.CASHADJUSTMENT){
			//Sign remains the same as the entry string
		}
		if (entry.getDocumentType() == ARDocumentTypes.CREDIT){
			//Sign remains the same as the entry string
		}
		if (entry.getDocumentType() == ARDocumentTypes.CREDITADJUSTMENT){
			//Sign remains the same as the entry string
		}
		if (entry.getDocumentType() == ARDocumentTypes.INVOICE){
			//Sign is opposite the sign of the line string
			line.dAmount(line.dAmount().negate());
		}
		if (entry.getDocumentType() == ARDocumentTypes.INVOICEADJUSTMENT){
			//Sign is opposite the sign of the line string
			line.dAmount(line.dAmount().negate());
		}
		if (entry.getDocumentType() == ARDocumentTypes.MISCRECEIPT){
			//Sign remains the same as the entry string
		}
		if (entry.getDocumentType() == ARDocumentTypes.PREPAYMENT){
			//Sign remains the same as the entry string
		}
		if (entry.getDocumentType() == ARDocumentTypes.RECEIPT){
			//Sign remains the same as the entry string
		}
		if (entry.getDocumentType() == ARDocumentTypes.RETAINAGE){
			//Sign is opposite the sign of the line string
			line.dAmount(line.dAmount().negate());
		}
		if (entry.getDocumentType() == ARDocumentTypes.REVERSAL){
			//Sign remains the same as the entry string
		}			
		
		if (!line.sComment(m_LineInputArray.get(iLineIndex).getComment().trim())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid comment on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getComment() + "'.");
		}
		if (!line.sDescription(m_LineInputArray.get(iLineIndex).getDescription().trim())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid description on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getDescription() + "'.");
		}
		if (!line.sDocAppliedTo(m_LineInputArray.get(iLineIndex).getDocAppliedTo().trim())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid apply-to-doc on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getDocAppliedTo() + "'.");
		}
		if (!line.setApplyToOrderNumber(m_LineInputArray.get(iLineIndex).getApplyToOrderNumber().trim())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid apply-to-order-number on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getApplyToOrderNumber() + "'.");
		}
		if (!line.sDocAppliedToId(m_LineInputArray.get(iLineIndex).getDocAppliedToID())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid doc-applied-to-ID on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getLineAcct() + "'.");
		}
		if (!line.sGLAcct(m_LineInputArray.get(iLineIndex).getLineAcct())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid GL Acct on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getLineAcct() + "'.");
		}
		if (m_LineInputArray.get(iLineIndex).getDocAppliedToID().equalsIgnoreCase("")){
			m_LineInputArray.get(iLineIndex).setDocAppliedToID("-1");
		}
		if (!entry.add_line(line)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Could not add line " + (iLineIndex + 1) + " - '" + entry.getErrorMessage() + "'.");
		}
		return bEntriesAreValid;
	}
	private boolean loadToCashLine(int iLineIndex, AREntry entry, ServletContext context, String sDBID){

		boolean bEntriesAreValid = true;
		
		if (iLineIndex > m_LineInputArray.size() - 1){
			m_sErrorMessageArray.add("Error [1429201637] iLineIndex (" + iLineIndex + ") is out of bounds because line input array size is " 
				+ m_LineInputArray.size() + ".");
			return false;
		}
		
		ARLine line = new ARLine(entry.sBatchNumber(), entry.sEntryNumber());
		
		if (m_LineInputArray.get(iLineIndex).getAmount().equalsIgnoreCase("")){
			m_LineInputArray.get(iLineIndex).setAmount("0.00");
		}
		
		//IF the user has checked the 'chkApplyTo' check box, then get the amount open on the apply-to
		//document, and use THAT as the amount for that line:
		
		if (m_LineInputArray.get(iLineIndex).getApplyCashToChk().equalsIgnoreCase("")){
			if (!line.setAmountString(m_LineInputArray.get(iLineIndex).getAmount())){
				bEntriesAreValid = false;
				m_sErrorMessageArray.add("Invalid amount on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getAmount() + "'.");
			}
		}else{
			ARTransaction trans = new ARTransaction(m_LineInputArray.get(iLineIndex).getDocAppliedToID());
			if (!trans.load(context, sDBID)){
				line.setAmountString("0.00");
			}else{
				BigDecimal bdCurrentAmount = trans.getdCurrentAmount();
				entry.update_distributed_amount();
				//The undistributed amount is a negative number:
				BigDecimal bdEntryRemainingAmount = entry.dUnDistributedAmount().negate();
				//If the current amount on the invoice is GREATER than remaining undistributed amount
				//on the entry, then apply the undistributed amount
				if(bdCurrentAmount.compareTo(bdEntryRemainingAmount) > 0){
					line.setAmountString(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdEntryRemainingAmount));
				}else{
					line.setAmountString(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCurrentAmount));
				}
			}
		}
		
		if (!line.sComment(m_LineInputArray.get(iLineIndex).getComment().trim())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid comment on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getComment() + "'.");
		}
		if (!line.sDescription(m_LineInputArray.get(iLineIndex).getDescription().trim())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid description on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getDescription() + "'.");
		}
		if (!line.sDocAppliedTo(m_LineInputArray.get(iLineIndex).getDocAppliedTo().trim())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid apply-to-doc on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getDocAppliedTo() + "'.");
		}
		if (!line.setApplyToOrderNumber(m_LineInputArray.get(iLineIndex).getApplyToOrderNumber().trim())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid apply-to-order-number on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getApplyToOrderNumber() + "'.");
		}
		if (!line.sDocAppliedToId(m_LineInputArray.get(iLineIndex).getDocAppliedToID())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid doc-applied-to-ID on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getLineAcct() + "'.");
		}
		//If there is a valid apply-to document number AND the apply-to doc ID is -1, that means
		//that the user entered a document manually, and we need to look up the doc ID for that
		//document:
		if (
			line.sDocAppliedTo().trim().compareToIgnoreCase("") != 0
			&& line.sDocAppliedTo().trim().compareToIgnoreCase("UNAPPLIED") != 0
		){
			if (line.sDocAppliedToId().compareToIgnoreCase("-1") == 0){
				//Look up the document ID:
				ARTransaction trans = new ARTransaction();
				if(!trans.load(
					m_sCustomerNumber, 
					line.sDocAppliedTo(),
					context, 
					sDBID)
				){
					bEntriesAreValid = false;
					m_sErrorMessageArray.add("Could not load apply-to doc " + line.sDocAppliedTo()
						+ " for customer " + m_sCustomerNumber
						+ " on line " + (iLineIndex + 1) + ".");
				}else{
					line.sDocAppliedToId(trans.getsTransactionID());
				}
			}
		}
		
		if (!line.sGLAcct(m_LineInputArray.get(iLineIndex).getLineAcct())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid GL Acct on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getLineAcct() + "'.");
		}
		if (m_LineInputArray.get(iLineIndex).getDocAppliedToID().equalsIgnoreCase("")){
			m_LineInputArray.get(iLineIndex).setDocAppliedToID("-1");
		}
		if (!entry.add_line(line)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Could not add line " + (iLineIndex + 1) + " - '" + entry.getErrorMessage() + "'.");
		}
		return bEntriesAreValid;
	}
	private boolean loadToApplyToLine(int iLineIndex, AREntry entry, ServletContext context, String sDBID){

		boolean bEntriesAreValid = true;
		
		ARLine line = new ARLine(entry.sBatchNumber(), entry.sEntryNumber());
		
		if (m_LineInputArray.get(iLineIndex).getAmount().equalsIgnoreCase("")){
			m_LineInputArray.get(iLineIndex).setAmount("0.00");
		}
		
		//IF the user has checked the 'chkApplyTo' check box, then get the amount open on the apply-to
		//document, and use THAT as the amount for that line:
		if (m_LineInputArray.get(iLineIndex).getApplyCashToChk().equalsIgnoreCase("")){
			if (!line.setAmountString(m_LineInputArray.get(iLineIndex).getAmount())){
				bEntriesAreValid = false;
				m_sErrorMessageArray.add("Invalid amount on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getAmount() + "'.");
			}
		}else{
			ARTransaction trans = new ARTransaction(m_LineInputArray.get(iLineIndex).getDocAppliedToID());
			if (!trans.load(context, sDBID)){
				line.setAmountString("0.00");
			}else{
				line.setAmountString(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(trans.getdCurrentAmount()));
			}
		}
		
		//Now calculate the distributed/undistributed amount:
		entry.update_distributed_amount();
		
		if (!line.sComment(m_LineInputArray.get(iLineIndex).getComment().trim())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid comment on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getComment() + "'.");
		}
		if (!line.sDescription(m_LineInputArray.get(iLineIndex).getDescription().trim())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid description on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getDescription() + "'.");
		}
		if (!line.sDocAppliedTo(m_LineInputArray.get(iLineIndex).getDocAppliedTo().trim())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid apply-to-doc on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getDocAppliedTo() + "'.");
		}
		if (!line.setApplyToOrderNumber(m_LineInputArray.get(iLineIndex).getApplyToOrderNumber().trim())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid apply-to-order-number on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getApplyToOrderNumber() + "'.");
		}
		if (!line.sDocAppliedToId(m_LineInputArray.get(iLineIndex).getDocAppliedToID())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid doc-applied-to-ID on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getLineAcct() + "'.");
		}

		//Here we set the GL Acct.  If this is a line with a manually entered doc number, the GL Acct
		//will be blank, and we'll update it below, after we read the transaction:
		if (!line.sGLAcct(m_LineInputArray.get(iLineIndex).getLineAcct())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid GL Acct on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getLineAcct() + "'.");
		}

		//If there is a valid apply-to document number AND the apply-to doc ID is -1, that means
		//that the user entered a document manually, and we need to look up the doc ID and GL 
		//for that document:
		if (
			line.sDocAppliedTo().trim().compareToIgnoreCase("") != 0
			&& line.sDocAppliedTo().trim().compareToIgnoreCase("UNAPPLIED") != 0
		){
			if (line.sDocAppliedToId().compareToIgnoreCase("-1") == 0){
				//Look up the document ID:
				ARTransaction trans = new ARTransaction();
				if(!trans.load(
					m_sCustomerNumber, 
					line.sDocAppliedTo(),
					context, 
					sDBID)
				){
					bEntriesAreValid = false;
					m_sErrorMessageArray.add("Could not load apply-to doc " + line.sDocAppliedTo()
						+ " for customer " + m_sCustomerNumber
						+ " on line " + (iLineIndex + 1) + ".");
				}else{
					line.sDocAppliedToId(trans.getsTransactionID());
					ARCustomer cust = new ARCustomer(m_sCustomerNumber);
					if (!cust.load(context, sDBID)){
						bEntriesAreValid = false;
						m_sErrorMessageArray.add("Could not load customer to check apply-to doc " + line.sDocAppliedTo()
							+ " for customer " + m_sCustomerNumber
							+ " on line " + (iLineIndex + 1) + ".");
					}else{
						String sGLAcct = "";
						if (trans.getiDocType() == ARDocumentTypes.PREPAYMENT){
							//If it's a prepay, then the GL Acct is the Customer Deposit account:
							sGLAcct = cust.getARPrepayLiabilityAccount(context, sDBID);
						}else{
							//If it's NOT a prepay, then the GL acct is just the AR account:
							sGLAcct = cust.getARControlAccount(context, sDBID);
						}
						line.sGLAcct(sGLAcct);
					}
				}
			}
		}
		if (m_LineInputArray.get(iLineIndex).getDocAppliedToID().equalsIgnoreCase("")){
			m_LineInputArray.get(iLineIndex).setDocAppliedToID("-1");
		}
		if (!entry.add_line(line)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Could not add line " + (iLineIndex + 1) + " - '" + entry.getErrorMessage() + "'.");
		}
		return bEntriesAreValid;
	}

	private boolean loadToPrepayLine(int iLineIndex, AREntry entry){

		boolean bEntriesAreValid = true;
		
		ARLine line = new ARLine(entry.sBatchNumber(), entry.sEntryNumber());
		
		if (m_LineInputArray.get(iLineIndex).getAmount().equalsIgnoreCase("")){
			m_LineInputArray.get(iLineIndex).setAmount("0.00");
		}
		
		if (!line.setAmountString(m_LineInputArray.get(iLineIndex).getAmount())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid amount on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getAmount() + "'.");
		}
		
		if (!line.sComment(m_LineInputArray.get(iLineIndex).getComment().trim())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid comment on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getComment() + "'.");
		}
		if (!line.sDescription(m_LineInputArray.get(iLineIndex).getDescription().trim())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid description on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getDescription() + "'.");
		}
		if (!line.sDocAppliedTo("UNAPPLIED")){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid apply-to-doc on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getDocAppliedTo() + "'.");
		}
		if (!line.setApplyToOrderNumber(m_LineInputArray.get(iLineIndex).getApplyToOrderNumber().trim())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid apply-to-order-number on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getApplyToOrderNumber() + "'.");
		}
		entry.sOrderNumber(m_LineInputArray.get(iLineIndex).getApplyToOrderNumber());
		if (!line.sDocAppliedToId("-1")){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid doc-applied-to-ID on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getLineAcct() + "'.");
		}
		if (!line.sGLAcct(m_LineInputArray.get(iLineIndex).getLineAcct())){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid GL Acct on line " + (iLineIndex + 1) + " - '" + m_LineInputArray.get(iLineIndex).getLineAcct() + "'.");
		}
		if (m_LineInputArray.get(iLineIndex).getDocAppliedToID().equalsIgnoreCase("")){
			m_LineInputArray.get(iLineIndex).setDocAppliedToID("-1");
		}
		if (!entry.add_line(line)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Could not add line " + (iLineIndex + 1) + " - '" + entry.getErrorMessage() + "'.");
		}
		return bEntriesAreValid;
	}

	public boolean loadFromEntry(AREntry entry){
		
		m_sEntryID = entry.slid();
		m_sBatchNumber = entry.sBatchNumber();
		m_sEntryNumber = entry.sEntryNumber();
		m_sDocumentType = entry.sDocumentType();
		m_sCustomerNumber = entry.sCustomerNumber();
		m_sDocNumber = entry.sDocNumber();
		m_sDocDescription = entry.sDocDescription();
		m_sDocDate = entry.sStdDocDate();
		m_sTerms = entry.sTerms();
		m_sDueDate = entry.sStdDueDate();
		m_sControlAcct = entry.sControlAcct();
		m_sOrderNumber = entry.sOrderNumber();
		m_sPONumber = entry.sPONumber();
		
		if (entry.getDocumentType() == ARDocumentTypes.APPLYTO){
			//Sign is opposite the sign of the entry string
			m_sOriginalAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(entry.dOriginalAmount().negate());
		}
		if (entry.getDocumentType() == ARDocumentTypes.CASHADJUSTMENT){
			//Sign is opposite the sign of the entry string
			m_sOriginalAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(entry.dOriginalAmount().negate());
		}
		if (entry.getDocumentType() == ARDocumentTypes.CREDIT){
			//Sign is opposite the sign of the entry string
			m_sOriginalAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(entry.dOriginalAmount().negate());
		}
		if (entry.getDocumentType() == ARDocumentTypes.CREDITADJUSTMENT){
			//Sign is opposite the sign of the entry string
			m_sOriginalAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(entry.dOriginalAmount().negate());
		}
		if (entry.getDocumentType() == ARDocumentTypes.INVOICE){
			//Sign is same as the sign of the entry string
			m_sOriginalAmount = entry.sOriginalAmountSTDFormat();
		}
		if (entry.getDocumentType() == ARDocumentTypes.INVOICEADJUSTMENT){
			//Sign is same as the sign of the entry string
			m_sOriginalAmount = entry.sOriginalAmountSTDFormat();
		}
		if (entry.getDocumentType() == ARDocumentTypes.MISCRECEIPT){
			//Sign is opposite the sign of the entry string
			m_sOriginalAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(entry.dOriginalAmount().negate());
		}
		if (entry.getDocumentType() == ARDocumentTypes.PREPAYMENT){
			//Sign is opposite the sign of the entry string
			m_sOriginalAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(entry.dOriginalAmount().negate());
		}
		if (entry.getDocumentType() == ARDocumentTypes.RECEIPT){
			//Sign is opposite the sign of the entry string
			m_sOriginalAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(entry.dOriginalAmount().negate());
		}
		if (entry.getDocumentType() == ARDocumentTypes.RETAINAGE){
			//Sign is same as the sign of the entry string
			m_sOriginalAmount = entry.sOriginalAmountSTDFormat();
		}
		if (entry.getDocumentType() == ARDocumentTypes.REVERSAL){
			//Sign is same as the sign of the entry string
			m_sOriginalAmount = entry.sOriginalAmountSTDFormat();
		}

		m_sUndistributedAmount = entry.sUnDistributedAmountSTDFormat();
		m_sBatchType = entry.sBatchType();
		m_LineInputArray = new ArrayList<ARLineInput> (0);
		
		for (int i = 0; i < entry.iLastLine(); i++){
			ARLineInput lineInput = new ARLineInput();
			ARLine line = entry.getLineByIndex(i);
			
			if (entry.getDocumentType() == ARDocumentTypes.APPLYTO){
				//Sign is same as the sign of the entry string
				lineInput.setAmount(line.sAmountSTDFormat());
			}
			//Set the signs, based on the entry type:
			if (entry.getDocumentType() == ARDocumentTypes.CASHADJUSTMENT){
				//Sign is same as the sign of the entry string
				lineInput.setAmount(line.sAmountSTDFormat());
			}
			if (entry.getDocumentType() == ARDocumentTypes.CREDIT){
				//Sign is same as the sign of the entry string
				lineInput.setAmount(line.sAmountSTDFormat());
			}
			if (entry.getDocumentType() == ARDocumentTypes.CREDITADJUSTMENT){
				//Sign is same as the sign of the entry string
				lineInput.setAmount(line.sAmountSTDFormat());
			}
			if (entry.getDocumentType() == ARDocumentTypes.INVOICE){
				//Sign is opposite the sign of the entry string
				lineInput.setAmount(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(line.dAmount().negate()));
			}
			if (entry.getDocumentType() == ARDocumentTypes.INVOICEADJUSTMENT){
				//Sign is opposite the sign of the entry string
				lineInput.setAmount(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(line.dAmount().negate()));
			}
			if (entry.getDocumentType() == ARDocumentTypes.MISCRECEIPT){
				//Sign is same as the sign of the entry string
				lineInput.setAmount(line.sAmountSTDFormat());
			}
			if (entry.getDocumentType() == ARDocumentTypes.PREPAYMENT){
				//Sign is same as the sign of the entry string
				lineInput.setAmount(line.sAmountSTDFormat());
			}
			if (entry.getDocumentType() == ARDocumentTypes.RECEIPT){
				//Sign is same as the sign of the entry string
				lineInput.setAmount(line.sAmountSTDFormat());
			}
			if (entry.getDocumentType() == ARDocumentTypes.RETAINAGE){
				//Sign is opposite the sign of the entry string
				lineInput.setAmount(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(line.dAmount().negate()));
			}
			if (entry.getDocumentType() == ARDocumentTypes.REVERSAL){
				//Sign is same as the sign of the entry string
				lineInput.setAmount(line.sAmountSTDFormat());
			}
			
			lineInput.setApplyToOrderNumber(line.getApplyToOrderNumber());
			lineInput.setComment(line.sComment());
			lineInput.setDescription(line.sDescription());
			lineInput.setDocAppliedTo(line.sDocAppliedTo());
			lineInput.setDocAppliedToID(line.sDocAppliedToId());
			lineInput.setLineAcct(line.sGLAcct());
			lineInput.setLineID(line.sId());
			lineInput.setEntryID(entry.slid());
			m_LineInputArray.add(lineInput);
		}
		
		m_iNumberOfLines = m_LineInputArray.size();
		m_sErrorMessageArray = new ArrayList<String> (0);
		return true;
	}
	public String getsEntryID(){
		return m_sEntryID;
	}
	public void setEntryID(String sEntryID){
		m_sEntryID = sEntryID.trim();
	}
	public String getsBatchNumber(){
		return m_sBatchNumber;
	}
	public void setBatchNumber(String sBatchNumber){
		m_sBatchNumber = sBatchNumber.trim();
	}
	public String getsEntryNumber(){
		return m_sEntryNumber;
	}
	public void setEntryNumber(String sEntryNumber){
		m_sEntryNumber = sEntryNumber.trim();
	}
	public String getsDocumentType(){
		return m_sDocumentType;
	}
	public void setDocumentType(String sDocumentType){
		m_sDocumentType = sDocumentType.trim();
	}
	public String getsCustomerNumber(){
		return m_sCustomerNumber;
	}
	public void setCustomerNumber(String sCustomerNumber){
		m_sCustomerNumber = sCustomerNumber.trim();
	}
	public String getsDocNumber(){
		return m_sDocNumber;
	}
	public void setDocNumber(String sDocNumber){
		m_sDocNumber = sDocNumber.trim();
	}
	public String getsDocDescription(){
		return m_sDocDescription;
	}
	public void setDocDescription(String sDocDescription){
		m_sDocDescription = sDocDescription.trim();
	}
	public String getsDocDate(){
		return m_sDocDate;
	}
	public void setDocDate(String sDocDate){
		m_sDocDate = sDocDate;
	}
	public String getsTerms(){
		return m_sTerms;
	}
	public void setTerms(String sTerms){
		m_sTerms = sTerms.trim();
	}
	public String getsDueDate(){
		return m_sDueDate;
	}
	public void setDueDate(String sDueDate){
		m_sDueDate = sDueDate;
	}
	public String getsControlAcct(){
		return m_sControlAcct;
	}
	public void setControlAcct(String sControlAcct){
		m_sControlAcct = sControlAcct.trim();
	}
	public String getsOrderNumber(){
		return m_sOrderNumber;
	}
	public void setOrderNumber(String sOrderNumber){
		m_sOrderNumber = sOrderNumber.trim();
	}
	public String getsPONumber(){
		return m_sPONumber;
	}
	public void setPONumber(String sPONumber){
		m_sPONumber = sPONumber.trim();
	}
	public String getsOriginalAmount(){
		return m_sOriginalAmount;
	}
	public void setOriginalAmount(String sOriginalAmount){
		m_sOriginalAmount = sOriginalAmount;
	}
	public String getsUndistributedAmount(){
		return m_sUndistributedAmount;
	}
	public void setUndistributedAmount(String sUndistributedAmount){
		m_sUndistributedAmount = sUndistributedAmount;
	}
	public String getsBatchType(){
		return m_sBatchType;
	}
	public void setBatchType(String sBatchType){
		m_sBatchType = sBatchType.trim();
	}
	public ARLineInput getLine (int iLineIndex){
		return m_LineInputArray.get(iLineIndex);
	}
	public int getLineCount (){
		return m_LineInputArray.size();
	}
	public void addLine (ARLineInput line){
		m_LineInputArray.add(line);
	}
	public String getQueryString(){
	
		String sQueryString = "";
		sQueryString += ParamEntryID + "=" + clsServletUtilities.URLEncode(m_sEntryID);
		sQueryString += "&" + ParamBatchNumber + "=" + clsServletUtilities.URLEncode(m_sBatchNumber);
		sQueryString += "&" + ParamEntryNumber + "=" + clsServletUtilities.URLEncode(m_sEntryNumber);
		sQueryString += "&" + ParamDocumentType + "=" + clsServletUtilities.URLEncode(m_sDocumentType);
		sQueryString += "&" + ParamCustomerNumber + "=" + clsServletUtilities.URLEncode(m_sCustomerNumber);
		sQueryString += "&" + ParamDocNumber + "=" + clsServletUtilities.URLEncode(m_sDocNumber);
		sQueryString += "&" + ParamDocDescription + "=" + clsServletUtilities.URLEncode(m_sDocDescription);
		sQueryString += "&" + ParamDocDate + "=" + clsServletUtilities.URLEncode(m_sDocDate);
		
		//sQueryString += "&" + ParamDocDateYear + "=" 
		//	+ ServletUtilities.ServletUtilities.URLEncode(SMUtilities.StringLeft(m_sDocDate,4));
		//sQueryString += "&" + ParamDocDateMonth + "=" 
		//	+ ServletUtilities.ServletUtilities.URLEncode(SMUtilities.StringRight(SMUtilities.StringLeft(m_sDocDate,7),2));
		//sQueryString += "&" + ParamDocDateDay + "=" 
		//	+ ServletUtilities.ServletUtilities.URLEncode(SMUtilities.StringRight(m_sDocDate,2));
		sQueryString += "&" + ParamTerms + "=" + clsServletUtilities.URLEncode(m_sTerms);
		sQueryString += "&" + ParamDueDate + "=" + clsServletUtilities.URLEncode(m_sDueDate);
		sQueryString += "&" + ParamControlAcct + "=" + clsServletUtilities.URLEncode(m_sControlAcct);
		sQueryString += "&" + ParamOrderNumber + "=" + clsServletUtilities.URLEncode(m_sOrderNumber);
		sQueryString += "&" + ParamPONumber + "=" + clsServletUtilities.URLEncode(m_sPONumber);
		sQueryString += "&" + ParamOriginalAmount + "=" + clsServletUtilities.URLEncode(m_sOriginalAmount);
		sQueryString += "&" + ParamUndistributedAmount + "=" + clsServletUtilities.URLEncode(m_sUndistributedAmount);
		sQueryString += "&" + ParamBatchType + "=" + clsServletUtilities.URLEncode(m_sBatchType);
		sQueryString += "&" + ParamNumberOfLines + "=" + clsServletUtilities.URLEncode(Integer.toString(m_iNumberOfLines));
		
		for (int i = 0; i < m_LineInputArray.size(); i ++){
			sQueryString += "&" + ARLineInput.ParamDistAcct + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "=" + clsServletUtilities.URLEncode(m_LineInputArray.get(i).getLineAcct());
			sQueryString += "&" + ARLineInput.ParamDocAppliedTo + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "=" + clsServletUtilities.URLEncode(m_LineInputArray.get(i).getDocAppliedTo());
			sQueryString += "&" + ARLineInput.ParamLineAmt + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "=" + clsServletUtilities.URLEncode(m_LineInputArray.get(i).getAmount());
			sQueryString += "&" + ARLineInput.ParamLineApplyToOrderNumber + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "=" + clsServletUtilities.URLEncode(m_LineInputArray.get(i).getApplyToOrderNumber());
			sQueryString += "&" + ARLineInput.ParamLineComment + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "=" + clsServletUtilities.URLEncode(m_LineInputArray.get(i).getComment());
			sQueryString += "&" + ARLineInput.ParamLineDesc + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "=" + clsServletUtilities.URLEncode(m_LineInputArray.get(i).getDescription());
			sQueryString += "&" + ARLineInput.ParamLineDocAppliedToID + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "=" + clsServletUtilities.URLEncode(m_LineInputArray.get(i).getDocAppliedToID());
			sQueryString += "&" + ARLineInput.ParamLineID + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "=" + clsServletUtilities.URLEncode(m_LineInputArray.get(i).getLineID());
			sQueryString += "&" + ARLineInput.ParamLineApplyCashToChk + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "=" + clsServletUtilities.URLEncode(m_LineInputArray.get(i).getApplyCashToChk());
		}
		
		return sQueryString;
	}
	public String getDataDump(){
	
		String s = "";
		s += ParamEntryID + "=" + clsServletUtilities.URLEncode(m_sEntryID);
		s += "\n" + ParamBatchNumber + "=" + clsServletUtilities.URLEncode(m_sBatchNumber);
		s += "\n" + ParamEntryNumber + "=" + clsServletUtilities.URLEncode(m_sEntryNumber);
		s += "\n" + ParamDocumentType + "=" + clsServletUtilities.URLEncode(m_sDocumentType);
		s += "\n" + ParamCustomerNumber + "=" + clsServletUtilities.URLEncode(m_sCustomerNumber);
		s += "\n" + ParamDocNumber + "=" + clsServletUtilities.URLEncode(m_sDocNumber);
		s += "\n" + ParamDocDescription + "=" + clsServletUtilities.URLEncode(m_sDocDescription);
		s += "\n" + ParamDocDate + "=" + clsServletUtilities.URLEncode(m_sDocDate);
		s += "\n" + ParamTerms + "=" + clsServletUtilities.URLEncode(m_sTerms);
		s += "\n" + ParamDueDate + "=" + clsServletUtilities.URLEncode(m_sDueDate);
		s += "\n" + ParamControlAcct + "=" + clsServletUtilities.URLEncode(m_sControlAcct);
		s += "\n" + ParamOrderNumber + "=" + clsServletUtilities.URLEncode(m_sOrderNumber);
		s += "\n" + ParamPONumber + "=" + clsServletUtilities.URLEncode(m_sPONumber);
		s += "\n" + ParamOriginalAmount + "=" + clsServletUtilities.URLEncode(m_sOriginalAmount);
		s += "\n" + ParamUndistributedAmount + "=" + clsServletUtilities.URLEncode(m_sUndistributedAmount);
		s += "\n" + ParamBatchType + "=" + clsServletUtilities.URLEncode(m_sBatchType);
		s += "\n" + ParamNumberOfLines + "=" + clsServletUtilities.URLEncode(Integer.toString(m_iNumberOfLines));
		
		for (int i = 0; i < m_LineInputArray.size(); i ++){
			s += "\n" + ARLineInput.ParamDistAcct + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "=" + clsServletUtilities.URLEncode(m_LineInputArray.get(i).getLineAcct());
			s += "\n" + ARLineInput.ParamDocAppliedTo + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "=" + clsServletUtilities.URLEncode(m_LineInputArray.get(i).getDocAppliedTo());
			s += "\n" + ARLineInput.ParamLineAmt + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "=" + clsServletUtilities.URLEncode(m_LineInputArray.get(i).getAmount());
			s += "\n" + ARLineInput.ParamLineApplyToOrderNumber + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "=" + clsServletUtilities.URLEncode(m_LineInputArray.get(i).getApplyToOrderNumber());
			s += "\n" + ARLineInput.ParamLineComment + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "=" + clsServletUtilities.URLEncode(m_LineInputArray.get(i).getComment());
			s += "\n" + ARLineInput.ParamLineDesc + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "=" + clsServletUtilities.URLEncode(m_LineInputArray.get(i).getDescription());
			s += "\n" + ARLineInput.ParamLineDocAppliedToID + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "=" + clsServletUtilities.URLEncode(m_LineInputArray.get(i).getDocAppliedToID());
			s += "\n" + ARLineInput.ParamLineID + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "=" + clsServletUtilities.URLEncode(m_LineInputArray.get(i).getLineID());
			s += "\n" + ARLineInput.ParamLineApplyCashToChk + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "=" + clsServletUtilities.URLEncode(m_LineInputArray.get(i).getApplyCashToChk());
		}
		return s;
	}
}