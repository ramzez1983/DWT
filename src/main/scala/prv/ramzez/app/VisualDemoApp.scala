package prv.ramzez.app

import java.nio.file.Paths

import javax.swing.WindowConstants
import org.bytedeco.javacpp.opencv_core.Mat
import org.bytedeco.javacpp.opencv_imgcodecs.imread
import org.bytedeco.javacpp.opencv_imgproc
import org.bytedeco.javacv.{CanvasFrame, OpenCVFrameConverter}
import prv.ramzez.dwt.{Transformer, Wavelet}
import prv.ramzez.visual.DwtStructVisualiser

object VisualDemoApp extends App {
  val path = Paths.get(getClass.getResource("/lena1.png").toURI)
  val img: Mat = imread(path.toString)
  val temp = new Mat()
  opencv_imgproc.cvtColor(img, temp, opencv_imgproc.CV_BGR2GRAY, 1)

  val trans = Transformer(Wavelet.d4)
  val result = DwtStructVisualiser.toSingleImg(trans.decompose(img, 2))

  val canvas = new CanvasFrame("My Image", 1)
  // Request closing of the application when the image window is closed
  canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  // Convert from OpenCV Mat to Java Buffered image for display
  val converter = new OpenCVFrameConverter.ToMat()
  // Show image on window
  canvas.showImage(converter.convert(result))
}
