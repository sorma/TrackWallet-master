package com.jm.trackwallet.adapter;

import static com.jm.trackwallet.util.Constants.colorList;
import static com.jm.trackwallet.util.Constants.iconList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jm.trackwallet.R;
import com.jm.trackwallet.repository.DBCategoriaRepository;
import com.jm.trackwallet.repository.DBContoRepository;
import com.jm.trackwallet.repository.DBTransazioneRepository;
import com.jm.trackwallet.model.Categoria;
import com.jm.trackwallet.model.Transazione;
import com.jm.trackwallet.manager.FirebaseManager;
import com.jm.trackwallet.ui.components.CategoriaDiffCallback;
import com.jm.trackwallet.ui.home.viewmodel.TransazioneViewModel;
import com.jm.trackwallet.ui.home.viewmodel.TransazioneViewModelCategoria;
import com.jm.trackwallet.ui.home.viewmodel.TransazioneViewModelConto;

import java.util.ArrayList;
import java.util.List;

public class AdapterCategoria extends RecyclerView.Adapter<AdapterCategoria.CategoriaViewHolder> {

    private List<Categoria> categorie;
    private Context context;
    private boolean tipo;


    public AdapterCategoria(Context context, List<Categoria> categorie, boolean tipo) {
        this.context = context;
        this.categorie = categorie;
        this.tipo = tipo;
    }

