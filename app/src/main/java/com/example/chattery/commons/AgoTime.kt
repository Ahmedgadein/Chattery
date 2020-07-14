package com.example.chattery.commons

import android.app.Application
import android.content.Context
import java.util.*


class AgoTime : Application() {
    /*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

    /*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


    companion object{
        private const val SECOND_MILLIS = 1000
        private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
        private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
        private const val DAY_MILLIS = 24 * HOUR_MILLIS

        fun getTimeAgo(time: Long, ctx: Context?): String? {
            var time = time
            if (time < 1000000000000L) { // if timestamp given in seconds, convert to millis
                time *= 1000
            }
            val now = Calendar.getInstance().timeInMillis

//            if (time > now || time <= 0) {
//                return time.toString()
//            }

            val diff = now - time
            return when {
                diff < MINUTE_MILLIS -> {
                    "just now"
                }

                diff < 2 * MINUTE_MILLIS -> {
                    "a minute ago"
                }

                diff < 50 * MINUTE_MILLIS -> {
                    val value = diff / MINUTE_MILLIS
                    "$value minutes ago"
                }

                diff < 90 * MINUTE_MILLIS -> {
                    "an hour ago"
                }

                diff < 24 * HOUR_MILLIS -> {
                    val value = diff / HOUR_MILLIS
                    "$value hours ago"
                }

                diff < 48 * HOUR_MILLIS -> {
                    "yesterday"
                }

                else -> {
                    val value = diff / DAY_MILLIS
                    "$value days ago"
                }
            }
        }
    }
}