/*
 * Copyright (c) 2020 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.tacotuesday.discover

import android.content.SharedPreferences
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raywenderlich.android.tacotuesday.shared.TryDiscardRecipeViewModel
import com.raywenderlich.android.tacotuesday.shared.TryDiscardRecipeViewModelImpl
import com.raywenderlich.android.tacotuesday.data.Recipe
import com.raywenderlich.android.tacotuesday.data.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DiscoverViewModel @ViewModelInject constructor(
    private val recipeRepository: RecipeRepository,
    private val sharedPreferences: SharedPreferences,
    @Assisted savedStateHandle: SavedStateHandle
) : ViewModel(), TryDiscardRecipeViewModel by TryDiscardRecipeViewModelImpl(
    recipeRepository) {

  private val recipe = savedStateHandle.getLiveData<Recipe>(SAVED_STATE_KEY)
  val nextRecipe = recipe

  private var fetchTacoJob: Job? = null

  fun fetchRandomTaco() {
    // Reset the timer if this is called again through requesting a new recipe
    fetchTacoJob?.cancel()

    viewModelScope.launch(Dispatchers.IO) {
      recipeRepository.randomTacoRecipe()?.let {
        recipe.postValue(it)
      }
    }

    if (sharedPreferences.getBoolean("auto_advance", false)) {
      fetchTacoJob = viewModelScope.launch(Dispatchers.IO) {
        delay(15000) // 15 seconds
        fetchRandomTaco()
      }
    }
  }

  fun discard(recipe: Recipe?) {
    fetchRandomTaco()
    discardRecipe(recipe, viewModelScope)
  }

  fun tryIt(recipe: Recipe?) {
    fetchRandomTaco()
    tryRecipe(recipe, viewModelScope)
  }

  companion object {
    private const val SAVED_STATE_KEY = "DiscoverViewModelState"
  }
}