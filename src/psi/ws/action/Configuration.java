package psi.ws.action;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import psi.ws.exception.ActionException;
import psi.ws.util.AppConfigUtil;
import psi.ws.util.Util;

public class Configuration
{
    private String server;

    private String username;

    private String password;

    private JSONArray configActionList;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public Configuration( HttpServletRequest request, String actionKey )
        throws ActionException
    {
        // Get server configuration
        setUpConfig( request );

        // GET config_action from 'config_actions.json' definition
        setActionConfigList( request, actionKey );
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getServer()
    {
        return server;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public JSONArray getConfigActionList()
    {
        return configActionList;
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private void setUpConfig( HttpServletRequest request )
        throws ActionException
    {
        JSONObject configData = AppConfigUtil
            .readConfigFile( Util.CONFIG_FILE_COMMON_DATE, request.getServletContext() );

        this.server = configData.getString( "ACCESS_SERVER_NAME" );
        this.username = configData.getString( "ACCESS_SERVER_USERNAME" );
        this.password = configData.getString( "ACCESS_SERVER_PASSWORD" );
    }

    private void setActionConfigList( HttpServletRequest request, String actionKey )
        throws ActionException
    {
        JSONObject configActionData = AppConfigUtil.readConfigFile( Util.CONFIG_FILE_ACTION_DATA,
            request.getServletContext() );

        this.configActionList = configActionData.getJSONObject( "actions" ).getJSONArray( actionKey );
    }
}