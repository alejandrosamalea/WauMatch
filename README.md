# WauMatch 🐶💘

**WauMatch** es una app Android que conecta amantes de los perros a través de anuncios, chats y perfiles personalizados.  
Está desarrollada con Firebase y requiere una configuración mínima para funcionar correctamente en tu entorno local.

---

## ⚙️ Requisitos de configuración

### 🔐 Firebase obligatorio

Para usar esta app, debes crear y configurar tu propio proyecto en Firebase.  
Esto evita que otros usuarios accedan a una base de datos compartida o sensible.

#### Pasos:

1. Ve a [Firebase Console](https://console.firebase.google.com/) y crea un nuevo proyecto.
2. Registra tu app Android en Firebase:
   - Usa el mismo nombre de paquete: `com.example.waumatch` (o cambia también en tu código).
3. Descarga el archivo `google-services.json` generado.
4. Coloca ese archivo dentro de la carpeta `/app` del proyecto.
5. Asegúrate de activar Firestore (o Realtime Database) y configurar las reglas de acceso según tus necesidades.

❗**No uses el `google-services.json` del repositorio original.**  
Cada usuario debe configurar su propia base de datos para mantener la seguridad y privacidad de los datos.

La Apk incluida en el repositorio solo se puede utilizar en un dispositivo android a ser preferible telefono
---

## 📱 Funcionamiento de la App

### 📝 Registro e Inicio de Sesión
- Al registrarte, deberás introducir tus datos personales.
- Se envía un **correo de verificación**. Es necesario confirmarlo para poder iniciar sesión.
- Luego podrás acceder a la app con tu cuenta verificada.

### 🏠 Pantalla Principal (Home)
- Visualizas anuncios de otros usuarios en un **radio de 100 km** desde tu ubicación registrada.
- Puedes:
  - Crear anuncios con campos obligatorios
  - Ver anuncios de otros
  - Añadir anuncios a favoritos
  - Iniciar conversaciones con otros usuarios

### 💬 Chat
- Chat privado con cada usuario con el que hayas interactuado.
- Permite coordinar encuentros, resolver dudas o simplemente charlar sobre las mascotas.

### ⭐ Favoritos
- Guarda anuncios que te interesen en tu sección de favoritos para fácil acceso.

### 👥 Perfiles de Usuarios
- Consulta los perfiles de otros:
  - Anuncios activos y caducados
  - Mascotas registradas
  - Reseñas de otros usuarios

### 👤 Tu Perfil
- Gestiona tu información personal:
  - Añade y edita mascotas
  - Gestiona tus reseñas
  - Configura horarios disponibles
  - Establece tu ubicación en el mapa
  - Edita tu nombre, gustos, y foto

🔒 Tu ubicación solo será visible para otros usuarios como un **área aproximada de 2 km** para proteger tu privacidad.

---

## 📦 Generar APK

Puedes generar una APK usando Android Studio:


El archivo se generará en `app/build/outputs/apk/release/`.  
El archivo `output-metadata.json` puede ser ignorado si solo necesitas la APK.

---

## 📄 Licencia

Este proyecto está bajo licencia [MIT](LICENSE).  
Tú eres responsable del uso de tu propia base de datos y entorno de backend.

---

## 🧾 Créditos

Desarrollado por [Tu Nombre o Usuario de GitHub]  
Diseñado para promover conexiones entre dueños de mascotas de forma segura y amigable 🐾
