package psi.ws.action.request;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import psi.ws.action.Action;
import psi.ws.action.ActionJSEngine;
import psi.ws.action.ActionRequest;
import psi.ws.configuration.Configuration;
import psi.ws.configuration.WebServiceConfiguration;
import psi.ws.exception.ActionException;
import psi.ws.util.JSONUtil;
import psi.ws.util.Util;

public class ActionWebServiceRequest
    extends ActionRequest
{
    private String regExp_URL = "\\{\\" + Action.CONFIG_PARAM_SIGN_START + "(\\w+)\\" + Action.CONFIG_PARAM_SIGN_END
        + "(\\..[^\\}]+)+\\}";

    public static String DEFAULT_REQUEST_TYPE = "GET";

    private ActionJSEngine actionJSEngine;

    private Configuration configuration;

    private String requestType = "";

    private String url = "";
    
    private WebServiceConfiguration webServiceConfiguration;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public ActionWebServiceRequest( Configuration configuration, JSONObject httpRequestData, String link,
        String requestType, ActionJSEngine actionJSEngine, JSONObject actionListByName )
        throws ActionException
    {
        super();

        this.configuration = configuration;
        this.webServiceConfiguration = this.configuration.getWebServiceConfig();
        
        if ( this.webServiceConfiguration.getServer() == null
            || this.webServiceConfiguration.getUsername() == null
            || this.webServiceConfiguration.getPassword() == null )
        {
            throw new ActionException( "Server name / user name/ password is not defined" );
        }

        if ( link == null )
        {
            throw new ActionException( "A DHIS action is missing URL" );
        }

        this.requestType = requestType;
        this.actionJSEngine = actionJSEngine;
        this.url = this.generateRequestURL( httpRequestData, link, actionListByName );
    }

    // -------------------------------------------------------------------------
    // Getters && Setters
    // -------------------------------------------------------------------------

    public String getRequestType()
    {
        return (requestType == null) ? ActionWebServiceRequest.DEFAULT_REQUEST_TYPE : requestType;
    }

    public String getUrl()
    {
        return url;
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }

    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    public void sendRequest( Action action )
        throws ActionException
    {
        Util.sendRequest( action );
    }

    // -------------------------------------------------------------------------
    // Supportive Methods
    // -------------------------------------------------------------------------

    private String generateRequestURL( JSONObject httpRequestData, String link, JSONObject actionList )
        throws ActionException
    {
        String url = this.webServiceConfiguration.getServer() + link;
        String requestUrl = url;

        Pattern pattern = Pattern.compile( regExp_URL );
        Matcher matcher = pattern.matcher( url );
        while ( matcher.find() )
        {
            String match = matcher.group();

            // Get [actionName] or [request]
            String key = matcher.group( 1 );
            // Get jsonPatch
            String path = matcher.group( 2 );

            String value = "";
            if ( key.equals( Action.PARAMS_REQUEST ) )
            {
                path = path.substring( 1 );
                value = JSONUtil.getValueFromJsonPath( httpRequestData, path );
            }
            else
            {
                value = actionJSEngine.getStringValue( key + path );
            }
            requestUrl = requestUrl.replace( match, value );
        }

        return requestUrl;
    }

}
