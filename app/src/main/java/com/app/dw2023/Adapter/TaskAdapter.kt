package com.app.dw2023.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.app.dw2023.Global.AppData
import com.app.dw2023.Global.ImagesMap
import com.app.dw2023.Model.Task
import com.app.dw2023.R

class TaskAdapter(var tasksArray: ArrayList<Task>, var context: Context) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var taskButton : AppCompatButton = itemView.findViewById(R.id.tasksCardPointsButton)
        var taskCardImageView : ImageView = itemView.findViewById(R.id.tasksCardImageView)
        var taskTitle : TextView = itemView.findViewById(R.id.tasksTitleTextView)
        var taskTips : TextView = itemView.findViewById(R.id.tasksTipsTextView)
        val taskNumber : TextView = itemView.findViewById(R.id.tasksNumberTextView)
        val taskConstraintLayout : ConstraintLayout = itemView.findViewById(R.id.tasksConstraintLayout)
        val taskQRCodeImageView : ImageView = itemView.findViewById(R.id.tasksQRCodeImageView)
        val taskCardView : CardView = itemView.findViewById(R.id.taskCardView)
        val taskQRCodeTextView : TextView = itemView.findViewById(R.id.tasksQRCodeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.task_card_design, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasksArray[position]
        val points = "${task.points} points"
        val taskNumberText = "Task ${task.taskNumber}"

        holder.taskButton.text = points
        holder.taskNumber.text = taskNumberText
        holder.taskTitle.text = task.title
        holder.taskTips.text = task.description
        holder.taskConstraintLayout.alpha = 1F
        holder.taskQRCodeImageView.setImageResource(R.drawable.tasks_qr_code)
        val taskAvailable = "Scan QR code to complete task"
        holder.taskQRCodeTextView.text = taskAvailable

        if (AppData.loadedQrCodes.contains(task.qrCode)) {
            holder.taskConstraintLayout.alpha = 0.4F
            holder.taskQRCodeImageView.setImageResource(R.drawable.tasks_completed)
            val taskCompletedText = "Task completed!"
            holder.taskQRCodeTextView.text = taskCompletedText
        }

        if (task.title == "Good job :)") {
            holder.taskButton.alpha = 0.0F
            holder.taskQRCodeTextView.alpha = 0.0F
            val congrats = "Congratulations!"
            holder.taskNumber.text = congrats
            holder.taskNumber.textSize = 16F
            holder.taskTitle.textSize = 14F
            holder.taskTips.textSize = 12F
            holder.taskQRCodeImageView.setImageResource(R.drawable.tasks_completed)
        }

        if (task.title == "placeholderTask") {
            holder.taskCardView.alpha = 0.0F
        }

        val imageSource = task.imageSource
        val drawableId = ImagesMap.imagesMap[imageSource] ?: R.drawable.taskphoto1
        holder.taskCardImageView.setImageResource(drawableId)

        holder.taskButton.isClickable = false

        holder.taskCardView.startAnimation(AnimationUtils.loadAnimation(holder.itemView.context, R.anim.item_anim))

    }

    override fun getItemCount(): Int {
        return tasksArray.size
    }

}