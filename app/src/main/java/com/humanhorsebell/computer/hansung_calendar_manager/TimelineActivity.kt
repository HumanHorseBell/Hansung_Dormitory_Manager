package com.humanhorsebell.computer.hansung_calendar_manager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_timeline.*

class TimelineActivity : AppCompatActivity() {
    val groupSchedules = HashMap<String, ArrayList<Schedule>>()
    val allSchedules = ArrayList<Schedule>()
    lateinit var userNumber : String
    var userGroup: String? = null
    lateinit var listAdapter : ListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)
        title = "Timeline"
        //userNumber = intent.getStringExtra("userNo")
        //userGroup = intent.getStringExtra("groupNo")
        userNumber = "3" //삭제
        //userGroup = "0" //삭제

        listAdapter = ListAdapter(applicationContext, allSchedules)
        listView.adapter = listAdapter

        switchActivity(applicationContext, linearLayout)

        var firebaseDatabase = FirebaseDatabase.getInstance().reference

        //파이어베이스에서 데이터 불러오기
        val singleGroupScheduleListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val grpNumber = dataSnapshot.key.toString()
                var groupNo: String
                if(userGroup == null) {
                    groupNo = dataSnapshot.child("grpName").value.toString()
                }
                else{
                    groupNo = ""
                    title = dataSnapshot.child("grpName").value.toString() + " Timeline"
                }
                val grpName = groupNo
                val children = dataSnapshot.child("schedule").children
                val schedulesInDay = ArrayList<Schedule>()

                for(child in children){
                    val grandChildren = child.children
                    schedulesInDay.clear()
                    Log.d("checkFirebase", child.key.toString())
                    for(grandChild in grandChildren){
                        val startDay = grandChild.child("startDate").value.toString()
                        val endDay = grandChild.child("endDate").value.toString()
                        val startTime = grandChild.child("startTime").value.toString()
                        val endTime = grandChild.child("endTime").value.toString()
                        val name = grandChild.child("name").value.toString()
                        val comments = grandChild.child("comment").children
                        var picture : String? = null
                        for(comment in comments){
                            if(comment.hasChild("picture")) {
                                picture = comment.child("picture").value.toString()
                                break
                            }
                        }
                        schedulesInDay.add(Schedule(grpName, Date(startDay, startTime), Date(endDay, endTime), picture, name))
                    }
                    groupSchedules.put(grpNumber, schedulesInDay)
                    allSchedules.addAll(schedulesInDay)
                }
                allSchedules.sort()
                Log.d("checkFirebaseList", allSchedules.toString())
                listAdapter.notifyDataSetChanged()
                listAdapter.notifyDataSetInvalidated()

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }

        if(userGroup == null) {
            val singleUserGroupListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val children = dataSnapshot.children

                    for (child in children) {
                        if (child.key.equals(userNumber)) {
                            val groups = child.child("group").children

                            groups.forEach {
                                if (it.value == "true")
                                    firebaseDatabase.child("group").child(it.key.toString()).addListenerForSingleValueEvent(singleGroupScheduleListener)
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            }
            firebaseDatabase.child("user").addListenerForSingleValueEvent(singleUserGroupListener)
        }else{
            val groupNo = userGroup!!
            firebaseDatabase.child("group").child(groupNo).addListenerForSingleValueEvent(singleGroupScheduleListener)
        }

        val itemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            var grpNo : String = ""
            for(groupSchedule in groupSchedules){
                if(groupSchedule.value[0].grpName == allSchedules[position].grpName){
                    grpNo = groupSchedule.key
                    //setAlarm(applicationContext, allSchedules[position])
                }
            }
            val nextIntent = Intent(this, this.javaClass) //수정
            nextIntent.putExtra("userGroup", grpNo)
        }
        listView.onItemClickListener = itemClickListener
    }
}
