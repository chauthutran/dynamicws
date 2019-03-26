package psi.ws.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jayway.jsonpath.JsonPath;

import psi.ws.util.JSONUtil;
import psi.ws.util.Util;


/* "issueVoucherAction": [
       {
         "actionId": "1_ClientCreate",
         "actionType": "DhisApi",
         "URL": "/api/trackedEntityInstace",
         "RESTType": "GET",
         "actionEval": "()   [$$%checkOutpout]  -=if ( !output.trackedEntityInstance ) ‘{ERROR}-Client Not Found’;"
       },
       {
         "actionId": "2_ClientUpdate",
         "actionType": "DhisApi",
         "URL": "/api/trackedEntityInstance/{config.teiId}",
         "RESTType": "PUT",
         "input": "{actinTpe: 1_ClientGet}",
         "actionEval": "if ( !output.importSummary.outputId ) ‘{ERROR}-Client Update Failed: {OUT_ERR}’;"
       }
     ]
     **/

public class ActionConfig
{
    private JSONArray configActionListByKey = new JSONArray();
    private JSONObject configActionListByActionId = new JSONObject();
    
    public String errorMsg;
    
    // -------------------------------------------------------------------------
    // Initialize
    
    public ActionConfig()
    {
        super();
    }
    
    /**
     * configData : Get from config_actions.json file
     * key: Key of an action ( "issueVoucherAction", ... )
     * 
     *  **/
    public ActionConfig( JSONObject configData, String key )
    {
        getActionListConfig( configData, key );
    }
    

    // -------------------------------------------------------------------------
    // Run one action in the action List
    
    /**
    {
       "actionId": "1_ClientCreate",
       "actionType": "DhisApi",
       "URL": "/api/trackedEntityInstace",
       "RESTType": "GET",
       "actionEval": "()   [$$%checkOutpout]  -=if ( !output.trackedEntityInstance ) ‘{ERROR}-Client Not Found’;"
     }
     
      // Payload for testing [Create TEI] action // For 'leap' server
      {
            "orgUnit": "TXqTosxccbc",
            "trackedEntityType": "XV3kldsZq0H",
            "attributes": [{
                    "attribute": "LoGHwYUQZ9y",
                    "value": "TEST1"
            }]
     }
     
     // For 'https://play.dhis2.org/2.30' server
      {
        "orgUnit": "Rp268JB6Ne4",
        "trackedEntityType": "nEenWmSyUEp",
        "attributes": [{
                "attribute": "w75KJ2mc4zz",
                "value": "First Name 1"
        }]
     }
 
 
    **/
     
   public DataStore runAction( int configIdx, JSONObject payload, String serverName, String username, String password ) throws Exception
   {
       DataStore dataStore = new DataStore();
       JSONObject actionConfig = getActionByIndex( configIdx );

       if( actionConfig != null )
       {
           // STEP 2. GET action configuration
           String actionId = actionConfig.getString( "actionId" );
           String requestType = actionConfig.getString( "RESTType" );

           String requestUrl = serverName + actionConfig.getString( "URL" );
           requestUrl = resolveUrl( requestUrl );
           

           // STEP 2. GET input data from action configuration

           JSONObject inputData = null;
           JSONObject inputJson = null;
           if( actionConfig.has( "input" ) )
           {
               String input = actionConfig.getString( "input" );
               inputJson = JSONUtil.convertJSONData( input );
               
               if( inputJson.getString( "status" ).equals( "SUCCESS" ) )
               {
                   inputData = inputJson.getJSONObject( "data" );
                   if( inputData != null )
                   {
                       if( inputData.has( "actionId" ) )
                       {
                           String inputActionId = inputData.getString( "actionId" );
                           if( inputActionId.equals( Util.ACTION_ID_PAYLOAD ) )
                           {
                               inputData = payload;
                           }
                           else
                           {
                               inputData = this.configActionListByActionId.getJSONObject( inputActionId );
                           }
                       }
                       else
                       {
                           inputData = inputJson.getJSONObject( "data" );
                       }
                   }
               }
               else
               {
                   dataStore.responseCode = 400;
                   dataStore.outMessage = inputJson.getString( "errorMsg" );
               }
           }
               
           Util.sendRequest( requestType, requestUrl, inputData, null, Util.REQUEST_CONTENT_TYPE_DHIS, dataStore, username, password );

           if( actionConfig.has( "actionEval" ) )
           {
               String script = actionConfig.getString( "actionEval" );
               String result = runScript( script, new JSONObject( dataStore.output ) );
               dataStore.output = result;
           }
           
           // Store response data to 'outMessage' of dataStore
           dataStore.outMessage = dataStore.output;    
           JSONObject returnData = new JSONObject( dataStore.output );

           // Add return data to list
           this.configActionListByActionId.put( actionId, returnData );
           
       }
       
       return dataStore;
   }
    
