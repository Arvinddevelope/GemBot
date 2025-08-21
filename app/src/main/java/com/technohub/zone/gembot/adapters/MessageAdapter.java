package com.technohub.zone.gembot.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.technohub.zone.gembot.R;
import com.technohub.zone.gembot.models.MessageModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<MessageModel> messageList;
    private Context context;

    public MessageAdapter(List<MessageModel> messageList, Context context) {
        this.messageList = messageList;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageModel message = messageList.get(position);

        holder.messageText.setText(message.getMessage());

        // Format timestamp
        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(new Date(message.getTimestamp()));
        holder.timestampText.setText(time);

        // Differentiate user vs bot
        if ("user".equals(message.getSender())) {
            holder.messageText.setBackgroundResource(R.drawable.bg_user_message);
        } else {
            holder.messageText.setBackgroundResource(R.drawable.bg_bot_message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timestampText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timestampText = itemView.findViewById(R.id.timestampText);
        }
    }
}
