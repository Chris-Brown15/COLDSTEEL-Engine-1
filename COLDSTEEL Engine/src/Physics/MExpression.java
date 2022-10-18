package Physics;

import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Supplier;

import CSUtil.DataStructures.Tuple2;

/**
 * 
 * Represents a math expression.
 * It serves as an input/output machine that can be specified by a string. Other operations, like getting a normal or tangent
 * to this MExpression are available.
 * <br><br>
 * Start by calling this class's constructor. 
 * <br><br>
 * TODO: Parentheses insertion for operator prescedence correctness.
 * <br><br>
 * Currently, there is no operator prescedence enforcement, which means that the user must add their own parentheses to control the flow of the 
 * evaluation. This could lead to unexpected results, but if parentheses are used, this can be averted.
 * 
 * @author Chris Brown
 *
 */
public class MExpression {
		
	private static final Random rng = new Random(1083490829048109348l);//start a random number generator with a randomly typed out seed
	
	public static boolean CHECKS = true;
	public static boolean ASSERTS = false;
	
	private final LinkedList<operator> function = new LinkedList<>();
	private int numberVariables = 0;
	private Consumer<Number> finish;
	private boolean stopInfinity = false;
	
	//if a getter is given for a variable, this number will increase
	private int doubleGetters = 0;
	private int longGetters = 0;
	private int intGetters = 0;
	private int floatGetters = 0;
	
	//Algebraic fuction data	
	private double translateY = 0d;
	private double translateX = 0d;	
	private boolean reflect = false;
	private double functionCoefficient = 1d;
	private double variableCoefficient = 1d;
	private boolean possiblyOneToOne = true;
	private boolean alwaysOneToOne = true;
	
	public int intVariables() {
		
		return numberVariables - intGetters;
		
	}

	public int doubleVariables() {
		
		return numberVariables - doubleGetters;
		
	}

	public int floatVariables() {
		
		return numberVariables - floatGetters;
		
	}

	public int longVariables() {
		
		return numberVariables - longGetters;
		
	}
	
	/**
	 * 
	 * Class representation of a function variable. The primary relevence of this class is it's {@code position}, which tells
	 * what order this variable is in the list of variables, and thus which index it's value will be when {@code at} is called.
	 * 
	 * Variable can also be asssigned to a variable from the outside world using {@code set} methods.
	 *
	 */
	private class variable {
		
		final String name;
		int intPosition;
		int floatPosition;
		int longPosition;
		int doublePosition;
				
		Supplier<Integer> intGetter;
		Supplier<Double> doubleGetter;
		Supplier<Float> floatGetter;
		Supplier<Long> longGetter;
		
		variable(String name , int position){
			
			this.name = name;
			this.intPosition = position;
			this.floatPosition = position;
			this.longPosition = position;			
			this.doublePosition = position;
			
		}
			
		int intValue(int...variables) {
			
			return (int) (((intGetter != null ? intGetter.get() : variables[intPosition]) + translateX) * variableCoefficient);
			
		}
		
		double doubleValue(double...variables) {
			
			return ((doubleGetter != null ? doubleGetter.get() : variables[doublePosition]) + translateX) * variableCoefficient;
			
		}
		
		float floatValue(float...variables) {
			
			return (float) (((floatGetter != null ? floatGetter.get() : variables[floatPosition]) + (float)translateX) * variableCoefficient);
			
		}
		
		long longValue(long...variables) {
			
			return (long) (((longGetter != null ? longGetter.get() : variables[longPosition]) + (long)translateX) * variableCoefficient);
			
		}
		
		int intValue() {
			
			return intGetter.get();
			
		}
		
		double doubleValue() {
			
			return doubleGetter.get();
			
		}
		
		float floatValue() {
			
			return floatGetter.get();
			
		}
		
		long longValue() {
			
			return longGetter.get();
			
		}
		
	}
		
	/**
	 * 
	 * Operator is the generic operator class for functions. Every part of a function is an operator. 
	 *
	 */
	private class operator {
	
		final OperatorType type;
		private final variable var;
		private final double constant;
		
		operator(OperatorType t){
		
			type = t;
			assert type != OperatorType.CONST || type != OperatorType.VAR : "Invalid Constructor called for type " + type.toString();
			var = null;
			constant = -1;
						
		}
		
		operator(variable v){
			
			type = OperatorType.VAR;
			var = v;
			constant = -1;			
			
		}
		
		operator(double constt){
			
			this.constant = constt;
			type = OperatorType.CONST;
			var = null;
		}
				
		variable var() {
			
			assert type == OperatorType.VAR ;
			return var;
			
		}
		
		double constant() {
			
			assert type == OperatorType.CONST;
			return constant;
			
		}
		
		boolean isVar(String name) {
			
			return type == OperatorType.VAR && var.name.equals(name);
			
		}
		
		boolean isVar() {
			
			return type == OperatorType.VAR;
			
		}
		
	}
	
	/**
	 * 
	 * The heavy lifting component of functions. OperatorType handles the actual arithmetic of the MFunction with it's {@code handle} methods.
	 * It also contains methods for ordering operators based on their precedence.
	 *
	 */
	private enum OperatorType {
	
		ADD(true),
		SUB(true),
		MUL(true),
		DIV(true),
		OPAREN(true),
		CPAREN(true),
		POW(true),
		SQRT(true),
		NTHRT(true),
		ABS(false),
		LN(true),
		LOG(true),
		LOGBS(true),
		FLR(true),
		CEIL(true),
		RMDR(true),
		ROUND(true),
		ULP(true),
		MAX(true),
		MIN(true),
		INC(true),
		DEC(true),
		CTPT(true),
		TODEG(true),
		TORAD(true),
		RAND(true),
		SRAND(true),
		
		//trig
		SIN(false),
		COS(false),
		TAN(false),
		CSC(false),
		SEC(false),
		CTAN(false),
		
		ASIN(true),
		ACOS(true),
		ATAN(true),
		ACSC(true),
		ASEC(true),
		ACTAN(true),
		
		SINH(true),
		COSH(false),
		TANH(true),
		CSCH(true),
		SECH(true),
		CTANH(true),
		
		ASINH(true),
		ACOSH(true),
		ATANH(true),
		ACSCH(true),
		ASECH(true),
		ACOTH(true),
		
		//subs tell us to look for a variable or constant that exists at this index
		VAR(true),
		CONST(true),
				
	;
			
		final boolean oneToOne;
		OperatorType(boolean oneToOne){
			
			this.oneToOne = oneToOne;
			
		}
				
		int handle(int a , int b) {
			
//			System.out.println(this.toString() + "ING " + a + " and " + b);
			
			return switch(this) {
			
				case ADD -> a + b;
				case SUB -> a - b;
				case DIV -> a / b;
				case MUL -> a * b;
				case POW -> (int) Math.pow(a, b);
				case SQRT -> (int) Math.sqrt(b);
				case NTHRT -> (int) Math.pow(a, 1/b);
				case LOG -> (int) Math.log10(b);
				case LN -> (int) Math.log(b);
				case ABS -> Math.abs(b);
				case LOGBS -> logBS(a , b);
				case FLR -> b;
				case CEIL -> b;
				case RMDR -> (int) Math.IEEEremainder(a, b);
				case ROUND -> b;
				case ULP -> (int)Math.ulp(b);
				case MAX -> Math.max(a, b);
				case MIN -> Math.min(a, b);
				case INC -> ++b;
				case DEC -> --b;	
				case CTPT -> (int) Math.atan2(a, b);
				case TODEG -> (int) Math.toDegrees(b);
				case TORAD -> (int) Math.toRadians(b);
				case RAND -> rng.nextInt(b);
				case SRAND -> randomlySignedInt(b);
				
				case SIN -> (int) Math.sin(b);
				case COS -> (int) Math.cos(b);
				case TAN -> (int) Math.tan(b);
				case CSC -> (int) (1 / Math.sin(b));
				case SEC -> (int) (1 / Math.cos(b));
				case CTAN -> (int) (1 / Math.tan(b));
				
				case ASIN -> (int) Math.asin(b);
				case ACOS -> (int) Math.acos(b);
				case ATAN -> (int) Math.atan(b);
				case ACSC -> (int) (1 / Math.asin(b));
				case ASEC -> (int) (1 / Math.cos(b));
				case ACTAN -> (int) (1 / Math.atan(b));
				
				case SINH -> (int) Math.sinh(b);					
				case COSH -> (int) Math.cosh(b);
				case TANH -> (int) Math.tanh(b);
				case CSCH -> (int) (1 / Math.sinh(b));
				case SECH -> (int) (1 / Math.cosh(b));
				case CTANH -> (int) (1 / Math.tanh(b));
				
				case ASINH -> arcSinh(b);
				case ACOSH -> arcCosh(b);				
				case ATANH -> arcTanh(b);
				case ACSCH -> arcCsch(b);
				case ACOTH -> arcCotanh(b);
				case ASECH -> arcSech(b);
				
				case CONST -> throw new UnsupportedOperationException("Unimplemented case: " + this);
				case CPAREN -> throw new UnsupportedOperationException("Unimplemented case: " + this);
				case OPAREN -> throw new UnsupportedOperationException("Unimplemented case: " + this);
				case VAR -> throw new UnsupportedOperationException("Unimplemented case: " + this);
								
			};
			
		}
		
		long handle(long a , long b) {
			
//			System.out.println(this.toString() + "ING " + a + " and " + b);
			
			return switch(this) {
			
				case ADD -> a + b;
				case SUB -> a - b;
				case DIV -> a / b;
				case MUL -> a * b;
				case POW ->(long) Math.pow(a, b);
				case SQRT -> (long) Math.sqrt(b);
				case NTHRT -> (long) Math.pow(a, 1/b);
				case LOG -> (long) Math.log10(b);
				case LN -> (long) Math.log(b);
				case ABS -> Math.abs(b);
				case LOGBS -> logBS(a , b);
				case FLR -> b;
				case CEIL -> b;
				case RMDR -> (long) Math.IEEEremainder(a, b);
				case ROUND -> b;
				case ULP -> (long)Math.ulp(b);
				case MAX -> Math.max(a, b);
				case MIN -> Math.min(a, b);
				case INC -> ++b;
				case DEC -> --b;
				case CTPT -> (long) Math.atan2(a, b);
				case TODEG -> (long) Math.toDegrees(b);
				case TORAD -> (long) Math.toRadians(b);
				case RAND -> rng.nextLong(b);
				case SRAND -> randomlySignedLong(b);
				
				case SIN -> (long) Math.sin(b);
				case COS -> (long) Math.cos(b);
				case TAN -> (long) Math.tan(b);
				case CSC -> (long) (1 / Math.sin(b));
				case SEC -> (long) (1 / Math.cos(b));
				case CTAN -> (long) (1 / Math.tan(b));
				
				case ASIN -> (long) Math.asin(b);
				case ACOS -> (long) Math.acos(b);
				case ATAN -> (long) Math.atan(b);
				case ACSC -> (long) (1 / Math.asin(b));
				case ASEC -> (long) (1 / Math.cos(b));
				case ACTAN -> (long) (1 / Math.atan(b));
				
				case SINH -> (long) Math.sinh(b);					
				case COSH -> (long) Math.cosh(b);
				case TANH -> (long) Math.tanh(b);
				case CSCH -> (long) (1 / Math.sinh(b));
				case SECH -> (long) (1 / Math.cosh(b));
				case CTANH -> (long) (1 / Math.tanh(b));
				
				case ASINH -> arcSinh(b);
				case ACOSH -> arcCosh(b);
				case ATANH -> arcTanh(b);
				case ACSCH -> arcCsch(b);
				case ACOTH -> arcCotanh(b);
				case ASECH -> arcSech(b);
				
				case CONST -> throw new UnsupportedOperationException("Unimplemented case: " + this);
				case CPAREN -> throw new UnsupportedOperationException("Unimplemented case: " + this);
				case OPAREN -> throw new UnsupportedOperationException("Unimplemented case: " + this);
				case VAR -> throw new UnsupportedOperationException("Unimplemented case: " + this);
								
			};
			
		}
		
