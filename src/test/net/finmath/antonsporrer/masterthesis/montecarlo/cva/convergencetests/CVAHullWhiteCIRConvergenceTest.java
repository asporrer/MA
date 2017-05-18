package test.net.finmath.antonsporrer.masterthesis.montecarlo.cva.convergencetests;


import java.util.HashMap;
import java.util.Map;

import main.net.finmath.antonsporrer.masterthesis.function.KahanSummation;
import main.net.finmath.antonsporrer.masterthesis.function.StatisticalFunctions;
import main.net.finmath.antonsporrer.masterthesis.integration.Integration.IntegrationMethod;
import main.net.finmath.antonsporrer.masterthesis.modifiedFromFinmathLib.HullWhiteModel;
import main.net.finmath.antonsporrer.masterthesis.montecarlo.cva.IntensityBasedCVA;
import main.net.finmath.antonsporrer.masterthesis.montecarlo.cva.NPVAndDefaultsimulation.NPVAndCorrelatedDefaultIntensitySimulation;
import main.net.finmath.antonsporrer.masterthesis.montecarlo.cva.NPVAndDefaultsimulation.NPVAndDefaultIntensitySimulationInterface;
import main.net.finmath.antonsporrer.masterthesis.montecarlo.intensitymodel.CIRModel;
import main.net.finmath.antonsporrer.masterthesis.montecarlo.intensitymodel.IntensityModelInterface;
import main.net.finmath.antonsporrer.masterthesis.montecarlo.intermodelbmcorrelation.Correlation;
import main.net.finmath.antonsporrer.masterthesis.montecarlo.intermodelbmcorrelation.CorrelationInterface;
import main.net.finmath.antonsporrer.masterthesis.montecarlo.product.CouponBondConditionalFairValueProcess;
import main.net.finmath.antonsporrer.masterthesis.montecarlo.product.ProductConditionalFairValueProcessInterface;
import main.net.finmath.antonsporrer.masterthesis.montecarlo.product.SwapConditionalFairValueProcess;
import net.finmath.exception.CalculationException;
import net.finmath.stochastic.RandomVariableInterface;
import test.net.finmath.antonsporrer.masterthesis.montecarlo.HullWhiteCreationHelper;

public class CVAHullWhiteCIRConvergenceTest {
	
	HashMap<Integer, Integer> pathsPerCVANumberOfCVAs;
	
	int numberOfPaths;
	int seed = 3142;
	
	int numberOfTimeSteps = 90;
	double timeStepSize = 1.0 / numberOfTimeSteps;
	
	HullWhiteModel underlyingModel; 
	
	IntensityModelInterface intensityModel;
	
	CorrelationInterface correlation;
	
	NPVAndDefaultIntensitySimulationInterface<HullWhiteModel> npvAndDefaultIntensitySimulation;
	
	IntensityBasedCVA intensityBasedCVA;
	
	
	
	public CVAHullWhiteCIRConvergenceTest() {
		System.out.println("Heap Space: " + java.lang.Runtime.getRuntime().maxMemory());

		// CVAs from independent simulations and their relative error are printed.
		pathsPerCVANumberOfCVAs = new HashMap<Integer, Integer>();
//		pathsPerCVANumberOfCVAs.put(1000, 10);
		pathsPerCVANumberOfCVAs.put(10000, 10);
//		pathsPerCVANumberOfCVAs.put(100000, 10);
//		pathsPerCVANumberOfCVAs.put(500000, 1);
		
		numberOfPaths = 0;
		for(Map.Entry<Integer, Integer> entry : pathsPerCVANumberOfCVAs.entrySet() ) {
			numberOfPaths += entry.getKey() * entry.getValue();
		}
		
		double[] meanReversion = new double[numberOfTimeSteps+1];
		double[] volatilities = new double[numberOfTimeSteps+1];
		double[] forwardRates = new double[] {0.02, 0.05, 0.01, 0.05, 0.01};
		
		for(int index = 0; index < numberOfTimeSteps + 1; index++) {
			meanReversion[index] = 0.05;
			volatilities[index] = 0.03;
		}
		
		underlyingModel = HullWhiteCreationHelper.createHullWhiteModel(0.0, numberOfTimeSteps, timeStepSize, meanReversion, volatilities, forwardRates, numberOfPaths ); 
		
		ProductConditionalFairValueProcessInterface<HullWhiteModel> productProcess = new CouponBondConditionalFairValueProcess<HullWhiteModel>(underlyingModel, new double[] {numberOfTimeSteps * timeStepSize }, new double[] {1.0}, new double[] {1.0});
		
//		ProductConditionalFairValueProcessInterface<HullWhiteModel> productProcess = new SwapConditionalFairValueProcess<HullWhiteModel>(underlyingModel, new double[] {numberOfTimeSteps/3 * timeStepSize, 2*numberOfTimeSteps/3 * timeStepSize, numberOfTimeSteps*timeStepSize });
		
		IntensityModelInterface intensityModel = new CIRModel(0.01, 0.05 , 0.01, 0.03);
		
		CorrelationInterface correlation = new Correlation(new double[][]{{0.9},{0.0}});
		
		npvAndDefaultIntensitySimulation = new NPVAndCorrelatedDefaultIntensitySimulation<HullWhiteModel>(underlyingModel, productProcess, intensityModel , correlation , seed);
		
		double lossGivenDefault = 1.0;
		intensityBasedCVA = new IntensityBasedCVA(lossGivenDefault);
		

		
	}
	
