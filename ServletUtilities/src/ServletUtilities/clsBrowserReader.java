package ServletUtilities;

import javax.servlet.http.HttpServletRequest;

public class clsBrowserReader {
	
	public static final String FORM_FIELD_SCREEN_WIDTH = "BROWSER_READER_SCREENWIDTH";
	public static final String FORM_FIELD_SCREEN_HEIGHT = "BROWSER_READER_SCREENHEIGHT";
	public static final String FORM_FIELD_COLOR = "BROWSER_READER_COLOR";
	public static final String FORM_FIELD_FONTS = "BROWSER_READER_FONTS";
	public static final String FORM_FIELD_NAVIGATOR = "BROWSER_READER_NAVIGATOR";
	public static final String FORM_FIELD_VERSION = "BROWSER_READER_VERSION";
	public static final String FORM_FIELD_COLORDEPTH = "BROWSER_READER_COLORDEPTH";
	public static final String FORM_FIELD_MAXWIDTH = "BROWSER_READER_MAXWIDTH";
	public static final String FORM_FIELD_MAXHEIGHT = "BROWSER_READER_MAXHEIGHT";
	public static final String FORM_FIELD_CODENAME = "BROWSER_READER_CODENAME";
	public static final String FORM_FIELD_PLATFORM = "BROWSER_READER_PLATFORM";
	public static final String FORM_FIELD_JAVA = "BROWSER_READER_JAVA";
	public static final String FORM_FIELD_LOCALHOST = "BROWSER_READER_LOCALHOST";
	public static final String FORM_FIELD_HOST = "BROWSER_READER_HOST";
	public static final String FORM_FIELD_IP = "BROWSER_READER_IP";

