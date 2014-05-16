package enc;

//http://java-swing-tips.blogspot.com/2008/07/create-round-image-jbutton.html

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;

class RoundButton extends JButton {

    //Color bckgrndClr = new Color(176, 224, 230);

  public RoundButton() {
    this(null, null);
  }
  public RoundButton(Icon icon) {
    this(null, icon);
  }
  public RoundButton(String text) {
    this(text, null);
  }
  public RoundButton(Action a) {
    this();
    setAction(a);
  }
  public RoundButton(String text, Icon icon) {
    setModel(new DefaultButtonModel());
    init(text, icon);
    if(icon==null) {
      return;
    }
    setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
    //setBackground(bckgrndClr);
    setContentAreaFilled(false);
    setFocusPainted(false);
    //setVerticalAlignment(SwingConstants.TOP);
    setAlignmentY(Component.TOP_ALIGNMENT);
    initShape();
  }
  protected Shape shape, base;
  protected void initShape() {
    if(!getBounds().equals(base)) {
      Dimension s = getPreferredSize();
      base = getBounds();
      shape = new Ellipse2D.Float(0, 0, s.width-1, s.height-1);
    }
  }
  @Override public Dimension getPreferredSize() {
    Icon icon = getIcon();
    Insets i = getInsets();
    int iw = Math.max(icon.getIconWidth(), icon.getIconHeight());
    return new Dimension(iw+i.right+i.left, iw+i.top+i.bottom);
  }
  @Override protected void paintBorder(Graphics g) {
    initShape();
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(getBackground());
    //g2.setStroke(new BasicStroke(1.0f));
    g2.draw(shape);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_OFF);
  }
  @Override public boolean contains(int x, int y) {
    initShape();
    return shape.contains(x, y);
    //or return super.contains(x, y) && ((image.getRGB(x, y) >> 24) & 0xff) > 0;
  }
}
