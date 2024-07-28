package com.prince.quizme;

import android.content.Intent;
import android.net.http.QuicOptions;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.prince.quizme.databinding.ActivityQuizBinding;

import java.util.ArrayList;
import java.util.Random;

public class QuizActivity extends AppCompatActivity {

    ActivityQuizBinding binding;
    ArrayList<Question> questions;
    Question question;
    int index =0;
    CountDownTimer timer;
    FirebaseFirestore database;
    int correctAnswers = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        questions =new ArrayList<>();

        database=FirebaseFirestore.getInstance();
        String catId =getIntent().getStringExtra("catId");
        Random random =new Random();
        int rand = random.nextInt(10);



        database.collection("categories")
                        .document(catId)
                        .collection("questions")
                        .whereGreaterThanOrEqualTo("index",rand)
                        .orderBy("index")
                        .limit(10).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.getDocuments().size()< 4 ){
                            database.collection("categories")
                                    .document(catId)
                                    .collection("questions")
                                    .whereLessThanOrEqualTo("index",rand)
                                    .orderBy("index")
                                    .limit(4).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for (DocumentSnapshot snapshot : queryDocumentSnapshots){
                                                Question question =snapshot.toObject(Question.class);
                                                questions.add(question);

                                            }
                                            setNextQuestion();

                                        }
                                    });

                        }else {
                            for (DocumentSnapshot snapshot : queryDocumentSnapshots){
                                Question question =snapshot.toObject(Question.class);
                                questions.add(question);

                            }
                            setNextQuestion();
                        }

                    }
                });



        resetTimer();

        binding.quitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(QuizActivity.this, HomeFragment.class));
            }
        });

    }

    void setNextQuestion() {
        if (timer != null) {
            timer.cancel();
        }

        if (index < questions.size()) {
            binding.questionCounter.setText(String.format("%d/%d", (index + 1), questions.size()));

            question = questions.get(index);
            binding.question.setText(question.getQuestion());
            binding.option1.setText(question.getOption1());
            binding.option2.setText(question.getOption2());
            binding.option3.setText(question.getOption3());
            binding.option4.setText(question.getOption4());

//            resetTimer();
            timer.start();
        } else {
            moveToResultActivity();
        }
    }



    void resetTimer() {
        timer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long l) {
                binding.timer.setText(String.valueOf(l / 1000));
            }

            @Override
            public void onFinish() {
                index++;
                if (index < questions.size()) {
                    reset();
                    setNextQuestion();
                } else {
                    moveToResultActivity();
                }
            }
        };
    }


    void checkAnswer(TextView textView){
        String selectedAnswer = textView.getText().toString();
        if(selectedAnswer.equals(question.getAnswer())) {
            correctAnswers++;
            textView.setBackground(getResources().getDrawable(R.drawable.option_right));
        }else {
            showAnswer();
            textView.setBackground(getResources().getDrawable(R.drawable.option_wrong));
        }

    }

    void showAnswer(){
        if(question.getAnswer().equals(binding.option1.getText().toString()))
            binding.option1.setBackground(getResources().getDrawable(R.drawable.option_right));
        else if (question.getAnswer().equals(binding.option2.getText().toString()))
            binding.option2.setBackground(getResources().getDrawable(R.drawable.option_right));
        else   if (question.getAnswer().equals(binding.option3.getText().toString()))
                binding.option3.setBackground(getResources().getDrawable(R.drawable.option_right));
        else if (question.getAnswer().equals(binding.option4.getText().toString()))
                    binding.option4.setBackground(getResources().getDrawable(R.drawable.option_right));


    }




    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.option_1 || id == R.id.option_2 || id == R.id.option_3 || id == R.id.option_4) {
//            if (timer != null) {
////                timer.cancel();
////                setNextQuestion();
//            }
            TextView selected = (TextView) view;
            checkAnswer(selected);
        } else if (id == R.id.nextBtn) {
            index++;
            if (index < questions.size()) {
                reset();
                setNextQuestion();
            } else {
                moveToResultActivity();
            }
        }
    }
    private void moveToResultActivity() {
        Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
        intent.putExtra("correct", correctAnswers);
        intent.putExtra("total", questions.size());
        startActivity(intent);
        finish();
    }






    void reset(){
        binding.option1.setBackground(getResources().getDrawable(R.drawable.option_unselected));
        binding.option2.setBackground(getResources().getDrawable(R.drawable.option_unselected));
        binding.option3.setBackground(getResources().getDrawable(R.drawable.option_unselected));
        binding.option4.setBackground(getResources().getDrawable(R.drawable.option_unselected));
    }












}