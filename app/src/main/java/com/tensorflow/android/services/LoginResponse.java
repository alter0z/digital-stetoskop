package com.tensorflow.android.services;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginResponse {
//    private final Context context;
    private final OkHttpClient client;
    private RequestBody body;
    private Request request;
    private Response response;

    public LoginResponse() {
//        this.context = context;

        client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("text/plain");
    }

    public void setRequestBody(String email, String password) {
        body = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("email",email)
                .addFormDataPart("password",password).build();
    }

    public void setRequest() {
        request = new Request.Builder().url("http://stetoskopdigital.com/api/login").method("POST",body).build();
    }

    public Response getResponse() {
        return response;
    }

    public OkHttpClient getClient() {
        return client;
    }

    public Request getRequest() {
        return request;
    }
}
