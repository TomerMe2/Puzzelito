import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;

public class PuzzleGame extends JFrame implements ActionListener{

    private static String _backgroundPath = "Background.jpg";
    private static int _blockMargin = 5;
    private static int _backgrndBorderPixels = 35;
    private static int _backgrndItselfPixels = 402;
    private int _n;
    private int _wholeImgSize;
    private int _singleImgSize;
    private int _padFromBorders;
    private GameImage[][] _images;
    private JButton[][] _btns;
    private Image _background;
    private JLabel _backgroundLbl;
    private SpringLayout _layout;

    public PuzzleGame(int n, URL imagePathURL) {
        super("Puzzelito");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        _n = n;
        detSize();
        cropImage(imagePathURL);
        _background = null;
        try {
            _background = ImageIO.read(getClass().getResource("\\Images\\" + _backgroundPath));

        } catch (IOException e) {
            e.printStackTrace();
        }
        int backgroundSize = (_n)*(_singleImgSize + _blockMargin);
        double backgroundProportion = backgroundSize/_backgrndItselfPixels;
        _padFromBorders = (int) Math.round(_backgrndBorderPixels*backgroundProportion);
        _background = _background.getScaledInstance(backgroundSize + 2*_padFromBorders
                , backgroundSize + 2*_padFromBorders, Image.SCALE_DEFAULT);
        makeButtons();
        getContentPane().add(new JLabel(new ImageIcon(_background)));
        this.setSize(_background.getWidth(this) + _padFromBorders,
                _background.getHeight(this) + 2*_padFromBorders);
        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        //TODO: I AND J ARE FLIPPED
        for (int i=0; i<_n; i++) {
            for (int j=0; j<_n; j++) {
                if (_btns[i][j].getModel().isArmed()) {
                    if (_images[i][j].getEmptyImgDirection() != Directions.NONE) {
                        //An image with a blank space near it was clicked
                        //We need to move the image to the blank space
                        movePicture(_images[i][j].getEmptyImgDirection(), i, j);
                    }
                }
            }
        }
    }

    private void movePicture(Directions dir, int i, int j) {
        JButton temp = _btns[i][j];
        if (dir == Directions.UP) {
            _btns[i][j] = _btns[i-1][j];
            _btns[i-1][j] = temp;
            int pad = i-1 * (_singleImgSize + _blockMargin) + _padFromBorders;
            //getContentPane().remove(_btns[i-1][j]);
            _layout.putConstraint(SpringLayout.NORTH, _btns[i-1][j], pad, SpringLayout.NORTH, getContentPane());
            //getContentPane().add(_btns[i-1][j]);
        }
        if (dir == Directions.DOWN) {
            _btns[i][j] = _btns[i+1][j];
            _btns[i+1][j] = temp;
            int pad = i+1 * (_singleImgSize + _blockMargin) + _padFromBorders;
            _layout.putConstraint(SpringLayout.NORTH, _btns[i+1][j], pad, SpringLayout.NORTH, getContentPane());
        }
        if (dir == Directions.LEFT) {
            _btns[i][j] = _btns[i][j-1];
            _btns[i][j-1] = temp;
            int pad = j-1 * (_singleImgSize + _blockMargin) + _padFromBorders;
            _layout.putConstraint(SpringLayout.NORTH, _btns[i+1][j], pad, SpringLayout.NORTH, getContentPane());
        }
        if (dir == Directions.RIGHT) {
            _btns[i][j] = _btns[i][j+1];
            _btns[i][j+1] = temp;
            int pad = j+1 * (_singleImgSize + _blockMargin) + _padFromBorders;
            _layout.putConstraint(SpringLayout.NORTH, _btns[i+1][j], pad, SpringLayout.NORTH, getContentPane());
        }
    }

    private void cropImage(URL absolutePathURL) {
        _images = new GameImage[_n][_n];
        Image img = null;
        try {
            img = ImageIO.read(absolutePathURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        img = img.getScaledInstance(_wholeImgSize, _wholeImgSize, Image.SCALE_DEFAULT);
        BufferedImage fullBfrd = new BufferedImage(img.getWidth(this), img.getHeight(this),
                BufferedImage.TYPE_INT_ARGB);
        Graphics gr = fullBfrd.getGraphics();
        gr.drawImage(img, 0, 0, this);
        gr.dispose();
        int counter = 0;
        Random rand = new Random();
        int notInGameIndex = rand.nextInt(_n*_n);
        //TODO: MAKE SOMETHING FROM A PIC NOT IN GAME
        for (int i=0; i<_n; i++) {
            for (int j=0; j<_n; j++) {
                BufferedImage cropped = fullBfrd.getSubimage(j*_singleImgSize, i*_singleImgSize,
                        _singleImgSize, _singleImgSize);
                Directions blankImgDir = Directions.NONE;   //Insert default value
                //Check if the notInGame Image is near this image
                if (notInGameIndex == counter+1 & j+1 < _n) {
                    blankImgDir = Directions.RIGHT;
                }
                else if (notInGameIndex == counter-1 & j-1 >= 0) {
                    blankImgDir = Directions.LEFT;
                }
                else if (notInGameIndex == counter + _n & i+1 < _n) {
                    blankImgDir = Directions.DOWN;
                }
                else if (notInGameIndex == counter - _n & i-1 >= 0) {
                    blankImgDir = Directions.UP;
                }
                GameImage gameImg = new GameImage(cropped, counter, blankImgDir);
                _images[i][j] = gameImg;
                //Pick our missing piece
                if (notInGameIndex == counter) {
                    _images[i][j].setIsInGame(false);
                }
                else {
                    _images[i][j].setIsInGame(true);
                }
                counter++;
            }
        }
    }

    //We want the size closest to prefSize, but divideable by _n
    private void detSize() {
        int prefSize = 600; //This is the preffereable window size
        // find the quotient
        int q = prefSize / _n;

        // 1st possible closest number
        int option1 = _n * q;

        // 2nd possible closest number
        int option2;
        if (prefSize*_n > 0) {
            option2 = _n*(q+1);
        }
        else {
            option2 = _n*(q-1);
        }
        if (Math.abs(prefSize - option1) < Math.abs(prefSize - option2))
            _wholeImgSize = option1;
        else {
            _wholeImgSize = option2;
        }
        _singleImgSize = _wholeImgSize/_n;
    }

    //Builds the array of the JButtons and places them on the JPanel
    private void makeButtons() {
        _btns = new JButton[_n][_n];
        _layout = new SpringLayout();
        getContentPane().setLayout(_layout);
        for (int i=0; i<_n; i++) {
            for (int j=0; j<_n; j++) {
                //Create a new button
                JButton current = new JButton();
                makeButtonCoolLooking(current);
                current.setIcon(new ImageIcon(_images[i][j].getBfrdImg()));
                current.addActionListener(this);
                int padNorth = i * (_singleImgSize + _blockMargin) + _padFromBorders;
                int padWest = j * (_singleImgSize + _blockMargin) + _padFromBorders;
                if (_images[i][j].getIsInGame()) {
                    //Place the button
                    getContentPane().add(current);
                    _layout.putConstraint(SpringLayout.NORTH, current, padNorth, SpringLayout.NORTH, getContentPane());
                    _layout.putConstraint(SpringLayout.WEST, current, padWest, SpringLayout.WEST, getContentPane());
                }
                _btns[i][j] = current;
            }
        }
    }

    //Makes the button look like an image
    private void makeButtonCoolLooking(JButton btn) {
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
    }
}
