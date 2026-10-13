package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Bakri Print", appName)
  }

  @Test
  fun `dimensional area and subtotal calculation`() {
    val width = 3.0
    val height = 1.5
    val quantity = 2
    val area = width * height * quantity
    assertEquals(9.0, area, 0.001)

    val unitPriceYer = 1200.0
    val printCost = area * unitPriceYer
    val designFee = 2000.0
    val subtotal = printCost + designFee
    assertEquals(12800.0, subtotal, 0.001)

    val discount = 800.0
    val deposit = 5000.0
    val totalDue = subtotal - discount
    val remaining = totalDue - deposit
    assertEquals(12000.0, totalDue, 0.001)
    assertEquals(7000.0, remaining, 0.001)
  }
}
