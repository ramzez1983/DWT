package prv.ramzez.dwt

import org.bytedeco.javacpp.indexer.FloatIndexer
import org.bytedeco.javacpp.opencv_core
import org.bytedeco.javacpp.opencv_core.Mat
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TransformerSuite extends FunSuite {
  test("decompose step test") {
    //given
    val trans = new Transformer(List(1, 2), List(0.5f, 0.25f))
    val img = Mat.zeros(3, 3, opencv_core.CV_32F).asMat()
    //    [[0,1,2]
    //     [3,4,5]
    //     [6,7,8]]
    //TODO: fix me
    val imgIndexer = img.createIndexer().asInstanceOf[FloatIndexer]
    0 until 9 foreach (i => imgIndexer.put(i, i.toFloat))

    //when
    val struc = trans.decomposeStep(img)
    //    low
    //    [[ 1, 2, 5]
    //     [10,11,14]
    //     [19,20,23]]
    //    high
    //    [[0.50, 0.25, 1.00]
    //     [2.75, 2.50, 3.25]
    //     [5.00, 4.75, 5.50]]
    //    hh
    //    [[0.9375, 1.3125]
    //      2.6250, 3.0000]
    //    hl
    //    [[ 6.00,  7.50]
    //      12.75, 14.25]]

    //then
    val hh = List(0.9375f, 1.3125f, 2.6250f, 3f).reshape(0, 2)
    println(s"hh should be: ${hh.createIndexer().asInstanceOf[FloatIndexer]}")
    println(s"hh is: ${struc.HH.createIndexer().asInstanceOf[FloatIndexer]}")
    assert(hh.equals(struc.HH),
      "decomposer hh check"
    )
  }

}
