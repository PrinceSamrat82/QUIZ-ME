package com.prince.quizme;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.prince.quizme.databinding.FragmentProfileBinding;
import com.squareup.picasso.Picasso;

import java.net.CookieHandler;
import java.util.HashMap;
import java.util.Map;


public class ProfileFragment extends Fragment {

    FragmentProfileBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore database;
    FirebaseStorage storage;
    StorageReference storageReference;
    final int PICK_IMAGE_REQUEST = 1;
    Uri profile;




    public ProfileFragment() {
        // Required empty public constructor
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding =FragmentProfileBinding.inflate(inflater, container,false);
        database =FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        loadUserProfile();
        binding.profileImage.setOnClickListener(v -> openImageChooser());
        binding.updateBtn.setOnClickListener(v -> updateProfile());



        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth= FirebaseAuth.getInstance();
                auth.signOut();
                startActivity(new Intent(getContext(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));


            }
        });




        // Inflate the layout for this fragment
        return binding.getRoot();
    }





    /** @noinspection deprecation*/
    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    /** @noinspection deprecation*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            profile = data.getData();
            binding.profileImage.setImageURI(profile);
        }
    }

    private void updateProfile() {
        String name = binding.nameBox.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (profile != null) {
            StorageReference fileRef = storageReference.child("profile_images/" + System.currentTimeMillis() + ".jpg");
            fileRef.putFile(profile).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                saveUserProfile(name, imageUrl);
            })).addOnFailureListener(e -> {
                Toast.makeText(getActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                Log.e("ProfileFragment", "Image upload failed", e);
            });
        } else {
            saveUserProfile(name, null);
        }
    }

    private void saveUserProfile(String name, @Nullable String imageUrl) {
        DocumentReference userRef = database.collection("users").document("user_id"); // Use the actual user ID
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("name", name);
        if (imageUrl != null) {
            userProfile.put("imageUrl", imageUrl);
        }

        userRef.set(userProfile).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getActivity(), "Profile updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                Log.e("ProfileFragment", "Profile update failed", task.getException());
            }
        });
    }

    private void loadUserProfile() {
        DocumentReference userRef = database.collection("users").document("user_id"); // Use the actual user ID
        userRef.get().addOnCompleteListener(this::onComplete);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void onComplete(Task<DocumentSnapshot> task) {
        if (task.isSuccessful() && task.getResult() != null) {
            DocumentSnapshot document = task.getResult();
            String name = document.getString("name");
            String profile = document.getString("profile");

            binding.nameBox.setText(name);


            if (profile!= null) {

                Picasso.get().load(profile).into(binding.profileImage);
            }
        } else {
            Toast.makeText(getActivity(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            Log.e("ProfileFragment", "Profile load failed", task.getException());
        }
    }
}