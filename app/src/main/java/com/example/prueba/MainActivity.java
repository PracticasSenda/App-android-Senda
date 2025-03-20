package com.example.prueba;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.database.Cursor;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar WebView
        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);

        CookieManager.getInstance().setAcceptCookie(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, android.webkit.WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.contains(".pdf") || url.contains(".docx")) {
                    descargarArchivo(url);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

        webView.loadUrl("https://www.sendagestion.com/campusvirtual");
    }

    private void descargarArchivo(String url) {
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        String cleanedFileName = limpiarNombreArchivo(fileName);  // Limpiar el nombre del archivo

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), cleanedFileName);

        // Si el archivo ya existe, no lo descargues nuevamente, solo ábrelo
        if (file.exists()) {
            Log.d("Download Path", "El archivo ya existe. Abriendo...");
            abrirArchivoDescargado(cleanedFileName);
            return;
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(fileName);
        request.setDescription("Descarga en progreso...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, cleanedFileName);

        // Obtener cookies de la sesión activa en WebView
        String cookies = CookieManager.getInstance().getCookie(url);
        if (cookies != null) {
            request.addRequestHeader("Cookie", cookies);
        }
        request.addRequestHeader("User-Agent", System.getProperty("http.agent"));

        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = downloadManager.enqueue(request);

        new Thread(() -> {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            boolean downloading = true;
            while (downloading) {
                Cursor cursor = downloadManager.query(query);
                if (cursor != null && cursor.moveToFirst()) {
                    int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (statusIndex != -1) {
                        int status = cursor.getInt(statusIndex);
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            downloading = false;
                            abrirArchivoDescargado(cleanedFileName);
                        }
                    }
                }
                if (cursor != null) cursor.close();
            }
        }).start();
    }

    private String limpiarNombreArchivo(String fileName) {
        // Reemplazar espacios por guiones bajos
        fileName = fileName.replaceAll(" ", "_");

        // Reemplazar otros caracteres especiales por sus equivalentes seguros
        fileName = fileName.replaceAll("[^a-zA-Z0-9\\._-]", "_");

        // Si lo deseas, puedes codificar el nombre de archivo para manejar caracteres no válidos.
        // fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());

        return fileName;
    }

    private void abrirArchivoDescargado(String fileName) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        Log.d("Download Path", "File path: " + file.getAbsolutePath());

        Uri fileUri = FileProvider.getUriForFile(this, "com.example.prueba.provider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);

        // Verificar la extensión del archivo y establecer el tipo MIME adecuado
        String fileExtension = getFileExtension(fileName);
        if (fileExtension != null && fileExtension.equalsIgnoreCase("docx")) {
            intent.setDataAndType(fileUri, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        } else if (fileExtension != null && fileExtension.equalsIgnoreCase("pdf")) {
            intent.setDataAndType(fileUri, "application/pdf");
        } else {
            intent.setDataAndType(fileUri, "*/*");  // Para cualquier otro tipo de archivo
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, "Abrir con"));
    }

    // Método para obtener la extensión del archivo
    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.lastIndexOf('.') > 0) {
            return fileName.substring(fileName.lastIndexOf('.') + 1);
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

}
