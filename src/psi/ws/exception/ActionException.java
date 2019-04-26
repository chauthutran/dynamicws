package psi.ws.exception;

public class ActionException extends Exception {
   
    private static final long serialVersionUID = -6653178421926783016L;

    public ActionException( String message ) {
        super(message);
    }
    
    public ActionException( String message, Throwable err ) {
        super(message, err);
    }
 }
