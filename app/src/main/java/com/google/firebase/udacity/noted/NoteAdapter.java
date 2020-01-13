package com.google.firebase.udacity.noted;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteHolder> {

    private List<Note> notes = new ArrayList<>();
    private OnNoteCardClickListener listener;

    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    public Note getNotePosition(int position) {
        return notes.get(position);
    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.note, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        Note note = notes.get(position);
        holder.titleTv.setText(note.getTitle());
        holder.noteTv.setText(note.getNote());
        holder.timeTv.setText(String.valueOf(note.getTimeStamp()));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public class NoteHolder extends RecyclerView.ViewHolder {

        private TextView titleTv;
        private TextView noteTv;
        private TextView timeTv;

        public NoteHolder(@NonNull View itemView) {
            super(itemView);

            titleTv = itemView.findViewById(R.id.act_main_tv_title);
            noteTv = itemView.findViewById(R.id.act_main_tv_note);
            timeTv = itemView.findViewById(R.id.act_main_tv_time);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onNoteCardClick(notes.get(position));
                    }
                }
            });
        }
    }

    public interface OnNoteCardClickListener {
        void onNoteCardClick(Note note);
    }

    public void setOnItemClickListener(OnNoteCardClickListener listener) {
        this.listener = listener;
    }
}