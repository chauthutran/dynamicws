package psi.ws.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import psi.ws.action.request.DateTimeRecord;
import psi.ws.configuration.Configuration;
import psi.ws.configuration.MongodbConfiguration;
import psi.ws.exception.ActionException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.TextSearchOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

public class MongoConnection
{
    private MongodbConfiguration configuration;
    private MongoClient mongoClient;
    private MongoCollection<Document> collection;
    private DBCollection dbCollection;

    public MongoConnection( Configuration configuration )
    {
        this.configuration = configuration.getMongodbConfig();
    }
 
    public MongoConnection( Configuration configuration, String username, String password, String cluster, String dbname, String collectionName )
    {
        configuration.setUpMongodbConfig( username, password, cluster, dbname, collectionName );
        this.configuration = configuration.getMongodbConfig();
    }
    
    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    public void open() throws ActionException
    {
        try
        {
            MongoClientURI uri = this.getURI();
            this.mongoClient = new MongoClient( uri );
            
            MongoDatabase database = mongoClient.getDatabase( this.configuration.getDbName() );
            this.collection = database.getCollection( this.configuration.getCollectionName() );
            
            
            DB db = mongoClient.getDB( this.configuration.getDbName() );
            this.dbCollection = db.getCollection( this.configuration.getCollectionName() );
            
        }
        catch ( Exception ex )
        {
            throw new ActionException( "Fail to connect mongo database" );
        }
    }

    public void close()
    {
        this.mongoClient.close();
    }
    
    public void insert( JSONObject json )
    {
        Document doc = Document.parse( json.toString() );
        this.collection.insertOne( doc );
    }
    
    public boolean update( JSONObject data )
    {
        ObjectId id = (ObjectId ) data.get("_id");
        UpdateResult result = this.collection.updateOne( Filters.eq( "_id", id ), Document.parse( data.toString() ) );
        return ( result.getModifiedCount() > 0 );
    }

    public boolean delete( String property, String value, String operator )
    {
        DeleteResult result = this.collection.deleteMany( makeFilter( property, value, operator ) );
        return ( result.getDeletedCount() == 1 );
    }
    
    public boolean delete( JSONObject data )
    {
        ObjectId id = (ObjectId ) data.get("_id");
        DeleteResult result = this.collection.deleteOne( Filters.eq( "_id", id) );
        return ( result.getDeletedCount() == 1 );
    }
    
    public JSONArray get( JSONArray conditions )
    {
 JSONObject dtRecord_actionJson = new JSONObject();
 DateTimeRecord dtRecord_Overall = new DateTimeRecord( "GETTING" );
        
        
        JSONArray list = new JSONArray();
        
        BasicDBObject andQuery = new BasicDBObject();
        List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
        for( int i = 0; i < conditions.length(); i++ )
        {
            JSONObject condition = conditions.getJSONObject( i );
            String key = condition.getString( "key" );
            String value = condition.getString( "value" );
            String operator = condition.getString( "operator" );
          
            obj.add( new BasicDBObject( key, new BasicDBObject("$" + operator, value ) ) );
        }
        andQuery.put("$and", obj);

        DBCursor cursor = this.dbCollection.find( andQuery );  
 System.out.println("\n\n 1 ");       
DateTimeRecord dtRecord_t3 = new DateTimeRecord( "Loop data" );
        while(cursor.hasNext()){
           String result = cursor.next().toString();
           list.put( new JSONObject( result ) );
        }
        
dtRecord_t3.addTimeMark_WtCount( dtRecord_actionJson );
        
dtRecord_Overall.addTimeMark_WtCount( dtRecord_actionJson ); 
System.out.println("\n\n\n--- GET " + dtRecord_actionJson.toString() );
        return list;
    }
    
    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private MongoClientURI getURI()
    {
        MongoClientOptions.Builder options = MongoClientOptions.builder()
            .maxConnectionLifeTime((30 * 1000));
        
        return new MongoClientURI( "mongodb+srv://" + this.configuration.getUsername() + ":"
            + this.configuration.getPassword() + "@" + this.configuration.getClusterUrl() + "/"
            + this.configuration.getDbName() + "?retryWrites=true", options );
    }
    
    private Bson makeFilter( String property, String value, String operator )
    {
        if( operator.equals( "eq" ))
        {
            return Filters.eq( property, value );
        }
        else if( operator.equals( "ilike" ))
        {
            return Filters.text( value, new TextSearchOptions().caseSensitive( false ) );
        }
        else if( operator.equals( "like" ))
        {
            return Filters.text( value, new TextSearchOptions().caseSensitive( true ) );
        }
        else if( operator.equals( "lt" ))
        {
            return Filters.lt( property, value );
        }
        else if( operator.equals( "lte" ))
        {
            return Filters.lte( property, value );
        }
        else if( operator.equals( "gt" ))
        {
            return Filters.gt( property, value );
        }
        else if( operator.equals( "gte" ))
        {
            return Filters.gte( property, value );
        }
        
        return null;
    }
    
}
