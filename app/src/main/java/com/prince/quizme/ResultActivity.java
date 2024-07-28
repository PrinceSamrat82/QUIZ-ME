package com.prince.quizme;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prince.quizme.databinding.ActivityResultBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ResultActivity extends AppCompatActivity {

    ActivityResultBinding binding;
    int POINTS =10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        int correctAnswers =getIntent().getIntExtra("correct", 0);
        int totalQuestions = getIntent().getIntExtra("total", 0);

        long points =correctAnswers * POINTS;

        binding.score.setText(String.format("%d/%d", correctAnswers, totalQuestions));
        binding.earnedCoins.setText(String.valueOf(points));

        FirebaseFirestore database = FirebaseFirestore.getInstance();

        database.collection("users")
                .document(FirebaseAuth.getInstance().getUid())
                .update("coins", FieldValue.increment(points));


        binding.restartBtn.setOnClickListener(v -> {
            // Restart the quiz by starting the initial quiz activity
            Intent intent = new Intent(ResultActivity.this, QuizActivity.class);
            startActivity(intent);
        });






        binding.shareBtn.setOnClickListener(v -> {
            Bitmap bitmap = getBitmapFromView(binding.constLayout);
            if (bitmap != null) {
                try {
                    File file = saveBitmap(bitmap);
                    shareImage(file);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to share image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.main.setOnClickListener(view -> startActivity(new Intent(ResultActivity.this, MainActivity.class)));


    }
    private Bitmap getBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private File saveBitmap(Bitmap bitmap) throws IOException {
        File filesDir = getFilesDir();
        File imageFile = new File(filesDir, "result_image.png");

        FileOutputStream fos = new FileOutputStream(imageFile);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.flush();
        fos.close();

        return imageFile;
    }
    private void shareImage(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(this, "com.prince.quizme.fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share Image"));
        } catch (Exception e) {
//            e.printStackTrace();
            Log.e("ResultActivity", "Failed to share image", e);
        }
    }




}