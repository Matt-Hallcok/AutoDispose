/*
 * Copyright (C) 2019. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.uber.autodispose.sample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDispose
import com.uber.autodispose.recipes.subscribeBy
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 * Demo activity, shamelessly borrowed from RxLifecycle's demo.
 *
 * This leverages the Architecture Components support for the demo
 */
class KotlinActivity : AppCompatActivity() {

  // Can be reused
  private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Log.d(TAG, "onCreate()")
    setContentView(R.layout.activity_main)

    // Using automatic disposal, this should determine that the correct time to
    // dispose is onDestroy (the opposite of onCreate).
    Observable.interval(1, TimeUnit.SECONDS)
        .doOnDispose { Log.i(TAG, "Disposing subscription from onCreate()") }
        .autoDispose(scopeProvider)
        .subscribeBy { num -> Log.i(TAG, "Started in onCreate(), running until onDestroy(): $num") }

    supportFragmentManager.beginTransaction()
        .add(R.id.fragment_container, KotlinFragment())
        .commitNow()
  }

  override fun onStart() {
    super.onStart()

    Log.d(TAG, "onStart()")

    // Using automatic disposal, this should determine that the correct time to
    // dispose is onStop (the opposite of onStart).
    Observable.interval(1, TimeUnit.SECONDS)
        .doOnDispose { Log.i(TAG, "Disposing subscription from onStart()") }
        .autoDispose(scopeProvider)
        .subscribeBy { num -> Log.i(TAG, "Started in onStart(), running until in onStop(): $num") }
  }

  override fun onResume() {
    super.onResume()

    Log.d(TAG, "onResume()")

    // Using automatic disposal, this should determine that the correct time to
    // dispose is onPause (the opposite of onResume).
    Observable.interval(1, TimeUnit.SECONDS)
        .doOnDispose { Log.i(TAG, "Disposing subscription from onResume()") }
        .autoDispose(scopeProvider)
        .subscribeBy { num -> Log.i(TAG, "Started in onResume(), running until in onPause(): $num") }

    // Setting a specific untilEvent, this should dispose in onDestroy.
    Observable.interval(1, TimeUnit.SECONDS)
        .doOnDispose {
          Log.i(TAG, "Disposing subscription from onResume() with untilEvent ON_DESTROY")
        }
        .autoDispose(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY))
        .subscribeBy { num -> Log.i(TAG, "Started in onResume(), running until in onDestroy(): $num") }
  }

  override fun onPause() {
    Log.d(TAG, "onPause()")
    super.onPause()
  }

  override fun onStop() {
    Log.d(TAG, "onStop()")
    super.onStop()
  }

  override fun onDestroy() {
    Log.d(TAG, "onDestroy()")
    super.onDestroy()
  }

  companion object {
    private const val TAG = "KotlinActivity"
  }
}
