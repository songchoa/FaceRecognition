package Main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.JLabel;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;
import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_core.cvFlip;
import static org.bytedeco.javacpp.opencv_core.cvGetSeqElem;
import static org.bytedeco.javacpp.opencv_core.cvGetSize;
import static org.bytedeco.javacpp.opencv_core.cvLoad;
import static org.bytedeco.javacpp.opencv_core.cvPoint;
import static org.bytedeco.javacpp.opencv_imgproc.CV_AA;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.CV_INTER_LINEAR;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.cvEqualizeHist;
import static org.bytedeco.javacpp.opencv_imgproc.cvRectangle;
import static org.bytedeco.javacpp.opencv_imgproc.cvResize;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;
import static org.bytedeco.javacpp.opencv_core.cvCopy;
import static org.bytedeco.javacpp.opencv_core.cvSetImageROI;

import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvScalar;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.javacv.VideoInputFrameGrabber;




public class Launcher extends JFrame {

    private static JPanel contentPane;
    File targetFile;
    BufferedImage targetImg;
	protected static ImageIcon loadedImg;
    public static JPanel panel;
	public static JPanel picPanel;
    private static JLabel imgLabel;
    
    private static JComponent canvas;
    protected static BufferedImage image;
    private static FrameGrabber grabber;
    protected static int width, height;
    private static CaptureImageThread streaming;
    protected static boolean mirrored = true;
    protected static double scale = 0.5;
    private static boolean isCancelled = false;
    
    private static boolean changeToTrue = false;
    
    private static boolean toCapture = false;
    
    private static CvRect faceRec;
    private static final String CASCADE_FILE = "haarcascade_frontalface_alt.xml";
    
