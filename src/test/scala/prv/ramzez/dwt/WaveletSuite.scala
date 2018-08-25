package prv.ramzez.dwt

import org.bytedeco.javacpp.indexer.FloatIndexer
import org.bytedeco.javacpp.opencv_core
import org.bytedeco.javacpp.opencv_core.Mat
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class WaveletSuite extends FunSuite {
  test("init filter test") {
    //given
    val columnLow = Mat.eye(2, 1, opencv_core.CV_32F).asMat()
    val columnHigh = Mat.ones(2, 1, opencv_core.CV_32F).asMat()
    //when
    val f = Wavelet(columnLow, columnHigh)
    //then
    assert(matEquals(columnLow, f.columnLow), s"columnLow should be: ${columnLow.createIndexer().asInstanceOf[FloatIndexer]} but was ${f.columnLow.createIndexer().asInstanceOf[FloatIndexer]}")
    assert(matEquals(columnHigh, f.columnHigh), s"columnLow should be: ${columnHigh.createIndexer().asInstanceOf[FloatIndexer]} but was ${f.columnHigh.createIndexer().asInstanceOf[FloatIndexer]}")
  }
}
