package com.jm.trackwallet.util;

import static android.provider.Settings.System.getString;

import android.graphics.Color;

import com.jm.trackwallet.R;

import java.util.Arrays;
import java.util.List;

public class Constants {
    public static List<Integer> iconListConto = Arrays.asList(
            R.drawable.banca,
            R.drawable.cash,
            R.drawable.savings,
            R.drawable.credit_card,
            R.drawable.lavoro,
            R.drawable.bank
    );
    public static List<Integer> iconList = Arrays.asList(
            R.drawable.home,
            R.drawable.family,
            R.drawable.cart,
            R.drawable.nights,
            R.drawable.restaurant,
            R.drawable.smoking,
            R.drawable.clothes,
            R.drawable.sports,
            R.drawable.palla,
            R.drawable.car,
            R.drawable.taxi,
            R.drawable.train,
            R.drawable.call,
            R.drawable.regalo,
            R.drawable.mondo,
            R.drawable.animali,
            R.drawable.forbici,
            R.drawable.medical


            );
    public static List<Integer> colorList = Arrays.asList(
            Color.parseColor("#D32F2F"), // Rosso acceso
            Color.parseColor("#9A0007"), // Rosso scuro

            Color.parseColor("#F57C00"), // Rosso aranciato
            Color.parseColor("#BB4D00"), // Rosso aranciato scuro

            Color.parseColor("#FFA726"), // Arancione acceso
            Color.parseColor("#C77600"), // Arancione scuro

            Color.parseColor("#FDD835"), // Giallo oro
            Color.parseColor("#C7A600"), // Giallo oro scuro

            Color.parseColor("#FBC02D"), // Giallo acceso
            Color.parseColor("#B28F00"), // Giallo scuro

            Color.parseColor("#9CCC65"), // Verde lime
            Color.parseColor("#6B8E23"), // Verde lime scuro

            Color.parseColor("#4CAF50"), // Verde acceso
            Color.parseColor("#2E7D32"), // Verde scuro

            Color.parseColor("#26A69A"), // Verde acqua
            Color.parseColor("#008B5F"), // Verde acqua scuro

            Color.parseColor("#29B6F6"), // Azzurro
            Color.parseColor("#0288D1"), // Azzurro scuro

            Color.parseColor("#1E88E5"), // Blu cielo
            Color.parseColor("#1565C0"), // Blu cielo scuro

            Color.parseColor("#3949AB"), // Indaco
            Color.parseColor("#283593"), // Indaco scuro

            Color.parseColor("#8E24AA"), // Viola
            Color.parseColor("#6A1B9A")  // Viola scuro


    );

}
