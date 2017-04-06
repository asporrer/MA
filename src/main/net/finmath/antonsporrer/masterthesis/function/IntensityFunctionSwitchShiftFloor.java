/* 
 * Contact: anton.sporrer@yahoo.com
 */

package main.net.finmath.antonsporrer.masterthesis.function;

import net.finmath.montecarlo.RandomVariable;
import net.finmath.stochastic.RandomVariableInterface;


/**
 * 
 * This class implements the following function operating on the paths of the input implementing RandomVariableInterface.
 * 
 * If the constructor parameter isSwitched is passed as true this function uses the following rule.
 * <br> x -> max( -x - shift , 0 )
 * <br> Otherwise the following rule is applied.
 * <br> x -> max( x - shift , 0 )
 * 
 * @author Anton Sporrer
 *
 */
public class IntensityFunctionSwitchShiftFloor implements RandomVariableFunctionInterface {

	// The shift parameter 
	private double shift;
	
	// The factor parameter
	private double factor;
	
	/**
	 * When using this constructor the function is reflected on the x-axis.
	 * 
	 * @param shift The shift parameter determines how much the function shifts towards negative infinity.
	 */
	public IntensityFunctionSwitchShiftFloor(double shift) {
		this(shift, true);
	}
	
	/**
	 * 
	 * @param shift The shift parameter determines how much the function shifts towards negative infinity.
	 * @param isSwitched If true the function reflects the input with respect to the x axis.
	 */
	public IntensityFunctionSwitchShiftFloor(double shift, boolean isSwitched) {
		this.shift = shift;
		if(isSwitched) {this.factor = -1.0;}
		else {this.factor = 1.0;}
	}
	
	/**
	 * The function is applied path-wise to the input.
	 */
	public RandomVariableInterface getValue(RandomVariableInterface input) {
	
		RandomVariableInterface output = null;
		
		output = (new RandomVariable(-shift)).addProduct(input, factor).floor(0);
		
		return output;
		
	}

	
	
}