	public void increasingSampleSizePlot(double[] realizations) {

		double[] increasingSampleSizeMeanArray = getIncreasingSampleSizeMeanArray( realizations );
		
		int stepSize = Math.max(1, numberOfPaths / 10);
		
		System.out.println("Printing the increasing sample size mean.");
		
		
		int index = 0;
		while(index < numberOfPaths) {
			System.out.println( Double.toString( increasingSampleSizeMeanArray[index] ).replace('.', ',') );
			index += stepSize;
		}
		
//		for(int i = 0; i < 1000; i+=10) {
//			System.out.println( Double.toString( increasingSampleSizeMeanArray[i] ).replace('.', ',') );
//		}
//		
//		for(int i = 1000; i < 10000; i+=100) {
//			System.out.println( Double.toString( increasingSampleSizeMeanArray[i] ).replace('.', ',') );
//		}
//		
//		for(int i = 10000; i< 1000000; i+=10000){
//			System.out.println( Double.toString( increasingSampleSizeMeanArray[i] ).replace('.', ',') );
//		}
	}
	
	public void calculatePrintCVAs(double[] realizations) throws CalculationException {
		
		

				
		HashMap<Integer, double[]> cvaHashMap = new HashMap<Integer, double[]>();
		
		int currentStartIndex = 0;
		int currentNumOfPaths = 0;
		int currentNumOfCVAs = 0;
		
		for(Map.Entry<Integer, Integer> numberOfPathsNumberOfCVAsEntry : pathsPerCVANumberOfCVAs.entrySet() ) {
			
			currentStartIndex += currentNumOfPaths*currentNumOfCVAs; 
			currentNumOfPaths = numberOfPathsNumberOfCVAsEntry.getKey();
			currentNumOfCVAs = numberOfPathsNumberOfCVAsEntry.getValue();
			
			cvaHashMap.put( currentNumOfPaths , getArithmeticMeans(realizations, currentStartIndex, currentNumOfCVAs , currentNumOfPaths) );
		}
		

		double[] currentCVAArray = null; 
		for(Map.Entry<Integer, double[]> cvaHashMapEntry: cvaHashMap.entrySet()) {
		
			currentCVAArray = cvaHashMapEntry.getValue();
			
			System.out.println( "Number of Paths is " + cvaHashMapEntry.getKey() + " for CVAs: ");
			System.out.println( "The mean relative error is: " + KahanSummation.getValue( StatisticalFunctions.getRelativeDeviationsWRTMean(currentCVAArray)) );
			for(int index = 0; index < currentCVAArray.length; index++) {
				System.out.println(  Double.toString( currentCVAArray[index] ).replace('.', ',') );
			}
			
		}

	}
	
	
	

