package com.example.osama.androideatitserver;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.example.osama.androideatitserver.Common.Common;
import com.example.osama.androideatitserver.Interface.ItemClickListener;
import com.example.osama.androideatitserver.Model.Restaurant;
import com.example.osama.androideatitserver.ViewHolder.RestaurantViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class RestaurantList extends AppCompatActivity {
    FirebaseRecyclerAdapter<Restaurant, RestaurantViewHolder> adapter;


    RecyclerView recyclerView;
    FirebaseDatabase database;
    DatabaseReference restaurant;

    FirebaseStorage storage;
    StorageReference storageReference;

    FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);


        database = FirebaseDatabase.getInstance();
        restaurant = database.getReference("Restaurants");

        recyclerView = findViewById(R.id.recycler_restaurant);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (Common.isConnectedToInternet(this)) {
            loadRestaurant();
        } else {
            Toast.makeText(this, "Please Check Your Connection!!", Toast.LENGTH_SHORT).show();
            return;
        }

        loadRestaurant();

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });


    }

    private void showDialog() {
        Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show();
    }

    private void loadRestaurant() {
        adapter = new FirebaseRecyclerAdapter<Restaurant, RestaurantViewHolder>(Restaurant.class, R.layout.restaurant_item, RestaurantViewHolder.class, restaurant) {
            @Override
            protected void populateViewHolder(RestaurantViewHolder viewHolder, Restaurant model, int position) {
                viewHolder.txtRestaurantName.setText(model.getName());
                Picasso.get().load(model.getImage()).into(viewHolder.imageView);

                final Restaurant clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        Intent foodList = new Intent(RestaurantList.this, Home.class);
                        Common.restaurantSelected = adapter.getRef(position).getKey();
                        Toast.makeText(RestaurantList.this, ""+Common.restaurantSelected, Toast.LENGTH_SHORT).show();
                        startActivity(foodList);

                    }
                });

            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

    }


}
