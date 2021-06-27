import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.geom.Rectangle2D;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class FractalExplorer {
    private JButton saveButton;
    private JButton resetButton;
    private JComboBox myComboBox;
    private int rowsRemaining;
    private int displaySize; //размер дисплея
    private JImageDisplay display; //для обновления отображения в разных методах
    private FractalGenerator fractal; //будет использоваться ссылка на баззовый класс для отображения других фракталов
    private Rectangle2D.Double range; //указывает диапазон коплексной плоскости, выводящийся на экран

    public FractalExplorer(int size) {
        //размер дисплея
        displaySize = size;
        //инициализирует фрактал генератор
        fractal = new Mandelbrot();
        range = new Rectangle2D.Double();
        fractal.getInitialRange(range);
        display = new JImageDisplay(displaySize, displaySize);
    }

    //создаем и рисуем на окне
    public void createAndShowGUI() {
        //рамка для java.awt.BorderLayout
        display.setLayout(new BorderLayout());
        JFrame myframe = new JFrame("Fractal Explorer");
        //изображение
        myframe.add(display, BorderLayout.CENTER);
        //кнопка сброса
        resetButton = new JButton("Reset");
        //сброс кнопки сброса //5
        ButtonHandler resetHandler = new ButtonHandler();
        resetButton.addActionListener(resetHandler);
        //приближение
        MouseHandler click = new MouseHandler();
        display.addMouseListener(click);
        //кнопка закрыть //5
        myframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //добавляем combo box //5
        myComboBox = new JComboBox();
        //добавляем типы фракталов в combo box
        FractalGenerator mandelbrotFractal = new Mandelbrot();
        myComboBox.addItem(mandelbrotFractal);
        FractalGenerator tricornFractal = new Tricorn();
        myComboBox.addItem(tricornFractal);
        FractalGenerator burningShipFractal = new BurningShip();
        myComboBox.addItem(burningShipFractal);
        //добавляем выбор в btnH //5
        ButtonHandler fractalChooser = new ButtonHandler();
        myComboBox.addActionListener(fractalChooser);
        //Создаем панель JPanel //5
        JPanel myPanel = new JPanel();
        JLabel myLabel = new JLabel("Fractal:");
        myPanel.add(myLabel);
        myPanel.add(myComboBox);
        myframe.add(myPanel, BorderLayout.NORTH);
        //кнорка сейв //5
        saveButton = new JButton("Save");
        JPanel myBottomPanel = new JPanel();
        myBottomPanel.add(saveButton);
        myBottomPanel.add(resetButton);
        myframe.add(myBottomPanel, BorderLayout.SOUTH);
        //кнопка сейв в btnH //5
        ButtonHandler saveHandler = new ButtonHandler();
        saveButton.addActionListener(saveHandler);
        //запрет растяжения и вывод //5
        myframe.pack();
        myframe.setVisible(true);
        myframe.setResizable(false);
    }

    //рисуем фрактал//6
    private void drawFractal()
    {
        enableUI(false);
        //размер
        rowsRemaining = displaySize;
        //проходим через каждую строку чтобы отрисовать
        for (int x=0; x<displaySize; x++){
            FractalWorker drawRow = new FractalWorker(x);
            drawRow.execute();
        }
    }

    //включение/выключение кнопок интерфейса //6
    private void enableUI(boolean val) {
        myComboBox.setEnabled(val);
        resetButton.setEnabled(val);
        saveButton.setEnabled(val);
    }

    //события btnH //5
    private class ButtonHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            //источник действия
            String command = e.getActionCommand();
            //если выбран combo box, то выводим фрактал
            if (e.getSource() instanceof JComboBox) {
                JComboBox mySource = (JComboBox) e.getSource();
                fractal = (FractalGenerator) mySource.getSelectedItem();
                fractal.getInitialRange(range);
                drawFractal();

            }
            //если кнопка сброса, то перерисовывем
            else if (command.equals("Reset")) {
                fractal.getInitialRange(range);
                drawFractal();
            }
            //если кнопка сохранения, то сохраняем
            else if (command.equals("Save")) {
                //выбираем файл для сохранения
                JFileChooser myFileChooser = new JFileChooser();
                //сохраняем в png
                FileFilter extensionFilter =
                        new FileNameExtensionFilter("PNG Images", "png");
                myFileChooser.setFileFilter(extensionFilter);
                //выбираем имя файла
                myFileChooser.setAcceptAllFileFilterUsed(false);
                //выбираем каталог
                int userSelection = myFileChooser.showSaveDialog(display);
                //пересохранение
                if (userSelection == JFileChooser.APPROVE_OPTION) {

                    //получаем название файла
                    java.io.File file = myFileChooser.getSelectedFile();
                    String file_name = file.toString();

                    try {
                        BufferedImage displayImage = display.getImage();
                        javax.imageio.ImageIO.write(displayImage, "png", file);
                    }
                    catch (Exception exception) {
                        JOptionPane.showMessageDialog(display,
                                exception.getMessage(), "Cannot Save Image",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
                else return;
            }
        }
    }

    //класс для обработки событий MouseListener с дисплея
    private class MouseHandler extends MouseAdapter {
        //приблежает при щелчке
        @Override
        public void mouseClicked(MouseEvent e) {
            //получаем х при щелчке
            if (rowsRemaining != 0) {
                return;
            }
            int x = e.getX();
            double xCoord = fractal.getCoord(range.x, range.x + range.width, displaySize, x);
            //получаем у при щелчке
            int y = e.getY();
            double yCoord = fractal.getCoord(range.y, range.y + range.height, displaySize, y);
            //вызов приблежения с увеличение 2 раза
            fractal.recenterAndZoomRange(range, xCoord, yCoord, 0.5);
            //перерисовываем фрактал
            drawFractal();
        }
    }

    //Вычисляем цвет для линии фрактала //6
    private class FractalWorker extends SwingWorker<Object, Object>
    {
        int yCoordinate;
        //цвет полосы
        int[] computedRGBValues;
        //храним строку
        private FractalWorker(int row) {
            yCoordinate = row;
        }
        //рисуем
        protected Object doInBackground() {
            computedRGBValues = new int[displaySize];
            //проходим через всю строку
            for (int i = 0; i < computedRGBValues.length; i++) {

                // соответствие между строкой и координатами
                double xCoord = fractal.getCoord(range.x,
                        range.x + range.width, displaySize, i);
                double yCoord = fractal.getCoord(range.y,
                        range.y + range.height, displaySize, yCoordinate);
                //вычисляем количество итераций
                int iteration = fractal.numIterations(xCoord, yCoord);
                //если количество итераций -1, черный цвет
                if (iteration == -1){
                    computedRGBValues[i] = 0;
                }
                else {
                    //устанавливаем цвет
                    float hue = 0.7f + (float) iteration / 200f;
                    int rgbColor = Color.HSBtoRGB(hue, 1f, 1f);
                    //обновляем цвет пикселя
                    computedRGBValues[i] = rgbColor;
                }
            }
            return null;

        }
        //рисуем фракталочки
        protected void done() {
            // перерисовываем
            for (int i = 0; i < computedRGBValues.length; i++) {
                display.drawPixel(i, yCoordinate, computedRGBValues[i]);
            }
            display.repaint(0, 0, yCoordinate, displaySize, 1);
            rowsRemaining--;
            if (rowsRemaining == 0) {
                enableUI(true);
            }
        }
    }


    //запускаем это дерьмо с размером 600*600
    public static void main(String[] args) {
        FractalExplorer displayExplorer = new FractalExplorer(600);
        displayExplorer.createAndShowGUI();
        displayExplorer.drawFractal();
    }
}
