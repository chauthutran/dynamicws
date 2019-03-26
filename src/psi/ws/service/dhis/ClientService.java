package psi.ws.service.dhis;

import org.json.JSONArray;
import org.json.JSONObject;

import psi.ws.service.DataStore;
import psi.ws.util.DateUtil;
import psi.ws.util.JSONUtil;
import psi.ws.util.Util;

public class ClientService
{
    private static String QUERY_URL_CLIENT = "/api/trackedEntityInstances";
    
    
    /**
    {
        "actionId": "ClientGet",
         "payload": {
               "teiId" : <clientId>
               "programId" : <programId> ( not required )
         },
         "actionType": "DhisApi",
         "actionEval": "if ( !output.trackedEntityInstance ) ‘{ERROR}-Client Not Found’;"
    }
    **/ 
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
    {
        "actionId": "ClientGet",
        "payload": {
               "teiId" : <clientId>
               "attributes"[
                  {
                      "attribute": <attributeId>,
                      "value": <value>
                  },
                  ....
               ]
         },
         "actionType": "DhisApi",
         "actionEval": "if ( !output.trackedEntityInstance ) ‘{ERROR}-Client Not Found’;"
    }
    **/ 
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
        String requestUrl = serverName + ClientService.QUERY_URL_CLIENT + "/" + teiId;
        Util.sendRequest( Util.REQUEST_TYPE_PUT, requestUrl, teiJson, null, Util.REQUEST_CONTENT_TYPE_DHIS, dataStore, username, password );

        dataStore.outMessage = teiJson.toString();
        
        return dataStore;
        
    }
    
    /**
    {
        "actionId": "ClientGet",
        "payload": {
               "programId" : <programId> ( if any )
               "incidentDate" : <incidentDate> ( if any )
               "enrollmentDate" : <enrollmentDate> ( if any )
               "attributes"[
                  {
                      "attribute": <attributeId>,
                      "value": <value>
                  },
                  ....
               ]
         },
         "actionType": "DhisApi",
         "actionEval": "if ( !output.trackedEntityInstance ) ‘{ERROR}-Client Not Found’;"
    }
    **/ 
    
    public static DataStore createClient( String username, String password, JSONObject requestData, String serverName ) throws Exception
    {
        DataStore dataStore = new DataStore();
        
        // STEP 1. Generate json TEI payloads
        String orgUnit = requestData.getString( "orgUnit" );
        String programId = requestData.getString( "programId" );
        
        JSONObject teiJson = new JSONObject();
        teiJson.put( "attributes", requestData.getJSONArray( "attributes" ) );
        teiJson.put(  "orgUnit", orgUnit );
        
        if( programId != null ) // Create an enrollment
        {
            String incidentDate = requestData.getString( "incidentDate" );
            incidentDate = ( incidentDate != null ) ? incidentDate : DateUtil.getCurrentDateTime();
            
            String enrollmentDate = requestData.getString( "enrollmentDate" );
            enrollmentDate = ( enrollmentDate != null ) ? enrollmentDate : DateUtil.getCurrentDateTime();

            JSONObject enrollment = new JSONObject();
            enrollment.put( "program", programId );
            enrollment.put( "incidentDate", incidentDate );
            enrollment.put( "enrollmentDate", enrollmentDate );
            
            JSONArray enrollments = new JSONArray();
            enrollments.put( enrollment );
            teiJson.put( "enrollments", enrollments );
        }
        
        // STEP 2. Create TEI
        
        String requestUrl = serverName + ClientService.QUERY_URL_CLIENT;
        Util.sendRequest( Util.REQUEST_TYPE_POST, requestUrl, teiJson, null, Util.REQUEST_CONTENT_TYPE_DHIS, dataStore, username, password );

        Util.processResponseMsg( dataStore, "importSummaries" );
        String clientId = dataStore.referenceId;
        teiJson = new JSONObject( dataStore.output );
        teiJson.put( "trackedEntityInstance", clientId );
        dataStore.output = teiJson.toString();
        
        return dataStore;
        
    }
    
}