		double handle(double a , double b) {
			
//			System.out.println(this.toString() + "ING " + a + " and " + b);
			
			return switch(this) {
			
				case ADD -> a + b;
				case SUB -> a - b;
				case DIV -> a / b;
				case MUL -> a * b;
				case POW -> Math.pow(a, b);
				case SQRT -> Math.sqrt(b);		
				case NTHRT -> Math.pow(b , 1/a);
				case LOG -> Math.log10(b);
				case LN -> Math.log(b);
				case ABS -> Math.abs(b);
				case LOGBS -> logBS(a , b);
				case FLR -> Math.floor(b);
				case CEIL -> Math.ceil(b);
				case RMDR -> Math.IEEEremainder(a, b);
				case ROUND -> Math.rint(b);
				case ULP -> Math.ulp(b);
				case MAX -> Math.max(a, b);
				case MIN -> Math.min(a, b);
				case INC -> ++b;
				case DEC -> --b;
				case CTPT -> Math.atan2(a, b);
				case TODEG -> Math.toDegrees(b);
				case TORAD -> Math.toRadians(b);
				case RAND -> rng.nextDouble(b);
				case SRAND -> randomlySignedDouble(b);
				
				case SIN -> Math.sin(b);
				case COS -> Math.cos(b);
				case TAN -> Math.tan(b);
				case CSC -> 1 / Math.sin(b);
				case SEC -> 1 / Math.cos(b);
				case CTAN -> 1 / Math.tan(b);
				
				case ASIN -> Math.asin(b);
				case ACOS -> Math.acos(b);
				case ATAN -> Math.atan(b);
				case ACSC -> 1 / Math.asin(b);
				case ASEC -> 1 / Math.acos(b);
				case ACTAN -> 1 / Math.atan(b);
				
				case SINH -> Math.sinh(b);					
				case COSH -> Math.cosh(b);
				case TANH -> Math.tanh(b);
				case CSCH -> 1 / Math.sinh(b);					
				case SECH -> 1 / Math.cosh(b);
				case CTANH -> 1 / Math.tanh(b);
				
				case ASINH -> arcSinh(b);
				case ACOSH -> arcCosh(b);
				case ATANH -> arcTanh(b);
				case ACSCH -> arcCsch(b);
				case ACOTH -> arcCotanh(b);
				case ASECH -> arcSech(b);
				
				case CONST -> throw new UnsupportedOperationException("Unimplemented case: " + this);
				case CPAREN -> throw new UnsupportedOperationException("Unimplemented case: " + this);
				case OPAREN -> throw new UnsupportedOperationException("Unimplemented case: " + this);
				case VAR -> throw new UnsupportedOperationException("Unimplemented case: " + this);
				
			};
			
			
		}
		
		float handle(float a , float b) {
			
//			System.out.println(toString() + "ING " + a + " and " + b);
			
			return switch(this) {
			
				case ADD -> a + b;
				case SUB -> a - b;
				case DIV -> a / b;
				case MUL -> a * b;
				case POW -> (float) Math.pow(a, b);
				case SQRT -> (float) Math.sqrt(b);
				case NTHRT -> (float) Math.pow(a, 1/b);
				case LOG -> (float) Math.log10(b);
				case LN -> (float) Math.log(b);
				case ABS -> Math.abs(b);
				case LOGBS -> logBS(a , b);
				case FLR -> (float) Math.floor(b);
				case CEIL -> (float) Math.ceil(b);
				case RMDR -> (float) Math.IEEEremainder(a, b);
				case ROUND -> (float) Math.rint(b);
				case ULP -> (float)Math.ulp(b);
				case MAX -> Math.max(a, b);
				case MIN -> Math.min(a, b);
				case INC -> ++b;
				case DEC -> --b;
				case CTPT -> (float) Math.atan2(a, b);
				case TODEG -> (float) Math.toDegrees(b);
				case TORAD -> (float) Math.toRadians(b);
				case RAND -> rng.nextFloat(b);
				case SRAND -> randomlySignedFloat(b);
				
				case SIN -> (float) Math.sin(b);
				case COS -> (float) Math.cos(b);
				case TAN -> (float) Math.tan(b);
				case CSC -> (float) (1 / Math.sin(b));
				case SEC -> (float) (1 / Math.cos(b));
				case CTAN -> (float) (1 / Math.tan(b));
				
				case ASIN -> (float) Math.asin(b);
				case ACOS -> (float) Math.acos(b);
				case ATAN -> (float) Math.atan(b);
				case ACSC -> (float) (1 / Math.asin(b));
				case ASEC -> (float) (1 / Math.acos(b));
				case ACTAN -> (float) (1 / Math.atan(b));
				
				case SINH -> (float) Math.sinh(b);					
				case COSH -> (float) Math.cosh(b);
				case TANH -> (float) Math.tanh(b);
				case CSCH -> (float) (1 / Math.sinh(b));					
				case SECH -> (float) (1 / Math.cosh(b));
				case CTANH -> (float) (1 / Math.tanh(b));
				
				case ASINH -> arcSinh(b);
				case ACOSH -> arcCosh(b);
				case ATANH -> arcTanh(b);
				case ACSCH -> arcCsch(b);
				case ASECH -> arcSech(b);
				case ACOTH -> arcCotanh(b);
				
				case CONST -> throw new UnsupportedOperationException("Unimplemented case: " + this);
				case CPAREN -> throw new UnsupportedOperationException("Unimplemented case: " + this);
				case OPAREN -> throw new UnsupportedOperationException("Unimplemented case: " + this);
				case VAR -> throw new UnsupportedOperationException("Unimplemented case: " + this);
			
			};
						
		}
		
	}

	/**
	 * If true, this MExpression will not permit the output of negative or positive infinity from its {@code at} methods.
	 * 
	 * @param should — true if this MExpression should not be allowed to output negative or positive infinity from its {@code at} methods
	 */
	public void shouldUseSafeOutputs(boolean should) {
		
		stopInfinity = should;
		
	}
		
	private void addOp(OperatorType opType) {
		
		if(!opType.oneToOne) this.possiblyOneToOne = false;		
		function.add(new operator(opType));
		
	}
	
	private void parseToken(int iteration , String token) {
				
		switch(token) {
		
			case "+" -> addOp(OperatorType.ADD);									
			case "-" -> addOp(OperatorType.SUB);	
			case "*" -> addOp(OperatorType.MUL);					
			case "/" -> addOp(OperatorType.DIV);	
			case "^" -> addOp(OperatorType.POW);
			case "(" -> addOp(OperatorType.OPAREN);
			case ")" -> addOp(OperatorType.CPAREN);
			case "log" -> addOp(OperatorType.LOG);
			case "sqrt" -> addOp(OperatorType.SQRT);
			case "nthrt" -> addOp(OperatorType.NTHRT);
			case "ln" -> addOp(OperatorType.LN);
			case "abs" -> addOp(OperatorType.ABS);
			case "logbs" -> addOp(OperatorType.LOGBS);
			case "PI" -> function.add(new operator(Math.PI));
			case "E" -> function.add(new operator(Math.E));
			case "floor" -> addOp(OperatorType.FLR);
			case "flr" -> addOp(OperatorType.FLR);
			case "ceil" -> addOp(OperatorType.CEIL);
			case "rmdr" -> addOp(OperatorType.RMDR);
			case "mod" -> addOp(OperatorType.RMDR);
			case "round" -> addOp(OperatorType.ROUND);
			case "ulp" -> addOp(OperatorType.ULP);
			case "max" -> addOp(OperatorType.MAX);
			case "min" -> addOp(OperatorType.MIN);
			case "inc" -> addOp(OperatorType.INC);
			case "++" -> addOp(OperatorType.INC);
			case "--" -> addOp(OperatorType.DEC);
			case "dec" -> addOp(OperatorType.DEC);
			case "torad" -> addOp(OperatorType.TORAD);
			case "todeg" -> addOp(OperatorType.TODEG);
			case "ctpt" -> addOp(OperatorType.CTPT);
			case "topolar" -> addOp(OperatorType.CTPT);
			case "rand" -> addOp(OperatorType.RAND);
			case "random" -> addOp(OperatorType.RAND);
			case "srand" -> addOp(OperatorType.SRAND);
			case "signedrandom" -> addOp(OperatorType.SRAND);
			
			case "sin" -> addOp(OperatorType.SIN);
			case "cos" -> addOp(OperatorType.COS);
			case "tan" -> addOp(OperatorType.TAN);
			case "csc" -> addOp(OperatorType.CSC);
			case "sec" -> addOp(OperatorType.SEC);
			case "ctan" -> addOp(OperatorType.CTAN);
			case "cot" -> addOp(OperatorType.CTAN);
			
			case "asin" -> addOp(OperatorType.ASIN);
			case "arcsin" -> addOp(OperatorType.ASIN);
			case "acos" -> addOp(OperatorType.ACOS);
			case "arccos" -> addOp(OperatorType.ACOS);
			case "atan" -> addOp(OperatorType.ATAN);
			case "arctan" -> addOp(OperatorType.ATAN);
			case "acsc" -> addOp(OperatorType.ACSC);
			case "arccsc" -> addOp(OperatorType.ACSC);
			case "asec" -> addOp(OperatorType.ASEC);
			case "arcsec" -> addOp(OperatorType.ASEC);
			case "acot" -> addOp(OperatorType.ACTAN);
			case "arccot" -> addOp(OperatorType.ACTAN);
			case "arcctan" -> addOp(OperatorType.ACTAN);
			
			case "sinh" -> addOp(OperatorType.SINH);
			case "hypsin" -> addOp(OperatorType.SINH);
			case "cosh" -> addOp(OperatorType.COSH);
			case "hypcos" -> addOp(OperatorType.COSH);
			case "tanh" -> addOp(OperatorType.TANH);
			case "hyptan" -> addOp(OperatorType.TANH);
			case "csch" -> addOp(OperatorType.CSCH);
			case "hypcsc" -> addOp(OperatorType.CSCH);
			case "sech" -> addOp(OperatorType.SECH);
			case "hypsec" -> addOp(OperatorType.SECH);
			case "coth" -> addOp(OperatorType.CTANH);
			case "hypcot" -> addOp(OperatorType.CTANH);
			case "ctanh" -> addOp(OperatorType.CTANH);
			
			case "arcsinh" -> addOp(OperatorType.ASINH);
			case "asinh" -> addOp(OperatorType.ASINH);
			case "arccosh" -> addOp(OperatorType.ACOSH);
			case "acosh" -> addOp(OperatorType.ACOSH);
			case "arctanh" -> addOp(OperatorType.ATANH);
			case "atanh" -> addOp(OperatorType.ATANH);
			case "arccsch" -> addOp(OperatorType.ACSCH);
			case "acsch" -> addOp(OperatorType.ACSCH);
			case "arcsech" -> addOp(OperatorType.ASECH);
			case "asech" -> addOp(OperatorType.ASECH);
			case "arccoth" -> addOp(OperatorType.ACOTH);
			case "acoth" -> addOp(OperatorType.ACOTH);
			case "actanh" -> addOp(OperatorType.ACOTH);
			
			default -> {
								
				try {
					
					/*
					 
					 if token can be parsed as a number, we stay in this branch, else we go to the catch clause.
					 in a for loop, we see if we have previously added the parsed number into the list of constants.
					 If we have not, we add a new constant representing it, and if we have, we add a new index onto it's array of indices. 
					 
					 */
					double inp;
					inp = toNumber(token);
					this.function.add(new operator(inp));
					
				} catch(NumberFormatException e) {
																
					/*
					 
					 If the token was not parseable as a number, it is a variable.
					 The for loop tries to find this variable in the function and if it can,
					 it places a reference to that variable in this position in the function
					 If it cannot, it adds a new variable to the function.
					 The position field of the variable class represents its order with respect to the other variables in the function
					 For example a position of 0 tells this is the first variable,a value of 1 represents the second variable. 
					 It is expected variables will be provided values which should 
					 corespond to the variable's position in the function. if x is a variable that appears first,
					 then the first value passed in will be substituted for x in all it's positions.
										 
					 */
					
					operator iter = null;
					boolean foundVar = false;
					
					for(operator x : function) if((iter = x).isVar(token)){
						
						foundVar = true;
						break;
						
					}
					
					if(foundVar) this.function.add(iter);
					if(!foundVar)this.function.add(new operator(new variable(token, numberVariables++)));
					
				}
				
			}				
				
		}
				
	}
	
