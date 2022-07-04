package com.tensorflow.android.example.stetoskopdigital1;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.textfield.TextInputEditText;
import com.tensorflow.android.R;
import com.tensorflow.android.services.RegisterResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Register extends AppCompatActivity {

    ProgressDialog pDialog;
    Button btn_register, btn_login;
    TextInputEditText txt_username, txt_password, txt_confirm_password, txt_fullname, txt_phone, txt_ktp;
    AutoCompleteTextView txt_gender, txt_role;
    Intent intent;

//    int success;
    ConnectivityManager conMgr;

//    private String url = Server.URL + "register.php";
//
//    private static final String TAG = Register.class.getSimpleName();
//
//    private static final String TAG_SUCCESS = "success";
//    private static final String TAG_MESSAGE = "message";
//
//    String tag_json_obj = "json_obj_req";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

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

        btn_login = findViewById(R.id.btn_login);
        btn_register = findViewById(R.id.btn_register);
        txt_username = findViewById(R.id.txt_username);
        txt_password = findViewById(R.id.txt_password);
        txt_confirm_password = findViewById(R.id.confirm_password);
        txt_gender = findViewById(R.id.gender);
        txt_fullname = findViewById(R.id.name);
        txt_ktp = findViewById(R.id.ktp);
        txt_phone = findViewById(R.id.phone);
        txt_role = findViewById(R.id.role);

        // list gender config
        ArrayList<String> genderList = new ArrayList<>();
        genderList.add("Laki-laki");
        genderList.add("Perempuan");

        ArrayAdapter genderAdapter = new ArrayAdapter(this,R.layout.gender_item_layout,genderList);
        txt_gender.setAdapter(genderAdapter);

        // list role config
        ArrayList<String> roleList = new ArrayList<>();
        roleList.add("Dokter");
        roleList.add("Pasien");

        ArrayAdapter roleAdapter = new ArrayAdapter(this,R.layout.role_item_layout,roleList);
        txt_role.setAdapter(roleAdapter);

        btn_login.setOnClickListener(v -> {
            // TODO Auto-generated method stub
            intent = new Intent(Register.this, Login.class);
            finish();
            startActivity(intent);
        });

        btn_register.setOnClickListener(v -> {
            // TODO Auto-generated method stub
            String fullname = Objects.requireNonNull(txt_fullname.getText()).toString();
            String email = Objects.requireNonNull(txt_username.getText()).toString();
            String password = Objects.requireNonNull(txt_password.getText()).toString();
            String ktp = Objects.requireNonNull(txt_ktp.getText()).toString();
            String phone = Objects.requireNonNull(txt_phone.getText()).toString();
            String gender = txt_gender.getText().toString();
            String confirm_password = Objects.requireNonNull(txt_confirm_password.getText()).toString();
            String roleID = null;

            if (txt_role.getText().toString().equals("Dokter")) {
                roleID = "1";
            } else if (txt_role.getText().toString().equals("Pasien")) {
                roleID = "2";
            }

//            Log.v("Result: ",fullname+" "+email+" "+password+" "+ktp+" "+phone+" "+gender+" "+confirm_password+" "+roleID);

            if (conMgr.getActiveNetworkInfo() != null
                    && conMgr.getActiveNetworkInfo().isAvailable()
                    && conMgr.getActiveNetworkInfo().isConnected()) {
                if (!(fullname.isEmpty() || password.isEmpty() || email.isEmpty() || ktp.isEmpty() || phone.isEmpty() || gender.isEmpty()
                    || confirm_password.isEmpty() || roleID == null)) {
                    if (password.equals(confirm_password)) {
                        checkRegister(this, fullname, email,password, ktp, phone, gender, confirm_password, roleID);
                    } else {
                        Toast.makeText(getApplicationContext(), "Confirm password doesn't match", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please fill all field!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void checkRegister(Context context, String name ,String email, String password, String ktp, String phone, String gender, String pwConfirm, String roleID) {
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Registering ...");
        showDialog();

        RegisterResponse registerResponse = new RegisterResponse();
        registerResponse.setRequestBody(name, email, password, ktp, phone, gender, pwConfirm, roleID);
        registerResponse.setRequest();
        registerResponse.getClient().newCall(registerResponse.getRequest()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(context ,"Response Failure: "+e, Toast.LENGTH_LONG).show());
                hideDialog();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    if (response.code() == 200) {
                        runOnUiThread(() -> Toast.makeText(context, response.message(), Toast.LENGTH_LONG).show());
                        startActivity(new Intent(Register.this,Login.class));
                        finish();
                        hideDialog();
                    } else {
                        hideDialog();
                        runOnUiThread(() -> Toast.makeText(context, response.message(), Toast.LENGTH_LONG).show());
                    }
                } else {
                    hideDialog();
                    runOnUiThread(() -> Toast.makeText(context, response.message(), Toast.LENGTH_LONG).show());
                }
            }
        });

//        StringRequest strReq = new StringRequest(Request.Method.POST, url, response -> {
//            Log.e(TAG, "Register Response: " + response.toString());
//            hideDialog();
//
//            try {
//                JSONObject jObj = new JSONObject(response);
//                success = jObj.getInt(TAG_SUCCESS);
//
//                // Check for error node in json
//                if (success == 1) {
//
//                    Log.e("Successfully Register!", jObj.toString());
//
//                    Toast.makeText(getApplicationContext(),
//                            jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();
//
//                    txt_username.setText("");
//                    txt_password.setText("");
//                    txt_confirm_password.setText("");
//
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
//                params.put("confirm_password", confirm_password);
//
//                return params;
//            }
//
//        };
//
//        // Adding request to request queue
//        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        intent = new Intent(Register.this, Login.class);
        finish();
        startActivity(intent);
    }
}