	public void testCouponBond() throws CalculationException {
		
		// For-Loop over different products. paymentDates have to fit to numberOfTimeSteps * timeStepSize !!!
		
		CouponBondConditionalFairValueProcess<HullWhiteModel> productProcess = null;
		
		int numberOfPaymentDates = 10; 
		
		double[] paymentDates = new double[numberOfPaymentDates];
		double[] periodFactors = new double[numberOfPaymentDates];
		double[] coupons = new double[numberOfPaymentDates];
		
		double maturity = numberOfTimeSteps * timeStepSize;
		double periodStepSize = maturity / (double) numberOfPaymentDates;
		
		for(int index = 0; index < numberOfPaymentDates ; index++) {
			
			paymentDates[index] = (index+1) * periodStepSize;
			periodFactors[index] = periodStepSize;
			coupons[index] = 0.1;
		}

		double[] currentPaymentDates = null;
		double[] currentPeriodFactors = null;
		double[] currentCoupons = null;
		
		for(int index = 0; index < numberOfPaymentDates ; index++) {
			
			System.out.println("Printing the " + index + "th coupon bond.");
			
			currentPaymentDates = java.util.Arrays.copyOfRange(paymentDates, index, numberOfPaymentDates);
			currentPeriodFactors = java.util.Arrays.copyOfRange(periodFactors, index, numberOfPaymentDates);
			currentCoupons = java.util.Arrays.copyOfRange(coupons, index, numberOfPaymentDates);
			
			productProcess = new CouponBondConditionalFairValueProcess<HullWhiteModel>(null, currentPaymentDates, currentPeriodFactors, currentCoupons);
			
			npvAndDefaultIntensitySimulation.plugInProductProcess(productProcess);
			
			RandomVariableInterface realizationsRV = intensityBasedCVA.getCVA( npvAndDefaultIntensitySimulation, IntegrationMethod.Trapezoidal );
			
			calculatePrintCVAs(realizationsRV.getRealizations());
		
			increasingSampleSizePlot(realizationsRV.getRealizations());
			
		}
		
	}
	

