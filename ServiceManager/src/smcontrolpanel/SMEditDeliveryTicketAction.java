package smcontrolpanel;

import java.io.IOException;
//import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.SMDeliveryTicket;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTablecompanyprofile;
import SMDataDefinition.SMTabledeliverytickets;
import SMDataDefinition.SMTablesmoptions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsEmailInlineHTML;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import smar.SMOption;


public class SMEditDeliveryTicketAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static final String DELIVERY_TICKET_EMAIL_SUBJECT = "Delivery Ticket Receipt";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
	
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMManageDeliveryTickets)){return;}
		smaction.getCurrentSession().removeAttribute(SMDeliveryTicket.ParamObjectName);
	   
		//Read the entry fields from the request object:
		SMDeliveryTicket entry = new SMDeliveryTicket(request);	

		 //First get the command value that was passed in:
	    String sCommandValue = clsManageRequestParameters.get_Request_Parameter(SMDeliveryTicketSignatureEdit.COMMAND_FLAG, request);
	    
		//If it's a request to delete:
	    if(sCommandValue.compareToIgnoreCase(SMEditDeliveryTicketEdit.DELETE_COMMAND_VALUE) == 0){
	       
	    	String sOrderNumber = entry.getstrimmedordernumber();
		    if (clsManageRequestParameters.get_Request_Parameter(SMEditDeliveryTicketEdit.CONFIRM_DELETE_CHECKBOX, request)
		    	.compareToIgnoreCase(SMEditDeliveryTicketEdit.CONFIRM_DELETE_CHECKBOX) == 0){
			    //Save this now so it's not lost after the delete:
			    String sLid = entry.getslid();
			    try {
					entry.delete(getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName());
				} catch (Exception e) {
			    	smaction.redirectAction(
				    		"Could not delete: " + e.getMessage(), 
				    		"", 
				    		SMDeliveryTicket.Paramlid + "=" + entry.getslid()
				    		+ "&" + SMDeliveryTicket.Paramstrimmedordernumber + "=" + sOrderNumber
				    		);
						return;
				}

		    	//If the delete succeeded, the entry will be initialized:
				if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
					smaction.returnToOriginalURL();
				}else{
					String sRedirectString = 
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditDeliveryTicketEdit"
						+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
						+ "&" + SMDeliveryTicket.Paramlid + "=-1"
						+ "&Status=" + entry.getObjectName() + ": " + sLid + " was successfully deleted."
						+ "&" + SMDeliveryTicket.Paramstrimmedordernumber + "=" + sOrderNumber
						;
					try {
						response.sendRedirect(sRedirectString);
					} catch (IOException e) {
						smaction.getPwOut().println("In " + this.toString() 
							+ ".redirectAction - IOException error redirecting with string: "
							+ clsServletUtilities.URLEncode(sRedirectString + " - " + e.getMessage())
						);
					} catch (IllegalStateException e) {
						smaction.getPwOut().println("In " + this.toString() 
							+ ".redirectAction - IllegalStateException error redirecting with string: "
							+ clsServletUtilities.URLEncode(sRedirectString + " - " + e.getMessage())
						);
					}
				}
				return;
	    	}else{
	    		smaction.getCurrentSession().setAttribute(SMDeliveryTicket.ParamObjectName, entry);
				smaction.redirectAction(
					"You chose to delete, but did not check the CONFIRM checkbox.", 
					"", 
					SMDeliveryTicket.Paramlid + "=" + entry.getslid()
					+ "&" + SMDeliveryTicket.Paramstrimmedordernumber + "=" + sOrderNumber
				);
				return;
	    	}
	    }
		
		//If it's an edit, process that:
	    if(sCommandValue.compareToIgnoreCase(SMEditDeliveryTicketEdit.SAVE_COMMAND_VALUE) == 0){
	    	if(entry.getiposted().compareToIgnoreCase("1") == 0){
	    		smaction.redirectAction(
					"", 
					SMDeliveryTicket.ParamObjectName + " is already posted, and cannot be updated",
					SMDeliveryTicket.Paramlid + "=" + entry.getslid()
				);
	    		return;
	    	}
	    	try {
				entry.save_without_data_transaction(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserName(),
					smaction.getUserID(),
					smaction.getFullUserName()
				);
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(SMDeliveryTicket.ParamObjectName, entry);
				smaction.redirectAction(
					"Could not save: " + e.getMessage(), 
					"", 
					SMDeliveryTicket.Paramlid + "=" + entry.getslid()
				);
				return;
			}
			//If the save succeeded, force the called function to reload it by NOT
			//putting the entry object in the current session, but by passing it
			//the lid instead:
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				smaction.redirectAction(
					"", 
					entry.getObjectName() + ": " + entry.getslid() + " was successfully saved.",
					SMDeliveryTicket.Paramlid + "=" + entry.getslid()
				);
			}
	    }
	    
	  //If it's a request to post the delivery ticket:
    	if (sCommandValue.compareToIgnoreCase(SMEditDeliveryTicketEdit.POST_COMMAND_VALUE) == 0){
		
			if(entry.getslid().compareToIgnoreCase("-1") != 0){
	    		try {
					entry.post_without_data_transaction(
						getServletContext(), 
						smaction.getsDBID(), 
						smaction.getUserName() );
				} catch (Exception e) {
					smaction.getCurrentSession().setAttribute(SMDeliveryTicket.ParamObjectName, entry);
					smaction.redirectAction(
							"Could not post: " + e.getMessage(), 
							"", 
							SMDeliveryTicket.Paramlid + "=" + entry.getslid()				
					);
					return;
				}
			}else{
				smaction.redirectAction(
						SMDeliveryTicket.ParamObjectName + " must be updated before it is posted.", 
						"",
						SMDeliveryTicket.Paramlid + "=" + entry.getslid()
					);
			}
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				smaction.redirectAction(
					"", 
					SMDeliveryTicket.ParamObjectName + " was successfully posted.",
					SMDeliveryTicket.Paramlid + "=" + entry.getslid()
				);
			}
    	}
    	//If its Print Delivery ticket, process that:
 	   if(sCommandValue.compareToIgnoreCase(SMEditDeliveryTicketEdit.PRINT_COMMAND_VALUE) == 0){
 		  try {
				smaction.getPwOut().println(getHTMLDeliveryTicketForm(
					1, 
					entry.getslid(), 
					smaction.getsDBID(),
					smaction,
					false,
					"")
				);
			} catch (Exception e1) {
				smaction.getCurrentSession().setAttribute(SMDeliveryTicket.ParamObjectName, entry);
				
				smaction.redirectAction(
					e1.getMessage(), 
					"",
					SMDeliveryTicket.Paramlid + "=" + entry.getslid()
				);
			}
			return;
 	   }
 	   
 	  	 //If its Email Delivery ticket, process that:
 	   if(sCommandValue.compareToIgnoreCase(SMEditDeliveryTicketEdit.EMAIL_COMMAND_VALUE) == 0){
 		//First we'll need to reload the delivery ticket, because the HTTP request won't carry all the fields we'll need,
   		//in particular the 'signature' which is a JSON string:
   		try {
				entry.load(getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName());
			} catch (Exception e1) {
				smaction.getCurrentSession().setAttribute(SMDeliveryTicket.ParamObjectName, entry);
				smaction.redirectAction(
					e1.getMessage(), 
					"",
					SMDeliveryTicket.Paramlid + "=" + entry.getslid()
				);
				return;
			}
   		try {
			sendEmail(request, entry, smaction);
			} catch (Exception e) {
			/*	log.writeEntry(
					smaction.getUserName(),
					SMLogEntry.LOG_OPERATION_DELIVERYTICKETEMAIL, 
					"Delivery Ticket #: " + entry.getslid(), 
					"Attempted to send to: '" + SMUtilities.get_Request_Parameter(SMEditDeliveryTicketEdit.EMAIL_TO_FIELD, request) + "'"
						+ " - Error message: " + e.getMessage(), 
					"[1417634373]"); */
				smaction.getCurrentSession().setAttribute(SMDeliveryTicket.ParamObjectName, entry);
				smaction.redirectAction(
					e.getMessage(), 
					"",
					SMDeliveryTicket.Paramlid + "=" + entry.getslid()
				);
				return;
			}
			/*log.writeEntry(
					smaction.getUserName(),
					SMLogEntry.LOG_OPERATION_DELIVERYTICKETEMAIL, 
					"Delivery Ticket #: " + entry.getslid(), 
					 "Sent to: '" + SMUtilities.get_Request_Parameter(SMEditDeliveryTicketEdit.EMAIL_TO_FIELD, request) + "'", 
					"[1417634374]");*/
			smaction.redirectAction(
				"", 
				"Email sent to " + clsManageRequestParameters.get_Request_Parameter(SMEditDeliveryTicketEdit.EMAIL_TO_FIELD, request) + ".",
				SMDeliveryTicket.Paramlid + "=" + entry.getslid()
			);
			return;
 		   
 	   }
 	   
	    //If its Accept Signature, process that:
	   if(sCommandValue.compareToIgnoreCase(SMEditDeliveryTicketEdit.ACCEPT_SIGNATURE_COMMAND_VALUE) == 0){
			
		   if(entry.getstrimmedordernumber() != null && !entry.getstrimmedordernumber().isEmpty()
				  && Integer.parseInt(entry.getslid()) >= 0 && entry.getsdetaillines() != null && !entry.getsdetaillines().isEmpty()){
			
			String sRedirectString = 
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMDeliveryTicketSignatureEdit"
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
					+ "&" + SMDeliveryTicket.Paramlid + "=" + entry.getslid()
			;
			redirectProcess(sRedirectString, response);
		   }else{
			   //smaction.getCurrentSession().setAttribute(SMDeliveryTicket.ParamObjectName, entry);
			   smaction.redirectAction(
					"You need to save the delivery ticket before accepting a signature", 
					"",
					SMDeliveryTicket.Paramlid + "=" + entry.getslid());
		   }
		   return;
	   }
	   
		return;
	}
	
	private void redirectProcess(String sRedirectString, HttpServletResponse res ){
		try {
			res.sendRedirect(sRedirectString);
		} catch (IOException e1) {
			System.out.println("Error [1445258625] in " + this.toString() + ".redirectAction - IOException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
			return;
		} catch (IllegalStateException e1) {
			System.out.println("Error [1445258625] in " + this.toString() + ".redirectAction - IllegalStateException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
			return;
		}
	}
	private String getHTMLDeliveryTicketForm(
			int iNumberOfCopies, 
			String sDeliveryTicketNumber, 
			String sDBID,
			SMMasterEditAction smaction,
			boolean bEmailMode, 
			String sEmailTo) throws Exception{
		String s = 
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">"
				+ "<HTML lang=\"" + SMUtilities.LANGUAGE_HTML_ENGLISH + "\"> <HEAD>" 
		   		+ "<STYLE TYPE=\"text/css\">P.nobreak {page-break-inside: avoid;}\n"
				+ "div.nobreak {page-break-inside: void;}\n"
		   		+ "H1.western { font-family: \"Arial\", sans-serif; font-size: 16pt; }\n"
		   		+ "H2.western { font-family: \"Arial\", sans-serif; font-size: 14pt; }\n"
		   		+ "H3.western { font-family: \"Arial\", sans-serif; font-size: 12pt; }\n"
		   		+ "H4.western { font-family: \"Arial\", sans-serif; font-size: 10pt; }\n"
		   		+ "@page { size:8.5in 11in; margin: 0.4in }\n"
		   		+ "@media print { div.nobreak {page-break-inside: avoid;}}"
		   		+ "</STYLE>"
		   		;
		//For printing signature:
		s += "<!--[if lt IE 9]><script src=\"scripts/flashcanvas.js\"></script><![endif]-->"
					+ "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js\"></script>";
		s += "</HEAD><BODY BGCOLOR=\"#FFFFFF\">"
		;
	 	//Retrieve information
	 	Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
					getServletContext(), 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".printProposal - user: " + smaction.getUserName()
					);
		} catch (Exception e1) {
	 		s += "<FONT COLOR=RED>Unable to get data connection - " + e1.getMessage() + ".</FONT>"
		 			+ "</BODY></HTML>"
		 			;
		 		return s;
		}
	 	
	 	SMDeliveryTicketReceipt delticket = new SMDeliveryTicketReceipt();
	 	
	 	SMLogEntry log = new SMLogEntry(smaction.getsDBID(), getServletContext());
	 	try {
			s += delticket.processReport(
				sDBID,
				sDeliveryTicketNumber,
				smaction.getUserName(),
				smaction.getUserID(),
				smaction.getFullUserName(),
				getServletContext(),
				bEmailMode
			);
			
			log.writeEntry(
				smaction.getUserID(), 
				SMLogEntry.LOG_OPERATION_PRINTINTERACTIVEDELIVERYTICKET, 
				"SUCCESSFULLY Printed delivery ticket number: '" + sDeliveryTicketNumber + "'", 
				"Email mode = '" + bEmailMode + "', emailed to '" + sEmailTo + "'", 
				"[1536256700]"
			);
			
		} catch (Exception e) {
			log.writeEntry(
				smaction.getUserID(), 
				SMLogEntry.LOG_OPERATION_PRINTINTERACTIVEDELIVERYTICKET, 
				"UNSUCCESSFULLY Printed delivery ticket number: '" + sDeliveryTicketNumber + "', Email mode = '" + bEmailMode + "', emailed to '" + sEmailTo + "'",
				"Error: " + e.getMessage(), 
				"[1536256701]"
			);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080483]");
	 		s += "<FONT COLOR=RED>Error - " + e.getMessage() + ".</FONT>"
		 			+ "</BODY></HTML>"
		 			;
		 		return s;
		}
	 		
	 	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080484]");
	 	s += "</BODY></HTML>";
	 	return s;
	}
	
	private void sendEmail(HttpServletRequest req, SMDeliveryTicket dt, SMMasterEditAction sm) throws Exception{
	
		String sEmailTo = clsManageRequestParameters.get_Request_Parameter(SMEditDeliveryTicketEdit.EMAIL_TO_FIELD, req);
	
		if (sEmailTo.compareToIgnoreCase("") == 0){
			throw new Exception("Email address cannot be blank.");
		}
		SMOption opt = new SMOption();
		try {
			opt.load(sm.getsDBID(), getServletContext(), sm.getUserName());
		} catch (Exception e) {
			throw new Exception("Error loading SM Options data to email delivery ticket receipt - " + e.getMessage() + ".");
		}
		String sSystemRootPath = SMUtilities.getAbsoluteRootPath(req, getServletContext());
		
		String sSubject = "";
		String SQL = "SELECT * FROM " + SMTablecompanyprofile.TableName;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".sendEmail - " + sm.getUserID()
				+ " - "
				+ sm.getFullUserName()
			);
			if (rs.next()){
				sSubject += rs.getString(SMTablecompanyprofile.sCompanyName).trim() + " - " + DELIVERY_TICKET_EMAIL_SUBJECT;
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1445618753] reading company profile - " + e.getMessage() + ".");
		}

		String sSignatureBoxWidth = dt.getlsignatureboxwidth();
		//If a signature has not been collected get the default dimensions for the signature.
		if(sSignatureBoxWidth.compareToIgnoreCase("0") == 0){
			sSignatureBoxWidth = opt.getisignatureboxwidth();
			dt.setlsignatureboxwidth(sSignatureBoxWidth);
		} 
		int iSignatureWidth = Integer.parseInt(sSignatureBoxWidth);
		String sSignatureBoxHeight = Integer.toString((int) (Math.round(iSignatureWidth/SMTablesmoptions.SIGNATURE_BOX_WIDTH_TO_HEIGHT_RATIO)));
		
		
		try {
			String sDeliveryTicketReceipt = getHTMLDeliveryTicketForm(1, dt.getslid(), sm.getsDBID(), sm, true, sEmailTo);
	        
            clsEmailInlineHTML.emailEmbeddedHTMLWithSignature(
            	sSystemRootPath,
            	dt.getmsignature(), 
            	Integer.parseInt(sSignatureBoxWidth),
	    		Integer.parseInt(sSignatureBoxHeight),
	    		Integer.parseInt(SMTabledeliverytickets.SIGNATURE_PEN_WIDTH),
	    		SMTabledeliverytickets.SIGNATURE_PEN_R_COLOUR,
	    		SMTabledeliverytickets.SIGNATURE_PEN_G_COLOUR,
	    		SMTabledeliverytickets.SIGNATURE_PEN_B_COLOUR,
            	opt.getSMTPServer(), 
            	opt.getSMTPUserName(), 
            	opt.getSMTPPassword(), 
            	sEmailTo,
            	opt.getSMTPReplyToAddress(),
            	sSubject, 
            	sDeliveryTicketReceipt, 
            	getServletContext());
		} catch (Exception e1) {
			throw new Exception(e1.getMessage());
		}
		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
