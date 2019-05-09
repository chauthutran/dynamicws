package psi.ws.configuration;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import psi.ws.exception.ActionException;
import psi.ws.util.AppConfigUtil;
import psi.ws.util.Util;

public class Configuration
{
    private WebServiceConfiguration webServiceConfig;

    private MongodbConfiguration mongodbConfig;

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

    public MongodbConfiguration getMongodbConfig()
    {
        return mongodbConfig;
    }

    public WebServiceConfiguration getWebServiceConfig()
    {
        return webServiceConfig;
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

        webServiceConfig = new WebServiceConfiguration( configData.getString( "ACCESS_SERVER_NAME" ),
            configData.getString( "ACCESS_SERVER_USERNAME" ), configData.getString( "ACCESS_SERVER_PASSWORD" ) );

        this.mongodbConfig = new MongodbConfiguration( configData.getString( "ACCESS_MONGODB_USERNAME" ),
            configData.getString( "ACCESS_MONGODB_PASSWORD" ), configData.getString( "ACCESS_MONGODB_CLUSTER" ),
            configData.getString( "ACCESS_MONGODB_DBNAME" ), configData.getString( "ACCESS_MONGODB_COLLECTION" ) );
    }

    private void setActionConfigList( HttpServletRequest request, String actionKey )
        throws ActionException
    {
        JSONObject configActionData = AppConfigUtil.readConfigFile( Util.CONFIG_FILE_ACTION_DATA,
            request.getServletContext() );

        this.configActionList = configActionData.getJSONObject( "actions" ).getJSONArray( actionKey );
    }
}
