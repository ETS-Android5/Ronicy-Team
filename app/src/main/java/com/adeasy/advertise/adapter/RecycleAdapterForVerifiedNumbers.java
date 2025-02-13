package com.adeasy.advertise.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adeasy.advertise.R;
import com.adeasy.advertise.helper.ViewHolderPhoneNumbers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manuka yasas,
 * University Sliit
 * Email manukayasas99@gmail.com
 **/
public class RecycleAdapterForVerifiedNumbers extends RecyclerView.Adapter<ViewHolderPhoneNumbers> {

    private List<Integer> verifiedNumberList;

    public RecycleAdapterForVerifiedNumbers(List<Integer> verifiedNumberList) {
        if (verifiedNumberList != null)
            this.verifiedNumberList = verifiedNumberList;
        else
            this.verifiedNumberList = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolderPhoneNumbers onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.manuka_phone_numbers, parent, false);
        return new ViewHolderPhoneNumbers(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolderPhoneNumbers holder, final int position) {
        holder.numberView.setText(verifiedNumberList.get(position).toString());
        holder.removeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifiedNumberList.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return verifiedNumberList.size();
    }

    public List<Integer> getSelectedNumbers() {
        return verifiedNumberList;
    }

}
