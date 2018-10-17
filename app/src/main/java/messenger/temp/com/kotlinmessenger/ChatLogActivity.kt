package messenger.temp.com.kotlinmessenger

import android.app.Activity
import android.content.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.image_to_row.view.*
import kotlinx.android.synthetic.main.message_from_row.view.*
import kotlinx.android.synthetic.main.message_to_row.view.*
import messenger.temp.com.kotlinmessenger.NewMessageActivity.Companion.USER_ID
import messenger.temp.com.kotlinmessenger.NewMessageActivity.Companion.USER_IMAGE
import messenger.temp.com.kotlinmessenger.NewMessageActivity.Companion.USER_KEY
import java.net.URI

class ChatLogActivity : AppCompatActivity() {

    var adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        val username = intent.extras.getString(USER_KEY)
        supportActionBar?.title = username
        recyclerview_chat_log.adapter = adapter
        lestenToMessages()
        scrollToLastItem()
        send_btn_chat_log.setOnClickListener {
            sendMessage()
        }

        // change background
        loadBackgroundImageFromFirebaseStoarge()

        send_image_btn_log_chat.setOnClickListener {
            loadImage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == 1){
                var uri = data?.data
                var bitmap = MediaStore.Images.Media.getBitmap(contentResolver,uri)
                var drawable = BitmapDrawable(bitmap)
                saveBackgroundImageTOFirebaseStorage(uri!!)
                recyclerview_chat_log.background = drawable
            }else if (requestCode == 2){
                saveImageToFirebaseStorage(data?.data!!)
                adapter.add(ImageItem(data?.data!!))
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.cler_messages ->{
                clearMessage()
            }

            R.id.change_background ->{
                var intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent,1)
                Log.i("chat_log","open pick up image action")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_log_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun sendMessage(){
        var message = edittext_chat_log.text.toString()
        var fromid = FirebaseAuth.getInstance().uid
        var toid = intent.extras.getString(USER_ID)

        var ref = FirebaseDatabase.getInstance().getReference("user-messages/$fromid/$toid").push()
        var new_message = ChatMessage(ref.key!!,message,fromid!!,toid,System.currentTimeMillis())
        ref.setValue(new_message).addOnSuccessListener {
            edittext_chat_log.text.clear()
            scrollToLastItem()
        }
        var toRef = FirebaseDatabase.getInstance().getReference("user-messages/${toid}/$fromid").push()
        toRef.setValue(new_message)

        var latestMessageRef = FirebaseDatabase.getInstance().getReference("latest-messages/$fromid/$toid")
        var latestMessageToRef = FirebaseDatabase.getInstance().getReference("latest-messages/$toid/$fromid")
        latestMessageRef.setValue(new_message)
        latestMessageToRef.setValue(new_message)
    }

    fun lestenToMessages(){
        val toId = intent.extras.getString(USER_ID)
        val fromId = FirebaseAuth.getInstance().uid
        var ref = FirebaseDatabase.getInstance().getReference("user-messages/$fromId/$toId")
        var toRef = FirebaseDatabase.getInstance().getReference("user-messages/$toId/$fromId")

        ref.addChildEventListener(object:ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                var chatMessage = p0.getValue(ChatMessage::class.java)
                if(chatMessage != null){
                    if(chatMessage.fromId == "0"){
                        Log.i("chat-log","i found a image in the database: ${chatMessage.text}")
                        adapter.add(ImageItem(Uri.parse(chatMessage.text)))
                    }else{
                        if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                            adapter.add(UserItemTo(chatMessage.text,LatestMessagesActivity.currentUser!!.imageProfileUri,
                                    applicationContext))
                            scrollToLastItem()
                        }else{
                            var uri = intent.extras.getString(USER_IMAGE)
                            adapter.add(UserItemFrom(chatMessage.text,uri,applicationContext))
                            scrollToLastItem()
                        }
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })

    }

    fun clearMessage(){
        val toId = intent.extras.getString(USER_ID)
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("user-messages/$fromId/$toId").removeValue()
                .addOnSuccessListener {
            Log.i("chat_log","messages removed")
                    adapter.notifyDataSetChanged()
        }
    }

    fun scrollToLastItem(){
        recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1 )
    }

    fun saveBackgroundImageTOFirebaseStorage(uri:Uri){
        var toid = intent.extras.getString(USER_ID)
        var ref = FirebaseStorage.getInstance().getReference("images/$toid")
        ref.putFile(uri).addOnSuccessListener {
            Log.i("chat_log","background image upload to firebase storage")
            ref.downloadUrl.addOnSuccessListener {
                Log.i("chat_log","download url: $it")
            }
        }
    }

    fun loadBackgroundImageFromFirebaseStoarge(){
        var toid = intent.extras.getString(USER_ID)
        var ref = FirebaseStorage.getInstance().getReference("images/$toid")
        ref.downloadUrl.addOnSuccessListener {
            Log.i("chat_log",it.toString())
            var imageView:ImageView = ImageView(this@ChatLogActivity)
            Picasso.get().load(it).into(imageView)
            //var bitmap = MediaStore.Images.Media.getBitmap(contentResolver,it!!)
            recyclerview_chat_log.background = imageView.drawable
        }
    }

    fun loadImage(){
        var intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        Log.i("chat-log","start pick image")
        startActivityForResult(intent,2)
    }

    fun saveImageToFirebaseStorage(uri:Uri){
        var ref = FirebaseStorage.getInstance().getReference("images")
        var fromid = FirebaseAuth.getInstance().uid
        var toid = intent.extras.getString(USER_ID)



        ref.putFile(uri).addOnSuccessListener {
            Log.i("chat-log","image uploaded to storage")
        }
        var urlDownload:String? = null
        ref.downloadUrl.addOnSuccessListener {
            urlDownload = it.toString()
        }

        var database = FirebaseDatabase.getInstance().getReference("user-messages/$fromid/$toid").push()
        var new_image = ChatMessage(database.key!!,urlDownload!!,"0","0",0)
        database.setValue(new_image).addOnSuccessListener {
            Log.i("chat-log","image now in database")
        }
    }
}

class ChatMessage(var id:String, var text:String,var fromId:String, var toId:String,
                  var timestamp:Long){
    constructor():this("","","","",-1)
}

class UserItemFrom(var text:String, var uri:String,var context: Context):Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.message_from_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.from_textview.text = text
        Picasso.get().load(uri).into(viewHolder.itemView.from_imageView)
        viewHolder.itemView.setOnLongClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            var clip = ClipData.newPlainText("plain text",text)
            clipboard.primaryClip = clip
            Toast.makeText(context,"coped text",Toast.LENGTH_SHORT).show()
            true
        }
    }

}

class UserItemTo(var text:String, var uri:String, var context: Context):Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.message_to_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.to_textview.text = text
        Picasso.get().load(uri).into(viewHolder.itemView.to_imageView)
        viewHolder.itemView.setOnLongClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            var clip = ClipData.newPlainText("plain text",text)
            clipboard.primaryClip = clip
            Toast.makeText(context,"coped text",Toast.LENGTH_SHORT).show()
            true
        }
    }

}

class ImageItem(var uri:Uri):Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.image_to_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        Log.i("chat-log","picasso will load: $uri")
        Picasso.get().load(uri).into(viewHolder.itemView.image_to_row_imageview)
    }

}
