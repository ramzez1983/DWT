package prv.ramzez

import org.bytedeco.javacpp.indexer.FloatIndexer
import org.bytedeco.javacpp.opencv_core
import org.bytedeco.javacpp.opencv_core.Mat

package object dwt {

  type DwtStructure = List[DwtStep]

  implicit def listOfFloatToMat(list: List[Float]): Mat = {
    val mat = Mat.zeros(1, list.length, opencv_core.CV_32F).asMat()
    val imgIndexer = mat.createIndexer().asInstanceOf[FloatIndexer]
    (0l until list.length zip list).foreach { case (i, v) => imgIndexer.put(0, i, v) }
    mat
  }

  implicit def listOfDoubleToMat(list: List[Double]): Mat = {
    val mat = Mat.zeros(1, list.length, opencv_core.CV_32F).asMat()
    val imgIndexer = mat.createIndexer().asInstanceOf[FloatIndexer]
    (0l until list.length zip list).foreach { case (i, v) => imgIndexer.put(0, i, v.toFloat) }
    mat
  }

  def matEquals(a: Mat, b: Mat): Boolean = {
    opencv_core.countNonZero(opencv_core.notEquals(a, b).asMat()) == 0
  }
}