	/**
	 * Pass in a string representation of a math function which this class will parse and, subsequent to this call, represent. 
	 * 
	 * The correct usage is, for operations which involve two operands, operands are on the left and right of the operator,
	 * and for operations on one operand, the operand is to the right of the operator.
	 * 
	 * <br><br>
	 * Valid operators will be:
	 * <ul>
	 * <li><b> + </b></li>
	 * <li><b> - </b></li>
	 * <li><b> * </b></li>
	 * <li><b> / </b></li>
	 * <li><b> ( </b></li>
	 * <li><b> ) </b></li>
	 * <li><b> ^ </b></li>
	 * <li><b> sqrt </b></li>
	 * <li><b> nthrt </b></li>
	 * <li><b> abs </b></li>
	 * <li><b> ln </b></li>
	 * <li><b> log </b></li>
	 * <li><b> logbs </b></li>
	 * <li><b> sin </b></li>
	 * <li><b> cos </b></li>
	 * <li><b> tan </b></li>
	 * <li><b> csc</b></li>
	 * <li><b> sec</b></li> 
	 * <li><b> ctan or cot</b></li>
	 * <li><b> asin or arcsin </b></li>
	 * <li><b> acos or arcos  </b></li>
	 * <li><b> atan or arctan </b></li>
	 * <li><b> acsc or arccsc </b></li>
	 * <li><b> asec or arcsec </b></li>
	 * <li><b> actan or arccot </b></li>
	 * <li><b> sinh or hypsin </b></li>
	 * <li><b> cosh or hypcos </b></li>
	 * <li><b> tanh or hyptan </b></li>
	 * <li><b> csch or hypcsc </b></li>
	 * <li><b> sech or hypsec </b></li>
	 * <li><b> ctanh or hypctan or hypcot </b></li>
	 * <li><b> floor or flr </b></li>
	 * <li><b> ceil </b></li>
	 * <li><b> rmdr </b></li>
	 * <li><b> ceil or mod </b></li>
	 * <li><b> round </b></li>
	 * <li><b> ulp </b></li>
	 * <li><b> max </b></li>
	 * <li><b> min </b></li>
	 * <li><b> inc or ++ </b></li>
	 * <li><b> dec or --</b></li>
	 * <li><b> todeg </b></li>
	 * <li><b> torad </b></li>
	 * <li><b> ctpt or topolar </b></li>
	 * <li><b> PI </b></li>
	 * <li><b> E </b></li>
	 * <li><b> arcsinh or asinh </b></li>
	 * <li><b> arccosh or acosh </b></li>
	 * <li><b> arctanh or atanh </b></li>
	 * <li><b> arccsch or acsch </b></li>
	 * <li><b> arccoth or acoth or actanh </b></li>
	 * <li><b> rand or random </b></li>
	 * <li><b> srand or signedrandom </b></li>
	 * </ul> 
	 * <br><br>
	 * Examples of MFunction literals:
	 * <br><br>
	 * new MFunction("x ^ 2 + 2"); <br>
	 * new MFunction("( x ^ 2 + 2 ) * 2"); <br>
	 * new MFunction("abs x ^ 3 + 2"); <br>
	 * new MFunction("cot a"); <br>
	 * new MFunction("dec 11"); <br>
	 * new MFunction("( a ctpt b ) * 123"); <br>
	 * new MFunction("ceil a"); <br>
	 * new MFunction("( ceil a ) max ( ceil b )"); <br>
	 * new MFunction("ln E"); <br>
	 * new MFunction("3++ logbs 8"); <br>
	 * new MFunction("log 2"); <br><br>
	 * 
	 * 
	 * With respect to the type of the input and output, anything that extends a number can be passed and the same type will be output.
	 * <br><br>
	 * <b> IMPORTANT:</b> spaces are used as the delimiter for operators so there should always be spaces between operators.  
	 * 
	 * @param function — a string representation of the function
	 */
	public MExpression(String function) {
	
		String[] tokens = function.split(" ");
		for(int i = 0; i < tokens.length ; i ++) parseToken(i , tokens[i]);
		
		//check for special case of non one to oneness, namely, if something is raied to a power that is even
		int i = 0;
		for(operator x : this.function) {
			
			if(x.type == OperatorType.POW) {
				
				operator next = this.function.get(i + 1);
				if(next.type == OperatorType.CONST && next.constant % 2 == 0 || next.isVar()) {
					
					this.alwaysOneToOne = false;
					return;
				}
				
				else if (next.type == OperatorType.OPAREN) {
					
					
					/*
					 * If the value something is being raised to is inside a set of parentheses, we will try to 
					 * find if the result of that set of parentheses is odd or even. We pass in no arguments, and if arguments are needed,
					 * we cannot determine whether the function is one to one, so we default to not being one to one.
					 */
					
					try {
						
						double res = at(i + 2 , new double[] {}).getSecond();
						if(res % 2 == 0) this.alwaysOneToOne = false;
						return;
						
					} catch (IndexOutOfBoundsException e) {
						
						this.alwaysOneToOne = false;
						return;
						
					}
					
				}
				
			}
			
			i++;
			
		}
				
	}	
	
	/**
	 * Pass in a string representation of a math function which this class will parse and, subsequent to this call, represent. 
	 * 
	 * The correct usage is, for operations which involve two operands, operands are on the left and right of the operator,
	 * and for operations on one operand, the operand is to the right of the operator.
	 * 
	 * <br><br>
	 * Valid operators will be:
	 * <ul>
	 * <li><b> + </b></li>
	 * <li><b> - </b></li>
	 * <li><b> * </b></li>
	 * <li><b> / </b></li>
	 * <li><b> ( </b></li>
	 * <li><b> ) </b></li>
	 * <li><b> ^ </b></li>
	 * <li><b> sqrt </b></li>
	 * <li><b> nthrt </b></li>
	 * <li><b> abs </b></li>
	 * <li><b> ln </b></li>
	 * <li><b> log </b></li>
	 * <li><b> logbs </b></li>
	 * <li><b> sin </b></li>
	 * <li><b> cos </b></li>
	 * <li><b> tan </b></li>
	 * <li><b> csc</b></li>
	 * <li><b> sec</b></li> 
	 * <li><b> ctan or cot</b></li>
	 * <li><b> asin or arcsin </b></li>
	 * <li><b> acos or arcos  </b></li>
	 * <li><b> atan or arctan </b></li>
	 * <li><b> acsc or arccsc </b></li>
	 * <li><b> asec or arcsec </b></li>
	 * <li><b> actan or arccot </b></li>
	 * <li><b> sinh or hypsin </b></li>
	 * <li><b> cosh or hypcos </b></li>
	 * <li><b> tanh or hyptan </b></li>
	 * <li><b> csch or hypcsc </b></li>
	 * <li><b> sech or hypsec </b></li>
	 * <li><b> ctanh or hypctan or hypcot </b></li>
	 * <li><b> floor or flr </b></li>
	 * <li><b> ceil </b></li>
	 * <li><b> rmdr </b></li>
	 * <li><b> ceil or mod </b></li>
	 * <li><b> round </b></li>
	 * <li><b> ulp </b></li>
	 * <li><b> max </b></li>
	 * <li><b> min </b></li>
	 * <li><b> inc or ++ </b></li>
	 * <li><b> dec or --</b></li>
	 * <li><b> todeg </b></li>
	 * <li><b> torad </b></li>
	 * <li><b> ctpt or topolar </b></li>
	 * <li><b> PI </b></li>
	 * <li><b> E </b></li>
	 * <li><b> arcsinh or asinh </b></li>
	 * <li><b> arccosh or acosh </b></li>
	 * <li><b> arctanh or atanh </b></li>
	 * <li><b> arccsch or acsch </b></li>
	 * <li><b> arccoth or acoth or actanh </b></li>
	 * <li><b> rand or random </b></li>
	 * <li><b> srand or signedrandom </b></li>
	 * </ul> 
	 * <br><br>
	 * Examples of MFunction literals:
	 * <br><br>
	 * new MFunction("x ^ 2 + 2"); <br>
	 * new MFunction("( x ^ 2 + 2 ) * 2"); <br>
	 * new MFunction("abs x ^ 3 + 2"); <br>
	 * new MFunction("cot a"); <br>
	 * new MFunction("dec 11"); <br>
	 * new MFunction("( a ctpt b ) * 123"); <br>
	 * new MFunction("ceil a"); <br>
	 * new MFunction("( ceil a ) max ( ceil b )"); <br>
	 * new MFunction("ln E"); <br>
	 * new MFunction("3++ logbs 8"); <br>
	 * new MFunction("log 2"); <br><br>
	 * 
	 * 
	 * With respect to the type of the input and output, anything that extends a number can be passed and the same type will be output.
	 * <br><br>
	 * <b> IMPORTANT:</b> spaces are used as the delimiter for operators so there should always be spaces between operators.  
	 * 
	 * @param function — a string representation of the function
	 * @param safeValues — if true, negative infinity and/or positive infinity cannot be results, 0 is returned instead
	 */
	public MExpression(String function , boolean safeValues) {
	
		this.stopInfinity = safeValues;
		String[] tokens = function.split(" ");				
		for(int i = 0; i < tokens.length ; i ++) parseToken(i , tokens[i]);
		
		//check for special case of non one to oneness, namely, if something is raied to a power that is even
		int i = 0;
		for(operator x : this.function) {
			
			if(x.type == OperatorType.POW) {
				
				operator next = this.function.get(i + 1);
				if(next.type == OperatorType.CONST && next.constant % 2 == 0 || next.isVar()) {
					
					this.alwaysOneToOne = false;
					return;
				}
				
				else if (next.type == OperatorType.OPAREN) {
					
					
					/*
					 * If the value something is being raised to is inside a set of parentheses, we will try to 
					 * find if the result of that set of parentheses is odd or even. We pass in no arguments, and if arguments are needed,
					 * we cannot determine whether the function is one to one, so we default to not being one to one.
					 */
					
					try {
						
						double res = at(i + 2 , new double[] {}).getSecond();
						if(res % 2 == 0) this.alwaysOneToOne = false;
						return;
						
					} catch (IndexOutOfBoundsException e) {
						
						this.alwaysOneToOne = false;
						return;
						
					}
					
				}
				
			}
			
			i++;
			
		}
				
	}	
		
