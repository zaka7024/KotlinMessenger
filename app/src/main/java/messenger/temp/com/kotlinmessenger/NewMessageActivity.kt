package messenger.temp.com.kotlinmessenger

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_item.view.*

class NewMessageActivity : AppCompatActivity() {

    companion object {
        val USER_KEY = "USER_KEY"
        val USER_ID = "USER_ID"
        val USER_IMAGE = "USER_IMAGE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title = "Select User"

        getUser()
    }

    private fun getUser(){
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                var adpater = GroupAdapter<ViewHolder>()
                if(p0.exists())
                    p0.children.forEach {
                        var user = it.getValue(RegisterActivity.User::class.java)
                        adpater.add(UserItem(user!!))
                    }

                recyclerView_newMessage.adapter = adpater

                adpater.setOnItemClickListener { item, view ->

                    var userItem = item as UserItem

                    var intent = Intent(this@NewMessageActivity,ChatLogActivity::class.java)
                    intent.putExtra(USER_KEY,userItem.user.username)
                    intent.putExtra(USER_ID,userItem.user.Uid)
                    intent.putExtra(USER_IMAGE,userItem.user.imageProfileUri)
                    startActivity(intent)
                    this@NewMessageActivity.finish()
                }
            }

        })
    }
}

class UserItem(var user:RegisterActivity.User): Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.user_row_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.username_textView.text = user.username
        Picasso.get().load(user.imageProfileUri).into(viewHolder.itemView.userprofile_imageView)
    }

}
