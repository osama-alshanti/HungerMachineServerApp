package com.example.osama.androideatitserver;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.osama.androideatitserver.Common.Common;
import com.example.osama.androideatitserver.Interface.ItemClickListener;
import com.example.osama.androideatitserver.Model.Request;
import com.example.osama.androideatitserver.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jaredrummler.materialspinner.MaterialSpinner;

public class OrderStatus extends AppCompatActivity {


    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    MaterialSpinner spinner;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        recyclerView = findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        loadOrders();


    }

    private void loadOrders() {
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(Request.class, R.layout.order_layout,
                OrderViewHolder.class, requests) {
            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, final Request model, final int position) {

                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(model.getPhone());

                viewHolder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showUpdateDialog(adapter.getRef(position).getKey(),adapter.getItem(position));
                    }
                });
                viewHolder.btnRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(OrderStatus.this);
                        builder.setTitle("Delete Category");
                        builder.setMessage("Are You Sure To Delete ?");
                        builder.setIcon(R.drawable.ic_delete_forever_black_24dp);

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                deleteCategory(adapter.getRef(position).getKey());
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
                });
                viewHolder.btnDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent orderDetial = new Intent(OrderStatus.this,OrderDetail.class);
                        Common.currentRequest = model;
                        orderDetial.putExtra("OrderId",adapter.getRef(position).getKey());
                        startActivity(orderDetial);
                    }
                });
                viewHolder.btnDirection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(OrderStatus.this, "Loaction of: "+model.getAddress(), Toast.LENGTH_SHORT).show();
                        Intent trackingOrder = new Intent(OrderStatus.this,TrackingOrder.class);
                        Common.currentRequest = model;
                        startActivity(trackingOrder);
                    }
                });

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        if(!isLongClick){
                           /* Toast.makeText(OrderStatus.this, "Loaction of: "+model.getAddress(), Toast.LENGTH_SHORT).show();
                            Intent trackingOrder = new Intent(OrderStatus.this,TrackingOrder.class);
                            Common.currentRequest = model;
                            startActivity(trackingOrder);*/
                        }else{
                           /* Intent orderDetial = new Intent(OrderStatus.this,OrderDetail.class);
                            Common.currentRequest = model;
                            orderDetial.putExtra("OrderId",adapter.getRef(position).getKey());
                            startActivity(orderDetial);*/
                        }


                    }
                });


            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getTitle().equals(Common.UPDATE)){

            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));

        }else if(item.getTitle().equals(Common.DELETE )) {
            deleteCategory(adapter.getRef(item.getOrder()).getKey());

        }

        return super.onContextItemSelected(item);

    } // remove replace to button (btnEdit,btnRemove)

    private void showUpdateDialog(String key, final Request item) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(OrderStatus.this);
        builder.setTitle("Update Orders");
        builder.setMessage("Please Choose Status");

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.update_order_layout,null);

        spinner = (MaterialSpinner) view.findViewById(R.id.spinner);
        spinner.setItems("Placed","On My Way","Shipped");
        builder.setView(view);

        final String localkey = key;
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
             dialogInterface.dismiss();
             item.setStatus(String.valueOf(spinner.getSelectedIndex()));

             requests.child(localkey).setValue(item);
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

        requests.child(key).removeValue();
        Toast.makeText(this, "Item deleted !!", Toast.LENGTH_SHORT).show();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(OrderStatus.this,Home.class));
    }
}