	/**
	 * finish will be invoked after a completion of the {@code at} method if a finish Consumer is provided. This is intended to be a setter for an
	 * outside variable, but any kind of Consumer of Number can be passed in, and will be executed just prior to {@code at}'s return.
	 * 
	 * @param finish — a Consumer of Number to be invoked at the end of {@code at}, the input will be the result of {@code at}'s evaluation
	 */
	public void onFinish(Consumer<Number> finish) {
		
		this.finish = finish;
		
	}
	
	/**
	 * Sets the variable with {@code name} to this getter function. From this call onward, this variable will use its getter for its
	 * value, and anything passed in for it on a call to {@code at} will be ignored.
	 * 
	 * @param name — a variable name
	 * @param getter — a Supplier returning Integer who will be invoked to get the value of {@code name}
	 */
	public void setInt(String name , Supplier<Integer> getter) {
		
		int i = 0;
		for(operator x : function) {
			
			if(x.isVar(name)) {
				
				if(x.var().intGetter == null && getter != null) {
					
					intGetters++;
					function.stream().skip(i).filter((op) -> op.isVar()).forEach((op) -> op.var().intPosition--); 
					
				}
				
				else if(x.var().intGetter != null && getter == null) {
					
					intGetters--;
					function.stream().limit(i).filter((op) -> op.isVar()).forEach((op) -> op.var().intPosition++);
					
				}
				
				x.var().intGetter = getter;
				
				return;
				
			}
			
			i++;
			
		}
		
		if(CHECKS) errorCheck("Invalid variable name given (" + name + " given) for getter." , true);
		
	}
		
	/**
	 * Sets the variable with {@code name} to this getter function. From this call onward, this variable will use its getter for its
	 * value, and anything passed in for it on a call to {@code at} will be ignored.
	 * 
	 * @param name — a variable name
	 * @param getter — a Supplier returning Double who will be invoked to get the value of {@code name}
	 */
	public void setDouble(String name , Supplier<Double> getter) {
		
		int i = 0 ;
		for(operator x : function) {
			
			if(x.isVar(name)) {

				if(x.var().doubleGetter == null && getter != null) {
					
					doubleGetters++;
					function.stream().skip(i).filter((op) -> op.isVar()).forEach((op) -> op.var().doublePosition--);
					
					
				}
				
				else if(x.var().doubleGetter != null && getter == null) {
					
					doubleGetters--;
					function.stream().limit(i).filter((op) -> op.isVar()).forEach((op) -> op.var().doublePosition++);
					
				}
				
				x.var().doubleGetter = getter;			
				return;
			
			}
			
			i++;
			
		}
		
		if(CHECKS) errorCheck("Invalid variable name given (" + name + " given) for getter." , true);
		
	}
	
	/**
	 * Sets the variable with {@code name} to this getter function. From this call onward, this variable will use its getter for its
	 * value, and anything passed in for it on a call to {@code at} will be ignored.
	 * 
	 * @param name — a variable name
	 * @param getter — a Supplier returning Float who will be invoked to get the value of {@code name}
	 */
	public void setFloat(String name , Supplier<Float> getter) {
		
		int i = 0;
		for(operator x : function) {
			
			if(x.isVar(name)) {

				if(x.var().floatGetter == null && getter != null) {
					
					floatGetters++;
					function.stream().skip(i).filter((op) -> op.isVar()).forEach((op) -> op.var().floatPosition--);
					
				}
				
				else if(x.var().floatGetter != null && getter == null) {
					
					floatGetters--;
					function.stream().limit(i).filter((op) -> op.isVar()).forEach((op) -> op.var().floatPosition++);
					
				}
				
				x.var().floatGetter = getter;
				return;
				
			}

			i ++;			
			
		}
		
		if(CHECKS) errorCheck("Invalid variable name given (" + name + " given) for getter." , true);
		
	}
	
	/**
	 * Sets the variable with {@code name} to this getter function. From this call onward, this variable will use its getter for its
	 * value, and anything passed in for it on a call to {@code at} will be ignored.
	 * 
	 * @param name — a variable name
	 * @param getter — a Supplier returning Long who will be invoked to get the value of {@code name}
	 */
	public void setLong(String name , Supplier<Long> getter) {
		
		int i = 0 ;
		for(operator x : function) {

			if(x.isVar(name)) {
			
				if(x.var().longGetter == null && getter != null) {
					
					longGetters++;
					function.stream().skip(i).filter((op) -> op.isVar()).forEach((op) -> op.var().longPosition--);
					
				}
				
				else if(x.var().longGetter != null && getter == null) {
					
					longGetters--;
					function.stream().skip(i).filter((op) -> op.isVar()).forEach((op) -> op.var().longPosition++);
					
				}
				
				x.var().longGetter = getter;
				return;
				
			}
		
			i ++;
			
		}			

		if(CHECKS) errorCheck("Invalid variable name given (" + name + " given) for getter." , true);
				
	}
	
	public void translateY(double translation) {
		
		translateY = translation;
		
	}
	
	public void bumpYTranslation(double translation) {
		
		translateY += translation;
		
	}
	
	public void translateX(double translation) {
		
		translateX = translation;
		
	}
	
	public void bumpXTranslation(double translation) {
		
		translateX += translation;
		
	}
	
	public void reflect(boolean reflect) {
		
		this.reflect = reflect;
		
	}
		
	public void functionCoefficient(double coefficient) {
		
		this.functionCoefficient = coefficient;
		
	}
	
	public void variableCoefficient(double coefficient) {
		
		this.variableCoefficient = coefficient;
		
	}
	
	/**
	 * Given a list of doubles as variable values, get a double representation of evaluating this MFunction at those inputs.
	 * @param variables — list of values for the variables given at the initialization of this MFunction
	 * @return a double representation of this MFunction's output at the given inputs.
	 */
	public double at(double...variables) {

		if(CHECKS) 
			errorCheck("Invalid number of inputs given, " + numberVariables + " expected, " + variables.length + " given" , 
					variables.length != (numberVariables - doubleGetters));
		
		double result = at(0 , variables).getSecond();
		if(finish != null) finish.accept(result);
		result = functionCoefficient * (reflect ? -(result + translateY) : result + translateY);
		if(stopInfinity && outOfBounds(result)) return 0d;
		else return result;
		
	}
		
	/**
	 * Use this method when there are no inputs needed for a certain type and it is desired to get a certain type as output.
	 * In this case, each variable must have a double getter.
	 * 
	 * @return a float representation of this MExpression at the given previously-set double variables.
	 */
	public double atDoubles() {
		
		if(CHECKS) 
			errorCheck("No inputs given but " + (numberVariables - doubleGetters) + " expected." , numberVariables - doubleGetters > 0);
		
		double result = atDoubles(0).getSecond();
		if(finish != null) finish.accept(result);
		result = functionCoefficient * (reflect ? -(result + translateY) : result + translateY);
		if(stopInfinity && outOfBounds(result)) return 0d;
		else return result;
	}
	
	private Tuple2<Integer , Double> at(int startAt , double... variables) {
			
		double res = 0;
		boolean started = false;
		final Stack<OperatorType> opStack = new Stack<OperatorType>();
		operator currentOp;	
		for(int i = startAt ; i < function.size() ; i ++) {
			
			currentOp = function.get(i);
			
			switch(currentOp.type) {
				
				case VAR ->{
					
					if(!started) res = currentOp.var().doubleValue(variables);
					else res = opStack.pop().handle(res, currentOp.var().doubleValue(variables));
										
				}
				
				case CONST ->{
					
					if (!started) res = currentOp.constant();
					else res = opStack.pop().handle(res, currentOp.constant());
					
				}			
					
				case OPAREN -> {
					
					Tuple2<Integer , Double> parenRes = at(i + 1 , variables);
					if(!started) res = parenRes.getSecond(); 
					else res = opStack.pop().handle(res , parenRes.getSecond());					
					i = parenRes.getFirst();
					
				}
				
				case CPAREN -> {
					
					return new Tuple2<>(i , res);				
				
				}
				
				default -> opStack.push(currentOp.type); 
			
			}

			started = true;
			
		}
		
		return new Tuple2<>(-1 , res);
		
	}
	
