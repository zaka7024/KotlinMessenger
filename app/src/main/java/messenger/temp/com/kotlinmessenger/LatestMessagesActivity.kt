package messenger.temp.com.kotlinmessenger

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_message_row.view.*
import kotlinx.android.synthetic.main.user_row_item.view.*
import messenger.temp.com.kotlinmessenger.NewMessageActivity.Companion.USER_ID
import messenger.temp.com.kotlinmessenger.NewMessageActivity.Companion.USER_IMAGE
import messenger.temp.com.kotlinmessenger.NewMessageActivity.Companion.USER_KEY

class LatestMessagesActivity : AppCompatActivity() {

    companion object {
        var currentUser: RegisterActivity.User? = null
    }

    var adapter = GroupAdapter<ViewHolder>()
    var lastMessageMap = HashMap<String?,ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)
        recyclerview_latest_message.adapter = adapter
        recyclerview_latest_message.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL
        ))
        adapter.setOnItemClickListener { item, view ->

            var row_item = item as LatestMessageItem

            var intent = Intent(this,ChatLogActivity::class.java)
            intent.putExtra(USER_KEY,row_item.userRow?.username)
            intent.putExtra(USER_ID,row_item.userRow?.Uid)
            intent.putExtra(USER_IMAGE,row_item.userRow?.imageProfileUri)

            startActivity(intent)
        }

        lestenToLatestMessages()
        checkIfUserLoged()
        getCureentUser()

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.new_message ->{
                var intent = Intent(this,NewMessageActivity::class.java)
                startActivity(intent)
            }

            R.id.sign_out ->{
                FirebaseAuth.getInstance().signOut()
                var intent = Intent(this@LatestMessagesActivity,RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun checkIfUserLoged(){
        if(FirebaseAuth.getInstance().uid == null){
            var intent = Intent(this,RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun getCureentUser(){
        // get uid of the current loged user
        var uid = FirebaseAuth.getInstance().uid
        var ref = FirebaseDatabase.getInstance().getReference("users/$uid")
        ref.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(RegisterActivity.User::class.java)
            }

        })
    }

    private fun refreshRecyclerView(){
        adapter.clear()
        lastMessageMap.values.forEach {
            adapter.add(LatestMessageItem(it))
        }
    }

    private fun lestenToLatestMessages(){
        var fromid = FirebaseAuth.getInstance().uid
        var ref = FirebaseDatabase.getInstance().getReference("latest-messages/$fromid")

        ref.addChildEventListener(object:ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                var latestmessage = p0.getValue(ChatMessage::class.java)
                lastMessageMap[p0.key!!] = latestmessage!!
                refreshRecyclerView()
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                var latestmessage = p0.getValue(ChatMessage::class.java)
                lastMessageMap[p0.key!!] = latestmessage!!
                refreshRecyclerView()
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })
    }

}

class LatestMessageItem(var chat:ChatMessage):Item<ViewHolder>(){

    var userRow:RegisterActivity.User? = null

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        var id:String? = null

        if(chat.fromId == FirebaseAuth.getInstance().uid){
            id = chat.toId
        }else{
            id = chat.fromId
        }

        viewHolder.itemView.latest_message_textview.text = chat.text
        var refToUser = FirebaseDatabase.getInstance().getReference("users/" +
                "$id")

        refToUser.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                userRow = p0.getValue(RegisterActivity.User::class.java)
                Picasso.get().load(userRow!!.imageProfileUri).into(viewHolder.itemView.image_profile_latest_message)
                viewHolder.itemView.username_latest_message.text = userRow!!.username

            }

        })


    }

}
