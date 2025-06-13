package com.example.waumatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.waumatch.ui.theme.WauMatchTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(navController: NavController) {
    WauMatchTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Términos y Condiciones",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFF2EDFF2)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF111826),
                        titleContentColor = Color.White
                    )
                )
            },
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF111826),
                                    Color(0xFF022859)
                                )
                            )
                        )
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Términos y Condiciones de WauMatch",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2EDFF2),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    Text(
                                text = """
        Bienvenido a WauMatch. Al registrarte y utilizar nuestra aplicación, aceptas cumplir con los siguientes términos y condiciones. Por favor, léelos cuidadosamente.
        
        **1. Aceptación de los Términos**  
        Al crear una cuenta en WauMatch, aceptas estos términos y condiciones en su totalidad. Si no estás de acuerdo, no podrás registrarte ni utilizar nuestros servicios.
        
        **2. Uso de la Plataforma**  
        - Debes ser mayor de 18 años para utilizar WauMatch.  
        - No puedes usar la plataforma para actividades ilegales, fraudulentas o no autorizadas.  
        - Eres responsable de mantener la confidencialidad de tu cuenta y contraseña.  
        - Está prohibido suplantar a otras personas o proporcionar información falsa.
        
        **3. Tratamiento de Datos Personales y Confidencialidad**  
        - WauMatch puede recopilar y procesar datos personales tales como nombre, correo electrónico, número de teléfono, ubicación y contenido de mensajes intercambiados mediante el chat.  
        - Estos datos son tratados de conformidad con nuestra Política de Privacidad y el Reglamento General de Protección de Datos (RGPD).  
        - WauMatch no vende ni comparte tu información con terceros no autorizados.  
        - El usuario acepta que la información proporcionada sea utilizada para prestar los servicios, mejorar la experiencia y garantizar la seguridad de la comunidad.
        
        **4. Seguridad y Mensajería**  
        - Los mensajes enviados a través de la aplicación no deben contener lenguaje ofensivo, amenazas, acoso o contenido ilegal.  
        - El uso del chat implica consentimiento explícito para el tratamiento de los mensajes con fines de moderación, análisis y mejora del servicio.  
        - Los números de teléfono y direcciones compartidos entre usuarios son responsabilidad exclusiva de las partes involucradas. WauMatch no se hace responsable por el mal uso de esta información.
        
        **5. Publicación de Anuncios y Contenido del Usuario**  
        - Eres el único responsable del contenido que compartes en tu perfil o anuncios (textos, fotos, ubicación, etc.).  
        - No se permite contenido ofensivo, ilegal, engañoso o que infrinja los derechos de terceros.  
        - WauMatch puede eliminar contenido que considere inapropiado o que viole estos términos, sin previo aviso.
        
        **6. Limitación de Responsabilidad**  
        - WauMatch actúa como intermediario entre usuarios que ofrecen y demandan servicios relacionados con el cuidado de mascotas.  
        - No se responsabiliza de los acuerdos privados entre usuarios, ni de posibles daños, pérdidas o incidentes derivados del uso de la plataforma.  
        - El uso de WauMatch es bajo tu propio riesgo.
        
        **7. Modificaciones de los Términos**  
        Nos reservamos el derecho de modificar estos términos en cualquier momento. Te notificaremos sobre cambios significativos a través de la aplicación o por correo electrónico.
        
        **8. Contacto**  
        Si tienes preguntas sobre estos términos, contáctanos en soporte@waumatch.com.
        
        Última actualización: 13 de junio de 2025
        """.trimIndent(),
                        fontSize = 16.sp,
                        color = Color.White,
                        textAlign = TextAlign.Justify,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        )
    }
}