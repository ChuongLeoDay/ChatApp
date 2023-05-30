package com.example.chatapp.Fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Adapter.listMessAdapter
import com.example.chatapp.Model.listMessModel
import com.example.chatapp.Model.messageModel
import com.example.chatapp.databinding.FragmentMessageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MessageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MessageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentMessageBinding
    private lateinit var auth : FirebaseAuth
    private var listUserAdapter = listMessAdapter(ArrayList())
    private val dbFireStore = Firebase.firestore
    private val rtDb = Firebase.database

    private var uidUser: String? = null
    private var imageUser: String? = null
    private var nameUser: String? = null
    private var lastMess: String? = null
    private var timeHour: String? = null
    private var timeDay: String? = null
    private var typeMess : String? = null
    private var textIsCheckRead: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMessageBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        initUI()
        return binding.root
    }

    private fun initUI() {
        val rvList : RecyclerView = binding.rvListUser
        rvList.layoutManager = LinearLayoutManager(context)
        rvList.apply {
            adapter = listUserAdapter
        }
        checkMessageList()
    }

    @SuppressLint("SuspiciousIndentation")
    private fun checkMessageList() {
        val search = dbFireStore.collection("RoomChat").whereEqualTo("idUser1", auth.currentUser?.uid)
            search.get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val idRoom = document.get("idRoomChat")
                        val listUserFinal = ArrayList<listMessModel>()
                        val idU2 = document.get("idUser2") as String

                        val queryFindUser = dbFireStore.collection("users")
                            .whereEqualTo("uid", idU2)
                        queryFindUser.get()
                            .addOnSuccessListener {
                                    infoUsers ->
                                if (!infoUsers.isEmpty) {
                                    for (value in infoUsers) {
                                        uidUser = value.getString("uid")
                                        imageUser = value.getString("urlImage")
                                        nameUser = value.getString("name")
                                    }
                                }
                                val messageRef = rtDb.getReference("Messages").child(idRoom as String)
                                messageRef
                                    .orderByKey()
                                    .limitToLast(15)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                            val messages = mutableListOf<messageModel>()

                                            // Kiểm tra xem có tồn tại tin nhắn trong node không
                                            if (dataSnapshot.exists()) {
                                                // Lặp qua danh sách 15 tin nhắn gần nhất (đã được sắp xếp theo thứ tự ngược)
                                                for (messageSnapshot in dataSnapshot.children.reversed()) {
                                                    val messageId = messageSnapshot.key
                                                    val message = messageSnapshot.getValue(messageModel::class.java)

                                                    if (messageId != null && message != null) {
                                                        messages.add(message)
                                                    }
                                                }

                                                // Kiểm tra nếu danh sách tin nhắn không rỗng
                                                if (messages.isNotEmpty()) {
                                                    val latestMessage = messages.first()

                                                    lastMess = latestMessage.content
                                                    timeHour = latestMessage.timeHour
                                                    timeDay = latestMessage.timedate
                                                    typeMess = latestMessage.type
                                                    var count  = 0
                                                    for (mes in messages) {
                                                        if(mes.isRead == "false_${idU2}") {
                                                            count += 1
                                                        }
                                                    }
                                                    textIsCheckRead = count
                                                    val dataUserFinal = listMessModel(uidUser, imageUser, nameUser, lastMess,typeMess,timeHour, timeDay,
                                                        textIsCheckRead!!
                                                    )
                                                    listUserFinal.add(dataUserFinal)
                                                    listUserAdapter.apply {
                                                        this.listUser = listUserFinal
                                                        notifyDataSetChanged()
                                                    }

                                                } else {
                                                    // Node không có tin nhắn
                                                    // Xử lý trường hợp không có tin nhắn theo yêu cầu
                                                }
                                            } else {
                                                // Node không có tin nhắn
                                                // Xử lý trường hợp không có tin nhắn theo yêu cầu
                                            }
                                        }

                                        override fun onCancelled(databaseError: DatabaseError) {
                                            // Xử lý lỗi nếu có
                                        }
                                    })
                            }
                    }
                }

        val search2 = dbFireStore.collection("RoomChat").whereEqualTo("idUser2", auth.currentUser?.uid)
        search2.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val idRoom = document.get("idRoomChat")
                    val listUserFinal = ArrayList<listMessModel>()
                    val idU1 = document.get("idUser1") as String


                    val queryFindUser = dbFireStore.collection("users")
                        .whereEqualTo("uid", idU1)
                    queryFindUser.get()
                        .addOnSuccessListener {
                                infoUsers ->
                            if (!infoUsers.isEmpty) {
                                for (value in infoUsers) {
                                    uidUser = value.getString("uid")
                                    imageUser = value.getString("urlImage")
                                    nameUser = value.getString("name")
                                }
                            }
                            val messageRef = rtDb.getReference("Messages").child(idRoom as String)
                            messageRef
                                .orderByKey()
                                .limitToLast(15)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        val messages = mutableListOf<messageModel>()

                                        // Kiểm tra xem có tồn tại tin nhắn trong node không
                                        if (dataSnapshot.exists()) {
                                            // Lặp qua danh sách 15 tin nhắn gần nhất (đã được sắp xếp theo thứ tự ngược)
                                            for (messageSnapshot in dataSnapshot.children.reversed()) {
                                                val messageId = messageSnapshot.key
                                                val message = messageSnapshot.getValue(messageModel::class.java)

                                                if (messageId != null && message != null) {
                                                    messages.add(message)
                                                }
                                            }

                                            // Kiểm tra nếu danh sách tin nhắn không rỗng
                                            if (messages.isNotEmpty()) {
                                                val latestMessage = messages.first()

                                                lastMess = latestMessage.content
                                                timeHour = latestMessage.timeHour
                                                timeDay = latestMessage.timedate
                                                typeMess = latestMessage.type
                                                var count  = 0
                                                for (mes in messages) {
                                                    if(mes.isRead == "false_${idU1}") {
                                                        count += 1
                                                    }
                                                }
                                                textIsCheckRead = count
                                                val dataUserFinal = listMessModel(uidUser, imageUser, nameUser, lastMess,typeMess,timeHour, timeDay,
                                                    textIsCheckRead!!
                                                )
                                                listUserFinal.add(dataUserFinal)
                                                listUserAdapter.apply {
                                                    this.listUser = listUserFinal
                                                    notifyDataSetChanged()
                                                }

                                            } else {
                                                // Node không có tin nhắn
                                                // Xử lý trường hợp không có tin nhắn theo yêu cầu
                                            }
                                        } else {
                                            // Node không có tin nhắn
                                            // Xử lý trường hợp không có tin nhắn theo yêu cầu
                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {
                                        // Xử lý lỗi nếu có
                                    }
                                })
                        }
                }
            }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MessageFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MessageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}