package com.example.waumatch.viewmodel;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;

public class ProfileManager extends ViewModel {
    // Clases de datos como objetos Java
    public static class Availability {
        private String weekdays;
        private String weekends;

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
        private String name;
        private String subtitle;
        private String about;
        private Availability availability;
        private String profileImage;

        public ProfileData() {
            this.name = "";
            this.subtitle = "Cuidadora Certificada 🐾";
            this.about = "¡Hola! Soy una apasionada cuidadora de perros con más de 3 años de experiencia...";
            this.availability = new Availability("9:00 - 18:00", "10:00 - 15:00");
            this.profileImage = "https://api.a0.dev/assets/image?text=friendly%20person%20with%20dog%20profile%20picture&aspect=1:1";
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

        public String getProfileImage() {
            return profileImage;
        }

        public void setProfileImage(String profileImage) {
            this.profileImage = profileImage;
        }
    }

    // Campos de la clase
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final Context context;

    // Estado observable
    private final MutableLiveData<ProfileData> profileData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isDataLoaded = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isEditing = new MutableLiveData<>();

    public ProfileManager(Context context) {
        this.context = context;
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.profileData.setValue(new ProfileData());
        this.isDataLoaded.setValue(false);
        this.isEditing.setValue(false);
        loadProfile();
    }

    // Métodos para obtener LiveData
    public MutableLiveData<ProfileData> getProfileData() {
        return profileData;
    }

    public MutableLiveData<Boolean> getIsDataLoaded() {
        return isDataLoaded;
    }

    public MutableLiveData<Boolean> getIsEditing() {
        return isEditing;
    }

    // Cargar datos del perfil desde Firestore
    public void loadProfile() {
        if (auth.getCurrentUser() == null) {
            isDataLoaded.setValue(true);
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        db.collection("usuarios").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    ProfileData currentData = profileData.getValue();
                    if (documentSnapshot.exists()) {
                        // Cargar nombre
                        String name = documentSnapshot.getString("nombre");
                        if (name == null) name = "Sin nombre";
                        currentData.setName(name);
                        // Cargar imagen de perfil
                        String profileImage = documentSnapshot.getString("profileImage");
                        if (profileImage != null && !profileImage.isEmpty()) {
                            currentData.setProfileImage(profileImage);
                        }
                        profileData.setValue(currentData);
                    }
                    isDataLoaded.setValue(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error al cargar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    isDataLoaded.setValue(true);
                });
    }

    // Guardar cambios en Firestore
    public void saveChanges() {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        ProfileData currentData = profileData.getValue();
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", currentData.getName());
        // Añadir profileImage para asegurar que se guarda si cambió
        updates.put("profileImage", currentData.getProfileImage());

        db.collection("usuarios").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Cambios guardados", Toast.LENGTH_SHORT).show();
                    isEditing.setValue(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Actualizar la imagen de perfil
    public void updateProfileImage(String imageUri) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profile_images/" + userId);

        // Subir la imagen a Firebase Storage
        storageRef.putFile(Uri.parse(imageUri))
                .addOnSuccessListener(taskSnapshot -> {
                    // Obtener la URL de descarga
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        ProfileData currentData = profileData.getValue();
                        currentData.setProfileImage(uri.toString());
                        profileData.setValue(currentData);

                        // Guardar la URL en Firestore
                        db.collection("usuarios").document(userId)
                                .update("profileImage", uri.toString())
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Foto actualizada", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Error al guardar URL de la foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }).addOnFailureListener(e -> {
                    });
                })
                .addOnFailureListener(e -> {
                });
    }

    // Cambiar el modo de edición
    public void toggleEditing() {
        Boolean currentEditing = isEditing.getValue();
        if (currentEditing != null && currentEditing) {
            saveChanges();
        } else {
            isEditing.setValue(true);
            Toast.makeText(context, "Modo edición activado", Toast.LENGTH_SHORT).show();
        }
    }
}