// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.github.alexdlaird.ngrok.NgrokClient
import com.github.alexdlaird.ngrok.protocol.CreateTunnel
import com.github.alexdlaird.ngrok.protocol.Proto
import com.github.alexdlaird.ngrok.protocol.Tunnel

@Composable
@Preview
fun App() {

    var tunnel by remember { mutableStateOf(Tunnel()) }

    var textInput by remember { mutableStateOf("80") }
    var tcpCh by remember { mutableStateOf(false) }
    var httpCh by remember { mutableStateOf(true) }

    MaterialTheme {
        Box(Modifier.fillMaxWidth().fillMaxHeight().padding(top = 20.dp)) {

            Box(Modifier.width(700.dp).fillMaxHeight().padding(start = 200.dp)) {
                Column {
                    Row {
                        Box(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(top = 10.dp),
                            contentAlignment = Center
                        ) {
                            Text(
                                "Открытие порта", fontSize = 30.sp
                            )
                        }
                    }
                    Row(Modifier.padding(start = 20.dp, top = 20.dp)) {
                        TextField(
                            value = textInput,
                            onValueChange = {
                                if (it.toIntOrNull() != null)
                                    textInput = it
                            },
                            modifier = Modifier
                                .width(250.dp)
                                .padding(vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            trailingIcon = {
                                Icon(Icons.Filled.Add, "", tint = Black)
                            },
                            colors = TextFieldDefaults.textFieldColors(
                                backgroundColor = Color(100, 100, 100, 50),
                                focusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                    Row {
                        Column {
                            Row(verticalAlignment = CenterVertically) {
                                Checkbox(tcpCh, {
                                    tcpCh = it
                                    if (tcpCh)
                                        httpCh = false
                                })
                                Text("TCP")
                            }
                            Row(verticalAlignment = CenterVertically) {
                                Checkbox(httpCh, {
                                    httpCh = it
                                    if (httpCh)
                                        tcpCh = false
                                })
                                Text("HTTP")
                            }
                        }
                    }
                    Row(Modifier.padding(top = 20.dp, start = 20.dp)) {
                        Button({
                            Thread {
                                tunnel = startTunnel(textInput.toInt(), if (httpCh) Proto.HTTP else Proto.TCP)
                            }.start()
                        }) {
                            Text("Открыть порт")
                        }
                    }
                    Row(Modifier.padding(top = 20.dp, start = 20.dp)) {
                        Column {
                            Row {
                                SelectionContainer {
                                    Text("URL: ${tunnel.publicUrl}")
                                }
                            }
                        }

                    }
                }
            }

            Box(Modifier.width(200.dp).fillMaxHeight().background(Color.LightGray)) {
                Column(Modifier.width(200.dp).fillMaxHeight()) {
                    Row {
                        Button(
                            onClick = {

                            },
                            Modifier.width(200.dp).height(35.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(138, 127, 142)),
                            shape = RectangleShape,
                        ) {
                            Text("-")
                            Spacer(Modifier.width(20.dp))
                            Text("Открыть порт")
                        }
                    }
                }
            }
        }

    }
}

fun main() = application {
    val winState = rememberWindowState(width = 700.dp, height = 500.dp)
    Window(
        onCloseRequest = ::exitApplication,
        state = winState,
        undecorated = true,
        resizable = false
    ) {
        WindowDraggableArea {
            Box(Modifier.fillMaxWidth().height(20.dp).background(Color.DarkGray)) {
                Text("NgrokGUI", Modifier.padding(start = 10.dp), color = Color.Cyan, fontWeight = FontWeight.ExtraBold)
                Button(
                    onClick = {
                        exitApplication()
                    },
                    Modifier.width(17.dp).height(17.dp).padding(top = 2.dp, end = 2.dp).align(Alignment.TopEnd),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                ) {}

                Button(
                    onClick = {
                        winState.isMinimized = true
                    },
                    Modifier.width(37.dp).height(17.dp).padding(top = 2.dp, end = 22.dp).align(Alignment.TopEnd),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
                ) {}
            }
        }
        App()
    }
}

fun startTunnel(port: Int, proto: Proto): Tunnel {
    val ngrokClient = NgrokClient.Builder().build()
    val tcpTunnelCreate = CreateTunnel.Builder().withProto(proto).withAddr(port).build()
    return ngrokClient.connect(tcpTunnelCreate)
}