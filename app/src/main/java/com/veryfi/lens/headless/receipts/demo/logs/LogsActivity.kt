package com.veryfi.lens.headless.receipts.demo.logs

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lriccardo.timelineview.TimelineDecorator
import com.veryfi.lens.headless.receipts.demo.R
import com.veryfi.lens.headless.receipts.demo.databinding.ActivityLogsBinding
import com.veryfi.lens.headless.receipts.demo.helpers.ThemeHelper
import org.json.JSONObject

class LogsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogsBinding
    private lateinit var adapter: LogsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applicationContext?.let { ThemeHelper.setSecondaryColorToStatusBar(this, it) }
        setUpToolBar()
        loadData()
    }

    private fun setUpToolBar() {
        setSupportActionBar(binding.topAppBar)
        binding.topAppBar.setNavigationIcon(R.drawable.ic_vector_close_shape)
        binding.topAppBar.setNavigationOnClickListener { finish() }
    }

    private fun loadData() {
        binding.timelineRv.let {
            it.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            adapter = LogsAdapter()
            it.adapter = adapter

            val colorPrimary = ThemeHelper.getPrimaryColor(this)
            it.addItemDecoration(
                TimelineDecorator(
                    position = TimelineDecorator.Position.Left,
                    indicatorColor = colorPrimary,
                    lineColor = colorPrimary
                )
            )

            it.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    (it.layoutManager as? LinearLayoutManager)?.let {

                    }
                }
            })
            showLogs()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showLogs() {
        val log = Log()
        val jsonString = intent.extras?.getSerializable(DATA) as String
        val json = JSONObject(jsonString)
        log.message = json
        log.title = resources.getString(R.string.logs_result)
        adapter.addItem(log)
        adapter.notifyDataSetChanged()
        binding.timelineRv.scrollToPosition(adapter.itemCount - 1)
    }

    companion object {
        private const val DATA = "data"
    }
}