    @NonNull
    @Override
    public CategoriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_categoria, parent, false);
        return new CategoriaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriaViewHolder holder, int position) {
        Categoria categoria = categorie.get(position);
        // Effetto visivo per il click (come se fosse un bottone premuto)


        if ((position == categorie.size() - 1)) { // L'ultimo elemento è il "+" per aggiungere
            holder.nomeCategoriaTextView.setText("");
            holder.iconaCategoriaImageView.setImageResource(R.drawable.aggiungi);  // Icona + (aggiungi)
            int color = ContextCompat.getColor(holder.itemView.getContext(), R.color.md_theme_onSurfaceVariant);

            holder.iconaCategoriaImageView.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            holder.cardView.setStrokeWidth(3);
            holder.cardView.setOnClickListener(v -> {
                holder.cardView.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                holder.cardView.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                openAddCategoryDialog();
            });
        } else {
            holder.nomeCategoriaTextView.setText(categoria.getNome());
            holder.iconaCategoriaImageView.setImageResource(iconList.get(categoria.getIcona()));
            holder.iconaCategoriaImageView.setColorFilter(colorList.get(categoria.getColore()), PorterDuff.Mode.SRC_IN);
            holder.cardView.setStrokeColor(colorList.get(categoria.getColore()));  // Colore del bordo
            holder.cardView.setStrokeWidth(3);
            holder.cardView.setOnClickListener(v -> {
                holder.cardView.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                holder.cardView.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                String idCategoria = categoria.getIdCategoria();
                Bundle bundle = new Bundle();
                bundle.putString("categoriaId", idCategoria);
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_categorieFragment_to_transazioniCategoriaFragment, bundle);
            });
            holder.cardView.setOnLongClickListener(view -> {
                showDeleteConfirmationDialog(categoria);
                return true;
            });
        }
    }

    private void showDeleteConfirmationDialog(Categoria categoria) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(R.string.conferma_cancellazione);
        builder.setMessage(R.string.text_elimina_categoria);

        builder.setPositiveButton(R.string.elimina, (dialog, which) -> {
            deleteCategoria(categoria);

        });
        builder.setNegativeButton(R.string.annulla, (dialog, which) -> {
            dialog.dismiss();
        });

        builder.show();
    }

    private void deleteCategoria(Categoria categoria) {

        List<Categoria> nuovaLista = new ArrayList<>(categorie);
        nuovaLista.remove(categoria); // Rimuove la categoria dalla nuova lista

        CategoriaDiffCallback diffCallback = new CategoriaDiffCallback(categorie, nuovaLista);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        categorie.clear();
        categorie.addAll(nuovaLista);
        diffResult.dispatchUpdatesTo(this); // Aggiorna l'adapter in modo ottimizzato

        DBTransazioneRepository dbTransazioneRepository=new DBTransazioneRepository();
        DBContoRepository dbContoRepository=new DBContoRepository();
        DBCategoriaRepository dbCategoriaRepository = new DBCategoriaRepository();

        dbTransazioneRepository.getTransazioniByCategoria(FirebaseManager.getInstance().getCurrentUser().getUid(), categoria.getIdCategoria(), new DBTransazioneRepository.TransazioniCallback() {
            @Override
            public void onSuccess(List<Transazione> transazioni) {
                // Gestisci la lista di transazioni qui
                for (Transazione transazione : transazioni) {
                    TransazioneViewModel transazioneViewModel = new ViewModelProvider((FragmentActivity) context).get(TransazioneViewModel.class);
                    transazioneViewModel.deleteTransazione(transazione);
                    TransazioneViewModelCategoria transazioneViewModelCategoria = new ViewModelProvider((FragmentActivity) context).get(TransazioneViewModelCategoria.class);
                    transazioneViewModelCategoria.deleteTransazione(transazione);
                    TransazioneViewModelConto transazioneViewModelConto = new ViewModelProvider((FragmentActivity) context).get(TransazioneViewModelConto.class);
                    transazioneViewModelConto.deleteTransazione(transazione);
                    double importoDaAggiungere = -transazione.getImporto();
                    dbContoRepository.updateSaldo(transazione.getIdConto(), importoDaAggiungere); // Metodo per aggiornare il saldo
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Gestisci l'errore qui
                Log.e("TransazioniError", "Errore nel recupero delle transazioni", e);
            }
        });
        // Osserva le transazioni dalla repository

        dbTransazioneRepository.deleteTransazioniByCategoria(categoria.getIdCategoria());
        dbCategoriaRepository.deleteCategoria(categoria.getIdCategoria());
    }

    @Override
    public int getItemCount() {
        return categorie.size();
    }

    public static class CategoriaViewHolder extends RecyclerView.ViewHolder {
        TextView nomeCategoriaTextView;
        ImageView iconaCategoriaImageView;
        MaterialCardView cardView;

        public CategoriaViewHolder(View itemView) {
            super(itemView);
            nomeCategoriaTextView = itemView.findViewById(R.id.nomeCategoriaTextView);
            iconaCategoriaImageView = itemView.findViewById(R.id.iconaCategoriaImageView);
            cardView = itemView.findViewById(R.id.card_categoria);
        }
    }

    private void openAddCategoryDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        SpannableString title = new SpannableString(context.getString(R.string.aggiungi_categoria));
        title.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(), 0);
        builder.setTitle(title);

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_aggiungi_categoria, null);
        builder.setView(dialogView);

        RecyclerView iconRecyclerView = dialogView.findViewById(R.id.recyclerViewIcone);
        iconRecyclerView.setLayoutManager(new GridLayoutManager(context, 3, GridLayoutManager.HORIZONTAL, false));
        IconAdapter iconAdapter = new IconAdapter(iconList, context);
        iconRecyclerView.setAdapter(iconAdapter);

        RecyclerView colorRecyclerView = dialogView.findViewById(R.id.recyclerViewColori);
        colorRecyclerView.setLayoutManager(new GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false));
        ColorAdapter colorAdapter = new ColorAdapter(colorList, context);
        colorRecyclerView.setAdapter(colorAdapter);

        AlertDialog dialog = builder.create();
        dialog.show();

        Button addButton = dialogView.findViewById(R.id.buttonAggiungi);
        Button cancelButton = dialogView.findViewById(R.id.buttonAnnulla);

        addButton.setOnClickListener(v -> {
            int selectedIcon = iconAdapter.getSelectedIcon();
            int selectedColor = colorAdapter.getSelectedColor();
            EditText nomeCategoriaEditText = dialogView.findViewById(R.id.editTextNomeCategoria);
            String nomeCategoria = nomeCategoriaEditText.getText().toString();

            if (nomeCategoria.isEmpty() || selectedIcon == -1 || selectedColor == -1) {
                Toast.makeText(context, R.string.inserire_tutti_i_campi, Toast.LENGTH_SHORT).show();
            } else {
                Categoria nuovaCategoria = new Categoria("", nomeCategoria, selectedIcon, selectedColor, tipo,
                        FirebaseManager.getInstance().getCurrentUser().getUid());
                DBCategoriaRepository dbCategoriaRepository = new DBCategoriaRepository();
                String idCategoria=dbCategoriaRepository.getNewId();
                nuovaCategoria.setIdCategoria(idCategoria); // Imposta l'ID sulla categoria

                // Aggiungi la nuova categoria alla lista (senza il pulsante "aggiungi")
                List<Categoria> nuovaLista = new ArrayList<>(categorie);
                nuovaLista.remove(nuovaLista.size() - 1); // Rimuove il pulsante "aggiungi"
                nuovaLista.add(nuovaCategoria);
                nuovaLista.add(new Categoria("", "aggiungi", R.drawable.aggiungi, Color.parseColor("#c4c6d0"), tipo, "")); // Riaggiungi il pulsante

                // Calcola le differenze
                CategoriaDiffCallback diffCallback = new CategoriaDiffCallback(categorie, nuovaLista);
                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

                // Sostituisci la lista dell'adapter
                categorie.clear();
                categorie.addAll(nuovaLista);

                // Notifica l'adapter che i dati sono cambiati
                diffResult.dispatchUpdatesTo(this);

                // Salva la categoria nel database con l'ID generato
                dbCategoriaRepository.addCategoria(nuovaCategoria);
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
    }

}




