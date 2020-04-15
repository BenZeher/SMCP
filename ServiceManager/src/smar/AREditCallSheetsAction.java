package smar;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import SMDataDefinition.SMTablearcustomer;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class AREditCallSheetsAction extends HttpServlet{
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.AREditCallSheets)){return;}
		//Read the entry fields from the request object:
		ARCallSheet entry = new ARCallSheet(request);
		
		smaction.getCurrentSession().setAttribute(AREditCallSheetsEdit.CALL_SHEET_OBJECT, entry);

		//Special cases - if this class was called by a finder for the 'starting customer' field:
		if (request.getParameter(AREditCallSheetsEdit.UPDATEFROMORDER_BUTTON) != null){
			if (!entry.updateDefaultsFromOrder(getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName())){
				smaction.redirectAction(
						entry.getErrorMessages(), 
						"", 
						ARCallSheet.ParamsID + "=" + entry.getM_siID()
				);
			}else{
				smaction.getCurrentSession().setAttribute(AREditCallSheetsEdit.CALL_SHEET_OBJECT, entry);
				smaction.redirectAction(
						"", 
						"Successfully updated call sheet from order", 
						ARCallSheet.ParamsID + "=" + entry.getM_siID()
				);
			}
			return;
		}

		//Special cases - if this class was called by 'update from order info' button:
		if (request.getParameter(AREditCallSheetsEdit.FIND_CUSTOMER_BUTTON) != null){
			//Then call the finder to search for customers:
			String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "SMClasses.ObjectFinder"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&ObjectName=Customer"
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=" + smaction.getCallingClass()
				+ "&ReturnField=" + AREditCallSheetsEdit.CUSTOMERFINDERRETURN_FIELD
				+ "&SearchField1=" + SMTablearcustomer.sCustomerName
				+ "&SearchFieldAlias1=Name"
				+ "&SearchField2=" + SMTablearcustomer.sCustomerNumber
				+ "&SearchFieldAlias2=Customer%20Code"
				+ "&SearchField3=" + SMTablearcustomer.sAddressLine1
				+ "&SearchFieldAlias3=Address%20Line%201"
				+ "&SearchField4=" + SMTablearcustomer.sPhoneNumber
				+ "&SearchFieldAlias4=Phone"
				+ "&ResultListField1="  + SMTablearcustomer.sCustomerNumber
				+ "&ResultHeading1=Customer%20Number"
				+ "&ResultListField2="  + SMTablearcustomer.sCustomerName
				+ "&ResultHeading2=Customer%20Name"
				+ "&ResultListField3="  + SMTablearcustomer.sAddressLine1
				+ "&ResultHeading3=Address%20Line%201"
				+ "&ResultListField4="  + SMTablearcustomer.sPhoneNumber
				+ "&ResultHeading4=Phone"
				+ "&ResultListField5="  + SMTablearcustomer.iActive
				+ "&ResultHeading5=Active"
				+ "&ResultListField6="  + SMTablearcustomer.iOnHold
				+ "&ResultHeading6=On%20Hold"
				+ "&ResultListField7="  + SMTablearcustomer.sCustomerGroup
				+ "&ResultHeading7=Customer%20Group"
				//+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				//	+ "*RETURNINGFROMFINDER=TRUE"
				;

			response.sendRedirect(sRedirectString);
			return;
		}

		//If a delete is requested:
		if(smaction.isDeleteRequested()){
			if (smaction.isDeleteConfirmed()){
				//Save this now so it's not lost after the delete:
				String sID = entry.getM_siID();
				if (!entry.delete(getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName())){
					//System.out.println("In " + this.toString() + " !entry.delete, error = '" + entry.getErrorMessages());
					smaction.redirectAction("Could not delete: " + entry.getErrorMessages(), "", "");
					return;
				}else{
					//If the delete succeeded, the entry will be initialized:
					if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
						smaction.returnToOriginalURL();
					}else{
						entry = new ARCallSheet();
						smaction.getCurrentSession().setAttribute(AREditCallSheetsEdit.CALL_SHEET_OBJECT, entry);
						smaction.redirectAction("", "Call sheet ID: " + sID + " was successfully deleted.", "");
					}
					return;
				}
			}
		}

		//If it's an edit, process that:
		if(smaction.isEditRequested()){
			if(!entry.save_without_data_transaction(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserID(),
					smaction.getFullUserName(),
					(String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME))){
				smaction.getCurrentSession().setAttribute(AREditCallSheetsEdit.CALL_SHEET_OBJECT, entry);
				smaction.redirectAction(
						"Could not save: " + entry.getErrorMessages(), 
						"", 
						ARCallSheet.ParamsID + "=" + entry.getM_siID()
				);
				return;
			}else{
				//If the save succeeded, force the called function to reload it by NOT
				//putting the entry object in the current session, but by passing it
				//in the query string instead:
				smaction.getCurrentSession().removeAttribute(AREditCallSheetsEdit.CALL_SHEET_OBJECT);
				if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
					smaction.returnToOriginalURL();
				}else{
					smaction.redirectAction(
							"", 
							entry.getObjectName() + ": " + entry.getM_siID() + " was successfully saved.",
							ARCallSheet.ParamsID + "=" + entry.getM_siID()
					);
				}
			}
		}
		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
