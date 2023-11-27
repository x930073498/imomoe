package com.skyd.imomoe.view.component.player

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.*
import android.view.View.OnClickListener
import android.widget.*
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shuyu.gsyvideoplayer.utils.CommonUtil
import com.shuyu.gsyvideoplayer.utils.Debuger
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import com.skyd.imomoe.App
import com.skyd.imomoe.R
import com.skyd.imomoe.bean.AnimeEpisodeDataBean
import com.skyd.imomoe.bean.BaseBean
import com.skyd.imomoe.util.Util.dp
import com.skyd.imomoe.util.Util.getResColor
import com.skyd.imomoe.util.Util.getResDrawable
import com.skyd.imomoe.util.Util.getScreenBrightness
import com.skyd.imomoe.util.Util.openVideoByExternalPlayer
import com.skyd.imomoe.util.Util.showToast
import com.skyd.imomoe.util.gone
import com.skyd.imomoe.util.invisible
import com.skyd.imomoe.util.visible
import com.skyd.imomoe.view.activity.DlnaActivity
import com.skyd.imomoe.view.adapter.SkinRvAdapter
import com.skyd.imomoe.view.component.ZoomView
import com.skyd.imomoe.view.component.textview.TypefaceTextView
import com.skyd.skin.SkinManager
import java.io.File
import java.io.Serializable
import kotlin.math.abs


open class AnimeVideoPlayer : StandardGSYVideoPlayer {
    companion object {
        val mScaleStrings = listOf(
            Pair("默认比例", GSYVideoType.SCREEN_TYPE_DEFAULT),
            Pair("16:9", GSYVideoType.SCREEN_TYPE_16_9),
            Pair("4:3", GSYVideoType.SCREEN_TYPE_4_3),
            Pair("全屏", GSYVideoType.SCREEN_TYPE_FULL),
            Pair("拉伸全屏", GSYVideoType.SCREEN_MATCH_FULL)
        )

        const val NO_REVERSE = 0
        const val HORIZONTAL_REVERSE = 1
        const val VERTICAL_REVERSE = 2
    }

    // 正在双指缩放移动
    private var doublePointerZoomingMoving = false

    private var mDownloadButton: ImageView? = null

    private var initFirstLoad = true

    //记住切换数据源类型
    private var mScaleIndex = 0

    //4:3  16:9等
    private var mMoreScaleTextView: TextView? = null

    //倍速按钮
    private var mSpeedTextView: TextView? = null
    private var mSpeedRecyclerView: RecyclerView? = null

    //速度
    private var mPlaySpeed = 1f

    //投屏按钮
    private var mClingImageView: ImageView? = null

    //分享按钮
    private var mShareImageView: ImageView? = null

    //更多按钮
    private var mMoreImageView: ImageView? = null

    //下一集按钮
    private var mNextImageView: ImageView? = null

    //选集
    private var mEpisodeTextView: TextView? = null
    private var mEpisodeTextViewVisibility: Int = View.VISIBLE
    private var mEpisodeButtonOnClickListener: OnClickListener? = null
    private var mEpisodeRecyclerView: RecyclerView? = null
    private var mEpisodeAdapter: EpisodeRecyclerViewAdapter? = null

    // 设置
    private var mSettingContainer: ViewGroup? = null
    private var mSettingImageView: ImageView? = null

    // 镜像RadioGroup
    private var mReverseRadioGroup: RadioGroup? = null
    private var mReverseValue: Int? = null
    private var mTextureViewTransform: Int =
        NO_REVERSE

    // 底部进度条CheckBox
    private var mBottomProgressCheckBox: CheckBox? = null
    private var mBottomProgressCheckBoxValue: Boolean = true

    //底部进度调
    private var mBottomProgress: ProgressBar? = null

    // 外部播放器打开
    private var mOpenByExternalPlayerTextView: TextView? = null

    // 硬解码CheckBox
    private var mMediaCodecCheckBox: CheckBox? = null

