package psi.ws.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import psi.ws.configuration.Configuration;
import psi.ws.configuration.MongodbConfiguration;
import psi.ws.exception.ActionException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
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
        
//        System.out.println(andQuery.toString());
        
        DBCursor cursor = this.dbCollection.find( andQuery );           
        while(cursor.hasNext()){
           String result = cursor.next().toString();
//           JSONObject output = new JSONObject(JSON.serialize(result));
           list.put( new JSONObject( result ) );
        }
        
        return list;
        
//        List<Bson> filters = new ArrayList<Bson>();
//        for( int i = 0; i < conditions.length(); i++ )
//        {
//            JSONObject condition = conditions.getJSONObject( i );
//            String key = condition.getString( "key" );
//            String value = condition.getString( "value" );
//            String operator = condition.getString( "operator" );
//            
//            Bson filter = makeFilter( key, value, operator );
//            filters.add( filter );
//        }
//        
//        
//        
//        AggregateIterable<Document> aggregate = this.collection.aggregate( filters );
//
//        List<JSONObject> list = new ArrayList<JSONObject>();
//        MongoCursor<Document> iterator = aggregate.iterator();
//        while (iterator.hasNext()) {
//            String data = iterator.next().toJson();
//            list.add( new JSONObject( data ) );
//        }
//        
//        return list;
        
        
//        JSONArray list = new JSONArray();
//        
//        if( conditions.length() > 1 )
//        {
//            List<BasicDBObject> searchArguments = new ArrayList<BasicDBObject>();
//            
//            for( int i = 0; i < conditions.length(); i++ )
//            {
//                JSONObject condition = conditions.getJSONObject( i );
//                
//                
//                String key = condition.getString( "key" );
//                String value = condition.getString( "value" );
//                String operator = condition.getString( "operator" );
//
//                searchArguments.add(new BasicDBObject( key ,new BasicDBObject( "$" + operator, value )));
//             }
//
//            BasicDBObject searchObject = new BasicDBObject();
//            searchObject.put("$and", searchArguments);
//            DBCursor logicalQueryResults = this.dbCollection.find( searchObject );           
//            while(logicalQueryResults.hasNext()){
//                 String result = logicalQueryResults.next().toString();
////                 JSONObject output = new JSONObject(JSON.serialize(result));
//                 list.put( new JSONObject( result ) );
//            }
//        }
//        else if( conditions.length() > 1 )
//        {
//            
//        }
//        
//        return list;
    }
    
    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private MongoClientURI getURI()
    {
        return new MongoClientURI( "mongodb+srv://" + this.configuration.getUsername() + ":"
            + this.configuration.getPassword() + "@" + this.configuration.getClusterUrl() + "/"
            + this.configuration.getDbName() + "?retryWrites=true" );
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
