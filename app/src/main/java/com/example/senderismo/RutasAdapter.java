package com.example.senderismo;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RutasAdapter extends RecyclerView.Adapter<RutasAdapter.RutaViewHolder> {

    private List<Ruta> listaDeRutas;
    private OnRutaInteractionListener listener;

    public interface OnRutaInteractionListener {
        void onRutaClick(Ruta ruta);
        void onFavoritoClick(Ruta ruta, int position);
        void onEliminarClick(Ruta ruta, int position);
    }

    public RutasAdapter(List<Ruta> listaDeRutas, OnRutaInteractionListener listener) {
        this.listaDeRutas = listaDeRutas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RutaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ruta_guardada, parent, false);
        return new RutaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RutaViewHolder holder, int position) {
        Ruta rutaActual = listaDeRutas.get(position);

        holder.nombreTextView.setText(rutaActual.getNombreRuta());
        holder.dificultadTextView.setText("Dificultad: " + rutaActual.getDificultad());

        if (rutaActual.getTipoDeRuta() != null && !rutaActual.getTipoDeRuta().isEmpty()) {
            holder.tipoDeRutaTextView.setText("Tipo: " + rutaActual.getTipoDeRuta());
            holder.tipoDeRutaTextView.setVisibility(View.VISIBLE);
        } else {
            holder.tipoDeRutaTextView.setVisibility(View.GONE);
        }

        if (rutaActual.isFavorita()) {
            holder.iconoFavorito.setImageResource(R.drawable.ic_star);
            holder.layoutPrincipal.setBackgroundColor(Color.parseColor("#FFF8E1"));
        } else {
            holder.iconoFavorito.setImageResource(R.drawable.ic_star_border);
            holder.layoutPrincipal.setBackgroundColor(Color.parseColor("#F0F0F0"));
        }

        holder.itemView.setOnClickListener(v -> listener.onRutaClick(rutaActual));
        holder.iconoFavorito.setOnClickListener(v -> listener.onFavoritoClick(rutaActual, position));
        holder.iconoEliminar.setOnClickListener(v -> listener.onEliminarClick(rutaActual, position));
    }

    @Override
    public int getItemCount() {
        return listaDeRutas.size();
    }

    public void setRutas(List<Ruta> nuevasRutas) {
        this.listaDeRutas = nuevasRutas;
        notifyDataSetChanged();
    }

    public static class RutaViewHolder extends RecyclerView.ViewHolder {
        TextView nombreTextView, dificultadTextView, tipoDeRutaTextView;
        ImageView iconoFavorito, iconoEliminar;
        RelativeLayout layoutPrincipal;

        public RutaViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreTextView = itemView.findViewById(R.id.textViewNombreRuta);
            dificultadTextView = itemView.findViewById(R.id.textViewDificultad);
            tipoDeRutaTextView = itemView.findViewById(R.id.textViewTipoDeRuta);
            iconoFavorito = itemView.findViewById(R.id.icono_favorito);
            iconoEliminar = itemView.findViewById(R.id.icono_eliminar);
            layoutPrincipal = itemView.findViewById(R.id.ruta_item_layout);
        }
    }
}
