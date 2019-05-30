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
        
        JSONObject dtRecord_actionJson = new JSONObject();
        DateTimeRecord dtRecord_Overall = new DateTimeRecord( "[L0]OVERALL" );
        
        
        if( action.getInput().getOverwriteConfigutation() == null )
        {

            DateTimeRecord dtRecord_t1 = new DateTimeRecord( "t1" );
            
            this.connection = new MongoConnection( this.configuration );
            dtRecord_t1.addTimeMark_WtCount( dtRecord_actionJson ); 
        }
        else
        {
            DateTimeRecord dtRecord_t2 = new DateTimeRecord( "t2" );
            
            JSONObject config = action.getInput().getOverwriteConfigutation();
            String username = config.getString( "username" );
            String password = config.getString( "password" ); 
            String cluster = config.getString( "cluster" ); 
            String dbName = config.getString( "dbName" ); 
            String collectionName = config.getString( "collectionName" );
            this.connection = new MongoConnection( this.configuration, username, password, cluster ,dbName, collectionName );
            
            dtRecord_t2.addTimeMark_WtCount( dtRecord_actionJson ); 
        }
        DateTimeRecord dtRecord_t3 = new DateTimeRecord( "t3" );
        this.connection.open();
        dtRecord_t3.addTimeMark_WtCount( dtRecord_actionJson ); 
        
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
            DateTimeRecord dtRecord_t4 = new DateTimeRecord( "t4" );
            JSONObject inputJson = action.getInput().getInputJson();
            JSONArray list = this.connection.get( inputJson.getJSONArray( "conditions" )  );
            dtRecord_t4.addTimeMark_WtCount( dtRecord_actionJson ); 
            
            DateTimeRecord dtRecord_t5 = new DateTimeRecord( "t5" );
            
            JSONObject result = new JSONObject();
            result.put( "result", list );
            action.getOutput().setOutputMsg( result.toString() );
            
            dtRecord_t5.addTimeMark_WtCount( dtRecord_actionJson ); 
        }
        
        dtRecord_Overall.addTimeMark_WtCount( dtRecord_actionJson ); 
        System.out.println( "\n\n\n --- " + dtRecord_actionJson.toString() );
        
        this.connection.close();
    }
    
}
