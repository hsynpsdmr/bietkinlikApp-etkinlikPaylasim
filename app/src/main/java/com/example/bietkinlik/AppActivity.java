package com.example.bietkinlik;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.util.Objects;

public class AppActivity extends AppCompatActivity {

    private Button update, map, exit,message,profilguncel;
    private EditText username, city, email, password;
    private ImageView imagePP;
    private TextView eye;
    private FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    private DatabaseReference dRef;
    StorageReference storageReference;
    private String UserId,text,URL;
    private int setPtype;
    SharedPreference sharedPreference;
    Context context=this;
    private static final String TAG = "AppActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        setPtype = 1;
        update = findViewById(R.id.update);
        map = findViewById(R.id.map);
        username = findViewById(R.id.username);
        city = findViewById(R.id.city);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        imagePP = findViewById(R.id.imagePP);
        exit = findViewById(R.id.exit);
        eye = findViewById(R.id.eye);
        message=findViewById(R.id.message);
        profilguncel=findViewById(R.id.addpp);

        sharedPreference=new SharedPreference();
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        dRef = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        UserId = fAuth.getCurrentUser().getUid();

        StorageReference profileRef = storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(imagePP);
                URL=String.valueOf(uri);
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fAuth.signOut();
                    Intent main = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(main);
                }
        });
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str_city = city.getText().toString();
                UsersUpdate(str_city);

            }
        });
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityControl();
                Intent map = new Intent(AppActivity.this, MapsActivity.class);
                map.putExtra("url",URL);
                startActivity(map);
            }
        });
        eye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (setPtype == 1) {
                    setPtype = 0;
                    password.setTransformationMethod(null);
                    if (password.getText().length() > 0)
                        password.setSelection(password.getText().length());
                    eye.setBackgroundResource(R.drawable.eye);
                } else {
                    setPtype = 1;
                    password.setTransformationMethod(new PasswordTransformationMethod());
                    if (password.getText().length() > 0)
                        password.setSelection(password.getText().length());
                    eye.setBackgroundResource(R.drawable.eyehidden);
                }
            }
        });
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent msg = new Intent(AppActivity.this, ConversationsActivity.class);
                startActivity(msg);
            }
        });
        profilguncel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGalleryIntent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent,1000);

            }
        });

        UsersProfile();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000){
            if(resultCode == Activity.RESULT_OK){
                Uri imageUri = data.getData();
                uploadImageToFirebase(imageUri);
            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        final StorageReference fileRef = storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(imagePP);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AppActivity.this,"Failed",Toast.LENGTH_SHORT).show();
            }
        });
    }

//    public boolean isServicesOK(){
//        Log.d(TAG, "isServicesOK: checking google services version");
//
//        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(AppActivity.this);
//
//        if(available == ConnectionResult.SUCCESS){
//            //everything is fine and the user can make map requests
//            Log.d(TAG, "isServicesOK: Google Play Services is working");
//            return true;
//        }
//        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
//            //an error occured but we can resolve it
//            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
//            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(AppActivity.this, available, ERROR_DIALOG_REQUEST);
//            dialog.show();
//        }else{
//            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
//        }
//        return false;
//    }

    public void ActivityControl(){
        dRef.child("Users").child(UserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("activity"))) {
                    text= Objects.requireNonNull(dataSnapshot.child("activity").getValue()).toString();
                    sharedPreference.save(context,text);
                } else {
                    Toast.makeText(AppActivity.this, "Activity Kontrol Sağlanamadı!!!", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void UsersProfile() {
        dRef.child("Users").child(UserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("username")) && (dataSnapshot.hasChild("city"))) {
                    String getUsername = Objects.requireNonNull(dataSnapshot.child("username").getValue()).toString();
                    String getCity = Objects.requireNonNull(dataSnapshot.child("city").getValue()).toString();
                    username.setText(getUsername);
                    city.setText(getCity);
                    email.setText(Objects.requireNonNull(fAuth.getCurrentUser()).getEmail());
                } else {
                    Toast.makeText(AppActivity.this, "Profilinizi Düzenleyin...", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    public void UsersUpdate(String city){
        dRef.child("Users").child(UserId).child("city").setValue(city);
    }
}
