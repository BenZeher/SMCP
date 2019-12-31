package ConnectionPool;

import javax.servlet.ServletContext;

public class WebContextParameters {
		
    public static final String webappname = "webappname";
    public static final String imagepath = "imagepath";
    public static final String scriptpath = "scriptpath";
    public static final String smlocalresourcespath = "smlocalresources";
    public static final String programtitle = "programtitle";
    public static final String maximumnumberofconnections = "maximumnumberofconnections";
    public static final String logworkorderupdates = "logworkorderupdates";
    public static final String documentationpageURL = "documentationpageURL";
    public static final String smtempfolder = "smtempfolder";
    
	 public static String getURLLinkBase(ServletContext context){
		 String sSMAppName = context.getInitParameter(WebContextParameters.webappname);
		 String sURLBase = "";
		 if ((sSMAppName == null) || (sSMAppName.compareToIgnoreCase("")) == 0){
			 	sURLBase = "/servlet/";
			}else{
				sURLBase = "/" + sSMAppName + "/";
			}
			return sURLBase;
	 }

	 public static String getInitImagePath(ServletContext context){
		 String sInitParam = context.getInitParameter(WebContextParameters.imagepath);
		 if ((sInitParam == null) || (sInitParam.compareToIgnoreCase("")) == 0){
			 sInitParam = "images";
			}
			return sInitParam;
	 }
	 public static String getInitProgramTitle(ServletContext context){
		 String sInitParam = context.getInitParameter(WebContextParameters.programtitle);
		 if ((sInitParam == null) || (sInitParam.compareToIgnoreCase("")) == 0){
			 sInitParam = "SM Control Panel";
			}
			return sInitParam;
	 }
	 public static String getInitScriptPath(ServletContext context){
		 String sInitParam = context.getInitParameter(WebContextParameters.scriptpath);
		 if ((sInitParam == null) || (sInitParam.compareToIgnoreCase("")) == 0){
			 sInitParam = "scripts";
			}
			return sInitParam;
	 }
	 public static String getLocalResourcesPath(ServletContext context){
		 String sInitParam = context.getInitParameter(WebContextParameters.smlocalresourcespath);
		 if ((sInitParam == null) || (sInitParam.compareToIgnoreCase("")) == 0){
			 sInitParam = "smlocalresources";
			}
			return sInitParam;
	 }
	 public static String getInitWebAppName(ServletContext context){
		 String sInitParam = context.getInitParameter(WebContextParameters.webappname);
		 if ((sInitParam == null) || (sInitParam.compareToIgnoreCase("")) == 0){
			 sInitParam = "sm";
			}
			return sInitParam;
	 }	 
	 public static String getMaximumNumberOfConnections(ServletContext context){
		 String sInitParam = context.getInitParameter(WebContextParameters.maximumnumberofconnections);
		 if ((sInitParam == null) || (sInitParam.compareToIgnoreCase("")) == 0){
			 sInitParam = "30";
			}
			return sInitParam;
	 }

	 public static String getLogWorkOrderUpdates(ServletContext context){
		 String sInitParam = context.getInitParameter(WebContextParameters.logworkorderupdates);
		 if ((sInitParam == null) || (sInitParam.compareToIgnoreCase("")) == 0){
			 sInitParam = "";
			}
			return sInitParam;
	 }

	 public static String getdocumentationpageURL(ServletContext context){
		 String sInitParam = context.getInitParameter(WebContextParameters.documentationpageURL);
		 if ((sInitParam == null) || (sInitParam.compareToIgnoreCase("")) == 0){
			 sInitParam = "";
			}
			return sInitParam;
	 }
	 
	 public static String getsmtempfolder(ServletContext context){
		 String sInitSMTemp = context.getInitParameter(WebContextParameters.smtempfolder);
		 if ((sInitSMTemp == null) || (sInitSMTemp.compareToIgnoreCase("")) == 0){
			 sInitSMTemp = "smtemp";
			}
			return sInitSMTemp;
	 }
}