package com.example.shots

import android.app.VoiceInteractor
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

object PromptsUtils {

    data class Prompt(val id: Int, val prompt: String)

    fun loadPromptsFromJson(context: Context): List<Prompt> {
        val inputStream = context.resources.openRawResource(R.raw.prompts)
        val json = inputStream.bufferedReader().use { it.readText() }
        return Gson().fromJson(json, Array<Prompt>::class.java).toList()
    }
}





