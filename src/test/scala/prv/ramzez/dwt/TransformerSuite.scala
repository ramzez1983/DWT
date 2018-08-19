package prv.ramzez.dwt

import org.bytedeco.javacpp.indexer.FloatIndexer
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TransformerSuite extends FunSuite {
  test("decompose step test") {
    //given
    val trans = Transformer(Filter(List(1f, 2f), List(0.5f, 0.25f)))
    val img = (0 until 9 map (i => i.toFloat) toList).reshape(0, 3)
    //    [[0,1,2]
    //     [3,4,5]
    //     [6,7,8]]
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
    //    [[1.500, 1.875]
    //      2.625, 3.000]
    //    hl
    //    [[ 6.00,  7.50]
    //      12.75, 14.25]]
    //then
    val hh = List(1.5f, 1.875f, 2.6250f, 3f).reshape(0, 2)
    assert(matEquals(hh, struc.HH), s"hh should be: ${hh.createIndexer().asInstanceOf[FloatIndexer]}, hh is: ${struc.HH.createIndexer().asInstanceOf[FloatIndexer]}"
    )
  }
  test("decompose and recompose step test") {
    //given
    val trans = Transformer(Filter.haar)
    val dim = 8
    val img = (0 until (dim * dim) map (i => Math.random()) toList).reshape(0, dim)
    //when
    val struc = trans.decomposeStep(img)
    val imgRe = trans.recomposeStep(struc)
    //then
    assert(matEquals(img, imgRe), s"imgRe should be: ${img.createIndexer().asInstanceOf[FloatIndexer]}, imgRe is: ${imgRe.createIndexer().asInstanceOf[FloatIndexer]}")
  }
}
