import com.github.sarxos.webcam.Webcam;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;

public class WebcamCanvas extends JPanel implements MouseInputListener, KeyListener {
	private static final long serialVersionUID = 1L;
	private ArrayList<ProcessStep> steps = new ArrayList<ProcessStep>();

	public WebcamCanvas(Webcam w) {
		super();
		cam = w;
		canvas = new Canvas();
		setSize(w.getViewSize());
		canvas.setSize(w.getViewSize());
		add(canvas);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
	}

	private Webcam cam;
	private Canvas canvas;
	private BufferedImage lastDrawn;
	private BufferedImage buf;

	private Lock lock;

	public void addStep(ProcessStep step)
	{
		steps.add(step);
	}

	public synchronized void setWebcam(Webcam w) {
		cam.close();
		cam = w;
		setSize(w.getViewSize());
		canvas.setSize(w.getViewSize());
		add(canvas);
	}

	public Webcam getWebcam() {
		return cam;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public BufferedImage lastDrawn() {
		return lastDrawn;
	}

	public synchronized void draw() {
		synchronized (cam)
		{
			if (!cam.isOpen())
			{
				cam.open(true);
			}
			lastDrawn = cam.getImage();

			if (lastDrawn == null)
			{
				return;
			}

			Dimension lastOutSize = cam.getViewSize();
			for (ProcessStep step : steps)
			{
				step.updateOutputDim(lastOutSize);
				lastOutSize = step.getOutputDim();
			}

			if (steps.size() > 0)
			{
				Dimension last = steps.get(steps.size() - 1).getOutputDim();
				resizeBuf(last);
			} else
			{
				Dimension view = cam.getViewSize();
				resizeBuf(view);
			}

			for (ProcessStep step : steps)
			{
				lastDrawn = step.doProcess(lastDrawn);
			}

			Graphics gi = canvas.getGraphics();
			Graphics g = buf.getGraphics();

			g.drawImage(lastDrawn, 0, 0, null);
			gi.drawImage(buf, 0, 0, null);
		}
	}

	private void resizeBuf(Dimension dim)
	{
		if (buf == null || buf.getHeight() != dim.width || buf.getWidth() != dim.height) {
			buf = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_3BYTE_BGR);
			setSize(dim);
			canvas.setSize(dim);
		}
	}

	public void close() {
		cam.close();
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		for(ProcessStep step : steps)
		{
			if(step.handleClick(arg0))
			{
				break;
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}