	private Tuple2<Integer , Double> atDoubles(int startAt) {
		
		double res = 0;
		boolean started = false;
		final Stack<OperatorType> opStack = new Stack<OperatorType>();
		operator currentOp;	
		for(int i = startAt ; i < function.size() ; i ++) {
			
			currentOp = function.get(i);
			
			switch(currentOp.type) {
				
				case VAR ->{
					
					if(!started) res = currentOp.var().doubleValue();
					else res = opStack.pop().handle(res, currentOp.var().doubleValue());
										
				}
				
				case CONST ->{
					
					if (!started) res = currentOp.constant();
					else res = opStack.pop().handle(res, currentOp.constant());
					
				}			
					
				case OPAREN -> {
					
					Tuple2<Integer , Double> parenRes = atDoubles(i + 1);
					if(!started) res = parenRes.getSecond(); 
					else res = opStack.pop().handle(res , parenRes.getSecond());					
					i = parenRes.getFirst();
					
				}
				
				case CPAREN ->{
					
					return new Tuple2<>(i , res);				
				
				}
				default -> opStack.push(currentOp.type); 
			
			}

			started = true;
			
		}
		
		return new Tuple2<>(-1 , res);
		
	}
	
	/**
	 * Given a list of ints as variable values, get an int representation of evaluating this MFunction at those inputs.
	 * @param variables — list of values for the variables given at the initialization of this MFunction
	 * @return an integer representation of this MFunction's output at the given inputs.
	 */
	public int at(int...variables) {

		if(CHECKS) 
			errorCheck("Invalid number of inputs given, " + numberVariables + " expected, " + variables.length + " given" , 
					variables.length != (numberVariables - intGetters));
		
		int result = at(0 , variables).getSecond();
		if(finish != null) finish.accept(result);
		result = (int) (functionCoefficient * (reflect ? -(result + translateY) : result + translateY));
		if(stopInfinity && outOfBounds(result)) return 0;
		else return result;
		
	}
		
	/**
	 * Use this method when there are no inputs needed for a certain type and it is desired to get a certain type as output.
	 * In this case, each variable must have an int getter.
	 * 
	 * @return a float representation of this MExpression at the given previously-set int variables.
	 */
	public int atInts() {
		
		if(CHECKS) errorCheck("No inputs given but " + (numberVariables - intGetters) + " expected." , numberVariables - intGetters > 0);
		
		int result = atInts(0).getSecond();
		if(finish != null) finish.accept(result);
		result = (int) (functionCoefficient * (reflect ? -(result + translateY) : result + translateY));
		if(stopInfinity && outOfBounds(result)) return 0;
		else return result;
		
	}
	
	private Tuple2<Integer , Integer> at(int startAt , int... variables) {
			
		int res = 0;
		boolean started = false;
		final Stack<OperatorType> opStack = new Stack<OperatorType>();
		operator currentOp;	
		for(int i = startAt ; i < function.size() ; i ++) {
			
			currentOp = function.get(i);
										
			switch(currentOp.type) {
				
				case VAR ->{
					
					if(!started) res = (currentOp.var().intValue(variables));
					else res = opStack.pop().handle(res, currentOp.var().intValue(variables));			
										
				}
				
				case CONST ->{
					
					if (!started) res = (int) currentOp.constant();
					else res = opStack.pop().handle(res, (int)currentOp.constant());
					
				}			
					
				case OPAREN -> {
					
					Tuple2<Integer , Integer> parenRes = at(i + 1 , variables);
					if(!started) res = parenRes.getSecond(); 
					else res = opStack.pop().handle(res , parenRes.getSecond());					
					i = parenRes.getFirst();
					
				}
				
				case CPAREN -> {
					
					return new Tuple2<>(i , res);				
				
				}
				
				default -> opStack.push(currentOp.type); 
			
			}

			started = true;
			
		}
		
		return new Tuple2<>(-1 , res);
		
	}

	private Tuple2<Integer , Integer> atInts(int startAt) {
			
		int res = 0;
		boolean started = false;
		final Stack<OperatorType> opStack = new Stack<OperatorType>();
		operator currentOp;	
		for(int i = startAt ; i < function.size() ; i ++) {
			
			currentOp = function.get(i);
										
			switch(currentOp.type) {
				
				case VAR ->{
					
					if(!started) res = (currentOp.var().intValue());
					else res = opStack.pop().handle(res, currentOp.var().intValue());			
										
				}
				
				case CONST ->{
					
					if (!started) res = (int) currentOp.constant();
					else res = opStack.pop().handle(res, (int)currentOp.constant());
					
				}			
					
				case OPAREN -> {
					
					Tuple2<Integer , Integer> parenRes = atInts(i + 1);
					if(!started) res = parenRes.getSecond(); 
					else res = opStack.pop().handle(res , parenRes.getSecond());					
					i = parenRes.getFirst();
					
				}
				
				case CPAREN -> {
					
					return new Tuple2<>(i , res);				
				
				}
				
				
				default -> opStack.push(currentOp.type); 
			
			}

			started = true;
			
		}
		
		return new Tuple2<>(-1 , res);
		
	}
	
	/**
	 * Given a list of floats as variable values, get a float representation of evaluating this MFunction at those inputs.
	 * @param variables — list of values for the variables given at the initialization of this MFunction
	 * @return a float representation of this MFunction's output at the given inputs.
	 */
	public float at(float...variables) {

		if(CHECKS) 
			errorCheck("Invalid number of inputs given, " + numberVariables + " expected, " + variables.length + " given" , 
					variables.length != (numberVariables - floatGetters));
		
		float result = at(0 , variables).getSecond();
		if(finish != null) finish.accept(result);
		result = (float) (functionCoefficient * (reflect ? -(result + translateY) : result + translateY));
		if(stopInfinity && outOfBounds(result)) return 0f;
		else return result;
		
	}
	
	/**
	 * Use this method when there are no inputs needed for a certain type and it is desired to get a certain type as output.
	 * In this case, each variable must have a float getter.
	 * 
	 * @return a float representation of this MExpression at the given previously-set float variables.
	 */
	public float atFloats() {
	
		if(CHECKS) errorCheck("No inputs given but " + (numberVariables - floatGetters) + " expected." , numberVariables - floatGetters > 0);
		
		float result = atFloats(0).getSecond();
		if(finish != null) finish.accept(result);
		result = (float) (functionCoefficient * (reflect ? -(result + translateY) : result + translateY));
		if(stopInfinity && outOfBounds(result)) return 0f;
		else return result;
		
	}
	
	private Tuple2<Integer , Float> at(int startAt , float... variables) {
						
		float res = 0;
		boolean started = false;
		final Stack<OperatorType> opStack = new Stack<OperatorType>();
		operator currentOp;	
		for(int i = startAt ; i < function.size() ; i ++) {
			
			currentOp = function.get(i);
			
			switch(currentOp.type) {
				
				case VAR -> {
					
					if(!started) res = currentOp.var().floatValue(variables);
					else res = opStack.pop().handle(res, currentOp.var().floatValue(variables));
										
				}
				
				case CONST ->{
					
					if (!started) res = (float)currentOp.constant();
					else res = opStack.pop().handle(res, (float)currentOp.constant());
					
				}			
					
				case OPAREN -> {
					
					Tuple2<Integer , Float> parenRes = at(i + 1 , variables);
					if(!started) res = parenRes.getSecond(); 
					else res = opStack.pop().handle(res , parenRes.getSecond());
					i = parenRes.getFirst();
					
				}
				
				case CPAREN -> {
					
					return new Tuple2<>(i , res);				
				
				}
				
				default -> opStack.push(currentOp.type); 
			
			}

			started = true;
			
		}
		
		return new Tuple2<>(-1 , res);
		
	}

	private Tuple2<Integer , Float> atFloats(int startAt) {
						
		float res = 0;
		boolean started = false;
		final Stack<OperatorType> opStack = new Stack<OperatorType>();
		operator currentOp;	
		for(int i = startAt ; i < function.size() ; i ++) {
			
			currentOp = function.get(i);
			
			switch(currentOp.type) {
				
				case VAR -> {
					
					if(!started) res = currentOp.var().floatValue();
					else res = opStack.pop().handle(res, currentOp.var().floatValue());				
										
				}
				
				case CONST ->{
					
					if (!started) res = (float)currentOp.constant();
					else res = opStack.pop().handle(res, (float)currentOp.constant());
					
				}			
					
				case OPAREN -> {
					
					Tuple2<Integer , Float> parenRes = atFloats(i + 1);
					if(!started) res = parenRes.getSecond(); 
					else res = opStack.pop().handle(res , parenRes.getSecond());					
					i = parenRes.getFirst();
					
				}
				
				case CPAREN -> {
					
					return new Tuple2<>(i , res);				
				
				}
				
				default -> opStack.push(currentOp.type); 
			
			}

			started = true;
			
		}
		
		return new Tuple2<>(-1 , res);
		
	}
	
	/**
	 * Given a list of longs as variable values, get a long representation of evaluating this MFunction at those inputs.
	 * @param variables — list of values for the variables given at the initialization of this MFunction
	 * @return a long representation of this MFunction's output at the given inputs.
	 */
	public long at(long...variables) {

		if(CHECKS) 
			errorCheck("Invalid number of inputs given, " + numberVariables + " expected, " + variables.length + " given" , 
					variables.length != (numberVariables - longGetters));
		
		long result = at(0 , variables).getSecond();
		if(finish != null) finish.accept(result);
		result = (long) (functionCoefficient * (reflect ? -(result + translateY) : result + translateY));
		if(stopInfinity && outOfBounds(result)) return 0l;
		else return result;
				
	}
	
	/**
	 * Use this method when there are no inputs needed for a certain type and it is desired to get a certain type as output.
	 * In this case, each variable must have a long getter.
	 * 
	 * @return a float representation of this MExpression at the given previously-set long variables.
	 */
	public long atLongs() {

		if(CHECKS) errorCheck("No inputs given but " + (numberVariables - longGetters) + " expected." , numberVariables - longGetters > 0);
		
		long result = atLongs(0).getSecond();
		if(finish != null) finish.accept(result);
		result = (long) (functionCoefficient * (reflect ? -(result + translateY) : result + translateY));
		if(stopInfinity && outOfBounds(result)) return 0l;
		else return result;
				
	}
	
	private Tuple2<Integer , Long> at(int startAt , long... variables) {
			
		long res = 0;
		boolean started = false;
		final Stack<OperatorType> opStack = new Stack<OperatorType>();
		operator currentOp;	
		for(int i = startAt ; i < function.size() ; i ++) {
			
			currentOp = function.get(i);

			switch(currentOp.type) {
				
				case VAR ->{
					
					if(!started) res = currentOp.var().longValue(variables);
					else res = opStack.pop().handle(res, currentOp.var().longValue(variables));				
										
				}
				
				case CONST ->{
					
					if (!started) res = (long) currentOp.constant();
					else res = opStack.pop().handle(res, (long)currentOp.constant());
					
				}			
					
				case OPAREN -> {
					
					Tuple2<Integer , Long> parenRes = at(i + 1 , variables);
					if(!started) res = parenRes.getSecond(); 
					else res = opStack.pop().handle(res , parenRes.getSecond());					
					i = parenRes.getFirst();
					
				}
				
				case CPAREN -> {
					
					return new Tuple2<>(i , res);				
				
				}
				
				default -> opStack.push(currentOp.type); 
			
			}

			started = true;
			
		}
		
		return new Tuple2<>(-1 , res);
		
	}

