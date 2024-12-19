//package com.android.unio.utils
//
//import com.android.unio.model.utils.TextLength
//import com.android.unio.model.utils.Utils
//import junit.framework.TestCase.assertEquals
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.robolectric.RobolectricTestRunner
//
//@RunWith(RobolectricTestRunner::class)
//class TextLengthTest {
//
//  @Test
//  fun testTextLengthWorksCorrectly() {
//    val largeTextTooLong =
//        "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo " +
//            "ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, " +
//            "nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. " +
//            "Nulla consequat massa quis enim. Donec p"
//
//    val largeTextOk =
//        "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo " +
//            "ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, " +
//            "nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. " +
//            "Nulla consequat massa quis enim."
//
//    val mediumTextTooLong =
//        "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. " +
//            "Aenean commodo ligula eget dolor. Aenean ma"
//
//    val mediumTextOk =
//        "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. " +
//            "Aenean commodo ligula eget dolor."
//
//    val smallTextTooLong = "Lorem ipsum dolor sit amet, con"
//
//    val smallTextOk = "Lorem ipsum dolor sit amet."
//
//    assertEquals(false, Utils.checkInputLength(largeTextTooLong, TextLength.LARGE))
//    assertEquals(true, Utils.checkInputLength(largeTextOk, TextLength.LARGE))
//    assertEquals(false, Utils.checkInputLength(mediumTextTooLong, TextLength.MEDIUM))
//    assertEquals(true, Utils.checkInputLength(mediumTextOk, TextLength.MEDIUM))
//    assertEquals(false, Utils.checkInputLength(smallTextTooLong, TextLength.SMALL))
//    assertEquals(true, Utils.checkInputLength(smallTextOk, TextLength.SMALL))
//  }
//
//  @Test
//  fun testTextLengthAreCloseWorksCorrectly() {
//    val largeTextClose =
//        "Sed ut perspiciatis unde omnis iste natus error sit " +
//            "voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae " +
//            "ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. " +
//            "Nemo enim ipsam voluptatem quia voluptas sit aspernatur"
//
//    val largeTextNotClose =
//        "Sed ut perspiciatis unde omnis iste natus error sit " +
//            "voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae " +
//            "ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. " +
//            "Nemo enim ipsam voluptatem quia voluptas"
//
//    val mediumTextClose =
//        "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium dolore"
//
//    val mediumTextNotClose = "Sed ut perspiciatis unde omnis iste natus error sit voluptatem"
//
//    val smallTextClose = "Sed ut perspiciatis u"
//
//    val smallTextNotClose = "Sed ut perspiciatis"
//
//    assertEquals(true, Utils.checkInputLengthIsClose(largeTextClose, TextLength.LARGE))
//    assertEquals(false, Utils.checkInputLengthIsClose(largeTextNotClose, TextLength.LARGE))
//    assertEquals(true, Utils.checkInputLengthIsClose(mediumTextClose, TextLength.MEDIUM))
//    assertEquals(false, Utils.checkInputLengthIsClose(mediumTextNotClose, TextLength.MEDIUM))
//    assertEquals(true, Utils.checkInputLengthIsClose(smallTextClose, TextLength.SMALL))
//    assertEquals(false, Utils.checkInputLengthIsClose(smallTextNotClose, TextLength.SMALL))
//  }
//}
