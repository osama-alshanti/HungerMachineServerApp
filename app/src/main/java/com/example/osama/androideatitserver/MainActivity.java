package com.example.osama.androideatitserver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import info.hoang8f.widget.FButton;
import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    FButton btnSignIn;
    TextView txtslogan;
    TextView textlogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnSignIn = findViewById(R.id.btnSignIn);

        txtslogan = findViewById(R.id.txtslogan);
        textlogo =  findViewById(R.id.textlogo);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/NABILA.TTF");
        txtslogan.setTypeface(face);
        textlogo.setTypeface(face);

        Paper.init(this);


        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signIn = new Intent(MainActivity.this, SignIn.class);
                startActivity(signIn);
            }
        });

        String user = Paper.book().read(Common.USER_KEY);
        String pass = Paper.book().read(Common.PWD_KEY);

        if (user != null && pass != null) {
            if (!user.isEmpty() && !pass.isEmpty()){

                login(user, pass);


            }
        }
    }

    private void login(final String phone, final String pass) {
        if(Common.isConnectedToInternet(getBaseContext())){

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference table_user = database.getReference("User");

            final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Please Waiting ...");
            dialog.show();

            table_user.addValueEventListener(new ValueEventListener(){



                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                    //if the user not exist in datebase


                    if(dataSnapshot.child(phone).exists()){//0592244405



                        //get user information

                        User user = dataSnapshot.child(phone).getValue(User.class);
                        user.setPhone(phone);
                        if(user.getPassword().equals(pass)){
                            dialog.dismiss();
                            Intent homeIntent = new Intent(MainActivity.this,RestaurantList.class);
                            Common.currentUser = user;
                            startActivity(homeIntent);
                            finish();


                        }else{
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this, "Sign In Failed , Please Try Again !!!", Toast.LENGTH_SHORT).show();
                        }

                    }else{
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, "User not exist !!", Toast.LENGTH_SHORT).show();

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else{
            Toast.makeText(MainActivity.this, "Please Check Your Connection!!", Toast.LENGTH_SHORT).show();
            return;
        }

    }


}

