package psi.ws.service.dhis;

import org.json.JSONArray;
import org.json.JSONObject;

import psi.ws.service.DataStore;
import psi.ws.util.JSONUtil;
import psi.ws.util.Util;

public class ClientService
{
    private static String QUERY_URL_CLIENT = "/api/trackedEntityInstances";
    
    /**
     * params requestData : JSONObject of client info
     * {
     *  "teiId" : <clientId>
     *  "programId" : <programId> ( not required )
     * }
     * **/ 
    
    public static DataStore getClientById( String username, String password, JSONObject requestData, String serverName ) throws Exception
    {
        DataStore dataStore = new DataStore();

        String teiId = requestData.getString( "teiId" );
        String programId = requestData.getString( "programId" );
        String requestUrl = serverName + ClientService.QUERY_URL_CLIENT + "/" + teiId + ".json" + "?fields=*,relationships[*]";
        if( programId != null )
        {
            requestUrl += "&program=" + programId;
        }

        Util.sendRequest( Util.REQUEST_TYPE_GET, requestUrl, null, null, Util.REQUEST_CONTENT_TYPE_DHIS, dataStore, username, password );
//            return new JSONObject( dataStore.output );
        
        dataStore.outMessage = dataStore.output;
        
        return dataStore;
   
    }
    
    /**
     * params requestData : JSONObject of client info
     * {
     *  "teiId" : <clientId>
     *  "programId" : <programId> ( not required )
     *  "attributes"[
     *     {
     *         "attribute": <attributeId>,
     *         "value": <value>
     *     },
     *     ....
     *  ]
     * }
     * **/ 
    
    public static DataStore updateClient( String username, String password, JSONObject requestData, String serverName ) throws Exception
    {
        DataStore dataStore = new DataStore();

        // STEP 1. Get client information from server
        dataStore = ClientService.getClientById( username, password, requestData, serverName );
        JSONObject teiJson = new JSONObject( dataStore.output );
        
        // STEP 2. Update the new attribute values
        String teiId = requestData.getString( "teiId" );
        JSONArray updateDataJsonArr = JSONUtil.getJsonArray( requestData, "attributes" );
        
        JSONUtil.jsonArrValueSetMerge( teiJson.getJSONArray( "attributes" ), updateDataJsonArr, "attribute" );

        // STEP 3. Update Client
        String requestUrl = ClientService.QUERY_URL_CLIENT + "/" + teiId;
        Util.sendRequest( Util.REQUEST_TYPE_PUT, requestUrl, teiJson, null, Util.REQUEST_CONTENT_TYPE_DHIS, dataStore, username, password );
        
        Util.processResponseMsg( dataStore, "importSummaries" );
        dataStore.output = teiJson.toString();
        
        return dataStore;
        
    }
    
    public static DataStore createClient( String username, String password, JSONObject requestData, String serverName ) throws Exception
    {
        DataStore dataStore = new DataStore();
        
        String requestUrl = ClientService.QUERY_URL_CLIENT;
        Util.sendRequest( Util.REQUEST_TYPE_POST, requestUrl, requestData, null, Util.REQUEST_CONTENT_TYPE_DHIS, dataStore, username, password );

        Util.processResponseMsg( dataStore, "importSummaries" );
        String clientId = dataStore.referenceId;
        JSONObject teiJson = new JSONObject( dataStore.output );
        teiJson.put( "trackedEntityInstance", clientId );
        dataStore.output = teiJson.toString();
        
        return dataStore;
        
    }
    
}
