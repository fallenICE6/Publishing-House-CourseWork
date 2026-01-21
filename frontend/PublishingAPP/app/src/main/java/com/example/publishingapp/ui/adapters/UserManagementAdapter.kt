package com.example.publishingapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.R
import com.example.publishingapp.data.network.UserDto
import com.google.android.material.chip.Chip

class UserManagementAdapter(
    private val onUserClick: (UserDto) -> Unit,
    private var isLoading: Boolean = false
) : ListAdapter<UserDto, RecyclerView.ViewHolder>(UserDiffCallback()) {

    companion object {
        private const val TYPE_USER = 0
        private const val TYPE_LOADING = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == currentList.size) TYPE_LOADING else TYPE_USER
    }

    override fun getItemCount(): Int {
        return currentList.size + if (isLoading) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_USER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_user_admin, parent, false)
                UserViewHolder(view, onUserClick)
            }
            TYPE_LOADING -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loading, parent, false)
                LoadingViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserViewHolder -> {
                val user = getItem(position)
                holder.bind(user)
            }
            is LoadingViewHolder -> {
            }
        }
    }

    fun setLoading(loading: Boolean) {
        val previousState = isLoading
        isLoading = loading

        if (previousState != loading) {
            if (loading) {
                notifyItemInserted(currentList.size)
            } else {
                notifyItemRemoved(currentList.size)
            }
        }
    }

    class UserViewHolder(
        itemView: View,
        private val onUserClick: (UserDto) -> Unit // ФИКС: принимаем параметр
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val tvFullName: TextView = itemView.findViewById(R.id.tvFullName)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        private val tvPhone: TextView = itemView.findViewById(R.id.tvPhone)
        private val chipRole: Chip = itemView.findViewById(R.id.chipRole)

        private var currentUser: UserDto? = null

        init {
            // Делаем всю карточку кликабельной
            itemView.setOnClickListener {
                currentUser?.let { user -> onUserClick(user) }
            }
            itemView.findViewById<View>(R.id.tapHintContainer)?.setOnClickListener {
                currentUser?.let { user -> onUserClick(user) }
            }
        }

        fun bind(user: UserDto) {
            currentUser = user

            tvUsername.text = "@${user.username}"
            tvFullName.text = "${user.lastName} ${user.firstName} ${user.middleName ?: ""}"
            tvEmail.text = user.email ?: "—"
            tvPhone.text = user.phone

            when (user.role) {
                "ADMIN" -> {
                    chipRole.text = "Администратор"
                    chipRole.setChipBackgroundColorResource(R.color.purple)
                }
                "AUTHOR" -> {
                    chipRole.text = "Автор"
                    chipRole.setChipBackgroundColorResource(R.color.blue)
                }
                "REVIEWER" -> {
                    chipRole.text = "Рецензент"
                    chipRole.setChipBackgroundColorResource(R.color.green)
                }
                else -> {
                    chipRole.text = user.role
                    chipRole.setChipBackgroundColorResource(R.color.gray)
                }
            }
        }
    }

    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class UserDiffCallback : DiffUtil.ItemCallback<UserDto>() {
        override fun areItemsTheSame(oldItem: UserDto, newItem: UserDto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UserDto, newItem: UserDto): Boolean {
            return oldItem == newItem
        }
    }
}