package prv.ramzez.visual

import org.bytedeco.javacpp.opencv_core
import org.bytedeco.javacpp.opencv_core.Mat
import prv.ramzez.dwt.{Dwt2DStep, DwtStructure}

object DwtStructVisualiser {

  def toSingleImg(step: Dwt2DStep): Mat = {
    val result, temp1, temp2 = new Mat()
    temp1.push_back(step.ll)
    temp1.push_back(step.lh)
    temp2.push_back(step.hl)
    temp2.push_back(step.hh)
    result.push_back(temp1.t().asMat())
    result.push_back(temp2.t().asMat())
    result.t().asMat()
  }

  def toSingleImg(struc: DwtStructure): Mat = {
    val rev = struc.reverse
    val result = new Mat()
    toSingleImg(toSingleImg(rev.head), rev.tail).convertTo(result, opencv_core.CV_8U)
    result
  }

  private def toSingleImg(img: Mat, struc: DwtStructure): Mat = {
    if (struc.isEmpty) img
    else {
      val step = struc.head
      toSingleImg(toSingleImg(Dwt2DStep(step.hh, step.hl, step.lh, img)), struc.tail)
    }
  }
}
