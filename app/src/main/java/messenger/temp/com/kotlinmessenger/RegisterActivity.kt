package messenger.temp.com.kotlinmessenger

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        register_btn.setOnClickListener {
            RegisterNewUser();
        }

        login_btn.setOnClickListener {
            var intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }

        select_photo_btn.setOnClickListener {
            var intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    var selectedImageUri:Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){

            Log.i("RegisterActivity","Image selected")

            selectedImageUri = data.data
            var bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedImageUri)

            profile_image.setImageBitmap(bitmap)

            select_photo_btn.alpha = 0f

            //var drawable = BitmapDrawable(bitmap)
           // select_photo_btn.background = drawable
        }
    }

    fun RegisterNewUser(){
        var email = email_EditText_MainActivity.text.toString()
        var password = password_EditText_MainActivity.text.toString()

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this,"please fill all fields",Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener {
            if(!it.isSuccessful){
                return@addOnCompleteListener
            }

            Toast.makeText(this@RegisterActivity,"Register complete",Toast.LENGTH_SHORT).show()
            Log.i("RegisterActivity","Register complete")
            UploadImageToFirebase()

        }.addOnFailureListener {

            Toast.makeText(this@RegisterActivity,"${it.message}",Toast.LENGTH_SHORT).show()
        }

    }

    fun UploadImageToFirebase(){
        if(selectedImageUri == null) return

        var filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedImageUri!!).addOnSuccessListener{
            Log.i("RegisterActivity","upload image completed")
            ref.downloadUrl.addOnSuccessListener {
                Log.i("RegisterActivity","image url: $it")
                saveUserDataToFirebaseDatabase(it.toString())
            }
        }
    }

    fun saveUserDataToFirebaseDatabase(imageUri:String){
        var uid = FirebaseAuth.getInstance().uid
        var user = User(uid!!,username_EditeText_MainActivity.text.toString(),imageUri)
        var database = FirebaseDatabase.getInstance().getReference("users/$uid")
        database.setValue(user).addOnSuccessListener {
            Log.i("RegisterActivity","user saved to firebase")
            var intent = Intent(this@RegisterActivity,LatestMessagesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    class User(var Uid:String, var username:String, var imageProfileUri:String){
        constructor():this("","","")
    }
}