	private Tuple2<Integer , Long> atLongs(int startAt) {
			
		long res = 0;
		boolean started = false;
		final Stack<OperatorType> opStack = new Stack<OperatorType>();
		operator currentOp;	
		for(int i = startAt ; i < function.size() ; i ++) {
			
			currentOp = function.get(i);

			switch(currentOp.type) {
				
				case VAR ->{
					
					if(!started) res = currentOp.var().longValue();
					else res = opStack.pop().handle(res, currentOp.var().longValue());				
										
				}
				
				case CONST ->{
					
					if (!started) res = (long) currentOp.constant();
					else res = opStack.pop().handle(res, (long)currentOp.constant());
					
				}			
					
				case OPAREN -> {
					
					Tuple2<Integer , Long> parenRes = atLongs(i + 1);
					if(!started) res = parenRes.getSecond(); 
					else res = opStack.pop().handle(res , parenRes.getSecond());					
					i = parenRes.getFirst();
					
				}
				
				case CPAREN -> {
					
					return new Tuple2<>(i , res);				
				
				}
				
				default -> opStack.push(currentOp.type); 
			
			}

			started = true;
			
		}
		
		return new Tuple2<>(-1 , res);
		
	}
	
	private String variablePosString(operator x) {
		
		String res = x.var().name + " positions: int -> ";
		if(x.var().intGetter != null) res += "set, float -> ";
		else res += x.var.intPosition + ", float -> ";
		
		if(x.var.floatGetter != null) res += "set, long -> ";
		else res += x.var.floatPosition + ", long -> ";
				
		if(x.var.longGetter != null) res += "set, double -> ";
		else res += x.var.longPosition + ", double -> ";
		
		if(x.var.doubleGetter != null) res += "set\n";
		else res += x.var.doublePosition + "\n";

		return res;
		
	}
	
	public String toString() {
		
		String res = "";
		
		OperatorType op;
		for(int i = 0 ; i < function.size() ; i ++) {
			
			op = function.get(i).type;			
			res += op.toString();
			if(op == OperatorType.VAR) res += ": " +  function.get(i).var().name + " ";
			else if (op == OperatorType.CONST) res += ": " + function.get(i).constant() + " ";
			else res += " ";
			
		}
				
		res += "\n";
		
		for(operator x : function) if(x.isVar()) {
			
			res += variablePosString(x);
			
		}
		
		return res;
		
	}

	/**
	 * Given a string representation of a number this returns a double representation of that string. 
	 * 
	 * @param number
	 * @return
	 * @throws NumberFormatException
	 */
	public static final double toNumber(String number) throws NumberFormatException {
		
		double res = 0;
		char[] chars = number.toCharArray();
		int end = chars[0] == '-' ? 1 : 0;
		byte digit;
		for(int i = chars.length -1, tensPlace = 1 ; i >= end ; i --) {
			
			if (chars[i] == '.') {
				
				res /= tensPlace;
				tensPlace = 1;
				continue;
				
			}  
			
			else if ((digit = charDigit(chars[i])) == '0') ;//do nothing in this case
			else res += digit * tensPlace;
			
			//bit shifting to multiply by 10 each time. after this, tensplace will be 10 times larger than previous
			tensPlace = (tensPlace << 3) + (tensPlace << 1);
			
		}
		
		if(chars[0] == '-') res *= -1;		
		return res;
		
	}
	
	public static final byte charDigit(char toDigit) throws NumberFormatException {
		
		switch(toDigit) {
		
			case '0' : return 0;
			case '1' : return 1;
			case '2' : return 2;
			case '3' : return 3;
			case '4' : return 4;
			case '5' : return 5;
			case '6' : return 6;
			case '7' : return 7;
			case '8' : return 8;
			case '9' : return 9;
			default: throw new NumberFormatException(toDigit + " is not a valid digit");
			
		}
		
	}
	
	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |				Symmetry Functions					|
     * |													|
     * |													|
     * —ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë—
     *
     */
	
	/**
	 * Evaluates function evenness. True iff f(x) == f(-x). 
	 * 
	 * @param inputs — inputs as ints
	 * @return true if this function is even
	 */
	public boolean isEven(int... inputs) {
	
		int evenRes = at(inputs);
		for(int i = 0 ; i < inputs.length ; i ++) inputs[i] = -inputs[i];
		return evenRes == at(inputs);
		
	}
	
	/**
	 * Evaluates function evenness. True iff f(x) == f(-x). 
	 * 
	 * @param inputs — inputs as longs
	 * @return true if this function is even
	 */
	public boolean isEven(long... inputs) {

		long evenRes = at(inputs);
		for(int i = 0 ; i < inputs.length ; i ++) inputs[i] = -inputs[i];
		return evenRes == at(inputs);
		
	}
	
	/**
	 * Evaluates function evenness. True iff f(x) == f(-x). 
	 * 
	 * @param inputs — inputs as floats
	 * @return true if this function is even
	 */
	public boolean isEven(float... inputs) {

		float evenRes = at(inputs);
		for(int i = 0 ; i < inputs.length ; i ++) inputs[i] = -inputs[i];
		return evenRes == at(inputs);
		
	}

	/**
	 * Evaluates function evenness. True iff f(x) == f(-x). 
	 * 
	 * @param inputs — inputs as doubles
	 * @return true if this function is even
	 */
	public boolean isEven(double... inputs) {

		double evenRes = at(inputs);
		for(int i = 0 ; i < inputs.length ; i ++) inputs[i] = -inputs[i];
		return evenRes == at(inputs);
		
	}

	/**
	 * Evaluates function oddness. True iff f(x) == -f(-x). 
	 * 
	 * @param inputs — inputs as ints
	 * @return true if this function is odd
	 */
	public boolean isOdd(int... inputs) {

		int evenRes = at(inputs);
		for(int i = 0 ; i < inputs.length ; i ++) inputs[i] = -inputs[i];
		return evenRes == -at(inputs);
		
	}

	/**
	 * Evaluates function oddness. True iff f(x) == -f(-x). 
	 * 
	 * @param inputs — inputs as longs
	 * @return true if this function is odd
	 */
	public boolean isOdd(long... inputs) {

		long evenRes = at(inputs);
		for(int i = 0 ; i < inputs.length ; i ++) inputs[i] = -inputs[i];
		return evenRes == -at(inputs);
		
	}

	/**
	 * Evaluates function oddness. True iff f(x) == -f(-x). 
	 * 
	 * @param inputs — inputs as floats
	 * @return true if this function is odd
	 */
	public boolean isOdd(float... inputs) {

		float evenRes = at(inputs);
		for(int i = 0 ; i < inputs.length ; i ++) inputs[i] = -inputs[i];
		return evenRes == -at(inputs);
		
	}

	/**
	 * Evaluates function oddness. True iff f(x) == -f(-x). 
	 * 
	 * @param inputs — inputs as doubles
	 * @return true if this function is odd
	 */
	public boolean isOdd(double... inputs) {

		double evenRes = at(inputs);
		for(int i = 0 ; i < inputs.length ; i ++) inputs[i] = -inputs[i];
		return evenRes == -at(inputs);
		
	}
		
	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |					One to Oneness					|
     * |													|
     * |													|
     * —ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë—
     *
     */

	/**
	 * Typically, one to oneness can be determined at construction based on what operators are present which are either generally
	 * one to one or not one to one. However, in the special case of the power operator, one to oneness is determined by the right hand
	 * operand. 
	 * <br><br> 
	 * If this function has a power operator "^", depending upon what is on the right hand side, the one to oneness may change.
	 * This method determines whether the inputs given change the one to oneness of the function.
	 * 
	 * @param inputs — variable inputs as ints
	 * @return true if this function is possibly one to one, and is one to one at the given inputs.
	 */
	public boolean computeOneToOneness(int...inputs) {

		if(!possiblyOneToOne) return false;
		int i = 0;
		boolean eval = true;
		for(operator x : function) {
			
			if(x.type == OperatorType.POW) {
				
				operator next = this.function.get(i + 1);
				if(next.type == OperatorType.CONST && next.constant % 2 == 0) {
					
					eval = false;
					break;
					
				} else if (next.isVar() && inputs[next.var.intPosition] % 2 == 0) {
					
					eval = false;
					break;
					
				}
				
				else if (next.type == OperatorType.OPAREN) {
					
					int res = at(i + 2 , inputs).getSecond();
					if(res % 2 == 0) {
						
						eval = false;
						break;
						
					}
					
				}
				
			}
			
			i ++;
			
		}
		
		return eval;
		
	}
	
	/**
	 * Typically, one to oneness can be determined at construction based on what operators are present which are either generally
	 * one to one or not one to one. However, in the special case of the power operator, one to oneness is determined by the right hand
	 * operand. 
	 * <br><br> 
	 * If this function has a power operator "^", depending upon what is on the right hand side, the one to oneness may change.
	 * This method determines whether the inputs given change the one to oneness of the function.
	 * 
	 * @param inputs — variable inputs as longs
	 * @return true if this function is possibly one to one, and is one to one at the given inputs.
	 */
	public boolean computeOneToOneness(long...inputs) {

		if(!possiblyOneToOne) return false;
		
		int i = 0;
		boolean eval = true;
		for(operator x : function) {
			
			if(x.type == OperatorType.POW) {
				
				operator next = this.function.get(i + 1);
				if(next.type == OperatorType.CONST && next.constant % 2 == 0) {
					
					eval = false;
					break;
					
				} else if (next.isVar() && inputs[next.var.longPosition] % 2 == 0) {
					
					eval = false;
					break;
					
				}
				
				else if (next.type == OperatorType.OPAREN) {
					
					long res = at(i + 2 , inputs).getSecond();
					if(res % 2 == 0) {
						
						eval = false;
						break;
						
					}
					
				}
				
			}
			
			i ++;
			
		}
		
		return eval;
		
	}

	/**
	 * Typically, one to oneness can be determined at construction based on what operators are present which are either generally
	 * one to one or not one to one. However, in the special case of the power operator, one to oneness is determined by the right hand
	 * operand. 
	 * <br><br> 
	 * If this function has a power operator "^", depending upon what is on the right hand side, the one to oneness may change.
	 * This method determines whether the inputs given change the one to oneness of the function.
	 * 
	 * @param inputs — variable inputs as floats
	 * @return true if this function is possibly one to one, and is one to one at the given inputs.
	 */
	public boolean computeOneToOneness(float...inputs) {

		if(!possiblyOneToOne) return false;
		
		int i = 0;
		boolean eval = true;
		for(operator x : function) {
			
			if(x.type == OperatorType.POW) {
				
				operator next = this.function.get(i + 1);
				if(next.type == OperatorType.CONST && next.constant % 2 == 0) {
					
					eval = false;
					break;
					
				} else if (next.isVar() && inputs[next.var.floatPosition] % 2 == 0) {
					
					eval = false;
					break;
					
				}
				
				else if (next.type == OperatorType.OPAREN) {
					
					float res = at(i + 2 , inputs).getSecond();
					if(res % 2 == 0) {
						
						eval = false;
						break;
						
					}
					
				}
				
			}
			
			i ++;
			
		}
		
		return eval;
		
	}

