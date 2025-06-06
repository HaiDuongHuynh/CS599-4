package com.example.lab4

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstraintSet
//import androidx.constraintlayout.compose.createRefs
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.constraintlayout.compose.ConstraintLayoutBaseScope.*
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lab4.ui.theme.Lab4Theme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab4Theme {
                AppNavigator()
            }
        }
    }
}

@Composable
fun AppNavigator() {
    val navController = rememberNavController()

    val users = remember {
        mutableStateListOf(
            User("John", "Doe", "john@example.com", "1234"),
            User("Jane", "Smith", "jane@example.com", "abcd"),
            User("Bob", "Marley", "bob@example.com", "pass123"),
            User("Alice", "Wonder", "alice@example.com", "wonder1"),
            User("Mark", "Zane", "mark@example.com", "zane987")
        )
    }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(userList = users, onLoginSuccess = { username ->
                navController.navigate("shopping/$username")
            }, onCreateAccount = {
                navController.navigate("register")
            })
        }
        composable("register") {
            CreateAccountScreen(
                onRegister = { newUser ->
                    users.add(newUser)
                    navController.popBackStack("login", inclusive = false)
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }
        composable("shopping/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            CategoryScreen(username)
        }
    }
}


@Composable
fun LoginScreen(userList: List<User>, onLoginSuccess: (String) -> Unit, onCreateAccount: () -> Unit) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Walmart Logo
        Image(
            painter = painterResource(id = R.drawable.maharishi_icon),
            contentDescription = "Walmart Logo",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = "Sign in to your account",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(24.dp))

        var email by remember { mutableStateOf("") }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email address (required)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(12.dp))

        var password by remember { mutableStateOf("") }
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (required)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        // Forgot Password
        Text(
            text = "Forgot password?",
            color = Color(0xFF1E88E5),
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.End)
                .clickable {
                    val user = userList.find { it.username == email }
                    if (user != null) {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${user.username}") // username is email
                            putExtra(Intent.EXTRA_SUBJECT, "Password Recovery")
                            putExtra(Intent.EXTRA_TEXT, "Your password is: ${user.password}")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Email not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sign In Button
        Button(
            onClick = { when {
                email.isBlank() || password.isBlank() -> {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val user = userList.find { it.username == email && it.password == password }
                    if (user != null) {
                        onLoginSuccess(user.username)
                    } else {
                        Toast.makeText(context, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    }
                }
            } },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0071CE)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Sign In", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Don't have an account
        Text(
            text = "Don’t have an account?",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Create new account
        Text(
            text = "Create a new account",
            fontSize = 14.sp,
            color = Color(0xFF0071CE),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onCreateAccount() }
        )
    }
}

@Composable
fun CategoryScreen(username: String) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val (welcome, title, electronicsImg, clothingImg, beautyImg, foodImg, electronicsTxt, clothingTxt, beautyTxt, foodTxt) = createRefs()

        Text("Welcome $username", modifier = Modifier.constrainAs(welcome) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
        })

        Text("Shop by Categories", fontSize = 20.sp, modifier = Modifier.constrainAs(title) {
            top.linkTo(welcome.bottom, margin = 16.dp)
            start.linkTo(parent.start)
        })

        // Electronics Image
        Image(
            painter = painterResource(id = R.drawable.electronics),
            contentDescription = null,
            modifier = Modifier
                .size(160.dp)
                .constrainAs(electronicsImg) {
                    top.linkTo(title.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                }
        )

        Text("Electronics", modifier = Modifier.constrainAs(electronicsTxt) {
            top.linkTo(electronicsImg.bottom)
            start.linkTo(electronicsImg.start)
        })

        // Clothing Image
        Image(
            painter = painterResource(id = R.drawable.clothing),
            contentDescription = null,
            modifier = Modifier
                .size(160.dp)
                .constrainAs(clothingImg) {
                    top.linkTo(title.bottom, margin = 16.dp)
                    end.linkTo(parent.end)
                }
        )

        Text("Clothing", modifier = Modifier.constrainAs(clothingTxt) {
            top.linkTo(clothingImg.bottom)
            start.linkTo(clothingImg.start)
        })

        // Beauty Image
        Image(
            painter = painterResource(id = R.drawable.beauty),
            contentDescription = null,
            modifier = Modifier
                .size(160.dp)
                .constrainAs(beautyImg) {
                    top.linkTo(electronicsImg.bottom, margin = 32.dp)
                    start.linkTo(parent.start)
                }
        )

        Text("Beauty", modifier = Modifier.constrainAs(beautyTxt) {
            top.linkTo(beautyImg.bottom)
            start.linkTo(beautyImg.start)
        })

        // Food Image
        Image(
            painter = painterResource(id = R.drawable.food),
            contentDescription = null,
            modifier = Modifier
                .size(160.dp)
                .constrainAs(foodImg) {
                    top.linkTo(clothingImg.bottom, margin = 32.dp)
                    end.linkTo(parent.end)
                }
        )

        Text("Food", modifier = Modifier.constrainAs(foodTxt) {
            top.linkTo(foodImg.bottom)
            start.linkTo(foodImg.start)
        })
    }
}

@Composable
fun CreateAccountScreen(onRegister: (User) -> Unit,
                        onCancel: () -> Unit) {
    val context = LocalContext.current
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var emails by remember { mutableStateOf("") }
    var passwords by remember { mutableStateOf("") }
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val (logo, title, fname, lname, email, password, button, button1) = createRefs()

        Image(
            painter = painterResource(id = R.drawable.maharishi_icon),
            contentDescription = "Logo",
            modifier = Modifier
                .size(100.dp)
                .constrainAs(logo) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        Text("Create your account", modifier = Modifier.constrainAs(title) {
            top.linkTo(logo.bottom, margin = 16.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        })

        OutlinedTextField(
            value = firstName,
            onValueChange = {firstName = it},
            label = { Text("First name*") },
            modifier = Modifier.constrainAs(fname) {
                top.linkTo(title.bottom, margin = 16.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )

        OutlinedTextField(
            value = lastName,
            onValueChange = {lastName = it},
            label = { Text("Last name*") },
            modifier = Modifier.constrainAs(lname) {
                top.linkTo(fname.bottom, margin = 8.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )

        OutlinedTextField(
            value = emails,
            onValueChange = { emails = it },
            label = { Text("Email address*") },
            modifier = Modifier.constrainAs(email) {
                top.linkTo(lname.bottom, margin = 8.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )

        OutlinedTextField(
            value = passwords,
            onValueChange = { passwords = it },
            label = { Text("Password*") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.constrainAs(password) {
                top.linkTo(email.bottom, margin = 8.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )

        Button(
            onClick = { if (firstName.isNotBlank() && lastName.isNotBlank() && emails.isNotBlank() && passwords.isNotBlank()) {
                onRegister(User(firstName, lastName, emails, passwords))
                Toast.makeText(context, "User registered succesfully!", Toast.LENGTH_SHORT).show()
            } },
            modifier = Modifier.constrainAs(button) {
                top.linkTo(password.bottom, margin = 16.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        ) {
            Text("Create Account")
        }

        Button(
            onClick = onCancel,
            modifier = Modifier.constrainAs(button1) {
                top.linkTo(button.bottom, margin = 16.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        ) {
            Text("Cancel")
        }
    }
}