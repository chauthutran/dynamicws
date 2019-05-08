package psi.ws.action.request;

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
        }
        else if( this.requestType.equals( Util.REQUEST_TYPE_PUT ))
        {
            this.connection.update( action.getInput().getInputJson() );
        }
        else if( this.requestType.equals( Util.REQUEST_TYPE_DELETE ))
        {
            this.connection.delete( action.getInput().getInputJson() );
        }
        
        action.getOutput().setOutputMsg( action.getInput().getInputStr() );
        
        this.connection.close();
    }
    
}
