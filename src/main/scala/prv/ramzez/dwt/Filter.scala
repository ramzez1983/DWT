package prv.ramzez.dwt

import com.typesafe.scalalogging.LazyLogging
import org.bytedeco.javacpp.opencv_core
import org.bytedeco.javacpp.opencv_core.Mat

trait Filter {
  def filterColumn(img: Mat, kernel: Mat): Mat

  def filterRow(img: Mat, kernel: Mat): Mat
}

//private val anchor = new Point(0, 1)
//private val delta = 0.0
//switching to own implementation because opencv_imgproc.filter2D does not support circular boarder type
//private val borderType = opencv_core.BORDER_DEFAULT
trait RangeFilter extends Filter with LazyLogging with Border {
  private def getColRange(img: Mat, start: Int, size: Int): Mat = {
    val result = new Mat
    for (i <- start until (start + size)) {
      result.push_back(img.col(getWithBorder(i, img.cols())))
    }
    result.reshape(0, size).t().asMat()
  }

  private def getRowRange(img: Mat, start: Int, size: Int): Mat = {
    val result = new Mat
    for (i <- start until (start + size)) {
      result.push_back(img.row(getWithBorder(i, img.rows())))
    }
    result
  }

  def filterColumn(img: Mat, kernel: Mat): Mat = {
    val result, tmp = new Mat()
    //    val anchor = new Point(0, 1)
    //    opencv_imgproc.filter2D(img, result, -1, kernel.t().asMat(), anchor, delta, borderType)
    val step = kernel.cols()
    val mod = step / 2 - 1
    for (i <- 0 - mod until img.rows() - mod) {
      opencv_core.gemm(kernel, getRowRange(img, i, step), 1, new Mat(), 0, tmp)
      result.push_back(tmp)
    }
    logger.debug(s"filtered columns of ${printMat(img)} to ${printMat(result)}")
    result
  }

  def filterRow(img: Mat, kernel: Mat): Mat = {
    val r, tmp = new Mat()
    //val result = new Mat()
    //val anchor = new Point(1, 0)
    //opencv_imgproc.filter2D(img, result, -1, kernel.t().asMat(), anchor, delta, borderType)
    val step = kernel.rows()
    val mod = step / 2 - 1
    for (i <- 0 - mod until img.cols() - mod) {
      opencv_core.gemm(getColRange(img, i, step), kernel, 1, new Mat(), 0, tmp)
      r.push_back(tmp)
    }
    val result = r.reshape(0, img.rows()).t().asMat()
    logger.debug(s"filtered rows of ${printMat(img)} to ${printMat(result)}")
    result
  }
}