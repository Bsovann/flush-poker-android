package com.example.flush_poker_android.Client.customviews;

import android.content.Context;
import android.util.DisplayMetrics;
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

        // Retrieve screen dimensions or density
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        // Calculate desired size for card images (example: 1/4 of screen width)
        int imageSize = (int) (dpWidth / 20 * displayMetrics.density);

        if (convertView == null) {
            cardImageView = new ImageView(context);
            cardImageView.setLayoutParams(new GridView.LayoutParams(imageSize, imageSize)); // Set both width and height
            cardImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            cardImageView = (ImageView) convertView;
        }

        cardImageView.setImageResource(cardImages.get(position));
        return cardImageView;
    }
}
