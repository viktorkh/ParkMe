package app.parkme.com.clientapp.Account;

/**
 * Created by Victor.Khazanov on 16/1/2016.
 */
public interface ServerAuthenticate {
    public String userSignUp(final String name, final String email, final String pass, String authType) throws Exception;
    public String userSignIn(final String user, final String pass, String authType) throws Exception;
}
