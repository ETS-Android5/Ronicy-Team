package com.adeasy.advertise.ui.getintouch;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adeasy.advertise.R;

//Creator-A.M.W.W.R.L.Wataketiya
//IT19014128
//ravinduwata@gmail.com

public class MyViewHolder extends RecyclerView.ViewHolder  {

    public TextView TVbugId, TVdescription;
    public View view;
    public MyViewHolder(@NonNull View itemView) {
        super(itemView);

        TVbugId = itemView.findViewById(R.id.TVbugID);
        TVdescription = itemView.findViewById(R.id.TVdescription);
        view = itemView;
    }
}
