package psi.ws.mongodb;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import psi.ws.configuration.Configuration;
import psi.ws.configuration.MongodbConfiguration;
import psi.ws.exception.ActionException;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

public class MongoConnection
{
    private MongodbConfiguration configuration;
    private MongoClient mongoClient;
    private MongoCollection<Document> collection;

    public MongoConnection( Configuration configuration )
    {
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
            
            MongoDatabase db = mongoClient.getDatabase( this.configuration.getDbName() );
            this.collection = db.getCollection( this.configuration.getCollectionName() );
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

    public boolean delete( String property, String value )
    {
        DeleteResult result = this.collection.deleteMany( this.filter( property, value ) );
        return ( result.getDeletedCount() == 1 );
    }
    
    public boolean delete( JSONObject data )
    {
        ObjectId id = (ObjectId ) data.get("_id");
        DeleteResult result = this.collection.deleteOne( Filters.eq( "_id", id) );
        return ( result.getDeletedCount() == 1 );
    }
    
    public List<JSONObject> get( String property, String value )
    {
//        BasicQuery query1 = new BasicQuery("{ age : { $lt : 40 }, name : 'cat' }");
        
        List<JSONObject> searchResult = new ArrayList<JSONObject>();
        
        this.collection.find( this.filter( property, value )).forEach((Consumer<? super Document>) (Document doc) -> {
            searchResult.add( new JSONObject( doc.toJson() ) );
        });
        
       return searchResult;
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
    
    private Bson filter( String property, String value )
    {
        return Filters.eq( property, value );
    }
}