    // 右侧弹出栏
    private var mRightContainer: ViewGroup? = null

    // 按住高速播放的tv
    private var mTouchDownHighSpeedTextView: TextView? = null
    private var mLongPressing: Boolean = false

    // 还原屏幕
    private var mRestoreScreenTextView: TextView? = null

    // 屏幕已经双指放大移动了
    private var mDoublePointerZoomMoved: Boolean = false

    // 屏幕已经双指放大移动了
    private var mBiggerSurface: ViewGroup? = null

    // 控件没有显示
    private var mUiCleared: Boolean = true

    // 显示系统时间
    private var mSystemTimeTextView: TextView? = null

    // top阴影
    private var mViewTopContainerShadow: View? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, fullFlag: Boolean?) : super(context, fullFlag)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun getLayoutId() = if (mIfCurrentIsFullscreen)
        R.layout.layout_anime_video_player_land else R.layout.layout_anime_video_player

    @SuppressLint("ClickableViewAccessibility")
    override fun init(context: Context?) {
        super.init(context)

        mDownloadButton = findViewById(R.id.iv_play_activity_toolbar_download)
        mMoreScaleTextView = findViewById(R.id.tv_more_scale)
        mSpeedTextView = findViewById(R.id.tv_speed)
//        mClingImageView = findViewById(R.id.iv_cling)
        mRightContainer = findViewById(R.id.layout_right)
        mSpeedRecyclerView = findViewById(R.id.rv_right)
        mEpisodeRecyclerView = findViewById(R.id.rv_right)
        mShareImageView = findViewById(R.id.iv_play_activity_toolbar_share)
        mNextImageView = findViewById(R.id.iv_next)
        mEpisodeTextView = findViewById(R.id.tv_episode)
        mSettingImageView = findViewById(R.id.iv_setting)
        mSettingContainer = findViewById(R.id.layout_setting)
        mReverseRadioGroup = findViewById(R.id.rg_reverse)
        mBottomProgressCheckBox = findViewById(R.id.cb_bottom_progress)
        mBottomProgress = super.mBottomProgressBar
        mMoreImageView = findViewById(R.id.iv_play_activity_toolbar_more)
        mOpenByExternalPlayerTextView = findViewById(R.id.tv_open_by_external_player)
//        mMediaCodecCheckBox = findViewById(R.id.cb_media_codec)
        mRestoreScreenTextView = findViewById(R.id.tv_restore_screen)
        mTouchDownHighSpeedTextView = findViewById(R.id.tv_touch_down_high_speed)
        mBiggerSurface = findViewById(R.id.bigger_surface)
        mSystemTimeTextView = findViewById(R.id.tv_system_time)
        mViewTopContainerShadow = findViewById(R.id.view_top_container_shadow)

        mRightContainer?.gone()
        mSettingContainer?.gone()
        mTouchDownHighSpeedTextView?.gone()

        mBiggerSurface?.setOnClickListener(this)
        mBiggerSurface?.setOnTouchListener(this)

        mRestoreScreenTextView?.setOnClickListener {
            mTextureViewContainer?.run {
                if (this is ZoomView) restore()
                else {
                    translationX = 0f
                    translationY = 0f
                    scaleX = 1f
                    scaleY = 1f
                    rotation = 0f
                }
                mDoublePointerZoomMoved = false
                it.gone()
            }
        }
        mSpeedTextView?.setOnClickListener {
            mRightContainer?.let {
                val adapter = SpeedAdapter(
                    listOf(
                        SpeedBean("speed", "", "0.5"),
                        SpeedBean("speed", "", "0.75"),
                        SpeedBean("speed", "", "1"),
                        SpeedBean("speed", "", "1.25"),
                        SpeedBean("speed", "", "1.5"),
                        SpeedBean("speed", "", "2")
                    )
                )
                mSpeedRecyclerView?.layoutManager = LinearLayoutManager(context)
                mSpeedRecyclerView?.adapter = adapter
                adapter.notifyDataSetChanged()
            }
            showRightContainer()
        }
        mEpisodeTextView?.setOnClickListener {
            mRightContainer?.let {
                mEpisodeRecyclerView?.layoutManager = LinearLayoutManager(context)
                mEpisodeRecyclerView?.adapter = mEpisodeAdapter
                mEpisodeAdapter?.notifyDataSetChanged()
                mEpisodeRecyclerView?.scrollToPosition(mEpisodeAdapter?.currentIndex ?: 0)
            }
            showRightContainer()
        }
        mSettingImageView?.setOnClickListener { showSettingContainer() }
        mReverseValue = mReverseRadioGroup?.getChildAt(0)?.id
        mReverseRadioGroup?.children?.forEach {
            (it as RadioButton).apply {
                setOnCheckedChangeListener { buttonView, isChecked ->
                    if (!isChecked) return@setOnCheckedChangeListener
                    mReverseValue = id
                    when (id) {
                        R.id.rb_no_reverse -> resolveTransform(NO_REVERSE)
                        R.id.rb_horizontal_reverse -> resolveTransform(HORIZONTAL_REVERSE)
                        R.id.rb_vertical_reverse -> resolveTransform(VERTICAL_REVERSE)
                    }
                }
            }
        }
        mBottomProgressCheckBox?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                mBottomProgress?.let {
                    mBottomProgressBar = it
                    it.visible()
                }
            } else {
                mBottomProgressBar?.let {
                    mBottomProgress = it
                    it.gone()
                    mBottomProgressBar = null
                }
            }
            mBottomProgressCheckBoxValue = isChecked
        }
        mBottomProgressCheckBox?.isChecked = mBottomProgressBar != null

        //重置视频比例
        GSYVideoType.setShowType(mScaleStrings[mScaleIndex].second)
        changeTextureViewShowType()
        if (mTextureView != null) mTextureView.requestLayout()

        mMoreScaleTextView?.text = mScaleStrings[mScaleIndex].first

        //切换视频比例
        mMoreScaleTextView?.setOnClickListener(OnClickListener {
            startDismissControlViewTimer()      //重新开始ui消失时间计时
            if (!mHadPlay) {
                return@OnClickListener
            }
            mScaleIndex = (mScaleIndex + 1) % mScaleStrings.size
            resolveTypeUI()
        })

        mClingImageView?.setOnClickListener {
            mContext.startActivity(
                Intent(mContext, DlnaActivity::class.java)
                    .putExtra("url", mUrl)
                    .putExtra("title", mTitle)
            )
            mOriginUrl
        }

        mOpenByExternalPlayerTextView?.setOnClickListener {
            if (!openVideoByExternalPlayer(mContext, mUrl))
                mContext.getString(R.string.matched_app_not_found).showToast()
        }
    }

    fun getUrl(): String = mUrl

    fun getTitle(): String = mTitle

    private fun showSettingContainer() {
        mSettingContainer?.let {
            hideAllWidget()
            it.translationX = 150f.dp
            it.visible()
            val animator = ObjectAnimator.ofFloat(
                it, "translationX", 170f.dp, 0f
            )
            animator.duration = 300
            animator.start()
            //取消xx秒后隐藏控制界面
            cancelDismissControlViewTimer()
            if (mReverseValue == null) mReverseValue = mReverseRadioGroup?.getChildAt(0)?.id
            mReverseValue?.let { id -> findViewById<RadioButton>(id).isChecked = true }
            mBottomProgressCheckBox?.isChecked = mBottomProgressCheckBoxValue

//            mMediaCodecCheckBox?.isChecked = GSYVideoType.isMediaCodec()
//            mMediaCodecCheckBox?.setOnCheckedChangeListener { buttonView, isChecked ->
//                if (isChecked) GSYVideoType.enableMediaCodec()
//                else GSYVideoType.disableMediaCodec()
//                startPlayLogic()
//            }
        }
    }

    fun setTopContainer(top: ViewGroup) {
        mTopContainer = top
        restartTimerTask()
    }

    private fun showRightContainer() {
        mRightContainer?.let {
            hideAllWidget()
            it.translationX = 150f.dp
            it.visible()
            val animator = ObjectAnimator.ofFloat(it, "translationX", 170f.dp, 0f)
            animator.duration = 300
            animator.start()
            //取消xx秒后隐藏控制界面
            cancelDismissControlViewTimer()
        }
    }

    override fun hideAllWidget() {
        super.hideAllWidget()
        setViewShowState(mRightContainer, INVISIBLE)
        setViewShowState(mSettingContainer, INVISIBLE)
        setViewShowState(mRestoreScreenTextView, View.GONE)
        setViewShowState(mViewTopContainerShadow, View.INVISIBLE)
    }

    override fun onClickUiToggle(e: MotionEvent?) {
        mRightContainer?.let {
            //如果右侧栏显示，则隐藏
            if (it.visibility == View.VISIBLE) {
                it.gone()
                //因为右侧界面显示时，不在xx秒后隐藏界面，所以要恢复xx秒后隐藏控制界面
                startDismissControlViewTimer()
                return
            }
        }
        mSettingContainer?.let {
            // 如果显示，则隐藏
            if (it.visibility == View.VISIBLE) {
                it.gone()
                // 因为设置界面显示时，不在xx秒后隐藏界面，所以要恢复xx秒后隐藏控制界面
                startDismissControlViewTimer()
                return
            }
        }
        super.onClickUiToggle(e)
        setRestoreScreenTextViewVisibility()
    }

    /**
     * 全屏时将对应处理参数逻辑赋给全屏播放器
     *
     * @param context
     * @param actionBar
     * @param statusBar
     * @return
     */
    override fun startWindowFullscreen(
        context: Context?,
        actionBar: Boolean,
        statusBar: Boolean
    ): GSYBaseVideoPlayer {
        val player = super.startWindowFullscreen(
            context,
            actionBar,
            statusBar
        ) as AnimeVideoPlayer
        player.mScaleIndex = mScaleIndex
        player.mSpeedTextView?.text = mSpeedTextView?.text
        player.mFullscreenButton.visibility = mFullscreenButton.visibility
        player.mEpisodeTextViewVisibility = mEpisodeTextViewVisibility
        player.mEpisodeTextView?.visibility = mEpisodeTextViewVisibility
        player.mEpisodeAdapter = mEpisodeAdapter
        player.mTextureViewTransform = mTextureViewTransform
        player.mReverseValue = mReverseValue
        player.mBottomProgressCheckBoxValue = mBottomProgressCheckBoxValue
        player.mPlaySpeed = mPlaySpeed
        if (player.mBottomProgressBar != null) player.mBottomProgress = player.mBottomProgressBar
        if (!player.mBottomProgressCheckBoxValue) player.mBottomProgressBar = null
        touchSurfaceUp()
        player.setRestoreScreenTextViewVisibility()
        player.resolveTypeUI()
        return player
    }

    private fun setRestoreScreenTextViewVisibility() {
        if (mUiCleared) {
            mRestoreScreenTextView?.gone()
        } else {
            if (mDoublePointerZoomMoved) mRestoreScreenTextView?.visible()
            else mRestoreScreenTextView?.gone()
        }
    }

    /**
     * 退出全屏时将对应处理参数逻辑返回给非播放器
     *
     * @param oldF
     * @param vp
     * @param gsyVideoPlayer
     */
    override fun resolveNormalVideoShow(
        oldF: View?,
        vp: ViewGroup?,
        gsyVideoPlayer: GSYVideoPlayer?
    ) {
        super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer)
        if (gsyVideoPlayer != null) {
            val player = gsyVideoPlayer as AnimeVideoPlayer
            mScaleIndex = player.mScaleIndex
            mFullscreenButton.visibility = player.mFullscreenButton.visibility
            mSpeedTextView?.text = player.mSpeedTextView?.text
            mEpisodeTextViewVisibility = player.mEpisodeTextViewVisibility
            mEpisodeTextView?.visibility = mEpisodeTextViewVisibility
            mEpisodeAdapter = player.mEpisodeAdapter
            mTextureViewTransform = player.mTextureViewTransform
            mReverseValue = player.mReverseValue
            mBottomProgressCheckBoxValue = player.mBottomProgressCheckBoxValue
            mPlaySpeed = player.mPlaySpeed
            if (mBottomProgressBar != null) mBottomProgress = mBottomProgressBar
            if (!mBottomProgressCheckBoxValue) mBottomProgressBar = null
            player.touchSurfaceUp()
            setRestoreScreenTextViewVisibility()
            resolveTypeUI()
        }
    }

    fun setShowType(index: Int) {
        if (!mHadPlay || index !in mScaleStrings.indices) {
            return
        }
        mScaleIndex = index
        resolveTypeUI()
    }

    /**
     * 全屏/退出全屏，显示比例
     * 注意，GSYVideoType.setShowType是全局静态生效，除非重启APP。
     */
    @SuppressLint("SetTextI18n")
    private fun resolveTypeUI() {
        if (!mHadPlay) {
            return
        }
        mMoreScaleTextView?.text = mScaleStrings[mScaleIndex].first
        GSYVideoType.setShowType(mScaleStrings[mScaleIndex].second)
        changeTextureViewShowType()
        if (mTextureView != null) mTextureView.requestLayout()
        setSpeed(mPlaySpeed, true)
        mTouchDownHighSpeedTextView?.gone()
        mLongPressing = false
    }

    /**
     * 需要在尺寸发生变化的时候重新处理
     */
    override fun onSurfaceSizeChanged(
        surface: Surface?,
        width: Int,
        height: Int
    ) {
        super.onSurfaceSizeChanged(surface, width, height)
        resolveTransform(mTextureViewTransform)
    }

    override fun onSurfaceAvailable(surface: Surface?) {
        super.onSurfaceAvailable(surface)
//        resolveRotateUI()
        resolveTransform(mTextureViewTransform)
    }

    /**
     * 处理镜像旋转
     */
    private fun resolveTransform(transformSize: Int) {
        if (mTextureView == null) return
        val transform = Matrix()
        when (transformSize) {
            NO_REVERSE -> {  // 正常
                transform.setScale(1f, 1f, mTextureView.width / 2.toFloat(), 0f)
            }
            HORIZONTAL_REVERSE -> {  // 左右镜像
                transform.setScale(-1f, 1f, mTextureView.width / 2.toFloat(), 0f)
            }
            VERTICAL_REVERSE -> {  // 上下镜像
                transform.setScale(1f, -1f, 0f, mTextureView.height / 2.toFloat())
            }
            else -> return
        }
        mTextureViewTransform = transformSize
        mTextureView.setTransform(transform)
        mTextureView.invalidate()
    }

    override fun setUp(
        url: String?,
        cacheWithPlay: Boolean,
        cachePath: File?,
        title: String?
    ): Boolean {
        val result = super.setUp(url, cacheWithPlay, cachePath, title)
        mTitleTextView?.let {
            if (it is TypefaceTextView) {
                it.setIsFocused(true)
            }
        }
        return result
    }

    override fun updateStartImage() {
        if (mStartButton is ImageView) {
            val imageView = mStartButton as ImageView
            when (mCurrentState) {
                GSYVideoView.CURRENT_STATE_PLAYING -> {
                    imageView.setImageDrawable(getResDrawable(R.drawable.ic_pause_white_24))
                }
                GSYVideoView.CURRENT_STATE_ERROR -> {
                    imageView.setImageDrawable(getResDrawable(R.drawable.ic_play_white_24))
                }
                GSYVideoView.CURRENT_STATE_AUTO_COMPLETE -> {
                    imageView.setImageDrawable(getResDrawable(R.drawable.ic_refresh_white_24))
                }
                else -> {
                    imageView.setImageDrawable(getResDrawable(R.drawable.ic_play_white_24))
                }
            }
        } else {
            super.updateStartImage()
        }
    }

    override fun onBrightnessSlide(percent: Float) {
        val activity = mContext as Activity
        val lpa = activity.window.attributes
        val mBrightnessData = lpa.screenBrightness
        if (mBrightnessData <= 0.00f) {
            getScreenBrightness(activity)?.div(255.0f)?.let {
                lpa.screenBrightness = it
                activity.window.attributes = lpa
            }
        }
        super.onBrightnessSlide(percent)
    }

    override fun onVideoSizeChanged() {
        super.onVideoSizeChanged()
        mVideoAllCallBack.let {
            if (it is MyVideoAllCallBack) it.onVideoSizeChanged()
        }
    }

    //正常
    override fun changeUiToNormal() {
        super.changeUiToNormal()
        mViewTopContainerShadow?.visible()
        initFirstLoad = true
        mUiCleared = false
    }

    override fun changeUiToPauseShow() {
        super.changeUiToPauseShow()
        mViewTopContainerShadow?.visible()
        mUiCleared = false
    }

    override fun changeUiToClear() {
        super.changeUiToClear()
        mViewTopContainerShadow?.invisible()
        mUiCleared = true
    }

    //准备中
    override fun changeUiToPreparingShow() {
        super.changeUiToPreparingShow()
        mViewTopContainerShadow?.visible()
        mUiCleared = false
    }

    //播放中
    override fun changeUiToPlayingShow() {
        super.changeUiToPlayingShow()
        mViewTopContainerShadow?.visible()
//        if (initFirstLoad) {
//            mBottomContainer.gone()
//            mStartButton.gone()
//        }
        initFirstLoad = false
        mUiCleared = false
    }

    //自动播放结束
    override fun changeUiToCompleteShow() {
        super.changeUiToCompleteShow()
        mViewTopContainerShadow?.visible()
        mBottomContainer.gone()
        mTouchDownHighSpeedTextView?.gone()
        mUiCleared = false
    }

    override fun changeUiToError() {
        super.changeUiToError()
        mViewTopContainerShadow?.invisible()
    }

    override fun changeUiToPrepareingClear() {
        super.changeUiToPrepareingClear()
        mViewTopContainerShadow?.invisible()
    }

    override fun changeUiToPlayingBufferingClear() {
        super.changeUiToPlayingBufferingClear()
        mViewTopContainerShadow?.invisible()
    }

    override fun changeUiToCompleteClear() {
        super.changeUiToCompleteClear()
        mViewTopContainerShadow?.invisible()
    }

    override fun changeUiToPlayingBufferingShow() {
        super.changeUiToPlayingBufferingShow()
        mViewTopContainerShadow?.visible()
    }

    override fun onVideoPause() {
        super.onVideoPause()
        mVideoAllCallBack.let {
            if (it is MyVideoAllCallBack) it.onVideoPause()
        }
    }

    override fun onVideoResume(seek: Boolean) {
//        super.onVideoResume(seek)
        mPauseBeforePrepared = false
        if (mCurrentState == GSYVideoView.CURRENT_STATE_PAUSE) {
            try {
                clickStartIcon()
                mVideoAllCallBack.let {
                    if (it is MyVideoAllCallBack) it.onVideoResume()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    public override fun clickStartIcon() {
        super.clickStartIcon()
    }

    override fun onClick(v: View) {
        super.onClick(v)

        val i = v.id
        // bigger_surface代替原有的surface_container执行点击动作
        if (i == R.id.bigger_surface && mCurrentState == GSYVideoView.CURRENT_STATE_ERROR) {
            if (mVideoAllCallBack != null) {
                Debuger.printfLog("onClickStartError")
                mVideoAllCallBack.onClickStartError(mOriginUrl, mTitle, this)
            }
            prepareVideo()
        } else if (i == R.id.bigger_surface) {
            if (mVideoAllCallBack != null && isCurrentMediaListener) {
                if (mIfCurrentIsFullscreen) {
                    Debuger.printfLog("onClickBlankFullscreen")
                    mVideoAllCallBack.onClickBlankFullscreen(mOriginUrl, mTitle, this)
                } else {
                    Debuger.printfLog("onClickBlank")
                    mVideoAllCallBack.onClickBlank(mOriginUrl, mTitle, this)
                }
            }
            startDismissControlViewTimer()
        }
    }

    override fun touchLongPress(e: MotionEvent?) {
        e ?: return
        if (e.pointerCount == 1) {
            // 长按加速
            if (!mLongPressing && e.action == MotionEvent.ACTION_DOWN && !doublePointerZoomingMoving) {
                mLongPressing = true
                // 此处不能设置mPlaySpeed
                setSpeed(2f, true)
                mTouchDownHighSpeedTextView?.text =
                    mContext.getString(R.string.touch_down_high_speed, "2")
                mTouchDownHighSpeedTextView?.visible()
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        // ---长按逻辑开始
        if (event.pointerCount == 1) {
            if (event.action == MotionEvent.ACTION_UP) {
                // 如果刚才在长按，则取消长按
                if (mLongPressing) {
                    mLongPressing = false
                    setSpeed(mPlaySpeed, true)
                    mTouchDownHighSpeedTextView?.gone()
                    return false
                }
            }
        }
        // ---长按逻辑结束
        // 不是全屏下，不使用双指操作
        if (!mIfCurrentIsFullscreen) return super.onTouch(v, event)
        if (v?.id == R.id.surface_container) {
            if (event.pointerCount > 1 && event.actionMasked == MotionEvent.ACTION_MOVE) {
                // 如果是surface_container并且触摸手指数大于1，则return false拦截
                // 不让super的代码执行，表明正在双指放大移动旋转
                doublePointerZoomingMoving = true
                mDoublePointerZoomMoved = true
                if (!mUiCleared) mRestoreScreenTextView?.visible()
                // 下面用bigger_surface代替原有的surface_container执行手势动作
                return false
            }
        }
        // 当正在双指操作时，禁止执行super的代码
        if (doublePointerZoomingMoving) {
            mRestoreScreenTextView?.visible()
            // 如果双指松开，则标志不是在移动
            if (event.action == MotionEvent.ACTION_UP) {
                doublePointerZoomingMoving = false
            }
            return false
        }
        return if (v?.id == R.id.bigger_surface || v?.id == R.id.surface_container) {
            val x = event.x
            val y = event.y
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchSurfaceDown(x, y)
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = x - mDownX
                    val deltaY = y - mDownY
                    val absDeltaX = abs(deltaX)
                    val absDeltaY = abs(deltaY)
                    if (mIfCurrentIsFullscreen && mIsTouchWigetFull
                        || mIsTouchWiget && !mIfCurrentIsFullscreen
                    ) {
                        if (!mChangePosition && !mChangeVolume && !mBrightness) {
                            touchSurfaceMoveFullLogic(absDeltaX, absDeltaY)
                        }
                    }
                    touchSurfaceMove(deltaX, deltaY, y)
                }
                MotionEvent.ACTION_UP -> {
                    startDismissControlViewTimer()
                    touchSurfaceUp()
                    Debuger.printfLog(
                        this.hashCode()
                            .toString() + "------------------------------ surface_container ACTION_UP"
                    )
                    startProgressTimer()
                    //不要和隐藏虚拟按键后，滑出虚拟按键冲突
                    if (mHideKey && mShowVKey) return true
                }
            }
            gestureDetector.onTouchEvent(event)
            return false
        } else {
            super.onTouch(v, event)
        }
    }

    override fun onBackFullscreen() {
        if (!mFullAnimEnd) {
            return
        }
        mIfCurrentIsFullscreen = false
        var delay = 0
        if (mOrientationUtils != null) {
            val orientationUtils = mOrientationUtils
            delay = if (orientationUtils is AnimeOrientationUtils)
                orientationUtils.backToProtVideo2()
            else
                orientationUtils.backToProtVideo()
            mOrientationUtils.isEnable = false
            if (mOrientationUtils != null) {
                mOrientationUtils.releaseListener()
                mOrientationUtils = null
            }
        }

        if (!mShowFullAnimation) {
            delay = 0
        }

        val vp = CommonUtil.scanForActivity(context)
            .findViewById<View>(Window.ID_ANDROID_CONTENT) as ViewGroup
        val oldF = vp.findViewById<View>(fullId)
        if (oldF != null) {
            //此处fix bug#265，推出全屏的时候，虚拟按键问题
            val gsyVideoPlayer = oldF as GSYVideoPlayer
            gsyVideoPlayer.isIfCurrentIsFullscreen = false
        }

        mInnerHandler.postDelayed({ backToNormal() }, delay.toLong())
    }

    fun setEpisodeButtonOnClickListener(listener: OnClickListener) {
        mEpisodeButtonOnClickListener = listener
    }

    fun setEpisodeAdapter(adapter: EpisodeRecyclerViewAdapter) {
        mEpisodeAdapter = adapter
    }

    fun getShareButton() = mShareImageView

    fun getMoreButton() = mMoreImageView

    fun getEpisodeButton() = mEpisodeTextView

    fun getDownloadButton() = mDownloadButton

    fun getBottomContainer() = mBottomContainer

    fun getClingButton() = mClingImageView

    fun getNextButton() = mNextImageView

    fun getRightContainer() = mRightContainer

    fun setEpisodeButtonVisibility(visibility: Int) {
        mEpisodeTextView?.visibility = visibility
        mEpisodeTextViewVisibility = visibility
    }

    fun enableDismissControlViewTimer(start: Boolean) {
        if (start) super.startDismissControlViewTimer()
        else super.cancelDismissControlViewTimer()
    }

    class SpeedBean(
        override var type: String,
        override var actionUrl: String,
        var title: String
    ) : BaseBean, Serializable

    inner class SpeedAdapter(val list: List<SpeedBean>) : SkinRvAdapter() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return RightRecyclerViewViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_player_list_item_1, parent, false)
            ).apply { SkinManager.setSkin(itemView) }
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            super.onBindViewHolder(holder, position)
            val item = list[position]

            when (holder) {
                is RightRecyclerViewViewHolder -> {
                    if (item.type == "speed") {
                        if (item.title.toFloat() == speed) {
                            holder.tvTitle.setTextColor(context.getResColor(R.color.unchanged_main_color_2_skin))
                        }
                        holder.tvTitle.text = item.title
                        holder.itemView.setOnClickListener {
                            if (item.title == "1") {
                                mSpeedTextView?.text = App.context.getString(R.string.play_speed)
                            } else {
                                mSpeedTextView?.text = item.title + "X"
                            }
                            mPlaySpeed = item.title.toFloat()
                            setSpeed(mPlaySpeed, true)
                            mRightContainer?.gone()
                            //因为右侧界面显示时，不在xx秒后隐藏界面，所以要恢复xx秒后隐藏控制界面
                            startDismissControlViewTimer()
                        }
                    }
                }
            }
        }

        override fun getItemCount(): Int = list.size
    }

    abstract class EpisodeRecyclerViewAdapter(
        private val activity: Activity,
        private val dataList: List<AnimeEpisodeDataBean>,
    ) : SkinRvAdapter() {

        abstract val currentIndex: Int

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return RightRecyclerViewViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_player_list_item_1, parent, false)
            ).apply { SkinManager.setSkin(itemView) }
        }

        override fun getItemCount(): Int = dataList.size
    }

    class RightRecyclerViewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle = view as TextView
    }
}