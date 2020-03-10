package com.example.osama.androideatitserver;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.example.osama.androideatitserver.Model.Food;
import com.example.osama.androideatitserver.Model.Offers;
import com.example.osama.androideatitserver.Service.ListenOrder;
import com.example.osama.androideatitserver.ViewHolder.MenuViewHolder;
import com.example.osama.androideatitserver.ViewHolder.OffersViewHolder;
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

public class OffersActivity extends AppCompatActivity {
    RelativeLayout rootLayout;


    FirebaseDatabase database;
    DatabaseReference offers;

    FirebaseStorage storage;
    StorageReference storageReference;

    RecyclerView recyclerViewOffers;
    RecyclerView.LayoutManager layoutManager;

    MaterialEditText edtTitle,edtType,edtMin,edtDelivery;
    Button btnSelect,btnUpload;

    FloatingActionButton fab;

    Offers newOffers;
    Uri saveUri;


    FirebaseRecyclerAdapter<Offers, OffersViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers);

        database = FirebaseDatabase.getInstance();
        offers = database.getReference("Offers");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        recyclerViewOffers = findViewById(R.id.recycler_offers);
        recyclerViewOffers.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerViewOffers.setLayoutManager(layoutManager);

        rootLayout = findViewById(R.id.rootLayout);

        if (Common.isConnectedToInternet(this)) {
            loadOffers();
        } else {
            Toast.makeText(this, "Please Check Your Connection!!", Toast.LENGTH_SHORT).show();
            return;
        }
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });


    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(OffersActivity.this);
        builder.setTitle("Add New Offers");
        builder.setMessage("Please Fill Full Information");

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.add_new_offer_layout,null);

        edtTitle = view.findViewById(R.id.edtTitle);
        edtType = view.findViewById(R.id.edtType);
        edtMin = view.findViewById(R.id.edtMin);
        edtDelivery = view.findViewById(R.id.edtDelivery);

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

                if(newOffers !=null){

                    offers.push().setValue(newOffers);
                    Snackbar.make(rootLayout,"New Offer "+newOffers.getType()+" Was added",Snackbar.LENGTH_SHORT).show();
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

            final ProgressDialog dialog = new ProgressDialog(OffersActivity.this);
            dialog.setMessage("Uploading...");
            dialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("offers/"+imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            dialog.dismiss();
                            Toast.makeText(OffersActivity.this, "Uploaded !!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    newOffers = new Offers();
                                    newOffers.setTitle(edtTitle.getText().toString());
                                    newOffers.setType(edtType.getText().toString());
                                    newOffers.setMin(edtMin.getText().toString());
                                    newOffers.setDelivery(edtDelivery.getText().toString());
                                    newOffers.setImage(uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(OffersActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void loadOffers() {
        adapter = new FirebaseRecyclerAdapter<Offers,OffersViewHolder>(Offers.class,R.layout.offers_item,OffersViewHolder.class,offers) {

            @Override
            protected void populateViewHolder(OffersViewHolder viewHolder, Offers model, int position) {
                Picasso.get().load(model.getImage()).into(viewHolder.imageOffers);
                viewHolder.tv_title.setText(model.getTitle());
                viewHolder.tv_type.setText(model.getType());
                viewHolder.tv_min_buy.setText(model.getMin());
                viewHolder.tv_delivery_price.setText(model.getDelivery());

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                    }
                });


            }
        };

        adapter.notifyDataSetChanged();
        recyclerViewOffers.setAdapter(adapter);
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(OffersActivity.this);
        builder.setTitle("Delete Offers");
        builder.setMessage("Are You Sure To Delete ?");
        builder.setIcon(R.drawable.ic_delete_forever_black_24dp);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                offers.child(key).removeValue();
                Toast.makeText(getBaseContext(), "Offers deleted !!", Toast.LENGTH_SHORT).show();
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

    private void showUpdateDialog(final String key, final Offers item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(OffersActivity.this);
        builder.setTitle("Update Offers");
        builder.setMessage("Please Fill Full Information");

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.add_new_offer_layout,null);

        edtTitle = view.findViewById(R.id.edtTitle);
        edtType = view.findViewById(R.id.edtType);
        edtMin = view.findViewById(R.id.edtMin);
        edtDelivery = view.findViewById(R.id.edtDelivery);

        edtTitle.setText(item.getTitle());
        edtType.setText(item.getType());
        edtMin.setText(item.getMin());
        edtDelivery.setText(item.getDelivery());

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


                item.setTitle(edtTitle.getText().toString());
                item.setType(edtType.getText().toString());
                item.setMin(edtMin.getText().toString());
                item.setDelivery(edtDelivery.getText().toString());

                offers.child(key).setValue(item);
                Snackbar.make(rootLayout," Offer "+item.getType()+"Was Edited",Snackbar.LENGTH_SHORT).show();


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

    private void changeImage(final Offers item) {
        if(saveUri !=null){

            final ProgressDialog dialog = new ProgressDialog(OffersActivity.this);
            dialog.setMessage("Uploading...");
            dialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("offers/"+imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            dialog.dismiss();
                            Toast.makeText(OffersActivity.this, "Uploaded !!", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(OffersActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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

}



