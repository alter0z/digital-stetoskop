package com.tensorflow.android.services;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterResponse {
    private final OkHttpClient client;
    private RequestBody body;
    private Request request;
    private Response response;

    public RegisterResponse() {
        client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
    }

    public void setRequestBody(String name ,String email, String password, String ktp, String phone, String gender, String pwConfirm, String roleID) {
        body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("name",name)
                .addFormDataPart("email",email)
                .addFormDataPart("password",password)
                .addFormDataPart("ktp",ktp)
                .addFormDataPart("phonenumber",phone)
                .addFormDataPart("gender",gender)
                .addFormDataPart("password_confirmation",pwConfirm)
                .addFormDataPart("role_id",roleID).build();
    }

    public void setRequest() {
        request = new Request.Builder().url("http://stetoskopdigital.com/api/register").method("POST",body).build();
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
