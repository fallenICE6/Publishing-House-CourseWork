package com.example.publishingapp.ui.fragments

import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.publishingapp.R
import com.example.publishingapp.ui.activities.MainActivity
import com.example.publishingapp.ui.adapters.SliderDotsAdapter
import com.example.publishingapp.ui.adapters.WorksSliderAdapter
import com.google.android.material.button.MaterialButton
import kotlin.math.abs

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var autoScrollRunnable: Runnable

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ===================== WORKS SLIDER =====================
        val viewPager = view.findViewById<ViewPager2>(R.id.viewPagerWorks)
        val dotsRv = view.findViewById<RecyclerView>(R.id.sliderDots)

        val images = listOf(
            R.drawable.swiper_1,
            R.drawable.swiper_2,
            R.drawable.swiper_3,
            R.drawable.swiper_4
        )

        viewPager.adapter = WorksSliderAdapter(images)
        viewPager.offscreenPageLimit = 3

        viewPager.setPageTransformer { page, position ->
            val scale = 0.85f + (1 - abs(position)) * 0.15f
            page.scaleX = scale
            page.scaleY = scale
            page.alpha = 0.7f + (1 - abs(position)) * 0.3f
        }

        // ===================== DOTS =====================
        val dotsAdapter = SliderDotsAdapter(images.size) { index ->
            viewPager.setCurrentItem(index, true)
        }

        dotsRv.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        dotsRv.adapter = dotsAdapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                dotsAdapter.selected = position
                dotsAdapter.notifyDataSetChanged()
            }
        })

        // ===================== AUTO SCROLL =====================
        autoScrollRunnable = object : Runnable {
            override fun run() {
                val next = (viewPager.currentItem + 1) % images.size
                viewPager.setCurrentItem(next, true)
                handler.postDelayed(this, 9000)
            }
        }
        handler.postDelayed(autoScrollRunnable, 9000)

        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                handler.removeCallbacks(autoScrollRunnable)
            }

            override fun onResume(owner: LifecycleOwner) {
                handler.postDelayed(autoScrollRunnable, 4000)
            }
        })

        // ===================== NAVIGATION =====================
        view.findViewById<MaterialButton>(R.id.btnApply).setOnClickListener {
            (requireActivity() as MainActivity).openCatalog()
        }

        view.findViewById<TextView>(R.id.btnAllWorks).setOnClickListener {
            (requireActivity() as MainActivity).openWorks()
        }

        // ===================== SCROLL ANIMATIONS =====================
        val scroll = view.findViewById<NestedScrollView>(R.id.homeScroll)
        val animatedViews = listOf(
            view.findViewById<View>(R.id.brick1),
            view.findViewById<View>(R.id.brick2),
            view.findViewById<View>(R.id.brick3),
            view.findViewById<View>(R.id.worksCard)
        )

        val animated = BooleanArray(animatedViews.size)
        animatedViews.forEach { v ->
            v.alpha = 0f
            v.translationY = 80f
        }

        scroll.viewTreeObserver.addOnScrollChangedListener {
            animatedViews.forEachIndexed { index, v ->
                if (!animated[index] && isVisible(v, scroll)) {
                    animated[index] = true
                    v.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(400)
                        .setStartDelay(index * 80L)
                        .start()
                }
            }
        }
    }

    private fun isVisible(view: View, scroll: NestedScrollView): Boolean {
        val rect = Rect()
        scroll.getHitRect(rect)
        return view.getLocalVisibleRect(rect)
    }
}
