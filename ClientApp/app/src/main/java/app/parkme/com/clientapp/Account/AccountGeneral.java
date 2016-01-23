package app.parkme.com.clientapp.Account;

/**
 * Created by Victor.Khazanov on 16/1/2016.
 */
public class AccountGeneral {

    public static final String ACCOUNT_TYPE = "com.Parkme.auth";

    /**
     * Account name
     */
    public static final String ACCOUNT_NAME = "Parkme";

    /**
     * Auth token types
     */
    public static final String AUTHTOKEN_TYPE_READ_ONLY = "Read only";
    public static final String AUTHTOKEN_TYPE_READ_ONLY_LABEL = "Read only access to an Parkme account";

    public static final String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";
    public static final String AUTHTOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to an Parkme account";

    public static final ServerAuthenticate sServerAuthenticate = new ParseComServerAuthenticate();
}
