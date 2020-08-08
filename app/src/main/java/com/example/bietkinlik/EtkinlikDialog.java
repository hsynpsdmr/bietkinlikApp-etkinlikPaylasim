package com.example.bietkinlik;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Objects;

public class EtkinlikDialog extends AppCompatDialogFragment {

    private EditText etEtkinlik, etEtkinlikZamani;
    private FirebaseAuth fAuth;
    private DatabaseReference dRef;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_etkinlik_dialog, null);
        builder.setView(view)
                .setTitle("Etkinlik Giriniz")
                .setNegativeButton("Çık", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((MapsActivity) Objects.requireNonNull(getActivity())).DialogDelete();
                    }
                })
                .setPositiveButton("Kaydet", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AddEtkinlik();

                    }
                });
        etEtkinlik = view.findViewById(R.id.Etkinlik);
        etEtkinlikZamani = view.findViewById(R.id.EtkinlikZamani);
        fAuth = FirebaseAuth.getInstance();
        dRef = FirebaseDatabase.getInstance().getReference();
        return builder.create();
    }

    private void AddEtkinlik() {
        String etkinlik = etEtkinlik.getText().toString();
        String etkinlikzamani = etEtkinlikZamani.getText().toString();
        FirebaseUser fUser = fAuth.getCurrentUser();
        assert fUser != null;
        String userId = fUser.getUid();
        dRef = FirebaseDatabase.getInstance().getReference().child("Activitys").child(userId);
        dRef.child("activity").setValue(etkinlik);
        dRef.child("time").setValue(etkinlikzamani);
    }
}