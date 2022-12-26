package com.legal.adviser;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import com.legal.adviser.Models.UserModel;

public class Signup extends AppCompatActivity {

    EditText user_name, user_email, user_password, uc_password, user_phone, user_address;
    Button signup_btn, image_btn;
    TextView s1, s2,moveAccount, showpath;
    ProgressDialog progressDialog;
    Switch select_log;

    FirebaseAuth auth;
    Uri selectedImage = null;
    Boolean status_pick = false;
    Uri DownloadUrl;

    FirebaseStorage storage;
    StorageReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_signup);

        progressDialog = new ProgressDialog(this);
        storage = FirebaseStorage.getInstance();
        reference = storage.getReference();
        auth = FirebaseAuth.getInstance();

        s1 = findViewById(R.id.textView);
        s2 = findViewById(R.id.textView2);
        user_name = findViewById(R.id.user_name);
        user_email = findViewById(R.id.user_email);
        user_password = findViewById(R.id.user_password);
        uc_password = findViewById(R.id.uc_password);
        user_phone = findViewById(R.id.user_phone);
        signup_btn = findViewById(R.id.signup_btn);
        moveAccount = findViewById(R.id.moveAccount);
        select_log = findViewById(R.id.select_log);
        user_address = findViewById(R.id.user_address);
        image_btn = findViewById(R.id.choose_btn);
        showpath = findViewById(R.id.show_path);

        image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
        });

        select_log.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                    select_log.setText("Client");
                else
                    select_log.setText("Lawyer");
            }
        });

        s2.setTypeface(s2.getTypeface(), Typeface.BOLD);

        s1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Signup.this,LoginActivity.class));
                finish();
            }
        });


        moveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Signup.this,LoginActivity.class));
                finish();
            }
        });

        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckValidation();
            }
        });
    }

    private void CheckValidation() {
        String Name = user_name.getText().toString();
        String Email = user_email.getText().toString();
        String Password = user_password.getText().toString();
        String CPassword = uc_password.getText().toString();
        String Phone = user_phone.getText().toString();
        String Address = user_address.getText().toString();

        if(TextUtils.isEmpty(Name)){
            user_name.setError("Enter Name Here");
        }
        else if(TextUtils.isEmpty(Email) || !(Patterns.EMAIL_ADDRESS.matcher(Email).matches())){
            user_email.setError("Enter Email here");
        }
        else if(TextUtils.isEmpty(Password)){
            user_password.setError("Enter Password here");
        }
        else if(TextUtils.isEmpty(CPassword)){
            uc_password.setError("Again Enter here");
        }
        else if(TextUtils.isEmpty(Phone)){
            user_phone.setError("Enter Phone here");
        }
        else if(TextUtils.isEmpty(Address)){
            user_address.setError("Enter Address here");
        }
        else if(!Password.equals(CPassword)){
            Toast.makeText(this, "Password Not Matched", Toast.LENGTH_SHORT).show();
        }else if (selectedImage!=null){
            if((select_log.getText().toString()).equals("Lawyer")){
                UpdateLawyerData(Name,Email,Password,Phone,Address,"Lawyer");
            }
            if((select_log.getText().toString()).equals("Client")){
                UpdateClientData(Name,Email,Password,Phone,Address,"Client");
            }
        }
        else{
            Toast.makeText(this, "Image is required", Toast.LENGTH_SHORT).show();

        }
    }

    private void UpdateClientData(final String name, final String email, final String password, final String phone, final String address, String status) {
        progressDialog.setMessage("Data Uploaded\nPlease Wait...");
        progressDialog.show();
        progressDialog.setCancelable(false);
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    final StorageReference ref = reference.child("ClientImages/"+ auth.getCurrentUser().getUid());
                    ref.putFile(selectedImage).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if(!task.isSuccessful()){
                                throw task.getException();
                            }
                            return ref.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful()){
                                status_pick = false;
                                DownloadUrl = task.getResult();
                                UploadDatainClient(name,email,password,phone,address,"Client",DownloadUrl);
                                progressDialog.dismiss();
                                Toast.makeText(Signup.this, "Created", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                progressDialog.dismiss();
                                Toast.makeText(Signup.this, "URL Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    Toast.makeText(Signup.this, "Authenticated", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(Signup.this, "Authentication Failed:"+task.toString(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Signup.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void UploadDatainClient(String name, String email, String password, String phone, String address, String status, Uri downloadUrl) {
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date date = new Date();
        final String DateTime = dateFormat.format(date);

        String token = FirebaseInstanceId.getInstance().getToken();

        UserModel values = new UserModel(token,name,email,password,phone,address,status,downloadUrl.toString(),DateTime);
        FirebaseDatabase.getInstance().getReference("Client").child(auth.getCurrentUser().getUid()).setValue(values)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            progressDialog.dismiss();
                            auth.signOut();
                            Intent i = new Intent(Signup.this, LoginActivity.class);
                            startActivity(i);
                            finish();

                        }else{
                            progressDialog.dismiss();
                            //Alert Dialog
                            AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext()).create();
                            alertDialog.setTitle("Uploading Failed");
                            alertDialog.setCancelable(false);
                            alertDialog.setMessage("Due to\nTechnical Issue\nTry Again");
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            //alertDialog.show();
                        }
                    }
                });
    }

    private void UpdateLawyerData(final String name, final String email, final String password, final String phone, final String address, String lawyer) {
        progressDialog.setMessage("Data Uploaded\nPlease Wait...");
        progressDialog.show();
        progressDialog.setCancelable(false);
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    final StorageReference ref = reference.child("LawyerImages/"+ auth.getCurrentUser().getUid());
                    ref.putFile(selectedImage).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if(!task.isSuccessful()){
                                throw Objects.requireNonNull(task.getException());
                            }
                            return ref.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful()){
                                status_pick = false;
                                DownloadUrl = task.getResult();
                                UploadDatainLawyer(name,email,password,phone,address,"Lawyer",DownloadUrl);
                                progressDialog.dismiss();
                                Toast.makeText(Signup.this, "URL Created", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                progressDialog.dismiss();
                                Toast.makeText(Signup.this, "URL Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    Toast.makeText(Signup.this, "Authentication Working", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(Signup.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void UploadDatainLawyer(String name, String email, String password, String phone, String address, String status, Uri downloadUrl) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date date = new Date();
        final String DateTime = dateFormat.format(date);

        String token = FirebaseInstanceId.getInstance().getToken();

        UserModel values = new UserModel(token,name,email,password,phone,address,status,downloadUrl.toString(),DateTime);
        FirebaseDatabase.getInstance().getReference("Lawyer").child(Objects.requireNonNull(auth.getCurrentUser()).getUid()).setValue(values)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            progressDialog.dismiss();
                            auth.signOut();
                            Intent i = new Intent(Signup.this, LoginActivity.class);
                            startActivity(i);
                            finish();

                        }else{
                            progressDialog.dismiss();
                            //Alert Dialog
                            AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext()).create();
                            alertDialog.setTitle("Uploading Failed");
                            alertDialog.setCancelable(false);
                            alertDialog.setMessage("Due to\nTechnical Issue\nTry Again");
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            //alertDialog.show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK){
            selectedImage = data.getData();
            status_pick = true;
            showpath.setText(selectedImage.toString());
        }
    }
}