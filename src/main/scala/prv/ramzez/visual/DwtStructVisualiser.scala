package prv.ramzez.visual

import org.bytedeco.javacpp.opencv_core.Mat
import prv.ramzez.dwt.{DwtStep, DwtStructure}

object DwtStructVisualiser {

  def toSingleImg(step: DwtStep): Mat = {
    val result, temp1, temp2 = new Mat()
    temp1.push_back(step.LL)
    temp1.push_back(step.LH)
    temp2.push_back(step.HL)
    temp2.push_back(step.HH)
    result.push_back(temp1.t().asMat())
    result.push_back(temp2.t().asMat())
    result.t().asMat()
  }

  def toSingleImg(struc: DwtStructure): Mat = {
    val rev = struc.reverse
    toSingleImg(toSingleImg(rev.head), rev.tail)
  }

  private def toSingleImg(img: Mat, struc: DwtStructure): Mat = {
    if (struc.isEmpty) img
    else {
      val step = struc.head
      toSingleImg(toSingleImg(DwtStep(step.HH, step.HL, step.LH, img)), struc.tail)
    }
  }
}
