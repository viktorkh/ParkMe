package app.parkme.com.clientapp;

import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

import butterknife.Bind;
import butterknife.ButterKnife;

import static app.parkme.com.clientapp.Account.AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS;
import static app.parkme.com.clientapp.Account.AccountGeneral.sServerAuthenticate;
import static app.parkme.com.clientapp.LoginActivity.ARG_ACCOUNT_TYPE;
import static app.parkme.com.clientapp.LoginActivity.KEY_ERROR_MESSAGE;
import static app.parkme.com.clientapp.LoginActivity.PARAM_USER_PASS;




public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    private String mAccountType;

    @Bind(R.id.input_name)
    EditText _nameText;
    @Bind(R.id.input_email) EditText _emailText;
    @Bind(R.id.input_password) EditText _passwordText;
    @Bind(R.id.btn_signup)
    Button _signupButton;
    @Bind(R.id.link_login)
    TextView _loginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        mAccountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);


        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        String name = _emailText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        // TODO: Implement your own signup logic here.

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onSignupSuccess or onSignupFailed
                        // depending on success
                        onSignupSuccess();
                        // onSignupFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }


    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);


        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    private void createAccount() {

        // Validation!

        try {
             new AsyncTask<String, Void, Intent>() {

                String name = _emailText.getText().toString();
                String email = _emailText.getText().toString();
                String password = _passwordText.getText().toString();



                @Override
                protected Intent doInBackground(String... params) {

                    Log.d("Parkme", TAG + "> Started authenticating");

                    String authtoken = null;
                    Bundle data = new Bundle();
                    try {
                        authtoken = sServerAuthenticate.userSignUp(name, email, password, AUTHTOKEN_TYPE_FULL_ACCESS);

                        data.putString(AccountManager.KEY_ACCOUNT_NAME, email);
                        data.putString(AccountManager.KEY_ACCOUNT_TYPE, mAccountType);
                        data.putString(AccountManager.KEY_AUTHTOKEN, authtoken);
                        data.putString(PARAM_USER_PASS, password);
                    } catch (Exception e) {
                        data.putString(KEY_ERROR_MESSAGE, e.getMessage());
                    }

                    final Intent res = new Intent();
                    res.putExtras(data);
                    return res;
                }

                @Override
                protected void onPostExecute(Intent intent) {
                    if (intent.hasExtra(LoginActivity.KEY_ERROR_MESSAGE)) {
                        Toast.makeText(getBaseContext(), intent.getStringExtra(LoginActivity.KEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
                    } else {
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            }.execute().get();
        } catch (InterruptedException e) {

            Log.e(TAG,e.getMessage());
            e.printStackTrace();
        } catch (ExecutionException e) {
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
        }
    }



}