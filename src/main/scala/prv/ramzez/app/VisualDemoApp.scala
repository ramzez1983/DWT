package prv.ramzez.app

import java.nio.file.Paths

import javax.swing.JFrame
import org.bytedeco.javacpp.opencv_core.Mat
import org.bytedeco.javacpp.opencv_imgcodecs.imread
import org.bytedeco.javacpp.{opencv_core, opencv_imgproc}
import org.bytedeco.javacv.{CanvasFrame, OpenCVFrameConverter}
import prv.ramzez.dwt.{Transformer, Wavelet}
import prv.ramzez.visual.DwtStructVisualiser

object VisualDemoApp extends App {
  val path = Paths.get(getClass.getResource("/lena1.png").toURI)
  val img: Mat = imread(path.toString)
  val greyMat, temp1, result = new Mat()
  opencv_imgproc.cvtColor(img, greyMat, opencv_imgproc.CV_BGR2GRAY, 1)

  greyMat.convertTo(temp1, opencv_core.CV_32F)
  val trans = Transformer(Wavelet.d4)
  val temp2 = DwtStructVisualiser.toSingleImg(trans.decompose(temp1, 3))
  temp2.convertTo(result, opencv_core.CV_8U)

  val canvas = new CanvasFrame("My Image", 1)
  // Request closing of the application when the image window is closed
  canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  // Convert from OpenCV Mat to Java Buffered image for display
  val converter = new OpenCVFrameConverter.ToMat()
  // Show image on window
  canvas.showImage(converter.convert(result))
}