	/**
	 * Typically, one to oneness can be determined at construction based on what operators are present which are either generally
	 * one to one or not one to one. However, in the special case of the power operator, one to oneness is determined by the right hand
	 * operand. 
	 * <br><br> 
	 * If this function has a power operator "^", depending upon what is on the right hand side, the one to oneness may change.
	 * This method determines whether the inputs given change the one to oneness of the function.
	 * 
	 * @param inputs — variable inputs as doubles
	 * @return true if this function is possibly one to one, and is one to one at the given inputs.
	 */
	public boolean computeOneToOneness(double...inputs) {

		if(!possiblyOneToOne) return false;
		
		int i = 0;
		boolean eval = true;
		for(operator x : function) {
			
			if(x.type == OperatorType.POW) {
				
				operator next = this.function.get(i + 1);
				if(next.type == OperatorType.CONST && next.constant % 2 == 0) {
					
					eval = false;
					break;
					
				} else if (next.isVar() && inputs[next.var.doublePosition] % 2 == 0) {
					
					eval = false;
					break;
					
				}
				
				else if (next.type == OperatorType.OPAREN) {
					
					double res = at(i + 2 , inputs).getSecond();
					if(res % 2 == 0) {
						
						eval = false;
						break;
						
					}
					
				}
				
			}
			
			i ++;
			
		}
		
		return eval;
		
	}
	
	/**
	 * A function is possibly one to one if its operators are one to one. Presence of non-one to one operators will cause this
	 * method to return false.
	 * <br><br>
	 * However, with respect to exponential operations, they can be one to one sometimes and not others. If the POW operator is found,
	 * and the right hand side operand is an odd constant, one to oneness is not violated. However, if it cannot
	 * be determined whether the right hand side is odd, the function is considered not one to one.
	 * <br><br>
	 * Call {@code computeOneToOneness} with inputs for variables to determine whether the power operator at some variables is actually 
	 * one to one or not, but if this method returns false, that one will too, meaning if it is not possible for this function
	 * to be one to one, then it cannot be one to one even if it's power operators are odd, making them one to one.
	 * 
	 * @return true if this function is possibly one to one, else false
	 */
	public boolean possiblyOneToOne() {
		
		return possiblyOneToOne ;
		
	}
	
	/**
	 * A function is strongly one to one if in addition to being generally one to one, as defined by {@code possiblyOneToOne}, it 
	 * will be one to one irrespective of its inputs. That is to say, if power operators are absent, or the right hand side of the power
	 * operator is odd.
	 * 
	 * @return true if this function contains only operations that are always one to one.
	 */
	public boolean stronglyOneToOne() {
		
		return possiblyOneToOne && alwaysOneToOne;
		
	}
	
	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |				Invserse Functions					|
     * |													|
     * |													|
     * —ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë—
     *
     */
	
	/**
	 * returns whether a function is an inverse of another function. A function is an inverse of another iff
	 * {@code f o g (x) == g o f (x)}. As such, only one input is given for this function.
	 * 
	 * @param other — another MFunction
	 * @param input — an int input
	 * @return true if other is an inverse of this
	 */
	public boolean isInverseOf(MExpression other , int input) {
		
		return compose(other , this , input) == compose(this , other , input);
		
	}
	
	/**
	 * returns whether a function is an inverse of another function. A function is an inverse of another iff
	 * {@code f o g (x) == g o f (x)}. As such, only one input is given for this function.
	 * 
	 * @param other — another MFunction
	 * @param input — an int input
	 * @return true if other is an inverse of this
	 */
	public boolean isInverseOf(MExpression other , float input) {
		
		return compose(other , this , input) == compose(this , other , input);
		
	}

	/**
	 * returns whether a function is an inverse of another function. A function is an inverse of another iff
	 * {@code f o g (x) == g o f (x)}. As such, only one input is given for this function.
	 * 
	 * @param other — another MFunction
	 * @param input — an int input
	 * @return true if other is an inverse of this
	 */
	public boolean isInverseOf(MExpression other , long input) {
		
		return compose(other , this , input) == compose(this , other , input);
		
	}

	/**
	 * returns whether a function is an inverse of another function. A function is an inverse of another iff
	 * {@code f o g (x) == g o f (x)}. As such, only one input is given for this function.
	 * 
	 * @param other — another MFunction
	 * @param input — an int input
	 * @return true if other is an inverse of this
	 */
	public boolean isInverseOf(MExpression other , double input) {
		
		return compose(other , this , input) == compose(this , other , input);
		
	}

	public MExpression getInverse() {
		
		//TODO
		return null;
		
	}
	
	
	/*
     * ______________________________________________________
     * |													|
     * |													|
     * |				Operations on Functions				|
     * |													|
     * |													|
     * —ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë——ë—
     *
     */
	
	/**
	 * Performs a standard addition of two functions
	 * 
	 * @param f — a function addend
	 * @param g — a function addend
	 * @param inputs — input as ints
	 * @return {@code f.at(inputs) + g.at(inputs}
	 */
	public static final int add(MExpression f , MExpression g , int...inputs) {
		
		if(CHECKS) errorCheck("Functions provided to MFunction add do not take in equal number of inputs" , f.numberVariables != g.numberVariables);
		return f.at(inputs) + g.at(inputs);
		
	}
	
	/**
	 * Performs a standard addition of two functions
	 * 
	 * @param f — a function addend
	 * @param g — a function addend
	 * @param inputs — input as longs
	 * @return {@code f.at(inputs) + g.at(inputs}
	 */
	public static final long add(MExpression f , MExpression g , long...inputs) {
		
		if(CHECKS) errorCheck("Functions provided to MFunction add do not take in equal number of inputs" , f.numberVariables != g.numberVariables);
		return f.at(inputs) + g.at(inputs);
		
	}
	
	/**
	 * Performs a standard addition of two functions
	 * 
	 * @param f — a function addend
	 * @param g — a function addend
	 * @param inputs — input as floats
	 * @return {@code f.at(inputs) + g.at(inputs}
	 */
	public static final float add(MExpression f, MExpression g , float...inputs) {
		
		if(CHECKS) errorCheck("Functions provided to MFunction add do not take in equal number of inputs" , f.numberVariables != g.numberVariables);
		return f.at(inputs) + g.at(inputs);
		
	}
	
	/**
	 * Performs a standard addition of two functions
	 * 
	 * @param f — a function addend
	 * @param g — a function addend
	 * @param inputs — input as doubles
	 * @return {@code f.at(inputs) + g.at(inputs}
	 */
	public static final double add(MExpression f, MExpression g , double...inputs) {
		
		if(CHECKS) errorCheck("Functions provided to MFunction add do not take in equal number of inputs" , f.numberVariables != g.numberVariables);
		return f.at(inputs) + g.at(inputs);
		
	}	
	
	/**
	 * Performs a standard subtraction of two functions
	 * 
	 * @param f — a function minuend
	 * @param g — a function subtrahend
	 * @param inputs — input as ints
	 * @return {@code f.at(inputs) - g.at(inputs}
	 */
	public static final int sub(MExpression f , MExpression g , int...inputs) {
		
		if(CHECKS) errorCheck("Functions provided to MFunction sub do not take in equal number of inputs" , f.numberVariables != g.numberVariables);
		return f.at(inputs) - g.at(inputs);
		
	}
	
	/**
	 * Performs a standard subtraction of two functions
	 * 
	 * @param f — a function minuend
	 * @param g — a function subtrahend
	 * @param inputs — input as longs
	 * @return {@code f.at(inputs) - g.at(inputs}
	 */
	public static final long sub(MExpression f , MExpression g , long...inputs) {
		
		if(CHECKS) errorCheck("Functions provided to MFunction sub do not take in equal number of inputs" , f.numberVariables != g.numberVariables);
		return f.at(inputs) - g.at(inputs);
		
	}
	
	/**
	 * Performs a standard subtraction of two functions
	 * 
	 * @param f — a function minuend
	 * @param g — a function subtrahend
	 * @param inputs — input as floats
	 * @return {@code f.at(inputs) - g.at(inputs}
	 */
	public static final float sub(MExpression f , MExpression g , float...inputs) {
		
		if(CHECKS) errorCheck("Functions provided to MFunction sub do not take in equal number of inputs" , f.numberVariables != g.numberVariables);
		return f.at(inputs) - g.at(inputs);
		
	}
	
	/**
	 * Performs a standard subtraction of two functions
	 * 
	 * @param f — a function minuend
	 * @param g — a function subtrahend
	 * @param inputs — input as doubles
	 * @return {@code f.at(inputs) - g.at(inputs}
	 */
	public static final double sub(MExpression f , MExpression g , double...inputs) {
		
		if(CHECKS) errorCheck("Functions provided to MFunction sub do not take in equal number of inputs" , f.numberVariables != g.numberVariables);
		return f.at(inputs) - g.at(inputs);
		
	}
		
	/**
	 * Performs a standard multiplication of two functions
	 * 
	 * @param f — a function multiplicand
	 * @param g — a function multiplicand
	 * @param inputs — input as long
	 * @return {@code f.at(inputs) * g.at(inputs}
	 */
	public static final long mul(MExpression f , MExpression g , long...inputs) {
		
		if(CHECKS) errorCheck("Functions provided to MFunction mul do not take in equal number of inputs" , f.numberVariables != g.numberVariables);
		return f.at(inputs) * g.at(inputs);
		
	}
	
	/**
	 * Performs a standard multiplication of two functions
	 * 
	 * @param f — a function multiplicand
	 * @param g — a function multiplicand
	 * @param inputs — input as floats
	 * @return {@code f.at(inputs) * g.at(inputs}
	 */
	public static final float mul(MExpression f , MExpression g , float...inputs) {
		
		if(CHECKS) errorCheck("Functions provided to MFunction mul do not take in equal number of inputs" , f.numberVariables != g.numberVariables);
		return f.at(inputs) * g.at(inputs);
		
	}
	
	/**
	 * Performs a standard multiplication of two functions
	 * 
	 * @param f — a function multiplicand
	 * @param g — a function multiplicand
	 * @param inputs — input as ints
	 * @return {@code f.at(inputs) * g.at(inputs}
	 */
	public static final int mul(MExpression f , MExpression g , int...inputs) {
		
		if(CHECKS) errorCheck("Functions provided to MFunction mul do not take in equal number of inputs" , f.numberVariables != g.numberVariables);
		return f.at(inputs) * g.at(inputs);
		
	}

