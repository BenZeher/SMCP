package smar;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.io.PrintWriter;
import javax.servlet.ServletContext;

import SMClasses.MySQLs;
import SMClasses.SMBatchTypes;
import SMDataDefinition.SMTableartransactions;
import SMDataDefinition.SMTablearterms;
import SMDataDefinition.SMTableglaccounts;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsStringFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTableentries;

public class ARCreateEntryForm {

	//We'll use these to store the GL List, so we don't have to load it several times:
    private static ArrayList<String> m_sGLValues = new ArrayList<String>();
    private static ArrayList<String> m_sGLDescriptions = new ArrayList<String>();
    private static String sDefaultARControlAcct = "";
    private static String sDefaultPrepayAcct = "";
    private static String sDefaultCashAcct = "";
    
	public static boolean createFormFromAREntryInput(
			PrintWriter pwOut,
			boolean bEditable,
			AREntryInput entryInput,
			ServletContext context,
			String sDBID,
			String sCallingClass,
			String sApplyToDocNumber,
			String sApplyToDocID
	){
		
		//Include the date picker java code:
		pwOut.println(SMUtilities.getDatePickerIncludeString(context));
		
	    //Start the entry edit form:
		pwOut.println("<FORM NAME='ENTRYEDIT' ACTION='" + SMUtilities.getURLLinkBase(context) + "smar.AREntryUpdate' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		//Record the hidden fields for the entry edit form:
	    storeHiddenFieldsOnForm (pwOut, bEditable, entryInput, sCallingClass, sApplyToDocID);
        //Display the entry header fields:
	    displayEntryHeaderFields (pwOut, bEditable, entryInput, context, sDBID);
	    if (!loadGLList(context, sDBID)){
	    	//System.out.println("In ARCreateentryForm - error loading GL list");
	    	return false;
	    }
	    
	    //Get the default GLs here:
	    if (entryInput.getsCustomerNumber().compareToIgnoreCase("") != 0){
	    	ARCustomer cus = new ARCustomer (entryInput.getsCustomerNumber());
	    	sDefaultARControlAcct = cus.getARControlAccount(context, sDBID);
	    	sDefaultPrepayAcct = cus.getARPrepayLiabilityAccount(context, sDBID);
	        sDefaultCashAcct = cus.getCashAccount(context, sDBID);
	    }
        if (bEditable){
        	displayEditableEntryFields (
        		pwOut, 
        		bEditable, 
        		entryInput, 
        		sApplyToDocNumber, 
        		sApplyToDocID,
        		context, 
        		sDBID);
        }
        
        //Else, if the record is NOT editable:
        else{
        	displayNonEditableEntryFields (pwOut, entryInput, sApplyToDocNumber, sApplyToDocID, sDBID, context);
        }
	    //Now display the transaction lines:
	    //If it's not editable, just show the current applied lines:
	    if (! bEditable){
	    	pwOut.println("<B>Line distribution:</B><BR>");
	    	Display_NONEditable_Lines(pwOut, entryInput, context, sDBID);
	    }else{
	    	if(entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RETAINAGE_STRING)){
	    		//pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamNumberOfLines + "\" VALUE=1>");
	    		//Add the values for the lines here, but we don't display them:
	    		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamNumberOfLines + "\" VALUE=1>");
	    		ARLineInput line = entryInput.getLine(0);
	    		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARLineInput.ParamLineID
	    				+ clsStringFunctions.PadLeft("0", "0", 6) + "\" VALUE='" + line.getLineID() + "'>");
	    		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARLineInput.ParamDocAppliedTo
	    				+ clsStringFunctions.PadLeft("0", "0", 6) + "\" VALUE='" + line.getDocAppliedTo() + "'>");
	    		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARLineInput.ParamDistAcct
	    				+ clsStringFunctions.PadLeft("0", "0", 6) + "\" VALUE='" + line.getLineAcct() + "'>");
	    		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARLineInput.ParamLineDesc
	    				+ clsStringFunctions.PadLeft("0", "0", 6) + "\" VALUE='" + line.getDescription() + "'>");
	    		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARLineInput.ParamLineAmt
	    				+ clsStringFunctions.PadLeft("0", "0", 6) + "\" VALUE='" + line.getAmount() + "'>");
	    		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARLineInput.ParamLineComment
	    				+ clsStringFunctions.PadLeft("0", "0", 6) + "\" VALUE='" + line.getComment() + "'>");
	    		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARLineInput.ParamLineDocAppliedToID
	    				+ clsStringFunctions.PadLeft("0", "0", 6) + "\" VALUE='" + line.getDocAppliedToID() + "'>");
	    		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARLineInput.ParamLineApplyToOrderNumber
	    				+ clsStringFunctions.PadLeft("0", "0", 6) + "\" VALUE='" + line.getApplyToOrderNumber() + "'>");
	    		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARLineInput.ParamLineApplyCashToChk
	    				+ clsStringFunctions.PadLeft("0", "0", 6) + "\" VALUE='" + line.getApplyCashToChk() + "'>");
	    	}else{
	    		pwOut.println("<B>Line distribution:</B><BR>");
		        //Display the line header:
			    Display_Line_Header(pwOut, entryInput);
	
			    //Display all the current transaction lines:
			    if (!displayTransactionLines(
			    		pwOut, 
			    		entryInput, 
			    		sApplyToDocNumber, 
			    		sApplyToDocID,
			    		context,
			    		sDBID
			    		)){
			    	//System.out.println("In ARCreateentryForm - error displaying transaction lines");
			    	return false;
			    }	    
	    	}
			    pwOut.println("</TABLE>");
	    }
	    if (bEditable){
	    	addCommandButtons(entryInput, pwOut);
	    }
	    //End the entry edit form:
	    pwOut.println("</FORM>");  

		return true;
	}
	private static void storeHiddenFieldsOnForm(
			PrintWriter pwOut,
			boolean bEditable,
			AREntryInput entryInput,
			String sCallingClass,
			String sApplyToDocumentID
	){
		
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamBatchNumber + "\" VALUE='" + entryInput.getsBatchNumber() + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamEntryNumber + "\" VALUE='" + entryInput.getsEntryNumber() + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamCustomerNumber + "\" VALUE='" + entryInput.getsCustomerNumber() + "'>");
	    String sEditable;
	    if (bEditable){
	    	sEditable = "Yes";
	    }else{
	    	sEditable = "No";
	    }
	    pwOut.println("<INPUT TYPE=HIDDEN NAME=\"Editable\" VALUE='" + sEditable + "'>");
	    pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamEntryID + "\" VALUE='" + entryInput.getsEntryID() + "'>");
	    pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamBatchType + "\" VALUE='" + entryInput.getsBatchType() + "'>");
	    pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamDocumentType + "\" VALUE='" + entryInput.getsDocumentType() + "'>");
	    pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE='" + sCallingClass + "'>");
	    pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + "DocumentID" + "\" VALUE='" + sApplyToDocumentID + "'>");
	}
	private static void displayEntryHeaderFields (
			PrintWriter pwOut,
			boolean bEditable,
			AREntryInput entryInput,
			ServletContext context,
			String sDBID
	){
		
		int iBatchType = -1;
		try {
			iBatchType = Integer.parseInt(entryInput.getsBatchType());
			pwOut.println("<B>" + SMBatchTypes.Get_Batch_Type(iBatchType) + "</B>");
		} catch (NumberFormatException e) {
			pwOut.println("<B>(Error reading batch type - " + e.getMessage() + ")</B>");
		}
		
		pwOut.println(" batch number: <B>" + entryInput.getsBatchNumber() + "</B>;");
		//Get the batch total:
		ARBatch batch = new ARBatch(entryInput.getsBatchNumber());
		pwOut.println(" batch total: <B>" + batch.sTotalAmount(context, sDBID) + "</B>;");
		
        if (entryInput.getsEntryNumber().equalsIgnoreCase("-1")){
        	pwOut.println(" entry number: <B>NEW</B>.  ");
        }else{
        	pwOut.println(" entry number: <B>" + entryInput.getsEntryNumber() + "</B>.  ");
        }
        
        //If the entry has a customer number, display the number and name:
		//Get the customer name here:
        String sCustomerNumber = "";
        String sCustomerName = "";
	    if (entryInput.getsCustomerNumber().equalsIgnoreCase("")){
	    	sCustomerNumber = "";
	    	sCustomerName = "";
	    }else{
	    	sCustomerNumber = entryInput.getsCustomerNumber();
	    	ARCustomer m_Customer = new ARCustomer(entryInput.getsCustomerNumber());
			if (! m_Customer.load(context, sDBID)){
				sCustomerName = "";
			}else{
				sCustomerName = m_Customer.getM_sCustomerName();
				
				//If it's a new entry, set the default control account:
				if (entryInput.getsEntryNumber().equalsIgnoreCase("-1")){
					//If it's a cash entry, set the control account to the customer's default cash account:
					if (
							(entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RECEIPT_STRING))
							|| (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.PREPAYMENT_STRING))
						){
						String sCashAcct = m_Customer.getCashAccount(context, sDBID);
						//If there is no cash account, we have to leave the control acct in the class
						//alone, since the user sould have selected one and we want to keep that:
						if (sCashAcct.compareToIgnoreCase("") != 0){
							entryInput.setControlAcct(m_Customer.getCashAccount(context, sDBID));
						}
					}else{
						//If it's a retainage entry, the control account should be the retainage acct:
						if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RETAINAGE_STRING)){
							entryInput.setControlAcct(m_Customer.getARRetainageAccount(context, sDBID));
						}else{
							entryInput.setControlAcct(m_Customer.getARControlAccount(context, sDBID));
						}
					}
				}
			}
	    }
    	pwOut.println("For customer: " + "<B>" + sCustomerNumber);
    	
        //Get the customer name:
    	pwOut.println(" - " 
        		+ sCustomerName
        		+ "</B><BR>");

	    pwOut.println(" Document type: <B>" 
        		+ ARDocumentTypes.Get_Document_Type_Label(
        				Integer.parseInt(entryInput.getsDocumentType()))
        		+ "</B>.  ");

	    if (bEditable){
	    	addCommandButtons(entryInput, pwOut);
        }
	}
	private static void displayEditableEntryFields(
			PrintWriter pwOut,
			boolean bEditable,
			AREntryInput entryInput,
			String sApplyToDoc,
			String sApplyToID,
			ServletContext context,
			String sDBID
	){
		
		pwOut.println("<TABLE BORDER=1 WIDTH=100% CELLSPACING=2 style=\"font-size:75%\">");
		
        //START ROW 1
		pwOut.println("<TR>");
        
        //Doc Number:
		pwOut.println("<TD>");
		pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Field(
        		AREntryInput.ParamDocNumber, 
        		clsStringFunctions.filter(entryInput.getsDocNumber()), 
        		SMTableentries.sdocnumberLength, 
        		"Doc. #:&nbsp;", 
        		"",
        		16
        		)
        );
		pwOut.println("</TD>");

        //Doc date:
		pwOut.println("<TD>");

		pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Field(
        		AREntryInput.ParamDocDate, 
        		clsStringFunctions.filter(entryInput.getsDocDate()), 
        		10, 
        		"Doc&nbsp;date:", 
        		"",
        		8
        		)
        );
		
		//Date picker:
		pwOut.println(SMUtilities.getDatePickerString(AREntryInput.ParamDocDate,context));

		/*
		 * Stopped using drop downs for dates:
		pwOut.println(ARUtilities.Create_Edit_Form_Date_Input_Field(
        		AREntryInput.ParamDocDateYear, 
        		AREntryInput.ParamDocDateMonth, 
        		AREntryInput.ParamDocDateDay, 
        		ARUtilities.DateToCalendar(
        				ARUtilities.StringToSQLDate("yyyy-MM-dd",entryInput.getsDocDate())), 
        		"Doc. date: ", 
        		"")
        		);
        */
		pwOut.println("</TD>");

        //Entry amount:
		//If it's a reversal, the amount CANNOT be edited, because we need to reverse the full amount:
		pwOut.println("<TD>");
		if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.REVERSAL_STRING)){
			pwOut.println("Entry&nbsp;amt:&nbsp;" + entryInput.getsOriginalAmount());
		}else{
			pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Field(
	        		AREntryInput.ParamOriginalAmount, 
	        		clsStringFunctions.filter(entryInput.getsOriginalAmount()), 
	        		9, 
	        		"Entry&nbsp;amt:", 
	        		"",
	        		8
	        		)
	        );
		}
		pwOut.println("</TD>");

		pwOut.println("<TD>");

        //Control Acct:
		pwOut.println("Control&nbsp;Acct:");
		
		//If it's retainage or a credit or an invoice, we always get the cash account from the account set:
		if (
				entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RETAINAGE_STRING)
				|| entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.CREDIT_STRING)
				|| entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.INVOICE_STRING)

			){
        	pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
        			+ AREntryInput.ParamControlAcct
        			+ "\" VALUE=\"" + entryInput.getsControlAcct() + "\">");
			pwOut.println(entryInput.getsControlAcct());
		}

		//If it's a receipt or a prepay, then it depends on whether or not we HAVE a cash account
		//in the account set:
		if
			(entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RECEIPT_STRING)
			|| entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.PREPAYMENT_STRING)){

			//If there is no cash account in the account set, we need to display a drop down
			//of GL accounts for the control account:
			if (sDefaultCashAcct.compareToIgnoreCase("") == 0){
				//TODO - switch this to a finder for the GL:
				/*
				pwOut.println("<SELECT NAME = \"" + AREntryInput.ParamControlAcct + "\">");
		        
		        //Read out the array list:
				//System.out.println("In ARCreateEntryForm m_sGLValues.get(1).toString() = " + m_sGLValues.get(1).toString());
	        	pwOut.println("<OPTION");
				//SQLStatements.get(i).toString()
				if (entryInput.getsControlAcct().compareToIgnoreCase("") == 0){
					pwOut.println(" selected=yes");
				}
				pwOut.println(" VALUE=\"" + "" + "\">");
				pwOut.println("*** Select a control account ***");
		        for (int i = 0; i < m_sGLValues.size();i++){
		        	pwOut.println("<OPTION");
					//SQLStatements.get(i).toString()
					if (m_sGLValues.get(i).toString().compareToIgnoreCase(entryInput.getsControlAcct()) == 0){
						pwOut.println(" selected=yes");
					}
					pwOut.println(" VALUE=\"" + m_sGLValues.get(i).toString() + "\">");
					pwOut.println(m_sGLDescriptions.get(i).toString());
		        }
		        
		        pwOut.println("</SELECT>");
				*/
				pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Field(
		        		AREntryInput.ParamControlAcct, 
		        		clsStringFunctions.filter(entryInput.getsControlAcct()), 
		        		SMTableentries.scontrolacctLength, 
		        		"", 
		        		"",
		        		20
		        		)
		        );

				pwOut.println("<INPUT TYPE=SUBMIT NAME='" + AREntryUpdate.FIND_GL_CONTROL_ACCT_BUTTON_NAME + "'" 
				+ " VALUE='" + AREntryUpdate.FIND_GL_CONTROL_ACCT_BUTTON_LABEL + "'" 
				+ " STYLE='height: 0.24in'>");
			}else{
	        	pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
	        			+ AREntryInput.ParamControlAcct
	        			+ "\" VALUE=\"" + entryInput.getsControlAcct() + "\">");
				pwOut.println(entryInput.getsControlAcct());
			}
		}
		//If it's a MISC receipt, it always needs a drop down:
		if(entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.MISCRECEIPT_STRING)){
			pwOut.println("<SELECT NAME = \"" + AREntryInput.ParamControlAcct + "\">");
	        
	        //Read out the array list:
        	pwOut.println("<OPTION");
			if (entryInput.getsControlAcct().compareToIgnoreCase("") == 0){
				pwOut.println(" selected=yes");
			}
			pwOut.println(" VALUE=\"" + "" + "\">");
			pwOut.println("*** Select a control account ***");
	        for (int i = 0; i < m_sGLValues.size();i++){
	        	pwOut.println("<OPTION");
				if (m_sGLValues.get(i).toString().compareToIgnoreCase(entryInput.getsControlAcct()) == 0){
					pwOut.println(" selected=yes");
				}
				pwOut.println(" VALUE=\"" + m_sGLValues.get(i).toString() + "\">");
				pwOut.println(m_sGLDescriptions.get(i).toString());
	        }
	        pwOut.println("</SELECT>");
		}
        pwOut.println("</TD>");

        //END ROW 1
        pwOut.println("</TR>");
        
        //START ROW 2
		//Terms:
        // If it's an invoice or retainage, we have to display a list of terms - 
        //otherwise, just an 'N/A' is enough:
        if (
        	(entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.INVOICE_STRING))
        	|| (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RETAINAGE_STRING))
        	){
            
        	ArrayList<String>sValues = new ArrayList<String>();
            ArrayList<String>sDescriptions = new ArrayList<String>();
            String sSQL =  "SELECT " 
            		+ SMTablearterms.sTermsCode + ", "
            		+ SMTablearterms.sDescription
            		+ " FROM " + SMTablearterms.TableName
            		+ " ORDER BY " + SMTablearterms.sTermsCode;
            try{
    	        ResultSet rsTerms = clsDatabaseFunctions.openResultSet(
    	        	sSQL, 
    	        	context, 
    	        	sDBID,
    	        	"MySQL",
    	        	"ARCreateEntryForm.displayEditableEntryFields");
    	        
    	        while (rsTerms.next()){
    	        	sValues.add((String) rsTerms.getString(SMTablearterms.sTermsCode).trim());
    	        	sDescriptions.add((String) (rsTerms.getString(SMTablearterms.sTermsCode).trim() + " - " + rsTerms.getString(SMTablearterms.sDescription).trim()));
    	        }
    	        
    	        rsTerms.close();
            }catch (SQLException ex){
    	    	System.out.println("Error in ARCreateEntryForm.Display_NONEditable_Lines class!!");
    	        System.out.println("SQLException: " + ex.getMessage());
    	        System.out.println("SQLState: " + ex.getSQLState());
    	        System.out.println("SQL: " + ex.getErrorCode());
            }
            pwOut.println("<TD>");
            pwOut.println("Terms:&nbsp;");
            pwOut.println(ARUtilities.Create_Edit_Form_List_Field(
            		AREntryInput.ParamTerms, 
            		sValues, 
            		clsStringFunctions.filter(entryInput.getsTerms()), 
            		sDescriptions
            		));
            
            pwOut.println("</TD>");

        }else{
            pwOut.println("<TD>");
            pwOut.println("Terms:&nbsp;(N/A)");
            pwOut.println("</TD>");
        }
		
		//Due Date:
        // If it's an invoice/retainage, we have to display a due date - otherwise, just an 'N/A' is enough:
        if (
        	(entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.INVOICE_STRING))
        	|| (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RETAINAGE_STRING))
        ){
			pwOut.println("<TD>");
			pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Field(
	        		AREntryInput.ParamDueDate, 
	        		clsStringFunctions.filter(entryInput.getsDueDate()), 
	        		10, 
	        		"Due&nbsp;date:", 
	        		"",
	        		8
	        		)
	        );

			//Date picker:
			pwOut.println(SMUtilities.getDatePickerString(AREntryInput.ParamDueDate, context));
			
			pwOut.println("</TD>");
        }else{
	        pwOut.println("<TD>");
	        pwOut.println("Due&nbsp;Date:&nbsp;(N/A)");
	        pwOut.println("</TD>");
        }
        
        //Display the out-of-balance amount:
        pwOut.println("<TD>");
        pwOut.println("Out&nbsp;of&nbsp;Balance:");
        pwOut.println(entryInput.getsUndistributedAmount());
        pwOut.println("</TD>");
		
        //Description:
        pwOut.println("<TD>");
        pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Field(
        		AREntryInput.ParamDocDescription, 
        		ARUtilities.Fill_In_Empty_String_For_HTML_Cell(clsStringFunctions.filter(entryInput.getsDocDescription())), 
        		SMTableentries.sdocdescriptionLength, 
        		"Description:", 
        		"",
        		40
        		)
        );
        pwOut.println("</TD>");
        
        //END ROW 2:
        pwOut.println("</TR>");
        
        //START ROW 3:
        pwOut.println("<TR>");
        
        //Apply-to-document number:
        String m_sApplyToDoc = "&nbsp;";
        String m_sApplyToID =  "&nbsp;";
        if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RETAINAGE_STRING)){
        	m_sApplyToDoc = entryInput.getLine(0).getDocAppliedTo();
        	m_sApplyToID = entryInput.getLine(0).getDocAppliedToID();
        }
        
        if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.CREDIT_STRING)){
        	//If there is already a line on the credit, use that line to get the apply-to information:
        	if(entryInput.getLineCount() > 0){
            	m_sApplyToDoc = entryInput.getLine(0).getDocAppliedTo();
            	m_sApplyToID = entryInput.getLine(0).getDocAppliedToID();
        	}else{
            	m_sApplyToDoc = sApplyToDoc;
            	m_sApplyToID = sApplyToID;
        	}
        }
        
        if (
        	entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RETAINAGE_STRING)
        	|| entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.CREDIT_STRING)
        ){
        	pwOut.println("<TD>Apply-to doc: ");
			pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "" 
					+ SMUtilities.lnViewInvoice(sDBID, m_sApplyToDoc )
		    		+ "\">"
		    		+ m_sApplyToDoc
		    		+ "</A>");
			pwOut.println("</TD>");
        	
        }else{
        	pwOut.println("<TD>Apply-to doc: " + m_sApplyToDoc + "</TD>");
        }
        //Apply-to-document ID:
        pwOut.println("<TD>Apply-to doc ID: " + m_sApplyToID + "</TD>");
        
        //If it's an invoice or a credit, we need a place for the order number to appear:
        //Order Number:
        if ((entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.INVOICE_STRING))
        ){
	        pwOut.println("<TD>");
	        pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Field(
	        		AREntryInput.ParamOrderNumber, 
	        		clsStringFunctions.filter(entryInput.getsOrderNumber()), 
	        		SMTableentries.sordernumberLength, 
	        		"Order&nbsp;#:", 
	        		"",
	        		15
	        		)
	        );
	        pwOut.println("</TD>");
        }
        if ((entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.CREDIT_STRING))
        	|| (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RETAINAGE_STRING))
        ){
        	pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
        			+ AREntryInput.ParamOrderNumber
        			+ "\" VALUE=\"" + entryInput.getsOrderNumber() + "\">");
        	pwOut.println("<TD>Order #: " 
        			+ "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
        			+ entryInput.getsOrderNumber() 
        			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
        			+ "\">" + entryInput.getsOrderNumber() + "</A></TD>");
        }

        //If it's an invoice, we need a place for the PO number to appear:
        //PO Number:
        if ((entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.INVOICE_STRING))
        ){
	        pwOut.println("<TD>");
	        pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Field(
	        		AREntryInput.ParamPONumber, 
	        		clsStringFunctions.filter(entryInput.getsPONumber()), 
	        		SMTableentries.sentryponumberLength, 
	        		"PO&nbsp;#:", 
	        		"",
	        		35
	        		)
	        );
	        pwOut.println("</TD>");
        }

        
        //END ROW 3:
        pwOut.println("</TR>");
        
        pwOut.println("</TABLE>");
	}
	private static boolean loadGLList(
			ServletContext context,
			String sDBID
	){
        m_sGLValues.clear();
        m_sGLDescriptions.clear();
        try{
	        String sSQL = MySQLs.Get_GL_Account_List_SQL(false);

	        ResultSet rsGLAccts = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	context, 
	        	sDBID,
	        	"MySQL",
	        	"ARCreateEntryForm.loadGLList");
	        
			//Print out directly so that we don't waste time appending to string buffers:
	        while (rsGLAccts.next()){
	        	m_sGLValues.add((String) rsGLAccts.getString(SMTableglaccounts.sAcctID).trim());
	        	m_sGLDescriptions.add((String) rsGLAccts.getString(SMTableglaccounts.sAcctID).trim() + " - " + (String) rsGLAccts.getString(SMTableglaccounts.sDesc).trim());
			}
	        rsGLAccts.close();

		}catch (SQLException ex){
	    	System.out.println("Error in ARCreateEntryForm.loadGLList!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}
		
		return true;
	}
	private static void displayNonEditableEntryFields (
			PrintWriter pwOut,
			AREntryInput entryInput,
			String sApplyToDoc,
			String sApplyToID,
			String sDBID,
			ServletContext context
	){

		pwOut.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">");
		
		//START ROW 1
        //Doc Number:
		pwOut.println("<TD>Doc #: <B>" 
				+ ARUtilities.Fill_In_Empty_String_For_HTML_Cell(clsStringFunctions.filter(entryInput.getsDocNumber()))+ "</B></TD>");
        //Doc date:
		pwOut.println("<TD>Doc. date: <B>" + entryInput.getsDocDate() + "</B></TD>");
        //Original amt:
		pwOut.println("<TD>Entry amt: <B>" + entryInput.getsOriginalAmount() + "</B></TD>");
        //Control Acct:
		pwOut.println("<TD>Control acct: <B>" + ARUtilities.Fill_In_Empty_String_For_HTML_Cell(entryInput.getsControlAcct()) + "</B></TD>");

        //END ROW 1
		pwOut.println("</TR>");

        //START ROW 2
		pwOut.println("<TR>");

        //Terms:
		//If it's an invoice, display the terms - otherwise, the N/A will do:
		if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.INVOICE_STRING)){
			pwOut.println("<TD>Terms: <B>" + ARUtilities.Fill_In_Empty_String_For_HTML_Cell(clsStringFunctions.filter(entryInput.getsTerms())) + "</B></TD>");	
		}else{
			pwOut.println("<TD>Terms: <B>(N/A)</B></TD>");
		}
        //Due date:
		//If it's an invoice, we need a due date - otherwise, just N/A:
		if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.INVOICE_STRING)){
			pwOut.println("<TD>Due date: <B>" + entryInput.getsDueDate() + "</B></TD>");
		}else{
			pwOut.println("<TD>Due date: <B>(N/A)</B></TD>");
		}
        //Out of balance amt:
		pwOut.println("<TD>Out of balance: <B>" + entryInput.getsUndistributedAmount() + "</B></TD>");		
		//Description:
		pwOut.println("<TD>Description: <B>"
        		+ ARUtilities.Fill_In_Empty_String_For_HTML_Cell(clsStringFunctions.filter(entryInput.getsDocDescription())) 
        		+ "</B></TD>");
        
        //END ROW 2:
		pwOut.println("</TR>");
		
        //START ROW 3:
        pwOut.println("<TR>");
        
        //Apply-to-document number:
        String m_sApplyToDoc = "&nbsp;";
        String m_sApplyToID =  "&nbsp;";
        if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RETAINAGE_STRING)){
        	m_sApplyToDoc = entryInput.getLine(0).getDocAppliedTo();
        	m_sApplyToID = entryInput.getLine(0).getDocAppliedToID();
        }
        if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.CREDIT_STRING)){
        	if(entryInput.getLineCount() > 0){
            	m_sApplyToDoc = entryInput.getLine(0).getDocAppliedTo();
            	m_sApplyToID = entryInput.getLine(0).getDocAppliedToID();
        	}else{
            	m_sApplyToDoc = sApplyToDoc;
            	m_sApplyToID = sApplyToID;
        	}
        }
        
        if (
        	entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RETAINAGE_STRING)
        	|| entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.CREDIT_STRING)
        ){
        	pwOut.println("<TD>Apply-to doc: ");
    		pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "" 
    				+ SMUtilities.lnViewInvoice(sDBID, m_sApplyToDoc )
    	    		+ "\">"
    	    		+ m_sApplyToDoc
    	    		+ "</A>");
    		pwOut.println("</TD>");
        }else{
        	pwOut.println("<TD>Apply-to doc: " + m_sApplyToDoc + "</TD>");
        }
        
        //Apply-to-document ID:
        pwOut.println("<TD>Apply-to doc ID: <B>" + m_sApplyToID + "</B></TD>");
        
        
        if ((entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.INVOICE_STRING))
        		|| (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.CREDIT_STRING))
        		|| (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RETAINAGE_STRING))
        		){
        	pwOut.println("<TD>Order #: " 
        			+ "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
        			+ entryInput.getsOrderNumber() 
        			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
        			+ "\">" + entryInput.getsOrderNumber() + "</A></TD>");
        }

        if ((entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.INVOICE_STRING))
        		|| (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.CREDIT_STRING))
        		|| (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RETAINAGE_STRING))
        		){
        	pwOut.println("<TD>PO #: " + entryInput.getsPONumber() + "</A></TD>");
        }

        //END ROW 3:
        pwOut.println("</TR>");
		pwOut.println("</TABLE>");
	}
	private static void Display_Line_Header(
			PrintWriter pwOut,
			AREntryInput entryInput
	){
		pwOut.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">");
		pwOut.println("<TR>");
		
		//If it's a prepay, we want to show the order number, not the apply to doc:
		if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.PREPAYMENT_STRING)){
			pwOut.println("<TD><B><U>Order #</B></U></TD>");
		}else{
			pwOut.println("<TD><B><U>Apply&nbsp;to<br>Doc #</B></U></TD>");
		}
		pwOut.println("<TD><B><U>Apply&nbsp;to<br>Doc&nbsp;ID</B></U></TD>");
		
		if(entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RECEIPT_STRING)){
			//Doc Type:
			pwOut.println("<TD>");
			pwOut.println("<B><U>Doc<br>type</B></U>");
			pwOut.println("</TD>");
			//Order number
			pwOut.println("<TD>");
			pwOut.println("<B><U>Order<br>number</B></U>");
			pwOut.println("</TD>");
			//Original amount
			pwOut.println("<TD>");
			pwOut.println("<B><U>Original<br>amount</B></U>");
			pwOut.println("</TD>");
			//Current amount
			pwOut.println("<TD>");
			pwOut.println("<B><U>Current<br>amount</B></U>");
			pwOut.println("</TD>");
			//Net amount
			pwOut.println("<TD>");
			pwOut.println("<B><U>Net<br>amount</B></U>");
			pwOut.println("</TD>");
		}
    	
		pwOut.println("<TD><B><U>GL&nbsp;Account</B></U></TD>");
		pwOut.println("<TD><B><U>Amount</B></U></TD>");
		pwOut.println("<TD><B><U>Description</B></U></TD>");
		pwOut.println("<TD><B><U>Comment</B></U></TD>");
		pwOut.println("</TR>");

	}
	private static boolean Display_NONEditable_Lines(
			PrintWriter pwOut,
			AREntryInput entryInput,
			ServletContext context,
			String sDBID
	){
		
        //Display the line header:
		Display_Line_Header(pwOut, entryInput);
        for (int i = 0; i < entryInput.getLineCount(); i++){
        	ARLineInput line = entryInput.getLine(i);
        	//Apply to doc #:
        	pwOut.println("<TR>");
        	pwOut.println("<TD>");
    		//If it's a prepay, we want to show the order number, not the apply to doc:
        	if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.PREPAYMENT_STRING)){
        		if(line.getApplyToOrderNumber().trim().length() > 0){
        		pwOut.println(
        				"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
        				+ line.getApplyToOrderNumber() 
        				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
        				+ "\">" + line.getApplyToOrderNumber() + "</A>"
        		);
        		}else{
        			pwOut.println("&nbsp;");
        		}	
        	}else{
        		//If it's a receipt or a credit, display a link to the invoice
        		if (
        			entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RECEIPT_STRING)
        			|| entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.CREDIT_STRING)
        		){
        			if (line.getDocAppliedToID().compareToIgnoreCase("-1") == 0){
		    			pwOut.println(line.getDocAppliedTo());
        			}else{
		    			pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "" 
								+ SMUtilities.lnViewInvoice(sDBID, line.getDocAppliedTo() )
					    		+ "\">"
					    		+ line.getDocAppliedTo()
					    		+ "</A>");
        			}
        		}else{
            		//Otherwise, just display the apply-to doc:
            		pwOut.println(ARUtilities.Fill_In_Empty_String_For_HTML_Cell(clsStringFunctions.filter(line.getDocAppliedTo())));
        		}
        	}
        	pwOut.println("</TD>");

        	//Apply to doc ID:
        	pwOut.println("<TD>");
        	pwOut.println(ARUtilities.Fill_In_Empty_String_For_HTML_Cell(clsStringFunctions.filter(line.getDocAppliedToID())));
        	pwOut.println("</TD>");
        	
    		if(entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RECEIPT_STRING)){
    			if (line.getDocAppliedToID().compareTo("-1") != 0){
					ARTransaction trans = new ARTransaction(line.getDocAppliedToID());
					if(!trans.load(context, sDBID)){
						pwOut.println("Error loading transaction with ID: " + line.getDocAppliedToID());
						//System.out.println("In ARCreateEntryForm - Error loading transaction with ID: " + line.getDocAppliedToID());
						return false;
					}
					
					//Doc Type
					pwOut.println("<TD>");
					pwOut.println(ARDocumentTypes.getSourceTypes(trans.getiDocType()));
					pwOut.println("</TD>");
					
					//Order number
					pwOut.println("<TD>");
					String sOrderNumber = trans.getOrderNumber();
					if(sOrderNumber.length() > 0){
						pwOut.println(
								"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
								+ sOrderNumber 
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
								+ "\">" + sOrderNumber + "</A>"
						);
					}else{
						pwOut.println("&nbsp;");
					}
					pwOut.println("</TD>");
					//Original amount
					pwOut.println("<TD ALIGN=RIGHT>");
					pwOut.println(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(trans.getdOriginalAmount()));
					pwOut.println("</TD>");
					//Current amount
					pwOut.println("<TD ALIGN=RIGHT>");
					pwOut.println(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(trans.getdCurrentAmount()));
					pwOut.println("</TD>");
					//Net amount
					pwOut.println("<TD ALIGN=RIGHT>");
					pwOut.println(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(trans.getNetAmount(context, sDBID)));
					pwOut.println("</TD>");
    			}else{
    				//If it's unapplied:
    				//Document type
    				pwOut.println("<TD>");
    				pwOut.println("N/A");
					pwOut.println("</TD>");
    				//Order number
    				pwOut.println("<TD>");
    				pwOut.println("&nbsp;");
					pwOut.println("</TD>");
					//Original amount
					pwOut.println("<TD ALIGN=RIGHT>");
					pwOut.println("0.00");
					pwOut.println("</TD>");
					//Current amount
					pwOut.println("<TD ALIGN=RIGHT>");
					pwOut.println("0.00");
					pwOut.println("</TD>");
					//Net amount
					pwOut.println("<TD ALIGN=RIGHT>");
					pwOut.println("0.00");
					pwOut.println("</TD>");
    			}
    		}
        	
        	//GL Acct:
        	pwOut.println("<TD>");
        	pwOut.println(ARUtilities.Fill_In_Empty_String_For_HTML_Cell(clsStringFunctions.filter(line.getLineAcct())));
        	pwOut.println("</TD>");
        	
        	//Amount:
        	pwOut.println("<TD ALIGN = RIGHT>");
        	pwOut.println(line.getAmount());
        	pwOut.println("</TD>");
        	
        	//Description:
        	pwOut.println("<TD>");
        	pwOut.println(ARUtilities.Fill_In_Empty_String_For_HTML_Cell(clsStringFunctions.filter(line.getDescription())));
        	pwOut.println("</TD>");
        	
        	//Comment:
        	pwOut.println("<TD>");
        	pwOut.println(ARUtilities.Fill_In_Empty_String_For_HTML_Cell(clsStringFunctions.filter(line.getComment())));
        	pwOut.println("</TD>");
        	
        	pwOut.println("</TR>");
        }

        pwOut.println("</TABLE>");

		return true;
	}
	private static boolean displayTransactionLines(
			PrintWriter pwOut,
			AREntryInput entryInput,
			String sApplyToDocNumber,
			String sApplyToDocID,
			ServletContext context,
			String sDBID
	){

		String m_sApplyToDocNumber = sApplyToDocNumber;
		String m_sApplyToDocID = sApplyToDocID;
		
		//For all cash entries, we want these to be empty in these lines:
    	if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.MISCRECEIPT_STRING)){
    		m_sApplyToDocNumber = "UNAPPLIED";
    		m_sApplyToDocID = "-1";
    	}
    	if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.PREPAYMENT_STRING)){
    		m_sApplyToDocNumber = "UNAPPLIED";
    		m_sApplyToDocID = "-1";
    	}
    	if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RECEIPT_STRING)){
    		m_sApplyToDocNumber = "UNAPPLIED";
    		m_sApplyToDocID = "-1";
    	}
		
		//  Get the lines by reading the database:
		int iLineIndex = 0;
        for (int i = 0; i < entryInput.getLineCount(); i++){
        	ARLineInput line = entryInput.getLine(iLineIndex);
        	
        	//Set the applytodocnumber and applytodocid depending on the types of documents:
        	if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.CASHADJUSTMENT_STRING)){
            	if (i == 0){
            		m_sApplyToDocNumber = line.getDocAppliedTo();
            		m_sApplyToDocID = line.getDocAppliedToID();
            	}
        	}
        	if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.CREDIT_STRING)){
            	if (i == 0){
            		m_sApplyToDocNumber = line.getDocAppliedTo();
            		m_sApplyToDocID = line.getDocAppliedToID();
            	}
        	}
        	if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.CREDITADJUSTMENT_STRING)){
            	if (i == 0){
            		m_sApplyToDocNumber = line.getDocAppliedTo();
            		m_sApplyToDocID = line.getDocAppliedToID();
            	}
        	}
        	if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.INVOICE_STRING)){
            	if (i == 0){
            		m_sApplyToDocNumber = line.getDocAppliedTo();
            		m_sApplyToDocID = line.getDocAppliedToID();
            	}
        	}
        	if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.INVOICEADJUSTMENT_STRING)){
            	if (i == 0){
            		m_sApplyToDocNumber = line.getDocAppliedTo();
            		m_sApplyToDocID = line.getDocAppliedToID();
            	}
        	}
        	if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RETAINAGE_STRING)){
            	if (i == 0){
            		m_sApplyToDocNumber = line.getDocAppliedTo();
            		m_sApplyToDocID = line.getDocAppliedToID();
            	}
        	}
        	if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.REVERSAL_STRING)){
            	if (i == 0){
            		m_sApplyToDocNumber = line.getDocAppliedTo();
            		m_sApplyToDocID = line.getDocAppliedToID();
            	}
        	}
        	
        	pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
        			+ ARLineInput.ParamDocAppliedTo 
        			+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) 
        			+ "\" VALUE=\"" + line.getDocAppliedTo() + "\">");
        	pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
        			+ ARLineInput.ParamLineDocAppliedToID 
        			+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) 
        			+ "\" VALUE=\"" + line.getDocAppliedToID() + "\">");
        	
        	//If it's a prepay, we'll need to store the apply to order number here, too:
        	if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.PREPAYMENT_STRING)){
            	pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
            			+ ARLineInput.ParamLineApplyToOrderNumber
            			+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) 
            			+ "\" VALUE=\"" + line.getApplyToOrderNumber() + "\">");
        	}
        	
        	pwOut.println("<TR>");

        	pwOut.println("<TD>");
    		//If it's a prepay, we want to show the order number, not the apply to doc:
        	if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.PREPAYMENT_STRING)){
        		if(line.getApplyToOrderNumber().trim().length() > 0){
        		pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
        		+ line.getApplyToOrderNumber() 
        		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
        		+ "\">" + line.getApplyToOrderNumber() + "</A>");
        		}else{
        			pwOut.println("&nbsp;");
        		}
        	}else{
        		//If it's a receipt, show the invoice number with a link to view the invoice:
        		if (
        			entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RECEIPT_STRING)
        			|| entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.CREDIT_STRING)
        		){
        			pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "" 
						+ SMUtilities.lnViewInvoice(sDBID, line.getDocAppliedTo() )
			    		+ "\">"
			    		+ line.getDocAppliedTo()
			    		+ "</A>");
        		}else{
        			pwOut.println(line.getDocAppliedTo());
        		}
        	}
        	pwOut.println("</TD>");

        	pwOut.println("<TD>");
        	pwOut.println(line.getDocAppliedToID());
        	pwOut.println("</TD>");
        	
			//If it's a cash entry, add some extra fields:
			if(entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RECEIPT_STRING)){
				//Order number
				String sDocType = "";
				String sOrderNumber = "";
				String sOriginalAmount = "0.00";
				String sCurrentAmount = "0.00";
				String sNetAmount = "0.00";
				
				if(!line.getDocAppliedToID().equalsIgnoreCase("-1")){
					ARTransaction trans = new ARTransaction(line.getDocAppliedToID());
					if(!trans.load(context, sDBID)){
						pwOut.println("Error loading transaction with ID: " + line.getDocAppliedToID());
						//System.out.println("In ARCreateEntryForm - Error loading transaction with ID: " + line.getDocAppliedToID());
						return false;
					}
					sDocType = ARDocumentTypes.getSourceTypes(trans.getiDocType());
					sOrderNumber = trans.getOrderNumber();
					sOriginalAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(trans.getdOriginalAmount());
					sCurrentAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(trans.getdCurrentAmount());
					sNetAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(trans.getNetAmount(context, sDBID));
				}
				
				//Doc Type:
				pwOut.println("<TD>");
				pwOut.println(sDocType);
				pwOut.println("</TD>");
				
				//Order number:
				pwOut.println("<TD>");
				if(sOrderNumber.length() > 0){
					pwOut.println(
							"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
							+ sOrderNumber 
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
							+ "\">" + sOrderNumber + "</A>"
					);
				}else{
					pwOut.println("&nbsp;");
				}
				pwOut.println("</TD>");
				//Original amount
				pwOut.println("<TD ALIGN=RIGHT>");
				pwOut.println(sOriginalAmount);
				pwOut.println("</TD>");
				//Current amount
				pwOut.println("<TD ALIGN=RIGHT>");
				pwOut.println(sCurrentAmount);
				pwOut.println("</TD>");
				//Net amount
				pwOut.println("<TD ALIGN=RIGHT>");
				pwOut.println(sNetAmount);
				pwOut.println("</TD>");
			}
        	
        	pwOut.println("<TD>");

        	//If it's retainage/credit/invoice/prepay/receipt transaction, 
        	//set the GL acct and don't allow editing:
        	if(
        		entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RETAINAGE_STRING)
        		|| entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.CREDIT_STRING)
        		|| entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.INVOICE_STRING)
        		|| entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.PREPAYMENT_STRING)
        		|| entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RECEIPT_STRING)
        	){
            	pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
            			+ ARLineInput.ParamDistAcct 
            			+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) 
            			+ "\" VALUE=\"" + line.getLineAcct() + "\">");

        		pwOut.println(line.getLineAcct());
        	}else{
	        	pwOut.println("<SELECT NAME = \"" + ARLineInput.ParamDistAcct 
	            		+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) + "\">");
	            
	            //Read out the array list:
	            for (int iGLCount = 0; iGLCount<m_sGLValues.size();iGLCount++){
	            	pwOut.println("<OPTION");
	    			if (m_sGLValues.get(iGLCount).toString().compareToIgnoreCase(line.getLineAcct()) == 0){
	    				pwOut.println( " selected=yes");
	    			}
	    			pwOut.println(" VALUE=\"" + m_sGLValues.get(iGLCount).toString() + "\">");
	    			pwOut.println(m_sGLDescriptions.get(iGLCount).toString());
	            }
	            pwOut.println("</SELECT>");
        	}
	        pwOut.println("</TD>");
            
        	//Amount:
	        pwOut.println("<TD ALIGN = RIGHT>");
        	if(entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RETAINAGE_STRING)){
            	pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
            			+ ARLineInput.ParamLineAmt 
            			+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) 
            			+ "\" VALUE=\"" + line.getAmount() + "\">");

        		pwOut.println(line.getAmount());
        	}else{
		        
	            pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Field(
	        			ARLineInput.ParamLineAmt 
	        				+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6), 
	        			line.getAmount(), 
	        			9, 
	        			"", 
	        			"",
	        			9
	        			)
	        	);
        	}
            pwOut.println("</TD>");

        	//Description:
            pwOut.println("<TD>");
            pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Field(
        			ARLineInput.ParamLineDesc 
        				+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6), 
        				clsStringFunctions.filter(line.getDescription()), 
        			25, 
        			"", 
        			""
        			)
        	);
            pwOut.println("</TD>");

        	//Comment:
            pwOut.println("<TD>");
            pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Field(
        			ARLineInput.ParamLineComment 
        				+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6), 
        				clsStringFunctions.filter(line.getComment()), 
        			25, 
        			"", 
        			""
        			)
        	);
            pwOut.println("</TD>");
        	
            pwOut.println("</TR>");
        	iLineIndex ++;
        }
        
        int iNumberOfBlankLines = 3;
      //Prepays can only have one line, because they can only apply to a single order number.
        if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.PREPAYMENT_STRING)){
        	iNumberOfBlankLines = 1 - entryInput.getLineCount();
        }
      //IF IT's A RETAINAGE TRANSACTION, Don't add ANY blank lines:
        if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RETAINAGE_STRING)){
        	iNumberOfBlankLines = 0;
        }
        for (int iLines = 1; iLines <= iNumberOfBlankLines; iLines++){
			
			//If it's not a cash entry or a prepay, the apply to doc and ID have to be the same
			//as they were for the other lines:
			if (
					(entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RECEIPT_STRING))
					 || (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.PREPAYMENT_STRING))
				){
				
			}else{
				pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
	        			+ ARLineInput.ParamDocAppliedTo 
	        			+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) 
	        			+ "\" VALUE=\"" + m_sApplyToDocNumber + "\">");
				pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
	        			+ ARLineInput.ParamLineDocAppliedToID 
	        			+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) 
	        			+ "\" VALUE=\"" + m_sApplyToDocID + "\">");
				}
			pwOut.println("<TR>");
			pwOut.println("<TD>");
			
			//If this is a cash entry, we'll need an input field here:
			if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RECEIPT_STRING)){
	            pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Field(
	        			ARLineInput.ParamDocAppliedTo
	        				+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6), 
	        				"", 
	        			12, 
	        			"", 
	        			"",
	        			12
	        			)
	        	);
			}else{
				if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.PREPAYMENT_STRING)){
		            pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Field(
		        			ARLineInput.ParamLineApplyToOrderNumber 
		        				+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6), 
		        				"", 
		        			12, 
		        			"", 
		        			"",
		        			12
		        			)
		        	);
				}else{
					if(entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.CREDIT_STRING)){
			    		pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "" 
			    			+ SMUtilities.lnViewInvoice(sDBID, m_sApplyToDocNumber )
			    	    	+ "\">"
			    	    	+ m_sApplyToDocNumber
			    	    	+ "</A>");
					}else{
						pwOut.println(m_sApplyToDocNumber);
					}
				}
			}
			pwOut.println("</TD>");

			pwOut.println("<TD>");
			pwOut.println(m_sApplyToDocID);
			pwOut.println("</TD>");

			//If it's a cash entry, add some extra fields:
			if(entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RECEIPT_STRING)){
				//Doc type
				pwOut.println("<TD>");
				pwOut.println("&nbsp;");
				pwOut.println("</TD>");
				//Order number
				pwOut.println("<TD>");
				pwOut.println("&nbsp;");
				pwOut.println("</TD>");
				//Original amount
				pwOut.println("<TD>");
				pwOut.println("&nbsp;");
				pwOut.println("</TD>");
				//Current amount
				pwOut.println("<TD>");
				pwOut.println("&nbsp;");
				pwOut.println("</TD>");
				//Net amount
				pwOut.println("<TD>");
				pwOut.println("&nbsp;");
				pwOut.println("</TD>");
			}
			
			pwOut.println("<TD>");
            //Read out the array list:
			String sDefaultGLAcct = "";
			if(
					(entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.PREPAYMENT_STRING))
					|| (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RECEIPT_STRING))
			){
				//If it's a receipt, use the AR acontrol Acct as the distribution acct:
				if(entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RECEIPT_STRING)){
					sDefaultGLAcct = sDefaultARControlAcct;
				}else{
				//But if it's a prepay, use the prepay liability account as the dist acct:
					sDefaultGLAcct = sDefaultPrepayAcct;
				}
				
				pwOut.println(ARUtilities.Fill_In_Empty_String_For_HTML_Cell(sDefaultGLAcct));
            	pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
            			+ ARLineInput.ParamDistAcct 
            			+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) 
            			+ "\" VALUE=\"" + sDefaultGLAcct + "\">");
			}else{
				pwOut.println("<SELECT NAME = \"" + ARLineInput.ParamDistAcct 
	            		+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) + "\">");
				for (int i = 0; i<m_sGLValues.size();i++){
	            	pwOut.println("<OPTION");
	            	if (m_sGLValues.get(i).toString().compareToIgnoreCase(sDefaultGLAcct) == 0){
	            		pwOut.println(" selected=yes");
	            	}
	            	pwOut.println(" VALUE=\"" + m_sGLValues.get(i).toString() + "\">");
	            	pwOut.println(m_sGLDescriptions.get(i).toString());
	            }
	            pwOut.println("</SELECT>");
			}
            pwOut.println("</TD>");
            
        	//Amount:
            pwOut.println("<TD>");
            pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Field(
        			ARLineInput.ParamLineAmt 
        				+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6), 
        			"0.00", 
        			9, 
        			"", 
        			"",
        			9
        			)
        	);
        	
            pwOut.println("</TD>");

        	//Description:
            pwOut.println("<TD>");
            pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Field(
        			ARLineInput.ParamLineDesc 
        				+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6), 
        				"", 
        			25, 
        			"", 
        			""
        			)
        	);
            pwOut.println("</TD>");

        	//Comment:
            pwOut.println("<TD>");
            pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Field(
        			ARLineInput.ParamLineComment 
        				+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6), 
        				"", 
        			25, 
        			"", 
        			""
        			)
        	);
            pwOut.println("</TD>");
            pwOut.println("</TR>");	
        	iLineIndex ++;
		}
		//IF it's a cash receipt entry, now add lines for all the invoices or retainage 
        //that a cash line could be applied to:
		if (entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RECEIPT_STRING)){
			String sSQL =  "SELECT *" 
					+ " FROM " + SMTableartransactions.TableName
					+ " WHERE ("
						+ "(" + SMTableartransactions.spayeepayor + " = '" + entryInput.getsCustomerNumber() + "')"
						+ " AND (" 
							+ "(" + SMTableartransactions.idoctype + " = " + ARDocumentTypes.INVOICE_STRING + ")"
							+ " OR (" + SMTableartransactions.idoctype + " = " + ARDocumentTypes.RETAINAGE_STRING + ")"
						+ ")"
						+ " AND (" + SMTableartransactions.dcurrentamt + " != 0.00)"
						+ ")";
	        try{
	        	ResultSet rs = clsDatabaseFunctions.openResultSet(
	        		sSQL, 
	        		context, 
	        		sDBID,
	        		"MySQL",
	        		"ARCreateEntryForm.displayTransactionLines");
		        
				//Print out directly so that we don't waste time appending to string buffers:
		        while (rs.next()){
		        	if (invoiceIsAlreadyListed(entryInput, rs.getString(SMTableartransactions.sdocnumber))){
		        	}else{
						pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
			        			+ ARLineInput.ParamDocAppliedTo 
			        			+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) 
			        			+ "\" VALUE=\"" + rs.getString(SMTableartransactions.sdocnumber) + "\">");
						pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
			        			+ ARLineInput.ParamLineDocAppliedToID 
			        			+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) 
			        			+ "\" VALUE=\"" + Long.toString(rs.getLong(SMTableartransactions.lid)) + "\">");
						
						pwOut.println("<TR>");
						pwOut.println("<TD>");
						String sDocNumber = rs.getString(SMTableartransactions.sdocnumber);
						pwOut.println(
								"<INPUT TYPE=CHECKBOX NAME=\""
								+ ARLineInput.ParamLineApplyCashToChk
								+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6)
								+ "\" >" 
								+ "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "" 
								+ SMUtilities.lnViewInvoice(sDBID, sDocNumber )
					    		+ "\">"
					    		+ sDocNumber
					    		+ "</A>"
						);
						pwOut.println("</TD>");
	
						pwOut.println("<TD>");
						pwOut.println(Long.toString(rs.getLong(SMTableartransactions.lid)));
						pwOut.println("</TD>");
	
						//Doc type:
						pwOut.println("<TD>");
						pwOut.println(ARDocumentTypes.getSourceTypes(rs.getInt(SMTableartransactions.idoctype)));
						pwOut.println("</TD>");
						
						//Order #
						pwOut.println("<TD>");
						String sOrderNumber = rs.getString(SMTableartransactions.sordernumber).trim();
						if(sOrderNumber.length() > 0){
							pwOut.println(
									"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
									+ sOrderNumber 
									+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
									+ "\">" + sOrderNumber + "</A>"
							);
						}else{
							pwOut.println("&nbsp;");
						}
						pwOut.println("</TD>");
						//Original amt:
						pwOut.println("<TD ALIGN=RIGHT>");
						pwOut.println(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableartransactions.doriginalamt)));
						pwOut.println("</TD>");
						
						//Current amt:
						pwOut.println("<TD ALIGN=RIGHT>");
						pwOut.println(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableartransactions.dcurrentamt)));
						pwOut.println("</TD>");
						
						//Pending:
						
						//Net:
						pwOut.println("<TD ALIGN=RIGHT>");
						ARTransaction trans = new ARTransaction(Long.toString(rs.getLong(SMTableartransactions.lid)));
						if(!trans.load(context, sDBID)){
							pwOut.println("Error loading transaction with ID: " + trans.getsTransactionID());
							//System.out.println("In ARCreateEntryForm - Error loading existing invoices: transaction with ID: " + trans.getsTransactionID());
							return false;
						}
						pwOut.println(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(trans.getNetAmount(context, sDBID)));
						pwOut.println("</TD>");
						
						pwOut.println("<TD>");

						//Distribution GLS:
						String sARTransGL = rs.getString(SMTableartransactions.scontrolacct);
			        	if(
			            		entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RETAINAGE_STRING)
			            		|| entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.CREDIT_STRING)
			            		|| entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.INVOICE_STRING)
			            		|| entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.PREPAYMENT_STRING)
			            		|| entryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RECEIPT_STRING)
			            	){
			            	pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
			            			+ ARLineInput.ParamDistAcct 
			            			+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) 
			            			+ "\" VALUE=\"" + sARTransGL + "\">");

			        		pwOut.println(sARTransGL);
			        	}else{
						
							pwOut.println("<SELECT NAME = \"" + ARLineInput.ParamDistAcct 
				            		+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) + "\">");
				            
				            //Read out the array list:
				            for (int i = 0; i<m_sGLValues.size();i++){
				            	pwOut.println("<OPTION");
				            	//Match the existing account here:
				    			if (m_sGLValues.get(i).toString().compareToIgnoreCase(sARTransGL) == 0){
				    				pwOut.println( " selected=yes");
				    			}
				            	pwOut.println(" VALUE=\"" + m_sGLValues.get(i).toString() + "\">");
				            	pwOut.println(m_sGLDescriptions.get(i).toString());
				            }
				            pwOut.println("</SELECT>");
			        	}
				        pwOut.println("</TD>");
			            
			        	//Amount:
			            pwOut.println("<TD>");
			            pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Field(
			        			ARLineInput.ParamLineAmt 
			        				+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6), 
			        			"0.00", 
			        			9, 
			        			"", 
			        			"",
			        			9
			        			)
			        	);
			        	
			            pwOut.println("</TD>");
	
			        	//Description:
			            pwOut.println("<TD>");
			            pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Field(
			        			ARLineInput.ParamLineDesc 
			        				+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6), 
			        				"", 
			        			25, 
			        			"", 
			        			""
			        			)
			        	);
			            pwOut.println("</TD>");
	
			        	//Comment:
			            pwOut.println("<TD>");
			            pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Field(
			        			ARLineInput.ParamLineComment 
			        				+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6), 
			        				"", 
			        			25, 
			        			"", 
			        			""
			        			)
			        	);
			            pwOut.println("</TD>");
			            pwOut.println("</TR>");	
			        	iLineIndex ++;
		        	}
				}
		        rs.close();

			}catch (SQLException ex){
		    	System.out.println("Error in ARCreateEntryForm.loadGLList!!");
		        System.out.println("SQLException: " + ex.getMessage());
		        System.out.println("SQLState: " + ex.getSQLState());
		        System.out.println("SQL: " + ex.getErrorCode());
				return false;
			}			
		}
		
    	//Record the number of lines:
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamNumberOfLines + "\" VALUE='" + Integer.toString(iLineIndex) + "'>");
		
		return true;
	}
	private static boolean invoiceIsAlreadyListed(AREntryInput entryInput, String sDocNumber){
		
        for (int i = 0; i < entryInput.getLineCount(); i++){
        	ARLineInput line = entryInput.getLine(i);
        	if (line.getDocAppliedTo().equalsIgnoreCase(sDocNumber)){
        		return true;
        	}
        }
        //If we never get a match, just return false:
        return false;
	}
	private static void addCommandButtons(AREntryInput enInput, PrintWriter pwOut){
    	pwOut.println("<BR><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Save Entry' STYLE='height: 0.24in'>");
    	if(
    		(enInput.getsDocumentType().compareToIgnoreCase(ARDocumentTypes.RECEIPT_STRING) == 0)
    		|| (enInput.getsDocumentType().compareToIgnoreCase(ARDocumentTypes.PREPAYMENT_STRING) == 0)
    	){
    		pwOut.println("<INPUT TYPE=SUBMIT NAME='SaveAndAdd' VALUE='Save and add' STYLE='height: 0.24in'>");
    	}
    	pwOut.println("  <INPUT TYPE=SUBMIT NAME='Delete' VALUE='Delete Entry' STYLE='height: 0.24in'>");
    	pwOut.println("  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmDelete\">");
    	if(enInput.getsDocumentType().compareToIgnoreCase(ARDocumentTypes.RECEIPT_STRING) == 0){
    		pwOut.println(
    			"  Distribute remaining amount to unapplied cash: " + "" 
    			+ "<INPUT TYPE=CHECKBOX NAME=\"DistributeToUnappliedCash\">");
    	}
	}
}
