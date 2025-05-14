package com.example.waumatch.viewmodel;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileManager extends ViewModel {
    public static class Availability {
        private String weekdays;
        private String weekends;

        public Availability() {
            this.weekdays = "";
            this.weekends = "";
        }

        public Availability(String weekdays, String weekends) {
            this.weekdays = weekdays;
            this.weekends = weekends;
        }

        public String getWeekdays() {
            return weekdays;
        }

        public void setWeekdays(String weekdays) {
            this.weekdays = weekdays;
        }

        public String getWeekends() {
            return weekends;
        }

        public void setWeekends(String weekends) {
            this.weekends = weekends;
        }
    }

    public static class ProfileData {
        private String profileImage = "";
        private String name = "";
        private String subtitle = "";
        private String about = "";
        private Availability availability = new Availability();

        public String getProfileImage() {
            return profileImage;
        }

        public void setProfileImage(String profileImage) {
            this.profileImage = profileImage;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        public String getAbout() {
            return about;
        }

        public void setAbout(String about) {
            this.about = about;
        }

        public Availability getAvailability() {
            return availability;
        }

        public void setAvailability(Availability availability) {
            this.availability = availability;
        }
    }

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final Context context;
    private final MutableLiveData<ProfileData> profileData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isDataLoaded = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isEditing = new MutableLiveData<>();

    public ProfileManager(Context context) {
        this.context = context;
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        CloudinaryManager.init(context);
        this.profileData.setValue(new ProfileData());
        this.isDataLoaded.setValue(false);
        this.isEditing.setValue(false);
        loadProfile();
    }

    public void updateProfileImage(String imageUri) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        Uri uri = Uri.parse(imageUri);

        Map<String, Object> options = new HashMap<>();
        options.put("public_id", "profile_images/" + userId);

        MediaManager.get().upload(uri)
                .options(options)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Toast.makeText(context, "Subiendo imagen...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        ProfileData currentData = profileData.getValue();
                        if (currentData != null) {
                            currentData.setProfileImage(url);
                            profileData.setValue(currentData);

                            db.collection("usuarios").document(userId)
                                    .update("profileImage", url)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Foto actualizada", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Error al guardar URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(context, "Error al subir imagen: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    public MutableLiveData<ProfileData> getProfileData() {
        return profileData;
    }

    public MutableLiveData<Boolean> getIsDataLoaded() {
        return isDataLoaded;
    }

    public MutableLiveData<Boolean> getIsEditing() {
        return isEditing;
    }

    public void loadProfile() {
        if (auth.getCurrentUser() == null) {
            isDataLoaded.setValue(true);
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        db.collection("usuarios").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ProfileData data = documentSnapshot.toObject(ProfileData.class);
                        profileData.setValue(data);
                    }
                    isDataLoaded.setValue(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error al cargar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    isDataLoaded.setValue(true);
                });
    }

    public void saveChanges(String nombre, String subtitle, String about, @NotNull Map<String, ? extends Map<String, String>> availability, List<String> tags) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        ProfileData currentData = profileData.getValue();
        if (currentData != null) {
            currentData.setName(nombre);
            currentData.setSubtitle(subtitle);
            currentData.setAbout(about);
            profileData.setValue(currentData);

            Map<String, Object> updates = new HashMap<>();
            updates.put("about", about);
            updates.put("nombre", nombre);
            updates.put("subtitle", subtitle);
            updates.put("availability", availability);
            updates.put("tags", tags);


            db.collection("usuarios").document(userId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Cambios guardados", Toast.LENGTH_SHORT).show();
                        isEditing.setValue(false);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error al guardar cambios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    public void toggleEditing() {
        Boolean isEditingNow = isEditing.getValue();
        if (isEditingNow != null) {
            isEditing.setValue(!isEditingNow);
        }
    }
}