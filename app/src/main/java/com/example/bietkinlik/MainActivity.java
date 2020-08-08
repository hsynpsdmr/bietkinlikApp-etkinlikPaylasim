package com.example.bietkinlik;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    EditText edt_email, edt_password;
    Button btn_signIn;
    TextView txt_goSignUp,txt_forgotPass;
    FirebaseAuth fAuth;
    DatabaseReference dRef;
    ProgressDialog pDlog;
    FirebaseUser fUser;

    @Override
    protected  void onStart(){
        super.onStart();
        fUser=fAuth.getInstance().getCurrentUser();
        if(fUser!=null){
            startActivity(new Intent(MainActivity.this,AppActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_main);

        edt_email = findViewById(R.id.email);
        edt_password = findViewById(R.id.password);
        btn_signIn = findViewById(R.id.signIn);
        txt_goSignUp = findViewById(R.id.goSignUp);
        txt_forgotPass=findViewById(R.id.forgotPass);

        fAuth = FirebaseAuth.getInstance();

        txt_goSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SignUpActivity.class));
            }
        });
        txt_forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,ForgotPassActivity.class));
            }
        });
        btn_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pDlog = new ProgressDialog(MainActivity.this);
                pDlog.setMessage("Giriş Yapılıyor...");
                pDlog.show();

                String str_email = edt_email.getText().toString();
                String str_password = edt_password.getText().toString();

                if (TextUtils.isEmpty(str_email) || TextUtils.isEmpty(str_password)) {
                    Toast.makeText(MainActivity.this, "Lütfen bütün alanları doldurun...", Toast.LENGTH_LONG).show();
                } else {
                    fAuth.signInWithEmailAndPassword(str_email, str_password)
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        dRef = FirebaseDatabase.getInstance().getReference()
                                                .child("Users").child(fAuth.getCurrentUser().getUid());
                                        dRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                pDlog.dismiss();
                                                Intent ıntent = new Intent(MainActivity.this, AppActivity.class);
                                                ıntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(ıntent);
                                                finish();
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                pDlog.dismiss();
                                            }
                                        });
                                    } else {
                                        pDlog.dismiss();
                                        Toast.makeText(MainActivity.this, "Giriş başarısız...", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }

        });
    }
}

