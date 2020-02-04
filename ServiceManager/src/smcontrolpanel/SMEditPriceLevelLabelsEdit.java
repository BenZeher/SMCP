package smcontrolpanel;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTablepricelistlevellabels;

public class SMEditPriceLevelLabelsEdit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String REQUIRED_FIELD_FLAG = "<FONT COLOR=RED><B>*</B></FONT>";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		SMPriceLevelLabels entry = new SMPriceLevelLabels();
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				SMPriceLevelLabels.ParamObjectName,
				SMUtilities.getFullClassName(this.toString()),
				SMUtilities.getFullClassName("smcontrolpanel.SMEditPriceLevelLabelsAction"),
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditPriceLevelLabels
		);    
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditPriceLevelLabels, request)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		//Get any object out of the session immediately, so we don't leave it in:
		HttpSession currentSession = smedit.getCurrentSession();
		if (currentSession.getAttribute(SMPriceLevelLabels.ParamObjectName) != null){
			entry = (SMPriceLevelLabels) currentSession.getAttribute(SMPriceLevelLabels.ParamObjectName);
			currentSession.removeAttribute(SMPriceLevelLabels.ParamObjectName);
		}else{
			//If there's no object in the session, then load the current record:
			try {
				entry.load(smedit.getsDBID(), getServletContext(), smedit.getUserName());
			} catch (Exception e) {
				smedit.getPWOut().println("<B><FONT COLOR=RED>Error [2020351556273] " + "could not load price level labels - " 
					+ e.getMessage() + "</FONT></B><BR>");
				return;
			}
		}
		
	    smedit.getPWOut().println(SMUtilities.getSMCPJSIncludeString(getServletContext()));
	    smedit.getPWOut().println(SMUtilities.getMasterStyleSheetLink());
	    smedit.getPWOut().println(SMUtilities.getShortcutJSIncludeString(getServletContext()));
	    smedit.printHeaderTable();
	    smedit.setbIncludeDeleteButton(false);

	    try {
			smedit.createEditPage(getEditHTML(smedit, entry), "");
		} catch (Exception e) {
    		smedit.getPWOut().println("<BR><BR><FONT COLOR=RED><B>Error [1580850127] creating edit screen for price level labels - " 
    			+ e.getMessage() + "</B></FONT>");
				return;
		}
	    
	    return;
	    
}

	private String getEditHTML(SMMasterEditEntry smedit, SMPriceLevelLabels entry) throws Exception	{
		String s = "";
		//Start the table:
		s += "<TABLE BORDER=12 CELLSPACING=2>";		
	
       
	    //Base price
        s += "  <TR>" + "\n"
    	  + "    <TD ALIGN=RIGHT><B>" + "Base price level label" + REQUIRED_FIELD_FLAG + ":"  + " </B></TD>" + "\n"
    	  + "    <TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + SMTablepricelistlevellabels.sbasepricelabel + "\""
    	  + " VALUE=\"" + entry.get_sbaselabel().replace("\"", "&quot;") + "\""
    	  + " MAXLENGTH=" + Integer.toString(SMTablepricelistlevellabels.sbasepricelabelLength)
    	  + " STYLE=\"height: 0.25in\""
    	  + "></TD>" + "\n"
    	  + "    <TD ALIGN=LEFT><I>" 
    	  + "Label for the 'Base Price' level" 
    	  + "</I></TD>" + "\n"
    	  + "  </TR>" + "\n\n"
    	  ;

	    //Level 1
        s += "  <TR>" + "\n"
    	  + "    <TD ALIGN=RIGHT><B>" + "Level 1 label" + REQUIRED_FIELD_FLAG + ":"  + " </B></TD>" + "\n"
    	  + "    <TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + SMTablepricelistlevellabels.spricelevel1label + "\""
    	  + " VALUE=\"" + entry.get_slevel1label().replace("\"", "&quot;") + "\""
    	  + " MAXLENGTH=" + Integer.toString(SMTablepricelistlevellabels.spricelevel1labelLength)
    	  + " STYLE=\"height: 0.25in\""
    	  + "></TD>" + "\n"
    	  + "    <TD ALIGN=LEFT><I>" 
    	  + "Label for the 'Level 1' price level" 
    	  + "</I></TD>" + "\n"
    	  + "  </TR>" + "\n\n"
    	  ;
        
	    //Level 2
        s += "  <TR>" + "\n"
    	  + "    <TD ALIGN=RIGHT><B>" + "Level 2 label" + REQUIRED_FIELD_FLAG + ":"  + " </B></TD>" + "\n"
    	  + "    <TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + SMTablepricelistlevellabels.spricelevel2label + "\""
    	  + " VALUE=\"" + entry.get_slevel2label().replace("\"", "&quot;") + "\""
    	  + " MAXLENGTH=" + Integer.toString(SMTablepricelistlevellabels.spricelevel2labelLength)
    	  + " STYLE=\"height: 0.25in\""
    	  + "></TD>" + "\n"
    	  + "    <TD ALIGN=LEFT><I>" 
    	  + "Label for the 'Level 2' price level" 
    	  + "</I></TD>" + "\n"
    	  + "  </TR>" + "\n\n"
    	  ;
        
	    //Level 3
        s += "  <TR>" + "\n"
    	  + "    <TD ALIGN=RIGHT><B>" + "Level 3 label" + REQUIRED_FIELD_FLAG + ":"  + " </B></TD>" + "\n"
    	  + "    <TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + SMTablepricelistlevellabels.spricelevel3label + "\""
    	  + " VALUE=\"" + entry.get_slevel3label().replace("\"", "&quot;") + "\""
    	  + " MAXLENGTH=" + Integer.toString(SMTablepricelistlevellabels.spricelevel3labelLength)
    	  + " STYLE=\"height: 0.25in\""
    	  + "></TD>" + "\n"
    	  + "    <TD ALIGN=LEFT><I>" 
    	  + "Label for the 'Level 3' price level" 
    	  + "</I></TD>" + "\n"
    	  + "  </TR>" + "\n\n"
    	  ;
        
	    //Level 4
        s += "  <TR>" + "\n"
    	  + "    <TD ALIGN=RIGHT><B>" + "Level 4 label" + REQUIRED_FIELD_FLAG + ":"  + " </B></TD>" + "\n"
    	  + "    <TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + SMTablepricelistlevellabels.spricelevel4label + "\""
    	  + " VALUE=\"" + entry.get_slevel4label().replace("\"", "&quot;") + "\""
    	  + " MAXLENGTH=" + Integer.toString(SMTablepricelistlevellabels.spricelevel4labelLength)
    	  + " STYLE=\"height: 0.25in\""
    	  + "></TD>" + "\n"
    	  + "    <TD ALIGN=LEFT><I>" 
    	  + "Label for the 'Level 4' price level" 
    	  + "</I></TD>" + "\n"
    	  + "</TR>" + "\n\n"
    	  ;
        
	    //Level 5
        s += "  <TR>" + "\n"
    	  + "    <TD ALIGN=RIGHT><B>" + "Level 5 label" + REQUIRED_FIELD_FLAG + ":"  + " </B></TD>" + "\n"
    	  + "    <TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + SMTablepricelistlevellabels.spricelevel5label + "\""
    	  + " VALUE=\"" + entry.get_slevel5label().replace("\"", "&quot;") + "\""
    	  + " MAXLENGTH=" + Integer.toString(SMTablepricelistlevellabels.spricelevel5labelLength)
    	  + " STYLE=\"height: 0.25in\""
    	  + "></TD>" + "\n"
    	  + "    <TD ALIGN=LEFT><I>" 
    	  + "Label for the 'Level 5' price level" 
    	  + "</I></TD>" + "\n"
    	  + "  </TR>" + "\n\n"
    	  ;
        
        s += "</TABLE>";
        
        s += "<BR>"
        	+ "&nbsp;" + REQUIRED_FIELD_FLAG + " Indicates a REQUIRED field";

		return s;
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}