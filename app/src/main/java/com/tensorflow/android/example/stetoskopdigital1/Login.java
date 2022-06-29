package com.tensorflow.android.example.stetoskopdigital1;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tensorflow.android.R;
import com.tensorflow.android.services.LoginResponse;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class Login extends AppCompatActivity {

    ProgressDialog pDialog;
    Button btn_register, btn_login;
    EditText txt_username, txt_password;
    Intent intent;
    private LoginResponse loginResponse;

    int success;
    ConnectivityManager conMgr;

//    private String url = Server.URL + "login.php";

//    private static final String TAG = Login.class.getSimpleName();
//
//    private static final String TAG_SUCCESS = "success";
//    private static final String TAG_MESSAGE = "message";

    public final static String TAG_USERNAME = "username";
    public final static String TAG_ID = "id";

//    String tag_json_obj = "json_obj_req";

    SharedPreferences sharedpreferences;
    Boolean session = false;
    String id, username;
    public static final String my_shared_preferences = "my_shared_preferences";
    public static final String session_status = "session_status";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        {
            if (conMgr.getActiveNetworkInfo() != null
                    && conMgr.getActiveNetworkInfo().isAvailable()
                    && conMgr.getActiveNetworkInfo().isConnected()) {
            } else {
                Toast.makeText(getApplicationContext(), "No Internet Connection",
                        Toast.LENGTH_LONG).show();
            }
        }

        btn_login = (Button) findViewById(R.id.btn_login);
        btn_register = (Button) findViewById(R.id.btn_register);
        txt_username = (EditText) findViewById(R.id.txt_username);
        txt_password = (EditText) findViewById(R.id.txt_password);

        // Cek session login jika TRUE maka langsung buka MainActivity
        sharedpreferences = getSharedPreferences(my_shared_preferences, Context.MODE_PRIVATE);
        session = sharedpreferences.getBoolean(session_status, false);
        id = sharedpreferences.getString(TAG_ID, null);
        username = sharedpreferences.getString(TAG_USERNAME, null);

        if (session) {
            Intent intent = new Intent(Login.this, MainActivity1.class);
            intent.putExtra(TAG_ID, id);
            intent.putExtra(TAG_USERNAME, username);
            finish();
            startActivity(intent);
        }


        btn_login.setOnClickListener(v -> {
            // TODO Auto-generated method stub
            String username = txt_username.getText().toString();
            String password = txt_password.getText().toString();

            // mengecek kolom yang kosong
            if (username.trim().length() > 0 && password.trim().length() > 0) {
                if (conMgr.getActiveNetworkInfo() != null
                        && conMgr.getActiveNetworkInfo().isAvailable()
                        && conMgr.getActiveNetworkInfo().isConnected()) {
                    checkLogin(this, username, password);
                } else {
                    Toast.makeText(getApplicationContext() ,"No Internet Connection", Toast.LENGTH_LONG).show();
                }
            } else {
                // Prompt user to enter credentials
                Toast.makeText(getApplicationContext() ,"Kolom tidak boleh kosong", Toast.LENGTH_LONG).show();
            }
        });

        btn_register.setOnClickListener(v -> {
            // TODO Auto-generated method stub
            intent = new Intent(Login.this, Register.class);
            finish();
            startActivity(intent);
        });

    }

    private void checkLogin(final Context context, final String email, final String password) {
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Logging in ...");
        showDialog();

//        StringRequest strReq = new StringRequest(Request.Method.POST, url, response -> {
//            Log.e(TAG, "Login Response: " + response);
//            hideDialog();
//
//            try {
//                JSONObject jObj = new JSONObject(response);
//                success = jObj.getInt(TAG_SUCCESS);
//
//                // Check for error node in json
//                if (success == 1) {
//                    String username1 = jObj.getString(TAG_USERNAME);
//                    String id = jObj.getString(TAG_ID);
//
//                    Log.e("Successfully Login!", jObj.toString());
//
//                    Toast.makeText(getApplicationContext(), jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();
//
//                    // menyimpan login ke session
//                    SharedPreferences.Editor editor = sharedpreferences.edit();
//                    editor.putBoolean(session_status, true);
//                    editor.putString(TAG_ID, id);
//                    editor.putString(TAG_USERNAME, username1);
//                    editor.commit();
//
//                    // Memanggil main activity
//                    Intent intent = new Intent(Login.this, MainActivity1.class);
//                    intent.putExtra(TAG_ID, id);
//                    intent.putExtra(TAG_USERNAME, username1);
//                    finish();
//                    startActivity(intent);
//                } else {
//                    Toast.makeText(getApplicationContext(),
//                            jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();
//
//                }
//            } catch (JSONException e) {
//                // JSON error
//                e.printStackTrace();
//            }
//
//        }, error -> {
//            Log.e(TAG, "Login Error: " + error.getMessage());
//            Toast.makeText(getApplicationContext(),
//                    error.getMessage(), Toast.LENGTH_LONG).show();
//
//            hideDialog();
//
//        }) {
//
//            @Override
//            protected Map<String, String> getParams() {
//                // Posting parameters to login url
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("username", username);
//                params.put("password", password);
//
//                return params;
//            }
//
//        };

        // Adding request to request queue
//        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);

        loginResponse = new LoginResponse();
//        loginResponse.setRequestBody(email, password);
        loginResponse.setRequest(email, password);
        loginResponse.getClient().newCall(loginResponse.getRequest()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(context ,"Response Failure: "+e, Toast.LENGTH_LONG).show());
                hideDialog();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String username = null;
                    String id = null;
                    String data = Objects.requireNonNull(response.body()).string();
//                    String data = "{\n" +
//                            "    \"meta\": [{\n" +
//                            "        \"code\": 200,\n" +
//                            "        \"status\": \"success\",\n" +
//                            "        \"message\": \"Authenticated\"\n" +
//                            "    }],\n" +
//                            "    \"data\": [{\n" +
//                            "        \"access_token\": \"48|8XTuxygQFtP2uMc7h074v3PrsXFsQBfGgNO9YC3N\",\n" +
//                            "        \"token_type\": \"Bearer\",\n" +
//                            "        \"user\": [{\n" +
//                            "            \"id\": 10,\n" +
//                            "            \"name\": \"Rspace\",\n" +
//                            "            \"gender\": \"laki-laki\",\n" +
//                            "            \"address\": \"Solo\",\n" +
//                            "            \"email\": \"osea.dev@gmail.com\",\n" +
//                            "            \"email_verified_at\": null,\n" +
//                            "            \"phonenumber\": \"081212228699\",\n" +
//                            "            \"current_team_id\": null,\n" +
//                            "            \"profile_photo_path\": null,\n" +
//                            "            \"created_at\": \"2022-04-05T06:14:57.000000Z\",\n" +
//                            "            \"updated_at\": \"2022-04-06T21:34:52.000000Z\",\n" +
//                            "            \"role_id\": 1,\n" +
//                            "            \"sip\": null,\n" +
//                            "            \"ktp\": null,\n" +
//                            "            \"profile_photo_url\": \"https://ui-avatars.com/api/?name=R&color=7F9CF5&background=EBF4FF\"\n" +
//                            "        }]\n" +
//                            "    }]\n" +
//                            "}";
                    try {
                        JSONObject object = new JSONObject(data);
                        String user = object.getJSONObject("data").getString("user");
                        JSONObject getUser = new JSONObject(user);
                        username = getUser.getString("name");
                        id = getUser.getString("id");

                        Log.v("user",username);
                        Log.v("id",id);
//                        JSONObject object = new JSONObject(data);
//                        JSONArray array = object.getJSONArray("data");
////                        String str = array.toString();
////                        JSONObject theData = new JSONObject(str);
////                        JSONArray jsonArray = theData.getJSONArray("user");
//                        for (int i = 0; i < array.length(); i++) {
//                            JSONObject jsonObject = array.getJSONObject(i);
////                            Log.v("tag", jsonObject.getString("user"));
//                            String stringData = jsonObject.getString("user");
////                            JSONObject theObject = new JSONObject(stringData);
//                            JSONArray theAray = new JSONArray(stringData);
//                            for (int j = 0; j < theAray.length(); j++) {
//                                JSONObject theObjects = theAray.getJSONObject(j);
//                                username = theObjects.getString("name");
//                                id = theObjects.getString("id");
//                                Log.v("name", theObjects.getString("name"));
//                                Log.v("id", theObjects.getString("id"));
//                            }
//                        }

                        // menyimpan login ke session
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putBoolean(session_status, true);
                        editor.putString(TAG_ID, id);
                        editor.putString(TAG_USERNAME, username);
                        editor.apply();

                        // Memanggil main activity
                        Intent intent = new Intent(Login.this, MainActivity1.class);
                        intent.putExtra(TAG_ID, id);
                        intent.putExtra(TAG_USERNAME, username);
                        finish();
                        startActivity(intent);
                        hideDialog();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.v("tag", e.toString());
                    }
                } else {
                    hideDialog();
                    runOnUiThread(() -> Toast.makeText(context, response.message(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}