	/**
	 * Performs a standard multiplication of two functions
	 * 
	 * @param f — a function multiplicand
	 * @param g — a function multiplicand
	 * @param inputs — input as doubles
	 * @return {@code f.at(inputs) * g.at(inputs}
	 */
	public static final double mul(MExpression f , MExpression g , double...inputs) {
		
		if(CHECKS) errorCheck("Functions provided to MFunction mul do not take in equal number of inputs" , f.numberVariables != g.numberVariables);
		return f.at(inputs) * g.at(inputs);
		
	}
	
	/**
	 * Performs a standard division of two functions
	 * 
	 * @param f — a function dividend
	 * @param g — a function divisor
	 * @param inputs — input as ints
	 * @return {@code f.at(inputs) / g.at(inputs}
	 */
	public static final int div(MExpression f , MExpression g , int...inputs) {
		
		if(CHECKS) errorCheck("Functions provided to MFunction div do not take in equal number of inputs" , f.numberVariables != g.numberVariables);
		return f.at(inputs) / g.at(inputs);
		
	}
	
	/**
	 * Performs a standard division of two functions
	 * 
	 * @param f — a function dividend
	 * @param g — a function divisor
	 * @param inputs — input as longs
	 * @return {@code f.at(inputs) / g.at(inputs}
	 */
	public static final long div(MExpression f , MExpression g , long...inputs) {
		
		if(CHECKS) errorCheck("Functions provided to MFunction div do not take in equal number of inputs" , f.numberVariables != g.numberVariables);
		return f.at(inputs) / g.at(inputs);
		
	}

	
	/**
	 * Performs a standard division of two functions
	 * 
	 * @param f — a function dividend
	 * @param g — a function divisor
	 * @param inputs — input as floats
	 * @return {@code f.at(inputs) / g.at(inputs}
	 */
	public static final float div(MExpression f , MExpression g , float...inputs) {
		
		if(CHECKS) errorCheck("Functions provided to MFunction div do not take in equal number of inputs" , f.numberVariables != g.numberVariables);
		return f.at(inputs) / g.at(inputs);
		
	}
	
	/**
	 * Performs a standard division of two functions
	 * 
	 * @param f — a function dividend
	 * @param g — a function divisor
	 * @param inputs — input as doubles
	 * @return {@code f.at(inputs) / g.at(inputs}
	 */
	public static final double div(MExpression f , MExpression g , double...inputs) {
		
		if(CHECKS) errorCheck("Functions provided to MFunction div do not take in equal number of inputs" , f.numberVariables != g.numberVariables);
		return f.at(inputs) / g.at(inputs);
		
	}
	
	
	/**
	 * Performs a standard composition of two functions
	 * 
	 * @param f — a function composed of another
	 * @param g — a function passed to f
	 * @param inputs — input as ints
	 * @return {@code f.at(g.at(input))}
	 */
	public static final int compose(MExpression f , MExpression g , int input) {
		
		if(CHECKS) errorCheck("Functions provided to MFunction compose do not take one argument. " , f.numberVariables > 1 || g.numberVariables > 1);
		return f.at(new int[] {g.at(new int[] {input})});
		
	}
	
	/**
	 * Performs a standard composition of two functions
	 * 
	 * @param f — a function composed of another
	 * @param g — a function passed to f
	 * @param inputs — input as longs
	 * @return {@code f.at(g.at(input))}
	 */
	public static final long compose(MExpression f , MExpression g , long input) {
		
		if(CHECKS) errorCheck("Functions provided to MFunction compose do not take one argument. " , f.numberVariables > 1 || g.numberVariables > 1);
		return f.at(new long[] {g.at(new long[] {input})});
		
	}

	/**
	 * Performs a standard composition of two functions
	 * 
	 * @param f — a function composed of another
	 * @param g — a function passed to f
	 * @param inputs — input as floats
	 * @return {@code f.at(g.at(input))}
	 */
	public static final float compose(MExpression f , MExpression g , float input) {
		
		if(CHECKS) errorCheck("Functions provided to MFunction compose do not take one argument. " , f.numberVariables > 1 || g.numberVariables > 1);
		return f.at(new float[] {g.at(new float[] {input})});
		
	}

	/**
	 * Performs a standard composition of two functions
	 * 
	 * @param f — a function composed of another
	 * @param g — a function passed to f
	 * @param inputs — input as doubles
	 * @return {@code f.at(g.at(input))}
	 */
	public static final double compose(MExpression f , MExpression g , double input) {
		
		if(CHECKS) errorCheck("Functions provided to MFunction compose do not take one argument. " , f.numberVariables > 1 || g.numberVariables > 1);
		return f.at(new double[] {g.at(new double[] {input})});
		
	}
	
	public static final double roundDouble(double round) {

		double dec = round % 1;
		int addend =  dec >= 0.5 ? 1 :0;
		return (long)round + addend;
		
	}
	
	public static final int logBS(int base , int operand) {
		
		return (int) (Math.log(operand) / Math.log(base));
		
	}
	
	public static final float logBS(float base , float operand) {
		
		return (float) (Math.log(operand) / Math.log(base));
		
	}
	
	public static final double logBS(double base , double operand) {
		
		return Math.log(operand) / Math.log(base);
		
	}
	
	public static final long logBS(long base , long operand) {
		
		return (long) (Math.log(operand) / Math.log(base));
		
	}
	
	public static final int arcSinh(int operand) {
		
		return (int) Math.log(operand + (Math.sqrt((Math.pow(operand, 2)) + 1)));
		
	}
	
	public static final long arcSinh(long operand) {
		
		return (long) Math.log(operand + (Math.sqrt((Math.pow(operand, 2)) + 1)));
		
	}
	
	public static final float arcSinh(float operand) {
		
		return (float) Math.log(operand + (Math.sqrt((Math.pow(operand, 2)) + 1)));
		
	}
	
	public static final double arcSinh(double operand) {
		
		return Math.log(operand + (Math.sqrt((Math.pow(operand, 2)) + 1)));
		
	}
	
	public static final int arcCosh(int operand) {
		
		return (int) Math.log(operand + (Math.sqrt((Math.pow(operand, 2)) - 1)));
		
	}
	
	public static final long arcCosh(long operand) {
		
		return (long) Math.log(operand + (Math.sqrt((Math.pow(operand, 2)) - 1)));
		
	}
	
	public static final float arcCosh(float operand) {
		
		return (float) Math.log(operand + (Math.sqrt((Math.pow(operand, 2)) - 1)));
		
	}
	
	public static final double arcCosh(double operand) {
		
		return Math.log(operand + (Math.sqrt((Math.pow(operand, 2)) - 1)));
		
	}
	
	public static final int arcTanh(int operand) {
		
		return (int) (Math.log((1 + operand) / (1 - operand))) / 2;
		
	}
	
	public static final long arcTanh(long operand) {
		
		return (long) (Math.log((1 + operand) / (1 - operand))) / 2;
		
	}
	
	public static final float arcTanh(float operand) {
		
		return (float) (Math.log((1 + operand) / (1 - operand))) / 2;
		
	}
	
	public static final double arcTanh(double operand) {
		
		return (Math.log((1 + operand) / (1 - operand))) / 2;
		
	}
	
	public static final int arcCotanh(int operand) {
		
		return (int) Math.log((1 + (1 / operand)) / (1 - (1 / operand))) / 2;
		
	}
	
	public static final long arcCotanh(long operand) {
		
		return (long) Math.log((1 + (1 / operand)) / (1 - (1 / operand))) / 2;
		
	}
	
	public static final float arcCotanh(float operand) {
		
		return (float) Math.log((1 + (1 / operand)) / (1 - (1 / operand))) / 2;
		
	}
	
	public static final double arcCotanh(double operand) {
		
		return Math.log((1 + (1 / operand)) / (1 - (1 / operand))) / 2;
		
	}
	
	public static final int arcCsch(int operand) {
		
		return (int) Math.log(Math.sqrt(1 + (1 / Math.pow(operand , 2))) + (1 / operand));
		
	}
	
	public static final long arcCsch(long operand) {
		
		return (long) Math.log(Math.sqrt(1 + (1 / Math.pow(operand , 2))) + (1 / operand));
		
	}

	public static final float arcCsch(float operand) {
		
		return (float) Math.log(Math.sqrt(1 + (1 / Math.pow(operand , 2))) + (1 / operand));
		
	}
	
	public static final double arcCsch(double operand) {
		
		return Math.log(Math.sqrt(1 + (1 / Math.pow(operand , 2))) + (1 / operand));
		
	}
	
	public static final int arcSech(int operand) {
		
		return (int) Math.log(Math.sqrt((1 / operand) - 1) * Math.sqrt(1 + (1 / operand)) + 1 / operand);
		
	}

	public static final long arcSech(long operand) {
		
		return (long) Math.log(Math.sqrt((1 / operand) - 1) * Math.sqrt(1 + (1 / operand)) + 1 / operand);
		
	}

	public static final float arcSech(float operand) {
		
		return (float) Math.log(Math.sqrt((1 / operand) - 1) * Math.sqrt(1 + (1 / operand)) + 1 / operand);
		
	}

	public static final double arcSech(double operand) {
		
		return Math.log(Math.sqrt((1 / operand) - 1) * Math.sqrt(1 + (1 / operand)) + 1 / operand);
		
	}

	private static final int randomlySignedInt(int bound) {
		
		if(outOfBounds(bound)) bound = 1;
		if(rng.nextBoolean()) return -rng.nextInt(bound);
		else return rng.nextInt(bound);
		
		
	}

	private static final float randomlySignedFloat(float bound) {
		
		if(outOfBounds(bound)) bound = 1;
		if(rng.nextBoolean()) return -rng.nextFloat(bound);
		else return rng.nextFloat(bound);
		
	}

	private static final long randomlySignedLong(long bound) {
		
		if(outOfBounds(bound)) bound = 1;
		if(rng.nextBoolean()) return -rng.nextLong(bound);
		else return rng.nextLong(bound);
		
	}

	private static final double randomlySignedDouble(double bound) {
		
		if(outOfBounds(bound)) bound = 1;
		if(rng.nextBoolean()) return -rng.nextDouble(bound);
		else return rng.nextDouble(bound);
		
	}

	private static boolean outOfBounds(double bound) {
	
		return bound == Double.NEGATIVE_INFINITY || bound == Double.POSITIVE_INFINITY;
		
	}
	
	private static boolean outOfBounds(float bound) {
		
		return bound == Float.NEGATIVE_INFINITY || bound == Float.POSITIVE_INFINITY;
		
	}
	
	private static void errorCheck(String complaint , boolean test) {

		if(test) {
			
			System.err.println(complaint);
			
			try {
				
				throw new AssertionError();
				
			} catch(AssertionError e) {
				
				e.printStackTrace();
				
			}
			
			if(ASSERTS) throw new AssertionError();
			
		}
		
	}
	
}