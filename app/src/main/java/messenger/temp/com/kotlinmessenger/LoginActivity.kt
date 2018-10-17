package messenger.temp.com.kotlinmessenger

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_signup.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        login_btn.setOnClickListener {
            Login()
        }

        back_btn_LoginActivity.setOnClickListener {
            this.finish()
        }
    }

    fun Login(){
        var email = email_EditText_LoginActivity.text.toString()
        var password = password_EditeText_LoginActivity.text.toString()

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this,"please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener {
            if(!it.isSuccessful){
                return@addOnCompleteListener
            }
            Toast.makeText(this@LoginActivity,"Login complete",Toast.LENGTH_SHORT).show()
            var intent = Intent(this@LoginActivity, LatestMessagesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }.addOnFailureListener {

            Toast.makeText(this@LoginActivity,"${it.message}",Toast.LENGTH_SHORT).show()
        }

    }
}