	public static String listPassedProperties(HttpServletRequest request){
		String s =
		"PROPERTIES FROM BROWSER: \n"
		+ "Screen width: " + readParam(request, FORM_FIELD_SCREEN_WIDTH) + "\n"
		+ "Screen height: " + readParam(request, FORM_FIELD_SCREEN_HEIGHT) + "\n"
		+ "Color: " + readParam(request, FORM_FIELD_COLOR) + "\n"
		+ "Fonts: " + readParam(request, FORM_FIELD_FONTS) + "\n"
		+ "Navigator: " + readParam(request, FORM_FIELD_NAVIGATOR) + "\n"
		+ "Version: " + readParam(request, FORM_FIELD_VERSION) + "\n"
		+ "Color depth: " + readParam(request, FORM_FIELD_COLORDEPTH) + "\n"
		+ "Max width: " + readParam(request, FORM_FIELD_MAXWIDTH) + "\n"
		+ "Max height: " + readParam(request, FORM_FIELD_MAXHEIGHT) + "\n"
		+ "Codename: " + readParam(request, FORM_FIELD_CODENAME) + "\n"
		+ "Java: " + readParam(request, FORM_FIELD_JAVA) + "\n"
		+ "Platform: " + readParam(request, FORM_FIELD_PLATFORM) + "\n"
		+ "Localhost: " + readParam(request, FORM_FIELD_LOCALHOST) + "\n"
		+ "Host: " + readParam(request, FORM_FIELD_HOST) + "\n"
		+ "IP: " + readParam(request, FORM_FIELD_IP) + "\n"
		+ "PROPERTIES FROM REQUEST: \n"
		+ "request.getLocalMethod() = '" + request.getMethod() + "'" + "\n"
		+ "request.getPathInfo() = '" + request.getPathInfo() + "'" + "\n"
		+ "request.getPathTranslated() = '" + request.getPathTranslated() + "'" + "\n"
		+ "request.getProtocol() = '" + request.getProtocol() + "'" + "\n"
		+ "request.getQueryString() = '" + request.getQueryString() + "'" + "\n"
		+ "request.getRemoteAddr() = '" + request.getRemoteAddr() + "'" + "\n"
		+ "request.getRemoteHost() = '" + request.getRemoteHost() + "'" + "\n"
		+ "request.getRemotePort() = '" + request.getRemotePort() + "'" + "\n"
		+ "request.getRemoteUser() = '" + request.getRemoteUser() + "'" + "\n"
		+ "request.getRequestURI() = '" + request.getRequestURI() + "'" + "\n"
		+ "request.getScheme() = '" + request.getScheme() + "'" + "\n"
		+ "request.getServerName() = '" + request.getServerName() + "'" + "\n"
		+ "request.getServerPort() = '" + request.getServerPort() + "'" + "\n"
		+ "request.getServletPath() = '" + request.getServletPath() + "'" + "\n"
		+ "request.getHeader(\"User-Agent\") = '" + request.getHeader("User-Agent") + "'" + "\n"
		;
		
		return s;
	}
	public static String getFormFields(){
		String s = "";
		
		s += "\n";
		s += "<INPUT TYPE=HIDDEN NAME='" + FORM_FIELD_SCREEN_WIDTH + "' ID = '" + FORM_FIELD_SCREEN_WIDTH + "' VALUE=''>" + "\n";
		s += "<INPUT TYPE=HIDDEN NAME='" + FORM_FIELD_SCREEN_HEIGHT + "' ID = '" + FORM_FIELD_SCREEN_HEIGHT + "' VALUE=''>" + "\n";
		s += "<INPUT TYPE=HIDDEN NAME='" + FORM_FIELD_COLOR + "' ID = '" + FORM_FIELD_COLOR + "' VALUE=''>" + "\n";
		s += "<INPUT TYPE=HIDDEN NAME='" + FORM_FIELD_FONTS + "' ID = '" + FORM_FIELD_FONTS + "' VALUE=''>" + "\n";
		s += "<INPUT TYPE=HIDDEN NAME='" + FORM_FIELD_NAVIGATOR + "' ID = '" + FORM_FIELD_NAVIGATOR + "' VALUE=''>" + "\n";
		s += "<INPUT TYPE=HIDDEN NAME='" + FORM_FIELD_VERSION + "' ID = '" + FORM_FIELD_VERSION + "' VALUE=''>" + "\n";
		s += "<INPUT TYPE=HIDDEN NAME='" + FORM_FIELD_COLORDEPTH + "' ID = '" + FORM_FIELD_COLORDEPTH + "' VALUE=''>" + "\n";
		s += "<INPUT TYPE=HIDDEN NAME='" + FORM_FIELD_MAXWIDTH + "' ID = '" + FORM_FIELD_MAXWIDTH + "' VALUE=''>" + "\n";
		s += "<INPUT TYPE=HIDDEN NAME='" + FORM_FIELD_MAXHEIGHT + "' ID = '" + FORM_FIELD_MAXHEIGHT + "' VALUE=''>" + "\n";
		s += "<INPUT TYPE=HIDDEN NAME='" + FORM_FIELD_CODENAME + "' ID = '" + FORM_FIELD_CODENAME + "' VALUE=''>" + "\n";
		s += "<INPUT TYPE=HIDDEN NAME='" + FORM_FIELD_PLATFORM + "' ID = '" + FORM_FIELD_PLATFORM + "' VALUE=''>" + "\n";
		s += "<INPUT TYPE=HIDDEN NAME='" + FORM_FIELD_JAVA + "' ID = '" + FORM_FIELD_JAVA + "' VALUE=''>" + "\n";
		s += "<INPUT TYPE=HIDDEN NAME='" + FORM_FIELD_LOCALHOST + "' ID = '" + FORM_FIELD_LOCALHOST + "' VALUE=''>" + "\n";
		s += "<INPUT TYPE=HIDDEN NAME='" + FORM_FIELD_HOST + "' ID = '" + FORM_FIELD_HOST + "' VALUE=''>" + "\n";
		s += "<INPUT TYPE=HIDDEN NAME='" + FORM_FIELD_IP + "' ID = '" + FORM_FIELD_IP + "' VALUE=''>" + "\n";
		//TODO...
		
		s += "\n";
		
		return s;
	}
	public static String getJavascriptForReading(String sFormName){
		return
	    	"<SCRIPT LANGUAGE=\"JavaScript\">" + "\n"
			
			+ "<!-- begin" + "\n"
			+ "function collectBrowserInfo() {" + "\n"
			+ "    window.onerror=null;" + "\n"
			+ "    colors = window.screen.colorDepth;" + "\n"
			
			//document.forms["MAINFORM"].submit();
			//+ "    document.forms[\"" + sFormName + "\"]." + FORM_FIELD_COLOR + ".value = Math.pow (2, colors);" + "\n"
			+ "    if (window.screen.fontSmoothingEnabled == true){" + "\n"
			+ "        document.forms[\"" + sFormName + "\"]." + FORM_FIELD_FONTS + ".value = \"Yes\";" + "\n"
			+ "    }else{" + "\n"
			//+ "        alert('TEST = ' + document.forms[\"" + sFormName + "\"]." + FORM_FIELD_FONTS + ".value);\n"
			+ "         document.forms[\"" + sFormName + "\"]." + FORM_FIELD_FONTS + ".value = \"No\";" + "\n"
			+ "    }" + "\n"
			
			+ "    document.forms[\"" + sFormName + "\"]." + FORM_FIELD_NAVIGATOR + ".value = navigator.appName;" + "\n"
			+ "    document.forms[\"" + sFormName + "\"]." + FORM_FIELD_VERSION + ".value = navigator.appVersion;" + "\n"
			+ "    document.forms[\"" + sFormName + "\"]." + FORM_FIELD_COLORDEPTH + ".value = window.screen.colorDepth;" + "\n"
			+ "    document.forms[\"" + sFormName + "\"]." + FORM_FIELD_SCREEN_WIDTH + ".value = window.screen.width;" + "\n"
			+ "    document.forms[\"" + sFormName + "\"]." + FORM_FIELD_SCREEN_HEIGHT + ".value = window.screen.height;" + "\n"
			+ "    document.forms[\"" + sFormName + "\"]." + FORM_FIELD_MAXWIDTH + ".value = window.screen.availWidth;" + "\n"
			+ "    document.forms[\"" + sFormName + "\"]." + FORM_FIELD_MAXHEIGHT + ".value = window.screen.availHeight;" + "\n"
			+ "    document.forms[\"" + sFormName + "\"]." + FORM_FIELD_CODENAME + ".value = navigator.appCodeName;" + "\n"
			+ "    document.forms[\"" + sFormName + "\"]." + FORM_FIELD_PLATFORM + ".value = navigator.platform;" + "\n"
			
			+ "    if (navigator.javaEnabled() < 1){" + "\n"
			+ "        document.forms[\"" + sFormName + "\"]." + FORM_FIELD_JAVA + ".value=\"No\";" + "\n"
			+ "    }" + "\n"
			+ "    if (navigator.javaEnabled() == 1){" + "\n"
			+ "        document.forms[\"" + sFormName + "\"]." + FORM_FIELD_JAVA + ".value=\"Yes\";" + "\n"
			+ "    }" + "\n"
			
			+ "    if(navigator.javaEnabled() && (navigator.appName != \"Microsoft Internet Explorer\")) {" + "\n"
			+ "        vartool=java.awt.Toolkit.getDefaultToolkit();" + "\n"
			+ "        addr=java.net.InetAddress.getLocalHost(); " + "\n"
			+ "        document.forms[\"" + sFormName + "\"]." + FORM_FIELD_LOCALHOST + ".value = java.net.InetAddress.getLocalHost();" + "\n"
			+ "        host=addr.getHostName();" + "\n"
			+ "        document.forms[\"" + sFormName + "\"]." + FORM_FIELD_HOST + ".value = addr.getHostName();" + "\n"
			+ "        ip=addr.getHostAddress();" + "\n"
			+ "        document.forms[\"" + sFormName + "\"]." + FORM_FIELD_IP + ".value = addr.getHostAddress();" + "\n"
			//+ "        alert('Your host name is ' + host + ', Your IP address is ' + ip);" + "\n"
			+ "    }else{" + "\n"
			+ "        getUserIP(function(ip){ \n"
			+ "            document.forms[\"" + sFormName + "\"]." + FORM_FIELD_IP + ".value = ip;" + "\n"
			+ "        });\n"
			
			+ "    }" + "\n"
			
			+ "}" + "\n"
			
			
			//Getting the browser's IP:
			//Got the code from here (but I don't have it fully working yet....):
			// https://ourcodeworld.com/articles/read/257/how-to-get-the-client-ip-address-with-javascript-only
			
			+ "\n"
			+ "function getUserIP(onNewIP) { \n" //  onNewIp - your listener function for new IPs
			+ "    //compatibility for firefox and chrome \n"
			+ "    var myPeerConnection = window.RTCPeerConnection || window.mozRTCPeerConnection || window.webkitRTCPeerConnection; \n"
			+ "    var pc = new myPeerConnection({ \n"
			+ "        iceServers: [] \n"
			+ "    }), \n"
			+ "    noop = function() {}, \n"
			+ "    localIPs = {}, \n"
			+ "    ipRegex = /([0-9]{1,3}(\\.[0-9]{1,3}){3}|[a-f0-9]{1,4}(:[a-f0-9]{1,4}){7})/g, \n"
			+ "    key; \n"

			+ "    function iterateIP(ip) { \n"
			+ "        if (!localIPs[ip]) onNewIP(ip); \n"
			+ "        localIPs[ip] = true; \n"
			+ "    } \n"
            + "\n"
			+ "    //create a bogus data channel \n"
			+ "    pc.createDataChannel(\"\"); \n"
            + "\n"
			+ "    // create offer and set local description \n"
			+ "    pc.createOffer().then(function(sdp) { \n"
			+ "        sdp.sdp.split('\\n').forEach(function(line) { \n"
			+ "            if (line.indexOf('candidate') < 0) return; \n"
			+ "            line.match(ipRegex).forEach(iterateIP); \n"
			+ "        }); \n"
			        
			+ "        pc.setLocalDescription(sdp, noop, noop); \n"
			+ "    }).catch(function(reason) { \n"
			+ "        // An error occurred, so handle the failure to connect \n"
			+ "    }); \n"

			+ "    //listen for candidate events \n"
			+ "    pc.onicecandidate = function(ice) { \n"
			+ "        if (!ice || !ice.candidate || !ice.candidate.candidate || !ice.candidate.candidate.match(ipRegex)) return; \n"
			+ "        ice.candidate.candidate.match(ipRegex).forEach(iterateIP); \n"
			+ "    }; \n"
			+ "} \n"

			
			+ "// end -->" + "\n"
			+ "</script>" + "\n"
			
	    	//+ "<BODY OnLoad=\"display()\">" + "\n"
	    ;
	}
	public static String getCollectInfoCommand(){
		//return "onload=\"collectBrowserInfo()\"" + "\n";
		return "collectBrowserInfo()";
	}
	private static String readParam(HttpServletRequest request, String sParam){
		
		if (request.getParameter(sParam) == null){
			return "";
		}else{
			return request.getParameter(sParam);
		}
	}
	
}
