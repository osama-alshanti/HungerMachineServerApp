package com.example.osama.androideatitserver;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.osama.androideatitserver.Common.Common;
import com.example.osama.androideatitserver.Interface.ItemClickListener;
import com.example.osama.androideatitserver.Service.ListenOrder;
import com.example.osama.androideatitserver.ViewHolder.MenuViewHolder;
import com.example.osama.androideatitserver.Model.Category;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import io.paperdb.Paper;


public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawer;

    FirebaseDatabase database;
    DatabaseReference categories;

    FirebaseStorage storage;
    StorageReference storageReference;

    FirebaseRecyclerAdapter<Category,MenuViewHolder> adapter;

    TextView txtFullName;

    RecyclerView recyclerViewMenu;
    RecyclerView.LayoutManager layoutManager ;

    MaterialEditText edtName;
    Button btnSelect,btnUpload;
    Category newCategory;

    Uri saveUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu Managmment");
        setSupportActionBar(toolbar);

        database = FirebaseDatabase.getInstance();
        categories = database.getReference("Restaurants").child(Common.restaurantSelected).child("detail").child("Category");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

         drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        txtFullName = headerView.findViewById(R.id.txtFullName);
        txtFullName.setText("Welcome Admin: "+ Common.currentUser.getName());


        recyclerViewMenu = findViewById(R.id.recycler_menu);
        recyclerViewMenu.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerViewMenu.setLayoutManager(layoutManager);

        if(Common.isConnectedToInternet(this)){
            loadMenu();
        }else{
            Toast.makeText(this, "Please Check Your Connection!!", Toast.LENGTH_SHORT).show();
            return;
        }

       Intent service = new Intent(Home.this, ListenOrder.class);
        startService(service);

    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Category");
        builder.setMessage("Please Fill Full Information");

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.add_new_menu_layout,null);

        edtName = view.findViewById(R.id.edtName);
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

                if(newCategory !=null){

                    categories.push().setValue(newCategory);
                    Snackbar.make(drawer,"New Category "+newCategory.getName()+" Was added",Snackbar.LENGTH_SHORT)
                            .show();
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

            final ProgressDialog dialog = new ProgressDialog(Home.this);
            dialog.setMessage("Uploading...");
            dialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("image/"+imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            dialog.dismiss();
                            Toast.makeText(Home.this, "Uploaded !!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    newCategory = new Category(edtName.getText().toString(),uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(Home.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data !=null &&data.getData() !=null){

            saveUri = data.getData();
            btnSelect.setText("Image Selected !");

        }

    }

    private void chooseImage() {

        Intent intent = new Intent();
        intent.setType("image/*"); //folder
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),Common.PICK_IMAGE_REQUEST);
    }

    private void loadMenu() {

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(Category.class,R.layout.menu_item,MenuViewHolder.class,categories) {
            @Override
            protected void populateViewHolder(MenuViewHolder viewHolder, Category model, int position) {

                viewHolder.txtMenuName.setText(model.getName());

                Picasso.get().load(model.getImage()).into(viewHolder.imageView);

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        Intent intent = new Intent(Home.this,FoodList.class);
                        intent.putExtra("CategoryId",adapter.getRef(position).getKey());
                        startActivity(intent);

                    }
                });
            }
        };

        adapter.notifyDataSetChanged();
        recyclerViewMenu.setAdapter(adapter);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.refresh) {
            loadMenu();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {

        } else if (id == R.id.nav_cart) {


        } else if (id == R.id.nav_orders) {

            startActivity(new Intent(Home.this,OrderStatus.class));


        }else if(id == R.id.nav_offers){
            startActivity(new Intent(Home.this,OffersActivity.class));
        }

        else if (id == R.id.nav_log_out) {

            Paper.book().destroy();

            Intent signIn = new Intent(Home.this,SignIn.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signIn);


        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public boolean onContextItemSelected(final MenuItem item) {

        if (item.getTitle().equals(Common.UPDATE)){

            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));

        }else if(item.getTitle().equals(Common.DELETE )) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
            builder.setTitle("Delete Category");
            builder.setMessage("Are You Sure To Delete ?");
            builder.setIcon(R.drawable.ic_delete_forever_black_24dp);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    deleteCategory(adapter.getRef(item.getOrder()).getKey());
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

        return super.onContextItemSelected(item);
    }



    private void showUpdateDialog(final String key, final Category item) {

        AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
        builder.setTitle("Update Category");
        builder.setMessage("Please Fill Full Information");

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.add_new_menu_layout,null);

        edtName = view.findViewById(R.id.edtName);
        btnSelect = view.findViewById(R.id.btnSelect);
        btnUpload = view.findViewById(R.id.btnUpload);

        edtName.setText(item.getName());

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
               categories.child(key).setValue(item);
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

    private void deleteCategory(String key) {
        //delete all food .. category + food item ;
        //firstly we neeed get all food in category

        DatabaseReference foods = database.getReference("Foods");
        Query foodInCategory = foods.orderByChild("menuId").equalTo(key);

        foodInCategory.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              for (final DataSnapshot postSnapShot : dataSnapshot.getChildren()){
                  postSnapShot.getRef().removeValue();
              }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        categories.child(key).removeValue();
        Toast.makeText(this, "Item deleted !!", Toast.LENGTH_SHORT).show();
    }

    private void changeImage(final Category item) {
        if(saveUri !=null){

            final ProgressDialog dialog = new ProgressDialog(Home.this);
            dialog.setMessage("Uploading...");
            dialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("image/"+imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            dialog.dismiss();
                            Toast.makeText(Home.this, "Uploaded !!", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(Home.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
