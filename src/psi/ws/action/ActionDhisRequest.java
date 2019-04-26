package psi.ws.action;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import psi.ws.exception.ActionException;

public class ActionDhisRequest extends ActionRequest
{
    private String regExp_URL = "\\{\\" + Action.CONFIG_PARAM_SIGN_START + "(\\w+)\\" + Action.CONFIG_PARAM_SIGN_END + "(\\..[^\\}]+)+\\}";
    public static String DEFAULT_REQUEST_TYPE = "GET";

    private ActionJSEngine actionJSEngine;
    private String serverName;
    private String link;
    private String requestType = "";
    private String username;
    private String password;
    
    
    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    
    public ActionDhisRequest( String serverName, String link, String requestType, String username, String password, ActionJSEngine actionJSEngine ) throws ActionException
    {
        super();
        
        if( serverName == null || username == null || password == null )
        {
            throw new ActionException( "Server name / user name/ password is not defined" );
        }
        
        if( link == null )
        {
            throw new ActionException( "A DHIS action is missing URL" );
        }
        
        this.serverName = serverName;
        this.link = link;
        this.requestType = requestType;
        this.username = username;
        this.password = password;
        this.actionJSEngine = actionJSEngine;
    }
    
    // -------------------------------------------------------------------------
    // Getters && Setters
    // -------------------------------------------------------------------------

    public String getServerName()
    {
        return serverName;
    }

    public String getLink()
    {
        return link;
    }

    public String getRequestType()
    {
        return( requestType == null ) ? ActionDhisRequest.DEFAULT_REQUEST_TYPE : requestType;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }
    
    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    public String generateRequestURL( JSONObject actionList ) throws ActionException
    {    
        String url = this.serverName + this.link;
        String requestUrl = url;
        
        Pattern pattern = Pattern.compile( regExp_URL );
        Matcher matcher = pattern.matcher( url );
        
        while( matcher.find() ) 
        {
            String match = matcher.group();
            
            // Get actionName
            String actionName = matcher.group( 1 );
            // Get jsonPatch
            String jsonPatch = matcher.group( 2 );

            String value = actionJSEngine.getStringValue( actionName + jsonPatch );
            requestUrl = requestUrl.replace( match, value );
        }
        
        return requestUrl;
    }

}
