package com.example.journey.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.journey.R

/**
 * 全局音效播放器
 * 使用单例模式管理 SoundPool，避免弹窗销毁导致音效中断
 */
object AppSoundPlayer {
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0
    private var isLoaded = false

    /**
     * 初始化音效播放器
     * 在 MainActivity.onCreate 中调用一次即可
     */
    fun init(context: Context) {
        if (soundPool != null) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA) // 使用媒体流，跟随媒体音量，不受通知静音影响
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool?.setOnLoadCompleteListener { _, _, status ->
            isLoaded = status == 0
        }

        soundId = soundPool?.load(context, R.raw.seed_click_sound, 1) ?: 0
    }

    /**
     * 播放音效
     */
    fun play() {
        if (isLoaded && soundId != 0) {
            soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    /**
     * 释放资源
     * 在应用退出时调用
     */
    fun release() {
        soundPool?.release()
        soundPool = null
        isLoaded = false
        soundId = 0
    }
}
