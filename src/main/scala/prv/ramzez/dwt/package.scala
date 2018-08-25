package prv.ramzez

import org.bytedeco.javacpp.indexer.FloatIndexer
import org.bytedeco.javacpp.opencv_core
import org.bytedeco.javacpp.opencv_core.{Mat, MatVector}

package object dwt {

  type DwtStructure = List[Dwt2DStep]

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

  def matSplit(img: Mat): List[Mat] = {
    val splited = new MatVector()
    opencv_core.split(img, splited)
    (0l until splited.size()).map(i => splited.get(i).clone()).toList
  }

  def matMerge(channels: List[Mat]): Mat = {
    val splited = new MatVector()
    val img = new Mat()
    channels.foreach(ch => splited.push_back(ch))
    opencv_core.merge(splited, img)
    img
  }

  def matEquals(a: Mat, b: Mat): Boolean = {
    opencv_core.countNonZero(opencv_core.notEquals(a, b).asMat()) == 0
  }

  def printMat(img: Mat): String = {
    img.createIndexer().asInstanceOf[FloatIndexer].toString
  }
}