    private static void createAndShowGUI() {
    	
    	//construct a frame
    	JFrame frame = new JFrame();
    	//set close operation on the frame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //set window position and size
        frame.setBounds(300, 300, 550, 400);
        //create main container
        contentPane = new JPanel();
        //set border for main container
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        //set contentPane for frame
        frame.setContentPane(contentPane);
        //set layout for main container
        contentPane.setLayout(new BorderLayout(0, 0));
        //construct another panel
        panel = new JPanel();
        //set border for that panel
        panel.setBorder(new LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        //add panel to main container in the left
        contentPane.add(panel, BorderLayout.WEST);

        //create the browse button
        JButton btnBrowse = new JButton("Browse");
        btnBrowse.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

        		JFileChooser fc = new JFileChooser();
                int res = fc.showOpenDialog(null);
                try {
                    if (res == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        String imagePath = file.getAbsolutePath();
                        imgLabel.setIcon(new ImageIcon(imagePath));

                    } 
            
                } catch (Exception iOException) {
                }
     
        	
            }
        });
        
        //create label for browse button
        JLabel lblSelectTargetPicture = new JLabel("select image");
        //create the detect button
        JButton btnDetect = new JButton("Detect");
        btnDetect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	changeToTrue =false;
            }
        });
        //create add digit button
        JButton btnAddDigit = new JButton("Open Camera");
        btnAddDigit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	isCancelled = false;
            	JFrame camera = new JFrame("Web Camera");
            	camera.getContentPane().setLayout(new BorderLayout());
            	
            	JButton quitBtn = new JButton("Quit camera");
            	quitBtn.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							isCancelled = true;
							grabber.stop();
							grabber.release();
							grabber = null;
							camera.dispose();
						} catch (org.bytedeco.javacv.FrameGrabber.Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
            		
            	});
            	
            	JButton capture = new JButton("Capture face");
            	JButton recognize = new JButton("Recognize face");
            	JButton addnew = new JButton("Add new face");
            	JPanel btnPanel = new JPanel();
            	btnPanel.setLayout(new BoxLayout(btnPanel,BoxLayout.X_AXIS));
            	capture.setAlignmentX(CENTER_ALIGNMENT);
            	capture.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {
						toCapture = true;
					}
            		
            	});
            	btnPanel.add(capture);
            	recognize.setAlignmentX(CENTER_ALIGNMENT);
            	btnPanel.add(recognize);
            	quitBtn.setAlignmentX(RIGHT_ALIGNMENT);
            	btnPanel.add(quitBtn);
            	addnew.setAlignmentX(LEFT_ALIGNMENT);
            	btnPanel.add(addnew);
            	
            	
            	canvas = new JComponent() {
        			public void paintComponent(Graphics g) {
        				super.paintComponent(g);
        				g.drawImage(image, 0, 0, null);
        				if(changeToTrue) {
        					g.drawLine(40, 40, 100, 100);
        				}
        			}
        		};
            	
        		// Set up webcam
        		grabber = new OpenCVFrameGrabber(0); 
        		// Repeated attempts following discussion on javacv forum, fall 2013 (might be fixed internally in future versions)
        		final int MAX_ATTEMPTS = 60;
        		int attempt = 0;
        		//Initializing camera
        		while (attempt < MAX_ATTEMPTS) {
        			//System.out.print('.');
        			attempt++;
        			try {
        				grabber.start();
        				break;
        			} 
        			catch (Exception e1) {
        				
        			}
        		}
        		if (attempt == MAX_ATTEMPTS) {
        			System.err.println("Failed");
        			return;
        		}
        		System.out.println("ready");
        		
        		
        		
        		width = grabber.getImageWidth();
        		height = grabber.getImageHeight();
            	
        		if (scale != 1) {
        			width = (int)(width*scale*0.8);
        			height = (int)(height*scale);
        		}
        		
            	camera.setSize(width,height + 50);
            	canvas.setPreferredSize(new Dimension(camera.getWidth(), camera.getHeight()));
            	camera.setLocationRelativeTo(null);
            	camera.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            	camera.getContentPane().add(canvas, BorderLayout.PAGE_START);
            	camera.getContentPane().add(btnPanel, BorderLayout.PAGE_END);
            	camera.setResizable(false);
            	camera.setVisible(true);
            	
            	streaming = new CaptureImageThread();
        		streaming.execute();
            }
        });
        //create recognize button
        JButton button = new JButton("Recognize");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	changeToTrue = true;
            }
        });
        //this is the picture panel 
        imgLabel = new JLabel();
        picPanel = new JPanel();
        picPanel.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
        picPanel.add(imgLabel);
        GroupLayout gl_panel = new GroupLayout(panel);
        gl_panel.setHorizontalGroup(
            gl_panel.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panel.createSequentialGroup()
                    .addGap(6)
                    .addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_panel.createSequentialGroup()
                            .addComponent(lblSelectTargetPicture)
                            .addGap(6)
                            .addComponent(btnBrowse))
                        .addGroup(gl_panel.createSequentialGroup()
                            .addGap(10)
                            .addComponent(btnDetect)
                            .addGap(18)
                            .addComponent(btnAddDigit))))
                .addGroup(gl_panel.createSequentialGroup()
                    .addGap(50)
                    .addComponent(button))
                .addGroup(gl_panel.createSequentialGroup()
                    .addContainerGap().addContainerGap()
                    .addComponent(picPanel, GroupLayout.PREFERRED_SIZE, 220, GroupLayout.PREFERRED_SIZE))
        );
        gl_panel.setVerticalGroup(
            gl_panel.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panel.createSequentialGroup()
                    .addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_panel.createSequentialGroup()
                            .addGap(7)
                            .addComponent(lblSelectTargetPicture))
                        .addGroup(gl_panel.createSequentialGroup()
                            .addGap(3)
                            .addComponent(btnBrowse)))
                    .addGap(18)
                    .addComponent(picPanel, GroupLayout.PREFERRED_SIZE, 199, GroupLayout.PREFERRED_SIZE)
                    .addGap(22)
                    .addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
                        .addComponent(btnDetect)
                        .addComponent(btnAddDigit))
                    .addGap(18)
                    .addComponent(button)
                    .addContainerGap())
        );
        
        panel.setLayout(gl_panel);
        
        frame.setVisible(true);
    }
    
    private static class CaptureImageThread extends SwingWorker<Void, Void> {
		protected Void doInBackground() throws Exception {
		    OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
		    Java2DFrameConverter paintConverter = new Java2DFrameConverter();
		    while (!isCancelled) {
				IplImage grabbed = null;
				while (grabbed == null) {
					try {
						grabbed = grabberConverter.convert(grabber.grab());
					}	
					catch (Exception e) {
						System.err.println("render failed");
						Thread.sleep(100); 
					}
				}
				if (mirrored) {
					cvFlip(grabbed, grabbed, 1);
				}
				if (scale != 1) {
					IplImage resized = IplImage.create(width, height, grabbed.depth(), grabbed.nChannels());
					cvResize(grabbed, resized);
					grabbed = resized;
				}
				
				if(toCapture) {
					captureFace(grabbed);
					toCapture = false;
				}
				
				
				
				//draw rectangle
				IplImage grayImg = cvCreateImage(cvGetSize(grabbed), IPL_DEPTH_8U, 1);
				cvCvtColor(grabbed, grayImg, CV_BGR2GRAY);  
				IplImage smallImg = IplImage.create(grayImg.width()/2,grayImg.height()/2, IPL_DEPTH_8U, 1);
				cvResize(grayImg, smallImg, CV_INTER_LINEAR);
				cvEqualizeHist(smallImg, smallImg);
				CvMemStorage storage = CvMemStorage.create();
				CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(cvLoad(CASCADE_FILE));
				CvSeq faces = cvHaarDetectObjects(smallImg, cascade, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
				cvClearMemStorage(storage);
				int totalFace = faces.total();
				for (int i = 0; i < totalFace; i++) {
					faceRec = new CvRect(cvGetSeqElem(faces, i));
					cvRectangle(
							grabbed, 
							cvPoint( faceRec.x()*2, faceRec.y()*2 ),
							cvPoint( (faceRec.x() + faceRec.width())*2, (faceRec.y() + faceRec.height())*2 ), 
							CvScalar.GREEN,
							6, 
							CV_AA, 
							0
							);
				}
				
				Frame frame = grabberConverter.convert(grabbed);
				image = paintConverter.getBufferedImage(frame);
				canvas.repaint();
				Thread.sleep(100); 
			}

			return null;
		}
	}
    
    public static void captureFace(IplImage img) {
  
    	if(faceRec.width() > 0) {
    		CvRect r = new CvRect();
    		r.x(faceRec.x()*2);
    		r.y(faceRec.y()*2);
    		r.width(faceRec.width()*2);
    		r.height(faceRec.height()*2);
    		
            // After setting ROI (Region-Of-Interest) all processing will only be done on the ROI
            cvSetImageROI(img, r);
            IplImage cropped = cvCreateImage(cvGetSize(img), img.depth(), img.nChannels());
            // Copy original image (only ROI) to the cropped image
            cvCopy(img, cropped);            
            cvSaveImage(JOptionPane.showInputDialog("name") + ".jpg", cropped);
    	}
        
        System.out.println("image captured");
    }
    

    public static void main(String[] args) {

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
