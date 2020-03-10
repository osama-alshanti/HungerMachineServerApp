package com.example.osama.androideatitserver;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.osama.androideatitserver.Common.Common;
import com.example.osama.androideatitserver.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.CheckBox;

import info.hoang8f.widget.FButton;
import io.paperdb.Paper;

public class SignIn extends AppCompatActivity {

    MaterialEditText edtPhone,edtPassword;
    FButton btnSignIn;
    TextView forgot_pass;
    CheckBox chbRemember;

    FirebaseDatabase database;
    DatabaseReference users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);

        btnSignIn = findViewById(R.id.btnSignIn);

        Paper.init(this);

        database = FirebaseDatabase.getInstance();
        users = database.getReference("User");

        chbRemember = findViewById(R.id.chbRemember);
        forgot_pass = findViewById(R.id.forgotPass);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Common.isConnectedToInternet(getBaseContext())) {

                    if(chbRemember.isChecked()){
                        Paper.book().write(Common.USER_KEY,edtPhone.getText().toString());
                        Paper.book().write(Common.PWD_KEY,edtPassword.getText().toString());
                    }

                    signInUser(edtPhone.getText().toString(), edtPassword.getText().toString());

                }else{
                    Toast.makeText(SignIn.this, "Please Check Your Connection!!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        forgot_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForgotPassword();
            }
        });
    }

    private void showForgotPassword() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(SignIn.this);
        builder.setTitle("Forgot Password");
        builder.setMessage("Enter your secure code");

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.forgot_password_layout,null);
        builder.setView(view);
        builder.setIcon(R.drawable.ic_security_black_24dp);

        final MaterialEditText edtPhone = view.findViewById(R.id.edtPhone);
        final MaterialEditText edtSecureCode = view.findViewById(R.id.edtSecureCode);



        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(edtPhone.getText().toString().equals("") ){
                    Toast.makeText(getBaseContext(), "Please Fill the you phone !", Toast.LENGTH_SHORT).show();
                    return;
                }else if(edtSecureCode.getText().toString().equals("")){
                    Toast.makeText(getBaseContext(), "Please Fill the you secure code !", Toast.LENGTH_SHORT).show();
                    return;
                }

                users.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);

                        if(user.getSecureCode().equals(edtSecureCode.getText().toString())){
                            Toast.makeText(SignIn.this, "Your Password : "+user.getPassword(), Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(SignIn.this, "Wrong secure code !", Toast.LENGTH_SHORT).show();
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

            }
        });
        builder.show();

    }


    private void signInUser(String phone, String password) {

        final ProgressDialog dialog = new ProgressDialog(SignIn.this);
        dialog.setMessage("Please Waiting ...");
        dialog.show();

        final String localPhone = phone;
        final String localPassword = password;

        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(localPhone).exists()) {
                    dialog.dismiss();
                    User user = dataSnapshot.child(localPhone).getValue(User.class);
                    user.setPhone(localPhone);

                    if (Boolean.parseBoolean(user.getIsStaff())) { // if true

                        if (user.getPassword().equals(localPassword)) {

                            Intent login = new Intent(SignIn.this,RestaurantList.class);
                            Common.currentUser = user;
                            startActivity(login);
                            finish();

                        }
                        else
                            Toast.makeText(SignIn.this, "Wrong Password !!!", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(SignIn.this, "Please Login with client Account !", Toast.LENGTH_SHORT).show();
                }
                else {
                    dialog.dismiss();
                    Toast.makeText(SignIn.this, "User not Exist in Database !", Toast.LENGTH_SHORT).show();
                }
            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
