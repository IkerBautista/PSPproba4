package com.example.pspproba4

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pspproba4.ui.theme.PSPproba4Theme
import kotlin.concurrent.thread
import kotlin.random.Random

val COCINEROS = listOf("Ane", "Jon", "Miren")
const val TOTAL_PLATOS = 12

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PSPproba4Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PantallaCocina(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@SuppressLint("ContextCastToActivity")
@Composable
fun PantallaCocina(modifier: Modifier = Modifier) {
    // Estado de la interfaz
    var txtEstado by remember { mutableStateOf("Cocina cerrada") }
    var platosServidos by remember { mutableIntStateOf(0) }
    var txtLog by remember { mutableStateOf("") }
    var cocinaAbierta by remember { mutableStateOf(false) }

    val activity = LocalContext.current as? ComponentActivity  // para runOnUiThread
    val lock = remember { Any() }

    // Añade una línea al log
    fun log(mensaje: String) {
        txtLog += "$mensaje\n"
    }

    // ScrollView del log: baja automáticamente al último mensaje
    val scrollState = rememberScrollState()
    LaunchedEffect(txtLog) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // txtEstado
        Text(
            text = txtEstado,
            style = MaterialTheme.typography.headlineSmall
        )

        // txtContador
        Text(
            text = "Platos servidos: $platosServidos / $TOTAL_PLATOS",
            style = MaterialTheme.typography.titleMedium
        )

        // txtLog dentro de un "ScrollView"
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = txtLog,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // btnAbrir
        Button(
            onClick = {
                cocinaAbierta = true
                txtEstado = "Cocina abierta"
                platosServidos = 0
                txtLog = ""
                var contador = 0                      // recurso compartido
                val inicio = System.currentTimeMillis()

                // Un hilo por cocinero (metodo 3)
                val hilos = COCINEROS.map { nombre ->
                    thread {
                        for (plato in 1..4) {
                            val ms = Random.nextLong(500, 2001)
                            Thread.sleep(ms)          // fuera del lock
                            synchronized(lock) { contador++ }
                            activity?.runOnUiThread {
                                log("$nombre: plato $plato listo ($ms ms)")
                                platosServidos = contador
                            }
                        }
                    }
                }

                // Hilo que espera a los cocineros
                thread {
                    hilos.forEach { it.join() }
                    val seg = (System.currentTimeMillis() - inicio) / 1000.0
                    activity?.runOnUiThread {
                        platosServidos = TOTAL_PLATOS
                        txtEstado = "Servicio terminado en %.1f s".format(seg)
                        cocinaAbierta = false
                    }
                }
            },
            enabled = !cocinaAbierta,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Abrir cocina")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PantallaCocinaPreview() {
    PSPproba4Theme {
        PantallaCocina()
    }
}