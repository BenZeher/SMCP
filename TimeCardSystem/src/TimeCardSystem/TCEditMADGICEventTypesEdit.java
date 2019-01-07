package TimeCardSystem;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsManageRequestParameters;
import TCSDataDefinition.TCSTablemadgiceventtypes;

public class TCEditMADGICEventTypesEdit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String MAIN_FORM_NAME = "MADGICEventTypeForm";
	private static final String CALLED_CLASS = "TimeCardSystem.TCEditMADGICEventTypesAction";
	
	public static final String SUBMIT_EDIT_BUTTON_NAME = "SUBMITEDIT";
	public static final String SUBMIT_EDIT_BUTTON_LABEL = "Update";
	public static final String SUBMIT_DELETE_BUTTON_NAME = "SUBMITDELETE";
	public static final String SUBMIT_DELETE_BUTTON_LABEL = "Delete";
	public static final String CONFIRM_DELETE_CHECKBOX_NAME = "CONFIRMDELETE";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
	    String sUser = (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID);
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    
		TCMADGICEventType entry = new TCMADGICEventType(request);

		//If this is a 'resubmit', meaning it's being called by the 'Action' class, then
		//the session will have an entry object in it, and that's what we'll pick up.
		
	    if (CurrentSession.getAttribute(TCMADGICEventType.ParamObjectName) != null){
	    	entry = (TCMADGICEventType) CurrentSession.getAttribute(TCMADGICEventType.ParamObjectName);
	    	CurrentSession.removeAttribute(TCMADGICEventType.ParamObjectName);
	    	
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	//Unless we are trying to create a NEW type, load the existing type:
	    	if (entry.get_slid().compareToIgnoreCase("-1") != 0){
	    		try {
					entry.load(sDBID, getServletContext(), sUser);
				} catch (Exception e) {
					response.sendRedirect(
							"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
							+ "?Warning=" + e.getMessage()
						);
						return;
				}
	    	}
	    }
	    
	    String sTitle = "Edit MADGIC Event Types";
	    String sSubtitle = "";
	    out.println(TimeCardUtilities.TCTitleSubBGColorWithFont(sTitle, sSubtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS, TimeCardUtilities.BASE_FONT_FAMILY));
	    
	    out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) 
	    	+ "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
	    
	    
    	String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
    	if (sWarning.compareToIgnoreCase("") != 0){
    		out.println("<BR><B><FONT COLOR=RED>Error [1486576245] editing MADGIC Event Type - " + sWarning + ".</FONT></B><BR>");
    	}
    	String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
    	if (sStatus.compareToIgnoreCase("") != 0){
    		out.println("<BR><B><FONT COLOR=YELLOW>" + sStatus + "</FONT></B><BR>");
    	}
	    
	    out.println("<FORM ID='\"" + MAIN_FORM_NAME + "'\" NAME=\"" + MAIN_FORM_NAME + "\" ACTION=\"" 
			+ TimeCardUtilities.getURLLinkBase(getServletContext()) + CALLED_CLASS + "\""
			+ " METHOD=\"POST\">"
		);
			
	    out.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" + this.getClass().getName() + "\">");

	    out.println("<TABLE BORDER=1>");

	    String sID = "NEW";
	    if (entry.get_slid().compareToIgnoreCase("-1") != 0){
	    	sID = entry.get_slid();
	    }
	    
		out.println("<TR>"
			+ "<TD ALIGN=RIGHT><B>" + "ID:" + " </B></TD>"
			+ "<TD ALIGN=LEFT>" + sID + "</TD>"
			+ "<TD ALIGN=LEFT>" 
			+ "&nbsp;" 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + TCSTablemadgiceventtypes.lid + "\" VALUE=\"" + entry.get_slid() + "\">"
			+ "</TD>"
			+ "</TR>"
		);
	    
	    //Name
		if (entry.get_slid().compareToIgnoreCase("-1") != 0){
			out.println("<TR>"
				+ "<TD ALIGN=RIGHT><B>" + "Name:" + " </B></TD>"
				+ "<TD ALIGN=LEFT>" + entry.get_sname() 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + TCSTablemadgiceventtypes.sname + "\" VALUE=\"" + entry.get_sname() + "\">"
				+ "</TD>"
				+ "<TD ALIGN=LEFT>" + "A unique name for this event type" + "</TD>"
				+ "</TR>"
			);
		}else{
		    out.println(TimeCardUtilities.Create_Edit_Form_Text_Input_Row(
		    	TCSTablemadgiceventtypes.sname,
		    	entry.get_sname(), 
		    	TCSTablemadgiceventtypes.snameLength, 
		    	"Name:", 
		    	"A unique name for this event type", 
		    	"40", 
		    	"")
		    );
		}

	    //Description
	    out.println(TimeCardUtilities.Create_Edit_Form_Text_Input_Row(
	    	TCSTablemadgiceventtypes.sdescription,
	    	entry.get_sdescription(), 
	    	TCSTablemadgiceventtypes.sdescriptionLength, 
	    	"Description:", 
	    	"A more detailed description for this event type", 
	    	"80", 
	    	"")
	    );
	    
	    //Number of points
	    out.println(TimeCardUtilities.Create_Edit_Form_Text_Input_Row(
	    	TCSTablemadgiceventtypes.inumberofpoints,
	    	entry.get_snumberofpoints(), 
	    	3, 
	    	"Number of points:", 
	    	"The DEFAULT number of points - this can be overridden when entering actual events", 
	    	"8", 
	    	"")
	    );

		out.println("</TABLE>");
	    
		out.println("<P><INPUT TYPE=SUBMIT NAME='" 
			+ SUBMIT_EDIT_BUTTON_NAME 
			+ "' VALUE='" + SUBMIT_EDIT_BUTTON_LABEL + "' STYLE='height: 0.24in'>"
		);
	    
	    
		out.println("<INPUT TYPE=SUBMIT NAME='" 
			+ SUBMIT_DELETE_BUTTON_NAME 
			+ "' VALUE=\"" + SUBMIT_DELETE_BUTTON_LABEL + "\"" + TCMADGICEventType.ParamObjectName 
			+ "' STYLE='height: 0.24in'>"
			+ "  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"" 
			+ CONFIRM_DELETE_CHECKBOX_NAME + "\"></P>"
		);
		
	    return;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
