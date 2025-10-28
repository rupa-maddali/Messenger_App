package com.av.avmessenger;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

class UserAdapter
        extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private final Context ctx;
    private final List<Users> users;

    public UserAdapter(Context ctx, List<Users> users) {
        this.ctx   = ctx;
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx)
                .inflate(R.layout.user_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder h, int pos) {
        Users u = users.get(pos);
        h.name.setText(u.getUserName());
        h.status.setText(u.getStatus());
        Picasso.get()
                .load(u.getProfilepic())
                .into(h.avatar);

        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(ctx, chatwindo.class);
            // these keys must match chatwindo.getIntent().getStringExtra(...)
            i.putExtra("receiverName", u.getUserName());
            i.putExtra("receiverImg",  u.getProfilepic());
            i.putExtra("receiverUid",  u.getUserId());
            ctx.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView avatar;
        TextView        name, status;
        ViewHolder(@NonNull View v) {
            super(v);
            avatar = v.findViewById(R.id.userimg);
            name   = v.findViewById(R.id.username);
            status = v.findViewById(R.id.userstatus);
        }
    }
}
