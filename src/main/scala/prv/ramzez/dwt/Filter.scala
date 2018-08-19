package prv.ramzez.dwt

import org.bytedeco.javacpp.opencv_core.Mat

case class Filter(rowLow: Mat, rowHigh: Mat) {
  lazy val columnHigh: Mat = toColumnKernel(rowHigh)
  lazy val columnLow: Mat = toColumnKernel(rowLow)

  private def toColumnKernel(filter: Mat): Mat = filter.t().asMat()
}

object Filter {
  private val s2: Double = Math.sqrt(2)
  val haar = Filter(List(1 / s2, 1 / s2), List(1 / s2, -1 / s2))
}
