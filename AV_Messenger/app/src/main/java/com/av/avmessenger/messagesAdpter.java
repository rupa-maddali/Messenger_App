package com.av.avmessenger;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class messagesAdpter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final List<msgModelclass> messages;
    private final String senderImg, receiverImg;
    private static final int TYPE_SEND = 1;
    private static final int TYPE_RECEIVE = 2;

    public messagesAdpter(Context ctx,
                          List<msgModelclass> msgs,
                          String sendImg,
                          String recvImg) {
        this.context     = ctx;
        this.messages    = msgs;
        this.senderImg   = sendImg;
        this.receiverImg = recvImg;
    }

    @Override
    public int getItemViewType(int pos) {
        String me = FirebaseAuth.getInstance()
                .getCurrentUser().getUid();
        return messages.get(pos)
                .getSenderid()
                .equals(me)
                ? TYPE_SEND
                : TYPE_RECEIVE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        LayoutInflater i = LayoutInflater.from(context);
        if (viewType == TYPE_SEND) {
            return new SendVH(
                    i.inflate(R.layout.sender_layout, parent, false));
        } else {
            return new RecvVH(
                    i.inflate(R.layout.reciver_layout, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder vh, int pos) {
        msgModelclass m = messages.get(pos);
        if (vh instanceof SendVH) {
            SendVH s = (SendVH) vh;
            s.text.setText(m.getMessage());
            Picasso.get().load(senderImg).into(s.avatar);
        } else {
            RecvVH r = (RecvVH) vh;
            r.text.setText(m.getMessage());
            Picasso.get().load(receiverImg).into(r.avatar);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SendVH extends RecyclerView.ViewHolder {
        CircleImageView avatar;
        TextView        text;
        SendVH(@NonNull View v) {
            super(v);
            avatar = v.findViewById(R.id.profilerggg);
            text   = v.findViewById(R.id.msgsendertyp);
        }
    }

    static class RecvVH extends RecyclerView.ViewHolder {
        CircleImageView avatar;
        TextView        text;
        RecvVH(@NonNull View v) {
            super(v);
            avatar = v.findViewById(R.id.pro);
            text   = v.findViewById(R.id.recivertextset);
        }
    }
}
