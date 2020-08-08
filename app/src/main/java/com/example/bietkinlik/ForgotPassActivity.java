package com.example.bietkinlik;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassActivity extends AppCompatActivity {

    Toolbar toolbar;
    ProgressBar progressBar;
    EditText edt_email;
    Button btn_resetPass;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);

        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
        edt_email = findViewById(R.id.resetEmail);
        btn_resetPass = findViewById(R.id.resetPass);

        toolbar.setTitle("Forgot Password");
        fAuth = FirebaseAuth.getInstance();

        btn_resetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fAuth.sendPasswordResetEmail(edt_email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if ((task.isSuccessful())) {
                            Toast.makeText(ForgotPassActivity.this, "Password send to your email.", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(ForgotPassActivity.this, MainActivity.class));
                        } else {
                            Toast.makeText(ForgotPassActivity.this, "WRONG E-MAIL!!!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}
