package com.example.bietkinlik;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    EditText edt_username, edt_city, edt_email, edt_password, edt_cpassword;
    Button btn_signUp;
    TextView txt_goSignIn,eye;
    FirebaseAuth fAuth;
    DatabaseReference dRef;
    ProgressDialog pDlog;
    private int setPtype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_sign_up);

        setPtype = 1;
        edt_username = findViewById(R.id.username);
        edt_city = findViewById(R.id.city);
        edt_email = findViewById(R.id.email);
        edt_password = findViewById(R.id.password);
        edt_cpassword = findViewById(R.id.cpassword);
        btn_signUp = findViewById(R.id.signUp);
        txt_goSignIn = findViewById(R.id.goSignIn);
        eye = findViewById(R.id.eye);

        fAuth = FirebaseAuth.getInstance();

        txt_goSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            }
        });
        eye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (setPtype == 1) {
                    setPtype = 0;
                    edt_password.setTransformationMethod(null);
                    if (edt_password.getText().length() > 0)
                        edt_password.setSelection(edt_password.getText().length());
                    edt_cpassword.setTransformationMethod(null);
                    if (edt_cpassword.getText().length() > 0)
                        edt_cpassword.setSelection(edt_cpassword.getText().length());
                    eye.setBackgroundResource(R.drawable.eye);
                } else {
                    setPtype = 1;
                    edt_password.setTransformationMethod(new PasswordTransformationMethod());
                    if (edt_password.getText().length() > 0)
                        edt_password.setSelection(edt_password.getText().length());
                    edt_cpassword.setTransformationMethod(new PasswordTransformationMethod());
                    if (edt_cpassword.getText().length() > 0)
                        edt_cpassword.setSelection(edt_cpassword.getText().length());
                    eye.setBackgroundResource(R.drawable.eyehidden);
                }
            }
        });
        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDlog = new ProgressDialog(SignUpActivity.this);
                pDlog.setMessage("Lütfen Bekleyin...");
                pDlog.show();

                String str_username = edt_username.getText().toString();
                String str_city = edt_city.getText().toString();
                String str_email = edt_email.getText().toString();
                String str_password = edt_password.getText().toString();
                String str_cpassword = edt_cpassword.getText().toString();

                    if (TextUtils.isEmpty(str_username) || TextUtils.isEmpty(str_city) || TextUtils.isEmpty(str_email) || TextUtils.isEmpty(str_password) || TextUtils.isEmpty(str_cpassword)) {
                        Toast.makeText(SignUpActivity.this, "Lütfen bütün alanları doldurun...", Toast.LENGTH_SHORT).show();
                    } else if (str_password.length() < 6) {
                        Toast.makeText(SignUpActivity.this, "Şifreniz minimum 6 karakter olmalı...", Toast.LENGTH_SHORT).show();
                    } else{
                        if (str_password.matches(str_cpassword)) {
                            SignUp(str_username, str_city, str_email, str_password);
                        } else {
                        Toast.makeText(SignUpActivity.this, "Şifreler Uyuşmuyor...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void SignUp(final String usernames, final String citys, String emails, String passwords) {
        fAuth.createUserWithEmailAndPassword(emails, passwords)
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            FirebaseUser fUser = fAuth.getCurrentUser();
                            String userId = fUser.getUid();
                            dRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("id", userId);
                            hashMap.put("username", usernames.toLowerCase());
                            hashMap.put("city", citys);
                            hashMap.put("resimurl", "https://firebasestorage.googleapis.com/v0/b/bietkinlik-3d8ac.appspot.com/o/userlogo.jpg?alt=media&token=73243b93-e5e8-43f2-983c-e55eb45debcc");
                            hashMap.put("activity","false");

                            dRef.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        pDlog.dismiss();
                                        Intent ıntent = new Intent(SignUpActivity.this, AppActivity.class);
                                        ıntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(ıntent);
                                    }
                                }
                            });
                        } else {
                            pDlog.dismiss();
                            Toast.makeText(SignUpActivity.this, "Bu şifre veya mail ile kayıt başarısız...", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
