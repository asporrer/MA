package main.net.finmath.antonsporrer.masterthesis.montecarlo.cva.NPVAndDefaultsimulation;

import main.net.finmath.antonsporrer.masterthesis.montecarlo.ProductConditionalFairValue_ModelInterface;
import main.net.finmath.antonsporrer.masterthesis.montecarlo.product.ProductConditionalFairValueProcessInterface;
import net.finmath.exception.CalculationException;
import net.finmath.stochastic.RandomVariableInterface;


/**
 * 
 * Function and intensity model have to be compatible!
 * 
 * @author Anton Sporrer
 *
 */
public class NPVAndDefaultIntensityFunctionSimulation<T extends  ProductConditionalFairValue_ModelInterface> extends AbstractNPVAndDefaultIntensitySimulation<T>{

	//TODO: Create Class Default Intensity Function and add to constructor.
	
	public NPVAndDefaultIntensityFunctionSimulation(
			T underlyingModel,
			ProductConditionalFairValueProcessInterface<T> productProcess, int seed) {
		super(underlyingModel, productProcess);
	}

	public RandomVariableInterface getIntensity(int timeIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public RandomVariableInterface getExpOfIntegratedIntensity(int timeIndex) throws CalculationException {
		// TODO Use if-statement to treat the following case. The intensity model provides a getExpOfIntegratedIntensity function. 
		return super.getExpOfIntegratedIntensity(timeIndex);
	}
	
}