package com.app.dw2023.Fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.dw2023.Activity.MainActivity
import com.app.dw2023.Adapter.TaskAdapter
import com.app.dw2023.Global.*
import com.app.dw2023.Model.Event
import com.app.dw2023.Model.Task
import com.app.dw2023.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    private lateinit var scrollView: ScrollView
    private lateinit var db: FirebaseFirestore

    private lateinit var homeEventCompany: TextView
    private lateinit var homeEventDesc: TextView
    private lateinit var homeEventDate: TextView
    private lateinit var homeEventSignUpButton: AppCompatButton
    private lateinit var homeEventImageView: ImageView
    private lateinit var seeAllEventTextView: TextView
    private lateinit var seeAllEventArrow: ImageView
    private lateinit var seeAllTaskTextView: TextView
    private lateinit var seeAllTaskArrow: ImageView

    lateinit var tasksNotDone: ArrayList<Task>

    private lateinit var horizontalRecyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        AppData.lastSelectedIndex = HOME_FRAGMENT_INDEX
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        scrollView = view.findViewById(R.id.homeScrollView)
        homeEventCompany = view.findViewById(R.id.homeEventCardTitle)
        homeEventDesc = view.findViewById(R.id.homeEventCardDesc)
        homeEventDate = view.findViewById(R.id.homeEventCardDate)
        homeEventSignUpButton = view.findViewById(R.id.homeEventCardSignUpButton)
        homeEventImageView = view.findViewById(R.id.homeEventCardImageView)
        seeAllEventTextView = view.findViewById(R.id.seeAllEventTextView)
        seeAllEventArrow = view.findViewById(R.id.seeAllEventArrow)
        seeAllTaskTextView = view.findViewById(R.id.seeAllTasksTextView)
        seeAllTaskArrow = view.findViewById(R.id.seeAllTasksArrow)

        tasksNotDone = ArrayList<Task>()

        hideNotLoaded()
        setSeeAllOnClickListeners()

        horizontalRecyclerView = view.findViewById(R.id.homeTasksRecyclerView)
        horizontalRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        adapter = TaskAdapter(tasksNotDone, requireContext())
        horizontalRecyclerView.adapter = adapter

        scrollView.isVerticalScrollBarEnabled = false
        homeEventSignUpButton.setOnClickListener {
            val uri = Uri.parse("https://weeia.p.lodz.pl/")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        eventChangeListener()
        tasksChangeListener()

        return view
    }

    private fun eventChangeListener() {

        AppData.eventList.clear()

        db = FirebaseFirestore.getInstance()
        db.collection("lectures").whereGreaterThan("timeEnd", Timestamp.now())
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {

                    if (error != null) {
                        Log.e(LOG_MESSAGE, error.toString())
                        return
                    }

                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            val event = dc.document.toObject(Event::class.java)
                            AppData.eventList.add(event)
                        }
                    }
                    keepOnlyUniqueEvents()
                }
            })

        db.collection("workshops").whereGreaterThan("timeEnd", Timestamp.now())
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {

                    if (error != null) {
                        Log.e(LOG_MESSAGE, error.toString())
                        return
                    }

                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            val event = dc.document.toObject(Event::class.java)
                            event.eventType = "workshops"
                            AppData.eventList.add(event)
                        }
                    }

                    keepOnlyUniqueEvents()
                    setUpcomingEvent()
                }
            })
    }

    private fun tasksChangeListener() {

        AppData.tasksList.clear()

        db = FirebaseFirestore.getInstance()
        db.collection("tasks")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {

                    if (error != null) {
                        Log.e(LOG_MESSAGE, error.toString())
                        return
                    }

                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            val task = dc.document.toObject(Task::class.java)
                            AppData.tasksList.add(task)
                        }
                    }

                    AppData.tasksList.filter { it.qrCode in AppData.loadedQrCodes }.forEach { it.isDone = true }
                    keepOnlyUniqueTasks()
                    AppData.tasksList.sortBy { it.isDone }

                    tasksNotDone.clear()
                    tasksNotDone.addAll(AppData.tasksList.filter { !it.isDone })
                    
                    adapter.notifyDataSetChanged()
                }
            })
    }

    private fun setUpcomingEvent() {
        if (AppData.eventList.isEmpty()) { return }

        AppData.eventList.sortWith(compareBy({it.timeStart}, {it.timeEnd}))

        val upcomingEvent = AppData.eventList.first()
        val type = if (upcomingEvent.eventType == "lecture") {
            "Lecture"
        } else "Workshop"
        homeEventCompany.text = type
        homeEventDesc.text = upcomingEvent.title

        val imageSource = upcomingEvent.imageSource
        val drawableId = ImagesMap.imagesMap[imageSource] ?: R.drawable.event_card_background
        homeEventImageView.setImageResource(drawableId)

        val dateStart = upcomingEvent.timeStart?.toDate()
        val dateEnd = upcomingEvent.timeEnd?.toDate()

        val sdfStart = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        sdfStart.timeZone = TimeZone.getTimeZone("GMT+1:00")
        val sdfEnd = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdfEnd.timeZone = TimeZone.getTimeZone("GMT+1:00")

        val date = "${sdfStart.format(dateStart!!)} - ${sdfEnd.format(dateEnd!!)}"
        homeEventDate.text = date

        showLoaded()
    }

    private fun setSeeAllOnClickListeners() {
        seeAllEventTextView.setOnClickListener {
            (activity as MainActivity).mainBinding.bottomNavView.selectedItemId = (activity as MainActivity).mainBinding.bottomNavView.menu.getItem(
                EVENTS_FRAGMENT_INDEX).itemId
        }
        seeAllEventArrow.setOnClickListener {
            (activity as MainActivity).mainBinding.bottomNavView.selectedItemId = (activity as MainActivity).mainBinding.bottomNavView.menu.getItem(
                EVENTS_FRAGMENT_INDEX).itemId
        }
        seeAllTaskTextView.setOnClickListener {
            (activity as MainActivity).mainBinding.bottomNavView.selectedItemId = (activity as MainActivity).mainBinding.bottomNavView.menu.getItem(
                TASKS_FRAGMENT_INDEX).itemId
        }
        seeAllTaskArrow.setOnClickListener {
            (activity as MainActivity).mainBinding.bottomNavView.selectedItemId = (activity as MainActivity).mainBinding.bottomNavView.menu.getItem(
                TASKS_FRAGMENT_INDEX).itemId
        }
    }

    private fun hideNotLoaded() {
        homeEventCompany.isInvisible = true
        homeEventDesc.isInvisible = true
        homeEventDate.isInvisible = true
        homeEventSignUpButton.isInvisible = true
        homeEventImageView.isInvisible = true
    }

    private fun showLoaded() {
        homeEventCompany.isInvisible = false
        homeEventDesc.isInvisible = false
        homeEventDate.isInvisible = false
        homeEventSignUpButton.isInvisible = false
        homeEventImageView.isInvisible = false
    }

    private fun keepOnlyUniqueTasks() {
        val set = mutableSetOf<Task>()
        for (task in AppData.tasksList) {
            set.add(task)
        }
        AppData.tasksList.clear()
        AppData.tasksList.addAll(set)
    }

    private fun keepOnlyUniqueEvents() {
        val set = mutableSetOf<Event>()
        for (task in AppData.eventList) {
            set.add(task)
        }
        AppData.eventList.clear()
        AppData.eventList.addAll(set)
    }

}