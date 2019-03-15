package psi.ws.service;

import org.json.JSONArray;
import org.json.JSONObject;

import psi.ws.service.dhis.ClientService;
import psi.ws.util.Util;

// Params serverName : https://clone.psi-mis.org ( NO "/" in the end of serverName )
public class DHISApiService
{      
    public static DataStore run( JSONArray actionListConfig, String serverName, String username, String password )
    {
       DataStore dataStore = null;
       for( int i = 0; i<actionListConfig.length(); i++ )
       {
          System.out.println( "actionListConfig.getJSONObject( i ) : " + actionListConfig.getJSONObject( i ).toString() );
           JSONObject actionConfig = actionListConfig.getJSONObject( i );
           dataStore = DHISApiService.runAction( actionConfig, serverName, username, password );
       }
       
       return dataStore;
    }
    
    /**
     * {
       "actionId": "ClientGet", "ClientUpdate", "Custom"
       "actionType": "DhisApi",
       // "URL": "/api/trackedEntityInstance/{config.teiId}.json",
       // "RESTType": "GET",
       "actionEval": "if ( !output.trackedEntityInstance ) ‘{ERROR}-Client Not Found’;"
     }
     *
     * For "actionId" as "Custom", need to define URL and RESTType
     **/
    public static DataStore runAction( JSONObject actionConfig, String serverName, String username, String password )
    {
        DataStore dataStore = null;
        
        try
        {
            String actionType = actionConfig.getString( "actionType" );
            String actionId = actionConfig.getString( "actionId" );
            JSONObject requestData = actionConfig.getJSONObject( "payload" );
            String actionEval = actionConfig.getString( "actionEval" );
            
            if( actionType.equals( Util.REQUEST_ACTION_TYPE_DHIS ))
            {
                if( actionId.equals( Util.REQUESTION_DHIS_ACTION_ID_REQUESTCUSTOM ) )
                {
                    String actionUrl = actionConfig.getString( "url" );
                    String requestUrl = serverName + actionUrl;
                    String requestType = actionConfig.getString( "RESTType" );
                    dataStore = DHISApiService.runRequestCustom( username, password, requestType, requestData, requestUrl );
                }
                if( actionId.equals( Util.REQUESTION_DHIS_ACTION_ID_CLIENTGET ) )
                {
                    dataStore = ClientService.getClientById( username, password, requestData, serverName );
                }
                else if( actionId.equals( Util.REQUESTION_DHIS_ACTION_ID_CLIENTUPDATE ) )
                {
                    dataStore = ClientService.updateClient( username, password, requestData, serverName );
                }
                else if( actionId.equals( Util.REQUESTION_DHIS_ACTION_ID_CLIENTUPDATE) )
                {
                    dataStore = ClientService.createClient( username, password, requestData, serverName );
                }
            } 
        }
        catch( Exception ex )
        {
            Util.outputErr( "errMsg: " + ex.getMessage() );
        }
        
        return dataStore;
    }
    
    // -------------------------------------------------------------------------
    // FOR "Custom" action type
    // -------------------------------------------------------------------------
    
    private static DataStore runRequestCustom( String username, String password, String requestType, JSONObject requestData, String requestUrl ) throws Exception
    {
        DataStore dataStore = new DataStore();
        Util.sendRequest( requestType, requestUrl, null, null, Util.REQUEST_CONTENT_TYPE_DHIS, dataStore, username, password );
        
        return dataStore;
    }
    
}
