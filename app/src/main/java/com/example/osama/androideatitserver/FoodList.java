package com.example.osama.androideatitserver;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.osama.androideatitserver.Common.Common;
import com.example.osama.androideatitserver.Interface.ItemClickListener;
import com.example.osama.androideatitserver.Model.Category;
import com.example.osama.androideatitserver.ViewHolder.FoodViewHolder;
import com.example.osama.androideatitserver.Model.Food;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import static com.example.osama.androideatitserver.Common.Common.PICK_IMAGE_REQUEST;

public class FoodList extends AppCompatActivity {

    RelativeLayout rootLayout;


    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database ;
    DatabaseReference foodList;

    FirebaseStorage storage;
    StorageReference storageReference;

    MaterialEditText edtName,edtPrice,edtDescription,edtDiscount;
    Button btnSelect,btnUpload;
    Food newFood;


    String categoryId = "";

    FloatingActionButton fab;

    FirebaseRecyclerAdapter<Food,FoodViewHolder> adapter;
     Uri saveUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Restaurants").child(Common.restaurantSelected).child("detail").child("Foods");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        recyclerView = findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //rootLayout = findViewById(R.id.rootLayout);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        //Need category Id when User Click to Menu
        if(getIntent() != null)
            categoryId = getIntent().getStringExtra("CategoryId");

        if(!categoryId.isEmpty() && categoryId !=null){

            if(Common.isConnectedToInternet(getBaseContext())){

                loadListFood(categoryId);
            }else{
                Toast.makeText(FoodList.this, "Please Check Your Connection!!", Toast.LENGTH_SHORT).show();
                return;
            }

        }

    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(FoodList.this);
        builder.setTitle("Add New Food");
        builder.setMessage("Please Fill Full Information");

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.add_new_food_layout,null);

        edtName = view.findViewById(R.id.edtName);
        edtPrice = view.findViewById(R.id.edtPrice);
        edtDescription = view.findViewById(R.id.edtDescription);
        edtDiscount = view.findViewById(R.id.edtDiscount);

        btnSelect = view.findViewById(R.id.btnSelect);
        btnUpload = view.findViewById(R.id.btnUpload);
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage(); //let user select image from gallery and save uri
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        builder.setView(view);
        builder.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                if(newFood !=null){

                    foodList.push().setValue(newFood);
                    Snackbar.make(rootLayout,"New Food "+newFood.getName()+" Was added",Snackbar.LENGTH_SHORT).show();
                }

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

    private void uploadImage() {
        if(saveUri !=null){

            final ProgressDialog dialog = new ProgressDialog(FoodList.this);
            dialog.setMessage("Uploading...");
            dialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("image/"+imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            dialog.dismiss();
                            Toast.makeText(FoodList.this, "Uploaded !!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                            newFood = new Food();
                            newFood.setName(edtName.getText().toString());
                            newFood.setDescription(edtDescription.getText().toString());
                            newFood.setDiscount(edtDiscount.getText().toString());
                            newFood.setPrice(edtPrice.getText().toString());
                            newFood.setImage(uri.toString());
                            newFood.setMenuId(categoryId);

                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(FoodList.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            dialog.setMessage("Uploaded "+progress);
                        }
                    });

        }
    }

    private void chooseImage() {

        Intent intent = new Intent();
        intent.setType("image/*"); //folder
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),Common.PICK_IMAGE_REQUEST);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data !=null &&data.getData() !=null){

            saveUri = data.getData();
            btnSelect.setText("Image Selected !");

        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getTitle().equals(Common.UPDATE)){

            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));

        }else if(item.getTitle().equals(Common.DELETE )) {
            deleteUpdate(adapter.getRef(item.getOrder()).getKey());

        }

        return super.onContextItemSelected(item);
    }

    private void deleteUpdate(final String key) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(FoodList.this);
        builder.setTitle("Delete Category");
        builder.setMessage("Are You Sure To Delete ?");
        builder.setIcon(R.drawable.ic_delete_forever_black_24dp);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                foodList.child(key).removeValue();
                Toast.makeText(getBaseContext(), "Item deleted !!", Toast.LENGTH_SHORT).show();
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


    private void changeImage(final Food item) {
        if(saveUri !=null){

            final ProgressDialog dialog = new ProgressDialog(FoodList.this);
            dialog.setMessage("Uploading...");
            dialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("image/"+imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            dialog.dismiss();
                            Toast.makeText(FoodList.this, "Uploaded !!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    item.setImage(uri.toString());


                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(FoodList.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            dialog.setMessage("Uploaded "+progress);
                        }
                    });

        }
    }



    private void loadListFood(String categoryId) {
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class,R.layout.food_item,FoodViewHolder.class
                ,foodList.orderByChild("menuId").equalTo(categoryId)) { //like > Select * from Foods where MenuId =
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food food, int position) {
                viewHolder.food_name.setText(food.getName());

                if(food.getImage() == null){
                    Toast.makeText(FoodList.this, "NULL", Toast.LENGTH_SHORT).show();
                    Picasso.get().load(R.mipmap.my_bg).into(viewHolder.food_image);
                }else{
                    Picasso.get().load(food.getImage()).into(viewHolder.food_image);
                }

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        //Toast.makeText(FoodList.this, ""+local.getName(), Toast.LENGTH_SHORT).show();

                       /* Intent foodDetale = new Intent(FoodList.this,FoodDetail.class);
                        foodDetale.putExtra("foodId",adapter.getRef(position).getKey());
                        startActivity(foodDetale);
                        */



                    }
                });



            }
        };

//        Log.d("TAG",""+adapter.getItemCount());
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        }


    private void showUpdateDialog(final String key, final Food item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(FoodList.this);
        builder.setTitle("Update Food");
        builder.setMessage("Please Fill Full Information");

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.add_new_food_layout,null);

        edtName = view.findViewById(R.id.edtName);
        edtPrice = view.findViewById(R.id.edtPrice);
        edtDescription = view.findViewById(R.id.edtDescription);
        edtDiscount = view.findViewById(R.id.edtDiscount);

        edtName.setText(item.getName());
        edtPrice.setText(item.getPrice());
        edtDescription.setText(item.getDescription());
        edtDiscount.setText(item.getDiscount());

        btnSelect = view.findViewById(R.id.btnSelect);
        btnUpload = view.findViewById(R.id.btnUpload);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage(); //let user select image from gallery and save uri
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeImage(item);
            }
        });

        builder.setView(view);
        builder.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();


                item.setName(edtName.getText().toString());
                item.setPrice(edtPrice.getText().toString());
                item.setDiscount(edtDiscount.getText().toString());
                item.setDescription(edtDescription.getText().toString());

                foodList.child(key).setValue(item);
                Snackbar.make(rootLayout," Food "+item.getName()+"Was Edited",Snackbar.LENGTH_SHORT).show();


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



    }

