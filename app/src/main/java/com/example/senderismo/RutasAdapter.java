package com.example.senderismo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RutasAdapter extends RecyclerView.Adapter<RutasAdapter.RutaViewHolder> {

    private List<Ruta> listaDeRutas;

    public RutasAdapter(List<Ruta> listaDeRutas) {
        this.listaDeRutas = listaDeRutas;
    }

    @NonNull
    @Override
    public RutaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ruta_guardada, parent, false);
        return new RutaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RutaViewHolder holder, int position) {
        Ruta ruta = listaDeRutas.get(position);
        holder.nombreTextView.setText(ruta.getNombre());
        holder.dificultadTextView.setText("Dificultad: " + ruta.getDificultad());
        holder.descripcionTextView.setText(ruta.getDescripcion());
        holder.imagenImageView.setImageResource(ruta.getImagenResId());
    }

    @Override
    public int getItemCount() {
        return listaDeRutas.size();
    }

   public static class RutaViewHolder extends RecyclerView.ViewHolder {
        public TextView nombreTextView;
        public TextView dificultadTextView;
        public TextView descripcionTextView;
        public ImageView imagenImageView;
       public TextView tvNombre;
       public TextView tvDescripcion;
       public TextView tvDificultad;

        public RutaViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreTextView = itemView.findViewById(R.id.textViewNombreRuta);
            dificultadTextView = itemView.findViewById(R.id.textViewDificultad);
            descripcionTextView = itemView.findViewById(R.id.textViewDescripcion);
            imagenImageView = itemView.findViewById(R.id.imageViewRuta);
            tvNombre = itemView.findViewById(R.id.tvNombreRutaItem);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionRutaItem);
            tvDificultad = itemView.findViewById(R.id.tvDificultadRutaItem);
        }
    }
}
