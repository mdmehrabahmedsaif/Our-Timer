package com.ourtimer.app.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ChallengeRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "our_timer_prefs"
        private const val KEY_CHALLENGES = "challenges"
        private const val KEY_ACTIVE_ID = "active_challenge_id"
    }

    fun getAll(): List<Challenge> {
        val json = prefs.getString(KEY_CHALLENGES, null) ?: return emptyList()
        return try {
            val jsonArray = gson.fromJson(json, com.google.gson.JsonArray::class.java)
            val list = mutableListOf<Challenge>()
            for (element in jsonArray) {
                val obj = element.asJsonObject
                val id = obj.get("id").asString
                val name = obj.get("name").asString
                
                val days = if (obj.has("days")) {
                    obj.get("days").asInt
                } else if (obj.has("totalDays")) {
                    obj.get("totalDays").asInt
                } else {
                    60
                }
                
                val alterEgo = obj.get("alterEgo").asString
                
                val startTime = if (obj.has("startTime")) {
                    obj.get("startTime").asLong
                } else if (obj.has("startDate")) {
                    obj.get("startDate").asLong
                } else {
                    System.currentTimeMillis()
                }
                
                val why = if (obj.has("why")) obj.get("why").asString else ""
                val lastWhyPromptDate = if (obj.has("lastWhyPromptDate")) obj.get("lastWhyPromptDate").asString else ""
                
                val shownMilestones = mutableListOf<Int>()
                if (obj.has("shownMilestones")) {
                    val array = obj.getAsJsonArray("shownMilestones")
                    for (m in array) {
                        shownMilestones.add(m.asInt)
                    }
                } else if (obj.has("milestoneShown")) {
                    val legacyVal = obj.get("milestoneShown").asInt
                    if (legacyVal > 0) {
                        shownMilestones.add(legacyVal)
                    }
                }
                
                list.add(Challenge(id, name, days, alterEgo, startTime, why, lastWhyPromptDate, shownMilestones))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(challenge: Challenge) {
        val list = getAll().toMutableList()
        val index = list.indexOfFirst { it.id == challenge.id }
        if (index != -1) {
            list[index] = challenge
        } else {
            list.add(challenge)
        }
        saveAll(list)
    }

    fun delete(id: String) {
        val list = getAll().toMutableList()
        list.removeAll { it.id == id }
        saveAll(list)

        // If the deleted challenge was active, reset active ID
        if (getActiveChallengeId() == id) {
            val newActive = list.firstOrNull()?.id
            setActiveChallengeId(newActive)
        }
    }

    fun getActiveChallengeId(): String? {
        return prefs.getString(KEY_ACTIVE_ID, null)
    }

    fun setActiveChallengeId(id: String?) {
        prefs.edit().putString(KEY_ACTIVE_ID, id).apply()
    }

    fun getActiveChallenge(): Challenge? {
        val list = getAll()
        val activeId = getActiveChallengeId() ?: return list.firstOrNull()
        return list.firstOrNull { it.id == activeId } ?: list.firstOrNull()
    }

    fun update(challenge: Challenge) {
        save(challenge)
    }

    fun getThemeMode(): String {
        return prefs.getString("theme_mode", "system") ?: "system"
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
    }

    private fun saveAll(challenges: List<Challenge>) {
        val json = gson.toJson(challenges)
        prefs.edit().putString(KEY_CHALLENGES, json).apply()
    }
}
