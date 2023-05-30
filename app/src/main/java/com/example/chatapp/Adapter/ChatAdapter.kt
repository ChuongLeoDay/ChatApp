import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.chatapp.Model.messageModel
import com.example.chatapp.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import jp.wasabeef.glide.transformations.RoundedCornersTransformation



class ChatAdapter(var message: ArrayList<messageModel>, val context: Context) : RecyclerView.Adapter<ChatAdapter.MyViewHolder>() {

    private val auth = Firebase.auth
    private val fbFireStore = FirebaseFirestore.getInstance()
    private val MESSAGE_TYPE_LEFT = 0
    private val MESSAGE_TYPE_RIGHT = 1
    private val requestOptions = RequestOptions()
        .override(120,120)
        .centerCrop()
        .placeholder(R.drawable.asset_11)
        .error(R.drawable.danger_triangle_alert_figma)

    private val requestOptions2 = RequestOptions()
        .transform(RoundedCornersTransformation(32,0)) // Đặt bán kính bo tròn ở đây, ví dụ 8
        .placeholder(R.drawable.asset_11)
        .error(R.drawable.danger_triangle_alert_figma)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = if (viewType == MESSAGE_TYPE_RIGHT) {
            LayoutInflater.from(parent.context).inflate(R.layout.item_home_chat, parent, false)
        } else {
            LayoutInflater.from(parent.context).inflate(R.layout.item_away_chat_room_meesage, parent, false)
        }
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return message.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (message[position].idUserSend == auth.currentUser?.uid) {
            MESSAGE_TYPE_RIGHT
        } else {
            MESSAGE_TYPE_LEFT
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val infoMess = message[position]
        val chat = infoMess.content

        if (holder.itemViewType == MESSAGE_TYPE_RIGHT) {
            if(infoMess.type == "text") {
                holder.textChatAway?.text = chat
                if (infoMess.isLastSend == "true_${infoMess.idUserSend}") {
                    fbFireStore.collection("users")
                        .whereEqualTo("uid", infoMess.idUserSend)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                for (doc in documents) {
                                    Glide.with(context)
                                        .load(doc.getString("urlImage"))
                                        .apply(requestOptions)
                                        .into(holder.imageChatAway)
                                }
                            }
                        }
                }
                else {
                    holder.cardViewAwayChat.visibility = View.INVISIBLE
                }
            }
            else if(infoMess.type == "image") {
                holder.textChatAway.visibility = View.GONE
                holder.imageSendImage.visibility = View.VISIBLE
                if (infoMess.isLastSend == "true_${infoMess.idUserSend}") {
                    fbFireStore.collection("users")
                        .whereEqualTo("uid", infoMess.idUserSend)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                for (doc in documents) {
                                    Glide.with(context)
                                        .load(doc.getString("urlImage"))
                                        .apply(requestOptions)
                                        .into(holder.imageChatAway)
                                }
                            }
                        }
                }
                else {
                    holder.cardViewAwayChat.visibility = View.INVISIBLE
                }
                Glide.with(context)
                    .load(chat)
                    .apply(requestOptions2)
                    .into(holder.imageSendImage)
            }
        } else {
            if (infoMess.type == "text") {
                holder.textChatAway?.text = chat
                if (infoMess.isLastSend == "true_${infoMess.idUserSend}") {
                    fbFireStore.collection("users")
                        .whereEqualTo("uid", infoMess.idUserSend)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                for (doc in documents) {
                                    Glide.with(context)
                                        .load(doc.getString("urlImage"))
                                        .apply(requestOptions)
                                        .into(holder.imageChatAway)
                                }
                            }
                        }
                }
                else {
                    holder.cardViewAwayChat.visibility = View.INVISIBLE
                }
            }
            else if(infoMess.type == "image") {
                holder.textChatAway.visibility = View.GONE
                holder.imageSendImage.visibility = View.VISIBLE
                if (infoMess.isLastSend == "true_${infoMess.idUserSend}") {
                    fbFireStore.collection("users")
                        .whereEqualTo("uid", infoMess.idUserSend)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                for (doc in documents) {
                                    Glide.with(context)
                                        .load(doc.getString("urlImage"))
                                        .apply(requestOptions)
                                        .into(holder.imageChatAway)
                                }
                            }
                        }
                }
                else {
                    holder.cardViewAwayChat.visibility = View.INVISIBLE
                }
                Glide.with(context)
                    .load(chat)
                    .apply(requestOptions2)
                    .into(holder.imageSendImage)
            }
        }
    }


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textChatAway: TextView = itemView.findViewById(R.id.content_mess)
        val imageChatAway: ImageView = itemView.findViewById(R.id.imageUser_content)
        val cardViewAwayChat : CardView = itemView.findViewById(R.id.card_view_image_away_chat_room)
        val imageSendImage : ImageView = itemView.findViewById(R.id.imageView_content_mess_send)
//        val cardViewImage : CardView = itemView.findViewById(R.id.card_view_image_send_image)
    }
}