	public void testSwap() throws CalculationException {
		
		// For-Loop over different products. paymentDates have to fit to numberOfTimeSteps * timeStepSize !!!
		
		SwapConditionalFairValueProcess<HullWhiteModel> productProcess = null;
		
		int numberOfPaymentDates = 10; 
		
		double[] paymentFixingDates = new double[numberOfPaymentDates];

		double maturity = numberOfTimeSteps * timeStepSize;
		double periodStepSize = maturity / (double) numberOfPaymentDates;
		
		for(int index = 0; index < numberOfPaymentDates ; index++) {
			paymentFixingDates[index] = (index+1) * periodStepSize;
		}
		
		double[] currentPaymentFixingDates = null;

		
		for(int index = 0; index < numberOfPaymentDates - 1 /* "- 1" since a swap needs at least to dates. */ ; index++) {
			
			System.out.println("Printing the " + index + "th swap.");
			
			currentPaymentFixingDates = java.util.Arrays.copyOfRange(paymentFixingDates, index, numberOfPaymentDates);

			productProcess = new SwapConditionalFairValueProcess<HullWhiteModel>(underlyingModel, currentPaymentFixingDates); 
			
			npvAndDefaultIntensitySimulation.plugInProductProcess(productProcess);
			
			RandomVariableInterface realizationsRV = intensityBasedCVA.getCVA( npvAndDefaultIntensitySimulation, IntegrationMethod.Trapezoidal );
			
			calculatePrintCVAs(realizationsRV.getRealizations());
		
			increasingSampleSizePlot(realizationsRV.getRealizations());
			
		}

	}
	
	
	public static void main(String[] args) throws CalculationException {


		CVAHullWhiteCIRConvergenceTest cvaHullWhiteCIRConvergenceTest = new CVAHullWhiteCIRConvergenceTest();
		
		cvaHullWhiteCIRConvergenceTest.testCouponBond();
		
		cvaHullWhiteCIRConvergenceTest.testSwap();
		
		// CVAs from independent simulations and their relative error are printed.
//		HashMap<Integer, Integer> pathsPerCVANumberOfCVAs = new HashMap<Integer, Integer>();
//		pathsPerCVANumberOfCVAs.put(1000, 10);
//		pathsPerCVANumberOfCVAs.put(10000, 10);
//		pathsPerCVANumberOfCVAs.put(100000, 10);
//		pathsPerCVANumberOfCVAs.put(500000, 1);
//		
//		calculatePrintCVAs(pathsPerCVANumberOfCVAs, 10, 3161);
		
		
		//
//		int numberOfPaths = 1000000;
//		int numberOfTimeSteps = 50;
//		double[] increasingSampleSizeMeanArray = getIncreasingSampleSizeMeanArray( getCVA(numberOfPaths, numberOfTimeSteps).getRealizations() );
//		
//		for(int i = 0; i < 1000; i+=10) {
//			System.out.println( Double.toString( increasingSampleSizeMeanArray[i] ).replace('.', ',') );
//		}
//		
//		for(int i = 1000; i < 10000; i+=100) {
//			System.out.println( Double.toString( increasingSampleSizeMeanArray[i] ).replace('.', ',') );
//		}
//		
//		for(int i = 10000; i< 1000000; i+=10000){
//			System.out.println( Double.toString( increasingSampleSizeMeanArray[i] ).replace('.', ',') );
//		}
		
	}

	
	public static void calculatePrintCVAs(HashMap<Integer, Integer> pathsPerCVANumberOfCVAs, int numberOfTimeSteps, int seed) throws CalculationException {
		
		int numberOfPaths = 0;
		for(Map.Entry<Integer, Integer> entry : pathsPerCVANumberOfCVAs.entrySet() ) {
			numberOfPaths += entry.getKey() * entry.getValue();
		}
		
		double startTimeRV = System.currentTimeMillis();
		double[] realizations = getCVA( numberOfPaths, numberOfTimeSteps, seed ).getRealizations();
		double endTimeRV = System.currentTimeMillis();
		
		double startTimeArithmeticMean = System.currentTimeMillis();		
		
		HashMap<Integer, double[]> cvaHashMap = new HashMap<Integer, double[]>();
		
		int currentStartIndex = 0;
		int currentNumOfPaths = 0;
		int currentNumOfCVAs = 0;
		
		for(Map.Entry<Integer, Integer> numberOfPathsNumberOfCVAsEntry : pathsPerCVANumberOfCVAs.entrySet() ) {
			
			currentStartIndex += currentNumOfPaths*currentNumOfCVAs; 
			currentNumOfPaths = numberOfPathsNumberOfCVAsEntry.getKey();
			currentNumOfCVAs = numberOfPathsNumberOfCVAsEntry.getValue();
			
			cvaHashMap.put( currentNumOfPaths , getArithmeticMeans(realizations, currentStartIndex, currentNumOfCVAs , currentNumOfPaths) );
		}
		
		double endTimeArithmeticMean = System.currentTimeMillis();
		
//		cvaList.add(, getArithmeticMeans(realizations, 0,1 ,889000));
//		cvaList.add(getArithmeticMeans(realizations,  890000, 10 , 100000));
//		cvaList.add(getArithmeticMeans(realizations, 1890000, 10 , 10000));
//		cvaList.add(getArithmeticMeans(realizations, 1990000, 10 , 1000));
//		double endTimeArithmeticMean = System.currentTimeMillis();
//	
//		int listItemNumber = 0;
//		for(double[] cvasArray: cvaHashMap) {
//			
//			System.out.println( ++listItemNumber + ". Set of CVA: ");
//			for(int index = 0; index < cvasArray.length; index++) {
//				System.out.println(  Double.toString( cvasArray[index] ).replace('.', ',') );
//			}
//			
//		}
//		
//		System.out.println("CVA RV calculation: " + (endTimeRV - startTimeRV));
//		System.out.println("Arithmetic Mean calculation: " + (endTimeArithmeticMean - startTimeArithmeticMean));
		
		double[] currentCVAArray = null; 
		for(Map.Entry<Integer, double[]> cvaHashMapEntry: cvaHashMap.entrySet()) {
		
			currentCVAArray = cvaHashMapEntry.getValue();
			
			System.out.println( "Number of Paths is " + cvaHashMapEntry.getKey() + " for CVAs: ");
			System.out.println( "The mean relative error is: " + KahanSummation.getValue( StatisticalFunctions.getRelativeDeviationsWRTMean(currentCVAArray)) );
			for(int index = 0; index < currentCVAArray.length; index++) {
				System.out.println(  Double.toString( currentCVAArray[index] ).replace('.', ',') );
			}
			
		}
		
		System.out.println("CVA RV calculation: " + (endTimeRV - startTimeRV));
		System.out.println("Arithmetic Mean calculation: " + (endTimeArithmeticMean - startTimeArithmeticMean));
		
	}
	
	
	/**
	 * 
	 * This auxiliary test function provides the CVA for a fixed set of model parameters. 
	 * The time horizon is 1.0. Only the number of paths and the number of time steps can be chosen.
	 * 90 Time Steps and 2 000 000 paths can be simulated. Computational more demanding simulations 
	 * probably need a larger heap space.
	 * 
	 * @param numberOfPaths
	 * @param numberOfTimeSteps
	 * @return The CVA
	 * @throws CalculationException
	 */
	public static RandomVariableInterface getCVA( int numberOfPaths, int numberOfTimeSteps, int seed) throws CalculationException {
		
		System.out.println("Heap Space: " + java.lang.Runtime.getRuntime().maxMemory());
		
		double timeStepSize = 1.0 / numberOfTimeSteps;
		
		double[] meanReversion = new double[numberOfTimeSteps+1];
		double[] volatilities = new double[numberOfTimeSteps+1];
		double[] forwardRates = new double[] {0.02, 0.05, 0.01, 0.05, 0.01};
		
		for(int index = 0; index < numberOfTimeSteps + 1; index++) {
			meanReversion[index] = 0.05;
			volatilities[index] = 0.03;
		}
		
		HullWhiteModel underlyingModel = HullWhiteCreationHelper.createHullWhiteModel(0.0, numberOfTimeSteps, timeStepSize, meanReversion, volatilities, forwardRates, numberOfPaths ); 
		
		ProductConditionalFairValueProcessInterface<HullWhiteModel> productProcess = new CouponBondConditionalFairValueProcess<HullWhiteModel>(underlyingModel, new double[] {numberOfTimeSteps * timeStepSize }, new double[] {1.0}, new double[] {1.0});
		
//		ProductConditionalFairValueProcessInterface<HullWhiteModel> productProcess = new SwapConditionalFairValueProcess<HullWhiteModel>(underlyingModel, new double[] {numberOfTimeSteps/3 * timeStepSize, 2*numberOfTimeSteps/3 * timeStepSize, numberOfTimeSteps*timeStepSize });
		
		IntensityModelInterface intensityModel = new CIRModel(0.01, 0.05 , 0.01, 0.03);
		
		CorrelationInterface correlation = new Correlation(new double[][]{{0.9},{0.0}});
		
		NPVAndDefaultIntensitySimulationInterface<HullWhiteModel> npvAndDefaultIntensitySimulation = new NPVAndCorrelatedDefaultIntensitySimulation<HullWhiteModel>(underlyingModel, productProcess, intensityModel , correlation , seed);
		
		
		double lossGivenDefault = 1.0;
		IntensityBasedCVA intensityBasedCVA = new IntensityBasedCVA(lossGivenDefault);
		
		return intensityBasedCVA.getCVA( npvAndDefaultIntensitySimulation, IntegrationMethod.Trapezoidal );
	
	}
	
	
	/**
	 * This function takes an array and calculates the arithmetic means of specified partitions. 
	 * 
	 * @param realizations 
	 * @param startPath The start index of the array from which onwards the partitions formed. 
	 * @param numberOfCVAs The number of equally spaced partitions.
	 * @param numberOfPaths The number of elements in each partition.
	 * @return The arithmetic means of the specified partitions.
	 */
	public static double[] getArithmeticMeans(double[] realizations, int startPath, int numberOfCVAs, int numberOfPaths ) {
	 
		if( realizations.length < startPath + numberOfCVAs * numberOfPaths ) {throw new IllegalArgumentException("The realizations array is to short.");}
		
		double[] cvas = new double[numberOfCVAs];
		
		for(int cvaIndex = 0; cvaIndex < numberOfCVAs; cvaIndex++) {
			cvas[cvaIndex] = StatisticalFunctions.getArithmeticMean( java.util.Arrays.copyOfRange( realizations, startPath + numberOfPaths*cvaIndex, startPath + numberOfPaths*(cvaIndex+1)  ) );
		}
			
		return cvas;
	}
	
	
	/**
	 * This method gets an array a = (a<sub>1</sub>, a<sub>2</sub>, ... , a<sub>n</sub>) and 
	 * returns the array b = ( a<sub>1</sub> / 1.0, (a<sub>1</sub> + a<sub>2</sub>) / 2.0, ... , (sum_(i = 1)^(n) a<sub>i</sub>)/ ((double) n ) )
	 * 
	 * 
	 * @param values (a<sub>1</sub>, a<sub>2</sub>, ... , a<sub>n</sub>)
	 * @return ( a<sub>1</sub> / 1.0, (a<sub>1</sub> + a<sub>2</sub>) / 2.0, ... , (sum_(i = 1)^(n) a<sub>i</sub>)/ ((double) n ) )
	 */
	public static double[] getIncreasingSampleSizeMeanArray(double[] values) {
		
		if(values == null) {throw new IllegalArgumentException("Not allowed to pass an array of value null to this method.");}
		
		double[] arithmeticMeans = new double[values.length];
		
		arithmeticMeans[0] = values[0];
		
		for(int i = 1; i < values.length; i++) {
			arithmeticMeans[i] = ( arithmeticMeans[i-1]*(i) + values[i] ) / ((double) i+1) ; 
		}
		
		return arithmeticMeans;
		
	}
	
	
	
	
}