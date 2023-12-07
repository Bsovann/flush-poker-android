/**
 * Author: Bondith Sovann
 * Description: This class, CardAdapter, is a custom adapter used to populate a GridView with card images.
 * It extends the BaseAdapter class and provides the necessary methods to display card images in the GridView.
 */
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

    /**
     * Author: Bondith Sovann
     * Description: Constructor for the CardAdapter class.
     * Initializes the adapter with a context and a list of card image resource IDs.
     * @param context The context in which the adapter is used.
     * @param cardImages A list of card image resource IDs to be displayed.
     */
    public CardAdapter(Context context, List<Integer> cardImages) {
        this.context = context;
        this.cardImages = cardImages;
    }

    /**
     * Author: Bondith Sovann
     * Description: Returns the number of items in the adapter.
     * @return The number of card images in the list.
     */
    @Override
    public int getCount() {
        return cardImages.size();
    }

    /**
     * Author: Bondith Sovann
     * Description: Returns the item at a specific position in the adapter.
     * @param position The position of the item to retrieve.
     * @return The card image resource ID at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return cardImages.get(position);
    }

    /**
     * Author: Bondith Sovann
     * Description: Returns the unique ID for an item at a specific position in the adapter.
     * @param position The position of the item.
     * @return The ID of the item, which is the same as the position in this case.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Author: Bondith Sovann
     * Description: Generates and returns a View for an item in the adapter at a specific position.
     * This method is responsible for creating or reusing ImageView instances to display card images.
     * @param position The position of the item to display.
     * @param convertView The recycled View that can be reused, if available.
     * @param parent The parent ViewGroup to which the View will be attached.
     * @return A View displaying the card image at the specified position.
     */
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
