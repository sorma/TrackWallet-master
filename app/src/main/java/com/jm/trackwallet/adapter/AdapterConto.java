package com.jm.trackwallet.adapter;

import static com.jm.trackwallet.util.Constants.iconListConto;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jm.trackwallet.R;
import com.jm.trackwallet.repository.DBContoRepository;
import com.jm.trackwallet.repository.DBTransazioneRepository;
import com.jm.trackwallet.model.Conto;
import com.jm.trackwallet.manager.FirebaseManager;
import com.jm.trackwallet.manager.PreferencesManager;
import com.jm.trackwallet.ui.home.MainActivity;
import com.jm.trackwallet.ui.components.ContoDiffCallback;

import java.util.ArrayList;
import java.util.List;

public class AdapterConto extends RecyclerView.Adapter<AdapterConto.ContoViewHolder> {

    private List<Conto> conti;
    private Context context;

    public AdapterConto(Context context, List<Conto> conti) {
        this.context = context;
        this.conti = conti;
    }

    @NonNull
    @Override
    public ContoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conto, parent, false);
        return new ContoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContoViewHolder holder, int position) {
        Conto conto = conti.get(position);
        int color = ContextCompat.getColor(holder.itemView.getContext(), R.color.md_theme_onSurfaceVariant);

        if ((position == conti.size() - 1)) { // Ultimo elemento per aggiungere un conto
            holder.nomeContoTextView.setVisibility(View.GONE);
            holder.saldoTextView.setVisibility(View.GONE);
            holder.icona.setImageResource(R.drawable.aggiungi);  // Icona + (aggiungi)

            holder.icona.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            holder.cardView.setStrokeWidth(3);
            ViewGroup.LayoutParams params = holder.cardView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;  // Imposta come wrap_content
            holder.cardView.setLayoutParams(params);
            holder.cardView.setOnClickListener(v -> {
                holder.cardView.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                holder.cardView.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                openAddContoDialog();
            });
        } else {
            holder.nomeContoTextView.setText(conto.getNome());
            PreferencesManager pm = new PreferencesManager(context);
            double convertedPrice = conto.getSaldo();
            String valuta = String.format("%.2f", convertedPrice) + "€";
            if (!pm.getCurrency().equals("€") && MainActivity.isOnline(context)) {
                convertedPrice *= pm.getExchangeRate();
                valuta = String.format("%.2f", convertedPrice) + pm.getCurrency();
            }

            holder.saldoTextView.setText(valuta); // Mostra il saldo del conto
            holder.icona.setImageResource(iconListConto.get(conto.getIcona()));

            holder.icona.setColorFilter(color, PorterDuff.Mode.SRC_IN);

            if (conto.getSaldo() > 0) {
                holder.cardView.setStrokeColor(Color.parseColor("#2E7D32"));  // Colore del bordo
                holder.cardView.setStrokeWidth(3);
                holder.saldoTextView.setTextColor(Color.parseColor("#2E7D32"));

            } else if (conto.getSaldo() < 0) {
                holder.cardView.setStrokeColor(Color.parseColor("#D32F2F"));  // Colore del bordo
                holder.cardView.setStrokeWidth(3);
                holder.saldoTextView.setTextColor(Color.parseColor("#D32F2F"));

            } else {
                holder.cardView.setStrokeColor(Color.parseColor("#c4c6d0"));  // Colore del bordo
                holder.cardView.setStrokeWidth(3);
            }


            holder.cardView.setOnClickListener(v -> {
                holder.cardView.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                holder.cardView.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                Bundle bundle = new Bundle();
                bundle.putString("contoId", conto.getIdConto());
                // Naviga al dettaglio del conto
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_categorieFragment_to_transazioniContoFragment, bundle);
            });
            holder.cardView.setOnLongClickListener(view -> {
                showDeleteConfirmationDialog(conto);
                return true;
            });
        }
    }

    private void openAddContoDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        SpannableString title = new SpannableString(context.getString(R.string.aggiungi_conto));
        title.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(), 0);
        builder.setTitle(title);

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_aggiungi_conto, null);
        builder.setView(dialogView);

        RecyclerView iconRecyclerView = dialogView.findViewById(R.id.recyclerViewIcone);
        iconRecyclerView.setLayoutManager(new GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false));

        IconAdapter iconAdapter = new IconAdapter(iconListConto, context);
        iconRecyclerView.setAdapter(iconAdapter);


        AlertDialog dialog = builder.create();
        dialog.show();

        Button addButton = dialogView.findViewById(R.id.buttonAggiungi);
        Button cancelButton = dialogView.findViewById(R.id.buttonAnnulla);

        EditText nomeContoE = dialogView.findViewById(R.id.editTextNomeConto);
        EditText saldoContoE = dialogView.findViewById(R.id.editTextSaldoConto);
        PreferencesManager pm = new PreferencesManager(context);
        if (pm.getCurrency().equals("€"))
            saldoContoE.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.euro, 0);
        else if (pm.getCurrency().equals("$")) {
            saldoContoE.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.dollar, 0);
        } else
            saldoContoE.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.pound, 0);

        addButton.setOnClickListener(v -> {
            int selectedIcon = iconAdapter.getSelectedIcon();

            String nomeConto = nomeContoE.getText().toString();
            String saldoConto = saldoContoE.getText().toString();

            if (nomeConto.isEmpty() || selectedIcon == -1 || saldoConto.isEmpty()) {
                Toast.makeText(context, R.string.inserire_tutti_i_campi, Toast.LENGTH_SHORT).show();
            } else {
                DBContoRepository dbContoRepository = new DBContoRepository();

                double convertedPrice = Double.parseDouble(saldoConto);
                if (!pm.getCurrency().equals("€") && MainActivity.isOnline(context)) {
                    convertedPrice *= pm.getExchangeRate();
                }

                Conto nuovoConto = new Conto("", nomeConto, selectedIcon, FirebaseManager.getInstance().getCurrentUser().getUid(), convertedPrice);

                String idConto=dbContoRepository.getNewId();
                nuovoConto.setIdConto(idConto);
                List<Conto> nuovaLista = new ArrayList<>(conti);
                nuovaLista.remove(nuovaLista.size() - 1); // Rimuove il pulsante "aggiungi"
                nuovaLista.add(nuovoConto);
                nuovaLista.add(new Conto("", "aggiungi", R.drawable.aggiungi, "", 0.0)); // Riaggiunge il pulsante

                ContoDiffCallback diffCallback = new ContoDiffCallback(conti, nuovaLista);
                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

                conti.clear();
                conti.addAll(nuovaLista);
                diffResult.dispatchUpdatesTo(this);

                dbContoRepository.addConto(nuovoConto);

                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
    }

    private void showDeleteConfirmationDialog(Conto conto) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(R.string.conferma_cancellazione);
        builder.setMessage(R.string.text_sei_sicuro_eliminazione_conto);

        builder.setPositiveButton(R.string.elimina, (dialog, which) -> {
            deleteConto(conto);
        });
        builder.setNegativeButton(R.string.annulla, (dialog, which) -> {
            dialog.dismiss();
        });

        builder.show();
    }

    private void deleteConto(Conto conto) {
        List<Conto> nuovaLista = new ArrayList<>(conti);
        nuovaLista.remove(conto); // Rimuove il conto dalla nuova lista

        ContoDiffCallback diffCallback = new ContoDiffCallback(conti, nuovaLista);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        conti.clear();
        conti.addAll(nuovaLista);
        diffResult.dispatchUpdatesTo(this);

        DBTransazioneRepository dbTransazioneRepository = new DBTransazioneRepository();
        dbTransazioneRepository.deleteTransazioniByConto(conto.getIdConto());

        DBContoRepository dbContoRepository = new DBContoRepository();
        dbContoRepository.deleteConto(conto.getIdConto());
    }

    @Override
    public int getItemCount() {
        return conti.size();
    }

    public static class ContoViewHolder extends RecyclerView.ViewHolder {
        TextView nomeContoTextView, saldoTextView;
        MaterialCardView cardView;
        ImageView icona;

        public ContoViewHolder(View itemView) {
            super(itemView);
            icona = itemView.findViewById(R.id.iconaContoImageView);
            nomeContoTextView = itemView.findViewById(R.id.nomeContoTextView);
            saldoTextView = itemView.findViewById(R.id.saldoContoTextView);
            cardView = itemView.findViewById(R.id.card_conto);
        }
    }


}
