package scm.dominwong4.back4appandroidtutorial;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.FunctionCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseBroadcastReceiver;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import com.parse.ParseFacebookUtils;
import com.facebook.FacebookSdk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {


    EditText et_username;
    EditText et_password;

    Button login;
    Button register;
    Button facebookLogin;
    Button twitterLogin;
    Button cloudCode;

    ProgressDialog progressDialog;
    final List<String> permissions = Arrays.asList("public_profile", "email");

    TextView t_username;
    TextView t_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);

        login = (Button) findViewById(R.id.login);
        register = (Button) findViewById(R.id.register);
        facebookLogin = (Button) findViewById(R.id.facebookButton);
        twitterLogin = (Button) findViewById(R.id.twitterButton);
        cloudCode = (Button) findViewById(R.id.cloudCodeButon);
        t_username = (TextView) findViewById(R.id.usernameText);
        t_email = (TextView) findViewById(R.id.emailText);
        progressDialog = new ProgressDialog(MainActivity.this);

        getKeyHash();
        Parse.initialize(new Parse.Configuration.Builder(this)
                        .applicationId("MP6H592QYgcXg3SwdqFO1BAUtQz1ukNFSAuKDR2n")
                        .clientKey("iL0IbxexpJFKh4Kx3aym8o6fbHrOWxeLGy1vmT7L")
                        .server("https://parseapi.back4app.com/").build()
        );

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Please Wait");
                progressDialog.setTitle("Logging in");
                progressDialog.show();
                parseLogin();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Please Wait");
                progressDialog.setTitle("Registering");
                progressDialog.show();
                parseRegister();
            }
        });

        facebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseFacebookUtils.logInWithReadPermissionsInBackground(MainActivity.this, permissions, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException err) {
                        if (user == null) {
                            Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                        } else if (user.isNew()) {
                            Log.d("MyApp", "User signed up and logged in through Facebook!");
                            getUserDetailFromFB();
                        } else {
                            Log.d("MyApp", "User logged in through Facebook!");
                            getUserDetailFromParse();
                        }
                    }
                });
            }
        });

        twitterLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseTwitterUtils.logIn(MainActivity.this, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException err) {
                        if (user == null) {
                            Log.d("MyApp", "Uh oh. The user cancelled the Twitter login.");
                        } else if (user.isNew()) {
                            Log.d("MyApp", "User signed up and logged in through Twitter!");
                        } else {
                            Log.d("MyApp", "User logged in through Twitter!");
                        }
                    }
                });

            }
        });
        cloudCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> parameters = new HashMap<String, String>();
                ParseCloud.callFunctionInBackground("test", parameters, new FunctionCallback<Map<String, Object>>() {
                    public void done(Map<String, Object> mapObject, ParseException e) {
                        if (e == null){
                            Toast.makeText(MainActivity.this, mapObject.get("answer").toString(), Toast.LENGTH_LONG).show();
                        }
                        else Log.d("Cloud",e.getMessage());
                    }
                });
            }
        });



        Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
        FacebookSdk.sdkInitialize(getApplicationContext());
        ParseFacebookUtils.initialize(this);
        ParseTwitterUtils.initialize("xCyRcswJvZtLpcqI5OYckLKCd","qEclZACDn4986AlP341A99dnXWv2wSZGBhjdOhAXSMke7POmrV");
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("GCMSenderId", "408752285858");
        installation.saveInBackground();
    }

    void parseLogin(){
        ParseUser.logInInBackground(et_username.getText().toString(), et_password.getText().toString(), new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (parseUser != null) {
                    progressDialog.dismiss();
                    getUserDetailFromParse();
                } else {
                    progressDialog.dismiss();
                    alertDisplayer("Login Fail", e.getMessage()+" Please re-try");
                }
            }
        });
    }

    void parseRegister(){
        ParseUser user = new ParseUser();
        user.setUsername(et_username.getText().toString());
        user.setPassword(et_password.getText().toString());
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {

                    progressDialog.dismiss();
                    t_username.setText(ParseUser.getCurrentUser().getUsername());
                    saveNewUser();
                } else {
                    progressDialog.dismiss();
                    alertDisplayer("Register Fail", e.getMessage());
                }
            }
        });
    }

    void alertDisplayer(String title,String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog ok = builder.create();
        ok.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    private void getKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("scm.dominwong4.back4appandroidtutorial", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:",Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            //something
        } catch (NoSuchAlgorithmException e) {
            //something
        }
    }

    void getUserDetailFromFB(){
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),new GraphRequest.GraphJSONObjectCallback(){
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try{
                    t_username.setText(object.getString("name"));
                }catch(JSONException e){
                    e.printStackTrace();
                }
                try{
                    t_email.setText(object.getString("email"));
                }catch(JSONException e){
                    e.printStackTrace();
                }
                saveNewUser();
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields","name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }
    void saveNewUser(){
        ParseUser user = ParseUser.getCurrentUser();
        user.setUsername(t_username.getText().toString());
        user.setEmail(t_email.getText().toString());
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                alertDisplayer("Register Successful Welcome", "User:" + t_username.getText().toString() + " Login.Email:" + t_email.getText().toString());
            }
        });
    }
    void getUserDetailFromParse(){
        ParseUser user = ParseUser.getCurrentUser();
        t_username.setText(user.getUsername());
        t_email.setText(user.getEmail());
        alertDisplayer("Welcome Back", "User:" + t_username.getText().toString() +" Login.Email:"+t_email.getText().toString());

    }

}
