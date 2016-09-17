package Main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
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
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;
import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_core.cvFlip;
import static org.bytedeco.javacpp.opencv_core.cvGetSize;
import static org.bytedeco.javacpp.opencv_core.cvPoint;
import static org.bytedeco.javacpp.opencv_imgproc.CV_AA;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.CV_INTER_LINEAR;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.cvEqualizeHist;
import static org.bytedeco.javacpp.opencv_imgproc.cvRectangle;
import static org.bytedeco.javacpp.opencv_imgproc.cvResize;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_core.cvCopy;
import static org.bytedeco.javacpp.opencv_core.cvSetImageROI;

import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvScalar;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import static org.bytedeco.javacpp.opencv_objdetect.*;
import static org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.RectVector;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;

import static org.bytedeco.javacpp.opencv_face.createFisherFaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;

import static java.nio.file.StandardCopyOption.*;

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
    private static boolean toRecognize = false;
    
    private static IplImage gray;
    
    private static IplImage grabbed;
    
    private static int location = 0;
        
    private static String imagePath;
   // private static CvRect faceRec;
    private static Rect rec;
//    private static final String CASCADE_FILE = "haarcascade_frontalface_alt.xml";
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
                        imagePath = file.getAbsolutePath();
                        imgLabel.setIcon(new ImageIcon(imagePath));
                    } 
            
                } catch (Exception iOException) {
                }
     
        	
            }
        });
        
        //create label for browse button
        JLabel lblSelectTargetPicture = new JLabel("select image");

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
            	recognize.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent arg0) {
						recoFace();
					}
            		
            	});
            	JButton addnew = new JButton("Add new face");
            	addnew.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {
						toCapture = true;
						location = 1;
					}
            		
            	});
            	JPanel btnPanel = new JPanel();
            	btnPanel.setLayout(new BoxLayout(btnPanel,BoxLayout.X_AXIS));
            	capture.setAlignmentX(CENTER_ALIGNMENT);
            	capture.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {
						toCapture = true;
						location = 0;
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
        			width = (int)(width);
        			height = (int)(height);
        		}
        		
            	camera.setSize(width,height+80);
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
            	recoFaceFromPic(imagePath);
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
		     CascadeClassifier cascade = new CascadeClassifier();
		     cascade.load("haarcascade_frontalface_alt.xml");
		    OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
		    Java2DFrameConverter paintConverter = new Java2DFrameConverter();
		    while (!isCancelled) {
				grabbed = null;
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
					if(toCapture) {
						saveFace(location);
						toCapture = false;
						location = 0;
					}
					if(toRecognize) {
						recoFace();
						toRecognize = false;
					}
				}

				//draw rectangle
				IplImage grayImg = cvCreateImage(cvGetSize(grabbed), IPL_DEPTH_8U, 1);
				cvCvtColor(grabbed, grayImg, CV_BGR2GRAY);  
				IplImage smallImg = IplImage.create(grayImg.width()/2,grayImg.height()/2, IPL_DEPTH_8U, 1);
				Mat img = new Mat(smallImg);
				cvResize(grayImg, smallImg, CV_INTER_LINEAR);
				cvEqualizeHist(smallImg, smallImg);
				CvMemStorage storage = CvMemStorage.create();
				RectVector faces = new RectVector();

				cascade.detectMultiScale(img, faces, 1.3, 3, CV_HAAR_FIND_BIGGEST_OBJECT, null, null);
				cvClearMemStorage(storage);

				for (int i = 0; i <faces.size(); i++) {

					rec = faces.get(i);
					cvRectangle(
							grabbed, 
							cvPoint( rec.x()*2, rec.y()*2 ),
							cvPoint( (rec.x() + rec.width())*2, (rec.y() + rec.height())*2 ), 
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
    
    public static void captureFace() {
    	IplImage img = grabbed;
    	if(rec.width() > 0) {
    		CvRect r = new CvRect();
    		r.x(rec.x()*2);
    		r.y(rec.y()*2);
    		r.width(rec.width()*2);
    		r.height(rec.height()*2);
    		
            // After setting ROI (Region-Of-Interest) all processing will only be done on the ROI
            cvSetImageROI(img, r);
            IplImage cropped = cvCreateImage(cvGetSize(img), img.depth(), img.nChannels());
            // Copy original image (only ROI) to the cropped image
            cvCopy(img, cropped);
            Mat inputImg = new Mat(cropped);
            Mat resultImg = new Mat();
            Size ruler = new Size(120,120); 
            org.bytedeco.javacpp.opencv_imgproc.resize(inputImg, resultImg, ruler);

            IplImage beforeGray = new IplImage(resultImg);
            gray = cvCreateImage(cvGetSize(beforeGray), IPL_DEPTH_8U, 1);
            cvCvtColor(beforeGray, gray, CV_BGR2GRAY); 
    	}
    }
    
    public static void saveFace(int locat) throws IOException {
    	captureFace();
    	String name = JOptionPane.showInputDialog("name");
    	if(name != null) {
    		cvSaveImage( name + ".jpg", gray);
    		if(locat == 1) {
        		Path source = FileSystems.getDefault().getPath(name+".jpg");
        		Path dest = FileSystems.getDefault().getPath("trainingImags/" + name+".jpg");
        		Files.move(source, dest, REPLACE_EXISTING);   			
    		}

    		System.out.println("image saved");
    	}  
    }
    
    public static void recoFace() {

    	if(rec.width()>0) {
    	captureFace();
    	Map<Integer, String> hm = new HashMap<Integer, String>();
    	File root = new File("trainingImags");
    	 FilenameFilter imgFilter = new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 name = name.toLowerCase();
                 return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
             }
         };
         
    	File[] imageFiles = root.listFiles(imgFilter);
    	MatVector images = new MatVector(imageFiles.length);
    	Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
        IntBuffer labelsBuf = labels.createBuffer();

        int counter = 0;

        for (File image : imageFiles) {
            Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            
            String[] temp = image.getName().split("\\-");
            int label = Integer.parseInt(temp[0]);
            hm.put(label, temp[1]);

            images.put(counter, img);

            labelsBuf.put(counter, label);

            counter++;
        }

        //FaceRecognizer faceRecognizer = createFisherFaceRecognizer();
        // FaceRecognizer faceRecognizer = createEigenFaceRecognizer();
         FaceRecognizer faceRecognizer = createLBPHFaceRecognizer();

        faceRecognizer.train(images, labels);
        Mat m = new Mat(gray);
        int predictedLabel = faceRecognizer.predict(m);

        System.out.println("Reconized: " + hm.get(predictedLabel));
    	hm.clear();
    	} else {
    		System.out.println("no face");
    	}
    	
    }
    
    private static void recoFaceFromPic(String name) {
    	Map<Integer, String> hm = new HashMap<Integer, String>();

    	// get rectangle data
        Mat testImage = imread(name, CV_LOAD_IMAGE_GRAYSCALE);
        IplImage picture = new IplImage(testImage);
        CascadeClassifier cascade = new CascadeClassifier();
	    cascade.load("haarcascade_frontalface_alt.xml");
	    IplImage grayImg = cvCreateImage(cvGetSize(picture), IPL_DEPTH_8U, 1);
		cvCvtColor(picture, grayImg, CV_BGR2GRAY);  
		IplImage smallImg = IplImage.create(grayImg.width()/2,grayImg.height()/2, IPL_DEPTH_8U, 1);
		Mat image = new Mat(smallImg);
		cvResize(grayImg, smallImg, CV_INTER_LINEAR);
		cvEqualizeHist(smallImg, smallImg);
		RectVector faces = new RectVector();
		cascade.detectMultiScale(image, faces, 1.3, 3, CV_HAAR_FIND_BIGGEST_OBJECT, null, null);
	    Rect picRect = faces.get(0);
	    
	    
	    // crop to get gray image
	    CvRect r = new CvRect();
		r.x(picRect.x()*2);
		r.y(picRect.y()*2);
		r.width(picRect.width()*2);
		r.height(picRect.height()*2);
		
        cvSetImageROI(picture, r);
        IplImage cropped = cvCreateImage(cvGetSize(picture), picture.depth(), picture.nChannels());
        cvCopy(picture, cropped);
        Mat inputImg = new Mat(cropped);
        Mat resultImg = new Mat();
        Size ruler = new Size(120,120); 
        org.bytedeco.javacpp.opencv_imgproc.resize(inputImg, resultImg, ruler);

        IplImage beforeGray = new IplImage(resultImg);
        IplImage finalTestImage = cvCreateImage(cvGetSize(beforeGray), IPL_DEPTH_8U, 1);
        cvCvtColor(beforeGray, finalTestImage, CV_BGR2GRAY);
	    
        File root = new File("trainingImags");

        FilenameFilter imgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
            }
        };

        File[] imageFiles = root.listFiles(imgFilter);

        MatVector images = new MatVector(imageFiles.length);

        Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
        IntBuffer labelsBuf = labels.createBuffer();

        int counter = 0;

        for (File tempImage : imageFiles) {
            Mat img = imread(tempImage.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            
            String[] temp = tempImage.getName().split("\\-");
            int label = Integer.parseInt(temp[0]);
            hm.put(label, temp[1]);

            images.put(counter, img);

            labelsBuf.put(counter, label);

            counter++;
        }

        //FaceRecognizer faceRecognizer = createFisherFaceRecognizer();
         FaceRecognizer faceRecognizer = createEigenFaceRecognizer();
        // FaceRecognizer faceRecognizer = createLBPHFaceRecognizer()

        faceRecognizer.train(images, labels);

        int predictedLabel = faceRecognizer.predict(new Mat(finalTestImage));

        System.out.println("Pic: Recognized: " + hm.get(predictedLabel));
    
    }
    

    public static void main(String[] args) {

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