    // -------------------------------------------------------------------------
    // Get/Set methods
    
    /**
     * @return An Action by index
     * 
     *  **/
    private JSONObject getActionByIndex( int index )
    {
        if( index < configActionListByKey.length() )
        {
            return configActionListByKey.getJSONObject( index );
        }
        
        return null;
    }
    
    /**
     * @return An Action by actionId ( "1_ClientCreate" )
     * 
     *  **/
    private JSONObject getActionByActionId( String searchActionId )
    {
        for ( int i = 0; i < configActionListByKey.length(); i++ )
        {
            JSONObject config = configActionListByKey.getJSONObject( i );
            String actionId = config.getString( "actionId" );
            if ( actionId.equals( searchActionId ) )
            {
                return config;
            }
        }
        
        return null;
    }
    
    public int getLeng()
    {
        return configActionListByKey.length();
    }
   
   
    // -------------------------------------------------------------------------
    // Get Action List by Key, such as [ "issueVoucherAction", ... ]
    
    private void getActionListConfig( JSONObject configData, String key )
    {
        try
        {
            JSONObject actionsConfig = configData.getJSONObject( "actions" );
            if( actionsConfig.length() != 0 )
            {
                if( actionsConfig.length() == 0 )
                {
                    errorMsg = "No action " + key + " defined";
                }
                else
                {
                    configActionListByKey = JSONUtil.getJsonArray( actionsConfig, key );
                }
            }
            else
            {
                errorMsg = "No action defined";
            }
            
        }
        catch( Exception ex )
        {
            errorMsg = "No action defined for " + key;
        }
    }
    
 
    private String runScript( String script, JSONObject output )
    {
        String result = null;
        
        try
        {
          script = script.replaceAll( "\\{OUTPUT\\}", "output" );
          script = "var f = { data: '" + output.toString() + "',run: function(){ var output = JSON.parse(this.data); " + script + " this.data = JSON.stringify(output); } }; f"; 
          System.out.println("\n script : " + script );


        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
        ScriptObjectMirror obj = (ScriptObjectMirror)engine.eval(script);
        obj.callMember("run");
        result = obj.getMember("data").toString();
            
        }
        catch ( ScriptException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * @param requestUrl "/api/trackedEntityInstances/{<actionId>.<jsonPath>}[/{<actionId>.<jsonPath>}]"
     * Ex "/api/trackedEntityInstances/{1_ClientCreate.trackedEntityInstance"
     * **/
    // 
    private String resolveUrl( String requestUrl )
    {    
        String url = requestUrl;
        
        Pattern pattern = Pattern.compile( "\\{(\\w+)(\\..[^\\}]+)+\\}" );
        Matcher matcher = pattern.matcher( requestUrl );
        
        while( matcher.find() ) 
        {
            String match = matcher.group();
            
            // Get actionId
            String actionId = matcher.group( 1 );
            
            // Get jsonPatch
            String jsonPatch = match.replace( actionId + ".", "" ).replace("{","").replace("}", "");

            // Get value from actionId and jsonPath
            JSONObject actionReturnData = this.configActionListByActionId.getJSONObject( actionId );
            String value = getValueFromJsonPath( actionReturnData, jsonPatch );
            url = url.replace( match, value );
        }
        return url;
    }
    
    private String getValueFromJsonPath( JSONObject json, String jsonPath )
    {
       return JsonPath.read( json.toString() , "$." + jsonPath ).toString();
    }
    
}
