package smcontrolpanel;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.FinderResults;
import SMClasses.SMFinderFunctions;
import SMClasses.SMOrderDetail;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTableicitemlocations;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;

public class SMEditOrderDetailAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	//private boolean bDebugMode = false;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMEditOrders)){return;}
		
		//First make sure to clear any order object from the session so it doesn't get inadvertently passed around:
		try {
			smaction.getCurrentSession().removeAttribute(SMOrderDetail.ParamObjectName);
		} catch (Exception e) {
			clsServletUtilities.sysprint(
				this.toString(), 
				smaction.getUserName(), 
				"Error [1423241179] removing order detail attribute - " + e.getMessage());
		}
		//Just in case there's one left floating around in the session:
		try {
			smaction.getCurrentSession().removeAttribute(SMOrderHeader.ParamObjectName);
		} catch (Exception e2) {
			clsServletUtilities.sysprint(
					this.toString(), 
					smaction.getUserName(), 
					"Error [1425502211] removing order attribute - " + e2.getMessage());
		}
		
	    //Read the entry fields from the request object:
		SMOrderDetail detail = new SMOrderDetail(request);
		//smaction.getCurrentSession().setAttribute(SMOrderHeader.ParamObjectName, entry);

    	//If it's a request to go to the lines:
    	if (clsManageRequestParameters.get_Request_Parameter(
	    	SMEditOrderDetailEdit.COMMAND_FLAG, request).compareToIgnoreCase(
	    			SMEditOrderDetailEdit.DETAILSCOMMAND_VALUE) == 0){
    		
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
			+ "smcontrolpanel.SMOrderDetailList"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			+ "&" + SMTableorderheaders.strimmedordernumber + "=" + detail.getM_strimmedordernumber()
			+ "&" + SMOrderDetailList.LASTDETAILNUMBEREDITED_PARAM + "=" + detail.getM_iDetailNumber()
			+ "&CallingClass=" + smaction.getCallingClass()
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	
    	//If it's a request to go to the totals:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMEditOrderDetailEdit.COMMAND_FLAG, request).compareToIgnoreCase(
    					SMEditOrderDetailEdit.TOTALSCOMMAND_VALUE) == 0){
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
			+ "smcontrolpanel.SMEditOrderTotalsEdit"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			+ "&" + SMTableorderheaders.strimmedordernumber + "=" + detail.getM_strimmedordernumber()
			+ "&CallingClass=" + smaction.getCallingClass()
			;
			redirectProcess(sRedirectString, response);
			return;
    	}

    	//If it's a request to go to the header:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMEditOrderDetailEdit.COMMAND_FLAG, request).compareToIgnoreCase(
    					SMEditOrderDetailEdit.HEADERCOMMAND_VALUE) == 0){
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
			+ "smcontrolpanel.SMEditOrderEdit"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			+ "&" + SMTableorderheaders.strimmedordernumber + "=" + detail.getM_strimmedordernumber()
			+ "&CallingClass=" + smaction.getCallingClass()
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	
    	//If this class has been called because the item number changed, handle that now:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMEditOrderDetailEdit.COMMAND_FLAG, request).compareToIgnoreCase(
    					SMEditOrderDetailEdit.ITEMCHANGEDCOMMAND_VALUE) == 0){
			smaction.getCurrentSession().setAttribute(SMOrderDetail.ParamObjectName, detail);
			smaction.redirectAction(
					"", 
					"Item updated", 
					SMOrderDetail.Paramstrimmedordernumber + "=" + detail.getM_strimmedordernumber()
					+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
					+ "&" + SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG, request)
					+ "&" + SMEditOrderDetailEdit.UPDATE_ITEM_DATA_FLAG + "=TRUE"
					+ "&" + SMOrderDetail.ParamsItemNumber + "=" + clsServletUtilities.URLEncode(detail.getM_sItemNumber())
					+ "&" + SMEditOrderDetailEdit.INSERTNEWLINEABOVEDETAILNUMBER + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderDetailEdit.INSERTNEWLINEABOVEDETAILNUMBER, request)
			);
			return;
    	}
    	
    	//If it's a request to find an item:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMEditOrderDetailEdit.COMMAND_FLAG, request).compareToIgnoreCase(
    					SMEditOrderDetailEdit.FINDITEMCOMMAND_VALUE) == 0){
    		
			String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "SMClasses.ObjectFinder"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&" + SMClasses.ObjectFinder.DO_NOT_SHOW_MENU_LINK + "=True"
				+ "&ObjectName=" + FinderResults.SEARCH_ITEMS_SHOWING_LOCATION_QTYS
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=" + smaction.getCallingClass()
				+ "&ReturnField=" + SMOrderDetail.ParamsItemNumber
				
				+ SMFinderFunctions.getStdITEMWithQtysSearchAndResultString(detail.getM_sLocationCode())

				+ " &" + FinderResults.ADDITIONAL_WHERE_CLAUSE_PARAMETER + "=" 
					+ "(" + SMTablelocations.TableName + "." + SMTablelocations.sLocation + " = '" + detail.getM_sLocationCode() + "')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.iActive + " = 1)"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.icannotbesold + " = 0)"
				
				+ " &" + FinderResults.FINDER_BOX_TITLE + "=ACTIVE items <I>showing qtys for location : '" + detail.getM_sLocationCode() + "'</I>. +\n"
				
				+ "&ParameterString="
				+ "*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "*" + SMOrderDetail.Paramstrimmedordernumber + "=" + detail.getM_strimmedordernumber()
				+ "*" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" 
					+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
				+ "*" + SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG + "=" 
					+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG, request)
				+ "*" + SMEditOrderDetailEdit.UPDATE_ITEM_DATA_FLAG + "=TRUE"
				+ "*" + SMEditOrderDetailEdit.INSERTNEWLINEABOVEDETAILNUMBER + "=" 
				+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderDetailEdit.INSERTNEWLINEABOVEDETAILNUMBER, request)

				+ "*CallingClass=" + smaction.getCallingClass()
			;
			//Store the detail info we have so far in the session:
			smaction.getCurrentSession().setAttribute(SMOrderDetail.ParamObjectName, detail);
			redirectProcess(sRedirectString, response);
			return;

    	}
    	
    	//If it's a request to find a non-dedicated item:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMEditOrderDetailEdit.COMMAND_FLAG, request).compareToIgnoreCase(
    					SMEditOrderDetailEdit.FINDNONDEDICATEDITEMCOMMAND_VALUE) == 0){
			String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "SMClasses.ObjectFinder"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&" + SMClasses.ObjectFinder.DO_NOT_SHOW_MENU_LINK + "=True"
				+ "&ObjectName=" + SMClasses.FinderResults.SEARCH_ITEMS_SHOWING_LOCATION_QTYS
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=" + smaction.getCallingClass()
				+ "&ReturnField=" + SMOrderDetail.ParamsItemNumber

				+ SMFinderFunctions.getStdITEMWithQtysSearchAndResultString(detail.getM_sLocationCode())

				+ " &" + FinderResults.ADDITIONAL_WHERE_CLAUSE_PARAMETER + "=(" 
					+ "(" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation + " = '" + detail.getM_sLocationCode() + "')"
					+ " OR (" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation + " IS NULL)"
				+ ")"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sDedicatedToOrderNumber + " = '')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.iActive + " = 1)"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.icannotbesold + " = 0)"
				
				+ " &" + FinderResults.FINDER_BOX_TITLE + "=ACTIVE, NON-DEDICATED items <I>showing qtys for location : '" + detail.getM_sLocationCode() + "'</I>. +\n"
				
				+ "&ParameterString="
				+ "*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "*" + SMOrderDetail.Paramstrimmedordernumber + "=" + detail.getM_strimmedordernumber()
				+ "*" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" 
					+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
				+ "*" + SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG + "=" 
					+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG, request)
				+ "*" + SMEditOrderDetailEdit.UPDATE_ITEM_DATA_FLAG + "=TRUE"
				+ "*" + SMEditOrderDetailEdit.INSERTNEWLINEABOVEDETAILNUMBER + "=" 
				+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderDetailEdit.INSERTNEWLINEABOVEDETAILNUMBER, request)

				+ "*CallingClass=" + smaction.getCallingClass()
			;
			//Store the detail info we have so far in the session:
			smaction.getCurrentSession().setAttribute(SMOrderDetail.ParamObjectName, detail);
			redirectProcess(sRedirectString, response);
			return;
    	}
    	
    	//If it's a request to SAVE the detail:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMEditOrderDetailEdit.COMMAND_FLAG, request).compareToIgnoreCase(
    					SMEditOrderDetailEdit.SAVECOMMAND_VALUE) == 0){
    		String sCompanyName = "";
    		try {
				sCompanyName = (String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
			} catch (Exception e1) {
				//Nothing to do here - couldn't get the company name from the session, so it will remain blank.
			}
    		if (!detail.save_line_and_update_order(
				getServletContext(), 
				smaction.getsDBID(), 
				smaction.getUserID(),
				smaction.getFullUserName(),
				sCompanyName,
				smaction.getCallingClass(),
				(String) clsManageRequestParameters.get_Request_Parameter(SMEditOrderDetailEdit.INSERTNEWLINEABOVEDETAILNUMBER, request))
				){

    			System.out.println("[1551731866] - failed to save line");
    			
				try {
					smaction.getCurrentSession().setAttribute(SMOrderDetail.ParamObjectName, detail);
				} catch (Exception e) {
					//Session may have already been invalidated - in that case, just trap the error, but there's nothing else to do:
					System.out.println("Error [1389889793] in getCurrentSession.setAttribute - " + e.getMessage());
				}
				smaction.redirectAction(
						"Could not save: " + detail.getErrorMessages(), 
						"", 
						SMOrderDetail.Paramstrimmedordernumber + "=" + detail.getM_strimmedordernumber()
						+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
						+ "&" + SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG, request)
						+ "&" + SMEditOrderDetailEdit.INSERTNEWLINEABOVEDETAILNUMBER + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderDetailEdit.INSERTNEWLINEABOVEDETAILNUMBER, request)
				);
				return;
    		}else{
				if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
					smaction.returnToOriginalURL();
				}else{
					smaction.redirectAction(
						"", 
						"Order number " + detail.getM_strimmedordernumber() 
							+ ", line " + detail.getM_iLineNumber() + " was successfully saved.",
						SMOrderDetail.Paramstrimmedordernumber + "=" + detail.getM_strimmedordernumber()
						+ "&" + SMOrderDetail.ParamiDetailNumber + "=" + detail.getM_iDetailNumber()
					);
				}
				return;
			}
    	}
    	
    	//If it's a request to SAVE AND ADD ANOTHER:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMEditOrderDetailEdit.COMMAND_FLAG, request).compareToIgnoreCase(
    				SMEditOrderDetailEdit.SAVEANDADDCOMMAND_VALUE) == 0){
    		if (!detail.save_line_and_update_order(
				getServletContext(), 
				smaction.getsDBID(), 
				smaction.getUserID(),
				smaction.getFullUserName(),
				(String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME),
				smaction.getCallingClass(),
				(String) clsManageRequestParameters.get_Request_Parameter(SMEditOrderDetailEdit.INSERTNEWLINEABOVEDETAILNUMBER, request))
				){

				smaction.getCurrentSession().setAttribute(SMOrderDetail.ParamObjectName, detail);
				smaction.redirectAction(
						"Could not save: " + detail.getErrorMessages(), 
						"", 
						SMOrderDetail.Paramstrimmedordernumber + "=" + detail.getM_strimmedordernumber()
						+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
						+ "&" + SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG, request)
						+ "&" + SMEditOrderDetailEdit.INSERTNEWLINEABOVEDETAILNUMBER + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderDetailEdit.INSERTNEWLINEABOVEDETAILNUMBER, request)
				);
				return;
    		}else{
				//If the save succeeded, then we have to call the order detail edit, but tell it we
    			//want to add a NEW line:
    			
    			//First, we have to get the door label from the last line on the order to use it for the
    			//newly added line:
    			String SQL = "SELECT"
        			+ " " + SMTableorderdetails.sLabel
        			+ " FROM " + SMTableorderdetails.TableName
        			+ " WHERE ("
        				+ "(" + SMTableorderdetails.strimmedordernumber + " = '" + detail.getM_strimmedordernumber() + "')"
        			+ ") ORDER BY " + SMTableorderdetails.iLineNumber + " DESC LIMIT 1"
        		;
        		String sLastSiteLabel = "";
        		try {
        			ResultSet rsDetails = clsDatabaseFunctions.openResultSet(
        				SQL, 
        				getServletContext(), 
        				smaction.getsDBID(),
        				"MySQL",
        				smaction.getCallingClass() + " - adding another line"
        			);
    				if (rsDetails.next()){
    					sLastSiteLabel = rsDetails.getString(SMTableorderdetails.sLabel).trim();
    					if (sLastSiteLabel.compareToIgnoreCase("Not Selected") == 0){
    						sLastSiteLabel = "";
    					}
    				}
    				rsDetails.close();
    			} catch (SQLException e) {
    				smaction.redirectAction(
    						"Error reading label from last line - " + e.getMessage(), 
    						"", 
    						SMOrderHeader.Paramstrimmedordernumber + "=" + detail.getM_strimmedordernumber()
    					);
    				return;
    			}
    			
				if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
					smaction.returnToOriginalURL();
				}else{
					smaction.redirectAction(
						"", 
						"Order number " + detail.getM_strimmedordernumber() 
						+ ", line " + detail.getM_iLineNumber() + " was successfully saved.",
						SMOrderDetail.Paramstrimmedordernumber + "=" + detail.getM_strimmedordernumber()
						+ "&" + SMOrderDetail.ParamiDetailNumber + "=-1"
						+ "&" + SMEditOrderDetailEdit.LASTSITELABEL_PARAM + "=" + clsServletUtilities.URLEncode(sLastSiteLabel)
						+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=Y" 
					);
				}
				return;
			}
    	}

    	//If it's a request to SAVE AND GO TO THE NEXT LINE:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMEditOrderDetailEdit.COMMAND_FLAG, request).compareToIgnoreCase(
    				SMEditOrderDetailEdit.SAVEANDGOTONEXTCOMMAND_VALUE) == 0){
    		if (!detail.save_line_and_update_order(
				getServletContext(), 
				smaction.getsDBID(), 
				smaction.getUserID(),
				smaction.getFullUserName(),
				(String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME),
				smaction.getCallingClass(),
				(String) clsManageRequestParameters.get_Request_Parameter(SMEditOrderDetailEdit.INSERTNEWLINEABOVEDETAILNUMBER, request))
				){
    			//System.out.println("[1111111111111]");
				smaction.getCurrentSession().setAttribute(SMOrderDetail.ParamObjectName, detail);
				smaction.redirectAction(
						"Could not save: " + detail.getErrorMessages(), 
						"", 
						SMOrderDetail.Paramstrimmedordernumber + "=" + detail.getM_strimmedordernumber()
						+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
						+ "&" + SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG, request)
						+ "&" + SMEditOrderDetailEdit.INSERTNEWLINEABOVEDETAILNUMBER + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderDetailEdit.INSERTNEWLINEABOVEDETAILNUMBER, request)
				);
				return;
    		}else{
				//If the save succeeded, then we have to call the order detail edit, but tell it we
    			//want the NEXT line:
				if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
					smaction.returnToOriginalURL();
				}else{
					//Here we pass in the line number, instead of the detail number
					//and SMEditOrderDetailEdit will use that to load the next detail:
					//		+ "&" + SMOrderDetail.ParamiLineNumber + "=" 
					//		+ Integer.toString(Integer.parseInt(detail.getM_iLineNumber()) + 1));
					smaction.redirectAction(
						"", 
						"Order number " + detail.getM_strimmedordernumber() 
							+ ", line " + detail.getM_iLineNumber() + " was successfully saved.",
							SMOrderDetail.Paramstrimmedordernumber + "=" + detail.getM_strimmedordernumber()
							+ "&" + SMOrderDetail.ParamiLineNumber + "=" 
								+ Integer.toString(Integer.parseInt(detail.getM_iLineNumber()) + 1)
					);
				}
				return;
			}
    	}
    	
    	//If it's a request to SAVE AND INSERT A NEW LINE:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMEditOrderDetailEdit.COMMAND_FLAG, request).compareToIgnoreCase(
    				SMEditOrderDetailEdit.SAVEANDINSERTNEWCOMMAND_VALUE) == 0){
    		if (!detail.save_line_and_update_order(
				getServletContext(), 
				smaction.getsDBID(), 
				smaction.getUserID(),
				smaction.getFullUserName(),
				(String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME),
				smaction.getCallingClass(),
				(String) clsManageRequestParameters.get_Request_Parameter(SMEditOrderDetailEdit.INSERTNEWLINEABOVEDETAILNUMBER, request))
				){

				smaction.getCurrentSession().setAttribute(SMOrderDetail.ParamObjectName, detail);
				smaction.redirectAction(
						"Could not save: " + detail.getErrorMessages(), 
						"", 
						SMOrderDetail.Paramstrimmedordernumber + "=" + detail.getM_strimmedordernumber()
						+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
						+ "&" + SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG, request)
						+ "&" + SMEditOrderDetailEdit.INSERTNEWLINEABOVEDETAILNUMBER + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderDetailEdit.INSERTNEWLINEABOVEDETAILNUMBER, request)
				);
				return;
    		}else{
				//If the save succeeded, then we have to call the order detail edit, but tell it we
    			//want to INSERT a NEW line AFTER the last one:
				if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
					smaction.returnToOriginalURL();
				}else{

					//First we have to get the detail number of the line AFTER the one we just updated:
					String SQL = "SELECT"
						+ " " + SMTableorderdetails.iDetailNumber
						+ " FROM " + SMTableorderdetails.TableName
						+ " WHERE ("
							+ "(" + SMTableorderdetails.iLineNumber + " > " + detail.getM_iLineNumber() + ")"
							+ " AND (" + SMTableorderdetails.strimmedordernumber + " = '" + detail.getM_strimmedordernumber() + "')"
						+ ") ORDER BY " + SMTableorderdetails.iLineNumber
						+ " LIMIT 1"
					;
					String sNextDetailNumber = "";
					try {
						ResultSet rsLines = clsDatabaseFunctions.openResultSet(
							SQL,
							getServletContext(),
							smaction.getsDBID(),
							"MySQL", 
							this.toString() + " - save and insert line; user: " + smaction.getUserID()
							+ " - "
							+ smaction.getFullUserName()
								);
						if (rsLines.next()){
							sNextDetailNumber = Long.toString(rsLines.getLong(SMTableorderdetails.iDetailNumber));
						}else{
							sNextDetailNumber = "9999";
						}
						rsLines.close();
					} catch (SQLException e) {
						smaction.redirectAction(
								"Error reading next line number with SQL: "+ SQL + " - " + e.getMessage() + ".", 
								"Order number " + detail.getM_strimmedordernumber() 
								+ ", line " + detail.getM_iLineNumber() + " was successfully saved.",
								SMOrderDetail.Paramstrimmedordernumber + "=" + detail.getM_strimmedordernumber()
								+ "&" + SMOrderDetail.ParamiDetailNumber + "=-1"
								+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=Y" 
							);
						return;
					}
					
					smaction.redirectAction(
						"", 
						"Order number " + detail.getM_strimmedordernumber() 
						+ ", line " + detail.getM_iLineNumber() + " was successfully saved.",
						SMOrderDetail.Paramstrimmedordernumber + "=" + detail.getM_strimmedordernumber()
						+ "&" + SMOrderDetail.ParamiDetailNumber + "=-1"
						+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=true" 
						+ "&" + SMEditOrderDetailEdit.INSERTNEWLINEABOVEDETAILNUMBER + "=" + sNextDetailNumber
						+ "&" + SMEditOrderDetailEdit.LASTSITELABEL_PARAM + "=" + clsServletUtilities.URLEncode(detail.getM_sLabel())
					);
				}
				return;
			}
    	}
    	
		return;
	}
	private void redirectProcess(String sRedirectString, HttpServletResponse res ){
		try {
			res.sendRedirect(sRedirectString);
		} catch (IOException e1) {
			System.out.println("In " + this.toString() + ".redirectAction - IOException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
			return;
		} catch (IllegalStateException e1) {
			System.out.println("In " + this.toString() + ".redirectAction - IllegalStateException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
			return;
		}
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}