package com.example.flush_poker_android.Client;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.flush_poker_android.Client.customviews.CardAdapter;
import com.example.flush_poker_android.R;

import java.util.ArrayList;
import java.util.List;

public class CardFragment extends Fragment implements GameUpdateListener {

    private CardAdapter cardAdapter;
    private List<Integer> cardImages;
    private GameUpdateListener listener;
    private View view;
    private GridView gridView;

    public CardFragment(){
        super(R.layout.fragment_card);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_card, container, false);

        // Initialize your list of card images
        cardImages = new ArrayList<>();

        gridView = view.findViewById(R.id.communityCards);
        // Create and set the adapter for the GridView

        cardAdapter = new CardAdapter(getActivity(), cardImages);
        gridView.setAdapter(cardAdapter);

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the state of your fragment's data into the bundle
        outState.putIntegerArrayList("cardImages", new ArrayList<>(cardImages));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Restore the saved state if available
        if (savedInstanceState != null) {
            ArrayList<Integer> savedCardImages = savedInstanceState.getIntegerArrayList("cardImages");
            if (savedCardImages != null) {
                // Restore the card images
                cardImages.clear();
                cardImages.addAll(savedCardImages);
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        this.cardAdapter.notifyDataSetChanged();
    }


    @Override
    public void onCardUpdate(List<Integer> updatedCardImages) {
        // Update the card images in your adapter and refresh the GridView
        if (cardImages != null && cardAdapter != null && gridView != null) {
            cardImages.clear();
            cardImages.addAll(updatedCardImages);
            cardAdapter.notifyDataSetChanged();
        } else {
            // Handle the case where one of the references is null
            Log.e("CardFragment", "One of the references (cardImages, cardAdapter, gridView) is null");
        }
    }

}
