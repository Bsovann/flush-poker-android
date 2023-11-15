package com.example.flush_poker_android.Client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.fragment.app.Fragment;

import com.example.flush_poker_android.Client.customviews.CardAdapter;
import com.example.flush_poker_android.R;

import java.util.ArrayList;
import java.util.List;

public class CardFragment extends Fragment implements GameUpdateListener {

    private CardAdapter cardAdapter;
    private List<Integer> cardImages;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_card,null);

        GridView gridView = view.findViewById(R.id.communityCardGrid);

        // Initialize your list of card images
        cardImages = new ArrayList<>();

        // Create and set the adapter for the GridView
        cardAdapter = new CardAdapter(getActivity(), cardImages);
        gridView.setAdapter(cardAdapter);

        return view;
    }
    @Override
    public void onCardUpdate(List<Integer> updatedCardImages) {
        // Update the card images in your adapter and refresh the GridView
        cardImages.clear();
        cardImages.addAll(updatedCardImages);
        cardAdapter.notifyDataSetChanged();
    }


}
