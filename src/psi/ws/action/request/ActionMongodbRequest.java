package psi.ws.action.request;

import org.json.JSONArray;
import org.json.JSONObject;

import psi.ws.action.Action;
import psi.ws.action.ActionRequest;
import psi.ws.configuration.Configuration;
import psi.ws.exception.ActionException;
import psi.ws.mongodb.MongoConnection;
import psi.ws.util.Util;

public class ActionMongodbRequest extends ActionRequest
{
    private String requestType;
    private Configuration configuration;
    private MongoConnection connection;
    
    
    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    
    public ActionMongodbRequest( Configuration configuration, String requestType )
    {
        this.requestType = requestType;
        this.configuration = configuration;
        this.connection = new MongoConnection( configuration );
    }
    
    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------
    
    @Override
    public String getRequestType()
    {
        return this.requestType;
    }

    @Override
    public Configuration getConfiguration()
    {
        return this.configuration;
    }


    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    @Override
    public void sendRequest( Action action ) throws ActionException
    {
        this.connection.open();
        
        if( this.requestType.equals( Util.REQUEST_TYPE_POST ))
        {
            this.connection.insert( action.getInput().getInputJson() );
            action.getOutput().setOutputMsg( "SUCCESS" );
        }
        else if( this.requestType.equals( Util.REQUEST_TYPE_PUT ))
        {
            this.connection.update( action.getInput().getInputJson() );
            action.getOutput().setOutputMsg( "SUCCESS" );
        }
        else if( this.requestType.equals( Util.REQUEST_TYPE_DELETE ))
        {
            this.connection.delete( action.getInput().getInputJson() );
            action.getOutput().setOutputMsg( "SUCCESS" );
        }
        else if( this.requestType.equals( Util.REQUEST_TYPE_GET ))
        {
            JSONObject inputJson = action.getInput().getInputJson();
            JSONArray list = this.connection.get( inputJson.getJSONArray( "conditions" )  );
            JSONObject result = new JSONObject();
            result.put( "result", list );
            action.getOutput().setOutputMsg( result.toString() );
        }
        
        this.connection.close();
    }
    
}
