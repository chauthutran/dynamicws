package psi.ws.action;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import psi.ws.util.JSONUtil;

public class ActionDhisRequest extends ActionRequest
{
    public static String DEFAULT_REQUEST_TYPE = "GET";

    private String serverName;
    private String link;
    private String requestType = "";
    private String username;
    private String password;
    
    
    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    
    public ActionDhisRequest( String serverName, String link )
    {
        super();
        
        this.serverName = serverName;
        this.link = link;
        this.requestType = ActionDhisRequest.DEFAULT_REQUEST_TYPE;
    }
    
    public ActionDhisRequest( String serverName, String link, String requestType, String username, String password )
    {
        super();
        
        this.serverName = serverName;
        this.link = link;
        this.requestType = requestType;
        this.username = username;
        this.password = password;
    }
    
    
    // -------------------------------------------------------------------------
    // Getters && Setters
    // -------------------------------------------------------------------------

    public String getServerName()
    {
        return serverName;
    }

    public void setServerName( String serverName )
    {
        this.serverName = serverName;
    }

    public String getLink()
    {
        return link;
    }

    public void setLink( String link )
    {
        this.link = link;
    }

    public String getRequestType()
    {
        return( requestType == null ) ? ActionDhisRequest.DEFAULT_REQUEST_TYPE : requestType;
    }

    public void setRequestType( String requestType )
    {
        this.requestType = requestType;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }
    
    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    public String generateRequestURL( JSONObject actionList )
    {    
        String url = this.serverName + this.link;
        String requestUrl = url;
        
        Pattern pattern = Pattern.compile( "\\{(\\%\\%\\w+\\%\\%)(\\..[^\\}]+)+\\}" );
        Matcher matcher = pattern.matcher( url );
        
        while( matcher.find() ) 
        {
            String match = matcher.group();
            
            // Get actionId
            String actionName = matcher.group( 1 ).replaceAll( "%%", "" );
            
            // Get jsonPatch
            String jsonPatch = match.replace( "%%" + actionName + "%%.", "" ).replace("{","").replace("}", "");

            // Get value from actionId and jsonPath
            JSONObject outputData = ( ( Action ) actionList.get( actionName ) ).getOutput().getOutputJson();
            String value = JSONUtil.getValueFromJsonPath( outputData, jsonPatch );
            requestUrl = requestUrl.replace( match, value );
        }
        
        return requestUrl;
    }

}
