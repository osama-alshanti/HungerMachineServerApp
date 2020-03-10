package com.example.osama.androideatitserver.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import com.example.osama.androideatitserver.Common.Common;
import com.example.osama.androideatitserver.Interface.ItemClickListener;
import com.example.osama.androideatitserver.R;
import com.rey.material.widget.Button;

import info.hoang8f.widget.FButton;

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnCreateContextMenuListener,View.OnLongClickListener{

    public TextView txtOrderId,txtOrderStatus,txtOrderPhone,txtOrderAddress;

    public FButton btnEdit,btnRemove,btnDetail,btnDirection;

    private ItemClickListener itemClickListener;


    public OrderViewHolder(View itemView) {
        super(itemView);
        txtOrderId = itemView.findViewById(R.id.order_id);
        txtOrderStatus = itemView.findViewById(R.id.order_status);
        txtOrderPhone = itemView.findViewById(R.id.order_phone);
        txtOrderAddress = itemView.findViewById(R.id.order_address);

        btnEdit = itemView.findViewById(R.id.btnEdit);
        btnRemove = itemView.findViewById(R.id.btnRemove);
        btnDetail = itemView.findViewById(R.id.btnDetail);
        btnDirection = itemView.findViewById(R.id.btnDirection);

        /*itemView.setOnCreateContextMenuListener(this);
        itemView.setOnLongClickListener(this);
        itemView.setOnClickListener(this);*/
    }

    public void setItemClickListener(ItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);

    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle("Select The Action");

        contextMenu.add(0,0,getAdapterPosition(), Common.UPDATE);
        contextMenu.add(0,1,getAdapterPosition(), Common.DELETE);
    }

    @Override
    public boolean onLongClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),true);
        return true;
    }
}
