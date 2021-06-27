import java.awt.geom.Rectangle2D;

//Фрактал BurningShip
public class BurningShip extends FractalGenerator
{
    //максимальное значение
    public static final int MAX_ITERATIONS = 2000;

    //позволяет генератору определить наиболее "интересную" область коплексной плоскости для фрактала
    public void getInitialRange(Rectangle2D.Double range)
    {
        range.x = -2;
        range.y = -2.5;
        range.width = 4;
        range.height = 4;
    }

    //реализует итеративную функцию для фрактала BurningShip
    public int numIterations(double x, double y)
    {
        int iteration = 0;
        double zreal = 0;
        double zimaginary = 0;
        while (iteration < MAX_ITERATIONS &&
               zreal * zreal + zimaginary * zimaginary < 4)
        {
            double zrealUpdated = zreal * zreal - zimaginary * zimaginary + x;
            double zimaginaryUpdated = 2 * Math.abs(zreal)
            * Math.abs(zimaginary) + y;
            
            zreal = zrealUpdated;
            zimaginary = zimaginaryUpdated;
            
            iteration += 1;
        }

        if (iteration == MAX_ITERATIONS)
        {
            return -1;
        }
        
        return iteration;
     }

    public String toString() {
        return "Burning Ship";
    }
    
}