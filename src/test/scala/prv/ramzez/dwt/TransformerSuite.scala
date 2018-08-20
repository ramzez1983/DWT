package prv.ramzez.dwt

import org.bytedeco.javacpp.indexer.FloatIndexer
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TransformerSuite extends FunSuite {
  test("decompose step test") {
    //given
    val trans = Transformer(Filter(List(1f, 1f), List(1f, -1f)))
    val img = (0 until 16 map (i => i.toFloat) toList).reshape(0, 4)
    //    [[ 0, 1, 2, 3]
    //     [ 4, 5, 6, 7]
    //     [ 8, 9,10,11]
    //     [12,13,14,15]
    //when
    val struc = trans.decomposeStep(img)
    //    low
    //    [[ 1, 3, 5, 3]
    //     [ 9,11,13,11]
    //     [17,19,21,19]
    //     [25,27,29,27]
    //    high
    //    [[-1,-1,-1, 1]
    //     [-1,-1,-1, 1]
    //     [-1,-1,-1, 1]
    //     [-1,-1,-1, 1]]
    //    hh
    //    [[0, 0]
    //      0, 0]
    //    ll
    //    [[10,18]
    //      42,50]]
    //then
    val ll = List(10f, 18f, 42f, 50f).reshape(0, 2)
    assert(matEquals(ll, struc.LL), s"ll should be: ${ll.createIndexer().asInstanceOf[FloatIndexer]}, ll is: ${struc.LL.createIndexer().asInstanceOf[FloatIndexer]}"
    )
  }
  test("decompose and recompose step test") {
    //given
    val trans = Transformer(Filter.d4)
    val dim = 8

    def produceValue(i: Int) = {
      val b = 0
          if (Math.floorDiv(i,dim)<b || Math.floorDiv(i,dim)>dim-1-b || Math.floorMod(i,dim)<b||Math.floorMod(i,dim)>dim-1-b) 0.0
          else i.toDouble //Math.random()
    }

    val img = (0 until (dim * dim) map (i => produceValue(i)) toList).reshape(0, dim)
    //when
    val struc = trans.decomposeStep(img)
    val imgRe = trans.recomposeStep(struc)
    //then
    assert(matEquals(img, imgRe), s"imgRe should be: ${img.createIndexer().asInstanceOf[FloatIndexer]}, imgRe is: ${imgRe.createIndexer().asInstanceOf[FloatIndexer]}")
  }
}
