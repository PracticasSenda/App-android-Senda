package com.example.prueba;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresExtension;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.pdf.viewer.fragment.PdfViewerFragment;
import androidx.pdf.viewer.fragment.PdfStylingOptions;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class PdfViewerActivity extends AppCompatActivity {

    public static final String PDF_URL_KEY = "PDF_URL";

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 13)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        String pdfUrl = getIntent().getStringExtra(PDF_URL_KEY);
        URL urlpdf = null;
        URI uripdf = null;
        try {
            urlpdf = new URL(pdfUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        try {
            uripdf = urlpdf.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        if (pdfUrl != null) {
            // Configurar opciones de estilo para el visor de PDF con un estilo válido
            int styleResId = R.style.Base_Theme_Prueba; // Asegúrate de definirlo en styles.xml
            PdfStylingOptions stylingOptions = new PdfStylingOptions(styleResId);

            // Crear una instancia de PdfViewerFragment con las opciones
            PdfViewerFragment pdfFragment = PdfViewerFragment.newInstance(stylingOptions);
            //pdfFragment.setDocumentUri(uripdf);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.pdf_container, pdfFragment);
            transaction.commit();
        }
    }
}