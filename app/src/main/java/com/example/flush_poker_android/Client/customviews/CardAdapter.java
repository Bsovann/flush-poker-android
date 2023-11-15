package com.example.flush_poker_android.Client.customviews;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

public class CardAdapter extends BaseAdapter {

    private final Context context;
    private final List<Integer> cardImages;  // List of card image resource IDs

    public CardAdapter(Context context, List<Integer> cardImages) {
        this.context = context;
        this.cardImages = cardImages;
    }
    @Override
    public int getCount() {
        return cardImages.size();
    }

    @Override
    public Object getItem(int position) {
        return cardImages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView cardImageView;
        if (convertView == null) {
            cardImageView = new ImageView(context);
            cardImageView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, 150));  // Adjust height as needed
            cardImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            cardImageView = (ImageView) convertView;
        }

        cardImageView.setImageResource(cardImages.get(position));
        return cardImageView;
    }
}
