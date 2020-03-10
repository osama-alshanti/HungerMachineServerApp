package com.example.osama.androideatitserver.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.osama.androideatitserver.Common.Common;
import com.example.osama.androideatitserver.Interface.ItemClickListener;
import com.example.osama.androideatitserver.R;

public class OffersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnCreateContextMenuListener{

    public TextView tv_min_buy,tv_delivery_price,tv_title,tv_type;
    public ImageView imageOffers;

    private ItemClickListener itemClickListener;

    public OffersViewHolder(View itemView) {
        super(itemView);

        tv_min_buy = itemView.findViewById(R.id.tv_min_buy);
        tv_delivery_price = itemView.findViewById(R.id.tv_delivery_price);
        tv_title = itemView.findViewById(R.id.tv_title);
        tv_type = itemView.findViewById(R.id.tv_type);

        imageOffers = itemView.findViewById(R.id.offers_image);

        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);

    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle("Select The Action");

        contextMenu.add(0,0,getAdapterPosition(), Common.UPDATE);
        contextMenu.add(0,1,getAdapterPosition(), Common.DELETE);
    }
}
